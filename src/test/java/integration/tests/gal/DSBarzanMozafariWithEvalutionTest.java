package test.java.integration.tests.gal;

import org.junit.Before;

public class DSBarzanMozafariWithEvalutionTest extends DSBaseTestScenario {

	public final static String TEST_NAME = "BarzanMozafariWithEvaluation";

	@Before
	public void setUp() {
		super.setUp(TEST_NAME, new DefaultDataLoader());
	}
}
