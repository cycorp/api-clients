package com.cyc.baseclient.util;

/*
 * #%L
 * File: CommUtils.java
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
import com.cyc.base.CycAccess;
import com.cyc.base.connection.Worker;
import com.cyc.base.cycobject.CycList;
import com.cyc.base.cycobject.CycObject;
import com.cyc.base.exception.BaseClientRuntimeException;
import com.cyc.base.exception.CycApiException;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.base.exception.CycTimeOutException;
import com.cyc.base.inference.InferenceWorkerSynch;
import com.cyc.baseclient.CycObjectFactory;
import com.cyc.baseclient.DefaultSublWorker;
import com.cyc.baseclient.connection.CycConnectionImpl;
import com.cyc.baseclient.connection.DefaultSublWorkerSynch;
import com.cyc.baseclient.connection.SublWorkerSynch;
import com.cyc.baseclient.cycobject.CycArrayList;
import com.cyc.baseclient.cycobject.CycSymbolImpl;
import com.cyc.baseclient.cycobject.DefaultCycObjectImpl;
import com.cyc.baseclient.exception.CycTaskInterruptedException;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.cyc.base.connection.Worker.EXPECT_INCREMENTAL_RESULTS;

//// External Imports

/**
 * <P>CommUtils is designed to...
 * <p>
 * @author tbrussea
 * @since Tue Aug  7 15:50:28 CDT 2007
 * @version $Id: CommUtils.java 173230 2017-08-10 00:13:28Z nwinant $
 */
public final class CommUtils {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(CommUtils.class);
  
  //// Constructors
  
  /** Creates a new instance of CommUtils. */
  private CommUtils() {}
  
  //// Public Area
  
  static public Object performApiCommand(String command, CycAccess cyc)  
  throws CycConnectionException, CycTimeOutException, CycApiException, CycTaskInterruptedException {
    return performApiCommand(command, cyc, "About to perform API command: ", 0);
  }
  
  static public Object performApiCommand(CycArrayList command, CycAccess cyc)  
  throws CycConnectionException, CycTimeOutException, CycApiException, CycTaskInterruptedException {
    return performApiCommand(command, cyc, "About to perform API command: ", 
      0, CycConnectionImpl.NORMAL_PRIORITY);
  }
  
  static public Object performApiCommand(CycArrayList command, CycAccess cyc, Integer priority)  
  throws CycConnectionException, CycTimeOutException, CycApiException, CycTaskInterruptedException {
    return performApiCommand(command, cyc, "About to perform API command: ", 0, priority);
  }
  
  static public Object performApiCommand(String command,
      CycAccess cyc, String taskDescription, long timeoutMsecs)  
  throws CycConnectionException, CycTimeOutException, CycApiException, CycTaskInterruptedException  {
    SublWorkerSynch worker = (SublWorkerSynch)CommUtils.
      makeSublWorker(command, cyc, false, timeoutMsecs, true);
    return performApiCommand(worker, taskDescription);
  }
  
  static public Object performApiCommand(CycArrayList command, CycAccess cyc,
      String taskDescription, long timeoutMsecs, Integer priority)  
  throws CycConnectionException, CycTimeOutException, CycApiException, CycTaskInterruptedException  {
    SublWorkerSynch worker = (SublWorkerSynch)CommUtils.
      makeSublWorker(command, cyc, false, timeoutMsecs, true, priority);
    return performApiCommand(worker, taskDescription);
  }

  static public Object performApiCommand(SublWorkerSynch command) 
  throws CycConnectionException, CycTimeOutException, CycApiException, CycTaskInterruptedException {
    return performApiCommand(command, "About to perform API command: ");
  }

  static public Object performApiCommand(SublWorkerSynch command, String taskDescription) 
  throws CycConnectionException, CycTimeOutException, CycApiException, CycTaskInterruptedException {
    LOGGER.debug("{}\n{}", taskDescription, command);
    long startTime = System.currentTimeMillis();
    final Object  apiResult = command.getWork();
    long endTime = System.currentTimeMillis();
    LOGGER.debug("Command took: {}ms.", (endTime - startTime));
    LOGGER.trace("{}", apiResult);
    return apiResult;
  }
  
  static public List performInference(InferenceWorkerSynch command) 
  throws CycConnectionException, CycTimeOutException, CycApiException {
    return performInference(command, "About to start inference: ");
  }
  
  static public List performInference(InferenceWorkerSynch command, String taskDescription) 
  throws CycConnectionException, CycTimeOutException, CycApiException {
    LOGGER.debug("{}\n{}", taskDescription, command);
    final List answers = command.performSynchronousInference();
    LOGGER.trace("Got answers: {}", answers);
    return answers;
  }
  
  static public void startAsynchApiCommand(Worker command) 
  throws CycConnectionException, CycTimeOutException, CycApiException {
    startAsynchApiCommand(command, "About to start API command: ");
  }
  
  static public void startAsynchApiCommand(Worker command, String taskDescription) 
  throws CycConnectionException, CycTimeOutException, CycApiException {
    LOGGER.debug("{}\n{}", taskDescription, command);
    command.start();
  }
  
  public static Worker makeSublWorker(String command, 
      CycAccess cyc, boolean asynch, long timeoutMsecs) {
    return makeSublWorker(command, cyc, asynch, timeoutMsecs, true, CycConnectionImpl.NORMAL_PRIORITY);
  }
  
  public static Worker makeSublWorker(String command, 
      CycAccess cyc, boolean asynch, long timeoutMsecs, Integer priority) {
    return makeSublWorker(command, cyc, asynch, timeoutMsecs, true, priority);
  }
  
  public static Worker makeSublWorker(String command, CycAccess cyc, boolean asynch, long timeoutMsecs,
      boolean wrapBookkeeping, Integer priority) {
    command = processCommand(command, cyc, wrapBookkeeping);
    final CycList cmdList = cyc.getObjectTool().makeCycList(command);
    return asynch ? new DefaultSublWorker(cmdList, cyc, !EXPECT_INCREMENTAL_RESULTS, timeoutMsecs, priority)
                   : new DefaultSublWorkerSynch(cmdList, cyc, timeoutMsecs, priority);
  }
  
  public static Worker makeSublWorker(CycArrayList command, 
      CycAccess cyc, boolean asynch, long timeoutMsecs) {
    return makeSublWorker(command, cyc, asynch, timeoutMsecs, true);
  }
  
  public static Worker makeSublWorker(CycArrayList command, 
      CycAccess cyc, boolean asynch, long timeoutMsecs, Integer priority) {
    return makeSublWorker(command, cyc, asynch, timeoutMsecs, true, priority);
  }
  
  public static Worker makeSublWorker(String command, 
      CycAccess cyc, boolean asynch, long timeoutMsecs,
      boolean wrapBookkeeping) {
    command = processCommand(command, cyc, wrapBookkeeping);
    return asynch ? new DefaultSublWorker(command, cyc, !EXPECT_INCREMENTAL_RESULTS, timeoutMsecs) : 
      new DefaultSublWorkerSynch(command, cyc, timeoutMsecs);
  }
  
  public static Worker makeSublWorker(CycList command, 
      CycAccess cyc, boolean asynch, long timeoutMsecs,
      boolean wrapBookkeeping) {
    command = processCommand(command, cyc, wrapBookkeeping);
    return asynch ? new DefaultSublWorker(command, cyc, timeoutMsecs) : 
      new DefaultSublWorkerSynch(command, cyc, timeoutMsecs);
  }
 
  public static Worker makeSublWorker(CycList command, 
      CycAccess cyc, boolean asynch, long timeoutMsecs,
      boolean wrapBookkeeping, Integer priority) {
    command = processCommand(command, cyc, wrapBookkeeping);
    return asynch ? new DefaultSublWorker(command, cyc, timeoutMsecs, priority) : 
      new DefaultSublWorkerSynch(command, cyc, timeoutMsecs, priority);
  }
  
  static public String getSubLForBoolean(boolean val) {
    return (val == false) ? "nil" : "T";
  }
  
  static public CycSymbolImpl getCycSymbolForBoolean(boolean val) {
    return (val == false) ? CycObjectFactory.nil : CycObjectFactory.t;
  }
  
  static public boolean getBooleanFromSubLString(String val) {
    return (val.toUpperCase(Locale.ENGLISH).equals("NIL")) ? false : true;
  }
    
  // For compatibility with Cyccyc.converseBoolean
  static public boolean convertResponseToBoolean(Object response) {
    if (response.toString().toUpperCase(Locale.ENGLISH).equals("T")) {
      return true;
    } else {
      return false;
    }
  }
  
  // For compatibility with CycClient.converseCycObject
  static public CycObject convertResponseToCycObject(Object response) {
    if (response.equals(CycObjectFactory.nil)) {
      return new CycArrayList();
    } else {
      return (CycObject) response;
    }
  }
  
  // For compatibility with CycClient.converseInt
  static public int convertResponseToInt(Object response) {
    return (new Integer(response.toString())).intValue();
  }
  
  // For compatibility with CycClient.converseList
  static public CycArrayList convertResponseToCycList(Object response, Object command) {  
    if (response.equals(CycObjectFactory.nil)) {
      return new CycArrayList();
    } else {
      if (response instanceof CycArrayList) {
        return (CycArrayList) response;
      }
    }
    String request;
    if (command instanceof CycArrayList) {
      request = ((CycArrayList) command).cyclify();
    } else {
      request = (String) command;
    }
    throw new CycApiException(response.toString() + "\nrequest: " + request);
  }
  
  // For compatibility with CycClient.converseString
  static public String convertResponseToString(Object response, Object command) {
    if (!(response instanceof String)) {
      throw new BaseClientRuntimeException("Expected String but received (" 
        + response.getClass() + ") " + response + "\n in response to command " + command);
    }
    return (String) response;
  }

  static public String composeApiCommand(String function, Object... arguments) {
    StringBuilder apiCmd = new StringBuilder(256);
    apiCmd.append('(');
    apiCmd.append(function);
    for (Object arg : arguments) {
      apiCmd.append(" ");
      apiCmd.append(DefaultCycObjectImpl.stringApiValue(arg));
    }
    apiCmd.append(')');
    return apiCmd.toString();
  }
  
  static public String composeMultipleApiCommands(String... commands) {
    StringBuilder apiCmd = new StringBuilder(256);
    apiCmd.append("(LIST ");
    for (Object command : commands) {
      apiCmd.append(command);
    }
    apiCmd.append(')');
    return apiCmd.toString();
  }
  
  private static String processCommand(String command, CycAccess cyc, boolean wrapBookkeeping) {
    return (wrapBookkeeping) 
           ? cyc.converse().wrapBookkeeping(command) 
           : command;
  }
  
  private static CycList processCommand(CycList command, CycAccess cyc, boolean wrapBookkeeping) {
    return (wrapBookkeeping)
           ? cyc.getObjectTool().makeCycList(cyc.converse().wrapBookkeeping("" + command.cyclify()))
           : command;
  }
  
  //// Main
  
}
