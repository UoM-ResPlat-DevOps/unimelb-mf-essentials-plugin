package unimelb.mf.essentials.plugin.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginTask;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcUserFind extends PluginService {

    // TODO
    /*
     * This implementation does not work with LDAP domains. So this service is
     * not enabled. Need to work out ldap domains by doing ldapsearch...
     */

    public static final String SERVICE_NAME = "unimelb.user.find";

    public static final int DEFAULT_PAGE_SIZE = 1000;

    private Interface _defn;

    public SvcUserFind() {
        _defn = new Interface();

        Interface.Element authority = new Interface.Element("authority", StringType.DEFAULT,
                "The identity of the authority/repository where the user identity originates. If unspecified, then refers to a user in this repository.",
                0, 1);
        authority.add(new Interface.Attribute("protocol", StringType.DEFAULT,
                "The protocol of the identity authority. If unspecified, defaults to federated user within the same type of repository.",
                0));
        _defn.add(authority);

        Interface.Element role = new Interface.Element("role", StringType.DEFAULT,
                "If specified, only users holding this role will be included.", 0, Integer.MAX_VALUE);
        role.add(new Interface.Attribute("type", new StringType(64), "Role type."));
        _defn.add(role);

        _defn.add(new Interface.Element("include-disabled", BooleanType.DEFAULT,
                "Include disabled users. Defaults to false.", 0, 1));

        _defn.add(new Interface.Element("domain", StringType.DEFAULT,
                "If specified, only users in this domain will be included.", 0, 1));

        Interface.Element user = new Interface.Element("user", StringType.DEFAULT, "User filter specification.", 0, 1);
        user.add(new Interface.Attribute("operator",
                new EnumType(new String[] { "equals", "starts-with", "ends-with", "contains", "matches" }),
                "Operator. Defaults to equals.", 0));
        user.add(new Interface.Attribute("ignore-case", BooleanType.DEFAULT, "Ignore case. Defaults to true.", 0));
        _defn.add(user);

        Interface.Element name = new Interface.Element("name", StringType.DEFAULT, "User's name filter specification.",
                0, 1);
        name.add(new Interface.Attribute("operator",
                new EnumType(new String[] { "equals", "starts-with", "ends-with", "contains", "matches" }),
                "Operator. Defaults to equals.", 0));
        name.add(new Interface.Attribute("ignore-case", BooleanType.DEFAULT, "Ignore case. Defaults to true.", 0));
        _defn.add(name);

        Interface.Element email = new Interface.Element("email", StringType.DEFAULT,
                "User's email filter specification.", 0, 1);
        email.add(new Interface.Attribute("operator",
                new EnumType(new String[] { "equals", "starts-with", "ends-with", "contains", "matches" }),
                "Operator. Defaults to equals.", 0));
        _defn.add(email);

        _defn.add(new Interface.Element("idx", IntegerType.POSITIVE_ONE,
                "The starting position to list entries. Defaults to 1.", 0, 1));
        _defn.add(new Interface.Element("size", IntegerType.POSITIVE_ONE,
                "The number of entries to retrieve. If not specified, defaults to 100.", 0, 1));

        _defn.add(new Interface.Element("details", BooleanType.DEFAULT, "Output the user details. Defaults to false.",
                0, 1));

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
        return "Find a user.";
    }

    @Override
    public void execute(Element args, Inputs i, Outputs o, XmlWriter w) throws Throwable {
        List<XmlDoc.Element> roles = args.elements("role");

        boolean includeDisabled = args.booleanValue("include-disabled", false);

        boolean details = args.booleanValue("details", false);

        XmlDoc.Element authority = args.element("authority");
        String authorityName = authority == null ? null : authority.value();
        String protocol = authority == null ? null : authority.value("@protocol");

        String domain = args.value("domain");

        String userFilter = args.value("user");
        String userOp = args.stringValue("user/@operator", "equals");
        boolean userIgnoreCase = args.booleanValue("user/@ignore-case", true);

        String nameFilter = args.value("name");
        String nameOp = args.stringValue("name/@operator", "equals");
        boolean nameIgnoreCase = args.booleanValue("name/@ignore-case", true);

        String emailFilter = args.value("email");
        String emailOp = args.stringValue("email/@operator", "equals");

        if (userFilter == null && nameFilter == null && emailFilter == null) {
            throw new IllegalArgumentException("Expects user, name or email arguments. Found none.");
        }

        int idx = args.intValue("idx", 1);
        int size = args.intValue("size", DEFAULT_PAGE_SIZE);

        List<XmlDoc.Element> ues = new ArrayList<XmlDoc.Element>();

        if (domain == null) {
            XmlDocMaker dm = new XmlDocMaker("args");
            if (authority != null) {
                dm.add(authority);
            }
            List<XmlDoc.Element> domainElements = executor().execute("authentication.domain.list", dm.root())
                    .elements("domain");
            if (domainElements != null) {
                for (XmlDoc.Element domainElement : domainElements) {
                    if (domainElement.booleanValue("@enabled", true)) {
                        String domainName = domainElement.value();
                        ues.addAll(findUsers(executor(), includeDisabled, roles, domainName,
                                domainElement.value("@authority"), domainElement.value("@protocol"), userFilter, userOp,
                                userIgnoreCase, nameFilter, nameOp, nameIgnoreCase, emailFilter, emailOp));
                    }
                }
            }
        } else {
            ues.addAll(findUsers(executor(), includeDisabled, roles, domain, authorityName, protocol, userFilter,
                    userOp, userIgnoreCase, nameFilter, nameOp, nameIgnoreCase, emailFilter, emailOp));
        }
        if (!ues.isEmpty()) {
            int from = idx - 1;
            int to = idx - 1 + size;
            int total = ues.size();
            if (from < ues.size()) {
                if (to > ues.size()) {
                    to = ues.size();
                }
                if (from <= to) {
                    List<XmlDoc.Element> rues = ues.subList(from, to);
                    int count = rues.size();
                    for (XmlDoc.Element rue : rues) {
                        if (details) {
                            w.add(rue);
                        } else {
                            w.add("user", new String[] { "id", rue.value("@id"), domain, rue.value("@domain"),
                                    "authority", rue.value("@authority"), "protocol", rue.value("@protocol"),
                                    "internal", rue.value("@internal"), "enabled", rue.value("@enabled"), "aid",
                                    rue.value("asset/@id"), "name", rue.value("@name"), "email", rue.value("@e-mail") },
                                    rue.value("@user"));
                        }
                    }
                    w.add("complete", total == to);
                    w.push("cursor");
                    w.add("from", idx);
                    w.add("to", to - 1);
                    w.add("count", count);
                    w.pop();
                    return;
                }
            }

        }
        w.add("complete", true);
        w.push("cursor");
        w.add("count", 0);
        w.pop();
    }

    private static List<XmlDoc.Element> findUsers(ServiceExecutor executor, boolean includeDisabled,
            List<XmlDoc.Element> roles, String domain, String authority, String protocol, String userFilter,
            String userOp, boolean userIgnoreCase, String nameFilter, String nameOp, boolean nameIgnoreCase,
            String emailFilter, String emailOp) throws Throwable {

        List<XmlDoc.Element> us = new ArrayList<XmlDoc.Element>();

        int idx = 1;
        int size = DEFAULT_PAGE_SIZE;
        boolean complete = false;

        do {
            XmlDocMaker dm = new XmlDocMaker("args");
            if (roles != null && !roles.isEmpty()) {
                dm.addAll(roles);
            }
            dm.add("domain", domain);
            if (authority != null) {
                dm.add("authority", new String[] { "protocol", protocol }, authority);
            }
            dm.add("idx", idx);
            dm.add("size", size);
            PluginTask.checkIfThreadTaskAborted();
            XmlDoc.Element re = executor.execute("user.describe", dm.root());
            List<XmlDoc.Element> ues = re.elements("user");
            if (ues != null) {
                for (XmlDoc.Element ue : ues) {
                    boolean enabled = ue.booleanValue("@enabled", true);
                    if (includeDisabled || enabled) {
                        if (accept(ue, userFilter, userOp, userIgnoreCase, nameFilter, nameOp, nameIgnoreCase,
                                emailFilter, emailOp)) {
                            us.add(ue);
                        }
                    }
                }
            }
            complete = re.booleanValue("complete");
            idx += size;
        } while (!complete);

        return us;
    }

    private static boolean accept(Element ue, String userFilter, String userOp, boolean userIgnoreCase,
            String nameFilter, String nameOp, boolean nameIgnoreCase, String emailFilter, String emailOp)
            throws Throwable {
        String user = ue.value("@user");
        if (userFilter != null) {
            if (!match(user, userOp, userFilter, userIgnoreCase)) {
                return false;
            }
        }
        if (nameFilter != null) {
            String name = ue.value("name");
            String firstName = ue.value("asset/meta/mf-user/name[@type='first']");
            String middleName = ue.value("asset/meta/mf-user/name[@type='middle']");
            String lastName = ue.value("asset/meta/mf-user/name[@type='last']");
            Set<String> names = new LinkedHashSet<String>();
            if (name != null) {
                names.addAll(Arrays.asList(name.split("\\ +")));
            }
            if (firstName != null) {
                names.add(firstName);
            }
            if (middleName != null) {
                names.add(middleName);
            }
            if (lastName != null) {
                names.add(lastName);
            }
            names.add(user);
            if (!matchAny(names, nameOp, nameFilter, nameIgnoreCase)) {
                return false;
            }
        }
        if (emailFilter != null) {
            String email1 = ue.value("e-mail");
            String email2 = ue.value("asset/meta/mf-user/email");
            if (email1 == null && email2 == null) {
                return false;
            }
            Set<String> emails = new LinkedHashSet<String>();
            if (email1 != null) {
                emails.add(email1.toLowerCase());
            }
            if (email2 != null) {
                emails.add(email2.toLowerCase());
            }
            if (!matchAny(emails, emailOp, emailFilter, true)) {
                return false;
            }
        }
        return true;
    }

    private static boolean matchAny(Collection<String> values, String op, String filterValue, boolean ignoreCase) {
        for (String value : values) {
            if (match(value, op, filterValue, ignoreCase)) {
                return true;
            }
        }
        return false;
    }

    private static boolean match(String value, String op, String filterValue, boolean ignoreCase) {
        // "equals", "starts-with", "ends-with", "contains", "matches"

        if ("starts-with".equalsIgnoreCase(op)) {
            if (ignoreCase) {
                return value.toLowerCase().startsWith(filterValue.toLowerCase());
            } else {
                return value.startsWith(filterValue);
            }
        } else if ("ends-with".equalsIgnoreCase(op)) {
            if (ignoreCase) {
                return value.toLowerCase().endsWith(filterValue.toLowerCase());
            } else {
                return value.endsWith(filterValue);
            }
        } else if ("contains".equalsIgnoreCase(op)) {
            if (ignoreCase) {
                return value.toLowerCase().contains(filterValue.toLowerCase());
            } else {
                return value.contains(filterValue);
            }
        } else if ("matches".equalsIgnoreCase(op)) {
            return value.matches(filterValue);
        } else {
            // defaults to equals
            // assert "equals".equalsIgnoreCase(userOp);
            if (ignoreCase) {
                return value.equalsIgnoreCase(filterValue);
            } else {
                return value.equals(filterValue);
            }
        }
    }

    @Override
    public String name() {
        return SERVICE_NAME;
    }

    @Override
    public boolean canBeAborted() {
        return true;
    }

}
