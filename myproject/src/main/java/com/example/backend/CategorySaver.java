package com.example.backend;

import org.json.JSONArray;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CategorySaver {


    public static void saveCategoriesToJSON(List<Categories> categories, String filePath) {
        // Create a JSON array to store category names
        JSONArray jsonArray = new JSONArray();
        for (Categories category : categories) {
            jsonArray.put(category.toString());
        }

        try (FileWriter fileWriter = new FileWriter(filePath)) {
            fileWriter.write(jsonArray.toString(4)); // Pretty print with indent factor 4
            System.out.println("Categories saved successfully to " + filePath);
        } catch (IOException e) {
            System.err.println("Error saving categories to file: " + filePath);
            e.printStackTrace();
        }
    }
}
