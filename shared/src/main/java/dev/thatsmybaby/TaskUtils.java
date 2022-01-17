package dev.thatsmybaby;

import cn.nukkit.Server;

public class TaskUtils {

    public static void runAsync(Runnable runnable) {
        if (isPrimaryThread()) {
            Server.getInstance().getScheduler().scheduleTask(runnable, true);

            return;
        }

        runnable.run();
    }

    public static void runSync(Runnable runnable) {
        if (isPrimaryThread()) {
            runnable.run();
        } else {
            Server.getInstance().getScheduler().scheduleTask(runnable);
        }
    }

    protected static boolean isPrimaryThread() {
        return Server.getInstance().isPrimaryThread();
    }
}