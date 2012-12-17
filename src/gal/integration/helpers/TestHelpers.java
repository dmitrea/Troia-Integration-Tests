package gal.integration.helpers;



import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import troiaClient.GoldLabel;
import troiaClient.Label;

import main.java.com.datascience.gal.dataGenerator.DataManager;

import com.datascience.gal.AssignedLabel;
import com.datascience.gal.Category;
import com.datascience.gal.CorrectLabel;
import com.datascience.gal.MisclassificationCost;

public class TestHelpers {
	
	DataManager dataManager = DataManager.getInstance();
	FileReaders fileReader = new FileReaders();
	
	/**
	 * Loads the categories and probabilities from the given file
	 * @param categoriesFileName
	 * @return Collection<Category>
	 */
	public Collection<Category> LoadCategories(String categoriesFileName){
		//Load the categories and probabilities file from CATEGORIES_FILE 
		Map<String, Double> categoryNamesProbsMap =  new HashMap<String, Double>();
		
		try{
			categoryNamesProbsMap = dataManager.loadCategoriesWithProbabilities(categoriesFileName);
		}
		catch (FileNotFoundException ex){
			ex.printStackTrace();
		}
				 
		Collection<Category> categories = new ArrayList<Category>();	 
		for (Map.Entry<String, Double> entry : categoryNamesProbsMap.entrySet()) {
			Category category  = new Category(entry.getKey());
			category.setPrior(entry.getValue());
			categories.add(category);
		}
		return categories;	
	}
	
	/**
	 * Loads the misclassification costs from the given file
	 * @param misclassificationCostFileName
	 * @return HashSet<MisclassificationCost>
	 */
	public HashSet<MisclassificationCost> LoadMisclassificationCosts(String misclassificationCostFileName){
		HashSet<MisclassificationCost> misclassificationCosts = new HashSet<MisclassificationCost>();
		
		try{
			misclassificationCosts = (HashSet<MisclassificationCost>) fileReader.loadMisclassificationCostData(misclassificationCostFileName);
		}
		catch (FileNotFoundException ex){
			ex.printStackTrace();
		}	
		return misclassificationCosts;
	}
	
	/**
	 * Loads the gold labels from the given file
	 * @param goldLabelsFileName
	 * @return Collection<CorrectLabel>
	 */
	public Collection<CorrectLabel> LoadGoldLabels(String goldLabelsFileName){
		Collection<GoldLabel> goldLabels = new ArrayList<GoldLabel>();
		
		try{
			goldLabels = dataManager.loadGoldLabelsFromFile(goldLabelsFileName);
		}
		catch (FileNotFoundException ex){
			ex.printStackTrace();
		}
		
		//Convert the Labels to AssignedLabels
		Collection<CorrectLabel> correctLabels = new ArrayList<CorrectLabel>();
		for (GoldLabel goldLabel : goldLabels) {
			CorrectLabel correctLabel = new CorrectLabel(goldLabel.getObjectName(), goldLabel.getCorrectCategory());
			correctLabels.add(correctLabel);
		}
		return correctLabels;
	}
	
	/**
	 * Loads the worker assigned labels from the given file
	 * @param labelsFileName
	 * @return Collection <Label>
	 */
	public Collection<AssignedLabel> LoadWorkerAssignedLabels(String labelsFileName){
		Collection <Label> labels = new ArrayList<Label>();
		
		try{
			labels = dataManager.loadLabelsFromFile(labelsFileName);
		}
		catch (FileNotFoundException ex){
			ex.printStackTrace();
		}
		
		//Convert the Labels to AssignedLabels
		Collection<AssignedLabel> assignedLabels = new ArrayList<AssignedLabel>();
		for (Label label : labels) {
			AssignedLabel assignedLabel = new AssignedLabel(label.getWorkerName(), label.getObjectName(), label.getCategoryName());
			assignedLabels.add(assignedLabel);
		}
		
		return assignedLabels;
	}
	
	
	public LinkedList<Map<String, Object>> LoadWorkerSummaryFile(String expectedSummaryFileName){
		LinkedList<Map<String, Object>>	expectedWorkerScores = new LinkedList<Map<String, Object>>();
		
		try{
			expectedWorkerScores = fileReader.loadWorkerSummaryFile(expectedSummaryFileName);
		}
		catch (FileNotFoundException ex){
			ex.printStackTrace();
		}
		
		return expectedWorkerScores;
		
	}
}
