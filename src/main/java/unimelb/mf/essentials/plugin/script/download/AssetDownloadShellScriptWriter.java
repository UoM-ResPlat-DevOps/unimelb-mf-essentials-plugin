package unimelb.mf.essentials.plugin.script.download;

import java.io.OutputStream;
import java.net.URI;
import java.util.Collection;
import java.util.List;

import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import unimelb.mf.essentials.plugin.script.ClientScriptWriter;
import unimelb.mf.essentials.plugin.services.SvcAssetDownloadScriptCreate.TargetOS;
import unimelb.mf.essentials.plugin.util.ServerDetails;
import unimelb.utils.MapUtils;
import unimelb.utils.PathUtils;
import unimelb.utils.URIBuilder;

public abstract class AssetDownloadShellScriptWriter extends ClientScriptWriter {

    public static final String SERVLET_PATH = "/mflux/content.mfjp";
    public static final String TOKEN_APP = "servlet:arc.mflux.content";
    public static final String TOKEN_TAG = "UNIMELB_DOWNLOAD_SHELL_SCRIPT";
    public static final int DEFAULT_PAGE_SIZE = 10000;

    public static final String ARG_PAGE_SIZE = "page-size";
    public static final String ARG_OVERWRITE = "overwrite";
    public static final String ARG_VERBOSE = "verbose";

    protected AssetDownloadShellScriptWriter(ServerDetails serverDetails, String token, int pageSize, boolean overwrite,
            boolean verbose, OutputStream os, boolean autoFlush, LineSeparator lineSeparator) throws Throwable {
        super(serverDetails, token, TOKEN_APP,
                MapUtils.createMap(new String[] { ARG_PAGE_SIZE, ARG_OVERWRITE, ARG_VERBOSE },
                        new Object[] { pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize, overwrite, verbose }),
                os, autoFlush, lineSeparator);
    }

    public int pageSize() {
        Integer pageSize = (Integer) argValue(ARG_PAGE_SIZE);
        if (pageSize == null || pageSize < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return pageSize;
    }

    public boolean verbose() {
        Boolean verbose = (Boolean) argValue(ARG_VERBOSE);
        if (verbose == null) {
            return false;
        } else {
            return verbose;
        }
    }

    public boolean overwrite() {
        Boolean overwrite = (Boolean) argValue(ARG_OVERWRITE);
        if (overwrite == null) {
            return false;
        } else {
            return overwrite;
        }
    }

    public final String servletPath() {
        return SERVLET_PATH;
    }

    public final URI servletURI() throws Throwable {
        URIBuilder ub = new URIBuilder().setScheme(server().transport()).setHost(server().host())
                .setPort(server().port()).setPath(servletPath());
        if (token() != null) {
            ub.addParam("_token", token());
        }
        return ub.build();
    }

    public abstract void addAsset(String assetId, String dstPath);

    public void addQuery(ServiceExecutor executor, String where) throws Throwable {
        addQuery(executor, where, null);
    }

    public void addQuery(ServiceExecutor executor, String where, String basePath) throws Throwable {
        int idx = 1;
        boolean completed = false;
        do {
            XmlDocMaker dm = new XmlDocMaker("args");
            dm.add("where", where);
            dm.add("action", "get-path");
            dm.add("size", pageSize());
            dm.add("idx", idx);
            XmlDoc.Element re = executor.execute("asset.query", dm.root());
            List<XmlDoc.Element> pes = re.elements("path");
            if (pes != null && !pes.isEmpty()) {
                for (XmlDoc.Element pe : pes) {
                    String path = pe.value();
                    String id = pe.value("@id");
                    if (basePath == null) {
                        addAsset(id, path);
                    } else {
                        String dstPath = PathUtils.joinSystemIndependent(PathUtils.getLastComponent(basePath), PathUtils
                                .getRelativePathSI(PathUtils.trimLeft('/', basePath), PathUtils.trimLeft('/', path)));
                        addAsset(id, dstPath);
                    }

                }
                flush();
            }
            completed = re.longValue("cursor/remaining") == 0;
        } while (!completed && !Thread.interrupted());
    }

    public void addNamespace(ServiceExecutor executor, String namespace) throws Throwable {
        addQuery(executor, "namespace>='" + namespace + "'", namespace);
    }

    public void addNamespaces(ServiceExecutor executor, Collection<String> namespaces) throws Throwable {
        if (namespaces != null) {
            for (String namespace : namespaces) {
                addNamespace(executor, namespace);
            }
        }
    }

    public static AssetDownloadShellScriptWriter create(TargetOS target, ServerDetails serverDetails, String token,
            int pageSize, boolean overwrite, boolean verbose, OutputStream out) throws Throwable {
        return target == TargetOS.UNIX
                ? new AssetDownloadShellUnixScriptWriter(serverDetails, token, pageSize, overwrite, verbose, out)
                : new AssetDownloadShellWindowsScriptWriter(serverDetails, token, pageSize, overwrite, verbose, out);
    }

}
