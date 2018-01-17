package com.cyc.session;

/*
 * #%L
 * File: ImmutableCycSessionConfiguration.java
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

import com.cyc.session.configuration.AbstractCycSessionConfigurationProperties;
import com.cyc.session.exception.SessionConfigurationException;
import java.util.Properties;

import static com.cyc.session.CycSessionConfigurationProperties.CONFIGURATION_FILE_KEY;
import static com.cyc.session.CycSessionConfigurationProperties.CONFIGURATION_LOADER_KEY;
import static com.cyc.session.CycSessionConfigurationProperties.SERVER_KEY;

/**
 * An immutable CycSessionConfiguration implementation.
 * @author nwinant
 */
public class ImmutableCycSessionConfiguration 
        extends AbstractCycSessionConfigurationProperties implements CycSessionConfiguration {
  
  // Fields
  
  private final Class loaderClass;
  
  private CycAddress server = null;
  
  // Constructor
  
  public ImmutableCycSessionConfiguration(Properties properties, Class loaderClass)
          throws SessionConfigurationException {
    super(properties);
    this.loaderClass = loaderClass;
    processProperties();
  }
  
  public ImmutableCycSessionConfiguration(CycAddress server, Class loaderClass)
          throws SessionConfigurationException {
    this(new Properties(), loaderClass);
    this.properties.put(SERVER_KEY, server.toString());
    processProperties();
  }
  
  
  // Public
  
  @Override
  public CycAddress getCycAddress() {
    return this.server;
  }
  
  @Override
  public String getConfigurationLoaderName() {
    return properties.getProperty(CONFIGURATION_LOADER_KEY);
  }
  
  @Override
  public String getConfigurationFileName() {
    return properties.getProperty(CONFIGURATION_FILE_KEY);
  }
  
  @Override
  public Class getLoaderClass() {
    return this.loaderClass;
  }
  
  @Override
  public boolean isEquivalent(CycSessionConfiguration configuration) {
    if ((configuration == null) || (configuration.getCycAddress() == null)) {
      return false;
    }
    return configuration.getCycAddress().equals(this.getCycAddress());
  }
  
  @Override
  public boolean equals(Object obj) {
    if ((obj == null) || !(obj instanceof CycSessionConfiguration)) {
      return false;
    }
    return obj.hashCode() == this.hashCode();
  }
  
  @Override
  public int hashCode() {
    int hash = 5;
    hash = 53 * hash + (this.loaderClass != null ? this.loaderClass.hashCode() : 0);
    hash = 53 * hash + (this.server != null ? this.server.hashCode() : 0);
    return hash;
  }
  
  @Override
  public String toString() {
    return "[" + this.getClass().getSimpleName() + "#" + this.hashCode()
            + " (via " + this.loaderClass.getSimpleName() + ")]"
            +  " -> [" + CycAddress.class.getSimpleName() + "=" + getCycAddress() + "]";
  }
  
  
  // Protected
  
  final protected void processProperties() throws SessionConfigurationException {
    if (properties.containsKey(SERVER_KEY)) {
      this.server = CycAddress.fromString(properties.getProperty(SERVER_KEY));
    }
    if (this.isMisconfigured()) {
      final String propString = toInvalidPropertiesString();
      throw new SessionConfigurationException("Properties were misconfigured:" + propString);
    }
  }
  
}
