package au.lupine.hopplet.filter;

import au.lupine.hopplet.filter.exception.FilterCompileException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Item;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

public final class Filter {

    /// Static {@link Filter} instance that always accepts any item.
    public static final @NonNull Filter TRUE = new Filter(new Node.Constant(true));

    private final @NonNull Node node;

    private Filter(@NonNull Node node) {
        this.node = node;
    }

    public static @NonNull Filter compile(@NonNull String raw) throws FilterCompileException {
        String cleaned = clean(raw);
        if (cleaned.isEmpty()) return TRUE;

        return TRUE;
    }

    public boolean test(@NonNull Context context) {
        return node.evaluate(context);
    }

    public static @NonNull Filter of(@Nullable String raw) throws FilterCompileException {
        if (raw == null) return TRUE;
        return Filter.compile(raw);
    }

    public static @NonNull Filter of(@Nullable Component component) throws FilterCompileException {
        return of(serialise(component));
    }

    public static @NonNull Filter of(@NonNull Hopper hopper) throws FilterCompileException {
        return of(hopper.customName());
    }

    public static @NonNull Filter of(@NonNull HopperMinecart hopper) throws FilterCompileException {
        return of(hopper.customName());
    }

    private static @Nullable String serialise(@Nullable Component component) {
        return component == null ? null : PlainTextComponentSerializer.plainText().serialize(component);
    }

    public static @NonNull String clean(@NonNull String string) {
        return string.trim().replaceAll("\\R", "");
    }

    public static final class Context {

        private final @NonNull ItemStack stack;
        private final @Nullable Item item;
        private final @Nullable Inventory source;
        private final @NonNull Inventory destination;

        private Context(@NonNull ItemStack stack, @Nullable Item item, @Nullable Inventory source, @NonNull Inventory destination) {
            if (item != null && !item.getItemStack().equals(stack)) throw new IllegalStateException("Specified ItemStack does not match the ItemStack of Item.");

            this.stack = stack;
            this.item = item;
            this.source = source;
            this.destination = destination;
        }

        public static @NonNull Builder builder() {
            return new Builder();
        }

        public @NonNull ItemStack stack() {
            return stack;
        }

        public @Nullable Item item() {
            return item;
        }

        public @Nullable Inventory source() {
            return source;
        }

        public @NonNull Inventory destination() {
            return destination;
        }

        public static final class Builder {

            private ItemStack stack;
            private @Nullable Item item;
            private @Nullable Inventory source;
            private Inventory destination;

            private Builder() {}

            public @NonNull Context build() {
                return new Context(stack, item, source, destination);
            }

            public @NonNull Builder stack(@NonNull ItemStack stack) {
                this.stack = stack;
                return this;
            }

            public @NonNull Builder item(@Nullable Item item) {
                this.item = item;
                if (item != null) this.stack = item.getItemStack();
                return this;
            }

            public @NonNull Builder source(@Nullable Inventory source) {
                this.source = source;
                return this;
            }

            public @NonNull Builder destination(@NonNull Inventory destination) {
                this.destination = destination;
                return this;
            }
        }
    }

    private sealed interface Node {

        boolean evaluate(@NonNull Context context);

        record Call<ArgumentType>(@NonNull Function<ArgumentType> function, @NonNull ArgumentType argument) implements Node {
            @Override public boolean evaluate(@NonNull Context context) {
                return function.test(context, argument);
            }

            static <ArgumentType> @NonNull Call<ArgumentType> of(@NonNull Function<ArgumentType> function, @NonNull List<String> arguments) throws FilterCompileException {
                ArgumentType compiled = function.compile(arguments);
                return new Call<>(function, compiled);
            }
        }

        record And(@NonNull Node left, @NonNull Node right) implements Node {
            @Override public boolean evaluate(@NonNull Context context) {
                return left.evaluate(context) && right.evaluate(context);
            }
        }

        record Or(@NonNull Node left, @NonNull Node right) implements Node {
            @Override public boolean evaluate(@NonNull Context context) {
                return left.evaluate(context) || right.evaluate(context);
            }
        }

        record Not(@NonNull Node inner) implements Node {
            @Override public boolean evaluate(@NonNull Context context) {
                return !inner.evaluate(context);
            }
        }

        record Constant(boolean value) implements Node {
            @Override public boolean evaluate(@NonNull Context context) {
                return value;
            }
        }
    }
}
