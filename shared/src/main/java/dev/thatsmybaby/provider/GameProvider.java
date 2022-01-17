package dev.thatsmybaby.provider;

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

    public static String HASH_GAMES_RUNNING = "sw_games_running";
    // Game server send the data to this hash
    public static String HASH_GAMES_UPDATING = "sw_games_updating";
    // Player send data to this hash (map selected, lastServer etc)
    public static String HASH_PLAYERS_JOINING = "sW_players_joining";

    public final static String PLAYER_MAP_HASH = "sw_player_map:%s";

    @Getter private final static GameProvider instance = new GameProvider();
    @Getter private final static VersionInfo versionInfo = loadVersion();

    private final ObjectMapper mapper = new ObjectMapper();

    protected JedisPool jedisPool;
    private String password;

    public void init(String address, String password) {
        String[] addressSplit = address.split(":");
        String host = addressSplit[0];
        int port = addressSplit.length > 1 ? Integer.parseInt(addressSplit[1]) : Protocol.DEFAULT_PORT;

        this.jedisPool = new JedisPool(new JedisPoolConfig() {{
            setMaxWaitMillis(1000 * 200000);
        }}, host, port, 1000 * 10);

        Jedis jedis = this.jedisPool.getResource();

        /*if (password != null && !password.isEmpty()) {
            jedis.auth(password);
        }*/

        this.password = password;
    }

    public void updateGame(String mapName, String positionString, String serverName, int id, GameStatus gameStatus, int playersCount, int maxSlots, boolean started) {
        runTransaction(jedis -> {
            if (started) {
                return;
            }

            if (!jedis.sismember(HASH_GAMES_RUNNING, positionString)) {
                jedis.sadd(HASH_GAMES_RUNNING, positionString);
            }

            jedis.hset(String.format(HASH_GAMES_UPDATING, positionString), new HashMap<String, String>() {{
                put("id", String.valueOf(id));
                put("mapName", mapName);
                put("serverName", serverName);
                put("status", gameStatus.name());
                put("playersCount", String.valueOf(playersCount));
                put("maxSlots", String.valueOf(maxSlots));
            }});
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

    public List<GameArena> getGamesAvailable() {
        return runTransaction(jedis -> {
            List<GameArena> games = new ArrayList<>();

            for (String hash : jedis.smembers(HASH_GAMES_RUNNING)) {
                if (!jedis.exists(String.format(HASH_GAMES_UPDATING, hash))) {
                    continue;
                }

                Map<String, String> storage = jedis.hgetAll(String.format(HASH_GAMES_UPDATING, hash));

                games.add(mapper.convertValue(storage, GameArena.class));

                /*arenas.add(new RedisGameArena(
                        storage.get("serverName"),
                        storage.get("folderName"),
                        storage.get("mapName"),
                        Integer.parseInt(storage.get("status")),
                        Integer.parseInt(storage.get("playersCount")),
                        Integer.parseInt(storage.get("maxSlots"))
                ));*/
            }

            return games;
        });
    }

    public String getPlayerMap(String name) {
        return runTransaction(jedis -> {
            return jedis.get(String.format(PLAYER_MAP_HASH, name));
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