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

public class SvcUserMetadataCopy extends PluginService {

    public static final String SERVICE_NAME = "unimelb.user.metadata.copy";

    private Interface _defn;

    public SvcUserMetadataCopy() {
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
        XmlDoc.Element toUser = args.element("to");
        final XmlDoc.Element fromUserElement = describeUser(executor(), fromUser, false);
        final XmlDoc.Element toUserElement = describeUser(executor(), toUser, false);

        new AtomicTransaction(new AtomicOperation() {
            @Override
            public boolean execute(ServiceExecutor executor) throws Throwable {
                copyUserMetadata(executor(), fromUserElement, toUserElement);
                return false;
            }
        }).execute(executor());
    }

    static void copyUserMetadata(ServiceExecutor executor, Element fromUserElement, Element toUserElement) throws Throwable {
        // String home1 = sue.value("home");
        // String homeFolder1 = sue.value("home-folder");
        XmlDoc.Element meta1 = fromUserElement.element("asset/meta");

        // String home2 = due.value("home");
        // String homeFolder2 = due.value("home-folder");

        XmlDocMaker dm = new XmlDocMaker("args");
        String authority = toUserElement.value("@authority");
        if (authority != null) {
            String protocol = toUserElement.value("@protocol");
            dm.add("authority", new String[] { "protocol", protocol }, authority);
        }
        dm.add("domain", toUserElement.value("@domain"));
        dm.add("user", toUserElement.value("@user"));

        // if (home2 == null && home1 != null) {
        // dm.add("home", home1);
        // }
        //
        // if (homeFolder2 == null && homeFolder1 != null) {
        // dm.add("home-folder", homeFolder1);
        // }

        if (meta1 != null) {
            List<XmlDoc.Element> els1 = meta1.elements();
            if (els1 != null) {
                boolean pushed = false;
                for (XmlDoc.Element el : els1) {
                    // excludes mf-user, mf-system-user and mf-revision-history
                    if (!el.nameEquals("mf-user") && !el.nameEquals("mf-system-user")
                            && !el.nameEquals("mf-revision-history")) {
                        if (!pushed) {
                            dm.push("meta", new String[] { "action", "merge" });
                            pushed = true;
                        }
                        dm.add(el);
                    }
                }
                if (pushed) {
                    dm.pop();
                }
            }
        }
        executor.execute("user.set", dm.root());
    }

    static XmlDoc.Element describeUser(ServiceExecutor executor, XmlDoc.Element user, boolean permissions)
            throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        if (user.elementExists("authority")) {
            dm.add(user.element("authority"));
        }
        dm.add(user.element("domain"));
        dm.add(user.element("user"));
        if (permissions) {
            dm.add("permissions", permissions);
        }
        return executor.execute("user.describe", dm.root()).element("user");
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
