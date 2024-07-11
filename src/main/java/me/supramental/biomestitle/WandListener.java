package me.supramental.biomestitle;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;

public class WandListener implements Listener {

    private final BiomesTitle plugin;
    private final Map<Player, Location> pos1 = new HashMap<>();
    private final Map<Player, Location> pos2 = new HashMap<>();

    public WandListener(BiomesTitle plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getItem() != null && event.getItem().getType() == Material.BLAZE_ROD && "§6BiomeTitle Wand".equals(event.getItem().getItemMeta().getDisplayName())) {
            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                pos1.put(player, event.getClickedBlock().getLocation());
                player.sendMessage(ChatColor.GREEN + "Position 1 set at " + formatLocation(event.getClickedBlock().getLocation()));
                event.setCancelled(true);
            } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                pos2.put(player, event.getClickedBlock().getLocation());
                player.sendMessage(ChatColor.GREEN + "Position 2 set at " + formatLocation(event.getClickedBlock().getLocation()));
                event.setCancelled(true);
            }

            if (pos1.containsKey(player) && pos2.containsKey(player)) {
                player.sendMessage(ChatColor.YELLOW + "Use /biometitlesetarea <name> <message> <title or actionbar> to set the area.");
                player.sendMessage(ChatColor.YELLOW + "To custom the commands,particles,and sound go to Area.yml.");
            }
        }
    }

    private String formatLocation(Location location) {
        return "[" + location.getWorld().getName() + ", " + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + "]";
    }

    public Map<Player, Location> getPos1() {
        return pos1;
    }

    public Map<Player, Location> getPos2() {
        return pos2;
    }
}