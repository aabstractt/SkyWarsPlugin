package dev.thatsmybaby.listener;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.entity.EntityLevelChangeEvent;
import dev.thatsmybaby.factory.ArenaFactory;
import dev.thatsmybaby.object.SWArena;
import dev.thatsmybaby.player.SWPlayer;

public class EntityDamageListener implements Listener {

    @EventHandler
    public void onEntityDamageEvent(EntityDamageEvent ev) {
        Entity entity = ev.getEntity();

        if (!(entity instanceof Player)) {
            ev.setCancelled();

            return;
        }

        if (entity.getValidLevel() == Server.getInstance().getDefaultLevel()) {
            ev.setCancelled();

            return;
        }

        SWArena arena = ArenaFactory.getInstance().getPlayerArena((Player) entity);

        if (arena == null || !arena.isStarted()) {
            ev.setCancelled();

            return;
        }

        SWPlayer instance = arena.getPlayer((Player) entity);

        if (instance == null) {
            ev.setCancelled();

            return;
        }

        if (ev instanceof EntityDamageByEntityEvent) {
            Entity target = ((EntityDamageByEntityEvent) ev).getDamager();

            if (!(target instanceof Player)) {
                ev.setCancelled();

                return;
            }

            if (!arena.inArenaAsPlayer((Player) target)) {
                ev.setCancelled();

                return;
            }

            instance.attack((Player) target);
        }

        if ((entity.getHealth() - ev.getFinalDamage()) / 2 > 0) {
            return;
        }

        ArenaFactory.getInstance().handlePlayerDeath((Player) entity, arena, ev.getCause(), false);

        arena.removePlayer((Player) entity);
        arena.joinAsSpectator(instance);

        ev.setCancelled();
    }

    @EventHandler
    public void onEntityChangeLevelEvent(EntityLevelChangeEvent ev) {
        Entity entity = ev.getEntity();

        if (!(entity instanceof Player)) {
            return;
        }

        String worldName = ev.getTarget().getFolderName();
        SWArena arena = ArenaFactory.getInstance().getPlayerArena((Player) entity);

        if (arena == null) {
            this.tryJoinArena((Player) entity, worldName);

            return;
        }

        if (arena.getWorldName().equalsIgnoreCase(worldName)) {
            return;
        }

        ArenaFactory.getInstance().handlePlayerDeath((Player) entity, arena, EntityDamageEvent.DamageCause.MAGIC, true);

        arena.forceRemovePlayer((Player) entity);

        this.tryJoinArena((Player) entity, worldName);
    }

    private void tryJoinArena(Player player, String worldName) {
        SWArena arena = ArenaFactory.getInstance().getArena(worldName);

        if (arena == null) {
            return;
        }

        SWPlayer target = new SWPlayer(player.getName(), player.getLoginChainData().getXUID(), arena, -1);

        arena.joinAsSpectator(target);

        target.lobbyAttributes();
    }
}