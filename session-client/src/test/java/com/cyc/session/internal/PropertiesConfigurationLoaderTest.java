package com.cyc.session.internal;

/*
 * #%L
 * File: PropertiesConfigurationLoaderTest.java
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

import com.cyc.session.CycSessionConfiguration;
import com.cyc.session.EnvironmentConfiguration;
import com.cyc.session.SessionConfigurationProperties;
import com.cyc.session.exception.SessionConfigurationException;
import com.cyc.session.exception.SessionRuntimeException;
import java.util.Properties;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.cyc.session.SessionConfigurationProperties.SERVER_PATCHING_ALLOWED_KEY;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import com.cyc.session.CycAddress;

/**
 *
 * @author nwinant
 */
public class PropertiesConfigurationLoaderTest {
  
  private EnvironmentConfigurationLoader envloader;
  private PropertiesConfigurationLoader propLoader;
  
  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }
  
  @Before
  public void setUp() throws Exception {
    this.envloader = new EnvironmentConfigurationLoader();
    this.propLoader = new PropertiesConfigurationLoader();
  }
  
  @After
  public void tearDown() throws Exception {
  }
  
  
  // Tests
  
  @Test
  public void testEmptyProperties() throws SessionConfigurationException {
    final Properties props = new Properties();
    final CycSessionConfiguration config = loadProperties(props);
    assertNull(config.getCycAddress());
    assertNull(config.getConfigurationFileName());
    assertNull(config.getConfigurationLoaderName());
  }
  
  @Test
  public void testEmptySystemProperties() throws SessionConfigurationException {
    if (EnvironmentConfigurationLoader.isEnvironmentEmpty()) {
      final CycSessionConfiguration config = envloader.getConfiguration();
      assertNull(config.getCycAddress());
      assertNull(config.getConfigurationFileName());
      assertNull(config.getConfigurationLoaderName());
    }
  }
  
  @Test
  public void testEmptyProperty() {
    String exMsg = null;
    try {
      final Properties props = new Properties();
      props.put(SessionConfigurationProperties.CONFIGURATION_FILE_KEY, "  ");
      final CycSessionConfiguration config = loadProperties(props);
      assertNull(config.getCycAddress());
      assertNull(config.getConfigurationFileName());
      assertNull(config.getConfigurationLoaderName());
    } catch (SessionConfigurationException ex) {
      exMsg = ex.getMessage();
    }
    assertEquals("Properties were misconfigured: [cyc.session.configurationFile=  ]", exMsg);
  }
  
  @Test
  public void testEmptyCycServer() {
    String exMsg = null;
    try {
      final Properties props = new Properties();
      props.put(SessionConfigurationProperties.SERVER_KEY, " ");
      final CycSessionConfiguration config = loadProperties(props);
      assertNull(config.getCycAddress());
      assertNull(config.getConfigurationFileName());
      assertNull(config.getConfigurationLoaderName());
    } catch (SessionConfigurationException ex) {
      exMsg = ex.getMessage();
    }
    assertEquals("Properties were misconfigured: [cyc.session.server= ]", exMsg);
  }

  @Test
  public void testBadCycServer() {
    String exMsg = null;
    try {
      final Properties props = new Properties();
      props.put(SessionConfigurationProperties.SERVER_KEY, "locsdfg:3a");
      final CycSessionConfiguration config = loadProperties(props);
      assertNull(config.getCycAddress());
      assertNull(config.getConfigurationFileName());
      assertNull(config.getConfigurationLoaderName());
    } catch (SessionConfigurationException ex) {
      exMsg = ex.getMessage();
    }
    assertEquals("Properties were misconfigured: [cyc.session.server=locsdfg:3a]", exMsg);
  }
  
  @Test
  public void testCycServerProperty() throws SessionConfigurationException {
    final Properties props = new Properties();
    props.put(SessionConfigurationProperties.SERVER_KEY, "somehost:3620");
    final CycSessionConfiguration config = loadProperties(props);
    assertNotNull(config.getCycAddress());
    assertEquals(CycAddress.get("somehost", 3620), config.getCycAddress());
    assertNull(config.getConfigurationFileName());
    assertNull(config.getConfigurationLoaderName());
  }
  
  @Test
  public void testIsServerReleasedWhenAllSessionsAreClosed() throws SessionConfigurationException {
    System.out.println("");
    
    final Properties props1 = new Properties();
    props1.put(SessionConfigurationProperties.SERVER_RELEASED_WHEN_ALL_SESSIONS_CLOSED, "false");
    final Boolean result1 = loadProperties(props1).isServerReleasedWhenAllSessionsAreClosed();
    assertNotNull(result1);
    assertFalse(result1);
    
    final Properties props2 = new Properties();
    props2.put(SessionConfigurationProperties.SERVER_RELEASED_WHEN_ALL_SESSIONS_CLOSED, "true");
    final Boolean result2 = loadProperties(props2).isServerReleasedWhenAllSessionsAreClosed();
    assertNotNull(result2);
    assertTrue(result2);
    
    final Properties props3 = new Properties();
    final Boolean result3 = loadProperties(props3).isServerReleasedWhenAllSessionsAreClosed();
    assertNotNull(result3);
    assertFalse(result3);
  }
  
  @Test
  public void testFileProperties() throws SessionConfigurationException {
    assumeEmptyEnvironment();
    try {
      final CycSessionConfiguration config = envloader.getConfiguration();
      assertNull(config.getCycAddress());
      assertNull(config.getConfigurationFileName());
      assertNull(config.getConfigurationLoaderName());

      System.setProperty(SessionConfigurationProperties.CONFIGURATION_FILE_KEY, PropertiesReaderTest.TEST_PROPERTIES_FILE);
      final EnvironmentConfiguration envconfig2 = envloader.getConfiguration();
      assertNull(envconfig2.getCycAddress());
      assertEquals(PropertiesReaderTest.TEST_PROPERTIES_FILE, envconfig2.getConfigurationFileName());
      assertNull(envconfig2.getConfigurationLoaderName());

      propLoader.setEnvironment(envconfig2);
      final CycSessionConfiguration config2 = propLoader.getConfiguration();
      assertEquals(CycAddress.get("testserver", 3640), config2.getCycAddress());
      assertNull(config2.getConfigurationFileName());
      assertNull(config2.getConfigurationLoaderName());
    } finally {
      clearSystemProperties();
    }
  }
  
  @Test
  public void testServerPatchingProperty() throws SessionConfigurationException {
    final Properties props1 = new Properties();
    props1.put(SERVER_PATCHING_ALLOWED_KEY, "true");
    assertEquals(true, loadProperties(props1).isServerPatchingAllowed());

    final Properties props2 = new Properties();
    props2.put(SERVER_PATCHING_ALLOWED_KEY, "false");
    assertEquals(false, loadProperties(props2).isServerPatchingAllowed());

    final Properties props3 = new Properties();
    assertEquals(false, loadProperties(props3).isServerPatchingAllowed());
    
    final Properties props4 = new Properties();
    props4.put(SERVER_PATCHING_ALLOWED_KEY, "   TrUe  ");
    assertEquals(true, loadProperties(props4).isServerPatchingAllowed());

    final Properties props5 = new Properties();
    props5.put(SERVER_PATCHING_ALLOWED_KEY, "  fAlSe   ");
    assertEquals(false, loadProperties(props5).isServerPatchingAllowed());
  }
  
  @Test(expected = SessionRuntimeException.class)
  public void testInvalidServerPatchingProperty() throws SessionConfigurationException {
    final Properties props4 = new Properties();
    props4.put(SERVER_PATCHING_ALLOWED_KEY, "obviously not a boolean");
    loadProperties(props4).isServerPatchingAllowed();
    fail("Should have thrown a SessionApiRuntimeException");
  }
  
  @Test
  public void testServerPatchingSystemProperty() throws SessionConfigurationException {
    assumeUnsetSystemProperties(SERVER_PATCHING_ALLOWED_KEY);
    try {
      assertEquals(false, envloader.getConfiguration().isServerPatchingAllowed());
            
      System.setProperty(SERVER_PATCHING_ALLOWED_KEY, "false");
      assertEquals(false, envloader.getConfiguration().isServerPatchingAllowed());
            
      System.setProperty(SERVER_PATCHING_ALLOWED_KEY, "true");
      assertEquals(true, envloader.getConfiguration().isServerPatchingAllowed());
            
      System.setProperty(SERVER_PATCHING_ALLOWED_KEY, "false");
      assertEquals(false, envloader.getConfiguration().isServerPatchingAllowed());
            
      System.clearProperty(SERVER_PATCHING_ALLOWED_KEY);
      assertEquals(false, envloader.getConfiguration().isServerPatchingAllowed());
            
      System.setProperty(SERVER_PATCHING_ALLOWED_KEY, "false");
      assertEquals(false, envloader.getConfiguration().isServerPatchingAllowed());
            
      System.clearProperty(SERVER_PATCHING_ALLOWED_KEY);
      assertNull(System.getProperty(SERVER_PATCHING_ALLOWED_KEY));
      assertEquals(false, envloader.getConfiguration().isServerPatchingAllowed());
      assertNull(System.getProperty(SERVER_PATCHING_ALLOWED_KEY));
    } finally {
      clearSystemProperties(SERVER_PATCHING_ALLOWED_KEY);
    }
  }

  
  // Protected
  
  protected CycSessionConfiguration loadProperties(Properties props) throws SessionConfigurationException {
    return this.propLoader.getConfiguration(props);
  }
  
  protected void clearSystemProperties(String... keys) {
    for (String key : keys) {
      System.out.println("Clearing " + key);
      System.clearProperty(key);
    }
  }
  
  protected void clearSystemProperties() {
    //System.clearProperty(SessionConfigurationProperties.CONFIGURATION_FILE_KEY);
    clearSystemProperties(SessionConfigurationProperties.ALL_KEYS);
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
