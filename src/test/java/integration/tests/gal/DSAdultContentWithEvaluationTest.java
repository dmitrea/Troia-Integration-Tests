package test.java.integration.tests.gal;

import org.junit.Before;

public class DSAdultContentWithEvaluationTest extends DSBaseTest {
	
	public final static String TEST_NAME = "AdultContentWithEvaluation";

	@Before
	public void setUp() {
		super.setUp(new DefaultTestInitializer(TEST_NAME, DATA_BASE_DIR));
	}
}
