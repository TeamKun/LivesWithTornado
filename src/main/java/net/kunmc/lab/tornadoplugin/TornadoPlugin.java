package net.kunmc.lab.tornadoplugin;

import net.kunmc.lab.tornadoplugin.command.CommandHandler;
import org.bukkit.plugin.java.JavaPlugin;

public final class TornadoPlugin extends JavaPlugin {
    private static TornadoPlugin INSTANCE;

    public static TornadoPlugin getInstance() {
        return INSTANCE;
    }

    @Override
    public void onEnable() {
        INSTANCE = this;
        getServer().getPluginCommand("tornado").setExecutor(new CommandHandler());
        getServer().getPluginCommand("tornado").setTabCompleter(new CommandHandler());
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
