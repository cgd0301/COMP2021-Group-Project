package hk.edu.polyu.comp.comp2021.crobs.controller;
import hk.edu.polyu.comp.comp2021.crobs.model.*;

public class LogoutCommand implements Command {
    @Override
    public boolean execute(CROBS crobs, String detail) {
        if (detail != null) {
            throw new CrobsException("Incorrect number of input parameters");
        } else {
            crobs.logout();
            return false;
        }
    }
}
