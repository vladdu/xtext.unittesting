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
		/* this will fail, if any of those occur  
		 * 
		 *  - parsing errros
		 *  - validationerrors/warnings
		 *  - the serializer fails
		 *  - the serialized and formatted result 
		 *    doesn't exactly match the input files' 
		 *    content
		 *    
		 * */
	}
	
	@Test
	public void person2_extends_person(){
		testFile("person2_extends_person.dmodel", /* not tested, but indexed */ "person_no_attributes.dmodel");
	}
}