package dev.thatsmybaby.object;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.utils.TextFormat;
import dev.thatsmybaby.Placeholders;
import dev.thatsmybaby.SkyWars;
import dev.thatsmybaby.TaskHandlerStorage;
import dev.thatsmybaby.factory.ArenaFactory;
import dev.thatsmybaby.player.SWPlayer;
import dev.thatsmybaby.provider.GameProvider;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

public class SWArena extends TaskHandlerStorage {

    @Getter private final int id;
    @Getter private final SWMap map;
    @Getter private final String worldName;
    @Getter private String rawId;
    @Getter private final Map<String, SWPlayer> players = new HashMap<>();
    @Getter private final Map<String, SWPlayer> spectators = new HashMap<>();

    private final List<Integer> slots;

    @Getter @Setter
    private GameStatus status = GameStatus.IDLE;

    public SWArena(Integer id, SWMap map, String rawId) {
        this.id = id;

        this.map = map;

        this.worldName = "SW-" + map.getMapName() + "-" + id;

        this.rawId = rawId;

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
        return Server.getInstance().isLevelGenerated(this.worldName);
    }

    public boolean isAllowedJoin() {
        return this.status.ordinal() < GameStatus.IN_GAME.ordinal() && !this.isFull();
    }

    public boolean isStarting() {
        return this.status == GameStatus.STARTING;
    }

    public boolean isStarted() {
        return this.status.ordinal() >= GameStatus.IN_GAME.ordinal();
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

        if (!Server.getInstance().isLevelLoaded(this.worldName)) {
            Server.getInstance().loadLevel(this.worldName);
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

        this.players.put(player.getName().toLowerCase(), targetPlayer);

        targetPlayer.lobbyAttributes();

        this.forceGetEveryone().forEach(target -> target.getScoreboardBuilder().update(this));

        this.pushUpdate();

        this.broadcastMessage("PLAYER_JOINED", player.getName(), String.valueOf(this.players.size()), String.valueOf(this.map.getMaxSlots()));
    }

    public void removePlayer(Player player) {
        SWPlayer target = this.players.remove(player.getName().toLowerCase());

        if (target == null || target.getSlot() == -1) {
            return;
        }

        if (this.isStarted()) {
            return;
        }

        this.slots.add(target.getSlot());

        this.pushUpdate();

        this.forceGetEveryone().forEach(p -> p.getScoreboardBuilder().update(this));
    }

    public SWPlayer getPlayer(Player player) {
        return this.players.get(player.getName().toLowerCase());
    }

    public boolean inArenaAsPlayer(Player player) {
        return this.getPlayer(player) != null;
    }

    public void joinAsSpectator(SWPlayer player) {
        this.spectators.put(player.getName().toLowerCase(), player);
    }

    public void removeSpectator(Player player) {
        this.spectators.remove(player.getName().toLowerCase());
    }

    public SWPlayer getSpectator(Player player) {
        return this.spectators.get(player.getName().toLowerCase());
    }

    public boolean inArenaAsSpectator(Player player) {
        return this.getSpectator(player) != null;
    }

    public void forceRemovePlayer(Player player) {
        if (this.inArenaAsPlayer(player)) {
            this.removePlayer(player);
        } else {
            this.removeSpectator(player);
        }
    }

    public SWPlayer forceGetPlayer(Player player) {
        return this.inArenaAsPlayer(player) ? this.getPlayer(player) : this.getSpectator(player);
    }

    public List<SWPlayer> forceGetEveryone() {
        return new ArrayList<SWPlayer>() {{
            addAll(getPlayers().values());
            addAll(getSpectators().values());
        }};
    }

    public boolean inArena(Player player) {
        return this.inArenaAsPlayer(player) || this.inArenaAsSpectator(player);
    }

    public void broadcastMessage(String message, String... args) {
        for (Player player : this.getWorld().getPlayers().values()) {
            player.sendMessage(Placeholders.replacePlaceholders(message, args));
        }
    }

    public void pushUpdate() {
        if (this.rawId == null) {
            return;
        }

        GameProvider.getInstance().updateGame(this.map.getMapName(), this.rawId, SkyWars.getServerName(), this.id, this.status, this.players.size(), this.map.getMaxSlots());
    }

    public void pushUpdateRemove() {
        if (this.rawId == null) {
            return;
        }

        GameProvider.getInstance().removeGame(this.rawId, SkyWars.getServerName());

        this.rawId = null;
    }

    public void forceClose() {
        this.slots.clear();
        this.players.clear();

        this.pushUpdateRemove();

        ArenaFactory.getInstance().unregisterArena(this.id);
    }
}