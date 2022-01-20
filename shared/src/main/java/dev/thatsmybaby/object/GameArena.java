package dev.thatsmybaby.object;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
final public class GameArena {

    private String serverName;
    private String mapName;
    private String id;
    private String playersCount;
    private String maxSlots;
    private String status;

    public int idAsInt() {
        return Integer.parseInt(this.id);
    }

    public int playersAsInt() {
        return Integer.parseInt(this.playersCount);
    }

    public int maxSlotsAsInt() {
        return Integer.parseInt(this.maxSlots);
    }
}