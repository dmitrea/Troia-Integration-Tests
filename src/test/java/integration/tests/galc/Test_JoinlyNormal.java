package test.java.integration.tests.galc;

import org.junit.Before;

public class Test_JoinlyNormal extends GALCBaseScenarios{

	public static String TEST_NAME = "joinlynormal";

	@Before
	public void setUp() {
		setInputDir(TEST_NAME);
		setOutputDir(TEST_NAME);
		super.setUp();
	}
}
