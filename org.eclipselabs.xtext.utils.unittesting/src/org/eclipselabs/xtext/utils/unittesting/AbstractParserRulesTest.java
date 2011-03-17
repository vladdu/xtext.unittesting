package org.eclipselabs.xtext.utils.unittesting;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.mwe.utils.StandaloneSetup;
import org.eclipse.xtext.parser.IParseResult;
import org.eclipse.xtext.parser.antlr.IAntlrParser;
import org.eclipse.xtext.parsetree.SyntaxError;
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
    private IAntlrParser parser;

    @BeforeClass
    public static void init() {
        StandaloneSetup setup = new StandaloneSetup();
        setup.setPlatformUri("..");
    }

    protected void testParserRule(String ruleName, String textToParse) {
        testParserRule(ruleName, textToParse, false);
    }

    protected List<SyntaxError> testParserRule(String ruleName,
            String textToParse, boolean errorsExpected) {
        IParseResult result = parser.parse(ruleName, new StringReader(
                textToParse));
        ArrayList<String> errMsg = Lists.newArrayList();
        for (SyntaxError err : result.getParseErrors()) {
            errMsg.add(err.getMessage());
        }
        if (!errorsExpected && !result.getParseErrors().isEmpty()) {
            fail("Parsing of text '" + textToParse + "' for rule '" + ruleName
                    + "' failed with errors: " + errMsg);
        }
        if (errorsExpected && result.getParseErrors().isEmpty()) {
            fail("Parsing of text '" + textToParse + "' for rule '" + ruleName
                    + "' was expected to have parse errors.");
        }

        return result.getParseErrors();
    }

    protected void testParserRule(String ruleName, String textToParse,
            String... expectedErrors) {
        List<SyntaxError> errors = testParserRule(ruleName, textToParse, true);
        List<String> expectedErrorMessages = Lists.newArrayList(expectedErrors);

        assertEquals("Number of errors", expectedErrors.length, errors.size());

        for (final SyntaxError err : errors) {
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
