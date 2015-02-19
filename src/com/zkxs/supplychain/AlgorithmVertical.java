package com.zkxs.supplychain;

public class AlgorithmVertical implements Algorithm
{
	/** True if we only want to exploit */
	private boolean exploitationMode;
	
	/** Something about how aggressive the algorithm is? */
	private double yqstddevtrheshold; //TODO: refactor
	
	/** A snapshot of the ranked arms as per the beginning of a SOAAV pass */
	private SortedList<ArmMemory> rankedArmListSnapshot;
	
	@Override
	public int getNextArm(AgentSupplier agent)
	{
		if (exploitationMode) // if exploiting
		{
			// pull the best arm every time
			return ArmPullRequest.get(agent.getAgentMemory().size() - 1, true);
		}
		else if (rankedArmListSnapshot == null) // if first run
		{
			// set up first run
			rankedArmListSnapshot = agent.getAgentMemory().getRankedListSnapshot();
			
		}
		
		return 0;
	}
	
	@Override
	public Algorithm duplicate()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean requiresInitialBudget()
	{
		// TODO Auto-generated method stub
		return false;
	}
	
}
