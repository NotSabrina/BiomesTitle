package me.supramental.biomestitle;

import me.supramental.biomestitle.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class BiomesTitle extends JavaPlugin {

    int serviceId = 22389;
    Metrics metrics = new Metrics(this, serviceId);

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(new BiomeListener(this), this);
        getLogger().info("BiomesTitle has been enabled!");

    }

    @Override
    public void onDisable() {
        getLogger().info("BiomesTitle has been disabled!");
    }
}