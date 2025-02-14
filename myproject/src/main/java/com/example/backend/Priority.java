package com.example.backend;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class Priority {
    private final String name;

    // Internally store all known priorities by name
    private static final Map<String, Priority> CONSTANTS = new LinkedHashMap<>();

    private Priority(String name) {
        this.name = name;
        CONSTANTS.put(name, this);
    }

    static {
        String filePath = "/home/dimitrios-georgoulopoulos/Desktop/java_project_v2/myproject/medialab/priorities.json"; // Replace with your actual path

        try {
            // Load priorities from the JSON file
            List<String> loadedPriorities = PriorityLoader.loadPrioritiesFromJSONFile(filePath);

            // Dynamicaaally add priorities 
            for (String priorityName : loadedPriorities) {
                if (!CONSTANTS.containsKey(priorityName)) {
                    new Priority(priorityName); 
                }
            }

        } catch (Exception e) {
            System.err.println("Error loading priorities from file: " + filePath);
            e.printStackTrace();

            // Add fallback default priorities if loading fails
            // new Priority("HIGH");
            // new Priority("MEDIUM");
            // new Priority("LOW");
            new Priority("DEFAULT");
            new Priority("ALL");

        }
        System.out.println("Loaded priorities: " + CONSTANTS.keySet());
    }

 
    public static Priority valueOf(String name) {
        Priority constant = CONSTANTS.get(name);
        if (constant == null) {
            throw new IllegalArgumentException(
                "No enum constant " + Priority.class.getName() + "." + name
            );
        }
        return constant;
    }


    public static Priority[] values() {
        return CONSTANTS.values().toArray(new Priority[0]);
    }

 
    @Override
    public String toString() {
        return name;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof Priority) {
            return Objects.equals(this.name, ((Priority) obj).name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }


    public static Priority addPriority(String newName) {
        if (CONSTANTS.containsKey(newName)) {
            return CONSTANTS.get(newName); 
        }
        return new Priority(newName);
    }

    public static boolean removePriority(String name) {
        if (name.equals("DEFAULT")) {
            return false; 
        }
        return (CONSTANTS.remove(name) != null);
    }
}



