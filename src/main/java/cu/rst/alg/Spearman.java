package cu.rst.alg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

import cu.rst.graph.*;
import cu.rst.util.Util;

public class Spearman
{
	private class RepElement 
	{
		public Agent agent;
		public double reputation;
		public RepElement(Agent agent, double reputation)
		{
			this.agent = agent;
			this.reputation = reputation;
		}
		
	}
	
	private class RepElementComparator implements Comparator<RepElement>
	{
		
		public int compare(RepElement o1, RepElement o2) 
		{
			if(o1.reputation == o2.reputation) return 0;
			if(o1.reputation > o2.reputation) return 1;
			else return -1;
		}
	}
	
	public void print(ArrayList<RepElement> res)
	{
		for(RepElement re : res)
		{
			System.out.println(re.agent + ", " + re.reputation);
		}
	}
	
	private int getPosition(ArrayList<RepElement> res, Agent a)
	{
		for(int i=0;i<res.size();i++)
		{
			if(res.get(i).agent.equals(a)) return i; 
		}
		return -1; //not found
	}


	public double execute(Graph g1, Graph g2) throws Exception 
	{

		Util.assertNotNull(g1);
		Util.assertNotNull(g2);
		
		if(!(g1 instanceof RG) || !(g2 instanceof RG))
		{
			throw new Exception("Input graphs must be of type RG");
		}
		
		RG rg1 = (RG) g1;
		RG rg2 = (RG) g2;
		
		//assuming global reputation (all incoming edges have the same weight)
		ArrayList<RepElement> rg1Agents = new ArrayList<RepElement>();
		for(Agent a : (Set<Agent>) rg1.vertexSet())
		{
			Set<ReputationEdge> incomingEdges = (Set<ReputationEdge>) rg1.incomingEdgesOf(a);
			if(incomingEdges!=null && incomingEdges.size()>1)
			{
				rg1Agents.add(new RepElement(a, ((ReputationEdge) incomingEdges.toArray()[0]).getReputation()));
			}
		}
		
		//assuming global reputation (all incoming edges have the same weight)
		ArrayList<RepElement> rg2Agents = new ArrayList<RepElement>();
		for(Agent a : (Set<Agent>) rg2.vertexSet())
		{
			Set<ReputationEdge> incomingEdges = (Set<ReputationEdge>) rg2.incomingEdgesOf(a);
			if(incomingEdges!=null && incomingEdges.size()>1)
			{
				rg2Agents.add(new RepElement(a, ((ReputationEdge) incomingEdges.toArray()[0]).getReputation()));
			}
		}
		
		//print(rg1Agents);
		Collections.sort(rg1Agents, new RepElementComparator());
		//System.out.println("-");
		//print(rg1Agents);
		//System.out.println("-");
		
		//print(rg2Agents);
		Collections.sort(rg2Agents, new RepElementComparator());
		//System.out.println("-");
		//print(rg2Agents);
		
		int total = 0;
		for(int i=rg1Agents.size(); i>0; i--)
		{
			total = total + i;
		}
		double avgRank = total / rg1Agents.size(); // assuming rg1.size == rg2.size
		
		double numerator = 0, denominator1 = 0, demoninator2 = 0;
		for(Agent a : (Set<Agent>) rg1.vertexSet())
		{
			int xi = getPosition(rg1Agents, a);
			int yi = getPosition(rg2Agents, a);
			numerator = numerator + (xi - avgRank) * (yi - avgRank);
		}
		
		for(Agent a : (Set<Agent>) rg1.vertexSet())
		{
			int xi = getPosition(rg1Agents, a);
			denominator1 = denominator1 + (xi - avgRank) * (xi - avgRank);
		}
		
		for(Agent a : (Set<Agent>) rg1.vertexSet())
		{
			int yi = getPosition(rg2Agents, a);
			demoninator2 = demoninator2 + (yi - avgRank) * (yi - avgRank);
		}
		
		double coeff = numerator/Math.sqrt(denominator1 * demoninator2);
		//System.out.println("Coeff: " + coeff);
		
		return coeff;
}

}

