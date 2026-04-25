package au.lupine.hopplet.util.edit;

import au.lupine.hopplet.Hopplet;
import au.lupine.hopplet.base.EditTarget;
import au.lupine.hopplet.filter.Filter;
import au.lupine.hopplet.filter.exception.FilterCompileException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

public final class HopperEditTarget implements EditTarget {

    private final @NonNull String name;
    private final @NonNull Location location;

    public HopperEditTarget(@NonNull Hopper hopper) {
        Component existing = hopper.customName();
        this.name = existing == null ? "" : PlainTextComponentSerializer.plainText().serialize(existing);

        this.location = hopper.getLocation();
    }

    @Override
    public @NonNull String name() {
        return name;
    }

    @Override
    public @NonNull Location location() {
        return location;
    }

    @Override
    public @NonNull Material icon() {
        return Material.HOPPER;
    }

    @Override
    public void edit(@NonNull String input, @NonNull Player player) {
        Hopplet instance = Hopplet.instance();

        instance.getServer().getRegionScheduler().run(instance, location, task -> {
            Block target = location.getBlock();
            if (!(target.getState(false) instanceof Hopper hopper)) return;

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

            hopper.setTransferCooldown(20);
            hopper.update();
        });
    }
}
