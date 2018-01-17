package com.cyc.baseclient.cycobject;

/*
 * #%L
 * File: CycVariableImplTest.java
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

import com.cyc.base.cycobject.CycVariable;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.baseclient.CycObjectFactory;
import com.cyc.baseclient.testing.TestIterator;
import com.cyc.baseclient.testing.TestIterator.IteratedTest;
import com.cyc.baseclient.xml.XmlStringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.cyc.baseclient.CycObjectFactory.makeCycVariable;
import static com.cyc.baseclient.cycobject.CycObjectUnitTest.doTestCycObjectRetrievable;
import static com.cyc.baseclient.testing.TestConstants.VAR_0;
import static com.cyc.baseclient.testing.TestConstants.VAR_VARIABLE;
import static com.cyc.baseclient.testing.TestConstants.VAR_X;
import static com.cyc.baseclient.testing.TestConstants.VAR_Y;
import static com.cyc.baseclient.testing.TestConstants.VAR_Z;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 *
 * @author baxter
 */
public class CycVariableImplTest {

  public CycVariableImplTest() {
  }

  @BeforeClass
  public static void setUpClass() {
  }

  @AfterClass
  public static void tearDownClass() {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }
  
  
  // Fields
  
  private static final TestIterator<String> NAME_TESTER = new TestIterator<String>();
  
  
  // Tests
  
  @Test(expected = NullPointerException.class)
  public void testNullVariableName() {
    System.out.println("testNullVariableName");
    CycVariableImpl.isValidVariableName(null);
  }

  @Test
  public void testValidVariableNames() {
    System.out.println("testValidVariableNames");
    assertEquals(0, NAME_TESTER.testValidAndInvalidObjects(
            Arrays.asList(
                    "?X", "?VAR", "??X", "??VAR", "?VAR-NAME",
                    ":X", ":METAVAR",
                    "?X", "?XX", "?X-X", "?ABC-234", "?ABC-ABC", "?UIO-123-UIO",
                    "??X", "??XX", "??X-X", "??ABC-234", "??ABC-ABC", "??UIO-123-UIO"),
            
            Arrays.asList(
                    "", " ", "     ",
                    "?", "??", "???",
                    "?x", "??x", "???x",
                    "?var", "??var", "???var",
                    " ?X", "        ?X", "?X ", "?X      ", " ?X ",
                    " ?VAR", "        ?VAR", "?VAR ", "?VAR      ", " ?VAR ",
                    "???VAR", "::VAR", ":::VAR",
                    "X", "VAR", "XX",
                    ":", ":?METAVAR",
                    "?:X", "?:VAR", "?:",
                    "?VAR NAME", "?X Y", "?X ?Y", "?VAR ?NAME",
                    "?VAR?NAME", "?VAR:NAME", ":VAR?NAME",
                    "?VAR??NAME", "?VAR::NAME", ":VAR??NAME", "?VAR:?NAME", ":VAR?:NAME",
                    "?-", "??234", "qawerpiouasdf",
                    "?1", "?.", "?@", "?!", "?234a2354dsf", "?234-ABC", "??234-ABC"),
            
            new IteratedTest<String>() {
              @Override
              public boolean isValidObject(String varName) {
                // TODO: add call to (valid-el-var-name? "?VAR-NAME") - nwinant, 2015-11-11
                return CycVariableImpl.isValidVariableName(varName);
              }
            }).size());
  }
  
  @Test
  public void testMetaVariableNames() {
    System.out.println("testMetaVariableNames");
    assertEquals(0, NAME_TESTER.testValidAndInvalidObjects(
            Arrays.asList(
                    ":X", ":VAR",
                    ":X", ":XX", ":X-X", ":ABC-234", ":ABC-ABC", ":UIO-123-UIO"),
            
            Arrays.asList(
                    "?X", "?VAR",
                    "??X", "??VAR",
                    "::X", "::VAR",
                    ":-", "::234", "", "XX", "qawerpiouasdf", "::",
                    "::X", ":1", ":.", ":@", ":!", ":234a2354dsf", ":234-ABC", "::234-ABC"),
            
            new IteratedTest<String>() {
              @Override
              public boolean isValidObject(String varName) {
                if (!CycVariableImpl.isValidVariableName(varName)) {
                  return false;
                }
                return new CycVariableImpl(varName).isMetaVariable();
              }
            }).size());
  }

  @Test
  public void testDontCareVariableNames() {
    System.out.println("testDontCareVariableNames");
    assertEquals(0, NAME_TESTER.testValidAndInvalidObjects(
            Arrays.asList(
                    "??X", "??VAR"),
            
            Arrays.asList(
                    "?X", "?VAR",
                    ":X", ":VAR"),
            
            new IteratedTest<String>() {
              @Override
              public boolean isValidObject(String varName) {
                if (!CycVariableImpl.isValidVariableName(varName)) {
                  return false;
                }
                return new CycVariableImpl(varName).isDontCareVariable();
              }
            }).size());
  }
  
  /**
   * Tests <tt>CycVariable</tt> object behavior.
   * @throws com.cyc.base.exception.CycConnectionException
   */
  @Test
  public void testCycVariable() throws CycConnectionException {
    System.out.println("\n*** testCycVariable ***");
    final CycVariable cycVariable1 = VAR_X;
    assertNotNull(cycVariable1);
    assertEquals("?X", cycVariable1.toString());
    assertEquals("?X", cycVariable1.cyclify());
    assertEquals("'?X", cycVariable1.stringApiValue());
    final CycVariable cycVariable2 = VAR_VARIABLE;
    assertNotNull(cycVariable2);
    assertEquals("?variable", cycVariable2.toString().toLowerCase());
    assertEquals("?variable", cycVariable2.cyclify().toLowerCase());
    assertEquals("'?variable", cycVariable2.stringApiValue().toLowerCase());
    final CycVariable cycVariable3 = new CycVariableImpl("?X");
    assertEquals(cycVariable1.toString(), cycVariable3.toString());
    assertEquals(cycVariable1.cyclify(), cycVariable3.cyclify());
    assertEquals(cycVariable1.stringApiValue(), cycVariable3.stringApiValue());
    assertEquals(cycVariable1, cycVariable3);
    assertTrue(makeCycVariable("??X").isDontCareVariable());

    // compareTo
    final ArrayList variables = new ArrayList();
    variables.add(VAR_Y);
    variables.add(VAR_Z);
    variables.add(VAR_Y);
    variables.add(VAR_X);
    variables.add(VAR_Z);
    variables.add(VAR_X);
    Collections.sort(variables);
    assertEquals("[?X, ?X, ?Y, ?Y, ?Z, ?Z]", variables.toString().toUpperCase());
    final CycVariable cycVariable1000 = new CycVariableImpl(":X");
    assertNotSame(cycVariable1, cycVariable1000);

    doTestCycObjectRetrievable(VAR_0);
    doTestCycObjectRetrievable(VAR_X);

    // makeUniqueCycVariable
    final CycVariableImpl x = (CycVariableImpl) VAR_X;
    final CycVariable x1 = CycObjectFactory.makeUniqueCycVariable(x);
    final CycVariable x2 = CycObjectFactory.makeUniqueCycVariable(x);
    final CycVariable x3 = CycObjectFactory.makeUniqueCycVariable(x);
    assertFalse((x.equals(x1)));
    assertFalse((x.equals(x2)));
    assertFalse((x.equals(x3)));
    assertFalse((x1.equals(x2)));
    assertTrue(x.cyclify().equals("?X"));
    assertTrue(x1.cyclify().startsWith("?X-"));
    assertTrue(x3.cyclify().startsWith("?X-"));

    // toXML, toXMLString, unmarshal
    final XmlStringWriter xmlStringWriter = new XmlStringWriter();
    try {
      x.toXML(xmlStringWriter, 0, false);
      assertEquals("<variable>X</variable>\n", xmlStringWriter.toString());
      assertEquals("<variable>X</variable>\n", x.toXMLString());
      final String cycVariableXMLString = x.toXMLString();
      CycObjectFactory.resetCycVariableCache();
      final Object object = CycObjectFactory.unmarshal(cycVariableXMLString);
      assertTrue(object instanceof CycVariable);
      assertEquals(x, (CycVariable) object);
      assertTrue(CycObjectFactory.unmarshal(cycVariableXMLString)
                         == CycObjectFactory.unmarshal(cycVariableXMLString));
    } catch (Exception e) {
      fail(e.getMessage());
    }
    System.out.println("*** testCycVariable OK ***");
  }
  
}
