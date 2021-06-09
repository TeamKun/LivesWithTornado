package net.kunmc.lab.tornadoplugin.tornado;

import net.kunmc.lab.tornadoplugin.TornadoPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class Tornado {
    private final Entity coreEntity;
    private Location center;
    private double radius;
    private double height;
    private double speed;
    private double riseCoef;
    private double centrifugalCoef;
    private final Set<Entity> involvedEntitySet = Collections.synchronizedSet(new LinkedHashSet<>());
    private BukkitTask involveTask;
    private final Set<BukkitTask> windUpTaskSet = Collections.synchronizedSet(new LinkedHashSet<>());

    public Tornado(Entity coreEntity, double radius, double height, double speed, double riseCoef, double centrifugalCoef) {
        this.coreEntity = coreEntity;
        this.radius = radius;
        this.height = height;
        this.speed = speed;
        this.riseCoef = riseCoef;
        this.centrifugalCoef = centrifugalCoef;
    }

    public void summon() {
        involveTask = new InvolveTask().runTaskTimer(TornadoPlugin.getInstance(), 0, 4);
    }

    public void remove() {
        involveTask.cancel();
        windUpTaskSet.forEach(BukkitTask::cancel);
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

    private class InvolveTask extends BukkitRunnable {
        @Override
        public void run() {
            center = coreEntity.getLocation();

            getAffectedBlocks(center, radius, height).forEach(x -> {
                if (x.getType().equals(Material.AIR) || x.getType().equals(Material.CAVE_AIR)) {
                    return;
                }
                BlockData blockData = x.getBlockData();
                x.setType(Material.AIR);
                FallingBlock fallingBlock = x.getWorld().spawnFallingBlock(x.getLocation(), blockData);
                fallingBlock.setGravity(false);
            });

            center.getNearbyEntities(radius, height, radius).parallelStream()
                    .filter(x -> !x.equals(coreEntity))
                    .filter(x -> x.getLocation().getY() >= center.getY() - 3)
                    .forEach(x -> {
                        if (involvedEntitySet.add(x)) {
                            windUpTaskSet.add(new WindUpTask(x).runTaskTimerAsynchronously(TornadoPlugin.getInstance(), 0, 0));
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
                        if (center.getLocation().distance(b.getLocation()) <= radius) {
                            blockSet.add(b);
                        }
                    }
                }
            }

            return blockSet;
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
