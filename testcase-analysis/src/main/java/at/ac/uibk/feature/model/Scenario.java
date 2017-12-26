package at.ac.uibk.feature.model;

import java.util.ArrayList;
import java.util.List;

public class Scenario {
	private ScenarioType type = null;
	private String name = "";
	private List<String> tags = new ArrayList<String>();
	private List<String> description = new ArrayList<String>();
	private List<Step> steps = new ArrayList<Step>();
	private List<Examples> outline = new ArrayList<Examples>();

	public ScenarioType getType() {
		return type;
	}

	public void setType(ScenarioType type) {
		this.type = type;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public List<String> getDescription() {
		return description;
	}

	public void setDescription(List<String> description) {
		this.description = description;
	}

	public List<Step> getSteps() {
		return steps;
	}

	public void setSteps(List<Step> steps) {
		this.steps = steps;
	}

	public List<Examples> getOutline() {
		return outline;
	}

	public void setOutline(List<Examples> outline) {
		this.outline = outline;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();

		// tags
		if (tags != null && tags.size() > 0) {
			for (String tag : tags) {
				str.append(tag + "\n");
			}
		}

		// headline
		if (type != null) {
			switch(type){
				case BACKGROUND: str.append("Background: "); break;
				case SCENARIO: str.append("Scenario: "); break;
				case SCENARIO_OUTLINE: str.append("Scenario with Outline: "); break;
			}
		}
		if(name != null){
			str.append(name + "\n");
		} else {
			str.append("\n");
		}

		// desc lines
		if (description != null && description.size() > 0) {
			for (String line : description) {
				str.append("\t" + line + "\n");
			}
		}

		// steps
		if (steps != null && steps.size() > 0) {
			for (Step step : steps) {
				str.append("\t" + step.toString());
			}
		}

		// outline
		if (outline != null) {
			for (Examples ex : outline){
				str.append(ex.toString());
			}
		}

		return str.toString();
	}

}
