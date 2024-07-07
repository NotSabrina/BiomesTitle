package me.supramental.biomestitle;

import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class BiomeListener implements Listener {

    private final JavaPlugin plugin;
    private final boolean isPlaceholderAPIAvailable;

    public BiomeListener(JavaPlugin plugin, boolean isPlaceholderAPIAvailable) {
        this.plugin = plugin;
        this.isPlaceholderAPIAvailable = isPlaceholderAPIAvailable;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();

        if (to == null) {
            return;
        }

        List<String> disabledWorlds = plugin.getConfig().getStringList("disabled-worlds");
        if (disabledWorlds.contains(player.getWorld().getName())) {
            return;
        }

        if (from.getBlock().getBiome() != to.getBlock().getBiome()) {
            org.bukkit.block.Biome newBiome = to.getBlock().getBiome();
            displayBiomeMessage(player, newBiome.name());
        }
    }

    private void displayBiomeMessage(Player player, String biomeName) {
        String path = "biomes." + biomeName;
        String message = plugin.getConfig().getString(path + ".message");
        String displayType = plugin.getConfig().getString(path + ".display");

        if (message != null && displayType != null) {
            if (isPlaceholderAPIAvailable) {
                message = PlaceholderAPI.setPlaceholders(player, message);
            }
            String formattedMessage = ChatColor.translateAlternateColorCodes('&', message);
            switch (displayType.toLowerCase()) {
                case "title":
                    player.sendTitle(formattedMessage, "", 10, 70, 20);
                    break;
                case "actionbar":
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(formattedMessage));
                    break;
                default:
                    plugin.getLogger().warning("Invalid display type for biome " + biomeName + ": " + displayType);
            }
        }
    }
}
