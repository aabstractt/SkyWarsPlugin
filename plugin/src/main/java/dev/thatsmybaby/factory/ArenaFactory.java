package dev.thatsmybaby.factory;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.level.Location;
import dev.thatsmybaby.Placeholders;
import dev.thatsmybaby.SkyWars;
import dev.thatsmybaby.TaskUtils;
import dev.thatsmybaby.object.SWArena;
import dev.thatsmybaby.object.SWMap;
import dev.thatsmybaby.object.task.GameCountDownUpdateTask;
import dev.thatsmybaby.player.SWPlayer;
import lombok.Getter;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

final public class ArenaFactory {

    @Getter private final static ArenaFactory instance = new ArenaFactory();
    @Getter private final Map<Integer, SWArena> arenas = new HashMap<>();

    private int gamesPlayed = 1;

    public SWArena registerNewArena(String positionString) {
        return registerNewArena(null, positionString);
    }

    public SWArena registerNewArena(SWMap map, String positionString) {
        if (map == null) {
            map = MapFactory.getInstance().getRandomMap();
        }

        if (map == null) {
            SkyWars.getInstance().getLogger().alert("SWMap is null, cannot found any map");

            return null;
        }

        SWArena arena = new SWArena(this.gamesPlayed++, map, positionString);

        this.arenas.put(arena.getId(), arena);

        TaskUtils.runAsync(() -> {
            MapFactory.getInstance().copyMap(new File(SkyWars.getInstance().getDataFolder(), "backups/" + arena.getMap().getMapName()), new File(Server.getInstance().getDataPath(), "worlds/" + arena.getWorldName()));

            Server.getInstance().loadLevel(arena.getWorldName());

            arena.scheduleRepeating(new GameCountDownUpdateTask(arena, MapFactory.getInstance().getInitialCountdown(), 0), 20);
        });

        return arena;
    }

    public boolean joinArena(Player player, int id) {
        SWArena arena = getArena(id);

        if (arena == null) {
            return false;
        }

        return joinArena(player, arena);
    }

    public boolean joinArena(Player player, SWArena arena) {
        if (!arena.isAllowedJoin()) {
            return false;
        }

        arena.joinAsPlayer(player);

        return true;
    }

    public void handlePlayerDeath(Player instance, SWArena arena, EntityDamageEvent.DamageCause cause, boolean removeScoreboard) {
        if (arena == null) {
            SkyWars.getInstance().getLogger().warning(instance.getName() + " tried died without game?");

            return;
        }

        SWPlayer player = arena.getPlayer(instance);

        if (player == null) {
            return;
        }

        if (removeScoreboard) {
            player.getInstance().dataPacket(player.getScoreboardBuilder().remove());
        }

        if (!arena.isStarted()) {
            arena.broadcastMessage("PLAYER_LEFT", player.getName(), String.valueOf(arena.getPlayers().size() - 1), String.valueOf(arena.getMap().getMaxSlots()));

            TaskUtils.runLater(arena::pushUpdate, 5);

            return;
        }

        Location location = arena.getMap().getSpawnLocation(player.getSlot(), arena.getWorld());

        if (location == null) {
            return;
        }

        instance.teleport(location);

        SWPlayer lastAttack = player.getLastAttack();

        String message;

        if (lastAttack != null) {
            message = Placeholders.replacePlaceholders(SkyWars.getRandomKillMessage("ENTITY_ATTACK"), player.getName(), lastAttack.getName());

            lastAttack.increaseKills();
        } else {
            message = Placeholders.replacePlaceholders(SkyWars.getRandomKillMessage(cause.name()), player.getName());
        }

        arena.broadcastMessage(message);
    }

    public void unregisterArena(int id) {
        this.arenas.remove(id);
    }

    public List<SWArena> getMapArenas(SWMap map) {
        return this.arenas.values().stream().filter(arena -> arena.getMap().getMapName().equalsIgnoreCase(map.getMapName())).collect(Collectors.toList());
    }

    public SWArena getArena(int id) {
        return this.arenas.get(id);
    }

    public SWArena getArena(String worldName) {
        return this.arenas.values().stream().filter(arena -> arena.getWorldName().equalsIgnoreCase(worldName)).findAny().orElse(null);
    }

    public SWArena getSignArena(String signString) {
        return this.arenas.values().stream().filter(arena -> signString.equals(arena.getRawId())).findFirst().orElse(null);
    }

    public SWArena getPlayerArena(Player player) {
        return this.arenas.values().stream().filter(arena -> arena.inArena(player)).findFirst().orElse(null);
    }

    public SWArena getRandomArena(boolean withoutSign) {
        SWArena betterArena = null;

        for (SWArena arena : this.arenas.values()) {
            if (!arena.isAllowedJoin() || (arena.getRawId() != null && withoutSign)) {
                continue;
            }

            if (betterArena == null) {
                betterArena = arena;

                continue;
            }

            if (betterArena.getPlayers().size() > arena.getPlayers().size()) {
                continue;
            }

            betterArena = arena;
        }

        return betterArena;
    }
}