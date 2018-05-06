package unimelb.mf.essentials.plugin.util;

import java.net.URI;

import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import unimelb.utils.URIBuilder;

public class ServerURIBuilder extends URIBuilder {

    public ServerURIBuilder() {
        super();
    }

    public String transport() {
        return scheme();
    }

    public ServerURIBuilder setTransport(boolean ssl) {
        if (ssl) {
            super.setScheme("https");
        } else {
            super.setScheme("http");
        }
        return this;
    }

    public ServerURIBuilder resolveServerDetails(ServiceExecutor executor) throws Throwable {
        String serverHost = executor.execute("system.session.self.describe").value("session/host");
        setHost(serverHost);

        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("type", "http");
        XmlDoc.Element re = executor.execute("network.describe", dm.root());
        if (!re.elementExists("service")) {
            throw new Exception("No http service is enabled on Mediaflux server.");
        }
        XmlDoc.Element se = re.element("service[@ssl='true']");
        if (se != null) {
            setTransport(true);
            setPort(se.intValue("@port"));
        } else {
            se = re.element("service[@ssl='false']");
            if (se != null) {
                setTransport(true);
                setPort(se.intValue("@port"));
            } else {
                throw new Exception("No http service is enabled on Mediaflux server.");
            }
        }
        return this;
    }



    public URI build(String assetId, String fileName) throws Throwable {
        if (host() == null) {
            throw new Exception("Mediaflux server host is not set.");
        }
        if (port() < 0) {
            throw new Exception("Mediaflux server port is not set.");
        }
        if (scheme() == null) {
            throw new Exception("Mediaflux server transport is not set.");
        }
        if (assetId != null) {
            addParam("id", assetId);
        }
        if (fileName != null) {
            addParam("filename", fileName);
        }
        return super.build();
    }

}
