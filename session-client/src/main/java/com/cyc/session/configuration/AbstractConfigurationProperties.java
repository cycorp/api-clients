package com.cyc.session.configuration;

/*
 * #%L
 * File: AbstractConfigurationProperties.java
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

import com.cyc.session.exception.SessionRuntimeException;
import java.util.Collection;
import java.util.Properties;

/**
 * Abstract Properties-based implementation of CycSessionConfiguration. 
 * @author nwinant
 */
abstract public class AbstractConfigurationProperties {
  
  // Internal
  
  protected final Properties properties;
  
  private final Properties rawProperties;
  
  private final Properties invalidProperties;
  
  // Constructor
  
  public AbstractConfigurationProperties(Properties properties, PropertiesReader reader) {
    if (properties == null) {
      throw new NullPointerException("Received a null Properties object.");
    }
    this.rawProperties = properties;
    this.properties = reader.filterProperties(properties);
    this.invalidProperties = reader.getInvalidProperties(properties);
  }
  
  
  // Public
  
  public Properties getRawProperties() {
    return (Properties) this.rawProperties.clone();
  }
  
  
  // Protected
  
  protected boolean isAnyKeyPresent(Collection<String> keys) {
    return keys.stream().anyMatch(key -> properties.containsKey(key));
  }
  
  protected Properties getInvalidProperties() {
    return this.invalidProperties;
  }
  
  protected boolean isMisconfigured() {
    return (this.invalidProperties != null) && !this.invalidProperties.isEmpty();
  }
  
  protected Boolean getBooleanProperty(String key, Boolean defaultValue) {
    final String value = properties.getProperty(key,
            (defaultValue != null) ? defaultValue.toString() : null);
    // We don't just call Boolean#parseBoolean(String) or Boolean#valueOf(String) because they will
    // coerce non-boolean string values (e.g., "not a boolean") to either true or false.
    if (value == null) {
      return null;
    }
    if (value.trim().toLowerCase().equals("true")) {
      return true;
    }
    if (value.trim().toLowerCase().equals("false")) {
      return false;
    }
    throw new SessionRuntimeException("Error parsing property '" + key + "': value '" + value + "' cannot be parsed to a boolean.");
  }
  
  protected String toInvalidPropertiesString() {
    if (isMisconfigured()) {
      final StringBuilder sb = new StringBuilder();
      final Properties invalid = this.getInvalidProperties();
      invalid.stringPropertyNames().forEach(key
              -> sb.append(" [")
                      .append(key).append("=").append(invalid.getProperty(key))
                      .append("]"));
      return sb.toString();
    }
    return null;
  }

}
