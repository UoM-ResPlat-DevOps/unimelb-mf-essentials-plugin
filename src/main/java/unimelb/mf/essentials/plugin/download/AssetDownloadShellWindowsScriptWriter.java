package unimelb.mf.essentials.plugin.download;

import java.io.OutputStream;

import unimelb.mf.essentials.plugin.script.TargetOS;
import unimelb.mf.essentials.plugin.util.ServerDetails;

public class AssetDownloadShellWindowsScriptWriter extends AssetDownloadShellScriptWriter {

    public AssetDownloadShellWindowsScriptWriter(ServerDetails serverDetails, String token, OutputStream os)
            throws Throwable {
        super(serverDetails, token, os, false, LineSeparator.WINDOWS);
    }

    @Override
    protected void writeHead() throws Throwable {
        println("@ECHO OFF");
        /*
         * output directory
         */
        println();
        println("SET DIR=%1");
        println("IF [%1]==[] SET DIR=%CD%");
        /*
         * servlet url
         */
        println();
        println("SET URI=" + servletURI());
        flush();
    }

    @Override
    protected void writeTail() {
        println();
        println(":DOWNLOAD");
        println("SETLOCAL EnableExtensions EnableDelayedExpansion");
        println("SET id=%~1");
        println("SET dst=%~2");
        println("SET url=%URI%^&id=%id%");
        println("SET out=\"%DIR%\\%dst%\"");
        println("FOR %%F IN (%out%) DO SET pdir=%%~dpF");
        println("WHERE POWERSHELL >NUL 2>NUL");
        println("IF %ERRORLEVEL% EQU 0 (");
        println("    MD \"%pdir%\" 2>NUL");
        println("    POWERSHELL -COMMAND \"(New-Object Net.WebClient).DownloadFile('%url%', '%out%')\" >NUL 2>NUL");
        println(")");
        println("IF NOT EXIST %out% (");
        println("    WHERE BITSADMIN >NUL 2>NUL");
        println("    IF %ERRORLEVEL% EQU 0 (");
        println("        MD \"%pdir%\" 2>NUL");
        println("        BITSADMIN /TRANSFER \"Download %out%\" %url% \"%out%\" >NUL 2>NUL");
        println("    )");
        println(")");
        println("IF NOT EXIST %out% (");
        println("    EXIT /B 1");
        println(")");
        println("EXIT /B 0");
        flush();
    }

    @Override
    public void addAsset(String assetId, String dstPath) {
        println(String.format("CALL :DOWNLOAD %s \"%s\"", assetId, dstPath));
    }

    @Override
    public final TargetOS targetOS() {
        return TargetOS.WINDOWS;
    }

}
