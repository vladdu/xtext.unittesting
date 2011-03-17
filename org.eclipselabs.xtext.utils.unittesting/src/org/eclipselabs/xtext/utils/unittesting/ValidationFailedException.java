package org.eclipselabs.xtext.utils.unittesting;

import java.util.List;

import org.eclipse.xtext.validation.Issue;

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
