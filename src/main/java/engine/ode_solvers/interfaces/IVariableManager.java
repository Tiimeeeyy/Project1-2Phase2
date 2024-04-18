package engine.ode_solvers.interfaces;

/**
 * Interface for managing variables in a mathematical expression.
 */
public interface IVariableManager {

    /**
     * Sets the value of a variable.
     * 
     * @param name The name of the variable to set.
     * @param value The value to assign to the variable.
     */
    void setVariable(String name, Double value);

    /**
     * Gets the value of a variable.
     * 
     * @param name The name of the variable to retrieve.
     * @return The value of the variable.
     * @throws Error if the variable is not defined.
     */
    double getVariable(String name);
}
