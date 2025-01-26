package com.example.backend;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javafx.beans.property.*;

public class Task {
    private static int nextId = 1; // Auto-incrementing task ID
    private final int id;         // Unique identifier
    private final StringProperty title;         // Task title
    private final StringProperty description;   // Task description
    private final ObjectProperty<LocalDateTime> deadline; // Deadline for the task
    private final ObjectProperty<Priority> priority;      // Task priority (HIGH, MEDIUM, LOW)
    private final BooleanProperty completed;    // Status of task completion
    private final ObjectProperty<Categories> category;      // Task category
    private final List<Reminder> reminders;    // List of reminders
    private Status status;

    // Constructor
    public Task(String title, String description, LocalDateTime deadline, Priority priority, Categories category, Status status) {
        this.id = nextId++;
        this.title = new SimpleStringProperty(title);
        this.description = new SimpleStringProperty(description);
        this.deadline = new SimpleObjectProperty<>(deadline);
        this.priority = new SimpleObjectProperty<>(priority);
        this.completed = new SimpleBooleanProperty(false); // Default to not completed
        this.category = new SimpleObjectProperty<>(category);
        this.reminders = new ArrayList<>(); // Initialize empty reminders list
        this.status = status;
    }

    // Status methods
    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;

        // Automatically clear reminders if the task is marked as completed
        if (status == Status.COMPLETED) {
            clearReminders();
        }
    }

    // Reminder management
    public List<Reminder> getReminders() {
        return reminders;
    }

    public void addReminder(Reminder reminder) {
        if (status == Status.COMPLETED) {
            throw new IllegalStateException("Cannot add reminders to a completed task.");
        }
        reminders.add(reminder);
    }

    public void removeReminder(Reminder reminder) {
        reminders.remove(reminder);
    }

    public void clearReminders() {
        reminders.clear();
    }

    // JavaFX properties
    public StringProperty titleProperty() {
        return title;
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public ObjectProperty<LocalDateTime> deadlineProperty() {
        return deadline;
    }

    public ObjectProperty<Priority> priorityProperty() {
        return priority;
    }

    public BooleanProperty completedProperty() {
        return completed;
    }

    public ObjectProperty<Categories> categoryProperty() {
        return category;
    }

    // Getters and setters
    public String getTitle() {
        return title.get();
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    public String getDescription() {
        return description.get();
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public LocalDateTime getDeadline() {
        return deadline.get();
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline.set(deadline);
    }

    public Priority getPriority() {
        return priority.get();
    }

    public void setPriority(Priority priority) {
        this.priority.set(priority);
    }

    public boolean isCompleted() {
        return completed.get();
    }

    public void setCompleted(boolean completed) {
        this.completed.set(completed);
        if (completed) {
            clearReminders(); // Automatically clear reminders when completed
        }
    }

    public Categories getCategory() {
        return category.get();
    }

    public void setCategory(Categories category) {
        this.category.set(category);
    }

    // Additional functionality
    public void markAsCompleted() {
        setCompleted(true);
    }

    public boolean isOverdue() {
        return !completed.get() && LocalDateTime.now().isAfter(deadline.get());
    }

    @Override
    public String toString() {
        return String.format("Task #%d: %s%nDescription: %s%nPriority: %s%nDeadline: %s%nCategory: %s%nCompleted: %s%nReminders: %s",
                id,
                title.get(),
                description.get(),
                priority.get(),
                deadline.get(),
                category.get(),
                completed.get() ? "Yes" : "No",
                reminders);
    }

    // Equality and hashCode
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Task task = (Task) obj;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
