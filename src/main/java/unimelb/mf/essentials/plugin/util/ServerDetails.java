package unimelb.mf.essentials.plugin.util;

import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;

public class ServerDetails {

    private String _host;
    private String _transport;
    private int _port;

    public ServerDetails(String transport, String host, int port) {
        _transport = transport;
        _host = host;
        _port = port;
    }

    public String host() {
        return _host;
    }

    public int port() {
        return _port;
    }

    public String transport() {
        return _transport;
    }

    public static ServerDetails resolve(ServiceExecutor executor) throws Throwable {
        String host = executor.execute("system.session.self.describe").value("session/host");
        XmlDocMaker dm = new XmlDocMaker("args");
        dm.add("type", "http");
        XmlDoc.Element re = executor.execute("network.describe", dm.root());
        if (!re.elementExists("service")) {
            throw new Exception("No http service is enabled on Mediaflux server.");
        }
        XmlDoc.Element se = re.element("service[@ssl='true']");
        if (se != null) {
            String transport = "https";
            int port = se.intValue("@port");
            return new ServerDetails(transport, host, port);
        } else {
            se = re.element("service[@ssl='false']");
            if (se != null) {
                String transport = "http";
                int port = se.intValue("@port");
                return new ServerDetails(transport, host, port);
            } else {
                throw new Exception("No http service is enabled on Mediaflux server.");
            }
        }
    }
}
