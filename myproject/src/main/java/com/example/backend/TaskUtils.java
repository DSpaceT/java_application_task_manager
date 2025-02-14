package com.example.backend;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The TaskUtils class provides basic utility functions.
 * This class includes methods to handle tasks . Especially for filtering tasks ( returning tasks with specific parameters)
 * , changing some of the tasks category or priority field, or return basic overall information about all the tasks (stats)
 * 
 * The class takes as input {@code Task} objects and offers several functionalities like:
 *  - Filtering tasks.
 *  - Updating fields of them like category and priority.
 *  - Generating summary of statistics for the tasks based on their deadline mainly.
 * 
 * @author Dimitrios Georgoulopoulos
 * @version 2.0
 */

public class TaskUtils {

    /**
     * Filters tasks by category , priority and title . Specifically it returns tasks with 
     * specific values for this field given by the user. 
     * 
     * @param tasks All of the tasks given to the function.
     * @param category The category type we want to apply to the filter. ("All" if we want tasks with every category)
     * @param priority The priority type we want to apply to the filter. ("ALL" if we want tasks with every priority)
     * @param titleFilter The part of the title we want to filter with. ("" if we want to not filter by title part)
     * @return A list of tasks that match the applied filters.
     */
    public static List<Task> filterTasks(List<Task> tasks, Categories category, Priority priority, String titleFilter) {
        return tasks.stream()
                .filter(task -> category == null || category == Categories.valueOf("All") || task.getCategory().equals(category))
                .filter(task -> priority == null || priority == Priority.valueOf("ALL") || task.getPriority().equals(priority))
                .filter(task -> titleFilter == null || titleFilter.isEmpty() || task.getTitle().toLowerCase().contains(titleFilter.toLowerCase()))
                .collect(Collectors.toList());
    }
    
    /**
     * Finds tasks that are not of a specified category or priority or a combination of them.
     * 
     * @param tasks All of the tasks given to the function
     * @param category The category the tasks with this category to not be returned. If {@code null}, category is ignored.
     * @param priority The priority the tasks with this priority to not be returned. If {@code null}, priority is ignored.
     * @return The tasks without category / priority of {@code category} and {@code priority}
     */
    public static List<Task> excludeTasks(List<Task> tasks, Categories category, Priority priority) {
        return tasks.stream()
                .filter(task -> category == null || !task.getCategory().equals(category))
                .filter(task -> priority == null || !task.getPriority().equals(priority))
                .collect(Collectors.toList());
    }

    /**
     * Changes the category type of tasks with a specific previous category type.
     * 
     * @param tasks All of the tasks given to the function
     * @param oldCategory The category of the tasks we want to modify.
     * @param newCategory The new category we want to give to the above tasks.
     * @return All of the tasks updated or not.
     */
    public static List<Task> updateTasksCategory(List<Task> tasks, Categories oldCategory, Categories newCategory) {
        tasks.forEach(task -> {
            if (task.getCategory().equals(oldCategory)) {
                task.setCategory(newCategory);
            }
        });
        return tasks;
    }

    /**
     * Changes the priority type of tasks with a specific previous priority type.
     * 
     * @param tasks All of the tasks given to the function
     * @param oldPriority The priority of the tasks we want to modify.
     * @param newPriority The new priority we want to give to the above tasks.
     * @return All of the tasks updated or not.
     */
    public static List<Task> updateTasksPriority(List<Task> tasks, Priority oldPriority, Priority newPriority) {
        tasks.forEach(task -> {
            if (task.getPriority().equals(oldPriority)) {
                task.setPriority(newPriority);
            }
        });
        return tasks;
    }

    /**
     * Sets the task prioriy of tasks with a specified priority type to the "DEFAULT" value.
     * 
     * @param tasks All of the tasks given to the function
     * @param oldPriority priority of the tasks we want to modify.
     * @return All of the given tasks updated or not.
     */
    public static List<Task> setTasksToDefaultPriority(List<Task> tasks, Priority oldPriority) {
        tasks.forEach(task -> {
            if (task.getPriority().equals(oldPriority)) {
                task.setPriority(Priority.valueOf("DEFAULT"));
            }
        });
        return tasks;
    }

    /**
     * Returns a summary of tasks depending on the status value. Specifically
     * it returns the number of tasks , the number of tasks with status COMPLETED,
     * the number of tasks with status DELAYED and the number of tasks with deadline
     * within the next 7 days.
     * 
     * @param tasks All of the tasks given to the function.
     * @return @return A {@code TaskSummary} object containing the task statistics.
     */
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
