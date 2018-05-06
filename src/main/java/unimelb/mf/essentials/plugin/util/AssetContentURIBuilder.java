package unimelb.mf.essentials.plugin.util;

public class AssetContentURIBuilder extends ServerURIBuilder {
    public static final String SERVLET_PATH = "/mflux/content.mfjp";

    public AssetContentURIBuilder() {
        super();
        super.setPath(SERVLET_PATH);
    }

    @Override
    public AssetContentURIBuilder setPath(String path) {
        return this;
    }

    public AssetContentURIBuilder setToken(String token) {
        addParam("_token", token);
        return this;
    }

    public String token() {
        return param("_token");
    }
}
