/*
 * Copyright 2015 Cycorp, Inc..
 *
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
 */
package com.cyc.query;

/*
 * #%L
 * File: InferenceAnswerBackedQueryAnswerIT.java
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
import com.cyc.base.inference.InferenceAnswer;
import com.cyc.kb.Context;
import com.cyc.kb.KbTerm;
import com.cyc.kb.Sentence;
import com.cyc.kb.Variable;
import com.cyc.kb.VariableFactory;
import com.cyc.kb.client.ContextImpl;
import com.cyc.kb.client.SentenceImpl;
import com.cyc.kb.exception.CreateException;
import com.cyc.kb.exception.KbException;
import com.cyc.kb.exception.KbTypeException;
import static com.cyc.query.TestUtils.assumeNotOpenCyc;
import static com.cyc.query.TestUtils.getCyc;
import com.cyc.query.exception.QueryConstructionException;
import com.cyc.session.exception.SessionCommunicationException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author daves
 */
public class InferenceAnswerBackedQueryAnswerIT {

  public InferenceAnswerBackedQueryAnswerIT() {
  }

  @BeforeClass
  public static void setUpClass() throws CycConnectionException {
    com.cyc.baseclient.testing.KbPopulator.ensureKBPopulated(getCyc());
  }

  @AfterClass
  public static void tearDownClass() {
  }

  /**
   * Test of getId method, of class InferenceAnswerBackedQueryAnswer.
   */
  //@Test
  public void testGetId() {
    System.out.println("getId");
    final InferenceAnswerBackedQueryAnswer instance = null;
    final InferenceAnswerIdentifier expResult = null;
    final InferenceAnswerIdentifier result = instance.getId();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getAnswerCyc method, of class InferenceAnswerBackedQueryAnswer.
   */
  //@Test
  public void testGetAnswerCyc() {
    System.out.println("getAnswerCyc");
    final InferenceAnswerBackedQueryAnswer instance = null;
    final InferenceAnswer expResult = null;
    final InferenceAnswer result = instance.getAnswerCyc();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getBinding method, of class InferenceAnswerBackedQueryAnswer.
   * @throws com.cyc.kb.exception.KbTypeException
   * @throws com.cyc.query.exception.QueryConstructionException
   * @throws com.cyc.session.exception.SessionCommunicationException
   */
  @Test
  public void testGetBinding() throws KbTypeException, QueryConstructionException, SessionCommunicationException {
    System.out.println("getBinding");
    final Variable var = VariableFactory.get("?SENT");
    final List<QueryAnswer> a = QueryFactory.getQuery("(equals ?SENT (likesAsFriend GeorgeWashington JohnAdams))").getAnswers();
    final Object result = a.get(0).getBinding(var);
    assertTrue("Inference answer " + result + " should have been a Sentence, but was a " + result.getClass(), result instanceof Sentence);
  }

  /**
   * Test of getProofIdentifier method, of class InferenceAnswerBackedQueryAnswer.
   */
  //@Test
  public void testGetProofIdentifier() {
    System.out.println("getProofIdentifier");
    final InferenceAnswerBackedQueryAnswer instance = null;
    final ProofIdentifier expResult = null;
    final ProofIdentifier result = instance.getProofIdentifier();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getProofIdentifiers method, of class InferenceAnswerBackedQueryAnswer.
   */
  //@Test
  public void testGetProofIdentifiers() throws Exception {
    System.out.println("getProofIdentifiers");
    final InferenceAnswerBackedQueryAnswer instance = null;
    final Set<ProofIdentifier> expResult = null;
    final Set<ProofIdentifier> result = instance.getProofIdentifiers();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getBindings method, of class InferenceAnswerBackedQueryAnswer.
   */
  //@Test
  public void testGetBindings() {
    System.out.println("getBindings");
    final InferenceAnswerBackedQueryAnswer instance = null;
    final Map<Variable, Object> expResult = null;
    final Map<Variable, Object> result = instance.getBindings();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of toString method, of class InferenceAnswerBackedQueryAnswer.
   */
  //@Test
  public void testToString() {
    System.out.println("toString");
    final InferenceAnswerBackedQueryAnswer instance = null;
    final String expResult = "";
    final String result = instance.toString();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getSources method, of class InferenceAnswerBackedQueryAnswer.
   */
  @Test
  public void testGetSources() throws KbTypeException, CreateException, SessionCommunicationException, QueryConstructionException, KbException {
    System.out.println("getSources");
    assumeNotOpenCyc();
    final Context WHH_WP_CTX = ContextImpl.findOrCreate("(#$ContextOfPCWFn #$TestFactEntrySource-WikipediaArticle-WilliamHenryHarrison)");
    final Sentence querySentence = new SentenceImpl("(#$isa #$WilliamHenryHarrison (#$FormerFn #$UnitedStatesPresident))");
    final Query q = QueryFactory.getQuery(querySentence, WHH_WP_CTX);
    q.retainInference();
    final QueryAnswer answer = q.getAnswer(0);
    final Set<KbTerm> sources = answer.getSources();
    assertFalse(sources.isEmpty());
  }

  /**
   * Test of getSources method, of class InferenceAnswerBackedQueryAnswer.  Should throw an exception because
   * the inference has not been retained.
   */
  @Test(expected=UnsupportedOperationException.class)
  public void testGetSourcesFromClosedInference() throws KbTypeException, CreateException, SessionCommunicationException, QueryConstructionException, KbException {
    System.out.println("getSourcesFromClosed");
    assumeNotOpenCyc();
    final Context WHH_WP_CTX = ContextImpl.findOrCreate("(#$ContextOfPCWFn #$TestFactEntrySource-WikipediaArticle-WilliamHenryHarrison)");
    final Sentence querySentence = new SentenceImpl("(#$isa #$WilliamHenryHarrison (#$FormerFn #$UnitedStatesPresident))");
    final Query q = QueryFactory.getQuery(querySentence, WHH_WP_CTX);
    final QueryAnswer answer = q.getAnswer(0);
    q.close();
    final Set<KbTerm> sources = answer.getSources();
    assertFalse("This code should never be run, due to the exception that gets thrown.", true);
  }

}
