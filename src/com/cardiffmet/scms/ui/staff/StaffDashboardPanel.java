package com.cardiffmet.scms.ui.staff;

import com.cardiffmet.scms.command.CancelBookingCommand;
import com.cardiffmet.scms.model.Announcement;
import com.cardiffmet.scms.model.Booking;
import com.cardiffmet.scms.model.BookingStatus;
import com.cardiffmet.scms.model.Room;
import com.cardiffmet.scms.model.Session;
import com.cardiffmet.scms.model.Urgency;
import com.cardiffmet.scms.service.BookingSeriesResult;
import com.cardiffmet.scms.service.CampusRepository;
import com.cardiffmet.scms.ui.common.UserAlertsPanel;
import com.cardiffmet.scms.ui.util.DateOptions;
import com.cardiffmet.scms.ui.util.TimeSlots;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

public class StaffDashboardPanel extends JPanel {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final CampusRepository campus;
    private final Session session;

    private final DefaultTableModel myBookingsModel;
    private final JTable myBookingsTable;
    private final DefaultTableModel announcementsModel;

    private JComboBox<Room> roomCombo;
    private JComboBox<LocalDate> dateCombo;
    private JComboBox<LocalTime> startCombo;
    private JComboBox<LocalTime> endCombo;
    private JTextField purposeField;
    private JCheckBox weeklyRepeat;
    private JSpinner weekCountSpinner;

    public StaffDashboardPanel(CampusRepository campus, Session session) {
        super(new BorderLayout());
        this.campus = campus;
        this.session = session;

        myBookingsModel = new DefaultTableModel(
                new String[]{"ID", "Room", "Date", "Time", "Status", "Purpose"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        myBookingsTable = new JTable(myBookingsModel);

        announcementsModel = new DefaultTableModel(new String[]{"Posted", "Title", "Author", "Preview"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable annTable = new JTable(announcementsModel);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Book a room", buildBookingTab());
        tabs.addTab("My bookings", buildMyBookingsTab());
        tabs.addTab("Report maintenance", buildMaintenanceTab());
        tabs.addTab("Announcements", buildAnnouncementsTab(annTable));
        tabs.addTab("Alerts", new UserAlertsPanel(campus, session));

        add(tabs, BorderLayout.CENTER);

        refreshMyBookings();
        refreshAnnouncements();
    }

    private JPanel buildBookingTab() {
        JPanel wrap = new JPanel(new BorderLayout(8, 8));
        wrap.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        roomCombo = new JComboBox<>();
        reloadRoomCombo();

        List<LocalDate> days = DateOptions.upcomingDays(60);
        dateCombo = new JComboBox<>(days.toArray(LocalDate[]::new));

        List<LocalTime> times = TimeSlots.hourlySlots();
        startCombo = new JComboBox<>(times.toArray(LocalTime[]::new));
        endCombo = new JComboBox<>(times.toArray(LocalTime[]::new));
        endCombo.setSelectedIndex(Math.min(2, endCombo.getItemCount() - 1));

        purposeField = new JTextField(32);

        weeklyRepeat = new JCheckBox("Weekly recurrence (same weekday & time)");
        weekCountSpinner = new JSpinner(new SpinnerNumberModel(8, 1, 52, 1));

        JButton reloadRooms = new JButton("Reload room list");
        reloadRooms.addActionListener(e -> reloadRoomCombo());

        JButton book = new JButton("Confirm booking");
        book.addActionListener(e -> confirmStaffBooking());

        JPanel form = new JPanel(new GridLayout(0, 1, 8, 8));
        form.add(labelRow("Room (active only)", roomCombo));
        form.add(labelRow("Date", dateCombo));
        form.add(labelRow("Start", startCombo));
        form.add(labelRow("End", endCombo));
        JPanel pRow = new JPanel(new BorderLayout(8, 0));
        pRow.add(new JLabel("Purpose"), BorderLayout.WEST);
        pRow.add(purposeField, BorderLayout.CENTER);
        form.add(pRow);

        JPanel recur = new JPanel(new FlowLayout(FlowLayout.LEFT));
        recur.add(weeklyRepeat);
        recur.add(new JLabel("Weeks:"));
        recur.add(weekCountSpinner);
        form.add(recur);

        form.add(reloadRooms);
        form.add(book);

        JLabel note = new JLabel("<html>Staff bookings are <b>Approved</b> immediately if the slot is free "
                + "(double-booking blocked). Optional weekly series creates one booking per week.</html>");
        note.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        JPanel north = new JPanel(new BorderLayout());
        north.add(note, BorderLayout.NORTH);
        north.add(form, BorderLayout.CENTER);
        wrap.add(north, BorderLayout.NORTH);
        return wrap;
    }

    private void reloadRoomCombo() {
        Room sel = (Room) roomCombo.getSelectedItem();
        roomCombo.removeAllItems();
        for (Room r : campus.getActiveRooms()) {
            roomCombo.addItem(r);
        }
        if (sel != null) {
            for (int i = 0; i < roomCombo.getItemCount(); i++) {
                if (roomCombo.getItemAt(i).getId().equals(sel.getId())) {
                    roomCombo.setSelectedIndex(i);
                    return;
                }
            }
        }
    }

    private static JPanel labelRow(String title, JComboBox<?> combo) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.add(new JLabel(title), BorderLayout.WEST);
        p.add(combo, BorderLayout.CENTER);
        return p;
    }

    private void confirmStaffBooking() {
        Room room = (Room) roomCombo.getSelectedItem();
        LocalDate date = (LocalDate) dateCombo.getSelectedItem();
        LocalTime start = (LocalTime) startCombo.getSelectedItem();
        LocalTime end = (LocalTime) endCombo.getSelectedItem();
        if (room == null || date == null || start == null || end == null) {
            JOptionPane.showMessageDialog(this, "Choose a room, date, and times.", "Booking",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!start.isBefore(end)) {
            JOptionPane.showMessageDialog(this, "End time must be after start time.", "Booking",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        String purpose = purposeField.getText().trim();
        if (purpose.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a short purpose.", "Booking",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            if (weeklyRepeat.isSelected()) {
                int weeks = (Integer) weekCountSpinner.getValue();
                BookingSeriesResult res = campus.addWeeklyBookingSeries(room.getId(), session.getUsername(),
                        date, start, end, purpose, BookingStatus.APPROVED, weeks);
                JOptionPane.showMessageDialog(this, res.summary(), "Recurring booking",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                campus.addBooking(room.getId(), session.getUsername(), date, start, end, purpose,
                        BookingStatus.APPROVED);
                JOptionPane.showMessageDialog(this, "Booking confirmed.", "Booking",
                        JOptionPane.INFORMATION_MESSAGE);
            }
            purposeField.setText("");
            refreshMyBookings();
        } catch (IllegalStateException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Booking", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel buildMyBookingsTab() {
        JPanel wrap = new JPanel(new BorderLayout(8, 8));
        wrap.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JButton refresh = new JButton("Refresh");
        JButton cancel = new JButton("Cancel selected booking");
        refresh.addActionListener(e -> refreshMyBookings());
        cancel.addActionListener(e -> cancelSelectedBooking());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
        south.add(refresh);
        south.add(cancel);
        wrap.add(new JScrollPane(myBookingsTable), BorderLayout.CENTER);
        wrap.add(south, BorderLayout.SOUTH);
        return wrap;
    }

    private void cancelSelectedBooking() {
        int row = myBookingsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select one of your bookings.", "Cancel",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        long id = Long.parseLong(String.valueOf(myBookingsModel.getValueAt(row, 0)));
        try {
            new CancelBookingCommand(campus, id, session.getUsername()).execute();
            refreshMyBookings();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cancel", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshMyBookings() {
        myBookingsModel.setRowCount(0);
        List<Booking> mine = campus.bookingsForUser(session.getUsername());
        mine.sort(Comparator.comparing(Booking::getDate).thenComparing(Booking::getStart));
        for (Booking b : mine) {
            String roomLabel = campus.findRoom(b.getRoomId()).map(Room::getName).orElse(b.getRoomId());
            myBookingsModel.addRow(new Object[]{
                    b.getId(),
                    roomLabel,
                    b.getDate(),
                    b.getStart() + "–" + b.getEnd(),
                    b.getStatus().getLabel(),
                    b.getPurpose()
            });
        }
    }

    private JPanel buildMaintenanceTab() {
        JPanel wrap = new JPanel(new BorderLayout(8, 8));
        wrap.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JComboBox<Room> roomField = new JComboBox<>();
        for (Room r : campus.getActiveRooms()) {
            roomField.addItem(r);
        }
        JComboBox<Urgency> urgency = new JComboBox<>(Urgency.values());

        JTextField title = new JTextField(36);
        JTextArea desc = new JTextArea(6, 36);
        desc.setLineWrap(true);
        desc.setWrapStyleWord(true);

        JButton submit = new JButton("Submit maintenance issue");
        submit.addActionListener(e -> {
            Room room = (Room) roomField.getSelectedItem();
            String t = title.getText().trim();
            String d = desc.getText().trim();
            Urgency u = (Urgency) urgency.getSelectedItem();
            if (room == null || u == null || t.isEmpty() || d.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Room, urgency, title, and description are required.",
                        "Maintenance", JOptionPane.WARNING_MESSAGE);
                return;
            }
            campus.addMaintenance(room.getId(), t, d, u, session.getUsername());
            title.setText("");
            desc.setText("");
            JOptionPane.showMessageDialog(this, "Maintenance issue logged.", "Maintenance",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        JPanel form = new JPanel(new GridLayout(0, 1, 8, 8));
        form.add(labelRow("Room", roomField));
        form.add(labelRow("Urgency", urgency));
        form.add(labelledField("Short title", title));
        form.add(new JLabel("Description"));
        form.add(new JScrollPane(desc));
        form.add(submit);

        wrap.add(form, BorderLayout.NORTH);
        return wrap;
    }

    private static JPanel labelledField(String title, JTextField field) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.add(new JLabel(title), BorderLayout.WEST);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildAnnouncementsTab(JTable annTable) {
        JPanel wrap = new JPanel(new BorderLayout(8, 8));
        wrap.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JLabel info = new JLabel("Official campus announcements (most recent first).");
        wrap.add(info, BorderLayout.NORTH);
        wrap.add(new JScrollPane(annTable), BorderLayout.CENTER);
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> refreshAnnouncements());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
        south.add(refresh);
        wrap.add(south, BorderLayout.SOUTH);
        return wrap;
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
