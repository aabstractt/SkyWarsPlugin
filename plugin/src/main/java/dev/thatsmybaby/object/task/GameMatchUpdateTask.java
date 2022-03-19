package dev.thatsmybaby.object.task;

import cn.nukkit.scheduler.Task;
import dev.thatsmybaby.Placeholders;
import dev.thatsmybaby.TaskUtils;
import dev.thatsmybaby.object.SWArena;
import dev.thatsmybaby.player.SWPlayer;

public class GameMatchUpdateTask extends Task {

    private final SWArena arena;

    private boolean finished = false;
    private boolean updated = false;

    private int ticks = 0;

    public GameMatchUpdateTask(SWArena arena) {
        this.arena = arena;
    }

    @Override
    public void onRun(int i) {
        if (!this.arena.isStarted()) {
            this.cancel();

            return;
        }

        this.arena.forceGetEveryone().forEach(player -> player.getScoreboardBuilder().update(this.arena));

        if (this.finished) {
            if (this.ticks++ > 8) {
                this.arena.forceClose();

                this.cancel();
            }

            if (this.ticks == 4) {
                this.arena.forceGetEveryone().forEach(player -> {
                    player.getInstance().dataPacket(player.getScoreboardBuilder().remove());

                    player.getInstance().kick("Server closed");
                });
            }

            return;
        }

        if (!this.updated) {
            if (this.ticks++ > 5) {
                TaskUtils.runAsync(arena::pushUpdateRemove);

                this.ticks = 0;
                this.updated = true;
            }
        }

        if (this.arena.getPlayers().size() <= 1) {
            SWPlayer target = this.arena.getPlayers().values().stream().findFirst().orElse(null);

            String message;
            if (target != null) {
                message = Placeholders.replacePlaceholders("GAME_WINNER", target.getName(), this.arena.getMap().getMapName());
            } else {
                message = Placeholders.replacePlaceholders("GAME_WINNER_NOT_FOUND", this.arena.getMap().getMapName());
            }

            this.arena.broadcastMessage(message);

            this.finished = true;

            return;
        }
    }
}