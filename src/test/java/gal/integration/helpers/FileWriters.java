package test.java.gal.integration.helpers;

import java.io.BufferedWriter;
import java.io.File;
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
			FileWriter fstream = new FileWriter(fileName, true);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(dataText);
			out.newLine();
			out.close();
		}
		catch (Exception e){
			System.err.println("Error: " + e.getMessage());
		}
	}
		
	public void createNewFile(String filePathString){
		try{
			File f = new File(filePathString);
			
			//create the parent directory if it doesn't exist
			new File(f.getParent()).mkdir();
			
			if(f.exists()) {
				f.delete();
			}
		}
		catch (Exception e){
			System.err.println("Error: " + e.getMessage());
		}
	}
	
}
