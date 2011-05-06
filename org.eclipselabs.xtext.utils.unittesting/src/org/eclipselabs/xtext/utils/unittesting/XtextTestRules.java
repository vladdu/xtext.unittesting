package org.eclipselabs.xtext.utils.unittesting;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.matchers.JUnitMatchers.both;
import static org.junit.matchers.JUnitMatchers.containsString;

import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.validation.Issue;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.Assert;
import org.junit.internal.matchers.TypeSafeMatcher;
import org.junit.rules.ExpectedException;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * 
 * @author Karsten Thoms
 * @author Lars Corneliussen
 */
public class XtextTestRules implements MethodRule {
	
	/**
	 * @return a Rule that expects no exception to be thrown
	 * (identical to behavior without this Rule)
	 */
	public static XtextTestRules none() {
		XtextTestRules result = new XtextTestRules();
		return result;
	}

	private Matcher<Object> fMatcher= null;

	protected XtextTestRules() {
	}
	
	public Statement apply(Statement base, FrameworkMethod method, Object target) {
		return new ExpectedExceptionStatement(base);
	}

	/**
	 * Adds {@code matcher} to the list of requirements for any thrown exception.
	 */
	// Should be able to remove this suppression in some brave new hamcrest world.
	@SuppressWarnings("unchecked")
	public void expect(Matcher<?> matcher) {
		if (fMatcher == null)
			fMatcher= (Matcher<Object>) matcher;
		else
			fMatcher= both(fMatcher).and(matcher);
	}

	protected class ExpectedExceptionStatement extends Statement {
		private final Statement fNext;

		public ExpectedExceptionStatement(Statement base) {
			fNext= base;
		}

		@Override
		public void evaluate() throws Throwable {
			try {
				fNext.evaluate();
			} catch (Throwable e) {
				if (fMatcher == null)
					throw e;
				Assert.assertThat(e, fMatcher);
				return;
			}
			if (fMatcher != null)
				throw new AssertionError("Expected test to throw "
						+ StringDescription.toString(fMatcher));
		}
	}
	
	/**
	 * Parsed successfully, and no validation errors occurred. May contain warnings.
	 */
	public void expectNoErrors () {
		expect(new TypeSafeMatcher<ValidationFailedException>() {
			public void describeTo(Description description) {
				description.appendText("exception no errors");
			}
		
			@Override
			public boolean matchesSafely(ValidationFailedException item) {
				return Iterables.all(item.getIssues(), new Predicate<Issue>() {
					public boolean apply(Issue issue) {
						return issue.getSeverity()!=Severity.ERROR;
					}
				});
			}
		});
	}
	
	boolean _validationExceptionExpectedRuleAdded = false;
	
	private void expectValidationException(){
		if (!_validationExceptionExpectedRuleAdded) {
			expect(instanceOf(ValidationFailedException.class));
		}
	}
	
	/**
	 * Parsed successfully, but validation error occurs containing <code>substring</code>.
	 */
	public void expectError (String substring) {
		expectValidationException();
		
		final Matcher<String> substringMatcher = containsString(substring);
		
		expect(new TypeSafeMatcher<ValidationFailedException>() {
			public void describeTo(Description description) {
				description.appendText("expected validation error: ");
				description.appendDescriptionOf(substringMatcher);
			}
		
			@Override
			public boolean matchesSafely(ValidationFailedException item) {
				return Iterables.any(item.getIssues(), new Predicate<Issue>() {
					public boolean apply(Issue issue) {
						if (issue.getSeverity() != Severity.ERROR)
							return false;
						
						return substringMatcher.matches(issue.getMessage());
					}
				});
			}
		});
	}
	
	/**
	 * Parsed successfully, but validation warning occurs containing <code>substring</code>.
	 */
	public void expectWarning (String substring) {
		expectValidationException();
		
		final Matcher<String> substringMatcher = containsString(substring);
		
		expect(new TypeSafeMatcher<ValidationFailedException>() {
			public void describeTo(Description description) {
				description.appendText("expected validation warning: ");
				description.appendDescriptionOf(substringMatcher);
			}
		
			@Override
			public boolean matchesSafely(ValidationFailedException item) {
				return Iterables.any(item.getIssues(), new Predicate<Issue>() {
					public boolean apply(Issue issue) {
						if (issue.getSeverity() != Severity.WARNING)
							return false;
						
						return substringMatcher.matches(issue.getMessage());
					}
				});
			}
		});
	}
}
