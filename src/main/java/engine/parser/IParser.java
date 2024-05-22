package engine.parser;

/**
 * Interface for parsing and evaluating mathematical expressions.
 */

public interface IParser {
    
    /**
     * Evaluates the mathematical expression and returns the result.
     *
     * @return The evaluated result of the expression as a double.
     * @throws Error if the expression is invalid or in case of undefined variables or operations.
     */
    double evaluate(); 
}
