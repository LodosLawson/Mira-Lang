package com.actmira;

import java.nio.file.Files;
import java.nio.file.Path;
import com.actmira.evaluator.Engine;
import com.actmira.repl.Repl;

public class Main {
    public static void main(String[] args) {
        if (args.length > 0) {
            // CLI Compiler Mode
            String filePath = args[0];
            try {
                String code = Files.readString(Path.of(filePath));
                String result = Engine.execute(code);
                System.out.println(result);
            } catch (Exception e) {
                System.err.println("Error reading or executing file: " + e.getMessage());
            }
        } else {
            // GUI IDE Mode
            System.out.println("Launching ACTNverionMira IDE...");
            com.actmira.ide.MiraIDE.launch();
        }
    }
}
