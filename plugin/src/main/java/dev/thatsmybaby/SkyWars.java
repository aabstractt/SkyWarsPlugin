package dev.thatsmybaby;

import cn.nukkit.Server;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.plugin.PluginLogger;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import dev.thatsmybaby.command.PlayAgainCommand;
import dev.thatsmybaby.command.SWCommand;
import dev.thatsmybaby.factory.ArenaFactory;
import dev.thatsmybaby.factory.MapFactory;
import dev.thatsmybaby.listener.*;
import dev.thatsmybaby.object.SWArena;
import dev.thatsmybaby.provider.GameProvider;
import lombok.Getter;

import java.io.File;
import java.util.*;

public class SkyWars extends PluginBase {

    @Getter private static SkyWars instance;
    @Getter private static String serverName;

    private static Map<String, List<String>> killMessages = new HashMap<>();

    public static int NORMAL_PRIORITY = 0, HIGH_PRIORITY = 1;

    @Override
    @SuppressWarnings("unchecked")
    public void onEnable() {
        instance = this;
        VersionInfo versionInfo = GameProvider.getVersionInfo();

        this.saveDefaultConfig();
        this.saveResource("messages.yml", versionInfo.development());
        this.saveResource("features.yml", versionInfo.development());

        Placeholders.messages = (new Config(new File(this.getDataFolder(), "messages.yml"))).getAll();
        killMessages = (Map<String, List<String>>) new Config(new File(this.getDataFolder(), "features.yml")).get("kill-messages");

        serverName = getConfig().getString("redis.server-name");

        MapFactory.getInstance().init();

        // TODO: initialize connection with the games management
        GameProvider.getInstance().init(getConfig().getString("redis.address"), getConfig().getString("redis.password"));
        GameProvider.getInstance().addServer(getServerName());

        this.getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        this.getServer().getPluginManager().registerEvents(new BlockBreakListener(), this);
        this.getServer().getPluginManager().registerEvents(new BlockPlaceListener(), this);
        this.getServer().getPluginManager().registerEvents(new EntityDamageListener(), this);
        this.getServer().getPluginManager().registerEvents(new PlayerQuitListener(), this);

        this.getServer().getCommandMap().register("sw", new SWCommand("sw", "SkyWars commands"));
        this.getServer().getCommandMap().register("playagain", new PlayAgainCommand("playagain", "Find a new game available"));

        Server.getInstance().getScheduler().scheduleRepeatingTask(this, this::tick, 10, true);

        PluginLogger logger = getLogger();

        // TODO: Waterdog log
        logger.info("§bStarting SkyWars Server!");
        logger.info("§9Commit Id: " + versionInfo.commitId());
        logger.info("§9Branch: " + versionInfo.branchName());
        logger.info("§9Build Version: " + versionInfo.buildVersion());
        logger.info("§9Build Author: " + versionInfo.author());
        logger.info("§9Development Build: " + versionInfo.development());

        if (versionInfo.development() || versionInfo.buildVersion().equals("#build") || versionInfo.branchName().equals("unknown")) {
            logger.error("Custom build? Unofficial builds should be not run in production!");
        } else {
            logger.info("§bDiscovered branch §9" + versionInfo.branchName() + "§b commitId §9" + versionInfo.commitId());
        }
    }

    @Override
    public void onDisable() {
        for (SWArena arena : ArenaFactory.getInstance().getArenas().values()) {
            arena.pushUpdateRemove();
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
                    getLogger().warning("Game is generating world...");

                    continue;
                }

                arena.pushUpdate();

                getLogger().warning("Game found! Sending to " + rawId);
            }
        });
    }

    public static String getRandomKillMessage(String type) {
        return killMessages.get(type).stream().sorted().findAny().orElse(null);
    }

    public static String invalidUsageGame() {
        return TextFormat.RED + "Run this command in-game";
    }
}