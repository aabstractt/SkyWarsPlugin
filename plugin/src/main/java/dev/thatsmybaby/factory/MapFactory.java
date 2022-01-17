package dev.thatsmybaby.factory;

import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.thatsmybaby.SkyWars;
import dev.thatsmybaby.object.SWMap;
import lombok.Getter;

import java.io.File;
import java.util.HashMap;

final public class MapFactory {

    @Getter private final static MapFactory instance = new MapFactory();
    @Getter private final java.util.Map<String, SWMap> maps = new HashMap<>();

    private final ObjectMapper mapper = new ObjectMapper();

    public void init() {
        Config config = new Config(new File(SkyWars.getInstance().getDataFolder(), "maps.json"));

        for (Object object : config.getAll().values()) {
            System.out.println(object);

            this.registerNewMap(mapper.convertValue(object, SWMap.class), false);
        }

        SkyWars.getInstance().getLogger().info(TextFormat.AQUA + "SkyWars: " + this.maps.size() + " map(s) loaded.");
    }

    public void registerNewMap(SWMap map, boolean save) {
        this.maps.put(map.getMapName(), map);

        if (save) {
            Config config = new Config(new File(SkyWars.getInstance().getDataFolder(), "maps.json"));

            config.set(map.getMapName(), map.serialize());
        }
    }

    public void copyMap(File from, File to) {
        copyMap(from.toString(), to.toString());
    }

    public void copyMap(String from, String to) {
        // TODO: Copy map folder
    }

    public void removeMap(String mapName) {
        this.maps.remove(mapName);
    }

    public SWMap getMap(String mapName) {
        return this.maps.get(mapName);
    }

    public SWMap getRandomMap() {
        SWMap betterMap = null;

        for (SWMap map : this.maps.values()) {
            if (betterMap == null) {
                betterMap = map;

                continue;
            }

            if (ArenaFactory.getInstance().getMapArenas(map).size() > ArenaFactory.getInstance().getMapArenas(betterMap).size()) {
                continue;
            }

            betterMap = map;
        }

        return betterMap;
    }
}