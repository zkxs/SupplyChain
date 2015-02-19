package com.zkxs.supplychain;

/**
 * A simple interface for arm-selecting algorithms. All information your algorithm requires
 * should be gathered by accessors within the agent passed to it by {@link #getNextArm(AgentSupplier)}.
 * Each agent has a separate instance of Algorithm, and therefore you may create any instance variables
 * necessary to 
 * @author Michael Ripley (<a href="mailto:michael-ripley@utulsa.edu">michael-ripley@utulsa.edu</a>) Jan 26, 2014
 */
public interface Algorithm
{	
	/**
	 * Select an arm to pull
	 * @param agent The agent this algorithm is working for
	 * @return the index of the arm to pull within the agent's AgentMemory object (relevant
	 * source code in {@link AgentSupplier#explore(double)})
	 */
	public int getNextArm(AgentSupplier agent);
	
	
	/**
	 * Return a fresh instance of this algorithm with the same initial parameters
	 * @return a fresh instance of this algorithm with the same initial parameters
	 */
	public Algorithm duplicate();
	
	/**
	 * Check if this algorithm requires the budget to be predetermined.
	 * @return <code>true</code> if this algorithm does not support dynamic budgeting, 
	 * <code>false</code> otherwise.
	 */
	public boolean requiresInitialBudget();
}
