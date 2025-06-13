package fcu.iecs.model;

import java.time.LocalDate;

public class DiaryEntry extends Entry {
    private String content;

    public DiaryEntry() {
    }

    public DiaryEntry(LocalDate date, String title, String content) {
        super(date, title);
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "[日記] %s\n%s\n%s".formatted(CDF.of(date), title, content);
    }
}

