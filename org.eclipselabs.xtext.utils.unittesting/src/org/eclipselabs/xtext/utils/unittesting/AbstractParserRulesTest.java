package org.eclipselabs.xtext.utils.unittesting;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.mwe.utils.StandaloneSetup;
import org.eclipse.xtext.ParserRule;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.SyntaxErrorMessage;
import org.eclipse.xtext.parser.IParseResult;
import org.eclipse.xtext.parser.IParser;
import org.junit.BeforeClass;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

/**
 * Base class for testing specific parser rules.
 *
 * @author Karsten Thoms (karsten.thoms@itemis.de)
 */
public class AbstractParserRulesTest {
    @Inject
    private IParser parser;

    @BeforeClass
    public static void init() {
        StandaloneSetup setup = new StandaloneSetup();
        setup.setPlatformUri("..");
    }

    protected void testParserRule(String ruleName, String textToParse) {
        testParserRule(ruleName, textToParse, false);
    }

    protected List<SyntaxErrorMessage> testParserRule(String ruleName,
            String textToParse, boolean errorsExpected) {
    	
    	// TODO: Find Parser Rule by ruleName
    	ParserRule parserRule = null;
        IParseResult result = parser.parse(parserRule, new StringReader(
                textToParse));
        
        ArrayList<SyntaxErrorMessage> errors = Lists.newArrayList();
        ArrayList<String> errMsg = Lists.newArrayList();
        
        for (INode err : result.getSyntaxErrors()) {
        	errors.add(err.getSyntaxErrorMessage());
        	errMsg.add(err.getSyntaxErrorMessage().getMessage());
        }
        
        if (!errorsExpected && !errors.isEmpty()) {
            fail("Parsing of text '" + textToParse + "' for rule '" + ruleName
                    + "' failed with errors: " + errMsg);
        }
        if (errorsExpected && errors.isEmpty()) {
            fail("Parsing of text '" + textToParse + "' for rule '" + ruleName
                    + "' was expected to have parse errors.");
        }

        return errors;
    }

    protected void testParserRule(String ruleName, String textToParse,
            String... expectedErrors) {
        List<SyntaxErrorMessage> errors = testParserRule(ruleName, textToParse, true);
        List<String> expectedErrorMessages = Lists.newArrayList(expectedErrors);

        assertEquals("Number of errors", expectedErrors.length, errors.size());

        for (final SyntaxErrorMessage err : errors) {
            if (!Iterables.any(expectedErrorMessages, new Predicate<String>() {
                public boolean apply(String input) {
                    return err.getMessage().contains(input);
                }
            })) {
                fail("Unexpected error message: " + err.getMessage());
            }
        }
    }

}
