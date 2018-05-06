package unimelb.mf.essentials.plugin.services;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Collection;

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
import unimelb.mf.essentials.plugin.util.ServerDetails;

public abstract class SvcAssetDownloadScriptCreate extends PluginService {

    public static enum TargetOS {
        UNIX, WINDOWS;
        public String toString() {
            return name().toLowerCase();
        }

        public String appendScriptFileExtension(String scriptFile) {
            if (scriptFile == null) {
                return null;
            }
            if (this == WINDOWS) {
                if (scriptFile.toLowerCase().endsWith(".cmd") || scriptFile.toLowerCase().endsWith(".bat")) {
                    return scriptFile;
                } else {
                    return scriptFile + ".cmd";
                }
            } else if (this == UNIX) {
                if (scriptFile.toLowerCase().endsWith(".sh")) {
                    return scriptFile;
                } else {
                    return scriptFile + ".sh";
                }
            } else {
                return scriptFile;
            }
        }

        public static TargetOS fromString(String s) {
            if (s != null) {
                TargetOS[] vs = values();
                for (TargetOS v : vs) {
                    if (v.name().equalsIgnoreCase(s)) {
                        return v;
                    }
                }
            }
            return null;
        }
    }

    protected Interface defn;

    protected SvcAssetDownloadScriptCreate() {
        this.defn = new Interface();

        this.defn.add(new Interface.Element("name", StringType.DEFAULT, "The output script file name.", 0, 1));

        this.defn.add(new Interface.Element("where", StringType.DEFAULT, "The query to select the assets to download.",
                0, 1));

        this.defn.add(new Interface.Element("namespace", StringType.DEFAULT, "The asset namespace to download.", 0,
                Integer.MAX_VALUE));

        /*
         * target os
         */
        this.defn.add(new Interface.Element("target", new EnumType(TargetOS.values()),
                "The target operating system. If not specified, the scripts for both windows and unix are generated.",
                0, 1));

        /*
         * server
         */
        Interface.Element server = new Interface.Element("server", XmlDocType.DEFAULT,
                "Server details. If not specified, it will try auto-detecting from the current session.", 0, 1);

        server.add(new Interface.Element("host", StringType.DEFAULT, "server host address", 0, 1));

        server.add(new Interface.Element("port", new IntegerType(0, 65535), "server host address", 0, 1));

        server.add(new Interface.Element("transport", new EnumType(new String[] { "http", "https" }),
                "server transport: http or https?", 0, 1));
        this.defn.add(server);

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
        this.defn.add(token);
    }

    @Override
    public Access access() {
        return ACCESS_MODIFY;
    }

    @Override
    public Interface definition() {
        return this.defn;
    }

    @Override
    public void execute(Element args, Inputs inputs, Outputs outputs, XmlWriter w) throws Throwable {
        final String where = args.value("where");
        final Collection<String> namespaces = args.values("namespace");
        if (where == null && (namespaces == null || namespaces.isEmpty())) {
            throw new IllegalArgumentException("where or namespace argument must be specified.");
        }
        final ServerDetails serverDetails = resolveServerDetails(executor(), args.element("server"));
        final String token = createToken(executor(), args.element("token"), tokenApp(), tokenTag());
        final TargetOS target = TargetOS.fromString(args.value("target"));
        final String name = args.stringValue("name", defaultScriptFileName());

        PipedInputStream pis = new PipedInputStream();
        final PipedOutputStream pos = new PipedOutputStream(pis);
        PluginThread.executeAsync(name(), new Runnable() {

            public void run() {
                try {
                    ArchiveOutput ao = ArchiveRegistry.createOutput(pos, "application/zip", 6, null);
                    try {
                        if (target == null || target == TargetOS.UNIX) {
                            addScript(TargetOS.UNIX, name, serverDetails, token, where, namespaces, ao);
                        }
                        if (target == null || target == TargetOS.WINDOWS) {
                            addScript(TargetOS.WINDOWS, name, serverDetails, token, where, namespaces, ao);
                        }
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
        outputs.output(0).setData(pis, -1, "application/zip");
    }

    private void addScript(TargetOS target, String name, ServerDetails serverDetails, String token, String where,
            Collection<String> namespaces, ArchiveOutput ao) throws Throwable {
        String fileName = target.appendScriptFileExtension(name);
        File tf = PluginTask.createTemporaryFile(fileName);
        try {
            OutputStream out = new BufferedOutputStream(new FileOutputStream(tf));
            try {
                generateScript(target, serverDetails, token, where, namespaces, out);
            } finally {
                out.close();
            }
            ao.add("text/plain", fileName, tf);
        } finally {
            PluginTask.deleteTemporaryFile(tf);
        }
    }

    protected abstract void generateScript(TargetOS target, ServerDetails serverDetails, String token, String where,
            Collection<String> namespaces, OutputStream out) throws Throwable;

    protected String tokenApp() {
        return null;
    }

    protected String tokenTag() {
        return null;
    }

    protected abstract String defaultScriptFileName();

    private static String createToken(ServiceExecutor executor, XmlDoc.Element te, String tokenApp, String tokenTag)
            throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        if (tokenApp != null) {
            dm.add("app", tokenApp);
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
        if (tokenTag != null) {
            dm.add("tag", tokenTag);
        }
        return executor.execute("secure.identity.token.create", dm.root()).value("token");
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

    @Override
    public int minNumberOfOutputs() {
        return 1;
    }

    @Override
    public int maxNumberOfOutputs() {
        return 1;
    }

}
