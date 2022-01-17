package dev.thatsmybaby.utils;

import cn.nukkit.blockentity.BlockEntitySign;
import cn.nukkit.level.Position;
import cn.nukkit.utils.TextFormat;
import dev.thatsmybaby.Placeholders;
import dev.thatsmybaby.object.GameArena;
import dev.thatsmybaby.provider.GameProvider;
import lombok.Getter;

final public class GameSign {

    private int tickWaiting = 0;

    @Getter private final String positionString;
    @Getter private final Position position;
    @Getter private GameArena gameArena = null;

    public GameSign(String string) {
        this.positionString = string;

        this.position = Placeholders.positionFromString(string);
    }

    private BlockEntitySign getBlockEntity() {
        return (BlockEntitySign) this.position.getLevel().getBlockEntity(this.position);
    }

    public void tick() {
        BlockEntitySign sign = getBlockEntity();

        System.out.println("Starting update...");

        if (sign == null) {
            System.out.println("Tile not found...");

            return;
        }

        if (this.canUpdate()) {
            sign.setText(TextFormat.colorize("&0&lSkyWars"), statusColor(), this.gameArena.getMapName(), this.gameArena.playersAsInt() + "/" + this.gameArena.maxSlotsAsInt());

            return;
        }

        if (this.tickWaiting > 5) {
            GameProvider.getInstance().requestGame(this.positionString);

            this.tickWaiting = 0;

            return;
        }

        System.out.println("Updating tile");

        sign.setText(TextFormat.DARK_PURPLE + "-------------", TextFormat.BLUE + "SEARCHING", TextFormat.BLUE + "FOR GAMES", TextFormat.DARK_PURPLE + "-------------");

        this.tickWaiting++;
    }

    private String statusColor() {
        if (this.gameArena.playersAsInt() > this.gameArena.maxSlotsAsInt()) {
            return TextFormat.DARK_PURPLE + "Full";
        }/* else if (10 < 10) {
            return TextFormat.GOLD + "Starting";
        }*/

        return TextFormat.GREEN + "Waiting";
    }

    private boolean canUpdate() {
        GameArena gameArena = GameProvider.getInstance().getGameArena(this.positionString);

        if (gameArena == null) {
            return false;
        }

        this.gameArena = gameArena;

        this.tickWaiting = 0;

        return true;
    }
}