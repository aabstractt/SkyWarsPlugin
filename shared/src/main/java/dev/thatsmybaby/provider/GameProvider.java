package dev.thatsmybaby.provider;

import cn.nukkit.Player;
import cn.nukkit.network.protocol.TransferPacket;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.thatsmybaby.VersionInfo;
import dev.thatsmybaby.object.GameArena;
import dev.thatsmybaby.object.GameStatus;
import lombok.Getter;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class GameProvider {

    // Lobby server add a sign id to request new game
    public static String HASH_GAMES_REQUEST = "sw_games_request";
    // Game server insert hash of a game running
    public static String HASH_GAMES_RUNNING = "sw_games_running";
    // Game server send the data to this hash
    public static String HASH_GAMES_UPDATING = "sw_games_updating";
    // Player send data to this hash (map selected, lastServer etc)
    public static String HASH_PLAYERS_JOINING = "sW_players_joining";
    // Put here when a player is joining to a game server
    public final static String PLAYER_MAP_HASH = "sw_player_map:%s";

    @Getter private final static GameProvider instance = new GameProvider();
    @Getter private final static VersionInfo versionInfo = loadVersion();

    private final ObjectMapper mapper = new ObjectMapper();

    protected JedisPool jedisPool;
    private String password;

    @SuppressWarnings("deprecation")
    public void init(String address, String password) {
        String[] addressSplit = address.split(":");
        String host = addressSplit[0];
        int port = addressSplit.length > 1 ? Integer.parseInt(addressSplit[1]) : Protocol.DEFAULT_PORT;

        this.jedisPool = new JedisPool(new JedisPoolConfig() {{
            setMaxWaitMillis(1000 * 200000);
        }}, host, port, 1000 * 10);

        Jedis jedis = this.jedisPool.getResource();

        if (password != null && !password.isEmpty()) {
            jedis.auth(password);
        }

        this.password = password;
    }

    public void updateGame(String mapName, String positionString, String serverName, int gameId, GameStatus gameStatus, int playersCount, int maxSlots, boolean started) {
        runTransaction(jedis -> {
            if (started) {
                return;
            }

            if (!jedis.sismember(HASH_GAMES_RUNNING, positionString)) {
                jedis.sadd(HASH_GAMES_RUNNING, positionString);
            }

            jedis.hset(String.format(HASH_GAMES_UPDATING, positionString), new HashMap<String, String>() {{
                put("id", String.valueOf(gameId));
                put("mapName", mapName);
                put("serverName", serverName);
                put("status", gameStatus.name());
                put("playersCount", String.valueOf(playersCount));
                put("maxSlots", String.valueOf(maxSlots));
            }});
        });
    }

    public void removeGame(String hash) {
        runTransaction(jedis -> {
            if (jedis.sismember(HASH_GAMES_REQUEST, hash)) {
                jedis.srem(HASH_GAMES_REQUEST, hash);
            }

            if (jedis.sismember(HASH_GAMES_RUNNING, hash)) {
                jedis.srem(HASH_GAMES_RUNNING, hash);
            }

            if (jedis.exists(String.format(HASH_GAMES_UPDATING, hash))) {
                jedis.hdel(String.format(HASH_GAMES_UPDATING, hash), jedis.hgetAll(String.format(HASH_GAMES_UPDATING, hash)).keySet().toArray(new String[0]));
            }
        });
    }

    public void requestGame(String string) {
        runTransaction(jedis -> {
            if (jedis.sismember(HASH_GAMES_REQUEST, string)) {
                return;
            }

            jedis.sadd(HASH_GAMES_REQUEST, string);
        });
    }

    public GameArena getGameArena(String string) {
        return runTransaction(jedis -> {
            Map<String, String> map = jedis.hgetAll(String.format(HASH_GAMES_UPDATING, string));

            if (map.isEmpty()) {
                return null;
            }

            return mapper.convertValue(map, GameArena.class);
        });
    }

    public int getPlayerMapId(String name) {
        return runTransaction(jedis -> {
            if (!jedis.exists(String.format(PLAYER_MAP_HASH, name))) {
                return 0;
            }

            return Integer.parseInt(jedis.get(String.format(PLAYER_MAP_HASH, name)));
        });
    }

    public void setPlayerMap(String name, String mapName) {
        runTransaction(jedis -> {
            if (mapName != null) {
                jedis.set(String.format(PLAYER_MAP_HASH, name), mapName);
            } else {
                jedis.del(String.format(PLAYER_MAP_HASH, name));
            }
        });
    }

    private  <T> T runTransaction(Function<Jedis, T> action) {
        try (Jedis jedis = jedisPool.getResource()) {
            if (this.password != null && !this.password.isEmpty()) {
                jedis.auth(this.password);
            }

            return action.apply(jedis);
        }
    }

    public static void runTransaction(Consumer<Jedis> action) {
        try (Jedis jedis = instance.jedisPool.getResource()) {
            if (instance.password != null && !instance.password.isEmpty()) {
                jedis.auth(instance.password);
            }

            action.accept(jedis);
        }
    }

    public void connectTo(Player player, GameArena gameArena) {
        setPlayerMap(player.getName(), gameArena.getId());

        player.dataPacket(new TransferPacket() {{
            address = gameArena.getServerName();
        }});
    }

    private static VersionInfo loadVersion() {
        InputStream inputStream = GameProvider.class.getClassLoader().getResourceAsStream("github.properties");

        if (inputStream == null) {
            return VersionInfo.unknown();
        }

        Properties properties = new Properties();

        try {
            properties.load(inputStream);
        } catch (IOException e) {
            return VersionInfo.unknown();
        }

        String branchName = properties.getProperty("git.branch", "unknown");
        String commitId = properties.getProperty("git.commit.id.abbrev", "unknown");

        return new VersionInfo(branchName, commitId, branchName.equals("release"), properties.getProperty("git.commit.user.name"));
    }
}