package com.actmira.parser;

import com.actmira.ast.*;
import com.actmira.lexer.Lexer;
import com.actmira.lexer.Token;
import com.actmira.lexer.TokenType;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final Lexer lexer;
    private Token currentToken;
    private Token peekToken;
    private final List<String> errors;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
        this.errors = new ArrayList<>();
        nextToken();
        nextToken();
    }

    public List<String> getErrors() { return errors; }

    private void nextToken() {
        currentToken = peekToken;
        peekToken = lexer.nextToken();
    }

    public Program parseProgram() {
        Program program = new Program();
        while (currentToken.type() != TokenType.EOF) {
            Statement stmt = parseStatement();
            if (stmt != null) {
                program.getStatements().add(stmt);
            }
            nextToken();
        }
        return program;
    }

    private Statement parseStatement() {
        switch (currentToken.type()) {
            case LET:
            case USTGLOBAL:
                return parseLetStatement();
            case FUNCTION:
                return parseFunctionStatement();
            case RETURN: return parseReturnStatement();
            case CLASS:  return parseClassStatement();
            case IMPORT: return parseImportStatement();
            default: return parseExpressionStatement();
        }
    }

    private Statement parseImportStatement() {
        nextToken(); // skip 'import'
        if (currentToken.type() != TokenType.STRING) {
            errors.add("expected string after import, got " + currentToken.literal());
            return null;
        }
        String path = currentToken.literal();
        if (peekToken.type() == TokenType.SEMICOLON) nextToken();
        return new ImportStatement(path);
    }


    private Statement parseFunctionStatement() {
        Token fnToken = currentToken;
        nextToken(); // skip fn

        if (currentToken.type() != TokenType.IDENT) {
            errors.add("expected function name after 'fn', got '" + currentToken.literal() + "'");
            return null;
        }

        Identifier name = new Identifier(currentToken, currentToken.literal());
        nextToken(); // skip name

        FunctionLiteral func = parseFunctionLiteral(fnToken);
        if (func == null) {
            errors.add("malformed function declaration for '" + name.getValue() + "': expected '('");
            return null;
        }
        func.setName(name);

        LetStatement stmt = new LetStatement(fnToken);
        stmt.setName(name);
        stmt.setValue(func);
        return stmt;
    }

    private FunctionLiteral parseFunctionLiteral(Token fnToken) {
        FunctionLiteral lit = new FunctionLiteral(fnToken);
        
        if (currentToken.type() != TokenType.LPAREN) return null;
        nextToken(); // skip (
        
        while (currentToken.type() != TokenType.RPAREN && currentToken.type() != TokenType.EOF) {
            if (currentToken.type() == TokenType.IDENT) {
                lit.getParameters().add(new Identifier(currentToken, currentToken.literal()));
            }
            nextToken();
            if (currentToken.type() == TokenType.COMMA) nextToken();
        }
        nextToken(); // skip )
        
        if (currentToken.type() != TokenType.LBRACE) return null;
        lit.setBody(parseBlockStatement());
        
        return lit;
    }

    private BlockStatement parseBlockStatement() {
        BlockStatement block = new BlockStatement(currentToken);
        nextToken(); // skip {
        
        while (currentToken.type() != TokenType.RBRACE && currentToken.type() != TokenType.EOF) {
            Statement stmt = parseStatement();
            if (stmt != null) {
                block.getStatements().add(stmt);
            }
            nextToken();
        }
        return block;
    }

    private LetStatement parseLetStatement() {
        LetStatement stmt = new LetStatement(currentToken);
        if (peekToken.type() == TokenType.IDENT) {
            nextToken();
            stmt.setName(new Identifier(currentToken, currentToken.literal()));
        }
        nextToken(); // Skip IDENT
        if (currentToken.literal().equals("=")) {
            nextToken(); // Skip =
        }
        stmt.setValue(parseExpression(0));
        
        while (currentToken.type() != TokenType.SEMICOLON && currentToken.type() != TokenType.EOF) {
            nextToken();
        }
        return stmt;
    }

    private ExpressionStatement parseExpressionStatement() {
        ExpressionStatement stmt = new ExpressionStatement(currentToken);
        stmt.setExpression(parseExpression(0));
        
        if (peekToken.type() == TokenType.SEMICOLON) {
            nextToken();
        }
        return stmt;
    }

    private Expression parseExpression(int precedence) {
        Expression leftExp = parsePrefix();
        if (leftExp == null) return null;

        while (peekToken.literal().equals("+") || peekToken.literal().equals("-") || peekToken.literal().equals("*") || peekToken.literal().equals("/") || peekToken.type() == TokenType.LPAREN || peekToken.literal().equals(".") || peekToken.literal().equals("==") || peekToken.literal().equals("!=") || peekToken.literal().equals("<") || peekToken.literal().equals(">") || peekToken.type() == TokenType.LBRACKET) {
            if (peekToken.type() == TokenType.LPAREN) {
                nextToken(); // current is (
                leftExp = parseCallExpression(leftExp);
            } else if (peekToken.type() == TokenType.LBRACKET) {
                nextToken(); // current is [
                leftExp = parseIndexExpression(leftExp);
            } else {
                nextToken();
                InfixExpression infix = new InfixExpression(currentToken, currentToken.literal(), leftExp);
                nextToken();
                infix.setRight(parseExpression(1));
                leftExp = infix;
            }
        }

        return leftExp;
    }

    private Expression parseIndexExpression(Expression left) {
        IndexExpression exp = new IndexExpression(currentToken, left);
        nextToken(); // skip [
        exp.setIndex(parseExpression(0));
        
        if (peekToken.type() == TokenType.RBRACKET) {
            nextToken();
        }
        return exp;
    }

    private Expression parseCallExpression(Expression function) {
        CallExpression call = new CallExpression(currentToken, function);
        nextToken(); // skip (
        
        while (currentToken.type() != TokenType.RPAREN && currentToken.type() != TokenType.EOF) {
            Expression arg = parseExpression(0);
            if (arg != null) call.getArguments().add(arg);
            nextToken();
            if (currentToken.type() == TokenType.COMMA) nextToken();
        }
        return call;
    }

    private Expression parsePrefix() {
        switch (currentToken.type()) {
            case IDENT:
                return new Identifier(currentToken, currentToken.literal());
            case INT:
                try {
                    int value = Integer.parseInt(currentToken.literal());
                    return new IntegerLiteral(currentToken, value);
                } catch (NumberFormatException e) {
                    errors.add("Could not parse " + currentToken.literal() + " as integer");
                    return null;
                }
            case STRING:
                return new StringLiteral(currentToken, currentToken.literal());
            case TRUE:
                return new BooleanLiteral(currentToken, true);
            case FALSE:
                return new BooleanLiteral(currentToken, false);
            case LBRACKET:
                return parseArrayLiteral();
            case PRINT:
                Token printToken = currentToken;
                nextToken(); // skip print
                if (currentToken.literal().equals("(")) nextToken();
                Expression arg = parseExpression(0);
                if (peekToken.literal().equals(")")) nextToken();
                CallExpression call = new CallExpression(printToken, new Identifier(printToken, "print"));
                call.getArguments().add(arg);
                return call;
            case NEW:
                // Parse class instantiation: new DatabaseConnection()
                Token newToken = currentToken;
                nextToken(); // skip new
                Identifier className = new Identifier(currentToken, currentToken.literal());
                nextToken(); // skip className
                CallExpression newCall = new CallExpression(newToken, className);
                if (currentToken.literal().equals("(")) {
                    nextToken(); // skip (
                    while (currentToken.type() != TokenType.RPAREN && currentToken.type() != TokenType.EOF) {
                        Expression a = parseExpression(0);
                        if (a != null) newCall.getArguments().add(a);
                        nextToken();
                        if (currentToken.type() == TokenType.COMMA) nextToken();
                    }
                }
                return newCall;
            case IF:
                return parseIfExpression();
            case WHILE:
                return parseWhileExpression();
            default:
                return null;
        }
    }

    private ArrayLiteral parseArrayLiteral() {
        ArrayLiteral array = new ArrayLiteral(currentToken);
        nextToken(); // skip [
        
        while (currentToken.type() != TokenType.RBRACKET && currentToken.type() != TokenType.EOF) {
            Expression exp = parseExpression(0);
            if (exp != null) array.getElements().add(exp);
            nextToken();
            if (currentToken.type() == TokenType.COMMA) nextToken();
        }
        return array;
    }

    private ReturnStatement parseReturnStatement() {
        ReturnStatement stmt = new ReturnStatement(currentToken);
        nextToken(); // skip return
        
        stmt.setReturnValue(parseExpression(0));
        
        if (peekToken.type() == TokenType.SEMICOLON) {
            nextToken();
        }
        return stmt;
    }

    private ClassStatement parseClassStatement() {
        ClassStatement stmt = new ClassStatement(currentToken);
        if (peekToken.type() == TokenType.IDENT) {
            nextToken();
            stmt.setName(new Identifier(currentToken, currentToken.literal()));
        }
        while (currentToken.type() != TokenType.RBRACE && currentToken.type() != TokenType.EOF) {
            nextToken();
        }
        return stmt;
    }

    private Expression parseIfExpression() {
        IfExpression exp = new IfExpression(currentToken);
        nextToken(); // skip 'if'
        if (currentToken.literal().equals("(")) {
            nextToken(); // skip '('
            exp.setCondition(parseExpression(0));
            if (peekToken.literal().equals(")")) nextToken(); // move to )
            nextToken(); // move to {
        } else {
            exp.setCondition(parseExpression(0));
            nextToken();
        }
        
        if (currentToken.type() == TokenType.LBRACE) {
            exp.setConsequence(parseBlockStatement());
        }
        
        if (peekToken.type() == TokenType.ELSE) {
            nextToken(); // move to else
            nextToken(); // move to {
            if (currentToken.type() == TokenType.LBRACE) {
                exp.setAlternative(parseBlockStatement());
            }
        }
        return exp;
    }

    private Expression parseWhileExpression() {
        WhileExpression exp = new WhileExpression(currentToken);
        nextToken(); // skip 'while'
        if (currentToken.literal().equals("(")) {
            nextToken(); // skip '('
            exp.setCondition(parseExpression(0));
            if (peekToken.literal().equals(")")) nextToken(); // move to )
            nextToken(); // move to {
        } else {
            exp.setCondition(parseExpression(0));
            nextToken();
        }
        
        if (currentToken.type() == TokenType.LBRACE) {
            exp.setConsequence(parseBlockStatement());
        }
        return exp;
    }
}
