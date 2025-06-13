package fcu.iecs;

import fcu.iecs.model.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

        System.out.println("\n===== categories ex =====");
        Set<String> expenseCategories = CategoryManager.load(RecordType.EXPENSE);
        Set<String> incomeCategories = CategoryManager.load(RecordType.INCOME);
        System.out.println("目前分類：" + expenseCategories);
        System.out.println("目前分類：" + incomeCategories);

        System.out.println("\n===== records ex =====");

//        System.out.println(RecordEntry.getCategories());
        List<RecordEntry> records = new ArrayList<>();
//        records.add(new RecordEntry(LocalDate.of(1991, 7, 31), "古靈閣提款", RecordType.INCOME, "其它", 7500));
//        records.add(new RecordEntry(LocalDate.of(1991, 7, 31), "我的第一支魔杖", RecordType.EXPENSE, "學用品", 3500));
//        CategoryManager.addCategory(RecordType.EXPENSE, "學用品");
//        records.add(new RecordEntry(LocalDate.of(1991, 9, 1), "特快上的零食", RecordType.EXPENSE, "零食", 500));
//        CategoryManager.addCategory(RecordType.EXPENSE, "零食");
//        RecordManager.save(records);
        records = RecordManager.load();
        int balance = 0;
        for (RecordEntry e : records) {
            System.out.println(e);
            if (e.getRecordType() == RecordType.EXPENSE) {
                balance -= e.getAmount();
            } else {
                balance += e.getAmount();
            }
        }
        System.out.println("總結餘: " + balance);
    }
}
