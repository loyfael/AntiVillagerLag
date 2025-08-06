package loyfael.antiVillagerLag;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.MultiLineChart;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import loyfael.antiVillagerLag.commands.OptimizeCommand;
import loyfael.antiVillagerLag.commands.ReloadCommand;
import loyfael.antiVillagerLag.commands.RemoveChangesCommand;
import loyfael.antiVillagerLag.commands.UnoptimizeCommand;
import loyfael.antiVillagerLag.commands.StatusCommand;
import loyfael.antiVillagerLag.commands.InfoCommand;
import loyfael.antiVillagerLag.events.EventListener;
import loyfael.antiVillagerLag.utils.UpdateChecker;
import loyfael.antiVillagerLag.utils.VillagerUtilities;
import loyfael.antiVillagerLag.utils.TaskManager;
import loyfael.antiVillagerLag.utils.VillagerCache;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class AntiVillagerLag extends JavaPlugin {

    @Override
    public void onEnable() {

        // Initialisation des optimisations de performance
        VillagerUtilities.initializeKeys(this);
        TaskManager.initialize(this);

        //  Command Registration
        getCommand("avlreload").setExecutor(new ReloadCommand(this));
        getCommand("avloptimize").setExecutor(new OptimizeCommand(this));
        getCommand("avlunoptimize").setExecutor(new UnoptimizeCommand(this));
        getCommand("avlremove").setExecutor(new RemoveChangesCommand(this));
        getCommand("avlstatus").setExecutor(new StatusCommand(this));
        getCommand("avlinfo").setExecutor(new InfoCommand(this));

        //  Event Registration
        getServer().getPluginManager().registerEvents(new EventListener(this), this);

        //  Config Stuff
        saveDefaultConfig();
        updateConfig();

        TaskManager.runAsync(() -> {
            VillagerUtilities.updateNameTags(this);
            VillagerUtilities.updateStandingOnBlocks(this);
            VillagerUtilities.updateWorkstationBlocks(this);
            VillagerUtilities.updateRestockTimes(this);
        }).thenRun(() -> {
            getLogger().info("AntiVillagerLag optimizations loaded - ready for 2000+ villagers!");
        });

        //  Bstats Code
        int pluginId = 15890;
        Metrics metrics = new Metrics(this, pluginId);
        //  Optional: Add custom charts
        metrics.addCustomChart(new MultiLineChart("players_and_servers", () -> {
            Map<String, Integer> valueMap = new HashMap<>();
            valueMap.put("servers", 1);
            valueMap.put("players", Bukkit.getOnlinePlayers().size());
            return valueMap;
        }));

        //  Check for plugin updates
        new UpdateChecker(this, 102949).getVersion(version -> {
            if (this.getDescription().getVersion().equals(version)) {
                getLogger().info("AntiVillagerLag is up to date!");
            } else {
                getLogger().info("There is an update for AntiVillagerLag! https://www.spigotmc.org/resources/antivillagerlag.102949/");
            }
        });
    }

    @Override
    public void onDisable() {
        TaskManager.shutdown();
        VillagerCache.clearCache();

        getLogger().info("AntiVillagerLag optimizations cleaned up");
    }

    //  Configuration File Updater
    public Configuration cfg = this.getConfig().getDefaults();
    public void updateConfig() {
        try {
            if(new File(getDataFolder() + "/config.yml").exists()) {
                boolean changesMade = false;
                YamlConfiguration tmp = new YamlConfiguration();
                tmp.load(getDataFolder() + "/config.yml");
                for(String str : cfg.getKeys(true)) {
                    if(!tmp.getKeys(true).contains(str)) {
                        tmp.set(str, cfg.get(str));
                        changesMade = true;
                    }
                }
                if(changesMade)
                    tmp.save(getDataFolder() + "/config.yml");
            }
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }
}
