package com.example.backend;

import java.util.ArrayList;
import java.util.List;

public class TaskManager {

    private static TaskManager instance;
    private final List<Task> tasks = new ArrayList<>();

    private TaskManager() {}

    public static TaskManager getInstance() {
        if (instance == null) {
            instance = new TaskManager();
        }
        return instance;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks.clear();
        if (tasks != null) {
            this.tasks.addAll(tasks);
        }
    }

    public void addTask(Task task) {
        tasks.add(task);
    }

    public void saveTasksToFile(String filePath) {
        TasksSaver.saveTasksToJSON(tasks, filePath);
    }

    public void loadTasksFromFile(String filePath) {
        List<Task> loadedTasks = TaskLoader.loadTasksFromJSONFile(filePath);
        setTasks(loadedTasks);
    }
}
