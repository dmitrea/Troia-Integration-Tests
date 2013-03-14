package test.java.integration.tests.galc;

import test.java.integration.helpers.*;
import org.junit.BeforeClass;
import com.datascience.galc.*;

public class Test_AdultContent extends GALCBaseScenarios{
	
	public static String FILEPATH_SEPARATOR = System.getProperty("file.separator");
	public static String TESTDATA_BASEDIR = TestSettings.GALC_TESTDATA_BASEDIR;
	public static String RESULTS_BASEDIR = TestSettings.GALC_RESULTS_BASEDIR;
	
	public static String TEST_DIR = "adultcontent";
	public static String INPUT_DIR 	= TESTDATA_BASEDIR + TEST_DIR + FILEPATH_SEPARATOR + "input" + FILEPATH_SEPARATOR;
	public static String OUTPUT_DIR	= TESTDATA_BASEDIR + TEST_DIR + FILEPATH_SEPARATOR + "output" + FILEPATH_SEPARATOR;
		
	public static String GOLDLABELS_FILE 	= INPUT_DIR 	+ 	"goldObjects.txt";
	public static String LABELS_FILE 		= INPUT_DIR 	+ 	"assignedLabels.txt";
	public static String EVAL_OBJECTS_FILE 	= INPUT_DIR 	+ 	"evaluationObjects.txt";
	public static String RESULTS_OBJECTS_FILE = OUTPUT_DIR 	+	"results-objects.txt";
	public static String RESULTS_WORKERS_FILE = OUTPUT_DIR 	+	"results-workers.txt";
	
	public static String TEST_RESULTS_FILE = RESULTS_BASEDIR + "Results_AdultContent.csv";

	static GALCBaseScenarios.Setup testSetup;
	
	@BeforeClass
	public static void setupTests(){
		ContinuousProject project = new ContinuousProject(new ContinuousIpeirotis());
		EmpiricalData empData = new EmpiricalData();
		empData.loadLabelFile(LABELS_FILE);
		empData.loadGoldLabelsFile(GOLDLABELS_FILE);
		empData.loadTrueObjectData(EVAL_OBJECTS_FILE);

		testSetup = new GALCBaseScenarios.Setup(project, TEST_RESULTS_FILE, RESULTS_OBJECTS_FILE, RESULTS_WORKERS_FILE);
		initSetup(testSetup);
		ci.setData(empData);
	}

	
	
}
