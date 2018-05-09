package unimelb.mf.essentials.plugin.script.download;

import java.io.OutputStream;
import java.util.Collection;
import java.util.Date;

import unimelb.mf.essentials.plugin.script.ClientScriptWriter;
import unimelb.mf.essentials.plugin.services.SvcAssetDownloadScriptCreate.TargetOS;
import unimelb.mf.essentials.plugin.util.ServerDetails;
import unimelb.utils.MapUtils;
import unimelb.utils.URIBuilder;

public abstract class AssetDownloadAtermScriptWriter extends ClientScriptWriter {

    public static final String TOKEN_APP = "ATERM";

    public static final String TOKEN_TAG = "UNIMELB_DOWNLOAD_ATERM_SCRIPT";

    public static final String ATERM_PATH = "/mflux/aterm.jar";

    public static final int DEFAULT_NUMBER_OF_CONCURRENT_SERVER_REQUESTS = 1;

    public static final String ARG_NCSR = "ncsr";
    public static final String ARG_OVERWRITE = "overwrite";
    public static final String ARG_VERBOSE = "verbose";

    protected AssetDownloadAtermScriptWriter(ServerDetails serverDetails, String token, Date tokenExpiry, int ncsr,
            boolean overwrite, boolean verbose, OutputStream os, boolean autoFlush, LineSeparator lineSeparator)
            throws Throwable {
        super(serverDetails, token, TOKEN_APP, tokenExpiry,
                MapUtils.createMap(new String[] { ARG_NCSR, ARG_OVERWRITE, ARG_VERBOSE },
                        new Object[] { ncsr, overwrite, verbose }),
                os, autoFlush, lineSeparator);
    }

    public String atermUrl() throws Throwable {
        return new URIBuilder().setScheme(server().transport()).setHost(server().host()).setPort(server().port())
                .setPath(ATERM_PATH).build().toString();
    }

    public int numberOfConcurrentServerRequests() {
        Integer ncsr = (Integer) argValue(ARG_NCSR);
        if (ncsr == null) {
            return DEFAULT_NUMBER_OF_CONCURRENT_SERVER_REQUESTS;
        }
        return ncsr;
    }

    public boolean verbose() {
        Boolean verbose = (Boolean) argValue(ARG_VERBOSE);
        if (verbose == null) {
            return true;
        } else {
            return verbose;
        }
    }

    public boolean overwrite() {
        Boolean overwrite = (Boolean) argValue(ARG_OVERWRITE);
        if (overwrite == null) {
            return true;
        } else {
            return overwrite;
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
            Date tokenExpiry, int ncsr, boolean overwrite, boolean verbose, OutputStream out) throws Throwable {
        return target == TargetOS.UNIX
                ? new AssetDownloadAtermUnixScriptWriter(serverDetails, token, tokenExpiry, ncsr, overwrite, verbose,
                        out)
                : new AssetDownloadAtermWindowsScriptWriter(serverDetails, token, tokenExpiry, ncsr, overwrite, verbose,
                        out);
    }

}
