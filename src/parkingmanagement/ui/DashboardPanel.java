package parkingmanagement.ui;

import parkingmanagement.ParkingLotService;
import parkingmanagement.VehicleType;
import parkingmanagement.model.ActiveParkingRecord;
import parkingmanagement.model.DashboardStats;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardPanel extends JPanel {

    private final ParkingLotService service;
    private final MainFrame mainFrame;

    private JPanel statsCardsContainer;
    private JProgressBar occupancyBar;
    private JLabel occupancyPercentLabel;
    private JLabel bikeAvailLabel;
    private JLabel carAvailLabel;
    private JLabel truckAvailLabel;

    private JTable recentTable;
    private DefaultTableModel tableModel;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM hh:mm a");

    public DashboardPanel(ParkingLotService service, MainFrame mainFrame) {
        this.service = service;
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout(0, 20));
        setBackground(UITheme.BG_COLOR);
        setBorder(new EmptyBorder(25, 30, 25, 30));

        add(createHeaderPanel(), BorderLayout.NORTH);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(UITheme.BG_COLOR);

        statsCardsContainer = new JPanel(new GridLayout(1, 4, 18, 0));
        statsCardsContainer.setOpaque(false);
        statsCardsContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        contentPanel.add(statsCardsContainer);
        contentPanel.add(Box.createVerticalStrut(20));

        contentPanel.add(createMiddleSection());
        contentPanel.add(Box.createVerticalStrut(20));

        contentPanel.add(createRecentParkedSection());

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);

        refreshData();
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titles = new JPanel();
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.setOpaque(false);

        JLabel title = new JLabel("System Dashboard");
        title.setFont(UITheme.TITLE_FONT);
        title.setForeground(UITheme.TEXT_DARK);

        JLabel subtitle = new JLabel("Real-time parking lot metrics, capacity overview, and active status");
        subtitle.setFont(UITheme.REGULAR_FONT);
        subtitle.setForeground(UITheme.TEXT_MUTED);

        titles.add(title);
        titles.add(Box.createVerticalStrut(4));
        titles.add(subtitle);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JButton refreshBtn = UITheme.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> refreshData());
        actions.add(refreshBtn);

        header.add(titles, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);
        return header;
    }

    private JPanel createMiddleSection() {
        JPanel middle = new JPanel(new GridLayout(1, 2, 20, 0));
        middle.setOpaque(false);
        middle.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

        // Capacity Overview Card
        JPanel capacityCard = UITheme.createCardPanel();
        capacityCard.setLayout(new BorderLayout(0, 12));

        JLabel cardTitle = new JLabel("Occupancy & Capacity");
        cardTitle.setFont(UITheme.HEADING_FONT);
        cardTitle.setForeground(UITheme.TEXT_DARK);

        JPanel progressPanel = new JPanel(new BorderLayout(10, 5));
        progressPanel.setOpaque(false);

        occupancyBar = new JProgressBar(0, 100);
        occupancyBar.setPreferredSize(new Dimension(occupancyBar.getWidth(), 22));
        occupancyBar.setForeground(UITheme.PRIMARY_BLUE);
        occupancyBar.setBackground(new Color(226, 232, 240));
        occupancyBar.setBorderPainted(false);

        occupancyPercentLabel = new JLabel("0% Occupied");
        occupancyPercentLabel.setFont(UITheme.BOLD_FONT);
        occupancyPercentLabel.setForeground(UITheme.TEXT_DARK);

        progressPanel.add(occupancyBar, BorderLayout.CENTER);
        progressPanel.add(occupancyPercentLabel, BorderLayout.EAST);

        // Breakdown pills
        JPanel pillsPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        pillsPanel.setOpaque(false);

        bikeAvailLabel = createTypeBadge("Bike Slots", "0 available");
        carAvailLabel = createTypeBadge("Car Slots", "0 available");
        truckAvailLabel = createTypeBadge("Truck Slots", "0 available");

        pillsPanel.add(bikeAvailLabel);
        pillsPanel.add(carAvailLabel);
        pillsPanel.add(truckAvailLabel);

        capacityCard.add(cardTitle, BorderLayout.NORTH);
        capacityCard.add(progressPanel, BorderLayout.CENTER);
        capacityCard.add(pillsPanel, BorderLayout.SOUTH);

        // Quick Actions Card
        JPanel actionsCard = UITheme.createCardPanel();
        actionsCard.setLayout(new BorderLayout(0, 12));

        JLabel actTitle = new JLabel("Quick Operations");
        actTitle.setFont(UITheme.HEADING_FONT);
        actTitle.setForeground(UITheme.TEXT_DARK);

        JPanel buttonsGrid = new JPanel(new GridLayout(1, 3, 12, 0));
        buttonsGrid.setOpaque(false);

        JButton parkBtn = UITheme.createPrimaryButton("+ Park Vehicle");
        parkBtn.addActionListener(e -> mainFrame.showScreen("PARK"));

        JButton exitBtn = UITheme.createStyledButton("Vehicle Exit", new Color(14, 116, 144), Color.WHITE);
        exitBtn.addActionListener(e -> mainFrame.showScreen("EXIT"));

        JButton gridBtn = UITheme.createSecondaryButton("Lot Grid");
        gridBtn.addActionListener(e -> mainFrame.showScreen("AVAILABLE"));

        buttonsGrid.add(parkBtn);
        buttonsGrid.add(exitBtn);
        buttonsGrid.add(gridBtn);

        actionsCard.add(actTitle, BorderLayout.NORTH);
        actionsCard.add(buttonsGrid, BorderLayout.CENTER);

        middle.add(capacityCard);
        middle.add(actionsCard);

        return middle;
    }

    private JLabel createTypeBadge(String title, String subtitle) {
        JLabel label = new JLabel("<html><div style='text-align:center;'><b>" + title + "</b><br><span style='color:#64748B; font-size:10px;'>" + subtitle + "</span></div></html>", SwingConstants.CENTER);
        label.setFont(UITheme.REGULAR_FONT);
        label.setOpaque(true);
        label.setBackground(new Color(241, 245, 249));
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(8, 10, 8, 10)
        ));
        return label;
    }

    private JPanel createRecentParkedSection() {
        JPanel section = UITheme.createCardPanel();
        section.setLayout(new BorderLayout(0, 12));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel title = new JLabel("Currently Parked Vehicles (Live Snapshot)");
        title.setFont(UITheme.HEADING_FONT);
        title.setForeground(UITheme.TEXT_DARK);

        JButton viewAllBtn = UITheme.createSecondaryButton("View All Parked >");
        viewAllBtn.addActionListener(e -> mainFrame.showScreen("PARKED"));

        top.add(title, BorderLayout.WEST);
        top.add(viewAllBtn, BorderLayout.EAST);

        String[] columns = {"Ticket #", "Vehicle No", "Type", "Slot", "Customer", "Entry Time", "Duration"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        recentTable = new JTable(tableModel);
        UITheme.styleTable(recentTable);

        JScrollPane tableScroll = new JScrollPane(recentTable);
        tableScroll.setPreferredSize(new Dimension(recentTable.getWidth(), 220));
        tableScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1));

        section.add(top, BorderLayout.NORTH);
        section.add(tableScroll, BorderLayout.CENTER);

        return section;
    }

    public void refreshData() {
        DashboardStats stats = service.getDashboardStats();

        // Update Stat Cards
        statsCardsContainer.removeAll();
        statsCardsContainer.add(UITheme.createStatCard(
                "Total Slots", String.valueOf(stats.totalSpaces()),
                stats.totalSpaces() + " configured slots", UITheme.PRIMARY_BLUE, "[P]"
        ));
        statsCardsContainer.add(UITheme.createStatCard(
                "Available", String.valueOf(stats.availableSpaces()),
                "Ready for parking", UITheme.SUCCESS_GREEN, "[OK]"
        ));
        statsCardsContainer.add(UITheme.createStatCard(
                "Occupied", String.valueOf(stats.occupiedSpaces()),
                stats.activeVehicles() + " vehicles parked", UITheme.DANGER_RED, "[BUSY]"
        ));
        statsCardsContainer.add(UITheme.createStatCard(
                "Total Revenue", String.format("Rs. %.2f", stats.totalRevenue()),
                stats.completedTransactions() + " completed exits", UITheme.WARNING_AMBER, "[Rs]"
        ));

        // Update Occupancy Bar
        int occupancyPercent = (int) Math.round(stats.getOccupancyRate());
        occupancyBar.setValue(occupancyPercent);
        occupancyPercentLabel.setText(occupancyPercent + "% Occupied (" + stats.occupiedSpaces() + "/" + stats.totalSpaces() + ")");

        if (occupancyPercent >= 90) {
            occupancyBar.setForeground(UITheme.DANGER_RED);
        } else if (occupancyPercent >= 60) {
            occupancyBar.setForeground(UITheme.WARNING_AMBER);
        } else {
            occupancyBar.setForeground(UITheme.PRIMARY_BLUE);
        }

        // Update Pills
        int bikeAvail = stats.availableByType().getOrDefault(VehicleType.BIKE, 0);
        int bikeTotal = stats.totalByType().getOrDefault(VehicleType.BIKE, 0);
        bikeAvailLabel.setText("<html><div style='text-align:center;'><b>Bike Slots</b><br><span style='color:#64748B; font-size:10px;'>" + bikeAvail + " of " + bikeTotal + " available</span></div></html>");

        int carAvail = stats.availableByType().getOrDefault(VehicleType.CAR, 0);
        int carTotal = stats.totalByType().getOrDefault(VehicleType.CAR, 0);
        carAvailLabel.setText("<html><div style='text-align:center;'><b>Car Slots</b><br><span style='color:#64748B; font-size:10px;'>" + carAvail + " of " + carTotal + " available</span></div></html>");

        int truckAvail = stats.availableByType().getOrDefault(VehicleType.TRUCK, 0);
        int truckTotal = stats.totalByType().getOrDefault(VehicleType.TRUCK, 0);
        truckAvailLabel.setText("<html><div style='text-align:center;'><b>Truck Slots</b><br><span style='color:#64748B; font-size:10px;'>" + truckAvail + " of " + truckTotal + " available</span></div></html>");

        // Update Table
        tableModel.setRowCount(0);
        List<ActiveParkingRecord> parked = service.getParkedVehicles();
        int limit = Math.min(parked.size(), 6);
        for (int i = 0; i < limit; i++) {
            ActiveParkingRecord r = parked.get(i);
            tableModel.addRow(new Object[]{
                    "#" + r.ticketId(),
                    r.registrationNumber(),
                    r.vehicleType(),
                    "Slot " + r.slotId(),
                    r.customerName(),
                    r.entryTime() != null ? r.entryTime().format(TIME_FORMATTER) : "N/A",
                    r.getFormattedDuration()
            });
        }

        statsCardsContainer.revalidate();
        statsCardsContainer.repaint();
    }
}
