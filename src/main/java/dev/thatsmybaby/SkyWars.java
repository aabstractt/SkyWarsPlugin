package dev.thatsmybaby;

import cn.nukkit.plugin.PluginBase;
import dev.thatsmybaby.arena.MapFactory;
import lombok.Getter;

public class SkyWars extends PluginBase {

    @Getter
    private static SkyWars instance;

    @Override
    public void onEnable() {
        instance = this;

        try {
            MapFactory.getInstance().init();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}