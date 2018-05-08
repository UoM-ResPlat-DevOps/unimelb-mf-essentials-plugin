package unimelb.mf.essentials.plugin.services;

public class SvcAssetDownloadShellScriptUrlCreate extends SvcAssetDownloadScriptUrlCreate {

    public static final String SERVICE_NAME = "unimelb.asset.download.shell.script.url.create";

    public static final String TOKEN_TAG = "UNIMELB_ASSET_DOWNLOAD_SHELL_SCRIPT_URL";

    public static final String FILENAME_PREFIX = "unimelb-asset-download-shell-script";

    public SvcAssetDownloadShellScriptUrlCreate() {
        super();
        SvcAssetDownloadAtermScriptCreate.addToDefn(this.defn);
    }

    @Override
    public String description() {
        return "Generate download link for the shell scripts.";
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

    @Override
    protected String tokenTag() {
        return TOKEN_TAG;
    }

    @Override
    protected String filenamePrefix() {
        return FILENAME_PREFIX;
    }
}
