package unimelb.mf.essentials.plugin.services;

import java.util.List;

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
        final XmlDoc.Element toUser = args.element("to");
        final XmlDoc.Element fromSettings = getUserSettings(executor(), from);

        new AtomicTransaction(new AtomicOperation() {
            @Override
            public boolean execute(ServiceExecutor executor) throws Throwable {
                copyUserSettings(executor(), fromSettings, toUser);
                return false;
            }
        }).execute(executor());

    }

    static XmlDoc.Element getUserSettings(ServiceExecutor executor, Element user) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add(user, false);
        return executor.execute("user.settings.get", dm.root());
    }

    static void copyUserSettings(ServiceExecutor executor, Element fromSettings, Element toUser) throws Throwable {
        List<XmlDoc.Element> ses = fromSettings.elements("settings");
        if (ses == null || ses.isEmpty()) {
            return;
        }
        for (XmlDoc.Element se : ses) {
            if (se.hasSubElements()) {
                XmlDocMaker dm = new XmlDocMaker("args");
                if (toUser.elementExists("authority")) {
                    dm.add(toUser.element("authority"));
                }
                dm.add(toUser.element("domain"));
                dm.add(toUser.element("user"));
                dm.add("app", se.value("@app"));
                dm.push("settings");
                dm.add(se, false);
                dm.pop();
                executor.execute("user.settings.set", dm.root());
            }
        }
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}