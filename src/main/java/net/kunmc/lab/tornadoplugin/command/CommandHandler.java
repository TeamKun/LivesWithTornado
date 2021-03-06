package net.kunmc.lab.tornadoplugin.command;

import net.kunmc.lab.tornadoplugin.Config;
import net.kunmc.lab.tornadoplugin.TornadoPlugin;
import net.kunmc.lab.tornadoplugin.tornado.Tornado;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.*;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandHandler implements CommandExecutor, TabCompleter {
    private final Map<String, Tornado> stringTornadoMap = new HashMap<>();
    private final List<String> settingItemList = Arrays.asList("radius", "height", "speed", "riseCoef", "centrifugalCoef", "exceptCreatives", "exceptSpectators", "exceptFlowing", "exceptSource", "effectEnabled", "followingSpeed", "limit", "involveBlockProbability", "involveEntityProbability");

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            return false;
        }

        String[] nextArgs = Arrays.copyOfRange(args, 1, args.length);
        switch (args[0]) {
            case "summon":
                summon(sender, nextArgs);
                break;
            case "remove":
                remove(sender, nextArgs);
                break;
            case "modify":
                modify(sender, nextArgs);
                break;
        }

        return true;
    }

    private void summon(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "usage: /tornado summon entity <selector> <tornadoName> <radius> <height> <speed>");
            sender.sendMessage(ChatColor.RED + "usage: /tornado summon location <world> <x> <y> <z> <tornadoName> <radius> <height> <speed>");
            return;
        }

        String[] nextArgs = Arrays.copyOfRange(args, 1, args.length);
        switch (args[0]) {
            case "entity":
                summonToEntity(sender, nextArgs);
                break;
            case "location":
                summonToLocation(sender, nextArgs);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "???????????????????????????.");
        }
    }

    private void summonToEntity(CommandSender sender, String[] args) {
        if (args.length < 5) {
            sender.sendMessage(ChatColor.RED + "usage: /tornado summon entity <selector> <tornadoName> <radius> <height> <speed>");
            return;
        }

        List<Entity> entityList = Bukkit.selectEntities(sender, args[0]);
        if (entityList.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "???????????????????????????????????????.");
            return;
        }

        String tornadoName = args[1];
        if (stringTornadoMap.containsKey(tornadoName)) {
            sender.sendMessage(ChatColor.RED + tornadoName + "??????????????????????????????.");
            return;
        }

        double radius;
        try {
            radius = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "<radius>???????????????????????????????????????.");
            return;
        }

        double height;
        try {
            height = Double.parseDouble(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "<height>???????????????????????????????????????.");
            return;
        }

        double speed;
        try {
            speed = Double.parseDouble(args[4]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "<speed>???????????????????????????????????????.");
            return;
        }

        Tornado tornado = new Tornado(entityList.get(0), radius, height, speed, Config.riseCoef, Config.centrifugalCoef);
        tornado.setExceptCreatives(Config.exceptCreatives);
        tornado.setExceptSpectators(Config.exceptSpectators);
        tornado.setExceptFlowing(Config.exceptFlowing);
        tornado.setExceptSource(Config.exceptSource);
        tornado.setExceptOtherTornado(Config.exceptOtherTornado);
        tornado.setLimitInvolvedEntity(Config.limitInvolvedEntity);
        tornado.setInvolveBlockProbability(Config.involveBlockProbability);
        tornado.setInvolveEntityProbability(Config.involveEntityProbability);
        tornado.summon();
        stringTornadoMap.put(tornadoName, tornado);
        sender.sendMessage(ChatColor.GREEN + tornadoName + "???" + entityList.get(0).getName() + "????????????????????????.");
    }

    private void summonToLocation(CommandSender sender, String[] args) {
        if (args.length < 8) {
            sender.sendMessage(ChatColor.RED + "usage: /tornado summon location <world> <x> <y> <z> <tornadoName> <radius> <height> <speed>");
            return;
        }

        String worldName = args[0].replace(NamespacedKey.MINECRAFT + ":", "");
        World world = Bukkit.getWorld(new NamespacedKey(NamespacedKey.MINECRAFT, worldName));
        if (world == null) {
            sender.sendMessage(ChatColor.RED + worldName + "?????????????????????.");
            return;
        }

        double x;
        try {
            x = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "<x>???????????????????????????????????????.");
            return;
        }

        double y;
        try {
            y = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "<y>???????????????????????????????????????.");
            return;
        }

        double z;
        try {
            z = Double.parseDouble(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "<z>???????????????????????????????????????.");
            return;
        }

        String tornadoName = args[4];
        if (stringTornadoMap.containsKey(tornadoName)) {
            sender.sendMessage(ChatColor.RED + tornadoName + "??????????????????????????????.");
            return;
        }

        double radius;
        try {
            radius = Double.parseDouble(args[5]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "<radius>???????????????????????????????????????.");
            return;
        }

        double height;
        try {
            height = Double.parseDouble(args[6]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "<height>???????????????????????????????????????.");
            return;
        }

        double speed;
        try {
            speed = Double.parseDouble(args[7]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "<speed>???????????????????????????????????????.");
            return;
        }

        Entity coreEntity = world.spawnEntity(new Location(world, x, y, z),
                EntityType.ZOMBIFIED_PIGLIN,
                CreatureSpawnEvent.SpawnReason.CUSTOM,
                e -> {
                    PigZombie entity = ((PigZombie) e);
                    entity.clearLootTable();
                    entity.setAnger(1 << 30);
                    entity.setAngry(true);
                    entity.setAdult();
                    entity.setCanPickupItems(false);
                    entity.setCollidable(false);
                    entity.setMemory(MemoryKey.UNIVERSAL_ANGER, true);
                    entity.setMetadata(Config.metadataKey, new FixedMetadataValue(TornadoPlugin.getInstance(), null));
                    entity.setInvulnerable(true);
                    entity.setInvisible(true);
                    entity.setRemoveWhenFarAway(false);
                    entity.setSilent(true);
                    entity.getEquipment().clear();
                    entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(Config.followingSpeed);
                    entity.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(2048.0);
                    entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(0.0);
                    entity.getAttribute(Attribute.GENERIC_ATTACK_KNOCKBACK).setBaseValue(0.0);

                    new BukkitRunnable() {
                        Player target;

                        @Override
                        public void run() {
                            if (entity.isDead()) {
                                if (target != null) {
                                    target.setGlowing(false);
                                }
                                this.cancel();
                                return;
                            }

                            Player newTarget = Bukkit.selectEntities(Bukkit.getConsoleSender(), "@r").stream()
                                    .map(x -> ((Player) x))
                                    .filter(x -> x.getGameMode().equals(GameMode.SURVIVAL) || x.getGameMode().equals(GameMode.ADVENTURE))
                                    .findFirst()
                                    .orElse(null);

                            if (target != null) {
                                target.setGlowing(false);

                            }

                            if (newTarget != null) {
                                target = newTarget;
                                target.setGlowing(true);
                                entity.setTarget(target);
                                Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "WARNING: " + target.getName() + "??????????????????????????????????????????");
                            }
                        }
                    }.runTaskTimerAsynchronously(TornadoPlugin.getInstance(), 0, Config.changeTargetInterval);
                });

        Tornado tornado = new Tornado(coreEntity, radius, height, speed, Config.riseCoef, Config.centrifugalCoef);
        tornado.setExceptCreatives(Config.exceptCreatives);
        tornado.setExceptSpectators(Config.exceptSpectators);
        tornado.setExceptFlowing(true);
        tornado.setExceptSource(true);
        tornado.setExceptOtherTornado(Config.exceptOtherTornado);
        tornado.setLimitInvolvedEntity(1500);
        tornado.setInvolveBlockProbability(0.15);
        tornado.setInvolveEntityProbability(Config.involveEntityProbability);
        tornado.summon();

        stringTornadoMap.put(tornadoName, tornado);
        sender.sendMessage(String.format(ChatColor.GREEN + "%s???x:%.3f y:%.3f z:%.3f????????????????????????.", tornadoName, x, y, z));
    }

    private void remove(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "usage: /tornado remove <tornadoName>");
            return;
        }

        String tornadoName = args[0];

        if (!stringTornadoMap.containsKey(tornadoName)) {
            sender.sendMessage(ChatColor.RED + tornadoName + "?????????????????????.");
            return;
        }

        Tornado tornado = stringTornadoMap.remove(tornadoName);
        tornado.remove();
        if (tornado.getCoreEntity().hasMetadata(Config.metadataKey)) {
            tornado.getCoreEntity().remove();
        }
        sender.sendMessage(ChatColor.GREEN + tornadoName + "?????????????????????.");
    }

    private void modify(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "usage: /tornado modify <tornadoName> <settingItem> <value>");
            return;
        }

        String tornadoName = args[0];
        if (!stringTornadoMap.containsKey(tornadoName)) {
            sender.sendMessage(ChatColor.RED + tornadoName + "?????????????????????.");
            return;
        }
        Tornado tornado = stringTornadoMap.get(tornadoName);

        String settingItem = args[1];
        if (!settingItemList.contains(settingItem)) {
            sender.sendMessage(ChatColor.RED + settingItem + "??????????????????????????????.");
            return;
        }

        double value;
        try {
            value = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "<value>?????????????????????????????????????????????.");
            return;
        }

        switch (settingItem) {
            case "radius":
                tornado.setRadius(value);
                break;
            case "height":
                tornado.setHeight(value);
                break;
            case "speed":
                tornado.setSpeed(value);
                break;
            case "riseCoef":
                tornado.setRiseCoef(value);
                break;
            case "centrifugalCoef":
                tornado.setCentrifugalCoef(value);
                break;
            case "exceptCreatives":
                tornado.setExceptCreatives(value != 0.0);
                break;
            case "exceptSpectators":
                tornado.setExceptSpectators(value != 0.0);
                break;
            case "exceptFlowing":
                tornado.setExceptFlowing(value != 0.0);
                break;
            case "exceptSource":
                tornado.setExceptSource(value != 0.0);
                break;
            case "effectEnabled":
                tornado.setEffectEnabled(value != 0.0);
                break;
            case "limit":
                tornado.setLimitInvolvedEntity(((int) value));
                break;
            case "involveBlockProbability":
                tornado.setInvolveBlockProbability(value);
                break;
            case "involveEntityProbability":
                tornado.setInvolveEntityProbability(value);
                break;
            case "followingSpeed":
                Entity entity = tornado.getCoreEntity();
                if (entity.hasMetadata(Config.metadataKey)) {
                    ((LivingEntity) entity).getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(value);
                } else {
                    sender.sendMessage(ChatColor.RED + tornadoName + "???followingSpeed?????????????????????????????????????????????.");
                    return;
                }
                break;
        }

        sender.sendMessage(ChatColor.GREEN + tornadoName + "???" + settingItem + "?????????" + value + "?????????????????????.");
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return Stream.of("summon", "remove", "modify").filter(x -> x.startsWith(args[0])).collect(Collectors.toList());
        }

        if (args[0].equals("summon")) {
            if (args.length == 2) {
                return Stream.of("entity", "location").filter(x -> x.startsWith(args[1])).collect(Collectors.toList());
            }

            if (args[1].equals("entity")) {
                switch (args.length) {
                    case 3:
                        return Stream.concat(Bukkit.getOnlinePlayers().stream().map(Player::getName), Stream.of("@a", "@p", "@e", "@r"))
                                .filter(x -> x.startsWith(args[2]))
                                .collect(Collectors.toList());
                    case 4:
                        return Collections.singletonList("<tornadoName>");
                    case 5:
                        return Collections.singletonList("<radius>");
                    case 6:
                        return Collections.singletonList("<height>");
                    case 7:
                        return Collections.singletonList("<speed>");
                }
            }

            if (args[1].equals("location")) {
                switch (args.length) {
                    case 3:
                        return Bukkit.getWorlds().stream().map(World::getKey).map(Object::toString).collect(Collectors.toList());
                    case 4:
                        return Collections.singletonList("<x>");
                    case 5:
                        return Collections.singletonList("<y>");
                    case 6:
                        return Collections.singletonList("<z>");
                    case 7:
                        return Collections.singletonList("<tornadoName>");
                    case 8:
                        return Collections.singletonList("<radius>");
                    case 9:
                        return Collections.singletonList("<height>");
                    case 10:
                        return Collections.singletonList("<speed>");
                }
            }
        }

        if (args[0].equals("remove")) {
            if (args.length == 2) {
                return stringTornadoMap.keySet().stream().filter(x -> x.startsWith(args[1])).collect(Collectors.toList());
            }
        }

        if (args[0].equals("modify")) {
            switch (args.length) {
                case 2:
                    return stringTornadoMap.keySet().stream().filter(x -> x.startsWith(args[1])).collect(Collectors.toList());
                case 3:
                    return settingItemList.stream().filter(x -> x.startsWith(args[2])).collect(Collectors.toList());
                case 4:
                    return Collections.singletonList("<value>");
            }
        }

        return Collections.emptyList();
    }
}
