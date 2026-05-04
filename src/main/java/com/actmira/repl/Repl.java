package com.actmira.repl;

import com.actmira.lexer.Lexer;
import com.actmira.lexer.Token;
import com.actmira.lexer.TokenType;

import java.util.Scanner;

public class Repl {
    private static final String PROMPT = ">> ";

    public static void start() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print(PROMPT);
            if (!scanner.hasNextLine()) {
                return;
            }
            String line = scanner.nextLine();
            if (line == null || line.trim().isEmpty()) {
                continue;
            }
            if (line.equalsIgnoreCase("exit")) {
                break;
            }

            Lexer lexer = new Lexer(line);
            Token token = lexer.nextToken();
            while (token.type() != TokenType.EOF) {
                System.out.println(token);
                token = lexer.nextToken();
            }
        }
    }
}
