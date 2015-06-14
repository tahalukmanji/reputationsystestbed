package cu.rst.graph;

import org.jgrapht.EdgeFactory;

public class TrustEdgeFactory implements EdgeFactory<Agent, TrustEdge>
{

	@Override
	public TrustEdge createEdge(Agent src, Agent sink)
	{
		
		return new TrustEdge(src, sink);
	}

}
