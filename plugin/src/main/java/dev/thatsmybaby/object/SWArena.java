package dev.thatsmybaby.object;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.level.Level;
import dev.thatsmybaby.player.SWPlayer;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

public class SWArena {

    @Getter private final int id;
    @Getter private final SWMap map;
    @Getter private final String worldName;
    @Getter private final Map<String, SWPlayer> players = new HashMap<>();

    private final List<Integer> slots = new ArrayList<>();

    @Getter @Setter
    private GameStatus status = GameStatus.IDLE;

    public SWArena(Integer id, SWMap map) {
        this.id = id;

        this.map = map;

        this.worldName = "SW-" + map.getMapName() + "-" + id;
    }

    public Level getWorld() {
        return Server.getInstance().getLevelByName(this.worldName);
    }

    public Optional<Integer> selectFirstSlot() {
        Collections.shuffle(this.slots);

        return this.slots.stream().findFirst();
    }

    public boolean worldWasGenerated() {
        return this.getWorld() != null;
    }

    public boolean isAllowedJoin() {
        return this.status.ordinal() < GameStatus.IN_GAME.ordinal() && !this.isFull();
    }

    public boolean isFull() {
        return this.players.size() > this.map.getMaxSlots();
    }

    public void joinAsPlayer(Player player) {
        if (!this.worldWasGenerated()) {
            System.out.println("Generating world...");

            return;
        }

        if (!player.isConnected() || this.inArenaAsPlayer(player) || this.inArenaAsSpectator(player) || !this.isAllowedJoin()) {
            return;
        }

        Optional<Integer> optional = this.selectFirstSlot();

        if (!optional.isPresent()) {
            System.out.println("Slots not found");

            return;
        }

        this.slots.removeIf(slot -> Objects.equals(slot, optional.get()));

        SWPlayer targetPlayer = new SWPlayer(player.getName(), player.getLoginChainData().getXUID(), this, optional.get());

        targetPlayer.lobbyAttributes();

        this.players.put(player.getName(), targetPlayer);

        this.broadcastMessage("PLAYER_JOINED", player.getName(), String.valueOf(this.players.size()), String.valueOf(this.map.getMaxSlots()));

        // TODO: create player instance and insert into array
    }

    public void removePlayer(Player player) {

    }

    public SWPlayer getPlayer(Player player) {
        return this.players.get(player.getName().toLowerCase());
    }

    public boolean inArenaAsPlayer(Player player) {
        return this.getPlayer(player) != null;
    }

    public void joinAsSpectator(SWPlayer player) {

    }

    public void removeSpectator(Player player) {

    }

    public SWPlayer getSpectator(Player player) {
        return null;
    }

    public boolean inArenaAsSpectator(Player player) {
        return this.getSpectator(player) != null;
    }

    public boolean inArena(Player player) {
        return this.inArenaAsPlayer(player) || this.inArenaAsSpectator(player);
    }

    public void broadcastMessage(String message, String... args) {

    }

    public void forceClose() {

    }
}