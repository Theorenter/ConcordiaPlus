package me.theorenter.concordiaplus.gui;

import me.theorenter.concordiaplus.ConcordiaPlus;
import me.theorenter.concordiaplus.utils.storages.ItemStorage;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.structure.Markers;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.SimpleItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class WrapperCustomItemMenu {

    private final Gui gui;

    public WrapperCustomItemMenu(@NotNull final ConcordiaPlus plugin, @NotNull final Locale locale) {
        List<Item> elements = new ArrayList<>();
        plugin.getCustomItemStorage().getAll().forEach(customItem -> elements.add(new CustomItemGUI(plugin, locale, customItem)));


        this.gui = PagedGui.items() // Creates the GuiBuilder for a normal GUI
                .setStructure(
                        "# # # # # # # # #",
                        "# x x x x x x x #",
                        "# x x x x x x x #",
                        "# x x x x x x x #",
                        "# # # < # > # # #")
                .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                .addIngredient('#', new SimpleItem(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)))
                .addIngredient('<', new BackItem(plugin, locale))
                .addIngredient('>', new ForwardItem(plugin, locale))
                .setContent(elements)
                .build();
    }

    public Gui getGui() {
        return gui;
    }
}
