package dev.thatsmybaby;

import cn.nukkit.Server;
import cn.nukkit.plugin.PluginBase;
import dev.thatsmybaby.command.SWCommand;
import dev.thatsmybaby.factory.ArenaFactory;
import dev.thatsmybaby.factory.MapFactory;
import dev.thatsmybaby.object.SWArena;
import dev.thatsmybaby.provider.GameProvider;
import lombok.Getter;

public class SkyWars extends PluginBase {

    @Getter
    private static SkyWars instance;

    @Override
    public void onEnable() {
        instance = this;

        this.saveDefaultConfig();

        MapFactory.getInstance().init();

        // TODO: initialize connection with the games management
        GameProvider.getInstance().init(getConfig().getString("redis.address"), getConfig().getString("redis.password"));

        this.getServer().getCommandMap().register("sw", new SWCommand("sw", "SkyWars commands"));

        Server.getInstance().getScheduler().scheduleRepeatingTask(this, this::tick, 10, true);
    }

    private void tick() {
        GameProvider.runTransaction(jedis -> {
            for (String rawId : jedis.smembers(GameProvider.HASH_GAMES_REQUEST)) {
                SWArena arena = ArenaFactory.getInstance().getRandomArena(true);

                if (arena == null) {
                    arena = ArenaFactory.getInstance().registerNewArena(rawId);

                    if (arena == null) {
                        System.out.println("Games available not found...");
                    }
                }

                if (arena != null) {
                    System.out.println("Mapa encontrado, enviandole mapa a " + rawId);
                    GameProvider.getInstance().updateGame(arena.getMap().getMapName(), rawId, getServerName(), arena.getId(), arena.getStatus(), arena.getPlayers().size(), arena.getMap().getMaxSlots(), !arena.isAllowedJoin());
                }

                jedis.srem(GameProvider.HASH_GAMES_REQUEST, rawId);
            }
        });
    }

    public static String getServerName() {
        return instance.getConfig().getString("server-name");
    }
}