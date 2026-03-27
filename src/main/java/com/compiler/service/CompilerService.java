package com.compiler.service;

import com.compiler.ai.AIAnalyzer;
import com.compiler.lexer.Lexer;
import com.compiler.lexer.Token;
import com.compiler.optimizer.Optimizer;
import com.compiler.parser.Parser;
import com.compiler.ast.Node;
import com.compiler.vm.VirtualMachine;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompilerService {

    public CompilerResult compile(String code) {
        CompilerResult result = new CompilerResult();
        long startTime = System.currentTimeMillis();

        try {
            // 1. Lexer
            Lexer lexer = new Lexer(code);
            List<Token> tokens = lexer.tokenize();
            result.setTokenCount(tokens.size());

            // 2. Parser
            Parser parser = new Parser(tokens);
            Node.Program program = parser.parse();

            // 3. AI Analyzer
            AIAnalyzer ai = new AIAnalyzer();
            AIAnalyzer.AnalysisReport report = ai.analyze(program);
            result.setAnalysis(report);

            // 4. Optimizer
            Optimizer optimizer = new Optimizer();
            Node.Program optimized = optimizer.optimize(program);
            result.setOptimizationCount(optimizer.getOptimizationCount());

            // 5. VM
            VirtualMachine vm = new VirtualMachine();
            vm.execute(optimized);
            result.setOutput(vm.getOutputLog());

            result.setSuccess(true);
            result.setMessage("Compilación exitosa");

        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("Error: " + e.getMessage());
        }

        result.setExecutionTime(System.currentTimeMillis() - startTime);
        return result;
    }

    // ─── Clase resultado ────────────────────────────────────
    public static class CompilerResult {
        private boolean success;
        private String message;
        private int tokenCount;
        private int optimizationCount;
        private long executionTime;
        private List<String> output;
        private AIAnalyzer.AnalysisReport analysis;

        // Getters y Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public int getTokenCount() { return tokenCount; }
        public void setTokenCount(int tokenCount) { this.tokenCount = tokenCount; }

        public int getOptimizationCount() { return optimizationCount; }
        public void setOptimizationCount(int count) { this.optimizationCount = count; }

        public long getExecutionTime() { return executionTime; }
        public void setExecutionTime(long executionTime) { this.executionTime = executionTime; }

        public List<String> getOutput() { return output; }
        public void setOutput(List<String> output) { this.output = output; }

        public AIAnalyzer.AnalysisReport getAnalysis() { return analysis; }
        public void setAnalysis(AIAnalyzer.AnalysisReport analysis) { this.analysis = analysis; }
    }
}