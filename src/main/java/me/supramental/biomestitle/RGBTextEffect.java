package me.supramental.biomestitle;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Arrays;

public class RGBTextEffect {

    private final JavaPlugin plugin;
    private final List<ChatColor> colors = Arrays.asList(
            ChatColor.RED, ChatColor.GOLD, ChatColor.YELLOW,
            ChatColor.GREEN, ChatColor.AQUA, ChatColor.BLUE, ChatColor.LIGHT_PURPLE
    );
    private int colorIndex = 0;

    public RGBTextEffect(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void startRGBEffect(Player player, String message) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    this.cancel();
                    return;
                }

                String coloredMessage = colors.get(colorIndex) + message;
                player.sendTitle(coloredMessage, "", 0, 20, 10);

                colorIndex = (colorIndex + 1) % colors.size();
            }
        }.runTaskTimer(plugin, 0, 20);
    }
}

