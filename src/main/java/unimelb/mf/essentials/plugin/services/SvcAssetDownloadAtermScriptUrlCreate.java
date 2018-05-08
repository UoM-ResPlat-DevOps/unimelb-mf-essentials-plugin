package unimelb.mf.essentials.plugin.services;

public class SvcAssetDownloadAtermScriptUrlCreate extends SvcAssetDownloadScriptUrlCreate {

    public static final String SERVICE_NAME = "unimelb.asset.download.aterm.script.url.create";

    public static final String TOKEN_TAG = "UNIMELB_ASSET_DOWNLOAD_ATERM_SCRIPT_URL";

    public static final String FILENAME_PREFIX = "unimelb-asset-download-aterm-script";

    public SvcAssetDownloadAtermScriptUrlCreate() {
        super();
        SvcAssetDownloadAtermScriptCreate.addToDefn(this.defn);
    }

    @Override
    public String description() {
        return "Generate download link for the aterm scripts.";
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
