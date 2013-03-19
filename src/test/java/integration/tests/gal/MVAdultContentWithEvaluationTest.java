package test.java.integration.tests.gal;

import org.junit.Before;

public class MVAdultContentWithEvaluationTest extends MVBaseTest {

	public final static String TEST_NAME = "AdultContentWithEvaluation";

	@Before
	public void setUp() {
		super.setUp(new MVTestInitializer(TEST_NAME));
	}
}
