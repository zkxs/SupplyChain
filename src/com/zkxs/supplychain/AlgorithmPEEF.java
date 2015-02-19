package com.zkxs.supplychain;
/**
 * Modified l-split with l chosen such that the number of
 * remaining arm choices is reduced to 1 approximately at the
 * end of the exploration phase. <br /><br />
 * 
 * The formula for l is:
 * <br />
 * l = (eB - 1) / (eB - K)
 * @author Michael Ripley (<a href="mailto:michael-ripley@utulsa.edu">michael-ripley@utulsa.edu</a>) Feb 4, 2014
 */
public class AlgorithmPEEF extends AlgorithmLSplit
{

	/**
	 * Construct an new PEEF algorithm
	 * @param numberOfArms The number of arms the agent has as children
	 * @param initialBudget The entire budget of the agent
	 * @param epsilon The percentage of the budget to devote to exploration
	 */
	public AlgorithmPEEF(int numberOfArms, double initialBudget, double epsilon)
	{
		// l = (eB - 1) / (eB - K)
		super((epsilon * initialBudget - 1) / (epsilon * initialBudget - numberOfArms));
	}
	
	/**
	 * Private constructor for duplication of this algorithm
	 * @param lValue the lValue to use in the superclass ({@link AlgorithmLSplit})
	 */
	private AlgorithmPEEF(double lValue)
	{
		super(lValue);
	}

	@Override
	public Algorithm duplicate()
	{
		return new AlgorithmPEEF(super.getLValue());
	}

	@Override
	public boolean requiresInitialBudget()
	{
		return true;
	}
	
}
