package org.eclipse.xtext.example.domainmodel.tests;

import org.eclipse.xtext.example.domainmodel.DomainmodelInjectorProvider;
import org.eclipse.xtext.junit4.InjectWith;
import org.eclipse.xtext.junit4.XtextRunner;
import org.eclipselabs.xtext.utils.unittesting.XtextTest;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(XtextRunner.class)
@InjectWith(DomainmodelInjectorProvider.class)
public class ModelFileTest extends XtextTest {
	public ModelFileTest() {
		super("ModelFileTest");
	}
	
	@Test
	public void person_no_attributes(){
		testFile("person_no_attributes.dmodel");
	}
}
