/*
 * generated by Xtext 2.10.0
 */
package at.ac.uibk.gherkin.ui.labeling

import at.ac.uibk.gherkin.gherkin.Background
import at.ac.uibk.gherkin.gherkin.Cell
import at.ac.uibk.gherkin.gherkin.Description
import at.ac.uibk.gherkin.gherkin.DescriptionLine
import at.ac.uibk.gherkin.gherkin.Examples
import at.ac.uibk.gherkin.gherkin.ExamplesDescription
import at.ac.uibk.gherkin.gherkin.ExamplesDescriptionLine
import at.ac.uibk.gherkin.gherkin.FeatureHeader
import at.ac.uibk.gherkin.gherkin.Row
import at.ac.uibk.gherkin.gherkin.Scenario
import at.ac.uibk.gherkin.gherkin.ScenarioDescription
import at.ac.uibk.gherkin.gherkin.ScenarioDescriptionLine
import at.ac.uibk.gherkin.gherkin.Step
import at.ac.uibk.gherkin.gherkin.Table
import at.ac.uibk.gherkin.gherkin.Tags
import com.google.inject.Inject
import org.eclipse.emf.common.util.EList
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider
import org.eclipse.xtext.ui.label.DefaultEObjectLabelProvider

/**
 * Provides labels for EObjects.
 * 
 * See https://www.eclipse.org/Xtext/documentation/304_ide_concepts.html#label-provider
 */
class GherkinLabelProvider extends DefaultEObjectLabelProvider {

	@Inject
	new(AdapterFactoryLabelProvider delegate) {
		super(delegate);
	}

	def package String text(FeatureHeader featureHeader){
		return getHeader(featureHeader.keyword, featureHeader.name);
	}

	def package String text(Description desc){
		return "Description";
	}
	
	def package String text(DescriptionLine line){
		return line.name.join(" ");
	}

	def package String text(ScenarioDescription desc){
		return "Description";
	}
	
	def package String text(ScenarioDescriptionLine line){
		return line.name.join(" ");
	}

	def package String text(ExamplesDescription desc){
		return "Description";
	}
	
	def package String text(ExamplesDescriptionLine line){
		return line.name.join(" ");
	}

	
	def package String text(Background background) {
		return getHeader(background.keyword, null);
	}
	
	def package String text(Scenario scenario){
		return getHeader(scenario.keyword, scenario.name);
	}


	def package String text(Examples examples) {
		return getHeader(examples.keyword, null);
	}
	
	def package String text(Table table){
		return "Table";
	}
	
	def package String text(Row row){
		var String txt = "| ";
		
		for (Cell cell : row.cells){
			if (cell.name != null && cell.name.length > 0){
				txt += cell.name.join(" ") + " | ";
			} else {
				txt += " | ";
			}
		}
		
		return txt;
	}
	
	def package String text(Cell cell){
		return cell.name.join(" ");
	}

	def package String text(Step step) {
		return getHeader(step.keyword, step.name);
	}
	
	def package String text(Tags tags){
		return "Tags: " + tags.tags.join(" ");
	}
	
	def String getHeader(String keyword, EList<String> words){
		var String txt = keyword;
		if (words != null && words.length > 0){
			txt += " " + words.join(" ")
		}
		return txt;
	}
	
//
//	def package String text(ExampleRow row) {
//		return '''| �Joiner.on(" | ").join(Iterables.transform(row.getCells(), [String from|return from]))� |'''
//	}
	
	// Labels and icons can be computed like this:
	
//	def text(Greeting ele) {
//		'A greeting to ' + ele.name
//	}
//
//	def image(Greeting ele) {
//		'Greeting.gif'
//	}
}
