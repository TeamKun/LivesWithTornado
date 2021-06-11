package net.kunmc.lab.tornadoplugin.tornado;

import net.kunmc.lab.tornadoplugin.TornadoPlugin;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;

public class Tornado {
    private final Entity coreEntity;
    private Location center;
    private double radius;
    private double height;
    private double speed;
    private double riseCoef;
    private double centrifugalCoef;
    private boolean exceptCreatives = true;
    private boolean exceptSpectators = true;
    private boolean exceptFlowing = true;
    private int limitInvolvedEntity = 0;
    private double involveProbability = 1.0;
    private final Set<Entity> involvedEntitySet = Collections.synchronizedSet(new LinkedHashSet<>());
    private BukkitTask involveTask;
    private BukkitTask effectTask;
    private final Set<BukkitTask> windUpTaskSet = Collections.synchronizedSet(new LinkedHashSet<>());

    public Tornado(Entity coreEntity, double radius, double height, double speed, double riseCoef, double centrifugalCoef) {
        this.coreEntity = coreEntity;
        this.radius = radius;
        this.height = height;
        this.speed = speed;
        this.riseCoef = riseCoef;
        this.centrifugalCoef = centrifugalCoef;
    }

    public Tornado(Entity coreEntity, double radius, double height, double speed, double riseCoef, double centrifugalCoef, boolean exceptCreatives, boolean exceptSpectators, boolean exceptFlowing) {
        this(coreEntity, radius, height, speed, riseCoef, centrifugalCoef);
        this.exceptCreatives = exceptCreatives;
        this.exceptSpectators = exceptSpectators;
        this.exceptFlowing = exceptFlowing;
    }

    public void summon() {
        involveTask = new InvolveTask().runTaskTimer(TornadoPlugin.getInstance(), 0, 4);
        effectTask = new EffectTaskGenerator().runTaskTimerAsynchronously(TornadoPlugin.getInstance(), 0, 60);
    }

    public void remove() {
        involveTask.cancel();
        windUpTaskSet.forEach(BukkitTask::cancel);
        effectTask.cancel();
    }

    public Entity getCoreEntity() {
        return this.coreEntity;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public void setRiseCoef(double riseCoef) {
        this.riseCoef = riseCoef;
    }

    public void setCentrifugalCoef(double centrifugalCoef) {
        this.centrifugalCoef = centrifugalCoef;
    }

    public void setExceptCreatives(boolean exceptCreatives) {
        this.exceptCreatives = exceptCreatives;
    }

    public void setExceptSpectators(boolean exceptSpectators) {
        this.exceptSpectators = exceptSpectators;
    }

    public void setExceptFlowing(boolean exceptFlowing) {
        this.exceptFlowing = exceptFlowing;
    }

    public void setEffectEnabled(boolean enable) {
        if (enable) {
            effectTask.cancel();
            effectTask = new EffectTaskGenerator().runTaskTimerAsynchronously(TornadoPlugin.getInstance(), 0, 60);
        } else {
            effectTask.cancel();
        }
    }

    public void setLimitInvolvedEntity(int limit) {
        this.limitInvolvedEntity = limit;
    }

    public void setInvolveProbability(double probability) {
        this.involveProbability = probability;
    }

    private class InvolveTask extends BukkitRunnable {
        @Override
        public void run() {
            center = coreEntity.getLocation();

            if (limitInvolvedEntity != 0 && involvedEntitySet.size() >= limitInvolvedEntity) {
                return;
            }

            getAffectedBlocks(center, radius, height).forEach(x -> {
                if (x.getType().equals(Material.AIR) || x.getType().equals(Material.CAVE_AIR)) {
                    return;
                }

                if (Math.random() <= involveProbability) {
                    BlockData blockData = x.getBlockData();
                    x.setType(Material.AIR);
                    FallingBlock fallingBlock = x.getWorld().spawnFallingBlock(x.getLocation(), blockData);
                    fallingBlock.setGravity(false);
                }
            });

            center.getNearbyEntities(radius, height, radius).parallelStream()
                    .filter(x -> !x.equals(coreEntity))
                    .filter(x -> x.getLocation().getY() >= center.getY() - 3)
                    .filter(x -> {
                        if (x instanceof Player) {
                            GameMode mode = ((Player) x).getGameMode();
                            return (!exceptCreatives || !mode.equals(GameMode.CREATIVE)) && (!exceptSpectators || !mode.equals(GameMode.SPECTATOR));
                        }
                        return true;
                    })
                    .forEach(x -> {
                        if (Math.random() <= involveProbability) {
                            if (involvedEntitySet.add(x)) {
                                windUpTaskSet.add(new WindUpTask(x).runTaskTimerAsynchronously(TornadoPlugin.getInstance(), 0, 0));
                            }
                        }
                    });
        }

        private Set<Block> getAffectedBlocks(Location origin, double radius, double height) {
            Set<Block> blockSet = new HashSet<>();

            for (int i = 0; i <= height; i++) {
                Block center = origin.clone().add(0, i, 0).getBlock();
                int offset = ((int) Math.min(i, radius));
                for (int x = -2 - offset; x <= 2 + offset; x++) {
                    for (int z = -2 - offset; z <= 2 + offset; z++) {
                        Block b = center.getRelative(x, 0, z);

                        //exceptFlowingがtrueの場合,溶岩流と水流を処理対象から外す.
                        if (b.getType().equals(Material.LAVA) || b.getType().equals(Material.WATER)) {
                            Levelled data = ((Levelled) b.getBlockData());
                            if (exceptFlowing && data.getLevel() >= 1) {
                                continue;
                            }
                        }

                        if (center.getLocation().distance(b.getLocation()) <= radius) {
                            blockSet.add(b);
                        }
                    }
                }
            }

            return blockSet;
        }
    }

    private class EffectTaskGenerator extends BukkitRunnable {
        private final List<BukkitTask> taskList = new ArrayList<>();

        @Override
        public void run() {
            taskList.add(new EffectTask().runTaskTimerAsynchronously(TornadoPlugin.getInstance(), 0, 0));
        }

        @Override
        public void cancel() {
            taskList.forEach(BukkitTask::cancel);
            super.cancel();
        }

        private class EffectTask extends BukkitRunnable {
            private double degree = 0;
            private double heightOffset = 0;
            private double currentRadius = 3;

            @Override
            public void run() {
                double radian = Math.toRadians(degree);
                double x = center.getX() + Math.cos(radian) * currentRadius;
                double y = center.getY() + heightOffset;
                double z = center.getZ() + Math.sin(radian) * currentRadius;

                World world = center.getWorld();
                world.spawnParticle(Particle.REDSTONE, x, y, z, 3, new Particle.DustOptions(Color.WHITE, 10));

                degree = (degree + 20) % 360;
                heightOffset += 0.25;
                currentRadius = Math.min(currentRadius + 0.125, radius);

                if (heightOffset > height) {
                    this.cancel();
                }
            }
        }
    }

    private class WindUpTask extends BukkitRunnable {
        private final Entity entity;
        private double heightOffset;

        public WindUpTask(Entity entity) {
            this.entity = entity;
            heightOffset = entity.getLocation().getY() - center.getY();
        }

        @Override
        public void run() {
            if (entity instanceof Player) {
                GameMode mode = ((Player) entity).getGameMode();
                if (exceptCreatives && mode.equals(GameMode.CREATIVE) || exceptSpectators && mode.equals(GameMode.SPECTATOR)) {
                    involvedEntitySet.remove(entity);
                    this.cancel();
                }
            }

            double degree = Math.toDegrees(Math.atan2(entity.getLocation().getZ() - center.getZ(), entity.getLocation().getX() - center.getX()));
            degree = (degree + speed) % 360;
            double radian = Math.toRadians(degree);
            double cos = Math.cos(radian);
            double sin = Math.sin(radian);

            //竜巻の中心点とエンティティの現在地との平面距離を求める
            double distance = center.toVector().setY(0).subtract(entity.getLocation().toVector().setY(0)).length();
            //竜巻の半径を最大とし,次の中心点からの平面距離を決定する
            double fromOrigin = Math.min(distance + centrifugalCoef, radius);

            double nextX = center.getX() + fromOrigin * cos;
            heightOffset += riseCoef;
            double nextY = center.getY() + heightOffset;
            double nextZ = center.getZ() + fromOrigin * sin;

            entity.setVelocity(new Vector(nextX, nextY, nextZ).subtract(entity.getLocation().toVector()));

            if (entity instanceof FallingBlock) {
                entity.setTicksLived(30);
                entity.setGravity(true);
            }

            if (heightOffset > height || entity.isDead()) {
                this.cancel();
                involvedEntitySet.remove(entity);
            }
        }
    }
}
