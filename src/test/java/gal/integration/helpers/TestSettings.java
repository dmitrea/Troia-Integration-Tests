package test.java.gal.integration.helpers;

public abstract class TestSettings {

	public static String FILEPATH_SEPARATOR = System.getProperty("file.separator");
	
	public static String TESTDATA_BASEDIR = System.getProperty("user.dir")
			+ FILEPATH_SEPARATOR + "src" + FILEPATH_SEPARATOR + "test"
			+ FILEPATH_SEPARATOR + "java" + FILEPATH_SEPARATOR + "gal"
			+ FILEPATH_SEPARATOR + "integration" + FILEPATH_SEPARATOR + "datasets" + FILEPATH_SEPARATOR;
	
	public static String RESULTS_BASEDIR = System.getProperty("user.dir") + FILEPATH_SEPARATOR + "results" + FILEPATH_SEPARATOR;
}
