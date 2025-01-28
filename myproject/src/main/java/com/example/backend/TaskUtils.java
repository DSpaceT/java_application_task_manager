package com.example.backend;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class TaskUtils {

    /**
     * Filters tasks based on the given category and priority.
     *
     * @param tasks    the list of tasks to filter
     * @param category the category to filter by (Categories.ALL to ignore)
     * @param priority the priority to filter by (Priority.ALL to ignore)
     * @return a new list of tasks matching the given filters
     */
    public static List<Task> filterTasks(List<Task> tasks, Categories category, Priority priority, String titleFilter) {
        return tasks.stream()
                .filter(task -> category == null || category == Categories.valueOf("All") || task.getCategory().equals(category))
                .filter(task -> priority == null || priority == Priority.valueOf("ALL") || task.getPriority().equals(priority))
                .filter(task -> titleFilter == null || titleFilter.isEmpty() || task.getTitle().toLowerCase().contains(titleFilter.toLowerCase()))
                .collect(Collectors.toList());
    }
    

        /**
     * Excludes tasks that match the given category or priority.
     *
     * @param tasks    the list of tasks to filter
     * @param category the category to exclude (null to ignore)
     * @param priority the priority to exclude (null to ignore)
     * @return a new list of tasks excluding those with the specified category or priority
     */
    public static List<Task> excludeTasks(List<Task> tasks, Categories category, Priority priority) {
        return tasks.stream()
                .filter(task -> category == null || !task.getCategory().equals(category))
                .filter(task -> priority == null || !task.getPriority().equals(priority))
                .collect(Collectors.toList());
    }

        /**
     * Updates all tasks with the given old category to the new category.
     *
     * @param tasks       the list of tasks to update
     * @param oldCategory the old category to be replaced
     * @param newCategory the new category to assign
     * @return the updated list of tasks
     */
    public static List<Task> updateTasksCategory(List<Task> tasks, Categories oldCategory, Categories newCategory) {
        tasks.forEach(task -> {
            if (task.getCategory().equals(oldCategory)) {
                task.setCategory(newCategory);
            }
        });
        return tasks;
    }
    public static List<Task> updateTasksPriority(List<Task> tasks, Priority oldPriority, Priority newPriority) {
        tasks.forEach(task -> {
            if (task.getPriority().equals(oldPriority)) {
                task.setPriority(newPriority);
            }
        });
        return tasks;
    }
    public static List<Task> setTasksToDefaultPriority(List<Task> tasks, Priority oldPriority) {
        tasks.forEach(task -> {
            if (task.getPriority().equals(oldPriority)) {
                task.setPriority(Priority.valueOf("DEFAULT"));
            }
        });
        return tasks;
    }

    public static TaskSummary getTaskSummary(List<Task> tasks) {
        int total = tasks.size();

        int completedCount = (int) tasks.stream()
                .filter(t -> t.getStatus() == Status.COMPLETED)
                .count();

        int delayedCount = (int) tasks.stream()
                .filter(t -> t.getStatus() == Status.DELAYED)
                .count();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextWeek = now.plusDays(7);

        int dueWithin7DaysCount = (int) tasks.stream()
                .filter(t -> {
                    LocalDateTime deadline = t.getDeadline();
                    return !deadline.isBefore(now) && deadline.isBefore(nextWeek);
                })
                .count();

        return new TaskSummary(
            total,
            completedCount,
            delayedCount,
            dueWithin7DaysCount
        );
    }
    
}
