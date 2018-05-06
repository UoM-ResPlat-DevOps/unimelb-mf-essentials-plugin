package unimelb.mf.essentials.plugin.services;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import arc.archive.ArchiveOutput;
import arc.archive.ArchiveRegistry;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginTask;
import arc.mf.plugin.PluginThread;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.DateType;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.dtype.XmlDocType;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;
import unimelb.mf.essentials.plugin.download.AssetDownloadAtermUnixScriptWriter;
import unimelb.mf.essentials.plugin.download.AssetDownloadAtermWindowsScriptWriter;
import unimelb.mf.essentials.plugin.download.AssetDownloadShellScriptWriter;
import unimelb.mf.essentials.plugin.download.AssetDownloadShellUnixScriptWriter;
import unimelb.mf.essentials.plugin.download.AssetDownloadShellWindowsScriptWriter;
import unimelb.mf.essentials.plugin.util.ServerDetails;

public class SvcAssetDownloadScriptGenerate extends PluginService {

    public static enum ScriptType {
        SHELL, ATERM;

        @Override
        public String toString() {
            return name().toLowerCase();
        }

        public String tokenApp() {
            if (this == SHELL) {
                return AssetDownloadShellScriptWriter.TOKEN_APP;
                // TODO enable token app
// @formatter:off
//            } else if (this == ATERM) {
//                return AssetDownloadAtermScriptWriter.TOKEN_APP;
// @formatter:on
            } else {
                return null;
            }
        }

        public String scriptFileName(String ext) {
            StringBuilder sb = new StringBuilder();
            sb.append("asset-");
            sb.append(name().toLowerCase());
            sb.append("-download-");
            sb.append(new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()));
            if (ext != null) {
                sb.append(".").append(ext);
            }
            return sb.toString();
        }

        public static ScriptType fromString(String s) {
            if (s != null) {
                ScriptType[] vs = values();
                for (ScriptType v : vs) {
                    if (v.name().equalsIgnoreCase(s)) {
                        return v;
                    }
                }
            }
            return null;
        }

        public static String[] stringValues() {
            ScriptType[] vs = values();
            String[] svs = new String[vs.length];
            for (int i = 0; i < vs.length; i++) {
                svs[i] = vs[i].toString();
            }
            return svs;
        }
    }

    public static final String SERVICE_NAME = "unimelb.asset.download.script.generate";

    public static final int BATCH_SIZE = 10000;

    private Interface _defn;

    public SvcAssetDownloadScriptGenerate() {
        _defn = new Interface();

        _defn.add(new Interface.Element("namespace", StringType.DEFAULT, "The namespace to download.", 1,
                Integer.MAX_VALUE));

        /*
         * server
         */
        Interface.Element server = new Interface.Element("server", XmlDocType.DEFAULT,
                "Server details. If not specified, it will try auto-detecting from the current session.", 0, 1);

        server.add(new Interface.Element("host", StringType.DEFAULT, "server host address", 0, 1));

        server.add(new Interface.Element("port", new IntegerType(0, 65535), "server host address", 0, 1));

        server.add(new Interface.Element("transport", new EnumType(new String[] { "http", "https" }),
                "server transport: http or https?", 0, 1));

        _defn.add(server);

        /*
         * type
         */
        _defn.add(new Interface.Element("type", new EnumType(ScriptType.values()),
                "Type of the script. ATERM script requires Java Runtime.", 1, 1));

        /*
         * token
         */
        Interface.Element token = new Interface.Element("token", XmlDocType.DEFAULT, "Token specification.", 1, 1);
        token.add(new Interface.Element("from", DateType.DEFAULT,
                "A time, before which the token is not valid. If not supplied token is valid immediately.", 0, 1));
        token.add(new Interface.Element("to", DateType.DEFAULT,
                "A time, after which the token is no longer valid. If not supplied token will not expire.", 1, 1));
        token.add(new Interface.Element("use-count", IntegerType.POSITIVE_ONE,
                "The number of times the token may be used.", 0, 1));

        _defn.add(token);

    }

    @Override
    public Access access() {
        return ACCESS_MODIFY;
    }

    @Override
    public Interface definition() {
        return _defn;
    }

    @Override
    public String description() {
        return "Generate a download script to download the specified assets (collections).";
    }

    @Override
    public void execute(Element args, Inputs inputs, Outputs outputs, XmlWriter w) throws Throwable {

        final ScriptType scriptType = ScriptType.fromString(args.stringValue("type", ScriptType.ATERM.toString()));
        final String token = createToken(executor(), args.element("token"), scriptType);
        final ServerDetails serverDetails = resolveServerDetails(executor(), args.element("server"));
        final Collection<String> namespaces = args.values("namespace");

        PipedInputStream pis = new PipedInputStream();
        final PipedOutputStream pos = new PipedOutputStream(pis);

        PluginThread.executeAsync(SERVICE_NAME, new Runnable() {
            public void run() {
                try {
                    ArchiveOutput ao = ArchiveRegistry.createOutput(pos, "application/zip", 6, null);
                    try {
                        addWindowsScript(executor(), serverDetails, token, namespaces, scriptType, ao);
                        addUnixScript(executor(), serverDetails, token, namespaces, scriptType, ao);
                    } finally {
                        ao.close();
                        pos.close();
                    }
                } catch (Throwable e) {
                    // destroy token and rethrow
                    e.printStackTrace();
                }
            }

        });
        outputs.output(0).setData(pis, -1, "text/plain");
    }

    private static void addWindowsScript(ServiceExecutor executor, ServerDetails server, String token,
            Collection<String> namespaces, ScriptType scriptType, ArchiveOutput ao) throws Throwable {
        File tf = PluginTask.createTemporaryFile("asset-" + scriptType.toString() + "-download.cmd");
        try {
            OutputStream out = new BufferedOutputStream(new FileOutputStream(tf));
            try {
                if (scriptType == ScriptType.ATERM) {
                    AssetDownloadAtermWindowsScriptWriter w = new AssetDownloadAtermWindowsScriptWriter(server, token,
                            out);
                    try {
                        w.addNamespaces(namespaces);
                    } finally {
                        w.close();
                    }
                } else if (scriptType == ScriptType.SHELL) {
                    AssetDownloadShellWindowsScriptWriter w = new AssetDownloadShellWindowsScriptWriter(server, token,
                            out);
                    try {
                        w.addNamespaces(executor, namespaces);
                    } finally {
                        w.close();
                    }
                }
            } finally {
                out.close();
            }
            ao.add("text/plain", scriptType.scriptFileName("cmd"), tf);
        } finally {
            PluginTask.deleteTemporaryFile(tf);
        }
    }

    private static void addUnixScript(ServiceExecutor executor, ServerDetails server, String token,
            Collection<String> namespaces, ScriptType scriptType, ArchiveOutput ao) throws Throwable {
        File tf = PluginTask.createTemporaryFile("asset-" + scriptType.toString() + "-download.sh");
        try {
            OutputStream out = new BufferedOutputStream(new FileOutputStream(tf));
            try {
                if (scriptType == ScriptType.ATERM) {
                    AssetDownloadAtermUnixScriptWriter w = new AssetDownloadAtermUnixScriptWriter(server, token, out);
                    try {
                        w.addNamespaces(namespaces);
                    } finally {
                        w.close();
                    }
                } else if (scriptType == ScriptType.SHELL) {
                    AssetDownloadShellUnixScriptWriter w = new AssetDownloadShellUnixScriptWriter(server, token, out);
                    try {
                        w.addNamespaces(executor, namespaces);
                    } finally {
                        w.close();
                    }
                }
            } finally {
                out.close();
            }
            ao.add("text/plain", scriptType.scriptFileName("sh"), tf);
        } finally {
            PluginTask.deleteTemporaryFile(tf);
        }
    }

    private static ServerDetails resolveServerDetails(ServiceExecutor executor, Element se) throws Throwable {
        ServerDetails sd = ServerDetails.resolve(executor);
        if (se == null) {
            return sd;
        } else {
            String transport = se.value("transport");
            String host = se.value("host");
            int port = se.intValue("port", -1);
            if (transport == null) {
                transport = sd.transport();
            }
            if (host == null) {
                host = sd.host();
            }
            if (port < 0) {
                port = sd.port();
            }
            return new ServerDetails(transport, host, port);
        }
    }

    private static String createToken(ServiceExecutor executor, XmlDoc.Element te, ScriptType scriptType)
            throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        if (scriptType.tokenApp() != null) {
            dm.add("app", scriptType.tokenApp());
        }
        if (te != null && te.elementExists("from")) {
            dm.add(te.element("from"));
        }
        dm.add(te.element("to"));
        if (te != null && te.elementExists("use-count")) {
            dm.add(te.element("use-count"));
        }
        dm.add("role", new String[] { "type", "role" }, "user");
        dm.add("role", new String[] { "type", "user" }, executor.execute("actor.self.describe").value("actor/@name"));
        dm.add("tag", scriptType.name() + "_DOWNLOAD_SCRIPT");
        return executor.execute("secure.identity.token.create", dm.root()).value("token");
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

    @Override
    public int minNumberOfOutputs() {
        return 1;
    }

    @Override
    public int maxNumberOfOutputs() {
        return 1;
    }

}
