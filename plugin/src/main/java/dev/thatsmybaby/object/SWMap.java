package dev.thatsmybaby.object;

import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import dev.thatsmybaby.Placeholders;
import lombok.*;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
public class SWMap {

    private String mapName;
    private int minSlots;
    private int maxSlots;
    private int priority;

    private Map<Integer, String> spawns;

    public void setSpawnLocation(int slot, Location loc) {
        this.spawns.put(slot, Placeholders.locationToString(loc));
    }

    public Location getSpawnLocation(int slot, Level level) {
        String serialized = this.spawns.get(slot);

        if (serialized == null) {
            return null;
        }

        return Placeholders.locationFromString(serialized, level);
    }

    public java.util.Map<String, Object> serialize() {
        HashMap<String, Object> serialized = new HashMap<>();

        for (Field field : this.getClass().getDeclaredFields()) {
            try {
                serialized.put(field.getName(), field.get(this));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return Collections.unmodifiableMap(serialized);
    }

    public static SWMap of(String mapName, int minSlots, int maxSlots, int priority) {
        return new SWMap(mapName, minSlots, maxSlots, priority, new HashMap<>());
    }
}