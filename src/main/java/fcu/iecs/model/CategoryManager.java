package fcu.iecs.model;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class CategoryManager {
    private final File file;
    private final ObjectMapper mapper;
    private final JavaType type;
    private final RecordType recordType;

    private Set<String> getDefaultCategories(RecordType type) {
        Set<String> defaults = new LinkedHashSet<>();
        if (type == RecordType.EXPENSE) {
            defaults.add("早餐");
            defaults.add("午餐");
            defaults.add("晚餐");
            defaults.add("其它");
        } else {
            defaults.add("薪水");
            defaults.add("獎金");
            defaults.add("其它");
        }
        return defaults;
    }

    public CategoryManager(String filename, RecordType recordType) {
        this.file = new File(filename);
        this.recordType = recordType;
        this.mapper = new ObjectMapper();
        this.type = mapper.getTypeFactory().constructCollectionType(LinkedHashSet.class, String.class);
        if (!file.exists()) {
            save(getDefaultCategories(recordType));
        }
    }

    public void save(Set<String> categories) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, categories);
        } catch (IOException e) {
            throw new RuntimeException("無法儲存分類資料", e);
        }
    }

    public Set<String> load() {
        try {
            return mapper.readValue(file, type);
        } catch (IOException e) {
            throw new RuntimeException("無法讀取分類資料", e);
        }
    }

    public void importData(List<RecordEntry> records) {
        Set<String> defaults = getDefaultCategories(recordType);
        for (RecordEntry r : records) {
            if (r.getRecordType() == recordType) {
                defaults.add(r.getCategory());
            }
        }
        save(defaults);
    }
}
