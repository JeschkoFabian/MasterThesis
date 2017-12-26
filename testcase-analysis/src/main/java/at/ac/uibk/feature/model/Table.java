package at.ac.uibk.feature.model;

import java.util.ArrayList;
import java.util.List;

public class Table {
	// table in the form of rows -> cells
	private List<List<String>> rows = new ArrayList<List<String>>();

	public List<List<String>> getRows() {
		return rows;
	}

	public void setRows(List<List<String>> rows) {
		this.rows = rows;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();

		// headline
		str.append("Table: \n");

		// rows
		if (rows != null && rows.size() > 0) {
			for (List<String> row : rows){
				for (String name : row) {
					str.append("| " + name);
				}
				str.append(" |\n");
			}
		}

		return str.toString();
	}
}
