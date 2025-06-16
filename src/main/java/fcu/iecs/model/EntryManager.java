package fcu.iecs.model;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EntryManager<T extends Entry> {
    private final File file;
    private final ObjectMapper mapper;
    private final JavaType type;

    public EntryManager(String filename, Class<T> clazz) {
        this.file = new File(filename);
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.type = mapper.getTypeFactory().constructCollectionType(List.class, clazz);
    }

    public void save(List<T> entries) {
        Collections.sort(entries);
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, entries);
        } catch (IOException e) {
            throw new RuntimeException("無法儲存資料", e);
        }
    }

    public List<T> load() {
        if (!file.exists()) return new ArrayList<>();
        try {
            return mapper.readValue(file, type);
        } catch (IOException e) {
            throw new RuntimeException("無法載入資料", e);
        }
    }

    public void exportData(File file) {
        try {
            List<T> entries = load();
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, entries);
        } catch (IOException e) {
            throw new RuntimeException("匯出失敗", e);
        }
    }

    public void importData(File file) {
        try {
            List<T> entries = mapper.readValue(file, type);
            save(entries);
        } catch (IOException e) {
            throw new RuntimeException("匯入失敗", e);
        }
    }
}
