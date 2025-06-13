package fcu.iecs;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

enum Type {
    EXPENSE {
        @Override
        public String toString() {
            return "支出";
        }
    }, INCOME {
        @Override
        public String toString() {
            return "收入";
        }
    }
}

public class RecordEntry extends Entry {
    private static List<String> categories = new ArrayList<>();

    private Type type;

    private String category;

    private int amount;

    static {
        categories.add("早餐");
        categories.add("午餐");
        categories.add("晚餐");
    }

    public RecordEntry() {
    }

    public RecordEntry(LocalDate date, String title, Type type, String category, int amount) {
        super(date, title);
        this.type = type;
        this.category = category;
        this.amount = amount;
    }

    public static List<String> getCategories() {
        return categories;
    }

    public Type getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return "[記帳] %s %s %s\n%s - $%d".formatted(CDF.of(date), type, category, title, amount);
    }
}

class RecordManager {
    private static final File FILE = new File("Record.json");
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public static void save(List<RecordEntry> records) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(FILE, records);
        } catch (IOException e) {
            throw new RuntimeException("無法儲存記帳資料", e);
        }
    }

    public static List<RecordEntry> load() {
        if (!FILE.exists()) return new ArrayList<>();
        try {
            return mapper.readValue(FILE, new TypeReference<List<RecordEntry>>() {
            });
        } catch (IOException e) {
            throw new RuntimeException("無法載入記帳資料", e);
        }
    }
}
