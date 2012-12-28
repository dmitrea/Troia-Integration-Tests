package gal.integration.tests;

import static org.junit.Assert.assertEquals;

import gal.integration.helpers.FileWriters;
import gal.integration.helpers.SummaryResultsParser;
import gal.integration.helpers.TestHelpers;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.datascience.gal.AbstractDawidSkene;
import com.datascience.gal.Datum;
import com.datascience.gal.core.DataCostEstimator;
import com.datascience.gal.core.DataCostEvaluator;

public class Tests {
	
	public static String SUMMARY_FILE;
	public static String TEST_RESULTS_FILE;
	public static AbstractDawidSkene ds;
	public static FileWriters fileWriter;
	public static TestHelpers testHelper;
	public static SummaryResultsParser summaryResultsParser;
	
	
	public static class TestSetup{
		public AbstractDawidSkene abstractDS;
		public String summaryResultsFile;
		public String testResultsFile;
		
		public TestSetup(AbstractDawidSkene ds, String summaryFile, String resultsFile) {
			abstractDS = ds;
			summaryResultsFile = summaryFile;
			testResultsFile = resultsFile;
		}		
	}
	
	public static void initSetup(TestSetup testSetup){
		ds = testSetup.abstractDS;
		SUMMARY_FILE = testSetup.summaryResultsFile;
		TEST_RESULTS_FILE = testSetup.testResultsFile;
		
		testHelper = new TestHelpers();

		//prepare the test results file
		fileWriter = new FileWriters();
		fileWriter.createNewFile(TEST_RESULTS_FILE);
		fileWriter.writeToFile(TEST_RESULTS_FILE, "Metric,GAL value,Troia value");
		
		summaryResultsParser = new SummaryResultsParser();
		summaryResultsParser.ParseSummaryFile(SUMMARY_FILE);
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
	public void testSummaryFile_DataQuality_DataCost_Estm_DS_Exp() {	
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
		String expectedClassificationCost = data.get("[DataCost_Estm_DS_Exp] Estimated classification cost (DS_Exp metric)");
		String actualClassificationCost = testHelper.format(avgClassificationCost);
		fileWriter.writeToFile(TEST_RESULTS_FILE, "DataCost_Estm_DS_Exp," + expectedClassificationCost + "," + actualClassificationCost);
		assertEquals(expectedClassificationCost, actualClassificationCost);
	}	
	
	@Test
	public void testSummaryFile_DataQuality_DataCost_Estm_MV_Exp () {	
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
		String expectedClassificationCost = data.get("[DataCost_Estm_MV_Exp] Estimated classification cost (MV_Exp metric)");
		String actualClassificationCost = testHelper.format(avgClassificationCost);
		fileWriter.writeToFile(TEST_RESULTS_FILE, "DataCost_Estm_MV_Exp," + expectedClassificationCost + "," + actualClassificationCost);
		assertEquals(expectedClassificationCost, actualClassificationCost);
	}	
	
	@Test
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
		String expectedClassificationCost = data.get("[DataCost_Estm_DS_Min] Estimated classification cost (DS_Min metric)");
		String actualClassificationCost =  testHelper.format(avgClassificationCost);
		fileWriter.writeToFile(TEST_RESULTS_FILE, "DataCost_Estm_DS_Min," + expectedClassificationCost + "," + actualClassificationCost);
		assertEquals(expectedClassificationCost, actualClassificationCost);
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
		String expectedClassificationCost = data.get("[DataCost_Estm_MV_Min] Estimated classification cost (MV_Min metric)");
		String actualClassificationCost = testHelper.format(avgClassificationCost);
		fileWriter.writeToFile(TEST_RESULTS_FILE, "DataCost_Estm_MV_Min," + expectedClassificationCost + "," + actualClassificationCost);
		assertEquals(expectedClassificationCost, actualClassificationCost);
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
		String expectedClassificationCost = data.get("[DataCost_Eval_DS_Min] Actual classification cost for EM, min-cost classification");
		String actualClassificationCost = testHelper.format(avgClassificationCost);
		fileWriter.writeToFile(TEST_RESULTS_FILE, "DataCost_Eval_DS_Min," + expectedClassificationCost + "," + actualClassificationCost);
		assertEquals(expectedClassificationCost, actualClassificationCost);
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
		String expectedClassificationCost = data.get("[DataCost_Eval_MV_Min] Actual classification cost for naive min-cost classification");
		String actualClassificationCost = testHelper.format(avgClassificationCost);
		fileWriter.writeToFile(TEST_RESULTS_FILE, "DataCost_Eval_MV_Min," + expectedClassificationCost + "," + actualClassificationCost);
		assertEquals(expectedClassificationCost, actualClassificationCost);
	}	

}
