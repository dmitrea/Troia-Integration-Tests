package test.java.integration.tests.galc;

import org.junit.Before;

public class ContinuousJoinlyNormal extends ContinuousBaseTestScenario {

	public static String TEST_NAME = "joinlynormal";

	@Before
	public void setUp() {
		setInputDir(TEST_NAME);
		setOutputDir(TEST_NAME);
		super.setUp(TEST_NAME);
	}
}
