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

import org.eclipse.swt.SWT
import org.eclipse.swt.graphics.RGB
import org.eclipse.xtext.ui.editor.syntaxcoloring.DefaultHighlightingConfiguration
import org.eclipse.xtext.ui.editor.syntaxcoloring.IHighlightingConfigurationAcceptor
import org.eclipse.xtext.ui.editor.utils.TextStyle

class HighlightingConfiguration extends DefaultHighlightingConfiguration {
	public static final String TAG_ID = "Tag"
	public static final String CODE_ID = "Code"
	public static final String CODE_BOLD_ID = "CodeBold"

	override void configure(IHighlightingConfigurationAcceptor acceptor) {
		acceptor.acceptDefaultHighlighting(COMMENT_ID, "Comment", commentTextStyle())
		acceptor.acceptDefaultHighlighting(NUMBER_ID, "Number", numberTextStyle())
		acceptor.acceptDefaultHighlighting(DEFAULT_ID, "Default", defaultTextStyle())
		acceptor.acceptDefaultHighlighting(INVALID_TOKEN_ID, "Invalid Symbol", errorTextStyle())
		acceptor.acceptDefaultHighlighting(TAG_ID, "Tag", tagTextStyle())
		acceptor.acceptDefaultHighlighting(CODE_ID, "Code", codeTextStyle())
		acceptor.acceptDefaultHighlighting(CODE_BOLD_ID, "Code Bold", codeBoldTextStyle())
		acceptor.acceptDefaultHighlighting(KEYWORD_ID, "Keyword", keywordTextStyle())
	}

	def private TextStyle tagTextStyle() {
		var TextStyle textStyle = super.defaultTextStyle().copy()
		textStyle.setColor(grey())
		return textStyle
	}

	def protected RGB grey() {
		return new RGB(125, 125, 125)
	}

	def TextStyle codeTextStyle() {
		var TextStyle textStyle = super.defaultTextStyle().copy()
		textStyle.setColor(new RGB(42, 0, 255))
		return textStyle
	}

	def TextStyle codeBoldTextStyle() {
		var TextStyle textStyle = super.defaultTextStyle().copy()
		textStyle.setColor(new RGB(255, 0, 0))
		return textStyle
	}

	override TextStyle keywordTextStyle() {
		var TextStyle textStyle = super.defaultTextStyle().copy()
		textStyle.style = SWT.BOLD;
		return textStyle
	}
	
//	override TextStyle defaultTextStyle() {
//		var TextStyle textStyle = new TextStyle()
//		textStyle.setColor(new RGB(0, 0, 0))
//		textStyle.setFontData(fontWithHeight(DEFAULT_FONT_SIZE, SWT.NORMAL))
//		return textStyle
//	}
}
