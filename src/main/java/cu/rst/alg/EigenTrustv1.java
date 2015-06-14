package cu.rst.alg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import cu.rst.graph.*;
import Jama.Matrix;

/**
 * This version of ET takes a rep graph and outputs a rep graph
 * 
 * @author partheinstein
 *
 */
public class EigenTrustv1 implements AlgorithmIntf {

	private static final int DEFAULT_NUM_ITERATIONS = 20;
	private static final double EIGENTRUST_PARAM_A = 0.2;

	private final int numIterations;

	public EigenTrustv1(int numOfIterations) {
		this.numIterations = numOfIterations;
	}

	public EigenTrustv1() {
		this.numIterations = DEFAULT_NUM_ITERATIONS;
	}

	
	public Graph execute(Graph g) throws Exception {
		if (g == null) {
			throw new IllegalArgumentException("Input graph is null.");
		}

		if (!(g instanceof FHG)) {
			throw new IllegalArgumentException(
					"EigenTrust requires a FHG as input.");
		}

		FHG fhg = (FHG) g;

		// the agent id may be some arbitrary number which cannot be used as
		// indices for the matrix multiplications

		Map<Integer, Integer> internalAgentIds = populateInternalAgentIds(fhg);

		double[][] feedbacks = convertToArray(fhg, internalAgentIds);
		// System.out.println("Before normalization:");
		// System.out.println(Arrays.deepToString(feedbacks));

		double[][] normalizedFeedbacks = normalizeFeedbacks(feedbacks);
		// System.out.println("After normalization:");
		// System.out.println(Arrays.deepToString(normalizedFeedbacks));

		double[][] eigenVector = computeEigenVector(normalizedFeedbacks,
				EIGENTRUST_PARAM_A, DEFAULT_NUM_ITERATIONS);

		RG outputGraph = new RG();
		// create all the reputation edges that needs to be added to RG. Its a
		// complete graph
		for (Agent src : (Set<Agent>) fhg.vertexSet()) {
			for (Agent sink : (Set<Agent>) fhg.vertexSet()) {
				outputGraph.addEdge(src, sink,
						eigenVector[internalAgentIds.get(sink.id)][0]);
			}
		}

		return outputGraph;
	}

	private Map<Integer, Integer> populateInternalAgentIds(FHG fhg) {
		Map<Integer, Integer> agentIdMap = new HashMap<>();

		Set<Agent> agents = fhg.vertexSet();

		// the agent id may be some arbitrary number which cannot be used as
		// indices for the matrix multiplications
		int internalId = 0;
		for (Agent agent : agents) {
			agentIdMap.put(agent.id, internalId);
			internalId++;
		}

		return agentIdMap;
	}

	private double[][] convertToArray(FHG fhg,
			Map<Integer, Integer> internalAgentIds) {

		Set<Agent> agents = fhg.vertexSet();
		int numAgents = agents.size();
		double[][] feedbackValues = new double[numAgents][numAgents];

		for (Agent source : agents) {
			Set<FeedbackHistoryGraphEdge> allOutgoingEdges = fhg
					.outgoingEdgesOf(source);

			// for each edge of the agent, get the feedback history and
			// calculate the sum of the feedback
			for (FeedbackHistoryGraphEdge edge : allOutgoingEdges) {

				ArrayList<Feedback> feedbacks = edge.feedbacks;
				double sumOfFeedbacks = 0;
				for (Feedback feedback : feedbacks) {
					if (feedback.value == 1)
						sumOfFeedbacks++;
					else
						sumOfFeedbacks--;
				}

				if (sumOfFeedbacks < 1) {
					sumOfFeedbacks = 0;
				}

				Agent sinkTemp = (Agent) edge.sink;

				int internalSourceId = internalAgentIds.get(source.id);
				int internalSinkId = internalAgentIds.get(sinkTemp.id);

				feedbackValues[internalSourceId][internalSinkId] = sumOfFeedbacks;
			}
		}

		return feedbackValues;
	}

	public double[][] computeEigenVector(double[][] normalizedFeedbacks,
			double pretrustedWeight, int numIterations) {

		/*
		 * t(0) = p; repeat t(k+1) = C(T).t(k); t(k+1) = (1 − a).t(k+1) + a.p;
		 * delta = ||t(k+1) − t(k)||; until delta < tau;
		 */

		int numAgents = normalizedFeedbacks.length;

		// transpose
		Matrix transposedFeedbacks = new Matrix(normalizedFeedbacks)
				.transpose();

		double equalTrust = 1.0 / (double) numAgents;

		// pre trust all agents equally
		double[][] pretrusted = new double[numAgents][1];
		for (int i = 0; i < numAgents; i++) {
			for (int j = 0; j < 1; j++)
				pretrusted[i][j] = equalTrust;
		}

		// t(0) = trust everyone equally in the beginning
		Matrix tkMatrix = new Matrix(pretrusted);
		// preTrusted matrix
		Matrix pMatrix = new Matrix(pretrusted);

		Matrix tkplus1Matrix = null;
		for (int i = 0; i < numIterations; i++) {
			// note that t(0) = p
			tkplus1Matrix = transposedFeedbacks.times(tkMatrix);
			tkplus1Matrix = tkplus1Matrix.times(1 - pretrustedWeight);
			tkplus1Matrix = tkplus1Matrix.plus(pMatrix.times(pretrustedWeight));
			tkMatrix = new Matrix(tkplus1Matrix.getArrayCopy());
		}

		return tkMatrix.getArray();
	}

	/**
	 * This is a simplified eigenvector calculation to test correctness of the
	 * 3rd party Matrix library used elsewhere. It is used only in the unit
	 * tests.
	 * 
	 * @param normalizedFeedbacks
	 * @return
	 */
	public static double[][] computeEigenVectorTmp(int numIterations,
			double[][] normalizedFeedbacks) {

		int numAgents = normalizedFeedbacks.length;

		Matrix transposedFeedbacks = new Matrix(normalizedFeedbacks);

		double[][] tmp = new double[1][numAgents];
		tmp[0][0] = 0;
		tmp[0][1] = 1;
		tmp[0][2] = 0;

		Matrix tkMatrix = new Matrix(transposedFeedbacks.getArrayCopy());

		Matrix tkplus1Matrix = null;

		for (int i = 0; i < numIterations; i++) {
			tkplus1Matrix = transposedFeedbacks.times(tkMatrix);
			tkMatrix = new Matrix(tkplus1Matrix.getArrayCopy());
		}

		return new Matrix(tmp).times(tkMatrix).getArray();
	}

	private double[][] normalizeFeedbacks(double[][] feedbacks) {

		int numAgents = feedbacks.length;
		double[][] result = new double[numAgents][numAgents];

		for (int i = 0; i < numAgents; i++) {
			// row by row normalization
			double total = 0;
			for (int j = 0; j < numAgents; j++) {
				total = total + feedbacks[i][j];
			}

			for (int j = 0; j < numAgents; j++) {
				if (total > 0) {
					result[i][j] = feedbacks[i][j] / total;
				} else {
					// agent doesn't trust anyone. make it trust everyone
					// equally.
					result[i][j] = 1.0 / (double) numAgents;
				}
			}
		}

		return result;
	}
}
