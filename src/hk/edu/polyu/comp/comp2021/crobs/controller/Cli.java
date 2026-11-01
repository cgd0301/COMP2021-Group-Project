package hk.edu.polyu.comp.comp2021.crobs.controller;
import hk.edu.polyu.comp.comp2021.crobs.model.CROBS;

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
    }

    /**
     * @param crobs core class
     */
    public void run(CROBS crobs) {
        try (Scanner input = new Scanner(System.in)) {
            while (input.hasNextLine()) {
                String commandLinetxt = input.nextLine().trim();
                if (commandLinetxt.isEmpty()) {
                    continue;
                }
                String[] commandLine = commandLinetxt.split("\\s+", 2);
                try {
                    Command command = commands.get(commandLine[0]);
                    if (command == null) {
                        System.out.println("No such command");
                    } else {
                        String commandDetail = (commandLine.length > 1) ? commandLine[1] : null;
                        boolean isQuit = command.execute(crobs, commandDetail);
                        if (isQuit) {break;}
                    }
                } catch (RuntimeException ex) {
                    System.out.println(ex.getMessage());
                }
            }
        }
    }
}
