package com.example.backend;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class Categories {
    private final String name;

    private static final Map<String, Categories> CONSTANTS = new LinkedHashMap<>();


    private Categories(String name) {
        this.name = name;
        CONSTANTS.put(name, this);
    }

    static {
        String filePath = "/home/dimitrios-georgoulopoulos/Desktop/java_project_v2/myproject/medialab/categories.json";

        try {
            List<String> loadedCategories = CategoriesLoader.loadCategoriesFromJSONFile(filePath);

            // Dynamically add categories to the CONSTANTS map
            for (String categoryName : loadedCategories) {
                if (!CONSTANTS.containsKey(categoryName)) {
                    new Categories(categoryName);
                }
            }

            // Ensure at least "All" exists
            if (!CONSTANTS.containsKey("All")) {
                new Categories("All");
            }

        } catch (Exception e) {
            System.err.println("Error loading categories from file: " + filePath);
            e.printStackTrace();

            new Categories("Default");
        }
    }


    public static Categories valueOf(String name) {
        Categories constant = CONSTANTS.get(name);
        if (constant == null) {
            throw new IllegalArgumentException(
                "No enum constant " + Categories.class.getName() + "." + name
            );
        }
        return constant;
    }


    public static Categories[] values() {
        return CONSTANTS.values().toArray(new Categories[0]);
    }

    @Override
    public String toString() {
        return name;
    }


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

    public static Categories addCategory(String newName) {
        if (CONSTANTS.containsKey(newName)) {
            return CONSTANTS.get(newName); // Already exists
        }
        return new Categories(newName);
    }


    public static boolean removeCategory(String name) {
        if (name.equals("All")) {
            return false; // Disallow removing "All"
        }
        return (CONSTANTS.remove(name) != null);
    }


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
