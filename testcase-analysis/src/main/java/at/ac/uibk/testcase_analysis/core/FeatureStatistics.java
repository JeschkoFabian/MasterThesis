package at.ac.uibk.testcase_analysis.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import at.ac.uibk.feature.model.Examples;
import at.ac.uibk.feature.model.Feature;
import at.ac.uibk.feature.model.Scenario;
import at.ac.uibk.feature.model.Step;
import at.ac.uibk.feature.model.Table;

/**
 * Class that calculates simple statistics for a List of Feature instances.
 * 
 * The statistics calculated here are the total amount of elements, the maximum
 * number of a element per another, the median, mean and lastly the standard
 * deviation.
 * 
 * Statistics are calculated per Feature, Scenario, Step, Examples, Table
 * elements.
 * 
 * @see <a href=
 *      "https://hubpages.com/education/How-to-calculate-simple-statistics">How
 *      to calculate simple statistics</a>
 * 
 * @author Fabian
 *
 */
public class FeatureStatistics implements Map<String, Map<String, DescriptiveStatistics>> {
	// the Features which should be analysed
	private List<Feature> features = new ArrayList<Feature>();
	// all relevant values gathered inside a giant Map
	private Map<String, Map<String, DescriptiveStatistics>> statistics = new TreeMap<>();

	/**
	 * Constructor, takes a List of Features and calculates all relevant
	 * statistics from it.
	 * 
	 * @param features
	 *            a List of Features
	 */
	public FeatureStatistics(List<Feature> features) {
		resetStatistics();
		calculateStatisticsFromFeatures(features);
	}

	public FeatureStatistics() {
		resetStatistics();
	}

	public FeatureStatistics(List<Feature> features, Map<String, Map<String, DescriptiveStatistics>> stats) {
		resetStatistics();
		this.features = features;
		this.statistics = stats;
	}

	public List<Feature> getFeatures(){
		return features;
	}
	
	public Map<String, Map<String, DescriptiveStatistics>> getStatistics() {
		return statistics;
	}

	public DescriptiveStatistics getStatistic(String element, String perElement) {
		return statistics.get(perElement).get(element);
	}

	private Map<String, DescriptiveStatistics> initStatisticsMap(String[] names) {
		Map<String, DescriptiveStatistics> map = new TreeMap<>();

		for (String name : names) {
			map.put(name, new DescriptiveStatistics());
		}

		return map;
	}
	
	private void resetStatistics(){
		Map<String, DescriptiveStatistics> perFeature = initStatisticsMap(new String[] { "NameWords",
				"DescriptionLines", "Tags", "Scenarios", "Steps", "ExamplesTables", "StepTables", "Examples" });
		Map<String, DescriptiveStatistics> perScenario = initStatisticsMap(
				new String[] { "NameWords", "DescriptionLines", "Tags", "Steps", "StepTables", "Given", "When", "Then",
						"And", "But", "OtherStep", "Examples", "DocStrings" });
		Map<String, DescriptiveStatistics> perStep = initStatisticsMap(
				new String[] { "NameWords", "Tags", "Rows", "Cols" });
		Map<String, DescriptiveStatistics> perExamples = initStatisticsMap(
				new String[] { "DescriptionLines", "Tags", "NameWords", "Rows", "Cols" });
		Map<String, DescriptiveStatistics> perTable = initStatisticsMap(new String[] { "Rows", "Cols" });
		Map<String, DescriptiveStatistics> perBackground = initStatisticsMap(new String[] { "DescriptionLines",
				"StepTables", "Steps", "Given", "When", "Then", "And", "But", "OtherStep", "DocStrings" });		
		
		statistics.put("Feature", perFeature);
		statistics.put("Scenario", perScenario);
		statistics.put("Step", perStep);
		statistics.put("Examples", perExamples);
		statistics.put("Table", perTable);
		statistics.put("Background", perBackground);
	}

	public void setStatistics(List<Feature> features, Map<String, Map<String, DescriptiveStatistics>> stats){
		this.features = features;
		this.statistics = stats;
	}
	
	/**
	 * Iterates over the whole data structure for each Feature and counts
	 * various values which are stored in a Map.
	 * 
	 * Values are stored perFeature, perScenario, perStep, perExamples and
	 * perStep.
	 */
	public void calculateStatisticsFromFeatures(List<Feature> newFeatures) {
		features.addAll(newFeatures);
		
		for (Feature f : newFeatures) {
			double numSteps = 0;
			double numExamples = 0;
			double numExamplesTables = 0;
			double numStepTables = 0;

			if (f.getName() != null && !f.getName().isEmpty()) {
				statistics.get("Feature").get("NameWords").addValue((double) f.getName().split(" ").length);
			} else {
				statistics.get("Feature").get("NameWords").addValue(0);
			}

			// not sure about that
			// if (f.getDescription() != null && f.getDescription().size() > 0)
			// {
			if (f.getDescription() != null) {
				statistics.get("Feature").get("DescriptionLines").addValue((double) f.getDescription().size());
			} else {
				statistics.get("Feature").get("DescriptionLines").addValue(0);
			}

			// if (f.getTags() != null && f.getTags().size() > 0) {
			if (f.getTags() != null) {
				statistics.get("Feature").get("Tags").addValue((double) f.getTags().size());
			} else {
				statistics.get("Feature").get("Tags").addValue(0);
			}

			statistics.get("Feature").get("Scenarios").addValue((double) f.getScenarios().size());

			if (f.getBackground() != null) {
				Scenario bg = f.getBackground();

				if (bg.getDescription() != null && bg.getDescription().size() > 0) {
					statistics.get("Background").get("DescriptionLines").addValue((double) bg.getDescription().size());
				}

				if (bg.getSteps() != null) {
					statistics.get("Background").get("Steps").addValue((double) bg.getSteps().size());

					double numStepTablesPerBackground = 0;
					double numGiven = 0;
					double numWhen = 0;
					double numThen = 0;
					double numAnd = 0;
					double numBut = 0;
					double numOtherStep = 0;
					double numDocStrings = 0;
					// i should probably extract that whole per step thing into
					// a method
					for (Step st : bg.getSteps()) {
						if (st.getName() != null && !st.getName().isEmpty()) {
							statistics.get("Step").get("NameWords").addValue((double) st.getName().split(" ").length);
						} else {
							statistics.get("Step").get("NameWords").addValue(0);
						}
						// if (step.getTags() != null && step.getTags().size() >
						// 0)
						// {
						if (st.getTags() != null) {
							statistics.get("Step").get("Tags").addValue((double) st.getTags().size());
						} else {
							statistics.get("Step").get("Tags").addValue(0);
						}

						if (st.getTable() != null) {
							numStepTablesPerBackground++;

							Table tab = st.getTable();
							if (tab.getRows() != null && tab.getRows().size() > 0) {
								statistics.get("Table").get("Cols").addValue((double) tab.getRows().get(0).size());
								statistics.get("Table").get("Rows").addValue((double) tab.getRows().size());

								statistics.get("Step").get("Cols").addValue((double) tab.getRows().get(0).size());
								statistics.get("Step").get("Rows").addValue((double) tab.getRows().size());
							}
						}

						if (st.getDocString() != null && !st.getDocString().isEmpty()) {
							numDocStrings++;
						}

						String stepName = st.getKeyword().toLowerCase().replace(":", "");
						if (stepName.equals("given")) {
							numGiven++;
						} else if (stepName.equals("when")) {
							numWhen++;
						} else if (stepName.equals("then")) {
							numThen++;
						} else if (stepName.equals("and")) {
							numAnd++;
						} else if (stepName.equals("but")) {
							numBut++;
						} else {
							numOtherStep++;
						}
					}

					statistics.get("Background").get("StepTables").addValue(numStepTablesPerBackground);
					statistics.get("Background").get("Given").addValue(numGiven);
					statistics.get("Background").get("When").addValue(numWhen);
					statistics.get("Background").get("Then").addValue(numThen);
					statistics.get("Background").get("And").addValue(numAnd);
					statistics.get("Background").get("But").addValue(numBut);
					statistics.get("Background").get("OtherStep").addValue(numOtherStep);
					statistics.get("Background").get("DocStrings").addValue(numDocStrings);
					numStepTables += numStepTablesPerBackground;

				} else {
					statistics.get("Background").get("Steps").addValue(0);
				}

			}

			// Scenario measures
			for (Scenario sc : f.getScenarios()) {
				if (sc.getName() != null && !sc.getName().isEmpty()) {
					statistics.get("Scenario").get("NameWords").addValue((double) f.getName().split(" ").length);
				} else {
					statistics.get("Scenario").get("NameWords").addValue(0);
				}
				// if (s.getDescription() != null && s.getDescription().size() >
				// 0) {
				if (sc.getDescription() != null) {
					statistics.get("Scenario").get("DescriptionLines").addValue((double) sc.getDescription().size());
				} else {
					statistics.get("Scenario").get("DescriptionLines").addValue(0);
				}
				// if (s.getTags() != null && s.getTags().size() > 0) {
				if (sc.getTags() != null) {
					statistics.get("Scenario").get("Tags").addValue((double) sc.getTags().size());
				} else {
					statistics.get("Scenario").get("Tags").addValue(0);
				}
				statistics.get("Scenario").get("Steps").addValue((double) sc.getSteps().size());

				numSteps += sc.getSteps().size();

				double numStepTablesPerScenario = 0;
				double numGiven = 0;
				double numWhen = 0;
				double numThen = 0;
				double numAnd = 0;
				double numBut = 0;
				double numOtherStep = 0;
				double numExamplesPerScen = 0;
				double numDocStrings = 0;

				if (sc.getOutline() != null) {
					for (Examples ex : sc.getOutline()) {
						numExamplesPerScen++;

						if (ex.getTable() != null) {
							numExamplesTables++;

							Table tab = ex.getTable();
							if (tab.getRows() != null && tab.getRows().size() > 0) {
								statistics.get("Table").get("Cols").addValue((double) tab.getRows().get(0).size());
								statistics.get("Table").get("Rows").addValue((double) tab.getRows().size());

								statistics.get("Examples").get("Cols").addValue((double) tab.getRows().get(0).size());
								statistics.get("Examples").get("Rows").addValue((double) tab.getRows().size());
							}
						}

						if (ex.getName() != null && !ex.getName().isEmpty()) {
							statistics.get("Examples").get("NameWords").addValue(ex.getName().split(" ").length);
						} else {
							statistics.get("Examples").get("NameWords").addValue(0);
						}

						// if (ex.getDescription() != null &&
						// ex.getDescription().size() > 0) {
						if (ex.getDescription() != null) {
							statistics.get("Examples").get("DescriptionLines").addValue((double) ex.getDescription().size());
						} else {
							statistics.get("Examples").get("DescriptionLines").addValue(0);
						}

						if (ex.getTags() != null) {
							statistics.get("Examples").get("Tags").addValue((double) ex.getTags().size());
						} else {
							statistics.get("Examples").get("Tags").addValue(0);
						}

					}
				}

				// Step measures
				for (Step st : sc.getSteps()) {
					if (st.getName() != null && !st.getName().isEmpty()) {
						statistics.get("Step").get("NameWords").addValue((double) st.getName().split(" ").length);
					} else {
						statistics.get("Step").get("NameWords").addValue(0);
					}
					// if (step.getTags() != null && step.getTags().size() > 0)
					// {
					if (st.getTags() != null) {
						statistics.get("Step").get("Tags").addValue((double) st.getTags().size());
					} else {
						statistics.get("Step").get("Tags").addValue(0);
					}

					if (st.getTable() != null) {
						numStepTablesPerScenario++;

						Table tab = st.getTable();
						if (tab.getRows() != null && tab.getRows().size() > 0) {
							statistics.get("Table").get("Cols").addValue((double) tab.getRows().get(0).size());
							statistics.get("Table").get("Rows").addValue((double) tab.getRows().size());

							statistics.get("Step").get("Cols").addValue((double) tab.getRows().get(0).size());
							statistics.get("Step").get("Rows").addValue((double) tab.getRows().size());
						}
					}

					if (st.getDocString() != null && !st.getDocString().isEmpty()) {
						numDocStrings++;
					}

					String stepName = st.getKeyword().toLowerCase().replace(":", "");
					if (stepName.equals("given")) {
						numGiven++;
					} else if (stepName.equals("when")) {
						numWhen++;
					} else if (stepName.equals("then")) {
						numThen++;
					} else if (stepName.equals("and")) {
						numAnd++;
					} else if (stepName.equals("but")) {
						numBut++;
					} else {
						numOtherStep++;
					}
				}

				// if (numStepTablesPerScenario > 0){
				statistics.get("Scenario").get("StepTables").addValue(numStepTablesPerScenario);
				// }
				statistics.get("Scenario").get("Given").addValue(numGiven);
				statistics.get("Scenario").get("When").addValue(numWhen);
				statistics.get("Scenario").get("Then").addValue(numThen);
				statistics.get("Scenario").get("And").addValue(numAnd);
				statistics.get("Scenario").get("But").addValue(numBut);
				statistics.get("Scenario").get("OtherStep").addValue(numOtherStep);
				statistics.get("Scenario").get("Examples").addValue(numExamplesPerScen);
				statistics.get("Scenario").get("DocStrings").addValue(numDocStrings);
				numStepTables += numStepTablesPerScenario;
				numExamples += numExamplesPerScen;
			}

			statistics.get("Feature").get("Steps").addValue(numSteps);
			// if (numExamplesTables > 0){
			statistics.get("Feature").get("Examples").addValue(numExamples);
			statistics.get("Feature").get("ExamplesTables").addValue(numExamplesTables);
			// }
			// if (numStepTables > 0){
			statistics.get("Feature").get("StepTables").addValue(numStepTables);
			// }
		}

	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		for (Map.Entry<String, Map<String, DescriptiveStatistics>> perElement : statistics.entrySet()) {
			builder.append("==============================================\n");
			builder.append(perElement.getKey() + "\n");

			for (Map.Entry<String, DescriptiveStatistics> element : perElement.getValue().entrySet()) {
				DescriptiveStatistics stats = element.getValue();

				builder.append("----------------------------------------------\n");
				builder.append(element.getKey() + "Per" + perElement.getKey() + ":\n");
				builder.append(String.format("\t%-10s:\t%6.0f\n", "Sum", stats.getSum()));
				builder.append(String.format("\t%-10s:\t%6.0f\n", "Max", stats.getMax()));
				builder.append(String.format("\t%-10s:\t%6.2f\n", "Median", stats.getPercentile(50)));
				builder.append(String.format("\t%-10s:\t%6.2f\n", "Mean", stats.getMean()));
				builder.append(String.format("\t%-10s:\t%6.2f\n", "Std. Dev.", stats.getStandardDeviation()));
				builder.append(String.format("\t%-10s:\t%6.2f\n", "Skewness", stats.getSkewness()));
			}
		}

		return builder.toString();
	}

	@Override
	public int size() {
		return statistics.size();
	}

	@Override
	public boolean isEmpty() {
		return statistics.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return statistics.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return statistics.containsValue(value);
	}

	@Override
	public Map<String, DescriptiveStatistics> get(Object key) {
		return statistics.get(key);
	}

	@Override
	public Map<String, DescriptiveStatistics> put(String key, Map<String, DescriptiveStatistics> value) {
		return statistics.put(key, value);
	}

	@Override
	public Map<String, DescriptiveStatistics> remove(Object key) {
		return statistics.remove(key);
	}

	@Override
	public void clear() {
		statistics.clear();
	}

	@Override
	public void putAll(Map<? extends String, ? extends Map<String, DescriptiveStatistics>> m) {
		statistics.putAll(m);
	}

	@Override
	public Set<String> keySet() {
		return statistics.keySet();
	}

	@Override
	public Collection<Map<String, DescriptiveStatistics>> values() {
		return statistics.values();
	}

	@Override
	public Set<Entry<String, Map<String, DescriptiveStatistics>>> entrySet() {
		return statistics.entrySet();
	}
}
