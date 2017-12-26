package at.ac.uibk.testcase_analysis.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import at.ac.uibk.feature.model.Examples;
import at.ac.uibk.feature.model.Feature;
import at.ac.uibk.feature.model.Scenario;
import at.ac.uibk.feature.model.Step;
import io.hummer.util.math.MathUtil;

public class StatisticMethods {

	public static FeatureStatistics removeOptionalZeros(FeatureStatistics statistics) {
		MathUtil util = MathUtil.getInstance();
		Map<String, Map<String, DescriptiveStatistics>> statisticsNoZeroValues = new TreeMap<>();

		for (Entry<String, Map<String, DescriptiveStatistics>> perElement : statistics.entrySet()) {
			Map<String, DescriptiveStatistics> tmp = new TreeMap<>();
			statisticsNoZeroValues.put(perElement.getKey(), tmp);

			for (Map.Entry<String, DescriptiveStatistics> element : perElement.getValue().entrySet()) {
				String key = element.getKey();
				if (key.equals("Tags") || key.equals("DescriptionLines") || key.endsWith("Tables")
						|| key.equals("Examples") || key.equals("DocStrings")
						|| (key.equals("NameWords") && perElement.getKey().equals("Examples"))) {
					DescriptiveStatistics stats = element.getValue();
					double[] values = stats.getValues();

					// remove 0 values
					List<Double> valuesList = new ArrayList<>();
					for (double value : values) {
						if (value != 0) {
							valuesList.add(value);
						}
					}

					// convert List<Doubl> back to double[] which is used in
					// DescriptiveStatistics
					double[] culledValues = util.toArray(valuesList);
					DescriptiveStatistics culledStats = new DescriptiveStatistics(culledValues);
					statisticsNoZeroValues.get(perElement.getKey()).put(element.getKey(), culledStats);
				} else {
					DescriptiveStatistics stats = element.getValue();
					double[] values = stats.getValues();
					DescriptiveStatistics newStats = new DescriptiveStatistics(values.clone());
					statisticsNoZeroValues.get(perElement.getKey()).put(element.getKey(), newStats);
				}
			}
		}

		FeatureStatistics noZeroStats = new FeatureStatistics(statistics.getFeatures(), statisticsNoZeroValues);

		return noZeroStats;
	}

	public static FeatureStatistics removeOutliers(FeatureStatistics statistics, double cutoff) {
		Map<String, Map<String, DescriptiveStatistics>> statisticsNoOutliers = new TreeMap<>();
		// statisticsOnlyOutliers = new TreeMap<>();
		MathUtil util = MathUtil.getInstance();

		for (Map.Entry<String, Map<String, DescriptiveStatistics>> perElement : statistics.entrySet()) {
			Map<String, DescriptiveStatistics> tmp = new TreeMap<>();
			statisticsNoOutliers.put(perElement.getKey(), tmp);

			// Map<String, DescriptiveStatistics> tmpOutliers = new TreeMap<>();
			// statisticsOnlyOutliers.put(perElement.getKey(), tmpOutliers);

			for (Map.Entry<String, DescriptiveStatistics> element : perElement.getValue().entrySet()) {
				DescriptiveStatistics stats = element.getValue();

				double[] values = stats.getValues();

				// conversion to list of Double since thats whats required in
				// MathUtil
				List<Double> valuesList = new ArrayList<>();
				for (double value : values) {
					valuesList.add(value);
				}

				// below sample size 6 grubbs doesn't really work
				while (valuesList.size() > 6) {
					Double outlier = util.getOutlier(valuesList, cutoff);
					if (outlier != null) {
						valuesList.remove(outlier);
					} else {
						break;
					}
				}

				// convert List<Doubl> back to double[] which is used in
				// DescriptiveStatistics
				double[] culledValues = util.toArray(valuesList);
				DescriptiveStatistics culledStats = new DescriptiveStatistics(culledValues);
				statisticsNoOutliers.get(perElement.getKey()).put(element.getKey(), culledStats);

				// same for outliers
				// DescriptiveStatistics outlierStats = new
				// DescriptiveStatistics(util.toArray(outliers));
				// statisticsOnlyOutliers.get(perElement.getKey()).put(element.getKey(),
				// outlierStats);
			}
		}

		FeatureStatistics noOutlierStats = new FeatureStatistics(statistics.getFeatures(), statisticsNoOutliers);

		return noOutlierStats;
	}

	public static Map<String, Integer> getExamplesToNameRelation(List<Feature> features) {
		Map<String, Integer> stats = new TreeMap<>();
		int singleName = 0, singleNoName = 0, multipleName = 0, multipleNoName = 0;

		for (Feature f : features) {
			for (Scenario sc : f.getScenarios()) {
				if (sc.getOutline() != null) {
					for (Examples ex : sc.getOutline()) {

						if (sc.getOutline().size() == 1) {
							if (ex.getName() != null && !ex.getName().isEmpty()) {
								singleName++;
							} else {
								singleNoName++;
							}
						} else {
							if (ex.getName() != null && !ex.getName().isEmpty()) {
								multipleName++;
							} else {
								multipleNoName++;
							}
						}
					}
				}
			}
		}

		stats.put("singleName", singleName);
		stats.put("singleNoName", singleNoName);
		stats.put("multipleName", multipleName);
		stats.put("multipleNoName", multipleNoName);

		return stats;
	}

	public static Map<String, DescriptiveStatistics> getBackgroundToScenarioRelation(List<Feature> features,
			boolean includeInbetweenSteps) {
		Map<String, DescriptiveStatistics> stats = new TreeMap<>();
		stats.put("0  Steps", new DescriptiveStatistics());
		stats.put("1  Steps", new DescriptiveStatistics());
		stats.put("2  Steps", new DescriptiveStatistics());
		stats.put("3  Steps", new DescriptiveStatistics());
		stats.put("4  Steps", new DescriptiveStatistics());
		stats.put("5+ Steps", new DescriptiveStatistics());

		for (Feature f : features) {
			int numBGSteps = 0;
			String name;

			if (f.getBackground() != null && f.getBackground().getSteps() != null) {
				numBGSteps = f.getBackground().getSteps().size();
			}

			if (numBGSteps > 4) {
				name = "5+ Steps";
			} else {
				name = numBGSteps + "  Steps";
			}

			for (Scenario sc : f.getScenarios()) {
				int numGiven = 0;
				String lastStep = "";
				for (Step st : sc.getSteps()) {
					String step = st.getKeyword().toLowerCase().replace(":", "");
					if (step.equals("given")) {
						numGiven++;
					}

					if (includeInbetweenSteps && lastStep.equals("given")
							&& (step.equals("and") || step.equals("but"))) {
						numGiven++;
					}

					if (step.equals("given") || step.equals("when") || step.equals("then")) {
						lastStep = step;
					}
				}

				stats.get(name).addValue(numGiven);
			}
		}

		return stats;
	}

	public static Map<Integer, Integer> getFrequencyTable(DescriptiveStatistics stats) {
		Map<Integer, Integer> table = new TreeMap<>();

		double[] values = stats.getSortedValues();

		for (double value : values) {
			Integer val = (int) value;
			if (!table.containsKey(val)) {
				table.put(val, 1);
			} else {
				table.put(val, table.get(val) + 1);
			}
		}

		return table;
	}

	public static Map<String, Map<String, Double>> getCorrelations(FeatureStatistics statistics) {
		Map<String, Map<String, Double>> correlations = new TreeMap<>();

		for (Map.Entry<String, Map<String, DescriptiveStatistics>> perElement : statistics.entrySet()) {
			Map<String, Double> tmp = new TreeMap<>();
			List<String> keys = new ArrayList<>(perElement.getValue().keySet());

			for (int i = 0; i < keys.size() - 1; i++) {
				for (int j = i + 1; j < keys.size(); j++) {
					// skip the step relations since they are somewhat obvious
					if (isStep(keys.get(i)) && isStep(keys.get(j))) {
						continue;
					}

					double[] valuesX = perElement.getValue().get(keys.get(i)).getValues();
					double[] valuesY = perElement.getValue().get(keys.get(j)).getValues();

					if (valuesX.length == valuesY.length && valuesX.length > 2) {
						double corr = new PearsonsCorrelation().correlation(valuesX, valuesY);
						String name = keys.get(i) + " <-> " + keys.get(j);
						tmp.put(name, corr);
					}
				}
			}

			if (tmp.size() > 0) {
				correlations.put(perElement.getKey(), tmp);
			}
		}

		return correlations;
	}

	private static boolean isStep(String element) {
		if (element.equals("Given") || element.equals("When") || element.equals("Then") || element.equals("And")
				|| element.equals("But") || element.equals("Steps") || element.equals("OtherStep")) {
			return true;
		}
		return false;
	}
}
