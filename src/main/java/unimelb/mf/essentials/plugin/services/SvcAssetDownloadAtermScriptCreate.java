package unimelb.mf.essentials.plugin.services;

import java.io.OutputStream;
import java.util.Collection;

import unimelb.mf.essentials.plugin.download.AssetDownloadAtermScriptWriter;
import unimelb.mf.essentials.plugin.util.ServerDetails;

public class SvcAssetDownloadAtermScriptCreate extends SvcAssetDownloadScriptCreate {

    public static final String SERVICE_NAME = "unimelb.asset.download.aterm.script.create";

    public static final String DEFAULT_SCRIPT_FILE_NAME = "unimelb-mf-aterm-download";

    public SvcAssetDownloadAtermScriptCreate() {
        super();
    }

    @Override
    public String description() {
        return "Generates a Unix/Windows shell script wrapper for aterm.jar. Java runtime is required.";
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

    @Override
    protected void generateScript(TargetOS target, ServerDetails serverDetails, String token, String where,
            Collection<String> namespaces, OutputStream out) throws Throwable {
        AssetDownloadAtermScriptWriter w = AssetDownloadAtermScriptWriter.create(target, serverDetails, token, out);
        try {
            if (where != null) {
                w.addQuery(where);
            }
            if (namespaces != null && !namespaces.isEmpty()) {
                w.addNamespaces(namespaces);
            }
        } finally {
            w.close();
        }
    }

    @Override
    protected String defaultScriptFileName() {
        return DEFAULT_SCRIPT_FILE_NAME;
    }

    @Override
    protected String tokenApp() {
        // TODO
        return null;
    }

    @Override
    protected final String tokenTag() {
        return AssetDownloadAtermScriptWriter.TOKEN_TAG;
    }
}
