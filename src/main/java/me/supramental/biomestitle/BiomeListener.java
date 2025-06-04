package me.supramental.biomestitle;

import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;

public class BiomeListener implements Listener {

    private final JavaPlugin plugin;
    private final boolean isPlaceholderAPIAvailable;
    private final Map<Player, String> playerAreas = new HashMap<>();
    private final Map<Player, BossBar> playerBossBars = new HashMap<>();

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

        if (!from.getBlock().getBiome().equals(to.getBlock().getBiome())) {
            String newBiome = to.getBlock().getBiome().toString();
            displayBiomeMessage(player, newBiome);
            spawnBiomeParticles(player, newBiome);
            executeBiomeCommands(player, newBiome);
        }

        checkCustomAreas(player, to);
        spawnTrailParticles(player);
    }

    private void displayBiomeMessage(Player player, String biomeName) {
        String path = player.getWorld().getName() + "." + biomeName;
        String message = plugin.getConfig().getString(path + ".message");
        String subtitle = plugin.getConfig().getString(path + ".subtitle", "");
        String displayType = plugin.getConfig().getString(path + ".display");

        if (message != null && displayType != null) {
            if (isPlaceholderAPIAvailable) {
                message = PlaceholderAPI.setPlaceholders(player, message);
                subtitle = PlaceholderAPI.setPlaceholders(player, subtitle);
            }
            String formattedMessage = ChatColor.translateAlternateColorCodes('&', message);
            String formattedSubtitle = ChatColor.translateAlternateColorCodes('&', subtitle);

            switch (displayType.toLowerCase()) {
                case "title":
                    handleTitleDisplay(player, formattedMessage, formattedSubtitle);
                    break;
                case "actionbar":
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(formattedMessage));
                    break;
                case "subtitle":
                    player.sendTitle("", formattedMessage, 10, 70, 20);
                    break;
                case "bossbar":
                    showBossBar(player, formattedMessage);
                    break;
                default:
                    plugin.getLogger().warning("Invalid display type for biome " + biomeName + ": " + displayType);
            }
        }
    }

    private void handleTitleDisplay(Player player, String message, String subtitle) {
        if (message.contains("<rgb>")) {
            String rgbMessage = message.replace("<rgb>", "").replace("</rgb>", "");
            startRGBEffect(player, rgbMessage);
        } else {
            player.sendTitle(message, subtitle, 10, 70, 20);
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
            try {
                Particle particle = Particle.valueOf(particleType.toUpperCase());
                player.getWorld().spawnParticle(particle, player.getLocation(), count, offsetX, offsetY, offsetZ);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid particle type for biome " + biomeName + ": " + particleType);
            }
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
                handleAreaEntry(player, area, areaConfig);
                break;
            }
        }

        if (!isInAnyArea && playerAreas.containsKey(player)) {
            player.sendTitle("", "", 0, 1, 0);
            playerAreas.remove(player);
        }
    }

    private void handleAreaEntry(Player player, String area, YamlConfiguration areaConfig) {
        String currentArea = playerAreas.get(player);
        if (!area.equals(currentArea)) {
            playerAreas.put(player, area);
            String message = areaConfig.getString(area + ".message");
            String subtitle = areaConfig.getString(area + ".subtitle", "");
            String displayType = areaConfig.getString(area + ".displayType");

            if (message != null && displayType != null) {
                if (isPlaceholderAPIAvailable) {
                    message = PlaceholderAPI.setPlaceholders(player, message);
                    subtitle = PlaceholderAPI.setPlaceholders(player, subtitle);
                }
                String formattedMessage = ChatColor.translateAlternateColorCodes('&', message);
                String formattedSubtitle = ChatColor.translateAlternateColorCodes('&', subtitle);

                switch (displayType.toLowerCase()) {
                    case "title":
                        handleTitleDisplay(player, formattedMessage, formattedSubtitle);
                        break;
                    case "actionbar":
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(formattedMessage));
                        break;
                    case "subtitle":
                        player.sendTitle("", formattedMessage, 10, 70, 20);
                        break;
                    default:
                        plugin.getLogger().warning("Invalid display type for area " + area + ": " + displayType);
                }
            }

            spawnAreaParticles(player, areaConfig, area);
            executeAreaCommands(player, areaConfig, area);
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
            try {
                Particle particle = Particle.valueOf(particleType.toUpperCase());
                player.getWorld().spawnParticle(particle, player.getLocation(), count, offsetX, offsetY, offsetZ);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid particle type for area " + area + ": " + particleType);
            }
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

        return loc.getX() >= xMin && loc.getX() <= xMax &&
                loc.getY() >= yMin && loc.getY() <= yMax &&
                loc.getZ() >= zMin && loc.getZ() <= zMax;
    }

    private void spawnTrailParticles(Player player) {
        String trailPath = "trails." + player.getWorld().getName();
        String particleType = plugin.getConfig().getString(trailPath + ".type");
        int count = plugin.getConfig().getInt(trailPath + ".count", 1);
        double offsetX = plugin.getConfig().getDouble(trailPath + ".offsetX", 0.1);
        double offsetY = plugin.getConfig().getDouble(trailPath + ".offsetY", 0.1);
        double offsetZ = plugin.getConfig().getDouble(trailPath + ".offsetZ", 0.1);

        if (particleType != null) {
            try {
                Particle particle = Particle.valueOf(particleType.toUpperCase());
                player.getWorld().spawnParticle(particle, player.getLocation().add(0, 0.1, 0), count, offsetX, offsetY, offsetZ);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid trail particle type: " + particleType);
            }
        }
    }

    private void showBossBar(Player player, String message) {
        BossBar existingBossBar = playerBossBars.get(player);
        if (existingBossBar == null) {
            existingBossBar = Bukkit.createBossBar(message, BarColor.BLUE, BarStyle.SOLID);
            playerBossBars.put(player, existingBossBar);
        }
        BossBar bossBar = existingBossBar;
        bossBar.setTitle(message);
        bossBar.setProgress(1.0);
        bossBar.addPlayer(player);
        bossBar.setVisible(true);

        new BukkitRunnable() {
            @Override
            public void run() {
                bossBar.setVisible(false);
            }
        }.runTaskLater(plugin, 100L);
    }

    private void startRGBEffect(Player player, String message) {
        new BukkitRunnable() {
            int colorIndex = 0;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }

                ChatColor color = ChatColor.values()[colorIndex % ChatColor.values().length];
                String rgbMessage = color + message;
                player.sendTitle(rgbMessage, "", 10, 40, 10);
                colorIndex++;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

}
