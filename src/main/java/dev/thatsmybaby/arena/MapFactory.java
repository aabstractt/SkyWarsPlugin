package dev.thatsmybaby.arena;

import cn.nukkit.utils.Config;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.thatsmybaby.SkyWars;
import lombok.Getter;

import java.io.File;
import java.util.HashMap;

public class MapFactory {

    @Getter
    private final static MapFactory instance = new MapFactory();

    private final ObjectMapper mapper = new ObjectMapper();

    private final java.util.Map<String, Map> maps = new HashMap<>();

    public final void init() throws IllegalAccessException {
        Config config = new Config(new File(SkyWars.getInstance().getDataFolder(), "maps.json"));

        for (Object object : config.getAll().values()) {
            this.addMap(mapper.convertValue(object, Map.class), false);
        }
    }

    public final void addMap(Map map, boolean save) throws IllegalAccessException {
        this.maps.put(map.getMapName(), map);

        if (save) {
            Config config = new Config(new File(SkyWars.getInstance().getDataFolder(), "maps.json"));

            config.set(map.getMapName(), map.serialize());
        }
    }

    public final void removeMap(String mapName) {

    }

    public final Map getMap(String mapName) {
        return this.maps.get(mapName);
    }

    public final Map getRandomMap() {
        return null;
    }
}