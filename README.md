# Introduction

For all non trivial DSLs it is important to develop them test driven, otherwise you won't be able to foresee all the side effects of grammar or implementation changes.

In the examples below we will use the Domainmodel Example for testing. This example can be installed with the project wizard.

## Installation
not available yet from here

## Testing DSL files

In real world projects it has proven to be helpful to have a set of simple models that show certain aspects of your DSL and then just try to read the file.

xtext.unittesting provides the base class XtextTest with which you can

* unit-test parser-rules and terminals
* integration-test DSL files including
* parse into EMF model
* resolve cross refs
* assert custom validations
* serialize emf to text
* compare serialized to input model (assuming input-file is correctly formatted)

## Usage
By default, XtextTest searches for models on the classpath. So just put it either beside your unit-test in the java-folder or create a folder added as resource folder where you put your test-models. Consider adding a folder named like the test-class wich then contains all tested models.

Add a dependency to plugin `com.itemis.xtext.testing` in `MANIFEST.MF`.
Derive your test class from `com.itemis.xtext.testing.XtextTest`.

Add `@RunWith(XtextRunner.class)` as class annotation
Add `@InjectWith(<MyDsl>InjectorProvider.class)` as class annotation. Xtext will generate an implementation of `IInjectorProvider` for your DSL.

```java
@RunWith(XtextRunner2.class)
@InjectWith(DomainmodelInjectorProvider.class)
public class ModelFileTest extends XtextTest {

   // your tests go here

}
```

### integration-style model testing
The most basic thing you can do is parse a model file and assure it is both valid when read and correctly serialized. Invoke the testFile method with a path to your model file (relative to classpath root).

We use the domainmodel example shipped with xtext to demonstrate the capabilities of the test framework. It is also checked in under [https://github.com/itemis/xtext-testing/tree/master/examples]().

Model file [`resources/ModelFileTest/person_no_attributes.dmodel`](https://raw.githubusercontent.com/itemis/xtext-testing/master/examples/org.eclipse.xtext.example.domainmodel.tests/resources/ModelFileTest/person_no_attributes.dmodel):

```
entity Person { }
```

Test class [`ModelFileTest`](https://github.com/itemis/xtext-testing/blob/master/examples/org.eclipse.xtext.example.domainmodel.tests/src/org/eclipse/xtext/example/domainmodel/tests/ModelFileTest.java):

```java
@RunWith(XtextRunner2.class)
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
```

This test method will fail when

* The model has syntax errors
* The model has cross references to external model files (which are not loaded in this example)
* Scoping does not work (which is part of cross-ref resolving)
* The model produces any warnings or errors
* The serialized/formatted form does not match the input
* Cross reference resolving

If your model has cross references to model elements from external model elements you, these model files must be passed as additional arguments for the testFile() method.

Consider this example [`person2_extends_person.dmodel`](https://raw.githubusercontent.com/itemis/xtext-testing/master/examples/org.eclipse.xtext.example.domainmodel.tests/resources/ModelFileTest/person2_extends_person.dmodel):

```
entity Person2 extends Person { }
```

To successfully parse `testcase02.dmodel` the reference to the supertype `Person` must be resolved, which is defined in `testcase01.dmodel`.

The test method looks like this:

```java
@Test
public void person2_extends_person(){
        testFile("person2_extends_person.dmodel", "person_no_attributes.dmodel");
}
```

### Fluent validation assertions
To test validation rules example models that violate constraints are required as input models. A fluent API allows to define the exact asserted constraint violation:

```java
  testFile(".....");
// error line 1: person.name-feature is missing display name
assertConstraints(
  issues.errorsOnly()
        .inLine(1)
        .under(Modul.class, "person")
        .named("name")
        .oneOfThemContains("missing display name")
);
```

### Unit-style grammar testing
In order to test this grammar snippet:

```
// from Xbase.xtext
QualifiedName:
  ValidID (=>'.' ValidID)*;

// from Xtype.xtext
terminal ID:
        '^'? ('a'..'z'|'A'..'Z'|'$'|'_') ('a'..'z'|'A'..'Z'|'$'|'_'|'0'..'9')*;

// from Domainmodel.xtext
QualifiedNameWithWildCard :
  QualifiedName  ('.' '*')?;
```

You might want to run these tests; just a first draft for showing how it works:

```java
@RunWith(XtextRunner2.class)
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
```

## Disabling behavior
Sometimes you may need to ignore problems or disable some of the tested features, like formatting or serializing. This can be done by calling the following methods:

  * `ignoreFormattingDifferences()`: Does not fail the test when the serialized file does not match the expected file
  * `ignoreParserWarnings()`: Does not fail the test if there are parser warnings
  * `ignoreSerializationDifferences()`
  * `ignoreUnassertedWarnings()`
  * `suppressSerialization()`: Does not invoke the serializer at all
  * `ignoreOsSpecificNewline()`: Text file comparison will ignore OS specific newlines by harmonizing expected and serialized text with Unix style newline.

The appropriate place will be most likely a @Before annotated method, but sometimes in a test method before invoking `testFile()`.

## Troubleshooting

**Error message "Content is not allowed in prolog."**

Xtext 2.0.1 introduced a bug that leads to deregistration of the resource factory after the first test.
```
java.lang.RuntimeException: org.eclipse.emf.ecore.resource.Resource$IOWrappedException: Content is not allowed in prolog.
```

Solution: Use `@RunWith(XtextRunner2.class)` (instead of `XtextRunner.class`). Version 0.1.0 or above is required to have this class. 

**Model files under test not found:**

The model files must be available on the project's classpath. The framework tries to load the files with a classpath URI. Usually it is a good idea to create a Java source folder "model" into which you put the model files under test.

## More Information

Blog Post: ["Eclipse Labs Xtext Utils: Unit Testing helpers released"](https://startbigthinksmall.wordpress.com/2011/08/01/eclipse-labs-xtext-utils-unit-testing-helpers-released/)
