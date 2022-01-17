package dev.thatsmybaby.command.argument;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.level.Level;
import cn.nukkit.utils.TextFormat;
import dev.thatsmybaby.SkyWars;
import dev.thatsmybaby.command.Argument;
import dev.thatsmybaby.factory.MapFactory;
import dev.thatsmybaby.object.SWMap;

import java.io.File;

public class CreateArgument extends Argument {

    public CreateArgument(String name, String permission, String... aliases) {
        super(name, permission, aliases);
    }

    @Override
    public void execute(CommandSender sender, String commandLabel, String argumentLabel, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(SkyWars.invalidUsageGame());

            return;
        }

        if (args.length < 2) {
            sender.sendMessage(TextFormat.RED + "Usage: /" + commandLabel + " create <minSlots> <maxSlots>");

            return;
        }

        Level level = ((Player) sender).getLevel();

        if (level == Server.getInstance().getDefaultLevel()) {
            sender.sendMessage(TextFormat.RED + "You can't setup maps in the lobby.");

            return;
        }

        MapFactory factory = MapFactory.getInstance();
        String mapName = level.getFolderName();

        if (factory.getMap(mapName) != null) {
            sender.sendMessage(TextFormat.RED + "Map already exists.");

            return;
        }

        int minSlots = Integer.parseInt(args[0]);
        int maxSlots = Integer.parseInt(args[1]);

        if (minSlots > maxSlots || minSlots <= 1) {
            sender.sendMessage(TextFormat.RED + "Invalid slots");

            return;
        }

        int priority = args.length > 2 ? Integer.parseInt(args[2]) : SkyWars.NORMAL_PRIORITY;

        if (priority < SkyWars.NORMAL_PRIORITY) {
            priority = SkyWars.NORMAL_PRIORITY;
        } else if (priority > SkyWars.HIGH_PRIORITY) {
            priority = SkyWars.HIGH_PRIORITY;
        }

        factory.registerNewMap(SWMap.of(mapName, minSlots, maxSlots, priority));
        factory.save();

        factory.copyMap(new File(Server.getInstance().getDataPath(), "worlds/" + mapName), new File(SkyWars.getInstance().getDataFolder(), "backups/" + mapName));
    }
}