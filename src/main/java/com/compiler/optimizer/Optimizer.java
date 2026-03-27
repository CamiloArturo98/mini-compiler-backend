package com.compiler.optimizer;

import com.compiler.ast.Node;
import java.util.ArrayList;
import java.util.List;

public class Optimizer {

    private int optimizationCount = 0;

    public Node.Program optimize(Node.Program program) {
        optimizationCount = 0;
        List<Node> optimized = new ArrayList<>();
        for (Node statement : program.statements) {
            optimized.add(optimizeNode(statement));
        }
        System.out.println("[Optimizer] Total optimizaciones aplicadas: " + optimizationCount);
        return new Node.Program(optimized);
    }

    private Node optimizeNode(Node node) {

        if (node instanceof Node.VarDeclaration v) {
            return new Node.VarDeclaration(v.name, optimizeNode(v.value));
        }

        if (node instanceof Node.Assignment a) {
            return new Node.Assignment(a.name, optimizeNode(a.value));
        }

        if (node instanceof Node.BinaryOp b) {
            Node left  = optimizeNode(b.left);
            Node right = optimizeNode(b.right);

            // Constant folding: ambos son números
            if (left instanceof Node.NumberLiteral l && right instanceof Node.NumberLiteral r) {
                double result = switch (b.operator) {
                    case "+"  -> l.value + r.value;
                    case "-"  -> l.value - r.value;
                    case "*"  -> l.value * r.value;
                    case "/"  -> r.value != 0 ? l.value / r.value : Double.NaN;
                    case "%"  -> l.value % r.value;
                    default   -> Double.NaN;
                };
                if (!Double.isNaN(result)) {
                    optimizationCount++;
                    System.out.println("[Optimizer] Constant folding: " +
                        l.value + " " + b.operator + " " + r.value + " = " + result);
                    return new Node.NumberLiteral(result);
                }
            }

            // String concatenation folding
            if (left instanceof Node.StringLiteral sl &&
                right instanceof Node.StringLiteral sr &&
                b.operator.equals("+")) {
                optimizationCount++;
                System.out.println("[Optimizer] String folding: '" + sl.value + "' + '" + sr.value + "'");
                return new Node.StringLiteral(sl.value + sr.value);
            }

            // Multiplicar por 0 = 0
            if (b.operator.equals("*")) {
                if (isZero(left) || isZero(right)) {
                    optimizationCount++;
                    System.out.println("[Optimizer] Multiply by zero eliminated");
                    return new Node.NumberLiteral(0);
                }
            }

            // Sumar o restar 0
            if (b.operator.equals("+") || b.operator.equals("-")) {
                if (isZero(right)) {
                    optimizationCount++;
                    System.out.println("[Optimizer] Add/subtract zero eliminated");
                    return left;
                }
            }

            return new Node.BinaryOp(left, b.operator, right);
        }

        if (node instanceof Node.UnaryOp u) {
            Node operand = optimizeNode(u.operand);
            if (u.operator.equals("-") && operand instanceof Node.NumberLiteral n) {
                optimizationCount++;
                return new Node.NumberLiteral(-n.value);
            }
            return new Node.UnaryOp(u.operator, operand);
        }

        if (node instanceof Node.IfStatement i) {
            Node condition = optimizeNode(i.condition);

            // Dead code elimination: if(true) o if(false)
            if (condition instanceof Node.BooleanLiteral bl) {
                optimizationCount++;
                System.out.println("[Optimizer] Dead code elimination: if(" + bl.value + ")");
                if (bl.value) return optimizeNode(i.thenBranch);
                if (i.elseBranch != null) return optimizeNode(i.elseBranch);
                return new Node.Block(new ArrayList<>());
            }

            Node thenBranch = optimizeNode(i.thenBranch);
            Node elseBranch = i.elseBranch != null ? optimizeNode(i.elseBranch) : null;
            return new Node.IfStatement(condition, thenBranch, elseBranch);
        }

        if (node instanceof Node.WhileStatement w) {
            Node condition = optimizeNode(w.condition);
            if (condition instanceof Node.BooleanLiteral bl && !bl.value) {
                optimizationCount++;
                System.out.println("[Optimizer] Dead loop eliminated: while(false)");
                return new Node.Block(new ArrayList<>());
            }
            return new Node.WhileStatement(condition, optimizeNode(w.body));
        }

        if (node instanceof Node.Block b) {
            List<Node> optimized = new ArrayList<>();
            for (Node s : b.statements) optimized.add(optimizeNode(s));
            return new Node.Block(optimized);
        }

        if (node instanceof Node.FuncDeclaration f) {
            return new Node.FuncDeclaration(f.name, f.params, optimizeNode(f.body));
        }

        if (node instanceof Node.ReturnStatement r) {
            return new Node.ReturnStatement(optimizeNode(r.value));
        }

        if (node instanceof Node.PrintStatement p) {
            return new Node.PrintStatement(optimizeNode(p.value));
        }

        if (node instanceof Node.FuncCall fc) {
            List<Node> optimizedArgs = new ArrayList<>();
            for (Node arg : fc.args) optimizedArgs.add(optimizeNode(arg));
            return new Node.FuncCall(fc.name, optimizedArgs);
        }

        // Literales e identificadores no se optimizan
        return node;
    }

    private boolean isZero(Node node) {
        return node instanceof Node.NumberLiteral n && n.value == 0;
    }

    public int getOptimizationCount() {
        return optimizationCount;
    }
}