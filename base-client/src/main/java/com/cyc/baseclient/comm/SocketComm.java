package com.cyc.baseclient.comm;

/*
 * #%L
 * File: SocketComm.java
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

import com.cyc.baseclient.exception.CommException;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.base.connection.CycConnection;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import com.cyc.baseclient.connection.CycConnectionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <P>SocketComm is a default implementation of the Comm API that uses socket
 * communications.
 *
 * <P>Ex.:
 * <CODE><PRE>
  Comm comm = new SocketComm(CycAddress.DEFAULT.getHostName(), CycAddress.DEFAULT.getBasePort());
  CycClient cycAccess = CycClient.get(comm);
 </CODE></PRE>
 *
 * @author tbrussea, May 7, 2013, 12:19:20 PM
 * @version $Id: SocketComm.java 176591 2018-01-09 17:27:27Z nwinant $
 */
public class SocketComm extends AbstractComm implements Comm {

  private static final Logger LOGGER = LoggerFactory.getLogger(SocketComm.class);

  private final String hostName;
  private final int port;
  private Socket socket;
  private InputStream inputStream;
  private CommOutputStream outputStream;
  private boolean isInitialized = false;

  public SocketComm(String hostName, int portNum) throws IOException {
    this.hostName = hostName;
    this.port = portNum + CycConnectionImpl.CFASL_PORT_OFFSET;
  }

  @Override
  public InputStream sendRequest(byte request[], String requestSummary, RequestType requestType)
          throws CommException, CycConnectionException {
    LOGGER.debug("{}", requestSummary);
    switch (requestType) {
      case INIT:
        try {
          socket = new Socket(this.hostName, this.port);
          int val1 = socket.getReceiveBufferSize();
          socket.setReceiveBufferSize(val1 * 2);
          socket.setTcpNoDelay(true);
          socket.setKeepAlive(true);
          outputStream = new DefaultCommOutputStream(socket.getOutputStream());
          
          Socket initSocket = new Socket(hostName, port);
          int val2 = initSocket.getReceiveBufferSize();
          initSocket.setReceiveBufferSize(val2 * 2);
          initSocket.setTcpNoDelay(true);
          initSocket.setKeepAlive(true);
          InputStream initInputStream = inputStream = initSocket.getInputStream();
          CommOutputStream initOutputStream = new DefaultCommOutputStream(initSocket.getOutputStream());
          synchronized (initOutputStream) {
            initOutputStream.write(request);
            initOutputStream.flush();
          }
          return initInputStream;
        } catch (IOException ioe) {
          throw new CycConnectionException(ioe);
        }
      case NORMAL:
        possiblyInitializeCommWIthServer();
        CommOutputStream oStream = outputStream;
        synchronized (oStream) {
          try {
            oStream.write(request);
            oStream.flush();
          } catch (IOException ioe) {
            throw new CycConnectionException(ioe);
          }
        }
        return inputStream;
      default:
        throw new IllegalArgumentException("Don't know about RequestType: " + requestType);
    }
  }

  public synchronized void possiblyInitializeCommWIthServer()
          throws CommException, CycConnectionException {
    try {
      if (isInitialized) {
        return;
      }
      isInitialized = true;
      byte[] initializationRequest = AbstractComm.getCycInitializationRequest(getCycConnection().getUuid());
      InputStream is = sendRequest(initializationRequest, makeRequestSummary("initialization request"), RequestType.INIT);
      AbstractComm.handleCycInitializationRequestResponse(is);
      getCycConnection().setupNewCommConnection(is);// @Note; this is required! It must happen after communication initialization.
    } catch (IOException ioe) {
      throw new CycConnectionException(ioe);
    }
  }

  @Override
  public String makeRequestSummary(String request) {
    return request;
  }

  @Override
  public synchronized void close() throws CycConnectionException {
    if (socket == null) {
      return;
    }
    if (!socket.isClosed()) {
      try {
        socket.close();
      } catch (IOException ioe) {
        throw new CycConnectionException(ioe);
      }
    }
  }

  @Override
  public void setCycConnection(CycConnection conn) {
    isInitialized = false;
    super.setCycConnection(conn);
  }

  @Override
  public String toString() {
    return "SocketComm with Host: " + hostName + " Port: " + port;
  }
}
