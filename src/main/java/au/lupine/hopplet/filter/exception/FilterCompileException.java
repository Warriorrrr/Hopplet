package au.lupine.hopplet.filter.exception;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jspecify.annotations.NonNull;

public final class FilterCompileException extends RuntimeException implements ComponentLike {

    private final @NonNull Component component;

    public FilterCompileException(@NonNull Component component) {
        super(PlainTextComponentSerializer.plainText().serialize(component));
        this.component = component.colorIfAbsent(NamedTextColor.RED);
    }

    public FilterCompileException(@NonNull String message) {
        this(Component.text(message, NamedTextColor.RED));
    }

    @Override
    public @NonNull Component asComponent() {
        return component;
    }
}
