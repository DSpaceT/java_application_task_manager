package com.example.frontend;

import java.io.File;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import com.example.backend.Task;
import com.example.backend.TaskLoader;
import com.example.backend.TaskSummary;
import com.example.backend.TaskUtils;
import com.example.backend.Priority;
import com.example.backend.Reminder;
import com.example.backend.ReminderType;
import com.example.backend.Categories;
import com.example.backend.Status;

import javafx.util.Callback;
import javafx.util.Duration;

public class TaskController {

    // --------------------------- Main Tasks Section ---------------------------
    @FXML private VBox taskContainer;

    // --------------------------- Add Task Overlay -----------------------------
    @FXML private StackPane overlayPane; 
    @FXML private TextField taskTitleField;
    @FXML private TextArea taskDescriptionField;
    @FXML private DatePicker taskDeadlinePicker;
    @FXML private ComboBox<Priority> PriorityChoiceBox;

    // ComboBox used to pick a category in the "Add Task" form
    @FXML private ComboBox<Categories> categoryChoiceBox;

    // --------------------------- Main Category Filter -------------------------
    @FXML private ChoiceBox<Categories> categoryChoiceBoxMain;
    @FXML private ChoiceBox<Priority> priorityChoiceBoxMain;

    // ---------------------- Manage Categories Overlay -------------------------
    @FXML private StackPane manageCategoriesOverlayPane;
    @FXML private ListView<Categories> categoriesListView;
    @FXML private TextField newCategoryField;

    // ---------------------- Manage Priorities Overlay -------------------------
    @FXML private ListView<Priority> prioritiesListView;   // <--- must be Priority, not Categories
    @FXML private TextField newpriorityField;


    // If you have a VBox for managing categories in-place (legacy UI)
    @FXML private VBox manageCategoriesSection;

    @FXML private TextField titleFilterField;

    @FXML private StackPane notificationOverlayPane;
    @FXML private ComboBox<ReminderType> reminderTypeChoiceBox;
    @FXML private DatePicker customReminderDatePicker;
    
    private Task currentTaskForReminder;


    // // --------------------------- Data Structures ------------------------------
    // private Map<Categories, List<Task>> categorizedTasks = new HashMap<>();
    private static List<Task> tasks = new ArrayList<>();

    @FXML
    private Label notificationIndicator;

    private Timeline reminderChecker;

    @FXML private Label lblTotalTasks;
    @FXML private Label lblCompletedTasks;
    @FXML private Label lblDelayedTasks;
    @FXML private Label lblDue7Days;

    // ============================= Initialization =============================
    @FXML
    private void initialize() {
        // 1) Setup the main category filter
        startReminderChecker();

        setupCategoryChoiceBoxMain();
        setupPriorityChoiceBoxMain();

        // 2) Setup the category ComboBox used inside the add-task form
        setupCategoryChoiceBoxForForm();

        // 3) Setup task priority choice box
        setupTaskPriorityChoiceBox();


        // // 4) Load tasks from JSON
        // loadTasks();

        // 5) Initialize the ListView for managing categories
        initCategoriesListView();

        initPrioritiesListView();

        updateTaskContainer(Categories.valueOf("All"),Priority.valueOf("ALL"),null);
    }

    private void refreshTaskSummary() {
        // 1) Get the summary
        TaskSummary summary = TaskUtils.getTaskSummary(tasks);

        // 2) Update the labels
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
    
        // Refresh task container to highlight overdue reminders
        updateTaskContainer(Categories.valueOf("All"), Priority.valueOf("ALL"), null);
    
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
    

    // ---------------------- Setup Category Filter (MAIN) ----------------------
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
        Categories selectedCategory = categoryChoiceBoxMain.getValue();
        Priority selectedPriority = priorityChoiceBoxMain.getValue();
        String titleFilter = titleFilterField.getText().toLowerCase(); // Get title filter text
    
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

    // ---------------------- Load tasks from JSON ------------------------------
    private void loadTasks() {
        String filePath = "/home/dimitrios-georgoulopoulos/Desktop/java_project_v2/myproject/medialab/tasks.json";
        System.out.println("Looking for tasks.json at: " + filePath);

        File file = new File(filePath);
        if (file.exists() && file.canRead()) {
            System.out.println("File exists and is readable!");
            tasks = TaskLoader.loadTasksFromJSONFile(filePath);

            updateTaskContainer(Categories.valueOf("All"),Priority.valueOf("ALL"),null);
        } else {
            System.out.println("File does not exist or is not readable!");
        }
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
        // Step 1: Modify the category name
        try {
            Categories newCategory = Categories.modifyCategoryName(oldCategory.toString(), newCategoryName);
    
            // Step 2: Update tasks associated with the old category
            tasks = TaskUtils.updateTasksCategory(tasks, oldCategory, newCategory);
    
            System.out.println("Updated category: " + oldCategory + " to " + newCategoryName);
    
            // Step 3: Refresh the categoriesListView, excluding "All" and "Default"
            categoriesListView.setItems(
                FXCollections.observableArrayList(Categories.values())
                    .filtered(c -> !c.equals(Categories.valueOf("All")) && !c.equals(Categories.valueOf("Default")))
            );
    
            // Step 4: Update the UI with the new category
            updateTaskContainer(Categories.valueOf("All"), Priority.valueOf("ALL"), "");
    
            // Step 5: Update dropdowns for categories
            updateCategoryChoiceBoxes();
    
        } catch (IllegalArgumentException ex) {
            System.err.println("Error updating category: " + ex.getMessage());
            // Optionally, show an error dialog to the user
        }
    }
    
    private void initPrioritiesListView() {
        // Show all priorities except "DEFAULT" and "ALL"
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
    
                // Modify button logic
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
    
                // Remove button logic
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
            // Step 1: Modify the priority name
            Priority newPriority = Priority.addPriority(newPriorityName);
    
            // Step 2: Update tasks associated with the old priority
            tasks = TaskUtils.updateTasksPriority(tasks, oldPriority, newPriority);
    
            // Step 3: Remove the old priority
            Priority.removePriority(oldPriority.toString());
    
            System.out.println("Updated priority: " + oldPriority + " to " + newPriorityName);
    
            // Step 4: Refresh the prioritiesListView
            prioritiesListView.setItems(
                FXCollections.observableArrayList(Priority.values())
                    .filtered(p -> !p.equals(Priority.valueOf("DEFAULT")) && !p.equals(Priority.valueOf("ALL")))
            );
            // Step 4: Update the UI with the new category
            updateTaskContainer(Categories.valueOf("All"), Priority.valueOf("ALL"), "");


            // Step 5: Update dropdowns or other UI elements
            updatePriorityChoiceBoxes();
    
        } catch (IllegalArgumentException ex) {
            System.err.println("Error updating priority: " + ex.getMessage());
            // Optionally, show an error dialog to the user
        }
    }
        
    /**
     * Reassigns tasks from the given priority to DEFAULT, 
     * removes the priority from the internal list, 
     * and refreshes the UI (but does NOT remove the tasks).
     *
     * @param priority the priority you want to reassign to DEFAULT
     */
    private void defaultPriorityAndTasks(Priority priority) {
        // Prevent removing or reassigning DEFAULT itself
        if (priority == Priority.valueOf("DEFAULT")) {
            System.out.println("Cannot reassign DEFAULT priority to itself.");
            return;
        }

        // 1) Reassign all tasks having this priority to DEFAULT
        tasks = TaskUtils.setTasksToDefaultPriority(tasks, priority);

        // 2) Remove the old priority from the internal map/enum
        boolean removed = Priority.removePriority(priority.toString());
        if (removed) {
            System.out.println("Removed priority from the internal list: " + priority);
        }

        // 3) Refresh the prioritiesListView to exclude DEFAULT and ALL
        prioritiesListView.setItems(
            FXCollections.observableArrayList(Priority.values())
                .filtered(p -> !p.equals(Priority.valueOf("DEFAULT")) && !p.equals(Priority.valueOf("ALL")))
        );

        // 4) Update UI with default filters
        updateTaskContainer(Categories.valueOf("All"), Priority.valueOf("ALL"), null);

        // 5) Update any priority dropdowns
        updatePriorityChoiceBoxes();

        System.out.println("All tasks that had priority " + priority 
            + " have now been reassigned to DEFAULT priority.");
    }


    private void removeCategoryAndTasks(Categories category) {
        tasks = TaskUtils.excludeTasks(tasks, category, null);


        // 1) Remove the category from the "fake enum" map:
        boolean removed = Categories.removeCategory(category.toString());
        if (removed) {
            System.out.println("Removed category from the internal list: " + category);
        }

        // Refresh the categoriesListView, excluding "All"
        categoriesListView.setItems(
            FXCollections.observableArrayList(Categories.values())
                .filtered(c -> !c.equals(Categories.valueOf("All")) && !c.equals(Categories.valueOf("Default")))
        );

        // 3) Update the UI (default to show "All" tasks)
        updateTaskContainer(Categories.valueOf("All"), Priority.valueOf("ALL"), "");

        // 4) Update the dropdowns
        updateCategoryChoiceBoxes();
    }

    
    @FXML
    private void handleAddPriority() {
        String newPriorityName = newpriorityField.getText().trim();

        if (!newPriorityName.isEmpty()) {
            // Dynamically add or retrieve the priority (if it exists, we get the existing one)
            Priority.addPriority(newPriorityName);

            // Refresh the ListView with the latest Priority values
            // prioritiesListView.setItems(FXCollections.observableArrayList(Priority.values()));
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
        // Use the items in the prioritiesListView to populate the PriorityChoiceBox
        ObservableList<Priority> filteredPriorities = prioritiesListView.getItems();
    
        PriorityChoiceBox.setItems(filteredPriorities);
        PriorityChoiceBox.setValue(filteredPriorities.isEmpty() ? null : filteredPriorities.get(0));
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
    }


    private HBox createTaskBox(Task task) {
        // Check if the deadline is in the past --> DELAYED
        LocalDateTime now = LocalDateTime.now();
        if (task.getDeadline().isBefore(now)) {
            task.setStatus(Status.DELAYED);
        }

        // Create the container for the task box
        HBox taskBox = new HBox(20); // Spacing between details and buttons
        taskBox.setAlignment(Pos.CENTER_LEFT);
        taskBox.setStyle("-fx-padding: 10; -fx-border-color: lightgray;"
            + " -fx-border-width: 1; -fx-background-color: #f9f9f9;");

        // Task details (left side)
        Label titleLabel = new Label("Title: " + task.getTitle());
        Label descriptionLabel = new Label("Description: " + task.getDescription());
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(300);

        Label deadlineLabel = new Label(
            "Deadline: " + task.getDeadline().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        );
        Label priorityLabel = new Label("Priority: " + task.getPriority());
        Label categoryLabel = new Label("Category: " + task.getCategory());
        Label statusLabel = new Label("Status: " + task.getStatus());

        VBox detailsBox = new VBox(5, titleLabel, descriptionLabel, deadlineLabel,
                                priorityLabel, categoryLabel, statusLabel);
        detailsBox.setAlignment(Pos.TOP_LEFT);
        taskBox.getChildren().add(detailsBox);

        // Buttons container (right side)
        HBox buttonsBox = new HBox(10); // Spacing between buttons
        buttonsBox.setAlignment(Pos.CENTER_LEFT);

        // ------------------------------
        // 1) Conditionally create ComboBox if not DELAYED
        // ------------------------------
        final ComboBox<Status> statusComboBox;
        if (task.getStatus() != Status.DELAYED) {
            statusComboBox = new ComboBox<>();
            statusComboBox.getItems().addAll(Status.values());
            statusComboBox.setPromptText("Status");
            
            // Prevent the ComboBox from pushing buttons to shrink
            statusComboBox.setPrefWidth(100);

            // Update task status upon selection
            statusComboBox.setOnAction(event -> {
                Status selectedStatus = statusComboBox.getValue();
                if (selectedStatus != null) {
                    task.setStatus(selectedStatus);
                    statusLabel.setText("Status: " + task.getStatus());

                    // If status becomes DELAYED, remove the ComboBox
                    if (selectedStatus == Status.DELAYED) {
                        buttonsBox.getChildren().remove(statusComboBox);
                    }
                    // Optionally save tasks here
                }
            });
        } else {
            statusComboBox = null;
        }

        // ------------------------------
        // 2) Create Buttons with minimum widths
        // ------------------------------
        Button deleteButton = new Button("Delete");
        deleteButton.setMinWidth(60);
        deleteButton.setOnAction(event -> {
            taskContainer.getChildren().remove(taskBox);
            tasks.remove(task);
            // Optionally save tasks here
        });

        Button modifyButton = new Button("Modify");
        modifyButton.setMinWidth(60);
        modifyButton.setOnAction(event -> openAddTaskOverlay(task));

        Button toggleStatusButton = new Button("Toggle Status");
        toggleStatusButton.setMinWidth(90); // a bit wider for the text
        toggleStatusButton.setOnAction(event -> {
            if (task.getDeadline().isBefore(LocalDateTime.now())) {
                task.setStatus(Status.DELAYED);
            } else {
                switch (task.getStatus()) {
                    case OPEN -> task.setStatus(Status.IN_PROGRESS);
                    case IN_PROGRESS -> task.setStatus(Status.COMPLETED);
                    case COMPLETED -> task.setStatus(Status.IN_PROGRESS);
                    case POSTPONED, DELAYED -> task.setStatus(Status.IN_PROGRESS);
                }
            }
            statusLabel.setText("Status: " + task.getStatus());

            // If status becomes DELAYED, remove the ComboBox if it exists
            if (task.getStatus() == Status.DELAYED && statusComboBox != null) {
                buttonsBox.getChildren().remove(statusComboBox);
            }
            refreshTaskSummary();
            // Optionally save tasks here
        });

        Button notifyButton = new Button("Notify");
        notifyButton.setMinWidth(60);
        notifyButton.setOnAction(event -> openAddNotificationOverlay(task));

        // ------------------------------
        // 3) Add buttons to the HBox
        // ------------------------------
        buttonsBox.getChildren().addAll(deleteButton, modifyButton, toggleStatusButton, notifyButton);

        // If ComboBox exists (not DELAYED), add it last
        if (statusComboBox != null) {
            buttonsBox.getChildren().add(statusComboBox);
        }

        // Add the buttons box to the main task box
        taskBox.getChildren().add(buttonsBox);

        // Overdue reminders
        Label overdueReminderLabel = new Label();
        overdueReminderLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");

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
        if (currentTaskForReminder == null) return;
    
        ReminderType selectedType = reminderTypeChoiceBox.getValue();
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
                    reminderDate = LocalDateTime.now().plusSeconds(5); // Add 5 seconds to the current time
                case CUSTOM_DATE -> {
                    if (customReminderDatePicker.getValue() == null) {
                        showError("Please select a custom date.");
                        return;
                    }
                    reminderDate = customReminderDatePicker.getValue().atStartOfDay();
                }
            }
    
            if (reminderDate == null || reminderDate.isBefore(LocalDateTime.now())) {
                showError("Selected reminder date is invalid or in the past.");
                return;
            }
    
            Reminder newReminder = new Reminder(
                currentTaskForReminder.getTitle(), 
                reminderDate, 
                selectedType
            );
            currentTaskForReminder.addReminder(newReminder);
    
            System.out.println("Added reminder: " + newReminder);
            closeNotificationOverlay();
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }
    
    @FXML
    private void closeNotificationOverlay() {
        notificationOverlayPane.setVisible(false);
        currentTaskForReminder = null;
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.showAndWait();
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
        // Pre-fill the overlay with the task's current details
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
    
            if (!tasks.contains(existingTask)) {
                // Add the task to the list if not already present
                tasks.add(existingTask);
            }
            // Clear the stored task reference
            overlayPane.setUserData(null);
        }
    
        // 3) Refresh UI
        updateTaskContainer(Categories.valueOf("All"), Priority.valueOf("ALL"), "");

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


        // ---------------------- Expose Current Categories ----------------
    public List<Categories> getCurrentCategories() {
        // Retrieve all currently loaded categories from the static Categories class
        Categories[] categoryArray = Categories.values();
        
        // Convert the array to a List and return
        return new ArrayList<>(FXCollections.observableArrayList(categoryArray));
    }


    // ---------------------- Expose Current Categories ----------------
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
    
            // Remove or comment out the following line:
            // NotificationsController notificationsController = loader.getController();
            // notificationsController.setReminders(fetchAllReminders());
    
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
    
    

    /**
     * Fetches all reminders from tasks (this method should collect reminders from all tasks).
     */
    private List<Reminder> fetchAllReminders() {
        return tasks.stream()
                    .flatMap(task -> task.getReminders().stream())
                    .toList();
    }

    

    

}