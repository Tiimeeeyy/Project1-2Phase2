package engine.parser;

import java.util.Map;
import engine.parser.interfaces.IParser;

public class ExpressionParser implements IParser {

    private String funcExpression;
    private int position = 0;
    private Variables variables;

    public ExpressionParser(String expression, Map<String, Double> initVarsHM) {
        this.funcExpression = expression.replaceAll("\\s+", "");
        this.variables = new Variables(initVarsHM);
    }

    public double evaluate() {
        double finalResult = processExpression();
        if (position < funcExpression.length()) {
            System.out.println("Unexpected character: " + funcExpression.charAt(position));
            throw new Error("Unexpected character at position: " + position);
        }
        return finalResult;
    }

    private double processExpression() {
        return processAdditionSubtraction();
    }

    private double processAdditionSubtraction() {
        double result = processMultiplicationDivision();
        while (position < funcExpression.length()) {
            char operator = funcExpression.charAt(position);
            if (operator == '+' || operator == '-') {
                position++;
                double right = processMultiplicationDivision();
                result = (operator == '+') ? result + right : result - right;
            } else {
                break;
            }
        }
        return result;
    }

    private double processMultiplicationDivision() {
        double result = processExponent();
        while (position < funcExpression.length()) {
            char operator = funcExpression.charAt(position);
            if (operator == '*' || operator == '/') {
                position++;
                double right = processExponent();
                result = (operator == '*') ? result * right : result / right;
            } else {
                break;
            }
        }
        return result;
    }

    private double processExponent() {
        double base = processParentheses();
        while (position < funcExpression.length() && funcExpression.charAt(position) == '^') {
            position++;
            double exponent = processParentheses(); // Use processParentheses to allow for nested expressions
            base = Math.pow(base, exponent);
        }
        return base;
    }

    private double processParentheses() {
        double sign = 1.0;
        if (position < funcExpression.length() && funcExpression.charAt(position) == '-') {
            sign = -1.0;
            position++;
        }
        double result = 0.0;
        if (position < funcExpression.length() && funcExpression.charAt(position) == '(') {
            position++;
            result = processExpression(); 
            if (position < funcExpression.length() && funcExpression.charAt(position) == ')') {
                position++;
            } else {
                System.out.println("Missing closing parenthesis");
                throw new Error("Missing closing parenthesis at position: " + position);
            }
        } else {
            result = processNumberOrVariable();
        }
        return sign * result;
    }

    private double processNumberOrVariable() {
        if (position < funcExpression.length() && (Character.isDigit(funcExpression.charAt(position)) || funcExpression.charAt(position) == '.')) {
            return processNumber();
        } else if (position < funcExpression.length() && Character.isLetter(funcExpression.charAt(position))) {
            return processFunctionOrVariable();
        } else {
            System.out.println("Unexpected character: " + funcExpression.charAt(position));
            throw new Error("Unexpected character at position: " + position);
        }
    }

    private double processFunctionOrVariable() {
        int start = position;
        while (position < funcExpression.length() && Character.isLetter(funcExpression.charAt(position))) {
            position++;
        }
        String token = funcExpression.substring(start, position);
    
        if (token.equals("sin") || token.equals("cos") || token.equals("tan")) {
            if (position < funcExpression.length() && funcExpression.charAt(position) == '(') {
                position++;
                double arg = processExpression(); 
                if (position < funcExpression.length() && funcExpression.charAt(position) == ')') {
                    position++;
                    switch (token) {
                        case "sin":
                            return Math.sin(Math.toRadians(arg));
                        case "cos":
                            return Math.cos(Math.toRadians(arg));
                        case "tan":
                            return Math.tan(Math.toRadians(arg));
                        default:
                            throw new Error("Unsupported trigonometric function: " + token);
                    }
                } else {
                    throw new Error("Missing closing parenthesis after function argument");
                }
            } else {
                throw new Error("Missing opening parenthesis for function argument");
            }
        } else if (variables.variableMap.containsKey(token)) {
            return variables.getVariable(token);
        } else if (token.equals("e")) {
            return Math.E;  // Recognize 'e' as the mathematical constant e
        } else {
            throw new Error("Unsupported function or variable: " + token);
        }
    }
    
    
    private double processNumber() {
        int start = position;
        while (position < funcExpression.length() && (Character.isDigit(funcExpression.charAt(position)) || funcExpression.charAt(position) == '.')) {
            position++;
        }
        return Double.parseDouble(funcExpression.substring(start, position));
    }

    private static class Variables {
        private Map<String, Double> variableMap;

        public Variables(Map<String, Double> variableMap) {
            this.variableMap = variableMap;
        }

        public double getVariable(String varName) {
            if (variableMap.containsKey(varName)) {
                return variableMap.get(varName);
            } else {
                throw new Error("Variable " + varName + " not found");
            }
        }
    }
}
