package dev.thatsmybaby.arena;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;

@AllArgsConstructor
@Getter
public class Map {

    private final String mapName;
    private int minSlots;
    private int maxSlots;
    private int priority;

    public java.util.Map<String, Object> serialize() throws IllegalAccessException {
        HashMap<String, Object> serialized = new HashMap<>();

        for (Field field : this.getClass().getDeclaredFields()) {
            serialized.put(field.getName(), field.get(this));
        }

        return Collections.unmodifiableMap(serialized);
    }
}