/*******************************************************************************
 * Copyright (c) 2010 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.common.types.util;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.common.types.ArrayType;
import org.eclipse.xtext.common.types.GenericArrayTypeReference;
import org.eclipse.xtext.common.types.LowerBound;
import org.eclipse.xtext.common.types.ParameterizedTypeReference;
import org.eclipse.xtext.common.types.ReferenceTypeArgument;
import org.eclipse.xtext.common.types.TypeArgument;
import org.eclipse.xtext.common.types.TypeReference;
import org.eclipse.xtext.common.types.TypesFactory;
import org.eclipse.xtext.common.types.UpperBound;
import org.eclipse.xtext.common.types.WildcardTypeArgument;
import org.eclipse.xtext.common.types.access.ITypeProvider;
import org.eclipse.xtext.common.types.access.impl.ClassURIHelper;
import org.eclipse.xtext.common.types.access.impl.DeclaredTypeFactory;

/**
 * @author svenefftinge - Initial contribution and API
 */
public abstract class AbstractAssignabilityComputerTest extends TestCase {

	private AssignabilityComputer computer;
	private DeclaredTypeFactory factory = new DeclaredTypeFactory(new ClassURIHelper());

	protected TypeReference ref(java.lang.reflect.Type type, TypeArgument... arguments) {
		TypeReference createTypeReference = factory.createTypeReference(type);
		if (arguments.length > 0) {
			for (TypeArgument typeArgument : arguments) {
				((ParameterizedTypeReference) createTypeReference).getArguments().add(typeArgument);
			}
		}
		Resource syntheticResource = getSyntheticResource();
		syntheticResource.getContents().add(createTypeReference);
		EcoreUtil.resolveAll(syntheticResource);
		return createTypeReference;
	}

	protected GenericArrayTypeReference array(TypeReference typeRef, int i) {
		GenericArrayTypeReference result = TypesFactory.eINSTANCE.createGenericArrayTypeReference();
		ArrayType arrayType = TypesFactory.eINSTANCE.createArrayType();
		result.setType(arrayType);
		if (i == 1) {
			arrayType.setComponentType(typeRef);
		} else {
			arrayType.setComponentType(array(typeRef, i - 1));
		}
		return result;
	}

	protected ReferenceTypeArgument arg(Type type) {
		return arg(ref(type));
	}

	protected ReferenceTypeArgument arg(TypeReference typeRef) {
		ReferenceTypeArgument argument = TypesFactory.eINSTANCE.createReferenceTypeArgument();
		argument.setTypeReference(typeRef);
		return argument;
	}

	protected WildcardTypeArgument wc() {
		WildcardTypeArgument result = TypesFactory.eINSTANCE.createWildcardTypeArgument();
		return result;
	}

	protected WildcardTypeArgument wc_extends(TypeReference typeRef) {
		WildcardTypeArgument result = TypesFactory.eINSTANCE.createWildcardTypeArgument();
		UpperBound upperBound = TypesFactory.eINSTANCE.createUpperBound();
		upperBound.setTypeReference(typeRef);
		result.getConstraints().add(upperBound);
		return result;
	}

	protected WildcardTypeArgument wc_super(TypeReference typeRef) {
		WildcardTypeArgument result = TypesFactory.eINSTANCE.createWildcardTypeArgument();
		LowerBound lowerBound = TypesFactory.eINSTANCE.createLowerBound();
		lowerBound.setTypeReference(typeRef);
		result.getConstraints().add(lowerBound);
		return result;
	}

	protected abstract ITypeProvider getTypeProvider();

	protected abstract Resource getSyntheticResource();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		computer = new AssignabilityComputer(new SuperTypeCollector());
	}

	@Override
	protected void tearDown() throws Exception {
		computer = null;
		super.tearDown();
	}

	/**
	 * List<? super String> <= List<? super CharSequence> (but not vice versa)
	 */
	public void testRawTypes() throws Exception {
		TypeReference rawList = ref(List.class);
		TypeReference List_of_super_String = ref(List.class, wc_super(ref(String.class)));
		assertTrue(computer.isAssignableFrom(rawList, List_of_super_String));
		assertFalse(computer.isAssignableFrom(List_of_super_String, rawList));
	}
	/**
	 * List<? super String> <= List<? super CharSequence> (but not vice versa)
	 */
	public void testGenerics_super_1() throws Exception {
		TypeReference List_of_super_CharSequence = ref(List.class, wc_super(ref(CharSequence.class)));
		TypeReference List_of_super_String = ref(List.class, wc_super(ref(String.class)));
		assertTrue(computer.isAssignableFrom(List_of_super_String, List_of_super_CharSequence));
		assertFalse(computer.isAssignableFrom(List_of_super_CharSequence, List_of_super_String));
	}

	/**
	 * List<?> <= List<CharSequence>
	 */
	public void testGenerics_UnconstraintWildcard_1() throws Exception {
		TypeReference List_CharSequence = ref(List.class, arg(CharSequence.class));
		TypeReference List_of_wildcard = ref(List.class, wc());
		assertTrue(computer.isAssignableFrom(List_of_wildcard, List_CharSequence));
		assertFalse(computer.isAssignableFrom(List_CharSequence, List_of_wildcard));
	}
	/**
	 * List<?> <= List<? super CharSequence> 
	 */
	public void testGenerics_UnconstraintWildcard_2() throws Exception {
		TypeReference List_of_super_CharSequence = ref(List.class, wc_super(ref(CharSequence.class)));
		TypeReference List_of_wildcard = ref(List.class, wc());
		assertTrue(computer.isAssignableFrom(List_of_wildcard, List_of_super_CharSequence));
		assertFalse(computer.isAssignableFrom(List_of_super_CharSequence, List_of_wildcard));
	}
	/**
	 * List<?> <= List<? extends CharSequence> 
	 */
	public void testGenerics_UnconstraintWildcard_3() throws Exception {
		TypeReference List_of_extends_CharSequence = ref(List.class, wc_extends(ref(CharSequence.class)));
		TypeReference List_of_wildcard = ref(List.class, wc());
		assertTrue(computer.isAssignableFrom(List_of_wildcard, List_of_extends_CharSequence));
		assertFalse(computer.isAssignableFrom(List_of_extends_CharSequence, List_of_wildcard));
	}

	public void testGenerics_1() throws Exception {
		TypeReference List_of_String = ref(List.class, arg(String.class));
		TypeReference List_of_extends_String = ref(List.class, wc_extends(ref(String.class)));
		assertTrue(computer.isAssignableFrom(List_of_extends_String, List_of_String));
		assertFalse(computer.isAssignableFrom(List_of_String, List_of_extends_String));
	}

	public void testGenerics_2() throws Exception {
		TypeReference List_of_String = ref(List.class, arg(String.class));
		TypeReference Collection_of_String = ref(Collection.class, arg(String.class));
		assertTrue(computer.isAssignableFrom(Collection_of_String, List_of_String));
		assertFalse(computer.isAssignableFrom(List_of_String, Collection_of_String));
	}

	public void testGenerics_3() throws Exception {
		TypeReference Func_of_String_String = ref(Map.class, arg(String.class), arg(String.class));
		TypeReference Func_of_extends_String_String = ref(Map.class, wc_extends(ref(String.class)), arg(String.class));
		assertTrue(computer.isAssignableFrom(Func_of_extends_String_String, Func_of_String_String));
		assertFalse(computer.isAssignableFrom(Func_of_String_String, Func_of_extends_String_String));
	}

	public void testGenerics_4() throws Exception {
		TypeReference Func_of_String_String = ref(Map.class, arg(String.class), arg(String.class));
		TypeReference Func_of_extends_String_String = ref(Map.class, arg(String.class), arg(String.class));
		assertTrue(computer.isAssignableFrom(Func_of_extends_String_String, Func_of_String_String));
		assertTrue(computer.isAssignableFrom(Func_of_String_String, Func_of_extends_String_String));
	}

	public void testSameType() throws Exception {
		assertTrue(computer.isAssignableFrom(ref(String.class), ref(String.class)));
	}

	public void testInheritanceCompatibility() throws Exception {
		assertTrue(computer.isAssignableFrom(ref(CharSequence.class), ref(String.class)));
		assertFalse(computer.isAssignableFrom(ref(String.class), ref(CharSequence.class)));
	}

	public void testAutoBoxingInteger() throws Exception {
		assertTrue(computer.isAssignableFrom(ref(Integer.TYPE), ref(Integer.class)));
		assertTrue(computer.isAssignableFrom(ref(Integer.class), ref(Integer.TYPE)));
	}

	public void testAutoBoxingBoolean() throws Exception {
		assertTrue(computer.isAssignableFrom(ref(Boolean.TYPE), ref(Boolean.class)));
		assertTrue(computer.isAssignableFrom(ref(Boolean.class), ref(Boolean.TYPE)));
	}

	public void testAutoBoxingLong() throws Exception {
		assertTrue(computer.isAssignableFrom(ref(Long.TYPE), ref(Long.class)));
		assertTrue(computer.isAssignableFrom(ref(Long.class), ref(Long.TYPE)));
	}

	public void testAutoBoxingFloat() throws Exception {
		assertTrue(computer.isAssignableFrom(ref(Float.TYPE), ref(Float.class)));
		assertTrue(computer.isAssignableFrom(ref(Float.class), ref(Float.TYPE)));
	}

	public void testAutoBoxingDouble() throws Exception {
		assertTrue(computer.isAssignableFrom(ref(Double.TYPE), ref(Double.class)));
		assertTrue(computer.isAssignableFrom(ref(Double.class), ref(Double.TYPE)));
	}

	public void testAutoBoxingByte() throws Exception {
		assertTrue(computer.isAssignableFrom(ref(Byte.TYPE), ref(Byte.class)));
		assertTrue(computer.isAssignableFrom(ref(Byte.class), ref(Byte.TYPE)));
	}

	public void testArrayType() throws Exception {
		assertTrue(computer.isAssignableFrom(array(ref(String.class), 1), array(ref(String.class), 1)));
		assertFalse(computer.isAssignableFrom(array(ref(String.class), 1), array(ref(CharSequence.class), 1)));
		assertFalse(computer.isAssignableFrom(array(ref(String.class), 2), array(ref(String.class), 1)));
		assertFalse(computer.isAssignableFrom(array(ref(String.class), 1), array(ref(String.class), 2)));
	}

}
