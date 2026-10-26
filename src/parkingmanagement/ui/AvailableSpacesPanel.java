package parkingmanagement.ui;

import parkingmanagement.FeeCalculator;
import parkingmanagement.ParkingLotService;
import parkingmanagement.ParkingSpace;
import parkingmanagement.model.ActiveParkingRecord;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AvailableSpacesPanel extends JPanel {

    private final ParkingLotService service;
    private final MainFrame mainFrame;

    private final JPanel gridContainer;
    private final JTable spacesTable;
    private final DefaultTableModel tableModel;
    private final JLabel summaryLabel;

    public AvailableSpacesPanel(ParkingLotService service, MainFrame mainFrame) {
        this.service = service;
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_COLOR);
        setBorder(new EmptyBorder(25, 35, 25, 35));

        add(createHeader(), BorderLayout.NORTH);

        // Center tabs: Visual Grid & Table View
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UITheme.BOLD_FONT);
        tabbedPane.setBackground(Color.WHITE);

        // Visual Grid Tab
        JPanel visualTab = new JPanel(new BorderLayout(0, 15));
        visualTab.setBackground(UITheme.BG_COLOR);
        visualTab.setBorder(new EmptyBorder(15, 0, 0, 0));

        gridContainer = new JPanel(new GridLayout(0, 3, 16, 16));
        gridContainer.setBackground(UITheme.BG_COLOR);

        JScrollPane gridScroll = new JScrollPane(gridContainer);
        gridScroll.setBorder(null);
        gridScroll.setOpaque(false);
        gridScroll.getViewport().setOpaque(false);
        gridScroll.getVerticalScrollBar().setUnitIncrement(16);

        visualTab.add(gridScroll, BorderLayout.CENTER);

        // Table View Tab
        JPanel tableTab = new JPanel(new BorderLayout(0, 15));
        tableTab.setBackground(UITheme.BG_COLOR);
        tableTab.setBorder(new EmptyBorder(15, 0, 0, 0));

        String[] columns = {"Slot ID", "Supported Category", "Hourly Tariff", "Status", "Current Occupant"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        spacesTable = new JTable(tableModel);
        UITheme.styleTable(spacesTable);

        JScrollPane tableScroll = new JScrollPane(spacesTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1));
        tableTab.add(tableScroll, BorderLayout.CENTER);

        tabbedPane.addTab("Visual Lot Map", visualTab);
        tabbedPane.addTab("Table View", tableTab);

        add(tabbedPane, BorderLayout.CENTER);

        // Bottom Summary Bar
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);
        summaryLabel = new JLabel("Loading parking space status...");
        summaryLabel.setFont(UITheme.BOLD_FONT);
        summaryLabel.setForeground(UITheme.TEXT_MUTED);
        bottomBar.add(summaryLabel, BorderLayout.WEST);

        add(bottomBar, BorderLayout.SOUTH);

        refreshSpaces();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Parking Spaces & Layout");
        title.setFont(UITheme.TITLE_FONT);
        title.setForeground(UITheme.TEXT_DARK);

        JLabel subtitle = new JLabel("Monitor real-time parking slot occupancy and physical bay allocation");
        subtitle.setFont(UITheme.REGULAR_FONT);
        subtitle.setForeground(UITheme.TEXT_MUTED);

        JPanel titles = new JPanel();
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.setOpaque(false);
        titles.add(title);
        titles.add(Box.createVerticalStrut(4));
        titles.add(subtitle);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controls.setOpaque(false);

        JButton refreshBtn = UITheme.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> refreshSpaces());

        controls.add(refreshBtn);

        header.add(titles, BorderLayout.WEST);
        header.add(controls, BorderLayout.EAST);
        return header;
    }

    public void refreshSpaces() {
        List<ParkingSpace> spaces = service.getAllSpaces();
        List<ActiveParkingRecord> parkedVehicles = service.getParkedVehicles();

        Map<Integer, ActiveParkingRecord> occupiedSlotsMap = new HashMap<>();
        for (ActiveParkingRecord r : parkedVehicles) {
            occupiedSlotsMap.put(r.slotId(), r);
        }

        // Populate Table View
        tableModel.setRowCount(0);
        int availableCount = 0;
        int occupiedCount = 0;

        for (ParkingSpace space : spaces) {
            boolean isAvail = space.isAvailable();
            if (isAvail) availableCount++;
            else occupiedCount++;

            ActiveParkingRecord occupant = occupiedSlotsMap.get(space.getSlotId());
            String occupantStr = occupant != null ? occupant.registrationNumber() + " (" + occupant.customerName() + ")" : "-";
            double rate = FeeCalculator.getHourlyRate(space.getSupportedType());

            tableModel.addRow(new Object[]{
                    "Slot " + space.getSlotId(),
                    space.getSupportedType(),
                    String.format("Rs. %.2f/hr", rate),
                    isAvail ? "AVAILABLE" : "OCCUPIED",
                    occupantStr
            });
        }

        // Populate Visual Grid
        gridContainer.removeAll();
        for (ParkingSpace space : spaces) {
            ActiveParkingRecord occupant = occupiedSlotsMap.get(space.getSlotId());
            gridContainer.add(createSlotCard(space, occupant));
        }

        summaryLabel.setText(String.format(
                "Total Slots: %d  |  Available: %d  |  Occupied: %d",
                spaces.size(), availableCount, occupiedCount
        ));

        gridContainer.revalidate();
        gridContainer.repaint();
    }

    private JPanel createSlotCard(ParkingSpace space, ActiveParkingRecord occupant) {
        boolean isAvail = space.isAvailable();
        Color accentColor = isAvail ? UITheme.SUCCESS_GREEN : UITheme.DANGER_RED;
        Color badgeBg = isAvail ? UITheme.SUCCESS_BG : UITheme.DANGER_BG;
        Color badgeText = isAvail ? UITheme.SUCCESS_TEXT : UITheme.DANGER_TEXT;

        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 5, 0, 0, accentColor),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                        new EmptyBorder(14, 16, 14, 16)
                )
        ));

        // Top Row: Slot Number & Status Badge
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel slotNum = new JLabel("Slot " + space.getSlotId());
        slotNum.setFont(UITheme.HEADING_FONT);
        slotNum.setForeground(UITheme.TEXT_DARK);

        JLabel statusBadge = new JLabel(isAvail ? " AVAILABLE " : " OCCUPIED ");
        statusBadge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        statusBadge.setOpaque(true);
        statusBadge.setBackground(badgeBg);
        statusBadge.setForeground(badgeText);
        statusBadge.setBorder(new EmptyBorder(4, 6, 4, 6));

        top.add(slotNum, BorderLayout.WEST);
        top.add(statusBadge, BorderLayout.EAST);

        // Center: Type & Details
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setOpaque(false);

        String typeName = "[" + space.getSupportedType() + "]";

        JLabel typeLabel = new JLabel(typeName);
        typeLabel.setFont(UITheme.BOLD_FONT);
        typeLabel.setForeground(UITheme.TEXT_MUTED);
        center.add(typeLabel);
        center.add(Box.createVerticalStrut(6));

        if (!isAvail && occupant != null) {
            JLabel regLabel = new JLabel("Reg: " + occupant.registrationNumber());
            regLabel.setFont(UITheme.BOLD_FONT);
            regLabel.setForeground(UITheme.TEXT_DARK);

            JLabel custLabel = new JLabel("Customer: " + occupant.customerName());
            custLabel.setFont(UITheme.REGULAR_FONT);
            custLabel.setForeground(UITheme.TEXT_MUTED);

            center.add(regLabel);
            center.add(Box.createVerticalStrut(2));
            center.add(custLabel);
        } else {
            double rate = FeeCalculator.getHourlyRate(space.getSupportedType());
            JLabel rateLabel = new JLabel(String.format("Rate: Rs. %.2f / hr", rate));
            rateLabel.setFont(UITheme.REGULAR_FONT);
            rateLabel.setForeground(UITheme.PRIMARY_BLUE);
            center.add(rateLabel);
        }

        // Bottom Action Button
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);

        if (isAvail) {
            JButton parkBtn = UITheme.createPrimaryButton("+ Park Here");
            parkBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            parkBtn.setPreferredSize(new Dimension(100, 32));
            parkBtn.addActionListener(e -> mainFrame.showScreen("PARK"));
            bottom.add(parkBtn, BorderLayout.CENTER);
        } else if (occupant != null) {
            JButton exitBtn = UITheme.createStyledButton("Checkout", new Color(14, 116, 144), Color.WHITE);
            exitBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            exitBtn.setPreferredSize(new Dimension(100, 32));
            exitBtn.addActionListener(e -> {
                mainFrame.getExitPanel().setVehicleNumber(occupant.registrationNumber());
                mainFrame.showScreen("EXIT");
            });
            bottom.add(exitBtn, BorderLayout.CENTER);
        }

        card.add(top, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);
        card.add(bottom, BorderLayout.SOUTH);

        return card;
    }
}
