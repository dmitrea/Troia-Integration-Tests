package test.java.integration.tests.gal;

import org.junit.Before;

public class IMVAdultContentWithEvaluationTest extends MVBaseTestScenario {

	public final static String TEST_NAME = "AdultContentWithEvaluation";

	@Before
	public void setUp() {
		super.setUp("IMV", TEST_NAME, new MVTestInitializer());
	}
}
