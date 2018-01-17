package com.cyc.session;

/*
 * #%L
 * File: SessionManagerImplIT.java
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

import com.cyc.Cyc;
import com.cyc.baseclient.CycClient;
import com.cyc.baseclient.CycClientSession;
import com.cyc.baseclient.CycClientSessionFactory;
import com.cyc.session.CycSession.ConnectionStatus;
import com.cyc.session.exception.SessionCommunicationException;
import com.cyc.session.exception.SessionConfigurationException;
import com.cyc.session.exception.SessionInitializationException;
import com.cyc.session.exception.SessionManagerConfigurationException;
import com.cyc.session.exception.SessionManagerException;
import com.cyc.session.exception.SessionRuntimeException;
import com.cyc.session.exception.SessionServiceException;
import java.io.IOException;
import junit.framework.TestCase;
import org.junit.Test;

import static org.junit.Assert.assertNotEquals;

/**
 *
 * @author nwinant
 */
public class SessionManagerImplIT extends TestCase {
  
  public SessionManagerImplIT(String testName) {
    super(testName);
  }
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    Cyc.getSessionManagerService().reloadSessionManager();
  }
  
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }
  
  /*
  @Test
  public void testReleaseSession() {
    // TODO: Does this need to be fleshed out, or is it redundant w/ #testSessionReaping()? - nwinant, 2015-10-23
    throw new UnsupportedOperationException("This feature is not yet implemented");
  }
  */
  
  @Test
  public void testSessionFactoryReleaseResourcesForServer() throws SessionConfigurationException, SessionInitializationException, SessionCommunicationException, IOException {
    // TODO: Could be fleshed out a bit more, with some multithreaded tests. - nwinant, 2015-10-23
    final SessionManagerImpl mgr = (SessionManagerImpl) Cyc.getSessionManager();
    //final CycClientSessionFactory factory = (CycClientSessionFactory) mgr.getSessionFactory();
    //assertEquals(0, factory.getServers().size());
    
    final CycSession session1 = mgr.getCurrentSession();
    //assertEquals(1, factory.getServers().size());
    //assertEquals(session1.getServerInfo().getCycAddress(), factory.getServers().iterator().next());
    final CycClient client1 = ((CycClientSession) session1).getAccess();
    assertFalse(client1.isClosed());
    
    session1.close();
    //assertEquals(0, factory.getServers().size());
    assertTrue(client1.isClosed());
    
    final CycSession session2 = mgr.getCurrentSession();
    //assertEquals(1, factory.getServers().size());
    //assertEquals(session2.getServerInfo().getCycAddress(), factory.getServers().iterator().next());
    final CycClient client2 = ((CycClientSession) session2).getAccess();
    assertFalse(client2.isClosed());
    assertNotEquals(client1, client2);
    
    session2.close();
    //assertEquals(0, factory.getServers().size());
    assertTrue(client2.isClosed());
    
    final CycSession session3 = mgr.getCurrentSession();
    //assertEquals(1, factory.getServers().size());
    //assertEquals(session3.getServerInfo().getCycAddress(), factory.getServers().iterator().next());
    final CycClient client3 = ((CycClientSession) session3).getAccess();
    assertFalse(client3.isClosed());
    assertNotEquals(client1, client3);
    assertNotEquals(client2, client3);
    
    session3.close();
    //assertEquals(0, factory.getServers().size());
    assertTrue(client3.isClosed());
  }
  
  public void testSessionFactoryManagement() throws SessionConfigurationException, SessionInitializationException, SessionCommunicationException, IOException {
    final SessionManagerImpl mgr1 = (SessionManagerImpl) Cyc.getSessionManager();
    final CycClientSessionFactory factory1 = (CycClientSessionFactory) mgr1.getSessionFactory();
    //assertEquals(0, factory1.getServers().size());
    
    final CycSession session1 = mgr1.getCurrentSession();
    //assertEquals(1, factory1.getServers().size());
    final CycClient client1 = ((CycClientSession) session1).getAccess();
    assertFalse(client1.isClosed());
    
    assertFalse(factory1.isClosed());
    factory1.close();
    assertTrue(factory1.isClosed());
    //assertEquals(0, factory1.getServers().size());
    
    Exception ex = null;
    try {
      mgr1.getCurrentSession();
    } catch (SessionRuntimeException sare) {
      ex = sare;
    }
    assertNotNull(ex);
  }
  
  public void testSessionManagerCloseCleanup() throws SessionConfigurationException, SessionInitializationException, SessionCommunicationException, IOException, SessionServiceException, SessionManagerException, SessionManagerConfigurationException {
    final SessionManagerImpl mgr1 = (SessionManagerImpl) Cyc.getSessionManager();
    final CycClientSessionFactory factory1 = (CycClientSessionFactory) mgr1.getSessionFactory();
    //assertEquals(0, factory1.getServers().size());
    
    final CycSession session1 = mgr1.getCurrentSession();
    //assertEquals(1, factory1.getServers().size());
    final CycClient client1 = ((CycClientSession) session1).getAccess();
    assertEquals(ConnectionStatus.CONNECTED, session1.getConnectionStatus());
    assertFalse(client1.isClosed());
    
    Cyc.getSessionManager().close();
    assertTrue(factory1.isClosed());
    assertTrue(client1.isClosed());
    assertEquals(ConnectionStatus.DISCONNECTED, session1.getConnectionStatus());
    assertTrue(session1.isClosed());
    
    assertEquals(mgr1, Cyc.getSessionManager());
    assertTrue(Cyc.getSessionManager().isClosed());
    
    Cyc.getSessionManagerService().reloadSessionManager();
    final SessionManagerImpl mgr2 = (SessionManagerImpl) Cyc.getSessionManager();
    assertNotEquals(mgr1, mgr2);
    assertFalse(mgr2.isClosed());
    
    final CycClientSessionFactory factory2 = (CycClientSessionFactory) mgr2.getSessionFactory();
    assertNotEquals(factory1, factory2);
    //assertEquals(0, factory2.getServers().size());
    assertFalse(factory2.isClosed());
    
    final CycSession session2 = mgr2.getCurrentSession();
    assertNotEquals(session1, session2);
    //assertEquals(1, factory2.getServers().size());
    
    final CycClient client2 = ((CycClientSession) session2).getAccess();
    assertNotEquals(client1, client2);
    assertFalse(client2.isClosed());
  }
  
  /*
  @Test 
  public void testSessionReaping() throws SessionConfigurationException, SessionCommunicationException, SessionInitializationException {
    // TODO: Could be fleshed out a bit more, with some multithreaded tests, 
    //       possibly broken out into a separate test class. - nwinant, 2015-10-16
    
    System.out.println("\n**** testSetCyclist ****");
    final CycSession session1 = CycSession.getCurrent();
    final int h1 = session1.hashCode();
    System.out.println("Hash for session 1: " + h1);
    session1.release();
    
    final CycSession session2 = CycSession.getCurrent();
    final int h2 = session2.hashCode();
    System.out.println("Hash for session 2: " + h2);
    session2.release();
    assertNotEquals(h1, h2);
    
    final CycSession session3 = CycSession.getCurrent();
    final int h3 = session3.hashCode();
    System.out.println("Hash for session 3: " + h3);
    session3.release();
    assertNotEquals(h2, h3);
  }
  */
  
}
