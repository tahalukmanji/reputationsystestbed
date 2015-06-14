package cu.rst.graph;

import java.util.ArrayList;

import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.DirectedPseudograph;

/**
 * @author partheinstein
 *
 */
public abstract class Graph<V, E> extends DirectedPseudograph
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/*
	 *  FHG = Feedback History Graph
	 *  RG = Reputation Graph
	 *  TG = Trust Graph
	 *  PN = Petri Net
	 */
	public static enum Type{FHG, RG, TG, PN};
	public int m_id;
	private static int ID;
	
	
	ArrayList observers; 
	public Graph(EdgeFactory ef)
	{
		super(ef);
		m_id = ID++;
		observers = new ArrayList();
	}

	public abstract Graph<V, E> clone(boolean addObservers);
	public abstract Graph<V, E> getTransitiveClosureGraph();
	public void removeAllObservers()
	{
		observers.clear();
	}
	
	
	public String getName()
	{
		return getClass().getSimpleName() + m_id;
	}
	
	public static void resetCounter()
	{
		ID = 0;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if(!(o instanceof Graph)) return false;
		Graph g = (Graph)o;
		if(this.m_id == g.m_id) return true;
		return false;
				
	}
	}
