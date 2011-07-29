package org.eclipse.xtext.example.domainmodel.tests;

import org.eclipse.xtext.example.domainmodel.DomainmodelInjectorProvider;
import org.eclipse.xtext.junit4.InjectWith;
import org.eclipse.xtext.junit4.XtextRunner;
import org.eclipselabs.xtext.utils.unittesting.XtextTest;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(XtextRunner.class)
@InjectWith(DomainmodelInjectorProvider.class)
public class ParserAndLexerTest extends XtextTest {
	@Test
	public void id(){
		testTerminal("bar", /* token stream: */ "ID");
		testTerminal("bar3", /* token stream: */ "ID");
		testTerminal("_bar_", /* token stream: */ "ID");
		testTerminal("$bar$", /* token stream: */ "ID");
		
		testNotTerminal("3bar", /* unexpected */ "ID");
		testNotTerminal("#bar", /* unexpected */ "ID");
		
		// token streams with multiple token
		testTerminal("foo.bar", "ID", "'.'", "ID");
		testTerminal("foo.*", "ID", "'.'", "'*'");
	}
	
	@Test
	public void qualifiedName(){
		testParserRule("foo.bar", "QualifiedName");
		testParserRuleErrors("3foo.bar", "QualifiedName", "extraneous input '3'");
	}
	
	@Test
	public void qualifiedNameWithWildcard(){
		testParserRule("foo.*", "QualifiedNameWithWildCard");
	}
}