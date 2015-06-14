import static cu.rst.util.Util.*;
import static org.testng.Assert.*;

import org.testng.annotations.Test;

import cu.rst.alg.Appleseed;
import cu.rst.alg.EigenTrustv1;
import cu.rst.graph.Agent;
import cu.rst.graph.FHG;
import cu.rst.graph.RG;
import cu.rst.graph.ReputationEdge;

public class Tests {

	/*
	 * Sanity Tests
	 */

	@Test
	public void eigenvectorSanityTest_AWest_PennState() {

		// This test uses the example provided in
		// http://rtg.cis.upenn.edu/qtm/doc/p2p_reputation.pdf. As per this
		// example, the expected eigenvector is [[0.35], [0.49], [0.19]]. We
		// were unable to verify this result using our implementation. However
		// the results from our implementation and results from manual
		// calculation are the same. So it is likely the results provided in the
		// link above are incorrect for the given input.

		// The expected values were obtained by calculating them by hand. See
		// src/test/resourcces/eigentrust_manual_calc.txt for the steps.

		double[][] normalizedFeedbacks = { { 0, 1, 0 },
				{ 0.66666666, 0, 0.33333333 }, { 0.125, 0.875, 0 } };

		double a = 0.5;

		double[][] eigenVector = (new EigenTrustv1()).computeEigenVector(
				normalizedFeedbacks, a, 20);

		assertTrue(Math.abs(eigenVector[0][0] - 0.32) < 0.1);
		assertTrue(Math.abs(eigenVector[1][0] - 0.42) < 0.1);
		assertTrue(Math.abs(eigenVector[2][0] - 0.23) < 0.1);

	}

	@Test
	public void eigenvectorSanityTest_Lian_UCSB() {

		// This test uses the samples provided in
		// iptps06.cs.ucsb.edu/talks/lian-maze-iptps06.ppt. Even the value of
		// the weight given to the pretrusted peers is not given in this
		// example, it appears the authors gave it a value of 0.
		double[][] normalizedFeedbacks = { { 0, 0.9, 0.1 }, { 0.9, 0, 0.1 },
				{ 0.2, 0.2, 0.6 } };

		double[][] eigenVector = (new EigenTrustv1()).computeEigenVector(
				normalizedFeedbacks, 0, 20);

		assertTrue(Math.abs(eigenVector[0][0] - 0.4) < 0.1);
		assertTrue(Math.abs(eigenVector[1][0] - 0.4) < 0.1);
		assertTrue(Math.abs(eigenVector[2][0] - 0.2) < 0.1);

	}

	@Test
	public void markovChainSanityTestFromWikipedia() {
		// http://en.wikipedia.org/wiki/Markov_chain
		double[][] normalizedFeedbacks = { { 0.9, 0.075, 0.025 },
				{ 0.15, 0.8, 0.05 }, { 0.25, 0.25, 0.5 } };

		double[][] expectedEigenVector = { { 0.625, 0.3125, 0.0625 } };

		double[][] computedEigenVector = EigenTrustv1.computeEigenVectorTmp(20,
				normalizedFeedbacks);

		assertTrue(Math.abs(computedEigenVector[0][0]
				- expectedEigenVector[0][0]) < 0.1);
		assertTrue(Math.abs(computedEigenVector[0][1]
				- expectedEigenVector[0][1]) < 0.1);
		assertTrue(Math.abs(computedEigenVector[0][2]
				- expectedEigenVector[0][2]) < 0.1);

		// System.out.println(Arrays.deepToString(computedEigenVector));
	}

	@Test
	public void eigenTrustRefactorSanityTest() throws Exception {
		String input = "src/test/resources/input/eigentrust_sanity.arff";

		FHG fhg = generateFHG(input);
		RG rg1 = (RG) eigenTrust(fhg);
		RG rg2 = (RG) (new EigenTrustv1()).execute(fhg);

		assertTrue(spearman(rg1, rg2) == 1);
	}

	@Test
	public void peerTrustSanityTest() throws Exception {

		String input = "src/test/resources/input/peertrust_sanity.arff";

		FHG fhg = generateFHG(input);
		RG rg = (RG) peerTrust(fhg);

		Agent alice = new Agent(0);
		Agent bob = new Agent(1);
		Agent charlie = new Agent(2);
		Agent dan = new Agent(3);

		double bobAliceTrust = ((ReputationEdge) rg.getEdge(bob, alice))
				.getReputation();
		double charlieAliceTrust = ((ReputationEdge) rg.getEdge(charlie, alice))
				.getReputation();
		double danAliceTrust = ((ReputationEdge) rg.getEdge(dan, alice))
				.getReputation();

		double aliceBobTrust = ((ReputationEdge) rg.getEdge(alice, bob))
				.getReputation();
		double charlieBobTrust = ((ReputationEdge) rg.getEdge(charlie, bob))
				.getReputation();
		double danBobTrust = ((ReputationEdge) rg.getEdge(charlie, bob))
				.getReputation();

		double aliceCharlieTrust = ((ReputationEdge) rg.getEdge(alice, charlie))
				.getReputation();
		double bobCharlieTrust = ((ReputationEdge) rg.getEdge(bob, charlie))
				.getReputation();
		double danCharlieTrust = ((ReputationEdge) rg.getEdge(dan, charlie))
				.getReputation();

		double aliceDanTrust = ((ReputationEdge) rg.getEdge(alice, dan))
				.getReputation();
		double bobDanTrust = ((ReputationEdge) rg.getEdge(bob, dan))
				.getReputation();
		double charlieDanTrust = ((ReputationEdge) rg.getEdge(charlie, dan))
				.getReputation();
		 
		// 

		// Alice's global trust score be around0.85
		assertTrue(Math.abs(bobAliceTrust - 0.85) < 0.01);
		assertTrue(Math.abs(charlieAliceTrust - 0.85) < 0.01);
		assertTrue(Math.abs(danAliceTrust - 0.85) < 0.01);

		// Bob's global trust score must be 0.85
		assertTrue(Math.abs(aliceBobTrust - 0.85) < 0.01);
		assertTrue(Math.abs(charlieBobTrust - 0.85) < 0.01);
		assertTrue(Math.abs(danBobTrust - 0.85) < 0.01);

		// Charlie's global trust score must be around 0.465
		assertTrue(Math.abs(aliceCharlieTrust - 0.465) < 0.01);
		assertTrue(Math.abs(bobCharlieTrust - 0.465) < 0.01);
		assertTrue(Math.abs(danCharlieTrust - 0.465) < 0.01);

		// Dan's global trust score must be 0.25
		assertTrue(aliceDanTrust == 0.25);
		assertTrue(bobDanTrust == 0.25);
		assertTrue(charlieDanTrust == 0.25);
	}

	@Test
	public void appleseedSanityTest() throws Exception {
		String input = "src/test/resources/input/appleseed_sanity.arff";

		RG inputRG = generateRG(input);
		RG outputRG = (RG) (new Appleseed(3)).execute(inputRG);

		Agent agent0 = new Agent(0);
		Agent agent1 = new Agent(1);
		Agent agent2 = new Agent(2);

		double rep01 = ((ReputationEdge) outputRG.getEdge(agent0, agent1))
				.getReputation();
		double rep02 = ((ReputationEdge) outputRG.getEdge(agent0, agent2))
				.getReputation();

		// These values were obtained by calculating them by hand. See
		// src/test/resources/appleseed_manual_calc.txt for the steps.
		assertTrue(rep01 == 0.1275);
		assertTrue(rep02 == 0.0541875);

	}

	/*
	 * Experiments Tests
	 */

	@Test
	public void testEigenTrust_NormAttack1() throws Exception {
		String input1 = "src/test/resources/input/normalization1.arff";
		String input2 = "src/test/resources/input/normalization2.arff";

		RG rg1 = (RG) eigenTrust(discretize(generateFHG(input1)));
		RG rg2 = (RG) eigenTrust(discretize(generateFHG(input2)));

		double spearmanCoeff = spearman(rg1, rg2);

		assertTrue(spearmanCoeff == 1);
	}

	@Test
	public void testPeerTrust_NormAttack1() throws Exception {
		String input1 = "src/test/resources/input/normalization1.arff";
		String input2 = "src/test/resources/input/normalization2.arff";

		RG rg1 = (RG) peerTrust(generateFHG(input1));
		RG rg2 = (RG) peerTrust(generateFHG(input2));

		double spearmanCoeff = spearman(rg1, rg2);

		// PeerTrust behaves odd in this situation
		assertFalse(spearmanCoeff == 1);
	}

	@Test
	public void testEigenTrust_SelfPromAttack_1() throws Exception {
		String input1 = "src/test/resources/input/normalization1.arff";
		String input2 = "src/test/resources/input/exp1b_bootstrap.arff";

		RG rg1 = (RG) eigenTrust(discretize(generateFHG(input1)));
		RG rg2 = (RG) eigenTrust(discretize(generateFHG(input2)));

		double spearmanCoeff = spearman(rg1, rg2);

		// Expect the two graphs to be different
		assertFalse(spearmanCoeff == 1);
	}

	@Test
	public void testPeerTrust_SelfPromAttack_2() {
		String input1 = "src/test/resources/input/normalization1.arff";
		String input2 = "src/test/resources/input/exp1b_bootstrap.arff";

		try {
			RG rg1 = (RG) peerTrust(generateFHG(input1));
			RG rg2 = (RG) peerTrust(generateFHG(input2));
		} catch (Exception e) {
			// TODO assert type of exception and not exception message
			assertEquals(e.getMessage(),
					"total trust score is 0. It should never be zero.");
		}
	}

	@Test
	public void testEigenTrust_SlanderingAttack_1() throws Exception {
		String input1 = "src/test/resources/input/normalization1.arff";
		String input2 = "src/test/resources/input/exp1c_slandering.arff";

		RG rg1 = (RG) eigenTrust(discretize(generateFHG(input1)));
		RG rg2 = (RG) eigenTrust(discretize(generateFHG(input2)));

		double spearmanCoeff = spearman(rg1, rg2);

		assertTrue(spearmanCoeff == 1);
	}

	@Test
	public void testPeerTrust_SlanderingAttack_1() {
		String input1 = "src/test/resources/input/normalization1.arff";
		String input2 = "src/test/resources/input/exp1c_slandering.arff";

		try {
			RG rg1 = (RG) peerTrust(generateFHG(input1));
			RG rg2 = (RG) peerTrust(generateFHG(input2));
		} catch (Exception e) {
			// TODO assert type of exception and not exception message
			assertEquals(e.getMessage(),
					"total trust score is 0. It should never be zero.");
		}
	}

	@Test
	public void testEigenTrust_SlanderingAttack_2() throws Exception {
		String input1 = "src/test/resources/input/normalization1.arff";
		String input2 = "src/test/resources/input/exp1d_slandering.arff";

		RG rg1 = (RG) eigenTrust(discretize(generateFHG(input1)));
		RG rg2 = (RG) eigenTrust(discretize(generateFHG(input2)));

		double spearmanCoeff = spearman(rg1, rg2);

		assertTrue(spearmanCoeff == 1);
	}

	@Test
	public void testPeerTrust_SlanderingAttack_e() throws Exception {
		String input1 = "src/test/resources/input/normalization1.arff";
		String input2 = "src/test/resources/input/exp1d_slandering.arff";

		RG rg1 = (RG) peerTrust(generateFHG(input1));
		RG rg2 = (RG) peerTrust(generateFHG(input2));

		double spearmanCoeff = spearman(rg1, rg2);

		assertTrue(spearmanCoeff == 1);
	}
}