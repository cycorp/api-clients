package com.cyc.session.services;

/*
 * #%L
 * File: SessionApiServiceImplTest.java
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

import com.cyc.session.SessionManager;
import com.cyc.session.TestEnvironmentProperties;
import com.cyc.session.exception.SessionConfigurationException;
import com.cyc.session.exception.SessionServiceException;
import com.cyc.session.spi.SessionApiService;
import java.util.ServiceConfigurationError;
import junit.framework.TestCase;
import org.junit.Test;

/**
 *
 * @author nwinant
 */
public class SessionApiServiceImplTest extends TestCase {
  
  public SessionApiServiceImplTest(String testName) {
    super(testName);
  }
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }
  
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }
  
  /**
   * No SessionConnectionFactory implementation should be available within this
   * project, so the CycSessionManager static initialization should throw a
   * CycSessionManagerInitializationError.
   */
  @Test
  public void testInstantiateCycSessionManager() throws SessionServiceException, SessionConfigurationException {
    System.out.println("TestEnvProperties.get().isConnectionFactoryExpectedOnClassPath()=" 
            + TestEnvironmentProperties.get().isConnectionFactoryExpectedOnClassPath());
    if (TestEnvironmentProperties.get().isConnectionFactoryExpectedOnClassPath()) {
      runCanInstantiateTest();
    } else {
      runCannotInstantiateTest();
    }
  }
  
  /**
   * A SessionConnectionFactory implementation is expected on the
   * classpath, so the SessionManagerImpl constructor should be able
   * to find one.
   * 
   * @throws SessionConfigurationException
   */
  protected void runCanInstantiateTest() throws SessionConfigurationException {
    final SessionApiService svc = new SessionApiServiceImpl();
    final SessionManager sessionMgr = svc.getSessionManager();
    System.out.println("Found SessionManager: " + sessionMgr);
    assertNotNull(sessionMgr);
  }
  
  /**
   * No SessionConnectionFactory implementation should be available within this
   * project, so the SessionManagerImpl constructor should throw a
   * SessionServiceException.
   * 
   * @throws SessionConfigurationException
   */
  protected void runCannotInstantiateTest() throws SessionConfigurationException {
    SessionManager sessionMgr = null;
    try {
      sessionMgr = new SessionApiServiceImpl().getSessionManager();
      fail("Should have thrown an exception, but did not.");
    } catch (SessionServiceException ex) {
      System.out.println("Good news! Test captured an expected exception: \"" + ex.getMessage() + "\"");
      //ex.printStackTrace(System.err);
      assertEquals(ServiceConfigurationError.class, ex.getClass());
    }
    assertNull(sessionMgr);
  }
  
}
