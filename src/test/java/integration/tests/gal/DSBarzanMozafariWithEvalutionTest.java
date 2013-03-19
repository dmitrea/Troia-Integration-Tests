package test.java.integration.tests.gal;

import org.junit.Before;

public class DSBarzanMozafariWithEvalutionTest extends DSBaseTest {

	public final static String TEST_NAME = "BarzanMozafariWithEvaluation";

	@Before
	public void setUp() {
		super.setUp(new DefaultTestInitializer(TEST_NAME, DATA_BASE_DIR));
	}
}
