package parkingmanagement.ui;

import parkingmanagement.ParkingLotService;
import parkingmanagement.model.ActiveParkingRecord;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ParkedVehiclesPanel extends JPanel {

    private final ParkingLotService service;
    private final MainFrame mainFrame;

    private final JTable table;
    private final DefaultTableModel tableModel;
    private final TableRowSorter<DefaultTableModel> sorter;
    private final JTextField searchField;
    private final JLabel countLabel;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MMM hh:mm a");

    public ParkedVehiclesPanel(ParkingLotService service, MainFrame mainFrame) {
        this.service = service;
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_COLOR);
        setBorder(new EmptyBorder(25, 35, 25, 35));

        add(createHeader(), BorderLayout.NORTH);

        // Center Content Card
        JPanel centerCard = UITheme.createCardPanel();
        centerCard.setLayout(new BorderLayout(0, 14));

        // Search & Filter Toolbar
        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        toolbar.setOpaque(false);

        JPanel searchBox = new JPanel(new BorderLayout(8, 0));
        searchBox.setOpaque(false);
        JLabel searchIcon = new JLabel("Search:");
        searchIcon.setFont(UITheme.BOLD_FONT);
        searchIcon.setForeground(UITheme.TEXT_MUTED);

        searchField = UITheme.createStyledTextField();
        searchField.setPreferredSize(new Dimension(280, 36));

        searchBox.add(searchIcon, BorderLayout.WEST);
        searchBox.add(searchField, BorderLayout.CENTER);

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightControls.setOpaque(false);

        JButton exitSelectedBtn = UITheme.createStyledButton("Exit Selected Vehicle", new Color(14, 116, 144), Color.WHITE);
        exitSelectedBtn.addActionListener(e -> exitSelectedVehicle());

        JButton refreshBtn = UITheme.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> refreshParkedList());

        rightControls.add(exitSelectedBtn);
        rightControls.add(refreshBtn);

        toolbar.add(searchBox, BorderLayout.WEST);
        toolbar.add(rightControls, BorderLayout.EAST);

        centerCard.add(toolbar, BorderLayout.NORTH);

        // Table
        String[] columns = {
                "Ticket #", "Vehicle Number", "Type", "Slot", "Customer Name", "Phone Number", "Entry Time", "Duration", "Estimated Fee"
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
                filterTable();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterTable();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterTable();
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1));
        centerCard.add(scrollPane, BorderLayout.CENTER);

        // Bottom Count Bar
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);
        countLabel = new JLabel("Total Active Parked: 0");
        countLabel.setFont(UITheme.BOLD_FONT);
        countLabel.setForeground(UITheme.TEXT_MUTED);
        bottomBar.add(countLabel, BorderLayout.WEST);

        centerCard.add(bottomBar, BorderLayout.SOUTH);

        add(centerCard, BorderLayout.CENTER);

        refreshParkedList();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Currently Parked Vehicles");
        title.setFont(UITheme.TITLE_FONT);
        title.setForeground(UITheme.TEXT_DARK);

        JLabel subtitle = new JLabel("Manage active parking sessions, check elapsed time, and initiate vehicle checkout");
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

    private void filterTable() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + query));
        }
    }

    public void refreshParkedList() {
        tableModel.setRowCount(0);
        List<ActiveParkingRecord> list = service.getParkedVehicles();

        for (ActiveParkingRecord r : list) {
            tableModel.addRow(new Object[]{
                    "#" + r.ticketId(),
                    r.registrationNumber(),
                    r.vehicleType(),
                    "Slot " + r.slotId(),
                    r.customerName(),
                    r.phoneNumber(),
                    r.entryTime() != null ? r.entryTime().format(FORMATTER) : "N/A",
                    r.getFormattedDuration(),
                    String.format("Rs. %.2f", r.getEstimatedFee())
            });
        }

        countLabel.setText("Total Active Parked Vehicles: " + list.size());
    }

    private void exitSelectedVehicle() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a vehicle from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);
        String vehicleNumber = (String) tableModel.getValueAt(modelRow, 1);

        mainFrame.getExitPanel().setVehicleNumber(vehicleNumber);
        mainFrame.showScreen("EXIT");
    }
}
