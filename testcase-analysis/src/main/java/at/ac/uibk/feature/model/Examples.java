package at.ac.uibk.feature.model;

import java.util.ArrayList;
import java.util.List;

public class Examples {
	private String name = "";
	private List<String> description = new ArrayList<String>();
	private List<String> tags = new ArrayList<String>();
	private Table table = null;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getDescription() {
		return description;
	}

	public void setDescription(List<String> description) {
		this.description = description;
	}
	
	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public Table getTable() {
		return table;
	}

	public void setTable(Table table) {
		this.table = table;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();

		// headline
		str.append("Examples: ");
		if(name != null){
			str.append(name + "\n");
		}

		// tags
		if (tags != null && tags.size() > 0) {
			for (String tag : tags) {
				str.append(tag + "\n");
			}
		}

		// desc lines
		if (description != null && description.size() > 0) {
			for (String line : description) {
				str.append("\t" + line + "\n");
			}
		}

		// table
		if (table != null) {
			str.append(table.toString());
		}

		return str.toString();
	}
}
