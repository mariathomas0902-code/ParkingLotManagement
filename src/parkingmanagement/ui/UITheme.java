package parkingmanagement.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class UITheme {

    public static final Color BG_COLOR = new Color(248, 250, 252);
    public static final Color PANEL_BG = Color.WHITE;

    public static final Color SIDEBAR_BG = new Color(15, 23, 42);
    public static final Color SIDEBAR_BTN_HOVER = new Color(51, 65, 85);
    public static final Color SIDEBAR_BTN_ACTIVE = new Color(37, 99, 235);

    public static final Color PRIMARY_BLUE = new Color(37, 99, 235);
    public static final Color PRIMARY_HOVER = new Color(29, 78, 216);

    public static final Color SUCCESS_GREEN = new Color(16, 185, 129);
    public static final Color SUCCESS_BG = new Color(236, 253, 245);
    public static final Color SUCCESS_TEXT = new Color(6, 95, 70);

    public static final Color DANGER_RED = new Color(239, 68, 68);
    public static final Color DANGER_BG = new Color(254, 242, 242);
    public static final Color DANGER_TEXT = new Color(153, 27, 27);

    public static final Color WARNING_AMBER = new Color(245, 158, 11);
    public static final Color WARNING_BG = new Color(254, 252, 232);
    public static final Color WARNING_TEXT = new Color(133, 77, 14);

    public static final Color INFO_BLUE = new Color(14, 165, 233);
    public static final Color INFO_BG = new Color(240, 249, 255);
    public static final Color INFO_TEXT = new Color(12, 74, 110);

    public static final Color TEXT_DARK = new Color(15, 23, 42);
    public static final Color TEXT_MUTED = new Color(100, 116, 139);
    public static final Color BORDER_COLOR = new Color(226, 232, 240);

    public static final Font TITLE_FONT =
            new Font("Segoe UI", Font.BOLD, 22);

    public static final Font HEADING_FONT =
            new Font("Segoe UI", Font.BOLD, 16);

    public static final Font SUBHEADING_FONT =
            new Font("Segoe UI", Font.BOLD, 13);

    public static final Font REGULAR_FONT =
            new Font("Segoe UI", Font.PLAIN, 13);

    public static final Font BOLD_FONT =
            new Font("Segoe UI", Font.BOLD, 13);

    public static final Font STAT_VALUE_FONT =
            new Font("Segoe UI", Font.BOLD, 26);

    public static final Font MONO_FONT =
            new Font("Consolas", Font.PLAIN, 13);

    public static JButton createStyledButton(
            String text,
            Color bgColor,
            Color fgColor
    ) {
        JButton button = new JButton(text);

        // Prevent the system theme from overriding custom colours.
        button.setUI(new BasicButtonUI());
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(true);
        button.setFocusPainted(false);

        button.setFont(BOLD_FONT);
        button.setBackground(bgColor);
        button.setForeground(fgColor);

        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bgColor.darker(), 1),
                new EmptyBorder(9, 18, 9, 18)
        ));

        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        return button;
    }

    public static JButton createPrimaryButton(String text) {
        return createStyledButton(text, PRIMARY_BLUE, Color.WHITE);
    }

    public static JButton createSecondaryButton(String text) {
        JButton button = createStyledButton(
                text,
                Color.WHITE,
                TEXT_DARK
        );

        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(9, 16, 9, 16)
        ));

        return button;
    }

    public static JTextField createStyledTextField() {
        JTextField field = new JTextField();

        field.setFont(REGULAR_FONT);
        field.setForeground(TEXT_DARK);

        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(8, 12, 8, 12)
        ));

        return field;
    }

    public static JPanel createCardPanel() {
        JPanel card = new JPanel();

        card.setBackground(PANEL_BG);

        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(18, 20, 18, 20)
        ));

        return card;
    }

    public static void styleTable(JTable table) {
        table.setFont(REGULAR_FONT);
        table.setRowHeight(36);
        table.setShowGrid(true);
        table.setGridColor(new Color(241, 245, 249));
        table.setSelectionBackground(new Color(224, 231, 255));
        table.setSelectionForeground(TEXT_DARK);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();

        header.setFont(BOLD_FONT);
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(new Color(51, 65, 85));
        header.setPreferredSize(
                new Dimension(header.getWidth(), 38)
        );

        header.setBorder(
                BorderFactory.createMatteBorder(
                        0, 0, 1, 0, BORDER_COLOR
                )
        );

        // Alternating row backgrounds and status text colours.
        table.setDefaultRenderer(
                Object.class,
                new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(
                            JTable t,
                            Object value,
                            boolean isSelected,
                            boolean hasFocus,
                            int row,
                            int col
                    ) {
                        Component component =
                                super.getTableCellRendererComponent(
                                        t,
                                        value,
                                        isSelected,
                                        hasFocus,
                                        row,
                                        col
                                );

                        setBorder(new EmptyBorder(0, 10, 0, 10));

                        if (!isSelected) {
                            component.setBackground(
                                    row % 2 == 0
                                            ? Color.WHITE
                                            : BG_COLOR
                            );

                            component.setForeground(TEXT_DARK);
                        }

                        if (value != null) {
                            String status = value.toString();

                            if ("AVAILABLE".equalsIgnoreCase(status)
                                    || "COMPLETED".equalsIgnoreCase(status)) {
                                component.setForeground(SUCCESS_TEXT);
                            } else if ("OCCUPIED".equalsIgnoreCase(status)) {
                                component.setForeground(DANGER_TEXT);
                            } else if ("ACTIVE".equalsIgnoreCase(status)) {
                                component.setForeground(
                                        new Color(30, 64, 175)
                                );
                            }
                        }

                        return component;
                    }
                }
        );
    }

    public static JPanel createStatCard(
            String title,
            String value,
            String subtitle,
            Color accentColor,
            String icon
    ) {
        JPanel card = new JPanel(new BorderLayout(10, 8));
        card.setBackground(PANEL_BG);

        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(
                        0, 4, 0, 0, accentColor
                ),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR, 1),
                        new EmptyBorder(14, 16, 14, 16)
                )
        ));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLabel.setForeground(TEXT_MUTED);

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));

        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(iconLabel, BorderLayout.EAST);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(STAT_VALUE_FONT);
        valueLabel.setForeground(TEXT_DARK);

        JLabel subLabel = new JLabel(subtitle);
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLabel.setForeground(TEXT_MUTED);

        card.add(topPanel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(subLabel, BorderLayout.SOUTH);

        return card;
    }
}