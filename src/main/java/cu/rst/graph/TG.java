package cu.rst.graph;

import java.util.ArrayList;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * This class models a Trust graph.
 * @author pchandra
 *
 */
public class TG extends Graph<Agent, TrustEdge>
{

	private static final long serialVersionUID = -327490271972222723L;
	static Logger logger = Logger.getLogger(TG.class.getName());

	public TG(TrustEdgeFactory trustEdgeFactory)
	{
		super(trustEdgeFactory);
	}
	
	public TG()
	{
		super(new TrustEdgeFactory());
	}
	
	public void addTrustEdges(ArrayList<TrustEdge> trustEdges)
	{
		for(TrustEdge te : trustEdges)
		{
			addEdge(te.src, te.sink);
		}
	}

	@Override
	public String toString()
	{
		String temp = null;
		temp = "Trust Graph" + System.getProperty("line.separator");
		temp += "Vertices:" + System.getProperty("line.separator");
		for(Agent a : (Set<Agent>) super.vertexSet())
		{
			temp += a + ",";
		}
		temp += System.getProperty("line.separator") + "Edges:" + System.getProperty("line.separator");
		for(TrustEdge e : (Set<TrustEdge>) super.edgeSet())
		{
			temp += e.toString() + " ,";
		}	
		return System.getProperty("line.separator") + temp;
	}

	@Override
	public TG clone(boolean addObservers)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TG getTransitiveClosureGraph()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object addEdge(Object src, Object sink)
	{
		if(!this.containsVertex(src)) this.addVertex(src);
		if(!this.containsVertex(sink)) this.addVertex(sink);
		return super.addEdge((Object)src, (Object)sink);
	}	

}
