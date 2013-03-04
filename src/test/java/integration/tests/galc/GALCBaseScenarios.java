package test.java.integration.tests.galc;

import static org.junit.Assert.assertEquals;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;


import org.junit.Test;
import test.java.integration.helpers.*;

import com.datascience.core.base.ContValue;
import com.datascience.core.base.LObject;
import com.datascience.galc.ContinuousIpeirotis;
import com.datascience.galc.DatumContResults;

public class GALCBaseScenarios {

	public static int MAX_ITERATIONS = 20;
	public static double EPSILON = 1e-5;
	public static ContinuousIpeirotis ci;
	public static String resObjectsFile;
	public static ObjectsResultsParser objectsResultsParser;
	
	public static class Setup{
		public ContinuousIpeirotis contIpeirotis;
		public String resultsObjectsFile;
		
		public Setup(ContinuousIpeirotis ci, String resObjectsFile) {
			contIpeirotis = ci;
			resultsObjectsFile = resObjectsFile;
		}		
	}
	
	public static void initSetup(Setup testSetup){
		ci = testSetup.contIpeirotis;
		resObjectsFile = testSetup.resultsObjectsFile;
		ci.estimate(EPSILON, MAX_ITERATIONS);
	
		objectsResultsParser = new ObjectsResultsParser();
		objectsResultsParser.ParseResultsObjectsFile(resObjectsFile);
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
	public void test_Objects_TrueValues() {	
		//TBD
	}
	
	
}
	
	
	