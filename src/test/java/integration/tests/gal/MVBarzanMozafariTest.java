package test.java.integration.tests.gal;


import org.junit.Before;

public class MVBarzanMozafariTest extends MVBaseTest {

	public final static String TEST_NAME = "BarzanMozafari";

	@Before
	public void setUp() {
		super.setUp(new MVTestInitializer(TEST_NAME));
	}
}
