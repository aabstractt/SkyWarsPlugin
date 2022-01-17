package dev.thatsmybaby.listener;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.SignChangeEvent;
import dev.thatsmybaby.Placeholders;
import dev.thatsmybaby.SignFactory;

public class SignChangeListener implements Listener {

    @EventHandler (priority = EventPriority.NORMAL)
    public void onSignChangeEvent(SignChangeEvent ev) {
        Player player = ev.getPlayer();

        if (!ev.getLine(0).equals("[SW]") || !player.hasPermission("skywars.dev")) {
            return;
        }

        Block block = ev.getBlock();

        SignFactory.getInstance().registerNewSign(Placeholders.positionToString(block), true);
    }
}