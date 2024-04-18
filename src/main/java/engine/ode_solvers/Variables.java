package engine.ode_solvers;

import java.util.HashMap;
import java.util.Map;

import engine.ode_solvers.interfaces.IVariableManager;

public class Variables implements IVariableManager{
    private Map<String, Double> variables = new HashMap<>();

    public Variables(Map<String, Double> initialVariables) {
        variables.putAll(initialVariables);
    }

    public void setVariable(String name, Double value) {
        variables.put(name, value);
    }

    public double getVariable(String name) {
        Double value = variables.get(name);
        if (value == null) {
            System.out.println("Undefined variable: " + name);
            throw new Error();
        }
        return value;
    }
}
