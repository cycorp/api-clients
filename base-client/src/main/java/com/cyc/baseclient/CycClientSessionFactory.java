package com.cyc.baseclient;

/*
 * #%L
 * File: CycClientSessionFactory.java
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

import com.cyc.base.exception.CycApiException;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.session.CycAddress;
import com.cyc.session.CycSessionConfiguration;
import com.cyc.session.configuration.ConfigurationValidator;
import com.cyc.session.exception.SessionCommunicationException;
import com.cyc.session.exception.SessionConfigurationException;
import com.cyc.session.exception.SessionInitializationException;
import com.cyc.session.exception.SessionRuntimeException;
import com.cyc.session.spi.SessionFactory;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author nwinant
 */
public class CycClientSessionFactory implements SessionFactory<CycClientSession> {
    
  private static final Logger LOGGER = LoggerFactory.getLogger(CycClientSessionFactory.class);
  //private final Map<CycAddress, CycClient> clients = new ConcurrentHashMap();
  private boolean closed = false;
  
  
  // Public
  
  @Override
  public CycClientSession createSession(CycSessionConfiguration config) throws SessionConfigurationException {
    errorIfClosed("Cannot create new session.");
    LOGGER.debug("Creating new session for {}", config);
    final ConfigurationValidator validator = new ConfigurationValidator(config);
    if (!validator.isSufficient()) {
      throw new SessionConfigurationException("Configuration is not sufficient to create a " + CycClientSession.class.getSimpleName());
    }
    return new CycClientSession(config);
  }
  
  @Override
  public CycClientSession initializeSession(CycClientSession session) throws SessionCommunicationException, SessionInitializationException {
    errorIfClosed("Cannot initialize session.");
    final CycSessionConfiguration config = session.getConfiguration();
    final CycAddress server = config.getCycAddress();
    LOGGER.debug("Initializing session for {}", server);
    try {
      CycClient client = session.setAccess(getClient(config));
      client.initializeSession(config);
    } catch (CycConnectionException ex) {
      throw ex.toSessionException("Error communicating with " + server);
    } catch (CycApiException ex) {
      throw SessionInitializationException.fromThrowable("Error initializing CycSession for " + server, ex);
    }
    // NOTE: I briefly thought of attaching SessionListeners here, but anything interesting that 
    //       could go in a SessionFactory would probably go better in the SessionManager instead,
    //       as the SessionManager should have configurable, executive control over cleanup, etc. 
    //       - nwinant, 2015-10-16
    return session;
  }
  
  @Override
  public void releaseResourcesForServer(CycAddress server) {
    LOGGER.info("Releasing resources for {}", server);
    CycClient.close(server);
  }
  
  @Override
  public void close() throws IOException {
    this.closed = true;
    /*
    final Set<CycAddress> servers = getServers();
    for (CycAddress server : servers) {
      releaseResourcesForServer(server);
    }
    */
    CycClient.closeAll();
  }
  
  @Override
  public boolean isClosed() {
    return this.closed;
  }
  /*
  private Set<CycAddress> getServers() {
    return Collections.unmodifiableSet(clients.keySet());
  }
  */
  private CycClient getClient(CycSessionConfiguration config) throws CycConnectionException {
    return CycClient.get(config);
    /*
    reapOldClients();
    final CycAddress server = config.getCycAddress();
    if (!clients.containsKey(server)) {
      final long startMillis = System.currentTimeMillis();
      final CycClient newClient = CycClient.get(config);
      //final CycClient newClient = CycClient.get(server);
      LOGGER.info("Created new client for {}: {} in {}ms", server, newClient, (System.currentTimeMillis() - startMillis));
      clients.put(server, newClient);
    }
    final CycClient client = clients.get(server);
    LOGGER.debug("Retrieving client for {}: {}", server, client);
    return client;
    */
  }
  
  
  // Private
  /*
  private void reapOldClients() {
    final long startMillis = System.currentTimeMillis();
    int numClientsReaped = 0;
    final Set<CycAddress> servers = clients.keySet();
    for (CycAddress server : servers) {
      final CycClient client = clients.get(server);
      if ((client == null) || client.isClosed()) {
        LOGGER.info("Found expired client for {}: {}", server, client);
        releaseResourcesForServer(server);
        numClientsReaped++;
      }
    }
    final long duration = System.currentTimeMillis() - startMillis;
    if ((numClientsReaped > 0) || (duration > 1)) {
      LOGGER.info("Reaped {} clients in {}ms", numClientsReaped, duration);
    }
  }
  */
  private void errorIfClosed(String msg) {
    if (isClosed()) {
      throw new SessionRuntimeException(getClass().getSimpleName() + " has been closed. " + msg + " " + this);
    }
  }
  
}
