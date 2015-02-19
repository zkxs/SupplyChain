package com.zkxs.supplychain;
/**
 * An example algorithm. It makes completely random decisions.
 * @author Michael Ripley (<a href="mailto:michael-ripley@utulsa.edu">michael-ripley@utulsa.edu</a>) Jan 26, 2014
 */
public class AlgorithmExampleRandom implements Algorithm
{
	
	@Override
	public int getNextArm(AgentSupplier agent)
	{
		return ArmPullRequest.get(RandomProvider.rand.nextInt(agent.getAgentMemory().size()), true);
	}

	@Override
	public Algorithm duplicate()
	{
		return new AlgorithmExampleRandom();
	}

	@Override
	public boolean requiresInitialBudget()
	{
		return false;
	}
	
}
