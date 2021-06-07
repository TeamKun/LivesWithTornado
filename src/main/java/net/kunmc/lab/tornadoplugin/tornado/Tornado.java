package net.kunmc.lab.tornadoplugin.tornado;

import net.kunmc.lab.tornadoplugin.TornadoPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class Tornado {
    private final Entity coreEntity;
    private Location currentLocation;
    private final Set<Entity> involvedEntitySet = Collections.synchronizedSet(new LinkedHashSet<>());
    private double radius;
    private double height;
    private double speed;
    private BukkitTask involveTask;
    private Set<BukkitTask> windUpTaskSet = Collections.synchronizedSet(new LinkedHashSet<>());

    public Tornado(Entity coreEntity, double radius, double height, double speed) {
        this.coreEntity = coreEntity;
        this.radius = radius;
        this.height = height;
        this.speed = speed;
    }

    public void summon() {
        involveTask = new BukkitRunnable() {
            @Override
            public void run() {
                currentLocation = coreEntity.getLocation();
                Bukkit.selectEntities(coreEntity, "@e[distance=1..10]").parallelStream()
                        .filter(x -> x.getLocation().getY() >= currentLocation.getY() - 2)
                        .forEach(x -> {
                            if (involvedEntitySet.add(x)) {
                                windUpTaskSet.add(new BukkitRunnable() {
                                    double degree = Math.toDegrees(Math.atan2(x.getLocation().getZ() - currentLocation.getZ(), x.getLocation().getX() - currentLocation.getX()));

                                    @Override
                                    public void run() {
                                        double currentX = x.getLocation().getX();
                                        double currentY = x.getLocation().getY();
                                        double currentZ = x.getLocation().getZ();

                                        degree = (degree + speed) % 360;
                                        double radian = Math.toRadians(degree);
                                        double cos = Math.cos(radian);
                                        double sin = Math.sin(radian);

                                        double distance = currentLocation.clone().set(currentLocation.getX(), 0,
                                                currentLocation.getZ()).distance(new Location(x.getWorld(), currentX, 0, currentZ)) + 0.3;
                                        double tornadoRadius = Math.min(distance, radius);

                                        double nextX = currentLocation.getX() + tornadoRadius * cos;
                                        double nextY = currentY + 0.1;
                                        double nextZ = currentLocation.getZ() + tornadoRadius * sin;

                                        x.setVelocity(new Vector(nextX - currentX, nextY - currentY, nextZ - currentZ));

                                        if (nextY - currentLocation.getY() > height || x.isDead()) {
                                            this.cancel();
                                            involvedEntitySet.remove(x);
                                        }
                                    }
                                }.runTaskTimerAsynchronously(TornadoPlugin.getInstance(), 0, 2));
                            }
                        });
            }
        }.runTaskTimer(TornadoPlugin.getInstance(), 0, 2);
    }

    public void remove() {
        involveTask.cancel();
        windUpTaskSet.forEach(BukkitTask::cancel);
    }
}
