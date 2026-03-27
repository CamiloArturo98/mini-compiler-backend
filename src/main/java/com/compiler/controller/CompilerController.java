package com.compiler.controller;

import com.compiler.service.CompilerService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/compiler")
@CrossOrigin(origins = {
    "http://localhost:4200",
    "https://mini-compiler-front.vercel.app"
})
public class CompilerController {

    private final CompilerService compilerService;

    public CompilerController(CompilerService compilerService) {
        this.compilerService = compilerService;
    }

    @PostMapping("/run")
    public CompilerService.CompilerResult run(@RequestBody CodeRequest request) {
        return compilerService.compile(request.getCode());
    }

    @GetMapping("/health")
    public String health() {
        return "OK";
    }

    @GetMapping("/examples")
    public String[] examples() {
        return new String[]{
            """
            var x = 10 + 5;
            print(x);
            """,
            """
            func factorial(n) {
                if (n <= 1) {
                    return 1;
                }
                return n * factorial(n - 1);
            }
            print(factorial(5));
            """,
            """
            var i = 1;
            while (i <= 5) {
                print(i);
                i = i + 1;
            }
            """,
            """
            func fibonacci(n) {
                if (n <= 1) {
                    return n;
                }
                return fibonacci(n - 1) + fibonacci(n - 2);
            }
            var i = 0;
            while (i <= 7) {
                print(fibonacci(i));
                i = i + 1;
            }
            """
        };
    }

    // ─── Request body ───────────────────────────────────────
    public static class CodeRequest {
        private String code;
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
    }
}