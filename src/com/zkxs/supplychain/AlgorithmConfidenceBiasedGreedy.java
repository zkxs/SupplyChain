package com.zkxs.supplychain;

import java.util.ArrayList;

/**
 * 
 * @author Michael Ripley (<a href="mailto:michael-ripley@utulsa.edu">michael-ripley@utulsa.edu</a>) Jun 5, 2014
 */
public class AlgorithmConfidenceBiasedGreedy implements Algorithm
{
	/** Used in comparisons in case of double rounding error */
	private final static double ALMOST_ONE = 0.996;
	
	/** The number of samples required to build an idea of the distribution of the current best arm */
	private final int initialExplorationSize;
	
	/** Has this algorithm been initialized? */
	private boolean initialized = false;
	
	/** Are we in the initial exploration phase? (Pulling each arm once) */
	private boolean initialExplore = true;
	
	/** The index of the current arm in the initial exploration phase */
	private int currentArmIndex = 0;
	
	/**
	 * Construct an new instance of AlgorithmConfidenceBiasedGreedy
	 * @param explorationInterval The number of samples required to build an
	 *        idea of the distribution of the current best arm
	 */
	public AlgorithmConfidenceBiasedGreedy(int initialExplorationSize)
	{
		this.initialExplorationSize = initialExplorationSize;
	}
	
	@Override
	public int getNextArm(AgentSupplier agent)
	{
		// if this is the first time this method is called, initialize things
		if (!initialized)
		{
			// for each arm in the agent's ArmMemory
			for (ArmMemory am : agent.getAgentMemory().indexedList)
			{
				// enable sample tracking in this ArmMemory object
				am.enable();
			}
			
			initialized = true;
		} // end of initialization block
		
		// if initial explore, pull each arm once
		if (initialExplore)
		{
			// generate a request for the current arm, and then increment the index
			final int request = ArmPullRequest.get(currentArmIndex++, false);
			
			// if the next request would be invalid, then exit initial-exploration mode
			if (currentArmIndex >= agent.getAgentMemory().size())
			{
				initialExplore = false;
			}
			
			printTrace(agent, request, Reason.INITIAL_EXPLORE);
			return request;
		}
		
		// if we need more samples, then perform online greedy
		else if (agent.getAgentMemory().rankedList.get(agent.getAgentMemory().size() - 1).getPulls() < initialExplorationSize)
		{
			// request the current best arm
			final int request = ArmPullRequest.get(agent.getAgentMemory().size() - 1, true);
			printTrace(agent, request, Reason.INITIAL_GREEDY);
			return request;
		}
		
		// if we have nothing else to do, then perform confidence-based exploration
		else
		{
			// get the ArmMemory of the best arm
			final ArmMemory best = agent.getAgentMemory().rankedList.get(agent.getAgentMemory().size() - 1);
			
			// accumulator to sum probability of each arm being at least as good as the current best
			double probabilityOfExplore = 0;
			
			// will eventually contain the most likely usurper of the current best arm
			ArmMemory maxNonBestArm = null;
			
			// for each arm
			for (ArmMemory arm : agent.getAgentMemory().indexedList)
			{
				// calculate the probability of this arm's samples occurring in best arm's distribution
				double currentProbability = arm.probabilityOfSamplesOccurring(best);
				
				/* If the probability of this arm occurring is one (or almost one, because of
				 * rounding error in floating-point numbers) then it is implied that the current
				 * distribution IS the best distribution. This is guaranteed to happen once, and
				 * should be ignored.
				 */
				if (currentProbability < ALMOST_ONE)
				{
					// this block calculates the most likely arm to usurp the best arm
					{
						// on the first loop through the for loop, initialize the max-finder variable
						if (maxNonBestArm == null)
						{
							maxNonBestArm = arm;
						}
						else // check for a new maximum
						{
							// find the probability of the current max
							final double pMax = maxNonBestArm.probabilityOfSamplesOccurring(best);
							
							// find the probability of this arm
							final double pCurrent = arm.probabilityOfSamplesOccurring(best);
							
							// if current arm is greater than the know maximum, update the known maximum
							if (pCurrent > pMax)
							{
								maxNonBestArm = arm;
							}
						}
					} // end find-max block
					
					// add current probability to the accumulator
					probabilityOfExplore += currentProbability;
				}
			} // end for
			
			/* Decide whether to explore or exploit. Exploration is performed
			 * proportionally to the probability that the current arm is not
			 * the best arm.
			 */
			if (RandomProvider.rand.nextDouble() < probabilityOfExplore)
			{
				// explore the arm most likely to usurp the current best arm
				final int request = ArmPullRequest.get(maxNonBestArm);
				printTrace(agent, request, Reason.EXPLORE);
				return request;
			}
			else
			{
				// exploit the current best arm (similar to online greedy)
				final int request = ArmPullRequest.get(agent.getAgentMemory().size() - 1, true);
				printTrace(agent, request, Reason.EXPLOIT);
				return request;
			}
			
		}
	}
	
	@Override
	public Algorithm duplicate()
	{
		return new AlgorithmConfidenceBiasedGreedy(initialExplorationSize);
	}
	
	@Override
	public boolean requiresInitialBudget()
	{
		return false;
	}
	
	private void printTrace(AgentSupplier agent, int request, Reason reason)
	{
		final ArrayList<Supplier> armsReal = agent.getChildren();
		final AgentMemory memory = agent.getAgentMemory();
		final ArrayList<ArmMemory> armsObserved = memory.indexedList;
		
		final int selected = memory.getIndex(request);
		
		// print intent
		switch(reason)
		{
			case EXPLOIT:
				System.out.print("Exploit best arm");
				break;
			case EXPLORE:
				System.out.print("Explore likely usurper");
				break;
			case INITIAL_EXPLORE:
				System.out.print("Initial exploration pass");
				break;
			case INITIAL_GREEDY:
				System.out.print("Inital greedy data gathering");
				break;
			default:
				System.err.print("Unknown reason");
				System.exit(1);
				break;
		}
		
		System.out.print(": ");
		System.out.println(selected);
		
		// print arm table
		{
			final int lastIndex = armsReal.size() - 1;
			final String indexFormat = "%6d";
			final String realFormat = "%6.3f";
			final String observedFormat = realFormat;
			final String percentageFormat = "%5.1f%%";
			final String infinity = " ∞    ";
			final String blank   =  "        ";
			final String pointer =  "   ^    ";
			
			// print indices
			System.out.print("{");
			for (int i = 0; i < armsReal.size(); i++)
			{
				System.out.printf(indexFormat, i);
				if (i != lastIndex)
				{
					System.out.print(", ");
				}
			}
			System.out.println("}");
			
			// print real values
			System.out.print("{");
			for (int i = 0; i < armsReal.size(); i++)
			{
				System.out.printf(realFormat, armsReal.get(i).getMeanTime());
				if (i != lastIndex)
				{
					System.out.print(", ");
				}
			}
			System.out.println("}");
			
			// print observed values
			System.out.print("{");
			for (int i = 0; i < armsReal.size(); i++)
			{
				double meanTime = armsObserved.get(i).getMeanTime();
				
				if (meanTime == Double.MAX_VALUE)
					System.out.print(infinity);
				else
					System.out.printf(observedFormat, meanTime);
				if (i != lastIndex)
				{
					System.out.print(", ");
				}
			}
			System.out.println("}");
			
			// print usurper chance
			final ArmMemory best = agent.getAgentMemory().rankedList.get(agent.getAgentMemory().size() - 1);
			System.out.print("{");
			for (int i = 0; i < armsReal.size(); i++)
			{
				System.out.printf(percentageFormat, 100 * armsObserved.get(i).probabilityOfSamplesOccurring(best));
				if (i != lastIndex)
				{
					System.out.print(", ");
				}
			}
			System.out.println("}");
			
			// point at selected
			System.out.print(" ");
			for (int i = 0; i < selected; i++)
			{
				System.out.print(blank);
			}
			System.out.println(pointer);
		}
		
//		try
//		{
//			Thread.sleep(100);
//		}
//		catch (InterruptedException e)
//		{
//			e.printStackTrace();
//		}
		
	}
	
	private enum Reason
	{
		EXPLOIT, EXPLORE, INITIAL_GREEDY, INITIAL_EXPLORE
	}
	
}
