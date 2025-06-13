package fcu.iecs;

import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        LocalDate date = LocalDate.of(1991, 7, 31);
        String title = "今天是我11歲生日";
        String content = "今天有我的信，但被弗農姨父搶走了，我沒看到";
        Entry diary = new DiaryEntry(date, title, content);
//        System.out.println(diary);

//        System.out.println(MoneyEntry.getCategories());
//        Entry org = new MoneyEntry(date, "我開始記帳了", Type.INCOME, "原始", 0);

    }
}