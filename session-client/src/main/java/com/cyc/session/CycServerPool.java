package com.cyc.session;



/*
 * #%L
 * File: CycServerPool.java
 * Project: Session Client
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

import com.cyc.session.exception.SessionCommunicationException;
import com.cyc.session.exception.SessionConfigurationException;
import com.cyc.session.exception.SessionInitializationException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class CycServerPool {

  // Factory methods
  
  /**
   * Creates a CycServerPool for a set of CycAddresses, with a default concurrency level for each if
   * unspecified in the original addresses. This method will not modify any of the original
   * CycAddresses.
   *
   * @param addresses               iterable group of CycAddresses from which to create the pool
   * @param defaultConcurrencyLevel the default concurrency level for each CycAddress, if
   *                                unspecified
   *
   * @return a new CycServerPool
   */
  public static CycServerPool createFromServersWithDefaults(Iterable<CycAddress> addresses,
                                                            int defaultConcurrencyLevel) {
    final List<CycAddress> servers = new ArrayList();
    addresses.forEach(address
            -> servers.add(CycAddress.fromAddressWithDefaults(address, defaultConcurrencyLevel)));
    final CycServerPool result = new CycServerPool(servers);
    LOG.info("Created {} for Cyc servers (each with concurrencyLevel {}): {}",
            result, defaultConcurrencyLevel, addresses);
    return result;
  }

  /**
   * Creates a CycServerPool for a CycAddress, with a default concurrency level if unspecified in
   * the original address. This method will not modify the original CycAddress.
   *
   * @param address                 CycAddress from which to create the pool
   * @param defaultConcurrencyLevel the default concurrency level for the CycAddress, if unspecified
   *
   * @return a new CycServerPool
   */
  public static CycServerPool createFromServerWithDefaults(CycAddress address,
                                                           int defaultConcurrencyLevel) {
    final CycServerPool result
            = new CycServerPool(
                    CycAddress.fromAddressWithDefaults(address, defaultConcurrencyLevel));
    LOG.info("Created {} for Cyc server at {}", result, address);
    return result;
  }

  /**
   * Creates a CycServerPool from the CycAddress specified in a CycSession, with a default
   * concurrency level if unspecified in the original address. This method will not modify the
   * original CycAddress or CycSession.
   *
   * @param session                 CycAddress from which to create the pool
   * @param defaultConcurrencyLevel the default concurrency level for the session's CycAddress, if
   *                                unspecified
   *
   * @return a new CycServerPool
   */
  public static CycServerPool createFromSessionWithDefaults(CycSession session,
                                                            int defaultConcurrencyLevel) {
    return createFromServerWithDefaults(
            session.getConfiguration().getCycAddress(), defaultConcurrencyLevel);
  }

  /**
   * Creates a CycServerPool from the CycAddress specified in the current CycSession, with a default
   * concurrency level if unspecified in the original address. This method will not modify the
   * original CycAddress or CycSession.
   *
   * @param defaultConcurrencyLevel the default concurrency level for the session's CycAddress, if
   *                                unspecified
   *
   * @return a new CycServerPool
   *
   * @throws com.cyc.session.exception.SessionConfigurationException
   * @throws com.cyc.session.exception.SessionCommunicationException
   * @throws com.cyc.session.exception.SessionInitializationException
   */
  public static CycServerPool createFromCurrentSessionWithDefaults(int defaultConcurrencyLevel)
          throws SessionConfigurationException, SessionCommunicationException,
                 SessionInitializationException {
    return createFromSessionWithDefaults(
            CycSessionManager.getCurrentSession(), defaultConcurrencyLevel);
  }

  /**
   * Creates a CycServerPool from the CycAddress specified in the current CycSession, with a default
   * concurrency level of 1 if unspecified in the original address. This method will not modify the
   * original CycAddress or CycSession.
   *
   * @return a new CycServerPool
   *
   * @throws com.cyc.session.exception.SessionConfigurationException
   * @throws com.cyc.session.exception.SessionCommunicationException
   * @throws com.cyc.session.exception.SessionInitializationException
   */
  public static CycServerPool createFromCurrentSession()
          throws SessionConfigurationException, SessionCommunicationException,
                 SessionInitializationException {
    return createFromCurrentSessionWithDefaults(1);
  }
  
  // Fields
  
  private static final Logger LOG = LoggerFactory.getLogger(CycServerPool.class);
  private final List<CycAddress> cycServers;
  private final BlockingQueue<CycAddress> cycWorkerPool;
  private int maxWorkerCount;
  
  // Construction
  
  public CycServerPool(List<CycAddress> cycAddresses) {
    cycServers = new ArrayList(cycAddresses);
    cycWorkerPool = createServerPool(cycServers);
  }
  
  public CycServerPool(CycAddress cycAddress) {
    this(Arrays.asList(cycAddress));
  }
  
  /**
   * Creates a Cyc server pool from provided collection of CycAddress string specifications.
   * @param cycServersSpecs specification of a Cyc pool, listing servers in the following form:
   *                        Collection<String> { "host1:port1:c1", "host2:port2:c2", ... },
   *                        where hostX specifies the host name, portX specifies the network port,
   *                        cX specifies the concurrency level - maximum number of
   *                        concurrent jobs a Cyc server will allow. The cX parameter is
   *                        optional and will default to 1 if absent.
   */
  public CycServerPool(Collection<String> cycServersSpecs) {
    this(serversFromSpecs(cycServersSpecs));
  }
  
  /**
   * Creates a Cyc server pool from provided string specification.
   * @param cycServersSpecs specification of a Cyc pool, listing servers in the following form:
   *                        "host1:port1:c1,host2:port2:c2,host3:port3:c2...",
   *                        where hostX specifies the host name, portX specifies the network port,
   *                        cX specifies the concurrency level - maximum number of
   *                        concurrent jobs a Cyc server will allow. The cX parameter is
   *                        optional and will default to 1 if absent.
   */
  public CycServerPool(String cycServersSpecs) {
    this(Arrays.asList(cycServersSpecs.split(",")));
  }
  
  
  private BlockingQueue<CycAddress> createServerPool(Collection<CycAddress> cycServers) {
    List<CycAddress> tempPool = new ArrayList<>();
  
    for (CycAddress server : cycServers) {
      for (int i = 0; i < server.getConcurrencyLevel().orElse(1); i++) {
        tempPool.add(server);
      }
    }
  
    maxWorkerCount = tempPool.size();
    Collections.shuffle(tempPool);
    
    BlockingQueue<CycAddress> pool = new ArrayBlockingQueue<>(maxWorkerCount);
    pool.addAll(tempPool);
    
    return pool;
  }
  
  private static List<CycAddress> serversFromSpecs(Collection<String> cycServersSpecs) {
    List<CycAddress> servers = new ArrayList<>(cycServersSpecs.size());
    
    for (String serverSpec : cycServersSpecs) {
      serverSpec = serverSpec.trim();
      
      if (CycAddress.isValidString(serverSpec)) {
        CycAddress server = CycAddress.fromString(serverSpec);
        servers.add(server);
      } else {
        LOG.info("{} is not a valid Cyc server specification, skipping.", serverSpec);
      }
    }
    
    return servers;
  }
  
  /**
   * Returns true if <code>possiblyCycPoolSpec</code> represents a valid Cyc server pool specification.
   * A string should have the following form: "host1:port1:c1,host2:port2:c2".
   * @param possiblyCycPoolSpec string to check for validity
   * @return true if possiblyCycPoolSpec is a valid Cyc server pool spec, false otherwise
   */
  public static boolean isValidString(String possiblyCycPoolSpec) {
    if (possiblyCycPoolSpec.isEmpty()) {
      return false;
    }
    
    String[] possiblyCycSpecs = possiblyCycPoolSpec.split(",");
    for (String possiblyCycSpec : possiblyCycSpecs) {
      if (!CycAddress.isValidString(possiblyCycSpec)) {
        return false;
      }
    }
    
    return true;
  }
  
  public CycAddress getDefaultServer() {
    if (cycServers.isEmpty()) {
      throw new RuntimeException("Unable to get default cyc server from pool, pool is empty.");
    }
    return cycServers.get(0);
  }
  
  public int getCycServerCount() {
    return cycServers.size();
  }
  
  public int getAvailableWorkerCount() {
    return cycWorkerPool.size();
  }
  
  public int getMaxWorkerCount() {
    return maxWorkerCount;
  }
  
  /**
   * Requests a Cyc worker from the pool, will block until one is available.
   * @return Cyc server specification
   * @throws InterruptedException
   */
  public CycAddress requestWorker() throws InterruptedException {
    return cycWorkerPool.take();
  }
  
  /**
   * Returns an unused Cyc worker back to the pool.
   * @param cyc Cyc worker to return to the pool
   */
  public void releaseWorker(CycAddress cyc) {
    // TODO: mark CycAddress as a part of the pool and change state whenever it's returned to the pool
    // to prevent the same Cyc server being released to the pool more than once
    cycWorkerPool.offer(cyc);
  }
  
  public List<CycAddress> getServerAddresses() {
    return new ArrayList<>(cycServers);
  }
  
  public String toCycAddressString() {
    final StringBuilder sb = new StringBuilder();
    cycServers.forEach((addr) -> {
      sb.append(sb.length() > 0 ? "," : "").append(addr);
    });
    return sb.toString();
  }
  
  @Override
  public String toString() {
    return CycServerPool.class.getSimpleName()
                   + ":maxWorkerCount=" + getMaxWorkerCount()
                   + "#" + hashCode();
  }
  
}
