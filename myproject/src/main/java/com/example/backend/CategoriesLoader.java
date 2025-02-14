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


    private static List<String> parseCategoriesFromJSON(String jsonContent) {
        List<String> categories = new ArrayList<>();
        try {

            JSONArray jsonArray = new JSONArray(jsonContent);

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
