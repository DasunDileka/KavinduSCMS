package com.cardiffmet.scms;

import com.cardiffmet.scms.facade.CampusServicesFacade;
import com.cardiffmet.scms.ui.LoginFrame;

import javax.swing.SwingUtilities;

/**
 * Entry point for the Smart Campus Management System prototype.
 */
public final class ScmsApp {

    private ScmsApp() {
    }

    public static void main(String[] args) {
        CampusServicesFacade services = CampusServicesFacade.bootstrap();

        SwingUtilities.invokeLater(() -> {
            LoginFrame.applyLookAndFeel();
            LoginFrame login = new LoginFrame(services);
            login.setLocationRelativeTo(null);
            login.setVisible(true);
        });
    }
}
