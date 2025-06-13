package fcu.iecs.model;

import java.time.LocalDate;

public class RecordEntry extends Entry {
    private RecordType recordType;

    private String category;

    private int amount;

    public RecordEntry() {
    }

    public RecordEntry(LocalDate date, String title, RecordType recordType, String category, int amount) {
        super(date, title);
        this.recordType = recordType;
        this.category = category;
        this.amount = amount;
    }

    public RecordType getRecordType() {
        return recordType;
    }

    public String getCategory() {
        return category;
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return "[記帳] %s %s %s\n%s - $%d".formatted(CDF.of(date), recordType, category, title, amount);
    }
}

