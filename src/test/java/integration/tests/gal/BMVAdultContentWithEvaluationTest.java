package test.java.integration.tests.gal;

import org.junit.Before;

public class BMVAdultContentWithEvaluationTest extends MVBaseTestScenario {

	public final static String TEST_NAME = "AdultContentWithEvaluation";

	@Before
	public void setUp() {
		super.setUp("BMV", TEST_NAME, new MVTestInitializer());
	}
}
