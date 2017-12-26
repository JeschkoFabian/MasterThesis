/** 
 * Copyright (c) 2011 Sebastian Benz.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 * Sebastian Benz - initial API and implementation
 */
package at.ac.uibk.gherkin.ui.autoedit

import org.eclipse.xtext.ui.editor.autoedit.DefaultAutoEditStrategyProvider

class AutoEditStrategyProvider extends DefaultAutoEditStrategyProvider {
//	protected static class LineBreakInserter extends AbstractEditStrategy {
//		final IAutoEditStrategy defaultStrategy
//
//		protected new(IAutoEditStrategy defaultStrategy) {
//			this.defaultStrategy = defaultStrategy
//		}
//
//		override protected void internalCustomizeDocumentCommand(IDocument document,
//			DocumentCommand command) throws BadLocationException {
//			if (!isLineBreak(document, command)) {
//				return;
//			}
//			var String line = currentLine(document, command)
//			if (isEmpty(line)) {
//				return;
//			}
//			defaultStrategy.customizeDocumentCommand(document, command)
//			if (requiresIndent(line)) {
//				command.text = '''«command.text»
//				'''
//			}
//		}
//
//		def private boolean isEmpty(String line) {
//			return line.length() === 0
//		}
//
//		def private boolean requiresIndent(String line) {
//			return isScenario(line) || isExamples(line)
//		}
//
//		def private boolean isExamples(String line) {
//			return line.trim().endsWith(":")
//		}
//
//		def private boolean isScenario(String line) {
//			return line.contains("Scenario") && line.contains(":")
//		}
//
//		def private String currentLine(IDocument document, DocumentCommand command) throws BadLocationException {
//			var IRegion region = document.getLineInformationOfOffset(command.offset)
//			var String line = document.get(region.getOffset(), region.getLength())
//			return line
//		}
//
//		def private boolean isLineBreak(IDocument document, DocumentCommand command) {
//			return command.text.equals(((document as IDocumentExtension4)).getDefaultLineDelimiter()) &&
//				command.length === 0
//		}
//	}
//
//	protected static class IntendationInserter extends AbstractEditStrategy {
//		override protected void internalCustomizeDocumentCommand(IDocument document,
//			DocumentCommand command) throws BadLocationException {
//			if (!isIntend(document, command)) {
//				return;
//			}
//			var IRegion region = document.getLineInformationOfOffset(command.offset)
//			var String line = document.get(region.getOffset(), region.getLength())
//			if (line.trim().length() === 0) {
//				return;
//			}
//			document.replace(region.getOffset(), 0, "\t")
//			command.text = ""
//		}
//
//		def private boolean isIntend(IDocument document, DocumentCommand command) {
//			var String text = command.text
//			return text.startsWith("\t")
//		}
//	}
//
//	@Inject protected Provider<ShortCutEditStrategy> shortCut
//
//	override protected void configure(IEditStrategyAcceptor acceptor) {
//		super.configure(acceptor)
//		acceptor.accept(new IntendationInserter(), IDocument.DEFAULT_CONTENT_TYPE)
//	}
//
//	override protected void configureCurlyBracesBlock(IEditStrategyAcceptor acceptor) {
//	}
//
//	override protected void configureMultilineComments(IEditStrategyAcceptor acceptor) {
//	}
//
//	override protected void configureIndentationEditStrategy(IEditStrategyAcceptor acceptor) {
//		acceptor.accept(new LineBreakInserter(defaultIndentLineAutoEditStrategy.get()), IDocument.DEFAULT_CONTENT_TYPE)
//	}
}
