/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyc.query.client;

/*
 * #%L
 * File: AsynchronousQueryTest.java
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
import com.cyc.base.cycobject.FormulaSentence;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.base.exception.CycTimeOutException;
import com.cyc.baseclient.CycObjectFactory;
import com.cyc.baseclient.cycobject.ArgPositionImpl;
import com.cyc.baseclient.cycobject.FormulaSentenceImpl;
import com.cyc.baseclient.inference.params.DefaultInferenceParameters;
import com.cyc.baseclient.inference.params.OpenCycInferenceParameterEnum.OpenCycInferenceMode;
import com.cyc.kb.ArgPosition;
import com.cyc.kb.Sentence;
import com.cyc.kb.Variable;
import com.cyc.kb.exception.KbException;
import com.cyc.query.InferenceStatus;
import com.cyc.query.InferenceSuspendReason;
import com.cyc.query.Query;
import com.cyc.query.QueryAnswer;
import com.cyc.query.QueryListener;
import com.cyc.query.exception.QueryConstructionException;
import com.cyc.query.metrics.InferenceMetric;
import com.cyc.query.metrics.InferenceMetricsValues;
import com.cyc.query.parameters.InferenceParameters;
import com.cyc.query.parameters.StandardInferenceMetric;
import com.cyc.session.exception.OpenCycUnsupportedFeatureException;
import com.cyc.session.exception.SessionCommunicationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.cyc.Cyc.Constants.INFERENCE_PSC;
import static com.cyc.query.client.TestUtils.X;
import static com.cyc.query.client.TestUtils.X_ISA_EMU;
import static com.cyc.query.client.TestUtils.assumeNotOpenCyc;
import static com.cyc.query.client.TestUtils.closeTestQuery;
import static com.cyc.query.client.TestUtils.constructXIsaBirdQuery;
import static com.cyc.query.client.TestUtils.currentQuery;
import static com.cyc.query.client.TestUtils.getCyc;
import static com.cyc.query.client.TestUtils.xIsaBird;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author baxter
 */
public class AsynchronousQueryTest {

  private InferenceParameters defaultParams;

  public AsynchronousQueryTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @Before
  public void setUp() {
    defaultParams = new DefaultInferenceParameters(getCyc()); 
  }

  @After
  public void tearDown() {
    closeTestQuery();
  }

  /**
   * Test of start method, of class Query.
   *
   */
  @Test
  public void testStart() throws QueryConstructionException, InterruptedException, SessionCommunicationException {
    System.out.println("start");
    currentQuery = constructXIsaBirdQuery();
    final InferenceStatus status = currentQuery.getStatus();
    currentQuery.start();
    waitForQueryToFinish();
    System.out.println(currentQuery.getStatus());
    assertFalse("Wrong query status after starting and waiting.", status.equals(currentQuery.getStatus()));
  }

  /**
   * Test of stop method, of class Query.
   *
   * @throws java.lang.InterruptedException
   * @throws com.cyc.query.exception.QueryConstructionException
   * @throws com.cyc.session.exception.SessionCommunicationException
   */
  @Test
  public void testStop() throws InterruptedException, QueryConstructionException, SessionCommunicationException {
    System.out.println("stop");
    currentQuery = constructXIsaBirdQuery();
    currentQuery.start();
    currentQuery.stop(1);
    waitForQueryToFinish();
    final InferenceStatus status = currentQuery.getStatus();
    System.out.println(currentQuery.getStatus());
    assertFalse("Wrong query status after starting, stopping and waiting.", status.equals(InferenceStatus.RUNNING));
  }

  /**
   * Test of continueQuery method, of class Query.
   *
   * @throws java.lang.InterruptedException
   * @throws com.cyc.query.exception.QueryConstructionException
   * @throws com.cyc.session.exception.SessionCommunicationException
   * @throws CycConnectionException
   */
  @Test
  public void testContinueQuery() throws InterruptedException, QueryConstructionException, SessionCommunicationException {
    System.out.println("continueQuery");
    currentQuery = constructXIsaBirdQuery();
    currentQuery.setInferenceMode(OpenCycInferenceMode.MAXIMAL_MODE);
    System.out.println(currentQuery.getStatus());
    currentQuery.start();
    System.out.println(currentQuery.getStatus());
    currentQuery.stop(1);
    waitForQueryToFinish();
    System.out.println(currentQuery.getStatus());
    currentQuery.continueQuery();
    System.out.println(currentQuery.getStatus());
  }

  /**
   * Test of addListener method, of class Query.
   *
   * @throws java.lang.InterruptedException
   * @throws com.cyc.query.exception.QueryConstructionException
   * @throws com.cyc.session.exception.SessionCommunicationException
   */
  @Test
  public void testAddListener() throws InterruptedException, QueryConstructionException, SessionCommunicationException {
    System.out.println("addListener");
    final TestQueryListener testQueryListener = new TestQueryListener();
    {
      currentQuery = constructXIsaBirdQuery();
      currentQuery.setMaxTime(1);
      currentQuery.addListener(testQueryListener).start();
      final long startMillis = System.currentTimeMillis();
      while (testQueryListener.terminated == false
              && System.currentTimeMillis() - startMillis < 1000) {
        Thread.sleep(10);
      }
      assertTrue(testQueryListener.terminated);
    }
    {//Try with performInference()
      currentQuery.close();
      currentQuery = constructXIsaBirdQuery();
      currentQuery.setMaxTime(1);
      currentQuery.addListener(testQueryListener).performInference();
      final long startMillis = System.currentTimeMillis();
      while (testQueryListener.terminated == false
              && System.currentTimeMillis() - startMillis < 1000) {
        Thread.sleep(10);
      }
      assertTrue(testQueryListener.terminated);
    }
  }

  /**
   * Test of removeQueryVariable and addQueryVariable methods, of class Query.
   * @throws com.cyc.query.exception.QueryConstructionException
   * @throws com.cyc.kb.exception.KbException
   */
  @Test
  public void testAddAndRemoveQueryVariable() throws QueryConstructionException, KbException {
    System.out.println("removeQueryVariable");
    currentQuery = constructXIsaBirdQuery();
    assertTrue(currentQuery.getQueryVariables().contains(X));
    currentQuery.removeQueryVariable(X);
    assertFalse(currentQuery.getQueryVariables().contains(X));
    currentQuery.addQueryVariable(X);
    assertTrue(currentQuery.getQueryVariables().contains(X));
  }

  /**
   * Test of setQueryVariables method, of class Query.
   * @throws com.cyc.query.exception.QueryConstructionException
   * @throws com.cyc.kb.exception.KbException
   */
  @Test
  public void testSetQueryVariables() throws QueryConstructionException, KbException {
    System.out.println("setQueryVariables");
    currentQuery = constructXIsaBirdQuery();
    assertTrue(currentQuery + " does not contain " + X, currentQuery.getQueryVariables().contains(X));
    final List<Variable> vars = new ArrayList<Variable>();
    currentQuery.setQueryVariables(vars);
    assertFalse(currentQuery.getQueryVariables().contains(X));
    vars.add(X);
    currentQuery.setQueryVariables(vars);
    assertTrue(currentQuery.getQueryVariables().contains(X));
  }

  /**
   * Test of setQuerySentenceMainClause method, of class Query.
   * @throws com.cyc.query.exception.QueryConstructionException
   * @throws com.cyc.kb.exception.KbException
   */
  @Test
  public void testSetQuerySentenceMainClause() throws QueryConstructionException, KbException {
    System.out.println("setQuerySentenceMainClause");
    currentQuery = constructXIsaBirdQuery().setQuerySentenceMainClause(X_ISA_EMU);
    assertEquals(X_ISA_EMU, currentQuery.getQuerySentenceMainClause());
  }

  /**
   * Test of setQuerySentenceHypothesizedClause method, of class Query.
   * @throws com.cyc.query.exception.QueryConstructionException
   * @throws com.cyc.kb.exception.KbException
   */
  @Test
  public void testSetQuerySentenceHypothesizedClause() throws QueryConstructionException, KbException {
    System.out.println("setQuerySentenceHypothesizedClause");
    currentQuery = constructXIsaBirdQuery().
            setQuerySentenceHypothesizedClause(X_ISA_EMU);
    assertEquals(X_ISA_EMU, currentQuery.getQuerySentenceHypothesizedClause());
    currentQuery.setQuerySentenceHypothesizedClause(xIsaBird());
    assertEquals(xIsaBird(), currentQuery.getQuerySentenceHypothesizedClause()
    );
  }
  
  /**
   * Test of findRedundantClauses method, of class Query.
   *
   * @throws com.cyc.query.exception.QueryConstructionException
   * @throws com.cyc.kb.exception.KbException
   * @throws com.cyc.session.exception.SessionCommunicationException
   */
  @Test
  public void testFindRedundantClauses() throws QueryConstructionException, KbException, SessionCommunicationException, OpenCycUnsupportedFeatureException {
    assumeNotOpenCyc();
    currentQuery = Query.get(Sentence.and(xIsaBird(), X_ISA_EMU), INFERENCE_PSC);
    final Collection<Collection<Sentence>> redundantClauses = currentQuery.findRedundantClauses();
    assertFalse(redundantClauses.isEmpty());
    final Collection<Sentence> oneSet = redundantClauses.iterator().next();
    assertTrue(oneSet.contains(xIsaBird()) && oneSet.contains(X_ISA_EMU));
  }

  /**
   * Test of findUnconnectedClauses method, of class QueryImpl.
   *
   * @throws com.cyc.query.exception.QueryConstructionException
   * @throws com.cyc.kb.exception.KbException
   * @throws com.cyc.session.exception.SessionCommunicationException
   */
  @Test
  public void testFindUnconnectedClauses() throws QueryConstructionException, KbException, SessionCommunicationException, OpenCycUnsupportedFeatureException {
    System.out.println("findUnconnectedClauses");
    assumeNotOpenCyc();
    currentQuery = Query.get(Sentence.and(xIsaBird(), X_ISA_EMU), INFERENCE_PSC);
    final Collection<ArgPosition> unconnectedClauses = currentQuery.findUnconnectedClauses();
    assertTrue(unconnectedClauses.isEmpty());
    FormulaSentence newSentence = ((QueryImpl)currentQuery).getQuerySentenceMainClauseCyc().deepCopy();
    newSentence.setSpecifiedObject(ArgPositionImpl.ARG1.deepCopy().extend(
            ArgPositionImpl.ARG1),
            CycObjectFactory.makeCycVariable("Y"));
    Sentence s = Sentence.get(newSentence);
    currentQuery.setQuerySentenceMainClause(s);
    unconnectedClauses.addAll(currentQuery.findUnconnectedClauses());
    assertFalse(unconnectedClauses.isEmpty());
  }

  /**
   * Test of merge method, of class QueryImpl.
   *
   * @throws com.cyc.kb.exception.KbException
   * @throws com.cyc.session.exception.SessionCommunicationException
   * @throws com.cyc.query.exception.QueryConstructionException
   */
  @Test
  public void testMerge() throws KbException, QueryConstructionException, SessionCommunicationException, OpenCycUnsupportedFeatureException {
    System.out.println("merge");
    assumeNotOpenCyc();
    currentQuery = Query.get(xIsaBird(), INFERENCE_PSC, defaultParams);
    Query otherQuery = Query.get(X_ISA_EMU, INFERENCE_PSC, defaultParams);
    currentQuery = currentQuery.merge(otherQuery);
    assertTrue(((FormulaSentenceImpl)currentQuery.getQuerySentence().getCore()).treeContains(QueryTestConstants.bird().getCore()));
  }

  /**
   * Test of setMaxTime method, of class QueryImpl.
   * @throws com.cyc.query.exception.QueryConstructionException
   */
  @Test
  public void testSetMaxTime() throws QueryConstructionException {
    System.out.println("setMaxTime");
    currentQuery = constructXIsaBirdQuery();
    currentQuery.setMaxTime(12);
    assertTrue(12 == currentQuery.getMaxTime());
  }

  /**
   * Test of setMaxNumber method, of class Query.
   * @throws com.cyc.query.exception.QueryConstructionException
   */
  @Test
  public void testSetMaxNumber() throws QueryConstructionException {
    System.out.println("setMaxNumber");
    currentQuery = constructXIsaBirdQuery();
    currentQuery.setMaxAnswerCount(12);
    assertTrue(12 == currentQuery.getMaxAnswerCount());
  }

  /**
   * Test of setInferenceMode method, of class Query.
   * @throws com.cyc.query.exception.QueryConstructionException
   */
  @Test
  public void testSetInferenceMode() throws QueryConstructionException {
    System.out.println("setInferenceMode");
    currentQuery = constructXIsaBirdQuery();
    currentQuery.setInferenceMode(OpenCycInferenceMode.MINIMAL_MODE); //@#$@%$@#$ OpenCycInferenceMode @##$&#%^@#$%
    assertEquals(OpenCycInferenceMode.MINIMAL_MODE, currentQuery.getInferenceMode());
  }

  private static void waitForQueryToFinish() throws InterruptedException {
    Thread.sleep(1000);
  }
  
  private static class TestQueryListener implements QueryListener {
    // TODO: use some of these fields for verifying test results.
    private boolean created = false;
    private boolean terminated = false;
    private final List<InferenceStatus> statuses = new ArrayList<InferenceStatus>();
    private final List answers = new ArrayList();

    @Override
    public void notifyInferenceCreated(Query query) {
      System.out.println("Inference created.");
      created = true;
    }

    @Override
    public void notifyInferenceStatusChanged(InferenceStatus oldStatus,
            InferenceStatus newStatus,
            InferenceSuspendReason suspendReason,
            Query query) {
      System.out.println(
              "Inference status changed from " + oldStatus + " to " + newStatus);
      statuses.add(oldStatus);
      statuses.add(newStatus);
    }

    @Override
    public void notifyInferenceAnswersAvailable(Query query, List<QueryAnswer> newAnswers) {
      System.out.println("New answers: " + newAnswers);
      answers.addAll(newAnswers);
    }

    @Override
    public void notifyInferenceTerminated(Query query, Exception e) {
      System.out.println("Inference terminated.");
      terminated = true;
    }
  }
  
  /**
   * Test of bindVariable method, of class Query.
   * @throws java.io.IOException
   * @throws com.cyc.query.exception.QueryConstructionException
   * @throws com.cyc.kb.exception.KbException
   */
  @Test
  public void testBindVariable_String_Object() throws IOException, QueryConstructionException, KbException {
    currentQuery = constructXIsaBirdQuery();
    assertTrue(currentQuery + " does not contain " + X, currentQuery.getQueryVariables().contains(X));
    currentQuery.bindVariable("?X", QueryTestConstants.bird());
    assertFalse(currentQuery + " contains " + X, currentQuery.getQueryVariables().contains(X));
    currentQuery.setQuerySentenceMainClause(xIsaBird());
    assertTrue(currentQuery + " does not contain " + X, currentQuery.getQueryVariables().contains(X));
    currentQuery.bindVariable("X", QueryTestConstants.bird());
  }

  /**
   * Test of bindVariable method, of class Query.
   * @throws java.io.IOException
   * @throws com.cyc.query.exception.QueryConstructionException
   * @throws com.cyc.kb.exception.KbException
   */
  @Test
  public void testBindVariable_CycVariable_Object() throws IOException, QueryConstructionException, KbException {
    currentQuery = constructXIsaBirdQuery();
    assertTrue(currentQuery + " does not contain " + X, currentQuery.getQueryVariables().contains(X));
    currentQuery.bindVariable(X, QueryTestConstants.bird());
    assertFalse(currentQuery + " contains " + X, currentQuery.getQueryVariables().contains(X));
  }

  /**
   * Test of metrics accessors, of class Query.
   *
   * @throws java.lang.InterruptedException
   * @throws com.cyc.query.exception.QueryConstructionException
   * @throws com.cyc.session.exception.SessionCommunicationException
   * @throws com.cyc.base.exception.CycConnectionException
   * @throws CycTimeOutException
   */
  @Test
  public void testMetrics() throws InterruptedException, QueryConstructionException, SessionCommunicationException, CycConnectionException {
    System.out.println("testMetrics");
    currentQuery = constructXIsaBirdQuery();
    // Gather up all the metrics we have java constants for:
    final List<? extends InferenceMetric> metricsList = Arrays.asList(
            StandardInferenceMetric.ANSWER_COUNT, StandardInferenceMetric.HYPOTHESIZATION_TIME, StandardInferenceMetric.LINK_COUNT,
            StandardInferenceMetric.PROBLEM_COUNT, StandardInferenceMetric.PROBLEM_STORE_PROBLEM_COUNT,
            StandardInferenceMetric.PROBLEM_STORE_PROOF_COUNT, StandardInferenceMetric.PROOF_COUNT,
            StandardInferenceMetric.TACTIC_COUNT, StandardInferenceMetric.TIME_PER_ANSWER, StandardInferenceMetric.TIME_TO_FIRST_ANSWER,
            StandardInferenceMetric.TIME_TO_LAST_ANSWER, StandardInferenceMetric.TOTAL_TIME, StandardInferenceMetric.WASTED_TIME_AFTER_LAST_ANSWER);
    // Add them all to our query:
    currentQuery.getInferenceParameters().getMetrics().addAll(metricsList);
    if (!TestUtils.getCyc().isOpenCyc()) {
      currentQuery.getInferenceParameters().getMetrics().add(StandardInferenceMetric.SKSI_QUERY_START_TIMES);
      currentQuery.getInferenceParameters().getMetrics().add(StandardInferenceMetric.SKSI_QUERY_TOTAL_TIME);
    }
    currentQuery.start();
    waitForQueryToFinish();
    final InferenceMetricsValues metricsValues =  currentQuery.getMetricsValues();
    for (final InferenceMetric metric : metricsList) {
      final Object value = metricsValues.getValue(metric);
      System.out.println(metric + ": " + value);
      assertNotNull("Got null for " + metric, value);
    }
  }

}
