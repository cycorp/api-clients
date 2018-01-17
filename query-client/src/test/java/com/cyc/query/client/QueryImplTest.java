package com.cyc.query.client;

/*
 * #%L
 * File: QueryImplTest.java
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
import com.cyc.Cyc;
import com.cyc.base.cycobject.CycAssertion;
import com.cyc.base.cycobject.CycConstant;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.baseclient.CycObjectFactory;
import com.cyc.baseclient.inference.params.OpenCycInferenceParameterEnum;
import com.cyc.kb.Fact;
import com.cyc.kb.KbCollection;
import com.cyc.kb.KbIndividual;
import com.cyc.kb.KbObject;
import com.cyc.kb.Sentence;
import com.cyc.kb.Variable;
import com.cyc.kb.exception.CreateException;
import com.cyc.kb.exception.DeleteException;
import com.cyc.kb.exception.KbException;
import com.cyc.kb.exception.KbRuntimeException;
import com.cyc.kb.exception.KbTypeException;
import com.cyc.nl.Paraphrase;
import com.cyc.nl.ParaphraserFactory;
import com.cyc.query.InferenceStatus;
import com.cyc.query.InferenceSuspendReason;
import com.cyc.query.ParaphrasedQueryAnswer;
import com.cyc.query.Query;
import com.cyc.query.QueryAnswer;
import com.cyc.query.QueryAnswers;
import com.cyc.query.QueryListener;
import com.cyc.query.QueryResultSet;
import com.cyc.query.exception.QueryConstructionException;
import com.cyc.session.exception.SessionCommunicationException;
import com.cyc.session.exception.SessionConfigurationException;
import com.cyc.session.exception.SessionInitializationException;
import com.cyc.session.exception.UnsupportedCycOperationException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.cyc.Cyc.Constants.EVERYTHING_PSC;
import static com.cyc.Cyc.Constants.INFERENCE_PSC;
import static com.cyc.Cyc.Constants.UV_MT;
import static com.cyc.query.client.QueryImpl.QUERY_LOADER_REQUIREMENTS;
import static com.cyc.query.client.QueryTestConstants.emu;
import static com.cyc.query.client.QueryTestConstants.zebra;
import static com.cyc.query.client.TestUtils.assumeCycSessionRequirements;
import static com.cyc.query.client.TestUtils.getCyc;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class QueryImplTest {

  private static final String QUERY_STRING_ASSEMBLING = "(#$disjointWith #$Assembling ?COLL)";
  private static final String QUERY_STRING_ABES_PRESIDENT = "(#$isa #$AbrahamLincoln #$UnitedStatesPresident)";
  private static final String QUERY_STRING_CONDITIONAL = "(implies " + QUERY_STRING_ASSEMBLING + " " + QUERY_STRING_ABES_PRESIDENT + ")";
  private static final String QUERY_STRING_EXISTENTIAL = "(thereExists ?COLL " + QUERY_STRING_ASSEMBLING + ")";
  private static final String QUERY_STRING_MULTIEXISTENTIAL = "(thereExistVars (?COLL) " + QUERY_STRING_ASSEMBLING + ")";
  private Query currentQuery = null;
  private QueryResultSet currentResults = null;

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
    if (currentQuery != null) {
      currentQuery.close();
    }
  }

  @Test
  public void testGetInferenceIdentifier() throws IOException, QueryConstructionException, SessionCommunicationException {
    System.out.println("testGetInferenceIdentifier");
    currentQuery = Query.get(testConstants().queryAnimals, INFERENCE_PSC);
    currentQuery.retainInference();
    assertNull(currentQuery.getInferenceIdentifier());
    currentQuery.addListener(new QueryListener() {
      int problemStoreID;

      @Override
      public void notifyInferenceCreated(Query query) {
        try {
          assertNotNull(currentQuery.getInferenceIdentifier());
          problemStoreID = currentQuery.getInferenceIdentifier().getProblemStoreId();
          System.out.println("Problem store ID: " + problemStoreID);
          assertTrue("Got problem store ID " + problemStoreID, problemStoreID > 1);
        } catch (SessionCommunicationException ex) {
          throw new RuntimeException(ex);
        }
      }

      @Override
      public void notifyInferenceStatusChanged(InferenceStatus oldStatus, InferenceStatus newStatus, InferenceSuspendReason suspendReason, Query query) {
        System.out.println(oldStatus + " -> " + newStatus);
      }

      @Override
      public void notifyInferenceAnswersAvailable(Query query, List<QueryAnswer> newAnswers) {
        try {
          final int problemStoreID1 = currentQuery.getInferenceIdentifier().getProblemStoreId();
          assertEquals("Inference answers available; problem store ID now " + problemStoreID1,
                  problemStoreID, problemStoreID1);
        } catch (SessionCommunicationException ex) {
          throw new RuntimeException(ex);
        }
      }

      @Override
      public void notifyInferenceTerminated(Query query, Exception e) {
        try {
          final int problemStoreID1 = currentQuery.getInferenceIdentifier().getProblemStoreId();
          assertEquals("Inference terminated; problem store ID now " + problemStoreID1,
                  problemStoreID, problemStoreID1);
        } catch (SessionCommunicationException ex) {
          throw new RuntimeException(ex);
        }
      }
    });
    currentQuery.performInference();
    assertNotNull(currentQuery.getInferenceIdentifier());
  }

  @Test
  public void testQueryString() throws QueryConstructionException {
    String queryStr = QUERY_STRING_ASSEMBLING;
    currentQuery = Query.get(queryStr);
    QueryResultSet results = currentQuery.getResultSet();
    while (results.next()) {
      results.getObject("?COLL", KbObject.class);
    }
  }

  @Test
  public void testBooleanQueryString() throws QueryConstructionException {
    String queryStr = QUERY_STRING_ABES_PRESIDENT;
    currentQuery = Query.get(queryStr);
    assertTrue(currentQuery.isTrue());
  }

  @Test
  public void testCycAssertionAsBinding() throws KbException, QueryConstructionException, SessionCommunicationException, CycConnectionException {
    final Variable var = Variable.get("AS");
    currentQuery = Query.get(Sentence.get(testConstants().assertionSentence, var,
            testConstants().genlsAnimalX), INFERENCE_PSC);
    currentQuery.setMaxAnswerCount(1);
    final Object binding = ((QueryImpl) currentQuery).getAnswerCyc(0).getBinding(CycObjectFactory.makeCycVariable(var.getName()));
    assertTrue("Wanted a CycAssertion, got " + binding.getClass().getSimpleName(),
            binding instanceof CycAssertion);
  }

  @Test
  public void testFactAsBinding() throws KbException, QueryConstructionException, SessionCommunicationException {
    final Variable var = Variable.get("AS");
    currentQuery = Query.get(Sentence.get(testConstants().assertionSentence, var,
            testConstants().genlsAnimalX), INFERENCE_PSC);
    currentQuery.setMaxAnswerCount(1);
    final Object binding = currentQuery.getAnswer(0).getBinding(var);
    assertTrue("Wanted a Fact, got " + binding.getClass().getSimpleName(),
            binding instanceof Fact);
  }

  @Test
  public void testQueryStringString() throws QueryConstructionException {
    currentResults = Query.get(testConstants().whatIsAbe.toString(), testConstants().peopleDataMt.toString()).getResultSet();
    while (currentResults.next()) {
      System.out.println("All types: " + currentResults.getObject("?TYPE", KbCollection.class));
    }
  }

  private QueryTestConstants testConstants() throws KbRuntimeException {
    return QueryTestConstants.getInstance();
  }

  @Test
  public void testContinuableQuery() throws QueryConstructionException {
    System.out.println("testContinuableQuery");
    currentQuery = Query.get(testConstants().whatIsAbe, testConstants().peopleDataMt);
    currentQuery.setInferenceMode(OpenCycInferenceParameterEnum.OpenCycInferenceMode.MINIMAL_MODE);
    currentQuery.setMaxAnswerCount(1);
    currentQuery.getInferenceParameters().setContinuable(true);
    assertTrue("Query not continuable.", currentQuery.isContinuable());
    int answerCount = currentQuery.getAnswerCount();
    assertEquals("Expected one answer, got " + answerCount, 1, answerCount);
    currentQuery.continueQuery();
    int updatedAnswerCount = currentQuery.getAnswerCount();
    while (updatedAnswerCount > answerCount) {
      answerCount = updatedAnswerCount;
      currentQuery.continueQuery();
      updatedAnswerCount = currentQuery.getAnswerCount();
    }
    assertTrue("Found only " + answerCount + " answers.", answerCount > 1);
  }

  @Test
  public void testNonContinuableQuery() throws QueryConstructionException {
    System.out.println("test setContinuable(false)");
    currentQuery = Query.get(testConstants().whatIsAbe, testConstants().peopleDataMt);
    currentQuery.setMaxAnswerCount(1);
    currentQuery.setInferenceMode(OpenCycInferenceParameterEnum.OpenCycInferenceMode.SHALLOW_MODE);
    currentQuery.getInferenceParameters().setContinuable(false);
    assertFalse("Query parameters are continuable.", currentQuery.getInferenceParameters().isContinuable());
    currentQuery.continueQuery();
    assertFalse("Query is continuable.", currentQuery.isContinuable());
  }

  @Test
  public void testQueryStringStringString() throws QueryConstructionException {
    String queryStr = testConstants().whatIsAbe.toString();

    currentResults = Query.get(queryStr, testConstants().peopleDataMt.toString(),
            ":INFERENCE-MODE :MINIMAL :MAX-TIME 1 :MAX-NUMBER 12").getResultSet();
    while (currentResults.next()) {
      System.out.println("TYPE: " + currentResults.getObject("?TYPE", KbObject.class));
    }
  }

  /**
   * Test of getId method, of class QueryImpl.
   *
   * @throws com.cyc.query.exception.QueryConstructionException
   * @throws com.cyc.kb.exception.KbException
   * @throws com.cyc.session.exception.SessionCommunicationException
   */
  @Test
  public void testGetId() throws QueryConstructionException, KbException, SessionCommunicationException {
    System.out.println("getId and saveAs");
    currentQuery = Query.get(QUERY_STRING_ASSEMBLING);
    final KbIndividual id = currentQuery.saveAs("TestQuery-AssemblingSlots");
    try {
      assertEquals(id, currentQuery.getId());
    } finally {
      id.delete();
    }
  }

  /**
   * Test of load method, of class QueryImpl.
   *
   * @throws com.cyc.query.exception.QueryConstructionException
   * @throws com.cyc.kb.exception.KbException
   * @throws com.cyc.session.exception.SessionCommunicationException
   */
  @Test
  public void testLoad() throws QueryConstructionException, KbException, SessionCommunicationException, UnsupportedCycOperationException {
    System.out.println("load");
    //Query conceptFinder = QueryImpl.load(new KBIndividual("AURORAQuery-PredictAllFeaturesFromReifiedVideosUsingThisFeatureSet"));
    assumeCycSessionRequirements(QUERY_LOADER_REQUIREMENTS);
    currentQuery = Query.get(QUERY_STRING_ASSEMBLING);
    final KbIndividual id = currentQuery.saveAs("TestQuery-AssemblingSlots");
    try {
      final Query loadedQuery = Query.get(id);
      assertEquals("Query contexts are different.", currentQuery.getContext(),
              loadedQuery.getContext());
      assertEquals("Query sentences are different.", ((QueryImpl) currentQuery).getQuerySentenceCyc(),
              ((QueryImpl) loadedQuery).getQuerySentenceCyc());
    } finally {
      id.delete();
    }
  }

  @Test
  public void testKBQIndexical() throws QueryConstructionException, DeleteException, KbException, SessionCommunicationException, UnsupportedCycOperationException {
    System.out.println("testKBQIndexical");
    assumeCycSessionRequirements(QUERY_LOADER_REQUIREMENTS);
    currentQuery = Query.get(testConstants().theAnimalIsAnAnimal, INFERENCE_PSC);
    currentQuery.setMaxAnswerCount(10);
    KbIndividual z = currentQuery.saveAs("TestKBQueryIndexical-2012-01-16");

    try {
      Map<KbObject, Object> indexicals = new HashMap<>();
      indexicals.put(testConstants().theAnimal,
              CycObjectFactory.makeCycVariable("X"));

      Query query = Query.get(KbIndividual.get("TestKBQueryIndexical-2012-01-16"),
              indexicals);
      currentResults = query.getResultSet();
      while (currentResults.next()) {
        System.out.println("Animal: " + currentResults.getObject("?X", KbIndividual.class));
      }
    } finally {
      currentQuery.getId().delete();
    }
  }

  @Test
  public void testKBQIndexicalKBObject() throws QueryConstructionException, KbException, SessionCommunicationException, UnsupportedCycOperationException {
    System.out.println("testKBQIndexicalKBObject");
    assumeCycSessionRequirements(QUERY_LOADER_REQUIREMENTS);
    currentQuery = Query.get(testConstants().theAnimalIsAnAnimal, INFERENCE_PSC);
    currentQuery.setMaxAnswerCount(10);
    currentQuery.saveAs("TestKBQueryKBObject-2014-04-1");

    try {
      Map<KbObject, Object> indexicals = new HashMap<>();
      indexicals.put(testConstants().theAnimal, Variable.get("X"));

      currentResults = Query.get(KbIndividual.get("TestKBQueryKBObject-2014-04-1"),
              indexicals).getResultSet();
      while (currentResults.next()) {
        System.out.println("Animal: " + currentResults.getObject("?X", KbIndividual.class));
      }
    } finally {
      currentQuery.getId().delete();
    }
  }

  @Test
  public void testKBQIndexicalString() throws QueryConstructionException, KbException, SessionCommunicationException, UnsupportedCycOperationException {
    System.out.println("testKBQIndexicalString");
    assumeCycSessionRequirements(QUERY_LOADER_REQUIREMENTS);
    currentQuery = Query.get(testConstants().theAnimalIsAnAnimal, INFERENCE_PSC);
    currentQuery.setMaxAnswerCount(10);
    currentQuery.saveAs("TestKBQuery-Vijay-2012-01-16");
    try {
      // The bound getQueryService is converted to an unbound getQueryService.
      // The "(#$TheFn #$Dog)" in the getQueryService is replaced with "?X".
      Map<String, String> indexicals = new HashMap<>();
      indexicals.put(testConstants().theAnimal.toString(), "?X");

      currentResults = Query.get("TestKBQuery-Vijay-2012-01-16", indexicals).getResultSet();

      while (currentResults.next()) {
        System.out.println("Animal: " + currentResults.getObject("?X", KbIndividual.class));
      }
    } finally {
      currentQuery.getId().delete();
    }
  }

  @Test
  public void testKBQIndexicalVarToConst()
          throws CycConnectionException, SessionConfigurationException, 
          SessionCommunicationException, SessionInitializationException, 
          QueryConstructionException, KbException {
    System.out.println("testKBQIndexicalVarToConst");
    assumeCycSessionRequirements(QUERY_LOADER_REQUIREMENTS);
    currentQuery = Sentence.and(testConstants().xIsAnAnimal, testConstants().yOwnsX)
            .toQuery(EVERYTHING_PSC);
    currentQuery.saveAs("TestKBQueryAnimalOwners-Vijay-2012-01-16");
    try {
      Map<KbObject, Object> indexicals = new HashMap<>();
      indexicals.put(Variable.get("Y"), testConstants().abrahamLincoln);
      try (Query loadedQuery = Query.get(KbIndividual.get("TestKBQueryAnimalOwners-Vijay-2012-01-16"), indexicals)) {
        System.out.println("Result of replacing variable: " + loadedQuery);
        currentResults = loadedQuery.getResultSet();
        System.out.println("Result set: " + currentResults);
        while (currentResults.next()) {
          System.out.println("Answer " + currentResults.getObject("?X", KbIndividual.class));
        }
      }
    } finally {
      currentQuery.getId().delete();
    }
  }

  @Test
  //@TODO Replace this with a test using vocabulary in OpenCyc (or at least RCyc).
  public void testKBQIndexicalAurora() 
          throws CycConnectionException, SessionConfigurationException, 
          SessionCommunicationException, SessionInitializationException, 
          QueryConstructionException, KbException {
    if (!getCyc().isOpenCyc()
            && (getCyc().getLookupTool().find("AuroraConceptIDSourceStore") instanceof CycConstant)) {
      System.out.println("testKBQIndexicalAurora");
      Map<KbObject, Object> binding = new HashMap<>();
      binding.put(Variable.get("?VIDEO-ID"), 27850);
      //Idiotically, need #$ here
      binding.put(
              KbIndividual.findOrCreate("(#$TheFn #$AuroraConceptIDSourceStore)"),
              getCyc().getLookupTool().getKnownFortByName("MED12-SIN-Concept-List"));
      currentQuery = Query.get(KbIndividual.get(
              "AURORAQuery-PredictAllFeaturesFromReifiedVideosUsingThisFeatureSet"),
              binding);
      currentQuery.setMaxTime(30).setMaxAnswerCount(10);
      currentResults = currentQuery.getResultSet();
      while (currentResults.next()) {
        System.out.println("Answer " + currentResults.getObject("?READABLE",
                String.class));
      }
    }
  }

  /**
   * Test of getCategories method, of class QueryImpl.
   *
   * @throws java.io.IOException
   * @throws com.cyc.query.exception.QueryConstructionException
   */
  @Test
  public void testGetCategories() throws IOException, QueryConstructionException {
    System.out.println("getCategories");
    currentQuery = Query.get(QUERY_STRING_ASSEMBLING);
    assertTrue(currentQuery.getCategories().isEmpty());
  }

  /**
   * Test of addCategory method, of class QueryImpl.
   *
   * @throws java.io.IOException
   * @throws com.cyc.query.exception.QueryConstructionException
   */
  @Test
  public void testAddCategory() throws IOException, QueryConstructionException {
    System.out.println("addCategory");
    currentQuery = Query.get(QUERY_STRING_ASSEMBLING);
    String cat = "Test Queries";
    currentQuery.addCategory(cat);
    assertTrue(currentQuery.getCategories().contains(cat));
  }

  /**
   * Test of getAnswerCount method, of class QueryImpl.
   *
   * @throws com.cyc.query.exception.QueryConstructionException
   */
  @Test
  public void testGetAnswerCount() throws QueryConstructionException {
    System.out.println("getAnswerCount");
    currentQuery = Query.get(testConstants().queryAnimals, INFERENCE_PSC);
    assertEquals(2, currentQuery.getAnswerCount());
  }

  /**
   * Test of getAnswerCount method, of class QueryImpl.
   *
   * @throws com.cyc.query.exception.QueryConstructionException
   * @throws com.cyc.session.exception.SessionCommunicationException
   * @throws com.cyc.kb.exception.KbTypeException
   * @throws com.cyc.kb.exception.CreateException
   */
  @Test
  public void testGetAnswerBindings() 
          throws QueryConstructionException, SessionCommunicationException,
          KbTypeException, CreateException {
    System.out.println("getAnswerBindings");
    currentQuery = Query.get(testConstants().queryAnimals, INFERENCE_PSC);
    assertTrue(currentQuery.getAnswer(0).getBinding(Variable.get("?N")) instanceof KbObject);
    assertTrue(currentQuery.getAnswer(0).getBinding(Variable.get("?N")).equals(Cyc.getKbObject("Emu"))
            || currentQuery.getAnswer(0).getBinding(Variable.get("?N")).equals(Cyc.getKbObject("Zebra")));
    assertEquals(2, currentQuery.getAnswerCount());
  }

  /**
   * Test of getAnswerCount method, of class QueryImpl.
   *
   * @throws com.cyc.query.exception.QueryConstructionException
   * @throws com.cyc.session.exception.SessionCommunicationException
   * @throws com.cyc.kb.exception.KbTypeException
   * @throws com.cyc.kb.exception.CreateException
   */
  @Test
  public void testGetParaphrasedAnswerBindings() throws QueryConstructionException, SessionCommunicationException, KbTypeException, CreateException {
    System.out.println("getParaphrasedAnswerBindings");
    currentQuery = Query.get(testConstants().queryAnimals, INFERENCE_PSC);
    ParaphrasedQueryAnswer answer = currentQuery.getAnswer(0, ParaphraserFactory.getInstance(ParaphraserFactory.ParaphrasableType.KBOBJECT));
    assertTrue(answer.getBindingParaphrase(Variable.get("?N")) instanceof Paraphrase);
    Paraphrase paraphrasedAnswer = answer.getBindingParaphrase(Variable.get("?N"));
    assertTrue(paraphrasedAnswer.getString().equals("emu") || paraphrasedAnswer.getString().equals("zebra"));
  }

  /**
   *
   * @throws QueryConstructionException
   * @throws SessionCommunicationException
   * @throws KbException
   */
  @Test
  public void testGetAnswerSentence() throws QueryConstructionException, SessionCommunicationException, KbException {
    System.out.println("getAnswerSentence");
    currentQuery = Query.get(testConstants().genlsSpecGenls, UV_MT);
    currentQuery.setMaxAnswerCount(10);

    final Variable varGenls = Variable.get("?GENLS");
    final Variable varSpec = Variable.get("?SPEC");
    final List<QueryAnswer> answers = currentQuery.getAnswers();
    assertFalse(answers.isEmpty());

    for (QueryAnswer answer : answers) {
      KbCollection bindingGenls = answer.getBinding(varGenls);
      KbCollection bindingSpec = answer.getBinding(varSpec);
      //System.out.println(varGenls + "=" + bindingGenls + "    " + varSpec + "=" + bindingSpec);
      final Sentence sentence = currentQuery.getAnswerSentence(answer);
      final Sentence inner = sentence.getArgument(1);
      System.out.println(inner);

      KbCollection arg1 = inner.getArgument(1);
      KbCollection arg2 = inner.getArgument(2);
      assertFalse(arg1 instanceof Variable);
      assertFalse(arg2 instanceof Variable);
      assertEquals(arg1, bindingSpec);
      //System.out.println(arg1 + " == " + bindingSpec);
      assertEquals(arg2, bindingGenls);
      //System.out.println(arg2 + " == " + bindingGenls);
    }
  }

  /**
   * Test of getContext method, of class QueryImpl.
   *
   * @throws com.cyc.query.exception.QueryConstructionException
   */
  @Test
  public void testGetContext() throws QueryConstructionException {
    System.out.println("getContext");
    currentQuery = Query.get(QUERY_STRING_ASSEMBLING);
    assertEquals(INFERENCE_PSC, currentQuery.getContext());
  }

  /**
   * Test of getQuerySentence method, of class QueryImpl.
   *
   * @throws com.cyc.query.exception.QueryConstructionException
   */
  @Test
  public void testgetQuerySentenceCyc() throws QueryConstructionException {
    System.out.println("getQuerySentence");
    currentQuery = Query.get(QUERY_STRING_ASSEMBLING);
    assertEquals(QUERY_STRING_ASSEMBLING, ((QueryImpl) currentQuery).getQuerySentenceCyc().cyclify());
  }

  /**
   * Test of getQuerySentenceMainClauseCyc method, of class QueryImpl.
   *
   * @throws java.io.IOException
   * @throws com.cyc.query.exception.QueryConstructionException
   */
  @Test
  public void testgetQuerySentenceMainClauseCyc() throws IOException, QueryConstructionException {
    System.out.println("getQuerySentenceMainClauseCyc");
    currentQuery = Query.get(QUERY_STRING_ASSEMBLING);
    assertEquals(QUERY_STRING_ASSEMBLING,
            ((QueryImpl) currentQuery).getQuerySentenceMainClauseCyc().cyclify());
  }

  /**
   * Test of getQuerySentenceHypothesizedClause method, of class QueryImpl.
   *
   * @throws java.io.IOException
   * @throws com.cyc.query.exception.QueryConstructionException
   */
  @Test
  public void testGetQuerySentenceHypothesizedClause() throws IOException, QueryConstructionException {
    System.out.println("getQuerySentenceHypothesizedClause");
    currentQuery = Query.get(QUERY_STRING_CONDITIONAL);
    assertEquals(QUERY_STRING_ASSEMBLING,
            ((QueryImpl) currentQuery).getQuerySentenceHypothesizedClauseCyc().cyclify());
    assertEquals(QUERY_STRING_ABES_PRESIDENT,
            ((QueryImpl) currentQuery).getQuerySentenceMainClauseCyc().cyclify());
  }

  /**
   * Test of getMaxTime method, of class QueryImpl.
   *
   * @throws java.io.IOException
   * @throws com.cyc.query.exception.QueryConstructionException
   */
  @Test
  public void testGetMaxTime() throws IOException, QueryConstructionException {
    System.out.println("getMaxTime");
    currentQuery = Query.get(QUERY_STRING_ASSEMBLING);
    assertEquals(null, currentQuery.getMaxTime());
  }

  /**
   * Test of getMaxNumber method, of class QueryImpl.
   *
   * @throws java.io.IOException
   * @throws com.cyc.query.exception.QueryConstructionException
   */
  @Test
  public void testGetMaxNumber() throws IOException, QueryConstructionException {
    System.out.println("getMaxNumber");
    currentQuery = Query.get(QUERY_STRING_ASSEMBLING);
    assertEquals(null, currentQuery.getMaxAnswerCount());
  }

  /**
   * Test of getInferenceMode method, of class QueryImpl.
   *
   * @throws java.io.IOException
   * @throws com.cyc.query.exception.QueryConstructionException
   */
  @Test
  public void testGetInferenceMode() throws IOException, QueryConstructionException {
    System.out.println("getInferenceMode");
    currentQuery = Query.get(QUERY_STRING_ASSEMBLING);
    assertEquals(null, currentQuery.getInferenceMode());
  }

  /**
   * Test of getStatus method, of class QueryImpl.
   *
   * @throws com.cyc.query.exception.QueryConstructionException
   */
  @Test
  public void testGetStatus() throws QueryConstructionException {
    System.out.println("getStatus");
    currentQuery = Query.get(QUERY_STRING_ASSEMBLING);
    currentQuery.performInference();
    assertEquals(InferenceStatus.SUSPENDED, currentQuery.getStatus());
  }

  /**
   * Test of get method, of class QueryImpl.
   *
   * @throws com.cyc.query.exception.QueryConstructionException
   * @throws com.cyc.kb.exception.KbException
   */
  @Test
  public void testGet() throws QueryConstructionException, IllegalArgumentException, KbException {
    System.out.println("get");
    currentResults = Query.get(testConstants().queryAnimals, INFERENCE_PSC).getResultSet();
    currentResults.next();
    final KbCollection animal = (KbCollection) currentResults.getKbObject("?N", KbCollection.class);
    final List<KbCollection> emuAndZebra = Arrays.asList(emu(), zebra());
    assertTrue("Couldn't find " + animal + " (" + animal.getClass().getSimpleName()
            + ") in " + emuAndZebra, emuAndZebra.contains(animal));
  }

  /**
   * Test of isTrue method, of class QueryImpl.
   *
   * @throws com.cyc.query.exception.QueryConstructionException
   */
  @Test
  public void testIsTrue() throws QueryConstructionException {
    System.out.println("isTrue");
    currentQuery = Query.get(QUERY_STRING_ABES_PRESIDENT);
    assertTrue(currentQuery.isTrue());
  }

  /**
   * Test of isProvable method, of class QueryImpl.
   *
   * @throws com.cyc.query.exception.QueryConstructionException
   */
  @Test
  public void testIsProvable() throws QueryConstructionException {
    System.out.println("isProvable");
    currentQuery = Query.get(QUERY_STRING_ABES_PRESIDENT);
    assertTrue(currentQuery.isProvable());
    currentQuery.close();
    currentQuery = Query.get(testConstants().queryAnimals, INFERENCE_PSC);
    assertTrue(currentQuery.isProvable());
    currentQuery.getResultSet().afterLast();
    assertTrue(currentQuery.isProvable());
  }

  @Test
  public void testClearResults() 
          throws QueryConstructionException, KbException, SessionCommunicationException,
          InterruptedException {
    System.out.println("clearResults");
    currentQuery = Query.get(testConstants().whatTimeIsIt, INFERENCE_PSC);
    final Variable var = currentQuery.getQuerySentence().getArgument(2);
    final Object firstTime = currentQuery.getAnswer(0).getBinding(var);
    assertEquals(firstTime, currentQuery.getAnswer(0).getBinding(var));
    currentQuery.clearResults();
    Thread.sleep(1500);
    assertFalse(firstTime.equals(currentQuery.getAnswer(0).getBinding(var)));
  }

  /**
   * Test of next method, of class QueryImpl.
   *
   * @throws com.cyc.query.exception.QueryConstructionException
   */
  @Test
  public void testNext() throws QueryConstructionException {
    System.out.println("next");
    currentResults = Query.get(testConstants().queryAnimals, INFERENCE_PSC).getResultSet();
    assertTrue(currentResults.next());
  }

  /**
   * Test of close method, of class QueryImpl.
   *
   * @throws java.io.IOException
   * @throws com.cyc.query.exception.QueryConstructionException
   */
  @Test
  public void testClose() throws IOException, QueryConstructionException {
    System.out.println("close");
    currentQuery = Query.get(testConstants().queryAnimals, INFERENCE_PSC);
    currentQuery.close();
  }

  /**
   * Test of getResultSet method, of class QueryImpl.
   *
   * @throws com.cyc.query.exception.QueryConstructionException
   */
  @Test
  public void testGetResultSet() throws QueryConstructionException {
    System.out.println("getResultSet");
    currentQuery = Query.get(testConstants().queryAnimals, INFERENCE_PSC);
    currentQuery.getResultSet();
  }

  /**
   * Test of getQueryVariablesCyc method, of class QueryImpl.
   *
   * @throws java.io.IOException
   * @throws com.cyc.query.exception.QueryConstructionException
   * @throws com.cyc.kb.exception.KbException
   */
  @Test
  public void testGetQueryVariables() throws IOException, QueryConstructionException, KbException {
    System.out.println("getQueryVariablesCyc");
    currentQuery = Query.get(testConstants().queryAnimals, INFERENCE_PSC);
    assertTrue(currentQuery.getQueryVariables().contains(Variable.get("N")));
    currentQuery.close();
    currentQuery = Query.get(QUERY_STRING_CONDITIONAL);
    assertTrue(currentQuery.getQueryVariables().isEmpty());
    currentQuery.close();
    currentQuery = Query.get(QUERY_STRING_EXISTENTIAL);
    assertTrue(currentQuery.getQueryVariables().isEmpty());
    currentQuery.close();
    currentQuery = Query.get(QUERY_STRING_MULTIEXISTENTIAL);
    assertTrue(currentQuery.getQueryVariables().isEmpty());
  }

  @Test
  public void testGetUnresolvedIndexicals()
          throws QueryConstructionException, KbException, SessionCommunicationException {
    // FIXME: find a better example! - nwinant, 2017-03-28
    System.out.println("testGetIndexicals()");
    final KbIndividual kbqId = KbIndividual.get("#$SNCQuery-GetMostRecentFiscalQuarterAndYear");
    final Query q = Query.get(kbqId);
    final Set<KbObject> expected = new HashSet(q.getQuerySentence().getIndexicals());
    final Set<KbObject> results = q.getUnresolvedIndexicals();
    assertEquals(expected, results);
    assertNotNull(results);
  }
  
  @Test
  public void testQueryAnswerVariableBindingSets() throws Exception {
    final Query q = Query.get(
            "(and"
            + " (integerBetween 8 ?X 12)"
            + " (or (unknownSentence (greaterThan ?X 10))"
            + "(equals ?Y True)))");
    QueryAnswers answers = q.getAnswers();
    answers.printAnswersTable(System.out, true);
    assertTrue(q.getAnswers().size() == 8);
  }

}
