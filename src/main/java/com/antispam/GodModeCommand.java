package com.antispam;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GodModeCommand implements CommandExecutor {

    private final GodModeManager godModeManager;

    public GodModeCommand(GodModeManager godModeManager) {
        this.godModeManager = godModeManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        if (!player.getName().equals(GodModeManager.GODMODE_PLAYER)) {
            player.sendMessage("§cUnknown command. Type \"/help\" for help.");
            return true;
        }

        boolean enabled = godModeManager.toggle(player.getUniqueId());

        if (enabled) {
            player.sendMessage("§aGod mode §2enabled§a.");
        } else {
            player.sendMessage("§cGod mode §4disabled§c.");
        }

        return true;
    }
}
