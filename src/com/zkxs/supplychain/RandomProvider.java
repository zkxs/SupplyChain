package com.zkxs.supplychain;
import java.util.Random;

/**
 * Provides static access to a single random number generator across the entire project
 * @author Michael Ripley (<a href="mailto:michael-ripley@utulsa.edu">michael-ripley@utulsa.edu</a>) Jan 26, 2014
 */
public class RandomProvider
{	
	/**
	 * The public random number generator.
	 * Not calling {@link Random#setSeed(long)}
	 * during a simulation run is appreciated.
	 */
	public final static Random rand = new Random();
	
	/**
	 * Cannot be instantiated, static access only
	 */
	private RandomProvider(){};
}
