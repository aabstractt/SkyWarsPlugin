package dev.thatsmybaby;

import cn.nukkit.Server;
import cn.nukkit.level.Position;
import cn.nukkit.utils.Config;
import dev.thatsmybaby.utils.GameSign;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

final public class SignFactory {

    @Getter private final static SignFactory instance = new SignFactory();
    @Getter private final Map<String, GameSign> gameSigns = new ConcurrentHashMap<>();

    public void init() {
        for (String loc : GameLobby.getInstance().getConfig().getStringList("signs")) {
            registerNewSign(loc, false);

            System.out.println("Loading sign");
        }

        Server.getInstance().getScheduler().scheduleRepeatingTask(GameLobby.getInstance(), this::tick, 20, true);
    }

    public void registerNewSign(String string, boolean save) {
        this.gameSigns.put(string, new GameSign(string));

        if (save) {
            Config config = GameLobby.getInstance().getConfig();

            List<String> list = config.getStringList("signs");
            list.add(string);

            config.set("signs", list);
            config.save();
        }
    }

    public GameSign getGameSignAt(Position position) {
        return this.gameSigns.get(Placeholders.positionToString(position));
    }

    private void tick() {
        for (GameSign gameSign : this.gameSigns.values()) {
            gameSign.tick();
        }
    }
}