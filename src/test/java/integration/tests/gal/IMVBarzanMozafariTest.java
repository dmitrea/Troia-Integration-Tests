package test.java.integration.tests.gal;

import org.junit.Before;

public class IMVBarzanMozafariTest extends MVBaseTestScenario {

	public final static String TEST_NAME = "BarzanMozafari";

	@Before
	public void setUp() {
		super.setUp("IMV", TEST_NAME, new IDataLoader() {
			@Override
			public void load(BaseTestScenario test) {
				test.loadCategories();
				test.loadAssignedLabels();
				test.loadGoldLabels();
			}
		});
	}
}
