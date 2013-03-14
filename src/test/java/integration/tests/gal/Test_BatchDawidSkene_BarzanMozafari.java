package test.java.integration.tests.gal;

import com.datascience.core.base.AssignedLabel;
import com.datascience.core.base.Category;
import com.datascience.core.base.LObject;
import com.datascience.core.nominal.NominalData;
import com.datascience.core.nominal.NominalProject;
import test.java.integration.helpers.*;

import java.util.Collection;
import java.util.HashSet;
import org.junit.BeforeClass;
import com.datascience.gal.*;

public class Test_BatchDawidSkene_BarzanMozafari extends GALBaseScenarios{
	
	public static String FILEPATH_SEPARATOR = System.getProperty("file.separator");
	public static String TESTDATA_BASEDIR = TestSettings.GAL_TESTDATA_BASEDIR;
	public static String RESULTS_BASEDIR = TestSettings.GAL_RESULTS_BASEDIR;
	
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
	static Collection<LObject<String>> correctLabels;
	static Collection<AssignedLabel<String>> assignedLabels;
	static BatchDawidSkene ds;
	static TestHelpers testHelper;
	static SummaryResultsParser summaryResultsParser;
	static FileWriters fileWriter;
	
	static GALBaseScenarios.Setup testSetup;
	
	@BeforeClass
	public static void setupTests(){
		testHelper = new TestHelpers();

		//prepare the test results file
		fileWriter = new FileWriters();
		fileWriter.createNewFile(TEST_RESULTS_FILE);
		fileWriter.writeToFile(TEST_RESULTS_FILE, "Metric,GAL value,Troia value");
		
		summaryResultsParser = new SummaryResultsParser();

		categories = testHelper.LoadCategories(CATEGORIES_FILE);
		correctLabels = testHelper.LoadGoldLabels(GOLDLABELS_FILE);
		assignedLabels = testHelper.LoadWorkerAssignedLabels(LABELS_FILE);
		misclassificationCosts = testHelper.LoadMisclassificationCosts(COSTS_FILE);

		AbstractDawidSkene algorithm = new BatchDawidSkene();
		project = new NominalProject(algorithm);
		project.initializeCategories(categories);

		NominalData data = algorithm.getData();
		for (AssignedLabel<String> assign : assignedLabels) {
			data.addAssign(assign);
		}
		for (LObject<String> gold : correctLabels) {
			data.addGoldObject(gold);
		}
//		for (LObject<String> eval : evaluationLabels) {
//			data.addEvaluationObject(eval);
//		}
		algorithm.addMisclassificationCosts(misclassificationCosts);
		
		//init the test setup
		testSetup = new GALBaseScenarios.Setup(project, SUMMARY_FILE, TEST_RESULTS_FILE);
		initSetup(testSetup);
	}

	
	
}
