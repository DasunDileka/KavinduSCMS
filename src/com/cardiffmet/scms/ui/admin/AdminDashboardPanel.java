package com.cardiffmet.scms.ui.admin;

import com.cardiffmet.scms.auth.UserDirectory;
import com.cardiffmet.scms.model.Announcement;
import com.cardiffmet.scms.model.Booking;
import com.cardiffmet.scms.model.BookingStatus;
import com.cardiffmet.scms.model.MaintenanceRequest;
import com.cardiffmet.scms.model.MaintenanceStatus;
import com.cardiffmet.scms.model.Room;
import com.cardiffmet.scms.model.Session;
import com.cardiffmet.scms.model.UserAccount;
import com.cardiffmet.scms.model.UserRole;
import com.cardiffmet.scms.service.CampusRepository;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AdminDashboardPanel extends JPanel {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final CampusRepository campus;
    private final UserDirectory users;
    private final Session session;

    private final DefaultTableModel roomsModel;
    private final DefaultTableModel usersModel;
    private final DefaultTableModel maintenanceModel;
    private final DefaultTableModel bookingsModel;
    private final DefaultTableModel announcementsModel;

    private final JTable roomsTable;
    private final JTable usersTable;
    private final JTable maintenanceTable;
    private final JTable bookingsTable;

    public AdminDashboardPanel(CampusRepository campus, UserDirectory users, Session session) {
        super(new BorderLayout());
        this.campus = campus;
        this.users = users;
        this.session = session;

        roomsModel = new DefaultTableModel(new String[]{"Room ID", "Name", "Building", "Capacity", "Equipment"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        roomsTable = new JTable(roomsModel);

        usersModel = new DefaultTableModel(new String[]{"Username", "Display name", "Role"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        usersTable = new JTable(usersModel);

        maintenanceModel = new DefaultTableModel(
                new String[]{"ID", "Title", "Reported by", "Status", "Created"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        maintenanceTable = new JTable(maintenanceModel);

        bookingsModel = new DefaultTableModel(
                new String[]{"ID", "Room", "Requested by", "Date", "Time", "Status", "Purpose"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        bookingsTable = new JTable(bookingsModel);

        announcementsModel = new DefaultTableModel(new String[]{"Posted", "Title", "Author", "Preview"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable annTable = new JTable(announcementsModel);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Rooms", buildRoomsTab());
        tabs.addTab("Users", buildUsersTab());
        tabs.addTab("Maintenance", buildMaintenanceTab());
        tabs.addTab("Booking requests", buildBookingsTab());
        tabs.addTab("Announcements", buildAnnouncementsTab(annTable));

        add(tabs, BorderLayout.CENTER);

        refreshRooms();
        refreshUsers();
        refreshMaintenance();
        refreshBookings();
        refreshAnnouncements();
    }

    private JPanel buildRoomsTab() {
        JPanel wrap = new JPanel(new BorderLayout(8, 8));
        wrap.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JLabel info = new JLabel(
                "Five campus rooms are pre-loaded (hard-coded seed). Administrators can edit details.");
        info.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        JButton edit = new JButton("Edit selected room…");
        edit.addActionListener(e -> editSelectedRoom());

        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
        south.add(edit);

        wrap.add(info, BorderLayout.NORTH);
        wrap.add(new JScrollPane(roomsTable), BorderLayout.CENTER);
        wrap.add(south, BorderLayout.SOUTH);
        return wrap;
    }

    private void editSelectedRoom() {
        int row = roomsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a room first.", "Rooms", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String roomId = String.valueOf(roomsModel.getValueAt(row, 0));
        Room r = campus.findRoom(roomId).orElse(null);
        if (r == null) {
            return;
        }
        JTextField name = new JTextField(r.getName(), 24);
        JTextField building = new JTextField(r.getBuilding(), 24);
        JTextField capacity = new JTextField(String.valueOf(r.getCapacity()), 8);
        JTextField equip = new JTextField(r.getEquipmentSummary(), 32);
        JPanel form = new JPanel(new GridLayout(0, 1, 6, 6));
        form.add(labelled("Name", name));
        form.add(labelled("Building", building));
        form.add(labelled("Capacity", capacity));
        form.add(labelled("Equipment / notes", equip));

        int ok = JOptionPane.showConfirmDialog(this, form, "Edit room", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            int cap = Integer.parseInt(capacity.getText().trim());
            if (cap <= 0) {
                throw new NumberFormatException();
            }
            campus.updateRoomFields(roomId, name.getText().trim(), building.getText().trim(),
                    cap, equip.getText().trim());
            refreshRooms();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Capacity must be a positive integer.", "Rooms",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static JPanel labelled(String title, JTextField field) {
        JPanel p = new JPanel(new BorderLayout(4, 4));
        p.add(new JLabel(title), BorderLayout.WEST);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private static JPanel labelledPassword(String title, JPasswordField field) {
        JPanel p = new JPanel(new BorderLayout(4, 4));
        p.add(new JLabel(title), BorderLayout.WEST);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildUsersTab() {
        JPanel wrap = new JPanel(new BorderLayout(8, 8));
        wrap.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JButton add = new JButton("Add user…");
        JButton remove = new JButton("Remove selected");
        add.addActionListener(e -> addUser());
        remove.addActionListener(e -> removeSelectedUser());

        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
        south.add(add);
        south.add(remove);

        wrap.add(new JScrollPane(usersTable), BorderLayout.CENTER);
        wrap.add(south, BorderLayout.SOUTH);
        return wrap;
    }

    private void addUser() {
        JTextField username = new JTextField(16);
        JPasswordField password = new JPasswordField(16);
        JTextField display = new JTextField(16);
        JComboBox<UserRole> role = new JComboBox<>(UserRole.values());
        JPanel form = new JPanel(new GridLayout(0, 1, 6, 6));
        form.add(labelled("Username", username));
        form.add(labelledPassword("Password", password));
        form.add(labelled("Display name", display));
        JPanel r = new JPanel(new BorderLayout());
        r.add(new JLabel("Role"), BorderLayout.WEST);
        r.add(role, BorderLayout.CENTER);
        form.add(r);

        int ok = JOptionPane.showConfirmDialog(this, form, "Add user", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) {
            return;
        }
        String u = username.getText().trim();
        String pw = new String(password.getPassword());
        String d = display.getText().trim();
        UserRole ur = (UserRole) role.getSelectedItem();
        if (u.isEmpty() || pw.isEmpty() || ur == null) {
            JOptionPane.showMessageDialog(this, "Username, password, and role are required.", "Users",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        boolean added = users.addAccount(u, pw, d.isEmpty() ? u : d, ur);
        if (!added) {
            JOptionPane.showMessageDialog(this, "That username already exists.", "Users",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        refreshUsers();
    }

    private void removeSelectedUser() {
        int row = usersTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a user first.", "Users", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String uname = String.valueOf(usersModel.getValueAt(row, 0));
        if (uname.equalsIgnoreCase(session.getUsername())) {
            JOptionPane.showMessageDialog(this, "You cannot remove your own account while logged in.", "Users",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int c = JOptionPane.showConfirmDialog(this,
                "Remove user \"" + uname + "\"?",
                "Users",
                JOptionPane.OK_CANCEL_OPTION);
        if (c != JOptionPane.OK_OPTION) {
            return;
        }
        users.removeAccount(uname);
        refreshUsers();
    }

    private JPanel buildMaintenanceTab() {
        JPanel wrap = new JPanel(new BorderLayout(8, 8));
        wrap.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JComboBox<MaintenanceStatus> status = new JComboBox<>(MaintenanceStatus.values());
        JButton apply = new JButton("Set status for selected");
        apply.addActionListener(e -> {
            int row = maintenanceTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a request.", "Maintenance",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            long id = Long.parseLong(String.valueOf(maintenanceModel.getValueAt(row, 0)));
            MaintenanceStatus st = (MaintenanceStatus) status.getSelectedItem();
            try {
                campus.setMaintenanceStatus(id, st);
                refreshMaintenance();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Maintenance", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
        south.add(new JLabel("New status:"));
        south.add(status);
        south.add(apply);

        wrap.add(new JScrollPane(maintenanceTable), BorderLayout.CENTER);
        wrap.add(south, BorderLayout.SOUTH);
        return wrap;
    }

    private JPanel buildBookingsTab() {
        JPanel wrap = new JPanel(new BorderLayout(8, 8));
        wrap.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JButton approve = new JButton("Approve selected");
        JButton decline = new JButton("Decline selected");
        approve.addActionListener(e -> setSelectedBookingStatus(BookingStatus.APPROVED));
        decline.addActionListener(e -> setSelectedBookingStatus(BookingStatus.DECLINED));

        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
        south.add(approve);
        south.add(decline);

        JLabel info = new JLabel("Pending requests from students appear with status “Pending”. "
                + "Approved bookings block overlapping slots.");
        info.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        JPanel north = new JPanel(new BorderLayout());
        north.add(info, BorderLayout.NORTH);

        wrap.add(north, BorderLayout.NORTH);
        wrap.add(new JScrollPane(bookingsTable), BorderLayout.CENTER);
        wrap.add(south, BorderLayout.SOUTH);
        return wrap;
    }

    private void setSelectedBookingStatus(BookingStatus target) {
        int row = bookingsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a booking.", "Bookings", JOptionPane.WARNING_MESSAGE);
            return;
        }
        long id = Long.parseLong(String.valueOf(bookingsModel.getValueAt(row, 0)));
        try {
            campus.setBookingStatus(id, target);
            refreshBookings();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Bookings", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel buildAnnouncementsTab(JTable annTable) {
        JPanel wrap = new JPanel(new BorderLayout(8, 8));
        wrap.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JTextField title = new JTextField(40);
        JTextArea body = new JTextArea(5, 40);
        body.setLineWrap(true);
        body.setWrapStyleWord(true);
        JButton post = new JButton("Post announcement");
        post.addActionListener(e -> {
            String t = title.getText().trim();
            String b = body.getText().trim();
            if (t.isEmpty() || b.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Title and body are required.", "Announcements",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            campus.postAnnouncement(t, b, session.getUsername());
            title.setText("");
            body.setText("");
            refreshAnnouncements();
        });

        JPanel form = new JPanel(new BorderLayout(8, 8));
        JPanel northForm = new JPanel(new GridLayout(0, 1, 4, 4));
        northForm.add(labelled("Title", title));
        northForm.add(new JLabel("Body"));
        northForm.add(new JScrollPane(body));
        northForm.add(post);
        form.add(northForm, BorderLayout.NORTH);

        JPanel both = new JPanel(new BorderLayout(8, 8));
        both.add(form, BorderLayout.NORTH);
        both.add(new JScrollPane(annTable), BorderLayout.CENTER);
        wrap.add(both, BorderLayout.CENTER);
        return wrap;
    }

    private void refreshRooms() {
        roomsModel.setRowCount(0);
        for (Room r : campus.getRooms()) {
            roomsModel.addRow(new Object[]{r.getId(), r.getName(), r.getBuilding(),
                    r.getCapacity(), r.getEquipmentSummary()});
        }
    }

    private void refreshUsers() {
        usersModel.setRowCount(0);
        for (UserAccount ua : users.listAccounts()) {
            usersModel.addRow(new Object[]{ua.getUsername(), ua.getDisplayName(), ua.getRole().getDisplayName()});
        }
    }

    private void refreshMaintenance() {
        maintenanceModel.setRowCount(0);
        List<MaintenanceRequest> list = new ArrayList<>(campus.getMaintenanceRequests());
        list.sort(Comparator.comparing(MaintenanceRequest::getCreatedAt).reversed());
        for (MaintenanceRequest m : list) {
            maintenanceModel.addRow(new Object[]{
                    m.getId(),
                    m.getTitle(),
                    m.getReportedByUsername(),
                    m.getStatus().getLabel(),
                    DT.format(m.getCreatedAt())
            });
        }
    }

    private void refreshBookings() {
        bookingsModel.setRowCount(0);
        List<Booking> list = new ArrayList<>(campus.getBookings());
        list.sort(Comparator.comparing(Booking::getDate).thenComparing(Booking::getStart));
        for (Booking b : list) {
            campus.findRoom(b.getRoomId()).ifPresentOrElse(room ->
                            bookingsModel.addRow(new Object[]{
                                    b.getId(),
                                    room.getName(),
                                    b.getRequestedByUsername(),
                                    b.getDate(),
                                    b.getStart() + "–" + b.getEnd(),
                                    b.getStatus().getLabel(),
                                    b.getPurpose()
                            }),
                    () -> bookingsModel.addRow(new Object[]{
                            b.getId(),
                            b.getRoomId(),
                            b.getRequestedByUsername(),
                            b.getDate(),
                            b.getStart() + "–" + b.getEnd(),
                            b.getStatus().getLabel(),
                            b.getPurpose()
                    }));
        }
    }

    private void refreshAnnouncements() {
        announcementsModel.setRowCount(0);
        for (Announcement a : campus.getAnnouncementsDescending()) {
            String preview = a.getBody().length() > 80 ? a.getBody().substring(0, 77) + "…" : a.getBody();
            announcementsModel.addRow(new Object[]{
                    DT.format(a.getPostedAt()),
                    a.getTitle(),
                    a.getAuthorUsername(),
                    preview.replace('\n', ' ')
            });
        }
    }
}
