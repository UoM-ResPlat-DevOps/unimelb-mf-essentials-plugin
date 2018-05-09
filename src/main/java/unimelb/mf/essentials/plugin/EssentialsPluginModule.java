package unimelb.mf.essentials.plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import arc.mf.plugin.ConfigurationResolver;
import arc.mf.plugin.PluginModule;
import arc.mf.plugin.PluginService;
import unimelb.mf.essentials.plugin.services.SvcAssetDownloadAtermScriptCreate;
import unimelb.mf.essentials.plugin.services.SvcAssetDownloadAtermScriptUrlCreate;
import unimelb.mf.essentials.plugin.services.SvcAssetDownloadShellScriptCreate;
import unimelb.mf.essentials.plugin.services.SvcAssetDownloadShellScriptUrlCreate;

public class EssentialsPluginModule implements PluginModule {

    private List<PluginService> _services;

    public EssentialsPluginModule() {
        _services = new ArrayList<PluginService>();
        _services.add(new SvcAssetDownloadAtermScriptCreate());
        _services.add(new SvcAssetDownloadAtermScriptUrlCreate());
        _services.add(new SvcAssetDownloadShellScriptCreate());
        _services.add(new SvcAssetDownloadShellScriptUrlCreate());

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
