package cu.rst.alg;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import cu.rst.graph.*;
import cu.rst.util.Util;

/**
 * This is an implementation of Cai Zeigler's Appleseed algorithm. It is also a
 * flow-based algorithm that calculates a trust score. Assuming that we are
 * given a directed weighted graph with agents as nodes, edges as trust
 * relationships, and weight of an edge as trustworthiness of the sink, we can
 * determine the amount of trust that flows in the graph. That is, given a trust
 * seed, an energy, spreading factor, and convergence threshold , Appleseed
 * returns a trust score of agents from the perspective of the trust seed.
 * 
 * @author partheinstein
 * 
 * 
 */
public class Appleseed implements AlgorithmIntf
{
	public static final int DEFAULT_NUM_ITERATIONS = 100;
	
	private final int numIterations;
	
	public Appleseed()
	{
		this.numIterations = DEFAULT_NUM_ITERATIONS;
	}
	
	public Appleseed(int numIterations)
	{
		this.numIterations = numIterations;
	}
	
	public double calculateTrustScore(Agent src, Agent sink, RG rg)
			throws Exception
	{
		double inEnergy = 1;
		double decay = 0.85;
		double threshold = 0.9;
		// returns {(agent, trust rank)}
		Hashtable<Agent, Double> trustRanks = trusts(src, inEnergy, decay,
				threshold, rg.vertexSet().size(), rg);
		// System.out.println(src);
		// System.out.println(sink);
		// System.out.println(trustRanks.get(sink));
		// System.out.println(trustRanks.get(new Agent(1)));
		// System.out.println(trustRanks.get(new Agent(2)));
		// System.out.println(trustRanks.get(new Agent(3)));
		//
		return trustRanks.containsKey(sink) ? (Double) trustRanks.get(sink)
				: -1;

	}

	private Hashtable<Agent, Double> trusts(Agent s, double inEnergy,
			double decay, double threshold, int totalNumAgents, RG rg)
			throws Exception
	{
		// each element corresponds to 'in' energies of agents for ith iteration
		ArrayList<Hashtable<Agent, Double>> in = new ArrayList<Hashtable<Agent, Double>>();
		// each element corresponds to trust ranks of agents for ith iteration
		ArrayList<Hashtable<Agent, Double>> trust = new ArrayList<Hashtable<Agent, Double>>();
		// already visited notes
		ArrayList<ArrayList<Agent>> v = new ArrayList<ArrayList<Agent>>();
		int i = 0;

		Hashtable<Agent, Double> temp = new Hashtable<Agent, Double>();
		temp.put(s, inEnergy);
		in.add(temp);

		temp = new Hashtable<Agent, Double>();
		temp.put(s, (double) 0);
		trust.add(temp);

		ArrayList<Agent> temp1 = new ArrayList<Agent>();
		temp1.add(s);
		v.add(temp1);

		do
		{
			i++;

			temp1 = new ArrayList<Agent>();
			temp1.addAll(v.get(i - 1));
			v.add(temp1);// V(i) = V(i-1)

			temp = new Hashtable<Agent, Double>();
			for (Agent a : v.get(i - 1))
			{
				temp.put(a, (double) 0);
			}
			in.add(temp); // for all x in V(i-1) : in(i)(x) = 0;

			trust.add(new Hashtable<Agent, Double>());

			for (Agent x : v.get(i - 1))
			{
				double temp2 = trust.get(i - 1).get(x) + (1 - decay)
						* in.get(i - 1).get(x);
				trust.get(i).put(x, temp2);

				double total = 0;
				Set<ReputationEdge> outgoingEdges = (Set<ReputationEdge>) rg
						.outgoingEdgesOf(x);
				for (ReputationEdge e : outgoingEdges)
				{
					total = total + e.getReputation();
				}

				for (ReputationEdge e : (Set<ReputationEdge>) rg
						.outgoingEdgesOf(x))
				{
					if (!Util.isPresent(v.get(i), e.sink))
					{
						v.get(i).add((Agent) e.sink);
						trust.get(i).put((Agent) e.sink, (double) 0);
						in.get(i).put((Agent) e.sink, (double) 0);
						// for some reason this adds '1' as the edge between
						// e.sink and s
						// rg.addEdge(e.sink, s, (double)1.0);
						ReputationEdge re = new ReputationEdge((Agent) e.sink,
								s);
						re.setReputation(1.0);
						rg.addEdge(e.sink, s, re);
						rg.setEdgeWeight(re, 1.0);
					}
					double w = e.getReputation() / total;
					temp2 = in.get(i).get(e.sink) + decay
							* in.get(i - 1).get(x) * w;
					in.get(i).put((Agent) e.sink, temp2);

				}
			}

		} while (i < numIterations);

		return trust.get(i);

	}



	
	public Graph<Agent, ReputationEdge> execute(Graph g) throws Exception
	{
		Util.assertNotNull(g);
		if(!(g instanceof RG))
		{
			throw new Exception("Input graph is not of type RG.");
		}
		
		RG rg0 = (RG) g;
		RG outputGraph = new RG();

		for (Agent src : (Set<Agent>) rg0.vertexSet())
		{
			for (Agent sink : (Set<Agent>) rg0.vertexSet())
			{
				// make a copy because this impl of AppleSeed destroys the
				// original input;
				RG rgTemp = rg0.clone(false);
				double rep = this.calculateTrustScore(src, sink, rgTemp);
				// rep = -1 if there at least no path from src to sink
				// rep = NaN if there is no path from src to any
				if (rep >= 0) // there is a path
				{
					outputGraph.addEdge(src, sink, rep);
				}
			}

		}

		return outputGraph;
	}

}
