package test.java.integration.tests.gal;

import org.junit.Before;

public class MVAdultContentWithEvaluationTest extends MVBaseTestScenario {

	public final static String TEST_NAME = "AdultContentWithEvaluation";

	@Before
	public void setUp() {
		super.setUp(TEST_NAME, new MVTestInitializer());
	}
}
