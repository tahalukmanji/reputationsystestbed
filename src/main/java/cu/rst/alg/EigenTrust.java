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
public class EigenTrust implements AlgorithmIntf
{

	
	private final int DEFAULT_ITERATIONS = 20;

	private int iterations;
	private Matrix trustScores;
	private double[][] cijMatrix;
	private Hashtable<Integer, Integer> agentIdmapping;
	private double threshold2Satisfy = 0.7;
	static Logger logger = Logger.getLogger(EigenTrust.class.getName());
		
	public EigenTrust()
	{
		this.iterations = DEFAULT_ITERATIONS;
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
		
		System.out.println("cijMatrix before normalization = " + this.printMatrix(cijMatrix));
		
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
	
		System.out.println("cijMatrix after normalization = " +  this.printMatrix(cijMatrix));
		
		
		 /* t(0) = p;
		 * repeat
		 *  t(k+1) = C(T).t(k);
		 *  t(k+1) = (1 − a).t(k+1) + a.p;
		 *  delta = ||t(k+1) − t(k)||;
		 * until delta < tau;
		 */
		
		//get the transpose matrix.
		trustScores = new Matrix(cijMatrix);
		Matrix trans = trustScores.transpose();
		
		//t(0) = trust everyone equally in the beginning
		double tempScore = 1.0/(double)numVertices;
		double[][] tk = new double[numVertices][1];
		for(int i=0;i<numVertices;i++)
		{
			for(int j=0;j<1;j++) tk[i][j] = tempScore;
		}
		Matrix tkMatrix = new Matrix(tk);
		
		//p = pre trust certain agents
		double[][] pretrusted = new double[numVertices][1];
		for(int i=0;i<numVertices;i++)
		{
			for(int j=0;j<1;j++) pretrusted[i][j] = tempScore; 
		}
		
		Matrix pMatrix = new Matrix(pretrusted); //preTrusted matrix
		
		double a=0.2; //TODO make this configurable
		Matrix tkplus1Matrix = null;
		
		
		for(int i=0;i<this.iterations;i++)
		{
			//note that t(0) = p
			tkplus1Matrix = trans.times(tkMatrix);
			tkplus1Matrix = tkplus1Matrix.times(1-a);
			tkplus1Matrix = tkplus1Matrix.plus(pMatrix.times(a));
			tkMatrix = new Matrix(tkplus1Matrix.getArrayCopy());
			//System.out.println(printMatrix(tkplus1Matrix.getArray()));
			//System.out.println("row=" + tkplus1Matrix.getRowDimension() + " column="+tkplus1Matrix.getColumnDimension());
		}
		
		this.trustScores = tkMatrix;

		System.out.println("cijMatrix after multiplying " + this.iterations + " times = " +  this.printMatrix(trustScores.getArray()));
		
		RG outputGraph = new RG();
		//create all the reputation edges that needs to be added to RG. Its a complete graph
		for(Agent src : (Set<Agent>)fhg.vertexSet())
		{
			for(Agent sink : (Set<Agent>)fhg.vertexSet())
			{
				outputGraph.addEdge(src, sink,  trustScores.getArray()[this.agentIdmapping.get(sink.id)][0]);
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
