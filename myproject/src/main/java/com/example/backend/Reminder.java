package com.example.backend;

import java.time.LocalDateTime;

public class Reminder {
    private final String taskTitle;       // Title of the associated task
    private final LocalDateTime reminderDate; // The date and time of the reminder
    private final ReminderType type;         // Type of reminder

    public Reminder(String taskTitle, LocalDateTime reminderDate, ReminderType type) {
        this.taskTitle = taskTitle;
        this.reminderDate = reminderDate;
        this.type = type;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public LocalDateTime getReminderDate() {
        return reminderDate;
    }

    public ReminderType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "Reminder for Task: " + taskTitle + " | " +
               "Type: " + type.name() + " | " +
               "Date: " + reminderDate;
    }
}
