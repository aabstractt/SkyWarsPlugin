package dev.thatsmybaby.object.task;

import cn.nukkit.scheduler.Task;
import dev.thatsmybaby.factory.MapFactory;
import dev.thatsmybaby.object.GameStatus;
import dev.thatsmybaby.object.SWArena;
import dev.thatsmybaby.player.SWPlayer;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
final public class GameCountDownUpdateTask extends Task {

    private final SWArena arena;

    @Getter private int countdown;

    @Override
    public void onRun(int i) {
        if (this.arena.getStatus() == GameStatus.IN_GAME) {
            this.cancel();

            return;
        }

        this.arena.getPlayers().values().forEach(player -> player.getScoreboardBuilder().update(this.arena));

        if (this.arena.getPlayers().size() < this.arena.getMap().getMinSlots()) {
            if (this.countdown != MapFactory.getInstance().getInitialCountdown()) {
                this.countdown = MapFactory.getInstance().getInitialCountdown();

                this.arena.broadcastMessage("START_CANCELLED_NOT_ENOUGH_PLAYERS");

                this.arena.setStatus(GameStatus.IDLE);
                this.arena.pushUpdate();
            }

            return;
        }

        if ((this.countdown > 0 && this.countdown < 6) || Arrays.asList(60, 50, 40, 30, 20, 15, 10).contains(this.countdown)) {
            this.arena.broadcastMessage("GAME_STARTING", String.valueOf(this.countdown));
        }

        if (!this.arena.isStarting() && this.countdown < 11) {
            this.arena.setStatus(GameStatus.STARTING);
            this.arena.pushUpdate();
        }

        this.countdown--;

        if (this.countdown > 0) {
            return;
        }

        for (SWPlayer player : this.arena.getPlayers().values()) {
            player.matchAttributes();

            player.getScoreboardBuilder().update(this.arena);
        }

        // change arena status and push the update to lobby servers
        this.arena.setStatus(GameStatus.IN_GAME);
        this.arena.pushUpdate();
    }

    @Override
    public void cancel() {
        arena.cancelTask(this.getClass().getSimpleName(), false);
    }
}