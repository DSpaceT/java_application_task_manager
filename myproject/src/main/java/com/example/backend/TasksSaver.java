package com.example.backend;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class TasksSaver {

    // Convert tasks to JSON and save to file
    public static void saveTasksToJSON(List<Task> tasks, String filePath) {
        String json = convertTasksToJSON(tasks);
        writeToFile(json, filePath);
    }

    // Convert list of tasks to a JSON string
    private static String convertTasksToJSON(List<Task> tasks) {
        JSONArray jsonArray = new JSONArray();
        for (Task task : tasks) {
            JSONObject taskObject = new JSONObject();
            taskObject.put("title", task.getTitle());
            taskObject.put("description", task.getDescription());
            taskObject.put("deadline", task.getDeadline().toString());
            taskObject.put("priority", task.getPriority().toString());
            taskObject.put("completed", task.isCompleted());
            taskObject.put("category", task.getCategory().toString());
            taskObject.put("status", task.getStatus().toString());

            // Add reminders
            JSONArray remindersArray = new JSONArray();
            for (Reminder reminder : task.getReminders()) {
                JSONObject reminderObject = new JSONObject();
                reminderObject.put("type", reminder.getType().toString());
                reminderObject.put("time", reminder.getReminderDate().toString()); // Use getReminderDate()
                remindersArray.put(reminderObject);
            }
            taskObject.put("reminders", remindersArray);

            jsonArray.put(taskObject);
        }
        return jsonArray.toString();
    }

    // Write the JSON string to a file
    private static void writeToFile(String json, String filePath) {
        try (FileWriter fileWriter = new FileWriter(filePath)) {
            fileWriter.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
