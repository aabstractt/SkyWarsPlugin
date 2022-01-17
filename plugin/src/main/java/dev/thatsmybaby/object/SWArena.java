package dev.thatsmybaby.object;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.utils.TextFormat;
import dev.thatsmybaby.factory.ArenaFactory;
import dev.thatsmybaby.player.SWPlayer;
import dev.thatsmybaby.provider.GameProvider;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

public class SWArena {

    @Getter private final int id;
    @Getter private final SWMap map;
    @Getter private final String worldName;
    @Getter @Setter private String positionString;
    @Getter private final Map<String, SWPlayer> players = new HashMap<>();

    private final List<Integer> slots;

    @Getter @Setter
    private GameStatus status = GameStatus.IDLE;

    public SWArena(Integer id, SWMap map, String positionString) {
        this.id = id;

        this.map = map;

        this.worldName = "SW-" + map.getMapName() + "-" + id;

        this.positionString = positionString;

        this.slots = new ArrayList<>(map.getSpawns().keySet());
    }

    public Level getWorld() {
        return Server.getInstance().getLevelByName(this.worldName);
    }

    /**
     * Select a slot for a player
     * @return Optional<Integer>
     */
    public Optional<Integer> selectFirstSlot() {
        Collections.shuffle(this.slots);

        return this.slots.stream().findAny();
    }

    public boolean worldWasGenerated() {
        return this.getWorld() != null;
    }

    public boolean isAllowedJoin() {
        return this.status.ordinal() < GameStatus.IN_GAME.ordinal() && !this.isFull();
    }

    public boolean isStarted() {
        return this.status.ordinal() >= GameStatus.IN_GAME.ordinal();
    }

    public boolean isStarting() {
        return false;
    }

    public boolean isFull() {
        return this.players.size() > this.map.getMaxSlots();
    }

    public void joinAsPlayer(Player player) {
        GameProvider.getInstance().setPlayerMap(player.getName(), -1);

        if (!this.worldWasGenerated()) {
            player.kick(TextFormat.RED + "World is generating...");

            return;
        }

        if (!player.isConnected() || this.inArenaAsPlayer(player) || this.inArenaAsSpectator(player) || !this.isAllowedJoin()) {
            return;
        }

        Optional<Integer> optional = this.selectFirstSlot();

        if (!optional.isPresent()) {
            player.kick("Slot available not found...");

            return;
        }

        this.slots.removeIf(slot -> slot == optional.get().intValue());

        SWPlayer targetPlayer = new SWPlayer(player.getName(), player.getLoginChainData().getXUID(), this, optional.get());

        this.players.put(player.getName(), targetPlayer);

        targetPlayer.lobbyAttributes();
        targetPlayer.getScoreboardBuilder().update(this);

        this.broadcastMessage("PLAYER_JOINED", player.getName(), String.valueOf(this.players.size()), String.valueOf(this.map.getMaxSlots()));
    }

    public void removePlayer(Player player) {
        this.players.remove(player.getName());
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
        for (SWPlayer player : this.players.values()) {
            player.sendMessage(message, args);
        }
    }

    public void forceClose() {
        ArenaFactory.getInstance().unregisterArena(this.id);
    }
}