package com.zkxs.supplychain;

public class AlgorithmSOAAV implements Algorithm
{
	/** True if we only have one arm left to pull */
	private boolean exploitationMode;
	
	/** The value of x to use in the algorithm */
	private final double xValue;
	
	/** the multiplier to apply to the overall average to derive the threshold */
	private final double thresholdMultiplier;
	
	/** The current cut-off point of the averages of the arms */
	private double threshold;
	
	/** Current index in the list of possible arms */
	private int currentPosition;
	
	/** The last starting point of the active arms */
	private int lastStart;
	
	/** A snapshot of the ranked arms as per the beginning of a SOAAV pass */
	private SortedList<ArmMemory> rankedArmListSnapshot;
	
	/**
	 * Construct a new SOAAV algorithm with given value of x
	 * @param x The value of x to use in the algorithm
	 */
	public AlgorithmSOAAV(double x)
	{
		exploitationMode = false;
		xValue = x; // remember so we can duplicate()
		thresholdMultiplier = 1 + x;
		threshold = Double.MIN_VALUE; // anything goes
		currentPosition = 0;
		lastStart = 0;
		rankedArmListSnapshot = null;
	}
	
	
	@Override
	public int getNextArm(AgentSupplier agent) // only one arm is feasible
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
		else if (currentPosition >= rankedArmListSnapshot.size()) // if end of pass reached
		{	// then reset the pass
			
			// get a new snapshot of the arm rank
			rankedArmListSnapshot = agent.getAgentMemory().getRankedListSnapshot();
			
			// update the average threshold
			threshold = agent.getAgentMemory().getAverage(lastStart) * thresholdMultiplier;
			
			// set initial index based off the now-infeasible portion of the list
			for (	currentPosition = 0; 
					rankedArmListSnapshot.get(currentPosition).getMeanTime() > threshold;
					currentPosition++); // this for-loop intentionally has no body. TODO: show folks
			lastStart = currentPosition;
			
			if (currentPosition >= rankedArmListSnapshot.size() - 1) // then we have 1 or less feasible arms left
			{
				// begin exploiting one arm, allowing much of this logic to be skipped
				exploitationMode = true;
				
				// this is no longer needed, set to null so the garbage collector can
				// do its work
				rankedArmListSnapshot = null;
				
				// this would only take effect next loop, therefore return now
				return ArmPullRequest.get(agent.getAgentMemory().size() - 1, true);
			}
		}
		
		// at this point it is safe to pull arm@currentPosition
		return ArmPullRequest.get(rankedArmListSnapshot.get(currentPosition++));
		// also increment currentPosition afterwards with the post-increment operator
	}
	
	@Override
	public Algorithm duplicate()
	{
		return new AlgorithmSOAAV(xValue);
	}

	@Override
	public boolean requiresInitialBudget()
	{
		return false;
	}
	
}
