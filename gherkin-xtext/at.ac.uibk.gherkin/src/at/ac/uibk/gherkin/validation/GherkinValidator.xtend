/*
 * generated by Xtext 2.10.0
 */
package at.ac.uibk.gherkin.validation

import at.ac.uibk.gherkin.gherkin.Background
import at.ac.uibk.gherkin.gherkin.Cell
import at.ac.uibk.gherkin.gherkin.Examples
import at.ac.uibk.gherkin.gherkin.Feature
import at.ac.uibk.gherkin.gherkin.FeatureHeader
import at.ac.uibk.gherkin.gherkin.GherkinPackage
import at.ac.uibk.gherkin.gherkin.Row
import at.ac.uibk.gherkin.gherkin.Scenario
import at.ac.uibk.gherkin.gherkin.Step
import at.ac.uibk.gherkin.gherkin.Table
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EStructuralFeature
import org.eclipse.xtext.nodemodel.ICompositeNode
import org.eclipse.xtext.nodemodel.util.NodeModelUtils
import org.eclipse.xtext.validation.Check

/**
 * This class contains custom validation rules. 
 * 
 * See https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#validation
 */
class GherkinValidator extends AbstractGherkinValidator {
	private boolean enableGuidelines = false;
	private String severityGuidelines = "info";

	private boolean enableImprovements = true;
	private String severityImprovements = "warning";

	private boolean enableRules = true;
	private String severityRules = "warning";

	// =====================================================================================================
	// Custom rules based on statistical analysis
	// =====================================================================================================
	@Check
	def validateStepsPerBackground(Background el) {
		if (el?.steps != null) {
			if (el.steps.length > 6) {
				markImprovement("Abnormally high amount of Steps per Background, should be 6 or less", el,
					GherkinPackage.Literals.BACKGROUND__STEPS);
			}
		}
	}

	@Check
	def validateNamesPerExamples(Examples el) {
		if (el?.name != null) {
			if (el.name.length > 4) {
				markImprovement("Abnormally high amount of Names per Examples, should be 4 or less", el,
					GherkinPackage.Literals.EXAMPLES__NAME);
			}
		}
	}

	@Check
	def validateNamesPerFeatureHeader(FeatureHeader el) {
		if (el?.name != null) {
			if (el.name.length > 8) {
				markImprovement("Abnormally high amount of Names per FeatureHeader, should be 8 or less", el,
					GherkinPackage.Literals.FEATURE_HEADER__NAME);
			}
		}
	}

	@Check
	def validateTagssPerFeatureHeader(FeatureHeader el) {
		if (el?.tags?.tags != null) {
			if (el.tags.tags.length > 4) {
				markImprovement("Abnormally high amount of Tagss per FeatureHeader, should be 4 or less", el,
					GherkinPackage.Literals.FEATURE_HEADER__TAGS);
			}
		}
	}

	@Check
	def validateScenariosPerFeature(Feature el) {
		if (el?.scenarios != null) {
			if (el.scenarios.length > 10) {
				markImprovement("Abnormally high amount of Scenarios per Feature, should be 10 or less", el,
					GherkinPackage.Literals.FEATURE__SCENARIOS);
			}
		}
	}

	@Check
	def validateNamesPerScenario(Scenario el) {
		if (el?.name != null) {
			if (el.name.length > 9) {
				markImprovement("Abnormally high amount of Names per Scenario, should be 9 or less", el,
					GherkinPackage.Literals.SCENARIO__NAME);
			}
		}
	}

	@Check
	def validateTagssPerScenario(Scenario el) {
		if (el?.tags?.tags != null) {
			if (el.tags.tags.length > 3) {
				markImprovement("Abnormally high amount of Tagss per Scenario, should be 3 or less", el,
					GherkinPackage.Literals.SCENARIO__TAGS);
			}
		}
	}

	@Check
	def validateStepsPerScenario(Scenario el) {
		if (el?.steps != null) {
			if (el.steps.length > 13) {
				markImprovement("Abnormally high amount of Steps per Scenario, should be 13 or less", el,
					GherkinPackage.Literals.SCENARIO__STEPS);
			}
		}
	}

	@Check
	def validateNamesPerStep(Step el) {
		if (el?.name != null) {
			if (el.name.length > 11) {
				markImprovement("Abnormally high amount of Names per Step, should be 11 or less", el,
					GherkinPackage.Literals.STEP__NAME);
			}
		}
	}

	@Check
	def validateCellsPerTable(Table el) {
		if (el?.rows?.head?.cells != null) {
			if (el.rows.head.cells.length > 6) {
				markImprovement("Abnormally high amount of Cells per Table, should be 6 or less", el.rows.head,
					GherkinPackage.Literals.ROW__CELLS);
			}
		}
	}

	@Check
	def validateRowsPerTable(Table el) {
		if (el?.rows != null) {
			if (el.rows.length > 9) {
				markImprovement("Abnormally high amount of Rows per Table, should be 9 or less", el,
					GherkinPackage.Literals.TABLE__ROWS);
			}
		}
	}

	// =====================================================================================================
	// Rules based on the Gherkin Guidelines
	// =====================================================================================================
	@Check
	def validateBackgroundGuidelines(Background el) {
		// Background no name
		if (el.name != null && el.name.length > 0) {
			markGuideline("Backgrounds don't need to have a name, since there should only be one.", el,
				GherkinPackage.Literals.BACKGROUND__NAME);
		}

		if (el?.steps != null) {
			// 4 Steps per Background
			if (el.steps.length > 4) {
				markGuideline("It is recommended to keep the Background short with up to 4 Steps, try combining" +
					" them into higher level Steps.", el, GherkinPackage.Literals.BACKGROUND__STEPS);
			}
			// only Given per Background
			for (Step st : el.steps) {
				if (!st.keyword.toLowerCase.contains("given")) {
					markGuideline("Backgrounds set up a state for scenarios and should therefore only use " +
						"'Given' Steps.", st, GherkinPackage.Literals.STEP__KEYWORD);
				}
			}
		}
	}

	@Check
	def validateScenarioGuidelines(Scenario el) {
		if (el?.steps != null) {
			// 5 Steps per Scenario
			if (el.steps.length > 5) {
				markGuideline("It is recommended to keep Scenarios short with 3 to 5 Steps, try combining" +
					" them into higher level Steps.", el, GherkinPackage.Literals.SCENARIO__STEPS);
			}
			// only Given per Background
			var String lastKeyword = "";
			var int numWhen = 0;
			for (Step st : el.steps) {
				var String keyword = st.keyword.toLowerCase.replace(":", "");

				if (keyword.equals("when") ||
					(lastKeyword.equals("when") && (keyword.equals("and") || keyword.equals("but")))) {
					numWhen++;

					if (numWhen > 1) {
						markGuideline("It is recommended to keep the number of events ('When' Steps) to one per " +
							"Scenario, try splitting up the Scenario instead.", st,
							GherkinPackage.Literals.STEP__KEYWORD);
					}
				}

				if (lastKeyword.equals(keyword) && !(lastKeyword.equals("and") || lastKeyword.equals("but"))) {
					markGuideline("Instead of using the same Step multiple times it is recommended to use either " +
						"'And' or 'But' Steps instead to improve readability.", st,
						GherkinPackage.Literals.STEP__KEYWORD);
				}

				if (!keyword.equals("and") && !keyword.equals("but")) {
					lastKeyword = keyword;
				}
			}
		}
	}

	@Check
	def validateExamplesGuidelines(Scenario el) {
		if (el?.examples.size > 0) {
			for (Examples ex : el.examples) {
				if (el.examples.size > 1 && (ex.name == null || ex.name.size == 0)) {
					markGuideline("We recommend to use a name to distinguish multiple Examples from each other.", ex,
						GherkinPackage.Literals.EXAMPLES__NAME);
				}

				if (el.examples.size == 1 && ex?.name.size > 0) {
					markGuideline("If there is only one Example a name is not required.", ex,
						GherkinPackage.Literals.EXAMPLES__NAME);
				}
			}
		}
	}

	// =====================================================================================================
	// Helpful methods to mark guideline or custom messages
	// =====================================================================================================
	def markImprovement(String message, EObject source, EStructuralFeature feature) {
		if (enableImprovements) {
			switch (severityImprovements) {
				case "info": {
					info(message, source, feature);
				}
				case "warning": {
					warning(message, source, feature);
				}
				case "error": {
					error(message, source, feature);
				}
				default: {
					println("Invalid severity for improvements, chose either info, warning or error.");
				}
			}
		}
	}

	def markGuideline(String message, EObject source, EStructuralFeature feature) {
		if (enableGuidelines) {
			switch (severityGuidelines) {
				case "info": {
					info(message, source, feature);
				}
				case "warning": {
					warning(message, source, feature);
				}
				case "error": {
					error(message, source, feature);
				}
				default: {
					println("Invalid severity for guidelines, chose either info, warning or error.");
				}
			}
		}
	}

	def markRule(String message, EObject source, EStructuralFeature feature) {
		if (enableRules) {
			switch (severityRules) {
				case "info": {
					info(message, source, feature);
				}
				case "warning": {
					warning(message, source, feature);
				}
				case "error": {
					error(message, source, feature);
				}
				default: {
					println("Invalid severity for rules, chose either info, warning or error.");
				}
			}
		}
	}

	// =====================================================================================================
	// Rules based on the Gherkin Definition
	// =====================================================================================================
	@Check
	def validateFeature(Feature feature) {
		if (feature.header == null) {
			markRule("Features require a header in the form of \"Feature: [Name]\"!", feature,
				GherkinPackage.Literals.FEATURE__HEADER);
		}

		if (feature.header == null && feature.background == null &&
			(feature.scenarios == null || feature.scenarios.size == 0)) {
			markRule("Feature has not a single keyword, therefore it is not considered to be one.", feature,
				GherkinPackage.Literals.FEATURE__DESC);
		}
	}

	@Check
	def validateFeatureHeader(FeatureHeader featureHeader) {
		if (featureHeader.name.length == 0) {
			markRule("Features require a name!", featureHeader, GherkinPackage.Literals.FEATURE_HEADER__NAME);
		}

		if (featureHeader.keyword.contains("Ability")) {
			markRule("Invalid keyword, use either \"Feature\" or \"Narrative\"!", featureHeader,
				GherkinPackage.Literals.FEATURE_HEADER__KEYWORD);
		}
	}

	@Check
	def validateBackground(Background background) {
		if (background.steps.length == 0) {
			markRule("Backgrounds require at least one Step!", background, GherkinPackage.Literals.BACKGROUND__STEPS);
		}

		if (background.tags != null) {
			markRule("Backgrounds are not supposed to have tags!", background,
				GherkinPackage.Literals.BACKGROUND__TAGS);
		}
	}

	@Check
	def validateScenario(Scenario scenario) {
		if (scenario.name.length == 0) {
			markRule("Scenario requires a name!", scenario, GherkinPackage.Literals.SCENARIO__NAME);
		}

		if (scenario.steps.length == 0) {
			markRule("Scenarios require at least one Step!", scenario, GherkinPackage.Literals.SCENARIO__STEPS);
		}

		var String scenarioType = scenario.keyword.replace(" ", "").replace("\t", "");
		if (scenarioType.equals("ScenarioOutline:") && (scenario.examples == null || scenario.examples.size == 0)) {
			markRule("Scenario Outlines require an \"Examples:\" table. If there is none, Scenario should be used.",
				scenario, GherkinPackage.Literals.SCENARIO__EXAMPLES);
		} else if (scenarioType.equals("Scenario:") && scenario.examples != null && scenario.examples.size > 0) {
			markRule("Scenarios are not allowed to have an Outline (Examples), use 'Scenario Outline:' instead.",
				scenario, GherkinPackage.Literals.SCENARIO__EXAMPLES);
		}
	}

	@Check
	def validateExamples(Examples examples) {
		if (examples.table == null) {
			markRule("Example is missing a table!", examples, GherkinPackage.Literals.EXAMPLES__TABLE);
		} else {
			if (examples.table.rows != null && examples.table.rows.size > 0) {
				var Row heading = examples.table.rows.get(0);
				for (Cell cell : heading.cells) {
					if (cell.name.length == 0) {
						markRule(
							"First row should not have empty empty cells, as they are considered to be variable names.",
							examples.table, GherkinPackage.Literals.TABLE__ROWS);
					}
				}
			}
		}
	}

	@Check
	def validateTable(Table table) {
		if (table.rows != null && table.rows.size > 0) {
			var Row heading = table.rows.get(0);
			for (Cell cell : heading.cells) {
				if (cell.name.length == 0) {
					markRule(
						"First row should not have empty empty cells, as they are considered to be variable names.",
						table, GherkinPackage.Literals.TABLE__ROWS);
				}
			}

//			for (var int i = 1; i < table.rows.size; i++){
//				if (table.rows.get(i).cells.size != heading.cells.size){
//					error("Number of cells inconsistent. Keep a consistent number of cells with the first row.", GherkinPackage.Literals.TABLE__ROWS);
//				}
//			}
		}
	}

	@Check
	def validateExamplesRow(Row row) {
		if (row.cells.length == 0) {
			markRule("Row requires at least one cell!", row, GherkinPackage.Literals.ROW__CELLS);
		}

		var ICompositeNode node = NodeModelUtils.getNode(row);
		if (node.text.matches(".*\\|[ \\t]*#.*\\|.*\r?\n")) {
			markRule(
				"Unescaped comment inside table, all the following cells will not be part of the table. Escape via \\#.",
				row, GherkinPackage.Literals.ROW__CELLS);
		}

		val Table tab = row.eContainer as Table;
		if (tab.rows.get(0).cells.size > row.cells.size) {
			// less is still possible to create
			markRule("Too few cells. Should be " + tab.rows.get(0).cells.size + " like in the header.", row,
				GherkinPackage.Literals.ROW__CELLS);
		}
		if (tab.rows.get(0).cells.size < row.cells.size) {
			markRule("Too many cells. Should be " + tab.rows.get(0).cells.size + " like in the header.", row,
				GherkinPackage.Literals.ROW__CELLS);
		}
	}

//
//	@Check
//	def validateCell(Cell cell){
//		if (cell.cellText.length == 0){
//			error("Cell requires at least one word!", GherkinPackage.Literals.CELL__CELL_TEXT);
//		}
//	}
//	
	@Check
	def validateStep(Step step) {
		if (step.name.length == 0) {
			markRule("Step requires some description on what it is supposed to do!", step,
				GherkinPackage.Literals.STEP__NAME);
		}

		var String keyword = step.keyword.toLowerCase().replace(":", "");

		if (keyword.equals("but") || keyword.equals("and")) {
			if (step.tags.tags.length > 0) {
				markRule(
					"And and But steps are an extension of the previous step, therefore no Tags allowed. If required, add them to the previous step.",
					step, GherkinPackage.Literals.STEP__TAGS);
			}
		}

		if (keyword.equals("i") || keyword.equals("check")) {
			markRule("Invalid Step, use one of the following: Given, When, Then, And, But, *", step,
				GherkinPackage.Literals.STEP__KEYWORD);
		}

		if (step.keyword.substring(0, 1).matches("[a-z]")) {
			markRule("Steps should start with a capital letter.", step, GherkinPackage.Literals.STEP__KEYWORD);
		}

		if (step.keyword.endsWith(":")) {
			markRule("Steps don't require a colon at the end.", step, GherkinPackage.Literals.STEP__KEYWORD);
		}
	}

//	@Check
//	def validateTags(Tags tags){
//	}
}
