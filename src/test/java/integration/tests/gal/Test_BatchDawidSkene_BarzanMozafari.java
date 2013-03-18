package test.java.integration.tests.gal;

import com.datascience.core.base.LObject;
import org.junit.Before;
import test.java.integration.helpers.FileWriters;
import test.java.integration.helpers.SummaryResultsParser;
import test.java.integration.helpers.TestSettings;

import java.util.HashSet;

public class Test_BatchDawidSkene_BarzanMozafari extends GALBaseScenarios {

	public final static String TEST_NAME = "BarzanMozafari";
	public final static String INPUT_DIR = DATA_BASE_DIR + TEST_NAME + TestSettings.FILEPATH_SEPARATOR + "input" + TestSettings.FILEPATH_SEPARATOR;
	public final static String OUTPUT_DIR = DATA_BASE_DIR + TEST_NAME + TestSettings.FILEPATH_SEPARATOR + "output" + TestSettings.FILEPATH_SEPARATOR;

	@Before
	public void setUp() {
		categories = loadCategories(INPUT_DIR + "categories.txt");
		correctLabels = loadGoldLabels(INPUT_DIR + "correct.txt");
		assignedLabels = loadAssignedLabels(INPUT_DIR + "input.txt");
		evaluationLabels = new HashSet<LObject<String>>();
		costs = loadCosts(INPUT_DIR + "costs.txt");
		fileWriter = new FileWriters(OUTPUT_DIR + "Results_" + TEST_NAME + ".csv");
		summaryResultsParser = new SummaryResultsParser(OUTPUT_DIR + "summary.txt");
		super.setUp();
	}
}
