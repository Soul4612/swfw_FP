package fcu.iecs.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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

