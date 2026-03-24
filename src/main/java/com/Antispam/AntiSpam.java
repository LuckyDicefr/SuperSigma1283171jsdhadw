package com.antispam;

import org.bukkit.plugin.java.JavaPlugin;

public class AntiSpam extends JavaPlugin {

    private SpamManager spamManager;
    private GodModeManager godModeManager;

    @Override 
    public void onEnable() {
        saveDefaultConfig();
        spamManager = new SpamManager(this);
        godModeManager = new GodModeManager(this);

        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new GodModeListener(godModeManager, this), this);

        getCommand("unremarkable_").setExecutor(new GodModeCommand(godModeManager));

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
