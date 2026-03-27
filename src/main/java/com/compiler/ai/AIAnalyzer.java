package com.compiler.ai;

import com.compiler.ast.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AIAnalyzer {

    // ─── Resultado del análisis ─────────────────────────────
    public static class AnalysisReport {
        public final List<String> errors        = new ArrayList<>();
        public final List<String> warnings      = new ArrayList<>();
        public final List<String> suggestions   = new ArrayList<>();
        public final List<String> optimizations = new ArrayList<>();
        public int complexityScore = 0;
        public String complexityLevel = "SIMPLE";

        public void print() {
            System.out.println("\n╔══════════════════════════════════════╗");
            System.out.println("║         AI ANALYZER REPORT           ║");
            System.out.println("╚══════════════════════════════════════╝");

            System.out.println("\n📊 Complejidad: " + complexityLevel +
                               " (score: " + complexityScore + ")");

            if (!errors.isEmpty()) {
                System.out.println("\n❌ ERRORES (" + errors.size() + "):");
                errors.forEach(e -> System.out.println("   • " + e));
            }

            if (!warnings.isEmpty()) {
                System.out.println("\n⚠️  ADVERTENCIAS (" + warnings.size() + "):");
                warnings.forEach(w -> System.out.println("   • " + w));
            }

            if (!optimizations.isEmpty()) {
                System.out.println("\n⚡ OPTIMIZACIONES SUGERIDAS (" + optimizations.size() + "):");
                optimizations.forEach(o -> System.out.println("   • " + o));
            }

            if (!suggestions.isEmpty()) {
                System.out.println("\n💡 SUGERENCIAS (" + suggestions.size() + "):");
                suggestions.forEach(s -> System.out.println("   • " + s));
            }

            if (errors.isEmpty() && warnings.isEmpty()) {
                System.out.println("\n✅ Código limpio, sin problemas detectados.");
            }

            System.out.println("\n══════════════════════════════════════");
        }
    }

    // ─── Estado interno del análisis ───────────────────────
    private final Map<String, Integer> variableUsage   = new HashMap<>();
    private final Map<String, Integer> functionUsage   = new HashMap<>();
    private final List<String>         declaredVars    = new ArrayList<>();
    private final List<String>         declaredFuncs   = new ArrayList<>();
    private int nestingDepth   = 0;
    private int maxNesting     = 0;
    private int loopCount      = 0;
    private int functionCount  = 0;
    private int recursionRisk  = 0;

    // ─── Entry point ────────────────────────────────────────
    public AnalysisReport analyze(Node.Program program) {
        AnalysisReport report = new AnalysisReport();

        // Primera pasada: recolectar declaraciones
        collectDeclarations(program);

        // Segunda pasada: analizar uso
        for (Node statement : program.statements) {
            analyzeNode(statement, report, null);
        }

        // Detectar variables no usadas
        for (String var : declaredVars) {
            int usage = variableUsage.getOrDefault(var, 0);
            if (usage == 0) {
                report.warnings.add("Variable '" + var + "' declarada pero nunca usada");
            }
        }

        // Detectar funciones no usadas
        for (String func : declaredFuncs) {
            int usage = functionUsage.getOrDefault(func, 0);
            if (usage == 0) {
                report.warnings.add("Función '" + func + "' declarada pero nunca llamada");
            }
        }

        // Calcular complejidad
        calculateComplexity(report);

        // Sugerencias generales
        generateSuggestions(report);

        return report;
    }

    // ─── Recolectar declaraciones ───────────────────────────
    private void collectDeclarations(Node.Program program) {
        for (Node statement : program.statements) {
            if (statement instanceof Node.VarDeclaration v) {
                declaredVars.add(v.name);
            }
            if (statement instanceof Node.FuncDeclaration f) {
                declaredFuncs.add(f.name);
            }
        }
    }

    // ─── Analizar nodo recursivamente ───────────────────────
    private void analyzeNode(Node node, AnalysisReport report, String currentFunc) {

        if (node == null) return;

        if (node instanceof Node.Program p) {
            for (Node s : p.statements) analyzeNode(s, report, currentFunc);

        } else if (node instanceof Node.Block b) {
            nestingDepth++;
            maxNesting = Math.max(maxNesting, nestingDepth);
            for (Node s : b.statements) analyzeNode(s, report, currentFunc);
            nestingDepth--;

        } else if (node instanceof Node.VarDeclaration v) {
            analyzeNode(v.value, report, currentFunc);

        } else if (node instanceof Node.Assignment a) {
            if (!declaredVars.contains(a.name)) {
                report.errors.add("Asignación a variable no declarada: '" + a.name + "'");
            }
            analyzeNode(a.value, report, currentFunc);

        } else if (node instanceof Node.FuncDeclaration f) {
            functionCount++;
            // Registrar parámetros como variables válidas
            List<String> savedDeclared = new ArrayList<>(declaredVars);
            declaredVars.addAll(f.params);
            analyzeNode(f.body, report, f.name);
            // Restaurar variables después de analizar la función
            declaredVars.clear();
            declaredVars.addAll(savedDeclared);

        } else if (node instanceof Node.FuncCall fc) {
            // Contar uso de función
            functionUsage.merge(fc.name, 1, Integer::sum);

            // Detectar recursión
            if (fc.name.equals(currentFunc)) {
                recursionRisk++;
                report.suggestions.add("Función '" + fc.name +
                    "' es recursiva. Considera usar iteración para mejor rendimiento.");
            }

            // Detectar función no declarada
            if (!declaredFuncs.contains(fc.name)) {
                report.errors.add("Llamada a función no declarada: '" + fc.name + "'");
            }

            for (Node arg : fc.args) analyzeNode(arg, report, currentFunc);

        } else if (node instanceof Node.Identifier id) {
            // Contar uso de variable
            variableUsage.merge(id.name, 1, Integer::sum);

            // Detectar variable no declarada
            if (!declaredVars.contains(id.name) && !declaredFuncs.contains(id.name)) {
                report.warnings.add("Posible variable no declarada: '" + id.name + "'");
            }

        } else if (node instanceof Node.IfStatement i) {
            analyzeNode(i.condition, report, currentFunc);
            analyzeNode(i.thenBranch, report, currentFunc);
            if (i.elseBranch != null) analyzeNode(i.elseBranch, report, currentFunc);

        } else if (node instanceof Node.WhileStatement w) {
            loopCount++;
            analyzeNode(w.condition, report, currentFunc);
            analyzeNode(w.body, report, currentFunc);

        } else if (node instanceof Node.BinaryOp b) {
            // Detectar division por cero
            if (b.operator.equals("/") && b.right instanceof Node.NumberLiteral n && n.value == 0) {
                report.errors.add("División por cero detectada");
            }
            // Detectar comparacion con null sin verificación
            if ((b.operator.equals("==") || b.operator.equals("!=")) &&
                (b.right instanceof Node.NullLiteral || b.left instanceof Node.NullLiteral)) {
                report.suggestions.add("Comparación con null detectada. Asegúrate de manejar valores nulos.");
            }
            analyzeNode(b.left,  report, currentFunc);
            analyzeNode(b.right, report, currentFunc);

        } else if (node instanceof Node.UnaryOp u) {
            analyzeNode(u.operand, report, currentFunc);

        } else if (node instanceof Node.ReturnStatement r) {
            analyzeNode(r.value, report, currentFunc);

        } else if (node instanceof Node.PrintStatement p) {
            analyzeNode(p.value, report, currentFunc);
        }
    }

    // ─── Calcular complejidad ───────────────────────────────
    private void calculateComplexity(AnalysisReport report) {
        report.complexityScore =
            (functionCount * 3) +
            (loopCount     * 2) +
            (maxNesting    * 2) +
            (recursionRisk * 4) +
            declaredVars.size();

        if      (report.complexityScore <= 5)  report.complexityLevel = "SIMPLE";
        else if (report.complexityScore <= 15) report.complexityLevel = "MODERADO";
        else if (report.complexityScore <= 30) report.complexityLevel = "COMPLEJO";
        else                                   report.complexityLevel = "MUY COMPLEJO";

        if (maxNesting >= 4) {
            report.warnings.add("Anidamiento profundo detectado (nivel " + maxNesting +
                "). Considera refactorizar.");
        }
        if (loopCount >= 3) {
            report.optimizations.add("Múltiples bucles detectados (" + loopCount +
                "). Evalúa si pueden combinarse.");
        }
    }

    // ─── Sugerencias generales ──────────────────────────────
    private void generateSuggestions(AnalysisReport report) {
        if (functionCount == 0 && declaredVars.size() > 5) {
            report.suggestions.add("Código largo sin funciones. Considera organizar en funciones.");
        }
        if (report.errors.isEmpty() && report.warnings.isEmpty()) {
            report.suggestions.add("¡Excelente código! Sigue las buenas prácticas.");
        }
        if (recursionRisk > 0) {
            report.optimizations.add("Funciones recursivas detectadas. " +
                "Considera memoización para optimizar.");
        }
    }
}