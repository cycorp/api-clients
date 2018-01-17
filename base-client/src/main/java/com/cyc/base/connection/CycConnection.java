package com.cyc.base.connection;

/*
 * #%L
 * File: CycConnection.java
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
import com.cyc.base.cycobject.CycList;
import com.cyc.base.exception.CycApiException;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.base.exception.CycTimeOutException;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;
import com.cyc.session.CycAddress;

/**
 * Defines the interface for local and remote CycConnection objects<p>
 *
 * @version $Id: CycConnection.java 175363 2017-10-18 19:06:10Z nwinant $
 * @author Stephen L. Reed
 */
public interface CycConnection {
  
  //====|    Constants    |=======================================================================//

  /* * Default host name for the OpenCyc server. */
  //public static String DEFAULT_HOSTNAME = CycAddress.DEFAULT.getHostName();
  
  /* * Default base tcp port for the OpenCyc server. */
  //public static final int DEFAULT_BASE_PORT = CycAddress.DEFAULT.getBasePort();
  
  /**
   * HTTP port offset for the OpenCyc server.
   */
  public static final int HTTP_PORT_OFFSET = CycAddress.STANDARD_HTTP_PORT_OFFSET;

  /**
   * HTTP port offset for the OpenCyc webapp server.
   */
  public static final int WEBAPP_PORT_OFFSET = CycAddress.STANDARD_SERVLET_PORT_OFFSET;

  /**
   * CFASL (binary) port offset for the OpenCyc server.
   */
  public static final int CFASL_PORT_OFFSET = CycAddress.STANDARD_CFASL_PORT_OFFSET;

  /**
   * No api trace.
   */
  public static final int API_TRACE_NONE = 0;

  /**
   * Message-level api trace.
   */
  public static final int API_TRACE_MESSAGES = 1;

  /**
   * Detailed api trace.
   */
  public static final int API_TRACE_DETAILED = 2;

  /**
   * CFASL (binary) mode connection to the Cyc server (preferred).
   */
  public static final int BINARY_MODE = 2;

  /** 
   * The priorities for the task processors. These correspond to the SubL priorities for
   * <code>SL:SET-PROCESS-PRIORITY</code> 
   */
  public static final Integer MAX_PRIORITY = 10;
  
  public static final Integer CRITICAL_PRIORITY = 7;
  
  public static final Integer NORMAL_PRIORITY = 5;
  
  public static final Integer BACKGROUND_PRIORITY = 3;
  
  public static final Integer MIN_PRIORITY = 1;
  
  // @legacy
  public static final int DEFAULT_PRIORITY = NORMAL_PRIORITY;
  
  
  //====|    Methods    |=========================================================================//
  
  /**
   * Sends a message to Cyc and return the <tt>Boolean</tt> true as the first element of an object
   * array, and the cyc response Symbolic Expression as the second element. If an error occurs the
   * first element is <tt>Boolean</tt>
   * false and the second element is the error message string.
   *
   * @param message the api command
   * @throws CycConnectionException when a communication error occurs or the time limit is exceeded
   * @return an array of two objects, the first is an Integer response code, and the second is the
   * response object or error string.
   */
  public Object[] converse(Object message) throws CycConnectionException, CycApiException;

  /**
   * Sends a message to Cyc and return the response code as the first element of an object array,
   * and the cyc response Symbolic Expression as the second element, spending no less time than the
   * specified timer allows but throwing a <code>CycTimeOutException</code> at the first opportunity
   * where that time limit is exceeded. If an error occurs the second element is the error message
   * string.
   *
   * @param message the api command which must be a String or a CycList
   * @param timeout a <tt>Timer</tt> object giving the time limit for the api call
   * @throws CycConnectionException when a communication error occurs or the time limit is exceeded
   * @return an array of two objects, the first is an Integer response code, and the second is the
   * response object or error string.
   */
  public Object[] converse(Object message, Timer timeout)
          throws CycConnectionException, CycTimeOutException, CycApiException;

  /**
   * Sends a message to Cyc and return the response code as the first element of an object array,
   * and the cyc response Symbolic Expression as the second element, spending no less time than the
   * specified timer allows but throwing a <code>CycTimeOutException</code> at the first opportunity
   * where that time limit is exceeded. If an error occurs the second element is the error message
   * string. The concurrent mode of Cyc server communication is supported by Cyc's pool of
   * transaction processor threads, each of which can concurrently process an api request.
   *
   * The CFASL input and output streams are encoded in Base64 format.
   *
   * @param message the api command
   * @param timeout a <tt>Timer</tt> object giving the time limit for the api call
   * @throws CycConnectionException when a communication error occurs or the time limit is exceeded
   * @return an array of two objects, the first is an Integer response code, and the second is the
   * response object or error string.
   */
  public Object[] converseBinary(final CycList message, final Timer timeout)
          throws CycConnectionException, CycTimeOutException, CycApiException;

  /**
   * Send a message to Cyc spending no less time than the specified timer allows but throwing a
   * <code>CycTimeOutException</code> at the first opportunity where that time limit is exceeded.
   * The concurrent mode of Cyc server communication is supported by Cyc's pool of transaction
   * processor threads, each of which can concurrently process an api request. The Worker object
   * notifies the caller when the api response is aschronously received.
   *
   * @param worker a <tt>Worker</tt> object that notifies the caller when work is done
   *
   * @throws CycConnectionException when a communication error occurs or the time limit is exceeded
   * @throws CycApiException when a Cyc api error occurs
   */
  public void converseBinary(Worker worker)
          throws CycConnectionException, CycTimeOutException, CycApiException;

  /**
   * Returns connection information, suitable for diagnostics.
   *
   * @return connection information, suitable for diagnostics
   */
  public String connectionInfo();

  /**
   * Closes the api sockets and streams.
   */
  public void close();

  /**
   * Returns the trace value.
   *
   * @return the trace value
   */
  public int getTrace();

  /**
   * Returns the connection type of the parent CycAccess object.
   *
   * @return the connection type of the parent CycAccess object
   */
  public int getConnectionType();

  /**
   * Is this CycConnection a connect to a specific Cyc server that will not change over the lifetime
   * of this connection?
   *
   * @return if this CycConnection will not change
   */
  public boolean connectedToStaticCyc();

  /**
   * Sets the trace value.
   *
   * @param trace the trace value
   */
  public void setTrace(int trace);

  /**
   * Turns off the diagnostic trace of socket messages.
   */
  public void traceOff();

  /**
   * Turns on the diagnostic trace of socket messages.
   */
  public void traceOn();

  /**
   * Turns on the detailed diagnostic trace of socket messages.
   */
  public void traceOnDetailed();

  /**
   * Returns the UUID that identifies this java api client connection.
   *
   * @return the UUID that identifies this java api client connection
   */
  public UUID getUuid();

  /**
   * Returns the hostname of this connection.
   *
   * @return <code>String</code> denoting this hostname.
   */
  public String getHostName();

  /**
   * Returns the resolved hostname of this connection. If the hostname as specified in the
   * CycConnection is a loopback address (e.g. 127.0.0.1 or localhost), this will return the
   * fully-qualified domain-name of the local address.
   *
   * @return <code>String</code> denoting this hostname.
   */
  public String getResolvedHostName();

  /**
   * Returns the base port of this connection.
   *
   * @return <code>int</code> of this connection's base port.
   */
  public int getBasePort();

  /**
   * @return the http port of the connected server.
   */
  public int getHttpPort();

  /* *
   * Sets the client name of this api connection.
   *
   * @param myClientName the client name of this api connection
   */
  //public void setMyClientName(String myClientName);
  /* *
   * Gets the client name of this api connection.
   *
   * @return the client name of this api connection
   */
  //public String getMyClientname();
  /**
   * Cancels the communication associated with the given Worker.
   *
   * @param worker the given Worker
   * @throws CycConnectionException when a communication error occurs or the time limit is exceeded
   */
  public void cancelCommunication(Worker worker) throws CycConnectionException;

  /**
   * Cancels the communication associated with the given Worker.
   *
   * @param worker the given Worker
   * @throws CycConnectionException when a communication error occurs or the time limit is exceeded
   */
  public void abortCommunication(Worker worker) throws CycConnectionException;

  public void setupNewCommConnection(InputStream is) throws CycConnectionException, CycApiException;

  public Map<String, LeaseManager> getCycLeaseManagerMap();

  public void setCycLeaseManagerMap(Map<String, LeaseManager> cycLeaseManagerMap);

  public Map<InputStream, LeaseManager> getCycLeaseManagerCommMap();

  public void setCycLeaseManagerCommMap(Map<InputStream, LeaseManager> cycLeaseManagerCommMap);

}
