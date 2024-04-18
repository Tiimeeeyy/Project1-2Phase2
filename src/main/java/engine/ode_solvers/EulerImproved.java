package engine.ode_solvers;

import java.util.ArrayList;
import java.util.HashMap;

import engine.ode_solvers.interfaces.ODESolver;
import engine.parser.ExpressionParser;

public class EulerImproved implements ODESolver{

    public Double[][] solverODE(int dim, Double t, Double h, HashMap<String, Double> initHM, ArrayList<String> functions, ArrayList<String> variables){
        int size = (int)(t/h);
        Double[][] fx = new Double[dim][size];
        Double[] dx = new Double[dim];
        Double[] dxNext = new Double[dim];
        HashMap<String, Double> initialCondHMNext = (HashMap<String, Double>) initHM.clone();

        for(int j = 0; j<size;j++){
            for(int i = 0; i<dim; i++){
                ExpressionParser parser = new ExpressionParser(functions.get(i), initHM);
                dx[i] = parser.evaluate();
            }
            for(int k = 0; k<dim;k++){
                String key = (String)(variables.get(k));
                initialCondHMNext.put(key, initHM.get(key)+(h*dx[k]));               
            }
            for(int i = 0; i<dim; i++){
                ExpressionParser parser = new ExpressionParser(functions.get(i), initHM);
                dxNext[i] = parser.evaluate();
            }
            for(int k = 0; k<dim;k++){
                String key = (String)(variables.get(k));
                initHM.put(key, initHM.get(key)+(h/2)*(dx[k]+dxNext[k]));   
                fx[k][j] = initHM.get(key);            
            }
        }

        return fx;
    }
}
