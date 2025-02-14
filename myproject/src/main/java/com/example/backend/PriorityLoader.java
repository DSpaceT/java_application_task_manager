package com.example.backend;

import org.json.JSONArray;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class PriorityLoader {


    public static List<String> loadPrioritiesFromJSONFile(String filePath) {
        List<String> priorities = new ArrayList<>();
        try (FileInputStream fileInputStream = new FileInputStream(new File(filePath))) {
            // Read the FileInputStream into a String
            String jsonContent = new Scanner(fileInputStream, StandardCharsets.UTF_8)
                    .useDelimiter("\\A") 
                    .next();

            // Parse the JSON array and extract priority names
            priorities = parsePrioritiesFromJSON(jsonContent);
        } catch (IOException e) {
            System.err.println("Error reading priorities from file: " + filePath);
            e.printStackTrace();
        }
        return priorities;
    }

    private static List<String> parsePrioritiesFromJSON(String jsonContent) {
        List<String> priorities = new ArrayList<>();
        try {
            // Convert the JSON string into a JSONArray
            JSONArray jsonArray = new JSONArray(jsonContent);

            // Extract each element as a String
            for (int i = 0; i < jsonArray.length(); i++) {
                priorities.add(jsonArray.getString(i));
                System.out.println(jsonArray.getString(i));
            }
        } catch (Exception e) {
            System.err.println("Error parsing priorities JSON: " + jsonContent);
            e.printStackTrace();
        }
        return priorities;
    }
}
