package unimelb.mf.essentials.plugin.services;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.atomic.AtomicOperation;
import arc.mf.plugin.atomic.AtomicTransaction;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcUserSettingsCopy extends PluginService {

    public static final String SERVICE_NAME = "unimelb.user.settings.copy";

    private Interface _defn;

    public SvcUserSettingsCopy() {
        _defn = new Interface();
        SvcUserProfileCopy.addToDefn(_defn);
    }

    @Override
    public Access access() {
        return ACCESS_ADMINISTER;
    }

    @Override
    public Interface definition() {
        return _defn;
    }

    @Override
    public String description() {
        return "Copy user asset metadata from one to the other.";
    }

    @Override
    public void execute(Element args, Inputs inputs, Outputs outputs, XmlWriter w) throws Throwable {
        XmlDoc.Element from = args.element("from");
        XmlDoc.Element to = args.element("to");
        final XmlDoc.Element sse = getUserSettings(executor(), from);
        final XmlDoc.Element dse = getUserSettings(executor(), to);

        new AtomicTransaction(new AtomicOperation() {
            @Override
            public boolean execute(ServiceExecutor executor) throws Throwable {
                copyUserSettings(executor(), sse, dse);
                return false;
            }
        }).execute(executor());

    }

    static XmlDoc.Element getUserSettings(ServiceExecutor executor, Element user) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add(user, false);
        return executor.execute("user.settings.get", dm.root());
    }

    static void copyUserSettings(ServiceExecutor executor, Element from, Element to) throws Throwable {
        // TODO
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}