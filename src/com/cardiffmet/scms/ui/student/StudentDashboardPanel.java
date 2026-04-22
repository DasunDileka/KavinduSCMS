package com.cardiffmet.scms.ui.student;

import com.cardiffmet.scms.model.Announcement;
import com.cardiffmet.scms.model.Booking;
import com.cardiffmet.scms.model.BookingStatus;
import com.cardiffmet.scms.model.Room;
import com.cardiffmet.scms.model.Session;
import com.cardiffmet.scms.service.CampusRepository;
import com.cardiffmet.scms.ui.util.DateOptions;
import com.cardiffmet.scms.ui.util.TimeSlots;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

public class StudentDashboardPanel extends JPanel {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final CampusRepository campus;
    private final Session session;

    private final DefaultTableModel roomsModel;
    private final DefaultTableModel myRequestsModel;

    private JComboBox<Room> roomCombo;
    private JComboBox<LocalDate> dateCombo;
    private JComboBox<LocalTime> startCombo;
    private JComboBox<LocalTime> endCombo;
    private JTextField purposeField;
    private final DefaultTableModel announcementsModel;

    public StudentDashboardPanel(CampusRepository campus, Session session) {
        super(new BorderLayout());
        this.campus = campus;
        this.session = session;

        roomsModel = new DefaultTableModel(
                new String[]{"Room ID", "Name", "Building", "Capacity", "Equipment"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        myRequestsModel = new DefaultTableModel(
                new String[]{"ID", "Room", "Date", "Time", "Status", "Purpose"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        announcementsModel = new DefaultTableModel(new String[]{"Posted", "Title", "Author", "Preview"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Available rooms", buildRoomsTab());
        tabs.addTab("Request a booking", buildRequestTab());
        tabs.addTab("Announcements", buildAnnouncementsTab());

        add(tabs, BorderLayout.CENTER);

        refreshRoomsTable();
        refreshMyRequests();
        refreshAnnouncements();
    }

    private JPanel buildRoomsTab() {
        JPanel wrap = new JPanel(new BorderLayout(8, 8));
        wrap.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JLabel info = new JLabel("Five campus rooms are available (seed list). Contact Estates for AV extras.");
        info.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        wrap.add(info, BorderLayout.NORTH);
        JTable table = new JTable(roomsModel);
        wrap.add(new JScrollPane(table), BorderLayout.CENTER);
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> refreshRoomsTable());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
        south.add(refresh);
        wrap.add(south, BorderLayout.SOUTH);
        return wrap;
    }

    private void refreshRoomsTable() {
        roomsModel.setRowCount(0);
        for (Room r : campus.getRooms()) {
            roomsModel.addRow(new Object[]{r.getId(), r.getName(), r.getBuilding(),
                    r.getCapacity(), r.getEquipmentSummary()});
        }
    }

    private JPanel buildRequestTab() {
        JPanel wrap = new JPanel(new BorderLayout(8, 8));
        wrap.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        List<Room> rooms = campus.getRooms();
        roomCombo = new JComboBox<>(rooms.toArray(Room[]::new));
        List<LocalDate> days = DateOptions.upcomingDays(60);
        dateCombo = new JComboBox<>(days.toArray(LocalDate[]::new));
        List<LocalTime> times = TimeSlots.hourlySlots();
        startCombo = new JComboBox<>(times.toArray(LocalTime[]::new));
        endCombo = new JComboBox<>(times.toArray(LocalTime[]::new));
        endCombo.setSelectedIndex(Math.min(2, endCombo.getItemCount() - 1));
        purposeField = new JTextField(32);

        JButton submit = new JButton("Submit booking request");
        submit.addActionListener(e -> submitStudentRequest());

        JPanel form = new JPanel(new GridLayout(0, 1, 8, 8));
        form.add(row("Room", roomCombo));
        form.add(row("Date", dateCombo));
        form.add(row("Start", startCombo));
        form.add(row("End", endCombo));
        JPanel pRow = new JPanel(new BorderLayout(8, 0));
        pRow.add(new JLabel("Purpose"), BorderLayout.WEST);
        pRow.add(purposeField, BorderLayout.CENTER);
        form.add(pRow);
        form.add(submit);

        JLabel note = new JLabel("<html>Student requests start as <b>Pending</b>. "
                + "An administrator approves or declines them under <i>Booking requests</i>.</html>");
        note.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        JPanel north = new JPanel(new BorderLayout());
        north.add(note, BorderLayout.NORTH);
        north.add(form, BorderLayout.CENTER);

        JTable requests = new JTable(myRequestsModel);
        JPanel centre = new JPanel(new BorderLayout(8, 8));
        centre.add(north, BorderLayout.NORTH);
        centre.add(new JScrollPane(requests), BorderLayout.CENTER);

        JButton refresh = new JButton("Refresh my requests");
        refresh.addActionListener(e -> refreshMyRequests());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
        south.add(refresh);

        wrap.add(centre, BorderLayout.CENTER);
        wrap.add(south, BorderLayout.SOUTH);
        return wrap;
    }

    private static JPanel row(String title, JComboBox<?> combo) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.add(new JLabel(title), BorderLayout.WEST);
        p.add(combo, BorderLayout.CENTER);
        return p;
    }

    private void submitStudentRequest() {
        Room room = (Room) roomCombo.getSelectedItem();
        LocalDate date = (LocalDate) dateCombo.getSelectedItem();
        LocalTime start = (LocalTime) startCombo.getSelectedItem();
        LocalTime end = (LocalTime) endCombo.getSelectedItem();
        if (room == null || date == null || start == null || end == null) {
            return;
        }
        if (!start.isBefore(end)) {
            JOptionPane.showMessageDialog(this, "End time must be after start time.", "Request",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        String purpose = purposeField.getText().trim();
        if (purpose.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please describe the purpose of the booking.", "Request",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            campus.addBooking(room.getId(), session.getUsername(), date, start, end, purpose,
                    BookingStatus.PENDING);
            JOptionPane.showMessageDialog(this,
                    "Request submitted. You will see the status update once an administrator reviews it.",
                    "Request",
                    JOptionPane.INFORMATION_MESSAGE);
            purposeField.setText("");
            refreshMyRequests();
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Request", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshMyRequests() {
        myRequestsModel.setRowCount(0);
        List<Booking> mine = campus.bookingsForUser(session.getUsername());
        mine.sort(Comparator.comparing(Booking::getDate).thenComparing(Booking::getStart));
        for (Booking b : mine) {
            String roomLabel = campus.findRoom(b.getRoomId()).map(Room::getName).orElse(b.getRoomId());
            myRequestsModel.addRow(new Object[]{
                    b.getId(),
                    roomLabel,
                    b.getDate(),
                    b.getStart() + "–" + b.getEnd(),
                    b.getStatus().getLabel(),
                    b.getPurpose()
            });
        }
    }

    private JPanel buildAnnouncementsTab() {
        JPanel wrap = new JPanel(new BorderLayout(8, 8));
        wrap.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        JLabel info = new JLabel("Official campus announcements for students and staff.");
        wrap.add(info, BorderLayout.NORTH);
        JTable table = new JTable(announcementsModel);
        wrap.add(new JScrollPane(table), BorderLayout.CENTER);
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
