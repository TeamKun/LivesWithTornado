package net.kunmc.lab.tornadoplugin;

import net.kunmc.lab.tornadoplugin.command.CommandHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

public final class TornadoPlugin extends JavaPlugin {
    private static TornadoPlugin INSTANCE;

    public static TornadoPlugin getInstance() {
        return INSTANCE;
    }

    @Override
    public void onEnable() {
        INSTANCE = this;

        saveDefaultConfig();
        Config.riseCoef = getConfig().getDouble("riseCoef");
        Config.centrifugalCoef = getConfig().getDouble("centrifugalCoef");
        Config.changeTargetInterval = getConfig().getInt("changeTargetInterval");
        Config.metadataKey = getConfig().getString("metadataKey");
        Config.exceptCreatives = getConfig().getBoolean("exceptCreatives");
        Config.exceptSpectators = getConfig().getBoolean("exceptSpectators");
        Config.exceptFlowing = getConfig().getBoolean("exceptFlowing");
        Config.limitInvolvedEntity = getConfig().getInt("limitInvolvedEntity");
        Config.involveProbability = getConfig().getDouble("involveProbability");

        CommandHandler commandHandler = new CommandHandler();
        getServer().getPluginCommand("tornado").setExecutor(commandHandler);
        getServer().getPluginCommand("tornado").setTabCompleter(commandHandler);
    }

    @Override
    public void onDisable() {
        Bukkit.selectEntities(Bukkit.getConsoleSender(), "@e").stream()
                .filter(x -> x.hasMetadata(Config.metadataKey))
                .forEach(Entity::remove);
    }
}
