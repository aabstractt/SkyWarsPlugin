package dev.thatsmybaby;

import cn.nukkit.plugin.PluginBase;
import cn.nukkit.plugin.PluginLogger;
import cn.nukkit.utils.Config;
import dev.thatsmybaby.listener.PlayerInteractListener;
import dev.thatsmybaby.listener.SignChangeListener;
import dev.thatsmybaby.provider.GameProvider;
import dev.thatsmybaby.utils.GameSign;
import lombok.Getter;

import java.io.File;

public class GameLobby extends PluginBase {

    @Getter private static GameLobby instance;

    @Override
    public void onEnable() {
        instance = this;
        VersionInfo versionInfo = GameProvider.getVersionInfo();

        this.saveDefaultConfig();
        this.saveResource("messages.yml", versionInfo.development());

        Placeholders.messages = (new Config(new File(this.getDataFolder(), "messages.yml"))).getAll();

        // TODO: initialize connection with the games management
        GameProvider.getInstance().init(getConfig().getString("redis.address"), getConfig().getString("redis.password"));

        SignFactory.getInstance().init();

        this.getServer().getPluginManager().registerEvents(new PlayerInteractListener(), this);
        this.getServer().getPluginManager().registerEvents(new SignChangeListener(), this);

        PluginLogger logger = getLogger();

        // TODO: Waterdog log
        logger.info("§bStarting SkyWars Lobby!");
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

    public void onDisable() {
        GameProvider.runTransaction(jedis -> {
            for (GameSign gameSign : SignFactory.getInstance().getGameSigns().values()) {
                if (gameSign.getServerName() == null) {
                    continue;
                }

                if (jedis.sismember(String.format(GameProvider.HASH_SERVER_GAMES_REQUEST, gameSign.getServerName()), gameSign.getPositionString())) {
                    jedis.srem(String.format(GameProvider.HASH_SERVER_GAMES_REQUEST, gameSign.getServerName()), gameSign.getPositionString());
                }
            }
        });
    }
}