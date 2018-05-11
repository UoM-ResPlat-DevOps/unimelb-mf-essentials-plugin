package unimelb.mf.essentials.plugin.services;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.Session;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.DateType;
import arc.mf.plugin.dtype.EmailAddressType;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.dtype.XmlDocType;
import arc.utils.DateTime;
import arc.xml.XmlDoc;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;
import unimelb.mf.essentials.plugin.script.download.AssetDownloadScriptWriter;
import unimelb.mf.essentials.plugin.services.SvcAssetDownloadScriptCreate.TargetOS;
import unimelb.mf.essentials.plugin.util.ServerDetails;
import unimelb.utils.URIBuilder;

public abstract class SvcAssetDownloadScriptUrlCreate extends PluginService {

    public static final String EXECUTE_SERVLET_PATH = "/mflux/execute.mfjp";

    public static final String DEFAULT_EMAIL_SUBJECT = "Mediaflux Download Script";

    private Interface _defn;

    protected SvcAssetDownloadScriptUrlCreate() {

        _defn = new Interface();
        /*
         * email notification
         */
        Interface.Element email = new Interface.Element("email", XmlDocType.DEFAULT,
                "email the link to the specified recipients.", 0, 1);
        email.add(new Interface.Attribute("bcc-self", BooleanType.DEFAULT,
                "Send blind carbon copy to the calling user self if the user account has email set. Defaults to true.",
                0));
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
        _defn.add(email);

        /*
         * download (download script specification.)
         */
        Interface.Element download = new Interface.Element("download", XmlDocType.DEFAULT,
                "Download script specification.", 1, 1);

        download.add(new Interface.Element("name", StringType.DEFAULT, "The output script file name.", 0, 1));

        download.add(new Interface.Element("where", StringType.DEFAULT, "The query to select the assets to download.",
                0, 1));

        download.add(new Interface.Element("namespace", StringType.DEFAULT, "The asset namespace to download.", 0,
                Integer.MAX_VALUE));

        // download - target os
        download.add(new Interface.Element("target", new EnumType(TargetOS.values()),
                "The target operating system. If not specified, the scripts for both windows and unix are generated.",
                0, 1));

        // download - server
        Interface.Element server = new Interface.Element("server", XmlDocType.DEFAULT,
                "Mediaflux server details. If not specified, it will try auto-detecting from the current session.", 0,
                1);

        server.add(new Interface.Element("host", StringType.DEFAULT, "server host address", 0, 1));

        server.add(new Interface.Element("port", new IntegerType(0, 65535), "server host address", 0, 1));

        server.add(new Interface.Element("transport", new EnumType(new String[] { "http", "https" }),
                "server transport: http or https?", 0, 1));
        download.add(server);

        // download - token
        Interface.Element downloadToken = new Interface.Element("token", XmlDocType.DEFAULT,
                "Downloader token specification.", 1, 1);
        Interface.Element downloadTokenRole = new Interface.Element("role", new StringType(128),
                "Role (name) to grant. If not specified, defaults to the calling user.", 0, Integer.MAX_VALUE);
        downloadTokenRole.add(new Interface.Attribute("type", new StringType(64), "Role type.", 1));
        downloadToken.add(downloadTokenRole);

        Interface.Element downloadTokenPerm = new Interface.Element("perm", XmlDocType.DEFAULT, "Permission to grant.",
                0, Integer.MAX_VALUE);
        downloadTokenPerm.add(new Interface.Element("access", new StringType(64), "Access type.", 1, 1));
        Interface.Element downloadTokenPermResource = new Interface.Element("resource", new StringType(255),
                "Pattern for resource.", 1, 1);
        downloadTokenPermResource.add(new Interface.Attribute("type", new StringType(32), "Resource type.", 1));
        downloadTokenPerm.add(downloadTokenPermResource);
        downloadToken.add(downloadTokenPerm);

        downloadToken.add(new Interface.Element("from", DateType.DEFAULT,
                "A time, before which the token is not valid. If not supplied token is valid immediately.", 0, 1));
        downloadToken.add(new Interface.Element("to", DateType.DEFAULT,
                "A time, after which the token is no longer valid. If not supplied token will not expire.", 1, 1));
        downloadToken.add(new Interface.Element("use-count", IntegerType.POSITIVE_ONE,
                "The number of times the token may be used.", 0, 1));
        download.add(downloadToken);

        // download - other arguments (to be added by sub-classes)
        addToDownloadDefn(download);
        _defn.add(download);

        /*
         * token (script generator token)
         */
        // download - token
        Interface.Element token = new Interface.Element("token", XmlDocType.DEFAULT,
                "Script generator token specification. It is to specify additional roles and permissions ONLY for the script generator token. In other words, the roles and permissions won't be passed to downloader token.",
                0, 1);
        Interface.Element tokenRole = new Interface.Element("role", new StringType(128),
                "Role (name) to grant. If not specified, defaults to the calling user.", 0, Integer.MAX_VALUE);
        tokenRole.add(new Interface.Attribute("type", new StringType(64), "Role type.", 1));
        token.add(tokenRole);

        Interface.Element tokenPerm = new Interface.Element("perm", XmlDocType.DEFAULT, "Permission to grant.", 0,
                Integer.MAX_VALUE);
        tokenPerm.add(new Interface.Element("access", new StringType(64), "Access type.", 1, 1));
        Interface.Element tokenPermResource = new Interface.Element("resource", new StringType(255),
                "Pattern for resource.", 1, 1);
        tokenPermResource.add(new Interface.Attribute("type", new StringType(32), "Resource type.", 1));
        tokenPerm.add(tokenPermResource);
        token.add(tokenPerm);

        _defn.add(token);
    }

    protected void addToDownloadDefn(Interface.Element download) {

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
    public void execute(Element args, Inputs inputs, Outputs outputs, XmlWriter w) throws Throwable {

        /*
         * check if specified namespaces and roles exist
         */
        SvcAssetDownloadScriptCreate.validateArgs(executor(), args.element("download"));

        String token = createToken(executor(), args);
        if (token != null) {
            String url = null;
            Date expiry = args.dateValue("download/token/to", null);
            try {
                ServerDetails serverDetails = SvcAssetDownloadScriptCreate.resolveServerDetails(executor(),
                        args.element("download/server"));
                url = createUrl(executor(), serverDetails, token);
            } catch (Throwable e) {
                // in case of error, make sure the token is destroyed.
                SvcAssetDownloadScriptCreate.destroyToken(executor(), token);
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

    protected abstract String scriptCreateServiceName();

    private void sendEmail(ServiceExecutor executor, String url, Date expiry, XmlDoc.Element ee) throws Throwable {
        boolean bccSelf = ee.booleanValue("@bcc-self", true);
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
        if (userEmail != null && bccSelf) {
            dm.add("bcc", userEmail);
        }
        if (ee.elementExists("bcc")) {
            Collection<String> bccs = ee.values("bcc");
            for (String bcc : bccs) {
                if (userEmail != null && bccSelf && userEmail.equalsIgnoreCase(bcc)) {
                    // skip, because already added.
                } else {
                    dm.add("bcc", bcc);
                }
            }
        }
        if (ee.elementExists("subject")) {
            dm.add(ee.element("subject"));
        } else {
            dm.add("subject", DEFAULT_EMAIL_SUBJECT);
        }
        dm.add("body", new String[] { "type", "text/html" }, emailMessage(ee.value("body"), url, expiry));
        executor.execute("mail.send", dm.root());
    }

    protected abstract String emailMessage(String message, String url, Date expiry) throws Throwable;

    private String createToken(ServiceExecutor executor, XmlDoc.Element args) throws Throwable {
        XmlDoc.Element dte = args.element("download/token");
        XmlDocMaker dm = new XmlDocMaker("args");
        if (dte != null && dte.elementExists("from")) {
            dm.add(dte.element("from"));
        }
        dm.add(dte.element("to"));
        if (dte != null && dte.elementExists("use-count")) {
            dm.add(dte.element("use-count"));
        }
        /*
         * roles
         */
        // grant unimelb:token-downloader role
        dm.add("role", new String[] { "type", "role" }, AssetDownloadScriptWriter.TOKEN_DOWNLOADER_ROLE);
        if (args.elementExists("token/role") || args.elementExists("download/token/role")) {
            if (args.elementExists("token/role")) {
                // roles only for script generator token
                dm.addAll(args.elements("token/role"));
            }
            if (args.elementExists("download/token/role")) {
                // all download token roles should also be added
                dm.addAll(dte.elements("role"));
            }
        } else {
            // TODO remove
//            XmlDoc.Element ae = executor.execute("actor.self.describe").element("actor");
//            dm.add("role", new String[] { "type", ae.value("@type") }, ae.value("@name"));
        }

        /*
         * perms
         */
        // grant perm to execute the script generator service
        dm.push("perm");
        dm.add("resource", new String[] { "type", "service" }, scriptCreateServiceName());
        dm.add("access", "MODIFY");
        dm.pop();
        if (args.elementExists("token/perm")) {
            // perms only for script generator token
            dm.addAll(args.elements("token/perm"));
        }
        if (args.elementExists("download/token/perm")) {
            // all download token perms should also be added
            dm.addAll(dte.elements("perm"));
        }

        dm.add("min-token-length", 20);
        dm.add("max-token-length", 20);
        dm.add("tag", tokenTag());
        dm.push("service", new String[] { "name", scriptCreateServiceName() });
        dm.add(args.element("download"), false);
        dm.pop();
        return executor.execute("secure.identity.token.create", dm.root()).value("token");
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
