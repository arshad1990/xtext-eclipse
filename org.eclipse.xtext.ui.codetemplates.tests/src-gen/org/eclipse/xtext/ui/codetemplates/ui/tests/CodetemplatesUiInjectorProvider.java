/*
 * generated by Xtext
 */
package org.eclipse.xtext.ui.codetemplates.ui.tests;

import com.google.inject.Injector;
import org.eclipse.xtext.testing.IInjectorProvider;
import org.eclipse.xtext.ui.codetemplates.ui.internal.CodetemplatesActivator;

public class CodetemplatesUiInjectorProvider implements IInjectorProvider {

	@Override
	public Injector getInjector() {
		return CodetemplatesActivator.getInstance().getInjector("org.eclipse.xtext.ui.codetemplates.Codetemplates");
	}

}
