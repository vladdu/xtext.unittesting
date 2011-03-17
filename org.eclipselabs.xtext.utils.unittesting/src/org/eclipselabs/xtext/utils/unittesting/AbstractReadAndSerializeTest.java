package org.eclipselabs.xtext.utils.unittesting;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.mwe.utils.StandaloneSetup;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.ISetup;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.util.EmfFormatter;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.Issue;
import org.junit.BeforeClass;

import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * Base class for testing that parsing of DSL files succeed without errors and
 * serialization of the file matches the original content.
 *
 * @author Karsten Thoms
 *
 */
public abstract class AbstractReadAndSerializeTest {
    protected Injector injector;
    protected String resourceRoot;
    @Inject
    protected ResourceSet resourceSet;
    @Inject
    private IResourceServiceProvider.Registry serviceProviderRegistry;

    @BeforeClass
    public static void init_internal() {
        StandaloneSetup setup = new StandaloneSetup();
        setup.setPlatformUri("..");
    }

    public AbstractReadAndSerializeTest(ISetup setup, String resourceRoot) {
        injector = setup.createInjectorAndDoEMFRegistration();
        injector.injectMembers(this);
    }

    protected void testFile(String fileToTest, String... referencedResources) {
        for (String referencedResource : referencedResources) {
            URI uri = URI.createURI(resourceRoot + "/" + referencedResource);
            loadModel(resourceSet, uri, getRootObjectType(uri));
        }
        String serialized = loadAndSaveModule(resourceRoot, fileToTest);
        String expected = loadFileContents(resourceRoot, fileToTest);
        // Remove trailing whitespace, see Bug#320074
        assertEquals(expected.trim(), serialized);
    }

    protected String loadFileContents(String rootPath, String filename) {
        ResourceSet rs = injector.getInstance(ResourceSet.class);
        URI uri = URI.createURI(resourceRoot + "/" + filename);
        try {
            InputStream is = rs.getURIConverter().createInputStream(uri);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            int i;
            while ((i = is.read()) >= 0) {
                bos.write(i);
            }
            is.close();
            bos.close();
            return bos.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected String loadAndSaveModule(String rootPath, String filename) {
        URI uri = URI.createURI(resourceRoot + "/" + filename);
        EObject m = loadModel(resourceSet, uri, getRootObjectType(uri));

        Resource r = resourceSet.getResource(uri, false);
        IResourceServiceProvider provider = serviceProviderRegistry
                .getResourceServiceProvider(r.getURI());
        List<Issue> result = provider.getResourceValidator().validate(r,
                CheckMode.ALL, null);

        if (!result.isEmpty()) {
            throw new ValidationFailedException(result);
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            m.eResource().save(bos, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return bos.toString();
    }

    /**
     * Returns the expected type of the root element of the given resource.
     */
    protected abstract Class<? extends EObject> getRootObjectType(URI uri);

    public void setResourceRoot(String resourceRoot) {
        this.resourceRoot = resourceRoot;
    }

    @SuppressWarnings("unchecked")
    protected <T extends EObject> T loadModel(ResourceSet rs, URI uri, Class<T> clazz) {
        Resource resource = rs.createResource(uri);
        try {
            resource.load(null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (!resource.getWarnings().isEmpty()) {
            System.err.println("Resource " + uri.toString() + " has warnings:");
            for (Resource.Diagnostic issue : resource.getWarnings()) {
                System.err.println(issue.getLine() + ": " + issue.getMessage());
            }
        }
        if (!resource.getErrors().isEmpty()) {
            System.err.println("Resource " + uri.toString() + " has errors:");
            for (Resource.Diagnostic issue : resource.getErrors()) {
                System.err.println(issue.getLine() + ": " + issue.getMessage());
            }
            fail("Resource has " + resource.getErrors().size() + " errors.");
        }

        assertFalse(resource.getContents().isEmpty());
        EObject o = resource.getContents().get(0);
        assertTrue(clazz.isInstance(o));
        EcoreUtil.resolveAll(resource);
        assertAllCrossReferencesResolvable(resource.getContents().get(0));
        return (T) o;
    }

    protected void assertAllCrossReferencesResolvable(EObject obj) {
        boolean allIsGood = true;
        TreeIterator<EObject> it = EcoreUtil2.eAll(obj);
        while (it.hasNext()) {
            EObject o = it.next();
            for (EObject cr : o.eCrossReferences())
                if (cr.eIsProxy()) {
                    allIsGood = false;
                    System.err.println("CrossReference from " + EmfFormatter.objPath(o) + " to "
                            + ((InternalEObject) cr).eProxyURI() + " not resolved.");
                }
        }
        if (!allIsGood) {
            fail("Unresolved cross references in " + EmfFormatter.objPath(obj));
        }
    }

}
