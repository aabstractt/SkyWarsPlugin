package dev.thatsmybaby;

import cn.nukkit.Server;

public class TaskUtil {

    public static void runAsync(Runnable runnable) {
        if (Server.getInstance().isPrimaryThread()) {
            new Thread(runnable).start();

            return;
        }

        runnable.run();
    }
}