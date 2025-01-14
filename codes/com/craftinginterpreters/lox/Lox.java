package com.craftinginterpreters.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.craftinginterpreters.lox.InterPreter.Interpreter;

public class Lox {
    // 7.4.2~
    private static final Interpreter interpreter = new Interpreter();
    static boolean hadError = false;
    static boolean hadRuntimeError = false;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        // 終了コードによって、エラーを示す
        if (hadError)
            System.exit(65);
        if (hadRuntimeError)
            System.exit(70);
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);
        for (;;) {
            System.out.print(">");
            String line = reader.readLine();
            if (line == null)
                break;
            run(line);
            hadError = false;
        }
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source); // Scanner のクラスが存在することを確認
        List<Token> tokens = scanner.scanTokens(); // Token のクラスが存在することを確認

        Parser parser = new Parser(tokens);

        List<Stmt> statements = parser.parse();
        /*
         * ↑ 置き換え前コード
         * Expr expression = parser.parse();
         */

        // Stop if there was a syntax error.
        if (hadError)
            interpreter.interpret(statements);
        /*
         * ↑ 置き換え前コード
         * return;
         */

        interpreter.interpret(expression);
        System.out.println(new AstPrinter().print(expression));

        // トークンの印字
        for (Token token : tokens) {
            System.out.println(token);
        }

        System.out.println(source);

    }

    static void error(int line, String message) {
        report(line, "", message);
    }

    // 7.4.1~
    static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() +
                "\n[line " + error.token.line + "]");
        hadRuntimeError = true;
    }

    private static void report(int line, String where, String message) {
        System.err.println("[Line" + line + "]Error" + where + ": " + message);
        hadError = true;
    }

    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }
}
