package com.cardiffmet.scms.ui.common;

import com.cardiffmet.scms.model.Session;
import com.cardiffmet.scms.model.UserNotification;
import com.cardiffmet.scms.service.CampusRepository;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * In-app alerts delivered via the Observer {@link com.cardiffmet.scms.notify.NotificationCenter}.
 */
public class UserAlertsPanel extends JPanel {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final CampusRepository campus;
    private final Session session;
    private final DefaultTableModel model;

    public UserAlertsPanel(CampusRepository campus, Session session) {
        super(new BorderLayout(8, 8));
        this.campus = campus;
        this.session = session;
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        model = new DefaultTableModel(new String[]{"ID", "When", "Category", "Message", "Read"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);

        JLabel info = new JLabel("<html>Booking confirmations, cancellations, and maintenance updates appear here "
                + "(Observer pattern).</html>");
        add(info, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton refresh = new JButton("Refresh");
        JButton markSel = new JButton("Mark selected as read");
        JButton markAll = new JButton("Mark all as read");
        refresh.addActionListener(e -> reload());
        markSel.addActionListener(e -> markSelected(table));
        markAll.addActionListener(e -> {
            campus.markAllNotificationsRead(session.getUsername());
            reload();
        });

        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
        south.add(refresh);
        south.add(markSel);
        south.add(markAll);
        add(south, BorderLayout.SOUTH);

        reload();
    }

    private void markSelected(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        long id = Long.parseLong(String.valueOf(model.getValueAt(row, 0)));
        campus.markNotificationRead(id, session.getUsername());
        reload();
    }

    public void reload() {
        model.setRowCount(0);
        List<UserNotification> list = campus.getUserNotifications(session.getUsername());
        for (UserNotification n : list) {
            model.addRow(new Object[]{
                    n.getId(),
                    DT.format(n.getCreatedAt()),
                    n.getCategory().name(),
                    n.getMessage(),
                    n.isRead() ? "Yes" : "No"
            });
        }
    }
}
