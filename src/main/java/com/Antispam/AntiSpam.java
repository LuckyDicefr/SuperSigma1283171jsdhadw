package com.antispam;

import org.bukkit.plugin.java.JavaPlugin;

public class AntiSpam extends JavaPlugin {

    private SpamManager spamManager;

    @Override 
    public void onEnable() {
        saveDefaultConfig();
        spamManager = new SpamManager(this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);

        getLogger().info("AntiSpam enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("AntiSpam disabled.");
    }

    public SpamManager getSpamManager() {
        return spamManager;
    }
}