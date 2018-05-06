package unimelb.mf.essentials.plugin.script.download;

import java.io.OutputStream;

import unimelb.mf.essentials.plugin.script.TargetOS;
import unimelb.mf.essentials.plugin.util.ServerDetails;

public class AssetDownloadShellUnixScriptWriter extends AssetDownloadShellScriptWriter {

    public AssetDownloadShellUnixScriptWriter(ServerDetails serverDetails, String token, int pageSize, boolean overwrite,
            boolean verbose, OutputStream os) throws Throwable {
        super(serverDetails, token, pageSize, overwrite, verbose, os, false, LineSeparator.UNIX);
    }

    @Override
    protected void writeHead() throws Throwable {
        println("#!/bin/bash");
                
        /*
         * output directory
         */
        println();
        println("DIR=$1");
        println("if [[ -z $DIR ]]; then");
        println("   DIR=.");
        println("fi");
        println("[[ ! -d \"${DIR}\" ]] && echo \"Error: directory: ${DIR} does not exist.\" 1>&2 && exit 1");

        /*
         * servlt url
         */
        println();
        println("URI=" + servletURI());

        /*
         * download function
         */
        println("");
        println("download() {");
        println("    local id=$1");
        println("    local path=$2");
        println("    local url=\"${URI}&id=${id}\"");
        println("    local out=${DIR}/${path}");
        println("    local overwrite=" + overwrite());
        println("    if [[ ! -z $(which curl) ]]; then");
        println("        curl --create-dirs -o \"${out}\" ${url}");
        println("    else");
        println("        if [[ ! -z $(which wget) ]]; then");
        println("            wget --force-directories -O \"${out}\" ${url}");
        println("        else");
        println("           echo \"Error: no curl or wget is found. Please install curl or wget and retry.\" 1>&2");
        println("           exit 1");
        println("        fi");
        println("    fi");
        println("}");
        println();
        flush();
    }

    @Override
    public void addAsset(String assetId, String dstPath) {
        println(String.format("download %s \"%s\"", assetId, dstPath));
    }

    @Override
    public final TargetOS targetOS() {
        return TargetOS.UNIX;
    }

}
