package unimelb.mf.essentials.plugin.services;

import java.io.OutputStream;
import java.util.Collection;

import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.IntegerType;
import arc.xml.XmlDoc;
import unimelb.mf.essentials.plugin.script.download.AssetDownloadAtermScriptWriter;
import unimelb.mf.essentials.plugin.util.ServerDetails;

public class SvcAssetDownloadAtermScriptCreate extends SvcAssetDownloadScriptCreate {

    public static final String SERVICE_NAME = "unimelb.asset.download.aterm.script.create";

    public static final String DEFAULT_SCRIPT_FILE_NAME = "unimelb-mf-aterm-download";

    public SvcAssetDownloadAtermScriptCreate() {
        super();
        SvcAssetDownloadAtermScriptCreate.addToDefn(this.defn);
    }

    static void addToDefn(Interface defn) {
        defn.add(new Interface.Element("ncsr", IntegerType.POSITIVE_ONE,
                "Number of concurrent server requests. Defaults to 1", 0, 1));
        defn.add(new Interface.Element("overwrite", BooleanType.DEFAULT,
                "Whether or not overwrite existing files. Defaults to false", 0, 1));
        defn.add(new Interface.Element("verbose", BooleanType.DEFAULT,
                "Whether or not display the files being downloaded. Defaults to false", 0, 1));
    }

    @Override
    public String description() {
        return "Generates a Unix/Windows shell script wrapper for aterm.jar. Java runtime is required.";
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

    @Override
    protected void generateScript(XmlDoc.Element args, TargetOS target, ServerDetails serverDetails, String token,
            String where, Collection<String> namespaces, OutputStream out) throws Throwable {
        int ncsr = args.intValue("ncsr", 1);
        boolean overwrite = args.booleanValue("overwrite", false);
        boolean verbose = args.booleanValue("verbose", false);
        AssetDownloadAtermScriptWriter w = AssetDownloadAtermScriptWriter.create(target, serverDetails, token, ncsr,
                overwrite, verbose, out);
        try {
            if (where != null) {
                w.addQuery(where);
            }
            if (namespaces != null && !namespaces.isEmpty()) {
                w.addNamespaces(namespaces);
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
    protected String tokenApp() {
        // TODO
        return null;
    }

    @Override
    protected final String tokenTag() {
        return AssetDownloadAtermScriptWriter.TOKEN_TAG;
    }
}
