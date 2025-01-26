package com.example.backend;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class Priority {
    private final String name;

    // Internally store all known priorities by name
    private static final Map<String, Priority> CONSTANTS = new LinkedHashMap<>();

    /**
     * Private constructor ensures no one can create new priorities
     * without going through the logic below.
     */
    private Priority(String name) {
        this.name = name;
        CONSTANTS.put(name, this);
    }

    /**
     * Static block to initialize the priorities.
     */
    static {
        String filePath = "/home/dimitrios-georgoulopoulos/Desktop/java_project_v2/myproject/medialab/priorities.json"; // Replace with your actual path

        try {
            // Load priorities from the JSON file
            List<String> loadedPriorities = PriorityLoader.loadPrioritiesFromJSONFile(filePath);

            // Dynamically add priorities to the CONSTANTS map
            for (String priorityName : loadedPriorities) {
                if (!CONSTANTS.containsKey(priorityName)) {
                    new Priority(priorityName); // Adds the priority to CONSTANTS
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

    /**
     * Mimics the enum 'valueOf(String)' method.
     * @throws IllegalArgumentException if not found (just like real enums)
     */
    public static Priority valueOf(String name) {
        Priority constant = CONSTANTS.get(name);
        if (constant == null) {
            throw new IllegalArgumentException(
                "No enum constant " + Priority.class.getName() + "." + name
            );
        }
        return constant;
    }

    /**
     * Mimics the enum 'values()' method.
     * Returns an array of all known "constants."
     */
    public static Priority[] values() {
        return CONSTANTS.values().toArray(new Priority[0]);
    }

    /**
     * For debugging or showing in UI controls.
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Typical equality method, comparing 'name' only.
     */
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

    /**
     * Dynamically registers a new "enum constant."
     * If a priority with this name already exists, returns the existing one.
     */
    public static Priority addPriority(String newName) {
        if (CONSTANTS.containsKey(newName)) {
            return CONSTANTS.get(newName); // Already exists
        }
        return new Priority(newName);
    }

    /**
     * Removes a "constant" by name (except default priorities).
     * Returns true if removed, false otherwise.
     */
    public static boolean removePriority(String name) {
        if (name.equals("DEFAULT")) {
            return false; // Disallow removing default priorities
        }
        return (CONSTANTS.remove(name) != null);
    }
}



