package com.zkxs.supplychain;
import java.util.Iterator;

/**
 * Static utility functions
 * @author Michael Ripley (<a href="mailto:michael-ripley@utulsa.edu">michael-ripley@utulsa.edu</a>) Jan 31, 2014
 */
public class Util
{	
	/**
	 * Make the no-arg constructor private so this class cannot be instantiated.
	 */
	private Util(){}
	
	/**
	 * Compare two doubles to see if they're "close enough" using percent error.
	 * This means that for numbers very close to zero, a larger acceptable
	 * epsilon value will be required to match.
	 * @param d1 A floating point number
	 * @param d2 A floating point number
	 * @param epsilon The percent error (1.0 = 100%) that is acceptable
	 * @return <code>true</code> if the numbers are "close enough"
	 */
	public static boolean fuzzyCompare(double d1, double d2, double epsilon)
	{
		return Math.abs(d1 - d2) <= epsilon * Math.max(Math.abs(d1), Math.abs(d2));
	}
	
	/**
	 * Print the contents of an Iterable object. Intended
	 * for debug purposes.
	 * @param e
	 */
	public static void printIterable(Iterable<?> e)
	{
		Iterator<?> i = e.iterator();
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		boolean first = true;
		
		while (i.hasNext())
		{
			if (first)
				first = false;
			else
				sb.append(", ");
			
			sb.append(i.next());
		}
		sb.append("}");
		System.out.println(sb.toString());
	}
	
	/**
	 * Print the number of pulls performed on each arm in
	 * the SortedList. Intended for debug purposes.
	 * @param e
	 */
	public static void printNumberOfPulls(SortedList<ArmMemory> e)
	{
		Iterator<ArmMemory> i = e.iterator();
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		boolean first = true;
		
		while (i.hasNext())
		{
			if (first)
				first = false;
			else
				sb.append(", ");
			
			sb.append(i.next().getPulls());
		}
		sb.append("}");
		System.out.println(sb.toString());
	}
	
}

