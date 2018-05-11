package unimelb.mf.essentials.plugin.services;

import java.util.Date;

import arc.mf.plugin.Session;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.IntegerType;
import arc.utils.DateTime;
import unimelb.mf.essentials.plugin.script.download.AssetDownloadShellScriptWriter;

public class SvcAssetDownloadShellScriptUrlCreate extends SvcAssetDownloadScriptUrlCreate {

    public static final String SERVICE_NAME = "unimelb.asset.download.shell.script.url.create";

    public static final String TOKEN_TAG = "UNIMELB_DOWNLOAD_SHELL_SCRIPT_URL";

    public static final String FILENAME_PREFIX = "unimelb-asset-download-shell-script";

    public SvcAssetDownloadShellScriptUrlCreate() {
        super();
    }

    @Override
    protected void addToDownloadDefn(Interface.Element download) {
        download.add(new Interface.Element("page-size", IntegerType.POSITIVE_ONE,
                "Query page size. Defaults to " + AssetDownloadShellScriptWriter.DEFAULT_PAGE_SIZE, 0, 1));
        download.add(new Interface.Element("overwrite", BooleanType.DEFAULT,
                "Whether or not overwrite existing files. Defaults to false", 0, 1));
        download.add(new Interface.Element("verbose", BooleanType.DEFAULT,
                "Whether or not display the files being downloaded. Defaults to false", 0, 1));
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

    @Override
    protected String scriptCreateServiceName() {
        return SvcAssetDownloadShellScriptCreate.SERVICE_NAME;
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
        sb.append("<li>execute .sh script in a terminal window, if you are on Mac OS or Linux.</li>\n");
        sb.append("<li>execute .cmd script in a command prompt window, if you are on Windows platform.</li>\n");
        sb.append("</ul>\n");
        sb.append("<br/><br/>");
        sb.append("<h3>Note:</h3>\n");
        sb.append("<ul>\n");
        sb.append(
                "<li>It is recommended to use Chrome or Firefox browser to download the above link. If the link is blocked by your mail server(mimecast), copy the url and paste in the browser's address field.</li>\n");
        sb.append("<li>The auth token associated the scripts and the download link will expire at <b>")
                .append(DateTime.string(expiry)).append("</b>.</li>\n");
        sb.append("</ul>\n");
        String userFullName = Session.user().fullName();
        if (userFullName != null) {
            sb.append("<br/></br>");
            sb.append(userFullName);
        }
        return sb.toString();
    }
}
