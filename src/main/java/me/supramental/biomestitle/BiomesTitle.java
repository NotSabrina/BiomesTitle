package me.supramental.biomestitle;

import me.supramental.biomestitle.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.*;
import me.clip.placeholderapi.PlaceholderHook;

public class BiomesTitle extends JavaPlugin {

    int serviceId = 22389;
    Metrics metrics = new Metrics(this, serviceId);
    private boolean isPlaceholderAPIAvailable = false;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        Plugin placeholderapiPlugin = getServer().getPluginManager().getPlugin("PlaceholderAPI");
        if (placeholderapiPlugin != null && placeholderapiPlugin.isEnabled()) {
            isPlaceholderAPIAvailable = true;
            getLogger().info("PlaceholderAPI detected. Enabling PlaceholderAPI support.");
        } else {
            getLogger().info("PlaceholderAPI not found. Running without PlaceholderAPI support.");
        }

        Bukkit.getPluginManager().registerEvents(new BiomeListener(this, isPlaceholderAPIAvailable), this);
        getLogger().info("BiomesTitle has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("BiomesTitle has been disabled!");
    }

    public boolean isPlaceholderAPIAvailable() {
        return isPlaceholderAPIAvailable;
    }
}
