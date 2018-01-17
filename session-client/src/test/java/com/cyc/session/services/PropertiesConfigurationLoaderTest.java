package com.cyc.session.services;

/*
 * #%L
 * File: PropertiesConfigurationLoaderTest.java
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

import com.cyc.session.CycAddress;
import com.cyc.session.CycSessionConfiguration;
import com.cyc.session.CycSessionConfigurationProperties;
import com.cyc.session.EnvironmentConfiguration;
import com.cyc.session.configuration.PropertiesReaderTest;
import com.cyc.session.exception.SessionConfigurationException;
import java.util.Properties;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

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
      props.put(CycSessionConfigurationProperties.CONFIGURATION_FILE_KEY, "  ");
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
      props.put(CycSessionConfigurationProperties.SERVER_KEY, " ");
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
      props.put(CycSessionConfigurationProperties.SERVER_KEY, "locsdfg:3a");
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
    props.put(CycSessionConfigurationProperties.SERVER_KEY, "somehost:3620");
    final CycSessionConfiguration config = loadProperties(props);
    assertNotNull(config.getCycAddress());
    assertEquals(CycAddress.get("somehost", 3620), config.getCycAddress());
    assertNull(config.getConfigurationFileName());
    assertNull(config.getConfigurationLoaderName());
  }
  
  @Test
  public void testFileProperties() throws SessionConfigurationException {
    assumeEmptyEnvironment();
    try {
      final CycSessionConfiguration config = envloader.getConfiguration();
      assertNull(config.getCycAddress());
      assertNull(config.getConfigurationFileName());
      assertNull(config.getConfigurationLoaderName());

      System.setProperty(CycSessionConfigurationProperties.CONFIGURATION_FILE_KEY, PropertiesReaderTest.TEST_PROPERTIES_FILE);
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
  
  // Protected
  
  protected CycSessionConfiguration loadProperties(Properties props) throws SessionConfigurationException {
    return this.propLoader.getSessionConfiguration(props);
  }
  
  protected void clearSystemProperties(String... keys) {
    for (String key : keys) {
      System.out.println("Clearing " + key);
      System.clearProperty(key);
    }
  }
  
  protected void clearSystemProperties() {
    //System.clearProperty(CycSessionConfigurationProperties.CONFIGURATION_FILE_KEY);
    clearSystemProperties(CycSessionConfigurationProperties.ALL_KEYS);
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
