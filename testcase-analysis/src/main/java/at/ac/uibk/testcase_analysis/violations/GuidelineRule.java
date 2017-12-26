package at.ac.uibk.testcase_analysis.violations;

public enum GuidelineRule {
	// Any kind of violation
	ANY,

	// Background Rules
	STEPS_PER_BACKGROUND, 
	BACKGROUND_NAME,
	BACKGROUND_GIVEN_ONLY,

	// Scenario Rules
	STEPS_PER_SCENARIO,
	SINGLE_WHEN, 
	NO_CONSECUTIVE_GIVEN_THEN
//	NO_IDENTICAL_STEPS
}
