package com.compiler.parser;

import com.compiler.ast.Node;
import com.compiler.lexer.Token;
import com.compiler.lexer.TokenType;

import java.util.ArrayList;
import java.util.List;

public class Parser {

    private final List<Token> tokens;
    private int pos;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.pos = 0;
    }

    private Token current() {
        return tokens.get(pos);
    }

    private Token peek(int offset) {
        int idx = pos + offset;
        if (idx >= tokens.size()) return tokens.get(tokens.size() - 1);
        return tokens.get(idx);
    }

    private Token consume() {
        return tokens.get(pos++);
    }

    private Token expect(TokenType type) {
        Token t = current();
        if (t.getType() != type) {
            throw new RuntimeException(
                "Expected " + type + " but got " + t.getType() +
                " ('" + t.getValue() + "') at line " + t.getLine()
            );
        }
        return consume();
    }

    private boolean check(TokenType type) {
        return current().getType() == type;
    }

    private boolean match(TokenType type) {
        if (check(type)) { consume(); return true; }
        return false;
    }

    // ─── Entry point ────────────────────────────────────────
    public Node.Program parse() {
        List<Node> statements = new ArrayList<>();
        while (!check(TokenType.EOF)) {
            statements.add(parseStatement());
        }
        return new Node.Program(statements);
    }

    // ─── Statements ─────────────────────────────────────────
    private Node parseStatement() {
        if (check(TokenType.VAR))    return parseVarDeclaration();
        if (check(TokenType.IF))     return parseIf();
        if (check(TokenType.WHILE))  return parseWhile();
        if (check(TokenType.FUNC))   return parseFuncDeclaration();
        if (check(TokenType.RETURN)) return parseReturn();
        if (check(TokenType.PRINT))  return parsePrint();
        if (check(TokenType.LEFT_BRACE)) return parseBlock();
        return parseExpressionStatement();
    }

    private Node parseVarDeclaration() {
        expect(TokenType.VAR);
        String name = expect(TokenType.IDENTIFIER).getValue();
        expect(TokenType.ASSIGN);
        Node value = parseExpression();
        expect(TokenType.SEMICOLON);
        return new Node.VarDeclaration(name, value);
    }

    private Node parseIf() {
        expect(TokenType.IF);
        expect(TokenType.LEFT_PAREN);
        Node condition = parseExpression();
        expect(TokenType.RIGHT_PAREN);
        Node thenBranch = parseBlock();
        Node elseBranch = null;
        if (match(TokenType.ELSE)) {
            elseBranch = parseBlock();
        }
        return new Node.IfStatement(condition, thenBranch, elseBranch);
    }

    private Node parseWhile() {
        expect(TokenType.WHILE);
        expect(TokenType.LEFT_PAREN);
        Node condition = parseExpression();
        expect(TokenType.RIGHT_PAREN);
        Node body = parseBlock();
        return new Node.WhileStatement(condition, body);
    }

    private Node parseFuncDeclaration() {
        expect(TokenType.FUNC);
        String name = expect(TokenType.IDENTIFIER).getValue();
        expect(TokenType.LEFT_PAREN);
        List<String> params = new ArrayList<>();
        if (!check(TokenType.RIGHT_PAREN)) {
            params.add(expect(TokenType.IDENTIFIER).getValue());
            while (match(TokenType.COMMA)) {
                params.add(expect(TokenType.IDENTIFIER).getValue());
            }
        }
        expect(TokenType.RIGHT_PAREN);
        Node body = parseBlock();
        return new Node.FuncDeclaration(name, params, body);
    }

    private Node parseReturn() {
        expect(TokenType.RETURN);
        Node value = parseExpression();
        expect(TokenType.SEMICOLON);
        return new Node.ReturnStatement(value);
    }

    private Node parsePrint() {
        expect(TokenType.PRINT);
        expect(TokenType.LEFT_PAREN);
        Node value = parseExpression();
        expect(TokenType.RIGHT_PAREN);
        expect(TokenType.SEMICOLON);
        return new Node.PrintStatement(value);
    }

    private Node.Block parseBlock() {
        expect(TokenType.LEFT_BRACE);
        List<Node> statements = new ArrayList<>();
        while (!check(TokenType.RIGHT_BRACE) && !check(TokenType.EOF)) {
            statements.add(parseStatement());
        }
        expect(TokenType.RIGHT_BRACE);
        return new Node.Block(statements);
    }

    private Node parseExpressionStatement() {
        String name = current().getValue();
        if (check(TokenType.IDENTIFIER) && peek(1).getType() == TokenType.ASSIGN) {
            consume();
            expect(TokenType.ASSIGN);
            Node value = parseExpression();
            expect(TokenType.SEMICOLON);
            return new Node.Assignment(name, value);
        }
        Node expr = parseExpression();
        expect(TokenType.SEMICOLON);
        return expr;
    }

    // ─── Expressions ────────────────────────────────────────
    private Node parseExpression() {
        return parseOr();
    }

    private Node parseOr() {
        Node left = parseAnd();
        while (check(TokenType.OR)) {
            String op = consume().getValue();
            Node right = parseAnd();
            left = new Node.BinaryOp(left, op, right);
        }
        return left;
    }

    private Node parseAnd() {
        Node left = parseEquality();
        while (check(TokenType.AND)) {
            String op = consume().getValue();
            Node right = parseEquality();
            left = new Node.BinaryOp(left, op, right);
        }
        return left;
    }

    private Node parseEquality() {
        Node left = parseComparison();
        while (check(TokenType.EQUALS) || check(TokenType.NOT_EQUALS)) {
            String op = consume().getValue();
            Node right = parseComparison();
            left = new Node.BinaryOp(left, op, right);
        }
        return left;
    }

    private Node parseComparison() {
        Node left = parseAddSub();
        while (check(TokenType.LESS_THAN) || check(TokenType.GREATER_THAN) ||
               check(TokenType.LESS_EQUAL) || check(TokenType.GREATER_EQUAL)) {
            String op = consume().getValue();
            Node right = parseAddSub();
            left = new Node.BinaryOp(left, op, right);
        }
        return left;
    }

    private Node parseAddSub() {
        Node left = parseMulDiv();
        while (check(TokenType.PLUS) || check(TokenType.MINUS)) {
            String op = consume().getValue();
            Node right = parseMulDiv();
            left = new Node.BinaryOp(left, op, right);
        }
        return left;
    }

    private Node parseMulDiv() {
        Node left = parseUnary();
        while (check(TokenType.MULTIPLY) || check(TokenType.DIVIDE) || check(TokenType.MODULO)) {
            String op = consume().getValue();
            Node right = parseUnary();
            left = new Node.BinaryOp(left, op, right);
        }
        return left;
    }

    private Node parseUnary() {
        if (check(TokenType.NOT) || check(TokenType.MINUS)) {
            String op = consume().getValue();
            Node operand = parseUnary();
            return new Node.UnaryOp(op, operand);
        }
        return parsePrimary();
    }

    private Node parsePrimary() {
        Token t = current();

        if (t.getType() == TokenType.NUMBER) {
            consume();
            return new Node.NumberLiteral(Double.parseDouble(t.getValue()));
        }
        if (t.getType() == TokenType.STRING) {
            consume();
            return new Node.StringLiteral(t.getValue());
        }
        if (t.getType() == TokenType.TRUE) {
            consume();
            return new Node.BooleanLiteral(true);
        }
        if (t.getType() == TokenType.FALSE) {
            consume();
            return new Node.BooleanLiteral(false);
        }
        if (t.getType() == TokenType.NULL) {
            consume();
            return new Node.NullLiteral();
        }
        if (t.getType() == TokenType.IDENTIFIER) {
            consume();
            if (check(TokenType.LEFT_PAREN)) {
                consume();
                List<Node> args = new ArrayList<>();
                if (!check(TokenType.RIGHT_PAREN)) {
                    args.add(parseExpression());
                    while (match(TokenType.COMMA)) {
                        args.add(parseExpression());
                    }
                }
                expect(TokenType.RIGHT_PAREN);
                return new Node.FuncCall(t.getValue(), args);
            }
            return new Node.Identifier(t.getValue());
        }
        if (t.getType() == TokenType.LEFT_PAREN) {
            consume();
            Node expr = parseExpression();
            expect(TokenType.RIGHT_PAREN);
            return expr;
        }

        throw new RuntimeException("Unexpected token: " + t.getType() +
                " ('" + t.getValue() + "') at line " + t.getLine());
    }
}