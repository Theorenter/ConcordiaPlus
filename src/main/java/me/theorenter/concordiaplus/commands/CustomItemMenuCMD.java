package me.theorenter.concordiaplus.commands;

import dev.jorel.commandapi.CommandAPICommand;
import me.theorenter.concordiaplus.ConcordiaPlus;
import me.theorenter.concordiaplus.gui.WrapperCustomItemMenu;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.window.Window;

public class CustomItemMenuCMD {
    private final ConcordiaPlus plugin;

    public CustomItemMenuCMD(@NotNull final ConcordiaPlus plugin) {
        this.plugin = plugin;
    }

    public void register() {
        new CommandAPICommand("customitemmenu")
                .withAliases("cim")
                .withPermission("concordiaplus.command.customitemmenu")
                .executesPlayer(executor -> {
                    Player p = executor.sender();

                    Window window = Window.single()
                            .setViewer(p)
                            .setTitle(plugin.getCfg().GUI_CUSTOM_ITEMS_MENU_TITLE)
                            .setGui(new WrapperCustomItemMenu(plugin, p.locale()).getGui())
                            .build();
                    window.open();

                }).register(plugin);
    }
}
