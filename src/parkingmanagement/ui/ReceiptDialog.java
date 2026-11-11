package parkingmanagement.ui;

import parkingmanagement.FeeCalculator;
import parkingmanagement.model.ExitResult;
import parkingmanagement.model.ParkResult;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.time.format.DateTimeFormatter;

public class ReceiptDialog extends JDialog {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm a");

    public ReceiptDialog(Frame owner, String title, String receiptText) {
        super(owner, title, true);
        setSize(460, 520);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.BG_COLOR);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.SIDEBAR_BG);
        headerPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UITheme.HEADING_FONT);
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JTextArea textArea = new JTextArea(receiptText);
        textArea.setFont(UITheme.MONO_FONT);
        textArea.setEditable(false);
        textArea.setBackground(Color.WHITE);
        textArea.setBorder(new EmptyBorder(15, 15, 15, 15));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        buttonPanel.setBackground(UITheme.BG_COLOR);
        buttonPanel.setBorder(new EmptyBorder(0, 20, 12, 20));

        JButton copyButton = UITheme.createSecondaryButton("Copy to Clipboard");
        copyButton.addActionListener(e -> {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                    new StringSelection(receiptText), null
            );
            JOptionPane.showMessageDialog(this, "Receipt copied to clipboard!", "Copied", JOptionPane.INFORMATION_MESSAGE);
        });

        JButton closeButton = UITheme.createPrimaryButton("Close");
        closeButton.addActionListener(e -> dispose());

        buttonPanel.add(copyButton);
        buttonPanel.add(closeButton);

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public static void showParkingTicket(Frame owner, ParkResult result) {
        if (!result.success()) return;

        String formattedTime = result.entryTime() != null ? result.entryTime().format(FORMATTER) : "N/A";
        double hourlyRate = FeeCalculator.getHourlyRate(result.vehicleType());

        String receipt = """
                ============================================
                         SMART PARKING MANAGEMENT
                       *** PARKING ENTRY TICKET ***
                ============================================
                Ticket No       : #%d
                Entry Date/Time : %s
                --------------------------------------------
                Slot Assigned   : Slot %d
                Vehicle Number  : %s
                Vehicle Type    : %s
                Hourly Rate     : Rs. %.2f / hr
                --------------------------------------------
                Customer Name   : %s
                --------------------------------------------
                * Please retain this ticket for vehicle exit.
                * Park at your own risk.
                ============================================
                """.formatted(
                result.ticketId(),
                formattedTime,
                result.slotId(),
                result.vehicleNumber(),
                result.vehicleType(),
                hourlyRate,
                result.customerName()
        );

        ReceiptDialog dialog = new ReceiptDialog(owner, "Parking Entry Ticket", receipt);
        dialog.setVisible(true);
    }

    public static void showExitInvoice(Frame owner, ExitResult result) {
        if (!result.success()) return;

        String entryStr = result.entryTime() != null ? result.entryTime().format(FORMATTER) : "N/A";
        String exitStr = result.exitTime() != null ? result.exitTime().format(FORMATTER) : "N/A";
        double hourlyRate = FeeCalculator.getHourlyRate(result.vehicleType());

        String receipt = """
                ============================================
                         SMART PARKING MANAGEMENT
                        *** PARKING EXIT INVOICE ***
                ============================================
                Ticket No       : #%d
                Customer Name   : %s
                --------------------------------------------
                Vehicle Number  : %s
                Vehicle Type    : %s (Rs. %.2f/hr)
                Slot Number     : Slot %d
                --------------------------------------------
                Entry Time      : %s
                Exit Time       : %s
                Total Duration  : %s
                --------------------------------------------
                TOTAL AMOUNT DUE: Rs. %.2f
                Payment Status  : PAID
                --------------------------------------------
                Thank you for parking with us!
                Drive safely and visit again.
                ============================================
                """.formatted(
                result.ticketId(),
                result.customerName() != null ? result.customerName() : "Customer",
                result.vehicleNumber(),
                result.vehicleType(),
                hourlyRate,
                result.slotId(),
                entryStr,
                exitStr,
                result.durationText(),
                result.fee()
        );

        ReceiptDialog dialog = new ReceiptDialog(owner, "Exit Payment Invoice", receipt);
        dialog.setVisible(true);
    }
}
