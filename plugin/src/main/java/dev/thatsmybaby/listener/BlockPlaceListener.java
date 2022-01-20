package dev.thatsmybaby.listener;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockPlaceEvent;
import dev.thatsmybaby.factory.ArenaFactory;
import dev.thatsmybaby.object.SWArena;

public class BlockPlaceListener implements Listener {

    @EventHandler
    public void onBlockPlaceEvent(BlockPlaceEvent ev) {
        SWArena arena = ArenaFactory.getInstance().getPlayerArena(ev.getPlayer());

        if (arena != null && arena.isStarted()) {
            return;
        }

        ev.setCancelled();
    }
}