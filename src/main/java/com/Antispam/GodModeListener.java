package com.antispam;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

public class GodModeListener implements Listener {

    private final GodModeManager godModeManager;
    private final AntiSpam plugin;

    private static final double HALF_HEART = 1.0;

    public GodModeListener(GodModeManager godModeManager, AntiSpam plugin) {
        this.godModeManager = godModeManager;
        this.plugin = plugin;
    }

    private boolean isGodModeActive(Player player) {
        if (!godModeManager.isInGodMode(player.getUniqueId())) return false;

        ItemStack mainhand = player.getInventory().getItemInMainHand();
        ItemStack offhand  = player.getInventory().getItemInOffHand();

        boolean holdingTotem = (mainhand.getType() == Material.TOTEM_OF_UNDYING)
                            || (offhand.getType()  == Material.TOTEM_OF_UNDYING);

        return !holdingTotem;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!isGodModeActive(player)) return;

        double currentHealth = player.getHealth();
        double finalDamage = event.getFinalDamage();
        double healthAfterDamage = currentHealth - finalDamage;

        if (healthAfterDamage < HALF_HEART) {
            if (currentHealth > HALF_HEART) {
                double rawDamage = event.getDamage();
                double targetFinal = currentHealth - HALF_HEART;
                if (finalDamage > 0) {
                    double scale = targetFinal / finalDamage;
                    event.setDamage(Math.max(0, rawDamage * scale));
                } else {
                    event.setDamage(0);
                }
            } else {
                event.setDamage(0);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        if (!isGodModeActive(player)) return;

        event.setCancelled(true);

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (player.isOnline()) {
                player.setHealth(HALF_HEART);
            }
        });
    }
}
