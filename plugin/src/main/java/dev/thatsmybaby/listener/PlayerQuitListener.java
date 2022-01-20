package dev.thatsmybaby.listener;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import dev.thatsmybaby.factory.ArenaFactory;
import dev.thatsmybaby.object.SWArena;

public class PlayerQuitListener implements Listener {

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent ev) {
        Player player = ev.getPlayer();

        SWArena arena = ArenaFactory.getInstance().getPlayerArena(player);

        if (arena == null) {
            return;
        }

        ArenaFactory.getInstance().handlePlayerDeath(player, arena, EntityDamageEvent.DamageCause.MAGIC, true);

        arena.forceRemovePlayer(player);
    }
}