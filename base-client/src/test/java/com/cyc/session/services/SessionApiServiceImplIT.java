package com.cyc.session.services;

/*
 * #%L
 * File: SessionApiServiceImplIT.java
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

import com.cyc.baseclient.CycClient;
import com.cyc.baseclient.CycClientSession;
import com.cyc.session.CycSession;
import com.cyc.session.SessionManager;
import com.cyc.session.SessionManagerImpl;
import com.cyc.session.exception.SessionCommunicationException;
import com.cyc.session.exception.SessionConfigurationException;
import com.cyc.session.exception.SessionInitializationException;
import com.cyc.session.exception.SessionManagerConfigurationException;
import com.cyc.session.exception.SessionManagerException;
import com.cyc.session.exception.SessionRuntimeException;
import com.cyc.session.exception.SessionServiceException;
import com.cyc.session.spi.SessionApiService;
import java.io.IOException;
import junit.framework.TestCase;
import org.junit.Test;

import static org.junit.Assert.assertNotEquals;

/**
 *
 * @author nwinant
 */
public class SessionApiServiceImplIT extends TestCase {
  
  public SessionApiServiceImplIT(String testName) {
    super(testName);
  }
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }
  
  @Override
  protected void tearDown() throws Exception {
    final SessionApiService svc = new SessionApiServiceImpl();
    super.tearDown();
    if (svc.getSessionManager().isClosed()) {
      svc.reloadSessionManager();
    }
  }
  
  @Test
  public void testSingletonAccessor() throws Exception {
    final SessionApiService svc = new SessionApiServiceImpl();
    SessionManager result = svc.getSessionManager();
    assertNotNull(result);
    System.out.println("SessionManager implementation: " + result.getClass().getName());
    assertTrue(SessionManagerImpl.class.isInstance(result));
  }
  
  @Test
  public void testReloadSessionManager() throws SessionServiceException, SessionConfigurationException, SessionManagerException, SessionManagerConfigurationException {
    final SessionApiService svc = new SessionApiServiceImpl();
    
    final SessionManager mgr1 = svc.getSessionManager();
    assertNotNull(mgr1);
    assertFalse(mgr1.isClosed());
    
    svc.reloadSessionManager();
    
    final SessionManager mgr2 = svc.getSessionManager();
    assertNotNull(mgr2);
    assertNotEquals(mgr1, mgr2);
    assertTrue(mgr1.isClosed());
    assertFalse(mgr2.isClosed());
  }
  
  @Test
  public void testCloseSessionManager() throws SessionServiceException, SessionConfigurationException, IOException, SessionCommunicationException, SessionInitializationException {
    final SessionApiService svc = new SessionApiServiceImpl();
    
    final SessionManager mgr = svc.getSessionManager();
    assertNotNull(mgr);
    assertFalse(mgr.isClosed());
    
    mgr.close();

    assertTrue(mgr.isClosed());
    assertTrue(svc.getSessionManager().isClosed());
    boolean errored = false;
    try {
      CycSession.getCurrent();
    } catch (SessionRuntimeException ex) {
      errored = true;
    }
    assertTrue(errored);
  }
  
  @Test
  public void testCloseCurrentSession() throws Exception {
    final SessionApiService svc = new SessionApiServiceImpl();
    
    System.out.println("Creating 1!");
    CycSession session1 = CycSession.getCurrent();
    session1.close();
    System.out.println("Closing 1!");
    
    System.out.println("Creating 2!");
    CycSession session2 = CycSession.getCurrent();
    session2.close();
    System.out.println("Closing 2!");
    
    System.out.println("Creating 3!");
    CycSession session3 = CycSession.getCurrent();
    session3.close();
    System.out.println("Closing 3!");
    
    svc.getSessionManager().close();
  }
  
  @Test
  public void testCloseSessionManagerWithOpenSessions() throws SessionServiceException, SessionConfigurationException, IOException, SessionCommunicationException, SessionInitializationException {
    final SessionApiService svc = new SessionApiServiceImpl();
    final SessionManager mgr = svc.getSessionManager();
    assertNotNull(mgr);
    assertFalse(mgr.isClosed());
    
    final CycSession session = mgr.getCurrentSession();
    assertNotNull(session);
    assertFalse(session.isClosed());
    
    mgr.close();

    assertTrue(mgr.isClosed());
    assertTrue(session.isClosed());
  }
  
  /*
  @Test
  public void testSessionClosingViaTryWithResources() throws SessionConfigurationException, SessionCommunicationException, SessionInitializationException {
    // Sources are still expected to build to Java 6, so this test is currently disabled. However,
    // it does pass when sources are built as Java 7.
    // TODO: re-enable once APIs have moved to Java 7.
    final CycSession session1;
    try (CycSession session = CycSession.getCurrent()) {
      session1 = session;
      assertFalse(session1.isClosed());
    }
    assertTrue(session1.isClosed());
    CycSession session2 = CycSession.getCurrent();
    assertFalse(session2.isClosed());
  }
  */
  
  @Test
  public void testClientClosing() throws SessionConfigurationException, SessionCommunicationException, SessionInitializationException {
    final CycClientSession session1 = (CycClientSession) CycSession.getCurrent();
    final CycClient client1 = session1.getAccess();
    assertFalse(client1.isClosed());
    assertFalse(session1.isClosed());
    client1.close();
    assertTrue(client1.isClosed());
    assertFalse(session1.isClosed()); // CycSession does not yet know that its CycAccess is closed.
    
    // TODO: CycSessions really ought to have a listener to self-update when their CycAccess is closed. - nwinant, 2015-10-27
    
    // Retrieving a new session forces the SessionManager to recognize that the underlying CycAccess
    // is closed, and to close & replace the session:
    final CycClientSession session2 = (CycClientSession) CycSession.getCurrent();
    final CycClient client2 = session2.getAccess();
    assertFalse(client2.isClosed());
    assertFalse(session2.isClosed());
    
    assertTrue(client1.isClosed());
    assertTrue(session1.isClosed());
    
    assertNotEquals(session1, session2);
    assertNotEquals(client1, client2);
    
    assertNotEquals(session1, CycSession.getCurrent());
    assertEquals(session2, CycSession.getCurrent());
  }
  
  @Test 
  public void testSessionReaping() throws SessionConfigurationException, SessionCommunicationException, SessionInitializationException {
    // TODO: Could be fleshed out a bit more, with some multithreaded tests, 
    //       possibly broken out into a separate test class. - nwinant, 2015-10-16
    
    final CycSession session1 = CycSession.getCurrent();
    final int h1 = session1.hashCode();
    System.out.println("Hash for session 1: " + h1);
    session1.close();
    
    final CycSession session2 = CycSession.getCurrent();
    final int h2 = session2.hashCode();
    System.out.println("Hash for session 2: " + h2);
    session2.close();
    assertNotEquals(h1, h2);
    
    final CycSession session3 = CycSession.getCurrent();
    final int h3 = session3.hashCode();
    System.out.println("Hash for session 3: " + h3);
    session3.close();
    assertNotEquals(h2, h3);
  }
  
  /*
  public void testGetEnvironmentProperties() throws SessionConfigurationException {
    // TODO: nwinant, 2015-07-06
    skipTest(this, "", 
            "This test needs to be moved somewhere where it won't pick up the actual Session Manager");
    
    CycSessionConfiguration result = CycSessionManager.get().getEnvironmentConfiguration();
    assertNotNull(result);
    assertNull(result.getCycAddress());
    assertNull(result.getConfigurationFileName());
    assertNull(result.getConfigurationLoaderName());
  }
  
  public void testSystemProperties() throws Exception {
    // TODO: nwinant, 2015-07-06
    skipTest(this, "", 
            "This test needs to be moved somewhere where it won't pick up the actual Session Manager");
    
    final CycAddress expectedServer = CycAddress.get("localhost", 3620);
    System.setProperty(SessionConfigurationProperties.SERVER_KEY, "localhost:3620");
    assertEquals(expectedServer, CycSessionManager.get().getSession().getServerInfo().getCycAddress());
  }
  
  public void testChangingSystemProperties() throws Exception {
    // TODO: nwinant, 2015-07-06
    skipTest(this, "", 
            "This test needs to be moved somewhere where it won't pick up the actual Session Manager");
    
    final CycAddress server1 = CycAddress.get("localhost", 3620);
    final CycAddress server2 = CycAddress.get("localhost", 3660);
    
    System.setProperty(SessionConfigurationProperties.SERVER_KEY, "localhost:3620");
    System.out.println(CycSessionManager.get().getSession().getServerInfo().getCycAddress());
    assertEquals(server1, CycSessionManager.get().getSession().getServerInfo().getCycAddress());
    
    System.setProperty(SessionConfigurationProperties.SERVER_KEY, "localhost:3660");
    System.out.println(CycSessionManager.get().getSession().getServerInfo().getCycAddress());
    assertEquals(server2, CycSessionManager.get().getSession().getServerInfo().getCycAddress());
    
    System.setProperty(SessionConfigurationProperties.SERVER_KEY, "localhost:3620");
    System.out.println(CycSessionManager.get().getSession().getServerInfo().getCycAddress());
    assertEquals(server1, CycSessionManager.get().getSession().getServerInfo().getCycAddress());
  }

  public void testInteractive() throws Exception {
    // TODO: nwinant, 2015-07-06
    skipTest(this, "", 
            "This test needs to be moved somewhere where it won't pick up the actual Session Manager");
    
    final CycAddress expectedServer = CycAddress.get("localhost", 3620);
    System.setProperty(SessionConfigurationProperties.CONFIGURATION_LOADER_KEY, SimpleInteractiveLoader.NAME);
    assertEquals(expectedServer, CycSessionManager.get().getSession().getServerInfo().getCycAddress());
  }
  */
}
