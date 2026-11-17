package hk.edu.polyu.comp.comp2021.crobs.controller;

import hk.edu.polyu.comp.comp2021.crobs.model.CROBS;
import hk.edu.polyu.comp.comp2021.crobs.model.CrobsException;
import hk.edu.polyu.comp.comp2021.crobs.model.UserRole;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Maintains the undo and redo history for successful state-changing commands.
 * Login, logout, queries, and the undo/redo operations themselves are excluded
 * by the command dispatcher, in accordance with the project specification.
 */
public class UndoRedoManager {
    private static final class Entry {
        private final CROBS.Memento state;
        private final String actor;
        private final UserRole requiredRole;
        private final boolean superadminTransfer;

        private Entry(CROBS.Memento state, String actor, UserRole requiredRole, boolean superadminTransfer) {
            this.state = state;
            this.actor = actor;
            this.requiredRole = requiredRole;
            this.superadminTransfer = superadminTransfer;
        }
    }

    private final Deque<Entry> undoHistory = new ArrayDeque<>();
    private final Deque<Entry> redoHistory = new ArrayDeque<>();

    /** Executes a command and records its state only after it succeeds. */
    public boolean execute(CROBS crobs, Command command, String detail) {
        crobs.checkLoggedIn();
        String actor = crobs.currentUser.getUserID();
        UserRole required = requiredRole(command);
        CROBS.Memento before = crobs.createMemento();
        boolean shouldQuit = command.execute(crobs, detail);
        boolean transfer = command instanceof ChangeRoleCommand
                && crobs.currentUser.getRole() == UserRole.ADMIN;
        undoHistory.push(new Entry(before, actor, required, transfer));
        redoHistory.clear();
        return shouldQuit;
    }

    private UserRole requiredRole(Command command) {
        if (command instanceof ChangeRoleCommand) {
            return UserRole.SUPERADMIN;
        }
        if (command instanceof AddUserCommand || command instanceof RemoveUserCommand
                || command instanceof AddRoomCommand || command instanceof ModifyRoomCommand
                || command instanceof DeleteRoomCommand) {
            return UserRole.ADMIN;
        }
        return UserRole.REGULAR;
    }

    /** Undo the latest operation by its original actor, subject to current authority. */
    public void undo(CROBS crobs) {
        move(crobs, undoHistory, redoHistory, "Nothing to undo");
    }

    /** Redo the latest undone operation by its original actor. */
    public void redo(CROBS crobs) {
        move(crobs, redoHistory, undoHistory, "Nothing to redo");
    }

    private void move(CROBS crobs, Deque<Entry> source, Deque<Entry> target, String emptyMessage) {
        crobs.checkLoggedIn();
        if (source.isEmpty()) {
            throw new CrobsException(emptyMessage);
        }
        Entry entry = source.peek();
        boolean transferUndo = source == undoHistory && entry.superadminTransfer
                && crobs.currentUser.getRole() == UserRole.ADMIN;
        if (!entry.actor.equals(crobs.currentUser.getUserID())
                || (!crobs.currentUser.compare(entry.requiredRole) && !transferUndo)) {
            throw new CrobsException("Insufficient permission to change this history entry");
        }
        CROBS.Memento current = crobs.createMemento();
        crobs.restoreMemento(entry.state);
        source.pop();
        target.push(new Entry(current, entry.actor, entry.requiredRole, entry.superadminTransfer));
    }

    /**
     * @return whether an undo operation is currently available
     */
    public boolean canUndo() {
        return !undoHistory.isEmpty();
    }

    /**
     * @return whether a redo operation is currently available
     */
    public boolean canRedo() {
        return !redoHistory.isEmpty();
    }
}
