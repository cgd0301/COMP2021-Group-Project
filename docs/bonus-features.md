# Bonus Features: GUI and Undo/Redo

This project provides both bonus features specified in the COMP2021 group-project description.

## BON1 — Graphical User Interface

The application retains its command-line interface. To open the Swing graphical interface, use the existing required log arguments and append `-gui`:

```text
java hk.edu.polyu.comp.comp2021.crobs.Application -html crobs.html -txt crobs.txt -gui
```

In IntelliJ IDEA, keep the project SDK at JDK 25 and Language Level 8, then put the following in the Run Configuration's Program arguments:

```text
-html crobs.html -txt crobs.txt -gui
```

Without `-gui`, the application starts in CLI mode as before.

The GUI provides tabs for user and role management, room management, bookings, searching and weekly schedules, and a free-form command console. The room and current-user booking tables show the current model data. Every GUI action is converted to the same command line used by the CLI and is passed through the same command dispatcher. Therefore both interfaces share the existing validation, permission checks, undo/redo history, and REQ1 operation logging. No external GUI library is required; the implementation uses Java Swing from the standard Java API.

## BON2 — Undo and Redo

The CLI and GUI both support:

```text
undo
redo
```

A successful undoable command stores a model-state memento. `undo` restores the state immediately before the latest such command; `redo` restores the state removed by the latest undo. Executing a new undoable command after an undo clears the redo history. Failed commands do not enter the history.

The history excludes `login`, `logout`, `search`, `schedule`, `listRoom`, `listAllRooms`, `listBookings`, and `quit`. Schedule is treated as a read-only query so it does not clear redo; this is an interpretation that needs instructor confirmation because BON2 does not explicitly exclude schedule. The `undo` and `redo` controls themselves are not added to the history. As required by REQ1, all non-empty commands, including `undo` and `redo`, are still written to both operation logs.

A memento covers users and roles, rooms and features, bookings, user-to-booking and room-to-booking indexes, and the generated-ID counters. Restoring it rebuilds shared booking references across all indexes. The active login session is deliberately not captured or rewound, because login and logout are not undoable commands.

Only the original actor may undo or redo an entry and must remain logged in with the authority required by the original command. One exception is undoing their own transfer of SUPERADMIN: the original actor, now ADMIN, may undo that specific latest transfer; redo requires that actor to be SUPERADMIN again. History is retained across login changes but cannot skip an entry belonging to another user. Restoration is rejected if it would remove the active user. Snapshots rebuild model objects, so clients should re-query by ID after undo/redo; retaining references across restoration is unsupported. Snapshots consume memory proportional to the state size and history length. ID counters are restored and IDs from undone creations can be reused; confirm this interpretation with the instructor.
