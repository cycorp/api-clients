/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyc.baseclient.datatype;

/*
 * #%L
 * File: StringUtilsTest.java
 * Project: Base Client
 * %%
 * Copyright (C) 2013 - 2017 Cycorp, Inc.
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
import static com.cyc.baseclient.datatype.StringUtils.DISABLE_WORD_WRAP;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import static org.apache.commons.lang3.StringUtils.repeat;
import org.junit.*;
import static org.junit.Assert.*;



/**
 *
 * @author baxter
 */
public class StringUtilsTest {

  public StringUtilsTest() {
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

  /**
   * Test of isNumeric method, of class StringUtils.
   */
  @Test
  public void testIsNumeric() {
  }

  /**
   * Test of hasNumericChar method, of class StringUtils.
   */
  @Test
  public void testHasNumericChar() {
  }

  /**
   * Test of isWhitespace method, of class StringUtils.
   */
  @Test
  public void testIsWhitespace() {
  }

  /**
   * Test of stripLeading method, of class StringUtils.
   */
  @Test
  public void testStripLeading() {
  }

  /**
   * Test of stripTrailing method, of class StringUtils.
   */
  @Test
  public void testStripTrailing() {
  }

  /**
   * Test of stripLeadingBlanks method, of class StringUtils.
   */
  @Test
  public void testStripLeadingBlanks() {
  }

  /**
   * Test of stripTrailingBlanks method, of class StringUtils.
   */
  @Test
  public void testStripTrailingBlanks() {
  }

  /**
   * Test of stripBlanks method, of class StringUtils.
   */
  @Test
  public void testStripBlanks() {
  }

  /**
   * Test of change method, of class StringUtils.
   */
  @Test
  public void testChange() {
  }

  /**
   * Test of isDelimitedString method, of class StringUtils.
   */
  @Test
  public void testIsDelimitedString() {
  }

  /**
   * Test of removeDelimiters method, of class StringUtils.
   */
  @Test
  public void testRemoveDelimiters() {
  }

  /**
   * Test of wordsToPhrase method, of class StringUtils.
   */
  @Test
  public void testWordsToPhrase() {
  }

  /**
   * Test of escapeDoubleQuotes method, of class StringUtils.
   */
  @Test
  public void testEscapeDoubleQuotes() {

    String str = "\\s \"werj 9234 \\3";

    String expResult = "\\\\s \\\"werj 9234 \\\\3";
    String result = StringUtils.escapeDoubleQuotes(str);
    assertEquals(expResult, result);
  }

  /**
   * Test of unescapeDoubleQuotes method, of class StringUtils.
   */
  @Test
  public void testUnescapeDoubleQuotes() {
  }

  /**
   * Test of escapeQuoteChars method, of class StringUtils.
   */
  @Test
  public void testEscapeQuoteChars() {
  }

  /**
   * Test of getStringForException method, of class StringUtils.
   */
  @Test
  public void testGetStringForException() {
  }

  /**
   * Test of is7BitASCII method, of class StringUtils.
   */
  @Test
  public void testIs7BitASCII() {
  }

  /**
   * Test of cyclStringToJavaString method, of class StringUtils.
   */
  @Test
  public void testCyclStringToJavaString() {
    System.out.println("cyclStringToJavaString");
    Object cyclString = "test";
    String expResult = "test";
    String result = StringUtils.cyclStringToJavaString(cyclString);
    assertEquals(expResult, result);

    cyclString = new NautImpl(CommonConstants.UNICODE_STRING_FN, "Brade&u161;ko");
    expResult = NonAsciiStrings.get("bradeshko");
    result = StringUtils.cyclStringToJavaString(cyclString);
    assertEquals(expResult, result);
  }

  /**
   * Test of unicodeEscaped method, of class StringUtils.
   */
  @Test
  public void testUnicodeEscaped_String() {

    StringBuffer ex_ascii = new StringBuffer("abc&289;");
    ex_ascii.append((char) 0xac);
    System.out.println("Original: " + ex_ascii.toString() + "|");
    System.out.println("Sublisp Unicode escaped: |" + StringUtils.unicodeEscaped(ex_ascii.toString()) + "|");
  }

  /**
   * Test of unicodeEscaped method, of class StringUtils.
   */
  @Test
  public void testUnicodeEscaped_String_boolean() {
  }

  /**
   * Test of deEscapeHTMLescapedString method, of class StringUtils.
   */
  @Test
  public void testDeEscapeHTMLescapedString() {

    String tests[] = {"simple", "&amp;", "abc&a", "&#64", "&#64;&#20;", "sadf&whatever;sdfsd", "", "&u64;"};

    for (int i = 0; i < tests.length; i++) {
      try {
        System.out.println("DeEscape: org: |" + tests[i] + "| deEscaped: |" + StringUtils.deEscapeHTMLescapedString(tests[i]) + "|.");
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
  
  private static final int EX1_LINE = 245;
  private static final int EX2_LINE = 246;
  private static final int EX3_LINE = 252;
  private static final int EX4_LINE = 254;
  
  @Test
  public void testToPrettyMessage_defaults() {
    System.out.println("testToPrettyMessage_defaults");
    final List<String> expected = Arrays.asList(
            "> CycApiException: com.cyc.baseclient.datatype.StringUtilsTest.getMockException(StringUtilsTest.java:" + EX4_LINE + ")",
            "  Important CycApiException: Here's a Cyc API exception...",
            "  which spans multiple lines"
    );
    final Exception ex = getMockException();
    final List<String> results = StringUtils.toPrettyMessage(ex);
    System.out.println("Results:");
    System.out.println("----------");
    StringUtils.printlns(results);
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
    final List<String> results = StringUtils
            .toPrettyMessage(ex, "+-", "|-", DISABLE_WORD_WRAP, true, false);
    System.out.println("Results:");
    System.out.println("----------");
    StringUtils.printlns(results);
    System.out.println("----------");
    assertEquals(expected, results);
  }
  
  @Test
  public void testToPrettyMessages_defaults() {
    System.out.println("testToPrettyMessages_defaults");
    final List<String> expected = Arrays.asList(
            "> CycApiException: com.cyc.baseclient.datatype.StringUtilsTest.getMockException(StringUtilsTest.java:" + EX4_LINE + ")",
            "  Important CycApiException: Here's a Cyc API exception...",
            "  which spans multiple lines",
            "  > UnsupportedOperationException: com.cyc.baseclient.datatype.StringUtilsTest.getMockException(StringUtilsTest.java:" + EX3_LINE + ")",
            "    Which wraps an UnsupportedOperationException",
            "    > SQLException: com.cyc.baseclient.datatype.StringUtilsTest.getMockException(StringUtilsTest.java:" + EX2_LINE + ")",
            "      And here's a random SQL exception...",
            "      ... whose message spans...",
            "      (some really very extremely and deliberately excessively over-verbose and ridiculously long lines which just keep going and going and going for...)",
            "      ... multiple lines!",
            "      > NullPointerException: com.cyc.baseclient.datatype.StringUtilsTest.getMockException(StringUtilsTest.java:" + EX1_LINE + ")",
            "        And finally, some random NullPointerException."
    );
    final Exception ex = getMockException();
    final List<String> results = StringUtils.toPrettyMessages(ex);
    System.out.println("Results:");
    System.out.println("----------");
    StringUtils.printlns(results);
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
    final List<String> results = StringUtils
            .toPrettyMessages(ex, "+-", "|---", DISABLE_WORD_WRAP, true, false);
    System.out.println("Results:");
    System.out.println("----------");
    StringUtils.printlns(results);
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
    final List<String> results = StringUtils.toPrettyMessages(ex, wrapLength, false, false);
    System.out.println("Results (" + wrapLength + "):");
    System.out.println(repeat('-', wrapLength));
    StringUtils.printlns(results);
    System.out.println(repeat('-', wrapLength));
    assertEquals(expected, results);
  }
  
  @Test
  public void testToPrettyMessages_wrapLength80() {
    System.out.println("testToPrettyMessages_wrapLength80");
    final List<String> expected = Arrays.asList(
            "> com.cyc.baseclient.datatype.StringUtilsTest.getMockException(StringUtilsTest.java:" + EX4_LINE + ")",
            "  Important CycApiException: Here's a Cyc API exception...",
            "  which spans multiple lines",
            "  > com.cyc.baseclient.datatype.StringUtilsTest.getMockException(StringUtilsTest.java:" + EX3_LINE + ")",
            "    Which wraps an UnsupportedOperationException",
            "    > com.cyc.baseclient.datatype.StringUtilsTest.getMockException(StringUtilsTest.java:" + EX2_LINE + ")",
            "      And here's a random SQL exception...",
            "      ... whose message spans...",
            "      (some really very extremely and deliberately excessively over-verbose and",
            "      ridiculously long lines which just keep going and going and going for...)",
            "      ... multiple lines!",
            "      > com.cyc.baseclient.datatype.StringUtilsTest.getMockException(StringUtilsTest.java:" + EX1_LINE + ")",
            "        And finally, some random NullPointerException."
    );
    final Exception ex = getMockException();
    final int wrapLength = 80;
    final List<String> results = StringUtils.toPrettyMessages(ex, wrapLength, false, true);
    System.out.println("Results (" + wrapLength + "):");
    System.out.println(repeat('-', wrapLength));
    StringUtils.printlns(results);
    System.out.println(repeat('-', wrapLength));
    assertEquals(expected, results);
  }

}
