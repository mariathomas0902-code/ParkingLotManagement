package parkingmanagement.ui;

import parkingmanagement.ParkingLotService;
import parkingmanagement.model.ActiveParkingRecord;
import parkingmanagement.model.ExitResult;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class ExitVehiclePanel extends JPanel {

    private final ParkingLotService service;
    private final MainFrame mainFrame;

    private final JTextField vehicleField;
    private final JComboBox<String> activeVehiclesBox;

    private final JLabel previewTicketLabel;
    private final JLabel previewCustomerLabel;
    private final JLabel previewVehicleLabel;
    private final JLabel previewSlotLabel;
    private final JLabel previewEntryTimeLabel;
    private final JLabel previewDurationLabel;
    private final JLabel previewAmountLabel;
    private final JButton completeExitButton;

    private ExitResult currentPreview;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm a");

    public ExitVehiclePanel(ParkingLotService service, MainFrame mainFrame) {
        this.service = service;
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout(0, 20));
        setBackground(UITheme.BG_COLOR);
        setBorder(new EmptyBorder(25, 35, 25, 35));

        add(createHeader(), BorderLayout.NORTH);

        JPanel mainContent = new JPanel(new GridLayout(1, 2, 25, 0));
        mainContent.setOpaque(false);

        // Left Form
        JPanel formCard = UITheme.createCardPanel();
        formCard.setLayout(new BorderLayout(0, 20));

        JLabel formTitle = new JLabel("Lookup Parked Vehicle");
        formTitle.setFont(UITheme.HEADING_FONT);
        formTitle.setForeground(UITheme.TEXT_DARK);
        formCard.add(formTitle, BorderLayout.NORTH);

        JPanel formFields = new JPanel();
        formFields.setLayout(new BoxLayout(formFields, BoxLayout.Y_AXIS));
        formFields.setOpaque(false);

        // Active vehicles dropdown
        JPanel dropdownPanel = new JPanel(new BorderLayout(5, 5));
        dropdownPanel.setOpaque(false);
        JLabel dropLabel = new JLabel("Select from Parked Vehicles (Quick Pick):");
        dropLabel.setFont(UITheme.BOLD_FONT);
        dropLabel.setForeground(UITheme.TEXT_DARK);

        activeVehiclesBox = new JComboBox<>();
        activeVehiclesBox.setFont(UITheme.REGULAR_FONT);
        activeVehiclesBox.setBackground(Color.WHITE);
        activeVehiclesBox.setPreferredSize(new Dimension(activeVehiclesBox.getWidth(), 38));

        dropdownPanel.add(dropLabel, BorderLayout.NORTH);
        dropdownPanel.add(activeVehiclesBox, BorderLayout.CENTER);

        formFields.add(dropdownPanel);
        formFields.add(Box.createVerticalStrut(15));

        // Or enter manually
        JPanel manualPanel = new JPanel(new BorderLayout(5, 5));
        manualPanel.setOpaque(false);
        JLabel manualLabel = new JLabel("Or Enter Registration Number Manually:");
        manualLabel.setFont(UITheme.BOLD_FONT);
        manualLabel.setForeground(UITheme.TEXT_DARK);

        vehicleField = UITheme.createStyledTextField();

        manualPanel.add(manualLabel, BorderLayout.NORTH);
        manualPanel.add(vehicleField, BorderLayout.CENTER);

        formFields.add(manualPanel);
        formFields.add(Box.createVerticalStrut(20));

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonRow.setOpaque(false);

        JButton calcBtn = UITheme.createPrimaryButton("Calculate / Preview Fee");
        calcBtn.setPreferredSize(new Dimension(220, 42));
        calcBtn.addActionListener(e -> calculateFeePreview());

        JButton clearBtn = UITheme.createSecondaryButton("Clear");
        clearBtn.addActionListener(e -> clearForm());

        buttonRow.add(calcBtn);
        buttonRow.add(clearBtn);

        formFields.add(buttonRow);
        formCard.add(formFields, BorderLayout.CENTER);

        // Right Preview / Checkout Card
        JPanel previewCard = UITheme.createCardPanel();
        previewCard.setLayout(new BorderLayout(0, 15));

        JLabel billTitle = new JLabel("Payment Summary & Fee Breakdown");
        billTitle.setFont(UITheme.HEADING_FONT);
        billTitle.setForeground(UITheme.TEXT_DARK);
        previewCard.add(billTitle, BorderLayout.NORTH);

        JPanel billBody = new JPanel();
        billBody.setLayout(new BoxLayout(billBody, BoxLayout.Y_AXIS));
        billBody.setOpaque(false);

        JPanel detailsTable = new JPanel(new GridLayout(6, 2, 10, 10));
        detailsTable.setOpaque(false);
        detailsTable.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(12, 16, 12, 16)
        ));

        previewTicketLabel = new JLabel("-");
        previewCustomerLabel = new JLabel("-");
        previewVehicleLabel = new JLabel("-");
        previewSlotLabel = new JLabel("-");
        previewEntryTimeLabel = new JLabel("-");
        previewDurationLabel = new JLabel("-");

        detailsTable.add(createLabel("Ticket ID:"));
        detailsTable.add(previewTicketLabel);
        detailsTable.add(createLabel("Customer:"));
        detailsTable.add(previewCustomerLabel);
        detailsTable.add(createLabel("Vehicle:"));
        detailsTable.add(previewVehicleLabel);
        detailsTable.add(createLabel("Parking Slot:"));
        detailsTable.add(previewSlotLabel);
        detailsTable.add(createLabel("Entry Time:"));
        detailsTable.add(previewEntryTimeLabel);
        detailsTable.add(createLabel("Elapsed Duration:"));
        detailsTable.add(previewDurationLabel);

        billBody.add(detailsTable);
        billBody.add(Box.createVerticalStrut(15));

        // Total Amount Callout
        JPanel amountCard = new JPanel(new BorderLayout());
        amountCard.setBackground(new Color(236, 253, 245));
        amountCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.SUCCESS_GREEN, 1),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JLabel amtTitle = new JLabel("TOTAL AMOUNT DUE");
        amtTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        amtTitle.setForeground(UITheme.SUCCESS_TEXT);

        previewAmountLabel = new JLabel("Rs. 0.00");
        previewAmountLabel.setFont(UITheme.STAT_VALUE_FONT);
        previewAmountLabel.setForeground(UITheme.SUCCESS_TEXT);

        amountCard.add(amtTitle, BorderLayout.NORTH);
        amountCard.add(previewAmountLabel, BorderLayout.CENTER);

        billBody.add(amountCard);
        billBody.add(Box.createVerticalStrut(20));

        completeExitButton = UITheme.createStyledButton("Confirm Payment & Complete Exit", UITheme.SUCCESS_GREEN, Color.WHITE);
        completeExitButton.setFont(UITheme.HEADING_FONT);
        completeExitButton.setPreferredSize(new Dimension(Integer.MAX_VALUE, 46));
        completeExitButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        completeExitButton.setEnabled(false);
        completeExitButton.addActionListener(e -> completeExit());

        billBody.add(completeExitButton);

        previewCard.add(billBody, BorderLayout.CENTER);

        mainContent.add(formCard);
        mainContent.add(previewCard);

        add(mainContent, BorderLayout.CENTER);

        activeVehiclesBox.addActionListener(e -> {
            String selected = (String) activeVehiclesBox.getSelectedItem();
            if (selected != null && !selected.startsWith("--") && selected.contains(" ")) {
                String reg = selected.split(" ")[0].trim();
                vehicleField.setText(reg);
            }
        });

        refreshActiveList();
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UITheme.BOLD_FONT);
        label.setForeground(UITheme.TEXT_MUTED);
        return label;
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Vehicle Exit & Checkout");
        title.setFont(UITheme.TITLE_FONT);
        title.setForeground(UITheme.TEXT_DARK);

        JLabel subtitle = new JLabel("Calculate parking fees based on duration and vehicle category, then process exit");
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

    public void refreshActiveList() {
        activeVehiclesBox.removeAllItems();
        activeVehiclesBox.addItem("-- Select currently parked vehicle --");

        List<ActiveParkingRecord> parked = service.getParkedVehicles();
        for (ActiveParkingRecord r : parked) {
            activeVehiclesBox.addItem(r.registrationNumber() + " (Slot " + r.slotId() + " - " + r.vehicleType() + ")");
        }
    }

    public void setVehicleNumber(String vehicleNumber) {
        vehicleField.setText(vehicleNumber);
        calculateFeePreview();
    }

    private void calculateFeePreview() {
        String vehicleNumber = vehicleField.getText().trim().toUpperCase(Locale.ROOT);
        if (vehicleNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter or select a vehicle registration number.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        ExitResult preview = service.previewExit(vehicleNumber);
        if (!preview.success()) {
            JOptionPane.showMessageDialog(this, preview.message(), "Not Found", JOptionPane.WARNING_MESSAGE);
            completeExitButton.setEnabled(false);
            return;
        }

        currentPreview = preview;

        previewTicketLabel.setText("#" + preview.ticketId());
        previewCustomerLabel.setText(preview.customerName() != null ? preview.customerName() : "Customer");
        previewVehicleLabel.setText(preview.vehicleNumber() + " (" + preview.vehicleType() + ")");
        previewSlotLabel.setText("Slot " + preview.slotId());
        previewEntryTimeLabel.setText(preview.entryTime() != null ? preview.entryTime().format(FORMATTER) : "-");
        previewDurationLabel.setText(preview.durationText());
        previewAmountLabel.setText(String.format("Rs. %.2f", preview.fee()));

        completeExitButton.setEnabled(true);
    }

    private void completeExit() {
        if (currentPreview == null) return;

        String vehicleNumber = currentPreview.vehicleNumber();
        int confirm = JOptionPane.showConfirmDialog(
                this,
                String.format("Confirm vehicle exit for %s?\nTotal parking fee: Rs. %.2f", vehicleNumber, currentPreview.fee()),
                "Confirm Exit & Payment",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        ExitResult result = service.exitVehicle(vehicleNumber);
        if (!result.success()) {
            JOptionPane.showMessageDialog(this, result.message(), "Exit Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        ReceiptDialog.showExitInvoice(mainFrame, result);
        clearForm();
        mainFrame.refreshAllScreens();
    }

    public void clearForm() {
        vehicleField.setText("");
        if (activeVehiclesBox.getItemCount() > 0) {
            activeVehiclesBox.setSelectedIndex(0);
        }
        currentPreview = null;
        previewTicketLabel.setText("-");
        previewCustomerLabel.setText("-");
        previewVehicleLabel.setText("-");
        previewSlotLabel.setText("-");
        previewEntryTimeLabel.setText("-");
        previewDurationLabel.setText("-");
        previewAmountLabel.setText("Rs. 0.00");
        completeExitButton.setEnabled(false);
        refreshActiveList();
    }
}
