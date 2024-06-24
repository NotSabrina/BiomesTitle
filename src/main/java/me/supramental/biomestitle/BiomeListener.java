package me.supramental.biomestitle;

import org.bukkit.block.Biome;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class BiomeListener implements Listener {

    private final JavaPlugin plugin;

    public BiomeListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();

        if (to != null && from.getBlock().getBiome() != to.getBlock().getBiome()) {
            Biome newBiome = to.getBlock().getBiome();
            String title = plugin.getConfig().getString("biomes." + newBiome.name());

            if (title != null) {
                player.sendTitle(ChatColor.translateAlternateColorCodes('&', title), "", 10, 70, 20);
            }
        }
    }
}