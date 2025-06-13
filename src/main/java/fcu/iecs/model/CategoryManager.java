package fcu.iecs.model;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

public class CategoryManager {

    private static final File EXPENSE_FILE = new File("ExpenseCategory.json");
    private static final File INCOME_FILE = new File("IncomeCategory.json");
    private static final ObjectMapper mapper = new ObjectMapper();

    // 共用方法：載入
    public static Set<String> load(RecordType type) {
        File file = type == RecordType.EXPENSE ? EXPENSE_FILE : INCOME_FILE;

        if (!file.exists()) {
            Set<String> defaults = new LinkedHashSet<>();
            if (type == RecordType.EXPENSE) {
                defaults.add("早餐");
                defaults.add("午餐");
                defaults.add("晚餐");
                defaults.add("其它");
            } else {
                defaults.add("薪水");
                defaults.add("獎金");
                defaults.add("其它");
            }
            save(type, defaults);
            return defaults;
        }

        try {
            return mapper.readValue(file, mapper.getTypeFactory().constructCollectionType(LinkedHashSet.class, String.class));
        } catch (IOException e) {
            throw new RuntimeException("無法讀取分類資料", e);
        }
    }

    // 共用方法：儲存
    public static void save(RecordType type, Set<String> categories) {
        File file = type == RecordType.EXPENSE ? EXPENSE_FILE : INCOME_FILE;
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, categories);
        } catch (IOException e) {
            throw new RuntimeException("無法儲存分類資料", e);
        }
    }

    public static void addCategory(RecordType type, String newCategory) {
        Set<String> categories = load(type);
        if (categories.add(newCategory)) {
            save(type, categories);
        }
    }

    public static void removeCategory(RecordType type, String category) {
        Set<String> categories = load(type);
        if (categories.remove(category)) {
            save(type, categories);
        }
    }
}
