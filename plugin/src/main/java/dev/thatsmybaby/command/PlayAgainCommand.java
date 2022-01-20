package dev.thatsmybaby.command;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.utils.TextFormat;
import dev.thatsmybaby.Placeholders;
import dev.thatsmybaby.SkyWars;
import dev.thatsmybaby.TaskUtils;
import dev.thatsmybaby.factory.ArenaFactory;
import dev.thatsmybaby.object.GameArena;
import dev.thatsmybaby.object.SWArena;
import dev.thatsmybaby.provider.GameProvider;

public class PlayAgainCommand extends Command {

    public PlayAgainCommand(String name, String description) {
        super(name, description, null, new String[]{"findgame"});
    }

    @Override
    public boolean execute(CommandSender sender, String s, String[] strings) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(TextFormat.RED + "Run this command in-game");

            return false;
        }

        SWArena arena = ArenaFactory.getInstance().getPlayerArena((Player) sender);

        if (arena == null) {
            ((Player) sender).kick(Placeholders.replacePlaceholders("GAME_NOT_FOUND"));

            return false;
        }

        if (!arena.isStarted() && !GameProvider.getVersionInfo().development()) {
            sender.sendMessage(Placeholders.replacePlaceholders("YOU_MUST_WAIT_TO_START"));

            return false;
        }

        ArenaFactory.getInstance().handlePlayerDeath((Player) sender, arena, EntityDamageEvent.DamageCause.MAGIC, true);
        arena.forceRemovePlayer((Player) sender);

        SWArena betterArena = ArenaFactory.getInstance().getRandomArena(false);

        if (betterArena == null || betterArena.getId() == arena.getId() || !ArenaFactory.getInstance().joinArena((Player) sender, betterArena)) {
            TaskUtils.runAsync(() -> findGame((Player) sender));

            return false;
        }

        sender.sendMessage(Placeholders.replacePlaceholders("GAME_FOUND_HERE", betterArena.getMap().getMapName()));

        return false;
    }

    /**
     * Search a game in redis but in other server because here not found an available game
     *
     * @param player    Player
     */
    private void findGame(Player player) {
        GameArena betterArena = null;

        for (GameArena arena : GameProvider.getInstance().getArenasAvailable()) {
            if (arena.getServerName().equalsIgnoreCase(SkyWars.getServerName()) || arena.playersAsInt() >= arena.maxSlotsAsInt()) {
                continue;
            }

            if (betterArena == null) {
                betterArena = arena;

                continue;
            }

            if (arena.playersAsInt() < betterArena.maxSlotsAsInt()) {
                continue;
            }

            betterArena = arena;
        }

        if (betterArena == null) {
            // I use runSync because if execute kickPlayer in async it gives an exception caused by spigot
            TaskUtils.runSync(() -> player.kick(Placeholders.replacePlaceholders("GAME_NOT_FOUND")));

            return;
        }

        player.sendMessage(Placeholders.replacePlaceholders("GAME_FOUND_SENDING", betterArena.getServerName(), betterArena.getMapName()));

        GameArena finalBetterArena = betterArena;
        TaskUtils.runAsync(() -> {
            GameProvider.getInstance().setPlayerMap(player.getName(), finalBetterArena.idAsInt());

            GameProvider.getInstance().connectTo(player, finalBetterArena);
        });
    }
}