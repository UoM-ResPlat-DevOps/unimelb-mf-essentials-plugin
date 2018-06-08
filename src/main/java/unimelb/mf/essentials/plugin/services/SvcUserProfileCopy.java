package unimelb.mf.essentials.plugin.services;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.atomic.AtomicOperation;
import arc.mf.plugin.atomic.AtomicTransaction;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.dtype.XmlDocType;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;

public class SvcUserProfileCopy extends PluginService {

    public static final String SERVICE_NAME = "unimelb.user.profile.copy";

    private Interface _defn;

    public SvcUserProfileCopy() {
        _defn = new Interface();

        addToDefn(_defn);
    }

    static void addToDefn(Interface defn) {
        Interface.Element from = new Interface.Element("from", XmlDocType.DEFAULT, "The source user to copy from.", 1,
                1);

        Interface.Element fromAuthority = new Interface.Element("authority", StringType.DEFAULT,
                "The identity of the authority/repository where the user identity originates. If unspecified, then refers to a user in this repository.",
                0, 1);
        fromAuthority.add(new Interface.Attribute("protocol", StringType.DEFAULT,
                "The protocol of the identity authority. If unspecified, defaults to federated user within the same type of repository.",
                0));
        from.add(fromAuthority);

        from.add(new Interface.Element("domain", StringType.DEFAULT, "The authentication domain the user belongs to", 1,
                1));

        from.add(new Interface.Element("user", StringType.DEFAULT, "The user name.", 1, 1));

        defn.add(from);

        Interface.Element to = new Interface.Element("to", XmlDocType.DEFAULT, "The destination user to copy to.", 1,
                1);

        Interface.Element toAuthority = new Interface.Element("authority", StringType.DEFAULT,
                "The identity of the authority/repository where the user identity originates. If unspecified, then refers to a user in this repository.",
                0, 1);
        toAuthority.add(new Interface.Attribute("protocol", StringType.DEFAULT,
                "The protocol of the identity authority. If unspecified, defaults to federated user within the same type of repository.",
                0));
        to.add(toAuthority);

        to.add(new Interface.Element("domain", StringType.DEFAULT, "The authentication domain the user belongs to", 1,
                1));

        to.add(new Interface.Element("user", StringType.DEFAULT, "The user name.", 1, 1));

        defn.add(to);

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
        return "Copy user profile (asset metadata, settings, permissions) from one to the other.";
    }

    @Override
    public void execute(Element args, Inputs inputs, Outputs outputs, XmlWriter w) throws Throwable {
        final XmlDoc.Element from = args.element("from");
        final XmlDoc.Element to = args.element("to");

        new AtomicTransaction(new AtomicOperation() {
            @Override
            public boolean execute(ServiceExecutor executor) throws Throwable {
                copyUserProfile(executor(), from, to);
                return false;
            }
        }).execute(executor());
    }

    static void copyUserProfile(ServiceExecutor executor, Element from, Element to) throws Throwable {
        SvcUserMetadataCopy.copyUserMetadata(executor, from, to);
        SvcUserPermissionsCopy.copyUserPermissions(executor, from, to);
        SvcUserSettingsCopy.copyUserSettings(executor, from, to);
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

}
