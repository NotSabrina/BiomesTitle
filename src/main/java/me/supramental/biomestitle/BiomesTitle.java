package me.supramental.biomestitle;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class BiomesTitle extends JavaPlugin {

    int serviceId = 22389;
    Metrics metrics = new Metrics(this, serviceId);
    private boolean isPlaceholderAPIAvailable = false;
    private static final String VERSION_URL = "https://raw.githubusercontent.com/NotSabrina/BiomesTitle/main/version.txt";

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

        checkForUpdates();
    }

    @Override
    public void onDisable() {
        getLogger().info("BiomesTitle has been disabled!");
    }

    public boolean isPlaceholderAPIAvailable() {
        return isPlaceholderAPIAvailable;
    }

    private void checkForUpdates() {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    HttpURLConnection connection = (HttpURLConnection) new URL(VERSION_URL).openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(5000);

                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String latestVersion = in.readLine();
                    in.close();

                    String currentVersion = getDescription().getVersion();
                    if (!currentVersion.equals(latestVersion)) {
                        notifyAdmins(latestVersion);
                    }

                } catch (Exception e) {
                    getLogger().severe("Failed to check for updates: " + e.getMessage());
                }
            }
        }.runTaskTimerAsynchronously(this, 0, 20 * 60 * 60);
    }

    private void notifyAdmins(String latestVersion) {
        String message = "A new version of BiomesTitle is available: " + latestVersion + ". You are using version: " + getDescription().getVersion();
        getLogger().info(message);
    }
}
