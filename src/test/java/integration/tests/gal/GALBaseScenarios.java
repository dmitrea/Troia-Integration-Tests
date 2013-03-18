package test.java.integration.tests.gal;

import com.datascience.core.base.AssignedLabel;
import com.datascience.core.base.Category;
import com.datascience.core.base.LObject;
import com.datascience.core.base.Worker;
import com.datascience.core.nominal.NominalData;
import com.datascience.core.nominal.NominalProject;
import com.datascience.core.nominal.decision.*;
import com.datascience.core.results.WorkerResult;
import com.datascience.gal.BatchDawidSkene;
import com.datascience.gal.MisclassificationCost;
import com.datascience.gal.Quality;
import com.datascience.gal.evaluation.DataEvaluator;
import com.datascience.gal.evaluation.WorkerEvaluator;
import org.junit.Test;
import test.java.integration.helpers.FileWriters;
import test.java.integration.helpers.SummaryResultsParser;
import test.java.integration.helpers.TestHelpers;
import test.java.integration.helpers.TestSettings;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class GALBaseScenarios {

	public final static int NO_ITERATIONS = 10;
	public final static double EPSILON = 1e-6;

	public final static String DATA_BASE_DIR = TestSettings.GAL_TESTDATA_BASEDIR;

	protected Collection<Category> categories;
	protected Collection<LObject<String>> correctLabels;
	protected Collection<AssignedLabel<String>> assignedLabels;
	protected Collection<LObject<String>> evaluationLabels;
	protected Set<MisclassificationCost> costs;

	protected TestHelpers testHelper = new TestHelpers();
	protected FileWriters fileWriter;
	protected SummaryResultsParser summaryResultsParser;

	private NominalProject project;
	private NominalData data;
	private BatchDawidSkene algorithm;

	public void setUp() {
		algorithm = new BatchDawidSkene();
		algorithm.setEpsilon(EPSILON);
		algorithm.setIterations(NO_ITERATIONS);
		project = new NominalProject(algorithm);
		data = project.getData();

		project.initializeCategories(categories);
		algorithm.addMisclassificationCosts(costs);
		for (LObject<String> gold : correctLabels) {
			data.addGoldObject(gold);
		}
		for (AssignedLabel<String> assign : assignedLabels) {
			data.addAssign(assign);
		}
		for (AssignedLabel<String> assign : data.getAssigns()) {
			Worker<String> worker = data.getWorker(assign.getWorker().getName());
			assign.setWorker(worker);
			worker.addAssign(assign);
			LObject<String> object = data.getObject(assign.getLobject().getName());
			assign.setLobject(object);
 		}
		for (LObject<String> eval : evaluationLabels) {
			data.addEvaluationObject(eval);
		}

		//algorithm.addMisclassificationCosts(costs);
		algorithm.compute();

		fileWriter.write("Metric,GAL value,Troia value");
	}

	public double estimateMissclassificationCost(
			ILabelProbabilityDistributionCostCalculator labelProbabilityDistributionCostCalculator,
			IObjectLabelDecisionAlgorithm objectLabelDecisionAlgorithm) {
		DecisionEngine decisionEngine = new DecisionEngine(labelProbabilityDistributionCostCalculator,
				objectLabelDecisionAlgorithm);
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
	
	public double evaluateMissclassificationCost(String labelChoosingMethod) {
		DataEvaluator dataEvaluator = new DataEvaluator(labelChoosingMethod);

		//compute the evaluated misclassification cost
		Map<String, Double> evaluated = dataEvaluator.evaluate(project);

		double avgCost = 0.0;

		for (Map.Entry<String, Double> entry : evaluated.entrySet()) {
			avgCost += entry.getValue();
		}
		
		//calculate the average cost
		avgCost = avgCost / data.getEvaluationObjects().size();
		return avgCost;
	}
	
	
	public double estimateCostToQuality(
			ILabelProbabilityDistributionCostCalculator labelProbabilityDistributionCostCalculator,
			IObjectLabelDecisionAlgorithm objectLabelDecisionAlgorithm) {
		DecisionEngine decisionEngine = new DecisionEngine(labelProbabilityDistributionCostCalculator,
				objectLabelDecisionAlgorithm);
		Map<String, Double> costQuality = Quality.fromCosts(project, decisionEngine.estimateMissclassificationCosts(project));
		
		double avgQuality = 0.0;
		
		//compute the estimated quality cost for each object, using MV
		for (Map.Entry<String, Double> cQuality : costQuality.entrySet()) { 
			avgQuality += cQuality.getValue();
		}
		
		//calculate the average
		avgQuality /= costQuality.size();
		return avgQuality;
	}
	
	public double evaluateCostToQuality(String labelChoosingMethod) {
		DataEvaluator dataEvaluator = new DataEvaluator(labelChoosingMethod);

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
	
	
	public double estimateWorkerQuality(String method, String estimationType) {
		ILabelProbabilityDistributionCostCalculator labelProbabilityDistributionCostCalculator =
				LabelProbabilityDistributionCostCalculators.get(method);
		WorkerEstimator workerEstimator = new WorkerEstimator(labelProbabilityDistributionCostCalculator);
		Map<String, Double> result = new HashMap<String, Double>();
		Map<String, Integer> workerAssignedLabels = new HashMap<String, Integer>();

		for (Map.Entry<Worker<String>, WorkerResult> w : project.getResults().getWorkerResults().entrySet()) {
			Worker<String> worker = w.getKey();
			result.put(worker.getName(), workerEstimator.getCost(project, worker));
			workerAssignedLabels.put(worker.getName(), worker.getAssigns().size());
		}

		Map <String, Double> workersQuality = Quality.fromCosts(project, result);
		double quality = 0.0;
		
		if (estimationType.equals("n")) {
			//compute the non weighted worker quality
			for (Map.Entry<String, Double> workerQuality : workersQuality.entrySet()) { 
				quality += workerQuality.getValue();
			}
		
			//calculate the average
			quality /= workersQuality.size();
			return quality;
		} else {
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
	
	public double evaluateWorkerQuality(String method, String estimationType) {
		ILabelProbabilityDistributionCostCalculator labelProbabilityDistributionCostCalculator =
				LabelProbabilityDistributionCostCalculators.get(method);
		WorkerEvaluator workerEvaluator = new WorkerEvaluator(labelProbabilityDistributionCostCalculator);
		Map<String, Double> result = new HashMap<String, Double>();
		Map<String, Integer> workerAssignedLabels = new HashMap<String, Integer>();

		for (Map.Entry<Worker<String>, WorkerResult> w : project.getResults().getWorkerResults().entrySet()) {
			Worker<String> worker = w.getKey();
			result.put(worker.getName(), workerEvaluator.getCost(project, worker));
			workerAssignedLabels.put(worker.getName(), worker.getAssigns().size());
		}

		Map <String, Double> workersQuality = Quality.fromCosts(project, result);
		double quality = 0.0;
		double denominator = 0.;
		
		if (estimationType.equals("n")) {
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
		} else {
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
		int actualCategoriesNo = this.data.getCategories().size();

		assertEquals(expectedCategoriesNo, actualCategoriesNo);
		fileWriter.write("Categories," + expectedCategoriesNo + "," + actualCategoriesNo);
				
		int expectedObjectsNo = Integer.parseInt(data.get("Objects in Data Set"));
		int actualObjectsNo = this.data.getObjects().size();
		assertEquals(expectedObjectsNo, actualObjectsNo);
		fileWriter.write("Objects in Data Set," + expectedObjectsNo + "," + actualObjectsNo);
		
		int expectedWorkersNo = Integer.parseInt(data.get("Workers in Data Set"));
		int actualWorkersNo = this.data.getWorkers().size();
		assertEquals(expectedWorkersNo, actualWorkersNo);
		fileWriter.write("Workers in Data Set," + expectedWorkersNo + "," + actualWorkersNo);
		
		//get the labels
		int noAssignedLabels = 0;
		Set<LObject<String>> objects = this.data.getObjects();
		for (LObject<String> object : objects) {
			noAssignedLabels +=	this.data.getAssignsForObject(object).size();
		}
		
		int expectedLabelsNo = Integer.parseInt(data.get("Labels Assigned by Workers"));
		assertEquals(expectedLabelsNo, noAssignedLabels);
		fileWriter.write("Labels Assigned by Workers," + expectedLabelsNo + "," + noAssignedLabels);
	}	
	
	@Test
	public void test_ProbabilityDistributions_DS() {
		HashMap<String, String> dataQuality = summaryResultsParser.getDataQuality();
		Set<LObject<String>> objects = data.getObjects();
		
		//init the categoryProbabilities hashmap
		HashMap <String, Double> categoryProbabilities = new HashMap<String, Double>();
		for (String categoryName : algorithm.getData().getCategoriesNames()) {
			categoryProbabilities.put(categoryName, 0.0);
		}

		//iterate through the datum objects and calculate the sum of the probabilities associated  to each category
		int noObjects = objects.size();
		for (LObject<String> object : objects) {
		    Map <String, Double> objectProbabilities = project.getObjectResults(object).getCategoryProbabilites();
		    for (String categoryName : objectProbabilities.keySet()) {
		    	categoryProbabilities.put(categoryName, (categoryProbabilities.get(categoryName) + objectProbabilities.get(categoryName)));    	
		    }
		}
		
		//calculate the average probability value for each category
		for (String categoryName : data.getCategoriesNames()) {
			categoryProbabilities.put(categoryName, categoryProbabilities.get(categoryName) / noObjects);
		}
		for (String categoryName : data.getCategoriesNames()){
			String metricName = "[DS_Pr[" + categoryName + "]] DS estimate for prior probability of category " + categoryName;
			String expectedCategoryProbability = dataQuality.get(metricName);
			String actualCategoryProbability = testHelper.format(categoryProbabilities.get(categoryName));
			fileWriter.write("[DS_Pr[" + categoryName + "]]," + expectedCategoryProbability + "," + actualCategoryProbability);
			assertEquals(expectedCategoryProbability, actualCategoryProbability);
		}	
	}
	
	@Test
	public void test_ProbabilityDistributions_MV(){
		HashMap<String, String> dataQuality = summaryResultsParser.getDataQuality();
		Set<LObject<String>> objects = algorithm.getData().getObjects();
		
		//init the categoryProbabilities hashmap
		HashMap <String, Double> categoryProbabilities = new HashMap<String, Double>();
		for (String categoryName : algorithm.getData().getCategoriesNames()) {
			categoryProbabilities.put(categoryName, 0.0);
		}
		
		//iterate through the datum objects and calculate the sum of the probabilities associated  to each category
		int noObjects = objects.size();
		for (LObject<String> object : objects){
		    Map <String, Double> objectProbabilities = project.getObjectResults(object).getCategoryProbabilites();
		    for (String categoryName : objectProbabilities.keySet()){
		    	categoryProbabilities.put(categoryName, (categoryProbabilities.get(categoryName) + objectProbabilities.get(categoryName)));    	
		    }
		}
		
		//calculate the average probability value for each category
		for (String categoryName : algorithm.getData().getCategoriesNames()){
			categoryProbabilities.put(categoryName, categoryProbabilities.get(categoryName) / noObjects);
		}
		
		for (String categoryName : algorithm.getData().getCategoriesNames()){
			String metricName = "[MV_Pr[" + categoryName + "]] Majority Vote estimate for prior probability of category " + categoryName;
			String expectedCategoryProbability = dataQuality.get(metricName);
			String actualCategoryProbability = testHelper.format(categoryProbabilities.get(categoryName));
			fileWriter.write("[MV_Pr[" + categoryName + "]]," + expectedCategoryProbability + "," + actualCategoryProbability);
			assertEquals(expectedCategoryProbability, actualCategoryProbability);
		}	
	}
	
	
	@Test
	public void test_DataCost_Estm_DS_Exp() {	
		HashMap<String, String> dataQuality = summaryResultsParser.getDataQuality();
		ILabelProbabilityDistributionCostCalculator labelProbabilityDistributionCostCalculator = LabelProbabilityDistributionCostCalculators.get("EXPECTEDCOST");
		
		double avgClassificationCost = estimateMissclassificationCost(labelProbabilityDistributionCostCalculator, null);
		
		String expectedClassificationCost = dataQuality.get("[DataCost_Estm_DS_Exp] Estimated classification cost (DS_Exp metric)");
		String actualClassificationCost = testHelper.format(avgClassificationCost);
		fileWriter.write("DataCost_Estm_DS_Exp," + expectedClassificationCost + "," + actualClassificationCost);
		assertEquals(expectedClassificationCost, actualClassificationCost);
	}	
	
	
	@Test
	public void test_DataCost_Estm_MV_Exp () {	
		HashMap<String, String> dataQuality = summaryResultsParser.getDataQuality();
		ILabelProbabilityDistributionCostCalculator	labelProbabilityDistributionCostCalculator = LabelProbabilityDistributionCostCalculators.get("EXPECTEDCOST");

		double avgClassificationCost = estimateMissclassificationCost(labelProbabilityDistributionCostCalculator, null);

		String expectedClassificationCost = dataQuality.get("[DataCost_Estm_MV_Exp] Estimated classification cost (MV_Exp metric)");
		String actualClassificationCost = testHelper.format(avgClassificationCost);
		fileWriter.write("DataCost_Estm_MV_Exp," + expectedClassificationCost + "," + actualClassificationCost);
		assertEquals(expectedClassificationCost, actualClassificationCost);
	}	
	
	@Test
	public void test_DataCost_Estm_DS_ML() {	
		HashMap<String, String> dataQuality = summaryResultsParser.getDataQuality();
		ILabelProbabilityDistributionCostCalculator	labelProbabilityDistributionCostCalculator = LabelProbabilityDistributionCostCalculators.get("MAXLIKELIHOOD");
		
		double avgClassificationCost = estimateMissclassificationCost(labelProbabilityDistributionCostCalculator, null);

		String expectedClassificationCost = dataQuality.get("[DataCost_Estm_DS_ML] Estimated classification cost (DS_ML metric)");
		String actualClassificationCost = testHelper.format(avgClassificationCost);
		fileWriter.write("DataCost_Estm_DS_ML," + expectedClassificationCost + "," + actualClassificationCost);
		assertEquals(expectedClassificationCost, actualClassificationCost);
		
	}	
	
	@Test
	public void test_DataCost_Estm_MV_ML() {	
		HashMap<String, String> dataQuality = summaryResultsParser.getDataQuality();
		ILabelProbabilityDistributionCostCalculator	labelProbabilityDistributionCostCalculator = LabelProbabilityDistributionCostCalculators.get("MAXLIKELIHOOD");
		
		double avgClassificationCost = estimateMissclassificationCost(labelProbabilityDistributionCostCalculator, null);

		String expectedClassificationCost = dataQuality.get("[DataCost_Estm_MV_ML] Estimated classification cost (MV_ML metric)");
		String actualClassificationCost = testHelper.format(avgClassificationCost);
		fileWriter.write("DataCost_Estm_MV_ML," + expectedClassificationCost + "," + actualClassificationCost);
		assertEquals(expectedClassificationCost, actualClassificationCost);
	}	
	
	
	@Test
	public void test_DataCost_Estm_DS_Min() {	
		HashMap<String, String> dataQuality = summaryResultsParser.getDataQuality();
		ILabelProbabilityDistributionCostCalculator	labelProbabilityDistributionCostCalculator = LabelProbabilityDistributionCostCalculators.get("MINCOST");
		
		double avgClassificationCost = estimateMissclassificationCost(labelProbabilityDistributionCostCalculator, null);
		
		String expectedClassificationCost = dataQuality.get("[DataCost_Estm_DS_Min] Estimated classification cost (DS_Min metric)");
		String actualClassificationCost =  testHelper.format(avgClassificationCost);
		fileWriter.write("DataCost_Estm_DS_Min," + expectedClassificationCost + "," + actualClassificationCost);
		assertEquals(expectedClassificationCost, actualClassificationCost);
	}	
	
	@Test
	public void test_DataCost_Estm_MV_Min() {	
		HashMap<String, String> dataQuality = summaryResultsParser.getDataQuality();
		ILabelProbabilityDistributionCostCalculator	labelProbabilityDistributionCostCalculator = LabelProbabilityDistributionCostCalculators.get("MINCOST");
		
		double avgClassificationCost = estimateMissclassificationCost(labelProbabilityDistributionCostCalculator, null);
		
		String expectedClassificationCost = dataQuality.get("[DataCost_Estm_MV_Min] Estimated classification cost (MV_Min metric)");
		String actualClassificationCost = testHelper.format(avgClassificationCost);
		fileWriter.write("DataCost_Estm_MV_Min," + expectedClassificationCost + "," + actualClassificationCost);
		assertEquals(expectedClassificationCost, actualClassificationCost);
	}
	
	@Test
	public void test_DataCost_Estm_NoVote_Exp() {	
		HashMap<String, String> dataQuality= summaryResultsParser.getDataQuality();
		ILabelProbabilityDistributionCostCalculator	labelProbabilityDistributionCostCalculator = LabelProbabilityDistributionCostCalculators.get("EXPECTEDCOST");

		double avgClassificationCost = estimateMissclassificationCost(labelProbabilityDistributionCostCalculator, null);
		
		String expectedClassificationCost = dataQuality.get("[DataCost_Estm_NoVote_Exp] Baseline classification cost (random spammer)");
		String actualClassificationCost = testHelper.format(avgClassificationCost);
		fileWriter.write("DataCost_Estm_NoVote_Exp," + expectedClassificationCost + "," + actualClassificationCost);
		assertEquals(expectedClassificationCost, actualClassificationCost);
	}	
	
	@Test
	public void test_DataCost_Estm_NoVote_Min() {	
		HashMap<String, String> dataQuality = summaryResultsParser.getDataQuality();
		ILabelProbabilityDistributionCostCalculator	labelProbabilityDistributionCostCalculator = LabelProbabilityDistributionCostCalculators.get("MINCOST");
		
		double avgClassificationCost = estimateMissclassificationCost(labelProbabilityDistributionCostCalculator, null);
		
		String expectedClassificationCost = dataQuality.get("[DataCost_Estm_NoVote_Min] Baseline classification cost (strategic spammer)");
		String actualClassificationCost = testHelper.format(avgClassificationCost);
		fileWriter.write("DataCost_Estm_NoVote_Min," + expectedClassificationCost + "," + actualClassificationCost);
		assertEquals(expectedClassificationCost, actualClassificationCost);
	}	
	
	@Test
	public void test_DataCost_Eval_DS_ML() {	
		HashMap<String, String> dataQuality = summaryResultsParser.getDataQuality();
		
		double avgClassificationCost = evaluateMissclassificationCost("MAXLIKELIHOOD");
		
		String expectedClassificationCost = dataQuality.get("[DataCost_Eval_DS_ML] Actual classification cost for EM, maximum likelihood classification");
		String actualClassificationCost = testHelper.format(avgClassificationCost);
		fileWriter.write("DataCost_Eval_DS_ML," + expectedClassificationCost + "," + actualClassificationCost);
		assertEquals(expectedClassificationCost, actualClassificationCost);
	}	
	
	
	@Test
	public void test_DataCost_Eval_MV_ML() {	
		HashMap<String, String> dataQuality = summaryResultsParser.getDataQuality();
		
		double avgClassificationCost = evaluateMissclassificationCost("MAXLIKELIHOOD");
		
		String expectedClassificationCost = dataQuality.get("[DataCost_Eval_MV_ML] Actual classification cost for majority vote classification");
		String actualClassificationCost = testHelper.format(avgClassificationCost);
		fileWriter.write("DataCost_Eval_MV_ML," + expectedClassificationCost + "," + actualClassificationCost);
		assertEquals(expectedClassificationCost, actualClassificationCost);
	}	
	
	
	@Test
	public void test_DataCost_Eval_DS_Min() {	
		HashMap<String, String> data = summaryResultsParser.getDataQuality();
		
		double avgClassificationCost = evaluateMissclassificationCost("MINCOST");
		
		String expectedClassificationCost = data.get("[DataCost_Eval_DS_Min] Actual classification cost for EM, min-cost classification");
		String actualClassificationCost = testHelper.format(avgClassificationCost);
		fileWriter.write("DataCost_Eval_DS_Min," + expectedClassificationCost + "," + actualClassificationCost);
		assertEquals(expectedClassificationCost, actualClassificationCost);
	}	

	@Test
	public void test_DataCost_Eval_MV_Min() {	
		HashMap<String, String> dataQuality = summaryResultsParser.getDataQuality();
		
		double avgClassificationCost = evaluateMissclassificationCost("MINCOST");
		
		String expectedClassificationCost = dataQuality.get("[DataCost_Eval_MV_Min] Actual classification cost for naive min-cost classification");
		String actualClassificationCost = testHelper.format(avgClassificationCost);
		fileWriter.write("DataCost_Eval_MV_Min," + expectedClassificationCost + "," + actualClassificationCost);
		assertEquals(expectedClassificationCost, actualClassificationCost);
	}	
	
	@Test
	public void test_DataCost_Eval_DS_Soft() {	
		HashMap<String, String> dataQuality = summaryResultsParser.getDataQuality();
		
		double avgClassificationCost = evaluateMissclassificationCost("SOFT");
		
		String expectedClassificationCost = dataQuality.get("[DataCost_Eval_DS_Soft] Actual classification cost for EM, soft-label classification");
		String actualClassificationCost = testHelper.format(avgClassificationCost);
		fileWriter.write("DataCost_Eval_DS_Soft," + expectedClassificationCost + "," + actualClassificationCost);
		assertEquals(expectedClassificationCost, actualClassificationCost);
	}	
	
	@Test
	public void test_DataCost_Eval_MV_Soft() {	
		HashMap<String, String> dataQuality = summaryResultsParser.getDataQuality();
		
		double avgClassificationCost = evaluateMissclassificationCost("SOFT");
		
		String expectedClassificationCost = dataQuality.get("[DataCost_Eval_MV_Soft] Actual classification cost for naive soft-label classification");
		String actualClassificationCost = testHelper.format(avgClassificationCost);
		fileWriter.write("DataCost_Eval_MV_Soft," + expectedClassificationCost + "," + actualClassificationCost);
		assertEquals(expectedClassificationCost, actualClassificationCost);
	}	
	
	@Test
	public void test_DataQuality_Estm_DS_ML() {	
		HashMap<String, String> dataQuality = summaryResultsParser.getDataQuality();
		ILabelProbabilityDistributionCostCalculator	labelProbabilityDistributionCostCalculator = LabelProbabilityDistributionCostCalculators.get("MAXLIKELIHOOD");
		
		double avgQuality =  estimateCostToQuality(labelProbabilityDistributionCostCalculator, null);
		
		String expectedClassificationCost = dataQuality.get("[DataQuality_Estm_DS_ML] Estimated data quality, EM algorithm, maximum likelihood");
		String actualClassificationCost = testHelper.formatPercent(avgQuality);
		fileWriter.write("DataQuality_Estm_DS_ML," + expectedClassificationCost + "," + actualClassificationCost);
		assertEquals(expectedClassificationCost, actualClassificationCost);
	}	
	
	@Test
	public void test_DataQuality_Estm_MV_ML() {	
		HashMap<String, String> dataQuality = summaryResultsParser.getDataQuality();
		ILabelProbabilityDistributionCostCalculator	labelProbabilityDistributionCostCalculator = LabelProbabilityDistributionCostCalculators.get("MAXLIKELIHOOD");
		
		double avgQuality =  estimateCostToQuality(labelProbabilityDistributionCostCalculator, null);
		
		String expectedClassificationCost = dataQuality.get("[DataQuality_Estm_MV_ML] Estimated data quality, naive majority label");
		String actualClassificationCost = testHelper.formatPercent(avgQuality);
		fileWriter.write("DataQuality_Estm_MV_ML," + expectedClassificationCost + "," + actualClassificationCost);
		assertEquals(expectedClassificationCost, actualClassificationCost);
	}	
	
	@Test
	public void test_DataQuality_Estm_DS_Exp() {	
		HashMap<String, String> dataQuality = summaryResultsParser.getDataQuality();
		ILabelProbabilityDistributionCostCalculator	labelProbabilityDistributionCostCalculator = LabelProbabilityDistributionCostCalculators.get("EXPECTEDCOST");
		
		double avgQuality =  estimateCostToQuality(labelProbabilityDistributionCostCalculator, null);
		
		String expectedClassificationCost = dataQuality.get("[DataQuality_Estm_DS_Exp] Estimated data quality, EM algorithm, soft label");
		String actualClassificationCost = testHelper.formatPercent(avgQuality);
		fileWriter.write("DataQuality_Estm_DS_Exp," + expectedClassificationCost + "," + actualClassificationCost);
		assertEquals(expectedClassificationCost, actualClassificationCost);
	}
	
	@Test
	public void test_DataQuality_Estm_MV_Exp() {	
		HashMap<String, String> dataQuality = summaryResultsParser.getDataQuality();
		ILabelProbabilityDistributionCostCalculator	labelProbabilityDistributionCostCalculator = LabelProbabilityDistributionCostCalculators.get("EXPECTEDCOST");
		
		double avgQuality =  estimateCostToQuality(labelProbabilityDistributionCostCalculator, null);
		
		String expectedClassificationCost = dataQuality.get("[DataQuality_Estm_MV_Exp] Estimated data quality, naive soft label");
		String actualClassificationCost = testHelper.formatPercent(avgQuality);
		fileWriter.write("DataQuality_Estm_MV_Exp," + expectedClassificationCost + "," + actualClassificationCost);
		assertEquals(expectedClassificationCost, actualClassificationCost);
	}
	
	@Test
	public void test_DataQuality_Estm_DS_Min() {	
		HashMap<String, String> dataQuality = summaryResultsParser.getDataQuality();
		ILabelProbabilityDistributionCostCalculator	labelProbabilityDistributionCostCalculator = LabelProbabilityDistributionCostCalculators.get("MINCOST");
		
		double avgQuality =  estimateCostToQuality(labelProbabilityDistributionCostCalculator, null);
		
		String expectedClassificationCost = dataQuality.get("[DataQuality_Estm_DS_Min] Estimated data quality, EM algorithm, mincost");
		String actualClassificationCost = testHelper.formatPercent(avgQuality);
		fileWriter.write("DataQuality_Estm_DS_Min," + expectedClassificationCost + "," + actualClassificationCost);
		assertEquals(expectedClassificationCost, actualClassificationCost);
	}
	
	@Test
	public void test_DataQuality_Estm_MV_Min() {	
		HashMap<String, String> dataQuality = summaryResultsParser.getDataQuality();
		ILabelProbabilityDistributionCostCalculator	labelProbabilityDistributionCostCalculator = LabelProbabilityDistributionCostCalculators.get("MINCOST");
		
		double avgQuality =  estimateCostToQuality(labelProbabilityDistributionCostCalculator, null);
		
		String expectedClassificationCost = dataQuality.get("[DataQuality_Estm_MV_Min] Estimated data quality, naive mincost label");
		String actualClassificationCost = testHelper.formatPercent(avgQuality);
		fileWriter.write("DataQuality_Estm_MV_Min," + expectedClassificationCost + "," + actualClassificationCost);
		assertEquals(expectedClassificationCost, actualClassificationCost);
	}
	
	@Test
	public void test_DataQuality_Eval_DS_ML() {	
		HashMap<String, String> dataQuality = summaryResultsParser.getDataQuality();
		
		double avgQuality =  evaluateCostToQuality("MAXLIKELIHOOD");
		
		String expectedClassificationCost = dataQuality.get("[DataQuality_Eval_DS_ML] Actual data quality, EM algorithm, maximum likelihood");
		String actualClassificationCost =  testHelper.formatPercent(avgQuality);
		fileWriter.write("DataQuality_Eval_DS_ML," + expectedClassificationCost + "," + actualClassificationCost);
		assertEquals(expectedClassificationCost, actualClassificationCost);
	}
	
	@Test
	public void test_DataQuality_Eval_MV_ML() {	
		HashMap<String, String> dataQuality = summaryResultsParser.getDataQuality();
		
		double avgQuality =  evaluateCostToQuality("MAXLIKELIHOOD");
		
		String expectedClassificationCost = dataQuality.get("[DataQuality_Eval_MV_ML] Actual data quality, naive majority label");
		String actualClassificationCost = testHelper.formatPercent(avgQuality);
		fileWriter.write("DataQuality_Eval_MV_ML," + expectedClassificationCost + "," + actualClassificationCost);
		assertEquals(expectedClassificationCost, actualClassificationCost);
	}
	
	@Test
	public void test_DataQuality_Eval_DS_Min() {	
		HashMap<String, String> dataQuality = summaryResultsParser.getDataQuality();
		
		double avgQuality =  evaluateCostToQuality("MINCOST");
		
		String expectedClassificationCost = dataQuality.get("[DataQuality_Eval_DS_Min] Actual data quality, EM algorithm, mincost");
		String actualClassificationCost = testHelper.formatPercent(avgQuality);
		fileWriter.write("DataQuality_Eval_DS_Min," + expectedClassificationCost + "," + actualClassificationCost);
		assertEquals(expectedClassificationCost, actualClassificationCost);
	}
	
	@Test
	public void test_DataQuality_Eval_MV_Min() {	
		HashMap<String, String> dataQuality = summaryResultsParser.getDataQuality();
		
		double avgQuality =  evaluateCostToQuality("MINCOST");
		
		String expectedClassificationCost = dataQuality.get("[DataQuality_Eval_MV_Min] Actual data quality, naive mincost label");
		String actualClassificationCost = testHelper.formatPercent(avgQuality);
		fileWriter.write("DataQuality_Eval_MV_Min," + expectedClassificationCost + "," + actualClassificationCost);
		assertEquals(expectedClassificationCost, actualClassificationCost);
	}
	
	@Test
	public void test_DataQuality_Eval_DS_Soft() {	
		HashMap<String, String> dataQuality = summaryResultsParser.getDataQuality();
		double avgQuality =  evaluateCostToQuality("SOFT");
		
		String expectedClassificationCost = dataQuality.get("[DataQuality_Eval_DS_Soft] Actual data quality, EM algorithm, soft label");
		String actualClassificationCost = testHelper.formatPercent(avgQuality);
		fileWriter.write("DataQuality_Eval_DS_Soft," + expectedClassificationCost + "," + actualClassificationCost);
		assertEquals(expectedClassificationCost, actualClassificationCost);
	}
	
	@Test
	public void test_DataQuality_Eval_MV_Soft() {	
		HashMap<String, String> dataQuality = summaryResultsParser.getDataQuality();
		double avgQuality =  evaluateCostToQuality("SOFT");
		
		String expectedClassificationCost = dataQuality.get("[DataQuality_Eval_MV_Soft] Actual data quality, naive soft label");
		String actualClassificationCost = testHelper.formatPercent(avgQuality);
		fileWriter.write("DataQuality_Eval_MV_Soft," + expectedClassificationCost + "," + actualClassificationCost);
		assertEquals(expectedClassificationCost, actualClassificationCost);
	}
	
	
	@Test
	public void test_WorkerQuality_Estm_DS_Exp_n() {
		HashMap<String, String> workerQuality = summaryResultsParser.getWorkerQuality();
		double avgQuality =  estimateWorkerQuality("EXPECTEDCOST", "n");
		
		String expectedQuality = workerQuality.get("[WorkerQuality_Estm_DS_Exp_n] Estimated worker quality (non-weighted, DS_Exp metric)");
		String actualQuality = testHelper.formatPercent(avgQuality);
		fileWriter.write("WorkerQuality_Estm_DS_Exp_n," + expectedQuality + "," + actualQuality);
		assertEquals(expectedQuality, actualQuality);
	}
	
	@Test
	public void test_WorkerQuality_Estm_DS_Exp_w() {
		HashMap<String, String> workerQuality = summaryResultsParser.getWorkerQuality();
		double avgQuality =  estimateWorkerQuality("EXPECTEDCOST", "w");
		
		String expectedQuality = workerQuality.get("[WorkerQuality_Estm_DS_Exp_w] Estimated worker quality (weighted, DS_Exp metric)");
		String actualQuality = testHelper.formatPercent(avgQuality);
		fileWriter.write("WorkerQuality_Estm_DS_Exp_w," + expectedQuality + "," + actualQuality);
		assertEquals(expectedQuality, actualQuality);
	}
	
	@Test
	public void test_WorkerQuality_Estm_DS_ML_n() {
		HashMap<String, String> workerQuality = summaryResultsParser.getWorkerQuality();
		double avgQuality =  estimateWorkerQuality("MAXLIKELIHOOD", "n");
		
		String expectedQuality = workerQuality.get("[WorkerQuality_Estm_DS_ML_n] Estimated worker quality (non-weighted, DS_ML metric)");
		String actualQuality = testHelper.formatPercent(avgQuality);
		fileWriter.write("WorkerQuality_Estm_DS_ML_n," + expectedQuality + "," + actualQuality);
		assertEquals(expectedQuality, actualQuality);
	}
	
	@Test
	public void test_WorkerQuality_Estm_DS_ML_w() {
		HashMap<String, String> workerQuality = summaryResultsParser.getWorkerQuality();
		double avgQuality =  estimateWorkerQuality("MAXLIKELIHOOD", "w");
		
		String expectedQuality = workerQuality.get("[WorkerQuality_Estm_DS_ML_w] Estimated worker quality (weighted, DS_ML metric)");
		String actualQuality = testHelper.formatPercent(avgQuality);
		fileWriter.write("WorkerQuality_Estm_DS_ML_w," + expectedQuality + "," + actualQuality);
		assertEquals(expectedQuality, actualQuality);
	}
	
	@Test
	public void test_WorkerQuality_Estm_DS_Min_n() {
		HashMap<String, String> workerQuality = summaryResultsParser.getWorkerQuality();
		double avgQuality =  estimateWorkerQuality("MINCOST", "n");
		
		String expectedQuality = workerQuality.get("[WorkerQuality_Estm_DS_Min_n] Estimated worker quality (non-weighted, DS_Min metric)");
		String actualQuality = testHelper.formatPercent(avgQuality);
		fileWriter.write("WorkerQuality_Estm_DS_Min_n," + expectedQuality + "," + actualQuality);
		assertEquals(expectedQuality, actualQuality);
	}

	
	@Test
	public void test_WorkerQuality_Estm_DS_Min_w() {
		HashMap<String, String> workerQuality = summaryResultsParser.getWorkerQuality();
		double avgQuality =  estimateWorkerQuality("MINCOST", "w");
		
		String expectedQuality = workerQuality.get("[WorkerQuality_Estm_DS_Min_w] Estimated worker quality (weighted, DS_Min metric)");
		String actualQuality = testHelper.formatPercent(avgQuality);
		fileWriter.write("WorkerQuality_Estm_DS_Min_w," + expectedQuality + "," + actualQuality);
		assertEquals(expectedQuality, actualQuality);
	}

	@Test
	public void test_WorkerQuality_Eval_DS_Exp_n() {
		HashMap<String, String> workerQuality = summaryResultsParser.getWorkerQuality();
		double avgQuality =  evaluateWorkerQuality("EXPECTEDCOST", "n");
		
		String expectedQuality = workerQuality.get("[WorkerQuality_Eval_DS_Exp_n] Actual worker quality (non-weighted, DS_Exp metric)");
		String actualQuality = testHelper.formatPercent(avgQuality);
		fileWriter.write("WorkerQuality_Eval_DS_Exp_n," + expectedQuality + "," + actualQuality);
		assertEquals(expectedQuality, actualQuality);
	}
	
	@Test
	public void test_WorkerQuality_Eval_DS_Exp_w() {
		HashMap<String, String> workerQuality = summaryResultsParser.getWorkerQuality();
		double avgQuality =  evaluateWorkerQuality("EXPECTEDCOST", "w");
		
		String expectedQuality = workerQuality.get("[WorkerQuality_Eval_DS_Exp_w] Actual worker quality (weighted, DS_Exp metric)");
		String actualQuality = testHelper.formatPercent(avgQuality);
		fileWriter.write("WorkerQuality_Eval_DS_Exp_w," + expectedQuality + "," + actualQuality);
		assertEquals(expectedQuality, actualQuality);
	}

	
	@Test
	public void test_WorkerQuality_Eval_DS_ML_n() {
		HashMap<String, String> workerQuality = summaryResultsParser.getWorkerQuality();
		double avgQuality =  evaluateWorkerQuality("MAXLIKELIHOOD", "n");
		
		String expectedQuality = workerQuality.get("[WorkerQuality_Eval_DS_ML_n] Actual worker quality (non-weighted, DS_ML metric)");
		String actualQuality = testHelper.formatPercent(avgQuality);
		fileWriter.write("WorkerQuality_Eval_DS_ML_n," + expectedQuality + "," + actualQuality);
		assertEquals(expectedQuality, actualQuality);
	}
	
	@Test
	public void test_WorkerQuality_Eval_DS_ML_w() {
		HashMap<String, String> workerQuality = summaryResultsParser.getWorkerQuality();
		double avgQuality =  evaluateWorkerQuality("MAXLIKELIHOOD", "w");
		
		String expectedQuality = workerQuality.get("[WorkerQuality_Eval_DS_ML_w] Actual worker quality (weighted, DS_ML metric)");
		String actualQuality = testHelper.formatPercent(avgQuality);
		fileWriter.write("WorkerQuality_Eval_DS_ML_w," + expectedQuality + "," + actualQuality);
		assertEquals(expectedQuality, actualQuality);
	}
	
	@Test
	public void test_WorkerQuality_Eval_DS_Min_n() {
		HashMap<String, String> workerQuality = summaryResultsParser.getWorkerQuality();
		double avgQuality =  evaluateWorkerQuality("MINCOST", "n");

		String expectedQuality = workerQuality.get("[WorkerQuality_Eval_DS_Min_n] Actual worker quality (non-weighted, DS_Min metric)");
		String actualQuality = testHelper.formatPercent(avgQuality);
		fileWriter.write("WorkerQuality_Eval_DS_Min_n," + expectedQuality + "," + actualQuality);
		assertEquals(expectedQuality, actualQuality);
	}
	
	@Test
	public void test_WorkerQuality_Eval_DS_Min_w() {
		HashMap<String, String> workerQuality = summaryResultsParser.getWorkerQuality();
		double avgQuality =  evaluateWorkerQuality("MINCOST", "w");
		
		String expectedQuality = workerQuality.get("[WorkerQuality_Eval_DS_Min_w] Actual worker quality (weighted, DS_Min metric)");
		String actualQuality = testHelper.formatPercent(avgQuality);
		fileWriter.write("WorkerQuality_Eval_DS_Min_w," + expectedQuality + "," + actualQuality);
		assertEquals(expectedQuality, actualQuality);
	}
	
	@Test
	public void test_LabelsPerWorker() {
		HashMap<String, String> workerQuality = summaryResultsParser.getWorkerQuality();
		double noAssignedLabels = 0.0;
		Set<LObject<String>> objects = data.getObjects();
		for (LObject<String> object : objects){
			noAssignedLabels +=	data.getAssignsForObject(object).size();
		}
		double labelsPerWorker = noAssignedLabels / data.getWorkers().size();
		String expectedNoLabelsPerWorker = workerQuality.get("[Number of labels] Labels per worker");
		String actualNoLabelsPerWorker = testHelper.format(labelsPerWorker);
		fileWriter.write("Labels per worker," + expectedNoLabelsPerWorker + "," + actualNoLabelsPerWorker);
		assertEquals(expectedNoLabelsPerWorker, actualNoLabelsPerWorker);	
	}
	
	@Test
	public void test_GoldTestsPerWorker() {
		HashMap<String, String> workerQuality = summaryResultsParser.getWorkerQuality();
		double avgNoGoldTests = 0.0;
		Set<Worker<String>>	workers  = data.getWorkers();
		for (Worker<String> worker : workers) {
			for (AssignedLabel<String> assign : worker.getAssigns()) {
				if (assign.getLobject().isGold()) {
					avgNoGoldTests += 1;
				}
			}
		}
		avgNoGoldTests = avgNoGoldTests / workers.size();
		String expectedNoGoldTestsPerWorker = workerQuality.get("[Gold Tests] Gold tests per worker");
		String actualNoGoldTestsPerWorker = testHelper.format(avgNoGoldTests);
		fileWriter.write("Gold Tests per worker," + expectedNoGoldTestsPerWorker + "," + actualNoGoldTestsPerWorker);
		assertEquals(expectedNoGoldTestsPerWorker, actualNoGoldTestsPerWorker);
	}

	public void loadData(String categoriesPath, String goldLabelsPath, String assignedLabelsPath,
						 String evaluationLabelsPath, String costsPath) {
		categories = loadCategories(categoriesPath);
		correctLabels = loadGoldLabels(goldLabelsPath);
		assignedLabels = loadAssignedLabels(assignedLabelsPath);
		evaluationLabels = loadEvaluationLabels(evaluationLabelsPath);
		costs = loadCosts(costsPath);
	}

	public void loadData(String inputDir) {
		loadData(inputDir + "categories.txt", inputDir + "correct.txt", inputDir + "input.txt", inputDir + "evaluation.txt", inputDir + "costs.txt");
	}

	public Collection<Category> loadCategories(String categoriesPath) {
		return testHelper.LoadCategories(categoriesPath);
	}

	public Collection<LObject<String>> loadGoldLabels(String goldLabelsPath) {
		return testHelper.LoadGoldLabels(goldLabelsPath);
	}

	public Collection<LObject<String>> loadEvaluationLabels(String evaluationLabelsPath) {
		return testHelper.LoadEvaluationLabels(evaluationLabelsPath);
	}

	public Collection<AssignedLabel<String>> loadAssignedLabels(String assignedLabelsPath) {
		return testHelper.LoadWorkerAssignedLabels(assignedLabelsPath);
	}

	public Set<MisclassificationCost> loadCosts(String costsPath) {
		return testHelper.LoadMisclassificationCosts(costsPath);
	}
}