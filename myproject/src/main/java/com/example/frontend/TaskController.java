package com.example.frontend;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.example.backend.Task;
import com.example.backend.TaskSummary;
import com.example.backend.TaskUtils;
import com.example.backend.Priority;
import com.example.backend.Reminder;
import com.example.backend.ReminderType;
import com.example.backend.Categories;
import com.example.backend.Status;
import javafx.util.Duration;

public class TaskController {


    @FXML private VBox taskContainer;

    @FXML private StackPane overlayPane; 
    @FXML private TextField taskTitleField;
    @FXML private TextArea taskDescriptionField;
    @FXML private DatePicker taskDeadlinePicker;
    @FXML private ComboBox<Priority> PriorityChoiceBox;

    @FXML private ComboBox<Categories> categoryChoiceBox;


    @FXML private ChoiceBox<Categories> categoryChoiceBoxMain;
    @FXML private ChoiceBox<Priority> priorityChoiceBoxMain;

    @FXML private StackPane manageCategoriesOverlayPane;
    @FXML private ListView<Categories> categoriesListView;
    @FXML private TextField newCategoryField;

    @FXML private ListView<Priority> prioritiesListView;  
    @FXML private TextField newpriorityField;


    @FXML private VBox manageCategoriesSection;

    @FXML private TextField titleFilterField;

    @FXML private StackPane notificationOverlayPane;
    @FXML private ComboBox<ReminderType> reminderTypeChoiceBox;
    @FXML private DatePicker customReminderDatePicker;
    
    private Task currentTaskForReminder;



    // private Map<Categories, List<Task>> categorizedTasks = new HashMap<>();
    private static List<Task> tasks = new ArrayList<>();

    @FXML
    private Label notificationIndicator;

    private Timeline reminderChecker;

    @FXML private Label lblTotalTasks;
    @FXML private Label lblCompletedTasks;
    @FXML private Label lblDelayedTasks;
    @FXML private Label lblDue7Days;


    @FXML Categories selectedCategory;
    @FXML Priority selectedPriority;
    @FXML String titleFilter;
    @FXML
    private Label addReminderErrorLabel;

    @FXML
    private void initialize() {
        startReminderChecker();
        setupCategoryChoiceBoxMain();
        setupPriorityChoiceBoxMain();
        setupCategoryChoiceBoxForForm();
        setupTaskPriorityChoiceBox();
        initCategoriesListView();
        initPrioritiesListView();
        updateTaskContainer(Categories.valueOf("All"),Priority.valueOf("ALL"),null);
    }

    private void refreshTaskSummary() {
        TaskSummary summary = TaskUtils.getTaskSummary(tasks);

        lblTotalTasks.setText("Total tasks: " + summary.getTotalTasks());
        lblCompletedTasks.setText("Completed: " + summary.getCompletedTasks());
        lblDelayedTasks.setText("Delayed: " + summary.getDelayedTasks());
        lblDue7Days.setText("Due in 7 days: " + summary.getDueWithin7Days());
    }

    private void startReminderChecker() {
        reminderChecker = new Timeline(new KeyFrame(Duration.seconds(10), event -> checkForDueReminders()));
        reminderChecker.setCycleCount(Timeline.INDEFINITE);
        reminderChecker.play();
    }

    private void checkForDueReminders() {
        boolean hasDueReminder = TaskController.getTasks().stream()
            .flatMap(task -> task.getReminders().stream())
            .anyMatch(reminder -> reminder.getReminderDate().isBefore(LocalDateTime.now()));
    

        // Now call updateTaskContainer with the *current* filter:
        updateTaskContainer(selectedCategory, selectedPriority, titleFilter);

        // updateTaskContainer(Categories.valueOf("All"), Priority.valueOf("ALL"), "");
    
        // Update notification indicator
        if (hasDueReminder) {
            showNotificationIndicator();
        } else {
            hideNotificationIndicator();
        }
    }
    
    

    private void showNotificationIndicator() {
        notificationIndicator.setText("!");
        notificationIndicator.setVisible(true);
    }

    private void hideNotificationIndicator() {
        notificationIndicator.setVisible(false);
    }
    public static List<Task> getTasks() {
        return tasks;
    }
    
    public static void setTasks(List<Task> newTasks) {
        tasks = newTasks;
    }
    


    private void setupCategoryChoiceBoxMain() {
        categoryChoiceBoxMain.setItems(FXCollections.observableArrayList(Categories.values()));
        // Default to "All" by name
        categoryChoiceBoxMain.setValue(Categories.valueOf("All"));
    }
    private void setupPriorityChoiceBoxMain(){
        priorityChoiceBoxMain.setItems(FXCollections.observableArrayList(Priority.values()));
        priorityChoiceBoxMain.setValue(Priority.valueOf("ALL"));
    }

    private void setupCategoryChoiceBoxForForm() {
        categoryChoiceBox.setItems(
            FXCollections.observableArrayList(Categories.values())
            .filtered(p -> !p.equals(Categories.valueOf("All"))));

        categoryChoiceBox.setValue(Categories.valueOf("Default"));
    }

    @FXML
    private void handleFilters() {
        selectedCategory = categoryChoiceBoxMain.getValue();
        selectedPriority = priorityChoiceBoxMain.getValue();
        titleFilter = titleFilterField.getText().toLowerCase(); 
    
        System.out.println("Selected Filters - Category: " + selectedCategory + ", Priority: " + selectedPriority + ", Title: " + titleFilter);
    
        // Update the task container with the selected filters
        updateTaskContainer(selectedCategory, selectedPriority, titleFilter);
    }
    

    // ---------------------- Setup Priority ChoiceBox --------------------------
    private void setupTaskPriorityChoiceBox() {
        PriorityChoiceBox.setItems(
            FXCollections.observableArrayList(Priority.values())
                .filtered(p -> !p.equals(Priority.valueOf("ALL")))
        );
    
        // PriorityChoiceBox.setItems(FXCollections.observableArrayList(Priority.values()));
        PriorityChoiceBox.setValue(Priority.valueOf("DEFAULT")); 
    }


    private void initCategoriesListView() {
        categoriesListView.setItems(
            FXCollections.observableArrayList(Categories.values())
                .filtered(p -> !p.equals(Categories.valueOf("All")) && !p.equals(Categories.valueOf("Default")))
        );
    
        categoriesListView.setCellFactory(listView -> new ListCell<Categories>() {
            private final HBox cellContainer = new HBox();
            private final Label categoryLabel = new Label();
            private final TextField modifyField = new TextField();
            private final Button modifyButton = new Button("Modify Name");
            private final Button removeButton = new Button("Remove");
    
            {
                cellContainer.setSpacing(10);
                cellContainer.getChildren().addAll(categoryLabel, modifyField, modifyButton, removeButton);
    
                // Modify button logic
                modifyButton.setOnAction(e -> {
                    Categories category = getItem();
                    if (category != null) {
                        String newName = modifyField.getText().trim();
                        if (!newName.isEmpty()) {
                            updateCategoryAndTasks(category, newName);
                            modifyField.clear();
                        }
                    }
                });
    
                // Remove button logic
                removeButton.setOnAction(e -> {
                    Categories category = getItem();
                    if (category != null) {
                        System.out.println("Removing category: " + category);
                        removeCategoryAndTasks(category);
                    }
                });
            }
    
            @Override
            protected void updateItem(Categories item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    categoryLabel.setText(item.toString());
                    modifyField.setPromptText("New name...");
                    setGraphic(cellContainer);
                }
            }
        });
    }
    
    private void updateCategoryAndTasks(Categories oldCategory, String newCategoryName) {

        try {
            Categories newCategory = Categories.modifyCategoryName(oldCategory.toString(), newCategoryName);

            tasks = TaskUtils.updateTasksCategory(tasks, oldCategory, newCategory);
    
            System.out.println("Updated category: " + oldCategory + " to " + newCategoryName);

            categoriesListView.setItems(
                FXCollections.observableArrayList(Categories.values())
                    .filtered(c -> !c.equals(Categories.valueOf("All")) && !c.equals(Categories.valueOf("Default")))
            );

            updateTaskContainer(Categories.valueOf("All"), Priority.valueOf("ALL"), "");
    
            updateCategoryChoiceBoxes();
    
        } catch (IllegalArgumentException ex) {
            System.err.println("Error updating category: " + ex.getMessage());

        }
    }
    
    private void initPrioritiesListView() {

        prioritiesListView.setItems(
            FXCollections.observableArrayList(Priority.values())
                .filtered(p -> !p.equals(Priority.valueOf("DEFAULT")) && !p.equals(Priority.valueOf("ALL")))
        );
    
        prioritiesListView.setCellFactory(listView -> new ListCell<Priority>() {
            private final HBox cellContainer = new HBox();
            private final Label priorityLabel = new Label();
            private final TextField modifyField = new TextField();
            private final Button modifyButton = new Button("Modify Name");
            private final Button removeButton = new Button("Remove");
    
            {
                cellContainer.setSpacing(10);
                cellContainer.getChildren().addAll(priorityLabel, modifyField, modifyButton, removeButton);
    

                modifyButton.setOnAction(e -> {
                    Priority priority = getItem();
                    if (priority != null) {
                        String newName = modifyField.getText().trim();
                        if (!newName.isEmpty()) {
                            updatePriorityAndTasks(priority, newName); // Update priority and tasks
                            modifyField.clear(); // Clear the text field after modification
                        }
                    }
                });

                removeButton.setOnAction(e -> {
                    Priority priority = getItem();
                    if (priority != null) {
                        System.out.println("Reassigning tasks from priority: " + priority + " to DEFAULT...");
                        defaultPriorityAndTasks(priority); // <-- the new method you created
                    }
                });
                
            }
    
            @Override
            protected void updateItem(Priority item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    priorityLabel.setText(item.toString());
                    modifyField.clear(); // Ensure the field is empty on item update
                    modifyField.setPromptText("New name...");
                    setGraphic(cellContainer);
                }
            }
        });
    }
    private void updatePriorityAndTasks(Priority oldPriority, String newPriorityName) {
        try {

            Priority newPriority = Priority.addPriority(newPriorityName);
    

            tasks = TaskUtils.updateTasksPriority(tasks, oldPriority, newPriority);

            Priority.removePriority(oldPriority.toString());
    
            System.out.println("Updated priority: " + oldPriority + " to " + newPriorityName);

            prioritiesListView.setItems(
                FXCollections.observableArrayList(Priority.values())
                    .filtered(p -> !p.equals(Priority.valueOf("DEFAULT")) && !p.equals(Priority.valueOf("ALL")))
            );

            updateTaskContainer(Categories.valueOf("All"), Priority.valueOf("ALL"), "");


            updatePriorityChoiceBoxes();
    
        } catch (IllegalArgumentException ex) {
            System.err.println("Error updating priority: " + ex.getMessage());

        }
    }
        

    private void defaultPriorityAndTasks(Priority priority) {
        // Prevent removing or reassigning DEFAULT itself
        if (priority == Priority.valueOf("DEFAULT")) {
            System.out.println("Cannot reassign DEFAULT priority to itself.");
            return;
        }

        tasks = TaskUtils.setTasksToDefaultPriority(tasks, priority);

        boolean removed = Priority.removePriority(priority.toString());
        if (removed) {
            System.out.println("Removed priority from the internal list: " + priority);
        }

        prioritiesListView.setItems(
            FXCollections.observableArrayList(Priority.values())
                .filtered(p -> !p.equals(Priority.valueOf("DEFAULT")) && !p.equals(Priority.valueOf("ALL")))
        );

        updateTaskContainer(Categories.valueOf("All"), Priority.valueOf("ALL"), null);

        updatePriorityChoiceBoxes();

        System.out.println("All tasks that had priority " + priority 
            + " have now been reassigned to DEFAULT priority.");
    }


    private void removeCategoryAndTasks(Categories category) {
        tasks = TaskUtils.excludeTasks(tasks, category, null);


        boolean removed = Categories.removeCategory(category.toString());
        if (removed) {
            System.out.println("Removed category from the internal list: " + category);
        }

        categoriesListView.setItems(
            FXCollections.observableArrayList(Categories.values())
                .filtered(c -> !c.equals(Categories.valueOf("All")) && !c.equals(Categories.valueOf("Default")))
        );

        updateTaskContainer(Categories.valueOf("All"), Priority.valueOf("ALL"), "");

        updateCategoryChoiceBoxes();
    }

    
    @FXML
    private void handleAddPriority() {
        String newPriorityName = newpriorityField.getText().trim();

        if (!newPriorityName.isEmpty()) {
            // Dynamically add or retrieve the priority (if it exists, we get the existing one)
            Priority.addPriority(newPriorityName);

            prioritiesListView.setItems(
                FXCollections.observableArrayList(Priority.values())
                    .filtered(p -> !p.equals(Priority.valueOf("DEFAULT")) && !p.equals(Priority.valueOf("ALL")))
            );

            // Refresh the dropdowns
            updatePriorityChoiceBoxes();

            // Clear the text field
            newpriorityField.clear();
        }
    }


    private void updatePriorityChoiceBoxes() {
        priorityChoiceBoxMain.setItems(FXCollections.observableArrayList(Priority.values()));
        priorityChoiceBoxMain.setValue(Priority.valueOf("ALL"));
        PriorityChoiceBox.setItems(
            FXCollections.observableArrayList(Priority.values())
                .filtered(c -> !c.equals(Priority.valueOf("ALL")))
        );
        // priorityChoiceBox.setItems(FXCollections.observableArrayList(Priority.values()));
        PriorityChoiceBox.setValue(Priority.valueOf("DEFAULT"));
    }

    

    private void updateCategoryChoiceBoxes() {
        categoryChoiceBoxMain.setItems(FXCollections.observableArrayList(Categories.values()));
        categoryChoiceBoxMain.setValue(Categories.valueOf("All"));
        categoryChoiceBox.setItems(
            FXCollections.observableArrayList(Categories.values())
                .filtered(c -> !c.equals(Categories.valueOf("All")))
        );
        // categoryChoiceBox.setItems(FXCollections.observableArrayList(Categories.values()));
        categoryChoiceBox.setValue(Categories.valueOf("Default"));
    }

    private void updateTaskContainer(Categories selectedCategory, Priority selectedPriority, String titleFilter) {
        taskContainer.getChildren().clear();

        // Use TaskUtils.filterTasks to get the filtered tasks
        List<Task> filteredTasks = TaskUtils.filterTasks(tasks, selectedCategory, selectedPriority,titleFilter);

        // Further filter by title
        if (titleFilter != null && !titleFilter.isEmpty()) {
            filteredTasks = filteredTasks.stream()
                    .filter(task -> task.getTitle().toLowerCase().contains(titleFilter))
                    .collect(Collectors.toList());
        }

        // Add the filtered tasks to the task container
        for (Task task : filteredTasks) {
            addTask(task);
        }
        refreshTaskSummary();
        

    }

    private void addTask(Task task) {
        HBox taskBox = createTaskBox(task);
        taskContainer.getChildren().add(taskBox);
        refreshTaskSummary();
    }
    

    private HBox createTaskBox(Task task) {
        LocalDateTime now = LocalDateTime.now();
        // Only set to DELAYED if the task is not already marked as COMPLETED
        if (task.getDeadline().isBefore(now) && task.getStatus() != Status.COMPLETED) {
            task.setStatus(Status.DELAYED);
        }
    
        // --- Container for the task box ---
        HBox taskBox = new HBox(20);
        taskBox.setAlignment(Pos.CENTER_LEFT);
        if (task.getStatus() == Status.COMPLETED) {
            taskBox.setStyle(
                "-fx-padding: 15; " +
                "-fx-border-color: #81c784; " +
                "-fx-border-width: 2; " +
                "-fx-background-color: #c8e6c9; " +
                "-fx-border-radius: 10; " +
                "-fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 10, 0.5, 0, 0);"
            );
        } else {
            // Set the default blue style (or another style if needed)
            taskBox.setStyle(
                "-fx-padding: 15; " +
                "-fx-border-color: #bbdefb; " +
                "-fx-border-width: 2; " +
                "-fx-background-color: linear-gradient(to bottom, #e3f2fd, #bbdefb); " +
                "-fx-border-radius: 10; " +
                "-fx-background-radius: 10; " +
                "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 10, 0.5, 0, 0);"
            );
        }
        
    
        // --- Task details ---
        Label titleLabel = new Label("Title: " + task.getTitle());
        titleLabel.setStyle("-fx-font-size: 14pt; -fx-font-weight: bold; -fx-text-fill: #0d47a1;");
    
        Label descriptionLabel = new Label("Description: " + task.getDescription());
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(300);
        descriptionLabel.setStyle("-fx-text-fill: #1976d2;");
    
        Label deadlineLabel = new Label(
            "Deadline: " + task.getDeadline().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        );
        deadlineLabel.setStyle("-fx-text-fill: #1e88e5; -fx-font-size: 12pt;");
    
        Label priorityLabel = new Label("Priority: " + task.getPriority());
        priorityLabel.setStyle("-fx-text-fill: #5d4037;");
    
        Label categoryLabel = new Label("Category: " + task.getCategory());
        categoryLabel.setStyle("-fx-text-fill: #00695c;");
    
        Label statusLabel = new Label("Status: " + task.getStatus());
        statusLabel.setStyle("-fx-text-fill: " + 
            (task.getStatus() == Status.DELAYED ? "#b71c1c" : 
            task.getStatus() == Status.COMPLETED ? "#2e7d32; -fx-font-weight: bold;" : "#1565c0") + ";");
    
        VBox detailsBox = new VBox(10, titleLabel, descriptionLabel, deadlineLabel, priorityLabel, categoryLabel, statusLabel);
        detailsBox.setAlignment(Pos.TOP_LEFT);
        taskBox.getChildren().add(detailsBox);
    
        // --- Buttons container ---
        HBox buttonsBox = new HBox(15);
        buttonsBox.setAlignment(Pos.CENTER);
    
        // --- Conditionally create ComboBox for status change ---
        final ComboBox<Status> statusComboBox;
        if (task.getStatus() != Status.DELAYED) {
            statusComboBox = new ComboBox<>();
            statusComboBox.getItems().addAll(
                Arrays.stream(Status.values())
                    .filter(status -> status != Status.DELAYED)
                    .collect(Collectors.toList())
            );

            statusComboBox.setPromptText("Status");
            statusComboBox.setPrefWidth(120); // Increased width for better text visibility
            statusComboBox.setPrefHeight(30); // Match button height
            
            // Style the ComboBox like the buttons
            statusComboBox.setStyle(
                "-fx-background-color: #90caf9;" +  // Light blue background
                "-fx-border-color: #0288d1;" +    // Blue border
                "-fx-border-width: 2;" +          // Consistent border width
                "-fx-font-size: 12pt;" +          // Font size
                "-fx-font-weight: bold;" +        // Bold font
                "-fx-text-fill: white;" +         // White text for visibility
                "-fx-background-radius: 10;" +    // Rounded corners
                "-fx-border-radius: 10;" +        // Rounded corners for the border\n" +
                "-fx-padding: 0 10 0 10;"         // Internal padding for text visibility
            );
        
            // Custom button cell to always display "Status"
            statusComboBox.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Status item, boolean empty) {
                    super.updateItem(item, empty);
                    setText("Status"); // Always display "Status"
                    setAlignment(Pos.CENTER); // Align text in the center
                }
            });
        
            // Cell factory to show options in the dropdown
            statusComboBox.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(Status item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item.toString());
                }
            });
        
            // Change status and dynamically update the task box
            statusComboBox.setOnAction(event -> {
                Status selectedStatus = statusComboBox.getValue();
                if (selectedStatus != null) {
                    task.setStatus(selectedStatus);
                    statusLabel.setText("Status: " + task.getStatus());
                    statusLabel.setStyle("-fx-text-fill: " +
                        (selectedStatus == Status.DELAYED ? "#b71c1c" :
                        selectedStatus == Status.COMPLETED ? "#2e7d32; -fx-font-weight: bold;" : "#1565c0") + ";");
        
                    // Update task box color
                    if (selectedStatus == Status.COMPLETED) {
                        taskBox.setStyle(
                            "-fx-padding: 15; " +
                            "-fx-border-color: #81c784; " +
                            "-fx-border-width: 2; " +
                            "-fx-background-color: #c8e6c9; " +
                            "-fx-border-radius: 10; " +
                            "-fx-background-radius: 10; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 10, 0.5, 0, 0);"
                        );
                    } else if (selectedStatus == Status.DELAYED) {
                        buttonsBox.getChildren().remove(statusComboBox);
                    } else {
                        taskBox.setStyle(
                            "-fx-padding: 15; " +
                            "-fx-border-color: #bbdefb; " +
                            "-fx-border-width: 2; " +
                            "-fx-background-color: linear-gradient(to bottom, #e3f2fd, #bbdefb); " +
                            "-fx-border-radius: 10; " +
                            "-fx-background-radius: 10; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 10, 0.5, 0, 0);"
                        );
                    }
                    refreshTaskSummary();
                }
            });
        }
        
        
        else {
            statusComboBox = null;
        }
    
        // --- Create Buttons ---
        Button deleteButton = new Button("Delete");
        deleteButton.setStyle("-fx-background-color: #ffcdd2; -fx-text-fill: #b71c1c; "
                             + "-fx-font-weight: bold; -fx-background-radius: 10;");
        deleteButton.setMinWidth(80);
        deleteButton.setOnAction(event -> {
            taskContainer.getChildren().remove(taskBox);
            tasks.remove(task);
        });
    
        Button modifyButton = new Button("Modify");
        modifyButton.setStyle("-fx-background-color: #bbdefb; -fx-text-fill: #0d47a1; "
                             + "-fx-font-weight: bold; -fx-background-radius: 10;");
        modifyButton.setMinWidth(80);
        modifyButton.setOnAction(event -> openAddTaskOverlay(task));
    
        Button toggleStatusButton = new Button("Toggle Status");
        toggleStatusButton.setStyle("-fx-background-color: #90caf9; -fx-text-fill: #0d47a1; "
                                  + "-fx-font-weight: bold; -fx-background-radius: 10;");
        toggleStatusButton.setMinWidth(100);
        toggleStatusButton.setOnAction(event -> {
            if (task.getDeadline().isBefore(now) && task.getStatus() != Status.COMPLETED) {
                task.setStatus(Status.DELAYED);
            }else {
                switch (task.getStatus()) {
                    case OPEN -> task.setStatus(Status.IN_PROGRESS);
                    case IN_PROGRESS -> task.setStatus(Status.COMPLETED);
                    case COMPLETED -> task.setStatus(Status.IN_PROGRESS);
                    case POSTPONED, DELAYED -> task.setStatus(Status.IN_PROGRESS);
                }
            }
            statusLabel.setText("Status: " + task.getStatus());
            statusLabel.setStyle("-fx-text-fill: " + 
                (task.getStatus() == Status.DELAYED ? "#b71c1c" : 
                task.getStatus() == Status.COMPLETED ? "#2e7d32; -fx-font-weight: bold;" : "#1565c0") + ";");
    
            if (task.getStatus() == Status.COMPLETED) {
                taskBox.setStyle(
                    "-fx-padding: 15; " +
                    "-fx-border-color: #81c784; " +
                    "-fx-border-width: 2; " +
                    "-fx-background-color: #c8e6c9; " +
                    "-fx-border-radius: 10; " +
                    "-fx-background-radius: 10; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 10, 0.5, 0, 0);"
                );
            } else {
                taskBox.setStyle(
                    "-fx-padding: 15; " +
                    "-fx-border-color: #bbdefb; " +
                    "-fx-border-width: 2; " +
                    "-fx-background-color: linear-gradient(to bottom, #e3f2fd, #bbdefb); " +
                    "-fx-border-radius: 10; " +
                    "-fx-background-radius: 10; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.1), 10, 0.5, 0, 0);"
                );
            }
    
            if (task.getStatus() == Status.DELAYED && statusComboBox != null) {
                buttonsBox.getChildren().remove(statusComboBox);
            }
            refreshTaskSummary();
        });
    
        Button notifyButton = new Button("Notify");
        notifyButton.setStyle("-fx-background-color: #e3f2fd; -fx-text-fill: #0288d1; "
                             + "-fx-font-weight: bold; -fx-background-radius: 10;");
        notifyButton.setMinWidth(80);
        notifyButton.setOnAction(event -> openAddNotificationOverlay(task));
    
        buttonsBox.getChildren().addAll(deleteButton, modifyButton, toggleStatusButton, notifyButton);
    
        if (statusComboBox != null) {
            buttonsBox.getChildren().add(statusComboBox);
        }
    
        taskBox.getChildren().add(buttonsBox);
    
        // --- Overdue reminders ---
        Label overdueReminderLabel = new Label();
        overdueReminderLabel.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
    
        List<Reminder> overdueReminders = task.getReminders().stream()
            .filter(r -> r.getReminderDate().isBefore(LocalDateTime.now()))
            .collect(Collectors.toList());
    
        if (!overdueReminders.isEmpty()) {
            String reminderDetails = overdueReminders.stream()
                .map(r -> r.getType().toString())
                .collect(Collectors.joining(", "));
            overdueReminderLabel.setText("Overdue Reminders: " + reminderDetails);
        }
    
        taskBox.getChildren().add(overdueReminderLabel);
    
        return taskBox;
    }
    
    

    public void openAddNotificationOverlay(Task task) {
        currentTaskForReminder = task;
        
        // Initialize reminder type choices
        reminderTypeChoiceBox.setItems(FXCollections.observableArrayList(ReminderType.values()));
        reminderTypeChoiceBox.setValue(ReminderType.ONE_DAY_BEFORE); // default choice
    
        // Reset custom date field
        customReminderDatePicker.setValue(null);
    
        // Show overlay
        notificationOverlayPane.setVisible(true);
        notificationOverlayPane.toFront();
    }
    
    @FXML
    private void handleAddReminderFromOverlay() {
        // Clear any old error messages every time we enter
        addReminderErrorLabel.setText("");

        if (currentTaskForReminder == null) {
            // You might log an error or simply return
            return;
        }

        ReminderType selectedType = reminderTypeChoiceBox.getValue();
        if (selectedType == null) {
            addReminderErrorLabel.setText("Please select a reminder type.");
            return;
        }

        LocalDateTime reminderDate = null;

        try {
            switch (selectedType) {
                case ONE_DAY_BEFORE ->
                    reminderDate = currentTaskForReminder.getDeadline().minusDays(1);
                case ONE_WEEK_BEFORE ->
                    reminderDate = currentTaskForReminder.getDeadline().minusWeeks(1);
                case ONE_MONTH_BEFORE ->
                    reminderDate = currentTaskForReminder.getDeadline().minusMonths(1);
                case RIGHT_NOW ->
                    // Some slight offset in the future
                    reminderDate = LocalDateTime.now().plusSeconds(5);
                case CUSTOM_DATE -> {
                    // Check for a custom date
                    LocalDate customDate = customReminderDatePicker.getValue();
                    if (customDate == null) {
                        addReminderErrorLabel.setText("Please select a custom date.");
                        return;
                    }
                    reminderDate = customDate.atStartOfDay();
                }
            }

            if (reminderDate == null) {
                addReminderErrorLabel.setText("Could not determine a valid reminder date.");
                return;
            }

            if (reminderDate.isBefore(LocalDateTime.now())) {
                // Instead of a pop-up, just set the error label
                addReminderErrorLabel.setText("Selected reminder date is in the past.");
                return;
            }

            // If we reach here, we have a valid reminderDate
            Reminder newReminder = new Reminder(
                    currentTaskForReminder.getTitle(),
                    reminderDate,
                    selectedType
            );
            currentTaskForReminder.addReminder(newReminder);

            System.out.println("Added reminder: " + newReminder);
            closeNotificationOverlay();

        } catch (Exception e) {
            // Catch any unforeseen errors and display them in-line
            addReminderErrorLabel.setText("Error: " + e.getMessage());
        }
    }

    
    @FXML
    private void closeNotificationOverlay() {
        notificationOverlayPane.setVisible(false);
        currentTaskForReminder = null;
    }
    
    
   
    // Overloaded method: Open overlay for adding a new task
    @FXML
    private void openAddTaskOverlay() {
        // Clear all fields to prepare for adding a new task
        taskTitleField.clear();
        taskDescriptionField.clear();
        taskDeadlinePicker.setValue(null);
        PriorityChoiceBox.setValue(Priority.valueOf("DEFAULT"));
        categoryChoiceBox.setValue(Categories.valueOf("Default"));

        // Ensure no task is being modified
        overlayPane.setUserData(null);

        // Show the overlay
        overlayPane.setVisible(true);
        overlayPane.toFront();
    }

    // Overloaded method: Open overlay for modifying an existing task
    private void openAddTaskOverlay(Task task) {

        taskTitleField.setText(task.getTitle());
        taskDescriptionField.setText(task.getDescription());
        taskDeadlinePicker.setValue(task.getDeadline().toLocalDate());
        PriorityChoiceBox.setValue(task.getPriority());
        categoryChoiceBox.setValue(task.getCategory());

        // Save the task being modified in the overlay's user data
        overlayPane.setUserData(task);

        // Show the overlay
        overlayPane.setVisible(true);
        overlayPane.toFront();
    }
    @FXML
    private void submitTaskForm() {
        // 1) Collect form data
        String title = taskTitleField.getText();
        String description = taskDescriptionField.getText();
        LocalDateTime deadline = (taskDeadlinePicker.getValue() != null)
                ? LocalDateTime.of(taskDeadlinePicker.getValue(), LocalTime.MIDNIGHT)
                : LocalDateTime.now();
    
        Priority priority = PriorityChoiceBox.getValue();
        Categories newCategory = categoryChoiceBox.getValue();
        Status status = Status.OPEN;
    
        // 2) Retrieve the task from overlay's user data (null means new)
        Task existingTask = (Task) overlayPane.getUserData();
    
        if (existingTask == null) {
            // ========== CREATE A NEW TASK ==========
            Task newTask = new Task(title, description, deadline, priority, newCategory,status);
            tasks.add(newTask);

        } else {
            // ========== MODIFY AN EXISTING TASK ==========

            existingTask.setTitle(title);
            existingTask.setDescription(description);
            existingTask.setDeadline(deadline);
            existingTask.setPriority(priority);
            existingTask.setCategory(newCategory);

            if (existingTask.getStatus() == Status.DELAYED && deadline.isAfter(LocalDateTime.now())) {
                existingTask.setStatus(Status.OPEN);
            }
            
    
            if (!tasks.contains(existingTask)) {
                // Add the task to the list if not already present
                tasks.add(existingTask);
            }
            // Clear the stored task reference
            overlayPane.setUserData(null);
        }
    
        // 3) Refresh UI
        updateTaskContainer(Categories.valueOf("All"), Priority.valueOf("ALL"), "");
        priorityChoiceBoxMain.setValue(Priority.valueOf("ALL"));
        categoryChoiceBoxMain.setValue(Categories.valueOf("All"));

        closeOverlay();
    }    
    

    @FXML
    private void closeOverlay() {
        overlayPane.setVisible(false);

        // Optionally clear form fields
        taskTitleField.clear();
        taskDescriptionField.clear();
        taskDeadlinePicker.setValue(null);
        PriorityChoiceBox.setValue(Priority.valueOf("DEFAULT"));
        categoryChoiceBox.setValue(Categories.valueOf("All"));
    }

    @FXML
    private void handleAddCategory() {
        String newCategoryName = newCategoryField.getText().trim();

        if (!newCategoryName.isEmpty()) {
            // Dynamically add or retrieve the category (if it exists, we get the existing one)
            Categories.addCategory(newCategoryName);

            // Refresh the ChoiceBoxes and ListView with the latest categories
            updateCategoryChoiceBoxes();
            // Refresh the categoriesListView, excluding "All"
            categoriesListView.setItems(
                FXCollections.observableArrayList(Categories.values())
                    .filtered(c -> !c.equals(Categories.valueOf("All")) && !c.equals(Categories.valueOf("Default")))
            );
            // Clear the text field for next input
            newCategoryField.clear();
        }
    }

    @FXML
    private void openManageCategoriesOverlay(ActionEvent event) {
        manageCategoriesOverlayPane.setVisible(true);
        manageCategoriesOverlayPane.toFront();
    }

    @FXML
    private void closeManageCategoriesOverlay(ActionEvent event) {
        manageCategoriesOverlayPane.setVisible(false);
    }

    public List<Task> getCurrentTasks() {
        // Return a copy of the tasks list to ensure encapsulation
        return new ArrayList<>(tasks);
    }

    public List<Categories> getCurrentCategories() {
        // Retrieve all currently loaded categories from the static Categories class
        Categories[] categoryArray = Categories.values();
        
        // Convert the array to a List and return
        return new ArrayList<>(FXCollections.observableArrayList(categoryArray));
    }


    public List<Priority> getCurrentPriorities() {
        // Retrieve all currently loaded categories from the static Categories class
        Priority[] prioritiesArray = Priority.values();
        
        // Convert the array to a List and return
        return new ArrayList<>(FXCollections.observableArrayList(prioritiesArray));
    }

    @FXML
    private void goToNotifications() {
        try {
            Stage stage = (Stage) categoryChoiceBoxMain.getScene().getWindow();
    
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/resources/notifications.fxml"));
            Parent notificationsRoot = loader.load();
    
            Scene notificationsScene;
            if (!stage.isFullScreen()) {
                notificationsScene = new Scene(notificationsRoot, stage.getWidth(), stage.getHeight());
            } else {
                notificationsScene = new Scene(notificationsRoot);
            }
    
            stage.setScene(notificationsScene);
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    

    // /**
    //  * Fetches all reminders from tasks (this method should collect reminders from all tasks).
    //  */
    // private List<Reminder> fetchAllReminders() {
    //     return tasks.stream()
    //                 .flatMap(task -> task.getReminders().stream())
    //                 .toList();
    // }


}