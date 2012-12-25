package gal.integration.tests;
import static org.junit.Assert.*;

import gal.integration.helpers.*;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import com.datascience.gal.*;
import com.datascience.gal.core.*;

public class IncrementalDawidSkeneTests {
	
	public static String TESTDATA_BASEDIR = System.getProperty("user.dir").concat("//src//gal//integration//datasets");
	public static String TEST_DIR = "testComputeDS";
	
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
	static IncrementalDawidSkene ds;
	static TestHelpers testHelper;
	static SummaryResultsParser summaryResultsParser;
	
	@BeforeClass
	public static void loadDSData(){
		testHelper = new TestHelpers();
		summaryResultsParser = new SummaryResultsParser();
		
		categories = testHelper.LoadCategories(CATEGORIES_FILE_PATH);
		ds = new IncrementalDawidSkene(PROJECT_ID, categories);
				
		misclassificationCosts = testHelper.LoadMisclassificationCosts(MISCLASSIFICATION_COSTS_FILE_PATH);
		ds.addMisclassificationCosts(misclassificationCosts);
				
		correctLabels = testHelper.LoadGoldLabels(GOLDLABELS_FILE_PATH);
		ds.addCorrectLabels(correctLabels);
				
		assignedLabels = testHelper.LoadWorkerAssignedLabels(LABELS_FILE_PATH);
		ds.addAssignedLabels(assignedLabels);
		
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

	@Test
	public void testSummaryFile_DataQuality_DataCost_Estm_DS_ML() {	
		HashMap<String, String> data = summaryResultsParser.getDataQuality();
		DataCostEstimator dataCostEstimator = DataCostEstimator.getInstance();
		Map<String, Datum> objects = ds.getObjects();
		double avgClassificationCost = 0.0;
		
		//compute the estimated misclassification cost for each object, using DS
		for (Map.Entry<String, Datum> object : objects.entrySet()) { 
			Datum obj = object.getValue();
			avgClassificationCost += dataCostEstimator.estimateMissclassificationCost(ds, "ExpectedCost", obj.getName());
		}
		
		//calculate the average
		avgClassificationCost = avgClassificationCost/objects.size();
		assertEquals(testHelper.format(avgClassificationCost), data.get("[DataCost_Estm_DS_ML] Estimated classification cost (DS_ML metric)"));
	}	
	
	@Test
	public void testSummaryFile_DataQuality_DataCost_Estm_MV_ML() {	
		HashMap<String, String> data = summaryResultsParser.getDataQuality();
		DataCostEstimator dataCostEstimator = DataCostEstimator.getInstance();
		Map<String, Datum> objects = ds.getObjects();
		double avgClassificationCost = 0.0;
		
		//compute the estimated misclassification cost for each object, using MV
		for (Map.Entry<String, Datum> object : objects.entrySet()) { 
			Datum obj = object.getValue();
			avgClassificationCost += dataCostEstimator.estimateMissclassificationCost(ds, "ExpectedMVCost", obj.getName());
		}
		
		//calculate the average
		avgClassificationCost = avgClassificationCost/objects.size();
		assertEquals(testHelper.format(avgClassificationCost), data.get("[DataCost_Estm_MV_ML] Estimated classification cost (MV_ML metric)"));
	}	
	
	//@Test
	public void testSummaryFile_DataQuality_DataCost_Estm_DS_Min() {	
		HashMap<String, String> data = summaryResultsParser.getDataQuality();
		DataCostEstimator dataCostEstimator = DataCostEstimator.getInstance();
		Map<String, Datum> objects = ds.getObjects();
		double avgClassificationCost = 0.0;
		
		//compute the estimated misclassification cost for each object, using DS
		for (Map.Entry<String, Datum> object : objects.entrySet()) { 
			Datum obj = object.getValue();
			avgClassificationCost += dataCostEstimator.estimateMissclassificationCost(ds, "MinCost", obj.getName());
		}
		
		//calculate the average
		avgClassificationCost = avgClassificationCost/objects.size();
		assertEquals(testHelper.format(avgClassificationCost), data.get("[DataCost_Estm_DS_Min] Estimated classification cost (DS_Min metric)"));
	}	
	
	@Test
	public void testSummaryFile_DataQuality_DataCost_Estm_MV_Min() {	
		HashMap<String, String> data = summaryResultsParser.getDataQuality();
		DataCostEstimator dataCostEstimator = DataCostEstimator.getInstance();
		Map<String, Datum> objects = ds.getObjects();
		double avgClassificationCost = 0.0;
		
		//compute the estimated misclassification cost for each object, using MV
		for (Map.Entry<String, Datum> object : objects.entrySet()) { 
			Datum obj = object.getValue();
			avgClassificationCost += dataCostEstimator.estimateMissclassificationCost(ds, "MinMVCost", obj.getName());
		}
		
		//calculate the average
		avgClassificationCost = avgClassificationCost/objects.size();
		assertEquals(testHelper.format(avgClassificationCost), data.get("[DataCost_Estm_MV_Min] Estimated classification cost (MV_Min metric)"));
	}	
	
	@Test
	public void testSummaryFile_DataQuality_DataCost_Eval_DS_Min() {	
		HashMap<String, String> data = summaryResultsParser.getDataQuality();
		DataCostEvaluator dataCostEvaluator = DataCostEvaluator.getInstance();
		Map<String, Datum> objects = ds.getObjects();
		double avgClassificationCost = 0.0;
		
		//compute the evaluated misclassification cost for each object, using DS
		for (Map.Entry<String, Datum> object : objects.entrySet()) { 
			Datum obj = object.getValue();
			avgClassificationCost += dataCostEvaluator.evaluateMissclassificationCost(ds, "MinCost", obj.getName());
		}
		
		//calculate the average
		avgClassificationCost = avgClassificationCost/objects.size();
		assertEquals(testHelper.format(avgClassificationCost), data.get("[DataCost_Eval_DS_Min] Actual classification cost for EM, min-cost classification"));
	}	

	@Test
	public void testSummaryFile_DataQuality_DataCost_Eval_MV_Min() {	
		HashMap<String, String> data = summaryResultsParser.getDataQuality();
		DataCostEvaluator dataCostEvaluator = DataCostEvaluator.getInstance();
		Map<String, Datum> objects = ds.getObjects();
		double avgClassificationCost = 0.0;
		
		//compute the evaluated misclassification cost for each object, using MV
		for (Map.Entry<String, Datum> object : objects.entrySet()) { 
			Datum obj = object.getValue();
			avgClassificationCost += dataCostEvaluator.evaluateMissclassificationCost(ds, "MinMVCost", obj.getName());
		}
		
		//calculate the average
		avgClassificationCost = avgClassificationCost/objects.size();
		assertEquals(testHelper.format(avgClassificationCost), data.get("[DataCost_Eval_MV_Min] Actual classification cost for naive min-cost classification"));
	}	

	

	
}
