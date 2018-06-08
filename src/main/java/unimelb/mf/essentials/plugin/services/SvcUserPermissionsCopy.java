package unimelb.mf.essentials.plugin.services;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.atomic.AtomicOperation;
import arc.mf.plugin.atomic.AtomicTransaction;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcUserPermissionsCopy extends PluginService {

    public static final String SERVICE_NAME = "unimelb.user.permissions.copy";

    private Interface _defn;

    public SvcUserPermissionsCopy() {
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
        final XmlDoc.Element sae = describeActor(executor(), from);
        final XmlDoc.Element dae = describeActor(executor(), to);

        new AtomicTransaction(new AtomicOperation() {
            @Override
            public boolean execute(ServiceExecutor executor) throws Throwable {
                copyUserPermissions(executor(), sae, dae);
                return false;
            }
        }).execute(executor());
    }

    static void copyUserPermissions(ServiceExecutor executor, Element sae, Element dae) throws Throwable {
        // TODO
    }

    static XmlDoc.Element describeActor(ServiceExecutor executor, XmlDoc.Element user) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("type", "user");
        String authority = user.value("authority");
        String domain = user.value("domain");
        String name = user.value("user");
        StringBuilder sb = new StringBuilder();
        if (authority != null) {
            sb.append(authority).append(":");
        }
        sb.append(domain).append(":").append(name);
        dm.add("name", sb.toString());
        return executor.execute("actor.describe", dm.root()).element("actor");
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}