package test.java.integration.tests.gal;

import org.junit.Before;
import test.java.integration.helpers.FileWriters;
import test.java.integration.helpers.SummaryResultsParser;
import test.java.integration.helpers.TestSettings;

public class Test_BatchDawidSkene_AdultContentWithEvaluation extends GALBaseScenarios {
	
	public final static String TEST_NAME = "AdultContentWithEvaluation";
	public final static String INPUT_DIR = DATA_BASE_DIR + TEST_NAME + TestSettings.FILEPATH_SEPARATOR + "input" + TestSettings.FILEPATH_SEPARATOR;
	public final static String OUTPUT_DIR = DATA_BASE_DIR + TEST_NAME + TestSettings.FILEPATH_SEPARATOR + "output" + TestSettings.FILEPATH_SEPARATOR;

	@Before
	public void setUp() {
		loadData(INPUT_DIR);
		fileWriter = new FileWriters(OUTPUT_DIR + "Results_" + TEST_NAME + ".csv");
		summaryResultsParser = new SummaryResultsParser(OUTPUT_DIR + "summary.txt");
		super.setUp();
	}
}
