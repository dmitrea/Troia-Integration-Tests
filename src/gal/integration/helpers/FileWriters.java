package gal.integration.helpers;

import java.io.BufferedWriter;
import java.io.FileWriter;

public class FileWriters {
	
	
	/**
	 * Writes data to a given file
	 * @param fileName
	 * 				The name of the file
	 * @param dataText
	 * 				The text to be written
	 */
	public void writeToFile(String fileName, String dataText){
		try{
			FileWriter fstream = new FileWriter(fileName);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(dataText);
			out.close();
		}
		catch (Exception e){
			System.err.println("Error: " + e.getMessage());
		}
	}

}
