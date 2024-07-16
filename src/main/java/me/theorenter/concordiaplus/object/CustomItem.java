package me.theorenter.concordiaplus.object;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CustomItem {
    private final String ID;
    private final ItemStack itemStack;

    public CustomItem(@NotNull final String ID, @NotNull final ItemStack itemStack) {
        this.ID = ID;
        this.itemStack = itemStack;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public String getID() {
        return ID;
    }
}
