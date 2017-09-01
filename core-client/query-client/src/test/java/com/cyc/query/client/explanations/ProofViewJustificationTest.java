/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyc.query.client.explanations;

/*
 * #%L
 * File: ProofViewJustificationTest.java
 * Project: Query Client
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
import com.cyc.base.exception.CycConnectionException;
import com.cyc.kb.Context;
import com.cyc.kb.exception.CreateException;
import com.cyc.kb.exception.KbTypeException;
import com.cyc.query.ProofViewSpecification;
import com.cyc.query.QueryAnswer;
import static com.cyc.query.client.TestUtils.assumeNotOpenCyc;
import com.cyc.session.exception.OpenCycUnsupportedFeatureException;
import com.cyc.session.exception.SessionCommunicationException;
import com.cyc.xml.query.ProofViewTestConstants;
import static com.cyc.xml.query.ProofViewTestConstants.*;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author baxter
 */
public class ProofViewJustificationTest {

  static ProofViewSpecification spec = new ProofViewSpecificationImpl();

  public ProofViewJustificationTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
    assumeNotOpenCyc();
    ProofViewTestConstants.setup();

  }
  static ProofViewGeneratorImpl instance;

  @AfterClass
  public static void tearDownClass() {
    ProofViewTestConstants.teardown();
  }

  @Before
  public void setUp() throws Exception {
    instance = new ProofViewGeneratorImpl(currentAnswer, spec);
  }

  @After
  public void tearDown() {
  }

  /**
   * Test of getAnswer method, of class ProofViewGenerator.
   */
  @Test
  public void testGetAnswer() {
    System.out.println("getAnswer");
    QueryAnswer expResult = currentAnswer;
    QueryAnswer result = instance.getQueryAnswer();
    System.out.println("Expected " + expResult + "\nGot " + result);
    assertEquals(expResult, result);
  }

  /**
   * Test of generate method, of class ProofViewGenerator.
   */
  @Test
  public void testPopulate() throws OpenCycUnsupportedFeatureException {
    System.out.println("populate");
    assumeNotOpenCyc();
    instance.generate();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testPopulateTwice() throws OpenCycUnsupportedFeatureException {
    System.out.println("populate");
    assumeNotOpenCyc();
    instance.generate();
    instance.generate();
  }

  /**
   * Test of getDomainContext method, of class ProofViewGenerator.
   *
   * @throws com.cyc.session.exception.SessionCommunicationException
   * @throws com.cyc.base.exception.CycConnectionException
   */
  @Test
  public void testGetDomainMt() throws SessionCommunicationException, CycConnectionException, OpenCycUnsupportedFeatureException {
    System.out.println("getDomainContext");
    assumeNotOpenCyc();
    Context result = spec.getDomainContext();
    assertNotNull("Domain mt was null", result);
    spec.setDomainContext(DOMAIN_CONTEXT);
    assertEquals(DOMAIN_CONTEXT, spec.getDomainContext());
    instance.generate();
    assertEquals(DOMAIN_CONTEXT, instance.getDomainContext());
  }

  /**
   * Test of setDomainContext method, of class ProofViewGenerator.
   *
   * @throws com.cyc.base.exception.CycConnectionException
   */
  @Test
  public void testSetDomainMt() throws CycConnectionException {
    System.out.println("setDomainContext");
    spec.setDomainContext(DOMAIN_CONTEXT);
  }

  /**
   * Test of isIncludeDetails method, of class ProofViewGenerator.
   */
  @Test
  public void testIsIncludeDetails() throws SessionCommunicationException, OpenCycUnsupportedFeatureException {
    System.out.println("isIncludeDetails");
    assumeNotOpenCyc();
    spec.isIncludeDetails();
    spec.setIncludeDetails(true);
    assertEquals(true, instance.isIncludeDetails());
    assertEquals(true, spec.isIncludeDetails());
    spec.setIncludeDetails(false);
    assertEquals(false, instance.isIncludeDetails());
    assertEquals(false, spec.isIncludeDetails());
    instance.generate();
    assertEquals(false, instance.isIncludeDetails());
  }

  /**
   * Test of setIncludeDetails method, of class ProofViewGenerator.
   *
   * @throws com.cyc.session.exception.SessionCommunicationException
   */
  @Test
  public void testSetIncludeDetails() throws SessionCommunicationException {
    System.out.println("setIncludeDetails");
    spec.setIncludeDetails(false);
  }

  /**
   * Test of isIncludeLinear method, of class ProofViewGenerator.
   *
   * @throws com.cyc.session.exception.SessionCommunicationException
   */
  @Test
  public void testIsIncludeLinear() throws SessionCommunicationException, OpenCycUnsupportedFeatureException {
    System.out.println("isIncludeLinear");
    assumeNotOpenCyc();
    instance.isIncludeLinear();
    spec.setIncludeLinear(true);
    assertEquals(true, instance.isIncludeLinear());
    assertEquals(true, spec.isIncludeLinear());
    spec.setIncludeLinear(false);
    assertEquals(false, instance.isIncludeLinear());
    assertEquals(false, spec.isIncludeLinear());
    instance.generate();
    assertEquals(false, instance.isIncludeLinear());
  }

  /**
   * Test of setIncludeLinear method, of class ProofViewGenerator.
   *
   * @throws com.cyc.session.exception.SessionCommunicationException
   */
  @Test
  public void testSetIncludeLinear() throws SessionCommunicationException {
    System.out.println("setIncludeLinear");
    spec.setIncludeLinear(false);
  }

  /**
   * Test of isIncludeSummary method, of class ProofViewGenerator.
   *
   * @throws com.cyc.session.exception.SessionCommunicationException
   */
  @Test
  public void testIsIncludeSummary() throws SessionCommunicationException, OpenCycUnsupportedFeatureException {
    System.out.println("isIncludeSummary");
    assumeNotOpenCyc();
    spec.setIncludeSummary(true);
    assertEquals(true, instance.isIncludeSummary());
    assertEquals(true, spec.isIncludeSummary());
    spec.setIncludeSummary(false);
    assertEquals(false, instance.isIncludeSummary());
    assertEquals(false, spec.isIncludeSummary());
    instance.generate();
    assertEquals(false, instance.isIncludeSummary());
  }

  /**
   * Test of setIncludeSummary method, of class ProofViewGenerator.
   */
  @Test
  public void testSetIncludeSummary() throws SessionCommunicationException {
    System.out.println("setIncludeSummary");
    spec.setIncludeSummary(false);
  }

  /**
   * Test of getLanguageMt method, of class ProofViewGenerator.
   *
   * @throws com.cyc.session.exception.SessionCommunicationException
   */
  @Test
  public void testGetLanguageMt() throws SessionCommunicationException, OpenCycUnsupportedFeatureException, KbTypeException, CreateException {
    System.out.println("getLanguageMt");
    assumeNotOpenCyc();
    Context result = instance.getLanguageContext();
    assertNotNull("Language mt was null", result);
    spec.setLanguageContext(LANGUAGE_CONTEXT);
    assertEquals(LANGUAGE_CONTEXT, instance.getLanguageContext());
    assertEquals(LANGUAGE_CONTEXT, spec.getLanguageContext());
    instance.generate();
    assertEquals(LANGUAGE_CONTEXT, instance.getLanguageContext());
  }

  /**
   * Test of setLanguageMt method, of class ProofViewGenerator.
   *
   * @throws com.cyc.session.exception.SessionCommunicationException
   */
  @Test
  public void testSetLanguageMt() throws SessionCommunicationException {
    System.out.println("setLanguageMt");
    spec.setLanguageContext(LANGUAGE_CONTEXT);
  }

  /**
   * Test of getJustification method, of class ProofViewGenerator.
   */
  @Test
  public void testGetRoot() throws OpenCycUnsupportedFeatureException {
    System.out.println("getRoot");
    assumeNotOpenCyc();
    instance.generate();
    assertNotNull(instance.getExplanation());
  }

  /**
   * Test of isSuppressAssertionBookkeeping method, of class ProofViewGenerator.
   *
   * @throws com.cyc.session.exception.SessionCommunicationException
   */
  @Test
  public void testIsSuppressAssertionBookkeeping() throws SessionCommunicationException, OpenCycUnsupportedFeatureException {
    System.out.println("isIncludeAssertionBookkeeping");
    assumeNotOpenCyc();
    spec.setIncludeAssertionBookkeeping(true);
    assertEquals(true, instance.isIncludeAssertionBookkeeping());
    assertEquals(true, spec.isIncludeAssertionBookkeeping());
    spec.setIncludeAssertionBookkeeping(false);
    assertEquals(false, instance.isIncludeAssertionBookkeeping());
    assertEquals(false, spec.isIncludeAssertionBookkeeping());
    instance.generate();
    assertEquals(false, instance.isIncludeAssertionBookkeeping());
  }

  /**
   * Test of setSuppressAssertionBookkeeping method, of class ProofViewGenerator.
   *
   * @throws com.cyc.session.exception.SessionCommunicationException
   */
  @Test
  public void testSetSuppressAssertionBookkeeping() throws SessionCommunicationException {
    System.out.println("setIncludeAssertionBookkeeping");
    spec.setIncludeAssertionBookkeeping(true);
  }

  /**
   * Test of isSuppressAssertionCyclists method, of class ProofViewGenerator.
   *
   * @throws com.cyc.session.exception.SessionCommunicationException
   */
  @Test
  public void testIsSuppressAssertionCyclists() throws SessionCommunicationException, OpenCycUnsupportedFeatureException {
    System.out.println("isIncludeAssertionCyclists");
    assumeNotOpenCyc();
    spec.isIncludeAssertionCyclists();
    spec.setIncludeAssertionCyclists(true);
    assertEquals(true, instance.isIncludeAssertionCyclists());
    assertEquals(true, spec.isIncludeAssertionCyclists());
    spec.setIncludeAssertionCyclists(false);
    assertEquals(false, instance.isIncludeAssertionCyclists());
    assertEquals(false, instance.isIncludeAssertionCyclists());
    instance.generate();
    assertEquals(false, instance.isIncludeAssertionCyclists());
  }

  /**
   * Test of setSuppressAssertionCyclists method, of class ProofViewGenerator.
   *
   * @throws com.cyc.session.exception.SessionCommunicationException
   */
  @Test
  public void testSetSuppressAssertionCyclists() throws SessionCommunicationException {
    System.out.println("setIncludeAssertionCyclists");
    spec.setIncludeAssertionCyclists(false);
  }

}
