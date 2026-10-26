package parkingmanagement;

import parkingmanagement.ui.MainFrame;

import javax.swing.*;
import java.awt.*;

public class GuiMain {

    public static void main(String[] args) {
        // Enable anti-aliased text rendering for crisp fonts
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(
                        UIManager.getSystemLookAndFeelClassName()
                );
            } catch (Exception exception) {
                System.out.println("Default look and feel will be used: " + exception.getMessage());
            }

            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
