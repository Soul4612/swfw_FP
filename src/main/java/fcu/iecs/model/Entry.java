package fcu.iecs.model;

import java.time.LocalDate;

public abstract class Entry implements Comparable<Entry> {
    protected LocalDate date;
    protected String title;

    public Entry() {
    }

    public Entry(LocalDate date, String title) {
        this.date = date;
        this.title = title;
    }

    @Override
    public int compareTo(Entry o) {
        int cmp = this.date.compareTo(o.date);
        if (cmp == 0) {
            return this.title.compareTo(o.title);
        }
        return cmp;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getTitle() {
        return title;
    }
}
