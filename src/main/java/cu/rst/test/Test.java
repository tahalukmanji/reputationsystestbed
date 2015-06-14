/**
 * 
 */
package cu.rst.test;

import static cu.rst.util.Util.*;

import java.util.ArrayList;

import org.apache.log4j.BasicConfigurator;

import cu.rst.graph.Agent;
import cu.rst.graph.FHG;
import cu.rst.graph.Feedback;
import cu.rst.graph.Graph;
import cu.rst.graph.RG;
import cu.rst.graph.ReputationEdge;

/**
 * @author partheinstein
 *
 */
public class Test 
{
	public static void testEigenTrust(String fn) throws Exception
	{
		display(generateFHG(fn));
		display(eigenTrust(discretize(generateFHG(fn))));
	}
	
	public static void testPeerTrust(String fn) throws Exception
	{
		display(generateFHG(fn));
		display(normalize(peerTrust(generateFHG(fn))));
	}

	public static void testSpearman(String fn) throws Exception
	{
		ArrayList graphs = new ArrayList();
		graphs.add(normalize(peerTrust(generateFHG(fn))));
		graphs.add(eigenTrust(discretize(generateFHG(fn))));
		double coeff = spearman((Graph)graphs.get(0), (Graph)graphs.get(1));
		display((Graph)graphs.get(0));
		display((Graph)graphs.get(1));
		
		System.out.println("Spearman coefficient: " + coeff);
	}
	
	public static void testSlanderingEigenTrust(String fn1, String fn2) throws Exception
	{
		display(generateFHG(fn1));
		display(generateFHG(fn2));
		ArrayList graphs = new ArrayList();
		graphs.add(eigenTrust(discretize(generateFHG(fn1))));
		graphs.add(eigenTrust(discretize(generateFHG(fn2))));
		double coeff = spearman((Graph)graphs.get(0), (Graph)graphs.get(1));
		display((Graph)graphs.get(0));
		display((Graph)graphs.get(1));
		
		System.out.println("Spearman coefficient: " + coeff);
		
	}
	
	public static void testAppleseed() throws Exception
	{
		RG rg3 = (RG) appleseed(generateRG("input//exp_sybilSlander2.arff"));
		
		System.out.println("Appleseed:");
		System.out.println(rg3);
	}
	
	public static void testSybil() throws Exception
	{
		
		int i = 3;
		FHG fhg0 = generateFHG("input//sybil.arff");
		boolean success = false;
		do
		{
			
			//RG rg1 = (RG) eigenTrust(fhg0);
			display(eigenTrustPart1(fhg0));
			RG rg1 = (RG) appleseed(eigenTrustPart1(fhg0));
			display(rg1);
			ReputationEdge re0 =  (ReputationEdge) rg1.getEdge(new Agent(0), new Agent(1));
			ReputationEdge re1 = (ReputationEdge) rg1.getEdge(new Agent(0), new Agent(2));
			double r0 = re0.getReputation();
			double r1 = re1.getReputation();
			System.out.println(r0 + "   " + r1);
			if(r0 < r1)
			{
				System.out.println("Sybil attack succeeded after " + i + " items.");
				success=true;
				break;
			}
			else
			{
				Agent sybil = new Agent(i);
				fhg0.addFeedback(new Feedback(new Agent(2), sybil, 1.0));
				fhg0.addFeedback(new Feedback(sybil, new Agent(1), (double) 0));
				System.out.println("Sybil attack not succeeded even after " + i + " items.");
			}
			i++;
		}while(i<100 + 3);
		
	}
	
	
	
	public static void main(String[] args) throws Exception
	{
		BasicConfigurator.configure();
//		ArrayList graphs = new ArrayList();
//		graphs.add(generateFHG("input//exp2c.arff"));
//		graphs.add(peerTrust(generateFHG("input//exp2c.arff")));
//		display(graphs);
		
//		testSpearman("input//exp1a_negfeedback.arff");
//		testEigenTrust("input//exp1b_bootstrap.arff");
//		testSlanderingEigenTrust("input//exp1a_negfeedback.arff", "input//exp1c_slandering.arff");
		//testEigenTrust("input//exp1d_slandering.arff");
		//testPeerTrust("input//normalization2.arff");
		testSybil();
//		testAppleseed();
	}

}
