package hk.edu.polyu.comp.comp2021.crobs.controller;

import hk.edu.polyu.comp.comp2021.crobs.model.CROBS;
import hk.edu.polyu.comp.comp2021.crobs.model.CrobsException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Receives command lines and dispatches them to the corresponding command objects.
 * The same dispatcher is used by the CLI and the optional Swing GUI.
 */
public class Cli {
    /** Commands keyed by the exact command name entered by the user. */
    public static final HashMap<String, Command> commands = new HashMap<>();

    static {
        commands.put("quit", new QuitCommand());
        commands.put("addUser", new AddUserCommand());
        commands.put("changeRole", new ChangeRoleCommand());
        commands.put("removeUser", new RemoveUserCommand());
        commands.put("login", new LoginCommand());
        commands.put("logout", new LogoutCommand());
        commands.put("addRoom", new AddRoomCommand());
        commands.put("modifyRoom", new ModifyRoomCommand());
        commands.put("deleteRoom", new DeleteRoomCommand());
        commands.put("listRoom", new ListRoomCommand());
        commands.put("listAllRooms", new ListAllRoomsCommand());
        commands.put("book", new BookCommand());
        commands.put("deleteBooking", new DeleteBookingCommand());
        commands.put("listBookings", new ListBookingsCommand());
        commands.put("modifyBooking", new ModifyBookingCommand());
        commands.put("search", new SearchCommand());
        commands.put("schedule", new ScheduleCommand());
    }

    private final UndoRedoManager history = new UndoRedoManager();

    /**
     * Reads commands until quit, a log failure, or the input ends.
     * Each non-empty input line is recorded, including unknown and failed commands.
     *
     * @param crobs the core model
     * @param log the session log, closed when this method returns
     */
    public void run(CROBS crobs, OperationLog log) {
        try (OperationLog sessionLog = log) {
            try (Scanner input = new Scanner(System.in)) {
                while (input.hasNextLine()) {
                    String commandText = input.nextLine().trim();
                    if (!commandText.isEmpty() && processLine(crobs, commandText, sessionLog)) {
                        break;
                    }
                }
            }
        } catch (IOException ex) {
            System.out.println("Cannot close operation log: " + ex.getMessage());
        }
    }

    /**
     * Executes one non-empty command line and records it in the operation log.
     * This method is public so the GUI can share exactly the same command semantics.
     *
     * @param crobs the core model
     * @param commandText one command line
     * @param log the session log
     * @return true when the application should terminate
     */
    public boolean processLine(CROBS crobs, String commandText, OperationLog log) {
        if (commandText == null || commandText.trim().isEmpty()) {
            return false;
        }
        String trimmedCommand = commandText.trim();
        boolean shouldQuit = dispatch(crobs, trimmedCommand);
        return !recordCommand(log, trimmedCommand) || shouldQuit;
    }

    /**
     * @return whether an undo operation is available
     */
    public boolean canUndo() {
        return history.canUndo();
    }

    /**
     * @return whether a redo operation is available
     */
    public boolean canRedo() {
        return history.canRedo();
    }

    private boolean dispatch(CROBS crobs, String commandText) {
        String[] commandLine = commandText.split("\\s+", 2);
        String commandName = commandLine[0];
        try {
            if (commandName.equals("undo")) {
                if (commandLine.length != 1) {
                    throw new CrobsException("Incorrect number of input parameters");
                }
                history.undo(crobs);
                System.out.println("Successfully undo the last command");
                return false;
            }
            if (commandName.equals("redo")) {
                if (commandLine.length != 1) {
                    throw new CrobsException("Incorrect number of input parameters");
                }
                history.redo(crobs);
                System.out.println("Successfully redo the last undone command");
                return false;
            }

            Command command = commands.get(commandName);
            if (command == null) {
                System.out.println("No such command");
                return false;
            }
            String commandDetail = commandLine.length > 1 ? commandLine[1] : null;
            if (isUndoable(commandName)) {
                return history.execute(crobs, command, commandDetail);
            }
            return command.execute(crobs, commandDetail);
        } catch (RuntimeException ex) {
            System.out.println(ex.getMessage());
            return false;
        }
    }

    private boolean isUndoable(String commandName) {
        return !commandName.equals("login")
                && !commandName.equals("logout")
                && !commandName.equals("search")
                && !commandName.equals("schedule")
                && !commandName.equals("listRoom")
                && !commandName.equals("listAllRooms")
                && !commandName.equals("listBookings")
                && !commandName.equals("quit");
    }

    private boolean recordCommand(OperationLog log, String commandText) {
        try {
            log.record(commandText);
            return true;
        } catch (IOException ex) {
            System.out.println("Cannot write operation log: " + ex.getMessage());
            return false;
        }
    }
}
