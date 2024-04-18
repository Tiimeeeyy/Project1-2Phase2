package engine.parser;

import java.util.HashMap;

import engine.parser.interfaces.IParser;

public class Main {
    public static void main(String[] args) {
        HashMap<String, Double> variables = new HashMap<>();
        variables.put("x", 10.0);
        variables.put("y", 5.0);

        String expression1 = "3 + 4 * 2";
        String expression2 = "(1 + 2) * (x - y)";
        String expression3 = "cos(0) + sin(90)";
        String expression4 = "x^y - 100";

        IParser parser1 = new ExpressionParser(expression1, variables);
        IParser parser2 = new ExpressionParser(expression2, variables);
        IParser parser3 = new ExpressionParser(expression3, variables);
        IParser parser4 = new ExpressionParser(expression4, variables);

        System.out.println(expression1 + " = " + parser1.evaluate());
        System.out.println(expression2 + " = " + parser2.evaluate());
        System.out.println(expression3 + " = " + parser3.evaluate());
        System.out.println(expression4 + " = " + parser4.evaluate());
    }
}
