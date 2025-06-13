package fcu.iecs;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public abstract class Entry {
    protected LocalDate date;
    protected String title;

    public Entry(LocalDate date, String title) {
        this.date = date;
        this.title = title;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getTitle() {
        return title;
    }
}

class DiaryEntry extends Entry {
    private static final String[] WEEKDAYS = {"", "一", "二", "三", "四", "五", "六", "日"};

    private String content;

    public DiaryEntry(LocalDate date, String title, String content) {
        super(date, title);
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    private String customDateFormat() {
        // for ex: 1991/07/31(三)
        return "%d/%02d/%02d(%s)".formatted(
                date.getYear(),
                date.getMonthValue(),
                date.getDayOfMonth(),
                WEEKDAYS[date.getDayOfWeek().getValue()]
        );
    }

    @Override
    public String toString() {
        return "[日記] %s\n%s\n%s\n".formatted(customDateFormat(), title, content);
    }
}

enum Type {
    EXPENSE, INCOME
}

class MoneyEntry extends Entry {
    private static List<String> categories = new ArrayList<>();

    private Type type;
    private String category;
    private int amount;

    static {
        categories.add("早餐");
        categories.add("午餐");
        categories.add("晚餐");
    }

    public MoneyEntry(LocalDate date, String title, Type type, String category, int amount) {
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
}