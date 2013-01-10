package test.java.gal.integration.tests;

import test.java.gal.integration.helpers.*;

import java.util.Collection;
import java.util.HashSet;
import org.junit.BeforeClass;
import com.datascience.gal.*;

public class Test_BatchDawidSkene_BarzanMozafari extends BaseScenarios{
	
	public static String FILEPATH_SEPARATOR = System.getProperty("file.separator");
	public static String TESTDATA_BASEDIR = TestSettings.TESTDATA_BASEDIR;
	public static String RESULTS_BASEDIR = TestSettings.RESULTS_BASEDIR;
	
	public static String TEST_DIR = "BarzanMozafari";
	public static String INPUT_DIR 	= TESTDATA_BASEDIR + TEST_DIR + FILEPATH_SEPARATOR + "input" + FILEPATH_SEPARATOR;
	public static String OUTPUT_DIR	= TESTDATA_BASEDIR + TEST_DIR + FILEPATH_SEPARATOR + "output" + FILEPATH_SEPARATOR;
		
	public static String CATEGORIES_FILE 	= INPUT_DIR 	+ 	"categories.txt";
	public static String COSTS_FILE 		= INPUT_DIR 	+ 	"costs.txt";
	public static String GOLDLABELS_FILE 	= INPUT_DIR 	+ 	"correct.txt";
	public static String LABELS_FILE 		= INPUT_DIR 	+ 	"input.txt";
	public static String SUMMARY_FILE 		= OUTPUT_DIR 	+	"summary.txt";

	//test results file
	public static String TEST_RESULTS_FILE = RESULTS_BASEDIR + "Results_BarzanMozafari.csv";
	public static String PROJECT_ID = "12345";
	
	static Collection<Category> categories;
	static HashSet<MisclassificationCost> misclassificationCosts;
	static Collection<CorrectLabel> correctLabels;
	static Collection<AssignedLabel> assignedLabels;
	static BatchDawidSkene ds;
	static TestHelpers testHelper;
	static SummaryResultsParser summaryResultsParser;
	static FileWriters fileWriter;
	
	static BaseScenarios.Setup testSetup;
	
	@BeforeClass
	public static void setupTests(){
		testHelper = new TestHelpers();

		//prepare the test results file
		fileWriter = new FileWriters();
		fileWriter.createNewFile(TEST_RESULTS_FILE);
		fileWriter.writeToFile(TEST_RESULTS_FILE, "Metric,GAL value,Troia value");
		
		summaryResultsParser = new SummaryResultsParser();
		
		categories = testHelper.LoadCategories(CATEGORIES_FILE);
		ds = new BatchDawidSkene(PROJECT_ID, categories);
				
		misclassificationCosts = testHelper.LoadMisclassificationCosts(COSTS_FILE);
		ds.addMisclassificationCosts(misclassificationCosts);
				
		correctLabels = testHelper.LoadGoldLabels(GOLDLABELS_FILE);
		ds.addCorrectLabels(correctLabels);
				
		assignedLabels = testHelper.LoadWorkerAssignedLabels(LABELS_FILE);
		ds.addAssignedLabels(assignedLabels);
		
		//init the test setup
		testSetup = new BaseScenarios.Setup(ds, SUMMARY_FILE, TEST_RESULTS_FILE); 
		initSetup(testSetup);
	}

	
	
}
