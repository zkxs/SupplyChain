package com.zkxs.supplychain;

public class AlgorithmUCB_BV1 implements Algorithm
{
	/** The cost of pulling an arm, assuming all arms have the same cost */
	private final static double COST = SupplyChainDriver.COST;
	
	/** current time */
	private int time = 0;
	
	/** Stores values of each arm for each time step */
	private double[] armIndexes;
	
	@Override
	public int getNextArm(AgentSupplier agent)
	{
		time++; // time starts at 1
		
		// if each arm has not yet been pulled once
		if (time <= agent.getAgentMemory().size())
		{
			// pull the next arm
			return ArmPullRequest.get(time - 1, false);
		}
		else
		{
			// lazily initialize armIndexes
			if (armIndexes == null) armIndexes = new double[agent.getAgentMemory().size()];
			
			// for each arm, calculate its index
			for (int i = 0; i < armIndexes.length; i++)
			{
				// reward is inversely proportional to time
				ArmMemory arm = agent.getAgentMemory().indexedList.get(i);
				double averageReward = 1 / arm.getMeanTime();
				int pulls = arm.getPulls();
				
				// this term is used twice, so I save it
				double term = Math.sqrt(Math.log(time - 1) / pulls);
				double armIndex = averageReward + (2 * term) / (1 - term);
				armIndexes[i] = armIndex;
			}
			
			// find the index of the maximum arm
			int maxIndex = 0;
			for (int i = 1; i < armIndexes.length; i++)
			{
				if (armIndexes[i] > armIndexes[maxIndex])
				{
					maxIndex = i;
				}
			}
			
			return ArmPullRequest.get(maxIndex, false);
		}
		
	}
	
	@Override
	public Algorithm duplicate()
	{
		return new AlgorithmUCB_BV1();
	}
	
	@Override
	public boolean requiresInitialBudget()
	{
		return false;
	}
	
}
