/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyc.baseclient.datatype;

/*
 * #%L
 * File: CycStringUtilsTest.java
 * Project: Base Client
 * %%
 * Copyright (C) 2013 - 2018 Cycorp, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.cyc.base.exception.CycApiException;
import com.cyc.baseclient.CommonConstants;
import com.cyc.baseclient.cycobject.NautImpl;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.cyc.baseclient.datatype.CycStringUtils.DISABLE_WORD_WRAP;
import static com.cyc.baseclient.testing.TestUtils.skipTest;
import static org.apache.commons.lang3.StringUtils.repeat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;



/**
 *
 * @author baxter
 */
public class CycStringUtilsTest {

  public CycStringUtilsTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  //====|    Tests    |===========================================================================//
  
  /** Tests the CycStringUtils.change method. */
  public void testChange() {
    System.out.println("** testChange **");
    assertEquals("", CycStringUtils.change("", "", ""));
    assertEquals("a", CycStringUtils.change("a", "b", "c"));
    assertEquals("z", CycStringUtils.change("a", "a", "z"));
    assertEquals("xyz", CycStringUtils.change("abc", "abc", "xyz"));
    assertEquals("zbc", CycStringUtils.change("abc", "a", "z"));
    assertEquals("azc", CycStringUtils.change("abc", "b", "z"));
    assertEquals("abz", CycStringUtils.change("abc", "c", "z"));
    assertEquals("", CycStringUtils.change("abc", "abc", ""));
    assertEquals("a123c", CycStringUtils.change("abc", "b", "123"));
    assertEquals("123bc", CycStringUtils.change("abc", "a", "123"));
    assertEquals("ab123", CycStringUtils.change("abc", "c", "123"));
    final StringBuffer stringBuffer = new StringBuffer(100);
    stringBuffer.append("abc");
    stringBuffer.append('\n');
    stringBuffer.append("def");
    assertEquals("abc\\ndef", CycStringUtils.change(stringBuffer.toString(), "\n",
            "\\n"));
    System.out.println("** testChange OK **");
  }

  /**
   * Tests the CycStringUtils.removeDelimiters method.
   */
  @Test
  public void testRemoveDelimiters() {
    System.out.println("** testRemoveDelimiters **");
    assertEquals("abc", CycStringUtils.removeDelimiters("\"abc\""));
    System.out.println("** testRemoveDelimiters OK**");
  }

  /**
   * Tests the CycStringUtils.isDelimitedString method.
   */
  @Test
  public void testIsDelimitedString() {
    System.out.println("** testIsDelimitedString **");
    assertTrue(CycStringUtils.isDelimitedString("\"abc\""));
    assertTrue(CycStringUtils.isDelimitedString("\"\""));
    assertTrue(!CycStringUtils.isDelimitedString("\""));
    assertTrue(!CycStringUtils.isDelimitedString(1));
    assertTrue(!CycStringUtils.isDelimitedString("abc\""));
    assertTrue(!CycStringUtils.isDelimitedString("\"abc"));
    System.out.println("** testIsDelimitedString OK **");
  }

  /**
   * Tests the CycStringUtils.isNumeric method.
   */
  @Test
  public void testIsNumeric() {
    System.out.println("** testIsNumeric **");
    assertTrue(CycStringUtils.isNumeric("0"));
    assertTrue(CycStringUtils.isNumeric("1"));
    assertTrue(CycStringUtils.isNumeric("2"));
    assertTrue(CycStringUtils.isNumeric("3"));
    assertTrue(CycStringUtils.isNumeric("4"));
    assertTrue(CycStringUtils.isNumeric("5"));
    assertTrue(CycStringUtils.isNumeric("6"));
    assertTrue(CycStringUtils.isNumeric("7"));
    assertTrue(CycStringUtils.isNumeric("8"));
    assertTrue(CycStringUtils.isNumeric("9"));
    assertTrue(!CycStringUtils.isNumeric("A"));
    assertTrue(!CycStringUtils.isNumeric("@"));
    assertTrue(!CycStringUtils.isNumeric("."));
    assertTrue(CycStringUtils.isNumeric("12345"));
    assertTrue(!CycStringUtils.isNumeric("123.45"));
    assertTrue(!CycStringUtils.isNumeric("123-45"));
    assertTrue(!CycStringUtils.isNumeric("12345+"));
    assertTrue(!CycStringUtils.isNumeric("+"));
    assertTrue(!CycStringUtils.isNumeric("-"));
    assertTrue(CycStringUtils.isNumeric("+1"));
    assertTrue(CycStringUtils.isNumeric("-1"));
    assertTrue(CycStringUtils.isNumeric("+12345"));
    assertTrue(CycStringUtils.isNumeric("-12345"));
    System.out.println("** testIsNumeric OK **");
  }

  /**
   * Tests the CycStringUtils.wordsToString method.
   */
  @Test
  public void testWordsToString() {
    System.out.println("** testWordsToString **");
    ArrayList words = new ArrayList();
    assertEquals("", CycStringUtils.wordsToPhrase(words));
    words.add("word1");
    assertEquals("word1", CycStringUtils.wordsToPhrase(words));
    words.add("word2");
    assertEquals("word1 word2", CycStringUtils.wordsToPhrase(words));
    words.add("word3");
    assertEquals("word1 word2 word3", CycStringUtils.wordsToPhrase(words));

    System.out.println("** testWordsToString OK **");
  }

  /**
   * Tests the CycStringUtils.escapeDoubleQuotes method.
   */
  @Test
  public void testEscapeDoubleQuotes() {
    System.out.println("** testEscapeDoubleQuotes **");
    String string = "";
    assertEquals(string, CycStringUtils.escapeDoubleQuotes(string));
    string = "1 2 3";
    assertEquals(string, CycStringUtils.escapeDoubleQuotes(string));
    string = "'1' '2' '3'";
    assertEquals(string, CycStringUtils.escapeDoubleQuotes(string));
    StringBuilder sb = new StringBuilder();
    sb.append("\"");
    sb.append("abc");
    sb.append("\"");
    string = sb.toString();
    String expectedString = "\\\"abc\\\"";
    String escapedString = CycStringUtils.escapeDoubleQuotes(string);
    assertEquals(expectedString, escapedString);

    String str = "\\s \"werj 9234 \\3";
    String expResult = "\\\\s \\\"werj 9234 \\\\3";
    String result = CycStringUtils.escapeDoubleQuotes(str);
    assertEquals(expResult, result);
    
    System.out.println("** testEscapeDoubleQuotes OK **");
  }
  
  
  /**
   * Tests the OcCollectionUtils.hasDuplicates method.
   */
  @Test
  public void testIs7BitASCII() {
    System.out.println("** testIs7BitASCII **");
    assertTrue(CycStringUtils.is7BitASCII("abc"));
    StringBuilder sb = new StringBuilder();
    sb.append('a');
    sb.append((char) 140);
    assertTrue(!CycStringUtils.is7BitASCII(sb.toString()));
    sb.append('c');
    assertTrue(!CycStringUtils.is7BitASCII(sb.toString()));
    System.out.println("** testIs7BitASCII OK **");
  }

  @Test
  public void testUnicodeEscaped() {
    System.out.println("** testUnicodeEscaped **");
    testUnicodeEscaped("abc", "abc");
    testUnicodeEscaped("ab\"c", "ab\\\"c");
    testUnicodeEscaped("ab\\c", "ab\\\\c");
    StringBuffer sb = new StringBuffer();
    sb.append((char) 0xff00);
    String testString = sb.toString();
    //System.out.println(testString);
    testUnicodeEscaped(testString, "&uff00;");
    sb = new StringBuffer();
    sb.append((char) 0xff00);
    sb.append(';');
    testString = sb.toString();
    //System.out.println(testString);
    testUnicodeEscaped(testString, "&uff00;;");
    sb = new StringBuffer();
    sb.append((char) 0xff00);
    sb.append(';');
    sb.append('a');
    testString = sb.toString();
    //System.out.println(testString);
    testUnicodeEscaped(testString, "&uff00;;a");
    System.out.println("** testUnicodeEscaped OK **");
  }

  private void testUnicodeEscaped(final String input, final String output) {
    assertTrue(CycStringUtils.unicodeEscaped(input).equalsIgnoreCase(
            "(list " + CommonConstants.UNICODE_STRING_FN.stringApiValue() + " \"" + output + "\")"));
  }

  /** Test isWhitespace. */
  @Test
  public void testIsWhitespace() {
    System.out.println("** testIsWhitespace **");
    String string = "abc";
    assertTrue(!CycStringUtils.isWhitespace(string));
    string = " abc ";
    assertTrue(!CycStringUtils.isWhitespace(string));
    string = "";
    assertTrue(!CycStringUtils.isWhitespace(string));
    string = " ";
    assertTrue(CycStringUtils.isWhitespace(string));
    string = " \n\r\t  ";
    assertTrue(CycStringUtils.isWhitespace(string));
    System.out.println("** testIsWhitespace OK **");
  }

  /** Test stripLeading. */
  @Test
  public void testStripLeading() {
    System.out.println("** testStripLeading **");
    String string = "abc";
    assertEquals("abc", CycStringUtils.stripLeading(string, ' '));
    string = "";
    assertEquals("", CycStringUtils.stripLeading(string, ' '));
    string = " abc ";
    assertEquals("abc ", CycStringUtils.stripLeading(string, ' '));
    string = "zzzzzzzzabc ";
    assertEquals("abc ", CycStringUtils.stripLeading(string, 'z'));
    string = "\n";
    assertEquals("", CycStringUtils.stripLeading(string, '\n'));
    System.out.println("** testStripLeading OK **");
  }

  /** Test stripTrailing. */
  @Test
  public void testStripTrailing() {
    System.out.println("** testStripTrailing **");
    String string = "abc";
    assertEquals("abc", CycStringUtils.stripTrailing(string, ' '));
    string = "";
    assertEquals("", CycStringUtils.stripTrailing(string, ' '));
    string = " abc ";
    assertEquals(" abc", CycStringUtils.stripTrailing(string, ' '));
    string = " abczzzzzzzz";
    assertEquals(" abc", CycStringUtils.stripTrailing(string, 'z'));
    System.out.println("** testStripTrailing OK **");
  }

  /** Test stripTrailingBlanks. */
  @Test
  public void testStripTrailingBlanks() {
    System.out.println("** testStripTrailingBlanks **");
    String string = "abc";
    assertEquals("abc", CycStringUtils.stripTrailingBlanks(string));
    string = "";
    assertEquals("", CycStringUtils.stripTrailingBlanks(string));
    string = " abc ";
    assertEquals(" abc", CycStringUtils.stripTrailingBlanks(string));
    string = " abc     ";
    assertEquals(" abc", CycStringUtils.stripTrailingBlanks(string));
    System.out.println("** testStripTrailingBlanks OK **");
  }
  
  /**
   * Test of cyclStringToJavaString method, of class CycStringUtils.
   */
  @Test
  public void testCyclStringToJavaString() {
    System.out.println("cyclStringToJavaString");
    Object cyclString = "test";
    String expResult = "test";
    String result = CycStringUtils.cyclStringToJavaString(cyclString);
    assertEquals(expResult, result);

    cyclString = new NautImpl(CommonConstants.UNICODE_STRING_FN, "Brade&u161;ko");
    expResult = NonAsciiStrings.get("bradeshko");
    result = CycStringUtils.cyclStringToJavaString(cyclString);
    assertEquals(expResult, result);
  }

  /**
   * Test of unicodeEscaped method, of class CycStringUtils.
   */
  @Test
  public void testUnicodeEscaped_String() {
    StringBuilder ex_ascii = new StringBuilder("abc&289;");
    ex_ascii.append((char) 0xac);
    System.out.println("Original: " + ex_ascii.toString() + "|");
    System.out.println("Sublisp Unicode escaped: |" + CycStringUtils.unicodeEscaped(ex_ascii.toString()) + "|");
  }

  /**
   * Test of unicodeEscaped method, of class CycStringUtils.
   */
  @Test
  public void testUnicodeEscaped_String_boolean() {
  }

  /**
   * Test of deEscapeHTMLescapedString method, of class CycStringUtils.
   */
  @Test
  public void testDeEscapeHTMLescapedString() {
    final String tests[] = {"simple", "&amp;", "abc&a", "&#64", "&#64;&#20;", "sadf&whatever;sdfsd", "", "&u64;"};
    for (int i = 0; i < tests.length; i++) {
      try {
        System.out.println("DeEscape: org: |" + tests[i] + "| deEscaped: |" + CycStringUtils.deEscapeHTMLescapedString(tests[i]) + "|.");
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
  
  private Exception getMockException() {
    final Exception ex1 = new NullPointerException("And finally, some random NullPointerException.");
    final Exception ex2 = new SQLException(
                            "And here's a random SQL exception...\n"
                            + "... whose message spans...\n"
                            + "(some really very extremely and deliberately excessively over-verbose and ridiculously long lines which just keep going and going and going for...)\n"
                            + "... multiple lines!",
                            ex1);
    final Exception ex3 = new UnsupportedOperationException("Which wraps an UnsupportedOperationException",
                    ex2);
    final Exception ex4 = new CycApiException(
            "Important CycApiException: Here's a Cyc API exception...\n"
            + "which spans multiple lines",
            ex3);
    return ex4;
  }
  
  private static final String CLASS_CANONICAL_NAME = CycStringUtilsTest.class.getCanonicalName();
  private static final String CLASS_NAME = CycStringUtilsTest.class.getSimpleName();
  private static final int EX1_LINE = 360;
  private static final int EX2_LINE = 361;
  private static final int EX3_LINE = 367;
  private static final int EX4_LINE = 369;
  
  @Test
  public void testToPrettyMessage_defaults() {
    skipTest(this, "", "Skip until handling of test results is improved."); // TODO: improve handling of test results - nwinant, 2017-12-18
    System.out.println("testToPrettyMessage_defaults");
    final List<String> expected = Arrays.asList(
            "> CycApiException: " + CLASS_CANONICAL_NAME + ".getMockException(" + CLASS_NAME + ".java:" + EX4_LINE + ")",
            "  Important CycApiException: Here's a Cyc API exception...",
            "  which spans multiple lines"
    );
    final Exception ex = getMockException();
    final List<String> results = CycStringUtils.toPrettyMessage(ex);
    System.out.println("Results:");
    System.out.println("----------");
    CycStringUtils.printlns(results);
    System.out.println("----------");
    assertEquals(expected, results);
  }
  
  @Test
  public void testToPrettyMessage_firstIndent_restIndent() {
    System.out.println("testToPrettyMessage_firstIndent_restIndent");
    final List<String> expected = Arrays.asList(
            "+-CycApiException:",
            "|-Important CycApiException: Here's a Cyc API exception...",
            "|-which spans multiple lines"
    );
    final Exception ex = getMockException();
    final List<String> results = CycStringUtils
            .toPrettyMessage(ex, "+-", "|-", DISABLE_WORD_WRAP, true, false);
    System.out.println("Results:");
    System.out.println("----------");
    CycStringUtils.printlns(results);
    System.out.println("----------");
    assertEquals(expected, results);
  }
  
  @Test
  public void testToPrettyMessages_defaults() {
    skipTest(this, "", "Skip until handling of test results is improved."); // TODO: improve handling of test results - nwinant, 2017-12-18
    System.out.println("testToPrettyMessages_defaults");
    final List<String> expected = Arrays.asList(
            "> CycApiException: " + CLASS_CANONICAL_NAME + ".getMockException(" + CLASS_NAME + ".java:" + EX4_LINE + ")",
            "  Important CycApiException: Here's a Cyc API exception...",
            "  which spans multiple lines",
            "  > UnsupportedOperationException: " + CLASS_CANONICAL_NAME + ".getMockException(" + CLASS_NAME + ".java:" + EX3_LINE + ")",
            "    Which wraps an UnsupportedOperationException",
            "    > SQLException: " + CLASS_CANONICAL_NAME + ".getMockException(" + CLASS_NAME + ".java:" + EX2_LINE + ")",
            "      And here's a random SQL exception...",
            "      ... whose message spans...",
            "      (some really very extremely and deliberately excessively over-verbose and ridiculously long lines which just keep going and going and going for...)",
            "      ... multiple lines!",
            "      > NullPointerException: " + CLASS_CANONICAL_NAME + ".getMockException(" + CLASS_NAME + ".java:" + EX1_LINE + ")",
            "        And finally, some random NullPointerException."
    );
    final Exception ex = getMockException();
    final List<String> results = CycStringUtils.toPrettyMessages(ex);
    System.out.println("Results:");
    System.out.println("----------");
    results.forEach(System.out::println);
    System.out.println("----------");
    assertEquals(expected, results);
  }
  
  @Test
  public void testToPrettyMessages_noWrapLength_firstIndent_restIndent() {
    System.out.println("testToPrettyMessages_noWrapLength_wrappedLineAdditionalIndent");
    final List<String> expected = Arrays.asList(
            "+-CycApiException:",
            "|---Important CycApiException: Here's a Cyc API exception...",
            "|---which spans multiple lines",
            "  +-UnsupportedOperationException:",
            "  |---Which wraps an UnsupportedOperationException",
            "    +-SQLException:",
            "    |---And here's a random SQL exception...",
            "    |---... whose message spans...",
            "    |---(some really very extremely and deliberately excessively over-verbose and ridiculously long lines which just keep going and going and going for...)",
            "    |---... multiple lines!",
            "      +-NullPointerException:",
            "      |---And finally, some random NullPointerException."
    );
    final Exception ex = getMockException();
    final List<String> results = CycStringUtils
            .toPrettyMessages(ex, "+-", "|---", DISABLE_WORD_WRAP, true, false);
    System.out.println("Results:");
    System.out.println("----------");
    results.forEach(System.out::println);
    System.out.println("----------");
    assertEquals(expected, results);
  }
  
  @Test
  public void testToPrettyMessages_wrapLength50() {
    System.out.println("testToPrettyMessages_wrapLength50");
    final List<String> expected = Arrays.asList(
            "> Important CycApiException: Here's a Cyc API",
            "  exception...",
            "  which spans multiple lines",
            "  > Which wraps an UnsupportedOperationException",
            "    > And here's a random SQL exception...",
            "      ... whose message spans...",
            "      (some really very extremely and deliberately",
            "      excessively over-verbose and ridiculously",
            "      long lines which just keep going and going",
            "      and going for...)",
            "      ... multiple lines!",
            "      > And finally, some random",
            "        NullPointerException."
    );
    final Exception ex = getMockException();
    final int wrapLength = 50;
    final List<String> results = CycStringUtils.toPrettyMessages(ex, wrapLength, false, false);
    System.out.println("Results (" + wrapLength + "):");
    System.out.println(repeat('-', wrapLength));
    results.forEach(System.out::println);
    System.out.println(repeat('-', wrapLength));
    assertEquals(expected, results);
  }
  
  @Test
  public void testToPrettyMessages_wrapLength80() {
    skipTest(this, "", "Skip until handling of test results is improved."); // TODO: improve handling of test results - nwinant, 2017-12-18
    System.out.println("testToPrettyMessages_wrapLength80");
    final List<String> expected = Arrays.asList(
            "> " + CLASS_CANONICAL_NAME + ".getMockException(" + CLASS_NAME + ".java:" + EX4_LINE + ")",
            "  Important CycApiException: Here's a Cyc API exception...",
            "  which spans multiple lines",
            "  > " + CLASS_CANONICAL_NAME + ".getMockException(" + CLASS_NAME + ".java:" + EX3_LINE + ")",
            "    Which wraps an UnsupportedOperationException",
            "    > " + CLASS_CANONICAL_NAME + ".getMockException(" + CLASS_NAME + ".java:" + EX2_LINE + ")",
            "      And here's a random SQL exception...",
            "      ... whose message spans...",
            "      (some really very extremely and deliberately excessively over-verbose and",
            "      ridiculously long lines which just keep going and going and going for...)",
            "      ... multiple lines!",
            "      > " + CLASS_CANONICAL_NAME + ".getMockException(" + CLASS_NAME + ".java:" + EX1_LINE + ")",
            "        And finally, some random NullPointerException."
    );
    final Exception ex = getMockException();
    final int wrapLength = 80;
    final List<String> results = CycStringUtils.toPrettyMessages(ex, wrapLength, false, true);
    System.out.println("Results (" + wrapLength + "):");
    System.out.println(repeat('-', wrapLength));
    results.forEach(System.out::println);
    System.out.println(repeat('-', wrapLength));
    assertEquals(expected, results);
  }
  
  /* *
   * Test of hasNumericChar method, of class CycStringUtils.
   * /
  @Test
  public void testHasNumericChar() {
  }

  /**
   * Test of stripLeadingBlanks method, of class CycStringUtils.
   * /
  @Test
  public void testStripLeadingBlanks() {
  }

  /**
   * Test of stripBlanks method, of class CycStringUtils.
   * /
  @Test
  public void testStripBlanks() {
  }
  
  /**
   * Test of wordsToPhrase method, of class CycStringUtils.
   * /
  @Test
  public void testWordsToPhrase() {
  }

  /**
   * Test of unescapeDoubleQuotes method, of class CycStringUtils.
   * /
  @Test
  public void testUnescapeDoubleQuotes() {
  }

  /**
   * Test of escapeQuoteChars method, of class CycStringUtils.
   * /
  @Test
  public void testEscapeQuoteChars() {
  }

  /**
   * Test of getStringForException method, of class CycStringUtils.
   * 
  @Test
  public void testGetStringForException() {
  }
  */

}
