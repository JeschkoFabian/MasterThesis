package at.ac.uibk.testcase_analysis.violations;

public enum SyntaxRule {
	// Any kind of violation
	ANY,
	
	// Background Rules
	BACKGROUND_TAGS,
	BACKGROUND_NO_STEPS,

	// Examples Rules
	EXAMPLES_NO_TABLE,
	
	// Feature Rules
	FEATURE_NO_KEYWORD,
	FEATURE_NO_NAME,
	FEATURE_NO_SCENARIOS,
	
	// Scenario & Outline Rules
	SCENARIO_NO_NAME,
	SCENARIO_NO_STEPS,
	SCENARIO_EXAMPLES,
	SCENARIO_OUTLINE_NO_EXAMPLES,

	// Step Rules
	STEP_NO_NAME,
	STEP_LOWERCASE,
	STEP_COLON,
	STEP_INVALID_KW,
	
	// Table Rules
	TABLE_INCONSISTENT_CELLS,
	TABLE_HEADER_EMPTY
}
