package com.compiler.lexer;

public enum TokenType {
    // Literales
    NUMBER,
    STRING,
    BOOLEAN,

    // Identificadores y palabras clave
    IDENTIFIER,
    VAR,
    IF,
    ELSE,
    WHILE,
    FOR,
    FUNC,
    RETURN,
    PRINT,
    TRUE,
    FALSE,
    NULL,

    // Operadores aritméticos
    PLUS,
    MINUS,
    MULTIPLY,
    DIVIDE,
    MODULO,

    // Operadores de comparación
    EQUALS,
    NOT_EQUALS,
    LESS_THAN,
    GREATER_THAN,
    LESS_EQUAL,
    GREATER_EQUAL,

    // Operadores lógicos
    AND,
    OR,
    NOT,

    // Asignación
    ASSIGN,

    // Delimitadores
    LEFT_PAREN,
    RIGHT_PAREN,
    LEFT_BRACE,
    RIGHT_BRACE,
    LEFT_BRACKET,
    RIGHT_BRACKET,
    SEMICOLON,
    COMMA,
    DOT,

    // Especiales
    EOF,
    UNKNOWN
}