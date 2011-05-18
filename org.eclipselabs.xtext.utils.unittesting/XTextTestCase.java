package de.itemis.xtext.typesystem.testing;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.ISetup;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.scoping.IScopeProvider;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.Issue;

import com.google.inject.Inject;

public abstract class XTextTestCase {

	protected XtextResource resource;
	protected IssueCollection allIssues;
	protected Set<Issue> assertedIssues = new HashSet<Issue>();
	
	@Inject
	protected IScopeProvider scoper;

	protected EObject root;

	private static Logger LOGGER = Logger.getLogger(XTextTestCase.class);
	
	public EObject initializeAndGetRoot(ISetup setup, String primaryFilename, String ... supportingFilenames) throws Exception {
		if ( root == null ) {
			LOGGER.info("");
			LOGGER.info("running test "+this.getClass().getName() + "." + new Throwable().fillInStackTrace().getStackTrace()[1].getMethodName());
			LOGGER.info("------------------------------------------");
			setup.createInjectorAndDoEMFRegistration();
			ResourceSet rs = new ResourceSetImpl();
			for (String supportFilename : supportingFilenames) {
				rs.getResource(URI.createURI(supportFilename), true);
			}
			resource = (XtextResource) rs.getResource(URI.createURI(primaryFilename), true);
			allIssues = new IssueCollection(resource, resource.getResourceServiceProvider().getResourceValidator().validate(resource, CheckMode.ALL, null), new ArrayList<String>());
			root = resource.getContents().get(0);
		}
		return root;
	}
	
	protected void resetAssertedIssues() {
		assertedIssues.clear();
	}
	
	protected void assertConstraints( IssueCollection coll, String msg ) {
		assertedIssues.addAll(coll.getIssues());
		Assert.assertTrue("failed "+msg+coll.getMessageString(), coll.evaluate() );
	}
	
	protected void assertConstraints( IssueCollection coll) {
		assertedIssues.addAll(coll.getIssues());
		Assert.assertTrue("<no id> failed"+coll.getMessageString(), coll.evaluate() );
	}
	
	protected void assertConstraints( String constraintID, IssueCollection coll) {
		assertedIssues.addAll(coll.getIssues());
		Assert.assertTrue(constraintID+" failed"+coll.getMessageString(), coll.evaluate() );
	}
	
	public EObject getEObject( URI uri ) {
		EObject eObject = resource.getEObject(uri.fragment());
		if ( eObject.eIsProxy()) {
			eObject = EcoreUtil.resolve(eObject, resource);
		}
		return eObject;
	}
	
	public List<Issue> getUnassertedIssues() {
		List<Issue> res = new ArrayList<Issue>();
		for (Issue issue : allIssues.getIssues()) {
			if ( !assertedIssues.contains(issue) ) {
				res.add(issue);
			}
		}
		return res;
	}
	
	public void dumpUnassertedIssues() {
		if ( getUnassertedIssues().size() > 0 ) {
			LOGGER.debug("---- Unasserted Issues ----");
			for (Issue issue: getUnassertedIssues()) {
				IssueCollection.dumpIssue( resource, issue );
			}
		} else {
			LOGGER.debug("no unasserted issues (good!)");
		}
	}


}
