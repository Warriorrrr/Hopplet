package au.lupine.hopplet.util;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

public record BlockKey(@NonNull UUID world, int x, int y, int z) {

    public static @NonNull BlockKey of(@NonNull Location location) {
        return new BlockKey(location.getWorld().getUID(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public static @NonNull BlockKey of(@NonNull Block block) {
        return of(block.getLocation());
    }
}
