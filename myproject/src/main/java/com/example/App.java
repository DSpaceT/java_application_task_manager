
package com.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.example.backend.TasksSaver;
import com.example.backend.CategorySaver;  // Import CategorySaver
import com.example.backend.PrioritySaver;
import com.example.backend.Task;
import com.example.backend.TaskLoader;
import com.example.backend.Categories;
import com.example.frontend.TaskController;
import com.example.backend.Priority;

import java.io.File;
import java.util.List;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            String filePath = "/home/dimitrios-georgoulopoulos/Desktop/java_project_v2/myproject/medialab/tasks.json";
            List<Task> loadedTasks = TaskLoader.loadTasksFromJSONFile(filePath);

        // Set the static tasks list in TaskController
            TaskController.setTasks(loadedTasks);
            // Load the FXML file for the main view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/resources/TaskManagerView.fxml"));
            Parent root = loader.load();

            // Print file properties for debugging
            File file = new File("/home/dimitrios-georgoulopoulos/Desktop/java_project_v2/myproject/medialab/tasks.json");
            System.out.println("File exists: " + file.exists());
            System.out.println("Can read: " + file.canRead());
            System.out.println("Can write: " + file.canWrite());
            System.out.println("Is file: " + file.isFile());

            // Get the controller
            TaskController taskController = loader.getController();
            
 
            // Set the scene and stage
            Scene scene = new Scene(root);
            primaryStage.setTitle("Task Manager");
            primaryStage.setScene(scene);

            primaryStage.setWidth(800);
            primaryStage.setHeight(600);
            primaryStage.centerOnScreen(); // Optionally center the stage
            primaryStage.setResizable(true);

            // File paths
            String tasksFilePath = "/home/dimitrios-georgoulopoulos/Desktop/java_project_v2/myproject/medialab/tasks.json";
            String categoriesFilePath = "/home/dimitrios-georgoulopoulos/Desktop/java_project_v2/myproject/medialab/categories.json";
            String prioritiesFilePath = "/home/dimitrios-georgoulopoulos/Desktop/java_project_v2/myproject/medialab/priorities.json";

            // Add close event handler to save tasks and categories when window is closed
            primaryStage.setOnCloseRequest(event -> {
                try {
                    // Save tasks
                    List<Task> currentTasks = taskController.getCurrentTasks();
                    System.out.println(currentTasks);
                    TasksSaver.saveTasksToJSON(currentTasks, tasksFilePath);

                    // Save categories
                    List<Categories> currentCategories = taskController.getCurrentCategories();
                    CategorySaver.saveCategoriesToJSON(currentCategories, categoriesFilePath);

                    // Save priorities
                    List<Priority> currentPriorities = taskController.getCurrentPriorities();
                    PrioritySaver.savePrioritiesToJSON(currentPriorities, prioritiesFilePath);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
