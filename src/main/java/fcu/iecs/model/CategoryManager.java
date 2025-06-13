package fcu.iecs.model;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

public class CategoryManager {
    private static final File FILE = new File("Category.json");
    private static final ObjectMapper mapper = new ObjectMapper();

    public static Set<String> load() {
        if (!FILE.exists()) {
            Set<String> defaults = new LinkedHashSet<>();
            defaults.add("早餐");
            defaults.add("午餐");
            defaults.add("晚餐");
            defaults.add("其它");
            save(defaults);
            return defaults;
        }
        try {
            return mapper.readValue(FILE, mapper.getTypeFactory().constructCollectionType(LinkedHashSet.class, String.class));
        } catch (IOException e) {
            throw new RuntimeException("無法讀取分類資料", e);
        }
    }

    public static void save(Set<String> categories) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(FILE, categories);
        } catch (IOException e) {
            throw new RuntimeException("無法儲存分類資料", e);
        }
    }

    public static void addCategory(String newCategory) {
        Set<String> categories = load();
        if (categories.add(newCategory)) {
            save(categories);
        }
    }

    public static void removeCategory(String category) {
        Set<String> categories = load();
        if (categories.remove(category)) {
            save(categories);
        }
    }
}
