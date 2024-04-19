package engine.ode_solvers;

import java.util.ArrayList;
import java.util.HashMap;

import engine.ode_solvers.interfaces.ODESolver;
import engine.parser.ExpressionParser;
// test a
public class Euler implements ODESolver{
    
    public Double[][] solverODE(int dim, Double t, Double h, HashMap<String, Double> initHM, ArrayList<String> functions, ArrayList<String> variables){
        int size = (int)(t/h);
        Double[][] fx = new Double[dim][size];
        Double[] dx = new Double[dim];

        HashMap<String,Double[]> fxMap=new HashMap<>();
        for (int i = 0; i < dim; i++) {
            Double[] fxelement=new Double[size];
            fxMap.put(variables.get(i), fxelement.clone());
        }

        for(int j = 0; j<size;j++){
            for(int i = 0; i<dim; i++){
                ExpressionParser parser = new ExpressionParser(functions.get(i), initHM);
                dx[i] = parser.evaluate();
            }
            for(int k = 0; k<dim;k++){
                String key = (String)(variables.get(k));
                initHM.put(key, initHM.get(key)+(h*dx[k]));
                fx[k][j]=initHM.get(key);   
            }
        }
        return fx;
    }
}
