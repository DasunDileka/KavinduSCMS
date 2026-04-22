package com.cardiffmet.scms;

import com.cardiffmet.scms.auth.UserDirectory;
import com.cardiffmet.scms.service.CampusRepository;
import com.cardiffmet.scms.ui.LoginFrame;

import javax.swing.SwingUtilities;

/**
 * Entry point for the Smart Campus Management System prototype.
 */
public final class ScmsApp {

    private ScmsApp() {
    }

    public static void main(String[] args) {
        CampusRepository campus = new CampusRepository();
        UserDirectory users = UserDirectory.withDemoAccounts();

        SwingUtilities.invokeLater(() -> {
            LoginFrame.applyLookAndFeel();
            LoginFrame login = new LoginFrame(campus, users);
            login.setLocationRelativeTo(null);
            login.setVisible(true);
        });
    }
}
