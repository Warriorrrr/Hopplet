package au.lupine.hopplet.listener;

import au.lupine.hopplet.filter.Filter;
import au.lupine.hopplet.filter.exception.FilterCompileException;
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Hopper;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.UUID;

public final class FilterCacheListener implements Listener {

    private void invalidate(@NonNull Collection<Block> blocks) {
        blocks.forEach(block -> {
            if (block.getType() == Material.HOPPER) Filter.Cache.invalidate(block);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void on(@NonNull BlockPlaceEvent event) {
        if (!(event.getBlock().getState(false) instanceof Hopper hopper)) return;

        Filter filter;
        try {
            filter = Filter.Compiler.compile(hopper);
        } catch (FilterCompileException e) {
            return;
        }

        if (filter == null) return;

        Filter.Cache.cache(hopper, filter);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void on(@NonNull BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.HOPPER) Filter.Cache.invalidate(event.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void on(@NonNull EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof HopperMinecart hopper)) return;

        Filter filter;
        try {
            filter = Filter.Compiler.compile(hopper);
        } catch (FilterCompileException e) {
            return;
        }

        if (filter == null) return;

        Filter.Cache.cache(hopper, filter);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void on(@NonNull EntityRemoveFromWorldEvent event) {
        if (event.getEntity() instanceof HopperMinecart hopper) Filter.Cache.invalidate(hopper);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void on(@NonNull BlockExplodeEvent event) {
        invalidate(event.blockList());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void on(@NonNull EntityExplodeEvent event) {
        invalidate(event.blockList());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void on(@NonNull BlockPistonExtendEvent event) {
        invalidate(event.getBlocks());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void on(@NonNull BlockPistonRetractEvent event) {
        invalidate(event.getBlocks());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void on(@NonNull WorldUnloadEvent event) {
        UUID uuid = event.getWorld().getUID();
        Filter.Cache.BLOCK_CACHE.keySet().removeIf(key -> key.world().equals(uuid));
    }
}
