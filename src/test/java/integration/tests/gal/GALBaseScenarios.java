package test.java.integration.tests.gal;

import com.datascience.core.base.AssignedLabel;
import com.datascience.core.base.LObject;
import com.datascience.core.base.Worker;
import com.datascience.core.nominal.NominalData;
import com.datascience.core.nominal.NominalProject;
import com.datascience.core.nominal.decision.*;
import com.datascience.core.results.WorkerResult;
import com.datascience.gal.AbstractDawidSkene;
import com.datascience.gal.Quality;
import com.datascience.gal.evaluation.DataEvaluator;
import com.datascience.gal.evaluation.WorkerEvaluator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import test.java.integration.helpers.FileWriters;
import test.java.integration.helpers.SummaryResultsParser;
import test.java.integration.helpers.TestHelpers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class GALBaseScenarios {

	public static int NO_ITERATIONS = 50;
	public static double EPSILON = 1e-5;
	public static String SUMMARY_FILE;
	public static String TEST_RESULTS_FILE;
	public static NominalProject project;
	public static NominalData data;
	public static AbstractDawidSkene ds;
	public static FileWriters fileWriter;
	public static TestHelpers testHelper;
	public static SummaryResultsParser summaryResultsParser;
	
	public static class Setup{

		public NominalProject project;
		public String summaryResultsFile;
		public String testResultsFile;
		
		public Setup(NominalProject project, String summaryFile, String resultsFile) {
			this.project = project;
			summaryResultsFile = summaryFile;
			testResultsFile = resultsFile;
		}		
	}

	public static void initSetup(Setup testSetup){
		project = testSetup.project;
		ds = (AbstractDawidSkene) project.getAlgorithm();
		data = project.getData();
		ds.estimate(EPSILON, NO_ITERATIONS);
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
	
	public double estimateMissclassificationCost(AbstractDawidSkene ds, ILabelProbabilityDistributionCalculator labelProbabilityDistributionCalculator, ILabelProbabilityDistributionCostCalculator labelProbabilityDistributionCostCalculator, IObjectLabelDecisionAlgorithm objectLabelDecisionAlgorithm) 
	{
		DecisionEngine decisionEngine = new DecisionEngine(labelProbabilityDistributionCalculator, labelProbabilityDistributionCostCalculator, objectLabelDecisionAlgorithm);
		NominalData data = ds.getData();
		Set<LObject<String>> objects = data.getObjects();
		double avgClassificationCost = 0.0;
		
		//compute the estimated misclassification cost for each object, using DS
		for (LObject<String> object : objects) {
			avgClassificationCost += decisionEngine.estimateMissclassificationCost(project, object);
		}
		
		//calculate the average
		avgClassificationCost = avgClassificationCost/objects.size();
		return avgClassificationCost;
	}
	
	public double evaluateMissclassificationCost(AbstractDawidSkene ds, String labelChoosingMethod, ILabelProbabilityDistributionCalculator labelProbabilityDistributionCalculator) 
	{
		DataEvaluator dataEvaluator = DataEvaluator.get(labelChoosingMethod, labelProbabilityDistributionCalculator);
		NominalData data = ds.getData();
		
		Set<LObject<String>> evaluationData = data.getEvaluationObjects();
		double avgCost = 0.0;
		
		//compute the evaluated misclassification cost for each evaluation datum
		Map<String, Double> evaluated = dataEvaluator.evaluate(project);
		for (Map.Entry<String, Double> entry : evaluated.entrySet()) {
			avgCost += entry.getValue();
		}
		
		//calculate the average
		avgCost = avgCost / evaluationData.size();
		return avgCost;
	}
	
	
	public double estimateCostToQuality(AbstractDawidSkene ds, ILabelProbabilityDistributionCalculator labelProbabilityDistributionCalculator, ILabelProbabilityDistributionCostCalculator labelProbabilityDistributionCostCalculator, IObjectLabelDecisionAlgorithm objectLabelDecisionAlgorithm) 
	{
		DecisionEngine decisionEngine = new DecisionEngine(labelProbabilityDistributionCalculator, labelProbabilityDistributionCostCalculator, objectLabelDecisionAlgorithm);
		Map <String, Double> costQuality = Quality.fromCosts(project, decisionEngine.estimateMissclassificationCosts(project));
		
		double avgQuality = 0.0;
		
		//compute the estimated quality cost for each object, using MV
		for (Map.Entry<String, Double> cQuality : costQuality.entrySet()) { 
			avgQuality += cQuality.getValue();
		}
		
		//calculate the average
		avgQuality /= costQuality.size();
		return avgQuality;
	}
	
	public double evaluateCostToQuality(AbstractDawidSkene ds, String labelChoosingMethod, ILabelProbabilityDistributionCalculator labelProbabilityDistributionCalculator) 
	{
		DataEvaluator dataEvaluator = DataEvaluator.get(labelChoosingMethod, labelProbabilityDistributionCalculator);

		Map <String, Double> costQuality = Quality.fromCosts(project, dataEvaluator.evaluate(project));
		double avgQuality = 0.0;
		
		//compute the estimated quality cost for each object, using MV
		for (Map.Entry<String, Double> cQuality : costQuality.entrySet()) { 
			avgQuality += cQuality.getValue();
		}
		
		//calculate the average
		avgQuality /= costQuality.size();
		return avgQuality;
	}
	
	
	public double estimateWorkerQuality(AbstractDawidSkene ds, String method, String estimationType) 
	{
		ILabelProbabilityDistributionCostCalculator labelProbabilityDistributionCostCalculator = LabelProbabilityDistributionCostCalculators.get(method);
		WorkerEstimator workerEstimator = new WorkerEstimator(labelProbabilityDistributionCostCalculator);
		Map<String, Double> result = new HashMap<String, Double>();
		Map<String, Integer> workerAssignedLabels = new HashMap<String, Integer>();

		
		for (Map.Entry<Worker<String>, WorkerResult> w : project.getResults().getWorkerResults().entrySet()){
			Worker<String> worker = w.getKey();
			result.put(worker.getName(), workerEstimator.getCost(project, worker));
			workerAssignedLabels.put(worker.getName(), worker.getAssigns().size());
		}

		Map <String, Double> workersQuality = Quality.fromCosts(project, result);
		double quality = 0.0;
		
		if (estimationType.equals("n"))
		{
			//compute the non weighted worker quality
			for (Map.Entry<String, Double> workerQuality : workersQuality.entrySet()) { 
				quality += workerQuality.getValue();
			}
		
			//calculate the average
			quality /= workersQuality.size();
			return quality;
		}
		else
		{
			//compute the weighted worker quality
			int totalNoLabels = 0;
			for (Map.Entry<String, Double> workerQuality : workersQuality.entrySet()) {
				quality += workerQuality.getValue() * workerAssignedLabels.get(workerQuality.getKey());
				totalNoLabels += workerAssignedLabels.get(workerQuality.getKey());
			}
			
			quality /= totalNoLabels;
			return quality;
		}
	}
	
	public double evaluateWorkerQuality(AbstractDawidSkene ds, String method, String estimationType) 
	{
		ILabelProbabilityDistributionCostCalculator labelProbabilityDistributionCostCalculator = LabelProbabilityDistributionCostCalculators.get(method);
		WorkerEvaluator workerEvaluator = new WorkerEvaluator(labelProbabilityDistributionCostCalculator);
		Map<String, Double> result = new HashMap<String, Double>();
		Map<String, Integer> workerAssignedLabels = new HashMap<String, Integer>();

		for (Map.Entry<Worker<String>, WorkerResult> w : project.getResults().getWorkerResults().entrySet()){
			Worker<String> worker = w.getKey();
			result.put(worker.getName(), workerEvaluator.getCost(project, worker));
			workerAssignedLabels.put(worker.getName(), worker.getAssigns().size());
		}

		Map <String, Double> workersQuality = Quality.fromCosts(project, result);
		double quality = 0.0;
		double denominator = 0.;
		
		if (estimationType.equals("n"))
		{
			//compute the non-weighted worker quality
			for (Map.Entry<String, Double> workerQuality : workersQuality.entrySet()) { 
				Double val = workerQuality.getValue();
				if (val == null || val.isNaN())
					continue;
				quality += val;
				denominator += 1.;
			}
		
			//calculate the average
			quality /= denominator;
			return quality;	
		}
	
		else
		{
			//compute the weighted worker quality
			int totalNoLabels = 0;
			for (Map.Entry<String, Double> workerQuality : workersQuality.entrySet()) {
				Double val = workerQuality.getValue();
				if (val == null || val.isNaN())
					continue;
				
				quality += val * workerAssignedLabels.get(workerQuality.getKey());
				totalNoLabels += workerAssignedLabels.get(workerQuality.getKey());
			}
			
			denominator += totalNoLabels;
			quality /= denominator;
			return quality;	
		}
	}
	
	@Test
	public void test_Data() {	
		HashMap<String, String> data = summaryResultsParser.getData();

		int expectedCategoriesNo = Integer.parseInt(data.get("Categories"));
		int actualCategoriesNo = ds.getData().getCategories().size();

		assertEquals(expectedCategoriesNo, actualCategoriesNo);
		fileWriter.writeToFile(TEST_RESULTS_FILE, "Categories," + expectedCategoriesNo + "," + actualCategoriesNo);
				
		int expectedObjectsNo = Integer.parseInt(data.get("Objects in Data Set"));
		int actualObjectsNo = ds.getData().getObjects().size();
		assertEquals(expectedObjectsNo, actualObjectsNo);
		fileWriter.writeToFile(TEST_RESULTS_FILE, "Objects in Data Set," + expectedObjectsNo + "," + actualObjectsNo);
		
		int expectedWorkersNo = Integer.parseInt(data.get("Workers in Data Set"));
		int actualWorkersNo = ds.getData().getWorkers().size();
		assertEquals(expectedWorkersNo, actualWorkersNo);
		fileWriter.writeToFile(TEST_RESULTS_FILE, "Workers in Data Set," + expectedWorkersNo + "," + actualWorkersNo);
		
		//get the labels
		int noAssignedLabels = 0;
		Set<LObject<String>> objects = ds.getData().getObjects();
		for (LObject<String> object : objects){
			noAssignedLabels +=	ds.getData().getAssignsForObject(object).size();
		}
		
		int expectedLabelsNo = Integer.parseInt(data.get("Labels Assigned by Workers"));
		assertEquals(expectedLabelsNo, noAssignedLabels);
		fileWriter.writeToFile(TEST_RESULTS_FILE, "Labels Assigned by Workers," + expectedLabelsNo + "," + noAssignedLabels);
	}	
	
	@Test
	public void test_ProbabilityDistributions_DS(){
		HashMap<String, String> data = summaryResultsParser.getDataQuality();
		ILabelProbabilityDistributionCalculator probDistributionCalculator = LabelProbabilityDistributionCalculators.get("DS");
		DecisionEngine decisionEngine = new DecisionEngine(probDistributionCalculator, null, null);
		//Map <String, Datum> objects = ds.getObjects();
		Set<LObject<String>> objects = ds.getData().getObjects();
		
		//init the categoryProbabilities hashmap
		HashMap <String, Double> categoryProbabilities = new HashMap<String, Double>();
		for (String categoryName : ds.getData().getCategoriesNames()) {
			categoryProbabilities.put(categoryName, 0.0);
		}

		//iterate through the datum objects and calculate the sum of the probabilities associated  to each category
		int noObjects = objects.size();
		for (LObject<String> object : objects){
		    Map <String, Double> objectProbabilities = decisionEngine.getPD(object, project);
		    for (String categoryName : objectProbabilities.keySet()){
		    	categoryProbabilities.put(categoryName, (categoryProbabilities.get(categoryName) + objectProbabilities.get(categoryName)));    	
		    }
		}
		
		//calculate the average probability value for each category
		for (String categoryName : ds.getData().getCategoriesNames()){
			categoryProbabilities.put(categoryName, categoryProbabilities.get(categoryName)/noObjects);
		}
		for (String categoryName : ds.getData().getCategoriesNames()){
			String metricName = "[DS_Pr[" + categoryName + "]] DS estimate for prior probability of category " + categoryName;
			String expectedCategoryProbability = data.get(metricName);
			String actualCategoryProbability = testHelper.format(categoryProbabilities.get(categoryName));
			fileWriter.writeToFile(TEST_RESULTS_FILE, "[DS_Pr[" + categoryName + "]]," + expectedCategoryProbability + "," + actualCategoryProbability);
			assertEquals(expectedCategoryProbability, actualCategoryProbability);
		}	
	}
	
	@Test
	public void test_ProbabilityDistributions_MV(){
		HashMap<String, String> data = summaryResultsParser.getDataQuality();
		ILabelProbabilityDistributionCalculator probDistributionCalculator = LabelProbabilityDistributionCalculators.get("MV");
		DecisionEngine decisionEngine = new DecisionEngine(probDistributionCalculator, null, null);
		Set<LObject<String>> objects = ds.getData().getObjects();
		
		//init the categoryProbabilities hashmap
		HashMap <String, Double> categoryProbabilities = new HashMap<String, Double>();
		for (String categoryName : ds.getData().getCategoriesNames()) {
			categoryProbabilities.put(categoryName, 0.0);
		}
		
		//iterate through the datum objects and calculate the sum of the probabilities associated  to each category
		int noObjects = objects.size();
		for (LObject<String> object : objects){
		    Map <String, Double> objectProbabilities = decisionEngine.getPD(object, project);
		    for (String categoryName : objectProbabilities.keySet()){
		    	categoryProbabilities.put(categoryName, (categoryProbabilities.get(categoryName) + objectProbabilities.get(categoryName)));    	
		    }
		}
		
		//calculate the average probability value for each category
		for (String categoryName : ds.getData().getCategoriesNames()){
			categoryProbabilities.put(categoryName, categoryProbabilities.get(categoryName)/noObjects);
		}
		
		for (String categoryName : ds.getData().getCategoriesNames()){
			String metricName = "[MV_Pr[" + categoryName + "]] Majority Vote estimate for prior probability of category " + categoryName;
			String expectedCategoryProbability = data.get(metricName);
			String actualCategoryProbability = testHelper.format(categoryProbabilities.get(categoryName));
			fileWriter.writeToFile(TEST_RESULTS_FILE, "[MV_Pr[" + categoryName + "]]," + expectedCategoryProbability + "," + actualCategoryProbability);
			assertEquals(expectedCategoryProbability, actualCategoryProbability);
		}	
	}
	
	
	@Test
	public void test_DataCost_Estm_DS_Exp() {	
		HashMap<String, String> data = summaryResultsParser.getDataQuality();
		ILabelProbabilityDistributionCalculator labelProbabilityDistributionCalculator = LabelProbabilityDistributionCalculators.get("DS");
		ILabelProbabilityDistributionCostCalculator labelProbabilityDistributionCostCalculator = LabelProbabilityDistributionCostCalculators.get("EXPECTEDCOST");
		
		double avgClassificationCost = estimateMissclassificationCost(ds, labelProbabilityDistributionCalculator, labelProbabilityDistributionCostCalculator, null);
		
		String expectedClassificationCost = data.get("[DataCost_Estm_DS_Exp] Estimated classification cost (DS_Exp metric)");
		String actualClassificationCost = testHelper.format(avgClassificationCost);
		fileWriter.writeToFile(TEST_RESULTS_FILE, "DataCost_Estm_DS_Exp," + expectedClassificationCost + "," + actualClassificationCost);
		assertEquals(expectedClassificationCost, actualClassificationCost);
	}	
	
	
	@Test
	public void test_DataCost_Estm_MV_Exp () {	
		HashMap<String, String> data = summaryResultsParser.getDataQuality();
		ILabelProbabilityDistributionCalculator labelProbabilityDistributionCalculator = LabelProbabilityDistributionCalculators.get("MV");
		ILabelProbabilityDistributionCostCalculator	labelProbabilityDistributionCostCalculator = LabelProbabilityDistributionCostCalculators.get("EXPECTEDCOST");

		double avgClassificationCost = estimateMissclassificationCost(ds, labelProbabilityDistributionCalculator, labelProbabilityDistributionCostCalculator, null);

		String expectedClassificationCost = data.get("[DataCost_Estm_MV_Exp] Estimated classification cost (MV_Exp metric)");
		String actualClassificationCost = testHelper.format(avgClassificationCost);
		fileWriter.writeToFile(TEST_RESULTS_FILE, "DataCost_Estm_MV_Exp," + expectedClassificationCost + "," + actualClassificationCost);
		assertEquals(expectedClassificationCost, actualClassificationCost);
	}	
	
	@Test
	public void test_DataCost_Estm_DS_ML() {	
		HashMap<String, String> data = summaryResultsParser.getDataQuality();
		ILabelProbabilityDistributionCalculator labelProbabilityDistributionCalculator = LabelProbabilityDistributionCalculators.get("DS");
		ILabelProbabilityDistributionCostCalculator	labelProbabilityDistributionCostCalculator = LabelProbabilityDistributionCostCalculators.get("MAXLIKELIHOOD");
		
		double avgClassificationCost = estimateMissclassificationCost(ds, labelProbabilityDistributionCalculator, labelProbabilityDistributionCostCalculator, null);

		String expectedClassificationCost = data.get("[DataCost_Estm_DS_ML] Estimated classification cost (DS_ML metric)");
		String actualClassificationCost = testHelper.format(avgClassificationCost);
		fileWriter.writeToFile(TEST_RESULTS_FILE, "DataCost_Estm_DS_ML," + expectedClassificationCost + "," + actualClassificationCost);
		assertEquals(expectedClassificationCost, actualClassificationCost);
		
	}	
	
	@Test
	public void test_DataCost_Estm_MV_ML() {	
		HashMap<String, String> data = summaryResultsParser.getDataQuality();
		ILabelProbabilityDistributionCalculator labelProbabilityDistributionCalculator = LabelProbabilityDistributionCalculators.get("MV");
		ILabelProbabilityDistributionCostCalculator	labelProbabilityDistributionCostCalculator = LabelProbabilityDistributionCostCalculators.get("MAXLIKELIHOOD");
		
		double avgClassificationCost = estimateMissclassificationCost(ds, labelProbabilityDistributionCalculator, labelProbabilityDistributionCostCalculator, null);

		String expectedClassificationCost = data.get("[DataCost_Estm_MV_ML] Estimated classification cost (MV_ML metric)");
		String actualClassificationCost = testHelper.format(avgClassificationCost);
		fileWriter.writeToFile(TEST_RESULTS_FILE, "DataCost_Estm_MV_ML," + expectedClassificationCost + "," + actualClassificationCost);
		assertEquals(expectedClassificationCost, actualClassificationCost);
	}	
	
	
	@Test
	public void test_DataCost_Estm_DS_Min() {	
		HashMap<String, String> data = summaryResultsParser.getDataQuality();
		ILabelProbabilityDistributionCalculator labelProbabilityDistributionCalculator = LabelProbabilityDistributionCalculators.get("DS");
		ILabelProbabilityDistributionCostCalculator	labelProbabilityDistributionCostCalculator = LabelProbabilityDistributionCostCalculators.get("MINCOST");
		
		double avgClassificationCost = estimateMissclassificationCost(ds, labelProbabilityDistributionCalculator, labelProbabilityDistributionCostCalculator, null);
		
		String expectedClassificationCost = data.get("[DataCost_Estm_DS_Min] Estimated classification cost (DS_Min metric)");
		String actualClassificationCost =  testHelper.format(avgClassificationCost);
		fileWriter.writeToFile(TEST_RESULTS_FILE, "DataCost_Estm_DS_Min," + expectedClassificationCost + "," + actualClassificationCost);
		assertEquals(expectedClassificationCost, actualClassificationCost);
	}	
	
	@Test
	public void test_DataCost_Estm_MV_Min() {	
		HashMap<String, String> data = summaryResultsParser.getDataQuality();
		ILabelProbabilityDistributionCalculator labelProbabilityDistributionCalculator = LabelProbabilityDistributionCalculators.get("MV");
		ILabelProbabilityDistributionCostCalculator	labelProbabilityDistributionCostCalculator = LabelProbabilityDistributionCostCalculators.get("MINCOST");
		
		double avgClassificationCost = estimateMissclassificationCost(ds, labelProbabilityDistributionCalculator, labelProbabilityDistributionCostCalculator, null);
		
		String expectedClassificationCost = data.get("[DataCost_Estm_MV_Min] Estimated classification cost (MV_Min metric)");
		String actualClassificationCost = testHelper.format(avgClassificationCost);
		fileWriter.writeToFile(TEST_RESULTS_FILE, "DataCost_Estm_MV_Min," + expectedClassificationCost + "," + actualClassificationCost);
		assertEquals(expectedClassificationCost, actualClassificationCost);
	}
	
	@Test
	public void test_DataCost_Estm_NoVote_Exp() {	
		HashMap<String, String> data = summaryResultsParser.getDataQuality();
		ILabelProbabilityDistributionCalculator labelProbabilityDistributionCalculator = LabelProbabilityDistributionCalculators.get("NOVOTE");
		ILabelProbabilityDistributionCostCalculator	labelProbabilityDistributionCostCalculator = LabelProbabilityDistributionCostCalculators.get("EXPECTEDCOST");

		double avgClassificationCost = estimateMissclassificationCost(ds, labelProbabilityDistributionCalculator, labelProbabilityDistributionCostCalculator, null);
		
		String expectedClassificationCost = data.get("[DataCost_Estm_NoVote_Exp] Baseline classification cost (random spammer)");
		String actualClassificationCost = testHelper.format(avgClassificationCost);
		fileWriter.writeToFile(TEST_RESULTS_FILE, "DataCost_Estm_NoVote_Exp," + expectedClassificationCost + "," + actualClassificationCost);
		assertEquals(expectedClassificationCost, actualClassificationCost);
	}	
	
	@Test
	public void test_DataCost_Estm_NoVote_Min() {	
		HashMap<String, String> data = summaryResultsParser.getDataQuality();
		ILabelProbabilityDistributionCalculator labelProbabilityDistributionCalculator = LabelProbabilityDistributionCalculators.get("NOVOTE");
		ILabelProbabilityDistributionCostCalculator	labelProbabilityDistributionCostCalculator = LabelProbabilityDistributionCostCalculators.get("MINCOST");
		
		double avgClassificationCost = estimateMissclassificationCost(ds, labelProbabilityDistributionCalculator, labelProbabilityDistributionCostCalculator, null);
		
		String expectedClassificationCost = data.get("[DataCost_Estm_NoVote_Min] Baseline classification cost (strategic spammer)");
		String actualClassificationCost = testHelper.format(avgClassificationCost);
		fileWriter.writeToFile(TEST_RESULTS_FILE, "DataCost_Estm_NoVote_Min," + expectedClassificationCost + "," + actualClassificationCost);
		assertEquals(expectedClassificationCost, actualClassificationCost);
	}	
	
	@Test
	public void test_DataCost_Eval_DS_ML() {	
		HashMap<String, String> data = summaryResultsParser.getDataQuality();
		ILabelProbabilityDistributionCalculator labelProbabilityDistributionCalculator = LabelProbabilityDistributionCalculators.get("DS");
		
		double avgClassificationCost = evaluateMissclassificationCost(ds, "MAXLIKELIHOOD", labelProbabilityDistributionCalculator);
		
		String expectedClassificationCost = data.get("[DataCost_Eval_DS_ML] Actual classification cost for EM, maximum likelihood classification");
		String actualClassificationCost = testHelper.format(avgClassificationCost);
		fileWriter.writeToFile(TEST_RESULTS_FILE, "DataCost_Eval_DS_ML," + expectedClassificationCost + "," + actualClassificationCost);
		assertEquals(expectedClassificationCost, actualClassificationCost);
	}	
	
	
	@Test
	public void test_DataCost_Eval_MV_ML() {	
		HashMap<String, String> data = summaryResultsParser.getDataQuality();
		ILabelProbabilityDistributionCalculator labelProbabilityDistributionCalculator = LabelProbabilityDistributionCalculators.get("MV");
		
		double avgClassificationCost = evaluateMissclassificationCost(ds, "MAXLIKELIHOOD", labelProbabilityDistributionCalculator);
		
		String expectedClassificationCost = data.get("[DataCost_Eval_MV_ML] Actual classification cost for majority vote classification");
		String actualClassificationCost = testHelper.format(avgClassificationCost);
		fileWriter.writeToFile(TEST_RESULTS_FILE, "DataCost_Eval_MV_ML," + expectedClassificationCost + "," + actualClassificationCost);
		assertEquals(expectedClassificationCost, actualClassificationCost);
	}	
	
	
	@Test
	public void test_DataCost_Eval_DS_Min() {	
		HashMap<String, String> data = summaryResultsParser.getDataQuality();
		ILabelProbabilityDistributionCalculator labelProbabilityDistributionCalculator = LabelProbabilityDistributionCalculators.get("DS");
		
		double avgClassificationCost = evaluateMissclassificationCost(ds, "MINCOST", labelProbabilityDistributionCalculator);
		
		String expectedClassificationCost = data.get("[DataCost_Eval_DS_Min] Actual classification cost for EM, min-cost classification");
		String actualClassificationCost = testHelper.format(avgClassificationCost);
		fileWriter.writeToFile(TEST_RESULTS_FILE, "DataCost_Eval_DS_Min," + expectedClassificationCost + "," + actualClassificationCost);
		assertEquals(expectedClassificationCost, actualClassificationCost);
	}	

	@Test
	public void test_DataCost_Eval_MV_Min() {	
		HashMap<String, String> data = summaryResultsParser.getDataQuality();
		ILabelProbabilityDistributionCalculator labelProbabilityDistributionCalculator = LabelProbabilityDistributionCalculators.get("MV");
		
		double avgClassificationCost = evaluateMissclassificationCost(ds, "MINCOST", labelProbabilityDistributionCalculator);
		
		String expectedClassificationCost = data.get("[DataCost_Eval_MV_Min] Actual classification cost for naive min-cost classification");
		String actualClassificationCost = testHelper.format(avgClassificationCost);
		fileWriter.writeToFile(TEST_RESULTS_FILE, "DataCost_Eval_MV_Min," + expectedClassificationCost + "," + actualClassificationCost);
		assertEquals(expectedClassificationCost, actualClassificationCost);
	}	
	
	@Test
	public void test_DataCost_Eval_DS_Soft() {	
		HashMap<String, String> data = summaryResultsParser.getDataQuality();
		ILabelProbabilityDistributionCalculator labelProbabilityDistributionCalculator = LabelProbabilityDistributionCalculators.get("DS");
		
		double avgClassificationCost = evaluateMissclassificationCost(ds, "SOFT", labelProbabilityDistributionCalculator);
		
		String expectedClassificationCost = data.get("[DataCost_Eval_DS_Soft] Actual classification cost for EM, soft-label classification");
		String actualClassificationCost = testHelper.format(avgClassificationCost);
		fileWriter.writeToFile(TEST_RESULTS_FILE, "DataCost_Eval_DS_Soft," + expectedClassificationCost + "," + actualClassificationCost);
		assertEquals(expectedClassificationCost, actualClassificationCost);
	}	
	
	@Test
	public void test_DataCost_Eval_MV_Soft() {	
		HashMap<String, String> data = summaryResultsParser.getDataQuality();
		ILabelProbabilityDistributionCalculator labelProbabilityDistributionCalculator = LabelProbabilityDistributionCalculators.get("MV");
		
		double avgClassificationCost = evaluateMissclassificationCost(ds, "SOFT", labelProbabilityDistributionCalculator);
		
		String expectedClassificationCost = data.get("[DataCost_Eval_MV_Soft] Actual classification cost for naive soft-label classification");
		String actualClassificationCost = testHelper.format(avgClassificationCost);
		fileWriter.writeToFile(TEST_RESULTS_FILE, "DataCost_Eval_MV_Soft," + expectedClassificationCost + "," + actualClassificationCost);
		assertEquals(expectedClassificationCost, actualClassificationCost);
	}	
	
	@Test
	public void test_DataQuality_Estm_DS_ML() {	
		HashMap<String, String> data = summaryResultsParser.getDataQuality();
		ILabelProbabilityDistributionCalculator labelProbabilityDistributionCalculator = LabelProbabilityDistributionCalculators.get("DS");
		ILabelProbabilityDistributionCostCalculator	labelProbabilityDistributionCostCalculator = LabelProbabilityDistributionCostCalculators.get("MAXLIKELIHOOD");
		
		double avgQuality =  estimateCostToQuality(ds, labelProbabilityDistributionCalculator, labelProbabilityDistributionCostCalculator, null);
		
		String expectedClassificationCost = data.get("[DataQuality_Estm_DS_ML] Estimated data quality, EM algorithm, maximum likelihood");
		String actualClassificationCost = testHelper.formatPercent(avgQuality);
		fileWriter.writeToFile(TEST_RESULTS_FILE, "DataQuality_Estm_DS_ML," + expectedClassificationCost + "," + actualClassificationCost);
		assertEquals(expectedClassificationCost, actualClassificationCost);
	}	
	
	@Test
	public void test_DataQuality_Estm_MV_ML() {	
		HashMap<String, String> data = summaryResultsParser.getDataQuality();
		ILabelProbabilityDistributionCalculator labelProbabilityDistributionCalculator = LabelProbabilityDistributionCalculators.get("MV");
		ILabelProbabilityDistributionCostCalculator	labelProbabilityDistributionCostCalculator = LabelProbabilityDistributionCostCalculators.get("MAXLIKELIHOOD");
		
		double avgQuality =  estimateCostToQuality(ds, labelProbabilityDistributionCalculator, labelProbabilityDistributionCostCalculator, null);
		
		String expectedClassificationCost = data.get("[DataQuality_Estm_MV_ML] Estimated data quality, naive majority label");
		String actualClassificationCost = testHelper.formatPercent(avgQuality);
		fileWriter.writeToFile(TEST_RESULTS_FILE, "DataQuality_Estm_MV_ML," + expectedClassificationCost + "," + actualClassificationCost);
		assertEquals(expectedClassificationCost, actualClassificationCost);
	}	
	
	@Test
	public void test_DataQuality_Estm_DS_Exp() {	
		HashMap<String, String> data = summaryResultsParser.getDataQuality();
		ILabelProbabilityDistributionCalculator labelProbabilityDistributionCalculator = LabelProbabilityDistributionCalculators.get("DS");
		ILabelProbabilityDistributionCostCalculator	labelProbabilityDistributionCostCalculator = LabelProbabilityDistributionCostCalculators.get("EXPECTEDCOST");
		
		double avgQuality =  estimateCostToQuality(ds, labelProbabilityDistributionCalculator, labelProbabilityDistributionCostCalculator, null);
		
		String expectedClassificationCost = data.get("[DataQuality_Estm_DS_Exp] Estimated data quality, EM algorithm, soft label");
		String actualClassificationCost = testHelper.formatPercent(avgQuality);
		fileWriter.writeToFile(TEST_RESULTS_FILE, "DataQuality_Estm_DS_Exp," + expectedClassificationCost + "," + actualClassificationCost);
		assertEquals(expectedClassificationCost, actualClassificationCost);
	}
	
	@Test
	public void test_DataQuality_Estm_MV_Exp() {	
		HashMap<String, String> data = summaryResultsParser.getDataQuality();
		ILabelProbabilityDistributionCalculator labelProbabilityDistributionCalculator = LabelProbabilityDistributionCalculators.get("MV");
		ILabelProbabilityDistributionCostCalculator	labelProbabilityDistributionCostCalculator = LabelProbabilityDistributionCostCalculators.get("EXPECTEDCOST");
		
		double avgQuality =  estimateCostToQuality(ds, labelProbabilityDistributionCalculator, labelProbabilityDistributionCostCalculator, null);
		
		String expectedClassificationCost = data.get("[DataQuality_Estm_MV_Exp] Estimated data quality, naive soft label");
		String actualClassificationCost = testHelper.formatPercent(avgQuality);
		fileWriter.writeToFile(TEST_RESULTS_FILE, "DataQuality_Estm_MV_Exp," + expectedClassificationCost + "," + actualClassificationCost);
		assertEquals(expectedClassificationCost, actualClassificationCost);
	}
	
	@Test
	public void test_DataQuality_Estm_DS_Min() {	
		HashMap<String, String> data = summaryResultsParser.getDataQuality();
		ILabelProbabilityDistributionCalculator labelProbabilityDistributionCalculator = LabelProbabilityDistributionCalculators.get("DS");
		ILabelProbabilityDistributionCostCalculator	labelProbabilityDistributionCostCalculator = LabelProbabilityDistributionCostCalculators.get("MINCOST");
		
		double avgQuality =  estimateCostToQuality(ds, labelProbabilityDistributionCalculator, labelProbabilityDistributionCostCalculator, null);
		
		String expectedClassificationCost = data.get("[DataQuality_Estm_DS_Min] Estimated data quality, EM algorithm, mincost");
		String actualClassificationCost = testHelper.formatPercent(avgQuality);
		fileWriter.writeToFile(TEST_RESULTS_FILE, "DataQuality_Estm_DS_Min," + expectedClassificationCost + "," + actualClassificationCost);
		assertEquals(expectedClassificationCost, actualClassificationCost);
	}
	
	@Test
	public void test_DataQuality_Estm_MV_Min() {	
		HashMap<String, String> data = summaryResultsParser.getDataQuality();
		ILabelProbabilityDistributionCalculator labelProbabilityDistributionCalculator = LabelProbabilityDistributionCalculators.get("MV");
		ILabelProbabilityDistributionCostCalculator	labelProbabilityDistributionCostCalculator = LabelProbabilityDistributionCostCalculators.get("MINCOST");
		
		double avgQuality =  estimateCostToQuality(ds, labelProbabilityDistributionCalculator, labelProbabilityDistributionCostCalculator, null);
		
		String expectedClassificationCost = data.get("[DataQuality_Estm_MV_Min] Estimated data quality, naive mincost label");
		String actualClassificationCost = testHelper.formatPercent(avgQuality);
		fileWriter.writeToFile(TEST_RESULTS_FILE, "DataQuality_Estm_MV_Min," + expectedClassificationCost + "," + actualClassificationCost);
		assertEquals(expectedClassificationCost, actualClassificationCost);
	}
	
	@Test
	public void test_DataQuality_Eval_DS_ML() {	
		HashMap<String, String> data = summaryResultsParser.getDataQuality();
		ILabelProbabilityDistributionCalculator labelProbabilityDistributionCalculator = LabelProbabilityDistributionCalculators.get("DS");
		
		double avgQuality =  evaluateCostToQuality(ds, "MAXLIKELIHOOD", labelProbabilityDistributionCalculator);
		
		String expectedClassificationCost = data.get("[DataQuality_Eval_DS_ML] Actual data quality, EM algorithm, maximum likelihood");
		String actualClassificationCost =  testHelper.formatPercent(avgQuality);
		fileWriter.writeToFile(TEST_RESULTS_FILE, "DataQuality_Eval_DS_ML," + expectedClassificationCost + "," + actualClassificationCost);
		assertEquals(expectedClassificationCost, actualClassificationCost);
	}
	
	@Test
	public void test_DataQuality_Eval_MV_ML() {	
		HashMap<String, String> data = summaryResultsParser.getDataQuality();
		ILabelProbabilityDistributionCalculator labelProbabilityDistributionCalculator = LabelProbabilityDistributionCalculators.get("MV");
		
		double avgQuality =  evaluateCostToQuality(ds, "MAXLIKELIHOOD", labelProbabilityDistributionCalculator);
		
		String expectedClassificationCost = data.get("[DataQuality_Eval_MV_ML] Actual data quality, naive majority label");
		String actualClassificationCost = testHelper.formatPercent(avgQuality);
		fileWriter.writeToFile(TEST_RESULTS_FILE, "DataQuality_Eval_MV_ML," + expectedClassificationCost + "," + actualClassificationCost);
		assertEquals(expectedClassificationCost, actualClassificationCost);
	}
	
	@Test
	public void test_DataQuality_Eval_DS_Min() {	
		HashMap<String, String> data = summaryResultsParser.getDataQuality();
		ILabelProbabilityDistributionCalculator labelProbabilityDistributionCalculator = LabelProbabilityDistributionCalculators.get("DS");
		
		double avgQuality =  evaluateCostToQuality(ds, "MINCOST", labelProbabilityDistributionCalculator);
		
		String expectedClassificationCost = data.get("[DataQuality_Eval_DS_Min] Actual data quality, EM algorithm, mincost");
		String actualClassificationCost = testHelper.formatPercent(avgQuality);
		fileWriter.writeToFile(TEST_RESULTS_FILE, "DataQuality_Eval_DS_Min," + expectedClassificationCost + "," + actualClassificationCost);
		assertEquals(expectedClassificationCost, actualClassificationCost);
	}
	
	@Test
	public void test_DataQuality_Eval_MV_Min() {	
		HashMap<String, String> data = summaryResultsParser.getDataQuality();
		ILabelProbabilityDistributionCalculator labelProbabilityDistributionCalculator = LabelProbabilityDistributionCalculators.get("MV");
		
		double avgQuality =  evaluateCostToQuality(ds, "MINCOST", labelProbabilityDistributionCalculator);
		
		String expectedClassificationCost = data.get("[DataQuality_Eval_MV_Min] Actual data quality, naive mincost label");
		String actualClassificationCost = testHelper.formatPercent(avgQuality);
		fileWriter.writeToFile(TEST_RESULTS_FILE, "DataQuality_Eval_MV_Min," + expectedClassificationCost + "," + actualClassificationCost);
		assertEquals(expectedClassificationCost, actualClassificationCost);
	}
	
	@Test
	public void test_DataQuality_Eval_DS_Soft() {	
		HashMap<String, String> data = summaryResultsParser.getDataQuality();
		ILabelProbabilityDistributionCalculator labelProbabilityDistributionCalculator = LabelProbabilityDistributionCalculators.get("DS");
		double avgQuality =  evaluateCostToQuality(ds, "SOFT", labelProbabilityDistributionCalculator);
		
		String expectedClassificationCost = data.get("[DataQuality_Eval_DS_Soft] Actual data quality, EM algorithm, soft label");
		String actualClassificationCost = testHelper.formatPercent(avgQuality);
		fileWriter.writeToFile(TEST_RESULTS_FILE, "DataQuality_Eval_DS_Soft," + expectedClassificationCost + "," + actualClassificationCost);
		assertEquals(expectedClassificationCost, actualClassificationCost);
	}
	
	@Test
	public void test_DataQuality_Eval_MV_Soft() {	
		HashMap<String, String> data = summaryResultsParser.getDataQuality();
		ILabelProbabilityDistributionCalculator labelProbabilityDistributionCalculator = LabelProbabilityDistributionCalculators.get("MV");
		double avgQuality =  evaluateCostToQuality(ds, "SOFT", labelProbabilityDistributionCalculator);
		
		String expectedClassificationCost = data.get("[DataQuality_Eval_MV_Soft] Actual data quality, naive soft label");
		String actualClassificationCost = testHelper.formatPercent(avgQuality);
		fileWriter.writeToFile(TEST_RESULTS_FILE, "DataQuality_Eval_MV_Soft," + expectedClassificationCost + "," + actualClassificationCost);
		assertEquals(expectedClassificationCost, actualClassificationCost);
	}
	
	
	@Test
	public void test_WorkerQuality_Estm_DS_Exp_n() {
		HashMap<String, String> data = summaryResultsParser.getWorkerQuality();
		double avgQuality =  estimateWorkerQuality(ds, "EXPECTEDCOST", "n");
		
		String expectedQuality = data.get("[WorkerQuality_Estm_DS_Exp_n] Estimated worker quality (non-weighted, DS_Exp metric)");
		String actualQuality = testHelper.formatPercent(avgQuality);
		fileWriter.writeToFile(TEST_RESULTS_FILE, "WorkerQuality_Estm_DS_Exp_n," + expectedQuality + "," + actualQuality);
		assertEquals(expectedQuality, actualQuality);
	}
	
	@Test
	public void test_WorkerQuality_Estm_DS_Exp_w() {
		HashMap<String, String> data = summaryResultsParser.getWorkerQuality();
		double avgQuality =  estimateWorkerQuality(ds, "EXPECTEDCOST", "w");
		
		String expectedQuality = data.get("[WorkerQuality_Estm_DS_Exp_w] Estimated worker quality (weighted, DS_Exp metric)");
		String actualQuality = testHelper.formatPercent(avgQuality);
		fileWriter.writeToFile(TEST_RESULTS_FILE, "WorkerQuality_Estm_DS_Exp_w," + expectedQuality + "," + actualQuality);
		assertEquals(expectedQuality, actualQuality);
	}
	
	@Test
	public void test_WorkerQuality_Estm_DS_ML_n() {
		HashMap<String, String> data = summaryResultsParser.getWorkerQuality();
		double avgQuality =  estimateWorkerQuality(ds, "MAXLIKELIHOOD", "n");
		
		String expectedQuality = data.get("[WorkerQuality_Estm_DS_ML_n] Estimated worker quality (non-weighted, DS_ML metric)");
		String actualQuality = testHelper.formatPercent(avgQuality);
		fileWriter.writeToFile(TEST_RESULTS_FILE, "WorkerQuality_Estm_DS_ML_n," + expectedQuality + "," + actualQuality);
		assertEquals(expectedQuality, actualQuality);
	}
	
	@Test
	public void test_WorkerQuality_Estm_DS_ML_w() {
		HashMap<String, String> data = summaryResultsParser.getWorkerQuality();
		double avgQuality =  estimateWorkerQuality(ds, "MAXLIKELIHOOD", "w");
		
		String expectedQuality = data.get("[WorkerQuality_Estm_DS_ML_w] Estimated worker quality (weighted, DS_ML metric)");
		String actualQuality = testHelper.formatPercent(avgQuality);
		fileWriter.writeToFile(TEST_RESULTS_FILE, "WorkerQuality_Estm_DS_ML_w," + expectedQuality + "," + actualQuality);
		assertEquals(expectedQuality, actualQuality);
	}
	
	@Test
	public void test_WorkerQuality_Estm_DS_Min_n() {
		HashMap<String, String> data = summaryResultsParser.getWorkerQuality();
		double avgQuality =  estimateWorkerQuality(ds, "MINCOST", "n");
		
		String expectedQuality = data.get("[WorkerQuality_Estm_DS_Min_n] Estimated worker quality (non-weighted, DS_Min metric)");
		String actualQuality = testHelper.formatPercent(avgQuality);
		fileWriter.writeToFile(TEST_RESULTS_FILE, "WorkerQuality_Estm_DS_Min_n," + expectedQuality + "," + actualQuality);
		assertEquals(expectedQuality, actualQuality);
	}

	
	@Test
	public void test_WorkerQuality_Estm_DS_Min_w() {
		HashMap<String, String> data = summaryResultsParser.getWorkerQuality();
		double avgQuality =  estimateWorkerQuality(ds, "MINCOST", "w");
		
		String expectedQuality = data.get("[WorkerQuality_Estm_DS_Min_w] Estimated worker quality (weighted, DS_Min metric)");
		String actualQuality = testHelper.formatPercent(avgQuality);
		fileWriter.writeToFile(TEST_RESULTS_FILE, "WorkerQuality_Estm_DS_Min_w," + expectedQuality + "," + actualQuality);
		assertEquals(expectedQuality, actualQuality);
	}

	@Test
	public void test_WorkerQuality_Eval_DS_Exp_n() {
		HashMap<String, String> data = summaryResultsParser.getWorkerQuality();
		double avgQuality =  evaluateWorkerQuality(ds, "EXPECTEDCOST", "n");
		
		String expectedQuality = data.get("[WorkerQuality_Eval_DS_Exp_n] Actual worker quality (non-weighted, DS_Exp metric)");
		String actualQuality = testHelper.formatPercent(avgQuality);
		fileWriter.writeToFile(TEST_RESULTS_FILE, "WorkerQuality_Eval_DS_Exp_n," + expectedQuality + "," + actualQuality);
		assertEquals(expectedQuality, actualQuality);
	}
	
	@Test
	public void test_WorkerQuality_Eval_DS_Exp_w() {
		HashMap<String, String> data = summaryResultsParser.getWorkerQuality();
		double avgQuality =  evaluateWorkerQuality(ds, "EXPECTEDCOST", "w");
		
		String expectedQuality = data.get("[WorkerQuality_Eval_DS_Exp_w] Actual worker quality (weighted, DS_Exp metric)");
		String actualQuality = testHelper.formatPercent(avgQuality);
		fileWriter.writeToFile(TEST_RESULTS_FILE, "WorkerQuality_Eval_DS_Exp_w," + expectedQuality + "," + actualQuality);
		assertEquals(expectedQuality, actualQuality);
	}

	
	@Test
	public void test_WorkerQuality_Eval_DS_ML_n() {
		HashMap<String, String> data = summaryResultsParser.getWorkerQuality();
		double avgQuality =  evaluateWorkerQuality(ds, "MAXLIKELIHOOD", "n");
		
		String expectedQuality = data.get("[WorkerQuality_Eval_DS_ML_n] Actual worker quality (non-weighted, DS_ML metric)");
		String actualQuality = testHelper.formatPercent(avgQuality);
		fileWriter.writeToFile(TEST_RESULTS_FILE, "WorkerQuality_Eval_DS_ML_n," + expectedQuality + "," + actualQuality);
		assertEquals(expectedQuality, actualQuality);
	}
	
	@Test
	public void test_WorkerQuality_Eval_DS_ML_w() {
		HashMap<String, String> data = summaryResultsParser.getWorkerQuality();
		double avgQuality =  evaluateWorkerQuality(ds, "MAXLIKELIHOOD", "w");
		
		String expectedQuality = data.get("[WorkerQuality_Eval_DS_ML_w] Actual worker quality (weighted, DS_ML metric)");
		String actualQuality = testHelper.formatPercent(avgQuality);
		fileWriter.writeToFile(TEST_RESULTS_FILE, "WorkerQuality_Eval_DS_ML_w," + expectedQuality + "," + actualQuality);
		assertEquals(expectedQuality, actualQuality);
	}
	
	@Test
	public void test_WorkerQuality_Eval_DS_Min_n() {
		HashMap<String, String> data = summaryResultsParser.getWorkerQuality();
		double avgQuality =  evaluateWorkerQuality(ds, "MINCOST", "n");
		
		String expectedQuality = data.get("[WorkerQuality_Eval_DS_Min_n] Actual worker quality (non-weighted, DS_Min metric)");
		String actualQuality = testHelper.formatPercent(avgQuality);
		fileWriter.writeToFile(TEST_RESULTS_FILE, "WorkerQuality_Eval_DS_Min_n," + expectedQuality + "," + actualQuality);
		assertEquals(expectedQuality, actualQuality);
	}
	
	@Test
	public void test_WorkerQuality_Eval_DS_Min_w() {
		HashMap<String, String> data = summaryResultsParser.getWorkerQuality();
		double avgQuality =  evaluateWorkerQuality(ds, "MINCOST", "w");
		
		String expectedQuality = data.get("[WorkerQuality_Eval_DS_Min_w] Actual worker quality (weighted, DS_Min metric)");
		String actualQuality = testHelper.formatPercent(avgQuality);
		fileWriter.writeToFile(TEST_RESULTS_FILE, "WorkerQuality_Eval_DS_Min_w," + expectedQuality + "," + actualQuality);
		assertEquals(expectedQuality, actualQuality);
	}
	
	@Test
	public void test_LabelsPerWorker() {
		HashMap<String, String> data = summaryResultsParser.getWorkerQuality();

		double noAssignedLabels = 0;
		Set<LObject<String>> objects = ds.getData().getObjects();
		for (LObject<String> object : objects){
			noAssignedLabels +=	ds.getData().getAssignsForObject(object).size();
		}
		
		double labelsPerWorker = noAssignedLabels/ds.getData().getWorkers().size();
		String expectedNoLabelsPerWorker = data.get("[Number of labels] Labels per worker");
		String actualNoLabelsPerWorker = testHelper.format(labelsPerWorker);
		fileWriter.writeToFile(TEST_RESULTS_FILE, "Labels per worker," + expectedNoLabelsPerWorker + "," + actualNoLabelsPerWorker);
		assertEquals(expectedNoLabelsPerWorker, actualNoLabelsPerWorker);	
	}
	
	@Test
	public void test_GoldTestsPerWorker() {
		HashMap<String, String> data = summaryResultsParser.getWorkerQuality();
		Set<Worker<String>>	workers  = ds.getData().getWorkers();
		Set<LObject<String>> objects = ds.getData().getObjects();
		double avgNoGoldTests = 0.0;
		Collection<String> categories = ds.getData().getCategoriesNames();
		
		for (Worker<String> worker : workers) {
			for (AssignedLabel<String> assign : worker.getAssigns()) {
				if (assign.getLobject().isGold()) {
					avgNoGoldTests += 1;
				}
			}
		}
	
		avgNoGoldTests = avgNoGoldTests/workers.size();
		String expectedNoGoldTestsPerWorker = data.get("[Gold Tests] Gold tests per worker");
		String actualNoGoldTestsPerWorker = testHelper.format(avgNoGoldTests);
		fileWriter.writeToFile(TEST_RESULTS_FILE, "Gold Tests per worker," + expectedNoGoldTestsPerWorker + "," + actualNoGoldTestsPerWorker);
		assertEquals(expectedNoGoldTestsPerWorker, actualNoGoldTestsPerWorker);
	}
	
}