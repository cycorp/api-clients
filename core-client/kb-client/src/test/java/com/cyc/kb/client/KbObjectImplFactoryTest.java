package com.cyc.kb.client;

/*
 * #%L
 * File: KbObjectImplFactoryTest.java
 * Project: KB Client
 * %%
 * Copyright (C) 2013 - 2017 Cycorp, Inc
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
import com.cyc.base.CycAccessManager;
import com.cyc.base.cycobject.CycAssertion;
import com.cyc.base.cycobject.CycList;
import com.cyc.base.cycobject.CycObject;
import com.cyc.base.cycobject.ElMt;
import com.cyc.base.cycobject.FormulaSentence;
import com.cyc.base.cycobject.Nart;
import com.cyc.base.cycobject.Naut;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.baseclient.cycobject.CycAssertionImpl;
import com.cyc.baseclient.cycobject.FormulaSentenceImpl;
import com.cyc.kb.Assertion;
import com.cyc.kb.Context;
import com.cyc.kb.KbIndividual;
import com.cyc.kb.KbObject;
import com.cyc.kb.KbPredicate;
import com.cyc.kb.exception.CreateException;
import com.cyc.kb.exception.KbException;
import com.cyc.kb.exception.KbTypeException;
import com.cyc.session.exception.SessionException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collection;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;


public class KbObjectImplFactoryTest {

  @BeforeClass
  public static void setUp() throws Exception {
    TestConstants.ensureInitialized();
  }

  @AfterClass
  public static void tearDown() throws Exception {
  }
  
  @Test
  public void testAssertionsAreNotSentences() throws SessionException, KbException, UnknownHostException, IOException, Exception {
//    FormulaSentence istSentence = FormulaSentenceImpl.makeCycSentence("(ist LogicalTruthMt (isa Collection Collection))");
    CycAccess cyc = CycAccessManager.getCurrentAccess();
    FormulaSentence cycSentence = FormulaSentenceImpl.makeCycSentence(cyc, "(isa Collection Collection)");
    ElMt ltMt = CycAccessManager.getCurrentAccess().getObjectTool().makeElMt("LogicalTruthMt");
    CycAssertion cycAssert = new CycAssertionImpl(cycSentence, ltMt);
    Assertion a = AssertionImpl.get(cycAssert); // TODO: since this is failing, it looks like the getter doesn't properly accept ist sentences
    KbObject s = new SentenceImpl(a.getCore());
    assertNotSame("Got the same object for " + cycSentence + " as both an Assertion and a Sentence.", s, a);
    assertTrue ("Got a " + s.getClass() + " when expecting a SentenceImpl", s instanceof SentenceImpl);
    assertTrue ("Got a " + a.getClass() + " when expecting an Assertion", a instanceof Assertion);
  }

  @Test
  public void testRuleCanonicalizationOKForCaches() throws SessionException, KbException, UnknownHostException, IOException, Exception {
    CycAccess cyc = CycAccessManager.getCurrentAccess();
    ElMt coreCyclMt = cyc.getObjectTool().makeElMt("CoreCycLMt");
    Context coreCyclMtContext = ContextImpl.get(coreCyclMt);
    FormulaSentence cycSentence = FormulaSentenceImpl.makeCycSentence(cyc, "(implies (and "
            + "(natFunction ?NAT ?FUNCTION) "
            + "(resultIsa ?FUNCTION ?COL)) "
            + "(isa ?NAT ?COL))");
    Assertion a = AssertionImpl.get(new SentenceImpl(cycSentence), coreCyclMtContext); 
    FormulaSentence cycSentence2 = FormulaSentenceImpl.makeCycSentence(cyc, "(implies (and "
            + "(natFunction ?NAT2 ?FUNCTION) "
            + "(resultIsa ?FUNCTION ?COL)) "
            + "(isa ?NAT2 ?COL))");
    Assertion a2 = AssertionImpl.get(new SentenceImpl(cycSentence2), coreCyclMtContext);
    assertSame("Got different rules back for what should be the same assertion.", a, a2);
  }
  
  @Test
  public void testNartAndNautWithSameFormula() throws KbException, UnknownHostException, IOException, Exception {
    Naut naut = CycAccessManager.getCurrentAccess().getObjectTool().makeCycNaut("(#$AirForceFn #$France)");
    Nart nart = CycAccessManager.getCurrentAccess().getLookupTool().getCycNartFromCons(naut.toCycList());
    KbIndividual nartInd = KbIndividualImpl.get(nart);
    KbIndividual nautInd = KbIndividualImpl.get(naut);
    assertSame("Got the different KBIndividuals back for what should be the same NAT.", nartInd, nautInd);
  }

  
  @Test
  public void testAlwaysGetTightestType() throws KbException {
    KbCollectionImpl c1 = KbCollectionImpl.get("Dog");
    KbCollectionImpl c2 = FirstOrderCollectionImpl.get("Dog");
    assertSame("Didn't get the same when looking for Dog as a KBCollection and a FirstOrderCollection", c1, c2);

  }

  @Test
  public void testCacheNoCollisions() throws Exception {
    Context testVocabularyCtx = ContextImpl.get("TestVocabularyMt");
    KbPredicate testQuerySpecificationPred = KbPredicateImpl.get("testQuerySpecification");
    KbIndividual q1 = KbIndividualImpl.get("BELLAMBGIT-ProblemHasNoUnfinishedEventNodes");
    KbIndividual q2 = KbIndividualImpl.get("BELLAMBGIT-GraphContainsNoTwinSiblings");

    assertNotEquals(q1, q2);
    
    Collection<KbIndividual> querySpecCol1 = testQuerySpecificationPred.getValuesForArgPosition(q1, 1, 2, testVocabularyCtx);
    Collection<KbIndividual> querySpecCol2 = testQuerySpecificationPred.getValuesForArgPosition(q2, 1, 2, testVocabularyCtx);

    assertNotEquals(querySpecCol1, querySpecCol2);
  }
  
  @Test
  public void testTightenCycObject_FormulaSentence() throws SessionException, CycConnectionException {
    System.out.println("testTightenCycObject_FormulaSentence");
    final CycAccess       cyc      = CycAccessManager.getCurrentAccess();
    final String          str      = "(#$objectFoundInLocation ?WHAT #$CityOfAustinTX)";
    final FormulaSentence expected = cyc.getObjectTool().makeCycSentence(str);
    final CycObject       result   = KbObjectImplFactory.tightenCycObject(expected.toCycList());
    assertEquals(expected, result);
  }
  
  @Test
  public void testTightenCycObject_Naut() throws SessionException, CycConnectionException {
    System.out.println("testTightenCycObject_Naut");
    final CycAccess cyc      = CycAccessManager.getCurrentAccess();
    final String    str      = "(#$CityNamedFn \"swaziville\" #$Swaziland)";
    final Naut      expected = cyc.getObjectTool().makeCycNaut(str);
    final CycObject result   = KbObjectImplFactory.tightenCycObject(expected.toCycList());
    assertEquals(expected, result);
  }
  
  @Test
  public void testGet_CycList_Sentence() throws SessionException, CreateException, KbTypeException {
    System.out.println("testGet_CycList_Sentence");
    final CycAccess       cyc  = CycAccessManager.getCurrentAccess();
    final String          str  = "(#$objectFoundInLocation ?WHAT #$CityOfAustinTX)";
    final FormulaSentence sent = cyc.getObjectTool().makeCycSentence(str);
    final CycList         list = sent.toCycList();
    assertNotEquals(sent, list);
    assertFalse(sent instanceof CycList);
    assertFalse(list instanceof FormulaSentence);
    final KbObject expected = KbObjectImplFactory.get(sent, KbObjectImpl.class);
    final KbObject result   = KbObjectImplFactory.get(list, KbObjectImpl.class);
    assertEquals(expected, result);
  }
  
  @Test
  public void testGet_CycList_Naut() throws SessionException, CreateException, KbTypeException {
    System.out.println("testGet_CycList_Naut");
    final CycAccess cyc  = CycAccessManager.getCurrentAccess();
    final String    str  = "(#$CityNamedFn \"swaziville\" #$Swaziland)";
    final Naut      naut = cyc.getObjectTool().makeCycNaut(str);
    final CycList   list = naut.toCycList();
    assertNotEquals(naut, list);
    assertFalse(naut instanceof CycList);
    assertFalse(list instanceof Naut);
    final KbObject expected = KbObjectImplFactory.get(naut, KbObjectImpl.class);
    final KbObject result   = KbObjectImplFactory.get(list, KbObjectImpl.class);
    assertEquals(expected, result);
  }
  
  

}
