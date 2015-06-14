package cu.rst.alg;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import cu.rst.graph.*;
import Jama.Matrix;

/**
 * This version of ET takes a rep graph and outputs a rep graph
 * @author partheinstein
 *
 */
public class EigenTrustPart1 implements AlgorithmIntf
{
	
	private double[][] cijMatrix;
	private Hashtable<Integer, Integer> agentIdmapping;
	static Logger logger = Logger.getLogger(EigenTrustPart1.class.getName());
		
	public EigenTrustPart1()
	{
		this.agentIdmapping = new Hashtable<Integer, Integer>();
	}
	

	
	public Graph execute(Graph g) throws Exception 
	{
		if(!(g instanceof FHG))
		{
			throw new Exception("Input graph must be a FHG.");
		}
		
		FHG fhg = (FHG) g;
		int numVertices = fhg.vertexSet().size();
		cijMatrix = new double[numVertices][numVertices];
		
		Set<Agent> agents = fhg.vertexSet();
		
		int internalId=0;
		this.agentIdmapping.clear();
		for(Agent a : agents)
		{
			this.agentIdmapping.put(a.id, internalId);
			internalId++;
		}
		
		for(Agent source : agents)
		{
			Set<FeedbackHistoryGraphEdge> allOutgoingEdges = fhg.outgoingEdgesOf(source); 
			
			
			//fill up cij matrix
			for(FeedbackHistoryGraphEdge edge : allOutgoingEdges)
			{
				ArrayList<Feedback> feedbacks = edge.feedbacks;
				double sij=0;
				for(Feedback feedback : feedbacks)
				{
					
//					if(feedback.value >= threshold2Satisfy) sij++;
//					else sij--;
					if(feedback.value == 1) sij++;
					else sij--;
				}
				
				if(sij<1) sij=0;
				Agent sinkTemp = (Agent)edge.sink;
				if(this.agentIdmapping.get(source.id) > cijMatrix.length || this.agentIdmapping.get(sinkTemp.id) > cijMatrix.length)
					throw new Exception("Array out of bounds exception will occur. Problem with internal id mapping.");
				cijMatrix[this.agentIdmapping.get(source.id)][this.agentIdmapping.get(sinkTemp.id)] = sij;		
			}
		}
		
		logger.info("cijMatrix before normalization = " + this.printMatrix(cijMatrix));
		
		//normalize cij matrix
		for(int i=0;i<numVertices;i++)
		{
			//row by row normalization
			double total = 0;
			for(int j=0;j<numVertices;j++)
			{
				total = total + cijMatrix[i][j];
			}
			for(int j=0;j<numVertices;j++)
			{
				if(total>0) cijMatrix[i][j] = cijMatrix[i][j] / total;
				//else cijMatrix[i][j]=0; //don't divide by 0
				
				//agent i doesnt trust anyone. make it trust everyone equally.
				else cijMatrix[i][j]=1.0/(double)numVertices;
			}
		}
	
		logger.info("cijMatrix after normalization = " +  this.printMatrix(cijMatrix));
		Matrix trustScores = new Matrix(cijMatrix);
		
		RG outputGraph = new RG();
		//create all the reputation edges that needs to be added to RG. Its a complete graph
		for(Agent src : (Set<Agent>)fhg.vertexSet())
		{
			for(Agent sink : (Set<Agent>)fhg.vertexSet())
			{
				double rep = trustScores.getArray()[this.agentIdmapping.get(src.id)][this.agentIdmapping.get(sink.id)];
				outputGraph.addEdge(src, sink,  rep);
			}
		}
	
		return outputGraph;
	}
	
		
	public String printMatrix(double[][] mat)
	{
		String output = "\n";
		output += "Internal id mapping:";
		Set<Entry<Integer, Integer>> temp = this.agentIdmapping.entrySet();
		for(Entry<Integer, Integer> e : temp)
		{
			output += "Agent.id: " + e.getKey() + ", Internal id: " + e.getValue() + "\n"; 
		}
		for(int i=0;i<mat.length;i++)
		{
			for(int j=0;j<mat[i].length;j++)
			{
				output = output + (mat[i][j] + " ");
			}
			output = output + "\n";
		}
		return output;

	}





}
