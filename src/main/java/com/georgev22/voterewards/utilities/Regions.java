package com.georgev22.voterewards.utilities;

import com.georgev22.library.minecraft.BukkitMinecraftUtils;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class Regions {

    private final UUID worldUniqueId;

    private final double maxX;
    private final double maxY;
    private final double maxZ;

    private final double minX;
    private final double minY;
    private final double minZ;

    public Regions(@NotNull BukkitMinecraftUtils.SerializableLocation firstPoint, @NotNull BukkitMinecraftUtils.SerializableLocation secondPoint) {
        worldUniqueId = firstPoint.getLocation().getWorld().getUID();

        maxX = Math.max(firstPoint.getLocation().getX(), secondPoint.getLocation().getX());
        maxY = Math.max(firstPoint.getLocation().getY(), secondPoint.getLocation().getY());
        maxZ = Math.max(firstPoint.getLocation().getZ(), secondPoint.getLocation().getZ());

        minX = Math.min(firstPoint.getLocation().getX(), secondPoint.getLocation().getX());
        minY = Math.min(firstPoint.getLocation().getY(), secondPoint.getLocation().getY());
        minZ = Math.min(firstPoint.getLocation().getZ(), secondPoint.getLocation().getZ());
    }

    public boolean locationIsInRegion(@NotNull BukkitMinecraftUtils.SerializableLocation loc) {
        return loc.getLocation().getWorld().getUID().equals(worldUniqueId) && loc.getLocation().getX() >= minX && loc.getLocation().getX() <= maxX
                && loc.getLocation().getY() >= minY && loc.getLocation().getY() <= maxY && loc.getLocation().getZ() >= minZ && loc.getLocation().getZ() <= maxZ;
    }

}