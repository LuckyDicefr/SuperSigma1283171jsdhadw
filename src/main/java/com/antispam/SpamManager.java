package com.antispam;

import org.bukkit.entity.Player;

import java.util.*;

public class SpamManager {

    private final AntiSpam plugin;

    private final Map<UUID, Deque<String>> recentMessages = new HashMap<>();
    private final Map<UUID, Long> mutedUntil = new HashMap<>();
    private final Map<UUID, Long> muteDuration = new HashMap<>();

    public SpamManager(AntiSpam plugin) {
        this.plugin = plugin;
    }

    private int levenshtein(String a, String b) {
        a = a.toLowerCase();
        b = b.toLowerCase();
        int[] dp = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) dp[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            int prev = dp[0];
            dp[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                int temp = dp[j];
                dp[j] = a.charAt(i - 1) == b.charAt(j - 1)
                        ? prev
                        : 1 + Math.min(prev, Math.min(dp[j], dp[j - 1]));
                prev = temp;
            }
        }
        return dp[b.length()];
    }

    private boolean isSimilar(String a, String b) {
        int maxLen = Math.max(a.length(), b.length());
        if (maxLen == 0) return true;
        int dist = levenshtein(a, b);
        return (double) dist / maxLen <= 0.3;
    }

    public boolean isSpam(Player player, String message) {
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

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

        long similarCount = history.stream()
                .filter(m -> isSimilar(m, message))
                .count();

        history.addLast(message);
        if (history.size() > 10) {
            history.removeFirst();
        }

        if (similarCount == 3) {
            player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                    .deserialize("<red>Don't spam!"));
            return true;
        }

        if (similarCount >= 4) {
            long lastDuration = muteDuration.getOrDefault(uuid, 30000L);
            long newDuration = lastDuration * 2;
            muteDuration.put(uuid, newDuration);
            mutedUntil.put(uuid, now + newDuration);

            long seconds = newDuration / 1000;
            player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                    .deserialize("<red>You have been muted for <bold>" + seconds + "s</bold> for spamming."));
            return true;
        }

        return false;
    }

    public void clearPlayer(UUID uuid) {
        recentMessages.remove(uuid);
        mutedUntil.remove(uuid);
        muteDuration.remove(uuid);
    }
}
