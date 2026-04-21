package au.lupine.hopplet.listener;

import au.lupine.hopplet.Hopplet;
import au.lupine.hopplet.filter.Filter;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public final class FilterEditListener implements Listener {

    @EventHandler
    public void on(@NonNull PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) return;

        Player player = event.getPlayer();
        if (!player.isSneaking()) return;

        if (!player.getInventory().getItemInMainHand().isEmpty()) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        BlockState state = block.getState();
        if (!(state instanceof Hopper)) return;

        BlockBreakEvent bbe = new BlockBreakEvent(block, player);
        if (!bbe.callEvent()) return; // Player does not have permission to edit this hopper

        event.setCancelled(true);

        Component existing = ((Hopper) state).customName();
        String name = existing == null ? "" : PlainTextComponentSerializer.plainText().serialize(existing);

        Location location = block.getLocation();

        player.showDialog(dialog(
            player,
            name,
            Material.HOPPER,
            input -> {
                Hopplet instance = Hopplet.instance();
                instance.getServer().getRegionScheduler().run(instance, location, task -> {
                    Block target = location.getBlock();
                    if (!(target.getState() instanceof Hopper hopper)) return;

                    if (input.isBlank()) {
                        hopper.customName(null);
                    } else {
                        hopper.customName(Component.text(input));
                    }

                    hopper.setTransferCooldown(20);

                    hopper.update();

                    Random random = new Random();
                    location.getWorld().playSound(location, Sound.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, 0.75F, random.nextFloat(1.25F, 1.5F));
                });
            },
            () -> playSound(location, Sound.BLOCK_ANVIL_LAND, 0.3F, 1.25F, 1.5F))
        );
    }

    @EventHandler
    public void on(@NonNull PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof HopperMinecart hopper)) return;

        Player player = event.getPlayer();
        if (!player.isSneaking()) return;

        if (!player.getInventory().getItemInMainHand().isEmpty()) return;

        BlockBreakEvent bbe = new BlockBreakEvent(hopper.getLocation().getBlock(), player);
        if (!bbe.callEvent()) return;

        event.setCancelled(true);

        Component existing = hopper.customName();
        String name = existing == null ? "" : PlainTextComponentSerializer.plainText().serialize(existing);

        player.showDialog(dialog(
            player,
            name,
            Material.HOPPER_MINECART,
            input -> {
                Location location = hopper.getLocation();

                Hopplet instance = Hopplet.instance();
                instance.getServer().getRegionScheduler().run(instance, location, task -> {
                    if (input.isBlank()) {
                        hopper.customName(null);
                        return;
                    }

                    hopper.customName(Component.text(input));
                });

                playSound(location, Sound.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, 0.75F, 1.25F, 1.5F);
            },
            () -> {
                Location location = hopper.getLocation();
                playSound(location, Sound.BLOCK_ANVIL_LAND, 0.3F, 1.25F, 1.5F);
            })
        );
    }

    @SuppressWarnings("UnstableApiUsage")
    private @NonNull Dialog dialog(@NonNull Player player, @NonNull String text, @NonNull Material icon, @NonNull Consumer<String> confirm, @NonNull Runnable cancel) {
        ItemStack item = new ItemStack(icon);
        if (!text.isBlank()) {
            ItemMeta meta = item.getItemMeta();
            meta.customName(Component.text(text));
            item.setItemMeta(meta);
        }

        return Dialog.create(builder -> builder.empty()
            .base(DialogBase.builder(translate(player, "hopplet.dialog.edit_filter.title"))
                .body(List.of(
                    DialogBody.item(new ItemStack(item))
                        .build(),
                    DialogBody.plainMessage(translate(player, "hopplet.dialog.edit_filter.body.need_help")),
                    DialogBody.plainMessage(translate(player, "hopplet.dialog.edit_filter.body.documentation")),
                    DialogBody.plainMessage(translate(player, "hopplet.dialog.edit_filter.body.discord"))
                ))
                .inputs(List.of(
                    DialogInput.text("filter_input", translate(player, "hopplet.dialog.edit_filter.input.filter_input"))
                        .initial(text)
                        .maxLength(512)
                        .width(300)
                        .multiline(TextDialogInput.MultilineOptions.create(null, 100))
                        .build()
                ))
                .build()
            )
            .type(DialogType.confirmation(
                ActionButton.builder(translate(player, "hopplet.dialog.edit_filter.confirmation.confirm"))
                    .action(DialogAction.customClick((view, audience) -> {
                            String input = view.getText("filter_input");
                            if (input == null) return;

                            input = Filter.clean(input);

                            if (input.equals(text)) return;

                            confirm.accept(input);
                        }, ClickCallback.Options.builder()
                            .uses(1)
                            .build()
                    ))
                    .build(),
                ActionButton.builder(translate(player, "hopplet.dialog.edit_filter.confirmation.cancel"))
                    .action(DialogAction.customClick((view, audience) -> cancel.run(), ClickCallback.Options.builder()
                        .uses(1)
                        .build()
                    ))
                    .build()
            ))
        );
    }

    // https://github.com/PaperMC/Paper/issues/12971
    private @NonNull Component translate(@NonNull Player player, @NonNull String key) {
        return GlobalTranslator.render(Component.translatable(key), player.locale());
    }

    private void playSound(@NonNull Location location, @NonNull Sound sound, float volume, float origin, float bound) {
        Hopplet instance = Hopplet.instance();
        instance.getServer().getRegionScheduler().run(instance, location, task -> {
            Random random = new Random();
            location.getWorld().playSound(location, sound, volume, random.nextFloat(origin, bound));
        });
    }
}
