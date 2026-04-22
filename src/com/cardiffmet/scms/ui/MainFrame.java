package com.cardiffmet.scms.ui;

import com.cardiffmet.scms.auth.UserDirectory;
import com.cardiffmet.scms.model.Session;
import com.cardiffmet.scms.service.CampusRepository;
import com.cardiffmet.scms.ui.admin.AdminDashboardPanel;
import com.cardiffmet.scms.ui.staff.StaffDashboardPanel;
import com.cardiffmet.scms.ui.student.StudentDashboardPanel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

/**
 * Main shell after login: loads the dashboard for the active role.
 */
public class MainFrame extends JFrame {

    public MainFrame(CampusRepository campus, UserDirectory users, Session session,
                     Runnable onLogout) {
        super("SCMS — " + session.getDisplayName());
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(920, 560));
        setLocationRelativeTo(null);

        JPanel north = new JPanel(new FlowLayout(FlowLayout.LEFT));
        north.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        north.add(new JLabel("<html><b>Smart Campus Management System</b> — "
                + "<span style='color:#444'>" + session.getRole().getDisplayName()
                + " • " + session.getDisplayName() + "</span></html>"));

        JButton logout = new JButton("Log out");
        logout.addActionListener(e -> confirmLogout(onLogout));
        north.add(logout);

        JPanel center = switch (session.getRole()) {
            case ADMINISTRATOR -> new AdminDashboardPanel(campus, users, session);
            case STAFF_MEMBER -> new StaffDashboardPanel(campus, session);
            case STUDENT -> new StudentDashboardPanel(campus, session);
        };

        add(north, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
    }

    private void confirmLogout(Runnable onLogout) {
        int c = JOptionPane.showConfirmDialog(this,
                "Log out and return to the login screen?",
                "Log out",
                JOptionPane.OK_CANCEL_OPTION);
        if (c == JOptionPane.OK_OPTION) {
            dispose();
            onLogout.run();
        }
    }
}
