package com.antispam;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.entity.Player;

public class ChatListener implements Listener {

    private final AntiSpam plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public ChatListener(AntiSpam plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void OnChat(AsyncChatEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission("antispam.bypass")) return;

        String raw = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
        .plainText().serialize(event.message());

        if (plugin.getSpamManager().isSpam(player, raw)) {
            event.setCancelled(true);

            String msg = plugin.getConfig().getString("spam-message", "<red>dont spam");

            player.sendMessage(mm.deserialize(msg));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {

        plugin.getSpamManager().clearPlayer(event.getPlayer().getUniqueId());
            }
}