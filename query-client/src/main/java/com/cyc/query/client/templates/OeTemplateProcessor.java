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
 * File: OeTemplateProcessor.java
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

import com.cyc.base.cycobject.CycList;
import com.cyc.base.cycobject.CycObject;
import com.cyc.base.cycobject.DenotationalTerm;
import com.cyc.base.exception.CycApiException;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.baseclient.CycClientManager;
import com.cyc.baseclient.inference.DefaultInferenceSuspendReason;
import com.cyc.baseclient.kbtool.TemplateOeToolImpl;
import com.cyc.kb.KbIndividual;
import com.cyc.kb.KbObject;
import com.cyc.query.InferenceSuspendReason;
import com.cyc.query.client.templates.OeTemplateJob.TemplateJobId;
import com.cyc.query.client.templates.OeTemplateJobImpl.TemplateJobIdImpl;
import com.cyc.session.CycAddress;
import com.cyc.session.exception.SessionCommunicationException;
import com.cyc.session.exception.SessionConfigurationException;
import com.cyc.session.exception.SessionInitializationException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.cyc.query.client.templates.OeTemplateListener.ToeTemplateEventType.PROCESSING_BEGUN;
import static com.cyc.query.client.templates.OeTemplateListener.ToeTemplateEventType.PROCESSING_COMPLETE;
import static com.cyc.query.client.templates.OeTemplateListener.ToeTemplateEventType.QUERY_COMPLETE;
import static org.apache.commons.lang3.StringUtils.leftPad;
import static org.apache.commons.lang3.StringUtils.rightPad;

/**
 *
 * @author nwinant
 */
public class OeTemplateProcessor {
  
  //====|    TaskComplete exception   |===========================================================//
  
  public class TaskComplete extends RuntimeException {
    public TaskComplete(String msg) {
      super(msg);
    }
  }
  
  //====|    Fields    |==========================================================================//
  
  private static final Logger LOG = LoggerFactory.getLogger(OeTemplateProcessor.class);
  
  private static final ExecutorService DEFAULT_EXECUTOR_SERVICE = Executors.newCachedThreadPool();
  
  private final KbIndividual queryId;
  private final DenotationalTerm queryTerm;
  private final TemplateOeToolImpl toeClient;
  private final ExecutorService executorSvc;
  private final Map<OeTemplateJob, Long> jobs;
  private String threadPrefix;
  
  //====|    Construction    |====================================================================//
  
  public OeTemplateProcessor(final KbIndividual queryId,
                             final TemplateOeToolImpl toeClient, 
                             final ExecutorService executorSvc) {
    this.queryId = queryId;
    this.queryTerm = (DenotationalTerm) queryId.getCore();
    this.toeClient = toeClient;
    this.executorSvc = executorSvc;
    this.jobs = new ConcurrentHashMap<>();
    this.threadPrefix = Thread.currentThread().getName();
  }
  
  public OeTemplateProcessor(final KbIndividual queryId, TemplateOeToolImpl toeClient) {
    this(queryId, toeClient, DEFAULT_EXECUTOR_SERVICE);
  }
  
  public OeTemplateProcessor(final KbIndividual queryId) throws SessionConfigurationException,
                                                                SessionCommunicationException,
                                                                SessionInitializationException {
    this(queryId, new TemplateOeToolImpl(CycClientManager.getCurrentClient()));
  }
  
  public OeTemplateProcessor(final KbIndividual queryId, CycAddress cyc) throws
      SessionConfigurationException,
      SessionCommunicationException,
      SessionInitializationException,
      CycConnectionException {
    this(queryId, new TemplateOeToolImpl(CycClientManager.get().getAccess(cyc)));
    this.threadPrefix = cyc.toString();
  }

  //====|    Getters   |==========================================================================//
  
  public TemplateOeToolImpl getToeClient() {
    return this.toeClient;
  }
  
  public KbIndividual getQueryId() {
    return this.queryId;
  }
  
  //====|    Logging utilities    |===============================================================//
  
  private static final int MSG_MIN_PAD = 18;
  private static final int COUNT_PAD = 5;
  private static final int MSG_FULL_PAD = MSG_MIN_PAD + COUNT_PAD + 5;
  
  private void debug(String msg, TemplateJobId jobId) {
    LOG.debug("{} {}", rightPad(msg, MSG_FULL_PAD), jobId);
  }
  
  private void debug(String msg, int count, TemplateJobId jobId) {
    LOG.debug("{} {} for {}", rightPad(msg, MSG_MIN_PAD), leftPad("" + count, COUNT_PAD), jobId);
  }
  
  private void trace(String msg, TemplateJobId jobId) {
    LOG.trace("{} {}", rightPad(msg, MSG_FULL_PAD), jobId);
  }
  
  //====|    Functions    |=======================================================================//
  
  private class Functions {
    
    private final Consumer<Consumer<OeTemplateListener>> fireListeners;
    public final Consumer<Exception>         errorHandler;
    public final Supplier<Boolean>           hasRemainingResults;
    public final Supplier<Integer>           remainingResultsCount;
    public final Supplier<OeTemplateResults> resultsSupplier;
    public final Supplier<OeTemplateResults> resultsDrainer;
    public final Supplier<InferenceSuspendReason>   toeRunner;
    
    public Functions(final TemplateJobId jobId,
                     final Map<KbObject, Object> substitutions,
                     final OeTemplateProcessorConfig config,
                     final Collection<OeTemplateListener> listeners) {
      final int resultsQueueId = jobId.getResultsId();
      fireListeners = (trigger) -> {
        executorSvc.submit(() -> { listeners.forEach(trigger); });
      };
      errorHandler = (ex) -> {              // TODO: use completeExceptionally - nwinant, 2017-08-23
        if (ex instanceof TaskComplete) {
          LOG.info(ex.getMessage());
        } else {
          LOG.error("Processing error", ex);
          fireListeners.accept((l) -> { l.onError(jobId, ex); });
          //throw QueryRuntimeException.fromThrowable(ex);
        }
      };
      final Map<CycObject, Object> cycSubstitutions = new LinkedHashMap<>();
      substitutions.forEach((k, v) -> { cycSubstitutions.put((CycObject) k.getCore(), v); });
      toeRunner = () -> {
        String oldName = Thread.currentThread().getName();
        try {
          Thread.currentThread().setName(threadPrefix + "-toerunner");
          fireListeners.accept((l) -> { l.onEvent(jobId, PROCESSING_BEGUN); });
          debug("Processing begun:", jobId);
          final CycObject result = toeClient.processTemplate(
                  queryTerm, 
                  config.getFocalMt(),
                  config.getFocalPredicateFort(),
                  config.getInferenceParameterOverrides(),
                  cycSubstitutions,
                  resultsQueueId,
                  config.getResultFieldsAsSymbols(),
                  config.getProcessingOverrides());
          debug("Processing complete:", jobId);
          fireListeners.accept((l) -> { l.onEvent(jobId, QUERY_COMPLETE); });
          return DefaultInferenceSuspendReason.fromCycSuspendReason(result);
        } catch (CycApiException | CycConnectionException ex) {
          errorHandler.accept(ex);
          return null;
        } finally {
          Thread.currentThread().setName(oldName);
        }
      };
      hasRemainingResults = () -> {
        try {
          return toeClient.hasMoreResults(resultsQueueId);
        } catch (CycApiException | CycConnectionException ex) {
          errorHandler.accept(ex);
          return null;
        }
      };
      remainingResultsCount = () -> {
        try {
          return toeClient.getNumberResultsRemaining(resultsQueueId);
        } catch (CycApiException | CycConnectionException ex) {
          errorHandler.accept(ex);
          return -1;
        }
      };
      resultsSupplier = () -> {
        try {
          //if (!hasRemainingResults.get()) {
          //  return new OeTemplateResults();
          //}
          final CycList<CycList> resultList = toeClient
                  .getResults(resultsQueueId, config.getResultsBatchSize(), config.getTimeoutSec());
          final OeTemplateResults results = new OeTemplateResults(resultList);
          if (!results.isEmpty()) {
            debug("Results retrieved:", results.size(), jobId);
            fireListeners.accept((l) -> { l.onResults(jobId, results); });
          } else {
            trace("No results retrieved", jobId);
          }
          return results;
        } catch (CycApiException | CycConnectionException ex) {
          errorHandler.accept(ex);
          return null;
        }
      };
      resultsDrainer = () -> {
        final OeTemplateResults results = new OeTemplateResults();
        while (hasRemainingResults.get()) {
          debug("Results to drain:", remainingResultsCount.get(), jobId);
          results.addAll(resultsSupplier.get());
        }
        if (!results.isEmpty()) {
          debug("... Draining complete.", jobId);
        } else {
          debug("... Nothing to drain.", jobId);
        }
        return results;
      };
    }
    
    public Functions(final TemplateJobId jobId,
                     final Map<KbObject, Object> substitutions,
                     final OeTemplateProcessorConfig config,
                     final OeTemplateListener listener) {
      this(jobId, substitutions, config, new LinkedHashSet<>(Arrays.asList(listener)));
    }
  }
  
  //====|    Execution   |========================================================================//
  
  private TemplateJobId createJobId() throws SessionCommunicationException {
    try {
      return new TemplateJobIdImpl(queryId, toeClient.getNewQueueId());
    } catch (CycApiException | CycConnectionException ex) {
      throw ex.toSessionException();
    }
  }

  private OeTemplateJob recordJob(OeTemplateJob job) {
    jobs.put(job, System.currentTimeMillis());
    return job;
  }
  
  public OeTemplateJob processToeTemplate(final Map<KbObject, Object> substitutions,
                                          final OeTemplateProcessorConfig config,
                                          final OeTemplateListener listener)
          throws SessionCommunicationException {
    final TemplateJobId jobId;
    final Functions functions;
    final CompletableFuture<InferenceSuspendReason> jobFuture;
    final CompletableFuture<InferenceSuspendReason> toeHandler;
    final CompletableFuture<?> resultHandler;
    jobId = createJobId();
    functions = new Functions(jobId, substitutions, config, listener);
    jobFuture = new CompletableFuture<>();
    toeHandler = CompletableFuture.supplyAsync(functions.toeRunner, executorSvc);
    LOG.trace("Registered toeHandler");
    resultHandler = CompletableFuture
            .runAsync(() -> {
              try {
                String oldName = Thread.currentThread().getName();
                Thread.currentThread().setName(threadPrefix + "-resulthandler");
                while (!toeHandler.isDone()) {
                  trace("Checking for results...", jobId);
                  functions.resultsSupplier.get();
                  if (!functions.hasRemainingResults.get()) {
                    try {
                      TimeUnit.MILLISECONDS.sleep(config.getPollingIntervalMillis());
                    } catch (InterruptedException ex) {
                      functions.errorHandler.accept(ex);
                      //toeHandler.completeExceptionally(ex);
                    }
                  }
                }
                Thread.currentThread().setName(oldName);
              } catch (RuntimeException ex) {
                LOG.error("Error occurred running " + OeTemplateProcessor.class.getSimpleName(), ex);
                functions.errorHandler.accept(ex);
                //jobFuture.completeExceptionally(ex);
              }
            }, executorSvc)
            .thenRun(() -> {
              String oldName = Thread.currentThread().getName();
              Thread.currentThread().setName(threadPrefix + "-jobdrainer");
              try {
                functions.resultsDrainer.get();
              } catch (RuntimeException ex) {
                LOG.error("Error occurred ending " + OeTemplateProcessor.class.getSimpleName(), ex);
                functions.errorHandler.accept(ex);
              } finally {
                functions.fireListeners.accept((l) -> { l.onEvent(jobId, PROCESSING_COMPLETE); });
              }
              try {
                jobFuture.complete(toeHandler.get());
              } catch (RuntimeException
                      | InterruptedException 
                      | ExecutionException ex) {
                functions.errorHandler.accept(ex);
                //throw QueryRuntimeException.fromThrowable(ex);
                jobFuture.completeExceptionally(ex);
              } finally {
                Thread.currentThread().setName(oldName);
              }
            });
    LOG.trace("Registered resultHandler    @ {} sec", config.getTimeoutSec());
    return recordJob(new OeTemplateJobImpl(jobId, jobFuture, toeHandler, resultHandler));
  }

}
