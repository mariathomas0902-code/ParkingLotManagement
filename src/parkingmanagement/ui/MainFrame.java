package parkingmanagement.ui;

import parkingmanagement.ParkingLotService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class MainFrame extends JFrame {

    private static final Color SIDEBAR_BACKGROUND =
            new Color(15, 23, 42);

    private static final Color BUTTON_BACKGROUND =
            new Color(30, 41, 59);

    private static final Color SELECTED_BACKGROUND =
            new Color(37, 99, 235);

    private static final Color SELECTED_INDICATOR =
            new Color(147, 197, 253);

    private final CardLayout cardLayout;
    private final JPanel contentPanel;
    private final ParkingLotService service;

    private DashboardPanel dashboardPanel;
    private ParkVehiclePanel parkVehiclePanel;
    private ExitVehiclePanel exitVehiclePanel;
    private AvailableSpacesPanel availableSpacesPanel;
    private ParkedVehiclesPanel parkedVehiclesPanel;
    private TransactionHistoryPanel transactionHistoryPanel;
    private CustomerDirectoryPanel customerDirectoryPanel;

    private final Map<String, JButton> menuButtons = new HashMap<>();

    private JLabel dbStatusLabel;

    public MainFrame() {
        this.service = new ParkingLotService();

        setTitle("Smart Parking Lot Management System");
        setSize(1180, 740);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(UITheme.BG_COLOR);

        initScreens();

        JPanel rootContainer = new JPanel(new BorderLayout());
        rootContainer.add(createSidebar(), BorderLayout.WEST);
        rootContainer.add(contentPanel, BorderLayout.CENTER);

        setContentPane(rootContainer);

        showScreen("DASHBOARD");
    }

    private void initScreens() {
        dashboardPanel = new DashboardPanel(service, this);
        parkVehiclePanel = new ParkVehiclePanel(service, this);
        exitVehiclePanel = new ExitVehiclePanel(service, this);
        availableSpacesPanel = new AvailableSpacesPanel(service, this);
        parkedVehiclesPanel = new ParkedVehiclesPanel(service, this);
        transactionHistoryPanel =
                new TransactionHistoryPanel(service, this);
        customerDirectoryPanel = new CustomerDirectoryPanel(service);

        contentPanel.add(dashboardPanel, "DASHBOARD");
        contentPanel.add(parkVehiclePanel, "PARK");
        contentPanel.add(exitVehiclePanel, "EXIT");
        contentPanel.add(availableSpacesPanel, "AVAILABLE");
        contentPanel.add(parkedVehiclesPanel, "PARKED");
        contentPanel.add(transactionHistoryPanel, "HISTORY");
        contentPanel.add(customerDirectoryPanel, "CUSTOMERS");
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(240, 740));
        sidebar.setBackground(SIDEBAR_BACKGROUND);

        // Header and logo
        JPanel logoPanel = new JPanel();
        logoPanel.setLayout(
                new BoxLayout(logoPanel, BoxLayout.Y_AXIS)
        );
        logoPanel.setOpaque(false);
        logoPanel.setBorder(new EmptyBorder(28, 20, 24, 20));

        JLabel brandIcon = new JLabel("[P] SMART PARK");
        brandIcon.setFont(new Font("Segoe UI", Font.BOLD, 19));
        brandIcon.setForeground(Color.WHITE);

        JLabel brandSub = new JLabel("Parking Lot System v2.0");
        brandSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        brandSub.setForeground(new Color(148, 163, 184));

        logoPanel.add(brandIcon);
        logoPanel.add(Box.createVerticalStrut(4));
        logoPanel.add(brandSub);

        sidebar.add(logoPanel, BorderLayout.NORTH);

        // Sidebar menu
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(
                new BoxLayout(menuPanel, BoxLayout.Y_AXIS)
        );
        menuPanel.setOpaque(false);
        menuPanel.setBorder(new EmptyBorder(10, 14, 10, 14));

        addMenuButton(menuPanel, "Dashboard", "DASHBOARD");
        addMenuButton(menuPanel, "Park Vehicle", "PARK");
        addMenuButton(menuPanel, "Vehicle Exit", "EXIT");
        addMenuButton(menuPanel, "Parking Spaces", "AVAILABLE");
        addMenuButton(menuPanel, "Parked Vehicles", "PARKED");
        addMenuButton(menuPanel, "Transactions", "HISTORY");
        addMenuButton(menuPanel, "Customer Directory", "CUSTOMERS");

        sidebar.add(menuPanel, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel();
        footer.setLayout(
                new BoxLayout(footer, BoxLayout.Y_AXIS)
        );
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(15, 18, 20, 18));

        dbStatusLabel = new JLabel();
        dbStatusLabel.setFont(
                new Font("Segoe UI", Font.BOLD, 12)
        );
        updateConnectionStatus();

        JLabel authorLabel = new JLabel("OOP Micro Project");
        authorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        authorLabel.setForeground(new Color(148, 163, 184));

        footer.add(dbStatusLabel);
        footer.add(Box.createVerticalStrut(4));
        footer.add(authorLabel);

        sidebar.add(footer, BorderLayout.SOUTH);

        return sidebar;
    }

    private void addMenuButton(
            JPanel parent,
            String label,
            String screenName
    ) {
        JButton button = new JButton(label);

        // Prevent the native system theme from replacing our colours.
        button.setUI(new BasicButtonUI());
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(true);
        button.setFocusPainted(false);

        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMaximumSize(new Dimension(212, 42));
        button.setPreferredSize(new Dimension(212, 42));
        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        applyMenuButtonStyle(button, false);

        button.addActionListener(e -> showScreen(screenName));

        menuButtons.put(screenName, button);
        parent.add(button);
        parent.add(Box.createVerticalStrut(6));
    }

    private void applyMenuButtonStyle(
            JButton button,
            boolean selected
    ) {
        Color background = selected
                ? SELECTED_BACKGROUND
                : BUTTON_BACKGROUND;

        Color indicator = selected
                ? SELECTED_INDICATOR
                : background;

        button.setBackground(background);
        button.setForeground(Color.WHITE);

        button.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(
                                0, 3, 0, 0, indicator
                        ),
                        new EmptyBorder(0, 14, 0, 10)
                )
        );

        button.repaint();
    }

    public void showScreen(String screenName) {
        // Ignore unknown screen names.
        if (!menuButtons.containsKey(screenName)) {
            return;
        }

        cardLayout.show(contentPanel, screenName);

        // Highlight the selected sidebar button.
        for (Map.Entry<String, JButton> entry
                : menuButtons.entrySet()) {

            boolean selected = entry.getKey().equals(screenName);
            applyMenuButtonStyle(entry.getValue(), selected);
        }

        // Refresh the selected screen.
        switch (screenName) {
            case "DASHBOARD" -> dashboardPanel.refreshData();
            case "PARK" -> parkVehiclePanel.updateTypeInfo();
            case "EXIT" -> exitVehiclePanel.refreshActiveList();
            case "AVAILABLE" -> availableSpacesPanel.refreshSpaces();
            case "PARKED" -> parkedVehiclesPanel.refreshParkedList();
            case "HISTORY" -> transactionHistoryPanel.refreshHistory();
            case "CUSTOMERS" ->
                    customerDirectoryPanel.refreshCustomers();
        }

        updateConnectionStatus();
    }

    private void updateConnectionStatus() {
        boolean connected = service.isDatabaseConnected();

        dbStatusLabel.setText(
                connected
                        ? "* MySQL Online"
                        : "* Demo Mode (In-Memory)"
        );

        dbStatusLabel.setForeground(
                connected
                        ? UITheme.SUCCESS_GREEN
                        : UITheme.WARNING_AMBER
        );
    }

    public void refreshAllScreens() {
        dashboardPanel.refreshData();
        parkVehiclePanel.updateTypeInfo();
        exitVehiclePanel.refreshActiveList();
        availableSpacesPanel.refreshSpaces();
        parkedVehiclesPanel.refreshParkedList();
        transactionHistoryPanel.refreshHistory();
        customerDirectoryPanel.refreshCustomers();

        updateConnectionStatus();
    }

    public ExitVehiclePanel getExitPanel() {
        return exitVehiclePanel;
    }

    public ParkingLotService getService() {
        return service;
    }
}