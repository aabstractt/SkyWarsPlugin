package dev.thatsmybaby;

import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.level.Position;
import cn.nukkit.utils.PluginException;

final public class Placeholders {

    public static String replacePlaceholders(String message, String... args) {
        return message;
    }

    public static String locationToString(Location loc) {
        return "";
    }

    public static Location locationFromString(String serialized, Level level) {
        return null;
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