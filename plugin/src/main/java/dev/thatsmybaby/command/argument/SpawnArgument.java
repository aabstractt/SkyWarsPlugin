package dev.thatsmybaby.command.argument;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.utils.TextFormat;
import dev.thatsmybaby.Placeholders;
import dev.thatsmybaby.SkyWars;
import dev.thatsmybaby.command.Argument;
import dev.thatsmybaby.factory.MapFactory;
import dev.thatsmybaby.object.SWMap;

public class SpawnArgument extends Argument {

    public SpawnArgument(String name, String permission, String... aliases) {
        super(name, permission, aliases);
    }

    @Override
    public void execute(CommandSender sender, String commandLabel, String argumentLabel, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(SkyWars.invalidUsageGame());

            return;
        }

        if (args.length == 0) {
            sender.sendMessage(TextFormat.RED + "Usage: /" + commandLabel + " spawn <slot>");

            return;
        }

        Level level = ((Player) sender).getLevel();

        if (level == Server.getInstance().getDefaultLevel()) {
            sender.sendMessage(TextFormat.RED + "You can't setup maps in the lobby.");

            return;
        }

        SWMap map = MapFactory.getInstance().getMap(level.getFolderName());

        if (map == null) {
            sender.sendMessage(TextFormat.RED + "This map doesn't exist.");

            return;
        }

        int slot = Placeholders.parseInt(args[0]);

        if (slot <= 0 || slot > map.getMaxSlots()) {
            sender.sendMessage(TextFormat.RED + "You must specify a valid slot.");

            return;
        }

        Location l = ((Player) sender).getLocation();
        map.setSpawnLocation(slot, l);

        MapFactory.getInstance().save();

        sender.sendMessage(TextFormat.colorize(String.format("&9Spawn %s set to &6X:&b%s &6Y:&b %s &6Z:&b %s &6Yaw:&b %s &6Pitch:&b %s", slot, l.getFloorX(), l.getFloorY(), l.getFloorZ(), l.yaw, l.pitch)));
    }
}