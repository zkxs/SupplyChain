package com.zkxs.supplychain;
/**
 * Represents a request to pull an arm
 * @author Michael Ripley (<a href="mailto:michael-ripley@utulsa.edu">michael-ripley@utulsa.edu</a>) Jan 27, 2014
 */
public class ArmPullRequest
{
	/**
	 * Cannot be instantiated
	 */
	private ArmPullRequest(){}
	
	/**
	 * Create a pull request given the index of the arm in the list. Either the indexed or
	 * ranked list can be used as a source.
	 * @param indexInList Index in the list the arm to pull is located at
	 * @param useRankedList if <code>true</code>, the index refers to the rank
	 * of the arm. If <code>false</code>, the index refers to the arms ID. (Determined
	 * by order of arm creation)
	 * @return the encoded arm pull request
	 */
	public static int get(int indexInList, boolean useRankedList)
	{
		if (useRankedList)
		{
			return indexInList;
		}
		else
		{
			return -1 - indexInList;
		}
	}
	
	/**
	 * Create a pull request given the arm to pull
	 * @param arm the arm to pull
	 * @return the encoded arm pull request
	 */
	public static int get(ArmMemory arm)
	{
		return get(arm.getIndex(), false);
	}

	/**
	 * Get the index of the arm to pull in whatever list it uses.
	 * @param pullRequest the encoded arm pull request
	 * @return the index of the arm to pull
	 */
	public static int getIndexInList(int pullRequest)
	{
		if (isUseRankedList(pullRequest))
		{
			return pullRequest;
		}
		else
		{
			return -1 - pullRequest;
		}
	}

	/**
	 * Determine if a ranked or index list should be used as the arm source
	 * @param pullRequest the encoded arm pull request
	 * @return <code>true</code> if the index refers to the rank
	 * of the arm. <code>false</code> if the index refers to the arms ID. (Determined
	 * by order of arm creation)
	 */
	public static boolean isUseRankedList(int pullRequest)
	{
		return pullRequest >= 0;
	}
}
