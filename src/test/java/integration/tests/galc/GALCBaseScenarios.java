package test.java.integration.tests.galc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;


import org.junit.Test;
import test.java.integration.helpers.*;

import com.datascience.core.base.Worker;
import com.datascience.core.base.ContValue;
import com.datascience.core.base.LObject;
import com.datascience.galc.ContinuousIpeirotis;
import com.datascience.galc.DatumContResults;
import com.datascience.galc.WorkerContResults;

public class GALCBaseScenarios {

	public static int MAX_ITERATIONS = 20;
	public static double EPSILON = 1e-5;
	public static double TOLERANCE = 0.0000000001;
	public static FileWriters fileWriter;
	public static ContinuousIpeirotis ci;
	public static String testResultsFile;
	public static String resObjectsFile;
	public static String resWorkersFile;
	public static ObjectsResultsParser objectsResultsParser;
	public static WorkersResultsParser workersResultsParser;
	
	public static class Setup{
		public ContinuousIpeirotis contIpeirotis;
		public String testResultsFile;
		public String resultsObjectsFile;
		public String resultsWorkersFile;
		
		public Setup(ContinuousIpeirotis ci, String tResultsFile, String resObjectsFile, String resWorkersFile) {
			testResultsFile = tResultsFile;
			contIpeirotis = ci;
			resultsObjectsFile = resObjectsFile;
			resultsWorkersFile = resWorkersFile;
		}		
	}
	
	public static void initSetup(Setup testSetup){
		ci = testSetup.contIpeirotis;
		testResultsFile = testSetup.testResultsFile;
		resObjectsFile = testSetup.resultsObjectsFile;
		resWorkersFile = testSetup.resultsWorkersFile;
		ci.estimate(EPSILON, MAX_ITERATIONS);
		
		//prepare the test results file
		fileWriter = new FileWriters();
		fileWriter.createNewFile(testResultsFile);
		fileWriter.writeToFile(testResultsFile, "Metric,Original GALC value,Troia value");
	
		objectsResultsParser = new ObjectsResultsParser();
		objectsResultsParser.ParseResultsObjectsFile(resObjectsFile);
		
		workersResultsParser = new WorkersResultsParser();
		workersResultsParser.ParseWorkerResultsFile(resWorkersFile);
	}
	

	@Test
	public void test_Objects_AverageLabel() {	
		Map <String, Map<String, Double>> expEstObjects = objectsResultsParser.getEstimatedObjectValues(); 
		Map<LObject<ContValue>, DatumContResults> objectsResult = ci.getObjectsResults();
		Iterator<Entry<LObject<ContValue>, DatumContResults>> entries = objectsResult.entrySet().iterator();
		while (entries.hasNext()) {
			Entry<LObject<ContValue>, DatumContResults> entry = (Entry<LObject<ContValue>, DatumContResults>) entries.next();
			LObject<ContValue> object = entry.getKey();
			String objectName = object.getName();
			Double actualAvgLabel = ci.getAverageLabel(object);
			Double expectedAvgLabel = expEstObjects.get(objectName).get("avgLabel");
			
			fileWriter.writeToFile(testResultsFile, "AvgLabel-" + objectName + "," + expectedAvgLabel + "," + actualAvgLabel);
			assertTrue(Math.abs(expectedAvgLabel - actualAvgLabel) < TOLERANCE);
		}
	}
	
	@Test
	public void test_Objects_EstimatedValues() {	
		Map <String, Map<String, Double>> expEstObjects = objectsResultsParser.getEstimatedObjectValues(); 
		Map<LObject<ContValue>, DatumContResults> objectsResult = ci.getObjectsResults();
		Iterator<Entry<LObject<ContValue>, DatumContResults>> entries = objectsResult.entrySet().iterator();
		while (entries.hasNext()) {
			Entry<LObject<ContValue>, DatumContResults> entry = (Entry<LObject<ContValue>, DatumContResults>) entries.next();
			LObject<ContValue> object = entry.getKey();
			String objectName = object.getName();

			DatumContResults datumContResults = entry.getValue();
			Double actualEstimatedValue = datumContResults.getEst_value();
			Double actualEstimatedZeta = datumContResults.getEst_zeta();	
			Double expectedEstimatedValue = expEstObjects.get(objectName).get("estValue");
			Double expectedEstimatedZeta = expEstObjects.get(objectName).get("estZeta");
			
			fileWriter.writeToFile(testResultsFile, "EstValue-" + objectName + "," + expectedEstimatedValue + "," + actualEstimatedValue);
			fileWriter.writeToFile(testResultsFile, "EstValue-" + objectName + "," + expectedEstimatedZeta + "," + actualEstimatedZeta);
			
			assertTrue(Math.abs(expectedEstimatedValue - actualEstimatedValue) < TOLERANCE);
			assertTrue(Math.abs(expectedEstimatedZeta - actualEstimatedZeta) < TOLERANCE);
		}
	}
	
	
	@Test
	public void test_Workers_Labels() {
		Map <String, HashMap<String, Object>> expWorkersResults = workersResultsParser.getWorkersResults();
		Map<Worker<ContValue>, WorkerContResults> workersResults = ci.getWorkersResults();
		Iterator<Entry<Worker<ContValue>, WorkerContResults>> entries = workersResults.entrySet().iterator();
		while (entries.hasNext()) {
			Entry<Worker<ContValue>, WorkerContResults> entry = entries.next();
			Worker<ContValue> worker = entry.getKey();
			String workerName = worker.getName();
			int expectedNoAssigns = Integer.parseInt(expWorkersResults.get(workerName).get("labels").toString()); 
			int actualNoAssigns = worker.getAssigns().size();
			fileWriter.writeToFile(testResultsFile, "NoAssigns-" + workerName + "," + expectedNoAssigns + "," + actualNoAssigns);
			assertEquals(expectedNoAssigns, actualNoAssigns);
		}			
	}

	
	@Test
	public void test_Workers_EstimatedValues() {
		Map <String, HashMap<String, Object>> expWorkersResults = workersResultsParser.getWorkersResults();
		Map<Worker<ContValue>, WorkerContResults> workersResults = ci.getWorkersResults();
		Iterator<Entry<Worker<ContValue>, WorkerContResults>> entries = workersResults.entrySet().iterator();
		while (entries.hasNext()) {
			Entry<Worker<ContValue>, WorkerContResults> entry = entries.next();
			Worker<ContValue> worker = entry.getKey();
			String workerName = worker.getName();
			WorkerContResults workerContResults = entry.getValue();
			Double expectedEstMu = (Double)expWorkersResults.get(workerName).get("est_mu");
			Double expectedEstSigma = (Double)expWorkersResults.get(workerName).get("est_sigma");
			Double expectedEstRho = (Double)expWorkersResults.get(workerName).get("est_rho");
			
			Double actualEstMu = workerContResults.getEst_mu();
			Double actualEstSigma = workerContResults.getEst_sigma();
			Double actualEstRho = workerContResults.getEst_rho();
			
			fileWriter.writeToFile(testResultsFile, "Est_Mu_" + workerName + "," + expectedEstMu + "," + actualEstMu);
			fileWriter.writeToFile(testResultsFile, "Est_Sigma_" + workerName + "," + expectedEstSigma + "," + actualEstSigma);
			fileWriter.writeToFile(testResultsFile, "Est_Rho_" + workerName + "," + expectedEstRho + "," + actualEstRho);
			
			assertTrue(Math.abs(expectedEstMu - actualEstMu) < TOLERANCE);
			assertTrue(Math.abs(expectedEstSigma - actualEstSigma) < TOLERANCE);
			assertTrue(Math.abs(expectedEstRho - actualEstRho) < TOLERANCE);
		}			
	}

}