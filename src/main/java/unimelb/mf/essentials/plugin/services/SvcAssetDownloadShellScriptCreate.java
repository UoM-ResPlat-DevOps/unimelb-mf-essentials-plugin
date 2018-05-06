package unimelb.mf.essentials.plugin.services;

import java.io.OutputStream;
import java.util.Collection;

import unimelb.mf.essentials.plugin.download.AssetDownloadShellScriptWriter;
import unimelb.mf.essentials.plugin.util.ServerDetails;

public class SvcAssetDownloadShellScriptCreate extends SvcAssetDownloadScriptCreate {

    public static final String SERVICE_NAME = "unimelb.asset.download.shell.script.create";

    public static final String DEFAULT_SCRIPT_FILE_NAME = "unimelb-mf-shell-download";

    public SvcAssetDownloadShellScriptCreate() {
        super();
    }

    @Override
    public String description() {
        return "Generates a Unix/Windows shell script to download the selected assets via /mflux/content.mfjp servlet. To execute the script on Unix platform, curl or wget is required.";
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

    @Override
    protected void generateScript(TargetOS target, ServerDetails serverDetails, String token, String where,
            Collection<String> namespaces, OutputStream out) throws Throwable {
        AssetDownloadShellScriptWriter w = AssetDownloadShellScriptWriter.create(target, serverDetails, token, out);
        try {
            if (where != null) {
                w.addQuery(executor(), where);
            }
            if (namespaces != null && !namespaces.isEmpty()) {
                w.addNamespaces(executor(), namespaces);
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
    protected final String tokenApp() {
        return AssetDownloadShellScriptWriter.TOKEN_APP;
    }

    @Override
    protected final String tokenTag() {
        return AssetDownloadShellScriptWriter.TOKEN_TAG;
    }
}
