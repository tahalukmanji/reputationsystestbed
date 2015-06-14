package cu.rst.alg;

import java.util.ArrayList;

import cu.rst.graph.*;
import cu.rst.graph.Graph;

public class Discretizer 
{
	private double threshold = 0.7;
	
	public Discretizer()
	{
		
	}
	
	public Graph execute(Graph g) throws Exception
	{
		if(!(g instanceof FHG))
		{
			throw new Exception("Input graph must be a FHG");
		}
		FHG fhg = (FHG)g;
		FHG outputGraph = new FHG();
		
		for(Feedback f : (ArrayList<Feedback>) fhg.m_feedbacks)
		{
			if(f.value >= threshold)
			{
				outputGraph.addFeedback(new Feedback(f.getAssesor(), f.getAssesee(), (double) 1));
			}
			else
			{
				outputGraph.addFeedback(new Feedback(f.getAssesor(), f.getAssesee(), (double) 0));
			}
		}
		
		return outputGraph;

	}

}
