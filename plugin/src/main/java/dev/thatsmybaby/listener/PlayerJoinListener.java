package dev.thatsmybaby.listener;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.utils.TextFormat;
import dev.thatsmybaby.TaskUtil;
import dev.thatsmybaby.factory.ArenaFactory;
import dev.thatsmybaby.object.SWArena;
import dev.thatsmybaby.provider.GameProvider;

public class PlayerJoinListener implements Listener {

    @EventHandler (priority = EventPriority.MONITOR)
    public void onPlayerJoinEvent(PlayerJoinEvent ev) {
        Player player = ev.getPlayer();

        TaskUtil.runAsync(() -> {
            SWArena arena = ArenaFactory.getInstance().getArena(GameProvider.getInstance().getPlayerMapId(player.getName()));

            if (arena != null) {
                arena.joinAsPlayer(player);

                return;
            }

            if (!player.hasPermission("skywars.staff")) {
                player.kick("Use /sw findgame");

                return;
            }

            player.sendMessage(TextFormat.RED + "Joined as staff");
            player.setGamemode(Player.SPECTATOR);
        });
    }
}
