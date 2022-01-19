package dev.thatsmybaby.player;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.level.Location;
import dev.thatsmybaby.Placeholders;
import dev.thatsmybaby.SkyWars;
import dev.thatsmybaby.object.SWArena;
import dev.thatsmybaby.object.scoreboard.ScoreboardBuilder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

public class SWPlayer {

    @Getter private final String name;
    @Getter private final String xuid;
    @Getter private final SWArena arena;
    @Getter private final ScoreboardBuilder scoreboardBuilder;
    @Getter private final int slot;

    @Getter private int kills = 0;
    private String lastAttack = null;
    private long lastAttackUpdate = -1;
    private String lastAssistance = null;
    private long lastAssistanceUpdate = -1;

    @SuppressWarnings("unchecked")
    public SWPlayer(String name, String xuid, SWArena arena, int slot) {
        this.name = name;

        this.xuid = xuid;

        this.arena = arena;

        this.slot = slot;

        this.scoreboardBuilder = new ScoreboardBuilder(
                this,
                SkyWars.getInstance().getConfig().getString("scoreboard.title"),
                (Map<String, List<String>>) SkyWars.getInstance().getConfig().get("scoreboard.lines"),
                this.arena.getWorldName(),
                ScoreboardBuilder.SIDEBAR,
                ScoreboardBuilder.ASCENDING
        );
    }

    @SuppressWarnings("deprecation")
    public void lobbyAttributes() {
        Player instance = getInstance();

        if (instance == null) {
            return;
        }

        Location location = this.arena.getMap().getSpawnLocation(this.slot, this.arena.getWorld());

        if (location == null) {
            return;
        }

        instance.dataPacket(this.scoreboardBuilder.initialize());

        instance.teleport(location);

        instance.getInventory().clearAll();
        instance.getCursorInventory().clearAll();

        instance.removeAllEffects();
        instance.clearTitle();

        instance.setAllowFlight(false);
        instance.setGamemode(Player.ADVENTURE);

        instance.getFoodData().setLevel(instance.getFoodData().getMaxLevel());
        instance.setHealth(instance.getMaxHealth());
    }

    @SuppressWarnings("deprecation")
    public void matchAttributes() {
        Player instance = getInstance();

        if (instance == null) {
            return;
        }

        instance.getInventory().clearAll();
        instance.getCursorInventory().clearAll();

        instance.removeAllEffects();
        instance.clearTitle();

        instance.setAllowFlight(false);
        instance.setGamemode(Player.SURVIVAL);

        instance.getFoodData().setLevel(instance.getFoodData().getMaxLevel());
        instance.setHealth(instance.getMaxHealth());
    }

    public Player getInstance() {
        return Server.getInstance().getPlayerExact(this.name);
    }

    public SWPlayer getLastAttack() {
        if (this.lastAttack == null || this.lastAttackUpdate == -1) {
            return null;
        }

        if (this.arena == null) {
            return null;
        }

        if (System.currentTimeMillis() - this.lastAttackUpdate > (10 * 1000)) {
            return null;
        }

        Player target = Server.getInstance().getPlayerExact(this.lastAttack);

        if (target == null || !target.isConnected()) {
            return null;
        }

        return this.arena.findInstance(target);
    }

    public SWPlayer getLastAssistance() {
        if (this.lastAssistance == null || this.lastAssistanceUpdate == -1) {
            return null;
        }

        if (this.arena == null) {
            return null;
        }

        if (System.currentTimeMillis() - this.lastAssistanceUpdate > (10 * 1000)) {
            return null;
        }

        Player target = Server.getInstance().getPlayerExact(this.lastAssistance);

        if (target == null || !target.isConnected()) {
            return null;
        }

        return this.arena.findInstance(target);
    }

    public void attack(Player player) {
        this.lastAttackUpdate = System.currentTimeMillis();

        if (this.lastAttack == null) {
            this.lastAttack = player.getName();

            return;
        }

        if (this.lastAttack.equalsIgnoreCase(player.getName())) {
            return;
        }

        this.lastAssistance = this.lastAttack;
        this.lastAssistanceUpdate = System.currentTimeMillis();

        this.lastAttack = player.getName();
    }

    public void sendMessage(String message, String... args) {
        getInstance().sendMessage(Placeholders.replacePlaceholders(message, args));
    }

    public void increaseKills() {
        this.kills++;
    }
}