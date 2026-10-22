package hk.edu.polyu.comp.comp2021.crobs.controller;

import hk.edu.polyu.comp.comp2021.crobs.model.CROBS;

/**
 * public interface for command classes
 */

public interface Command {
    /**
     * @param crobs the management class
     * @param detail the input paramters for a command
     * @return whether the system is required to quit
     */
    boolean execute(CROBS crobs, String detail);
}