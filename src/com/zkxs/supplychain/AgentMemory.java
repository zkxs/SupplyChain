package com.zkxs.supplychain;
import java.util.ArrayList;

/**
 * Stores an agents memory of each arm. Allows arm lookup by index or rank. The
 * ranked list is ordered according to the contents natural order, which in the
 * case of ArmMemory objects is in order of decreasing average time (increasing utility).
 * @author Michael Ripley (<a href="mailto:michael-ripley@utulsa.edu">michael-ripley@utulsa.edu</a>) Jan 27, 2014
 */
public class AgentMemory
{	
	/*private*/ ArrayList<ArmMemory> indexedList;
	/*private*/ SortedList<ArmMemory> rankedList;
	
	/**
	 * Creates a new arm memory object for the given suppliers
	 * @param suppliers All of the child suppliers of the agent
	 */
	public AgentMemory(ArrayList<Supplier> suppliers)
	{
		// initialize the indexed list
		indexedList = new ArrayList<ArmMemory>(suppliers.size());
		for (int i = 0; i < suppliers.size(); i++)
		{
			indexedList.add(new ArmMemory(suppliers.get(i), i));
		}
		
		// initialize the ranked list
		rankedList = new SortedList<ArmMemory>(suppliers.size());
		for (ArmMemory arm : indexedList)
		{
			rankedList.append(arm);
		}
	}
	
	/**
	 * Reset this Agent Memory
	 */
	public void reset()
	{
		rankedList.clear();
		for (ArmMemory arm : indexedList)
		{
			arm.reset();
			rankedList.append(arm);
		}
	}
	
	/**
	 * Pull the requested arm and update relevant memory
	 * @param request The arm pull request
	 * @return the time the arm took to supply us
	 */
	public double pull(int request)
	{
		ArmMemory selectedArmMemory;
		
		if (ArmPullRequest.isUseRankedList(request))
		{
			// select arm appropriately (linear time)
			selectedArmMemory = rankedList.get(ArmPullRequest.getIndexInList(request));
			
			// we must remove the arm from the sorted list and re-enter it after changing it's average cost
			rankedList.remove(ArmPullRequest.getIndexInList(request));
		}
		else
		{
			// select arm appropriately (linear time)
			selectedArmMemory = indexedList.get(ArmPullRequest.getIndexInList(request));
			
			// we must remove the arm from the sorted list and re-enter it after changing it's average cost
			// search is O( log(n) )
			rankedList.remove(selectedArmMemory);
			
		}
		
		// pull the arm
		double timeSpent = selectedArmMemory.getSupplier().supply();
		
		// record arm pull
		selectedArmMemory.recordPull(timeSpent);
		
		// replace the updated ArmMemory
		rankedList.add(selectedArmMemory);
		
		// return time spent by the arm
		return timeSpent;
	}
	
	/**
	 * Get the number of arms
	 * @return the number of arms
	 */
	public int size()
	{
		return indexedList.size();
	}
	
	/**
	 * Get a snapshot of the current ranked list
	 * @return a snapshot of the current ranked list
	 */
	public SortedList<ArmMemory> getRankedListSnapshot()
	{
		return new SortedList<ArmMemory>(rankedList);
	}
	
	/**
	 * Get the total average of all of the arms' performances
	 * @param beginningIndex The index (inclusive) to start the average at
	 * @return The total average of all of the arms' performances
	 */
	public double getAverage(int beginningIndex)
	{
		double sum = 0;
		double arms = 0;
		
		for (int i = beginningIndex; i < rankedList.size(); i++)
		{
			sum += rankedList.get(i).getMeanTime();
			arms++;
		}
		
		return sum / arms;
	}
	
	/**
	 * Check if the current top-ranked arm is the best arm
	 * @return <code>true</code> if the current top-ranked arm is the best arm,
	 * <code>false</code> otherwise.
	 */
	public boolean checkOptimal()
	{
		return rankedList.get(rankedList.size() - 1).getSupplier().isBestArm();
	}
	
	/**
	 * Given an ArmPullRequest, find the index (indexed list) of the requested arm
	 * @param pullRequest int representation of the ArmPullRequest
	 * @return the index (indexed list) of the requested arm
	 */
	public int getIndex(int pullRequest)
	{
		int index = ArmPullRequest.getIndexInList(pullRequest);
		if (ArmPullRequest.isUseRankedList(pullRequest))
		{
			index = rankedList.get(index).getIndex();
		}
		return index;
	}
}
