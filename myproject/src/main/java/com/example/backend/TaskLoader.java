package com.example.backend;

import java.time.LocalDateTime;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.time.format.DateTimeFormatter;

import java.io.FileInputStream;
import java.io.File;

public class TaskLoader {

    // Method to load tasks from a JSON file located at an absolute path
    public static List<Task> loadTasksFromJSONFile(String filePath) {
        List<Task> tasks = new ArrayList<>();
        try (FileInputStream fileInputStream = new FileInputStream(new File(filePath))) {
            // Read the FileInputStream into a String
            String jsonContent = new Scanner(fileInputStream, StandardCharsets.UTF_8).useDelimiter("\\A").next();

            // Pass the file content to loadTasksFromJSON to process it
            tasks = loadTasksFromJSON(jsonContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tasks;
    }

    // Existing method to load tasks from a JSON string
    public static List<Task> loadTasksFromJSON(String json) {
        List<Task> tasks = new ArrayList<>();
        JSONArray taskArray = new JSONArray(json);

        for (int i = 0; i < taskArray.length(); i++) {
            JSONObject taskObject = taskArray.getJSONObject(i);

            String title = taskObject.getString("title");
            String description = taskObject.getString("description");
            LocalDateTime deadline = LocalDateTime.parse(
                    taskObject.getString("deadline"),
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME
            );
            Priority priority = Priority.valueOf(taskObject.getString("priority"));
            boolean completed = taskObject.getBoolean("completed");
            Categories category = Categories.valueOf(taskObject.getString("category"));
            Status status = Status.valueOf(taskObject.getString("status"));

            Task task = new Task(title, description, deadline, priority, category, status);
            task.setCompleted(completed);

            // Parse reminders if present
            if (taskObject.has("reminders")) {
                JSONArray remindersArray = taskObject.getJSONArray("reminders");
                for (int j = 0; j < remindersArray.length(); j++) {
                    JSONObject reminderObject = remindersArray.getJSONObject(j);
                    ReminderType type = ReminderType.valueOf(reminderObject.getString("type"));
                    LocalDateTime reminderTime = LocalDateTime.parse(
                            reminderObject.getString("time"),
                            DateTimeFormatter.ISO_LOCAL_DATE_TIME
                    );
                    task.addReminder(new Reminder(task.getTitle(), reminderTime, type));
                }
            }

            tasks.add(task);
        }

        return tasks;
    }
}
