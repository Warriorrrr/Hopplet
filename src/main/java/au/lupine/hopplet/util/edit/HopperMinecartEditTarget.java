package au.lupine.hopplet.util.edit;

import au.lupine.hopplet.Hopplet;
import au.lupine.hopplet.base.EditTarget;
import au.lupine.hopplet.filter.Filter;
import au.lupine.hopplet.filter.exception.FilterCompileException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.HopperMinecart;
import org.jspecify.annotations.NonNull;

public final class HopperMinecartEditTarget implements EditTarget {

    private final @NonNull HopperMinecart hopper;
    private final @NonNull String name;

    public HopperMinecartEditTarget(@NonNull HopperMinecart hopper) {
        this.hopper = hopper;

        Component existing = hopper.customName();
        this.name = existing == null ? "" : PlainTextComponentSerializer.plainText().serialize(existing);
    }

    @Override
    public @NonNull String name() {
        return name;
    }

    @Override
    public @NonNull Location location() {
        return hopper.getLocation();
    }

    @Override
    public @NonNull Material icon() {
        return Material.HOPPER_MINECART;
    }

    @Override
    public void edit(@NonNull String input, @NonNull Player player) {
        Hopplet instance = Hopplet.instance();

        instance.getServer().getRegionScheduler().run(instance, hopper.getLocation(), task -> {
            if (!hopper.isValid()) return;

            Filter.Cache.invalidate(hopper);

            if (input.isBlank()) {
                hopper.customName(null);
            } else {
                hopper.customName(Component.text(input));

                try {
                    Filter filter = Filter.Compiler.compile(input);
                    if (filter != null) Filter.Cache.cache(hopper, filter);
                } catch (FilterCompileException e) {
                    player.sendMessage(e);
                }
            }
        });
    }
}
