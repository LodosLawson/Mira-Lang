package com.actmira.evaluator;

import com.actmira.lexer.Lexer;
import com.actmira.parser.Parser;
import com.actmira.ast.Program;

public class Engine {
    
    // Global Memory Scope that persists while IDE is running
    private static Environment globalEnv = new Environment();
    
    // Callback for rendering HTML to IDE Web View
    public static java.util.function.Consumer<String> htmlRenderer = null;

    public static String execute(String code) {
        StringBuilder output = new StringBuilder();
        
        if (code == null || code.trim().isEmpty()) {
            return "No code provided.";
        }
        
        try {
            // 1. Lexer & Parser
            Lexer lexer = new Lexer(code);
            Parser parser = new Parser(lexer);
            Program program = parser.parseProgram();
            
            // Syntax Errors check
            if (!parser.getErrors().isEmpty()) {
                output.append("Syntax Errors:\n");
                for (String err : parser.getErrors()) {
                    output.append("- ").append(err).append("\n");
                }
                return output.toString();
            }
            
            // 2. Evaluator Runtime
            // We reset the global environment for each run in the IDE so variables don't conflict between runs
            globalEnv = new Environment();
            Evaluator.importedPaths.clear(); // Reset import cache so stdlib re-imports each run

            MiraObject result = Evaluator.eval(program, globalEnv);
            
            if (result != null) {
                if (result.type().equals("ERROR")) {
                    output.append(result.inspect());
                } else if (result.type().equals("STRING")) {
                    // This string contains the print outputs
                    output.append(result.inspect());
                } else if (result != Evaluator.NULL) {
                    // If there's a loose value not printed, show it optionally
                    output.append("\n[Return]: ").append(result.inspect());
                }
            }
            
        } catch (Exception e) {
            output.append("\nFatal Execution Error: ").append(e.getMessage()).append("\n");
        }
        
        return output.toString();
    }
}
