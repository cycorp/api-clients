package com.cyc.session;

/*
 * #%L
 * File: SessionManagerConfigurationTest.java
 * Project: Session Client
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

import com.cyc.session.exception.SessionConfigurationException;
import com.cyc.session.exception.SessionRuntimeException;
import com.cyc.session.services.EnvironmentConfigurationLoader;
import java.util.Properties;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.cyc.session.SessionManagerConfigurationProperties.ALL_KEYS;
import static com.cyc.session.SessionManagerConfigurationProperties.SERVER_PATCHING_ALLOWED_DEFAULT_VALUE;
import static com.cyc.session.SessionManagerConfigurationProperties.SERVER_PATCHING_ALLOWED_KEY;
import static com.cyc.session.SessionManagerConfigurationProperties.SERVER_RELEASED_WHEN_ALL_SESSIONS_CLOSED_DEFAULT_VALUE;
import static com.cyc.session.SessionManagerConfigurationProperties.SERVER_RELEASED_WHEN_ALL_SESSIONS_CLOSED_KEY;
import static com.cyc.session.SessionManagerConfigurationProperties.SESSION_AUTO_CREATION_ALLOWED_DEFAULT_VALUE;
import static com.cyc.session.SessionManagerConfigurationProperties.SESSION_AUTO_CREATION_ALLOWED_KEY;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 *
 * @author nwinant
 */
public class SessionManagerConfigurationTest {
  
  private EnvironmentConfigurationLoader envloader;
  
  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }
  
  @Before
  public void setUp() throws Exception {
    this.envloader = new EnvironmentConfigurationLoader();
  }
  
  @After
  public void tearDown() throws Exception {
  }
  
  
  // Tests
  
  @Test
  public void testIsServerReleasedWhenAllSessionsAreClosed() throws SessionConfigurationException {
    System.out.println("testIsServerReleasedWhenAllSessionsAreClosed");
    {
      final Properties props = new Properties();
      props.put(SERVER_RELEASED_WHEN_ALL_SESSIONS_CLOSED_KEY, "false");
      final Boolean result = loadProperties(props).isServerReleasedWhenAllSessionsAreClosed();
      assertNotNull(result);
      assertFalse(result);
    }
    {
      final Properties props = new Properties();
      props.put(SERVER_RELEASED_WHEN_ALL_SESSIONS_CLOSED_KEY, "true");
      final Boolean result = loadProperties(props).isServerReleasedWhenAllSessionsAreClosed();
      assertNotNull(result);
      assertTrue(result);
    }
    {
      final Properties props = new Properties();
      final Boolean result = loadProperties(props).isServerReleasedWhenAllSessionsAreClosed();
      assertNotNull(result);
      assertFalse(result);
      assertEquals(
              SERVER_RELEASED_WHEN_ALL_SESSIONS_CLOSED_DEFAULT_VALUE, 
              loadProperties(props).isServerReleasedWhenAllSessionsAreClosed());
    }
  }
  
  @Test
  public void testServerPatchingProperty() throws SessionConfigurationException {
    System.out.println("testServerPatchingProperty");
    {
      final Properties props = new Properties();
      props.put(SERVER_PATCHING_ALLOWED_KEY, "true");
      assertEquals(true, loadProperties(props).isServerPatchingAllowed());
    }
    {
      final Properties props = new Properties();
      props.put(SERVER_PATCHING_ALLOWED_KEY, "false");
      assertEquals(false, loadProperties(props).isServerPatchingAllowed());
    }
    {
      final Properties props = new Properties();
      props.put(SERVER_PATCHING_ALLOWED_KEY, "   TrUe  ");
      assertEquals(true, loadProperties(props).isServerPatchingAllowed());
    }
    {
      final Properties props = new Properties();
      props.put(SERVER_PATCHING_ALLOWED_KEY, "  fAlSe   ");
      assertEquals(false, loadProperties(props).isServerPatchingAllowed());
    }
    {
      final Properties props = new Properties();
      assertEquals(false, loadProperties(props).isServerPatchingAllowed());
      assertEquals(
              SERVER_PATCHING_ALLOWED_DEFAULT_VALUE, 
              loadProperties(props).isServerPatchingAllowed());
    }
  }
  
  @Test(expected = SessionRuntimeException.class)
  public void testServerPatchingProperty_invalid() throws SessionConfigurationException {
    System.out.println("testServerPatchingProperty_invalid");
    final Properties props = new Properties();
    props.put(SERVER_PATCHING_ALLOWED_KEY, "obviously not a boolean");
    loadProperties(props).isServerPatchingAllowed();
    fail("Should have thrown a SessionApiRuntimeException");
  }
  
  @Test
  public void testIsSessionAutoCreationAllowed() throws SessionConfigurationException {
    System.out.println("testIsSessionAutoCreationAllowed");
    {
      final Properties props = new Properties();
      props.put(SESSION_AUTO_CREATION_ALLOWED_KEY, "true");
      assertEquals(true, loadProperties(props).isSessionAutoCreationAllowed());
    }
    {
      final Properties props = new Properties();
      props.put(SESSION_AUTO_CREATION_ALLOWED_KEY, "false");
      assertEquals(false, loadProperties(props).isSessionAutoCreationAllowed());
    }
    {
      final Properties props = new Properties();
      props.put(SESSION_AUTO_CREATION_ALLOWED_KEY, "   TrUe  ");
      assertEquals(true, loadProperties(props).isSessionAutoCreationAllowed());
    }
    {
      final Properties props = new Properties();
      props.put(SESSION_AUTO_CREATION_ALLOWED_KEY, "  fAlSe   ");
      assertEquals(false, loadProperties(props).isSessionAutoCreationAllowed());
    }
    {
      final Properties props = new Properties();
      assertEquals(true, loadProperties(props).isSessionAutoCreationAllowed());
      assertEquals(
              SESSION_AUTO_CREATION_ALLOWED_DEFAULT_VALUE, 
              loadProperties(props).isSessionAutoCreationAllowed());
    }
  }
  
  @Test
  public void testServerPatchingSystemProperty() throws SessionConfigurationException {
    assumeUnsetSystemProperties(SERVER_PATCHING_ALLOWED_KEY);
    System.out.println("testServerPatchingSystemProperty");
    try {
      assertEquals(false, envloader.getConfiguration().getManagerConfiguration().isServerPatchingAllowed());
            
      System.setProperty(SERVER_PATCHING_ALLOWED_KEY, "false");
      assertEquals(false, envloader.getConfiguration().getManagerConfiguration().isServerPatchingAllowed());
            
      System.setProperty(SERVER_PATCHING_ALLOWED_KEY, "true");
      assertEquals(true, envloader.getConfiguration().getManagerConfiguration().isServerPatchingAllowed());
            
      System.setProperty(SERVER_PATCHING_ALLOWED_KEY, "false");
      assertEquals(false, envloader.getConfiguration().getManagerConfiguration().isServerPatchingAllowed());
            
      System.clearProperty(SERVER_PATCHING_ALLOWED_KEY);
      assertEquals(false, envloader.getConfiguration().getManagerConfiguration().isServerPatchingAllowed());
            
      System.setProperty(SERVER_PATCHING_ALLOWED_KEY, "false");
      assertEquals(false, envloader.getConfiguration().getManagerConfiguration().isServerPatchingAllowed());
            
      System.clearProperty(SERVER_PATCHING_ALLOWED_KEY);
      assertNull(System.getProperty(SERVER_PATCHING_ALLOWED_KEY));
      assertEquals(false, envloader.getConfiguration().getManagerConfiguration().isServerPatchingAllowed());
      assertNull(System.getProperty(SERVER_PATCHING_ALLOWED_KEY));
    } finally {
      clearSystemProperties(SERVER_PATCHING_ALLOWED_KEY);
    }
  }
  
  
  // Protected
  
  protected SessionManagerConfiguration loadProperties(Properties props) throws SessionConfigurationException {
    return new ImmutableSessionManagerConfiguration(props);
  }
  
  protected void clearSystemProperties(String... keys) {
    for (String key : keys) {
      System.out.println("Clearing " + key);
      System.clearProperty(key);
    }
  }
  
  protected void clearSystemProperties() {
    //System.clearProperty(CONFIGURATION_FILE_KEY);
    clearSystemProperties(ALL_KEYS);
  }
  
  protected void assumeUnsetSystemProperties(String... keys) {
    for (String key : keys) {
      System.out.println("Checking " + key);
      Assume.assumeTrue(System.getProperty(key) == null);
    }
  }
   
  protected void assumeEmptyEnvironment() {
    Assume.assumeTrue(EnvironmentConfigurationLoader.isEnvironmentEmpty());
  }
  
}
