package com.example.backend;

import org.json.JSONArray;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class PrioritySaver {


    public static void savePrioritiesToJSON(List<Priority> priorities, String filePath) {
        // Create a JSON array to store priority names
        JSONArray jsonArray = new JSONArray();
        for (Priority priority : priorities) {
            jsonArray.put(priority.toString());
            System.out.println(priority);
        }

        // Write the JSON array to the file
        try (FileWriter fileWriter = new FileWriter(filePath)) {
            fileWriter.write(jsonArray.toString(4)); 
            System.out.println("Priorities saved successfully to " + filePath);
        } catch (IOException e) {
            System.err.println("Error saving priorities to file: " + filePath);
            e.printStackTrace();
        }
    }
}
