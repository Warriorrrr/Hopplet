package au.lupine.hopplet.filter.function;

import au.lupine.hopplet.Hopplet;
import au.lupine.hopplet.filter.Filter;
import au.lupine.hopplet.filter.Function;
import au.lupine.hopplet.filter.exception.FilterCompileException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.translation.Argument;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Item;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class ThrowerFunction implements Function<Set<UUID>> {

    @Override
    public @NonNull String name() {
        return "thrower";
    }

    @Override
    public @NonNull Set<String> aliases() {
        return Set.of("thrown_by");
    }

    @Override
    public @NonNull Component description() {
        return Component.translatable("hopplet.filter.function.thrower.description");
    }

    @Override
    public @NonNull Plugin plugin() {
        return Hopplet.instance();
    }

    @Override
    public @NonNull Set<UUID> compile(@NonNull List<String> arguments) throws FilterCompileException {
        if (arguments.isEmpty()) {
            throw new FilterCompileException(
                Component.translatable(
                    "hopplet.filter.function.default.compilation.exception.no_arguments_provided",
                    Argument.string("name", name())
                )
            );
        }

        Set<UUID> uuids = new HashSet<>();
        for (String argument : arguments) {
            try {
                uuids.add(UUID.fromString(argument));
            } catch (IllegalArgumentException e) {
                throw new FilterCompileException(
                    Component.translatable(
                        "hopplet.filter.function.thrower.exception.invalid_uuid",
                        Argument.string("argument", argument)
                    )
                );
            }
        }

        return uuids;
    }

    @Override
    public boolean test(Filter.@NonNull Context context, @NonNull Set<UUID> uuids) {
        Item item = context.item();
        if (item == null) return false;

        UUID thrower = item.getThrower();
        if (thrower == null) return false;

        return uuids.contains(thrower);
    }
}
