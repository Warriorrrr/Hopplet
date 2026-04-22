package au.lupine.hopplet.filter;

import au.lupine.hopplet.filter.exception.FilterCompileException;
import au.lupine.hopplet.util.BlockKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Item;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class Filter {

    /// Static {@link Filter} instance that always accepts any item.
    public static final @NonNull Filter TRUE = new Filter(new Node.Constant(true));

    private final @NonNull Node node;

    private Filter(@NonNull Node node) {
        this.node = node;
    }

    /// @return `true` if the filter accepts the item in the specified {@link Context}.
    public boolean test(@NonNull Context context) {
        return node.evaluate(context);
    }

    public static final class Compiler {

        /// Compiles a raw string into a {@link Filter}.
        /// @return A compiled Filter, or `null` if the specified string is null, empty, or contains only whitespace.
        /// @throws FilterCompileException Thrown if the specified string is non-empty but has a compilation error.
        public static @Nullable Filter compile(@Nullable String raw) throws FilterCompileException {
            if (raw == null) return null;

            String cleaned = clean(raw);
            if (cleaned.isEmpty()) return null;

            return new Filter(new Node.Constant(true));
        }

        public static @Nullable Filter compile(@Nullable Component component) throws FilterCompileException {
            return compile(serialise(component));
        }

        public static @Nullable Filter compile(@NonNull Hopper hopper) throws FilterCompileException {
            return compile(hopper.customName());
        }

        public static @Nullable Filter compile(@NonNull HopperMinecart hopper) throws FilterCompileException {
            return compile(hopper.customName());
        }

        private static @Nullable String serialise(@Nullable Component component) {
            return component == null ? null : PlainTextComponentSerializer.plainText().serialize(component);
        }

        /// Cleans this string by removing leading and trailing whitespace, as well as any line breaks.
        public static @NonNull String clean(@NonNull String string) {
            return string.trim().replaceAll("\\R", "");
        }
    }

    public static final class Cache {

        public static final Map<BlockKey, Filter> BLOCK_CACHE = new ConcurrentHashMap<>();
        public static final Map<UUID, Filter> ENTITY_CACHE = new ConcurrentHashMap<>();

        public static void cache(@NonNull BlockKey key, @NonNull Filter filter) {
            BLOCK_CACHE.put(key, filter);
        }

        public static @Nullable Filter get(@NonNull BlockKey key) {
            return BLOCK_CACHE.get(key);
        }

        public static void invalidate(@NonNull BlockKey key) {
            BLOCK_CACHE.remove(key);
        }

        public static void cache(@NonNull Location location, @NonNull Filter filter) {
            cache(BlockKey.of(location), filter);
        }

        public static @Nullable Filter get(@NonNull Location location) {
            return get(BlockKey.of(location));
        }

        public static void invalidate(@NonNull Location location) {
            invalidate(BlockKey.of(location));
        }

        public static void cache(@NonNull Block block, @NonNull Filter filter) {
            cache(block.getLocation(), filter);
        }

        public static @Nullable Filter get(@NonNull Block block) {
            return get(block.getLocation());
        }

        public static void invalidate(@NonNull Block block) {
            invalidate(block.getLocation());
        }

        public static void cache(@NonNull Hopper hopper, @NonNull Filter filter) {
            cache(hopper.getLocation(), filter);
        }

        public static @Nullable Filter get(@NonNull Hopper hopper) {
            return get(hopper.getLocation());
        }

        public static @Nullable Filter getOrCompile(@NonNull Hopper hopper) throws FilterCompileException {
            Filter filter = get(hopper);
            if (filter != null) return filter;

            Filter compiled = Filter.Compiler.compile(hopper);
            if (compiled != null) cache(hopper, compiled);
            return compiled;
        }

        public static void invalidate(@NonNull Hopper hopper) {
            invalidate(hopper.getLocation());
        }

        public static void cache(@NonNull UUID uuid, @NonNull Filter filter) {
            ENTITY_CACHE.put(uuid, filter);
        }

        public static @Nullable Filter get(@NonNull UUID uuid) {
            return ENTITY_CACHE.get(uuid);
        }

        public static void invalidate(@NonNull UUID uuid) {
            ENTITY_CACHE.remove(uuid);
        }

        public static void cache(@NonNull HopperMinecart hopper, @NonNull Filter filter) {
            cache(hopper.getUniqueId(), filter);
        }

        public static @Nullable Filter get(@NonNull HopperMinecart hopper) {
            return get(hopper.getUniqueId());
        }

        public static @Nullable Filter getOrCompile(@NonNull HopperMinecart hopper) throws FilterCompileException {
            Filter filter = get(hopper);
            if (filter != null) return filter;

            Filter compiled = Filter.Compiler.compile(hopper);
            if (compiled != null) cache(hopper, compiled);
            return compiled;
        }

        public static void invalidate(@NonNull HopperMinecart hopper) {
            invalidate(hopper.getUniqueId());
        }
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
