package unimelb.mf.essentials.plugin.script.download;

import java.io.OutputStream;
import java.util.Date;

import arc.utils.DateTime;
import unimelb.mf.essentials.plugin.script.TargetOS;
import unimelb.mf.essentials.plugin.util.ServerDetails;

public class AssetDownloadShellUnixScriptWriter extends AssetDownloadShellScriptWriter {

    public AssetDownloadShellUnixScriptWriter(ServerDetails serverDetails, String token, Date tokenExpiry, int pageSize,
            boolean overwrite, boolean verbose, OutputStream os) throws Throwable {
        super(serverDetails, token, tokenExpiry, pageSize, overwrite, verbose, os, false, LineSeparator.UNIX);
    }

    @Override
    protected void writeHead() throws Throwable {
        println("#!/bin/bash");

        if (tokenExpiry() != null) {
            println("echo \"Mediaflux auth token expiry: " + DateTime.string(tokenExpiry())+"\"");
        }
        
        /*
         * Is the current script being sourced or executed
         */
        // @formatter:off
//        println();
//        println("if [[ \"${BASH_SOURCE[0]}\" != \"${0}\" ]]; then");
//        println("    SOURCED=true");
//        println("else");
//        println("    SOURCED=false");
//        println("fi");
        // @formatter:on

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
        println("    local out=\"${DIR}/${path}\"");
        println("    local overwrite=" + overwrite());
        if (!overwrite()) {
            println("    if [[ -f \"${out}\" ]]; then");
            println("        echo \"File: ${out} alread exists. Skipped.\"");
            println("        return 0");
            println("    fi");
        }
        if (verbose()) {
            println("    echo \"downloading file: ${out}\"");
        }
        println("    if [[ ! -z $(which curl) ]]; then");
        println("        curl --create-dirs -k -o \"${out}\" \"${url}\"");
        println("        [[ $? -ne 0 ]] && echo \"Error: curl failed to download ${out}\" 1>&2 && return 2");
        println("    else");
        println("        if [[ ! -z $(which wget) ]]; then");
        println("            local pdir=$(dirname \"${out}\")");
        println("            mkdir -p \"${pdir}\" && wget --quiet --no-check-certificate -O \"${out}\" \"${url}\"");
        println("            [[ $? -ne 0 ]] && echo \"Error: wget failed to download ${out}\" 1>&2 && return 2");
        println("        else");
        println("           echo \"Error: no curl or wget is found. Please install curl or wget and retry.\" 1>&2");
        println("           return 1");
        println("        fi");
        println("    fi");
        println("    return 0");
        println("}");
        println();
        flush();
    }

    @Override
    public void addAsset(String assetId, String dstPath) {
        println(String.format("download %s \"%s\" || exit 2", assetId, dstPath));
    }

    @Override
    public final TargetOS targetOS() {
        return TargetOS.UNIX;
    }

}
