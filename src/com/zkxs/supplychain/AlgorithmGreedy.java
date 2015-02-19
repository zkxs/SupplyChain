package com.zkxs.supplychain;
/**
 * Online budget limited epsilon first. Explore until the exploration budget
 * is depleted, then exploit.
 * @author Michael Ripley (<a href="mailto:michael-ripley@utulsa.edu">michael-ripley@utulsa.edu</a>) Feb 9, 2014
 */
public class AlgorithmGreedy implements Algorithm
{
	/** The cost of pulling an arm, assuming all arms have the same cost */
	private final static double COST = SupplyChainDriver.COST;
	
	/** Current index in the list of possible arms */
	private int index;
	
	/**
	 * Construct a new epsilon-first algorithm
	 * @param initialBudget the entire budget the agent has
	 * @param epsilon the percentage of the budget to devote to exploration
	 */
	public AlgorithmGreedy()
	{
		index = 0;
	}
	
	@Override
	public int getNextArm(AgentSupplier agent)
	{
		if (index == agent.getAgentMemory().size()) // then we are done exploring
		{
			// pull the best arm every time
			return ArmPullRequest.get(agent.getAgentMemory().size() - 1, true);
		}
		
		// at this point it is safe to pull arm@index
		return ArmPullRequest.get(index++, false);
		// also increment index afterwards with the post-increment operator
	}
	
	@Override
	public Algorithm duplicate()
	{
		return new AlgorithmGreedy();
	}
	
	@Override
	public boolean requiresInitialBudget()
	{
		return false;
	}
	
}
