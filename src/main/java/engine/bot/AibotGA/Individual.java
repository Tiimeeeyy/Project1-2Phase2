package engine.bot.AibotGA;

/**
 * The individual class represents an individual in a genetic algorithm.
 * Each individual has a chromosome and a fitness value.
 */
public class Individual {
	
	char[][] chromosome;
	double fitness;

	/**
	 * Constructor for the Individual class.
	 * @param chromosome The chromosome of the individual.
	 */
	public Individual(char[][] chromosome) {
		this.chromosome = new char[chromosome.length][chromosome[0].length];
		for (int i = 0; i < chromosome.length; i++) {
			this.chromosome[i] = chromosome[i].clone();
		}
		
		this.fitness = 0;
	}

	/**
	 * Getter for the chromosome.
	 * @return The chromosome of the individual.
	 */
	public char[][] getChromosome() {
		return chromosome;
	}

	/**
	 * Setter for the chromosome.
	 * @param chromosome The new chromosome of the individual.
	 */
	public void setChromosome(char[][] chromosome) {
		this.chromosome = chromosome;
	}

	/**
	 * Getter for the fitness.
	 * @return The fitness of the individual.
	 */
	public double getFitness() {
		return fitness;
	}

	/**
	 * Setter of the fitness.
	 * @param fitness The new fitness of the individual.
	 */
	public void setFitness(double fitness) {
		this.fitness = fitness;
	}

	/**
	 * This method converts the genotype (chromosome) to a phenotype.
	 * @return The phenotype of an individual.
	 */
	public double[] genoToPhenotype() {
		
		double[] out=new double[chromosome.length];
		for (int i = 0; i < chromosome.length; i++) {
			StringBuilder builder = new StringBuilder();
			builder.append(chromosome[i]);

			out[i]=(Integer.parseInt(builder.toString(),2)-500)/100.0;
		}
		
		return out;
	}

	/**
	 * This method create a deep copy of the individual.
	 * @return A new individual Object with the same chromosome as the original.
	 */
	public Individual clone() {
		char[][] chromClone = new char[chromosome.length][chromosome[0].length];
		for(int i = 0; i < chromClone.length; i++) {
			for(int j=0;j<chromosome[0].length;j++)
			chromClone[i][j] = chromosome[i][j];
		}
		return new Individual(chromClone);
	}
	


}
