package engine.bot.AibotGA;

public class IndividualV {
    
	double[] chromosome;
	double fitness;


	public IndividualV(double[] chromosome) {
		this.chromosome = chromosome;
		this.fitness = 0;
	}

	public double[] getChromosome() {
		return chromosome;
	}

	public void setChromosome(double[] chromosome) {
		this.chromosome = chromosome;
	}


	public double getFitness() {
		return fitness;
	}


	public void setFitness(double fitness) {
		this.fitness = fitness;
	}

	
	public IndividualV clone() {
		IndividualV newindi=new IndividualV(chromosome.clone());
		newindi.setFitness(fitness);
		return newindi;
	}
	


}
