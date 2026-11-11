package parkingmanagement.ui;

import parkingmanagement.ParkingLotService;
import parkingmanagement.model.CustomerRecord;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

public class CustomerDirectoryPanel extends JPanel {

    private final ParkingLotService service;
    private final JTable table;
    private final DefaultTableModel tableModel;
    private final TableRowSorter<DefaultTableModel> sorter;
    private final JTextField searchField;
    private final JLabel countLabel;

    public CustomerDirectoryPanel(ParkingLotService service) {
        this.service = service;

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_COLOR);
        setBorder(new EmptyBorder(25, 35, 25, 35));

        add(createHeader(), BorderLayout.NORTH);

        JPanel centerCard = UITheme.createCardPanel();
        centerCard.setLayout(new BorderLayout(0, 14));

        // Toolbar
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

        JButton refreshBtn = UITheme.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> refreshCustomers());

        toolbar.add(searchBox, BorderLayout.WEST);
        toolbar.add(refreshBtn, BorderLayout.EAST);
        centerCard.add(toolbar, BorderLayout.NORTH);

        // Table
        String[] columns = {"Customer ID", "Full Name", "Phone Number", "Email Address", "Registered Vehicle", "Vehicle Category"};
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

        // Bottom Bar
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);
        countLabel = new JLabel("Total Registered Customers: 0");
        countLabel.setFont(UITheme.BOLD_FONT);
        countLabel.setForeground(UITheme.TEXT_MUTED);
        bottomBar.add(countLabel, BorderLayout.WEST);

        centerCard.add(bottomBar, BorderLayout.SOUTH);

        add(centerCard, BorderLayout.CENTER);

        refreshCustomers();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Customer Directory");
        title.setFont(UITheme.TITLE_FONT);
        title.setForeground(UITheme.TEXT_DARK);

        JLabel subtitle = new JLabel("Directory of registered parking customers and their linked vehicles");
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

    public void refreshCustomers() {
        tableModel.setRowCount(0);
        List<CustomerRecord> customers = service.getAllCustomers();

        for (CustomerRecord c : customers) {
            tableModel.addRow(new Object[]{
                    "#" + c.customerId(),
                    c.name(),
                    c.phoneNumber(),
                    c.email() != null ? c.email() : "-",
                    c.registrationNumber() != null ? c.registrationNumber() : "-",
                    c.vehicleType() != null ? c.vehicleType().name() : "-"
            });
        }

        countLabel.setText("Total Registered Customers: " + customers.size());
    }
}
