package com.example.backend;

import org.json.JSONArray;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CategoriesLoader {

    /**
     * Loads categories from a JSON file.
     *
     * @param filePath the path to the JSON file containing categories
     * @return a list of category names
     */
    public static List<String> loadCategoriesFromJSONFile(String filePath) {
        List<String> categories = new ArrayList<>();
        try (FileInputStream fileInputStream = new FileInputStream(new File(filePath))) {
            // Read the FileInputStream into a String
            String jsonContent = new Scanner(fileInputStream, StandardCharsets.UTF_8)
                    .useDelimiter("\\A") // Read the entire file
                    .next();

            // Parse the JSON array and extract category names
            categories = parseCategoriesFromJSON(jsonContent);
        } catch (IOException e) {
            System.err.println("Error reading categories from file: " + filePath);
            e.printStackTrace();
        }
        return categories;
    }

    /**
     * Parses a JSON string representing an array of categories.
     *
     * @param jsonContent the JSON string
     * @return a list of category names
     */
    private static List<String> parseCategoriesFromJSON(String jsonContent) {
        List<String> categories = new ArrayList<>();
        try {
            // Convert the JSON string into a JSONArray
            JSONArray jsonArray = new JSONArray(jsonContent);

            // Extract each element as a String
            for (int i = 0; i < jsonArray.length(); i++) {
                categories.add(jsonArray.getString(i));
            }
        } catch (Exception e) {
            System.err.println("Error parsing categories JSON: " + jsonContent);
            e.printStackTrace();
        }
        return categories;
    }
}
