package test.java.integration.tests.gal;

import org.junit.Before;

public class BDSMovieLensTest extends DSBaseTestScenario {

	public final static String TEST_NAME = "Movielens-ml-100k";
	
	@Before
	public void setUp() {
		super.setUp("BDS", TEST_NAME, new IDataLoader() {
			@Override
			public void load(BaseTestScenario test) {
				test.loadCategories();
				test.loadAssignedLabels();
			}
		});
	}
}