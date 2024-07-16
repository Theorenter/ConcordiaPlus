package me.theorenter.concordiaplus;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public final class Config {

    @NotNull public final String DEFAULT_LOCALIZATION;
    public final boolean LOCALIZATION_CLIENT_ORIENTED;
    @NotNull public final String GUI_CUSTOM_ITEMS_MENU_TITLE;
    @NotNull public final net.kyori.adventure.sound.Sound GUI_ITEM_GIVE_SOUND;
    @NotNull public final net.kyori.adventure.sound.Sound GUI_CLICK_SOUND;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Config(@NotNull ConcordiaPlus plugin) throws IOException, InvalidConfigurationException {
        File file;
        FileConfiguration fileConfig;

        file = new File(plugin.getDataFolder() + File.separator + "settings",
                "configuration.yml");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            plugin.saveResource("settings" + File.separator + "configuration.yml", false);
        }

        fileConfig = new YamlConfiguration();
        fileConfig.load(file);

        DEFAULT_LOCALIZATION = fileConfig.getString("language.default-localization");
        LOCALIZATION_CLIENT_ORIENTED = fileConfig.getBoolean("language.client-based");
        GUI_CUSTOM_ITEMS_MENU_TITLE = fileConfig.getString("settings.custom-items-gui.name");
        GUI_CLICK_SOUND = giveSound(fileConfig, "settings.custom-items-gui.click-sound");
        GUI_ITEM_GIVE_SOUND = giveSound(fileConfig, "settings.custom-items-gui.item-give-sound");
    }

    private Sound giveSound(@NotNull final FileConfiguration fileConfig, @NotNull final String path) {
        float p = (float) fileConfig.getDouble(path + ".pitch");
        float v = (float) fileConfig.getDouble(path + ".volume");
        String sourceStr = fileConfig.getString(path + ".source");
        String soundPath = fileConfig.getString(path + ".sound");
        if (soundPath == null) {

        }

        String[] strings = soundPath.split(":");

        if (strings.length != 2) {

        }

        String space = strings[0].toLowerCase();
        String name = strings[1].toLowerCase();
        return Sound.sound(Key.key(space, name), Sound.Source.PLAYER, v, p);
    }
}
