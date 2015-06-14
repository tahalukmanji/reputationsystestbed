package cu.rst.graph;

import java.io.Serializable;

import org.jgrapht.graph.DefaultWeightedEdge;

public abstract class TestbedEdge extends DefaultWeightedEdge implements Cloneable, Serializable
{
	private static final long serialVersionUID = 3258408452177932855L;
	public Object src, sink;
	
    /**
     * @see Object#clone()
     */
    public Object clone()
    {
        return super.clone();
    }
}
