# COMP2021 CROBS Group Project

## Environment

Use JDK 25 with IntelliJ IDEA Language Level 8, as required by the assignment. Runtime implementation uses only the Java SE API. JUnit libraries under `lib/` are for tests.

## Run the command-line interface

Configure the project SDK and run `hk.edu.polyu.comp.comp2021.crobs.Application` with:

```text
-html crobs.html -txt crobs.txt
```

## Run the graphical interface

Append `-gui` to the same required arguments:

```text
-html crobs.html -txt crobs.txt -gui
```

The GUI is implemented with Java Swing. It provides forms for account and role management, room management, bookings, room search, and weekly schedules. The Command Console tab accepts the original CLI commands.

## Undo and redo

Both interfaces support:

```text
undo
redo
```

The history treats schedule as a read-only query; this differs from the literal BON2 exclusions and requires instructor clarification. Details are in `docs/bonus-features.md`.

## Tests

The original project tests are retained. Additional tests cover state snapshots and undo/redo behavior, including booking indexes, cascading deletion, role transitions, ID counters, and CLI logging of undo/redo commands.

Log files must not already exist: select fresh HTML and TXT paths for each run. The application refuses to overwrite existing files.

The original actor can undo their own latest SUPERADMIN transfer after the transfer demotes them to ADMIN; the same actor can redo it after undo restores their SUPERADMIN role. Other users cannot undo that history entry. This is a design choice pending instructor confirmation.
