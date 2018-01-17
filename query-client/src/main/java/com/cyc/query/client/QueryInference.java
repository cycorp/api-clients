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
package com.cyc.query.client;

/*
 * #%L
 * File: QueryInference.java
 * Project: Query Client
 * %%
 * Copyright (C) 2013 - 2018 Cycorp, Inc.
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
import com.cyc.base.cycobject.ElMt;
import com.cyc.base.exception.CycApiException;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.base.exception.CycTimeOutException;
import com.cyc.base.inference.InferenceResultSet;
import com.cyc.base.inference.InferenceWorker;
import com.cyc.base.inference.InferenceWorkerListener;
import com.cyc.baseclient.inference.DefaultInferenceWorkerSynch;
import com.cyc.kb.client.ContextImpl;
import com.cyc.query.InferenceIdentifier;
import com.cyc.query.InferenceStatus;
import com.cyc.query.InferenceSuspendReason;
import com.cyc.query.QueryListener;
import com.cyc.query.QueryResultSet;
import com.cyc.session.exception.SessionCommunicationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Inner class to hold the aspects of a QueryImpl that it acquires when run.
 *
 * // TODO: update javadocs - nwinant, 2017-08-01
 *
 * @author nwinant
 */
class QueryInference implements InferenceWorkerListener {

  //====|    Fields    |==========================================================================//
  
  long closeTimeoutMS = 5000; // TODO: encapsulate in setter/getter? - nwinant, 2017-08-02
  private static final Logger LOGGER = LoggerFactory.getLogger(QueryInference.class);
  private QueryResultSet rs = null;
  private boolean started = false;
  private QueryWorker worker;
  private InferenceStatus inferenceStatus = InferenceStatus.NOT_STARTED;
  private InferenceIdentifier inferenceIdentifier = null;
  private final List<Object> cycAnswers = new ArrayList<>();

  private final Object continueInferenceLock = new Object();
  private boolean isContinuingInference = false;

  private final QueryImpl query;

  //====|    Construction    |====================================================================//
  
  QueryInference(QueryImpl query) {
    this.query = query;
  }

  //====|    Methods    |=========================================================================//
  
  @Override
  public void notifyInferenceCreated(InferenceWorker inferenceWorker) {
    this.started = true;
  }
  
  @Override
  public void notifyInferenceStatusChanged(InferenceStatus oldStatus,
                                           InferenceStatus newStatus,
                                           InferenceSuspendReason suspendReason,
                                           InferenceWorker inferenceWorker) {
    this.inferenceStatus = newStatus;
  }
  
  @Override
  public void notifyInferenceAnswersAvailable(InferenceWorker inferenceWorker, List newAnswers) {
    cycAnswers.addAll(newAnswers);
  }

  @Override
  public void notifyInferenceTerminated(InferenceWorker inferenceWorker, Exception e) {
    if (isContinuingInference) {
      isContinuingInference = false;
      synchronized (continueInferenceLock) {
        continueInferenceLock.notify();
      }
    }
  }

  public void start() throws IOException, CycConnectionException {
    worker = createInferenceWorker();
    worker.start();
    started = true;
  }

  public QueryResultSet performInference() throws CycConnectionException {
    if (query.requiresInferenceWorker()) {
      worker = createInferenceWorker();
      worker.executeQuerySynch();
    } else {
      // Don't retrieve anything until query is completely finished.
      final InferenceResultSet inferenceResultSet
              = query.getCycAccess().getInferenceTool().executeQuery(
                      query.getQuerySentenceCyc(),
                      ContextImpl.asELMt(query.getContext()), 
                      query.getInferenceParameters());
      setResultSet(new QueryResultSetImpl(inferenceResultSet));
      started = true;
      inferenceStatus = InferenceStatus.SUSPENDED;
    }
    return rs;
  }
  
  public void continueInference() throws CycConnectionException {
    if (!query.isContinuable()) {
      throw new UnsupportedOperationException("This query is not continuable.");
    } else if (!started) {
      performInference();
    } else if (worker != null) {
      try {
        isContinuingInference = true;
        worker.continueInference(query.getParams());
        synchronized (continueInferenceLock) {
          while (isContinuingInference) {
            continueInferenceLock.wait();
          }
        }
      } catch (InterruptedException ex) {

      } finally {
        isContinuingInference = false;
      }
    } else {
      throw new UnsupportedOperationException("This query cannot be continued.");
    }
  }

  public QueryResultSet getResultSet() throws CycConnectionException {
    if (rs == null) {
      if (!started) {
        performInference();
      }
      if (rs == null) {
        setResultSet(new QueryResultSetImpl(cycAnswers));
      }
    }
    if (rs.getCurrentRowCount() < cycAnswers.size()) {
      setResultSet(new QueryResultSetImpl(cycAnswers));
    }
    return rs;
  }
  
  public InferenceIdentifier getInferenceIdentifier() throws SessionCommunicationException {
    if (inferenceIdentifier == null && started) {
      try {
        if (worker != null) {
          inferenceIdentifier = worker.getInferenceIdentifier();
        }
        if (getResultSet() != null && inferenceIdentifier == null) {
          inferenceIdentifier = rs.getInferenceIdentifier();
        }
      } catch (CycConnectionException ex) {
        throw SessionCommunicationException.fromThrowable(ex);
      }
    }
    return inferenceIdentifier;
  }
  
  public void stop(final Integer patience) {
    if (patience == null) {
      worker.interruptInference();
    } else {
      worker.interruptInference(patience);
    }
  }

  public InferenceSuspendReason getSuspendReason() {
    if (worker != null) {
      return worker.getSuspendReason();
    } else {
      return null;
    }
  }

  public void close() {
    if (worker != null) {
      try {
        worker.releaseInferenceResources(closeTimeoutMS);
        return;
      } catch (CycConnectionException | CycTimeOutException | CycApiException ex) {
        LOGGER.error("Got exception trying to free inference resources for " + this, ex);
        ex.printStackTrace(System.err);
      }
    }
    if (rs != null) {
      ((QueryResultSetImpl) rs).close();
    }
  }

  public void clear() {
    close();
    rs = null;
    started = false;
    worker = null;
    inferenceStatus = InferenceStatus.NOT_STARTED;
    inferenceIdentifier = null;
    cycAnswers.clear();
  }
  
  public boolean hasBeenStarted() {
    return started;
  }

  public InferenceStatus getInferenceStatus() {
    return inferenceStatus;
  }
  
  /*
  private CycList getSublCommand() {
    return (worker != null) ? worker.getSublCommand() : null;
  }
  */
  
 /*====  internals  =========================================================================*/
  
  private QueryWorker createInferenceWorker() {
    final ElMt elmt = ContextImpl.asELMt(query.getContext());
    final QueryWorker newWorker = new QueryWorker(elmt, query.getCycAccess());
    // We get to be the first listener, so we can be sure we're up to date
    // when other listeners are called.
    newWorker.addInferenceListener(this);
    query.getListeners().forEach((listener) -> {
      newWorker.addQueryListener(listener);
    });
    return (QueryWorker) newWorker;
  }

  private void setResultSet(QueryResultSet rs) {
    this.rs = rs;
  }

  /* ==  QueryWorker  =========================================================================== */
  
  class QueryWorker extends DefaultInferenceWorkerSynch {

    QueryWorker(ElMt mt, CycAccess access) {
      super(query.getQuerySentenceCyc(), mt, query.getParams(), access, query
            .getQueryWorkerTimeoutMillis());
    }

    QueryImpl getQuery() {
      return query;
    }

    public void addQueryListener(QueryListener listener) {
      if (listener instanceof InferenceWorkerListener) {
        addInferenceListener((InferenceWorkerListener) listener);
      } else {
        addInferenceListener(new QueryListenerAdaptor(listener));
      }
    }
  }

}
