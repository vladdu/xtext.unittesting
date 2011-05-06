package org.eclipselabs.xtext.utils.unittesting;

import java.util.List;

import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.validation.Issue;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class ValidationFailedException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private List<Issue> issues;
	public ValidationFailedException (List<Issue> issues) {
		super(issues.toString());
		this.issues = issues;
	}
	public List<Issue> getIssues() {
		return issues;
	}
}
