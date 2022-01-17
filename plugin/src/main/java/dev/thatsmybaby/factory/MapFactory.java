package dev.thatsmybaby.factory;

import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.thatsmybaby.SkyWars;
import dev.thatsmybaby.object.SWMap;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

final public class MapFactory {

    @Getter private final static MapFactory instance = new MapFactory();
    @Getter private final java.util.Map<String, SWMap> maps = new HashMap<>();

    private final ObjectMapper mapper = new ObjectMapper();

    public void init() {
        Config config = new Config(new File(SkyWars.getInstance().getDataFolder(), "maps.json"));

        for (Object object : config.getAll().values()) {
            System.out.println(object);

            this.registerNewMap(mapper.convertValue(object, SWMap.class));
        }

        SkyWars.getInstance().getLogger().info(TextFormat.AQUA + "SkyWars: " + this.maps.size() + " map(s) loaded.");

        if ((new File(SkyWars.getInstance().getDataFolder(), "backups")).mkdirs()) {
            SkyWars.getInstance().getLogger().info(String.format("Folder %s was generated successfully.", "arenas"));
        }
    }

    public void registerNewMap(SWMap map) {
        this.maps.put(map.getMapName(), map);
    }

    public void save() {
        Config config = new Config(new File(SkyWars.getInstance().getDataFolder(), "maps.json"));

        for (SWMap map : this.maps.values()) {
            config.set(map.getMapName(), map.serialize());
        }

        config.save();
    }

    public void copyMap(File from, File to) {
        copyMap(from.toString(), to.toString());
    }

    public void copyMap(String from, String to) {
        Path sourceDirectory = Paths.get(from);
        Path targetDirectory = Paths.get(to);

        //copy source to target using Files Class
        try {
            Files.copy(sourceDirectory, targetDirectory);
        } catch (IOException e) {
            e.printStackTrace();
        }
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