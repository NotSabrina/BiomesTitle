package me.supramental.biomestitle;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

public class SetAreaCommand implements CommandExecutor {

    private final BiomesTitle plugin;
    private final WandListener wandListener;

    public SetAreaCommand(BiomesTitle plugin, WandListener wandListener) {
        this.plugin = plugin;
        this.wandListener = wandListener;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("biomestitle.command.setarea")) {
            player.sendMessage("You do not have permission to use this command.");
            return true;
        }

        if (args.length < 3) {
            player.sendMessage("Usage: /biometitlesetarea <name> <message> <displayType>");
            return true;
        }

        Location pos1 = wandListener.getPos1().get(player);
        Location pos2 = wandListener.getPos2().get(player);

        if (pos1 == null || pos2 == null) {
            player.sendMessage("You must set both positions using the wand.");
            return true;
        }

        String areaName = args[0];
        String message = args[1];
        String displayType = args[2];

        File file = new File(plugin.getDataFolder(), "Area.yml");
        YamlConfiguration areaConfig = YamlConfiguration.loadConfiguration(file);

        areaConfig.set(areaName + ".pos1.world", pos1.getWorld().getName());
        areaConfig.set(areaName + ".pos1.x", pos1.getX());
        areaConfig.set(areaName + ".pos1.y", pos1.getY());
        areaConfig.set(areaName + ".pos1.z", pos1.getZ());
        areaConfig.set(areaName + ".pos2.world", pos2.getWorld().getName());
        areaConfig.set(areaName + ".pos2.x", pos2.getX());
        areaConfig.set(areaName + ".pos2.y", pos2.getY());
        areaConfig.set(areaName + ".pos2.z", pos2.getZ());
        areaConfig.set(areaName + ".message", message);
        areaConfig.set(areaName + ".displayType", displayType);

        if (args.length > 3) {
            String sounds = args[3];
            areaConfig.set(areaName + ".sounds", sounds.split(","));
        }

        if (args.length > 4) {
            String commands = args[4];
            areaConfig.set(areaName + ".commands", commands.split(","));
        }

        if (args.length > 5) {
            String particles = args[5];
            areaConfig.set(areaName + ".particles.type", particles);
        }

        try {
            areaConfig.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        player.sendMessage("Area " + areaName + " has been set.");
        return true;
    }
}
