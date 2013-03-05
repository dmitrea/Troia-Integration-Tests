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
	public static ContinuousIpeirotis ci;
	public static String resObjectsFile;
	public static String resWorkersFile;
	public static ObjectsResultsParser objectsResultsParser;
	public static WorkersResultsParser workersResultsParser;
	
	public static class Setup{
		public ContinuousIpeirotis contIpeirotis;
		public String resultsObjectsFile;
		public String resultsWorkersFile;
		
		public Setup(ContinuousIpeirotis ci, String resObjectsFile, String resWorkersFile) {
			contIpeirotis = ci;
			resultsObjectsFile = resObjectsFile;
			resultsWorkersFile = resWorkersFile;
		}		
	}
	
	public static void initSetup(Setup testSetup){
		ci = testSetup.contIpeirotis;
		resObjectsFile = testSetup.resultsObjectsFile;
		resWorkersFile = testSetup.resultsWorkersFile;
		ci.estimate(EPSILON, MAX_ITERATIONS);
	
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
			assertEquals(expEstObjects.get(objectName).get("avgLabel"), actualAvgLabel);
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
			assertEquals(expEstObjects.get(objectName).get("estValue"), actualEstimatedValue);
			assertEquals(expEstObjects.get(objectName).get("estZeta"), actualEstimatedZeta);
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
			int expectedNoObjects = (int) expWorkersResults.get(workerName).get("labels"); 
			assertEquals(expectedNoObjects, worker.getAssigns().size());
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
			assertTrue((Math.abs((Double)expWorkersResults.get(workerName).get("est_mu")) - workerContResults.getEst_rho()) < TOLERANCE);
			assertTrue((Math.abs((Double)expWorkersResults.get(workerName).get("est_sigma")) - workerContResults.getEst_sigma()) < TOLERANCE);
			assertTrue((Math.abs((Double)expWorkersResults.get(workerName).get("est_rho")) - workerContResults.getEst_rho()) < TOLERANCE);
		}			
	}

}