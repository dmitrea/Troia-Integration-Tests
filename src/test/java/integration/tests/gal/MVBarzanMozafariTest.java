package test.java.integration.tests.gal;


import org.junit.Before;

public class MVBarzanMozafariTest extends MVBaseTest {

	public final static String TEST_NAME = "BarzanMozafari";

	public static class InitilizerWithoutEvaluation extends MVTestInitializer {

		public InitilizerWithoutEvaluation(String testName) {
			super(testName);
		}

		@Override
		public void loadData(BaseTest test) {
			test.loadCategories(inputDir + "categories.txt");
			test.loadAssignedLabels(inputDir + "input.txt");
			test.loadGoldLabels(inputDir + "correct.txt");
		}
	}

	@Before
	public void setUp() {
		super.setUp(new InitilizerWithoutEvaluation(TEST_NAME));
	}
}
