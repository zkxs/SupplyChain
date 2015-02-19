package com.zkxs.supplychain;
/**
 * Implementation of the online fractional knapsack-based decreasing epsilon-greedy (fKDE) algorithm.
 * After an initial exploration-sweep phase using epsilon*budget (where epsilon is between 0 and 1),
 * the best arm is pulled with increasing probability. The probability of uniform random exploration is 
 * <code>min(1, pullsForExploration / pullsSoFar)</code>, otherwise the best arm is pulled. 
 * @author Michael Ripley (<a href="mailto:michael-ripley@utulsa.edu">michael-ripley@utulsa.edu</a>) Feb 9, 2014
 */
public class AlgorithmKDE implements Algorithm
{
	/** The cost of pulling an arm, assuming all arms have the same cost */
	private final static double COST = SupplyChainDriver.COST;
	
	/** the entire budget the agent has */
	private final double initialBudget;
	
	/** Percentage of budget to devote to exploration */
	private final double epsilon;
	
	/** The the number of times to pull arms */
	private final int gamma;
	
	/** Current index in the list of possible arms */
	private int index;
	
	/** The number of pulls we have performed so far */
	private int pulls;

	/**
	 * Construct a new KDE algorithm
	 * @param initialBudget the entire budget the agent has
	 * @param epsilon the percentage of the budget to devote to exploration
	 */
	public AlgorithmKDE(double initialBudget, double epsilon)
	{
		this.initialBudget = initialBudget;
		this.epsilon = epsilon;
		this.gamma = (int)(initialBudget * epsilon / COST);
		
		index = 0;
		pulls = 0;
	}
	
	@Override
	public int getNextArm(AgentSupplier agent)
	{
		if (pulls >= gamma) // if we are done exploring
		{
			
			// if random number 0-1  <=  probability of exploration
			// (essentially, if we "roll" an exploration)
			if (RandomProvider.rand.nextDouble() <= Math.min(1, gamma / pulls++))
			{	// then explore
				
				// pull a random arm
				return ArmPullRequest.get(RandomProvider.rand.nextInt(agent.getAgentMemory().size()), true);
			}
			else // exploit
			{
				// pull the best arm
				return ArmPullRequest.get(agent.getAgentMemory().size() - 1, true);
			}
		}
		else if (index >= agent.getAgentMemory().size()) // then we must begin a new pass
		{
			// reset the index
			index = 0;
		}
		
		// continue the exploration pass
		
		// at this point it is safe to pull arm@index
		pulls++;
		return ArmPullRequest.get(index++, false);
		// also increment index afterwards with the post-increment operator
	}
	
	@Override
	public Algorithm duplicate()
	{
		return new AlgorithmKDE(initialBudget, epsilon);
	}
	
	@Override
	public boolean requiresInitialBudget()
	{
		return true;
	}
	
}
