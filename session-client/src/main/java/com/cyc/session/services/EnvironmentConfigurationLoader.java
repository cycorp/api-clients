package com.cyc.session.services;

/*
 * #%L
 * File: EnvironmentConfigurationLoader.java
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

import com.cyc.session.CycSessionConfigurationProperties;
import com.cyc.session.EnvironmentConfiguration;
import com.cyc.session.ImmutableCycSessionConfiguration;
import com.cyc.session.ImmutableSessionManagerConfiguration;
import com.cyc.session.SessionManagerConfiguration;
import com.cyc.session.configuration.PropertiesReader;
import com.cyc.session.exception.SessionConfigurationException;
import com.cyc.session.spi.SessionConfigurationLoader;
import java.awt.GraphicsEnvironment;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A SessionConfigurationLoader devoted specifically to providing {@link EnvironmentConfiguration}s
 * from System properties.
 * @author nwinant
 */
public class EnvironmentConfigurationLoader implements SessionConfigurationLoader {
  
  // Fields
  
  public static final String NAME = "environment";
  
  private static final Logger LOGGER = LoggerFactory.getLogger(EnvironmentConfigurationLoader.class);
  
  private final PropertiesReader reader = new PropertiesReader();
  
  // Public
  
  @Override
  public void setEnvironment(EnvironmentConfiguration config) {
    // For this class, this is a no-op.
  }
  
  @Override
  public EnvironmentConfiguration getConfiguration() throws SessionConfigurationException {
    return new EnvironmentConfigurationImpl(reader.fromSystem(), this.getClass());
  }
  
  @Override
  public String getName() {
    return NAME;
  }
  
  @Override
  public boolean isCapableOfSuccess() {
    return true;
  }
  
  
  // Static

  static public boolean isHeadlessEnvironment() {
    // TODO: should this be in EnvironmentConfiguration?
    //       return javax.swing.SwingUtilities.isEventDispatchThread();
    if (!"true".equals(System.getProperty("java.awt.headless"))) {
      try {
        // See http://www.oracle.com/technetwork/articles/javase/headless-136834.html
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        return ge.isHeadlessInstance();
      } catch (UnsatisfiedLinkError ule) {
        LOGGER.warn("Assuming headless environment: {}", ule.getMessage());
      }
    }
    return true;
  }
  
  public static boolean isEnvironmentEmpty() {
    for (String key : CycSessionConfigurationProperties.ALL_KEYS) {
      if (System.getProperty(key) != null) {
        return false;
      }
    }
    return true;
  }
  
  
  // Inner classes
  
  /**
   * Implementation of {@link EnvironmentConfiguration}.
   */
  public static class EnvironmentConfigurationImpl extends ImmutableCycSessionConfiguration implements EnvironmentConfiguration {
    
    private final SessionManagerConfiguration managerConfiguration;
    
    public EnvironmentConfigurationImpl(Properties properties, Class loaderClass) throws SessionConfigurationException {
      super(properties, loaderClass);
      this.managerConfiguration = new ImmutableSessionManagerConfiguration(properties);
    }

    @Override
    public SessionManagerConfiguration getManagerConfiguration() {
      return this.managerConfiguration;
    }
    
  }
}
