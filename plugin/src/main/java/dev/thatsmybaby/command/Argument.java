package dev.thatsmybaby.command;

import cn.nukkit.command.CommandSender;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
public abstract class Argument {

    @Getter private final String name;
    @Getter private final String permission;
    @Getter private final String[] aliases;

    public boolean match(String label) {
        return this.name.equalsIgnoreCase(label) || Arrays.stream(this.aliases).anyMatch(alias -> alias.equalsIgnoreCase(name));
    }

    public abstract void execute(CommandSender sender, String commandLabel, String argumentLabel, String[] args);
}