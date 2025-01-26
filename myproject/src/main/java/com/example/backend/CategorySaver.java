package com.example.backend;

import org.json.JSONArray;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CategorySaver {

    /**
     * Saves the list of categories to a JSON file.
     *
     * @param categories the list of categories to save
     * @param filePath   the file path where to save the categories JSON
     */
    public static void saveCategoriesToJSON(List<Categories> categories, String filePath) {
        // Create a JSON array to store category names
        JSONArray jsonArray = new JSONArray();
        for (Categories category : categories) {
            jsonArray.put(category.toString());
        }

        // Write the JSON array to the file
        try (FileWriter fileWriter = new FileWriter(filePath)) {
            fileWriter.write(jsonArray.toString(4)); // Pretty print with indent factor 4
            System.out.println("Categories saved successfully to " + filePath);
        } catch (IOException e) {
            System.err.println("Error saving categories to file: " + filePath);
            e.printStackTrace();
        }
    }
}
