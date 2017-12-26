/** 
 * Copyright (c) 2011 Sebastian Benz.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 * Sebastian Benz - initial API and implementation
 */
package at.ac.uibk.gherkin.ui.highlighting

import at.ac.uibk.gherkin.gherkin.Background
import at.ac.uibk.gherkin.gherkin.DescriptionLine
import at.ac.uibk.gherkin.gherkin.Examples
import at.ac.uibk.gherkin.gherkin.FeatureHeader
import at.ac.uibk.gherkin.gherkin.GherkinPackage
import at.ac.uibk.gherkin.gherkin.Row
import at.ac.uibk.gherkin.gherkin.Scenario
import at.ac.uibk.gherkin.gherkin.Step
import at.ac.uibk.gherkin.gherkin.Table
import at.ac.uibk.gherkin.gherkin.util.GherkinSwitch
import java.util.Iterator
import java.util.List
import java.util.regex.Pattern
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.ide.editor.syntaxcoloring.IHighlightedPositionAcceptor
import org.eclipse.xtext.ide.editor.syntaxcoloring.ISemanticHighlightingCalculator
import org.eclipse.xtext.nodemodel.ICompositeNode
import org.eclipse.xtext.nodemodel.INode
import org.eclipse.xtext.nodemodel.util.NodeModelUtils
import org.eclipse.xtext.resource.XtextResource
import org.eclipse.xtext.util.CancelIndicator

class SemanticHighlightingCalculator implements ISemanticHighlightingCalculator {
	static final Pattern PLACEHOLDER = Pattern.compile("<[^<\\r\\n]*>")

	private static class Implementation extends GherkinSwitch<Boolean> {
		final IHighlightedPositionAcceptor acceptor

		new(IHighlightedPositionAcceptor acceptor) {
			this.acceptor = acceptor
		}

		override Boolean caseDescriptionLine(DescriptionLine line) {
			var String[] names = line.getName();
			var text = names.join(" ");
			var List<INode> nodes = NodeModelUtils.findNodesForFeature(line,
				GherkinPackage.Literals.DESCRIPTION_LINE__NAME);
			var Iterator<INode> iter = nodes.iterator();

			var String[] narratives = #["I want to", "In order to", "As a"];

			for (String narrative : narratives) {
				if (text.startsWith(narrative)) {
					for (var int i = 0; i < narrative.split(" ").length() && iter.hasNext(); i++) {
						var INode node = iter.next();
						acceptor.addPosition(node.getOffset(), names.get(i).length,
							HighlightingConfiguration.KEYWORD_ID);
					}
				}
			}

			return Boolean.TRUE
		}

		override Boolean caseTable(Table table) {
			if (table != null && table.rows != null && table.rows.size > 0) {
				var Row heading = table.rows.get(0)
				if (heading === null) {
					return Boolean.TRUE
				}
				var ICompositeNode node = NodeModelUtils.getNode(heading);
				var String txt = node.text;
				// find first occurrence of unescaped comment
				var int nodeEnd = Math.max(Math.max(txt.indexOf(" #"), txt.indexOf("|#")), txt.indexOf("	#")) -
					txt.indexOf("|") + 1; // exclude part before table;
				if (nodeEnd <= 0) {
					nodeEnd = node.getLength();
				}
				acceptor.addPosition(node.getOffset(), nodeEnd, HighlightingConfiguration.CODE_BOLD_ID)

				// set non heading rows			
				for (var int i = 1; i < table.rows.length; i++) {
					var Row row = table.rows.get(i);
					var ICompositeNode rowNode = NodeModelUtils.getNode(row);
					txt = rowNode.text;
					nodeEnd = Math.max(Math.max(txt.indexOf(" #"), txt.indexOf("|#")), txt.indexOf("	#")) -
						txt.indexOf("|") + 1; // exclude part before table;
					if (nodeEnd <= 0) {
						nodeEnd = rowNode.getLength();
					}
					acceptor.addPosition(rowNode.getOffset(), nodeEnd, HighlightingConfiguration.CODE_ID)
				}

			}
			return Boolean.TRUE
		}

//		override Boolean caseExample(Example example) {
//			var ExampleRow heading = example.rows.get(0)
//			if (heading === null) {
//				return Boolean.TRUE
//			}
//			var ICompositeNode node = NodeModelUtils.getNode(heading)
//			acceptor.addPosition(node.getOffset(), node.getLength(), HighlightingConfiguration.CODE_BOLD_ID)
//
//			// set non heading rows			
//			for (var int i = 1; i < example.rows.length; i++){
//				var ExampleRow row = example.rows.get(i);
//				var ICompositeNode rowNode = NodeModelUtils.getNode(row)
//				acceptor.addPosition(rowNode.getOffset(), rowNode.getLength(), HighlightingConfiguration.CODE_ID)
//			}
//			
//			return Boolean.TRUE
//		}
		override Boolean caseScenario(Scenario object) {
			var String scenType = object.keyword;
			var INode node = NodeModelUtils.findNodesForFeature(object, GherkinPackage.Literals.STEP__KEYWORD).head;
			
			if (scenType != null){
				acceptor.addPosition(node.offset, scenType.length, HighlightingConfiguration.KEYWORD_ID);
			}

			return Boolean.TRUE
		}

		override Boolean caseExamples(Examples object) {
			var String examplesType = object.keyword;
			var INode node = NodeModelUtils.findNodesForFeature(object, GherkinPackage.Literals.STEP__KEYWORD).head;
			acceptor.addPosition(node.offset, examplesType.length, HighlightingConfiguration.KEYWORD_ID);

			return Boolean.TRUE
		}

		override Boolean caseBackground(Background object) {
			var String backgroundType = object.keyword;
			var INode node = NodeModelUtils.findNodesForFeature(object, GherkinPackage.Literals.STEP__KEYWORD).head;
			acceptor.addPosition(node.offset, backgroundType.length, HighlightingConfiguration.KEYWORD_ID);

			return Boolean.TRUE
		}

		override Boolean caseFeatureHeader(FeatureHeader object) {
			var String headerType = object.keyword;
			var INode node = NodeModelUtils.findNodesForFeature(object, GherkinPackage.Literals.STEP__KEYWORD).head;
			acceptor.addPosition(node.offset, headerType.length, HighlightingConfiguration.KEYWORD_ID);

			return Boolean.TRUE
		}

		override Boolean caseStep(Step object) {
			var String stepName = object.keyword;
			var INode node = NodeModelUtils.findNodesForFeature(object, GherkinPackage.Literals.STEP__KEYWORD).head;
			acceptor.addPosition(node.offset, stepName.length, HighlightingConfiguration.KEYWORD_ID);

			var String[] desc = object.name
			hightlightPlaceHolders(object, desc)
			return Boolean.TRUE
		}

		def private void hightlightPlaceHolders(Step object, String[] desc) {
			var List<INode> nodes = NodeModelUtils.findNodesForFeature(object, GherkinPackage.Literals.STEP__NAME)
			var Iterator<INode> iter = nodes.iterator();

			for (String word : desc) {
				var int offset = iter.next().getOffset();
				if (PLACEHOLDER.matcher(word).matches()) {
					// highlight for the length of the word
					var int length = word.length();
					acceptor.addPosition(offset, length, HighlightingConfiguration.CODE_BOLD_ID)
				}
			}
		}
	}

	override void provideHighlightingFor(XtextResource resource, IHighlightedPositionAcceptor acceptor,
		CancelIndicator cancelor) {
		if (noNodeModel(resource)) {
			return;
		}
		var Implementation highlighter = new Implementation(acceptor)
		var Iterator<EObject> contents = resource.getAllContents()
		while (contents.hasNext()) {
			highlighter.doSwitch((contents.next() as EObject))
		}
	}

	def protected EObject root(XtextResource resource) {
		return resource.getParseResult().getRootASTElement()
	}

	def protected boolean noNodeModel(XtextResource resource) {
		return resource === null || resource.getParseResult() === null || root(resource) === null
	}
}
