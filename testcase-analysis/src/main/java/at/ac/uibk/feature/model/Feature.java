package at.ac.uibk.feature.model;

import java.util.ArrayList;
import java.util.List;

public class Feature {
	private String keyword = "";
	private String fileName = "";
	private String name = "";
	private List<String> tags = new ArrayList<String>();
	private List<String> description = new ArrayList<String>();
	private Scenario background = null;
	private List<Scenario> scenarios = new ArrayList<Scenario>();

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
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

	public Scenario getBackground() {
		return background;
	}

	public void setBackground(Scenario background) {
		this.background = background;
	}

	public List<Scenario> getScenarios() {
		return scenarios;
	}

	public void setScenarios(List<Scenario> scenarios) {
		this.scenarios = scenarios;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();

		str.append("#file: " + fileName + "\n");
		// tags
		if (tags != null && tags.size() > 0) {
			for (String tag : tags) {
				str.append(tag + "\n");
			}
		}

		// headline
		str.append("Feature: " + name + "\n");

		// desc lines
		if (description != null && description.size() > 0) {
			for (String line : description) {
				str.append("\t" + line + "\n");
			}
		}

		// background if theres one
		if (background != null) {
			str.append(background);
		}

		// scenarios
		if (scenarios != null && scenarios.size() > 0) {
			for (Scenario scenario : scenarios) {
				str.append(scenario);
			}
		}

		return str.toString();
	}
}
