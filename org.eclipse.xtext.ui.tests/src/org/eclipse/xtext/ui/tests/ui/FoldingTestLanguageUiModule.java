/*
 * generated by Xtext
 */
package org.eclipse.xtext.ui.tests.ui;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.xtext.ui.editor.IXtextEditorCallback;

/**
 * Use this class to register components to be used within the IDE.
 */
public class FoldingTestLanguageUiModule extends org.eclipse.xtext.ui.tests.ui.AbstractFoldingTestLanguageUiModule {
	public FoldingTestLanguageUiModule(AbstractUIPlugin plugin) {
		super(plugin);
	}
	
	@Override
	public Class<? extends IXtextEditorCallback> bindIXtextEditorCallback() {
		return IXtextEditorCallback.NullImpl.class;
	}
}