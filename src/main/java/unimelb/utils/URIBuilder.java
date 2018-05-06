package unimelb.utils;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class URIBuilder {

    private String _scheme;
    private String _userInfo;
    private String _host;
    private Integer _port;
    private String _path;
    private Map<String, String> _params;
    private String _fragment;

    public URIBuilder() {

    }

    public URIBuilder setScheme(String scheme) {
        _scheme = scheme;
        return this;
    }

    public String scheme() {
        return _scheme;
    }

    public URIBuilder setUserInfo(String userInfo) {
        _userInfo = userInfo;
        return this;
    }

    public URIBuilder setHost(String host) {
        _host = host;
        return this;
    }

    public String host() {
        return _host;
    }

    public URIBuilder setPath(String path) {
        _path = path;
        return this;
    }

    public String path() {
        return _path;
    }

    public URIBuilder setFragment(String fragment) {
        _fragment = fragment;
        return this;
    }

    public String fragment() {
        return _fragment;
    }

    public URIBuilder setPort(int port) {
        _port = port;
        return this;
    }

    public int port() {
        return _port;
    }

    public URIBuilder addParam(String name, String value) {
        if (_params == null) {
            _params = new LinkedHashMap<String, String>();
        }
        _params.put(name, value);
        return this;
    }

    public boolean hasParams() {
        return _params != null && !_params.isEmpty();
    }

    public String param(String name) {
        if (hasParams()) {
            return _params.get(name);
        }
        return null;
    }

    public Map<String, String> params() {
        if (hasParams()) {
            return Collections.unmodifiableMap(_params);
        }
        return null;
    }

    public String query() throws UnsupportedEncodingException {
        String query = null;
        if (_params != null && !_params.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            Set<String> names = _params.keySet();
            for (String name : names) {
                String value = _params.get(name);
                if (sb.length() > 0) {
                    sb.append("&");
                }
                sb.append(URLEncoder.encode(name, "UTF-8")).append("=").append(URLEncoder.encode(value, "UTF-8"));
            }
            query = sb.toString();
        }
        return query;
    }

    public URI build() throws Throwable {
        return new URI(_scheme, _userInfo, _host, _port, _path, query(), _fragment);
    }

}
