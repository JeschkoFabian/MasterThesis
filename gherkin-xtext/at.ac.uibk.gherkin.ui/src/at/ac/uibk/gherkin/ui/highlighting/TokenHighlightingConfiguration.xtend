package at.ac.uibk.gherkin.ui.highlighting

import org.eclipse.xtext.ui.editor.syntaxcoloring.DefaultAntlrTokenToAttributeIdMapper

class TokenHighlightingConfiguration extends DefaultAntlrTokenToAttributeIdMapper {
	
	override protected String calculateId(String tokenName, int tokenType) {
		if ("RULE_TAG".equals(tokenName)) {
			return HighlightingConfiguration.TAG_ID;
		} else if ("RULE_CODE".equals(tokenName)) {
			return HighlightingConfiguration.CODE_ID;
		}
		
		return super.calculateId(tokenName, tokenType);
	}
}
