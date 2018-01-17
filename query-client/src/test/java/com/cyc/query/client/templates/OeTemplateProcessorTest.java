/*
 * Copyright 2017 Cycorp, Inc..
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
package com.cyc.query.client.templates;

/*
 * #%L
 * File: OeTemplateProcessorTest.java
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

import com.cyc.kb.KbIndividual;
import com.cyc.kb.Variable;
import com.cyc.kb.exception.KbException;
import com.cyc.query.Query;
import com.cyc.query.QueryAnswers;
import com.cyc.query.client.templates.OeTemplateJob.TemplateJobId;
import com.cyc.query.client.templates.OeTemplateListener.ToeTemplateEventType;
import com.cyc.query.exception.QueryConstructionException;
import com.cyc.session.exception.SessionException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.cyc.Cyc.Constants.INFERENCE_PSC;
import static com.cyc.query.client.TestUtils.findOrCreate_Kbq;
import static com.cyc.query.client.TestUtils.xIsaBird;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author nwinant
 */
public class OeTemplateProcessorTest {
  
  public OeTemplateProcessorTest() {
  }
  
  @BeforeClass
  public static void setUpClass() throws QueryConstructionException, KbException {
    //deleteAllTestConstants();
    KBQ_ID = KbIndividual.get("#$SNCVA-AWCAQuery");
    SIMPLE_KBQ_ID = findOrCreate_Kbq("SimpleQuery", xIsaBird(), INFERENCE_PSC).getId();
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
  
  //====|    Fields    |==========================================================================//
  
  private static final Logger LOG = LoggerFactory.getLogger(OeTemplateProcessorTest.class);
  
  public static final int MAX_ANSWERS = 10;
  
  public static KbIndividual KBQ_ID;
  public static KbIndividual SIMPLE_KBQ_ID;
  
  private static final Consumer<QueryAnswers> ANSWERS_PRINTER = (answers) -> {
    answers.printAnswersTable(System.out, false);
    System.out.println("Total: " + answers);
    System.out.println("---------");
  };
  
  //====|    Tests    |===========================================================================//
  
  @Test
  public void testGetResults() throws Exception {
    final OeTemplateResults totalResults = new OeTemplateResults();
    final OeTemplateProcessor toeProcessor = new OeTemplateProcessor(KBQ_ID);
    final OeTemplateProcessorConfig config = getConfig();
    final OeTemplateListener listener = getListener(totalResults);
    final OeTemplateJob job = toeProcessor
            .processToeTemplate(Collections.emptyMap(), config, listener);
    int count = -1;
    while (!job.isDone()) {
      try {
        LOG.debug("Sleeping..." 
                  + "  " + job.isDone());
                  //+ "  " + job.getToeHandler().isDone() 
                  //+ "  " + job.getPollingHandler().isDone());
        TimeUnit.SECONDS.sleep(3);
        if (count > 0 && count == totalResults.size()) {
          LOG.debug("No recent activity...");
        }
        LOG.debug("... awake! (retrieved: " + totalResults.size() + ")");
        count = totalResults.size();
      } catch (InterruptedException e) {
        LOG.error("Sleep error", e);
      }
    }
    LOG.info("Done:");
    LOG.info("\n{}", totalResults.toPrettyString("-|"));
    LOG.info("Resulting status: {}", job.get());
    assertEquals(MAX_ANSWERS, totalResults.size());
  }
  
  @Test
  public void testGetResults_KBQ_sync() throws Exception {
    System.out.println("testGetResults_KBQ_sync");
    final QueryAnswers expectedAnswers;
    final int expectedNumAnswers;
    {
      expectedAnswers = Query.get(SIMPLE_KBQ_ID).getAnswers();
      expectedNumAnswers = expectedAnswers.size();
      System.out.println("Expected:");
      ANSWERS_PRINTER.accept(expectedAnswers);
    }
    System.out.println();
    
    final OeTemplateResults jobResults = new OeTemplateResults();
    final OeTemplateProcessorConfig config = OeTemplateProcessorConfig.create();
    final OeTemplateJob job = processTemplateAndWait(SIMPLE_KBQ_ID, config, jobResults);
    final int jobResultsSize = jobResults.size();
    final boolean jobIsDone = job.isDone();
    final QueryAnswers resultingAnswers = jobResults.toQueryAnswers();
    System.out.println("Results:");
    ANSWERS_PRINTER.accept(resultingAnswers);
    //System.out.println(jobResults.toPrettyString("    "));
    //System.out.println("Total: " + jobResultsSize);
    //System.out.println("--------");
    assertTrue(jobIsDone);
    assertTrue(job.isDone());
    LOG.info("Resulting status: {}", job.get());
    
    final QueryAnswers verifiedAnswers;
    {
      verifiedAnswers = Query.get(SIMPLE_KBQ_ID).getAnswers();
      assertEquals(expectedNumAnswers, verifiedAnswers.size());
    }
    assertEquals(expectedAnswers.size(), jobResults.size());
    assertEquals(jobResultsSize, jobResults.size());
    
    final Variable var = Variable.get("?X");
    final List<String> expectedBindings = expectedAnswers.toBindingsStringsForVariable(var);
    final List<String> resultingBindings = resultingAnswers.toBindingsStringsForVariable(var);
    System.out.println("----------");
    expectedBindings.stream().forEach(System.out::println);
    System.out.println("----------");
    resultingBindings.stream().forEach(System.out::println);
    System.out.println("----------");
    assertEquals(expectedBindings, resultingBindings);
  }
  
  /*
  @Test
  public void testGetResults_KBQ_async() throws Exception {
    System.out.println("testGetResults_KBQ_async");
    
    final QueryAnswers expectedAnswers;
    final int expectedNumAnswers;
    {
      expectedAnswers = Cyc.getQuery(SIMPLE_KBQ_ID).getAnswers();
      expectedNumAnswers = expectedAnswers.size();
      System.out.println("Expected:");
      ANSWERS_PRINTER.accept(expectedAnswers);
    }
    System.out.println();
    
    final OeTemplateResults jobResults = new OeTemplateResults();
    final OeTemplateProcessorConfig config = OeTemplateProcessorConfig.create();
    final OeTemplateJob job = processTemplateAndWait(SIMPLE_KBQ_ID, config, jobResults);
    final int jobResultsSize = jobResults.size();
    final boolean jobIsDone = job.isDone();
    final QueryAnswers resultingAnswers = jobResults.toQueryAnswers();
    System.out.println("Results:");
    ANSWERS_PRINTER.accept(resultingAnswers);
    //System.out.println(jobResults.toPrettyString("    "));
    //System.out.println("Total: " + jobResultsSize);
    //System.out.println("--------");
    assertTrue(jobIsDone);
    assertTrue(job.isDone());
    
    LOG.info("Resulting status: {}", job.get());
    
    final QueryAnswers verifiedAnswers;
    {
      verifiedAnswers = Cyc.getQuery(SIMPLE_KBQ_ID).getAnswers();
      assertEquals(expectedNumAnswers, verifiedAnswers.size());
    }
    assertEquals(expectedAnswers.size(), jobResults.size());
    assertEquals(jobResultsSize, jobResults.size());
    
    final Variable var = KbFactory.getVariable("?X");
    final List<String> expectedBindings = expectedAnswers.toBindingsStringsForVariable(var);
    final List<String> resultingBindings = resultingAnswers.toBindingsStringsForVariable(var);
    
    System.out.println("----------");
    expectedBindings.stream().forEach(System.out::println);
    System.out.println("----------");
    resultingBindings.stream().forEach(System.out::println);
    System.out.println("----------");
    assertEquals(expectedBindings, resultingBindings);
  }
  */
  
  //====|    Configuration & helper methods    |==================================================//
  
  protected OeTemplateProcessorConfig getConfig() {
    final OeTemplateProcessorConfig config = OeTemplateProcessorConfig.create();
    config
            //.setPollingIntervalMillis(1000)
            //.setPollingMaxDurationSecs(5)
            //.setResultsBatchSize(100)
            .getInferenceParameterOverrides()
            .setMaxAnswerCount(MAX_ANSWERS);
    return config;
  }
  
  protected OeTemplateListener getListener(OeTemplateResults jobResults) {
    return new OeTemplateListener() {
      @Override
      public void onEvent(TemplateJobId jobId, ToeTemplateEventType eventType) {
        LOG.info("EVENT: {}: {}", jobId, eventType);
      }
      @Override
      public void onResults(TemplateJobId jobId, OeTemplateResults results) {
        jobResults.addAll(results);
        if (LOG.isTraceEnabled()) {
          LOG.info("{} results, {} total:\n{}",
                  results.size(), jobResults.size(), results.toPrettyString("    |"));
        } else {
          LOG.info("{} results, {} total", results.size(), jobResults.size());
        }
      }
      @Override
      public void onError(TemplateJobId jobId, Throwable error) {
        LOG.error(jobId + ": " + error.getMessage(), error);
      }
    };
  }
  
  //====|    Node processing    |=================================================================//
  
  protected OeTemplateJob processTemplate(KbIndividual kbqId,
                                          OeTemplateProcessorConfig config,
                                          OeTemplateResults jobResults) throws SessionException {
    final OeTemplateProcessor toeProcessor = new OeTemplateProcessor(kbqId);
    final OeTemplateListener listener = getListener(jobResults);
    return toeProcessor.processToeTemplate(Collections.emptyMap(), config, listener);
  }
  
  protected OeTemplateJob waitForResults(OeTemplateJob job, OeTemplateResults jobResults) {
    int count = -1;
    while (!job.isDone()) {
      try {
        LOG.debug("Sleeping..." 
                  + "  " + job.isDone());
                  //+ "  " + job.getToeHandler().isDone() 
                  //+ "  " + job.getPollingHandler().isDone());
        TimeUnit.SECONDS.sleep(3);
        if (count > 0 && count == jobResults.size()) {
          LOG.debug("No recent activity...");
        }
        LOG.debug("... awake! (retrieved: " + jobResults.size() + ")");
        count = jobResults.size();
      } catch (InterruptedException e) {
        LOG.error("Sleep error", e);
      }
    }
    return job;
  }
  
  protected OeTemplateJob processTemplateAndWait(KbIndividual kbqId,
                                                 OeTemplateProcessorConfig config,
                                                 OeTemplateResults jobResults)
          throws SessionException {
    final OeTemplateJob job = processTemplate(kbqId, config, jobResults);
    waitForResults(job, jobResults);
    return job;
  }
  
}
