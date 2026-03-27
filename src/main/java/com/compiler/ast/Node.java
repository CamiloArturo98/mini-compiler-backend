package com.compiler.ast;

import java.util.List;

public abstract class Node {

    public abstract String getType();

    // ─── Programa ───────────────────────────────────────────
    public static class Program extends Node {
        public final List<Node> statements;
        public Program(List<Node> statements) {
            this.statements = statements;
        }
        @Override public String getType() { return "Program"; }
    }

    // ─── Declaración de variable ────────────────────────────
    public static class VarDeclaration extends Node {
        public final String name;
        public final Node value;
        public VarDeclaration(String name, Node value) {
            this.name = name;
            this.value = value;
        }
        @Override public String getType() { return "VarDeclaration"; }
    }

    // ─── Asignación ─────────────────────────────────────────
    public static class Assignment extends Node {
        public final String name;
        public final Node value;
        public Assignment(String name, Node value) {
            this.name = name;
            this.value = value;
        }
        @Override public String getType() { return "Assignment"; }
    }

    // ─── If / Else ──────────────────────────────────────────
    public static class IfStatement extends Node {
        public final Node condition;
        public final Node thenBranch;
        public final Node elseBranch;
        public IfStatement(Node condition, Node thenBranch, Node elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }
        @Override public String getType() { return "IfStatement"; }
    }

    // ─── While ──────────────────────────────────────────────
    public static class WhileStatement extends Node {
        public final Node condition;
        public final Node body;
        public WhileStatement(Node condition, Node body) {
            this.condition = condition;
            this.body = body;
        }
        @Override public String getType() { return "WhileStatement"; }
    }

    // ─── Bloque de sentencias ───────────────────────────────
    public static class Block extends Node {
        public final List<Node> statements;
        public Block(List<Node> statements) {
            this.statements = statements;
        }
        @Override public String getType() { return "Block"; }
    }

    // ─── Función ────────────────────────────────────────────
    public static class FuncDeclaration extends Node {
        public final String name;
        public final List<String> params;
        public final Node body;
        public FuncDeclaration(String name, List<String> params, Node body) {
            this.name = name;
            this.params = params;
            this.body = body;
        }
        @Override public String getType() { return "FuncDeclaration"; }
    }

    // ─── Return ─────────────────────────────────────────────
    public static class ReturnStatement extends Node {
        public final Node value;
        public ReturnStatement(Node value) {
            this.value = value;
        }
        @Override public String getType() { return "ReturnStatement"; }
    }

    // ─── Print ──────────────────────────────────────────────
    public static class PrintStatement extends Node {
        public final Node value;
        public PrintStatement(Node value) {
            this.value = value;
        }
        @Override public String getType() { return "PrintStatement"; }
    }

    // ─── Llamada a función ──────────────────────────────────
    public static class FuncCall extends Node {
        public final String name;
        public final List<Node> args;
        public FuncCall(String name, List<Node> args) {
            this.name = name;
            this.args = args;
        }
        @Override public String getType() { return "FuncCall"; }
    }

    // ─── Operación binaria ──────────────────────────────────
    public static class BinaryOp extends Node {
        public final Node left;
        public final String operator;
        public final Node right;
        public BinaryOp(Node left, String operator, Node right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }
        @Override public String getType() { return "BinaryOp"; }
    }

    // ─── Operación unaria ───────────────────────────────────
    public static class UnaryOp extends Node {
        public final String operator;
        public final Node operand;
        public UnaryOp(String operator, Node operand) {
            this.operator = operator;
            this.operand = operand;
        }
        @Override public String getType() { return "UnaryOp"; }
    }

    // ─── Número literal ─────────────────────────────────────
    public static class NumberLiteral extends Node {
        public final double value;
        public NumberLiteral(double value) {
            this.value = value;
        }
        @Override public String getType() { return "NumberLiteral"; }
    }

    // ─── String literal ─────────────────────────────────────
    public static class StringLiteral extends Node {
        public final String value;
        public StringLiteral(String value) {
            this.value = value;
        }
        @Override public String getType() { return "StringLiteral"; }
    }

    // ─── Boolean literal ────────────────────────────────────
    public static class BooleanLiteral extends Node {
        public final boolean value;
        public BooleanLiteral(boolean value) {
            this.value = value;
        }
        @Override public String getType() { return "BooleanLiteral"; }
    }

    // ─── Null literal ───────────────────────────────────────
    public static class NullLiteral extends Node {
        @Override public String getType() { return "NullLiteral"; }
    }

    // ─── Identificador (variable) ───────────────────────────
    public static class Identifier extends Node {
        public final String name;
        public Identifier(String name) {
            this.name = name;
        }
        @Override public String getType() { return "Identifier"; }
    }
}