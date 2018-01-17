package com.cyc.session.configuration;

/*
 * #%L
 * File: AbstractCycSessionConfigurationProperties.java
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
import java.util.Arrays;
import java.util.Properties;

/**
 * Abstract Properties-based implementation of CycSessionConfiguration. 
 * @author nwinant
 */
abstract public class AbstractCycSessionConfigurationProperties 
        extends AbstractConfigurationProperties implements CycSessionConfiguration {
  
  //====|    PropertiesReader    |================================================================//
  
  private static final PropertiesReader READER = new PropertiesReader() {
    
    @Override
    protected boolean isValidKey(String key) {
      if(super.isValidKey(key)) {
        for (String knownKey : CycSessionConfigurationProperties.ALL_KEYS) {
          if (knownKey.equals(key)) {
            return true;
          }
        }
      }
      return false;
    }
    
    @Override
    protected boolean isValidProperty(String key, String value) {
      if (CycSessionConfigurationProperties.SERVER_KEY.equals(key)) {
        return CycAddress.isValidString(value);
      } else if (this.isValidKey(key)) {
        return (value != null) && !value.trim().isEmpty();
      }
      return true;
    }
    
  };
  
  //====|    Construction    |====================================================================//
  
  public AbstractCycSessionConfigurationProperties(Properties properties) {
    super(properties, READER);
  }
  
  //====|    Internal methods    |================================================================//
  
  protected boolean isSufficientlyConfigured() {
    return isAnyKeyPresent(Arrays.asList(CycSessionConfigurationProperties.CONFIGURATION_LOADER_KEY,
                    CycSessionConfigurationProperties.CONFIGURATION_FILE_KEY,
                    CycSessionConfigurationProperties.SERVER_KEY));
  }

}
