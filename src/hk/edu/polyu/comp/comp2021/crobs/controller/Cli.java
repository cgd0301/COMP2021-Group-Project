package hk.edu.polyu.comp.comp2021.crobs.controller;
import hk.edu.polyu.comp.comp2021.crobs.model.CROBS;

import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

/**
 * the Cli class is for receiving input command and calling corresponding command object
 */
public class Cli {
    /**
     * commands key: input string for calling a function
     * commands value: object of different command
     */
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

    /**
     * Reads commands until quit or the input ends.
     * Each non-empty line is recorded after it is handled, including unknown and failed commands.
     *
     * @param crobs core class
     * @param log session log, closed when this method returns
     */
    public void run(CROBS crobs, OperationLog log) {
        try (OperationLog sessionLog = log) {
            try (Scanner input = new Scanner(System.in)) {
                readCommands(input, crobs, sessionLog);
            }
        } catch (IOException ex) {
            System.out.println("Cannot close operation log: " + ex.getMessage());
        }
    }

    private void readCommands(Scanner input, CROBS crobs, OperationLog log) {
        while (input.hasNextLine()) {
            String commandText = input.nextLine().trim();
            if (commandText.isEmpty()) {
                continue;
            }
            boolean isQuit = dispatch(crobs, commandText);
            if (!recordCommand(log, commandText) || isQuit) {
                break;
            }
        }
    }

    private boolean dispatch(CROBS crobs, String commandText) {
        String[] commandLine = commandText.split("\\s+", 2);
        try {
            Command command = commands.get(commandLine[0]);
            if (command == null) {
                System.out.println("No such command");
                return false;
            }
            String commandDetail = commandLine.length > 1 ? commandLine[1] : null;
            return command.execute(crobs, commandDetail);
        } catch (RuntimeException ex) {
            System.out.println(ex.getMessage());
            return false;
        }
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
