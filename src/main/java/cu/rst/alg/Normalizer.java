package cu.rst.alg;

import java.util.Set;

import cu.rst.graph.*;

public class Normalizer 
{
	public Normalizer()
	{
		
	}
	
	public Graph execute(Graph g) throws Exception
	{
		if(!(g instanceof RG))
		{
			throw new Exception("Input graph must be of type RG.");
		}
		
		RG rg = (RG)g;
		RG outputGraph = new RG();
		
		for(Agent src : (Set<Agent>)rg.vertexSet())
		{
			//normalize over the outgoing edges
			double totalRep = 0;
			for(ReputationEdge re : (Set<ReputationEdge>)rg.outgoingEdgesOf(src))
			{
				totalRep = totalRep + rg.getEdgeWeight(re);
			}
			
			for(ReputationEdge re : (Set<ReputationEdge>)rg.outgoingEdgesOf(src))
			{
				double newWeight = (totalRep>0)? (rg.getEdgeWeight(re) / totalRep) : 0;
				outputGraph.addEdge(src, (Agent) re.sink, rg.getEdgeWeight(re) / totalRep);
			}
		}
		
		return outputGraph;
	}

}
