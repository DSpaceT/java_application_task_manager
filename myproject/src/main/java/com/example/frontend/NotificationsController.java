package com.example.frontend;

import com.example.backend.Reminder;
import com.example.backend.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.List;
import java.util.stream.Collectors;

public class NotificationsController {

    @FXML
    private ListView<HBox> notificationsList;

    @FXML
    private void initialize() {
        // Fetch reminders from the static tasks list in TaskController
        List<Reminder> reminders = TaskController.getTasks().stream()
                .flatMap(task -> task.getReminders().stream())
                .collect(Collectors.toList());

        // Populate the notifications list with HBox items (Label + Button)
        if (reminders != null && !reminders.isEmpty()) {
            notificationsList.getItems().setAll(
                reminders.stream()
                         .map(this::createNotificationItem)
                         .collect(Collectors.toList())
            );
        } else {
            HBox noRemindersBox = new HBox(new Label("No reminders available."));
            notificationsList.getItems().add(noRemindersBox);
        }
    }

    private HBox createNotificationItem(Reminder reminder) {
        Label reminderLabel = new Label(reminder.toString());
        Button removeButton = new Button("Remove");

        // Set the action for the remove button
        removeButton.setOnAction(event -> {
            removeReminder(reminder);
            notificationsList.getItems().removeIf(item -> 
                ((Label) item.getChildren().get(0)).getText().equals(reminder.toString())
            );
        });

        HBox itemBox = new HBox(10, reminderLabel, removeButton);
        itemBox.setSpacing(10);
        return itemBox;
    }

    private void removeReminder(Reminder reminder) {
        // Remove the reminder from its associated task
        TaskController.getTasks().forEach(task -> 
            task.getReminders().removeIf(r -> r.equals(reminder))
        );
    }

    @FXML
    private void goBackToTasks() {
        try {
            Stage stage = (Stage) notificationsList.getScene().getWindow();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/resources/TaskManagerView.fxml"));
            Parent tasksRoot = loader.load();

            Scene tasksScene;
            if (!stage.isFullScreen()) {
                // If not fullscreen, preserve current width and height
                tasksScene = new Scene(tasksRoot, stage.getWidth(), stage.getHeight());
            } else {
                // If fullscreen, create scene without fixed dimensions
                tasksScene = new Scene(tasksRoot);
            }

            stage.setScene(tasksScene);
            stage.centerOnScreen(); // Optionally re-center if needed
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
