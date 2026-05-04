package com.actmira;

import com.actmira.lexer.Lexer;
import com.actmira.parser.Parser;
import com.actmira.ast.Program;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestParse {
    public static void main(String[] args) throws Exception {
        String source = Files.readString(Path.of("stdlib/canvas2d.mira"));
        Lexer lexer = new Lexer(source);
        Parser parser = new Parser(lexer);
        Program program = parser.parseProgram();
        
        System.out.println("Parsed Statements: " + program.getStatements().size());
        if (!parser.getErrors().isEmpty()) {
            System.out.println("Parse Errors:");
            for (String err : parser.getErrors()) {
                System.out.println("  " + err);
            }
        } else {
            System.out.println("canvas2d.mira parsed successfully without errors.");
        }
    }
}
