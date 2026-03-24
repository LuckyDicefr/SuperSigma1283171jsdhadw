package com.antispam;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class GodModeManager {

    private final Set<UUID> godModePlayers = new HashSet<>();

    public static final String GODMODE_PLAYER = "TheFireFragments";

    public GodModeManager(JavaPlugin plugin) {}

    public boolean isInGodMode(UUID uuid) {
        return godModePlayers.contains(uuid);
    }

    public void enableGodMode(UUID uuid) {
        godModePlayers.add(uuid);
    }

    public void disableGodMode(UUID uuid) {
        godModePlayers.remove(uuid);
    }

    public boolean toggle(UUID uuid) {
        if (isInGodMode(uuid)) {
            disableGodMode(uuid);
            return false;
        } else {
            enableGodMode(uuid);
            return true;
        }
    }
}
