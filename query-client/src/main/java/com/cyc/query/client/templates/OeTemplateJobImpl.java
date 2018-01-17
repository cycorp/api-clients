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
 * File: OeTemplateJobImpl.java
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
import com.cyc.query.InferenceStatus;
import com.cyc.query.InferenceSuspendReason;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 
 * @author nwinant
 */
public class OeTemplateJobImpl implements OeTemplateJob {
  
  private final TemplateJobId jobId;
  private final Future<InferenceSuspendReason> jobFuture;
  private final Future<InferenceSuspendReason> toeHandler;           // TODO: remove? - nwinant, 2017-08-23
  private final Future<?> resultHandler;                      // TODO: remove? - nwinant, 2017-08-23

  public OeTemplateJobImpl(
          TemplateJobId jobId,
          Future<InferenceSuspendReason> jobFuture,
          Future<InferenceSuspendReason> toeHandler,
          Future<?> resultHandler) {
    this.jobId = jobId;
    this.jobFuture = jobFuture;
    this.toeHandler = toeHandler;
    this.resultHandler = resultHandler;
  }
  
  @Override
  public TemplateJobId getId() {
    return this.jobId;
  }
  
  @Override
  public boolean isDone() {
    return jobFuture.isDone();
  }

  @Override
  public String toString() {
    return "JOB:" + this.getId();
  }

  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    return jobFuture.cancel(mayInterruptIfRunning);
  }

  @Override
  public boolean isCancelled() {
    return jobFuture.isCancelled();
  }

  @Override
  public InferenceSuspendReason get() throws InterruptedException, ExecutionException {
    return jobFuture.get(); // TODO: change this to throw UnsupportedOperationException - nwinant, 2017-08-24
  }

  @Override
  public InferenceSuspendReason get(long timeout, TimeUnit unit)
          throws InterruptedException, ExecutionException, TimeoutException {
    return jobFuture.get(timeout, unit);
  }
  
  //====|    TemplateJobId    |===================================================================//
  
  public static class TemplateJobIdImpl implements TemplateJobId {
    private final KbIndividual queryId;
    private final int resultsId;
    private final String name;
    
    public TemplateJobIdImpl(final KbIndividual queryId, final int resultsId) {
      this.queryId = queryId;
      this.resultsId = resultsId;
      this.name = "{" + queryId + " #" + resultsId + "}";
    }
    
    @Override
    public KbIndividual getQueryId() {
      return this.queryId;
    }
    
    @Override
    public int getResultsId() {
      return this.resultsId;
    }
    
    @Override
    public String toString() {
      return this.name;
    }
    
    @Override
    public boolean equals(Object obj) {
      if (!(obj instanceof TemplateJobId)) {
        return false;
      }
      final TemplateJobId that = (TemplateJobId) obj;
      return Objects.equals(this.getQueryId(), that.getQueryId()) 
             && Objects.equals(this.getResultsId(), that.getResultsId());
    }
    
    @Override
    public int hashCode() {
      int hash = 3;
      hash = 37 * hash + Objects.hashCode(this.queryId);
      hash = 37 * hash + this.resultsId;
      return hash;
    }
    
  }
  
}
