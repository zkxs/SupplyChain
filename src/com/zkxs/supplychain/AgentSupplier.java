package com.zkxs.supplychain;
import java.util.ArrayList;

import org.apache.commons.math3.distribution.RealDistribution;

/**
 * This is the agent used in this simulation, as well as being a valid supplier (arm).
 * This allows for the creation of a hierarchy of agents that dynamically attempt to find
 * the best suppliers.
 * @author Michael Ripley (<a href="mailto:michael-ripley@utulsa.edu">michael-ripley@utulsa.edu</a>) Jan 26, 2014
 */
public class AgentSupplier extends Supplier 
{	
	long id = RandomProvider.rand.nextLong(); // TODO: make a getter
	
	/** The algorithm used to select the next arm to pull */
	Algorithm algorithm;
	
	/** This supplier's suppliers */
	private final ArrayList<Supplier> children;
	
	/** The agent's memory of each arm's performance */
	private AgentMemory agentMemory;
	
	/** The current budget of this agent */
	private double budget;
	
	/** The total amount of time this agent has taken */
	private double totalTimeTaken;

	/** The number of times the agent has pulled an arm */
	private int totalPulls;
	
	/** This agent's budget is multiplied by this amount when exploring */
	private final double budgetMultiplier;
	
	/** Is this node the root node? */
	private final boolean isRoot;
	
	/**
	 * Constructs a new AgentSupplier
	 * @param algorithm
	 * @param children The child nodes of this supplier
	 * @param cost The cost to use this supplier
	 * @param meanTime Average time this supplier takes to provide services
	 * @param distribution The distribution to use for this arm's samples
	 * @param budgetMultiplier The amount to multiply the budget increase by when {@link #supply()} is called.
	 */
	public AgentSupplier(Algorithm algorithm, ArrayList<Supplier> children, double cost, double meanTime,
			RealDistribution distribution, double scale, double budgetMultiplier, boolean isRoot)
	{
		super(cost, meanTime, distribution, scale);
		
		// !A + B == A -> B
		// algorithm.requiresInitialBudget() IMPLIES isRoot
		assert !algorithm.requiresInitialBudget() || isRoot : "required inital budget IMPLIES root node";
		
		this.algorithm = algorithm;
		this.children = children;
		this.budgetMultiplier = budgetMultiplier;
		this.isRoot = isRoot;
		
		agentMemory = new AgentMemory(children);
		totalPulls = 0;
		totalTimeTaken = 0;
		budget = 0;
	}
	
	/**
	 * Reset the state of this agent
	 */
	public void reset(RealDistribution distribution, double scale)
	{
		setDistribution(distribution);
		setScale(scale);
		agentMemory.reset();
		totalPulls = 0;
		totalTimeTaken = 0;
		budget = 0;
		
		algorithm = algorithm.duplicate();
	}
	
	/**
	 * Reset the state of this agent and select a new algorithm
	 * @param newAlgorithm the new algorithm to use
	 */
	public void reset(RealDistribution distribution, double scale, Algorithm newAlgorithm)
	{
		reset(distribution, scale);
		this.algorithm = newAlgorithm;
	}
	
	@Override
	public double supply()
	{		
		// return the time our suppliers took in addition to our personal
		// processing time
		return explore(getCost() * budgetMultiplier) + sample();
	}

	/**
	 * Spend the budget as much as possible.  
	 * @param budget The exploration budget
	 * @return The time taken for all of our suppliers to complete delivery
	 */
	public double explore(double budget)
	{	
		// this assumes all child arms have the same cost //TODO: unify cost
		double cost = children.get(0).getCost();
		
		/*
		 * I suspect that dynamically increasing the budget during the simulation
		 * will interfere with the normal operation of some of the algorithms, as they
		 * appear to assume that the total budget is initially known.
		 */
		this.budget += budget;
		
		// total time is the maximum of all of the times
		// (imagine all purchases are made simultaneously
		double maxTimeSpent = 0;
		
		// actual total time spent this explore()
		double totalTimeSpent = 0;
		
		// number of pulls during this exploration
		int pullsThisExplore = 0;
		
		// while we have budget, explore
		while(this.budget >= cost)
		{
			// debug current arm memory state
//			System.out.println(agentMemory);
//			try {Thread.sleep(500);} catch (InterruptedException e){}
			
			// select an arm using our algorithm
			int pullRequest = algorithm.getNextArm(this);
			this.budget -= cost;
			
			// pull the arm
			double timeSpent = agentMemory.pull(pullRequest);
			totalPulls++;
			pullsThisExplore++;
			
			// maintain running maximum
			if (timeSpent > maxTimeSpent)
			{
				maxTimeSpent = timeSpent;
			}
			
			// maintain running sum
			totalTimeSpent += timeSpent;
		}
		
		// budget should never go negative
		assert this.budget >= 0.0;
		
		// budget is quantized
		assert this.budget == 0.0;
		
		
		/* 
		 * TODO: re-enable this?
		 * Returning the maximum of all the times is seriously messing with things, disabling for now
		 */
		
		// remember how much time it has taken to get supplied
		//totalTimeTaken += maxTimeSpent; // disabled for now
		totalTimeTaken += totalTimeSpent / pullsThisExplore;
		
		// assert that no double rounding errors have occurred
		assert (double)pullsThisExplore == budget : pullsThisExplore + ", " + budget;
		
		//return maxTimeSpent; // disabled for now
		
		return totalTimeSpent / pullsThisExplore;
	}
	
	@Override
	public ArrayList<Supplier> getChildren()
	{
		return children;
	}
	
	@Override
	public boolean isLeafNode()
	{
		return getChildren() == null;
	}

	/**
	 * Get the agent's memory of each arm's performance
	 * @return The agent's memory of each arm's performance
	 */
	public AgentMemory getAgentMemory()
	{
		return agentMemory;
	}

	/**
	 * Get the total amount of time this agent has taken
	 * @return The total amount of time this agent has taken
	 */
	public double getTotalTimeTaken()
	{
		return totalTimeTaken;
	}

	/**
	 * Get the number of times the agent has pulled an arm
	 * @return The number of times the agent has pulled an arm
	 */
	public int getTotalPulls()
	{
		return totalPulls;
	}

	/**
	 * Get the budget multiplier of this agent. For example, if the agent is given enough
	 * money for 1 pull and the multiplier is 5, the agent can actually perform 5 pulls.
	 * This allows arms to explore more efficiently.
	 * @return The budget multiplier of this agent
	 */
	public double getBudgetMultiplier()
	{
		return budgetMultiplier;
	}

	/**
	 * Check if this agent is the root agent in the tree
	 * @return <code>true</code> if this agent is the root agent in the tree, 
	 * <code>false</code> otherwise.
	 */
	public boolean isRoot()
	{
		return isRoot;
	}
	
}
