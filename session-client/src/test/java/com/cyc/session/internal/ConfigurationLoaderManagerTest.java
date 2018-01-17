package com.cyc.session.internal;

/*
 * #%L
 * File: ConfigurationLoaderManagerTest.java
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
import com.cyc.session.services.EnvironmentConfigurationLoader;
import com.cyc.session.services.PropertiesConfigurationLoader;
import com.cyc.session.services.SimpleInteractiveLoader;
import com.cyc.session.spi.SessionConfigurationLoader;
import java.util.Map;
import junit.framework.TestCase;
import org.junit.Test;

/**
 *
 * @author nwinant
 */
public class ConfigurationLoaderManagerTest extends TestCase {
  
  public ConfigurationLoaderManagerTest(String testName) {
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
  
  @Test
  public void testEmptyProperties() throws SessionConfigurationException {
    ConfigurationLoaderManager configMgr = new ConfigurationLoaderManager();
    Map<String, SessionConfigurationLoader> mgrs = configMgr.getConfigurationLoaders();
    System.out.println("SessionConfigurationLoaders found:");
    for (String key : mgrs.keySet()) {
      System.out.println("  - " + key);
    }
    System.out.println("Total: " + mgrs.size());
    assertEquals(3, mgrs.size());
    assertTrue(mgrs.containsKey(PropertiesConfigurationLoader.NAME));
    assertTrue(mgrs.containsKey(EnvironmentConfigurationLoader.NAME));
    assertTrue(mgrs.containsKey(SimpleInteractiveLoader.NAME));
  }
  
}
