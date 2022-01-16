package dev.thatsmybaby.command.argument;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.EventPriority;
import cn.nukkit.level.Level;
import cn.nukkit.utils.TextFormat;
import dev.thatsmybaby.SkyWars;
import dev.thatsmybaby.object.SWMap;
import dev.thatsmybaby.command.Argument;
import dev.thatsmybaby.factory.MapFactory;

import java.io.File;

public class CreateArgument extends Argument {

    public CreateArgument(String name, String permission, String... aliases) {
        super(name, permission, aliases);
    }

    @Override
    public void execute(CommandSender sender, String commandLabel, String argumentLabel, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(TextFormat.RED + "Run this command in-game");

            return;
        }

        if (args.length < 3) {
            sender.sendMessage(TextFormat.RED + "Usage: /" + commandLabel + " create <lobby> <minSlots> <maxSlots>");

            return;
        }

        Level level = ((Player) sender).getLevel();

        if (level == Server.getInstance().getDefaultLevel()) {
            sender.sendMessage(TextFormat.RED + "You can't setup maps in the lobby.");

            return;
        }

        String mapName = level.getFolderName();

        if (MapFactory.getInstance().getMap(mapName) != null) {
            sender.sendMessage(TextFormat.RED + "Map already exists.");

            return;
        }

        MapFactory.getInstance().registerNewMap(SWMap.of(mapName, Integer.parseInt(args[0]), Integer.parseInt(args[1]), args.length == 3 ? Integer.parseInt(args[2]) : EventPriority.NORMAL.ordinal()), true);

        MapFactory.getInstance().copyMap(new File(Server.getInstance().getDataPath(), "worlds/" + mapName), new File(SkyWars.getInstance().getDataFolder(), "backups/" + mapName));
    }
}