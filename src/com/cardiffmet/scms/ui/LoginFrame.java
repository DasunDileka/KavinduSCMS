package com.cardiffmet.scms.ui;

import com.cardiffmet.scms.auth.UserDirectory;
import com.cardiffmet.scms.model.Session;
import com.cardiffmet.scms.model.UserRole;
import com.cardiffmet.scms.service.CampusRepository;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * Login window for SCMS (prototype).
 */
public class LoginFrame extends JFrame {

    private final CampusRepository campus;
    private final UserDirectory users;

    private final JTextField usernameField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JComboBox<UserRole> roleCombo = new JComboBox<>(UserRole.values());

    public LoginFrame(CampusRepository campus, UserDirectory users) {
        super("SCMS Login — Cardiff Metropolitan University");
        this.campus = campus;
        this.users = users;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(440, 380));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(0, 12));
        root.setBorder(BorderFactory.createEmptyBorder(20, 28, 20, 28));

        JLabel banner = new JLabel("<html><center><b>Smart Campus Management System</b><br>"
                + "<span style='color:#555'>Cardiff Metropolitan University</span></center></html>",
                SwingConstants.CENTER);
        banner.setFont(banner.getFont().deriveFont(Font.PLAIN, 14f));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        form.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        form.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        form.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        form.add(new JLabel("I am a:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        form.add(roleCombo, gbc);

        JLabel hint = new JLabel(" ");
        hint.setForeground(new Color(0x555555));
        hint.setFont(hint.getFont().deriveFont(11f));
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        form.add(hint, gbc);

        roleCombo.addActionListener(e -> updateHint(hint));

        JPanel buttons = new JPanel();
        JButton loginBtn = new JButton("Log in");
        JButton clearBtn = new JButton("Clear");
        JButton exitBtn = new JButton("Exit");
        buttons.add(loginBtn);
        buttons.add(clearBtn);
        buttons.add(exitBtn);

        root.add(banner, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);

        setContentPane(root);

        updateHint(hint);

        loginBtn.addActionListener(e -> attemptLogin());
        clearBtn.addActionListener(e -> {
            usernameField.setText("");
            passwordField.setText("");
            usernameField.requestFocusInWindow();
        });
        exitBtn.addActionListener(e -> System.exit(0));

        getRootPane().setDefaultButton(loginBtn);
    }

    private void updateHint(JLabel hint) {
        UserRole role = (UserRole) roleCombo.getSelectedItem();
        hint.setText(role != null ? UserDirectory.loginHint(role) : " ");
    }

    private void attemptLogin() {
        UserRole role = (UserRole) roleCombo.getSelectedItem();
        if (role == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a role.",
                    "Login",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        char[] pass = passwordField.getPassword();
        boolean ok = users.authenticate(usernameField.getText(), pass, role);
        if (!ok) {
            JOptionPane.showMessageDialog(this,
                    "Invalid username or password for the selected role.\n"
                            + UserDirectory.loginHint(role),
                    "Login failed",
                    JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
            passwordField.requestFocusInWindow();
            return;
        }

        users.resolveSession(usernameField.getText().trim()).ifPresentOrElse(info -> {
            Session session = new Session(info.username(), info.displayName(), info.role());
            MainFrame main = new MainFrame(campus, users, session, () -> {
                LoginFrame next = new LoginFrame(campus, users);
                next.setLocationRelativeTo(null);
                next.setVisible(true);
            });
            main.setLocationRelativeTo(null);
            dispose();
            main.setVisible(true);
        }, () -> JOptionPane.showMessageDialog(this,
                "Could not load user profile.",
                "Login",
                JOptionPane.ERROR_MESSAGE));
    }

    /**
     * Uses the system look-and-feel when available for a native feel on Windows/macOS/Linux.
     */
    public static void applyLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                 UnsupportedLookAndFeelException ignored) {
            // Fall back to default Metal/other LAF
        }
    }
}
