package ru.sema1ary.chatroom.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

@UtilityClass
public class LocationUtil {
    public String locationToString(@NonNull Location location) {
        return location.getWorld().getName() + ";" + location.getX() + ";"
                + location.getY() + ";" + location.getZ() + ";"
                + location.getYaw() + ";" + location.getPitch();
    }

    public Location stringToLocation(@NonNull String str) {
        String[] parts = str.split(";");

        String worldName = parts[0];
        double x = Double.parseDouble(parts[1]);
        double y = Double.parseDouble(parts[2]);
        double z = Double.parseDouble(parts[3]);
        float yaw = Float.parseFloat(parts[4]);
        float pitch = Float.parseFloat(parts[5]);

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return null;
        }

        return new Location(world, x, y, z, yaw, pitch);
    }
}
