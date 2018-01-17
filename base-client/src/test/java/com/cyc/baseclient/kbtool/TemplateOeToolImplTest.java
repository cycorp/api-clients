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
package com.cyc.baseclient.kbtool;

/*
 * #%L
 * File: TemplateOeToolImplTest.java
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
import com.cyc.base.cycobject.CycList;
import com.cyc.base.cycobject.CycObject;
import com.cyc.base.cycobject.DenotationalTerm;
import com.cyc.base.cycobject.Guid;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.baseclient.cycobject.CycArrayList;
import com.cyc.baseclient.cycobject.CycConstantImpl;
import com.cyc.baseclient.inference.params.SpecifiedInferenceParameters;
import com.cyc.baseclient.testing.TestUtils;
import com.cyc.query.parameters.InferenceParameters;
import com.cyc.session.exception.SessionException;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

/**
 *
 * @author nwinant
 */
public class TemplateOeToolImplTest {
  
  public TemplateOeToolImplTest() {
  }
  
  @BeforeClass
  public static void setUpClass() throws SessionException, CycConnectionException {
    assumeTrue("TemplateOe tests are currently disabled.", false); // FIXME: revise tests & re-enable - nwinant, 2017-08-09
    TestUtils.ensureTestEnvironmentInitialized();
  }
  
  @AfterClass
  public static void tearDownClass() {
  }
  
  @Before
  public void setUp() throws CycConnectionException {
    cyc = TestUtils.getCyc();
    toeTool = new TemplateOeToolImpl(cyc);
  }
  
  @After
  public void tearDown() {
  }
  
  //====|    Fields    |==========================================================================//
  
  //public static final boolean TEMPLATE_OE_TOOL_KE_PRESENT = false;
  
  public static final DenotationalTerm KBQ_ID = null; // FIXME: change this test constant - nwinant, 2017-08-09
  
  public static final DenotationalTerm SIMPLE_KBQ_ID = new CycConstantImpl(
            "#$SimpleQuery_ContentForAPITest", new Guid("f4a5aebb-808b-42ac-b942-c7389e34458a")); // FIXME: make sure this is created in KB - nwinant, 2017-08-23
  
  public static final int MAX_ANSWERS = 10;
  
  public static final int POLLING_INTERVAL_MILLIS = 1000;
  
  public static final InferenceParameters TEST_QUERY_PROPS 
          = new SpecifiedInferenceParameters().setMaxAnswerCount(MAX_ANSWERS);
  
  private static final Logger LOG = LoggerFactory.getLogger(TemplateOeToolImplTest.class);
  
  private CycAccess cyc = null;
  
  private TemplateOeToolImpl toeTool = null;
  
  //====|    Tests    |===========================================================================//
  
  @Test
  public void testGetNewQueueId() throws Exception {
    System.out.println("getNewQueueId");
    final int result = toeTool.getNewQueueId();
    System.out.println("Result: " + result);
    assertTrue(result > -1);
  }
  
  @Test
  public void testProcessTemplate() throws Exception {
    System.out.println("processQuery");
    final int queueId = toeTool.getNewQueueId();
    System.out.println("queueId: " + queueId);
    final long startMillis = startElapsed();
    final CycObject result = toeTool.processTemplate(
            KBQ_ID, 
            TEST_QUERY_PROPS, 
            TemplateOeToolImpl.EMPTY_SUBSTITUTIONS,
            queueId);
    finishElapsed(startMillis, true);
    System.out.println("Status: " + result);
    System.out.println("      : " + (result != null ? result.getClass().getSimpleName() : null));
    assertNotNull(result);
  }
  /*
  @Test
  public void testGetResult_timeout() throws Exception {
    System.out.println("testGetResult_timeout");
    final int timeoutSecs = 5000;
    final int queueId = toeTool.getNewQueueId();
    final long startMillis = startElapsed();
    toeTool.getResults(queueId, 10000, timeoutSecs);
    final long elapsed = finishElapsed(startMillis, true);
    assertTrue(elapsed > 1000);
  }
  */
  @Test
  public void testGetResult() throws Exception {
    System.out.println("getResult (singular)");
    int queueId = toeTool.getNewQueueId();
    /*
      Process TOE-assert asynchronously:
    */
    final CompletableFuture<CycObject> toeProcessor = CompletableFuture.supplyAsync(() -> {
      try {
        LOG.info("Running ToeAssert for queueId: {}...", queueId);
        return toeTool.processTemplate(
                KBQ_ID,
                TEST_QUERY_PROPS,
                TemplateOeToolImpl.EMPTY_SUBSTITUTIONS,
                queueId,
                TemplateOeToolImpl.ALL_IPC_QUEUE_ELEMENTS);
      } catch (CycConnectionException ex) {
        throw new RuntimeException(ex);
      }
    });
    /*
      Get results:
    */
    final CycList totalResults = new CycArrayList();
    LOG.info("Polling every {} ms...", POLLING_INTERVAL_MILLIS);
    while (!toeProcessor.isDone()) {
      try {
        TimeUnit.MILLISECONDS.sleep(POLLING_INTERVAL_MILLIS);
      } catch (InterruptedException e) {
        e.printStackTrace(System.err);
      }
      LOG.info("Polling...");
      if (toeTool.hasMoreResults(queueId)) {
        LOG.info("checking for results...");
        final long startMillis = startElapsed();
        final CycList<?> result = toeTool.getResult(queueId);
        finishElapsed(startMillis);
        assertNotNull(result);
        LOG.info("Result:\n{}", result.toPrettyString("-|"));
        totalResults.add(result);
        LOG.info("Retrieved 1 result, {} total.", totalResults.size());
      } else {
        LOG.info("no results currently on queue...");
        LOG.info("Total results so far: {}", totalResults.size());
      }
    }
    LOG.info("Done!");
    LOG.info("Total results:\n{}", totalResults.toPrettyString("-|"));
    assertEquals(MAX_ANSWERS, totalResults.size());
  }
  
  @Test
  public void testGetResults() throws Exception {
    System.out.println("getResults (plural)");
    int maxResultBatchSize = 100;
    int timeoutSec = 5;
    int queueId = toeTool.getNewQueueId();
    /*
      Process TOE-assert asynchronously:
    */
    final CompletableFuture<CycObject> toeProcessor = CompletableFuture.supplyAsync(() -> {
      try {
        LOG.info("Running ToeAssert for queueId: {}...", queueId);
        return toeTool.processTemplate(
                KBQ_ID,
                TEST_QUERY_PROPS,
                TemplateOeToolImpl.EMPTY_SUBSTITUTIONS,
                queueId,
                TemplateOeToolImpl.ALL_IPC_QUEUE_ELEMENTS);
      } catch (CycConnectionException ex) {
        throw new RuntimeException(ex);
      }
    });
    /*
      Get results:
    */
    final CycList totalResults = new CycArrayList();
    LOG.info("Polling every {} ms...", POLLING_INTERVAL_MILLIS);
    while (!toeProcessor.isDone()) {
      try {
        TimeUnit.MILLISECONDS.sleep(POLLING_INTERVAL_MILLIS);
      } catch (InterruptedException e) {
        e.printStackTrace(System.err);
      }
      LOG.info("Polling...");
      if (toeTool.hasMoreResults(queueId)) {
        LOG.info("checking for results...");
        final long startMillis = startElapsed();
        final CycList<?> results = toeTool.getResults(queueId, maxResultBatchSize, timeoutSec);
        finishElapsed(startMillis);
        assertNotNull(results);
        LOG.info("Results:\n{}", results.toPrettyString("-|"));
        totalResults.addAll(results);
        LOG.info("Retrieved {} results, {} total.", results.size(), totalResults.size());
      } else {
        LOG.info("no results currently on queue...");
        LOG.info("Total results so far: {}", totalResults.size());
      }
    }
    LOG.info("Done!");
    LOG.info("Total results:\n{}", totalResults.toPrettyString("-|"));
    assertEquals(MAX_ANSWERS, totalResults.size());
  }
  
  @Test
  public void testProcessing_sync() throws Exception {
    System.out.println("testProcessing_sync (simple!)");
    int maxResultBatchSize = 100;
    int timeoutSec = 5;
    int queueId = toeTool.getNewQueueId();
    System.out.println("Running ToeAssert for queueId: " + queueId + "...");
    final CycObject returnValue = toeTool.processTemplate(
            SIMPLE_KBQ_ID,
            TEST_QUERY_PROPS,
            null,
            queueId,
            null);
    System.out.println("Returned:    " + returnValue);
    System.out.println("hasResults?  " + toeTool.hasMoreResults(queueId));
    System.out.println("# remaining? " + toeTool.getNumberResultsRemaining(queueId));
    final CycList<?> results = toeTool.getResults(queueId, maxResultBatchSize, timeoutSec);
    System.out.println();
    System.out.println("=========================================================================");
    System.out.println();
    System.out.println(results.toPrettyString("-|"));
    System.out.println();
    System.out.println("=========================================================================");
    assertEquals(MAX_ANSWERS, results.size());
  }
  
  //====|    Utilities    |=======================================================================//
  
  private static long startElapsed() {
    final long startMillis = System.currentTimeMillis();
    LOG.info("Started at {}", LocalDateTime.now());
    return startMillis;
  }
  
  private static long finishElapsed(long startMillis, boolean verbose) {
    final long elapsedMillis = System.currentTimeMillis() - startMillis;
    if (verbose) {
      LOG.info("Finised at {}", LocalDateTime.now());
    }
    LOG.info("Elapsed: {} secs ({} ms)", (elapsedMillis / 1000), elapsedMillis);
    return elapsedMillis;
  }
  
  private static long finishElapsed(long startMillis) {
    return finishElapsed(startMillis, false);
  }
  
}
