package com.antispam;

import org.bukkit.entity.Player;

import java.util.*;

public class SpamManager {

    private final AntiSpam plugin;

    private final Map<UUID, Deque<String>> recentMessages = new HashMap<>();
    private final Map<UUID, Long> mutedUntil = new HashMap<>();
    private final Map<UUID, Long> muteDuration = new HashMap<>(); // tracks current mute length to double it

    public SpamManager(AntiSpam plugin) {
        this.plugin = plugin;
    }

    // Returns similarity ratio between 0 and 1
    private double similarity(String a, String b) {
        a = a.toLowerCase();
        b = b.toLowerCase();
        if (a.equals(b)) return 1.0;

        int maxLen = Math.max(a.length(), b.length());
        if (maxLen == 0) return 1.0;

        // Count matching characters using a sliding window approach
        int matches = 0;
        String shorter = a.length() <= b.length() ? a : b;
        String longer  = a.length() <= b.length() ? b : a;

        for (int i = 0; i < shorter.length(); i++) {
            if (i < longer.length() && shorter.charAt(i) == longer.charAt(i)) {
                matches++;
            }
        }

        // Also count shared characters regardless of position
        Map<Character, Integer> aChars = new HashMap<>();
        for (char c : a.toCharArray()) aChars.merge(c, 1, Integer::sum);

        int shared = 0;
        for (char c : b.toCharArray()) {
            if (aChars.getOrDefault(c, 0) > 0) {
                shared++;
                aChars.merge(c, -1, Integer::sum);
            }
        }

        return (double)(matches + shared) / (maxLen * 2.0);
    }

    private boolean isSimilarToHistory(Deque<String> history, String message) {
        for (String past : history) {
            if (similarity(past, message) >= 0.7) {
                return true;
            }
        }
        return false;
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

        // Count how many recent messages are similar to this one
        long similarCount = history.stream()
                .filter(m -> similarity(m, message) >= 0.7)
                .count();

        // Always add message to history
        history.addLast(message);
        if (history.size() > 10) {
            history.removeFirst();
        }

        // 4th similar message -> warn
        if (similarCount == 3) {
            player.sendMessage(net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
                    .deserialize("<red>Don't spam!"));
            return true;
        }

        // 5th similar message -> mute, doubling each time
        if (similarCount >= 4) {
            long lastDuration = muteDuration.getOrDefault(uuid, 30000L); // half of 60 so first mute = 60s
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
