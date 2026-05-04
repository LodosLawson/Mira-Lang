package com.actmira;

import com.actmira.evaluator.Engine;

public class Debug {
    public static void main(String[] args) {
        String code = "// 3. Object Oriented Programming (Classes)\n" +
                "class DatabaseConnection {\n" +
                "    fn connect(url) {\n" +
                "        return true;\n" +
                "    }\n" +
                "    \n" +
                "    fn disconnect() {\n" +
                "        return false;\n" +
                "    }\n" +
                "}\n\n" +
                "let db = new DatabaseConnection();\n" +
                "let status = db.connect(\"localhost\");\n" +
                "print(status);\n";
        System.out.println("Executing:\n" + code);
        System.out.println("\nResult:");
        System.out.println(Engine.execute(code));
    }
}
