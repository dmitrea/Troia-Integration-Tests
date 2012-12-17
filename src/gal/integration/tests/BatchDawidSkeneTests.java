package gal.integration.tests;
import static org.junit.Assert.*;

import gal.integration.helpers.*;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.junit.BeforeClass;
import org.junit.Test;
import com.datascience.gal.*;

public class BatchDawidSkeneTests {
	
	public static String TESTDATA_BASEDIR = System.getProperty("user.dir").concat("//src//gal//integration//datasets");
	public static String TEST_DIR = "testComputeBatchDavidSkene";
	
	//input 
	public static String CATEGORIES_FILE = "categories.txt";
	public static String MISCLASSIFICATION_COSTS_FILE = "costs.txt";
	public static String GOLDLABELS_FILE = "correct.txt";
	public static String LABELS_FILE = "input.txt";
	
	public static String inputDir = TESTDATA_BASEDIR + File.separator + TEST_DIR + File.separator + "input";
	public static String CATEGORIES_FILE_PATH = inputDir + File.separator + CATEGORIES_FILE;
	public static String MISCLASSIFICATION_COSTS_FILE_PATH = inputDir + File.separator + MISCLASSIFICATION_COSTS_FILE;
	public static String GOLDLABELS_FILE_PATH = inputDir + File.separator +  GOLDLABELS_FILE;
	public static String LABELS_FILE_PATH =  inputDir + File.separator + LABELS_FILE;

	//output
	public static String outputDir = TESTDATA_BASEDIR + File.separator + TEST_DIR + File.separator + "output";
	public static String EXPECTED_SUMMARY_FILE =  outputDir + File.separator + "summary.txt";
	
	public static String PROJECT_ID = "12345";
	
	static Collection<Category> categories;
	static HashSet<MisclassificationCost> misclassificationCosts;
	static Collection<CorrectLabel> correctLabels;
	static Collection<AssignedLabel> assignedLabels;
	static BatchDawidSkene ds;
	static TestHelpers testHelper;
	static SummaryResultsParser summaryResultsParser;
	
	@BeforeClass
	public static void loadDSData(){
		testHelper = new TestHelpers();
		summaryResultsParser = new SummaryResultsParser();
		
		categories = testHelper.LoadCategories(CATEGORIES_FILE_PATH);
		ds = new BatchDawidSkene(PROJECT_ID, categories);
				
		misclassificationCosts = testHelper.LoadMisclassificationCosts(MISCLASSIFICATION_COSTS_FILE_PATH);
		ds.addMisclassificationCosts(misclassificationCosts);
				
		correctLabels = testHelper.LoadGoldLabels(GOLDLABELS_FILE_PATH);
		ds.addCorrectLabels(correctLabels);
				
		assignedLabels = testHelper.LoadWorkerAssignedLabels(LABELS_FILE_PATH);
		ds.addAssignedLabels(assignedLabels);
				
		//make the computations
		ds.estimate(1);
		
		//load the expected summary file
		summaryResultsParser.ParseSummaryFile(EXPECTED_SUMMARY_FILE);
	}

	@Test
	public void testSummaryFile_Data() {	
		HashMap<String, String> data = summaryResultsParser.getData();
		assertEquals(ds.getCategories().size(), Integer.parseInt(data.get("Categories")));
		assertEquals(ds.getNumberOfObjects(), Integer.parseInt(data.get("Objects in Data Set")));
		assertEquals(ds.getNumberOfWorkers() , Integer.parseInt(data.get("Workers in Data Set")));
		//assertEquals(ds., data.get("Labels Assigned by Workers"));
	}	
	
	
}
