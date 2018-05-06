package unimelb.mf.essentials.plugin.download;

import java.io.OutputStream;
import java.util.Collection;

import unimelb.mf.essentials.plugin.script.ClientScriptWriter;
import unimelb.mf.essentials.plugin.services.SvcAssetDownloadScriptCreate.TargetOS;
import unimelb.mf.essentials.plugin.util.ServerDetails;
import unimelb.utils.URIBuilder;

public abstract class AssetDownloadAtermScriptWriter extends ClientScriptWriter {

    public static final String TOKEN_APP = "ATERM";
    
    public static final String TOKEN_TAG = "UNIMELB_DOWNLOAD_ATERM_SCRIPT";

    public static final String ATERM_PATH = "/mflux/aterm.jar";

    public static final int DEFAULT_NUMBER_OF_CONCURRENT_SERVER_REQUESTS = 1;

    private int _ncsr = DEFAULT_NUMBER_OF_CONCURRENT_SERVER_REQUESTS;

    protected AssetDownloadAtermScriptWriter(ServerDetails serverDetails, String token, OutputStream os,
            boolean autoFlush, LineSeparator lineSeparator) throws Throwable {
        super(serverDetails, token, TOKEN_APP, os, autoFlush, lineSeparator);
    }

    protected void initialize() {
        super.initialize();
        _ncsr = DEFAULT_NUMBER_OF_CONCURRENT_SERVER_REQUESTS;
    }

    public String atermUrl() throws Throwable {
        return new URIBuilder().setScheme(server().transport()).setHost(server().host()).setPort(server().port())
                .setPath(ATERM_PATH).build().toString();
    }

    public int numberOfConcurrentServerRequests() {
        return _ncsr;
    }

    public void setNumberOfConcurrentServerRequests(int ncsr) {
        if (ncsr < 1) {
            _ncsr = 1;
        } else {
            _ncsr = ncsr;
        }
    }

    public abstract void addNamespace(String namespace);

    public void addNamespaces(Collection<String> namespaces) {
        if (namespaces != null) {
            for (String namespace : namespaces) {
                addNamespace(namespace);
            }
            flush();
        }
    }

    public abstract void addQuery(String where);

    public static AssetDownloadAtermScriptWriter create(TargetOS target, ServerDetails serverDetails, String token,
            OutputStream out) throws Throwable {
       return target == TargetOS.UNIX
                ? new AssetDownloadAtermUnixScriptWriter(serverDetails, token, out)
                : new AssetDownloadAtermWindowsScriptWriter(serverDetails, token, out);
    }

}
