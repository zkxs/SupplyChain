package com.zkxs.supplychain;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Comparator;

/**
 * Stores statistics about a supplier, in addition to a reference to this supplier
 * @author Michael Ripley (<a href="mailto:michael-ripley@utulsa.edu">michael-ripley@utulsa.edu</a>) Jan 26, 2014
 */
public class ArmMemory implements Comparable<ArmMemory>
{	
	// these fields are currently protected 
	
	private double totalTime;
	private int pulls;
	private final Supplier supplier;
	
	/** This arm's index on the multi-armed bandit */
	private final int index;
	
	private boolean enabled = false;
	private ArmMemory oldBest = null;
	private boolean probabilityCached = false;
	private double cachedProbability;
	private SortedList<Double> samples;
	
	/** Used to sort doubles in descending order */
	private static Comparator<Double> descendingDoubleComparator = new Comparator<Double>(){
		@Override
		public int compare(Double o1, Double o2)
		{
			return -o1.compareTo(o2);
		}
	};
	
	/**
	 * Constructs an ArmMemory object
	 * @param supplier The supplier this object is remembering
	 * @param index index of the arm in the ArrayList of suppliers (should be unique)
	 */
	public ArmMemory(Supplier supplier, int index)
	{
		this.supplier = supplier;
		this.index = index;
		totalTime = 0;
		pulls = 0;
		
		samples = new SortedList<>(descendingDoubleComparator);
	}
	
	/**
	 * Reset this ArmMemory object
	 */
	public void reset()
	{
		totalTime = 0;
		pulls = 0;
		
		if (enabled)
		{
			enabled = false;
			oldBest = null;
			probabilityCached = false;
			samples.clear();
		}
	}
	
	/**
	 * Record the length of time this supplier just took
	 * @param time the length of time this supplier just took
	 */
	public void recordPull(double time)
	{
		totalTime += time;
		pulls++;
		
		if (enabled)
		{
			samples.add(time);
			probabilityCached = false;
		}
	}
	
	/**
	 * Enable tracking of every individual sample. This is expensive in both
	 * both time and memory, and few algorithms need this functionality,
	 * therefore it is disabled by default or when the ArmMemory is {@link #reset()}
	 */
	public void enable()
	{
		enabled = true;
	}
	
	/**
	 * Computes the average time this supplier takes
	 * @return the average time this supplier takes, or Double.MAX_VALUE if no data has been gathered yet
	 * (this way untested arms have the worst utility)
	 */
	public double getMeanTime()
	{
		if (isUnpulled()) return Double.MAX_VALUE;
		return totalTime / pulls;
	}
	
	/**
	 * Returns the number of times the arm has been pulled
	 * @return the number of times the arm has been pulled
	 */
	public int getPulls()
	{
		return pulls;
	}
	
	/**
	 * Check if this arm has been pulled
	 * @return <code>true</code> if this arm has been pulled, <code>false</code> if it has not.
	 */
	public boolean isUnpulled()
	{
		return pulls == 0;
	}
	
	/**
	 * Get the supplier this arm memory object is recording data about
	 * @return The supplier this arm memory object is recording data about
	 */
	public Supplier getSupplier()
	{
		return supplier;
	}

	/**
	 * Get the index of this arm in the ArrayList of suppliers
	 * @return The index of this arm in the ArrayList of suppliers
	 */
	public int getIndex()
	{
		return index;
	}

	@Override
	public int compareTo(ArmMemory that)
	{
		double thisMean = this.getMeanTime();
		double thatMean = that.getMeanTime();
		
		// sorted by decreasing average time (increasing utility)
		if (thisMean < thatMean) return 1;
		else if (thisMean > thatMean) return -1;
		else // the ranks are the same, fall back to index
		{
			double thisIndex = this.index;
			double thatIndex = that.index;
			if (thisIndex < thatIndex) return -1;
			else if (thisIndex > thatIndex) return 1;
			else return 0;
		}
	}
	
	/**
	 * Returns a String representation of the mean of this ArmMemory
	 */
	@Override
	public String toString()
	{
		return String.format("%.2f", getMeanTime());
	}

	/**
	 * Eclipse generated this method, but it is slightly silly when it is only
	 * based off of one field. This method is (currently) unused, so I left it
	 * in it's silly form because it is humorous.
	 * @return <code>{@link #index} + 31</code>
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + index;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		ArmMemory other = (ArmMemory) obj;
		if (index != other.index) return false;
		return true;
	}
	
	/**
	 * Get the collection of samples from this arm.
	 * You'd better not modify the list this returns,
	 * or Lucas will be very disappointed in you.
	 * @return the collection of samples from this arm
	 */
	public SortedList<Double> getSamples()
	{
		return samples;
	}
	
	/**
	 * Calculate the probability of samples from the this arm occurring in given arm's 
	 * distribution. This is done by finding the number of ways that samples from the
	 * "other" arm can be placed into the set of samples of the "best" arm. This is 
	 * equivalent to finding the number of subsets of "best" that satisfy the following
	 * condition: each sample s_i in "other" is paired with a sample from "best" that is
	 * equal to or worse than s_i. Samples from best cannot be reused in another pairing. 
	 * <br /><br />
	 * In order to perform this calculation, for each sample in "other" the number of samples
	 * at least as bad in "best" are found. From this number, the number of samples already paired
	 * is subtracted to yield the number of remaining possible pairings for sample s_i. Let's call
	 * this result sp_i. The product of the sequence of terms sp_0..sp_n is the number of possible
	 * placements of "other" into "best". Let's call this value P.
	 * <br /><br />
	 * Finally, we must calculate the probability of this placement occurring, which would be the number
	 * of valid placements divided by number of total placements.  Total placements (PTotal) is equal to
	 * C( |best| , |other| ), where C is the binomial function (combinations). The returned value is
	 * simply P / PTotal.
	 * 
	 * @param bestArmMemory The Arm memory to check against
	 * @return the probability of samples from the this arm occurring in given arm's 
	 * distribution
	 */
	public double probabilityOfSamplesOccurring(ArmMemory bestArmMemory)
	{
		/* We only need to operate if the result is not cached, or would be different
		 * from the currently cached result. Caching is used here because it is
		 * expensive to calculate the return value, but it is used multiple times
		 * without actually changing the input (which in this case is the set of samples).
		 */
		if (!probabilityCached || !bestArmMemory.equals(oldBest))
		{	
			// samples of best arm
			final SortedList<Double> best = bestArmMemory.getSamples();
			
			// samples of some other arm
			final SortedList<Double> other = samples;
			
			/* if best has less samples than other, our method will not work as you cannot,
			 * for example, make a combination of 4 objects taken 5 at a time
			 */
			if (best.size() < other.size())
			{
				// the logical result to return here is 0, as no combinations exist
				return 0;
			}
			
			/* This is a multiplicative accumulator (*= instead of +=) and is therefore initialized to 1
			 * This store the number of combinations 
			 */
			int accumulator = 1;
			
			// for each sample in order from worst to best
			for (int sample = 0; sample < other.size(); sample++)
			{
				// first, find the number of samples worse than the current sample
				
				/* returns the last index of the value we searched for if it exists, otherwise
				 * returns the index of where it would have been
				 */
				int worseSamples = best.lastIndexOf(other.get(sample));
				
				// adjust value if needed
				if (worseSamples < best.size() && Double.compare(best.get(worseSamples), other.get(sample)) == 0)
				{
					worseSamples++; // include the indexed value if is the same as the one we're looking for
				}
				
				/* The next term to multiply into the accumulator
				 * 
				 * This value cannot be negative unless it has already been zero in a previous
				 * loop, in which case we returned early. So it cannot be negative, ever.
				 */
				final int term = worseSamples - sample; System.out.printf("(%d - %d)", worseSamples, sample);
				
				// if there is a zero term, there is no need to keep going
				if (term == 0)
				{
					accumulator = 0;
					break;
				}
				
				// update the accumulator
				accumulator *= term;
			}
			System.out.println();
			
			// BigDecimals are used as the binomial result could be quite large
			BigDecimal worseCombinations = new BigDecimal(accumulator);
			BigDecimal totalCombinations = new BigDecimal(binomial(best.size(), other.size()));
			
			final int precision = 5; // the precision of the division operation, in number of decimal places
			
			// worseCombinations / totalCombination
			final BigDecimal probability = worseCombinations.divide(totalCombinations, precision, BigDecimal.ROUND_HALF_EVEN);
			
			// cache the results for possible reuse
			oldBest = bestArmMemory;
			cachedProbability = probability.doubleValue();
			probabilityCached = true;
			
			assert cachedProbability <= 1.0:"Over 100% doesn't make sense: " + worseCombinations + "/" + totalCombinations;
		}
		
		// return the cached probability
		return cachedProbability;
	}
	
	/**
	 * Calculate nCr, or C(n, r)
	 * @param N Number of elements
	 * @param K Take K elements at a time
	 * @return Number of combinations C(n, k)
	 */
	private static BigInteger binomial(final int N, final int K)
	{
		// sanitize parameters
		if (N < K)
		{
			throw new IllegalArgumentException(String.format("N=%d K=%d, N < K", N, K));
		}
		
	    BigInteger ret = BigInteger.ONE;
	    for (int k = 0; k < K; k++) {
	        ret = ret.multiply(BigInteger.valueOf(N-k))
	                 .divide(BigInteger.valueOf(k+1));
	    }
	    return ret;
	}
}
