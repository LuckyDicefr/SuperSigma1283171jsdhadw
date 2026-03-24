package com.antispam;

import org.bukkit.entity.Player;

import java.util.*;

public class SpamManager {

    private final AntiSpam plugin;

    private final Map<UUID, Deque<String>> recentMessages = new HashMap<>();
    private final Map<UUID, Long> mutedUntil = new HashMap<>();

    public SpamManager(AntiSpam plugin) {
        this.plugin = plugin;
    }

    public boolean isSpam(Player player, String message) {
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        // Check if still muted
        if (mutedUntil.containsKey(uuid)) {
            long muteEnd = mutedUntil.get(uuid);
            if (now < muteEnd) {
                long secondsLeft = (long) Math.ceil((muteEnd - now) / 1000.0);
                player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                        .deserialize("<red>You are muted for <bold>" + secondsLeft + "s</bold>."));
                return true;
            } else {
                mutedUntil.remove(uuid);
            }
        }

        Deque<String> history = recentMessages.computeIfAbsent(uuid, k -> new ArrayDeque<>());

        // Count how many times this exact message appears in recent history
        long matchCount = history.stream()
                .filter(m -> m.equalsIgnoreCase(message))
                .count();

        // Always add the message to history
        history.addLast(message);
        if (history.size() > 10) {
            history.removeFirst();
        }

        // 4th duplicate -> warn
        if (matchCount == 3) {
            player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                    .deserialize("<red>Don't spam!"));
            return true;
        }

        // 5th duplicate -> mute for 5s
        if (matchCount >= 4) {
            long muteTime = now + 5000L;
            mutedUntil.put(uuid, muteTime);
            player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                    .deserialize("<red>You have been muted for <bold>5s</bold> for spamming."));
            return true;
        }

        return false;
    }

    public void clearPlayer(UUID uuid) {
        recentMessages.remove(uuid);
        mutedUntil.remove(uuid);
    }
}
