package parkingmanagement.ui;

import parkingmanagement.FeeCalculator;
import parkingmanagement.ParkingLotService;
import parkingmanagement.VehicleType;
import parkingmanagement.model.DashboardStats;
import parkingmanagement.model.ParkResult;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Locale;

public class ParkVehiclePanel extends JPanel {

    private final ParkingLotService service;
    private final MainFrame mainFrame;

    private final JTextField nameField;
    private final JTextField phoneField;
    private final JTextField vehicleField;
    private final JComboBox<VehicleType> typeBox;

    private final JLabel ratePreviewLabel;
    private final JLabel availabilityNoticeLabel;

    public ParkVehiclePanel(ParkingLotService service, MainFrame mainFrame) {
        this.service = service;
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout(0, 20));
        setBackground(UITheme.BG_COLOR);
        setBorder(new EmptyBorder(25, 35, 25, 35));

        add(createHeader(), BorderLayout.NORTH);

        JPanel mainContent = new JPanel(new GridLayout(1, 2, 25, 0));
        mainContent.setOpaque(false);

        // Form Panel
        JPanel formCard = UITheme.createCardPanel();
        formCard.setLayout(new BorderLayout(0, 20));

        JLabel formHeading = new JLabel("Vehicle Entry Form");
        formHeading.setFont(UITheme.HEADING_FONT);
        formHeading.setForeground(UITheme.TEXT_DARK);
        formCard.add(formHeading, BorderLayout.NORTH);

        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));
        fieldsPanel.setOpaque(false);

        nameField = UITheme.createStyledTextField();
        phoneField = UITheme.createStyledTextField();
        vehicleField = UITheme.createStyledTextField();

        typeBox = new JComboBox<>(VehicleType.values());
        typeBox.setFont(UITheme.REGULAR_FONT);
        typeBox.setBackground(Color.WHITE);
        typeBox.setPreferredSize(new Dimension(typeBox.getWidth(), 38));

        fieldsPanel.add(createFieldBlock("Customer Full Name *", nameField));
        fieldsPanel.add(Box.createVerticalStrut(14));
        fieldsPanel.add(createFieldBlock("Phone Number (10 Digits) *", phoneField));
        fieldsPanel.add(Box.createVerticalStrut(14));
        fieldsPanel.add(createFieldBlock("Vehicle Registration Number *", vehicleField));
        fieldsPanel.add(Box.createVerticalStrut(14));

        JPanel typePanel = new JPanel(new BorderLayout(5, 5));
        typePanel.setOpaque(false);
        JLabel typeLabel = new JLabel("Vehicle Type *");
        typeLabel.setFont(UITheme.BOLD_FONT);
        typeLabel.setForeground(UITheme.TEXT_DARK);
        typePanel.add(typeLabel, BorderLayout.NORTH);
        typePanel.add(typeBox, BorderLayout.CENTER);

        ratePreviewLabel = new JLabel("Hourly Rate: Rs. 40.00 / hr");
        ratePreviewLabel.setFont(UITheme.REGULAR_FONT);
        ratePreviewLabel.setForeground(UITheme.PRIMARY_BLUE);
        typePanel.add(ratePreviewLabel, BorderLayout.SOUTH);

        fieldsPanel.add(typePanel);
        fieldsPanel.add(Box.createVerticalStrut(20));

        JPanel actionButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        actionButtons.setOpaque(false);

        JButton parkButton = UITheme.createPrimaryButton("+ Park Vehicle & Issue Ticket");
        parkButton.setPreferredSize(new Dimension(240, 42));
        parkButton.addActionListener(e -> parkVehicle());

        JButton clearButton = UITheme.createSecondaryButton("Clear");
        clearButton.addActionListener(e -> clearFields());

        actionButtons.add(parkButton);
        actionButtons.add(clearButton);

        fieldsPanel.add(actionButtons);
        formCard.add(fieldsPanel, BorderLayout.CENTER);

        // Right Info Card
        JPanel availabilityCard = UITheme.createCardPanel();
        availabilityCard.setLayout(new BorderLayout(0, 15));

        JLabel infoHeading = new JLabel("Live Slot Status & Tariff");
        infoHeading.setFont(UITheme.HEADING_FONT);
        infoHeading.setForeground(UITheme.TEXT_DARK);
        availabilityCard.add(infoHeading, BorderLayout.NORTH);

        JPanel infoContent = new JPanel();
        infoContent.setLayout(new BoxLayout(infoContent, BoxLayout.Y_AXIS));
        infoContent.setOpaque(false);

        availabilityNoticeLabel = new JLabel("Checking space availability...", SwingConstants.CENTER);
        availabilityNoticeLabel.setFont(UITheme.HEADING_FONT);
        availabilityNoticeLabel.setOpaque(true);
        availabilityNoticeLabel.setBackground(UITheme.INFO_BG);
        availabilityNoticeLabel.setForeground(UITheme.INFO_TEXT);
        availabilityNoticeLabel.setBorder(new EmptyBorder(15, 15, 15, 15));
        availabilityNoticeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        infoContent.add(availabilityNoticeLabel);
        infoContent.add(Box.createVerticalStrut(20));

        // Tariff Card
        JPanel tariffTable = new JPanel(new GridLayout(4, 2, 10, 8));
        tariffTable.setOpaque(false);
        tariffTable.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(12, 14, 12, 14)
        ));

        tariffTable.add(new JLabel("Vehicle Category"));
        tariffTable.add(new JLabel("Standard Hourly Rate"));
        tariffTable.add(new JLabel("Bike"));
        tariffTable.add(new JLabel("Rs. 20.00 / hr"));
        tariffTable.add(new JLabel("Car"));
        tariffTable.add(new JLabel("Rs. 40.00 / hr"));
        tariffTable.add(new JLabel("Truck"));
        tariffTable.add(new JLabel("Rs. 60.00 / hr"));

        infoContent.add(tariffTable);
        infoContent.add(Box.createVerticalStrut(20));

        // Guidelines
        JLabel rulesTitle = new JLabel("Entry Instructions:");
        rulesTitle.setFont(UITheme.BOLD_FONT);
        rulesTitle.setForeground(UITheme.TEXT_DARK);

        JTextArea rulesText = new JTextArea(
                "- Minimum billing duration is 1 hour.\n"
                        + "- Parking space is automatically allocated based on selected category.\n"
                        + "- Retain the generated ticket for vehicle checkout.\n"
                        + "- Park within the marked boundaries."
        );
        rulesText.setFont(UITheme.REGULAR_FONT);
        rulesText.setForeground(UITheme.TEXT_MUTED);
        rulesText.setEditable(false);
        rulesText.setOpaque(false);
        rulesText.setLineWrap(true);
        rulesText.setWrapStyleWord(true);

        infoContent.add(rulesTitle);
        infoContent.add(Box.createVerticalStrut(5));
        infoContent.add(rulesText);

        availabilityCard.add(infoContent, BorderLayout.CENTER);

        mainContent.add(formCard);
        mainContent.add(availabilityCard);

        add(mainContent, BorderLayout.CENTER);

        typeBox.addActionListener(e -> updateTypeInfo());
        updateTypeInfo();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Park Vehicle");
        title.setFont(UITheme.TITLE_FONT);
        title.setForeground(UITheme.TEXT_DARK);

        JLabel subtitle = new JLabel("Register vehicle entry and automatically allocate an available parking slot");
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

    private JPanel createFieldBlock(String labelText, JTextField field) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(UITheme.BOLD_FONT);
        label.setForeground(UITheme.TEXT_DARK);

        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    public void updateTypeInfo() {
        VehicleType selected = (VehicleType) typeBox.getSelectedItem();
        if (selected == null) return;

        double rate = FeeCalculator.getHourlyRate(selected);
        ratePreviewLabel.setText(String.format("Hourly Rate: Rs. %.2f / hr", rate));

        DashboardStats stats = service.getDashboardStats();
        int available = stats.availableByType().getOrDefault(selected, 0);

        if (available > 0) {
            availabilityNoticeLabel.setText(available + " " + selected + " space(s) available");
            availabilityNoticeLabel.setBackground(UITheme.SUCCESS_BG);
            availabilityNoticeLabel.setForeground(UITheme.SUCCESS_TEXT);
        } else {
            availabilityNoticeLabel.setText("No available spaces for " + selected);
            availabilityNoticeLabel.setBackground(UITheme.DANGER_BG);
            availabilityNoticeLabel.setForeground(UITheme.DANGER_TEXT);
        }
    }

    private void parkVehicle() {
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String vehicleNumber = vehicleField.getText().trim().toUpperCase(Locale.ROOT);
        VehicleType type = (VehicleType) typeBox.getSelectedItem();

        if (name.isEmpty()) {
            showError("Customer name cannot be empty.");
            nameField.requestFocus();
            return;
        }

        if (!phone.matches("\\d{10}")) {
            showError("Please enter a valid 10-digit mobile number.");
            phoneField.requestFocus();
            return;
        }

        if (vehicleNumber.isEmpty()) {
            showError("Vehicle registration number cannot be empty.");
            vehicleField.requestFocus();
            return;
        }

        ParkResult result = service.parkVehicle(name, phone, vehicleNumber, type);

        if (!result.success()) {
            showError(result.message());
            return;
        }

        ReceiptDialog.showParkingTicket(mainFrame, result);
        clearFields();
        mainFrame.refreshAllScreens();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Input Error", JOptionPane.ERROR_MESSAGE);
    }

    public void clearFields() {
        nameField.setText("");
        phoneField.setText("");
        vehicleField.setText("");
        typeBox.setSelectedIndex(0);
        updateTypeInfo();
    }
}
