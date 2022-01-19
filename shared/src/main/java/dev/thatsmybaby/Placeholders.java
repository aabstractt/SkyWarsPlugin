package dev.thatsmybaby;

import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.level.Position;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.PluginException;
import cn.nukkit.utils.TextFormat;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

final public class Placeholders {

    public static Map<String, Object> messages = new HashMap<>();

    public static String replacePlaceholders(String text, String... args) {
        if (messages.containsKey(text)) {
            text = messages.get(text).toString();
        }

        for (int i = 0; i < args.length; i++) {
            text = text.replaceAll("\\{%" + i + "}", args[i]);
        }

        return TextFormat.colorize(text);
    }

    public static String locationToString(Location loc) {
        return positionToString(loc) + ":" + loc.yaw + ":" + loc.pitch;
    }

    public static Location locationFromString(String string, Level level) {
        String[] split = string.split(":");

        return Location.fromObject(positionFromString(string), level, Double.parseDouble(split[4]), Double.parseDouble(split[5]));
    }

    public static String positionToString(Position pos) {
        return pos.getFloorX() + ":" + pos.getFloorY() + ":" + pos.getFloorZ() + ":" + pos.getLevel().getFolderName();
    }

    public static Position positionFromString(String string) {
        String[] split = string.split(":");

        if (split.length < 4) {
            throw new PluginException("Invalid length, expected 4 and received " + split.length);
        }

        return new Position(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]), Server.getInstance().getLevelByName(split[3]));
    }

    public static int parseInt(String parse) {
        try {
            return Integer.parseInt(parse);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }
}