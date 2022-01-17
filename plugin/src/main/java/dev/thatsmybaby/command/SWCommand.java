package dev.thatsmybaby.command;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;
import dev.thatsmybaby.command.argument.CreateArgument;
import dev.thatsmybaby.command.argument.SpawnArgument;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class SWCommand extends Command {

    private final List<Argument> arguments = new ArrayList<>();

    public SWCommand(String name, String description) {
        super(name, description);

        this.addArgument(
                new CreateArgument("create", "skywars.command.create"),
                new SpawnArgument("spawn", "skywars.command.spawn")
        );
    }

    private void addArgument(Argument... arguments) {
        this.arguments.addAll(Arrays.asList(arguments));
    }

    private Optional<Argument> getArgument(String label) {
        return this.arguments.stream().filter(argument -> argument.match(label)).findFirst();
    }

    @Override
    public boolean execute(CommandSender commandSender, String label, String[] args) {
        if (args.length == 0) {
            commandSender.sendMessage(TextFormat.RED + "Usage: /" + label + " help");

            return false;
        }

        if (args[0].equalsIgnoreCase("help")) {
            commandSender.sendMessage(TextFormat.BLUE + "SkyWars Commands.");

            this.arguments.forEach(argument -> commandSender.sendMessage(TextFormat.RED + "/" + String.format("%s %s", label, argument.getName())));

            return false;
        }

        Argument argument = this.getArgument(args[0]).orElse(null);

        if (argument == null) {
            commandSender.sendMessage(TextFormat.RED + "Usage: /" + label + " help");

            return false;
        }

        if (argument.getPermission() != null && !commandSender.hasPermission(argument.getPermission())) {
            commandSender.sendMessage(TextFormat.RED + "You don't have permissions to use this command.");

            return false;
        }

        argument.execute(commandSender, label, args[0], Arrays.copyOfRange(args, 1, args.length));

        return false;
    }
}