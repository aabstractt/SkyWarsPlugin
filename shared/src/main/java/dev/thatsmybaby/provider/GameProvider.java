package dev.thatsmybaby.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.thatsmybaby.object.GameArena;
import dev.thatsmybaby.object.GameStatus;
import lombok.Getter;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class GameProvider {

    public final static String GAMES_HASH = "sw_games";
    public final static String GAMES_STATUS_HASH = "sw_games_status:%s";
    public final static String PLAYER_MAP_HASH = "sw_player_map:%s";

    @Getter private final static GameProvider instance = new GameProvider();

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

        if (password != null && !password.isEmpty()) {
            jedis.auth(password);
        }

        this.password = password;
    }

    public void updateGame(String serverName, String mapName, int id, GameStatus gameStatus, int playersCount, int maxSlots, boolean started) {
        runTransaction(jedis -> {
            if (started) {
                return;
            }

            String hash = serverName + "%" + mapName;

            if (!jedis.sismember(GAMES_HASH, hash)) {
                jedis.sadd(GAMES_HASH, hash);
            }

            jedis.hset(String.format(GAMES_STATUS_HASH, hash), new HashMap<String, String>() {{
                put("id", String.valueOf(id));
                put("mapName", mapName);
                put("serverName", serverName);
                put("status", gameStatus.name());
                put("playersCount", String.valueOf(playersCount));
                put("maxSlots", String.valueOf(maxSlots));
            }});
        });
    }

    public List<GameArena> getGamesAvailable() {
        return runTransaction(jedis -> {
            List<GameArena> games = new ArrayList<>();

            for (String hash : jedis.smembers(GAMES_HASH)) {
                if (!jedis.exists(String.format(GAMES_STATUS_HASH, hash))) {
                    continue;
                }

                Map<String, String> storage = jedis.hgetAll(String.format(GAMES_STATUS_HASH, hash));

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
            if (this.password != null) {
                jedis.auth(this.password);
            }

            return action.apply(jedis);
        }
    }

    private void runTransaction(Consumer<Jedis> action) {
        try (Jedis jedis = jedisPool.getResource()) {
            if (this.password != null) {
                jedis.auth(this.password);
            }

            action.accept(jedis);
        }
    }
}