package at.ac.uibk.testcase_analysis.improvements;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class ValidationRuleBuilder {

	public static String buildBoundsRule(FeatureElement element, FeatureElement perElement,
			FeatureElement inbetweenElement, int lowerBound, int upperBound) {
		// smaller than 0 makes no sense
		if (lowerBound < 0) {
			lowerBound = 0;
		}
		
		if (lowerBound == 0 && upperBound == 0){
			return "Both bounds are 0, resulting rule would be nonsensical.";
		}

		StringBuilder rule = new StringBuilder();
		rule.append("@Check\n");
		rule.append("def validate" + element.getVarType() + "sPer" + perElement.getVarType() + "("
				+ perElement.getVarType() + " el){\n");

		String variablePath = "el.";
		String elementName = "el";
		String lowerBoundsCondition = "";
		String upperBoundsCondition = "";
		String nullPointerCondition = "";
		String literal = "";

		if (inbetweenElement == null) {
			// path is something like el.scenarios for example
			variablePath += element.getVarName();
			// literals are something like FEATURE__SCENARIOS, where the first
			// is usually the variable type in all caps
			// and the second the variable name in all caps
			literal += perElement + "__" + element.getLiteralAlt();
		} else {
			// same as above but with a in between variable
			variablePath += inbetweenElement.getVarName() + "." + element.getVarName();
			// try the more precise one, might have to be adjusted manually
			literal += inbetweenElement + "__" + element.getLiteralAlt();
			elementName += "." + inbetweenElement.getVarName();
		}
		// avoid exception
		nullPointerCondition += buildNullCheck(variablePath);
		// the actual conditions
		lowerBoundsCondition += variablePath + ".length < " + lowerBound;
		upperBoundsCondition += variablePath + ".length > " + upperBound;

		rule.append("	if(" + nullPointerCondition + "){\n");
		if (lowerBound > 0) {
			rule.append("		if(" + lowerBoundsCondition + "){\n");
			rule.append("			markImprovement(\"Abnormally low amount of " + element.getVarType() + "s per " + perElement.getVarType()
					+ ", should be " + lowerBound + "or more\", " + elementName + ", GherkinPackage.Literals." + literal + ");\n");
			rule.append("		}\n");
		}
		if (upperBound > 0) {
			rule.append("		if(" + upperBoundsCondition + "){\n");
			rule.append("			markImprovement(\"Abnormally high amount of " + element.getVarType() + "s per " + perElement.getVarType()
					+ ", should be " + upperBound + " or less\", " + elementName + ", GherkinPackage.Literals." + literal + ");\n");
			rule.append("		}\n");
		}
		rule.append("	}\n");
		rule.append("}");

		// @Check
		// def validateScenario(Scenario scenario){
		// if (scenario.name.length == 0){
		// warning("Scenario requires a name!",
		// GherkinPackage.Literals.SCENARIO__NAME);
		// }
		//
		// if (scenario.steps.length == 0){
		// warning("Scenarios require at least one Step!",
		// GherkinPackage.Literals.SCENARIO__STEPS);
		// }

		return rule.toString();
	}
	
	private static String buildNullCheck(String variable){
		// just use the elvis operator, duh
		String check = variable.replaceAll("\\.", "?.");
		check += " != null";
		return check;
	}

	public static String buildBoundsRule(FeatureElement element, FeatureElement perElement, int lowerBound,
			int upperBound) {
		return buildBoundsRule(element, perElement, null, lowerBound, upperBound);
	}

	public static String buildBoundsRule(FeatureElement element, FeatureElement perElement,
			FeatureElement inbetweenElement, DescriptiveStatistics stats) {
		// mean +- 2*StdDev should encapsulate 90% i think it was of the values,
		// 0.5 to round
//		int lowerBound = (int) stats.getPercentile(2.5);
		int upperBound = (int) stats.getPercentile(90.0);


		return buildBoundsRule(element, perElement, inbetweenElement, 0, upperBound);
	}

	public static String buildBoundsRule(FeatureElement element, FeatureElement perElement,
			DescriptiveStatistics stats) {
		return buildBoundsRule(element, perElement, null, stats);
	}

}
