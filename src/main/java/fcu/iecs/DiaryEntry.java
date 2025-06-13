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

class DiaryManager {
    private static final File FILE = new File("Diary.json");
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public static void save(List<DiaryEntry> diaries) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(FILE, diaries);
        } catch (IOException e) {
            throw new RuntimeException("無法儲存日記資料", e);
        }
    }

    public static List<DiaryEntry> load() {
        if (!FILE.exists()) return new ArrayList<>();
        try {
            return mapper.readValue(FILE, new TypeReference<List<DiaryEntry>>() {
            });
        } catch (IOException e) {
            throw new RuntimeException("無法載入日記資料", e);
        }
    }
}
