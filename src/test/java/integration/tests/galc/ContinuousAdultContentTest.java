package test.java.integration.tests.galc;

import org.junit.Before;

public class ContinuousAdultContentTest extends ContinousBaseTestScenario {

	public static String TEST_NAME = "adultcontent";

	@Before
	public void setUp() {
		setInputDir(TEST_NAME);
		setOutputDir(TEST_NAME);
		super.setUp();
	}
}
