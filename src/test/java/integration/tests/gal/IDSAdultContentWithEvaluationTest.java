package test.java.integration.tests.gal;

import org.junit.Before;

public class IDSAdultContentWithEvaluationTest extends DSBaseTestScenario {
	
	public final static String TEST_NAME = "AdultContentWithEvaluation";

	@Before
	public void setUp() {
		super.setUp("IDS", TEST_NAME, new DefaultDataLoader());
	}
}
