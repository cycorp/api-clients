package com.cyc.baseclient.inference;

/*
 * #%L
 * File: DefaultInferenceWorker.java
 * Project: Base Client
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

//// External Imports
import com.cyc.base.CycAccess;
import com.cyc.base.CycAccessManager;
import com.cyc.base.connection.WorkerEvent;
import com.cyc.base.connection.WorkerListener;
import com.cyc.base.cycobject.CycList;
import com.cyc.base.cycobject.CycObject;
import com.cyc.base.cycobject.CycSymbol;
import com.cyc.base.cycobject.ElMt;
import com.cyc.base.cycobject.FormulaSentence;
import com.cyc.base.exception.BaseClientRuntimeException;
import com.cyc.base.exception.CycApiException;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.base.exception.CycTimeOutException;
import com.cyc.base.inference.InferenceWorker;
import com.cyc.base.inference.InferenceWorkerListener;
import com.cyc.baseclient.CommonConstants;
import com.cyc.baseclient.CycObjectFactory;
import com.cyc.baseclient.DefaultSublWorker;
import com.cyc.baseclient.connection.CycConnectionImpl;
import com.cyc.baseclient.connection.DefaultSublWorkerSynch;
import com.cyc.baseclient.connection.SublWorkerSynch;
import com.cyc.baseclient.cycobject.CycArrayList;
import com.cyc.baseclient.cycobject.CycSymbolImpl;
import com.cyc.baseclient.inference.params.DefaultInferenceParameters;
import com.cyc.baseclient.parser.CyclParserUtil;
import com.cyc.query.InferenceStatus;
import com.cyc.query.InferenceSuspendReason;
import com.cyc.query.parameters.InferenceParameters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventListener;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.event.EventListenerList;

/**
 * <P>DefaultInferenceWorker is designed to...
 <p>
 * @author tbrussea, zelal
 * @since July 27, 2005, 11:55 AM
 * @version $Id: DefaultInferenceWorker.java 176591 2018-01-09 17:27:27Z nwinant $
 */
public class DefaultInferenceWorker extends DefaultSublWorker implements InferenceWorker {

  //// Constructors
  /**
   * Creates a new instance of DefaultInferenceWorker.
   * @param query
   * @param mt
   * @param queryProperties
   * @param access
   * @param timeoutMsecs
   */
  public DefaultInferenceWorker(String query, ElMt mt, InferenceParameters queryProperties,
          CycAccess access, long timeoutMsecs) {
    this(makeCycLSentence(query, access), mt, queryProperties,
            access, timeoutMsecs, CycConnectionImpl.NORMAL_PRIORITY);
  }

  /**
   * Creates a new instance of DefaultInferenceWorker.
   * @param query
   * @param mt
   * @param queryProperties
   * @param access
   * @param timeoutMsecs
   */
  public DefaultInferenceWorker(CycList query, ElMt mt, InferenceParameters queryProperties,
          CycAccess access, long timeoutMsecs) {
    this(query, mt, queryProperties, access, timeoutMsecs,
            CycConnectionImpl.NORMAL_PRIORITY);
  }

  /**
   * Creates a new instance of DefaultInferenceWorker.
   * @param query
   * @param mt
   * @param queryProperties
   * @param access
   * @param timeoutMsecs
   */
  public DefaultInferenceWorker(FormulaSentence query, ElMt mt, InferenceParameters queryProperties,
          CycAccess access, long timeoutMsecs) {
    this(query.getArgs(), mt, queryProperties, access, timeoutMsecs);
  }

  /**
   * Creates a new instance of DefaultInferenceWorker.
   * @param query
   * @param mt
   * @param queryProperties
   * @param access
   * @param timeoutMsecs
   * @param priority
   */
  public DefaultInferenceWorker(String query, ElMt mt, InferenceParameters queryProperties,
          CycAccess access, long timeoutMsecs, Integer priority) {
    this(makeCycLSentence(query, access), mt, queryProperties,
            DEFAULT_NL_GENERATION_PROPERTIES, null, false, access, timeoutMsecs, priority);
  }

  /**
   * Creates a new instance of DefaultInferenceWorker.
   * @param query
   * @param mt
   * @param queryProperties
   * @param access
   * @param timeoutMsecs
   * @param priority
   */
  public DefaultInferenceWorker(CycList query, ElMt mt, InferenceParameters queryProperties,
          CycAccess access, long timeoutMsecs, Integer priority) {
    super(access.getObjectTool().makeCycList(createInferenceCommand(query, mt, queryProperties,
            DEFAULT_NL_GENERATION_PROPERTIES, null, false, access)), access, true, timeoutMsecs, priority);
    init();
  }

  /**
   * Creates a new instance of DefaultInferenceWorker.
   * @param query
   * @param mt
   * @param queryProperties
   * @param access
   * @param timeoutMsecs
   * @param priority
   */
  public DefaultInferenceWorker(FormulaSentence query, ElMt mt, InferenceParameters queryProperties,
          CycAccess access, long timeoutMsecs, Integer priority) {
    super(access.getObjectTool().makeCycList(createInferenceCommandInternal(query, mt, queryProperties,
            DEFAULT_NL_GENERATION_PROPERTIES, null, false, access)), access, true, timeoutMsecs, priority);
    init();
  }

  /**
   * Creates a new instance of DefaultInferenceWorker.
   * @param query
   * @param mt
   * @param queryProperties
   * @param nlGenerationProperties
   * @param answerProcessingFunction
   * @param optimizeVariables
   * @param access
   * @param timeoutMsecs
   */
  public DefaultInferenceWorker(String query, ElMt mt, InferenceParameters queryProperties,
          Map nlGenerationProperties, CycSymbol answerProcessingFunction,
          boolean optimizeVariables, CycAccess access, long timeoutMsecs) {
    this(makeCycLSentence(query, access), mt, queryProperties, nlGenerationProperties,
            answerProcessingFunction, optimizeVariables, access, timeoutMsecs,
            CycConnectionImpl.NORMAL_PRIORITY);
  }

  /**
   * Creates a new instance of DefaultInferenceWorker with normal priority.
   * @param query
   * @param mt
   * @param queryProperties
   * @param nlGenerationProperties
   * @param answerProcessingFunction
   * @param optimizeVariables
   * @param access
   * @param timeoutMsecs
   */
  public DefaultInferenceWorker(CycList query, ElMt mt, InferenceParameters queryProperties,
          Map nlGenerationProperties, CycSymbol answerProcessingFunction,
          boolean optimizeVariables, CycAccess access, long timeoutMsecs) {
    this(query, mt, queryProperties, nlGenerationProperties, answerProcessingFunction,
            optimizeVariables, access, timeoutMsecs, CycConnectionImpl.NORMAL_PRIORITY);
  }

  /**
   * Creates a new instance of DefaultInferenceWorker.
   * @param query
   * @param mt
   * @param queryProperties
   * @param nlGenerationProperties
   * @param answerProcessingFunction
   * @param optimizeVariables
   * @param access
   * @param timeoutMsecs
   */
  public DefaultInferenceWorker(FormulaSentence query, ElMt mt, InferenceParameters queryProperties,
          Map nlGenerationProperties, CycSymbol answerProcessingFunction,
          boolean optimizeVariables, CycAccess access, long timeoutMsecs) {
    this(query.getArgs(), mt, queryProperties, nlGenerationProperties, answerProcessingFunction,
            optimizeVariables, access, timeoutMsecs);
  }

  /**
   * Creates a new instance of DefaultInferenceWorker.
   * @param query
   * @param mt
   * @param queryProperties
   * @param nlGenerationProperties
   * @param answerProcessingFunction
   * @param optimizeVariables
   * @param access
   * @param timeoutMsecs
   * @param priority
   */
  public DefaultInferenceWorker(CycList query, ElMt mt, InferenceParameters queryProperties,
          Map nlGenerationProperties, CycSymbol answerProcessingFunction,
          boolean optimizeVariables, CycAccess access, long timeoutMsecs, Integer priority) {
    super(access.getObjectTool().makeCycList(createInferenceCommand(query, mt, queryProperties,
            nlGenerationProperties, answerProcessingFunction, optimizeVariables, access)),
            access, true, timeoutMsecs, priority);
    this.answerProcessingFunction = answerProcessingFunction;
    init();
  }

  /**
   * Creates a new instance of DefaultInferenceWorker.
   * @param query
   * @param mt
   * @param queryProperties
   * @param nlGenerationProperties
   * @param answerProcessingFunction
   * @param optimizeVariables
   * @param access
   * @param timeoutMsecs
   * @param priority
   */
  public DefaultInferenceWorker(FormulaSentence query, ElMt mt, InferenceParameters queryProperties,
          Map nlGenerationProperties, CycSymbol answerProcessingFunction,
          boolean optimizeVariables, CycAccess access, long timeoutMsecs, Integer priority) {
    super(access.getObjectTool().makeCycList(createInferenceCommandInternal(query, mt, queryProperties,
            nlGenerationProperties, answerProcessingFunction, optimizeVariables, access)),
            access, true, timeoutMsecs, priority);
    this.answerProcessingFunction = answerProcessingFunction;
    init();
  }

  //// Public Area
  @Override
  public void releaseInferenceResources(long timeoutMsecs)
          throws CycConnectionException, CycTimeOutException, CycApiException {
    abort();
    SublWorkerSynch subLWorker = new DefaultSublWorkerSynch("(destroy-problem-store "
                                                            + "(find-problem-store-by-id " + getProblemStoreId() + "))",
            getCycAccess(), timeoutMsecs);
    subLWorker.getWork();
  }

  public static void releaseAllInferenceResourcesForClient(CycAccess cycAccess, long timeoutMsecs)
          throws CycConnectionException, CycTimeOutException, CycApiException {
    SublWorkerSynch subLWorker = new DefaultSublWorkerSynch("(open-cyc-release-inference-resources-for-client)", cycAccess, timeoutMsecs);
    subLWorker.getWork();
  }

  /**
   * Returns all the InferenceWorkerListeners listening in on this
 InferenceWorker's events
   * @return all the InferenceWorkerListeners listening in on this
 InferenceWorker's events
   */
  @Override
  public Object[] getInferenceListeners() {
    synchronized (inferenceListeners) {
      return inferenceListeners.getListeners(inferenceListenerClass);
    }
  }

  /**
   * Adds the given listener to this InferenceWorker.
   * @param listener the listener that wishes to listen
 for events sent out by this InferenceWorker
   */
  @Override
  public void addInferenceListener(InferenceWorkerListener listener) {
    synchronized (inferenceListeners) {
      inferenceListeners.add(inferenceListenerClass, listener);
    }
  }

  /**
   * Removes the given listener from this InferenceWorker.
   * @param listener the listener that no longer wishes
 to receive events from this InferenceWorker
   */
  @Override
  public void removeInferenceListener(InferenceWorkerListener listener) {
    synchronized (inferenceListeners) {
      inferenceListeners.remove(inferenceListenerClass, listener);
    }
  }

  /** Removes all listeners from this InferenceWorker. */
  @Override
  public void removeAllInferenceListeners() {
    synchronized (inferenceListeners) {
      Object[] listenerArray = inferenceListeners.getListenerList();
      for (int i = 0, size = listenerArray.length; i < size; i += 2) {
        inferenceListeners.remove((Class) listenerArray[i],
                (EventListener) listenerArray[i + 1]);
      }
    }
  }

  //public void continueInference() {
  //  throw new UnsupportedOperationException("continueInference() needs to be implemented.");
  //}
  
  /**
   * Issue a request that the inference stop running. May take arbitrarily
   * long to actually halt. May be continuable after it halts.
   * @see com.cyc.baseclient.inference.DefaultInferenceWorker#interruptInference(int) 
   * @see com.cyc.baseclient.inference.DefaultInferenceWorker#continueInference(com.cyc.query.parameters.InferenceParameters) 
   */
  @Override
  public void interruptInference() {
    interruptInference(null);
  }

  /**
   * Interrupt inference with specified patience.
   * @param patience After this many seconds, if it has not halted, inference will be aborted.
   */
  @Override
  public void interruptInference(int patience) {
    interruptInference(new Integer(patience));
  }

  protected void interruptInference(Integer patience) {
    String command = createInferenceInterruptCommand(patience);
    DefaultSublWorker newWorker = new DefaultSublWorker(
            command, getCycAccess(), EXPECT_INCREMENTAL_RESULTS, NO_TIMEOUT);
    WorkerListener listener = new WorkerListener() {

      @Override
      public void notifySublWorkerStarted(WorkerEvent event) {
      }

      @Override
      public void notifySublWorkerDataAvailable(WorkerEvent event) {
      }

      @Override
      public void notifySublWorkerTerminated(WorkerEvent event) {
//      System.out.println("Inference Interrupted "+event.getStatus()+" "+event.getWork());
      }
    };
    newWorker.addListener(listener);
    try {
      //System.out.println("running "+command);
      newWorker.start();
      //Object result = newWorker.getWork();
      //System.out.println(result);
    } catch (CycConnectionException cce) {
      throw new BaseClientRuntimeException("Failed to continue inference.", cce);
    }
  }

  @Override
  public void continueInference(InferenceParameters queryProperties) {
    String command = createInferenceContinuationCommand(queryProperties);
    DefaultSublWorker newWorker = new DefaultSublWorker(
            command, getCycAccess(), EXPECT_INCREMENTAL_RESULTS, getTimeoutMsecs());
    /*newWorker.addListener(new WorkerListener() {
    public void notifySublWorkerStarted(SublWorkerEvent event) {}
    public void notifySublWorkerDataAvailable(SublWorkerEvent event) {}
    public void notifySublWorkerTerminated(SublWorkerEvent event) {}
    });*/
    newWorker.addListener(new WorkerListener() {

      @Override
      public void notifySublWorkerStarted(WorkerEvent event) {
        doSublWorkerStarted(event);
      }

      @Override
      public void notifySublWorkerDataAvailable(WorkerEvent event) {
        doSublWorkerDataAvailable(event);
      }

      @Override
      public void notifySublWorkerTerminated(WorkerEvent event) {
        doSublWorkerTerminated(event);
      }
    });
    try {
      newWorker.start();
    } catch (CycConnectionException cce) {
      throw new BaseClientRuntimeException("Failed to continue inference.", cce);
    }
    //throw new UnsupportedOperationException("continueInference() needs to be implemented.");
  }

  @Override
  public void abort() throws CycConnectionException {
    //String command = createInferenceAbortionCommand();
    //DefaultSublWorkerSynch newWorker = new DefaultSublWorkerSynch(command, getCycAccess(), false, getTimeoutMsecs());
    //newWorker.getWork();
    if (this.suspendReason == DefaultInferenceSuspendReason.INTERRUPT) {
      this.suspendReason = DefaultInferenceSuspendReason.ABORTED;
    }
    super.abort();
  }

  /**
   *
   * @param index
   * @return a specified answer
   */
  @Override
  public Object getAnswerAt(int index) {
    return answers.get(index);
  }

  /**
   *
   * @return the number of answers that this worker's inference currently has.
   */
  @Override
  public int getAnswersCount() {
    return answers.size();
  }

  /**
   *
   * @return a list of answers from this worker's inference.
   */
  @Override
  public List getAnswers() {
    synchronized (answers) {
      return new CycArrayList(answers);
    }
  }

  /**
   *
   * @param startIndex
   * @param endIndex
   * @return a sub-list of answers from this worker's inference.
   */
  @Override
  public List getAnswers(int startIndex, int endIndex) {
    return new ArrayList(answers.subList(startIndex, endIndex));
  }

  /**
   *
   * @return the ID number of this worker's inference.
   */
  @Override
  public int getInferenceId() {
    return inferenceId;
  }

  @Override
  public DefaultInferenceIdentifier getInferenceIdentifier() {
    return new DefaultInferenceIdentifier(getProblemStoreId(), getInferenceId(), getCycAccess().getCycSession());
  }

  /**
   *
   * @return the current status of this worker's inference.
   */
  @Override
  public InferenceStatus getInferenceStatus() {
    return status;
  }

  /**
   *
   * @return the ID number of the problem store for this worker.
   */
  @Override
  public int getProblemStoreId() {
    return problemStoreId;
  }

  /**
   * Returns a string representation of the InferenceWorker.
   * @return a string representation of the InferenceWorker
   */
  @Override
  public String toString() {
    return toString(2);
  }

  /** Returns a string representation of the InferenceWorker.
   * @return a string representation of the InferenceWorker
   * @param indentLength the number of spaces to preceed each line of
   * output String
   */
  @Override
  public String toString(int indentLength) {
    final String newline = System.getProperty("line.separator");
    final StringBuffer nlBuff = new StringBuffer();
    nlBuff.append(newline);
    for (int i = 1; i < indentLength; i++) {
      nlBuff.append(" ");
    }
    final String newlinePlusIndent = nlBuff.toString();
    nlBuff.append(super.toString(indentLength));
    nlBuff.append("Inference id: ").append(inferenceId).append(newlinePlusIndent);
    nlBuff.append("ProblemStore id: ").append(problemStoreId).append(newlinePlusIndent);
    nlBuff.append("Status: ").append(status).append(newlinePlusIndent);
    if (status == InferenceStatus.SUSPENDED) {
      nlBuff.append("Suspend reason: ").append(suspendReason).append(newlinePlusIndent);
    }
    nlBuff.append(getAnswersCount()).append(" answers").append(newlinePlusIndent);
    final int maxAnswersToShow = 10;
    if (getAnswersCount() > maxAnswersToShow) {
      nlBuff.append("First ").append(maxAnswersToShow).append(": ").append(newlinePlusIndent);
      for (int i = 0; i < maxAnswersToShow; i++) {
        nlBuff.append(answers.get(i)).append(newlinePlusIndent);
      }
    } else {
      for (Iterator i = answers.iterator(); i.hasNext();) {
        nlBuff.append(i.next()).append(newlinePlusIndent);
      }
    }
    return nlBuff.toString();
  }

  /**
   *
   * @return the reason this worker was suspended.
   */
  @Override
  public DefaultInferenceSuspendReason getSuspendReason() {
    return suspendReason;
  }
  //// Protected Area

  //// Private Area
  private void fireInferenceStatusChanged(final InferenceStatus oldStatus) throws BaseClientRuntimeException {
    Object[] curListeners = getInferenceListeners();
    List<Exception> errors = new ArrayList<Exception>();
    for (int i = curListeners.length - 1; i >= 0; i -= 1) {
      try {
        ((InferenceWorkerListener) curListeners[i]).notifyInferenceStatusChanged(oldStatus, status, suspendReason, this);
      } catch (Exception e) {
        errors.add(e);
      }
    }
    if (errors.size() > 0) {
      throw new BaseClientRuntimeException(errors.get(0)); // @hack
    }
  }

  private void init() {
    this.addListener(new WorkerListener() {

      @Override
      public void notifySublWorkerStarted(WorkerEvent event) {
        doSublWorkerStarted(event);
      }

      @Override
      public void notifySublWorkerDataAvailable(WorkerEvent event) {
        doSublWorkerDataAvailable(event);
      }

      @Override
      public void notifySublWorkerTerminated(WorkerEvent event) {
        doSublWorkerTerminated(event);
      }
    });
  }

  private void doSublWorkerStarted(WorkerEvent event) {
    InferenceStatus oldStatus = status;
    status = InferenceStatus.STARTED;
    Object[] curListeners = getInferenceListeners();
    List<Exception> errors = new ArrayList<Exception>();
    for (int i = curListeners.length - 1; i >= 0; i -= 1) {
      try {
        ((InferenceWorkerListener) curListeners[i]).notifyInferenceStatusChanged(oldStatus, status, null, this);
      } catch (Exception e) {
        errors.add(e);
      }
    }
    if (errors.size() > 0) {
      throw new BaseClientRuntimeException(errors.get(0)); // @hack
    }
  }

  private void doSublWorkerDataAvailable(WorkerEvent event) {
    Object obj = event.getWork();
    if ((obj == null) || (!(obj instanceof CycList))) {
      if (CycObjectFactory.nil.equals(obj)) {
        return;
      }
      throw new BaseClientRuntimeException("Got invalid result from inference: " + obj);
    }
    final CycList data = (CycList) obj;
    if (data.size() < 2) {
      throw new BaseClientRuntimeException("Got wrong number of arguments " + "from inference result: " + data);
    }
    Object obj2 = data.get(0);
    if ((obj2 == null) || (!(obj2 instanceof CycSymbol))) {
      throw new BaseClientRuntimeException("Got bad result keyword " + "from inference result: " + obj2);
    }
    CycSymbol keyword = (CycSymbol) obj2;
    final String keywordName = keyword.toCanonicalString();
    if (keywordName.equals(":INFERENCE-START")) {
      handleInferenceInitializationResult(data);
    } else if (keywordName.equals(":INFERENCE-ANSWER")) {
      handleInferenceAnswerResult(data);
    } else if (keywordName.equals(":INFERENCE-STATUS")) {
      handleInferenceStatusChangedResult(data);
    }
  }

  private void doSublWorkerTerminated(WorkerEvent event) {
    Object[] curListeners = getInferenceListeners();
    List<Exception> errors = new ArrayList<Exception>();
    for (int i = curListeners.length - 1; i >= 0; i -= 1) {
      try {
        ((InferenceWorkerListener) curListeners[i]).notifyInferenceTerminated(this, event.getException());
      } catch (Exception e) {
        errors.add(e);
      }
    }
    if (errors.size() > 0) {
      throw new BaseClientRuntimeException(errors.get(0)); // @hack
    }
  }

  private void handleInferenceInitializationResult(CycList data) {
    if (data.size() != 3) {
      throw new BaseClientRuntimeException("Got wrong number of arguments " + "from inference result (expected 3): " + data);
    }
    Object problemStoreObj = data.get(1);
    Object inferenceObj = data.get(2);
    if ((problemStoreObj == null) || (!(problemStoreObj instanceof Number))) {
      throw new BaseClientRuntimeException("Got bad inference problem store id: " + problemStoreObj);
    }
    if ((inferenceObj == null) || (!(inferenceObj instanceof Number))) {
      throw new BaseClientRuntimeException("Got bad inference id: " + inferenceObj);
    }
    problemStoreId = ((Number) problemStoreObj).intValue();
    inferenceId = ((Number) inferenceObj).intValue();
    List<Exception> errors = new ArrayList<Exception>();
    Object[] curListeners = getInferenceListeners();
    for (int i = curListeners.length - 1; i >= 0; i -= 1) {
      try {
        ((InferenceWorkerListener) curListeners[i]).notifyInferenceCreated(this);
      } catch (Exception e) {
        errors.add(e);
      }
    }
    if (errors.size() > 0) {
      throw new BaseClientRuntimeException(errors.get(0)); // @hack
    }
  }

  private void handleInferenceAnswerResult(CycList data) {
    if (data.size() != 2) {
      throw new BaseClientRuntimeException("Got wrong number of arguments " + "from inference result (expected 2): " + data);
    }
    Object newAnswers = data.get(1);
    if ((newAnswers == null) || (!(newAnswers instanceof CycList))) {
      throw new BaseClientRuntimeException("Got bad inference answers list: " + newAnswers);
    }
    answers.addAll((List) newAnswers);
    Object[] curListeners = getInferenceListeners();
    List<Exception> errors = new ArrayList<Exception>();
    for (int i = curListeners.length - 1; i >= 0; i -= 1) {
       try {
        ((InferenceWorkerListener) curListeners[i]).notifyInferenceAnswersAvailable(this, (List) newAnswers);
      } catch (Exception e) {
        errors.add(e);
      }
    }
    if (errors.size() > 0) {
      throw new BaseClientRuntimeException(errors.get(0)); // @hack
    }
  }

  private void handleInferenceStatusChangedResult(CycList data) {
    // Expected format: (:INFERENCE-STATUS <STATUS-KEYWORD> <SUSPEND-REASON>)
    if (data.size() != 3) {
      throw new BaseClientRuntimeException("Got wrong number of arguments " + "from inference status changed (expected 3): " + data);
    }
    Object statusObj = data.get(1);
    if ((statusObj == null) || (!(statusObj instanceof CycSymbol))) {
      throw new BaseClientRuntimeException("Got bad inference status: " + statusObj);
    }
    InferenceStatus newStatus = InferenceStatus.valueOf(statusObj.toString().replace(":", ""));
    InferenceStatus oldStatus = status;
    status = newStatus;
    if (status == null) {
      throw new BaseClientRuntimeException("Got bad inference status name: " + statusObj);
    }
    suspendReason = DefaultInferenceSuspendReason.fromCycSuspendReason(data.get(2));
    fireInferenceStatusChanged(oldStatus);
  }

  /**
   * (define-api open-cyc-start-continuable-query (sentence mt &optional properties
 (nl-generation-properties *default-open-cyc-nl-generation-properties*)
 inference-answer-process-function
 (incremental-results? *use-api-task-processor-incremental-results?*)
 (optimize-query-sentence-variables? t))
   **/
  protected static String createInferenceCommand(CycList query, ElMt mt,
          InferenceParameters queryProperties, Map nlGenerationProperties,
          CycSymbol answerProcessingFunction, boolean optimizeVariables, CycAccess cycAccess) {
    return createInferenceCommandInternal(query, mt, queryProperties, nlGenerationProperties,
            answerProcessingFunction, optimizeVariables, cycAccess);
  }

  private static String createInferenceCommandInternal(CycObject query, ElMt mt,
          InferenceParameters queryProperties, Map nlGenerationProperties,
          CycSymbol answerProcessingFunction, boolean optimizeVariables, CycAccess cycAccess) {
    if (queryProperties == null) {
      queryProperties = new DefaultInferenceParameters(cycAccess);
    }
    if ((answerProcessingFunction != null) && (!answerProcessingFunction.shouldQuote())) {
      answerProcessingFunction = new CycSymbolImpl(answerProcessingFunction.getPackageName(),
              answerProcessingFunction.getSymbolName());
    }
    String processingFnStr = ((answerProcessingFunction != null) ? answerProcessingFunction.stringApiValue() : "nil");
    queryProperties.put(":CONTINUABLE?", Boolean.TRUE);
    return "(open-cyc-start-continuable-query " + query.stringApiValue() + " " + mt.stringApiValue() + " " + ((CycList)queryProperties.cycListApiValue()).stringApiValue() + " " + CycArrayList.convertMapToPlist(nlGenerationProperties).stringApiValue() + " " + processingFnStr + " t " + (optimizeVariables ? "t" : "nil") + ")";
  }

  /**
   * @param patience - seconds to wait; 0 -> no patience ; null -> inf patience
   **/
  protected static String createInferenceInterruptCommand(int problemStoreId, int inferenceId, Integer patience) {
    String patienceStr = patience == null ? "NIL" : patience.toString();
    return "(cdr (list (inference-interrupt (find-inference-by-ids "
            + problemStoreId + " " + inferenceId + ") " + patienceStr + ")))";
  }

  protected String createInferenceInterruptCommand(Integer patience) {
    return DefaultInferenceWorker.createInferenceInterruptCommand(
            problemStoreId, inferenceId, patience);
  }

  /**
   * (define-api open-cyc-continue-query (problem-store-id inference-id properties
 &optional (nl-generation-properties *default-open-cyc-nl-generation-properties*)
 inference-answer-process-function
 (incremental-results? *use-api-task-processor-incremental-results?*))
   **/
  protected String createInferenceContinuationCommand(InferenceParameters queryProperties) {
    if (queryProperties == null) {
      queryProperties = new DefaultInferenceParameters(getCycAccess());
    }
    if ((answerProcessingFunction != null) && (!answerProcessingFunction.shouldQuote())) {
      answerProcessingFunction = new CycSymbolImpl(answerProcessingFunction.getPackageName(),
              answerProcessingFunction.getSymbolName());
    }
    String processingFnStr = ((answerProcessingFunction != null) ? answerProcessingFunction.stringApiValue() : "nil");
    queryProperties.put(":CONTINUABLE?", Boolean.TRUE);
    return "(cdr (list (open-cyc-continue-query " + problemStoreId + " " + inferenceId + " " + queryProperties.stringApiValue() + " nil " + processingFnStr + " t)))";
  }

  protected String createInferenceAbortionCommand() {
    return "(cdr (list (inference-abort (find-inference-by-ids "
            + problemStoreId + " " + inferenceId + "))))";
  }
  
  //// Private
  
  private static FormulaSentence makeCycLSentence(String query, CycAccess access) {
    try {
      return CyclParserUtil.parseCycLSentence(query, true, access);
    } catch (Exception e) {
      throw new BaseClientRuntimeException(e);
    }
  }
  
  //// Internal Rep
  
  private volatile int problemStoreId;
  private volatile int inferenceId;
  private volatile InferenceStatus status = InferenceStatus.NOT_STARTED;
  private List answers = Collections.synchronizedList(new ArrayList());
  /** This holds the list of registered WorkerListener listeners. */
  final private EventListenerList inferenceListeners = new EventListenerList();
  private static Class inferenceListenerClass = InferenceWorkerListener.class;
  private volatile DefaultInferenceSuspendReason suspendReason = null;
  protected CycSymbol answerProcessingFunction;
  static private Map DEFAULT_NL_GENERATION_PROPERTIES = Collections.emptyMap();

  //// Main
  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    try {
      CycAccess access = CycAccessManager.getCurrentAccess();
      InferenceWorker worker = new DefaultInferenceWorker("(#$isa ?X #$Dog)",
              CommonConstants.INFERENCE_PSC, null, access, 10000);
      worker.addInferenceListener(new InferenceWorkerListener() {

        @Override
        public void notifyInferenceCreated(InferenceWorker inferenceWorker) {
          System.out.println("GOT CREATED EVENT\n" + inferenceWorker);
        }

        @Override
        public void notifyInferenceStatusChanged(InferenceStatus oldStatus, InferenceStatus newStatus,
                InferenceSuspendReason suspendReason, InferenceWorker inferenceWorker) {
          System.out.println("GOT STATUS CHANGED EVENT\n" + inferenceWorker);
        }

        @Override
        public void notifyInferenceAnswersAvailable(InferenceWorker inferenceWorker, List newAnswers) {
          System.out.println("GOT NEW ANSWERS EVENT\n" + inferenceWorker);
        }

        @Override
        public void notifyInferenceTerminated(InferenceWorker inferenceWorker, Exception e) {
          System.out.println("GOT TERMINATED EVENT\n" + inferenceWorker);
          if (e != null) {
            e.printStackTrace();
          }
          System.exit(0);
        }
      });
      worker.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
