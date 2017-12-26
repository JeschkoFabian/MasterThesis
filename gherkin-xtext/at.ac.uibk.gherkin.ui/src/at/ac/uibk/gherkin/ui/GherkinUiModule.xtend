/*
 * generated by Xtext 2.10.0
 */
package at.ac.uibk.gherkin.ui

import at.ac.uibk.gherkin.ui.highlighting.HighlightingConfiguration
import at.ac.uibk.gherkin.ui.highlighting.SemanticHighlightingCalculator
import at.ac.uibk.gherkin.ui.highlighting.TokenHighlightingConfiguration
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor
import org.eclipse.xtext.ide.editor.syntaxcoloring.ISemanticHighlightingCalculator
import org.eclipse.xtext.ui.editor.syntaxcoloring.AbstractAntlrTokenToAttributeIdMapper
import org.eclipse.xtext.ui.editor.syntaxcoloring.IHighlightingConfiguration

/**
 * Use this class to register components to be used within the Eclipse IDE.
 */
@FinalFieldsConstructor
class GherkinUiModule extends AbstractGherkinUiModule {
	
	def Class<? extends ISemanticHighlightingCalculator> bindISemanticHighlightingCalculator() {
		return SemanticHighlightingCalculator;
	}

	def Class<? extends IHighlightingConfiguration> bindIHighlightingConfiguration() {
		return HighlightingConfiguration;
	}
	
	def Class<? extends AbstractAntlrTokenToAttributeIdMapper> bindAbstractAntlrTokenToAttributeIdMapper() {
		return TokenHighlightingConfiguration;
	}
	
//	override Class<? extends AbstractEditStrategyProvider> bindAbstractEditStrategyProvider() {
//		return AutoEditStrategyProvider;
//	}
	
//	def Class<? extends XtextSourceViewerConfiguration> bindXtextSourceViewerConfiguration() {
//		return SourceViewerConfiguration;
//	}
//	
//	def Class<? extends IFoldingRegionProvider> bindIFoldingRegionProvider(){
//		return FoldingRegionProvider;
//	}
	
}