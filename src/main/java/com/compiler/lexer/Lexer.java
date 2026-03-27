package com.compiler.lexer;

import java.util.ArrayList;
import java.util.List;

public class Lexer {

    private final String source;
    private int pos;
    private int line;
    private int column;
    private final List<Token> tokens;

    public Lexer(String source) {
        this.source = source;
        this.pos = 0;
        this.line = 1;
        this.column = 1;
        this.tokens = new ArrayList<>();
    }

    public List<Token> tokenize() {
        while (pos < source.length()) {
            skipWhitespaceAndComments();
            if (pos >= source.length()) break;

            char c = current();

            if (Character.isDigit(c)) {
                readNumber();
            } else if (Character.isLetter(c) || c == '_') {
                readIdentifierOrKeyword();
            } else if (c == '"') {
                readString();
            } else {
                readSymbol();
            }
        }

        tokens.add(new Token(TokenType.EOF, "", line, column));
        return tokens;
    }

    private char current() {
        return source.charAt(pos);
    }

    private char peek(int offset) {
        int idx = pos + offset;
        if (idx >= source.length()) return '\0';
        return source.charAt(idx);
    }

    private void advance() {
        if (pos < source.length() && source.charAt(pos) == '\n') {
            line++;
            column = 1;
        } else {
            column++;
        }
        pos++;
    }

    private void skipWhitespaceAndComments() {
        while (pos < source.length()) {
            char c = current();
            if (c == ' ' || c == '\t' || c == '\r' || c == '\n') {
                advance();
            } else if (c == '/' && peek(1) == '/') {
                while (pos < source.length() && current() != '\n') {
                    advance();
                }
            } else {
                break;
            }
        }
    }

    private void readNumber() {
        int startCol = column;
        StringBuilder sb = new StringBuilder();
        while (pos < source.length() && (Character.isDigit(current()) || current() == '.')) {
            sb.append(current());
            advance();
        }
        tokens.add(new Token(TokenType.NUMBER, sb.toString(), line, startCol));
    }

    private void readIdentifierOrKeyword() {
        int startCol = column;
        StringBuilder sb = new StringBuilder();
        while (pos < source.length() && (Character.isLetterOrDigit(current()) || current() == '_')) {
            sb.append(current());
            advance();
        }
        String word = sb.toString();
        tokens.add(new Token(getKeywordOrIdentifier(word), word, line, startCol));
    }

    private TokenType getKeywordOrIdentifier(String word) {
        switch (word) {
            case "var":    return TokenType.VAR;
            case "if":     return TokenType.IF;
            case "else":   return TokenType.ELSE;
            case "while":  return TokenType.WHILE;
            case "for":    return TokenType.FOR;
            case "func":   return TokenType.FUNC;
            case "return": return TokenType.RETURN;
            case "print":  return TokenType.PRINT;
            case "true":   return TokenType.TRUE;
            case "false":  return TokenType.FALSE;
            case "null":   return TokenType.NULL;
            case "and":    return TokenType.AND;
            case "or":     return TokenType.OR;
            case "not":    return TokenType.NOT;
            default:       return TokenType.IDENTIFIER;
        }
    }

    private void readString() {
        int startCol = column;
        advance(); // skip opening "
        StringBuilder sb = new StringBuilder();
        while (pos < source.length() && current() != '"') {
            if (current() == '\\' && peek(1) == '"') {
                sb.append('"');
                advance();
                advance();
            } else {
                sb.append(current());
                advance();
            }
        }
        advance(); // skip closing "
        tokens.add(new Token(TokenType.STRING, sb.toString(), line, startCol));
    }

    private void readSymbol() {
        int startCol = column;
        char c = current();
        advance();

        switch (c) {
            case '+': tokens.add(new Token(TokenType.PLUS,          "+", line, startCol)); break;
            case '-': tokens.add(new Token(TokenType.MINUS,         "-", line, startCol)); break;
            case '*': tokens.add(new Token(TokenType.MULTIPLY,      "*", line, startCol)); break;
            case '/': tokens.add(new Token(TokenType.DIVIDE,        "/", line, startCol)); break;
            case '%': tokens.add(new Token(TokenType.MODULO,        "%", line, startCol)); break;
            case ';': tokens.add(new Token(TokenType.SEMICOLON,     ";", line, startCol)); break;
            case ',': tokens.add(new Token(TokenType.COMMA,         ",", line, startCol)); break;
            case '.': tokens.add(new Token(TokenType.DOT,           ".", line, startCol)); break;
            case '(': tokens.add(new Token(TokenType.LEFT_PAREN,    "(", line, startCol)); break;
            case ')': tokens.add(new Token(TokenType.RIGHT_PAREN,   ")", line, startCol)); break;
            case '{': tokens.add(new Token(TokenType.LEFT_BRACE,    "{", line, startCol)); break;
            case '}': tokens.add(new Token(TokenType.RIGHT_BRACE,   "}", line, startCol)); break;
            case '[': tokens.add(new Token(TokenType.LEFT_BRACKET,  "[", line, startCol)); break;
            case ']': tokens.add(new Token(TokenType.RIGHT_BRACKET, "]", line, startCol)); break;
            case '=':
                if (peek(0) == '=') {
                    advance();
                    tokens.add(new Token(TokenType.EQUALS, "==", line, startCol));
                } else {
                    tokens.add(new Token(TokenType.ASSIGN, "=", line, startCol));
                }
                break;
            case '!':
                if (peek(0) == '=') {
                    advance();
                    tokens.add(new Token(TokenType.NOT_EQUALS, "!=", line, startCol));
                } else {
                    tokens.add(new Token(TokenType.NOT, "!", line, startCol));
                }
                break;
            case '<':
                if (peek(0) == '=') {
                    advance();
                    tokens.add(new Token(TokenType.LESS_EQUAL, "<=", line, startCol));
                } else {
                    tokens.add(new Token(TokenType.LESS_THAN, "<", line, startCol));
                }
                break;
            case '>':
                if (peek(0) == '=') {
                    advance();
                    tokens.add(new Token(TokenType.GREATER_EQUAL, ">=", line, startCol));
                } else {
                    tokens.add(new Token(TokenType.GREATER_THAN, ">", line, startCol));
                }
                break;
            default:
                tokens.add(new Token(TokenType.UNKNOWN, String.valueOf(c), line, startCol));
        }
    }
}