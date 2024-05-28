package engine.parser;

import java.util.Map;

/**
 * The expression parser class Implements the IParser Interface and is used to parse and evaluate Mathematical expressions.
 * The expressions can contain numbers, variables and the operators: +, -, *, /, ^.
 * The Variables are stored in a map and their values can be set when creating a new ExpressionParser object.
 */
public class ExpressionParser implements IParser {

    private String funcExpression;
    private int position = 0;
    private Variables variables;

    /**
     * Class constructor
     * @param expression The mathematical expression to be parsed and evaluated.
     * @param initVarsHM The initial values of the Variables.
     */
    public ExpressionParser(String expression, Map<String, Double> initVarsHM) {
        this.funcExpression = expression.replaceAll("\\s+", "");
        this.variables = new Variables(initVarsHM);
    }

    /**
     * This method evaluates the mathematical expression and returns the result.
     * @return The result of the mathematical expression.
     */
    public double evaluate() {
        double finalResult = processExpression();
        if (position < funcExpression.length()) {
            System.out.println("Unexpected character: " + funcExpression.charAt(position));
            throw new Error("Unexpected character at position: " + position);
        }
        return finalResult;
    }
    // The following methods are helper functions used to parse and evaluate the mathematical expression.
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
                            // return Math.sin(Math.toRadians(arg));
                            return Math.sin(arg);
                        case "cos":
                            // return Math.cos(Math.toRadians(arg));
                            return Math.cos(arg);
                        case "tan":
                            // return Math.tan(Math.toRadians(arg));
                            return Math.tan(arg);
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

    /**
     * The Variables class is a private static inner class used to store the Variables and their values.
     */
    private static class Variables {
        private Map<String, Double> variableMap;

        /**
         * Class constructor.
         * @param variableMap The map of Variables and their values.
         */
        public Variables(Map<String, Double> variableMap) {
            this.variableMap = variableMap;
        }

        /**
         * This method returns the value of a Variable.
         * @param varName The name of the Variable.
         * @return The value of the Variable.
         */
        public double getVariable(String varName) {
            if (variableMap.containsKey(varName)) {
                return variableMap.get(varName);
            } else {
                throw new Error("Variable " + varName + " not found");
            }
        }
    }
}
