package cu.rst.graph;

import java.util.ArrayList;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * This class models a Reputation graph.
 * @author pchandra
 *
 */
public class RG extends Graph<Agent, ReputationEdge>
{

	private static final long serialVersionUID = 2768260651851459417L;
	static Logger logger = Logger.getLogger(RG.class.getName());
		
	public RG(ReputationEdgeFactory reputationEdgeFactory)
	{
		super(reputationEdgeFactory);
	}
	
	public RG()
	{
		super(new ReputationEdgeFactory());
	}
	
	public void addEdges(ArrayList<ReputationEdge> repEdges)
	{
		for(ReputationEdge re : repEdges)
		{
			addEdge((Agent)re.src, (Agent)re.sink, re.getReputation());
		}
	}
	
	/**
	 * Adds the edge if it doesn't exist already. If it exists, the edge weight is updated.
	 * @param src
	 * @param sink
	 * @param reputation
	 */
	public void addEdge(Agent src, Agent sink, double reputation)
	{
		if(!this.containsVertex(src)) this.addVertex(src);
		if(!this.containsVertex(sink)) this.addVertex(sink);
			
		ReputationEdge repEdge = null;
		if(super.containsEdge(src, sink))
		{
			 repEdge = (ReputationEdge) super.getEdge(src, sink);			
		}else
		{
			repEdge = new ReputationEdge(src, sink);
			addEdge(src, sink, repEdge);
		}
		repEdge.setReputation(reputation);
		setEdgeWeight(repEdge, reputation);
		
	}
	

	@Override
	public RG clone(boolean addObservers)
	{
		RG clone = new RG(new ReputationEdgeFactory());
		Set<ReputationEdge> edges = this.edgeSet();
		Set<Agent> agents = this.vertexSet();
		
		for(Agent a : agents)
		{
			clone.addVertex(a); //not copying the agent
		}
		for(ReputationEdge e : edges)
		{
			clone.addEdge((Agent)e.src, (Agent)e.sink, e.getReputation());
		}
		
		return clone;
	}
	
	@Override
	public RG getTransitiveClosureGraph()
	{
//		ReputationGraph temp = this.clone(false);
//		TransitiveClosure.INSTANCE.closeSimpleDirectedGraph(temp);
//		return temp;
		return null;
	}
	
	public RG getTransitiveClosureGraph(FHG f)
	{
//		ReputationGraph temp = new ReputationGraph(new ReputationEdgeFactory());
//		Set<FeedbackHistoryGraphEdge> edges = f.edgeSet();
//		for(FeedbackHistoryGraphEdge edge : edges)
//		{
//			Agent src = (Agent)edge.src;
//			Agent sink = (Agent)edge.sink;
//			temp.addVertex(src);
//			temp.addVertex(sink);
//			temp.addEdge(src, sink);
//		}
//		TransitiveClosure.INSTANCE.closeSimpleDirectedGraph(temp);
//		return temp;
		return null;
	}
	
	@Override
	public String toString()
	{
		String temp = null;
		temp = "Reputation Graph" + System.getProperty("line.separator");
		temp += "Vertices:" + System.getProperty("line.separator");
		for(Agent a : (Set<Agent>) super.vertexSet())
		{
			temp += a + ", ";
		}
		temp += System.getProperty("line.separator") + "Edges:" + System.getProperty("line.separator");
		for(ReputationEdge e : (Set<ReputationEdge>) super.edgeSet())
		{
			temp += e.toString() + ", ";
		}	
		return System.getProperty("line.separator") + temp;
	}

		

}
