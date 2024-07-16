package me.theorenter.concordiaplus.gui;

import me.theorenter.concordiaplus.ConcordiaPlus;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.controlitem.PageItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ForwardItem extends PageItem {

    private final ConcordiaPlus plugin;
    private final Locale locale;

    public ForwardItem(@NotNull final ConcordiaPlus plugin, @NotNull final Locale locale) {
        super(true);
        this.plugin = plugin;
        this.locale = locale;
    }

    @Override
    public ItemProvider getItemProvider(PagedGui<?> gui) {
        ItemStack i = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        ItemMeta meta = i.getItemMeta();

        Component name = plugin.getLoc().get("ui.element.universal.button.nextpage", locale);
        meta.displayName(name);

        List<Component> lore = new ArrayList<>();


        Component elementComponent;

        if (gui.hasPreviousPage())
            elementComponent = plugin.getLoc().get(
                    "ui.element.universal.button.nextpage.status",
                    locale,
                    String.valueOf((gui.getCurrentPage())),
                    String.valueOf(gui.getPageAmount()));
        else
            elementComponent = plugin.getLoc().get(
                    "ui.element.universal.button.nextpage.limit",
                    locale);

        lore.add(elementComponent);
        meta.lore(lore);

        i.setItemMeta(meta);
        return new ItemBuilder(i);
    }
}
