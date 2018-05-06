package unimelb.mf.essentials.plugin.script.download;

import java.io.OutputStream;

import unimelb.mf.essentials.plugin.script.TargetOS;
import unimelb.mf.essentials.plugin.util.ServerDetails;

public class AssetDownloadAtermUnixScriptWriter extends AssetDownloadAtermScriptWriter {

    public AssetDownloadAtermUnixScriptWriter(ServerDetails serverDetails, String token, int ncsr, boolean overwrite,
            boolean verbose, OutputStream os) throws Throwable {
        super(serverDetails, token, ncsr, overwrite, verbose, os, false, LineSeparator.UNIX);
    }

    @Override
    public final TargetOS targetOS() {
        return TargetOS.UNIX;
    }

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
         * download function
         */
        println();
        println("download() {");
        println("    local url=$1");
        println("    local out=$2");
        println("    if [[ ! -z $(which curl) ]]; then");
        println("        curl --create-dirs -o \"${out}\" ${url}");
        println("    else");
        println("        if [[ ! -z $(which wget) ]]; then");
        println("            wget --force-directories -O \"${out}\" ${url}");
        println("        else");
        println("           echo \"Error: no curl or wget is found. Please install curl or wget and retry.\" 1>&2");
        println("           exit 2");
        println("        fi");
        println("    fi");
        println("}");

        /*
         * aterm download function
         */
        println();
        println("download_namespace() {");
        println("    local namespace=$1");
        println("    java -jar -Dmf.cfg=${MFLUX_CFG} ${MFLUX_ATERM} nogui download -filename-collisions "
                + (overwrite() ? "overwrite" : "skip") + " -verbose " + verbose() + " -ncsr "
                + numberOfConcurrentServerRequests() + " -namespace \"${namespace}\" \"${DIR}\"");
        println("}");
        println();
        println("download_where() {");
        println("    local where=$1");
        println("    java -jar -Dmf.cfg=${MFLUX_CFG} ${MFLUX_ATERM} nogui download -filename-collisions "
                + (overwrite() ? "overwrite" : "skip") + " -verbose " + verbose() + "-ncsr "
                + numberOfConcurrentServerRequests() + " -where \"${where}\" \"${DIR}\"");
        println("}");

        /*
         * check if java exists;
         */
        println();
        println("[[ -z $(which java) ]] && echo \"Error: java is not found. Install Java and retry.\" 1>&2 && exit 1");

        /*
         * check if curl or wget exists
         */
        println();
        println("[[ -z $(which curl) && -z $(which wget) ]] && echo \"Error: curl and wget are not found. Install curl or wget and retry.\" 1>&2 && exit 2");

        /*
         * download aterm.jar
         */
        println();
        println("export MFLUX_ATERM=\"${DIR}/aterm.jar\"");
        println(String.format("[[ ! -f \"${MFLUX_ATERM}\" ]] && download %s \"${MFLUX_ATERM}\"", atermUrl()));
        println("[[ ! -f \"${MFLUX_ATERM}\" ]] && echo \"Error: Failed to download aterm.jar.\" 1>&2 && exit 3");

        /*
         * MFLUX_CFG
         */
        println();
        println("export MFLUX_CFG=$(mktemp)");
        println(String.format("echo \"host=%s\" >> \"${MFLUX_CFG}\"", server().host()));
        println(String.format("echo \"port=%d\" >> \"${MFLUX_CFG}\"", server().port()));
        println(String.format("echo \"transport=%s\" >> \"${MFLUX_CFG}\"", server().transport()));
        println(String.format("echo \"token=%s\" >> \"${MFLUX_CFG}\"", token()));
        // TODO token app
        println();
        flush();
    }

    protected void writeTail() {
        // delete MFLUX_CFG
        println();
        println("[[ -f \"${MFLUX_CFG}\" ]] && rm -f \"${MFLUX_CFG}\"");

        // delete aterm.jar
        println();
        println("[[ -f \"${MFLUX_ATERM}\" ]] && rm -f \"${MFLUX_ATERM}\"");
    }

    @Override
    public void addNamespace(String namespace) {
        println(String.format("download_namespace \"%s\"", namespace));
    }

    @Override
    public void addQuery(String where) {
        println(String.format("download_where \"%s\"", where));
    }

}
