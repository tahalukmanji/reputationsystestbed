package cu.rst.graph;


public class TrustEdge extends TestbedEdge
{
	public TrustEdge(Agent src, Agent sink)
	{
		super.src = src;
		super.sink = sink;
	}
	
	@Override
	public String toString()
	{
//		return "Edge: (" + src + ", " + sink + ")";
		return "";
	}

}
