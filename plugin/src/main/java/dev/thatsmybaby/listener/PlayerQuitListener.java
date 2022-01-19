package dev.thatsmybaby.listener;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import dev.thatsmybaby.factory.ArenaFactory;

public class PlayerQuitListener implements Listener {

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent ev) {
        ArenaFactory.getInstance().handlePlayerDeath(ev.getPlayer(), ArenaFactory.getInstance().getPlayerArena(ev.getPlayer()), EntityDamageEvent.DamageCause.MAGIC, true);
    }
}