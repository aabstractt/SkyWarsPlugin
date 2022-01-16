package dev.thatsmybaby;

import cn.nukkit.plugin.PluginBase;
import dev.thatsmybaby.command.SWCommand;
import dev.thatsmybaby.factory.MapFactory;
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
    }
}