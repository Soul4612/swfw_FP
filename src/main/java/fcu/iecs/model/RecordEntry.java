package fcu.iecs.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RecordEntry extends Entry {
    private Type type;

    private String category;

    private int amount;

    public RecordEntry() {
    }

    public RecordEntry(LocalDate date, String title, Type type, String category, int amount) {
        super(date, title);
        this.type = type;
        this.category = category;
        this.amount = amount;
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

