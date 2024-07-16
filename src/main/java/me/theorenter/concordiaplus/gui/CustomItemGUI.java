package me.theorenter.concordiaplus.gui;

import me.theorenter.concordiaplus.ConcordiaPlus;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

import java.util.Locale;

public class CustomItemGUI extends AbstractItem {

    private final ConcordiaPlus plugin;
    private final Locale locale;
    private final ItemStack item;
    private boolean isLoreShown;

    public CustomItemGUI(@NotNull final ConcordiaPlus plugin, @NotNull final Locale locale, @NotNull final ItemStack item) {
        this.item = item;
        this.plugin = plugin;
        this.locale = locale;
        ItemMeta meta = item.getItemMeta();

        this.isLoreShown = false;
        meta.lore(plugin.getLoc().getNumberedList("ui.element.customitems.button.item.lore", locale));
        item.setItemMeta(meta);
    }

    @Override
    public ItemProvider getItemProvider() {
        return new ItemBuilder(item);
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
        if (clickType.isLeftClick()) {
            PlayerInventory playerInv = player.getInventory();
            if (playerInv.firstEmpty() == -1) {
                player.sendMessage(plugin.getLoc().get("message.error.your_inventory_is_full", locale));
                return;
            }
            player.getInventory().addItem(plugin.getCustomItemStorage().getItem(this.item.getItemMeta().getPersistentDataContainer().get(NamespacedKey.fromString("id", plugin), PersistentDataType.STRING)));
            player.playSound(plugin.getCfg().GUI_ITEM_GIVE_SOUND);
            return;
        }

        if (clickType.isRightClick()) {
            ItemMeta meta = item.getItemMeta();

            if (isLoreShown) {
                meta.lore(plugin.getLoc().getNumberedList("ui.element.customitems.button.item.lore", locale));
                item.setItemMeta(meta);
                isLoreShown = false;
            } else {
                meta.lore(
                        plugin.getCustomItemStorage().getItem(this.item.getItemMeta().getPersistentDataContainer().get(NamespacedKey.fromString("id", plugin), PersistentDataType.STRING)).lore()
                );
                item.setItemMeta(meta);
                isLoreShown = true;
            }
            player.playSound(plugin.getCfg().GUI_CLICK_SOUND);
            notifyWindows();
            return;
        }
    }
}
