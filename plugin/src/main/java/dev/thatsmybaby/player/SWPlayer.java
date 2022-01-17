package dev.thatsmybaby.player;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.level.Location;
import dev.thatsmybaby.Placeholders;
import dev.thatsmybaby.object.SWArena;
import lombok.Getter;

public class SWPlayer {

    @Getter private final String name;
    @Getter private final String xuid;
    @Getter private final SWArena arena;
    @Getter private final int slot;

    @Getter private int kills = 0;

    public SWPlayer(String name, String xuid, SWArena arena, int slot) {
        this.name = name;

        this.xuid = xuid;

        this.arena = arena;

        this.slot = slot;
    }

    public Player getInstance() {
        return Server.getInstance().getPlayerExact(this.name);
    }

    public void lobbyAttributes() {
        Player instance = getInstance();

        if (instance == null) {
            return;
        }

        Location location = this.arena.getMap().getSpawnLocation(this.slot, this.arena.getWorld());

        if (location == null) {
            return;
        }

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

    public void sendMessage(String message, String... args) {
        getInstance().sendMessage(Placeholders.replacePlaceholders(message, args));
    }

    public void increaseKills() {
        this.kills++;
    }
}