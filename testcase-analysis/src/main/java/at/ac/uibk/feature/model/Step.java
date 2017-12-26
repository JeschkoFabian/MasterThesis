package at.ac.uibk.feature.model;

import java.util.ArrayList;
import java.util.List;

public class Step {
	private String keyword = "";
	private String name = "";
	private List<String> tags = new ArrayList<String>();
	private String docString = "";
	private Table table = null;

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
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

	public String getDocString() {
		return docString;
	}

	public void setDocString(String docString) {
		this.docString = docString;
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

		// tags
		if (tags != null && tags.size() > 0) {
			for (String tag : tags) {
				str.append(tag + "\n");
			}
		}

		// step line
		str.append(keyword + " ");
		str.append(name + "\n");

		// either docString or table or none
		if (docString != null) {
			str.append(docString + "\n");
		}
		if (table != null) {
			str.append(table.toString());
		}

		return str.toString();
	}
}
