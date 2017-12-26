package at.ac.uibk.testcase_analysis.core;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import at.ac.uibk.feature.model.Feature;
import at.ac.uibk.feature.model.Scenario;
import at.ac.uibk.feature.model.Step;

public class PrintStatistics {

	public static String compareStatistics(FeatureStatistics statistics, FeatureStatistics statisticsNoOutliers) {
		StringBuilder builder = new StringBuilder();

		for (Map.Entry<String, Map<String, DescriptiveStatistics>> perElement : statistics.entrySet()) {
			builder.append("==============================================\n");
			builder.append(perElement.getKey() + "\n");

			for (Map.Entry<String, DescriptiveStatistics> element : perElement.getValue().entrySet()) {
				DescriptiveStatistics stats = element.getValue();
				DescriptiveStatistics statsAlt = statisticsNoOutliers.get(perElement.getKey()).get(element.getKey());

				builder.append("----------------------------------------------\n");
				builder.append(element.getKey() + "Per" + perElement.getKey() + "\n");
				builder.append(String.format("\t%-10s:\t%6d -> %6d\n", "Entries", stats.getN(), statsAlt.getN()));
				builder.append(String.format("\t%-10s:\t%6.0f -> %6.0f\n", "Sum", stats.getSum(), statsAlt.getSum()));
				builder.append(String.format("\t%-10s:\t%6.0f -> %6.0f\n", "Max", stats.getMax(), statsAlt.getMax()));
				builder.append(String.format("\t%-10s:\t%6.2f -> %6.2f\n", "Median", stats.getPercentile(50),
						statsAlt.getPercentile(50)));
				builder.append(
						String.format("\t%-10s:\t%6.2f -> %6.2f\n", "Mean", stats.getMean(), statsAlt.getMean()));
				builder.append(String.format("\t%-10s:\t%6.2f -> %6.2f\n", "Std. Dev.", stats.getStandardDeviation(),
						statsAlt.getStandardDeviation()));
				builder.append(String.format("\t%-10s:\t%6.2f -> %6.2f\n", "Skewness", stats.getSkewness(),
						statsAlt.getSkewness()));
				
				double iqr = stats.getPercentile(75) - stats.getPercentile(25);
				double iqrAlt = statsAlt.getPercentile(75) - statsAlt.getPercentile(25);
				builder.append(String.format("\t%-10s:\t%6.2f -> %6.2f\n", "IQR", iqr,
						iqrAlt));

				// test
				double numOutliers = stats.getValues().length - statsAlt.getValues().length;
				builder.append(String.format("\t%-10s:\t%6.0f -> %6.2f%%\n", "Outliers", numOutliers,
						numOutliers * 100 / stats.getN()));
			}
		}

		return builder.toString();
	}
	
	public static String stepDistribution(List<Feature> features) {
		Map<String, Integer> steps = new TreeMap<>();

		for (Feature f : features) {
			if (f.getBackground() != null && f.getBackground().getSteps() != null) {
				for (Step st : f.getBackground().getSteps()) {
					String key = st.getKeyword();
					if (!steps.containsKey(key)) {
						steps.put(key, 1);
					} else {
						steps.put(key, steps.get(key) + 1);
					}
				}
			}
			for (Scenario sc : f.getScenarios()) {
				for (Step st : sc.getSteps()) {
					String key = st.getKeyword();
					if (!steps.containsKey(key)) {
						steps.put(key, 1);
					} else {
						steps.put(key, steps.get(key) + 1);
					}
				}
			}
		}

		StringBuilder builder = new StringBuilder();
		
		
		builder.append("Step Keywords\n");
		for (Entry<String, Integer> entry : steps.entrySet()) {
			builder.append(entry.getKey() + "\t->\t" + entry.getValue() + "\n");
		}
		
		return builder.toString();
	}
	
	public static String correlations(Map<String, Map<String, Double>> correlations, double lower, double upper) {
		StringBuilder builder = new StringBuilder();
		builder.append("Correlations\n");

		for (Map.Entry<String, Map<String, Double>> perElement : correlations.entrySet()) {
			builder.append("--------------------------------------------------------------\n");
			builder.append(perElement.getKey() + "\n");

			for (Map.Entry<String, Double> corrEntry : perElement.getValue().entrySet()){
				double corrVal = corrEntry.getValue();
				
				if ((corrVal > lower && corrVal < upper) || (corrVal < -lower && corrVal > -upper)) {
					String name = corrEntry.getKey();
					builder.append(String.format("\t%-45s:\t%6.3f\n", name, corrVal));
				}	
			}
		}

		return builder.toString();
	}
	
	public static String frequencyTable(Map<Integer, Integer> table, String separator){
		StringBuilder builder = new StringBuilder();
		builder.append("value"+ separator + "occurrences\n");
		for (Entry<Integer, Integer> entry : table.entrySet()) {
			builder.append(entry.getKey() + separator + entry.getValue() + "\n");
		}

		return builder.toString();
	}
	
	public static String backgroundToScenarioRelation(Map<String, DescriptiveStatistics> givenRelation){
		StringBuilder builder = new StringBuilder();		
		builder.append("Given Background Relations\n");
		for (Entry<String, DescriptiveStatistics> entry : givenRelation.entrySet()) {
			DescriptiveStatistics rels = entry.getValue();
			builder.append(String.format(entry.getKey() + " -> %1.2f with %5d Scenarios\n", rels.getMean(), rels.getN()));
		}
		
		return builder.toString();
	}

}
