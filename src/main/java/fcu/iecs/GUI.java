package fcu.iecs;

import com.formdev.flatlaf.FlatDarkLaf;
import fcu.iecs.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.*;

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

    private String generateContentSummary(String content) {
        return content.length() > 20 ? content.substring(0, 20) + "..." : content;
    }

    class ButtonRenderer extends JPanel implements TableCellRenderer {
        private final JButton viewBtn = new JButton("檢視");
        private final JButton deleteBtn = new JButton("刪除");

        public ButtonRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
            add(viewBtn);
            add(deleteBtn);
            viewBtn.setFocusable(false);
            deleteBtn.setFocusable(false);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Color bg = isSelected ? table.getSelectionBackground() : table.getBackground();
            Color fg = isSelected ? table.getSelectionForeground() : table.getForeground();

            setBackground(bg);

            viewBtn.setPreferredSize(new Dimension(60, 19));
            viewBtn.setBackground(Color.DARK_GRAY);
            viewBtn.setForeground(Color.WHITE);
            viewBtn.setOpaque(true);

            deleteBtn.setPreferredSize(new Dimension(60, 19));
            deleteBtn.setBackground(Color.DARK_GRAY);
            deleteBtn.setForeground(Color.WHITE);
            deleteBtn.setOpaque(true);

            return this;
        }
    }

    private void reloadDiaryTable() {
        diaryTableModel.setRowCount(0); // 清空表格

        for (DiaryEntry entry : diaryList) {
            diaryTableModel.addRow(new Object[]{
                    CDF.of(entry.getDate()),
                    entry.getTitle(),
                    generateContentSummary(entry.getContent()),
                    "操作"
            });
        }
    }

    class ButtonEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel panel = new JPanel();
        private final JButton viewBtn = new JButton("檢視");
        private final JButton deleteBtn = new JButton("刪除");
        private int currentRow;

        private DefaultTableModel diaryTableModel;

        public ButtonEditor(JCheckBox checkBox, DefaultTableModel model) {
            this.diaryTableModel = model;
            panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
            panel.add(viewBtn);
            panel.add(deleteBtn);

            viewBtn.addActionListener(e -> {
                DiaryEntry entry = diaryList.get(currentRow);
                DiaryViewDialog dialog = new DiaryViewDialog(
                        (Frame) SwingUtilities.getWindowAncestor(panel),
                        entry
                );
                dialog.setVisible(true);

                if (dialog.isUpdated()) {
                    DiaryManager.save(diaryList);
                    reloadDiaryTable();
                }

                fireEditingStopped();
            });

            deleteBtn.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(panel,
                        "確定要刪除此日記？", "刪除確認", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    diaryList.remove(currentRow);
                    DiaryManager.save(diaryList);
                    refreshDiaryTable();
                }
                fireEditingStopped();
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.currentRow = row;
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }
    }

    private class DiaryViewDialog extends JDialog {
        private DiaryEntry entry;
        private boolean updated = false;

        public DiaryViewDialog(Frame owner, DiaryEntry entry) {
            super(owner, "日記檢視", true);
            this.entry = entry;

            setSize(400, 300);
            setLocationRelativeTo(owner);

            JLabel dateLabel = new JLabel("日期: " + CDF.of(entry.getDate()));
            dateLabel.setFont(new Font("微軟正黑體", Font.BOLD, 14));

            JLabel titleLabel = new JLabel("標題: " + entry.getTitle());
            titleLabel.setFont(new Font("微軟正黑體", Font.BOLD, 14));

            JTextArea contentArea = new JTextArea(entry.getContent());
            contentArea.setLineWrap(true);
            contentArea.setWrapStyleWord(true);
            contentArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(contentArea);

            JButton editButton = new JButton("編輯");
            editButton.addActionListener(e -> {
                DiaryEditDialog editDialog = new DiaryEditDialog(
                        (Frame) SwingUtilities.getWindowAncestor(this),
                        entry.getDate(),
                        entry.getTitle(),
                        entry.getContent()
                );
                editDialog.setVisible(true);

                if (editDialog.isSaved()) {
                    entry.edit(
                            editDialog.getDatefromGUI(),
                            editDialog.getTitlefromGUI(),
                            editDialog.getContentfromGUI()
                    );
                    updated = true;
                    this.dispose();
                }
            });

            JPanel topPanel = new JPanel(new GridLayout(2, 1, 5, 5));
            topPanel.add(dateLabel);
            topPanel.add(titleLabel);

            JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
            bottomPanel.add(scrollPane, BorderLayout.CENTER);
            bottomPanel.add(editButton, BorderLayout.SOUTH);

            getContentPane().setLayout(new BorderLayout(10, 10));
            getContentPane().add(topPanel, BorderLayout.NORTH);
            getContentPane().add(bottomPanel, BorderLayout.CENTER);

            ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        }

        public boolean isUpdated() {
            return updated;
        }
    }

    private class DiaryEditDialog extends JDialog {
        private JSpinner yearSpinner;
        private JSpinner monthSpinner;
        private JSpinner daySpinner;
        private JTextField titleField;
        private JTextArea contentArea;
        private boolean saved = false;

        public DiaryEditDialog(Frame owner, LocalDate date, String title, String content) {
            super(owner, "編輯日記", true);
            setSize(480, 400);
            setLocationRelativeTo(owner);

            // 建立 JSpinner：年 / 月 / 日
            yearSpinner = new JSpinner(new SpinnerNumberModel(date.getYear(), 1800, 2500, 1));
            monthSpinner = new JSpinner(new SpinnerNumberModel(date.getMonthValue(), 1, 12, 1));
            daySpinner = new JSpinner(new SpinnerNumberModel(date.getDayOfMonth(), 1, 31, 1));

            titleField = new JTextField(title);
            contentArea = new JTextArea(content);
            contentArea.setLineWrap(true);
            contentArea.setWrapStyleWord(true);
            contentArea.setFont(new Font("微軟正黑體", Font.PLAIN, 14));

            JPanel formPanel = new JPanel(new BorderLayout(5, 5));

            // 上方：日期 + 標題
            JPanel topPanel = new JPanel();
            topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

            // 日期區
            JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            datePanel.add(new JLabel("日期:"));
            datePanel.add(yearSpinner);
            datePanel.add(new JLabel("年"));
            datePanel.add(monthSpinner);
            datePanel.add(new JLabel("月"));
            datePanel.add(daySpinner);
            datePanel.add(new JLabel("日"));

            // 標題區
            JPanel titlePanel = new JPanel(new BorderLayout());
            titlePanel.add(new JLabel("標題:"), BorderLayout.NORTH);
            titlePanel.add(titleField, BorderLayout.CENTER);

            topPanel.add(datePanel);
            topPanel.add(Box.createVerticalStrut(5));
            topPanel.add(titlePanel);

            formPanel.add(topPanel, BorderLayout.NORTH);

            // 中央：內容
            JPanel contentPanel = new JPanel(new BorderLayout());
            contentPanel.add(new JLabel("內容:"), BorderLayout.NORTH);
            JScrollPane scrollPane = new JScrollPane(contentArea);
            scrollPane.setPreferredSize(new Dimension(440, 200));
            contentPanel.add(scrollPane, BorderLayout.CENTER);

            formPanel.add(contentPanel, BorderLayout.CENTER);

            JButton saveBtn = new JButton("保存");
            JButton cancelBtn = new JButton("取消");

            saveBtn.addActionListener(e -> {
                try {
                    LocalDate editedDate = getDatefromGUI();
                    // 驗證日期合法（例如 2/30）
                    editedDate.getDayOfMonth();  // 呼叫觸發例外（如果不合法）
                    saved = true;
                    setVisible(false);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "請選擇有效的日期", "錯誤", JOptionPane.ERROR_MESSAGE);
                }
            });

            cancelBtn.addActionListener(e -> {
                saved = false;
                setVisible(false);
            });

            JPanel btnPanel = new JPanel();
            btnPanel.add(saveBtn);
            btnPanel.add(cancelBtn);

            getContentPane().setLayout(new BorderLayout(10, 10));
            getContentPane().add(formPanel, BorderLayout.CENTER);
            getContentPane().add(btnPanel, BorderLayout.SOUTH);
            ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        }

        public boolean isSaved() {
            return saved;
        }

        public LocalDate getDatefromGUI() {
            int y = (Integer) yearSpinner.getValue();
            int m = (Integer) monthSpinner.getValue();
            int d = (Integer) daySpinner.getValue();
            return LocalDate.of(y, m, d);
        }

        public String getTitlefromGUI() {
            return titleField.getText();
        }

        public String getContentfromGUI() {
            return contentArea.getText();
        }
    }

    public GUI() {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
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
        frame.setMinimumSize(new Dimension(800, 600));  // 設定視窗最小尺寸
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
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // 頂部 summary 顯示
        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        incomeLabel = new JLabel("總收入: 0");
        expenseLabel = new JLabel("總支出: 0");
        balanceLabel = new JLabel("總結餘: 0");

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

        // 固定日期欄寬 (假設第0欄是日期欄)
        recordTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        recordTable.getColumnModel().getColumn(0).setMaxWidth(100);
        recordTable.getColumnModel().getColumn(0).setMinWidth(100);

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

        addRecordBtn.addActionListener(e -> openAddRecordDialog());
        deleteRecordBtn.addActionListener(e -> deleteSelectedRecord());

        return panel;
    }

    private JPanel createDiaryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        diaryTableModel = new DefaultTableModel(
                new Object[]{"日期", "標題", "內容摘要", "操作"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3;  // 只有操作欄可互動（實際上由按鈕 editor 處理）
            }
        };
        diaryTable = new JTable(diaryTableModel);
        diaryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(diaryTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // 固定日期欄寬 (假設第0欄是日期欄)
        diaryTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        diaryTable.getColumnModel().getColumn(0).setMaxWidth(150);
        diaryTable.getColumnModel().getColumn(0).setMinWidth(150);

        // 這裡放設定按鈕欄的 CellRenderer 和 CellEditor
        diaryTable.getColumnModel().getColumn(3).setCellRenderer(new ButtonRenderer());
        diaryTable.getColumnModel().getColumn(3).setCellEditor(new ButtonEditor(new JCheckBox(), diaryTableModel));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addDiaryBtn = new JButton("新增");
        btnPanel.add(addDiaryBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        addDiaryBtn.addActionListener(e -> openAddDiaryDialog());

        reloadDiaryTable();
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
        balanceLabel.setText("總結餘: " + (totalIncome - totalExpense));
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
                contentPreview = generateContentSummary(contentPreview);
            }
            diaryTableModel.addRow(new Object[]{
                    CDF.of(d.getDate()),
                    d.getTitle(),
                    contentPreview,
                    "操作"
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
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 日期（年/月/日）
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("日期:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;

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
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("標題:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        JTextField titleField = new JTextField();
        formPanel.add(titleField, gbc);

        // 收入/支出
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("收入/支出:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 2;
        JComboBox<RecordType> recordTypeCombo = new JComboBox<>(RecordType.values());
        formPanel.add(recordTypeCombo, gbc);

        // 類型
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("類型:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 3;

        // 類型下拉 + 新增類型按鈕
        JPanel categoryPanel = new JPanel(new BorderLayout(5, 0));
        JComboBox<String> categoryCombo = new JComboBox<>();
        JButton addCategoryBtn = new JButton("+");
        addCategoryBtn.setToolTipText("新增類型");

        categoryPanel.add(categoryCombo, BorderLayout.CENTER);
        categoryPanel.add(addCategoryBtn, BorderLayout.EAST);

        formPanel.add(categoryPanel, gbc);

        // 金額
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(new JLabel("金額:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 4;
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
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setResizable(false);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 日期（年/月/日）
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("日期:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;

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
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("標題:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        JTextField titleField = new JTextField();
        formPanel.add(titleField, gbc);

        // 內容
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("內容:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 2;
        JTextArea contentArea = new JTextArea(6, 20);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
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
}