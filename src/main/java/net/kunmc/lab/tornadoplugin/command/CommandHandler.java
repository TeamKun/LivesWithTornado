package net.kunmc.lab.tornadoplugin.command;

import net.kunmc.lab.tornadoplugin.tornado.Tornado;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandHandler implements CommandExecutor, TabCompleter {
    Map<String, Tornado> stringTornadoMap = new HashMap<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 2) {
            return false;
        }

        String name = args[1];

        switch (args[0]) {
            case "summon":
                if (args.length < 5) {
                    return false;
                }

                double radius = Double.parseDouble(args[2]);
                double height = Double.parseDouble(args[3]);
                double speed = Double.parseDouble(args[4]);

                Player p = ((Player) sender);
                Tornado tornado = new Tornado(p, radius, height, speed);
                if (stringTornadoMap.containsKey(name)) {
                    sender.sendMessage(ChatColor.RED + name + "は既に存在しています.");
                    break;
                }

                stringTornadoMap.put(name, tornado);
                tornado.summon();
                break;
            case "remove":
                if (!stringTornadoMap.containsKey(name)) {
                    sender.sendMessage(ChatColor.RED + name + "は存在しません.");
                    break;
                }

                stringTornadoMap.get(name).remove();
                break;
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return Stream.of("summon", "remove").filter(x -> x.startsWith(args[0])).collect(Collectors.toList());
        }

        if (args.length == 2) {
            
            return Stream.of("<name>").filter(x -> x.startsWith(args[1])).collect(Collectors.toList());
        }

        if (args[0].equals("summon")) {
            if (args.length == 3) {
                return Stream.of("<radius>").filter(x -> x.startsWith(args[2])).collect(Collectors.toList());
            }

            if (args.length == 4) {
                return Stream.of("<height>").filter(x -> x.startsWith(args[3])).collect(Collectors.toList());
            }

            if (args.length == 5) {
                return Stream.of("<speed>").filter(x -> x.startsWith(args[4])).collect(Collectors.toList());
            }
        }

        return Collections.emptyList();
    }
}
