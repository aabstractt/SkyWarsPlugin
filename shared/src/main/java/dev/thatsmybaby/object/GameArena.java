package dev.thatsmybaby.object;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
@Setter
public class GameArena {

    private final String serverName;
    private final String mapName;
    private final String gameId;
    private final String playersCount;
    private final String maxSlots;
    private String gameStatus;

    public int idAsInt() {
        return Integer.parseInt(this.gameId);
    }

    public int playersAsInt() {
        return Integer.parseInt(this.playersCount);
    }

    public int maxSlotsAsInt() {
        return Integer.parseInt(this.maxSlots);
    }

    public String asHash() {
        return this.serverName + "%" + this.gameId;
    }
}