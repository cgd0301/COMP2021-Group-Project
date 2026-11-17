package hk.edu.polyu.comp.comp2021.crobs.view;

import hk.edu.polyu.comp.comp2021.crobs.controller.Cli;
import hk.edu.polyu.comp.comp2021.crobs.controller.OperationLog;
import hk.edu.polyu.comp.comp2021.crobs.model.Booking;
import hk.edu.polyu.comp.comp2021.crobs.model.CROBS;
import hk.edu.polyu.comp.comp2021.crobs.model.Room;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Supplier;

/**
 * Swing view for the Campus Room Booking System.
 * All actions are translated into the same command language used by the CLI,
 * so both interfaces share validation, permissions, operation logging, and history.
 */
public class CrobsGui {
    private static final int MIN_WINDOW_WIDTH = 960;
    private static final int MIN_WINDOW_HEIGHT = 720;
    private static final int WINDOW_WIDTH = 1180;
    private static final int WINDOW_HEIGHT = 880;
    private static final int GAP_SMALL = 2;
    private static final int GAP_STANDARD = 4;
    private static final int GAP_MEDIUM = 6;
    private static final int GAP_LARGE = 8;
    private static final int GAP_WIDE = 12;
    private static final int HEADER_TOP_PADDING = 8;
    private static final int HEADER_SIDE_PADDING = 10;
    private static final int OUTPUT_BOTTOM_PADDING = 8;
    private static final int MAIN_DIVIDER_LOCATION = 380;
    private static final int ROOM_TABLE_INITIAL_ROWS = 0;
    private static final int BOOKING_TABLE_INITIAL_ROWS = 0;
    private static final int OUTPUT_LINE_COUNT = 8;
    private static final int OUTPUT_FONT_SIZE = 12;
    private static final int TABLE_ROW_HEIGHT = 22;
    private static final int LOGIN_FIELD_COLUMNS = 10;
    private static final int USER_NAME_FIELD_COLUMNS = 22;
    private static final int ID_FIELD_COLUMNS = 16;
    private static final int ROOM_CAPACITY_FIELD_COLUMNS = 7;
    private static final int FEATURE_FIELD_COLUMNS = 20;
    private static final int ROOM_ID_FIELD_COLUMNS = 9;
    private static final int SHORT_FIELD_COLUMNS = 8;
    private static final int DATE_FIELD_COLUMNS = 10;
    private static final int TIME_FIELD_COLUMNS = 6;
    private static final int MIN_CAPACITY_FIELD_COLUMNS = 6;
    private static final int PURPOSE_FIELD_COLUMNS = 18;
    private static final int SEARCH_FEATURE_FIELD_COLUMNS = 18;
    private static final int MODIFY_PURPOSE_FIELD_COLUMNS = 14;
    private static final int CONSOLE_FIELD_COLUMNS = 58;
    private static final double HALF_RESIZE_WEIGHT = 0.5;
    private static final double TAB_RESIZE_WEIGHT = 0.56;

    private final CROBS crobs;
    private final Cli cli;
    private final OperationLog log;
    private final JFrame frame = new JFrame("CROBS - Campus Room Booking System");
    private final JTextField loginUserID = new JTextField(LOGIN_FIELD_COLUMNS);
    private final JLabel sessionLabel = new JLabel("Not logged in");
    private final JLabel historyLabel = new JLabel("Undo: unavailable | Redo: unavailable");
    private final JTextArea outputArea = new JTextArea();
    private final DefaultTableModel roomTableModel = new ReadOnlyTableModel(
            new Object[]{"Room ID", "Room type", "Capacity", "Features"}, ROOM_TABLE_INITIAL_ROWS);
    private final DefaultTableModel bookingTableModel = new ReadOnlyTableModel(
            new Object[]{"Booking ID", "Room ID", "Date", "Start", "End", "Purpose"},
            BOOKING_TABLE_INITIAL_ROWS);
    private boolean logClosed;

    /**
     * Creates the GUI around the existing CROBS model and shared command dispatcher.
     *
     * @param crobs the application model
     * @param cli the shared CLI command dispatcher and undo/redo history
     * @param log the session operation log
     */
    public CrobsGui(CROBS crobs, Cli cli, OperationLog log) {
        this.crobs = crobs;
        this.cli = cli;
        this.log = log;
        buildWindow();
        refreshView();
    }

    /** Displays the application window on the Swing event dispatch thread. */
    public void show() {
        if (SwingUtilities.isEventDispatchThread()) {
            frame.setVisible(true);
        } else {
            SwingUtilities.invokeLater(() -> frame.setVisible(true));
        }
    }

    private void buildWindow() {
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.setMinimumSize(new Dimension(MIN_WINDOW_WIDTH, MIN_WINDOW_HEIGHT));
        frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout(GAP_LARGE, GAP_LARGE));

        frame.add(createHeader(), BorderLayout.NORTH);
        frame.add(createMainContent(), BorderLayout.CENTER);
        frame.add(createOutputPanel(), BorderLayout.SOUTH);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                executeCommand("quit");
            }
        });
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(GAP_LARGE, GAP_STANDARD));
        header.setBorder(BorderFactory.createEmptyBorder(HEADER_TOP_PADDING, HEADER_SIDE_PADDING, GAP_STANDARD, HEADER_SIDE_PADDING));

        JPanel session = new JPanel(new FlowLayout(FlowLayout.LEFT, GAP_MEDIUM, GAP_SMALL));
        session.add(new JLabel("User ID:"));
        session.add(loginUserID);
        session.add(createButton("Login", () -> "login " + value(loginUserID)));
        session.add(createButton("Logout", () -> "logout"));
        session.add(sessionLabel);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, GAP_MEDIUM, GAP_SMALL));
        JButton undoButton = createButton("Undo", () -> "undo");
        JButton redoButton = createButton("Redo", () -> "redo");
        controls.add(undoButton);
        controls.add(redoButton);
        controls.add(createButton("Quit", () -> "quit"));
        controls.add(historyLabel);

        header.add(session, BorderLayout.WEST);
        header.add(controls, BorderLayout.EAST);
        return header;
    }

    private JSplitPane createMainContent() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Users & Roles", createUsersTab());
        tabs.addTab("Rooms", createRoomsTab());
        tabs.addTab("Bookings", createBookingsTab());
        tabs.addTab("Search & Schedule", createSearchTab());
        tabs.addTab("Command Console", createConsoleTab());

        JSplitPane tables = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                createTablePanel("All rooms", roomTableModel),
                createTablePanel("My bookings", bookingTableModel));
        tables.setResizeWeight(HALF_RESIZE_WEIGHT);
        tables.setDividerLocation(HALF_RESIZE_WEIGHT);

        JSplitPane content = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tabs, tables);
        content.setResizeWeight(TAB_RESIZE_WEIGHT);
        content.setDividerLocation(MAIN_DIVIDER_LOCATION);
        content.setBorder(BorderFactory.createEmptyBorder(0, GAP_LARGE, 0, GAP_LARGE));
        return content;
    }

    private JPanel createUsersTab() {
        JPanel panel = createTabPanel();
        JTextField addName = new JTextField(USER_NAME_FIELD_COLUMNS);
        addRow(panel, "Add regular user", addName,
                createButton("Add user", () -> "addUser " + value(addName)));

        JTextField removeID = new JTextField(ID_FIELD_COLUMNS);
        addRow(panel, "Remove regular user ID", removeID,
                createButton("Remove user", () -> "removeUser " + value(removeID)));

        JTextField changeID = new JTextField(ID_FIELD_COLUMNS);
        JComboBox<String> role = new JComboBox<>(new String[]{"REGULAR", "ADMIN", "SUPERADMIN"});
        JPanel changePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        changePanel.add(new JLabel("Target user ID"));
        changePanel.add(changeID);
        changePanel.add(new JLabel("New role"));
        changePanel.add(role);
        changePanel.add(createButton("Change role", () ->
                joinCommand("changeRole", value(changeID), String.valueOf(role.getSelectedItem()))));
        addSection(panel, "Change user access privilege", changePanel);
        addSection(panel, "Access control",
                new JLabel("The model enforces the assignment's admin and superadmin permissions."));
        return panel;
    }

    private JPanel createRoomsTab() {
        JPanel panel = createTabPanel();
        JComboBox<String> roomType = new JComboBox<>(new String[]{
                "LECTURETHEATRE", "COMPUTERLAB", "SEMINARROOM", "MEETINGROOM"});
        JTextField capacity = new JTextField(ROOM_CAPACITY_FIELD_COLUMNS);
        JTextField features = new JTextField(FEATURE_FIELD_COLUMNS);
        JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addPanel.add(new JLabel("Room type"));
        addPanel.add(roomType);
        addPanel.add(new JLabel("Capacity"));
        addPanel.add(capacity);
        addPanel.add(new JLabel("Features (comma-separated)"));
        addPanel.add(features);
        addPanel.add(createButton("Add room", () -> joinCommand("addRoom",
                String.valueOf(roomType.getSelectedItem()), value(capacity), value(features))));
        addSection(panel, "Add room", addPanel);

        JTextField modifyID = new JTextField(ROOM_ID_FIELD_COLUMNS);
        JComboBox<String> field = new JComboBox<>(new String[]{"ROOMTYPE", "CAPACITY", "FEATURES"});
        JTextField newValue = new JTextField(ID_FIELD_COLUMNS);
        JPanel modifyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        modifyPanel.add(new JLabel("Room ID"));
        modifyPanel.add(modifyID);
        modifyPanel.add(new JLabel("Field"));
        modifyPanel.add(field);
        modifyPanel.add(new JLabel("New value"));
        modifyPanel.add(newValue);
        modifyPanel.add(createButton("Modify room", () -> joinCommand("modifyRoom", value(modifyID),
                String.valueOf(field.getSelectedItem()), value(newValue))));
        addSection(panel, "Modify room information", modifyPanel);

        JTextField deleteID = new JTextField(LOGIN_FIELD_COLUMNS);
        addRow(panel, "Delete room ID", deleteID,
                createButton("Delete room", () -> "deleteRoom " + value(deleteID)));
        JTextField lookupID = new JTextField(LOGIN_FIELD_COLUMNS);
        addRow(panel, "List one room ID", lookupID,
                createButton("List room", () -> "listRoom " + value(lookupID)));
        addSection(panel, "List all rooms", createButton("List all rooms", () -> "listAllRooms"));
        return panel;
    }

    private JPanel createBookingsTab() {
        JPanel panel = createTabPanel();
        JTextField bookRoom = new JTextField(SHORT_FIELD_COLUMNS);
        JTextField bookDate = new JTextField("2026-12-01", DATE_FIELD_COLUMNS);
        JTextField bookStart = new JTextField("10:00", TIME_FIELD_COLUMNS);
        JTextField bookEnd = new JTextField("11:00", TIME_FIELD_COLUMNS);
        JTextField bookPurpose = new JTextField(PURPOSE_FIELD_COLUMNS);
        JPanel bookPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bookPanel.add(new JLabel("Room"));
        bookPanel.add(bookRoom);
        bookPanel.add(new JLabel("Date"));
        bookPanel.add(bookDate);
        bookPanel.add(new JLabel("Start"));
        bookPanel.add(bookStart);
        bookPanel.add(new JLabel("End"));
        bookPanel.add(bookEnd);
        bookPanel.add(new JLabel("Purpose"));
        bookPanel.add(bookPurpose);
        bookPanel.add(createButton("Book", () -> joinCommand("book", value(bookRoom), value(bookDate),
                value(bookStart), value(bookEnd), value(bookPurpose))));
        addSection(panel, "Create booking", bookPanel);

        JTextField modifyBookingID = new JTextField(SHORT_FIELD_COLUMNS);
        JTextField modifyRoom = new JTextField(SHORT_FIELD_COLUMNS);
        JTextField modifyDate = new JTextField("2026-12-01", DATE_FIELD_COLUMNS);
        JTextField modifyStart = new JTextField("10:00", TIME_FIELD_COLUMNS);
        JTextField modifyEnd = new JTextField("11:00", TIME_FIELD_COLUMNS);
        JTextField modifyPurpose = new JTextField(MODIFY_PURPOSE_FIELD_COLUMNS);
        JPanel modifyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        modifyPanel.add(new JLabel("Booking ID"));
        modifyPanel.add(modifyBookingID);
        modifyPanel.add(new JLabel("Room"));
        modifyPanel.add(modifyRoom);
        modifyPanel.add(new JLabel("Date"));
        modifyPanel.add(modifyDate);
        modifyPanel.add(new JLabel("Start"));
        modifyPanel.add(modifyStart);
        modifyPanel.add(new JLabel("End"));
        modifyPanel.add(modifyEnd);
        modifyPanel.add(new JLabel("Purpose"));
        modifyPanel.add(modifyPurpose);
        modifyPanel.add(createButton("Modify booking", () -> joinCommand("modifyBooking",
                value(modifyBookingID), value(modifyRoom), value(modifyDate), value(modifyStart),
                value(modifyEnd), value(modifyPurpose))));
        addSection(panel, "Modify booking", modifyPanel);

        JTextField deleteBookingID = new JTextField(LOGIN_FIELD_COLUMNS);
        addRow(panel, "Cancel booking ID", deleteBookingID,
                createButton("Delete booking", () -> "deleteBooking " + value(deleteBookingID)));
        addSection(panel, "List my bookings", createButton("List bookings", () -> "listBookings"));
        return panel;
    }

    private JPanel createSearchTab() {
        JPanel panel = createTabPanel();
        JTextField searchDate = new JTextField("2026-12-01", DATE_FIELD_COLUMNS);
        JTextField searchStart = new JTextField("10:00", TIME_FIELD_COLUMNS);
        JTextField searchEnd = new JTextField("11:00", TIME_FIELD_COLUMNS);
        JTextField minCapacity = new JTextField("1", MIN_CAPACITY_FIELD_COLUMNS);
        JTextField requiredFeatures = new JTextField(SEARCH_FEATURE_FIELD_COLUMNS);
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Date"));
        searchPanel.add(searchDate);
        searchPanel.add(new JLabel("Start"));
        searchPanel.add(searchStart);
        searchPanel.add(new JLabel("End"));
        searchPanel.add(searchEnd);
        searchPanel.add(new JLabel("Minimum capacity"));
        searchPanel.add(minCapacity);
        searchPanel.add(new JLabel("Features"));
        searchPanel.add(requiredFeatures);
        searchPanel.add(createButton("Search rooms", () -> joinCommand("search", value(searchDate),
                value(searchStart), value(searchEnd), value(minCapacity), value(requiredFeatures))));
        addSection(panel, "Find available rooms", searchPanel);

        JTextField scheduleRoom = new JTextField(LOGIN_FIELD_COLUMNS);
        JTextField weekStart = new JTextField("2026-12-01", DATE_FIELD_COLUMNS);
        JPanel schedulePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        schedulePanel.add(new JLabel("Room ID"));
        schedulePanel.add(scheduleRoom);
        schedulePanel.add(new JLabel("Week start date"));
        schedulePanel.add(weekStart);
        schedulePanel.add(createButton("View weekly schedule", () ->
                joinCommand("schedule", value(scheduleRoom), value(weekStart))));
        addSection(panel, "Weekly room schedule", schedulePanel);
        return panel;
    }

    private JPanel createConsoleTab() {
        JPanel panel = createTabPanel();
        JTextField command = new JTextField(CONSOLE_FIELD_COLUMNS);
        JButton execute = new JButton("Execute command");
        execute.addActionListener(event -> {
            executeCommand(value(command));
            command.setText("");
        });
        command.addActionListener(event -> {
            executeCommand(value(command));
            command.setText("");
        });
        addRow(panel, "Enter any supported CLI command", command, execute);
        addSection(panel, "Examples", new JLabel(
                "login S000000  |  addRoom MEETINGROOM 20 PROJECTOR,WHITEBOARD  |  undo  |  redo"));
        return panel;
    }

    private JPanel createOutputPanel() {
        JPanel panel = new JPanel(new BorderLayout(GAP_STANDARD, GAP_STANDARD));
        panel.setBorder(BorderFactory.createEmptyBorder(0, HEADER_SIDE_PADDING, OUTPUT_BOTTOM_PADDING, HEADER_SIDE_PADDING));
        JLabel title = new JLabel("Command output");
        title.setFont(title.getFont().deriveFont(Font.BOLD));
        outputArea.setEditable(false);
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, OUTPUT_FONT_SIZE));
        outputArea.setRows(OUTPUT_LINE_COUNT);
        panel.add(title, BorderLayout.NORTH);
        panel.add(new JScrollPane(outputArea), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createTablePanel(String title, DefaultTableModel model) {
        JPanel panel = new JPanel(new BorderLayout(GAP_STANDARD, GAP_STANDARD));
        panel.setBorder(BorderFactory.createTitledBorder(title));
        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setRowHeight(TABLE_ROW_HEIGHT);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createTabPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 1, GAP_LARGE, GAP_LARGE));
        panel.setBorder(BorderFactory.createEmptyBorder(GAP_WIDE, GAP_WIDE, GAP_WIDE, GAP_WIDE));
        return panel;
    }

    private void addRow(JPanel parent, String label, JTextField field, JButton button) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel rowLabel = new JLabel(label);
        rowLabel.setHorizontalAlignment(SwingConstants.LEFT);
        row.add(rowLabel);
        row.add(field);
        row.add(button);
        addSection(parent, label, row);
    }

    private void addSection(JPanel parent, String title, java.awt.Component content) {
        JPanel section = new JPanel(new BorderLayout(GAP_STANDARD, GAP_STANDARD));
        section.setBorder(BorderFactory.createTitledBorder(title));
        section.add(content, BorderLayout.CENTER);
        parent.add(section);
    }

    private JButton createButton(String label, Supplier<String> commandSupplier) {
        JButton button = new JButton(label);
        button.addActionListener(event -> executeCommand(commandSupplier.get()));
        return button;
    }

    private String value(JTextField field) {
        return field.getText().trim();
    }

    private String joinCommand(String command, String... arguments) {
        StringBuilder builder = new StringBuilder(command);
        for (int index = 0; index < arguments.length; index++) {
            String argument = arguments[index];
            boolean optionalLast = index == arguments.length - 1
                    && (command.equals("addRoom") || command.equals("search"));
            if (argument == null || argument.trim().isEmpty()) {
                if (optionalLast) {
                    continue;
                }
                outputArea.append("Please complete all required fields.\n");
                return null;
            }
            builder.append(' ').append(argument.trim());
        }
        return builder.toString();
    }

    private void executeCommand(String commandText) {
        if (commandText == null || commandText.trim().isEmpty()) {
            return;
        }

        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        PrintStream originalOutput = System.out;
        boolean shouldQuit;
        try (PrintStream redirected = new PrintStream(captured, true, StandardCharsets.UTF_8.name())) {
            System.setOut(redirected);
            shouldQuit = cli.processLine(crobs, commandText, log);
        } catch (IOException ex) {
            outputArea.append("Cannot capture command output: " + ex.getMessage() + "\n");
            shouldQuit = false;
        } finally {
            System.setOut(originalOutput);
        }

        String output = new String(captured.toByteArray(), StandardCharsets.UTF_8);
        outputArea.append("> " + commandText.trim() + "\n");
        if (!output.isEmpty()) {
            outputArea.append(output);
            if (!output.endsWith("\n")) {
                outputArea.append("\n");
            }
        }
        outputArea.setCaretPosition(outputArea.getDocument().getLength());
        refreshView();

        if (shouldQuit) {
            closeLog();
            frame.dispose();
        }
    }

    private void refreshView() {
        if (crobs.currentUser == null) {
            sessionLabel.setText("Not logged in");
        } else {
            sessionLabel.setText("Logged in: " + crobs.currentUser.getUserID()
                    + " (" + crobs.currentUser.getRole().getRole() + ")");
        }
        historyLabel.setText("Undo: " + (cli.canUndo() ? "available" : "unavailable")
                + " | Redo: " + (cli.canRedo() ? "available" : "unavailable"));

        roomTableModel.setRowCount(0);
        bookingTableModel.setRowCount(0);
        if (crobs.currentUser == null) {
            return;
        }

        try {
            Map<String, Room> rooms = new TreeMap<>(crobs.getAllRooms());
            for (Room room : rooms.values()) {
                Set<String> sortedFeatures = new TreeSet<>(room.getFeatures());
                roomTableModel.addRow(new Object[]{room.getRoomID(), room.getRoomType().getType(),
                        room.getCapacity(), String.join(",", sortedFeatures)});
            }
            for (Booking booking : crobs.getBookings()) {
                bookingTableModel.addRow(new Object[]{booking.getBookingID(), booking.getRoomID(),
                        booking.getDate(), booking.getStartTime(), booking.getEndTime(),
                        booking.getPurpose()});
            }
        } catch (RuntimeException ex) {
            outputArea.append(ex.getMessage() + "\n");
        }
    }

    private void closeLog() {
        if (logClosed) {
            return;
        }
        logClosed = true;
        try {
            log.close();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(frame, "Cannot close operation log: " + ex.getMessage(),
                    "Log error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static final class ReadOnlyTableModel extends DefaultTableModel {
        private static final long serialVersionUID = 1L;

        /**
         * Creates a table model whose cells are read-only.
         *
         * @param columnNames the visible column headings
         * @param rowCount the initial number of rows
         */
        ReadOnlyTableModel(Object[] columnNames, int rowCount) {
            super(columnNames, rowCount);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    }

}
