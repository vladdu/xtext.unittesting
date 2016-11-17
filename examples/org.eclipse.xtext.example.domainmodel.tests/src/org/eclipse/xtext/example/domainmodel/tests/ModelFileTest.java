package org.eclipse.xtext.example.domainmodel.tests;

import org.eclipse.xtext.example.domainmodel.DomainmodelInjectorProvider;
import org.eclipse.xtext.example.domainmodel.validation.IssueCodes;
import org.eclipse.xtext.junit4.InjectWith;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.itemis.xtext.testing.XtextRunner2;
import com.itemis.xtext.testing.XtextTest;

@RunWith(XtextRunner2.class)
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

  @Test
  public void test_withCode(){
    testFile("person_invalid_typename.dmodel");
    assertConstraints(issues.withCode(IssueCodes.INVALID_TYPE_NAME).sizeIs(1));
  }
}