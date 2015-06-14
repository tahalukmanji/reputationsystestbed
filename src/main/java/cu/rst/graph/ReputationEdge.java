package cu.rst.graph;

import java.util.ArrayList;

import cu.rst.util.Util;

public class ReputationEdge extends TestbedEdge implements Comparable
{

	private static final long serialVersionUID = -6027745106941999388L;
	private double reputation; //not used.
	private ArrayList<Double> m_reputationHistory; 
	
	public ReputationEdge(Agent src, Agent sink, double reputation)
	{
		super.src = src;
		super.sink = sink;
		this.setReputation(reputation);
	}
	
	public ReputationEdge(Agent src, Agent sink)
	{
		super.src = src;
		super.sink = sink;
		this.reputation = Double.MIN_VALUE;
	}

	public void setReputation(double reputation)
	{
		this.reputation = reputation;
		if(m_reputationHistory == null) m_reputationHistory = new ArrayList<Double>();
		m_reputationHistory.add(reputation);
	}

	public double getReputation()
	{
		return reputation;
	}
	
	public ArrayList<Double> getReputationHistory()
	{
		return m_reputationHistory;
	}

	@Override
	public int compareTo(Object arg0)
	{
		ReputationEdge otherEdge = (ReputationEdge)arg0;
		if(this.getWeight() < otherEdge.getWeight()) return -1;
		else if(this.getWeight() > otherEdge.getWeight()) return 1;
		else return 0;

	}
	
	@Override
	public String toString()
	{
//		return "(" + (Agent)src + ", " + (Agent)sink + "," + reputation + ")";
		return new Double(Util.round(reputation, 2)).toString();
	}
	
	@Override
	public boolean equals(Object o)
	{
		if(!(o instanceof ReputationEdge)) return false;
		ReputationEdge re = (ReputationEdge)o;
		return re.sink.equals(sink) && re.src.equals(src);
	}

}