package at.ac.uibk.gherkin.ui.editor

import at.ac.uibk.gherkin.gherkin.DescriptionLine
import at.ac.uibk.gherkin.gherkin.Row
import at.ac.uibk.gherkin.gherkin.Step
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.ui.editor.folding.DefaultFoldingRegionProvider

class FoldingRegionProvider extends DefaultFoldingRegionProvider {
	override protected boolean isHandled(EObject eObject) {
		if (eObject instanceof Step) {
			var Step step = (eObject as Step)
			return !step.eContents().isEmpty() || !(step.name.size() <= 1)
		} else if (eObject instanceof Row) {
			return false
		} else if (eObject instanceof DescriptionLine) {
			return false
		}
		return super.isHandled(eObject)
	}
}
