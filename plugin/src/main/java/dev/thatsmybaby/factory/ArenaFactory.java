package dev.thatsmybaby.factory;

import cn.nukkit.Player;
import dev.thatsmybaby.SkyWars;
import dev.thatsmybaby.object.SWArena;
import dev.thatsmybaby.object.SWMap;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

final public class ArenaFactory {

    @Getter private final static ArenaFactory instance = new ArenaFactory();
    @Getter private final Map<Integer, SWArena> arenas = new HashMap<>();

    private int gamesPlayed = 1;

    public SWArena registerNewArena() {
        return registerNewArena(null);
    }

    public SWArena registerNewArena(SWMap map) {
        if (map == null) {
            map = MapFactory.getInstance().getRandomMap();
        }

        if (map == null) {
            SkyWars.getInstance().getLogger().alert("SWMap is null, cannot found any map");

            return null;
        }

        SWArena arena = new SWArena(this.gamesPlayed++, map);

        this.arenas.put(arena.getId(), arena);

        return arena;
    }

    public void unregisterArena(int id) {
        this.arenas.remove(id);
    }

    public List<SWArena> getMapArenas(SWMap map) {
        return this.arenas.values().stream().filter(arena -> arena.getMap().getMapName().equalsIgnoreCase(map.getMapName())).collect(Collectors.toList());
    }

    public SWArena getPlayerArena(Player player) {
        return this.arenas.values().stream().filter(arena -> arena.inArena(player)).findFirst().orElse(null);
    }

    public SWArena getRandomArena() {
        SWArena betterArena = null;

        for (SWArena arena : this.arenas.values()) {
            if (!arena.isAllowedJoin()) {
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