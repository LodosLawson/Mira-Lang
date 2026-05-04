package com.actmira.lexer;

public class Lexer {
    private final String input;
    private int position; // current position in input (points to current char)
    private int readPosition; // current reading position in input (after current char)
    private char ch; // current char under examination

    public Lexer(String input) {
        this.input = input;
        readChar();
    }

    private void readChar() {
        if (readPosition >= input.length()) {
            ch = 0; // ASCII code for "NUL"
        } else {
            ch = input.charAt(readPosition);
        }
        position = readPosition;
        readPosition += 1;
    }

    private char peekChar() {
        if (readPosition >= input.length()) {
            return 0;
        } else {
            return input.charAt(readPosition);
        }
    }

    public Token nextToken() {
        Token tok;
        skipWhitespace();

        switch (ch) {
            case '=' -> {
                if (peekChar() == '=') {
                    char c = ch;
                    readChar();
                    tok = new Token(TokenType.EQ, String.valueOf(c) + ch);
                } else {
                    tok = new Token(TokenType.ASSIGN, String.valueOf(ch));
                }
            }
            case '+' -> tok = new Token(TokenType.PLUS, String.valueOf(ch));
            case '-' -> tok = new Token(TokenType.MINUS, String.valueOf(ch));
            case '!' -> {
                if (peekChar() == '=') {
                    char c = ch;
                    readChar();
                    tok = new Token(TokenType.NOT_EQ, String.valueOf(c) + ch);
                } else {
                    tok = new Token(TokenType.BANG, String.valueOf(ch));
                }
            }
            case '#' -> {
                // Support '#' as a comment for script-like feel
                while (ch != '\n' && ch != 0) {
                    readChar();
                }
                return nextToken();
            }
            case '/' -> {
                if (peekChar() == '/') {
                    // It's a comment, skip to end of line
                    while (ch != '\n' && ch != 0) {
                        readChar();
                    }
                    return nextToken();
                } else {
                    tok = new Token(TokenType.SLASH, String.valueOf(ch));
                }
            }
            case '*' -> tok = new Token(TokenType.ASTERISK, String.valueOf(ch));
            case '<' -> tok = new Token(TokenType.LT, String.valueOf(ch));
            case '>' -> tok = new Token(TokenType.GT, String.valueOf(ch));
            case ';' -> tok = new Token(TokenType.SEMICOLON, String.valueOf(ch));
            case ',' -> tok = new Token(TokenType.COMMA, String.valueOf(ch));
            case '{' -> tok = new Token(TokenType.LBRACE, String.valueOf(ch));
            case '}' -> tok = new Token(TokenType.RBRACE, String.valueOf(ch));
            case '(' -> tok = new Token(TokenType.LPAREN, String.valueOf(ch));
            case ')' -> tok = new Token(TokenType.RPAREN, String.valueOf(ch));
            case '[' -> tok = new Token(TokenType.LBRACKET, String.valueOf(ch));
            case ']' -> tok = new Token(TokenType.RBRACKET, String.valueOf(ch));
            case '"' -> tok = new Token(TokenType.STRING, readString());
            case 0 -> tok = new Token(TokenType.EOF, "");
            default -> {
                if (isLetter(ch)) {
                    String literal = readIdentifier();
                    TokenType type = Token.lookupIdent(literal);
                    return new Token(type, literal);
                } else if (isDigit(ch)) {
                    return new Token(TokenType.INT, readNumber());
                } else {
                    tok = new Token(TokenType.ILLEGAL, String.valueOf(ch));
                }
            }
        }

        readChar();
        return tok;
    }

    private void skipWhitespace() {
        while (ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r') {
            readChar();
        }
    }

    private String readIdentifier() {
        int startPos = position;
        while (isLetter(ch) || (isDigit(ch) && position > startPos)) {
            readChar();
        }
        return input.substring(startPos, position);
    }

    private String readNumber() {
        int startPos = position;
        while (isDigit(ch)) {
            readChar();
        }
        return input.substring(startPos, position);
    }

    private String readString() {
        int startPos = position + 1;
        while (true) {
            readChar();
            if (ch == '"' || ch == 0) {
                break;
            }
        }
        return input.substring(startPos, position);
    }

    private boolean isLetter(char ch) {
        return 'a' <= ch && ch <= 'z' || 'A' <= ch && ch <= 'Z' || ch == '_';
    }

    private boolean isDigit(char ch) {
        return '0' <= ch && ch <= '9';
    }
}
