package cu.rst.alg;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

import cu.rst.graph.Agent;
import cu.rst.graph.FHG;
import cu.rst.graph.Feedback;
import cu.rst.graph.FeedbackHistoryGraphEdge;
import cu.rst.graph.Graph;
import cu.rst.graph.Graph.Type;
import cu.rst.graph.RG;
import cu.rst.graph.ReputationEdge;


/**
 * An implementation of "L. Xiong and L. Liu, Peertrust: Supporting reputation-based trust for peer-to-peer electronic communities," 
 * IEEE Transactions on Knowledge and Data Engineering, vol. 16, pp. 843-857, July 2004.
 * @author partheinstein
 *
 */
public class PeerTrust implements AlgorithmIntf
{
	
	Hashtable trustScores = new Hashtable(); //key is agent id, value is trust score. using hashtable to improve performance.
	private static double MINIMUM_TRUST_SCORE = 0.0;
	private static double MAXIMUM_TRUST_SCORE = 1.0;
	static Logger logger = Logger.getLogger(PeerTrust.class.getName());
	
	public PeerTrust()
	{
		
	}
	

	public double calculateTrustScoreInternal(Agent src, Agent sink, ArrayList nodesVisited, FHG fhg) throws Exception
	{

		//if hasn't been already added, then add it
		if(!this.nodeAlreadyVisited(nodesVisited, sink)) nodesVisited.add(sink); 
		
		//get all incoming edges of the given sink node
		Set<FeedbackHistoryGraphEdge> incomingEdges = fhg.incomingEdgesOf(sink);
		
		//nobody has had transactions with this sink, return a default trust score
		int numberOfAgentsInSystem = fhg.vertexSet().size();
		if(incomingEdges.isEmpty()) return (double)1/(double)numberOfAgentsInSystem;  
		
		
		//calculate the total trust score of all the recommenders
		//this is the denominator part to calculate the cr(p(u,i)
		double totalTrustScore = 0;
		ArrayList<Feedback> allFeedbacks = new ArrayList<Feedback>();
		for(FeedbackHistoryGraphEdge e : incomingEdges)
		{
			for(Feedback f : e.feedbacks)
			{
				allFeedbacks.add(f);
			}
			
		}
		
		for(Feedback f : allFeedbacks)
		{
			double temp = 0;
			
			/*
			 * loop poses a problem with peerTrust and the authors don't explain on how to solve them, so using heuristics here...
			 * bootstrapping: all peers have initial trust score of 0.5.
			 * Consider a  loop looks like this A<->B. You want to calculate the A's trust score. Getting the incoming edges of A contains B.
			 * Calculating B's trust score, you need to get the incoming edges of B which contains A. There is a loop here. As a heuristic, if
			 * we are in B and we have already visited the edge.source (i.e A) and we haven't calculated A's trust score, then set A's trust score = 0.5.
			 * Note that agent's trust score (once calculated) is stored in this.trustscores hashtable.
			 */
			
			if(nodeAlreadyVisited(nodesVisited, (Agent)f.getAssesor())) //note edge.src == feedback.src
			{		
				logger.debug("Already visited agent" + (Agent)f.getAssesor());
				if(this.trustScores.get(((Agent)f.getAssesor()).id)==null) 
					temp = (double)1/(double)numberOfAgentsInSystem;
				else
					temp = (Double)this.trustScores.get(((Agent)f.getAssesor()).id);
			}else
			{
				logger.debug("Never visited agent" + (Agent)f.getAssesor());
				//breadth-first
				temp = (Double)this.calculateTrustScoreInternal(null, (Agent)f.getAssesor(), nodesVisited, fhg); //recursive
			}
			
			totalTrustScore = totalTrustScore + temp;
		}
		//just a check
		if(totalTrustScore==0) throw new Exception("total trust score is 0. It should never be zero.");
			
		//now caclulate the trust score of the sink (passed in as a parameter)
		double trustScoreToBeCalculated = 0;
	
		Iterator it1 = incomingEdges.iterator();
		while(it1.hasNext())
		{
			FeedbackHistoryGraphEdge e = (FeedbackHistoryGraphEdge)it1.next();
			ArrayList experiences = e.feedbacks;
			//breadth-first
			double trustScore = 0;
			
			if(this.trustScores.get(((Agent)e.src).id)==null) 
			{
				//trustScore = (Double) this.calculateTrustScore(null, (Agent)e.src, nodesVisited); //recursive
				trustScore = (double)1/(double)numberOfAgentsInSystem; 
			
			}else
			{
				trustScore = (Double)this.trustScores.get(((Agent)e.src).id);
			}
			
			Iterator it2 = experiences.iterator();
			while(it2.hasNext())
			{
				/*
				 * t(u) = sum( s(u,i) * cr(p(u,i)) )
				 * cr(p(u,i)) = t(p(u,i)) / sum(p(u,i))
				 */
				Feedback exp = (Feedback)it2.next();
				trustScoreToBeCalculated = trustScoreToBeCalculated + exp.value * (trustScore/totalTrustScore);
			}
			
		}
		
		this.trustScores.put(sink.id, trustScoreToBeCalculated);
		return trustScoreToBeCalculated;
	}
	
	private boolean nodeAlreadyVisited(ArrayList nodesVisited, Agent b)
	{
		Iterator it = nodesVisited.iterator();
		while(it.hasNext())
		{
			Agent a = (Agent)it.next();
			if(a.id == b.id) return true;
		}
		return false;
	}



	public boolean assertGraph2ListenType(Graph g) throws Exception
	{
		if(!(g instanceof FHG)) return false;
		return true;
	}


	public boolean assertGraph2OutputType(Graph g) throws Exception
	{
		if(!(g instanceof RG)) return false;
		return true;
	}


	public boolean assertVariablePrecondition(double variable) throws Exception
	{
		if(variable < this.MINIMUM_TRUST_SCORE || variable > this.MAXIMUM_TRUST_SCORE)
		{
			return false;
		}
		return true;
	}



	public Type getInputGraphType() throws Exception
	{
		return Graph.Type.FHG;
	}



	public Type getOutputGraphType() throws Exception
	{
		return Graph.Type.RG;
	}


	public Graph execute(Graph g) throws Exception 
	{
		FHG fhg = (FHG) g;
		RG rg = new RG();
		for(Agent src : (Set<Agent>)fhg.vertexSet())
		{
			for(Agent sink : (Set<Agent>)fhg.vertexSet())
			{
				logger.debug("Calculating rep between " + src +  " and " + sink);
				double rep = calculateTrustScoreInternal(src, sink, new ArrayList<Agent>(), fhg);
				rg.addEdge(src, sink, rep);

			}
		}
		return rg;
	}

}
