package dev.thatsmybaby;

import cn.nukkit.Server;
import cn.nukkit.scheduler.Task;
import cn.nukkit.scheduler.TaskHandler;
import cn.nukkit.utils.PluginException;
import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;

public abstract class TaskHandlerStorage {

    private final Map<String, Task> taskStorage = new HashMap<>();
    private final Map<String, Integer> taskIds = new HashMap<>();

    final public void scheduleRepeating(Task task, int ticks) {
        TaskHandler taskHandler = Server.getInstance().getScheduler().scheduleRepeatingTask(SkyWars.getInstance(), task, ticks);

        this.taskStorage.put(task.getClass().getSimpleName(), task);
        this.taskIds.put(task.getClass().getSimpleName(), taskHandler.getTaskId());
    }

    final public void cancelTask(String taskName, boolean cancel) {
        int taskId = this.taskIds.getOrDefault(taskName, -2);

        if (taskId == -1) {
            throw new PluginException("Task " + taskName + " is invalid name");
        }

        this.taskStorage.remove(taskName);
        this.taskIds.remove(taskName);

        if (cancel) {
            Server.getInstance().getScheduler().cancelTask(taskId);
        }
    }

    final public <T extends Task> T getTask(@NonNull Class<T> type) {
        Task task = this.taskStorage.get(type.getSimpleName());

        return type.isInstance(task) ? type.cast(task) : null;
    }
}