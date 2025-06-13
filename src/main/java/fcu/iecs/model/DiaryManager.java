package fcu.iecs.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DiaryManager {
    private static final File FILE = new File("Diary.json");
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public static void save(List<DiaryEntry> diaries) {
        Collections.sort(diaries);
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
