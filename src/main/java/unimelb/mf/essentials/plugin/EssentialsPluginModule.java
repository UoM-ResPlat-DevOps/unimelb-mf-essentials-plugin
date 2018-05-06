package unimelb.mf.essentials.plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import arc.mf.plugin.ConfigurationResolver;
import arc.mf.plugin.PluginModule;
import arc.mf.plugin.PluginService;
import unimelb.mf.essentials.plugin.services.SvcAssetDownloadAtermScriptCreate;
import unimelb.mf.essentials.plugin.services.SvcAssetDownloadScriptGenerate;
import unimelb.mf.essentials.plugin.services.SvcAssetDownloadShellScriptCreate;

public class EssentialsPluginModule implements PluginModule {

    private List<PluginService> _services;

    public EssentialsPluginModule() {
        _services = new ArrayList<PluginService>();
        _services.add(new SvcAssetDownloadScriptGenerate());
        _services.add(new SvcAssetDownloadAtermScriptCreate());
        _services.add(new SvcAssetDownloadShellScriptCreate());
    }

    public String description() {
        return "Plugin services to send server metrics to Graphite carbon server.";
    }

    public void initialize(ConfigurationResolver conf) throws Throwable {

    }

    public Collection<PluginService> services() {
        return _services;
    }

    public void shutdown(ConfigurationResolver conf) throws Throwable {

    }

    public String vendor() {
        return "Research Platform Services, Infrastructure Services, University Services, The University of Melbourne";
    }

    public String version() {
        return "0.0.1";
    }

}
