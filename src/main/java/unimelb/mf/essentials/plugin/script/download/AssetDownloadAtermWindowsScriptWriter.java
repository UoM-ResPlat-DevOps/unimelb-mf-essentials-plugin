package unimelb.mf.essentials.plugin.script.download;

import java.io.OutputStream;
import java.util.Date;

import arc.utils.DateTime;
import unimelb.mf.essentials.plugin.script.TargetOS;
import unimelb.mf.essentials.plugin.util.ServerDetails;

public class AssetDownloadAtermWindowsScriptWriter extends AssetDownloadAtermScriptWriter {

    public AssetDownloadAtermWindowsScriptWriter(ServerDetails serverDetails, String token, Date tokenExpiry, int ncsr,
            boolean overwrite, boolean verbose, OutputStream os) throws Throwable {
        super(serverDetails, token, tokenExpiry, ncsr, overwrite, verbose, os, false, LineSeparator.WINDOWS);
    }

    @Override
    public final TargetOS targetOS() {
        return TargetOS.WINDOWS;
    }

    protected void writeHead() throws Throwable {
        println("@ECHO OFF");
        
        if (tokenExpiry() != null) {
            println("ECHO Mediaflux auth token expiry: " + DateTime.string(tokenExpiry()));
        }

        /*
         * output directory
         */
        println();
        println("SET DIR=%1");
        println("IF [%1]==[] SET DIR=%CD%");

        /*
         * check java
         */
        println();
        println("WHERE java >NUL 2>NUL");
        println("IF %ERRORLEVEL% NEQ 0 (");
        println("    ECHO cannot find java. Install java and retry. && EXIT /B 1");
        println(")");

        /*
         * download aterm.jar
         */
        println();
        println("SET MFLUX_ATERM=%~dp0aterm.jar");
        println("IF NOT EXIST \"%MFLUX_ATERM%\" (");
        println("    ECHO downloading %MFLUX_ATERM%");
        println("    CALL :DOWNLOAD " + atermUrl() + " %MFLUX_ATERM%");
        println(")");
        println("IF NOT EXIST \"%MFLUX_ATERM%\" (");
        println("    ECHO Failed to download aterm.jar. && EXIT /B 1");
        println(")");

        /*
         * create mflux.cfg
         */
        println();
        println("SET MFLUX_CFG=%~dp0mflux.cfg");
        println("ECHO host=" + server().host() + "> \"%MFLUX_CFG%\"");
        println("ECHO port=" + server().port() + ">> \"%MFLUX_CFG%\"");
        println("ECHO transport=" + server().transport() + ">> \"%MFLUX_CFG%\"");
        println("ECHO token=" + token() + ">> \"%MFLUX_CFG%\"");
        // TODO token app
        println();
        flush();
    }

    protected void writeTail() {
        /*
         * clean up
         */
        println();
        println("DEL \"%MFLUX_CFG%\"");
        println("DEL \"%MFLUX_ATERM%\"");

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
        println("SET url=%~1");
        println("SET out=%~2");
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
        /*
         * aterm download function
         */
        // download namespace
        println();
        println(":DOWNLOAD_NAMESPACE");
        println("SETLOCAL EnableExtensions EnableDelayedExpansion");
        println("SET namespace=%~1");
        println("java -jar -Dmf.cfg=\"%MFLUX_CFG%\" \"%MFLUX_ATERM%\" nogui download -verbose " + verbose()
                + " -filename-collisions " + (overwrite() ? "overwrite" : "skip") + " -ncsr "
                + numberOfConcurrentServerRequests() + " -namespace \"%namespace%\" \"%DIR%\"");
        println("EXIT /B 0");
        // download where
        println();
        println(":DOWNLOAD_WHERE");
        println("SETLOCAL EnableExtensions EnableDelayedExpansion");
        println("SET where=%~1");
        println("java -jar -Dmf.cfg=\"%MFLUX_CFG%\" \"%MFLUX_ATERM%\" nogui download -verbose " + verbose()
                + " -filename-collisions " + (overwrite() ? "overwrite" : "skip") + " -ncsr "
                + numberOfConcurrentServerRequests() + " -where \"%where%\" \"%DIR%\"");
        println("EXIT /B 0");

        flush();
    }

    @Override
    public void addNamespace(String namespace) {
        println("CALL :DOWNLOAD_NAMESPACE \"" + namespace + "\"");
    }

    @Override
    public void addQuery(String where) {
        println("CALL :DOWNLOAD_WHERE \"" + where + "\"");
    }

}
