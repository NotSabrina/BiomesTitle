package me.supramental.biomestitle;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SetAreaTabCompleter implements TabCompleter {

    private static final List<String> DISPLAY_TYPES = Arrays.asList("title", "actionbar");

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 3) {
            return getMatchingOptions(args[2], DISPLAY_TYPES);
        }
        return Collections.emptyList();
    }

    private List<String> getMatchingOptions(String input, List<String> options) {
        List<String> matches = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase().startsWith(input.toLowerCase())) {
                matches.add(option);
            }
        }
        return matches;
    }
}
