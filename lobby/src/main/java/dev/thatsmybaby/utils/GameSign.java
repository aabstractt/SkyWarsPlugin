package dev.thatsmybaby.utils;

import cn.nukkit.Player;
import cn.nukkit.blockentity.BlockEntitySign;
import cn.nukkit.level.Position;
import cn.nukkit.utils.TextFormat;
import dev.thatsmybaby.GameLobby;
import dev.thatsmybaby.Placeholders;
import dev.thatsmybaby.TaskUtil;
import dev.thatsmybaby.object.GameArena;
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

        player.sendMessage(TextFormat.GREEN + "Sending you to " + gameArena.getServerName() + "*" + gameArena.getMapName());

        TaskUtil.runAsync(() -> GameProvider.getInstance().connectTo(player, gameArena));
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

                return;
            }

            this.tickWaiting = 0;

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
        }/* else if (10 < 10) {
            return TextFormat.GOLD + "Starting";
        }*/

        return TextFormat.GREEN + "Waiting";
    }
}