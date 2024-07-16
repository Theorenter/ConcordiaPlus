package me.theorenter.concordiaplus.utils.storages;

import me.theorenter.concordiaplus.ConcordiaPlus;
import me.theorenter.concordiaplus.object.CustomItem;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ItemStorage {

    private final ConcordiaPlus plugin;

    private final Map<String, ItemStack> ITEM_MAP;

    public ItemStorage(@NotNull final ConcordiaPlus plugin) {
        this.plugin = plugin;
        this.ITEM_MAP = new HashMap<>();
    }

    public void register(@NotNull final String ID, @NotNull final ItemStack item) {
        ITEM_MAP.put(ID, item);
    }

    public void register(@NotNull final CustomItem customItem) {
        ITEM_MAP.put(customItem.getID(), customItem.getItemStack());
    }

    public void unregister(@NotNull final String ID) {
        ITEM_MAP.remove(ID);
    }

    @Nullable
    public ItemStack getItem(@NotNull final String ID) {
        ItemStack item = ITEM_MAP.get(ID);
        if (item != null)
            return new ItemStack(ITEM_MAP.get(ID));
        else return null;
    }

    public boolean hasItem(@NotNull final String ID) {
        return ITEM_MAP.containsKey(ID);
    }

    public List<ItemStack> getAll() {
        List<ItemStack> list = new ArrayList<>();
        ITEM_MAP.values().forEach(item -> list.add(new ItemStack(item)));
        return list;
    }
}