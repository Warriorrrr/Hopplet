package au.lupine.hopplet.listener;

import au.lupine.hopplet.filter.Filter;
import org.bukkit.block.Hopper;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jspecify.annotations.NonNull;

public final class HopperInventoryListener implements Listener {

    @EventHandler
    public void on(@NonNull InventoryMoveItemEvent event) {
        Inventory destination = event.getDestination();
        if (destination.getType() != InventoryType.HOPPER)return;

        InventoryHolder holder = destination.getHolder(false);
        if (holder == null) return;

        Filter filter = switch (holder) {
            case Hopper hopper -> Filter.of(hopper);
            case HopperMinecart hopper -> Filter.of(hopper);
            default -> Filter.TRUE;
        };

        Filter.Context context = Filter.Context.builder()
            .stack(event.getItem())
            .source(event.getSource())
            .destination(destination)
            .build();

        if (!filter.test(context)) event.setCancelled(true);
    }

    @EventHandler
    public void on(@NonNull InventoryPickupItemEvent event) {
        Inventory inventory = event.getInventory();
        if (!inventory.getType().equals(InventoryType.HOPPER)) return;

        InventoryHolder holder = inventory.getHolder(false);
        if (holder == null) return;

        Filter filter = switch (holder) {
            case Hopper hopper -> Filter.of(hopper);
            case HopperMinecart hopper -> Filter.of(hopper);
            default -> Filter.TRUE;
        };

        Filter.Context context = Filter.Context.builder()
            .item(event.getItem())
            .destination(inventory)
            .build();

        if (!filter.test(context)) event.setCancelled(true);
    }
}
