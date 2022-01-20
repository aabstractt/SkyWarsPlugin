package dev.thatsmybaby.listener;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import dev.thatsmybaby.factory.ArenaFactory;
import dev.thatsmybaby.object.SWArena;

public class BlockBreakListener implements Listener {

    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent ev) {
        SWArena arena = ArenaFactory.getInstance().getPlayerArena(ev.getPlayer());

        if (arena != null && arena.isStarted()) {
            return;
        }

        ev.setCancelled();
    }
}