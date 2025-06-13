package fcu.iecs;

import com.formdev.flatlaf.FlatLightLaf;
import fcu.iecs.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class GUI {
    private JFrame frame;
    private JTabbedPane tabbedPane;

    // 記帳頁面元件
    private JLabel incomeLabel;
    private JLabel expenseLabel;
    private JLabel balanceLabel;
    private JTable recordTable;
    private DefaultTableModel recordTableModel;
    private JButton addRecordBtn;
    private JButton deleteRecordBtn;

    // 日記頁面元件
    private JTable diaryTable;
    private DefaultTableModel diaryTableModel;
    private JButton addDiaryBtn;
    private JButton deleteDiaryBtn;

    // 記帳資料
    private List<RecordEntry> recordList;
    private Set<String> expenseCategories;
    private Set<String> incomeCategories;

    // 日記資料
    private List<DiaryEntry> diaryList;

    public GUI() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        recordList = RecordManager.load();
        expenseCategories = CategoryManager.load(RecordType.EXPENSE);
        incomeCategories = CategoryManager.load(RecordType.INCOME);
        diaryList = DiaryManager.load();

        frame = new JFrame("記帳與日記管理系統");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();

        tabbedPane.addTab("記帳", createRecordPanel());
        tabbedPane.addTab("日記", createDiaryPanel());

        frame.add(tabbedPane);
    }

    public void show() {
        frame.setVisible(true);
        refreshRecordSummary();
        refreshRecordTable();
        refreshDiaryTable();
    }

    private JPanel createRecordPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10,10,10,10));

        // 頂部 summary 顯示
        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        incomeLabel = new JLabel("總收入: 0");
        expenseLabel = new JLabel("總支出: 0");
        balanceLabel = new JLabel("結餘: 0");

        Font font = new Font("微軟正黑體", Font.BOLD, 16);
        incomeLabel.setFont(font);
        expenseLabel.setFont(font);
        balanceLabel.setFont(font);

        summaryPanel.add(incomeLabel);
        summaryPanel.add(expenseLabel);
        summaryPanel.add(balanceLabel);

        panel.add(summaryPanel, BorderLayout.NORTH);

        // 中間 Table 顯示明細
        recordTableModel = new DefaultTableModel(
                new Object[]{"日期", "標題", "收入/支出", "類型", "金額"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 不能直接編輯表格
            }
        };
        recordTable = new JTable(recordTableModel);
        recordTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(recordTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // 底部按鈕
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addRecordBtn = new JButton("新增");
        deleteRecordBtn = new JButton("刪除");
        deleteRecordBtn.setEnabled(false);
        btnPanel.add(addRecordBtn);
        btnPanel.add(deleteRecordBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        // 選擇表格列時啟用刪除按鈕
        recordTable.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            deleteRecordBtn.setEnabled(recordTable.getSelectedRow() != -1);
        });

        // 按鈕事件
        addRecordBtn.addActionListener(e -> openAddRecordDialog());
        deleteRecordBtn.addActionListener(e -> deleteSelectedRecord());

        return panel;
    }

    private JPanel createDiaryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10,10,10,10));

        diaryTableModel = new DefaultTableModel(
                new Object[]{"日期", "標題", "內容摘要"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        diaryTable = new JTable(diaryTableModel);
        diaryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(diaryTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addDiaryBtn = new JButton("新增");
        deleteDiaryBtn = new JButton("刪除");
        deleteDiaryBtn.setEnabled(false);
        btnPanel.add(addDiaryBtn);
        btnPanel.add(deleteDiaryBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        diaryTable.getSelectionModel().addListSelectionListener(e -> {
            deleteDiaryBtn.setEnabled(diaryTable.getSelectedRow() != -1);
        });

        addDiaryBtn.addActionListener(e -> openAddDiaryDialog());
        deleteDiaryBtn.addActionListener(e -> deleteSelectedDiary());

        return panel;
    }

    private void refreshRecordSummary() {
        int totalIncome = recordList.stream()
                .filter(r -> r.getRecordType() == RecordType.INCOME)
                .mapToInt(RecordEntry::getAmount).sum();

        int totalExpense = recordList.stream()
                .filter(r -> r.getRecordType() == RecordType.EXPENSE)
                .mapToInt(RecordEntry::getAmount).sum();

        incomeLabel.setText("總收入: " + totalIncome);
        expenseLabel.setText("總支出: " + totalExpense);
        balanceLabel.setText("結餘: " + (totalIncome - totalExpense));
    }

    private void refreshRecordTable() {
        recordTableModel.setRowCount(0);
        for (RecordEntry r : recordList) {
            recordTableModel.addRow(new Object[]{
                    CDF.of(r.getDate()),
                    r.getTitle(),
                    r.getRecordType().toString(),
                    r.getCategory(),
                    r.getAmount()
            });
        }
    }

    private void refreshDiaryTable() {
        diaryTableModel.setRowCount(0);
        for (DiaryEntry d : diaryList) {
            String contentPreview = d.getContent();
            if (contentPreview.length() > 20) {
                contentPreview = contentPreview.substring(0, 20) + "...";
            }
            diaryTableModel.addRow(new Object[]{
                    CDF.of(d.getDate()),
                    d.getTitle(),
                    contentPreview
            });
        }
    }

    private void openAddRecordDialog() {
        JDialog dialog = new JDialog(frame, "新增記帳", true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(frame);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setResizable(false);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(10,10,10,10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6,6,6,6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 日期（年/月/日）
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("日期:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0;

        // 三個 Spinner
        SpinnerNumberModel yearModel = new SpinnerNumberModel(LocalDate.now().getYear(), 1900, 2100, 1);
        JSpinner yearSpinner = new JSpinner(yearModel);
        SpinnerNumberModel monthModel = new SpinnerNumberModel(LocalDate.now().getMonthValue(), 1, 12, 1);
        JSpinner monthSpinner = new JSpinner(monthModel);
        SpinnerNumberModel dayModel = new SpinnerNumberModel(LocalDate.now().getDayOfMonth(), 1, 31, 1);
        JSpinner daySpinner = new JSpinner(dayModel);

        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        datePanel.add(yearSpinner);
        datePanel.add(new JLabel("年"));
        datePanel.add(monthSpinner);
        datePanel.add(new JLabel("月"));
        datePanel.add(daySpinner);
        datePanel.add(new JLabel("日"));

        formPanel.add(datePanel, gbc);

        // 標題
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("標題:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        JTextField titleField = new JTextField();
        formPanel.add(titleField, gbc);

        // 收入/支出
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("收入/支出:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        JComboBox<RecordType> recordTypeCombo = new JComboBox<>(RecordType.values());
        formPanel.add(recordTypeCombo, gbc);

        // 類型
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("類型:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3;

        // 類型下拉 + 新增類型按鈕
        JPanel categoryPanel = new JPanel(new BorderLayout(5, 0));
        JComboBox<String> categoryCombo = new JComboBox<>();
        JButton addCategoryBtn = new JButton("+");
        addCategoryBtn.setToolTipText("新增類型");

        categoryPanel.add(categoryCombo, BorderLayout.CENTER);
        categoryPanel.add(addCategoryBtn, BorderLayout.EAST);

        formPanel.add(categoryPanel, gbc);

        // 金額
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("金額:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4;
        JTextField amountField = new JTextField();
        formPanel.add(amountField, gbc);

        // 根據記帳類型刷新類型選項
        Runnable refreshCategoryCombo = () -> {
            categoryCombo.removeAllItems();
            RecordType rt = (RecordType) recordTypeCombo.getSelectedItem();
            Set<String> categories = (rt == RecordType.EXPENSE) ? expenseCategories : incomeCategories;
            for (String c : categories) {
                categoryCombo.addItem(c);
            }
        };
        refreshCategoryCombo.run();
        recordTypeCombo.addActionListener(e -> refreshCategoryCombo.run());

        addCategoryBtn.addActionListener(e -> {
            String newCat = JOptionPane.showInputDialog(dialog, "請輸入新類型名稱");
            if (newCat != null && !newCat.trim().isEmpty()) {
                RecordType rt = (RecordType) recordTypeCombo.getSelectedItem();
                if (rt == RecordType.EXPENSE) {
                    expenseCategories.add(newCat.trim());
                } else {
                    incomeCategories.add(newCat.trim());
                }
                CategoryManager.save(rt, rt == RecordType.EXPENSE ? expenseCategories : incomeCategories);
                refreshCategoryCombo.run();
                JOptionPane.showMessageDialog(dialog, "新增類型成功！");
            }
        });

        // 下方按鈕區
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("儲存");
        JButton cancelBtn = new JButton("取消");
        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        saveBtn.addActionListener(e -> {
            try {
                int y = (int) yearSpinner.getValue();
                int m = (int) monthSpinner.getValue();
                int d = (int) daySpinner.getValue();
                LocalDate date = LocalDate.of(y, m, d);

                String title = titleField.getText().trim();
                if (title.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "標題不可空白");
                    return;
                }
                RecordType rt = (RecordType) recordTypeCombo.getSelectedItem();
                String category = (String) categoryCombo.getSelectedItem();
                if (category == null || category.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "請選擇或新增類型");
                    return;
                }

                String amountStr = amountField.getText().trim();
                int amount = Integer.parseInt(amountStr);
                if (amount <= 0) {
                    JOptionPane.showMessageDialog(dialog, "金額需大於0");
                    return;
                }

                // 新增記錄
                RecordEntry newEntry = new RecordEntry(date, title, rt, category, amount);
                recordList.add(newEntry);
                RecordManager.save(recordList);

                // 更新類型集，確保同步
                if (rt == RecordType.EXPENSE) expenseCategories.add(category);
                else incomeCategories.add(category);
                CategoryManager.save(rt, rt == RecordType.EXPENSE ? expenseCategories : incomeCategories);

                refreshRecordSummary();
                refreshRecordTable();
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "輸入資料錯誤: " + ex.getMessage());
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    private void deleteSelectedRecord() {
        int idx = recordTable.getSelectedRow();
        if (idx >= 0) {
            int confirm = JOptionPane.showConfirmDialog(frame, "確定要刪除此筆記錄？", "刪除確認", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                recordList.remove(idx);
                RecordManager.save(recordList);
                refreshRecordSummary();
                refreshRecordTable();
            }
        }
    }

    private void openAddDiaryDialog() {
        JDialog dialog = new JDialog(frame, "新增日記", true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(frame);
        dialog.setLayout(new BorderLayout(10,10));
        dialog.setResizable(false);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(10,10,10,10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6,6,6,6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 日期（年/月/日）
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("日期:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0;

        SpinnerNumberModel yearModel = new SpinnerNumberModel(LocalDate.now().getYear(), 1900, 2100, 1);
        JSpinner yearSpinner = new JSpinner(yearModel);
        SpinnerNumberModel monthModel = new SpinnerNumberModel(LocalDate.now().getMonthValue(), 1, 12, 1);
        JSpinner monthSpinner = new JSpinner(monthModel);
        SpinnerNumberModel dayModel = new SpinnerNumberModel(LocalDate.now().getDayOfMonth(), 1, 31, 1);
        JSpinner daySpinner = new JSpinner(dayModel);

        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        datePanel.add(yearSpinner);
        datePanel.add(new JLabel("年"));
        datePanel.add(monthSpinner);
        datePanel.add(new JLabel("月"));
        datePanel.add(daySpinner);
        datePanel.add(new JLabel("日"));

        formPanel.add(datePanel, gbc);

        // 標題
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("標題:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        JTextField titleField = new JTextField();
        formPanel.add(titleField, gbc);

        // 內容
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("內容:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        JTextArea contentArea = new JTextArea(6, 20);
        JScrollPane contentScroll = new JScrollPane(contentArea);
        formPanel.add(contentScroll, gbc);

        // 按鈕區
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("儲存");
        JButton cancelBtn = new JButton("取消");
        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        saveBtn.addActionListener(e -> {
            try {
                int y = (int) yearSpinner.getValue();
                int m = (int) monthSpinner.getValue();
                int d = (int) daySpinner.getValue();
                LocalDate date = LocalDate.of(y, m, d);

                String title = titleField.getText().trim();
                if (title.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "標題不可空白");
                    return;
                }
                String content = contentArea.getText().trim();
                if (content.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "內容不可空白");
                    return;
                }

                DiaryEntry newDiary = new DiaryEntry(date, title, content);
                diaryList.add(newDiary);
                DiaryManager.save(diaryList);

                refreshDiaryTable();
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "輸入資料錯誤: " + ex.getMessage());
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    private void deleteSelectedDiary() {
        int idx = diaryTable.getSelectedRow();
        if (idx >= 0) {
            int confirm = JOptionPane.showConfirmDialog(frame, "確定要刪除此筆日記？", "刪除確認", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                diaryList.remove(idx);
                DiaryManager.save(diaryList);
                refreshDiaryTable();
            }
        }
    }

    // 日期格式化工具，方便輸出 yyyy/MM/dd
    static class CDF {
        static String of(LocalDate date) {
            return String.format("%04d/%02d/%02d",
                    date.getYear(), date.getMonthValue(), date.getDayOfMonth());
        }
    }
}