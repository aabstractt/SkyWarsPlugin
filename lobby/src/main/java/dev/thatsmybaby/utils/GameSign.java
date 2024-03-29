package dev.thatsmybaby.utils;

import cn.nukkit.Player;
import cn.nukkit.blockentity.BlockEntitySign;
import cn.nukkit.level.Position;
import cn.nukkit.utils.TextFormat;
import dev.thatsmybaby.GameLobby;
import dev.thatsmybaby.Placeholders;
import dev.thatsmybaby.TaskUtils;
import dev.thatsmybaby.object.GameArena;
import dev.thatsmybaby.object.GameStatus;
import dev.thatsmybaby.provider.GameProvider;
import lombok.Getter;

final public class GameSign {

    @Getter private final String positionString;
    @Getter private final Position position;
    @Getter private String serverName = null;

    private int tickWaiting = 0;

    public GameSign(String string) {
        this.positionString = string;

        this.position = Placeholders.positionFromString(string);
    }

    public void handleInteract(Player player) {
        GameArena gameArena = GameProvider.getInstance().getGameArena(this.positionString);

        if (gameArena == null) {
            player.sendMessage(TextFormat.RED + "Searching game...");

            return;
        }

        if (gameArena.playersAsInt() > gameArena.maxSlotsAsInt()) {
            player.sendMessage(TextFormat.RED + gameArena.getMapName() + " is full!");

            return;
        }

        if (gameArena.getStatus().equalsIgnoreCase(GameStatus.IN_GAME.name())) {
            player.sendMessage(TextFormat.RED + "Game already started! Searching other game...");

            return;
        }

        player.sendMessage(Placeholders.replacePlaceholders("GAME_FOUND_SENDING", gameArena.getServerName(), gameArena.getMapName()));

        TaskUtils.runAsync(() -> GameProvider.getInstance().connectTo(player, gameArena));
    }

    public void tick() {
        BlockEntitySign sign = (BlockEntitySign) this.position.getLevel().getBlockEntity(this.position);

        if (sign == null) {
            return;
        }

        GameArena gameArena = GameProvider.getInstance().getGameArena(this.positionString);

        if (gameArena != null) {
            this.tickWaiting = 0;

            sign.setLevel(this.position.getLevel());
            sign.setText(TextFormat.colorize("&0&lSkyWars"), statusColor(gameArena), gameArena.getMapName(), gameArena.playersAsInt() + "/" + gameArena.maxSlotsAsInt());

            return;
        }

        if (this.tickWaiting > 5) {
            if ((this.serverName = GameProvider.getInstance().requestGame(this.positionString, this.serverName)) == null) {
                GameLobby.getInstance().getLogger().warning("Server available not found...");
            }

            this.tickWaiting = -10;

            return;
        }

        if (this.serverName != null) {
            this.serverName = null;
        }

        sign.setText(TextFormat.DARK_PURPLE + "-------------", TextFormat.BLUE + "SEARCHING", TextFormat.BLUE + "FOR GAMES", TextFormat.DARK_PURPLE + "-------------");

        this.tickWaiting++;
    }

    private String statusColor(GameArena gameArena) {
        if (gameArena.playersAsInt() > gameArena.maxSlotsAsInt()) {
            return TextFormat.DARK_PURPLE + "Full";
        } else if (gameArena.getStatus().equals(GameStatus.STARTING.name())) {
            return TextFormat.GOLD + "Starting";
        } else if (gameArena.getStatus().equals(GameStatus.IN_GAME.name())) {
            return TextFormat.DARK_RED + "In-Game";
        }

        return TextFormat.GREEN + "Waiting";
    }
}