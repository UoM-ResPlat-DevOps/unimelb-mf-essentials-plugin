package unimelb.mf.essentials.plugin.services;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.Session;
import arc.mf.plugin.dtype.EmailAddressType;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.dtype.XmlDocType;
import arc.utils.DateTime;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;
import unimelb.mf.essentials.plugin.util.ServerDetails;
import unimelb.utils.URIBuilder;

public abstract class SvcAssetDownloadScriptUrlCreate extends PluginService {

    public static final String EXECUTE_SERVLET_PATH = "/mflux/execute.mfjp";

    public static final String DEFAULT_EMAIL_SUBJECT = "Mediaflux Download Script";

    protected Interface defn;

    protected SvcAssetDownloadScriptUrlCreate() {
        this.defn = new Interface();
        addToDefn(this.defn);
        SvcAssetDownloadScriptCreate.addToDefn(this.defn);
    }

    static void addToDefn(Interface defn) {
        Interface.Element email = new Interface.Element("email", XmlDocType.DEFAULT,
                "email the link to the specified recipients.", 0, 1);
        email.add(new Interface.Element("from", EmailAddressType.DEFAULT,
                "The reply address. If not set, uses default for the domain.", 0, 1));
        email.add(new Interface.Element("to", EmailAddressType.DEFAULT, "The recipient(s).", 1, Integer.MAX_VALUE));
        email.add(new Interface.Element("cc", EmailAddressType.DEFAULT, "The carbon copy recipient(s).", 0,
                Integer.MAX_VALUE));
        email.add(new Interface.Element("bcc", EmailAddressType.DEFAULT, "The blind carbon copy recipient(s).", 0,
                Integer.MAX_VALUE));
        email.add(new Interface.Element("reply-to", EmailAddressType.DEFAULT, "The reply address.", 0, 1));
        email.add(new Interface.Element("subject", StringType.DEFAULT, "Message subject.", 0, 1));
        email.add(new Interface.Element("body", StringType.DEFAULT, "Message content.", 0, 1));
        defn.add(email);
    }

    @Override
    public Access access() {
        return ACCESS_MODIFY;
    }

    @Override
    public Interface definition() {
        return this.defn;
    }

    @Override
    public void execute(Element args, Inputs inputs, Outputs outputs, XmlWriter w) throws Throwable {
        String token = createToken(executor(), args);
        if (token != null) {
            String url = null;
            Date expiry = args.dateValue("token/to", null);
            try {
                ServerDetails serverDetails = SvcAssetDownloadScriptCreate.resolveServerDetails(executor(),
                        args.element("server"));
                url = createUrl(executor(), serverDetails, token);
            } catch (Throwable e) {
                // in case of error, make sure the token is destroyed.
                destroyToken(executor(), token);
                throw e;
            }
            if (url != null) {
                w.add("url", new String[] { "expiry", DateTime.string(expiry) }, url);
                if (args.elementExists("email")) {
                    sendEmail(executor(), url, expiry, args.element("email"));
                }
            }
        } else {
            throw new Exception("Failed to create token.");
        }
    }

    protected abstract String tokenTag();

    protected abstract String filenamePrefix();

    private void sendEmail(ServiceExecutor executor, String url, Date expiry, XmlDoc.Element ee) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("async", true);
        dm.addAll(ee.elements("to"));
        String userEmail = Session.user().email();
        if (ee.elementExists("from")) {
            dm.add(ee.element("from"));
        } else {
            if (userEmail != null) {
                dm.add("from", userEmail);
            }
        }
        if (ee.elementExists("reply-to")) {
            dm.addAll(ee.elements("reply-to"));
        } else {
            if (userEmail != null) {
                dm.add("reply-to", userEmail);
            }
        }
        if (ee.elementExists("cc")) {
            dm.addAll(ee.elements("cc"));
        }
        if (ee.elementExists("bcc")) {
            dm.addAll(ee.elements("bcc"));
        }
        if (ee.elementExists("subject")) {
            dm.add(ee.element("subject"));
        } else {
            dm.add("subject", DEFAULT_EMAIL_SUBJECT);
        }
        dm.add("body", new String[] { "type", "text/html" }, emailMessage(ee.value("body"), url, expiry));
        executor.execute("mail.send", dm.root());
    }

    protected String emailMessage(String message, String url, Date expiry) throws Throwable {
        StringBuilder sb = new StringBuilder();
        if (message != null) {
            sb.append(message.replaceAll("(\r\n|\n)", "<br/>"));
        } else {
            sb.append("Dear User,<br/><br/>\n");
            // TODO comprehensive message
        }
        sb.append("Please download the <b><a href=\"" + url
                + "\">scripts</a></b> and extract the zip archive. And to download the data from Mediaflux: <br/>");
        sb.append("<ul>\n");
        sb.append("<li>execute .sh script in a terminal window, if you are on Mac OS or Linux.</li>\n");
        sb.append("<li>execute .cmd script in a command prompt window, if you are on Windows platform.</li>\n");
        sb.append("</ul>\n");
        sb.append("<br/><br/>");
        sb.append("<b>Note:</b> The above download link and the embeded credentials will expire at <b>")
                .append(DateTime.string(expiry)).append("</b>.");
        String userFullName = Session.user().fullName();
        if (userFullName != null) {
            sb.append("<br/></br>");
            sb.append(userFullName);
        }
        return sb.toString();
    }

    private String createToken(ServiceExecutor executor, XmlDoc.Element args) throws Throwable {
        XmlDoc.Element te = args.element("token");
        XmlDocMaker dm = new XmlDocMaker("args");
        if (te != null && te.elementExists("from")) {
            dm.add(te.element("from"));
        }
        dm.add(te.element("to"));
        if (te != null && te.elementExists("use-count")) {
            dm.add(te.element("use-count"));
        }
        if (te != null && te.elementExists("role")) {
            boolean hasUserRole = false;
            List<XmlDoc.Element> res = te.elements("role");
            for (XmlDoc.Element re : res) {
                String type = re.value("@type");
                String role = re.value();
                if ("role".equalsIgnoreCase(type) && "user".equalsIgnoreCase(role)) {
                    hasUserRole = true;
                }
                dm.add(re);
            }
            if (!hasUserRole) {
                dm.add("role", new String[] { "type", "role" }, "user");
            }
        } else {
            dm.add("role", new String[] { "type", "role" }, "user");
            dm.add("role", new String[] { "type", "user" },
                    executor.execute("actor.self.describe").value("actor/@name"));
        }
        dm.add("min-token-length", 20);
        dm.add("max-token-length", 20);
        dm.add("tag", tokenTag());
        dm.push("service", new String[] { "name", SvcAssetDownloadAtermScriptCreate.SERVICE_NAME });
        List<XmlDoc.Element> ees = args.elements();
        if (ees != null) {
            for (XmlDoc.Element ee : ees) {
                if (!"email".equals(ee.name())) { // exclude email argument
                    dm.add(ee);
                }
            }
        }
        dm.pop();
        return executor.execute("secure.identity.token.create", dm.root()).value("token");
    }

    private void destroyToken(ServiceExecutor executor, String token) throws Throwable {
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("token", token);
        executor.execute("secure.identity.token.destroy", dm.root());
    }

    private String createUrl(ServiceExecutor executor, ServerDetails serverDetails, String token) throws Throwable {
        URIBuilder ub = new URIBuilder();
        ub.setScheme(serverDetails.transport()).setHost(serverDetails.host()).setPort(serverDetails.port());
        ub.setPath(EXECUTE_SERVLET_PATH);
        ub.addParam("token", token);
        ub.addParam("filename", createFilename());
        return ub.build().toString();
    }

    private String createFilename() {
        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        return new StringBuilder(filenamePrefix()).append("-").append(timeStamp).append(".zip").toString();
    }

}
