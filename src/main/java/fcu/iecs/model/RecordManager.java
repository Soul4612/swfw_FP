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

public class RecordManager {
    private static final File FILE = new File("Record.json");
    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public static void save(List<RecordEntry> records) {
        Collections.sort(records);
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
