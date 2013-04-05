package test.java.integration.tests.gal;

import org.junit.Before;

public class IMVBarzanMozafariWithEvaluationTest extends MVBaseTestScenario {

	public final static String TEST_NAME = "BarzanMozafariWithEvaluation";

	@Before
	public void setUp() {
		super.setUp("IMV", TEST_NAME, new MVTestInitializer());
	}
}
