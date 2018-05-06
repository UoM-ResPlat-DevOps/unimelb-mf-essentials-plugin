package unimelb.mf.essentials.plugin.download;

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
import unimelb.utils.PathUtils;
import unimelb.utils.URIBuilder;

public abstract class AssetDownloadShellScriptWriter extends ClientScriptWriter {

    public static final String SERVLET_PATH = "/mflux/content.mfjp";
    public static final String TOKEN_APP = "servlet:arc.mflux.content";
    public static final String TOKEN_TAG = "UNIMELB_DOWNLOAD_SHELL_SCRIPT";
    public static final int DEFAULT_PAGE_SIZE = 10000;

    private int _pageSize = DEFAULT_PAGE_SIZE;

    protected AssetDownloadShellScriptWriter(ServerDetails serverDetails, String token, OutputStream os,
            boolean autoFlush, LineSeparator lineSeparator) throws Throwable {
        super(serverDetails, token, TOKEN_APP, os, autoFlush, lineSeparator);
        _pageSize = DEFAULT_PAGE_SIZE;
    }

    public void setPageSize(int pageSize) {
        if (pageSize < 1) {
            _pageSize = 1;
        } else {
            _pageSize = pageSize;
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
            dm.add("size", _pageSize);
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
            OutputStream out) throws Throwable {
        return target == TargetOS.UNIX ? new AssetDownloadShellUnixScriptWriter(serverDetails, token, out)
                : new AssetDownloadShellWindowsScriptWriter(serverDetails, token, out);
    }

}
