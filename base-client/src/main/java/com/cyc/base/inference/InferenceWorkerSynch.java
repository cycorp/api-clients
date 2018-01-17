package com.cyc.base.inference;

/*
 * #%L
 * File: InferenceWorkerSynch.java
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

//// Internal Imports
import com.cyc.base.exception.CycApiException;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.base.exception.CycTimeOutException;
import java.util.*;

/**
 * <P>InferenceWorker is designed to...
 *
 * @author tbrussea, zelal
 * @since July 27, 2005, 11:40 AM
 * @version $Id: InferenceWorkerSynch.java 173230 2017-08-10 00:13:28Z nwinant $
 */
public interface InferenceWorkerSynch extends InferenceWorker {

  /** 
   * Performs the synchronous inference operation.
   * 
   * @return a list of query answers
   * @throws com.cyc.base.exception.CycConnectionException 
   */
  public List performSynchronousInference()
          throws CycConnectionException, CycTimeOutException, CycApiException;

  /** 
   * Performs the synchronous inference operation.
   * 
   * @return a list of query answers
   * @throws com.cyc.base.exception.CycConnectionException
   */
  public InferenceResultSet executeQuerySynch()
          throws CycConnectionException, CycTimeOutException, CycApiException;
  
}
