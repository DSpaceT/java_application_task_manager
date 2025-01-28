package com.example.frontend;

import com.example.backend.Reminder;
import com.example.backend.ReminderType;
import com.example.backend.Task;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class NotificationsController {

    @FXML
    private ListView<HBox> notificationsList;

    // === New overlay controls for modifying a reminder ===
    @FXML
    private StackPane modifyNotificationOverlayPane;
    @FXML
    private ComboBox<ReminderType> modifyReminderTypeChoiceBox;
    @FXML
    private DatePicker modifyCustomReminderDatePicker;
    @FXML
    private Label modifyErrorLabel;  // optional, to show errors

    // Holds the current reminder being modified
    private Reminder currentReminderToModify;

    @FXML
    private void initialize() {
        // Fetch reminders from the static tasks list in TaskController
        List<Reminder> reminders = TaskController.getTasks().stream()
                .flatMap(task -> task.getReminders().stream())
                .collect(Collectors.toList());

        // Populate the notifications list with HBox items (Label + Buttons)
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

    /**
     * Creates the HBox for each reminder, including "Modify" and "Remove" buttons.
     */
    private HBox createNotificationItem(Reminder reminder) {
        Label reminderLabel = new Label(reminder.toString());
        Button modifyButton = new Button("Modify");
        Button removeButton = new Button("Remove");

        // Set the action for the "Modify" button
        modifyButton.setOnAction(event -> openModifyNotificationOverlay(reminder));

        // Set the action for the "Remove" button
        removeButton.setOnAction(event -> {
            removeReminder(reminder);
            notificationsList.getItems().removeIf(item -> 
                ((Label) item.getChildren().get(0)).getText().equals(reminder.toString())
            );
        });

        // Add both buttons to the HBox
        HBox itemBox = new HBox(10, reminderLabel, modifyButton, removeButton);
        itemBox.setSpacing(10);
        return itemBox;
    }

    /**
     * Removes the given reminder from its associated Task in TaskController.
     */
    private void removeReminder(Reminder reminder) {
        TaskController.getTasks().forEach(task ->
            task.getReminders().removeIf(r -> r.equals(reminder))
        );
        System.out.println("Removed reminder: " + reminder);
    }

    /**
     * Opens the overlay to modify an existing reminder's details.
     */
    private void openModifyNotificationOverlay(Reminder reminder) {
        // Store the current reminder being modified
        currentReminderToModify = reminder;

        // Set the overlay to visible
        modifyNotificationOverlayPane.setVisible(true);
        modifyNotificationOverlayPane.toFront();

        // Populate the combo box with possible ReminderTypes
        modifyReminderTypeChoiceBox.setItems(FXCollections.observableArrayList(ReminderType.values()));
        modifyReminderTypeChoiceBox.setValue(reminder.getType()); // Default to existing type

        // If the reminder has a custom date, you might want to detect it. 
        // Otherwise, just clear for the user to pick if they want.
        modifyCustomReminderDatePicker.setValue(null);
        modifyErrorLabel.setText("");
    }

    /**
     * Called when the user clicks "Modify" in the overlay.
     * Updates the reminder's type and/or date, then hides the overlay.
     */
    @FXML
    private void handleModifyNotificationFromOverlay() {
        if (currentReminderToModify == null) {
            return; // No reminder is being modified
        }

        ReminderType selectedType = modifyReminderTypeChoiceBox.getValue();
        if (selectedType == null) {
            modifyErrorLabel.setText("Please select a reminder type.");
            return;
        }

        // The new date for the reminder, if applicable
        LocalDateTime newReminderDate = null;

        try {
            switch (selectedType) {
                case ONE_DAY_BEFORE -> 
                    newReminderDate = getAssociatedTaskDeadline().minusDays(1);
                case ONE_WEEK_BEFORE -> 
                    newReminderDate = getAssociatedTaskDeadline().minusWeeks(1);
                case ONE_MONTH_BEFORE -> 
                    newReminderDate = getAssociatedTaskDeadline().minusMonths(1);
                case RIGHT_NOW -> 
                    // Some immediate time in the future (like in 5 seconds)
                    newReminderDate = LocalDateTime.now().plusSeconds(5);
                case CUSTOM_DATE -> {
                    LocalDate pickedDate = modifyCustomReminderDatePicker.getValue();
                    if (pickedDate == null) {
                        modifyErrorLabel.setText("Please select a custom date.");
                        return;
                    }
                    newReminderDate = pickedDate.atStartOfDay();
                }
            }

            if (newReminderDate == null) {
                modifyErrorLabel.setText("Could not determine a valid reminder date.");
                return;
            }
            if (newReminderDate.isBefore(LocalDateTime.now())) {
                modifyErrorLabel.setText("Selected reminder date is in the past.");
                return;
            }

            // Apply changes to the existing reminder
            currentReminderToModify.setType(selectedType);
            currentReminderToModify.setReminderDate(newReminderDate);

            System.out.println("Modified reminder: " + currentReminderToModify);

            closeModifyNotificationOverlay();

            // Optionally re-initialize the list if you want to see updated text
            initialize();

        } catch (Exception e) {
            modifyErrorLabel.setText("Error: " + e.getMessage());
        }
    }

    /**
     * Helper method to get the associated Task's deadline. 
     */
    private LocalDateTime getAssociatedTaskDeadline() {
        for (Task task : TaskController.getTasks()) {
            if (task.getReminders().contains(currentReminderToModify)) {
                return task.getDeadline();
            }
        }
        return LocalDateTime.now().plusDays(1);
    }

    @FXML
    private void closeModifyNotificationOverlay() {
        modifyNotificationOverlayPane.setVisible(false);
        currentReminderToModify = null;
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
