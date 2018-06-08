package unimelb.mf.essentials.plugin.services;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.atomic.AtomicOperation;
import arc.mf.plugin.atomic.AtomicTransaction;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlDoc.Element;
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
        XmlDoc.Element from = args.element("from");
        XmlDoc.Element to = args.element("to");
        final XmlDoc.Element sue = describeUser(executor(), from);
        final XmlDoc.Element due = describeUser(executor(), to);

        new AtomicTransaction(new AtomicOperation() {
            @Override
            public boolean execute(ServiceExecutor executor) throws Throwable {
                copyUserMetadata(executor(), sue, due);
                return false;
            }
        }).execute(executor());
    }

    static void copyUserMetadata(ServiceExecutor executor, Element sue, Element due) throws Throwable {
        // TODO
    }

    static XmlDoc.Element describeUser(ServiceExecutor executor, XmlDoc.Element user) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        if (user.elementExists("authority")) {
            dm.add(user.element("authority"));
        }
        dm.add(user.element("domain"));
        dm.add(user.element("user"));
        return executor.execute("user.describe", dm.root()).element("user");
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
