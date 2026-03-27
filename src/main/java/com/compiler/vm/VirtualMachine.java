package com.compiler.vm;

import com.compiler.ast.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VirtualMachine {

    // ─── Entorno de ejecución ───────────────────────────────
    private static class Environment {
        private final Map<String, VMValue> variables = new HashMap<>();
        private final Environment parent;

        Environment(Environment parent) {
            this.parent = parent;
        }

        void define(String name, VMValue value) {
            variables.put(name, value);
        }

        VMValue get(String name) {
            if (variables.containsKey(name)) return variables.get(name);
            if (parent != null) return parent.get(name);
            throw new RuntimeException("Variable no definida: " + name);
        }

        void set(String name, VMValue value) {
            if (variables.containsKey(name)) {
                variables.put(name, value);
                return;
            }
            if (parent != null) {
                parent.set(name, value);
                return;
            }
            throw new RuntimeException("Variable no definida: " + name);
        }
    }

    // ─── Señal de retorno ───────────────────────────────────
    private static class ReturnSignal extends RuntimeException {
        final VMValue value;
        ReturnSignal(VMValue value) {
            super(null, null, true, false);
            this.value = value;
        }
    }

    // ─── Función almacenada ─────────────────────────────────
    private record VMFunction(List<String> params, Node body, Environment closure) {}

    // ─── Estado de la VM ────────────────────────────────────
    private final Environment globalEnv;
    private final Map<String, VMFunction> functions;
    private final List<String> outputLog;

    public VirtualMachine() {
        this.globalEnv  = new Environment(null);
        this.functions  = new HashMap<>();
        this.outputLog  = new ArrayList<>();
    }

    // ─── Entry point ────────────────────────────────────────
    public void execute(Node.Program program) {
        System.out.println("\n=== EJECUTANDO EN LA VM ===");
        for (Node statement : program.statements) {
            executeNode(statement, globalEnv);
        }
        System.out.println("=== FIN DE EJECUCIÓN ===");
        System.out.println("[VM] Líneas de output: " + outputLog.size());
    }

    // ─── Ejecutar nodo ──────────────────────────────────────
    private VMValue executeNode(Node node, Environment env) {

        if (node instanceof Node.Program p) {
            VMValue last = VMValue.ofNull();
            for (Node s : p.statements) last = executeNode(s, env);
            return last;
        }

        if (node instanceof Node.Block b) {
            Environment blockEnv = new Environment(env);
            VMValue last = VMValue.ofNull();
            for (Node s : b.statements) last = executeNode(s, blockEnv);
            return last;
        }

        if (node instanceof Node.VarDeclaration v) {
            VMValue value = executeNode(v.value, env);
            env.define(v.name, value);
            return value;
        }

        if (node instanceof Node.Assignment a) {
            VMValue value = executeNode(a.value, env);
            env.set(a.name, value);
            return value;
        }

        if (node instanceof Node.FuncDeclaration f) {
            functions.put(f.name, new VMFunction(f.params, f.body, env));
            return VMValue.ofNull();
        }

        if (node instanceof Node.ReturnStatement r) {
            throw new ReturnSignal(executeNode(r.value, env));
        }

        if (node instanceof Node.PrintStatement p) {
            VMValue value = executeNode(p.value, env);
            String output = value.toString();
            outputLog.add(output);
            System.out.println("[OUTPUT] " + output);
            return value;
        }

        if (node instanceof Node.IfStatement i) {
            VMValue condition = executeNode(i.condition, env);
            if (condition.isTruthy()) {
                return executeNode(i.thenBranch, env);
            } else if (i.elseBranch != null) {
                return executeNode(i.elseBranch, env);
            }
            return VMValue.ofNull();
        }

        if (node instanceof Node.WhileStatement w) {
            int iterations = 0;
            while (executeNode(w.condition, env).isTruthy()) {
                if (++iterations > 100_000) {
                    throw new RuntimeException("Infinite loop detected after 100,000 iterations");
                }
                executeNode(w.body, env);
            }
            return VMValue.ofNull();
        }

        if (node instanceof Node.FuncCall fc) {
            return executeCall(fc.name, fc.args, env);
        }

        if (node instanceof Node.BinaryOp b) {
            return executeBinaryOp(b, env);
        }

        if (node instanceof Node.UnaryOp u) {
            VMValue operand = executeNode(u.operand, env);
            return switch (u.operator) {
                case "-"   -> VMValue.ofNumber(-operand.asNumber());
                case "!", "not" -> VMValue.ofBoolean(!operand.isTruthy());
                default -> throw new RuntimeException("Unknown unary op: " + u.operator);
            };
        }

        if (node instanceof Node.NumberLiteral n)  return VMValue.ofNumber(n.value);
        if (node instanceof Node.StringLiteral s)  return VMValue.ofString(s.value);
        if (node instanceof Node.BooleanLiteral bl) return VMValue.ofBoolean(bl.value);
        if (node instanceof Node.NullLiteral)       return VMValue.ofNull();

        if (node instanceof Node.Identifier id) {
            return env.get(id.name);
        }

        throw new RuntimeException("Unknown node type: " + node.getType());
    }

    // ─── Llamada a función ──────────────────────────────────
    private VMValue executeCall(String name, List<Node> argNodes, Environment env) {
        List<VMValue> args = new ArrayList<>();
        for (Node arg : argNodes) args.add(executeNode(arg, env));

        if (!functions.containsKey(name)) {
            throw new RuntimeException("Función no definida: " + name);
        }

        VMFunction func = functions.get(name);

        if (args.size() != func.params().size()) {
            throw new RuntimeException("Función '" + name + "' espera " +
                func.params().size() + " argumentos pero recibió " + args.size());
        }

        Environment funcEnv = new Environment(func.closure());
        for (int i = 0; i < func.params().size(); i++) {
            funcEnv.define(func.params().get(i), args.get(i));
        }

        try {
            executeNode(func.body(), funcEnv);
            return VMValue.ofNull();
        } catch (ReturnSignal rs) {
            return rs.value;
        }
    }

    // ─── Operaciones binarias ───────────────────────────────
    private VMValue executeBinaryOp(Node.BinaryOp b, Environment env) {
        VMValue left  = executeNode(b.left,  env);
        VMValue right = executeNode(b.right, env);

        return switch (b.operator) {
            case "+"  -> {
                if (left.getType() == VMValue.Type.STRING ||
                    right.getType() == VMValue.Type.STRING) {
                    yield VMValue.ofString(left.asString() + right.asString());
                }
                yield VMValue.ofNumber(left.asNumber() + right.asNumber());
            }
            case "-"  -> VMValue.ofNumber(left.asNumber() - right.asNumber());
            case "*"  -> VMValue.ofNumber(left.asNumber() * right.asNumber());
            case "/"  -> {
                if (right.asNumber() == 0) throw new RuntimeException("Division by zero");
                yield VMValue.ofNumber(left.asNumber() / right.asNumber());
            }
            case "%"  -> VMValue.ofNumber(left.asNumber() % right.asNumber());
            case "==" -> VMValue.ofBoolean(left.equals(right));
            case "!=" -> VMValue.ofBoolean(!left.equals(right));
            case "<"  -> VMValue.ofBoolean(left.asNumber() <  right.asNumber());
            case ">"  -> VMValue.ofBoolean(left.asNumber() >  right.asNumber());
            case "<=" -> VMValue.ofBoolean(left.asNumber() <= right.asNumber());
            case ">=" -> VMValue.ofBoolean(left.asNumber() >= right.asNumber());
            case "and" -> VMValue.ofBoolean(left.isTruthy() && right.isTruthy());
            case "or"  -> VMValue.ofBoolean(left.isTruthy() || right.isTruthy());
            default -> throw new RuntimeException("Unknown operator: " + b.operator);
        };
    }

    public List<String> getOutputLog() {
        return outputLog;
    }
}