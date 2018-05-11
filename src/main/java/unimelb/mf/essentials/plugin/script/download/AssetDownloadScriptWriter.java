package unimelb.mf.essentials.plugin.script.download;

import java.io.OutputStream;
import java.util.Date;
import java.util.Map;

import unimelb.mf.essentials.plugin.script.ClientScriptWriter;
import unimelb.mf.essentials.plugin.util.ServerDetails;

public abstract class AssetDownloadScriptWriter extends ClientScriptWriter {

    public static final String TOKEN_DOWNLOADER_ROLE = "unimelb:token-downloader";

    protected AssetDownloadScriptWriter(ServerDetails serverDetails, String token, String tokenApp, Date tokenExpiry,
            Map<String, Object> args, OutputStream os, boolean autoFlush, LineSeparator lineSeparator)
            throws Throwable {
        super(serverDetails, token, tokenApp, tokenExpiry, args, os, autoFlush, lineSeparator);
    }

}
