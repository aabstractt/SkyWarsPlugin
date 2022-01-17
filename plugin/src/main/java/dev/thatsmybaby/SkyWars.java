package dev.thatsmybaby;

import cn.nukkit.Server;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.TextFormat;
import dev.thatsmybaby.command.PlayAgainCommand;
import dev.thatsmybaby.command.SWCommand;
import dev.thatsmybaby.factory.ArenaFactory;
import dev.thatsmybaby.factory.MapFactory;
import dev.thatsmybaby.listener.PlayerJoinListener;
import dev.thatsmybaby.object.SWArena;
import dev.thatsmybaby.provider.GameProvider;
import lombok.Getter;

public class SkyWars extends PluginBase {

    @Getter private static SkyWars instance;
    @Getter private static String serverName;

    public static int NORMAL_PRIORITY = 0, HIGH_PRIORITY = 1;

    @Override
    public void onEnable() {
        instance = this;

        this.saveDefaultConfig();

        serverName = getConfig().getString("redis.server-name");

        MapFactory.getInstance().init();

        // TODO: initialize connection with the games management
        GameProvider.getInstance().init(getConfig().getString("redis.address"), getConfig().getString("redis.password"));
        GameProvider.getInstance().addServer(getServerName());

        this.getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);

        this.getServer().getCommandMap().register("sw", new SWCommand("sw", "SkyWars commands"));
        this.getServer().getCommandMap().register("playagain", new PlayAgainCommand("playagain", "Find a new game available"));

        Server.getInstance().getScheduler().scheduleRepeatingTask(this, this::tick, 10, true);
    }

    @Override
    public void onDisable() {
        for (SWArena arena : ArenaFactory.getInstance().getArenas().values()) {
            if (arena.getPositionString() == null) {
                continue;
            }

            GameProvider.getInstance().removeGame(arena.getPositionString(), getServerName());
        }

        GameProvider.getInstance().removeServer(getServerName());
    }

    /*
     * Always listen every time there are requests for new arenas, in order to search for an available arena and send it
     */
    private void tick() {
        GameProvider.runTransaction(jedis -> {
            for (String rawId : jedis.smembers(String.format(GameProvider.HASH_SERVER_GAMES_REQUEST, serverName))) {
                SWArena arena = ArenaFactory.getInstance().getSignArena(rawId);

                if (arena == null) {
                    jedis.srem(String.format(GameProvider.HASH_SERVER_GAMES_REQUEST, serverName), rawId);
                }

                if (arena == null && (arena = ArenaFactory.getInstance().getRandomArena(true)) == null && (arena = ArenaFactory.getInstance().registerNewArena(rawId)) == null) {
                    getLogger().error("No game found! Looking again at another server");

                    continue;
                }

                if (!arena.worldWasGenerated()) {
                    return;
                }

                GameProvider.getInstance().updateGame(arena.getMap().getMapName(), rawId, getServerName(), arena.getId(), arena.getStatus(), arena.getPlayers().size(), arena.getMap().getMaxSlots(), !arena.isAllowedJoin());

                getLogger().warning("Game found! Sending to " + rawId);
            }
        });
    }

    public static String invalidUsageGame() {
        return TextFormat.RED + "Run this command in-game";
    }
}