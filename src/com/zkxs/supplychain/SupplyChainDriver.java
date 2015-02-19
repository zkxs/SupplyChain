package com.zkxs.supplychain;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Random;

import org.apache.commons.math3.distribution.*;

/* to-do list
 * TODO: Give all algorithms getName() method
 * TODO: Give all algorithms an init() method that the agent calls only once before getNextArm() is ever called
 * TODO: Give agents a getInitialBudget() (either absolute or an estimate) for the algorithms to look at
 * TODO: actually implement greedy
 */


/**
 * Drives the supply chain simulation by creating a tree of
 * suppliers with the non-leaf nodes being agents and the leaf nodes
 * being random number generators with a Gaussian distribution of specified
 * mean and standard deviation.  The goal is to minimize time, and at each
 * step in the supply chain additional processing time is accrued.
 * @author Michael Ripley (<a href="mailto:michael-ripley@utulsa.edu">michael-ripley@utulsa.edu</a>) Jan 26, 2014
 */
public class SupplyChainDriver
{	
	public static final double COST = 1.0;                  // DEFAULT: 1
	private static final double MEAN_TIME_MINIMUM = 10;     // DEFAULT: 10
	private static final double MEAN_TIME_INCREMENT = 0.25;   // DEFAULT: 10
	
	/**
	 * The superlinearity of the arms. A factor of 1 is linear. Greater than
	 * 1 is superlinear. (3 works well). Less than 1 and greater than 0 is sublinear.
	 * (1/3) works well.
	 */
	private static final double SUPER_FACTOR = 1. / 3.;      // DEFAULT: 3
	
	/** The number of children the root node has */
	private static final int ROOT_CHILDREN = 20;             // DEFAULT: 10
	
	/** The number of children the non-root nodes have */
	private static final int NONROOT_CHILDREN = 5;          // DEFAULT: 10
	
	/** Tree height including root node */
	private static final int TREE_DEPTH = 2;                // DEFAULT: 4
	
	/** If <code>true</code>, always use the child algorithm for child nodes */
	private static boolean fallbackOverride = false;
	
	final static Algorithm fallbackAlgorithm = new AlgorithmLSplit(2);
	
	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException
	{
		String fileLabel = "TEST";
//		String fileLabel = "~new_budget50-500_stddev20_branch10-10_terraced";
//		String fileLabel = "~new_stddev1-50_budget200_branch10-5_linear";
		
		//FIXME: parameters
		
		// Parameters
		final int trials = 500;
		double budget = 200;			// CHANGEME
		double scale = 10; 	// CHANGEME
//		final RealDistribution distribution = new BetaDistribution(0.5, 0.5); // lucas distribution
		final RealDistribution distribution = new NormalDistribution(); // normal distribution
//		final RealDistribution distribution = new UniformRealDistribution(); // square distribution
//		final RealDistribution distribution = new ChiSquaredDistribution(4); // 4 distribution?
//		final RealDistribution distribution = new BetaDistribution(1, 2); // triangle
//		final RealDistribution distribution = new BetaDistribution(1.5, 3); // skewed left
		
		if (args.length >= 2)
		{
			fileLabel = args[0];
			try
			{
				scale = Double.parseDouble(args[1]);
			}
			catch (NumberFormatException e)
			{}
		}
		
		// set up file writing
		PrintStream fileSummary = new PrintStream("output_" + fileLabel + ".txt", "UTF-8");
		PrintStream fileDynamic = new PrintStream("output_" + fileLabel + "_dynamic.txt", "UTF-8");
		PrintStream fileStatic  = new PrintStream("output_" + fileLabel + "_static.txt" , "UTF-8");
		
		final PrintStream[] streams = {System.out, fileSummary};
		
		
		final Algorithm[] dynamicAlgorithms = {
				new AlgorithmSOAAV(0),
				new AlgorithmLSplit(2),
				new AlgorithmExampleRandom(),
				new AlgorithmExampleArbitraryExploitation(),
				new AlgorithmPEEF(ROOT_CHILDREN, budget, 0.25),
				new AlgorithmEpsilonFirst(budget, 0.25),
				new AlgorithmKDE(budget, 0.25),
				new AlgorithmGreedy()
				//new AlgorithmConfidenceBiasedGreedy(20) // was 5
		};
		
		final String[] dynamicAlgorithmNames = {"soaav", "l-split", "(random)", "(arbitrary)",
				"PEEF (.25)", "E-First (.25)", "KDE (.25)", "greedy", "greedy+"};
		
		final Algorithm[] staticAlgorithms = {
				new AlgorithmPEEF(ROOT_CHILDREN, budget, 0.25),
				new AlgorithmEpsilonFirst(budget, 0.25),
				new AlgorithmKDE(budget, 0.25)
		};
		
		final String[] staticAlgorithmNames = {"PEEF (.25)", "E-First (.25)", "KDE (.25)"};
		
		
		//FIXME: tree setup
		//		RandomProvider.rand.setSeed("deja vu".hashCode()); // static seed for testing		
		
		// linear
		AgentSupplier root = (AgentSupplier) constructTree(TREE_DEPTH, ROOT_CHILDREN, 0, dynamicAlgorithms[0], true,
				distribution, scale);
		
		// superlinear
//		AgentSupplier root = (AgentSupplier) constructTreeSuper(TREE_DEPTH, ROOT_CHILDREN, 0.0,
//				dynamicAlgorithms[0], true, standardDeviation);
		
//		 terraced
//		AgentSupplier root = (AgentSupplier) constructTreeTerraced(TREE_DEPTH, ROOT_CHILDREN, 0.0,
//				dynamicAlgorithms[0], true, standardDeviation, standardDeviation);
		
		// show level 2 of the tree
//		Util.printIterable(root.getChildren());
//		System.out.println();
		
		// begin big for loop that runs the 1000 trials each time
		boolean firstLoop = true;
		//FIXME: independant variable
		for (scale = 1; scale <= 50.7; scale += 1.6)
		{
			fileDynamic.print(scale);
			fileStatic.print(scale);
//		for (budget = 50; budget <= 521; budget += 22)
//		{
//			fileDynamic.print(budget);
//			fileStatic.print(budget);
			
			
			// print trial information:
			for (int i = 0; i <= (firstLoop?1:0); i++)
			{
				PrintStream stream = streams[i];
				
				stream.printf("%d algorithms loaded, %d trials per algorithm\n",
						dynamicAlgorithms.length + staticAlgorithms.length, trials);
				stream.printf("Tree setup:\n");
				stream.printf("    Tree has %d levels, including the root node\n", 
						TREE_DEPTH);
				stream.printf("    Root node has %d children\n", 
						ROOT_CHILDREN);
				stream.printf("    Other nodes have %d children\n", 
						NONROOT_CHILDREN);
				stream.printf("    Total nodes = %d\n",
						(1 - (int)Math.pow(NONROOT_CHILDREN, TREE_DEPTH - 1)) 
						/ (1 - NONROOT_CHILDREN) * ROOT_CHILDREN + 1);
				stream.printf("    Total agents = %d\n",
						(1 - (int)Math.pow(NONROOT_CHILDREN, TREE_DEPTH - 2)) 
						/ (1 - NONROOT_CHILDREN) * ROOT_CHILDREN + 1);
				stream.printf("    Total leaf-suppliers = %d\n", 
						(int)Math.pow(NONROOT_CHILDREN, TREE_DEPTH - 2) * ROOT_CHILDREN);
				stream.printf("    Arm averages start at %.1f and are %.1f apart.\n", 
						MEAN_TIME_MINIMUM, MEAN_TIME_INCREMENT);
				stream.printf("    The scale of all arm pulls is %.1f\n", 
						scale);
				stream.printf("    The root node has an initial budget of %.1f.\n", 
						budget);
				stream.printf("    All arms cost %.1f to pull.\n\n", COST);
			}
			
			// print algorithm names in summary
			for (int i = 0; i < dynamicAlgorithms.length + staticAlgorithms.length; i++)
			{
				if (i < dynamicAlgorithms.length)
				{
					fileSummary.printf("Algorithm %2d: %15s\n", i + 1, dynamicAlgorithmNames[i]);
				}
				else
				{
					fileSummary.printf("Algorithm %2d: %15s\n", i + 1, staticAlgorithmNames[i - dynamicAlgorithms.length]);
				}
			}
			
			/*
			System.out.println("Begin homogenous trial:");
			// homogeneous trial (all nodes using the same algorithm)
			fallbackOverride = false;
			for (int algorithmNumber = 0; algorithmNumber < dynamicAlgorithms.length; algorithmNumber++)
			{
				System.out.printf("Algorithm %2d: %-15s", algorithmNumber + 1, dynamicAlgorithmNames[algorithmNumber]);
				
				double timeTaken = 0;
				
				for (int trial = 0; trial < trials; trial++)
				{
					// reset the tree for consistency
					if (trial == 0) // new trial, new algorithm
					{
						resetTree(root, dynamicAlgorithms[algorithmNumber], distribution, scale);
					}
					else // implies running trial
					{
						// no need to change algorithms
						resetTree(root, null, distribution, scale);
					}
					
					assert root.algorithm.getClass().equals(dynamicAlgorithms[algorithmNumber].getClass()):
						String.format("\nexpected %s, but got %s.\ntrial=%d\n",
								dynamicAlgorithms[algorithmNumber].getClass().getName(),
								root.algorithm.getClass().getName(),
								trial);
					
					// run the trial
					root.explore(budget);
					
					// keep running sum of average time
					timeTaken += root.getTotalTimeTaken();
				}
				
				fileDynamic.print("\t" + (timeTaken / trials));
				System.out.printf("    Average time taken: %.2f\n", timeTaken / trials);
			} // end homogeneous trial
			
			System.out.println();
			*/
			
			System.out.println("Begin static w/ fallback trial:");
			
			// dynamic algorithms with fallback (children use a dynamic algorithm)
			fallbackOverride = true;
			for (int algorithmNumber = 0; algorithmNumber < dynamicAlgorithms.length; algorithmNumber++)
			{
				System.out.printf("Algorithm %2d: %-15s", algorithmNumber + 1, dynamicAlgorithmNames[algorithmNumber]);
				
				double timeTaken = 0;
				
				for (int trial = 0; trial < trials; trial++)
				{
					// reset the tree for consistency
					if (trial == 0) // new trial, new algorithm
					{
						resetTree(root, dynamicAlgorithms[algorithmNumber], distribution, scale);
					}
					else // implies running trial
					{
						// no need to change algorithms
						resetTree(root, null, distribution, scale);
					}
					
					// run the trial
					root.explore(budget);
					
					// keep running sum of average time
					timeTaken += root.getTotalTimeTaken();
				}
				
				fileStatic.print("\t" + (timeTaken / trials));
				System.out.printf("    Average time taken: %.2f\n", timeTaken / trials);
			} // end dynamic algorithms with fallback
			
			/*
			// static algorithms with fallback (children use a dynamic algorithm)
			//fallbackOverride = false;
			for (int algorithmNumber = 0; algorithmNumber < staticAlgorithms.length; algorithmNumber++)
			{
				System.out.printf("Algorithm %2d: %-15s",
						algorithmNumber + dynamicAlgorithms.length + 1, staticAlgorithmNames[algorithmNumber]);
				
				double timeTaken = 0;
				
				for (int trial = 0; trial < trials; trial++)
				{
					// reset the tree for consistency
					if (trial == 0) // new trial, new algorithm
					{
						resetTree(root, staticAlgorithms[algorithmNumber], distribution, scale);
					}
					else // implies running trial
					{
						// no need to change algorithms
						resetTree(root, null, distribution, scale);
					}
					
					// run the trial
					root.explore(budget);
					
					// keep running sum of average time
					timeTaken += root.getTotalTimeTaken();
				}
				
				fileStatic.print("\t" + (timeTaken / trials));
				System.out.printf("    Average time taken: %.2f\n", timeTaken / trials);
			} // end static algorithms with fallback
			*/
			System.out.print("\n\n");
			fileDynamic.println();
			fileStatic.println();
			
			if (firstLoop)
			{
				fileSummary.close();
				firstLoop = false;
			}
		} // end giant for loop
		
		
		fileDynamic.close();
		fileStatic.close();
	}
	
	/**
	 * Recursively construct a tree
	 * @param treeSize height of tree to generate, including the root node
	 * @param numChildren number of children this layer should have
	 * @param meanIncrementMultiplier amount to increment child average by
	 * @param algorithm The algorithm to use in this tree
	 * @param isRoot Should always be <code>true</code>. Tells the recursive function
	 * @param standardDeviation The standard deviation all arms in this tree are to have
	 * that this call was the initial call.
	 * @return The root node of the new tree
	 */
	public static Supplier constructTree(int treeSize, int numChildren, int meanIncrementMultiplier, Algorithm algorithm, boolean isRoot, RealDistribution distribution, double scale)
	{
		if (treeSize == 1) // base case, leaf node
		{
			return new SimpleSupplier(COST, MEAN_TIME_MINIMUM
					+ meanIncrementMultiplier * MEAN_TIME_INCREMENT, distribution, scale);
		}
		else
		{
			// create children
			ArrayList<Supplier> childrenOrdered = new ArrayList<Supplier>(numChildren);
			for (int i = 0; i < numChildren; i++)
			{
				Supplier child = constructTree(treeSize - 1, NONROOT_CHILDREN, i,
						//(algorithm.requiresInitialBudget() || fallbackOverride) ?
						(fallbackOverride) ?
						fallbackAlgorithm : algorithm, false, distribution, scale);
				
				if (i == 0) child.setBestArm(true);
				childrenOrdered.add(child);
			}
			
			// scramble children order
			Random rand = RandomProvider.rand; // shared source of randomness
			ArrayList<Supplier> childrenScrambled = new ArrayList<Supplier>(numChildren);
			while (!childrenOrdered.isEmpty())
			{
				childrenScrambled.add(childrenOrdered.remove(rand.nextInt(childrenOrdered.size())));
			}
			
			/*
			 * Create and return root node.
			 * Note that the budgetMultiplier does not affect the root node as it is taken into
			 * account in the supply() method, which is never called on the root node.
			 */
			return new AgentSupplier(algorithm.duplicate(), childrenScrambled, COST, MEAN_TIME_MINIMUM
					+ meanIncrementMultiplier * MEAN_TIME_INCREMENT, distribution, scale, numChildren, isRoot);
		}
	}
	
	/**
	 * Recursively construct a tree
	 * @param treeSize height of tree to generate, including the root node
	 * @param numChildren number of children this layer should have
	 * @param meanTime mean time this root node will take to pull
	 * @param algorithm The algorithm to use in this tree
	 * @param isRoot Should always be <code>true</code>. Tells the recursive function
	 * @param standardDeviation The standard deviation all arms in this tree are to have
	 * that this call was the initial call.
	 * @return The root node of the new tree
	 */
	public static Supplier constructTreeSuper(int treeSize, int numChildren, double meanTime,
			Algorithm algorithm, boolean isRoot, RealDistribution distribution, double scale)
	{
		if (treeSize == 1) // base case, leaf node
		{
			return new SimpleSupplier(COST, meanTime, distribution, scale);
		}
		else
		{
			// create children
			ArrayList<Supplier> childrenOrdered = new ArrayList<Supplier>(numChildren);
			for (int i = 0; i < numChildren; i++)
			{
				Supplier child = constructTreeSuper(treeSize - 1, NONROOT_CHILDREN,
						
						// min + max * (i / (num - 1)) ^ super
						MEAN_TIME_MINIMUM + (numChildren - 1) * MEAN_TIME_INCREMENT 
						* Math.pow((double)i / (numChildren - 1), SUPER_FACTOR),
						
						//(algorithm.requiresInitialBudget() || fallbackOverride) ?
						(fallbackOverride) ?
						fallbackAlgorithm : algorithm, false, distribution, scale);
				
				if (i == 0) child.setBestArm(true);
				childrenOrdered.add(child);
			}
			
			// scramble children order
			Random rand = RandomProvider.rand; // shared source of randomness
			ArrayList<Supplier> childrenScrambled = new ArrayList<Supplier>(numChildren);
			while (!childrenOrdered.isEmpty())
			{
				childrenScrambled.add(childrenOrdered.remove(rand.nextInt(childrenOrdered.size())));
			}
			
			/*
			 * Create and return root node.
			 * Note that the budgetMultiplier does not affect the root node as it is taken into
			 * account in the supply() method, which is never called on the root node.
			 */
			return new AgentSupplier(algorithm.duplicate(), childrenScrambled, COST, meanTime, distribution, scale, numChildren, isRoot);
		}
	}

	/**
	 * Recursively construct a tree
	 * @param treeSize height of tree to generate, including the root node
	 * @param numChildren number of children this layer should have
	 * @param meanTime mean time this root node will take to pull
	 * @param algorithm The algorithm to use in this tree
	 * @param isRoot Should always be <code>true</code>. Tells the recursive function
	 * @param baseStandardDeviation The standard deviation all arms in this tree are to have
	 * that this call was the initial call.
	 * @param standardDeviation The standard deviation this node will have
	 * @return The root node of the new tree
	 */
	/*public static Supplier constructTreeTerraced(int treeSize, int numChildren, double meanTime,
			Algorithm algorithm, boolean isRoot, double baseStandardDeviation, double standardDeviation)
	{
		if (treeSize == 1) // base case, leaf node
		{
			return new SimpleSupplier(COST, meanTime, standardDeviation);
		}
		else
		{
			// create children
			ArrayList<Supplier> childrenOrdered = new ArrayList<Supplier>(numChildren);
			for (int i = 0; i < numChildren; i++)
			{
				double newMeanTime;
				double newStandardDeviation;
				if (i == 0)
				{	// 0th terrace (best)
					newMeanTime = MEAN_TIME_MINIMUM;
					newStandardDeviation = 0;
				}
				else if (i < (numChildren + 1) / 2)
				{	// 1st terrace (good)
					newMeanTime = MEAN_TIME_MINIMUM + MEAN_TIME_INCREMENT * 0.5;
					newStandardDeviation = baseStandardDeviation;
				}
				else
				{	// 2nd terrace (worst)
					newMeanTime = MEAN_TIME_MINIMUM + MEAN_TIME_INCREMENT * 1.5;
					newStandardDeviation = baseStandardDeviation;
				}
				
				Supplier child = constructTreeTerraced(treeSize - 1, NONROOT_CHILDREN, newMeanTime,
						//(algorithm.requiresInitialBudget() || fallbackOverride) ?
						(fallbackOverride) ?
						fallbackAlgorithm : algorithm, false, baseStandardDeviation, newStandardDeviation);
				
				if (i == 0) child.setBestArm(true);
				childrenOrdered.add(child);
			}
			
			// scramble children order
			Random rand = RandomProvider.rand; // shared source of randomness
			ArrayList<Supplier> childrenScrambled = new ArrayList<Supplier>(numChildren);
			while (!childrenOrdered.isEmpty())
			{
				childrenScrambled.add(childrenOrdered.remove(rand.nextInt(childrenOrdered.size())));
			}
			
			/*
			 * Create and return root node.
			 * Note that the budgetMultiplier does not affect the root node as it is taken into
			 * account in the supply() method, which is never called on the root node.
			 * /
			return new AgentSupplier(algorithm.duplicate(), childrenScrambled, COST, meanTime, standardDeviation, numChildren, isRoot);
		}
	}*/

	/**
	 * Reset all of the agents in this tree so that it can be used in another run
	 * @param root the root node of this tree
	 * @param newAlgorithm the new algorithm to use, or <code>null</code> to keep
	 * the current algorithm
	 */
	public static void resetTree(Supplier root, Algorithm newAlgorithm, RealDistribution distribution, double scale)
	{
		// reset this node if it is an agent
		if (root instanceof AgentSupplier)
		{
			AgentSupplier rootAgent = (AgentSupplier)root;
			
			if (newAlgorithm == null)
				rootAgent.reset(distribution, scale);
			else
				rootAgent.reset(distribution, scale, newAlgorithm.duplicate());
		}
		
		// reset all children
		if (!root.isLeafNode()) // base case
		{
			// for all children of root
			for (Supplier child: root.getChildren())
			{
				resetTree(child,
						(newAlgorithm != null && 
						//(newAlgorithm.requiresInitialBudget() || fallbackOverride)) ?
						(fallbackOverride)) ?
						fallbackAlgorithm : newAlgorithm, distribution, scale); // reset them as well
			}
		}
	}
}
