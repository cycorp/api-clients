package com.cyc.base.inference;

/*
 * #%L
 * File: InferenceWorker.java
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

//// External Imports
import com.cyc.base.connection.Worker;
import com.cyc.base.exception.CycApiException;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.base.exception.CycTimeOutException;
import com.cyc.query.InferenceIdentifier;
import com.cyc.query.InferenceStatus;
import com.cyc.query.InferenceSuspendReason;
import com.cyc.query.parameters.InferenceParameters;
import java.util.List;

/**
 * <P>InferenceWorker is designed to...
 *
 * @author tbrussea, zelal
 * @since July 27, 2005, 11:40 AM
 * @version $Id: InferenceWorker.java 173230 2017-08-10 00:13:28Z nwinant $
 */
public interface InferenceWorker extends Worker {

  //void setSublCommand(CycList cycList);

  int getInferenceId();
  
  int getProblemStoreId();
  
  void releaseInferenceResources(long timeoutMsecs) throws CycConnectionException, 
    CycTimeOutException, CycApiException;

  int getAnswersCount();
  
  Object getAnswerAt(int index);
  
  List getAnswers();
  
  List getAnswers(int startIndex, int endIndex);
  
  void interruptInference(); // with infinite patience
  
  void interruptInference(int patience); // with some amount of patience

  void continueInference(InferenceParameters queryProperties);
  
  InferenceStatus getInferenceStatus();
  
  InferenceSuspendReason getSuspendReason();
  
  public Object[] getInferenceListeners();
  
  void addInferenceListener(InferenceWorkerListener inferenceListener);
  
  void removeInferenceListener(InferenceWorkerListener inferenceListener);
  
  void removeAllInferenceListeners();

  public InferenceIdentifier getInferenceIdentifier();
  
}
