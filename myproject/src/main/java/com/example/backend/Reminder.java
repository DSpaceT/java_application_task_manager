package com.example.backend;

import java.time.LocalDateTime;

public class Reminder {
    private final String taskTitle;         // Title of the associated task (still final if you don't want to change it)
    private LocalDateTime reminderDate;     // The date/time of the reminder (no longer final)
    private ReminderType type;              // Type of reminder (no longer final)

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

    // === NEW SETTERS ===
    public void setReminderDate(LocalDateTime newDate) {
        this.reminderDate = newDate;
    }

    public void setType(ReminderType newType) {
        this.type = newType;
    }

    @Override
    public String toString() {
        return "Reminder for Task: " + taskTitle + " | " +
               "Type: " + type.name() + " | " +
               "Date: " + reminderDate;
    }
}
