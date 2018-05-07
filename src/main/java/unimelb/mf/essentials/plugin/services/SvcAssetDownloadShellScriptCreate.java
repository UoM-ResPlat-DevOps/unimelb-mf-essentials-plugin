package unimelb.mf.essentials.plugin.services;

import java.io.OutputStream;
import java.util.Collection;

import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.IntegerType;
import arc.xml.XmlDoc;
import unimelb.mf.essentials.plugin.script.download.AssetDownloadShellScriptWriter;
import unimelb.mf.essentials.plugin.util.ServerDetails;

public class SvcAssetDownloadShellScriptCreate extends SvcAssetDownloadScriptCreate {

    public static final String SERVICE_NAME = "unimelb.asset.download.shell.script.create";

    public static final String DEFAULT_SCRIPT_FILE_NAME = "unimelb-mf-shell-download";

    public SvcAssetDownloadShellScriptCreate() {
        super();
        SvcAssetDownloadShellScriptCreate.addToDefn(this.defn);
    }

    static void addToDefn(Interface defn) {
        defn.add(new Interface.Element("page-size", IntegerType.POSITIVE_ONE,
                "Query page size. Defaults to " + AssetDownloadShellScriptWriter.DEFAULT_PAGE_SIZE, 0, 1));
        defn.add(new Interface.Element("overwrite", BooleanType.DEFAULT,
                "Whether or not overwrite existing files. Defaults to false", 0, 1));
        defn.add(new Interface.Element("verbose", BooleanType.DEFAULT,
                "Whether or not display the files being downloaded. Defaults to false", 0, 1));
    }

    @Override
    public String description() {
        return "Generates a Unix/Windows shell script to download the selected assets via /mflux/content.mfjp servlet. To execute the script on Unix platform, curl or wget is required.";
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

    @Override
    protected void generateScript(XmlDoc.Element args, TargetOS target, ServerDetails serverDetails, String token,
            String where, Collection<String> namespaces, OutputStream out) throws Throwable {
        int pageSize = args.intValue("page-size", AssetDownloadShellScriptWriter.DEFAULT_PAGE_SIZE);
        boolean overwrite = args.booleanValue("overwrite", false);
        boolean verbose = args.booleanValue("verbose", false);
        AssetDownloadShellScriptWriter w = AssetDownloadShellScriptWriter.create(target, serverDetails, token, pageSize,
                overwrite, verbose, out);
        try {
            if (where != null) {
                w.addQuery(executor(), where);
            }
            if (namespaces != null && !namespaces.isEmpty()) {
                w.addNamespaces(executor(), namespaces);
            }
        } finally {
            w.close();
        }
    }

    @Override
    protected String defaultScriptFileName() {
        return DEFAULT_SCRIPT_FILE_NAME;
    }

    @Override
    protected final String tokenApp() {
        return AssetDownloadShellScriptWriter.TOKEN_APP;
    }

    @Override
    protected final String tokenTag() {
        return AssetDownloadShellScriptWriter.TOKEN_TAG;
    }
}
