package me.supramental.biomestitle;

import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

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
            spawnBiomeParticles(player, newBiome.name());
            executeBiomeCommands(player, newBiome.name());
        }

        checkCustomAreas(player, to);
        spawnTrailParticles(player);
    }

    private void displayBiomeMessage(Player player, String biomeName) {
        String path = player.getWorld().getName() + "." + biomeName;
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

    private void spawnBiomeParticles(Player player, String biomeName) {
        String path = player.getWorld().getName() + "." + biomeName + ".particles";
        String particleType = plugin.getConfig().getString(path + ".type");
        int count = plugin.getConfig().getInt(path + ".count", 10);
        double offsetX = plugin.getConfig().getDouble(path + ".offsetX", 0.5);
        double offsetY = plugin.getConfig().getDouble(path + ".offsetY", 0.5);
        double offsetZ = plugin.getConfig().getDouble(path + ".offsetZ", 0.5);

        if (particleType != null) {
            Particle particle = Particle.valueOf(particleType.toUpperCase());
            player.getWorld().spawnParticle(particle, player.getLocation(), count, offsetX, offsetY, offsetZ);
        }
    }

    private void executeBiomeCommands(Player player, String biomeName) {
        String path = player.getWorld().getName() + "." + biomeName + ".commands";
        List<String> commands = plugin.getConfig().getStringList(path);

        if (commands != null) {
            for (String command : commands) {
                String formattedCommand = command.replace("%player%", player.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), formattedCommand);
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

                    spawnAreaParticles(player, areaConfig, area);
                    executeAreaCommands(player, areaConfig, area);
                }
                break;
            }
        }

        if (!isInAnyArea && playerAreas.containsKey(player)) {
            player.sendTitle("", "", 0, 1, 0);
            playerAreas.remove(player);
        }
    }

    private void spawnAreaParticles(Player player, YamlConfiguration areaConfig, String area) {
        String path = area + ".particles";
        String particleType = areaConfig.getString(path + ".type");
        int count = areaConfig.getInt(path + ".count", 10);
        double offsetX = areaConfig.getDouble(path + ".offsetX", 0.5);
        double offsetY = areaConfig.getDouble(path + ".offsetY", 0.5);
        double offsetZ = areaConfig.getDouble(path + ".offsetZ", 0.5);

        if (particleType != null) {
            Particle particle = Particle.valueOf(particleType.toUpperCase());
            player.getWorld().spawnParticle(particle, player.getLocation(), count, offsetX, offsetY, offsetZ);
        }
    }

    private void executeAreaCommands(Player player, YamlConfiguration areaConfig, String area) {
        List<String> commands = areaConfig.getStringList(area + ".commands");

        if (commands != null) {
            for (String command : commands) {
                String formattedCommand = command.replace("%player%", player.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), formattedCommand);
            }
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

    private void spawnTrailParticles(Player player) {
        File file = new File(plugin.getDataFolder(), "Area.yml");
        YamlConfiguration areaConfig = YamlConfiguration.loadConfiguration(file);
        Location playerLocation = player.getLocation();

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

            if (isInArea(playerLocation, pos1, pos2)) {
                String path = area + ".trail_particles";
                String particleType = areaConfig.getString(path + ".type");
                int count = areaConfig.getInt(path + ".count", 10);
                double offsetX = areaConfig.getDouble(path + ".offsetX", 0.5);
                double offsetY = areaConfig.getDouble(path + ".offsetY", 0.5);
                double offsetZ = areaConfig.getDouble(path + ".offsetZ", 0.5);

                if (particleType != null) {
                    Particle particle = Particle.valueOf(particleType.toUpperCase());
                    player.getWorld().spawnParticle(particle, player.getLocation(), count, offsetX, offsetY, offsetZ);
                }
            }
        }
    }
}
