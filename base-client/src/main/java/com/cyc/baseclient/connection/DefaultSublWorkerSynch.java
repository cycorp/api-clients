package com.cyc.baseclient.connection;

/*
 * #%L
 * File: DefaultSublWorkerSynch.java
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
import com.cyc.base.connection.WorkerEvent;
import com.cyc.base.connection.WorkerListener;
import com.cyc.base.connection.WorkerStatus;
import com.cyc.base.cycobject.CycList;
import com.cyc.base.exception.BaseClientRuntimeException;
import com.cyc.base.exception.CycApiException;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.base.exception.CycTimeOutException;
import com.cyc.baseclient.CycObjectFactory;
import com.cyc.baseclient.DefaultSublWorker;
import com.cyc.baseclient.cycobject.CycArrayList;
import com.cyc.baseclient.exception.CycApiInvalidObjectException;
import com.cyc.baseclient.exception.CycApiServerSideException;
import com.cyc.baseclient.exception.CycTaskInterruptedException;
import java.io.*;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * <P>SublWorkerSynch is designed to provide a handle for a particular
 * communication event with a Cyc server in a synchronous manner.
 * DefaultSublWorkerSynch provides the default
 * implementation while SublWorker and DefaultSublWorker provide
 * asynchronous communications capabilities. Currently, SublWorkerSynchs are one-shot,
 * i.e., a new SublWorkerSynch needs to be created for every new communication.
 * SublWorkerSynchs are cancelable, time-outable and provide means for incremental
 * return results.
 *
 * <P>Example usage: <code>
 try {
    CycAccess access = CycAccessManager.getAccess();
    SublWorkerSynch worker = new DefaultSublWorkerSynch("(+ 1 1)", access);
    Object work = worker.getWork();
    System.out.println("Got worker: " + worker + "\nGot result: " + work + ".");
  } catch (Exception e) {
    e.printStackTrace();
  }
 </code>
 * <p>
 * @author tbrussea
 * @since March 25, 2004, 2:01 PM
 * @version $Id: DefaultSublWorkerSynch.java 176591 2018-01-09 17:27:27Z nwinant $
 */
public class DefaultSublWorkerSynch
    extends DefaultSublWorker
    implements SublWorkerSynch, WorkerListener {
  
  //// Constructors
  
  /**
   * Creates a new instance of DefaultSublWorkerSynch.
   *
   * @param subLCommand              the SubL command that does the work as a CycArrayList
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
  public DefaultSublWorkerSynch(CycList subLCommand,
                                CycAccess access,
                                boolean expectIncrementalResults,
                                long timeoutMsecs,
                                Integer priority) {
    super(subLCommand, access, expectIncrementalResults, timeoutMsecs, priority);
    addListener(this);
  }

  /**
   * Creates a new instance of DefaultSublWorkerSynch.
   *
   * @param subLCommand  the SubL command that does the work as a CycArrayList
   * @param access       the Cyc server that should process the SubL command
   * @param timeoutMsecs the max time to wait in msecs for the work to be completed before giving up
   *                     (0 means to wait forever, and negative values will cause an exception to be
   *                     thrown). When communications time out, an abort command is sent back to the
   *                     Cyc server so processing will stop there as well.
   * @param priority     the priority at which the worker will be scheduled on the CYC server side;
   *
   * @see #getPriority()
   */
  public DefaultSublWorkerSynch(CycList subLCommand,
                                CycAccess access,
                                long timeoutMsecs,
                                Integer priority) {
    this(subLCommand, access, EXPECT_INCREMENTAL_RESULTS_DEFAULT, timeoutMsecs, priority);
  }

  /**
   * Creates a new instance of DefaultSublWorkerSynch with normal priority.
   *
   * @param subLCommand              the SubL command that does the work as a String
   * @param access                   the Cyc server that should process the SubL command
   * @param expectIncrementalResults boolean indicating whether to expect incremental results
   * @param timeoutMsec              the max time to wait in msecs for the work to be completed
   *                                 before giving up (0 means to wait forever, and negative values
   *                                 will cause an exception to be thrown). When communications time
   *                                 out, an abort command is sent back to the Cyc server so
   *                                 processing will stop there as well. on the CYC server side;
   *
   * @see com.cyc.base.connection.CycConnection#NORMAL_PRIORITY
   */
  public DefaultSublWorkerSynch(String subLCommand,
                                CycAccess access,
                                boolean expectIncrementalResults,
                                long timeoutMsec) {
    this(access.getObjectTool().makeCycList(subLCommand), access, 
            expectIncrementalResults, timeoutMsec, PRIORITY_DEFAULT);
  }

  /**
   * Creates a new instance of DefaultSublWorkerSynch.
   *
   * @param subLCommand              the SubL command that does the work as a CycArrayList
   * @param access                   the Cyc server that should process the SubL command
   * @param expectIncrementalResults boolean indicating whether to expect incremental results
   */
  public DefaultSublWorkerSynch(CycList subLCommand,
                                CycAccess access,
                                boolean expectIncrementalResults) {
    this(subLCommand, access, expectIncrementalResults, TIMEOUT_MSECS_DEFAULT, PRIORITY_DEFAULT);
  }

  /**
   * Creates a new instance of DefaultSublWorkerSynch.
   *
   * @param subLCommand              the SubL command that does the work as a String
   * @param access                   the Cyc server that should process the SubL command
   * @param expectIncrementalResults boolean indicating whether to expect incremental results
   */
  public DefaultSublWorkerSynch(String subLCommand,
                                CycAccess access,
                                boolean expectIncrementalResults) {
    this(access.getObjectTool().makeCycList(subLCommand), access, expectIncrementalResults);
  }

  /**
   * Creates a new instance of DefaultSublWorkerSynch.
   *
   * @param subLCommand  the SubL command that does the work as a CycArrayList
   * @param access       the Cyc server that should process the SubL command
   * @param timeoutMsecs the max time to wait in msecs for the work to be completed before giving up
   *                     (0 means to wait forever, and negative values will cause an exception to be
   *                     thrown). When communications time out, an abort command is sent back to the
   *                     Cyc server so processing will stop there as well.
   */
  public DefaultSublWorkerSynch(CycList subLCommand,
                                CycAccess access,
                                long timeoutMsecs) {
    this(subLCommand, access, timeoutMsecs, PRIORITY_DEFAULT);
  }

  /**
   * Creates a new instance of DefaultSublWorkerSynch.
   *
   * @param subLCommand  the SubL command that does the work as a String
   * @param access       the Cyc server that should process the SubL command
   * @param timeoutMsecs the max time to wait in msecs for the work to be completed before giving up
   *                     (0 means to wait forever, and negative values will cause an exception to be
   *                     thrown). When communications time out, an abort command is sent back to the
   *                     Cyc server so processing will stop there as well.
   */
  public DefaultSublWorkerSynch(String subLCommand,
                                CycAccess access,
                                long timeoutMsecs) {
    this(access.getObjectTool().makeCycList(subLCommand), access, timeoutMsecs);
  }

  /**
   * Creates a new instance of DefaultSublWorkerSynch.
   *
   * @param subLCommand the SubL command that does the work as a CycArrayList
   * @param access      the Cyc server that should process the SubL command
   */
  public DefaultSublWorkerSynch(CycList subLCommand, CycAccess access) {
    this(subLCommand, access, EXPECT_INCREMENTAL_RESULTS_DEFAULT);
  }

  /**
   * Creates a new instance of DefaultSublWorkerSynch.
   *
   * @param access      the Cyc server that should process the SubL command
   * @param subLCommand the SubL command that does the work as a String
   */
  public DefaultSublWorkerSynch(String subLCommand, CycAccess access) {
    this(access.getObjectTool().makeCycList(subLCommand), access);
  }
  
  // NOTE:  We don't allow constructors to take a priority (Int) but not also take a timeout (long).
  //        It introduces the possibility of ambiguity, and no one seems to be using it anyway.
  //        - nwinant, 2017-08-03
  /* *
   * Creates a new instance of DefaultSublWorkerSynch.
   *
   * @param subLCommand              the SubL command that does the work as a CycArrayList
   * @param access                   the Cyc server that should process the SubL command
   * @param expectIncrementalResults boolean indicating whether to expect incremental results
   * @param priority                 the priority at which the worker will be scheduled on the CYC
   *                                 server side;
   *
   * @see #getPriority()
   * /
  public DefaultSublWorkerSynch(CycList subLCommand,
                                CycAccess access, 
                                boolean expectIncrementalResults,
                                Integer priority) {
    this(access, subLCommand, expectIncrementalResults, 0, priority);
  }
  */
  
  
  //// Public Area
  
  /** This method starts communications with the Cyc server, waits for the work
   * to be performed, then returns the resultant work.
   * @throws IOException thown when there is a problem with the communications
   * protocol with the CycAddress
   * @throws CycTimeOutException thrown if the worker takes to long to return results
   * @throws CycApiException thrown if any other error occurs
   * @return The work produced by this SublWorkerSynch
   */
  @Override
  public Object getWork()
      throws CycConnectionException, CycTimeOutException, CycApiException, CycTaskInterruptedException {
    if (getStatus() == WorkerStatus.NOT_STARTED_STATUS) {
      start();
    }
    if (getStatus() == WorkerStatus.WORKING_STATUS) {
      try {
        boolean gotResults = true;
        if (getTimeoutMsecs() > 0) {
          gotResults = sem.tryAcquire(getTimeoutMsecs(), TimeUnit.MILLISECONDS);
        } else {
          sem.acquire();
        }
        if ((!gotResults) || (getStatus() == WorkerStatus.WORKING_STATUS)) {
          try {
            this.abort();
          } catch (CycConnectionException xcpt) {
            throw xcpt;
          } finally {
            this.fireSublWorkerTerminatedEvent(new SublWorkerEvent(this,
                    WorkerStatus.EXCEPTION_STATUS,
                    new CycTimeOutException(
                            "Communications took more than: " + getTimeoutMsecs()
                                    + " msecs.\nWhile trying to execute: \n"
                                    + getSublCommand().toPrettyCyclifiedString(""))));
          }
        }
      } catch (Exception xcpt) {
        setException(xcpt);
      }
    }
    if (getException() != null) {
      try {
        throw getException();
      } catch (CycConnectionException ioe) {
        throw ioe;
      } catch (CycTimeOutException toe) {
        throw toe;
      } catch (CycApiInvalidObjectException cae) {
        setException(new CycApiInvalidObjectException(cae.getMessage(), cae));
        throw (RuntimeException) getException();
      } catch (CycApiServerSideException cae) {
        setException(new CycApiServerSideException(cae.getMessage(), cae));
        throw (RuntimeException) getException();
      } catch (CycApiException cae) {
        setException(new CycApiException(cae.getMessage(), cae));
        throw (RuntimeException) getException();
      } catch (InterruptedException ie) {
        setException(new CycTaskInterruptedException(ie));
        throw (RuntimeException) getException();
      } catch (RuntimeException re) {
        throw re;
      } catch (Exception xcpt) {
        setException(new BaseClientRuntimeException(xcpt));
        throw (RuntimeException) getException();
      }
    }
    return work;
  }
  
  /** Ignore.
   * @param event the event object with details about this event
   */
  @Override
  public void notifySublWorkerStarted(WorkerEvent event) {}
  
  /** Saves any  available work.
   * @param event the event object with details about this event
   */
  @Override
  public void notifySublWorkerDataAvailable(WorkerEvent event) {
    appendWork(event.getWork());
  }
  
  /** Make sure to save any applicable exceptions,
   * @param event the event object with details about this event
   */
  @Override
  public void notifySublWorkerTerminated(WorkerEvent event) {
    setException(event.getException());
    sem.release();
  }
  
  /** Returns the exception thrown in the process of doing the work.
   * The value will be null if now exception has been thrown.
   * @return the exception thrown in the process of doing the work
   */
  public Exception getException() { return e; }
  
  //// Protected Area
  
  /** Sets the exception.
   * @param e The exception that was thrown while processing this worker
   */
  protected void setException(Exception e) {
    this.e = e;
  }
  
  /** Make sure to keep track of the resulting work, especially in the
   * case if incremental return results.
   * @param newWork The lastest batch of work.
   */
  protected void appendWork(Object newWork) {
    if (expectIncrementalResults()) {
      if (work == null) {
        work = new CycArrayList();
      }
      if (newWork != CycObjectFactory.nil) {
        ((List)work).addAll((List)newWork);
      }
    } else {
      work = newWork;
    }
  }
  
  //// Private Area
  
  //// Internal Rep
  
  private final Semaphore sem = new Semaphore(0);
  volatile private Object work = null;
  volatile private Exception e = null;
  
  /** For testing puposes only. */
  private static SublWorkerSynch testWorker;
  
  //// Main
  
  /* Test main method and example usage.
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    try {
      CycAccess access = CycAccessManager.getCurrentAccess();
      SublWorkerSynch worker = new DefaultSublWorkerSynch("(+ 1 1)", access);
      Object work = worker.getWork();
      System.out.println("Got worker: " + worker + "\nGot result: " + work + ".");
    } catch (Exception e) {
      e.printStackTrace();
    }
    try {
      final CycAccess access = CycAccessManager.getCurrentAccess();
      Thread workerThread = new Thread() {
        @Override
        public void run() {
          try {
            System.out.println("Starting work.");
            testWorker = new DefaultSublWorkerSynch("(do-assertions (a))", access);
            Object obj = testWorker.getWork();
            System.out.println("Finished work with " + testWorker.getStatus().getName()
            + ", received: " + obj);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      };
      workerThread.start();
      Thread.currentThread().sleep(10000);
      System.out.println("About to cancel work.");
      testWorker.cancel();
      System.out.println("Canceled work.");
      
      System.out.println("\nGiving chance to get ready ....\n");
      Thread.currentThread().sleep(1000);
      
      System.out.println( "\nOk, second round ....\n\n");
      workerThread = new Thread() {
        @Override
        public void run() {
          try {
            System.out.println("Starting work.");
            testWorker = new DefaultSublWorkerSynch("(do-assertions (a))", access);
            Object obj = testWorker.getWork();
            System.out.println("Finished work with " + testWorker.getStatus().getName()
            + ", received: " + obj);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      };
      workerThread.start();
      Thread.currentThread().sleep(10000);
      System.out.println("About to abort work.");
      testWorker.abort();
      System.out.println("Aborted work.");
      
      System.out.println("\nGiving chance to get ready ....\n");
      Thread.currentThread().sleep(1000);
      
      System.out.println( "\nOk, third round ....\n\n");
      workerThread = new Thread() {
        @Override
        public void run() {
          long timeBefore = 0, timeAfter = 0;
          try {
            System.out.println("Starting work.");
            timeBefore = System.currentTimeMillis();
            testWorker = new DefaultSublWorkerSynch("(do-assertions (a))", access, 500);
            Object obj = testWorker.getWork();
            timeAfter = System.currentTimeMillis();
            System.out.println("Finished work with " + testWorker.getStatus().getName()
            + " after " + (timeAfter - timeBefore)
            + " millisecs (should be about 500), received: " + obj);
          } catch (Exception e) {
            timeAfter = System.currentTimeMillis();
            System.out.println( "The current time is: " + (timeAfter - timeBefore)
            + " millisecs (should be about 500)");
            e.printStackTrace();
          }
        }
      };
      workerThread.start();
      Thread.currentThread().sleep(10000);
    } catch (Exception e) {
      e.printStackTrace();
    }
    System.exit(0);
  }
  
}
