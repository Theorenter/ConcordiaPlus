package me.theorenter.concordiaplus.utils.loaders;

import me.theorenter.concordiaplus.ConcordiaPlus;
import me.theorenter.concordiaplus.exceptions.NotFoundIdentifierException;
import me.theorenter.concordiaplus.exceptions.IllegalMaterialException;
import me.theorenter.concordiaplus.exceptions.NotFoundMaterialException;
import me.theorenter.concordiaplus.object.CustomItem;
import me.theorenter.concordiaplus.utils.storages.ItemStorage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ItemLoader {
    private final ConcordiaPlus plugin;

    public ItemLoader(@NotNull final ConcordiaPlus plugin, @NotNull final String path) throws IOException {
        this.plugin = plugin;

        if (!(new File(plugin.getDataFolder() + File.separator + path).exists()))
            loadToDataFolder(path);
    }

    private void loadToDataFolder(@NotNull final String path) throws IOException {
        Matcher m = Pattern.compile("plugins/.+\\.jar$")
                .matcher(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().getPath());

        String plPath = null;
        while (m.find()) {
            plPath = m.group(0);
        }

        assert plPath != null;
        try (ZipFile pJAR = new ZipFile(plPath)) {
            String p = (path.replaceAll("\\\\", "\\/"));
            Pattern m2 = Pattern.compile("^"+ p +"/.+$");

            Enumeration<? extends ZipEntry> zipEntries = pJAR.entries();
            while (zipEntries.hasMoreElements()) {
                String fileName = zipEntries.nextElement().getName();
                if (m2.matcher(fileName).find()) {
                    File pdFile = new File(plugin.getDataFolder() + File.separator + fileName);
                    if (!pdFile.exists())
                        plugin.saveResource(fileName, false);
                }
            }
        }
    }

    public void loadAll(@NotNull final String path) {
        File itemDir = new File(plugin.getDataFolder() + File.separator + path);

        File[] itemFiles = itemDir.listFiles();

        if (itemFiles == null || itemFiles.length == 0) {
            plugin.getLogger().warning("No custom items found in ConcordiaPlus\\"+ path +" folder!");
            return;
        }

        int registeredItems = 0;

        for (File itemFile : itemFiles) {
            CustomItem customItem;
            try {
                customItem = loadItem(itemFile);
            } catch (NotFoundMaterialException | NotFoundIdentifierException | IllegalMaterialException ex) {
                continue;
            }

            ItemStorage iStorage = plugin.getCustomItemStorage();
            if (iStorage.getItem(customItem.getID()) == null) {
                iStorage.register(customItem);
                registeredItems++;
            }
        }
        plugin.getLogger().info("Successfully loaded & registered " + registeredItems + " / " + itemFiles.length + " custom items.");
    }
    @NotNull
    private CustomItem loadItem(@NotNull final File itemFile) throws
            NotFoundIdentifierException,
            NotFoundMaterialException,
            IllegalMaterialException {
        ItemStack item;

        FileConfiguration itemData = YamlConfiguration.loadConfiguration(itemFile);

        // requirements
        String ID = itemData.getString("ID");
        String materialStr = itemData.getString("type");

        if (ID == null) {
            plugin.getLogger().severe("Cannot load the custom item:");
            throw new NotFoundIdentifierException("ID not found in the file: \"" + itemFile.getName() + "\"!");
        }

        if (materialStr == null) {
            plugin.getLogger().severe("Cannot load the custom item:");
            throw new NotFoundMaterialException("Material of the item not found in the file: \"" + itemFile.getName() + "\"");
        }

        Material material;
        try {
            material = Material.valueOf(materialStr);
        } catch (IllegalArgumentException ex) {
            plugin.getLogger().severe("Cannot load the custom item:");
            throw new IllegalMaterialException("\"" + itemFile.getName() + "\" file has invalid \"type\" field: \"" + materialStr +"\"!");
        }

        // set requirement fields
        item = new ItemStack(material);

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        pdc.set(new NamespacedKey(plugin, "ID"), PersistentDataType.STRING, ID);

        // optional
        // - meta
        MemorySection metaSection = (MemorySection) itemData.get("meta");
        if (metaSection != null) {

            String displayName = (String) metaSection.get("display-name");
            if (displayName != null)
                meta.displayName(MiniMessage.miniMessage().deserialize(displayName));

            ArrayList<String> loreStr = (ArrayList) metaSection.get("lore");
            if (loreStr != null && !loreStr.isEmpty()) {
                List<Component> lore = new ArrayList<>();
                loreStr.forEach(str -> {
                    if (str == null)
                        str = "";
                    lore.add(MiniMessage.miniMessage().deserialize(str));
                });
                meta.lore(lore);
            }
            // PDC
            List<Object> listPDC = (ArrayList) metaSection.get("persistent-data-container");
            if (listPDC != null && !listPDC.isEmpty()) {
                listPDC.forEach(PDCObject -> {
                    LinkedHashMap<String, Object> PDCData = (LinkedHashMap<String, Object>) PDCObject;

                    String key = (String) PDCData.get("key");
                    String rawType = (String) PDCData.get("type");
                    Object value = PDCData.get("value");

                    PersistentDataType type;

                    if (key == null || rawType == null || value == null)
                        return;

                    switch (rawType.toUpperCase()) {
                        case "STRING": type = PersistentDataType.STRING; break;
                        case "BOOLEAN": type = PersistentDataType.BOOLEAN; break;
                        case "BYTE": type = PersistentDataType.BYTE; break;
                        case "BYTE_ARRAY": type = PersistentDataType.BYTE_ARRAY; break;
                        case "DOUBLE": type = PersistentDataType.DOUBLE; break;
                        case "FLOAT": type = PersistentDataType.FLOAT; break;
                        case "INTEGER": type = PersistentDataType.INTEGER; break;
                        case "INTEGER_ARRAY": type = PersistentDataType.INTEGER_ARRAY; break;
                        case "LONG": type = PersistentDataType.LONG; break;
                        case "LONG_ARRAY": type = PersistentDataType.LONG_ARRAY; break;
                        default: {
                            plugin.getLogger().warning("Invalid persistent data type specified in custom item file \"" + itemFile.getName() + "\" under key \"" + key + "\" (\"" + rawType +"\")!");
                            plugin.getLogger().warning("This persistent data will be skipped.");
                            plugin.getLogger().warning("Available types: STRING, BOOLEAN, BYTE, BYTE_ARRAY, DOUBLE, FLOAT, INTEGER, INTEGER_ARRAY, LONG, LONG_ARRAY");
                            return;
                        }
                    }
                    pdc.set(new NamespacedKey(plugin, key), type, value);
                });
            }
        }


        item.setItemMeta(meta);
        return new CustomItem(ID, item);
    }

}
