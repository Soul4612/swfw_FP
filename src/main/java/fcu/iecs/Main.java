package fcu.iecs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("===== diaries ex =====");
        List<DiaryEntry> diaries = new ArrayList<>();
//        diaries.add(new DiaryEntry(LocalDate.of(1980, 7, 31), "歡迎來到這個世界", "親愛的，今天是你出生的日子，願你未來平安順遂。媽媽留。"));
//        diaries.add(new DiaryEntry(LocalDate.of(1980, 10, 31), "*充滿血跡的字跡*", "對不起孩子……我們不能陪你長大了……但你要知道我們愛你……"));
//        diaries.add(new DiaryEntry(LocalDate.of(1991, 7, 31), "今天是我11歲生日", "今天有我的信，但被弗農姨父搶走了，我沒看到。"));
//        diaries.add(new DiaryEntry(LocalDate.of(1991, 9, 1), "今天入學霍格華茲", "今天認識了榮恩，被分配到了格蘭分多，那個馬份好討厭。"));
//        DiaryManager.save(diaries);
        diaries = DiaryManager.load();
        for (DiaryEntry e : diaries) {
            System.out.println(e);
        }

        System.out.println("\n===== records ex =====");
//        System.out.println(RecordEntry.getCategories());
        List<RecordEntry> records = new ArrayList<>();
//        records.add(new RecordEntry(LocalDate.of(1991, 7, 31), "古靈閣提款", Type.INCOME, "其它", 7500));
//        records.add(new RecordEntry(LocalDate.of(1991, 7, 31), "我的第一支魔杖", Type.EXPENSE, "學用品", 3500));
//        records.add(new RecordEntry(LocalDate.of(1991, 9, 1), "特快上的零食", Type.EXPENSE, "零食", 500));
//        RecordManager.save(records);
        records = RecordManager.load();
        int balance = 0;
        for (RecordEntry e : records) {
            System.out.println(e);
            if (e.getType() == Type.EXPENSE) {
                balance -= e.getAmount();
            } else {
                balance += e.getAmount();
            }
        }
        System.out.println("總結餘: " + balance);
    }
}
