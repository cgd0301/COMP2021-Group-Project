package hk.edu.polyu.comp.comp2021.crobs.controller;
import hk.edu.polyu.comp.comp2021.crobs.model.*;

/**
 * quit command for quiting the system
 */

public class QuitCommand implements Command {
    @Override
    public boolean execute(CROBS crobs, String detail) {
        if (detail == null) {
            System.out.print("System quit");
            return true;
        } else {
            System.out.print("Incorrect number of arguments");
            return false;
        }
    }
}
