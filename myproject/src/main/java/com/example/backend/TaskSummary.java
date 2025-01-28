package com.example.backend;

public class TaskSummary {
    private final int totalTasks;
    private final int completedTasks;
    private final int delayedTasks;
    private final int dueWithin7Days;

    public TaskSummary(int totalTasks, int completedTasks, int delayedTasks, int dueWithin7Days) {
        this.totalTasks = totalTasks;
        this.completedTasks = completedTasks;
        this.delayedTasks = delayedTasks;
        this.dueWithin7Days = dueWithin7Days;
    }

    public int getTotalTasks() {
        return totalTasks;
    }

    public int getCompletedTasks() {
        return completedTasks;
    }

    public int getDelayedTasks() {
        return delayedTasks;
    }

    public int getDueWithin7Days() {
        return dueWithin7Days;
    }
}
