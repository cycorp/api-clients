package com.cyc.baseclient;

/*
 * #%L
 * File: DefaultSublWorker.java
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

//// Internal Imports
import com.cyc.base.CycAccess;
import com.cyc.base.CycAccessManager;
import com.cyc.base.connection.Worker;
import com.cyc.base.connection.WorkerEvent;
import com.cyc.base.connection.WorkerListener;
import com.cyc.base.connection.WorkerStatus;
import com.cyc.base.cycobject.CycList;
import com.cyc.base.exception.BaseClientRuntimeException;
import com.cyc.base.exception.CycApiException;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.base.exception.CycTimeOutException;
import com.cyc.baseclient.connection.CycConnectionImpl;
import com.cyc.baseclient.connection.SublCommandProfiler;
import com.cyc.baseclient.connection.SublWorkerEvent;
import java.io.IOException;
import java.util.EventListener;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.swing.event.EventListenerList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <P>
 * DefaultWorker is designed to provide a default implementation of a class to handle a particular
 * communication event with a Cyc server. Workers by default handle communications asynchronously
 * with callbacks through the WorkerListener interface. DefaultWorker provides the default
 * implementation while WorkerSynch and DefaultWorkerSynch provide synchronous communications
 * capabilities -- and are somewhat easier to use when synchronous communications are desired.
 * Currently, Workers are one-shot, i.e., a new Worker needs to be created for every new
 * communication. Workers are cancelable, time-outable and provide means for incremental return
 * results. Note, all callbacks happen in a single communications thread -- if you need to do any
 * significant work based on the results of a callback you *must* do it in a separate thread or risk
 * delaying or breaking other communications with Cyc. The SubL command
 * (post-task-info-processor-partial-results <data>) is used for sending back the results
 * incrementally which will cause notifyWorkerDataAvailable to be called but only if the worker was
 * created with expectIncrementalResults=true.
 *
 * <P>Example usage: 
 * <code>
 * try {
 *   CycAccess access = CycAccessManager.getAccess();
 *   Worker worker = new DefaultWorker("(+ 1 1)", access);
 *   worker.addListener(new WorkerListener() {
 *     public void notifyWorkerStarted(WorkerEvent event) {
 *       System.out.println("Received SubL Worker Event: \n" + event.toString(2) + "\n");
 *     }
 *     public void notifyWorkerDataAvailable(WorkerEvent event) {
 *       System.out.println("Received SubL Worker Event: \n" + event.toString(2) + "\n");
 *     }
 *     public void notifyWorkerTerminated(WorkerEvent event) {
 *       System.out.println("Received SubL Worker Event: \n" + event.toString(2) + "\n");
 *       System.exit(0);
 *     }
 *   });
 *   worker.start();
 * } catch (Exception e) {
 *   e.printStackTrace();
 * }
 * </code>
 * <p>
 * To perform time-duration profiling of SubL commands:  
 * <code>
 * DefaultWorker.startProfiling();
 * .... perform commands to be timed.
 * DefaultWorker.endProfiling({@code <report file path>});
 * </code>
 * <p>
 * @author tbrussea
 * @since March 25, 2004, 2:01 PM
 * @version $Id: DefaultSublWorker.java 176591 2018-01-09 17:27:27Z nwinant $
 */
public class DefaultSublWorker implements Worker {
  
  //// Public Constants
  
  /**
   * @see Worker#expectIncrementalResults() 
   */
  public static final boolean EXPECT_INCREMENTAL_RESULTS_DEFAULT = false;
  
  /**
   * @see Worker#getTimeoutMsecs() 
   */
  public static final long TIMEOUT_MSECS_DEFAULT = NO_TIMEOUT;
  
  /**
   * @see Worker#getPriority() 
   */
  public static final Integer PRIORITY_DEFAULT = CycConnectionImpl.NORMAL_PRIORITY;
  
  /**
   * @see Worker#shouldIgnoreInvalidLeases() 
   */
  public static final boolean SHOULD_IGNORE_INVALID_LEASES_DEFAULT = false;
  
  private static final Class LISTENER_CLASS = WorkerListener.class;
  
  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSublWorker.class);
  
  
  //// Private static fields
  
  /** 
   * the indicator for whether API requests should be profiled
   */
  private static boolean subLCommandProfiling = false;
  
  /**
   * the SubL command profiler that listens to each event when profiling is in effect
   */
  private static WorkerListener subLCommandProfiler = null;
  
  
  //// Instance fields
  
  /**
   * This holds the list of registered WorkerListener listeners. 
   */
  private final EventListenerList listeners = new EventListenerList();
  
  private final BlockingQueue<NotificationTask> notificationQueue = new LinkedBlockingQueue<>();
  
  private final CycList subLCommand;
  
  private final CycAccess access;
  
  private final boolean expectIncrementalResults;
  
  private final long timeoutMsecs;
  
  private final Integer priority;
  
  private final boolean shouldIgnoreInvalidLeases;
    
  private Integer id = null;
  
  private volatile WorkerStatus status = WorkerStatus.NOT_STARTED_STATUS;
  
  
  //// Constructors
  
  /**
   * Creates a new instance of DefaultSublWorker.
   *
   * @param subLCommand               the SubL command that does the work as a CycArrayList
   * @param access                    the Cyc server that should process the SubL command
   * @param expectIncrementalResults  boolean indicating whether to expect incremental results
   * @param timeoutMsecs              the max time to wait in msecs for the work to be completed
   *                                  before giving up (0 means to wait forever, and negative values
   *                                  will cause an exception to be thrown). When communications
   *                                  time out, an abort command is sent back to the Cyc server so
   *                                  processing will stop there as well.
   * @param priority                  the priority at which the worker will be scheduled on the CYC
   *                                  server side;
   * @param shouldIgnoreInvalidLeases Indicates whether this communication should be attempted even
   *                                  if the current lease to the Cyc image has expired.
   *
   * @see #getPriority()
   */
  public DefaultSublWorker(final CycList subLCommand,
                           final CycAccess access,
                           final boolean expectIncrementalResults,
                           final long timeoutMsecs, 
                           final Integer priority,
                           final boolean shouldIgnoreInvalidLeases) {
    this.subLCommand = subLCommand;
    this.access = access;
    this.expectIncrementalResults = expectIncrementalResults;
    this.timeoutMsecs = timeoutMsecs;
    this.priority = priority;
    this.shouldIgnoreInvalidLeases = shouldIgnoreInvalidLeases;
    if (subLCommandProfiler != null) {
      this.addListener(subLCommandProfiler);
    }
  }
  
  /**
   * Creates a new instance of DefaultSublWorker.
   *
   * @param subLCommand              the SubL command that does the work as a CycList
   * @param access                   the Cyc server that should process the SubL command
   * @param expectIncrementalResults boolean indicating whether to expect incremental results
   * @param timeoutMsecs             the max time to wait in msecs for the work to be completed
   *                                 before giving up (0 means to wait forever, and negative values
   *                                 will cause an exception to be thrown). When communications time
   *                                 out, an abort command is sent back to the Cyc server so
   *                                 processing will stop there as well.
   * @param priority                 the priority at which the worker will be scheduled on the CYC
   *                                 server side;
   *
   * @see #getPriority()
   */
  public DefaultSublWorker(CycList subLCommand,
                           CycAccess access,
                           boolean expectIncrementalResults,
                           long timeoutMsecs,
                           Integer priority) {
    this(subLCommand, access, expectIncrementalResults, timeoutMsecs, priority,
            SHOULD_IGNORE_INVALID_LEASES_DEFAULT);
  }
  
  /**
   * Creates a new instance of DefaultSublWorker.
   *
   * @param subLCommand              the SubL command that does the work as a CycList
   * @param access                   the Cyc server that should process the SubL command
   * @param expectIncrementalResults boolean indicating whether to expect incremental results
   * @param timeoutMsecs             the max time to wait in msecs for the work to be completed
   *                                 before giving up (0 means to wait forever, and negative values
   *                                 will cause an exception to be thrown). When communications time
   *                                 out, an abort command is sent back to the Cyc server so
   *                                 processing will stop there as well.
   *
   * @see #getPriority()
   */
  public DefaultSublWorker(CycList subLCommand,
                           CycAccess access,
                           boolean expectIncrementalResults,
                           long timeoutMsecs) {
    this(subLCommand, access, expectIncrementalResults, timeoutMsecs, PRIORITY_DEFAULT);
  }
  
  /**
   * Creates a new instance of DefaultSublWorker.
   *
   * @param subLCommand              the SubL command that does the work as a String
   * @param access                   the Cyc server that should process the SubL command
   * @param expectIncrementalResults boolean indicating whether to expect incremental results
   * @param timeoutMsec              the max time to wait in msecs for the work to be completed
   *                                 before giving up (0 means to wait forever, and negative values
   *                                 will cause an exception to be thrown). When communications time
   *                                 out, an abort command is sent back to the Cyc server so
   *                                 processing will stop there as well.
   */
  public DefaultSublWorker(String subLCommand,
                           CycAccess access,
                           
                           boolean expectIncrementalResults,
                           long timeoutMsec) {
    this(access.getObjectTool().makeCycList(subLCommand), access, expectIncrementalResults, 
            timeoutMsec);
  }
  
  /**
   * Creates a new instance of DefaultSublWorker.
   * 
   * @param subLCommand  the SubL command that does the work as a CycList
   * @param access       the Cyc server that should process the SubL command
   * @param timeoutMsecs the max time to wait in msecs for the work to be completed before giving up
   *                     (0 means to wait forever, and negative values will cause an exception to be
   *                     thrown). When communications time out, an abort command is sent back to the
   *                     Cyc server so processing will stop there as well.
   * @param priority     the priority at which the worker will be scheduled on the CYC server side;
   *
   * @see #getPriority()
   */
  public DefaultSublWorker(CycList subLCommand,
                           CycAccess access,
                           long timeoutMsecs,
                           Integer priority) {
    this(subLCommand, access, EXPECT_INCREMENTAL_RESULTS_DEFAULT, timeoutMsecs, priority);
  }
  
  /**
   * Creates a new instance of DefaultSublWorker.
   *
   * @param subLCommand              the SubL command that does the work as a CycList
   * @param access                   the Cyc server that should process the SubL command
   * @param expectIncrementalResults boolean indicating whether to expect incremental results
   */
  public DefaultSublWorker(CycList subLCommand,
                           CycAccess access, 
                           boolean expectIncrementalResults) {
    this(subLCommand, access, expectIncrementalResults, TIMEOUT_MSECS_DEFAULT);
  }
  
  /**
   * Creates a new instance of DefaultSublWorker.
   * 
   * @param subLCommand              the SubL command that does the work as a String
   * @param access                   the Cyc server that should process the SubL command
   * @param expectIncrementalResults boolean indicating whether to expect incremental results
   */
  public DefaultSublWorker(String subLCommand,
                           CycAccess access,
                           boolean expectIncrementalResults) {
    this(access.getObjectTool().makeCycList(subLCommand), access, expectIncrementalResults);
  }
  
  /**
   * Creates a new instance of DefaultSublWorker.
   *
   * @param subLCommand  the SubL command that does the work as a CycList
   * @param access       the Cyc server that should process the SubL command
   * @param timeoutMsecs the max time to wait in msecs for the work to be completed before giving up
   *                     (0 means to wait forever, and negative values will cause an exception to be
   *                     thrown). When communications time out, an abort command is sent back to the
   *                     Cyc server so processing will stop there as well.
   */
  public DefaultSublWorker(CycList subLCommand,
                           CycAccess access,
                           
                           long timeoutMsecs) {
    this(subLCommand, access, EXPECT_INCREMENTAL_RESULTS_DEFAULT, timeoutMsecs);
  }
  
  /**
   * Creates a new instance of DefaultSublWorker.
   *
   * @param subLCommand  the SubL command that does the work as a String
   * @param access       the Cyc server that should process the SubL command
   * @param timeoutMsecs the max time to wait in msecs for the work to be completed before giving up
   *                     (0 means to wait forever, and negative values will cause an exception to be
   *                     thrown). When communications time out, an abort command is sent back to the
   *                     Cyc server so processing will stop there as well.
   */
  public DefaultSublWorker(String subLCommand,
                           CycAccess access,
                           long timeoutMsecs) {
    this(access.getObjectTool().makeCycList(subLCommand), access, timeoutMsecs);
  }
  
  /**
   * Creates a new instance of DefaultSublWorker.
   *
   * @param subLCommand the SubL command that does the work as a CycList
   * @param access      the Cyc server that should process the SubL command
   */
  public DefaultSublWorker(CycList subLCommand, CycAccess access) {
    this(subLCommand, access, EXPECT_INCREMENTAL_RESULTS_DEFAULT);
  }
  
  /**
   * Creates a new instance of DefaultSublWorker.
   *
   * @param subLCommand the SubL command that does the work as a String
   * @param access      the Cyc server that should process the SubL command
   */
  public DefaultSublWorker(String subLCommand, CycAccess access) {
    this(access.getObjectTool().makeCycList(subLCommand), access);
  }
  
  // NOTE:  We don't allow constructors that take a priority (Int) but don't also take a timeout 
  //        (long). It introduces the possibility of ambiguity, and no one seems to be using it 
  //        anyway.  - nwinant, 2017-08-03
  //
  /* *
   * Creates a new instance of DefaultSublWorker.
   *
   * 
   * @param subLCommand              the SubL command that does the work as a CycList
   * @param access                   the Cyc server that should process the SubL command
   * @param expectIncrementalResults boolean indicating whether to expect incremental results
   * @param priority                 the priority at which the worker will be scheduled on the CYC
   *                                 server side;
   *
   * @see #getPriority()
   * /
  public DefaultSublWorker(CycList subLCommand,
                           CycAccess access,
                           boolean expectIncrementalResults,
                           Integer priority) {
    this(subLCommand, access, expectIncrementalResults, 0, priority);
  }
  */
  /* *
   * Creates a new instance of DefaultSublWorker.
   *
   * @param subLCommand the SubL command that does the work as a CycList
   * @param access      the Cyc server that should process the SubL command
   * @param priority    the priority at which the worker will be scheduled on the CYC server side;
   *
   * @see #getPriority()
   * /
  public DefaultSublWorker(CycList subLCommand,
                           CycAccess access,
                           Integer priority) {
    this(subLCommand, access, EXPECT_INCREMENTAL_RESULTS_DEFAULT, 0, priority);
  }
  */
  
  
  //// Public Area
  
  /** Begins profiling SubL commands. */
  public static synchronized void startProfiling() {
    subLCommandProfiling = true;
    if (subLCommandProfiler != null) {
      LOGGER.info("SubL command profiling already started.");
      return;
    }
    
    LOGGER.info("Start of SubL command profiling.");
    subLCommandProfiler = new SublCommandProfiler();
  }
  
  /** Ends the profiling SubL commands and creates the profile report.
   *
   * @param reportPath the profiling report path
   */
  public static synchronized void endProfiling(final String reportPath) throws CycConnectionException {
    if (reportPath == null)
      throw new NullPointerException("reportPath must not be null");
    if (reportPath.length() == 0)
      throw new IllegalArgumentException("reportPath must not be an empty string");
    
    subLCommandProfiling = false;
    if (subLCommandProfiler == null) {
      LOGGER.info("SubL command profiling is not active.");
      return;
    }
    
    LOGGER.info("End of SubL command profiling, writing report to {}", reportPath);
    try {
    ((SublCommandProfiler) subLCommandProfiler).report(reportPath);
    } catch (IOException ioe) {
      throw new CycConnectionException(ioe);
    }
    subLCommandProfiler = null;
  }
  
  /**
   * Returns the SubL command that will be evaluated to execute the
 work requested by this Worker.
   * @return the SubL command that will be evaluated to execute the
 work requested by this Worker
   */
  @Override
  public CycList getSublCommand() {
    return subLCommand;
  }
  
  /* *
   * Sets the SubL command that will be evaluated to execute the
 work requested by this Worker.
   * /
  public void setSublCommand(final CycList command) {
    if (getStatus().equals(WorkerStatus.NOT_STARTED_STATUS)) {
    subLCommand = command;
    } else {
      throw new UnsupportedOperationException("Worker has already started.");
    }
  }
  */
  
  /**
   * The Cyc server that should do the work.
   * @return the Cyc server that should do the work
   */
  public CycAccess getCycAccess() {
    return access;
  }
  
  /**
   * Return's the unique id for this communication. It typically won't
   * be valid until the start event has been sent out.
   * @return the unique id for this communication. It typically won't
   * be valid until the start event has been sent out.
   */
  @Override
  public Integer getId() {
    return id;
  }
  
  /**
   * This call will start the Cyc server processing the worker's SubL command.
   * @throws CycConnectionException if communications with Cyc server fail
   * @throws CycTimeOutException if the communication with Cyc takes too long
   * @throws CycApiException all other errors
   */
  @Override
  public synchronized void start() 
  throws CycConnectionException, CycTimeOutException, CycApiException {
    if (getStatus() != WorkerStatus.NOT_STARTED_STATUS) { 
      throw new BaseClientRuntimeException("This communication has already been started.");
    }
    setStatus(WorkerStatus.WORKING_STATUS);
    
    getCycClient().converseWithRetrying(this);
  }
  
  /**
   * Attempts to terminate the work being processed by the Cyc server.
   * This method should be preferred to abort() in that it tries to use
   * the natural termination event messaging infracture.
   * @throws CycConnectionException if communications with the Cyc server fails
   */
  @Override
  public void cancel() throws CycConnectionException {
    if (!(getStatus() == WorkerStatus.WORKING_STATUS)) { return; }
    CycAccess cycAccess = getCycAccess();
     synchronized (cycAccess) {
    cycAccess.getCycConnection().cancelCommunication(this);
     }
  }
  
  /**
   * Attempts to terminate the work being processed by the Cyc server.
   * This call bypasses the communications infrasture and results in
   * no new messages being sent back to the Java client.
   * @throws CycConnectionException throws IO exception if communication with Cyc server fails
   */
  @Override
  public void abort() throws CycConnectionException {
    if (!(getStatus() == WorkerStatus.WORKING_STATUS)) { return; }
    CycAccess cycAccess = getCycAccess();
     synchronized (cycAccess) {
    cycAccess.getCycConnection().abortCommunication(this);
    }
  }
  
  @Override
  public void cancelTask() {
    try {
     abort();
    } catch (CycConnectionException e) {
      throw new BaseClientRuntimeException(e);
    }
  }
  
  /**
   * Return the task's priority. This is a value that meets the
   * constraints of SL:SET-PROCESS-PRIORITY.
   * @see com.cyc.base.connection.CycConnection#MAX_PRIORITY
   * @see com.cyc.base.connection.CycConnection#CRITICAL_PRIORITY
   * @see com.cyc.base.connection.CycConnection#NORMAL_PRIORITY
   * @see com.cyc.base.connection.CycConnection#BACKGROUND_PRIORITY
   * @see com.cyc.base.connection.CycConnection#MIN_PRIORITY
   * @return the priority of the process
   */
  @Override
  public Integer getPriority() {
    return priority;
  }
  
  /**
   * Returns the current status of this Worker.
   * @return the current status of this Worker
   */
  @Override
  public WorkerStatus getStatus() { 
    return status;
  }
  
  /**
   * Returns A boolean indicating whether communications with the Cyc server on behalf of this
   * Worker have terminated
   *
   * @return a boolean indicating whether communications with the Cyc server on behalf of this
   * Worker have terminated
   */
  @Override
  public boolean isDone() {
    final WorkerStatus currStatus = getStatus();
    return !((currStatus == WorkerStatus.NOT_STARTED_STATUS) 
      || (currStatus == WorkerStatus.WORKING_STATUS));
  }
  
  /** 
   * Returns the number of msecs that this communication will wait, once
 started, before throwing a CycTimeOutException. 0 msecs means to wait forever.
   * @return the number of msecs that this communication will wait before 
 throwing a CycTimeOutException
   */
  @Override
  public long getTimeoutMsecs() { 
    return timeoutMsecs;
  }
  
  /** Returns whether this communication should expect incremental results.
   * @return whether this communication should expect incremental results
   */
  @Override
  public boolean expectIncrementalResults() {
    return expectIncrementalResults;
  }
  
  /**
   * Returns all the WorkerListeners listening in on this
 Worker's events
   * @return all the WorkerListeners listening in on this
 Worker's events
   */
  @Override
  public Object[] getListeners() {
    return listeners.getListeners(LISTENER_CLASS);
  }
  
  /**
   * Adds the given listener to this Worker.
   * @param listener the listener that wishes to listen
 for events sent out by this Worker
   */
  @Override
  public void addListener(WorkerListener listener) {
    listeners.add(LISTENER_CLASS, listener);
  }
  
  /** 
   * Removes the given listener from this Worker.
   * @param listener the listener that no longer wishes
 to receive events from this Worker
   */
  @Override
  public void removeListener(WorkerListener listener) {
     listeners.remove(LISTENER_CLASS, listener);
  }
  
  /** Removes all listeners from this Worker. */
  @Override
  public void removeAllListeners() { 
    Object[] listenerArray = listeners.getListenerList();
    for (int i = 0, size = listenerArray.length; i < size; i += 2) {
      listeners.remove((Class)listenerArray[i], 
        (EventListener)listenerArray[i+1]);
    }
  }
   
  /**
   * Returns a string representation of the Worker.
   * @return a string representation of the Worker
   */
  @Override
  public String toString() {
    return toString(2);
  }
  
  /** Returns a string representation of the Worker.
   * @return a string representation of the Worker
   * @param indentLength the number of spaces to preceed each line of 
   * output String
   */
  @Override
  public String toString(int indentLength) {
    StringBuffer nlBuff = new StringBuffer();
    nlBuff.append(System.getProperty("line.separator"));
    for (int i = 1; i < indentLength; i++) { nlBuff.append(" "); }
    String nl = nlBuff.toString();
    String sp = nl.substring(1);
    StringBuffer buf = new StringBuffer(sp + this.getClass().getName());
    buf.append(":").
      append(nl).append("Id: "). append(getId()).
      append(nl).append("Server: ").append(this.getCycAccess().toString()).
      append(nl).append("Status: ").append(getStatus().getName()).
      append(nl).append("Incremental results: ").append(expectIncrementalResults()).
      append(nl).append("Timeout: ").append(getTimeoutMsecs()).append(" msecs").
      append(nl).append("Command: \n").append(getSublCommand().toPrettyCyclifiedString("")).
      append(nl);
    return buf.toString();
  }
  
  /** 
   * Public for implementation reasons only, this method should
   * only ever be called by subclasses of CycConnection.java.
   * 
   * @param event The start event that should be transmitted to listeners of this Worker
   */
  @Override
  public void fireSublWorkerStartedEvent(WorkerEvent event) {
    if (event.getEventType() != SublWorkerEvent.STARTING_EVENT_TYPE) {
      throw new BaseClientRuntimeException("Got bad event type; " + 
        event.getEventType().getName());
    }
    setId(event.getId());
    synchronized(listeners) {
   Object[] curListeners = listeners.getListenerList();
    for (int i = curListeners.length-2; i >= 0; i -= 2) {
      if (curListeners[i] == LISTENER_CLASS) {
        try {
          ((WorkerListener)curListeners[i+1]).notifySublWorkerStarted(event);
        } catch (Exception e) {
          LOGGER.warn(e.getMessage(), e);
        }
      }
    }
    }
  }
    
  /**
   * Public for implementation reasons only, this method should
   * only ever be called by subclasses of CycConnection.java.
   * @param event The data available event that should be transmitted to
 listeners of this Worker
   */
  @Override
  public void fireSublWorkerDataAvailableEvent(WorkerEvent event) {
    if (event.getEventType() != SublWorkerEvent.DATA_AVAILABLE_EVENT_TYPE) {
      throw new BaseClientRuntimeException("Got bad event type; " + 
        event.getEventType().getName());
    } 
    synchronized(listeners) {
   Object[] curListeners = listeners.getListenerList();
    for (int i = curListeners.length-2; i >= 0; i -= 2) {
      if (curListeners[i] == LISTENER_CLASS) {
        try {
          //System.out.println("GOT DATA FOR SUBL CALL: " + event);
          ((WorkerListener)curListeners[i+1]).notifySublWorkerDataAvailable(event);
        } catch (Exception e) {
          LOGGER.warn(e.getMessage(), e);
        }
      }
    }
  }
  }
    
  /**
   * Public for implementation reasons only, this method should
   * only ever be called by subclasses of CycConnection.java.
   * @param event The termination event that should be transmitted to
 listeners of this Worker
   */
  @Override
  public void fireSublWorkerTerminatedEvent(WorkerEvent event) {
    if (event.getEventType() != SublWorkerEvent.TERMINATION_EVENT_TYPE) {
      throw new BaseClientRuntimeException("Got bad event type; " + 
        event.getEventType().getName());
    }
    setStatus(event.getStatus());
    synchronized(listeners) {
   Object[] curListeners = listeners.getListenerList();
    for (int i = curListeners.length-2; i >= 0; i -= 2) {
      if (curListeners[i] == LISTENER_CLASS) {
        try {
          ((WorkerListener)curListeners[i+1]).notifySublWorkerTerminated(event);
        } catch (Exception e) {
          LOGGER.warn(e.getMessage(), e);
        }
      }
    }
  }
  }
  
  /**
   * Indicates whether this communication should be attempted even if
   * the current lease to the Cyc image has expired.
   */  
  @Override
  public boolean shouldIgnoreInvalidLeases() {
    return shouldIgnoreInvalidLeases;
  }
  
  //// Protected Area
  /*
  protected void setShouldIgnoreInvalidLeases(boolean newVal) {
    this.shouldIgnoreInvalidLeases = newVal;
  }
  
  /** Sets the client-unique communication id for this message.
   * @param id the communication id
   */  
  private void setId(Integer id) {
    this.id = id;
  }
  
  /** Sets the current status of this Worker
   * @param status the new status value
   */  
  protected void setStatus(WorkerStatus status) {
    
    this.status = status;
  }
  
  @Override
  public BlockingQueue<NotificationTask> getNotificationQueue() {
    return notificationQueue;
  }
  
  
  //// Private Area
  
  private CycClient getCycClient() {
    return CycClientManager.getClientManager().fromCycAccess(getCycAccess());
  }
  
  
  //// Main
  
  /**
   * Example usage for this class. The SubL command (post-task-info-processor-partial-results
   * <data>) is used for sending back the results incrementally which will cause
   * notifyWorkerDataAvailable to be called but only if the worker was created with
   * expectIncrementalResults=true.
   *
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    try {
      CycAccess access = CycAccessManager.getCurrentAccess();
      Worker worker = new DefaultSublWorker("(+ 1 1)", access);
      worker.addListener(new WorkerListener() {
        public void notifySublWorkerStarted(WorkerEvent event) {
          System.out.println("Received SubL Worker Event: \n" + event.toString(2) + "\n");
        }
        @Override
        public void notifySublWorkerDataAvailable(WorkerEvent event) {
          System.out.println("Received SubL Worker Event: \n" + event.toString(2) + "\n");
        }
        @Override
        public void notifySublWorkerTerminated(WorkerEvent event) {
          System.out.println("Received SubL Worker Event: \n" + event.toString(2) + "\n");
          System.exit(0);
        }
      });
      worker.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
