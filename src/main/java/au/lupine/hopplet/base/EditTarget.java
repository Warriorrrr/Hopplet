package au.lupine.hopplet.base;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

public interface EditTarget {

    @NonNull String name();

    @NonNull Location location();

    @NonNull Material icon();

    void edit(@NonNull String input, @NonNull Player player);
}
