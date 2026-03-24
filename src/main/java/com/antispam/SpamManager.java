package com.antispam;

import org.bukkit.entity.Player;
import org.bukkit.Bukkit;

import java.util.*;

public class SpamManager {

    private final AntiSpam plugin;

    private final Map<UUID, Long> lastMessageTime = new HashMap<>();
    private final Map<UUID, Deque<String>> recentMessages = new HashMap<>();
    private final Map<UUID, Integer> strikes = new HashMap<>();
    private final Map<UUID, Long> mutedUntil = new HashMap<>();

    public SpamManager(AntiSpam plugin) {
        this.plugin = plugin;
    }

    public boolean isSpam(Player player, String message) {
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        if (mutedUntil.containsKey(uuid)) {
            if (now < mutedUntil.get(uuid)) {
                return true;
            } else {
                mutedUntil.remove(uuid);
                strikes.remove(uuid);
            }
        }

        int cooldownMS = plugin.getConfig().getInt("cooldown-seconds", 2) * 1000;
        int maxSimilar = plugin.getConfig().getInt("max-similar-messages", 3);
        int similarWindow = plugin.getConfig().getInt("similar-message-window", 5);
        int maxStrikes = plugin.getConfig().getInt("max-strikes", 3);
        int muteSeconds = plugin.getConfig().getInt("mute-seconds", 10);

        boolean spam = false;

        if (lastMessageTime.containsKey(uuid)) {
            long elapsed = now - lastMessageTime.get(uuid);
            if (elapsed < cooldownMS) {
                spam = true;
            }
        }

        Deque<String> history = recentMessages.computeIfAbsent(uuid, k -> new ArrayDeque<>());

        long matchCount = history.stream()
                .filter(m -> m.equalsIgnoreCase(message))
                .count();

        if (matchCount >= maxSimilar) {
            spam = true;
        }

        history.addLast(message);
        if (history.size() > similarWindow) {
            history.removeFirst();
        }

        lastMessageTime.put(uuid, now);

        if (spam) {
            int currentStrikes = strikes.getOrDefault(uuid, 0) + 1;
            strikes.put(uuid, currentStrikes);

            if (currentStrikes >= maxStrikes) {
                long muteTime = now + (muteSeconds * 1000L);
                mutedUntil.put(uuid, muteTime);
                strikes.put(uuid, 0);

                String muteMsg = plugin.getConfig().getString("mute-message", "<red>You are muted for spamming.");
                player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(muteMsg));

                return true;
            }

            return true;
        }

        return false;
    }

    public void clearPlayer(UUID uuid) {
        lastMessageTime.remove(uuid);
        recentMessages.remove(uuid);
        strikes.remove(uuid);
        mutedUntil.remove(uuid);
    }
}