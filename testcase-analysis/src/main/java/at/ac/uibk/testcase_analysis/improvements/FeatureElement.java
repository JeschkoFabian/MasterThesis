package at.ac.uibk.testcase_analysis.improvements;

public enum FeatureElement {
	FEATURE("feature", "Feature", "FEATURE"),
	SCENARIO("scenarios", "Scenario", "SCENARIOS"),
	FEATURE_HEADER("header", "FeatureHeader", "FEATURE_HEADER"),
	DESCRIPTION("desc", "Description", "DESCRIPTION"),
	DESCRIPTION_LINE("lines", "DescriptionLine", "LINES"),
	SCENARIO_DESCRIPTION("desc", "ScenarioDescription", "SCENARIO_DESCRIPTION"),
	SCENARIO_DESCRIPTION_LINE("lines", "ScenarioDescriptionLine", "LINES"),
	BACKGROUND("background", "Background", "BACKGROUND"),
	EXAMPLES("examples", "Examples", "EXAMPLES"),
	TABLE("table", "Table", "TABLE"),
	ROW("rows", "Row", "ROWS"),
	// include head since only first row matters
	CELL("head.cells", "Cell", "CELLS"),
	STEP("steps", "Step", "STEPS"),
	// include tags since there is a in between step
	TAGS("tags.tags", "Tags", "TAGS"),
	NAME("name", "Name", "NAME");
	
	private String varName;
	private String varType;
	private String literalAlt;
	
	private FeatureElement(String varName, String varType, String literal){
		this.varName = varName;
		this.varType = varType;
		this.literalAlt = literal;
	}
	
	public String getVarName(){
		return varName;
	}
	
	public String getVarType(){
		return varType;
	}

	public String getLiteralAlt(){
		return literalAlt;
	}

}
