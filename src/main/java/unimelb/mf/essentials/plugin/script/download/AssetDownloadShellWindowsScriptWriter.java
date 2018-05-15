package unimelb.mf.essentials.plugin.script.download;

import java.io.OutputStream;
import java.util.Date;

import arc.utils.DateTime;
import unimelb.mf.essentials.plugin.script.TargetOS;
import unimelb.mf.essentials.plugin.util.ServerDetails;

public class AssetDownloadShellWindowsScriptWriter extends AssetDownloadShellScriptWriter {

    public AssetDownloadShellWindowsScriptWriter(ServerDetails serverDetails, String token, Date tokenExpiry,
            int pageSize, boolean overwrite, boolean verbose, OutputStream os) throws Throwable {
        super(serverDetails, token, tokenExpiry, pageSize, overwrite, verbose, os, false, LineSeparator.WINDOWS);
    }

    @Override
    protected void writeHead() throws Throwable {
        println("@ECHO OFF");
        
        if (tokenExpiry() != null) {
            println("ECHO Mediaflux auth token expiry: " + DateTime.string(tokenExpiry()));
        }
        
        /*
         * output directory
         */
        println();
        println("SET \"DIR=%~1\"");
        println("IF \"%DIR%\"==\"\" SET \"DIR=%CD%\"");
        // Remove trailing slash
        println("IF \"%DIR:~-1%\"==\"\\\" SET \"DIR=%DIR:~0,-1%\"");
        println("IF \"%DIR:~-1%\"==\"/\" SET \"DIR=%DIR:~0,-1%\"");

        /*
         * servlet url
         */
        println();
        println("SET \"URI=" + servletURI()+"\"");
        flush();
    }

    @Override
    protected void writeTail() {
        /*
         * exit 0
         */
        println();
        println("EXIT /B 0");

        /*
         * download function
         */
        println();
        println(":DOWNLOAD");
        println("SETLOCAL EnableExtensions EnableDelayedExpansion");
        println("SET id=%~1");
        println("SET dst=%~2");
        println("SET url=%URI%^&id=%id%");
        println("SET out=%DIR%\\%dst%");
        if (!overwrite()) {
            println("IF EXIST \"%out%\" (");
            println("    ECHO %out% already exists. Skipped.");
            println("    EXIT /B 0");
            println(")");
        }
        if (verbose()) {
            println("ECHO downloading %out%");
        }
        println("FOR %%F IN (\"%out%\") DO SET \"pdir=%%~dpF\"");
        println("WHERE POWERSHELL >NUL 2>NUL");
        println("IF %ERRORLEVEL% EQU 0 (");
        println("    MD \"%pdir%\" 2>NUL");
        println("    POWERSHELL -COMMAND \"[System.Net.ServicePointManager]::ServerCertificateValidationCallback = {$true};(New-Object Net.WebClient).DownloadFile('%url%', '%out%')\" >NUL 2>NUL");
        println(")");
        // @formatter:off
//        println("IF %ERRORLEVEL% NEQ 0 (");
//        println("    WHERE BITSADMIN >NUL 2>NUL");
//        println("    IF %ERRORLEVEL% EQU 0 (");
//        println("        MD \"%pdir%\" 2>NUL");
//        println("        BITSADMIN /TRANSFER \"Download %out%\" %url% \"%out%\" >NUL 2>NUL");
//        println("    )");
//        println(")");
        // @formatter:on
        println("IF %ERRORLEVEL% NEQ 0 (");
        println("    ECHO failed to download %out%");
        println("    EXIT /B 1");
        println(")");
        println("EXIT /B 0");
        flush();
    }

    @Override
    public void addAsset(String assetId, String dstPath) {
        println(String.format("CALL :DOWNLOAD %s \"%s\" || EXIT /B 1", assetId, dstPath));
    }

    @Override
    public final TargetOS targetOS() {
        return TargetOS.WINDOWS;
    }

}
