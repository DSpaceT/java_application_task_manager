package com.example.backend;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class Categories {
    private final String name;

    // Internally store all known Categories by name
    private static final Map<String, Categories> CONSTANTS = new LinkedHashMap<>();

    /**
     * Private constructor ensures no one can create new Categories
     * without going through the logic below.
     */
    private Categories(String name) {
        this.name = name;
        CONSTANTS.put(name, this);
    }

    /**
     * Static block to initialize categories from a JSON file.
     * This replaces the need for hardcoded categories.
     */
    static {
        String filePath = "/home/dimitrios-georgoulopoulos/Desktop/java_project_v2/myproject/medialab/categories.json";

        try {
            // Load categories from the JSON file
            List<String> loadedCategories = CategoriesLoader.loadCategoriesFromJSONFile(filePath);

            // Dynamically add categories to the CONSTANTS map
            for (String categoryName : loadedCategories) {
                if (!CONSTANTS.containsKey(categoryName)) {
                    new Categories(categoryName); // Adds the category to CONSTANTS
                }
            }

            // Ensure at least "All" exists
            if (!CONSTANTS.containsKey("All")) {
                new Categories("All");
            }

        } catch (Exception e) {
            System.err.println("Error loading categories from file: " + filePath);
            e.printStackTrace();
            // Add a fallback default category if loading fails
            new Categories("Default");
        }
    }

    /**
     * Mimics the enum 'valueOf(String)' method.
     * @throws IllegalArgumentException if not found (just like real enums)
     */
    public static Categories valueOf(String name) {
        Categories constant = CONSTANTS.get(name);
        if (constant == null) {
            throw new IllegalArgumentException(
                "No enum constant " + Categories.class.getName() + "." + name
            );
        }
        return constant;
    }

    /**
     * Mimics the enum 'values()' method.
     * Returns an array of all known "constants."
     */
    public static Categories[] values() {
        return CONSTANTS.values().toArray(new Categories[0]);
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
        if (obj instanceof Categories) {
            return Objects.equals(this.name, ((Categories) obj).name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    /**
     * Dynamically registers a new "enum constant."
     * If a category with this name already exists, returns the existing one.
     */
    public static Categories addCategory(String newName) {
        if (CONSTANTS.containsKey(newName)) {
            return CONSTANTS.get(newName); // Already exists
        }
        return new Categories(newName);
    }

    /**
     * Removes a "constant" by name (except "All").
     * Returns true if removed, false otherwise.
     */
    public static boolean removeCategory(String name) {
        if (name.equals("All")) {
            return false; // Disallow removing "All"
        }
        return (CONSTANTS.remove(name) != null);
    }

        /**
     * Modifies the name of an existing category.
     *
     * @param oldName the old category name
     * @param newName the new category name
     * @return the modified category
     */
    public static Categories modifyCategoryName(String oldName, String newName) {
        if (!CONSTANTS.containsKey(oldName)) {
            throw new IllegalArgumentException("Category with name " + oldName + " does not exist.");
        }

        if (CONSTANTS.containsKey(newName)) {
            throw new IllegalArgumentException("Category with name " + newName + " already exists.");
        }

        Categories oldCategory = CONSTANTS.remove(oldName);
        Categories newCategory = new Categories(newName);

        // Reassign all tasks to the new category name (done externally in TaskUtils)
        return newCategory;
    }
}
