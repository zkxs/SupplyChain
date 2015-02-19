package com.zkxs.supplychain;
/**
 * Online budget limited epsilon first. Explore until the exploration budget
 * is depleted, then exploit.
 * @author Michael Ripley (<a href="mailto:michael-ripley@utulsa.edu">michael-ripley@utulsa.edu</a>) Feb 9, 2014
 */
public class AlgorithmEpsilonFirst implements Algorithm
{
	/** The cost of pulling an arm, assuming all arms have the same cost */
	private final static double COST = SupplyChainDriver.COST;
	
	/** the entire budget the agent has */
	private final double initialBudget;
	
	/** Percentage of budget to devote to exploration */
	private final double epsilon;
	
	/** The current budget left for exploring */
	private double explorationBudget;
	
	/** Current index in the list of possible arms */
	private int index;
	
	/**
	 * Construct a new epsilon-first algorithm
	 * @param initialBudget the entire budget the agent has
	 * @param epsilon the percentage of the budget to devote to exploration
	 */
	public AlgorithmEpsilonFirst(double initialBudget, double epsilon)
	{
		this.initialBudget = initialBudget;
		this.epsilon = epsilon;
		
		explorationBudget = initialBudget * epsilon;
		index = 0;
	}
	
	@Override
	public int getNextArm(AgentSupplier agent)
	{
		if (explorationBudget < COST) // if we are done exploring
		{
			// pull the best arm every time
			return ArmPullRequest.get(agent.getAgentMemory().size() - 1, true);
		}
		else if (index == agent.getAgentMemory().size()) // then we must begin a new pass
		{
			// reset the index
			index = 0;
		}
		
		// at this point it is safe to pull arm@index
		explorationBudget -= COST; // decrement budget ASSUMES ALL SUPPLIERS HAVE THE SAME COST
		return ArmPullRequest.get(index++, false);
		// also increment index afterwards with the post-increment operator
	}
	
	@Override
	public Algorithm duplicate()
	{
		return new AlgorithmEpsilonFirst(initialBudget, epsilon);
	}
	
	@Override
	public boolean requiresInitialBudget()
	{
		return true;
	}
	
}
