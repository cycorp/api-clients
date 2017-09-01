package com.cyc.baseclient.kbtool;

/*
 * #%L
 * File: AssertToolImplTest.java
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

import com.cyc.base.CycAccess;
import com.cyc.base.cycobject.CycAssertion;
import com.cyc.base.cycobject.CycList;
import com.cyc.base.cycobject.CycVariable;
import com.cyc.base.cycobject.ElMt;
import com.cyc.base.cycobject.FormulaSentence;
import com.cyc.base.exception.CycApiException;
import com.cyc.base.exception.CycConnectionException;
import static com.cyc.baseclient.CycObjectFactory.makeCycVariable;
import com.cyc.baseclient.cycobject.DefaultCycObjectImpl;
import com.cyc.baseclient.cycobject.FormulaSentenceImpl;
import com.cyc.baseclient.inference.params.DefaultInferenceParameters;
import com.cyc.baseclient.testing.TestUtils;
import static com.cyc.baseclient.testing.TestUtils.TEST_MT;
import com.cyc.query.parameters.InferenceParameters;
import com.cyc.session.exception.SessionException;
import java.util.HashSet;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author nwinant
 */
public class AssertToolImplTest {

  public AssertToolImplTest() {
  }

  @BeforeClass
  public static void setUpClass() throws SessionException, CycConnectionException {
    TestUtils.ensureTestEnvironmentInitialized();
    cyc = TestUtils.getCyc();
    mt  = TEST_MT;
  }
  
  @AfterClass
  public static void tearDownClass() {
  }

  @Before
  public void setUp() throws CycConnectionException {
    deleteExistingTestAssertions();
  }

  @After
  public void tearDown() {
    //deleteExistingTestAssertions();
  }

  
  // Fields
  
  private static final CycVariable VAR = makeCycVariable("?VAR");
  public static final String VARSTR      = "" + VAR.getName();
  public static final String EMAIL_ADDR  = "williamhenryharrison"+VARSTR+"@whigpartyusa.com";
  public static final String ASSERT_SENT = "(#$eMailAddressText #$WilliamHenryHarrison "+VARSTR+")";
  
  private static CycAccess cyc = null;
  private static ElMt      mt  = null;
  
  
  // Utility methods
  
  private CycList getExistingTestAssertEmails() throws CycApiException, CycConnectionException {
    final String sentence = ASSERT_SENT.replaceFirst(VARSTR, "" + VAR);
    final InferenceParameters queryProperties = new DefaultInferenceParameters(cyc);
    final CycList response = cyc.getInferenceTool()
            .queryVariable(VAR, sentence, mt, queryProperties, 30);
    return response;
  }
  
  private void deleteExistingTestAssertions() throws CycApiException, CycConnectionException {
    final CycList existingEmails = getExistingTestAssertEmails();
    if (existingEmails.isEmpty()) {
      System.out.println("No previous test assertions to delete!");
      return;
    }
    System.out.println("Current number of existing test assertions: " + existingEmails.size());
    for (Object email : existingEmails) {
      final String sentStr = ASSERT_SENT.replaceFirst(VARSTR, "\"" + email + "\"");
      final FormulaSentence sent = FormulaSentenceImpl.makeCycSentence(cyc, sentStr);
      System.out.println("- Deleting: " + sent.cyclify());
      cyc.getUnassertTool().unassertGaf(sent, mt);
    }
    System.out.println(
            "Updated number of existing test assertions: "
            + getExistingTestAssertEmails().size());
  }
  
  private void printAssertions(CycList<CycAssertion> assertions) {
    System.out.println("Result:");
    System.out.println(assertions.toPrettyString("  "));
    System.out.println("--");
    for (Object o : assertions) {
      System.out.println("- " + (o != null ? o.getClass().getSimpleName() : null));
      if (o instanceof List) {
        final List l = (List) o;
        for (Object o1 : l) {
          System.out.println("  - " + (o1 != null ? o1.getClass().getSimpleName() : null));
          if (o1 instanceof List) {
            final List l1 = (List) o1;
            for (Object o2 : l1) {
              System.out.println("    - " + (o2 != null ? o2.getClass().getSimpleName() : null));
            }
          }
        }
      }
    }
  }
  
  
  // Tests
  
  @Test
  public void testAssertSentence_single() throws CycApiException, CycConnectionException {
    // Setup
    System.out.println("testAssertSentence_single");
    final long emailAddrId    = System.currentTimeMillis();
    final String emailAddrStr = "\"" + EMAIL_ADDR.replaceFirst(VARSTR, "" + emailAddrId) + "\"";
    final String sentenceStr  = ASSERT_SENT.replaceFirst(VARSTR, emailAddrStr);
    
    // Assert
    System.out.println("Asserting: " + sentenceStr);
    System.out.println("       In: " + mt);
    final CycList<CycAssertion> result = cyc.getAssertTool()
            .assertSentence("'" + sentenceStr, mt, false, true);
    //printAssertions(result);
    
    // Evaluate
    assertNotNull(result);
    assertEquals(1, result.size());
    assertTrue(result.get(0) instanceof CycAssertion);
    final CycAssertion asserted = (CycAssertion) result.get(0);
    System.out.println("Resulting assertion: " + asserted.cyclify());
    assertEquals(mt, asserted.getMt());
    //assertEquals(sentenceStr, asserted.getFormula().cyclify());
  }

  @Test
  public void testAssertSentence_repeatedly_identical() 
          throws CycApiException, CycConnectionException {
    // Setup
    System.out.println("testAssertSentence_repeatedly_identical");
    final long emailAddrId    = System.currentTimeMillis();
    final String emailAddrStr = "\"" + EMAIL_ADDR.replaceFirst(VARSTR, "" + emailAddrId) + "\"";
    final String sentenceStr  = ASSERT_SENT.replaceFirst(VARSTR, emailAddrStr);
    final int totalNumAsserts = 10;
    CycList<CycAssertion> expected = null;
    CycList<CycAssertion> result = null;
    
    // Assert & evaluate
    for (int i = 0; i < totalNumAsserts; i++) {
      System.out.println("Asserting: " + sentenceStr);
      System.out.println("       In: " + mt);
      result = cyc.getAssertTool()
              .assertSentence("'" + sentenceStr, mt, false, false);
      if (expected == null) {
        expected = result;
      }
    }
    //printAssertions(result);
    assertNotNull(result);
    assertEquals(1, result.size());
    assertTrue(result.get(0) instanceof CycAssertion);
    final CycAssertion asserted = (CycAssertion) result.get(0);
    System.out.println("Resulting assertion: " + asserted.cyclify());
    assertEquals(mt, asserted.getMt());
    assertEquals(expected, result);
    assertEquals(expected.toString(), result.toString());
    final String resultId
            = DefaultCycObjectImpl.toCompactExternalId(asserted, cyc);
    final String expectedId
            = DefaultCycObjectImpl.toCompactExternalId((CycAssertion) expected.get(0), cyc);
    System.out.println("Result   ID: " + resultId);
    System.out.println("Expected ID: " + expectedId);
    assertEquals(expectedId, resultId);
  }
  
  @Test
  public void testAssertSentence_conjunction() throws CycApiException, CycConnectionException {
    // Setup
    System.out.println("testAssertSentence_conjunction");
    final long emailAddrId    = System.currentTimeMillis();
    final int totalNumAsserts = 3;
    final StringBuilder sb = new StringBuilder().append("(#$and ");
    for (int i = 0; i < totalNumAsserts; i++) {
      final String emailAddr 
              = "\"" + EMAIL_ADDR.replaceFirst(VARSTR, "" + (emailAddrId + i)) + "\"";
      final String sentence  = ASSERT_SENT.replaceFirst(VARSTR, emailAddr);
      sb.append("\n  ").append(sentence);
    }
    sb.append(")");
    final String sentenceStr  = sb.toString();
    
    // Assert
    System.out.println("Asserting: " + sentenceStr);
    System.out.println("       In: " + mt);
    final CycList<CycAssertion> results = cyc.getAssertTool()
            .assertSentence("'" + sentenceStr, mt, false, true);
    //printAssertions(result);
    
    // Evaluate
    assertNotNull(results);
    assertEquals(totalNumAsserts, results.size());
    for (int i = 0; i < totalNumAsserts; i++) {
      assertTrue(results.get(i) instanceof CycAssertion);
      final CycAssertion asserted = (CycAssertion) results.get(i);
      System.out.println("Resulting assertion: " + asserted.cyclify());
      assertEquals(mt, asserted.getMt());
    }
    assertEquals(results.size(), new HashSet(results).size());    
  }
  
}
