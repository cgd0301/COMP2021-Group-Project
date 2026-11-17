package hk.edu.polyu.comp.comp2021.crobs;

import hk.edu.polyu.comp.comp2021.crobs.controller.Cli;
import hk.edu.polyu.comp.comp2021.crobs.controller.CompositeOperationLog;
import hk.edu.polyu.comp.comp2021.crobs.controller.OperationLog;
import hk.edu.polyu.comp.comp2021.crobs.model.CROBS;
import hk.edu.polyu.comp.comp2021.crobs.view.CrobsGui;

import javax.swing.SwingUtilities;
import java.io.IOException;

/** Application entry point for the CLI and optional Swing GUI. */
public class Application {
    private static final int REQUIRED_ARGUMENT_COUNT = 4;
    private static final int GUI_ARGUMENT_COUNT = 5;
    /**
     * Starts CROBS with both required operation logs.
     * Append -gui to launch the graphical interface; without it, the CLI is used.
     *
     * @param args command-line options: -html path -txt path [-gui]
     */
    public static void main(String[] args) {
        String htmlPath = null;
        String txtPath = null;
        boolean guiMode = false;

        if (args.length != REQUIRED_ARGUMENT_COUNT && args.length != GUI_ARGUMENT_COUNT) {
            System.out.println("Incorrect number of arguments");
            return;
        }

        for (int index = 0; index < args.length; index++) {
            String argument = args[index];
            if (argument.equals("-gui")) {
                if (guiMode) {
                    System.out.println("Duplicate -gui argument");
                    return;
                }
                guiMode = true;
            } else if (argument.equals("-html") || argument.equals("-txt")) {
                if (index + 1 >= args.length || args[index + 1].startsWith("-")) {
                    System.out.println("Missing input parameters");
                    return;
                }
                String path = args[++index];
                if (argument.equals("-html")) {
                    if (htmlPath != null) {
                        System.out.println("Missing input parameters");
                        return;
                    }
                    if (!path.matches(".*\\.html")) {
                        System.out.println("Invalid input parameters: invalid html file name");
                        return;
                    }
                    htmlPath = path;
                } else {
                    if (txtPath != null) {
                        System.out.println("Missing input parameters");
                        return;
                    }
                    if (!path.matches(".*\\.txt")) {
                        System.out.println("Invalid input parameters: invalid txt file name");
                        return;
                    }
                    txtPath = path;
                }
            } else {
                System.out.println("Unknown argument: " + argument);
                return;
            }
        }

        if (htmlPath == null) {
            System.out.println("Missing -html path");
            return;
        }
        if (txtPath == null) {
            System.out.println("Missing -txt path");
            return;
        }

        try {
            OperationLog log = CompositeOperationLog.open(htmlPath, txtPath);
            CROBS crobs = new CROBS();
            Cli cli = new Cli();
            if (guiMode) {
                SwingUtilities.invokeLater(() -> new CrobsGui(crobs, cli, log).show());
            } else {
                cli.run(crobs, log);
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }
}
