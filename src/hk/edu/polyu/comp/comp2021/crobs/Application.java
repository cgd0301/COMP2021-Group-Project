package hk.edu.polyu.comp.comp2021.crobs;
import hk.edu.polyu.comp.comp2021.crobs.controller.*;
import hk.edu.polyu.comp.comp2021.crobs.model.*;

import java.io.IOException;

public class Application {

    public static void main(String[] args){
        // Initialize and utilize the system
        String htmlPath = null;
        String txtPath = null;
        // Manipulating input paramters
        if (args.length != 4) {
            System.out.println("Incorrect number of arguments");
            return;
        }

        for (int i = 0; i < args.length; i += 2) {
            if (args[i].equals("-html")) {
                if (i + 1 >= args.length || htmlPath != null) {
                    System.out.println("Missing input parameters");
                    return;
                } else if (!args[i + 1].matches(".*\\.html")) {
                    System.out.println("Invalid input parameters: invalid html file name");
                    return;
                } else {
                    htmlPath = args[i + 1];
                }
            } else if (args[i].equals("-txt")) {
                if (i + 1 >= args.length || txtPath != null) {
                    System.out.println("Missing input parameters");
                    return;
                } else if (!args[i + 1].matches(".*\\.txt")) {
                    System.out.println("Invalid input parameters: invalid txt file name");
                    return;
                } else {
                    txtPath = args[i + 1];
                }
            } else {
                System.out.println("Unknown argument: " + args[i]);
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
            new Cli().run(new CROBS(), log);
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }
}

