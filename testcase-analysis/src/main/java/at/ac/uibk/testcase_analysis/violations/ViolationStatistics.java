package at.ac.uibk.testcase_analysis.violations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import at.ac.uibk.feature.model.Examples;
import at.ac.uibk.feature.model.Feature;
import at.ac.uibk.feature.model.Scenario;
import at.ac.uibk.feature.model.ScenarioType;
import at.ac.uibk.feature.model.Step;

import java.util.TreeMap;

public class ViolationStatistics {
	private List<Feature> features;
	private Map<GuidelineRule, ViolationCount> guidelineViolations;
	private Map<SyntaxRule, ViolationCount> syntaxViolations;
	private int noViolations;
	private int noViolationsDesc;

	public ViolationStatistics(List<Feature> features) {
		this.features = features;
		calculateViolations(features);
	}
	
	public Map<GuidelineRule, ViolationCount> getGuidelineViolations(){
		return guidelineViolations;
	}
	
	public Map<SyntaxRule, ViolationCount> getSyntaxViolations(){
		return syntaxViolations;
	}
	
	public int getNoViolationsCount(){
		return noViolations;
	}

	private void calculateViolations(List<Feature> features) {
		initMaps();

		for (Feature f : features) {
			Map<GuidelineRule, ViolationCount> guidelineTmp = new HashMap<>();
			Map<SyntaxRule, ViolationCount> syntaxTmp = new HashMap<>();

			if (f.getKeyword() == null || f.getKeyword().isEmpty()) {
				addViolation(syntaxTmp, SyntaxRule.FEATURE_NO_KEYWORD);
			} else {
				if (f.getName() == null || f.getName().isEmpty()) {
					addViolation(syntaxTmp, SyntaxRule.FEATURE_NO_NAME);
				}
			}

			if (f.getBackground() != null) {
				Scenario bg = f.getBackground();
				if (bg.getSteps() != null && bg.getSteps().size() > 4) {
					addViolation(guidelineTmp, GuidelineRule.STEPS_PER_BACKGROUND);
				}

				if (bg.getSteps() != null) {
					for (Step st : bg.getSteps()) {
						if (st.getKeyword().toLowerCase().contains("then")
								|| st.getKeyword().toLowerCase().contains("when")) {
							addViolation(guidelineTmp, GuidelineRule.BACKGROUND_GIVEN_ONLY);
						}
					}
				}

				if (bg.getName() != null && !bg.getName().isEmpty()) {
					addViolation(guidelineTmp, GuidelineRule.BACKGROUND_NAME);
				}

				if (bg.getTags() != null && bg.getTags().size() > 0) {
					addViolation(syntaxTmp, SyntaxRule.BACKGROUND_TAGS);
				}

				if (bg.getSteps() == null || bg.getSteps().size() == 0) {
					addViolation(syntaxTmp, SyntaxRule.BACKGROUND_NO_STEPS);
				}
			}

			if (f.getScenarios() == null || f.getScenarios().size() == 0){
				addViolation(syntaxTmp, SyntaxRule.FEATURE_NO_SCENARIOS);
			}
			
			for (Scenario sc : f.getScenarios()) {
				if (sc.getSteps() != null && sc.getSteps().size() > 5) {
					addViolation(guidelineTmp, GuidelineRule.STEPS_PER_SCENARIO);
				}

				if (sc.getName() == null || sc.getName().isEmpty()) {
					addViolation(syntaxTmp, SyntaxRule.SCENARIO_NO_NAME);
				}

				if (sc.getSteps() == null || sc.getSteps().size() == 0) {
					addViolation(syntaxTmp, SyntaxRule.SCENARIO_NO_STEPS);
				}

				if (sc.getType().equals(ScenarioType.SCENARIO) && sc.getOutline() != null
						&& sc.getOutline().size() > 0) {
					addViolation(syntaxTmp, SyntaxRule.SCENARIO_EXAMPLES);
				}

				if (sc.getType().equals(ScenarioType.SCENARIO_OUTLINE)
						&& (sc.getOutline() == null || sc.getOutline().size() == 0)) {
					addViolation(syntaxTmp, SyntaxRule.SCENARIO_OUTLINE_NO_EXAMPLES);
				}

				if (sc.getOutline() != null) {
					for (Examples outline : sc.getOutline()) {
						if (outline.getTable() == null) {
							addViolation(syntaxTmp, SyntaxRule.EXAMPLES_NO_TABLE);
						}

						if (outline.getTable() != null && outline.getTable().getRows() != null
								&& outline.getTable().getRows().size() > 0) {
							List<List<String>> rows = outline.getTable().getRows();
							List<String> header = rows.get(0);

							for (String name : header) {
								if (name.isEmpty()) {
									addViolation(syntaxTmp, SyntaxRule.TABLE_HEADER_EMPTY);
									break;
								}
							}

							for (List<String> row : rows) {
								if (row.size() != header.size()) {
									addViolation(syntaxTmp, SyntaxRule.TABLE_INCONSISTENT_CELLS);
									break;
								}
							}
						}
					}
				}

				int numWhen = 0;
				String lastKeyword = "";
				for (Step st : sc.getSteps()) {
					String keyword = st.getKeyword().toLowerCase().replace(":", "");

					if (keyword.contains("given") && lastKeyword.contains("given")) {
						addViolation(guidelineTmp, GuidelineRule.NO_CONSECUTIVE_GIVEN_THEN);
					}
					if (keyword.contains("when")) {
						numWhen++;
					}
					if (keyword.contains("then") && lastKeyword.contains("then")) {
						addViolation(guidelineTmp, GuidelineRule.NO_CONSECUTIVE_GIVEN_THEN);
					}
					if (keyword.contains("and") || keyword.contains("but")) {
						if (lastKeyword.contains("when")) {
							numWhen++;
						}
					}

					if (st.getName() == null || st.getName().isEmpty()) {
						addViolation(syntaxTmp, SyntaxRule.STEP_NO_NAME);
					}

					if (st.getKeyword().endsWith(":")) {
						addViolation(syntaxTmp, SyntaxRule.STEP_COLON);
					}

					if (st.getKeyword().substring(0, 1).matches("[a-z]")) {
						addViolation(syntaxTmp, SyntaxRule.STEP_LOWERCASE);
					}

					if (keyword.equals("i") || keyword.equals("check")) {
						addViolation(syntaxTmp, SyntaxRule.STEP_INVALID_KW);
					}

					if (st.getTable() != null && st.getTable().getRows() != null
							&& st.getTable().getRows().size() > 0) {
						List<List<String>> rows = st.getTable().getRows();
						List<String> header = rows.get(0);

						for (List<String> row : rows) {
							if (row.size() != header.size()) {
								addViolation(syntaxTmp, SyntaxRule.TABLE_INCONSISTENT_CELLS);
								break;
							}
						}
					}

					if (!keyword.equals("and") && !keyword.equals("but")){
						lastKeyword = keyword;
					}
				}

				if (numWhen > 1) {
					addViolation(guidelineTmp, GuidelineRule.SINGLE_WHEN);
				}
			}

			int guidelineSum = 0, syntaxSum = 0;
			for (Map.Entry<GuidelineRule, ViolationCount> entry : guidelineTmp.entrySet()) {
				int violations = entry.getValue().getViolationCount();
				guidelineViolations.get(entry.getKey()).addViolationsCount(violations);
				guidelineSum += violations;
			}

			for (Map.Entry<SyntaxRule, ViolationCount> entry : syntaxTmp.entrySet()) {
				int violations = entry.getValue().getViolationCount();
				syntaxViolations.get(entry.getKey()).addViolationsCount(violations);
				syntaxSum += violations;
			}

			if (guidelineSum > 0) {
				guidelineViolations.get(GuidelineRule.ANY).addViolationsCount(guidelineSum);
			}
			if (syntaxSum > 0) {
				syntaxViolations.get(SyntaxRule.ANY).addViolationsCount(syntaxSum);
			}
			if (guidelineSum == 0 && syntaxSum == 0) {
				noViolations++;
				
				if (f.getDescription() != null && f.getDescription().size() > 0){
					noViolationsDesc++;
				}
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Breakdown of Violations for a total of " + features.size() + " Features.\n");

		builder.append("Guideline Violations\n");
		for (Entry<GuidelineRule, ViolationCount> entry : guidelineViolations.entrySet()) {
			ViolationCount count = entry.getValue();
			builder.append(String.format("  %-30s: %7d across %4d Features\n", entry.getKey(),
					count.getViolationCount(), count.getFeatureCount()));
		}

		builder.append("\nSyntax Violations\n");
		for (Entry<SyntaxRule, ViolationCount> entry : syntaxViolations.entrySet()) {
			ViolationCount count = entry.getValue();
			builder.append(String.format("  %-30s: %7d across %4d Features\n", entry.getKey(),
					count.getViolationCount(), count.getFeatureCount()));
		}

		builder.append("\nFeatures without Any Violations: " + noViolations + "\n");
		builder.append("Features without Any Violations and Desc: " + noViolationsDesc + "\n");

		return builder.toString();
	}

	private <T> void addViolation(Map<T, ViolationCount> map, T type) {
		if (map.containsKey(type)) {
			map.get(type).increaseViolationCount();
		} else {
			map.put(type, new ViolationCount(1, 1));
		}
	}

//	private <T> void addViolations(Map<T, ViolationCount> map, T type, int num) {
//		if (map.containsKey(type)) {
//			map.get(type).addViolations(num);
//		} else {
//			map.put(type, new ViolationCount(1, num));
//		}
//	}

	private void initMaps() {
		guidelineViolations = new TreeMap<>();
		for (GuidelineRule rule : GuidelineRule.values()) {
			guidelineViolations.put(rule, new ViolationCount());
		}

		syntaxViolations = new TreeMap<>();
		for (SyntaxRule rule : SyntaxRule.values()) {
			syntaxViolations.put(rule, new ViolationCount());
		}
	}

}
