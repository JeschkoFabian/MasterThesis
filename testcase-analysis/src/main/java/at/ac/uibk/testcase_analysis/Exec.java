package at.ac.uibk.testcase_analysis;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import at.ac.uibk.feature.model.Feature;
import at.ac.uibk.testcase_analysis.core.FeatureStatistics;
import at.ac.uibk.testcase_analysis.core.FeatureLoader;
import at.ac.uibk.testcase_analysis.core.PrintStatistics;
import at.ac.uibk.testcase_analysis.core.StatisticMethods;
import at.ac.uibk.testcase_analysis.improvements.FeatureElement;
import at.ac.uibk.testcase_analysis.improvements.ValidationRuleBuilder;
import at.ac.uibk.testcase_analysis.violations.ViolationStatistics;

public class Exec {

	public static void main(String[] args) throws Exception {
		// Map<String, List<Feature>> featuresByRepo =
		// FeatureLoader.loadAsMap("at.ac.uibk.testcase_analysis.data");
		//
		// for (Map.Entry<String, List<Feature>> entry :
		// featuresByRepo.entrySet()) {
		// System.out.println("numFeatures: " + entry.getValue().size() +
		// "\tRepo: " + entry.getKey());
		// }

		// // files that are not inside a repo
		// for (Feature f : featuresByRepo.get("")){
		//// System.out.println(f.getName() + " - " + f.getFileName());
		// System.out.println(f);
		// System.out.println("=====================================================");
		// }
		String seperator = "==============================================================";

		// statistics
		List<Feature> features = FeatureLoader.loadAsList("tests");
		FeatureStatistics zeroStats = new FeatureStatistics(features);
		FeatureStatistics noZeroStats = StatisticMethods.removeOptionalZeros(zeroStats);

		// probability for it to be an outlier
		// double p = 1 - 0.5 / valuesList.size();
		// p = 0.95;
		FeatureStatistics noOutlierStats = StatisticMethods.removeOutliers(noZeroStats, 0.95);

		// System.out.println(stats);
		System.out.println(PrintStatistics.compareStatistics(noZeroStats, noOutlierStats));

		// correlations
		System.out.println(seperator);
		Map<String, Map<String, Double>> correlations = StatisticMethods.getCorrelations(zeroStats);
		System.out.println(PrintStatistics.correlations(correlations, 0.05, 1.0));
		
		System.out.println(seperator);
		System.out.println(PrintStatistics.stepDistribution(features));
		
		// guidelines
		System.out.println(seperator);
		ViolationStatistics violations = new ViolationStatistics(features);
		System.out.println(violations);

		storeTables(zeroStats);
		
//		System.out.println(seperator);
//		System.out.println("Examples Names");
//		Map<String, Integer> examplesStats = StatisticMethods.getExamplesToNameRelation(features);
//		for (Entry<String, Integer> entry : examplesStats.entrySet()) {
//			System.out.println(entry.getKey() + "\t->\t" + entry.getValue());
//		}

		System.out.println(seperator);
		Map<String, DescriptiveStatistics> givenRelation = StatisticMethods.getBackgroundToScenarioRelation(features, false);
		System.out.println(PrintStatistics.backgroundToScenarioRelation(givenRelation));

		System.out.println("==============================================================");
		printValidationRules(noZeroStats);
	}
	
	public static void printValidationRules(Map<String, Map<String, DescriptiveStatistics>> allStats) {
		// Steps/Background
		DescriptiveStatistics stats = allStats.get("Background").get("Steps");
		System.out
				.println(ValidationRuleBuilder.buildBoundsRule(FeatureElement.STEP, FeatureElement.BACKGROUND, stats));

		// Description/Feature
//		stats = allStats.get("Feature").get("DescriptionLines");
//		System.out.println(ValidationRuleBuilder.buildBoundsRule(FeatureElement.DESCRIPTION_LINE,
//				FeatureElement.FEATURE, FeatureElement.DESCRIPTION, stats));

		// Name/Example
		stats = allStats.get("Examples").get("NameWords");
		System.out.println(
				ValidationRuleBuilder.buildBoundsRule(FeatureElement.NAME, FeatureElement.EXAMPLES, stats));

		// Tags/Example
//		stats = allStats.get("Examples").get("Tags");
//		System.out.println(
//				ValidationRuleBuilder.buildBoundsRule(FeatureElement.TAGS, FeatureElement.EXAMPLES, stats));

		// Name/Feature
		stats = allStats.get("Feature").get("NameWords");
		System.out.println(
				ValidationRuleBuilder.buildBoundsRule(FeatureElement.NAME, FeatureElement.FEATURE_HEADER, stats));

		// Scenarios/Feature
		stats = allStats.get("Feature").get("Scenarios");
		System.out
				.println(ValidationRuleBuilder.buildBoundsRule(FeatureElement.SCENARIO, FeatureElement.FEATURE, stats));

		// Tags/Feature
		stats = allStats.get("Feature").get("Tags");
		System.out.println(
				ValidationRuleBuilder.buildBoundsRule(FeatureElement.TAGS, FeatureElement.FEATURE_HEADER, stats));

		// Description/Scenario
//		stats = allStats.get("Scenario").get("DescriptionLines");
//		System.out.println(ValidationRuleBuilder.buildBoundsRule(FeatureElement.SCENARIO_DESCRIPTION_LINE,
//				FeatureElement.SCENARIO, FeatureElement.SCENARIO_DESCRIPTION, stats));

		// Name/Scenario
		stats = allStats.get("Scenario").get("NameWords");
		System.out.println(ValidationRuleBuilder.buildBoundsRule(FeatureElement.NAME, FeatureElement.SCENARIO, stats));

		// Steps/Scenario
		stats = allStats.get("Scenario").get("Steps");
		System.out.println(ValidationRuleBuilder.buildBoundsRule(FeatureElement.STEP, FeatureElement.SCENARIO, stats));

		// Tags/Scenario
		stats = allStats.get("Scenario").get("Tags");
		System.out.println(ValidationRuleBuilder.buildBoundsRule(FeatureElement.TAGS, FeatureElement.SCENARIO, stats));

		// Name/Step
		stats = allStats.get("Step").get("NameWords");
		System.out.println(ValidationRuleBuilder.buildBoundsRule(FeatureElement.NAME, FeatureElement.STEP, stats));

		// Cols/Table
		stats = allStats.get("Table").get("Cols");
		System.out.println(ValidationRuleBuilder.buildBoundsRule(FeatureElement.CELL, FeatureElement.TABLE,
				FeatureElement.ROW, stats));

		// Rows/Table
		stats = allStats.get("Table").get("Rows");
		System.out.println(ValidationRuleBuilder.buildBoundsRule(FeatureElement.ROW, FeatureElement.TABLE, stats));
	}
	
	public static void storeTables(Map<String, Map<String, DescriptiveStatistics>> statsMap){
		String folder = "tables";
		File f = new File(folder);
		if (!f.exists()){
			f.mkdirs();
		}
		
		for (Entry<String, Map<String, DescriptiveStatistics>> perElement : statsMap.entrySet()){
			for (Entry<String, DescriptiveStatistics> element : perElement.getValue().entrySet()){
				Path path = Paths.get(folder,  perElement.getKey() + "_" + element.getKey());
				Map<Integer, Integer> freq = StatisticMethods.getFrequencyTable(element.getValue());
				String table = PrintStatistics.frequencyTable(freq, "\t");
				
				try {
					Files.write(path, table.getBytes(StandardCharsets.UTF_8));
				} catch (IOException e) {
					System.out.println("Failed to create " +  path);
					e.printStackTrace();
				}		
			}
		}
	}
	
}
