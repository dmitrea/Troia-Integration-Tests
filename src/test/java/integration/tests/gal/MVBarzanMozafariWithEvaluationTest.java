package test.java.integration.tests.gal;

import org.junit.Before;

public class MVBarzanMozafariWithEvaluationTest extends MVBaseTest {

	public final static String TEST_NAME = "BarzanMozafariWithEvaluation";

	@Before
	public void setUp() {
		super.setUp(new MVTestInitializer(TEST_NAME));
	}
}
