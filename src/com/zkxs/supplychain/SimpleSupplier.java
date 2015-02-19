package com.zkxs.supplychain;
import java.util.ArrayList;

import org.apache.commons.math3.distribution.RealDistribution;

/**
 * A simple supplier intended for use as a leaf node in the supply tree.
 * (Raw material supplier)
 * @author Michael Ripley (<a href="mailto:michael-ripley@utulsa.edu">michael-ripley@utulsa.edu</a>) Jan 26, 2014
 */
public class SimpleSupplier extends Supplier
{
	/**
	 * Constructs a new SimpleSupplier
	 * @param cost The cost to use this supplier
	 * @param meanTime Average time this supplier takes to provide services
	 * @param distribution The distribution to use for this arm's samples
	 */
	public SimpleSupplier(double cost, double meanTime, RealDistribution distribution, double scale)
	{
		super(cost, meanTime, distribution, scale);
	}

	@Override
	public ArrayList<Supplier> getChildren()
	{
		return null;
	}

	@Override
	public boolean isLeafNode()
	{
		return true;
	}
	
	@Override
	public double supply()
	{
		return sample();
	}
}
