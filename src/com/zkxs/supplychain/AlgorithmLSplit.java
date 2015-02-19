package com.zkxs.supplychain;
/**
 * The l-split algorithm successively eliminates (1 - 1 / l)th of arms after each pass
 * @author Michael Ripley (<a href="mailto:michael-ripley@utulsa.edu">michael-ripley@utulsa.edu</a>) Jan 28, 2014
 */
public class AlgorithmLSplit implements Algorithm
{
	/** The percentage of the arms that are feasible */
	private double feasible;
	
	/** The value of L to use in the algorithm */
	private final double lValue;
	
	/** The threshold calculated from the l-value */
	private final double threshold;
	
	/** Current index in the list of possible arms */
	private int index;
	
	/** A snapshot of the ranked arms as per the beginning of a l-split pass */
	private SortedList<ArmMemory> rankedListSnapshot;
	
	/** True if we only have one arm left to pull */
	private boolean exploitationMode;
	
	/**
	 * Construct a new l-split algorithm with given value of l
	 * @param lValue The value of L to use in the algorithm
	 */
	public AlgorithmLSplit(double lValue)
	{
		this.feasible = 1.0;
		this.lValue = lValue;
		this.threshold = 1 - 1 / lValue;
		this.index = 0;
		this.rankedListSnapshot = null; // will be initialized on first call to algorithm
		this.exploitationMode = false;
	}
	
	@Override
	public int getNextArm(AgentSupplier agent)
	{
		if (exploitationMode) // only one arm is feasible
		{
			// pull the best arm every time
			return ArmPullRequest.get(agent.getAgentMemory().size() - 1, true);
		}
		else if (rankedListSnapshot == null) // first run, initialize
		{
			// take the first snapshot
			rankedListSnapshot = agent.getAgentMemory().getRankedListSnapshot();
		}
		else if (index >= rankedListSnapshot.size()) // then we must begin a new pass
		{
			// get a new snapshot of the arm rank
			rankedListSnapshot = agent.getAgentMemory().getRankedListSnapshot();
			
			// perform a split
			feasible *= threshold;
			
			// set initial index based off the now-infeasible portion of the list
			index = (int)(rankedListSnapshot.size() - rankedListSnapshot.size() * feasible);
			
			if (index >= rankedListSnapshot.size() - 1) // then we have 1 or less feasible arms left
			{
//				if (!agent.getAgentMemory().checkOptimal())
//					System.out.println("l-split converged incorrectly");
				
				// begin exploiting one arm, allowing much of this logic to be skipped
				exploitationMode = true;
				
				// this is no longer needed, set to null so the garbage collector can
				// do its work
				rankedListSnapshot = null;
				
				// this would only take effect next loop, therefore return now
				return ArmPullRequest.get(agent.getAgentMemory().size() - 1, true);
			}
		}
		
		// at this point it is safe to pull arm@index
		return ArmPullRequest.get(rankedListSnapshot.get(index++));
		// also increment index afterwards with the post-increment operator
	}

	@Override
	public Algorithm duplicate()
	{
		return new AlgorithmLSplit(lValue);
	}

	@Override
	public boolean requiresInitialBudget()
	{
		return false;
	}

	/**
	 * Get the value of l this algorithm is using
	 * @return The value of l this algorithm is using
	 */
	public double getLValue()
	{
		return lValue;
	}	
	
}
