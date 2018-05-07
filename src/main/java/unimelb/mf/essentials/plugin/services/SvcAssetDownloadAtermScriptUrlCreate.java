package unimelb.mf.essentials.plugin.services;

import arc.mf.plugin.PluginService;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;

public class SvcAssetDownloadAtermScriptUrlCreate extends PluginService {

    public static final String SERVICE_NAME = "unimelb.asset.download.aterm.script.url.create";

    private Interface _defn;

    public SvcAssetDownloadAtermScriptUrlCreate() {
        _defn = new Interface();
        SvcAssetDownloadScriptCreate.addToDefn(_defn);
        SvcAssetDownloadAtermScriptCreate.addToDefn(_defn);
    }

    @Override
    public Access access() {
        return ACCESS_MODIFY;
    }

    @Override
    public Interface definition() {
        return _defn;
    }

    @Override
    public String description() {
        return "Generate download link for the script.";
    }

    @Override
    public void execute(Element args, Inputs arg1, Outputs arg2, XmlWriter w) throws Throwable {
        // TODO Auto-generated method stub

    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
