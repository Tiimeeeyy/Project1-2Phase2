package engine.bot.AibotGA;
public class Individual {
	
	char[][] chromosome;
	double fitness;
	
	public Individual(char[][] chromosome) {
		this.chromosome = new char[chromosome.length][chromosome[0].length];
		for (int i = 0; i < chromosome.length; i++) {
			this.chromosome[i] = chromosome[i].clone();
		}
		
		this.fitness = 0;
	}


	public char[][] getChromosome() {
		return chromosome;
	}

	public void setChromosome(char[][] chromosome) {
		this.chromosome = chromosome;
	}

	public double getFitness() {
		return fitness;
	}

	public void setFitness(double fitness) {
		this.fitness = fitness;
	}
	
	public double[] genoToPhenotype() {
		
		double[] out=new double[chromosome.length];
		for (int i = 0; i < chromosome.length; i++) {
			StringBuilder builder = new StringBuilder();
			builder.append(chromosome[i]);

			out[i]=(Integer.parseInt(builder.toString(),2)-500)/100.0;
		}
		
		return out;
	}
	
	public Individual clone() {
		char[][] chromClone = new char[chromosome.length][chromosome[0].length];
		for(int i = 0; i < chromClone.length; i++) {
			for(int j=0;j<chromosome[0].length;j++)
			chromClone[i][j] = chromosome[i][j];
		}
		return new Individual(chromClone);
	}
	


}
