package me.theorenter.concordiaplus;

import me.theorenter.concordiaplus.commands.CustomItemMenuCMD;
import me.theorenter.concordiaplus.localization.Localization;
import me.theorenter.concordiaplus.utils.loaders.ItemLoader;
import me.theorenter.concordiaplus.utils.loaders.RecipeLoader;
import me.theorenter.concordiaplus.utils.storages.ItemStorage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public final class ConcordiaPlus extends JavaPlugin {

    private Config config;
    private Localization localization;
    private ItemLoader itemLoader;
    private RecipeLoader recipeLoader;
    private ItemStorage itemStorage;

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void onEnable() {
        Bukkit.getLogger().info("================     ConfigurableVillagers     =================");
        loadConfig();
        loadLocalization();
        loadLoaders();
        loadStorages();
        registerCommands();

        itemLoader.loadAll("data" + File.separator + "items");
        recipeLoader.registerAll("data" + File.separator + "recipes");
        showStatus();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    /**
     * Shows the plugin enabling status.
     */
    @SuppressWarnings("UnstableApiUsage")
    private void showStatus() {
        Bukkit.getLogger().info("================================================================");
        if(this.isEnabled())
            getLogger().info("Version: "+ getPluginMeta().getVersion() + " – Plugin Enabled");
        else
            getLogger().info("Version: "+ getPluginMeta().getVersion() + " – Plugin Disabled");
        Bukkit.getLogger().info("================================================================");
    }

    @SuppressWarnings("UnstableApiUsage")
    private void loadConfig() {
        try {
            this.config = new Config(this);
            getLogger().info("Configuration successfully loaded.");
        } catch (IOException | InvalidConfigurationException ex) {
            this.setEnabled(false);
            getLogger().severe("An error occurred while loading the configuration:");
            throw new RuntimeException(ex);
        }
    }

    @SuppressWarnings({"UnstableApiUsage", "ConstantConditions"})
    private void loadLocalization() {
        try {
            this.localization = new Localization(this,
                    config.LOCALIZATION_CLIENT_ORIENTED,
                    "settings" + File.separator + "localization",
                    "reference",
                    config.DEFAULT_LOCALIZATION);
            getLogger().info("Localization successfully loaded.");
        } catch (IOException | InvalidConfigurationException ex) {
            getLogger().severe("An error occurred while loading the localization:");
            this.setEnabled(false);
            throw new RuntimeException(ex);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private void loadLoaders() {
        try {
            this.itemLoader = new ItemLoader(this, "data" + File.separator + "items");
        } catch (IOException ex) {
            this.setEnabled(false);
            getLogger().severe("An error occurred while loading the itemLoader:");
            throw new RuntimeException(ex);
        }

        try {
            this.recipeLoader = new RecipeLoader(this, "data" + File.separator + "recipes");
        } catch (IOException ex) {
            this.setEnabled(false);
            getLogger().severe("An error occurred while loading the recipeLoader:");
            throw new RuntimeException(ex);
        }
    }

    private void loadStorages() {
        this.itemStorage = new ItemStorage(this);

    }

    private void registerCommands() {
        new CustomItemMenuCMD(this).register();

        getLogger().info("Plugin commands successfully loaded.");
    }

    private void registerListeners() {
        //PluginManager pm = this.getServer().getPluginManager();
        //pm.registerEvent(new CustomItemMenuCMD(this), this);
    }

    public ItemStorage getCustomItemStorage() {
        return itemStorage;
    }

    @NotNull
    public Config getCfg() {
        return this.config;
    }

    @NotNull
    public Localization getLoc() {
        return this.localization;
    }
}
