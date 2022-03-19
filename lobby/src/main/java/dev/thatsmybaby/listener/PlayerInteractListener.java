package dev.thatsmybaby.listener;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerInteractEvent;
import dev.thatsmybaby.SignFactory;
import dev.thatsmybaby.TaskUtils;
import dev.thatsmybaby.utils.GameSign;

public class PlayerInteractListener implements Listener {

    @EventHandler (priority = EventPriority.NORMAL)
    public void onPlayerInteractEvent(PlayerInteractEvent ev) {
        Player player = ev.getPlayer();

        GameSign gameSign = SignFactory.getInstance().getGameSignAt(ev.getBlock());

        if (gameSign == null) {
            return;
        }

        TaskUtils.runAsync(() -> gameSign.handleInteract(player));
    }
}