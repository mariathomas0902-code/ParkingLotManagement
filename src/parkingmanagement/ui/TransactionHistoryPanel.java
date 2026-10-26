package parkingmanagement.ui;

import parkingmanagement.ParkingLotService;
import parkingmanagement.model.ExitResult;
import parkingmanagement.model.TransactionRecord;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TransactionHistoryPanel extends JPanel {

    private final ParkingLotService service;
    private final MainFrame mainFrame;

    private final JTable table;
    private final DefaultTableModel tableModel;
    private final TableRowSorter<DefaultTableModel> sorter;

    private final JTextField searchField;
    private final JComboBox<String> statusFilter;
    private final JLabel recordsCountLabel;
    private final JLabel totalRevenueLabel;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm a");

    public TransactionHistoryPanel(ParkingLotService service, MainFrame mainFrame) {
        this.service = service;
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_COLOR);
        setBorder(new EmptyBorder(25, 35, 25, 35));

        add(createHeader(), BorderLayout.NORTH);

        JPanel centerCard = UITheme.createCardPanel();
        centerCard.setLayout(new BorderLayout(0, 14));

        // Toolbar
        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        toolbar.setOpaque(false);

        JPanel filterGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterGroup.setOpaque(false);

        JLabel searchIcon = new JLabel("Search:");
        searchIcon.setFont(UITheme.BOLD_FONT);
        searchIcon.setForeground(UITheme.TEXT_MUTED);

        searchField = UITheme.createStyledTextField();
        searchField.setPreferredSize(new Dimension(220, 36));

        statusFilter = new JComboBox<>(new String[]{"All Transactions", "ACTIVE", "COMPLETED"});
        statusFilter.setFont(UITheme.REGULAR_FONT);
        statusFilter.setBackground(Color.WHITE);
        statusFilter.setPreferredSize(new Dimension(160, 36));

        filterGroup.add(searchIcon);
        filterGroup.add(searchField);
        filterGroup.add(new JLabel("Status:"));
        filterGroup.add(statusFilter);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightActions.setOpaque(false);

        JButton viewReceiptBtn = UITheme.createPrimaryButton("View Ticket / Invoice");
        viewReceiptBtn.addActionListener(e -> viewSelectedReceipt());

        JButton exportCsvBtn = UITheme.createSecondaryButton("Export CSV");
        exportCsvBtn.addActionListener(e -> exportToCsv());

        JButton refreshBtn = UITheme.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> refreshHistory());

        rightActions.add(viewReceiptBtn);
        rightActions.add(exportCsvBtn);
        rightActions.add(refreshBtn);

        toolbar.add(filterGroup, BorderLayout.WEST);
        toolbar.add(rightActions, BorderLayout.EAST);
        centerCard.add(toolbar, BorderLayout.NORTH);

        // Table
        String[] columns = {
                "Ticket #", "Vehicle Number", "Type", "Slot", "Customer", "Entry Time", "Exit Time", "Duration", "Fee (Rs.)", "Status"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        table = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        UITheme.styleTable(table);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyFilters();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyFilters();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyFilters();
            }
        });

        statusFilter.addActionListener(e -> applyFilters());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1));
        centerCard.add(scrollPane, BorderLayout.CENTER);

        // Bottom Metrics
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);

        recordsCountLabel = new JLabel("Total Records: 0");
        recordsCountLabel.setFont(UITheme.BOLD_FONT);
        recordsCountLabel.setForeground(UITheme.TEXT_MUTED);

        totalRevenueLabel = new JLabel("Completed Revenue: Rs. 0.00");
        totalRevenueLabel.setFont(UITheme.BOLD_FONT);
        totalRevenueLabel.setForeground(UITheme.SUCCESS_TEXT);

        bottomBar.add(recordsCountLabel, BorderLayout.WEST);
        bottomBar.add(totalRevenueLabel, BorderLayout.EAST);

        centerCard.add(bottomBar, BorderLayout.SOUTH);

        add(centerCard, BorderLayout.CENTER);

        refreshHistory();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Transaction History & Audit");
        title.setFont(UITheme.TITLE_FONT);
        title.setForeground(UITheme.TEXT_DARK);

        JLabel subtitle = new JLabel("Complete audit logs of parking admissions, departure timestamps, and fees collected");
        subtitle.setFont(UITheme.REGULAR_FONT);
        subtitle.setForeground(UITheme.TEXT_MUTED);

        JPanel titles = new JPanel();
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.setOpaque(false);
        titles.add(title);
        titles.add(Box.createVerticalStrut(4));
        titles.add(subtitle);

        header.add(titles, BorderLayout.WEST);
        return header;
    }

    private void applyFilters() {
        String text = searchField.getText().trim();
        String status = (String) statusFilter.getSelectedItem();

        List<RowFilter<DefaultTableModel, Object>> filters = new ArrayList<>();

        if (!text.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + text));
        }

        if (status != null && !"All Transactions".equalsIgnoreCase(status)) {
            filters.add(RowFilter.regexFilter("^" + status + "$", 9));
        }

        if (filters.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.andFilter(filters));
        }

        updateBottomMetrics();
    }

    private void updateBottomMetrics() {
        int visibleRows = table.getRowCount();
        double totalRev = 0.0;

        for (int i = 0; i < visibleRows; i++) {
            int modelRow = table.convertRowIndexToModel(i);
            String feeStr = (String) tableModel.getValueAt(modelRow, 8);
            String status = (String) tableModel.getValueAt(modelRow, 9);
            if ("COMPLETED".equalsIgnoreCase(status)) {
                try {
                    totalRev += Double.parseDouble(feeStr);
                } catch (NumberFormatException ignored) {
                }
            }
        }

        recordsCountLabel.setText("Filtered Records: " + visibleRows);
        totalRevenueLabel.setText(String.format("Completed Revenue: Rs. %.2f", totalRev));
    }

    public void refreshHistory() {
        tableModel.setRowCount(0);
        List<TransactionRecord> list = service.getTransactionHistory();

        for (TransactionRecord r : list) {
            String entryStr = r.entryTime() != null ? r.entryTime().format(FORMATTER) : "N/A";
            String exitStr = r.exitTime() != null ? r.exitTime().format(FORMATTER) : "-";
            String feeStr = String.format("%.2f", r.parkingFee());

            tableModel.addRow(new Object[]{
                    "#" + r.ticketId(),
                    r.registrationNumber(),
                    r.vehicleType(),
                    "Slot " + r.slotId(),
                    r.customerName() != null ? r.customerName() : "Customer",
                    entryStr,
                    exitStr,
                    r.getFormattedDuration(),
                    feeStr,
                    r.status()
            });
        }

        applyFilters();
    }

    private void viewSelectedReceipt() {
        int selected = table.getSelectedRow();
        if (selected == -1) {
            JOptionPane.showMessageDialog(this, "Please select a transaction to view receipt.", "Select Row", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = table.convertRowIndexToModel(selected);
        String ticketStr = (String) tableModel.getValueAt(modelRow, 0);
        int ticketId = Integer.parseInt(ticketStr.replace("#", "").trim());

        for (TransactionRecord t : service.getTransactionHistory()) {
            if (t.ticketId() == ticketId) {
                ExitResult exitResult = ExitResult.success(
                        t.ticketId(),
                        t.slotId(),
                        t.registrationNumber(),
                        t.vehicleType(),
                        t.customerName(),
                        t.entryTime(),
                        t.exitTime() != null ? t.exitTime() : java.time.LocalDateTime.now(),
                        t.parkingFee(),
                        t.getFormattedDuration()
                );
                ReceiptDialog.showExitInvoice(mainFrame, exitResult);
                return;
            }
        }
    }

    private void exportToCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("parking_transactions.csv"));
        int ret = chooser.showSaveDialog(this);
        if (ret != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            // Write Header
            for (int col = 0; col < tableModel.getColumnCount(); col++) {
                writer.print(tableModel.getColumnName(col));
                if (col < tableModel.getColumnCount() - 1) writer.print(",");
            }
            writer.println();

            // Write Data
            for (int row = 0; row < tableModel.getRowCount(); row++) {
                for (int col = 0; col < tableModel.getColumnCount(); col++) {
                    Object val = tableModel.getValueAt(row, col);
                    String valStr = val != null ? val.toString().replace(",", " ") : "";
                    writer.print(valStr);
                    if (col < tableModel.getColumnCount() - 1) writer.print(",");
                }
                writer.println();
            }

            JOptionPane.showMessageDialog(this, "Transactions exported successfully to:\n" + file.getAbsolutePath(), "Export Complete", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to export CSV: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
