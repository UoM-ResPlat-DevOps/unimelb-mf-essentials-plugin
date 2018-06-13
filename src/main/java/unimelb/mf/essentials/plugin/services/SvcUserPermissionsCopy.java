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
        XmlDoc.Element fromUser = args.element("from");
        final XmlDoc.Element toUser = args.element("to");
        final XmlDoc.Element fromActor = describeActor(executor(), fromUser);

        new AtomicTransaction(new AtomicOperation() {
            @Override
            public boolean execute(ServiceExecutor executor) throws Throwable {
                copyUserPermissions(executor(), fromActor, toUser);
                return false;
            }
        }).execute(executor());
    }

    static void copyUserPermissions(ServiceExecutor executor, Element fromActor, Element toUser) throws Throwable {
        List<XmlDoc.Element> res = fromActor.elements("role");
        List<XmlDoc.Element> pes = fromActor.elements("perm");
        if ((res == null || res.isEmpty()) && (pes == null || pes.isEmpty())) {
            return;
        }

        XmlDocMaker dm = new XmlDocMaker("args");
        if (toUser.elementExists("authority")) {
            dm.add(toUser.element("authority"));
        }
        dm.add(toUser.element("domain"));
        dm.add(toUser.element("user"));
        if (res != null) {
            for (XmlDoc.Element re : res) {
                dm.add("role", new String[] { "type", re.value("@type") }, re.value());
            }
        }
        if (pes != null) {
            for (XmlDoc.Element pe : pes) {
                dm.add(pe);
            }
        }
        executor.execute("user.grant", dm.root());
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