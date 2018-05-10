package unimelb.mf.essentials.plugin.services;

import java.util.Date;

import arc.mf.plugin.Session;
import arc.utils.DateTime;

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

    @Override
    protected String scriptCreateServiceName() {
        return SvcAssetDownloadAtermScriptCreate.SERVICE_NAME;
    }

    @Override
    protected String emailMessage(String message, String url, Date expiry) throws Throwable {
        StringBuilder sb = new StringBuilder();
        if (message != null) {
            sb.append(message.replaceAll("(\r\n|\n)", "<br/>"));
        } else {
            sb.append("Dear User,<br/><br/>\n");
            // TODO comprehensive message
        }
        sb.append("Please download the <b><a href=\"" + url + "\">scripts</a></b> from <a href=\"" + url + "\">" + url
                + "</a> and extract the zip archive. To download the data from Mediaflux: <br/>");
        sb.append("<ul>\n");
        sb.append("<li>make sure you have <a href=\"https://java.com/en/download/\">Java</a> installed.</li>");
        sb.append("<li>execute .sh script in a terminal window, if you are on Mac OS or Linux.</li>\n");
        sb.append("<li>execute .cmd script in a command prompt window, if you are on Windows platform.</li>\n");
        sb.append("</ul>\n");
        sb.append("<br/><br/>\n");
        sb.append("<h3>Note:</h3>\n");
        sb.append("<ul>\n");
        sb.append("<li>It is recommended to use Chrome or Firefox browser to download the above link. If the link is blocked by your mail server(mimecast), copy the url and paste in the browser's address field.</li>\n");
        sb.append("<li>The auth token associated the scripts and the download link will expire at <b>").append(DateTime.string(expiry)).append("</b>.</li>\n");
        sb.append("</ul>\n");
        String userFullName = Session.user().fullName();
        if (userFullName != null) {
            sb.append("<br/></br>");
            sb.append(userFullName);
        }
        return sb.toString();
    }
}
