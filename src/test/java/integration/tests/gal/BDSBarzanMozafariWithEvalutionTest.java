package test.java.integration.tests.gal;

import org.junit.Before;

public class BDSBarzanMozafariWithEvalutionTest extends DSBaseTestScenario {

	public final static String TEST_NAME = "BarzanMozafariWithEvaluation";

	@Before
	public void setUp() {
		super.setUp("BDS", TEST_NAME, new DefaultDataLoader());
	}
}
