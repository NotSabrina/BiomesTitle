package me.supramental.biomestitle;

import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class BiomeListener implements Listener {

    private final JavaPlugin plugin;
    private final boolean isPlaceholderAPIAvailable;
    private final Map<Player, String> playerAreas = new HashMap<>();

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

        checkCustomAreas(player, to);
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

    private void checkCustomAreas(Player player, Location to) {
        File file = new File(plugin.getDataFolder(), "Area.yml");
        YamlConfiguration areaConfig = YamlConfiguration.loadConfiguration(file);
        boolean isInAnyArea = false;

        for (String area : areaConfig.getKeys(false)) {
            Location pos1 = new Location(
                    player.getWorld(),
                    areaConfig.getDouble(area + ".pos1.x"),
                    areaConfig.getDouble(area + ".pos1.y"),
                    areaConfig.getDouble(area + ".pos1.z")
            );
            Location pos2 = new Location(
                    player.getWorld(),
                    areaConfig.getDouble(area + ".pos2.x"),
                    areaConfig.getDouble(area + ".pos2.y"),
                    areaConfig.getDouble(area + ".pos2.z")
            );

            if (isInArea(to, pos1, pos2)) {
                isInAnyArea = true;
                String currentArea = playerAreas.get(player);
                if (!area.equals(currentArea)) {
                    playerAreas.put(player, area);
                    String message = areaConfig.getString(area + ".message");
                    String displayType = areaConfig.getString(area + ".displayType");

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
                                plugin.getLogger().warning("Invalid display type for area " + area + ": " + displayType);
                        }
                    }
                }
                break;
            }
        }

        if (!isInAnyArea && playerAreas.containsKey(player)) {
            player.sendTitle("", "", 0, 1, 0); // Clear the title
            playerAreas.remove(player);
        }
    }


    private boolean isInArea(Location loc, Location pos1, Location pos2) {
        double xMin = Math.min(pos1.getX(), pos2.getX());
        double xMax = Math.max(pos1.getX(), pos2.getX());
        double yMin = Math.min(pos1.getY(), pos2.getY());
        double yMax = Math.max(pos1.getY(), pos2.getY());
        double zMin = Math.min(pos1.getZ(), pos2.getZ());
        double zMax = Math.max(pos1.getZ(), pos2.getZ());

        return loc.getX() >= xMin && loc.getX() <= xMax
                && loc.getY() >= yMin && loc.getY() <= yMax
                && loc.getZ() >= zMin && loc.getZ() <= zMax;
    }
}
