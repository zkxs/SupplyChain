package com.zkxs.supplychain;
import java.util.ArrayList;

import org.apache.commons.math3.distribution.RealDistribution;

/**
 * A supplier that provides services and possibly has subsuppliers that it in turn buys
 * services from.
 * @author Michael Ripley (<a href="mailto:michael-ripley@utulsa.edu">michael-ripley@utulsa.edu</a>) Jan 26, 2014
 */
public abstract class Supplier
{	
	/** Cost to use our services */
	private final double cost;
	
	/** Offset of the distribution */
	private final double offset;
	
	/** stretch the distribution */
	private double scale;
	
	/** True if this arm is the best arm amongst its peers */
	private boolean bestArm;
	
	/** The distribuiton of this arm's values */
	private RealDistribution distribution;
	
	/**
	 * Constructs a new SimpleSupplier
	 * @param cost The cost to use this supplier
	 * @param meanTime Average time this supplier takes to provide services
	 * @param standardDeviation Standard deviation of the service time
	 */
	public Supplier(double cost, double meanTime, RealDistribution distribution, double scale)
	{
		this.cost = cost;
		this.distribution = distribution;
		this.scale = scale;
		
		this.offset = meanTime;
	}
	
	/**
	 * Get the child nodes of this node
	 * @return <code>null</code> if this node has no children
	 */
	public abstract ArrayList<Supplier> getChildren();
	
	/**
	 * Check if this supplier is a leaf node (raw material supplier)
	 * @return <code>true</code> if this supplier is a leaf node
	 */
	public abstract boolean isLeafNode();
	
	/**
	 * Purchase supplies from this supplier.  The supplier will be paid the current
	 * value returned by {@link #getCost()} if the supplier has the ability to make purchases.
	 * The calling agent is expected to decrement its own budget.
	 * @return The amount of time required to complete the order
	 */
	public abstract double supply();
	
	/**
	 * Get a sample
	 * @return a sample
	 */
	protected double sample()
	{
		return (distribution.sample() - distribution.getNumericalMean() ) * scale + offset;
	}
	
	/**
	 * Get the cost to use this supplier
	 * @return the cost to use this supplier
	 */
	public double getCost()
	{
		return cost;
	}

	/**
	 * Returns the mean time taken by this supplier.
	 * Agents aren't allowed to use this method, that would be cheating!
	 * @return the mean time taken by this supplier
	 */
	public double getMeanTime()
	{
		return offset;
	}
	
	/**
	 * Returns this arm's distribution
	 * Agents aren't allowed to use this method, that would be cheating!
	 * @return This arm's distribution
	 */
	public RealDistribution getDistribution()
	{
		return distribution;
	}

	/**
	 * Sets this arm's distribution
	 * @param distribution This arm's distribution
	 */
	public void setDistribution(RealDistribution distribution)
	{
		this.distribution = distribution;
	}
	
	public void setScale(double scale)
	{
		this.scale = scale;
	}

	/**
	 * Checks if this is the best arm to pull
	 * Agents aren't allowed to use this method, that would be cheating!
	 * @return <code>true</code> if this is the best arm to pull
	 */
	public boolean isBestArm()
	{
		return bestArm;
	}
	
	/**
	 * Indicate that this is the best arm to pull. This method is intended
	 * to be called during the creation of sets of arms so that after the
	 * simulation is complete regret can be more easily computed
	 * @param bestArm <code>true</code> if this is the best arm to pull
	 */
	public void setBestArm(boolean bestArm)
	{
		this.bestArm = bestArm;
	}

	@Override
	public String toString()
	{
		return "" + getMeanTime();
	}
}
