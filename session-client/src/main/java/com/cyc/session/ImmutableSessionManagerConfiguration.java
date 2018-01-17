package com.cyc.session;

/*
 * #%L
 * File: ImmutableSessionManagerConfiguration.java
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

import com.cyc.session.SessionOptions.DefaultSessionOptions;
import com.cyc.session.configuration.AbstractSessionManagerConfigurationProperties;
import com.cyc.session.configuration.DefaultSessionOptionsImpl;
import com.cyc.session.configuration.PropertiesReader;
import com.cyc.session.exception.SessionConfigurationException;
import com.cyc.session.exception.SessionManagerConfigurationException;
import java.util.Objects;
import java.util.Properties;

import static com.cyc.session.SessionManagerConfigurationProperties.GUI_INTERACTION_ALLOWED_DEFAULT_VALUE;
import static com.cyc.session.SessionManagerConfigurationProperties.GUI_INTERACTION_ALLOWED_KEY;
import static com.cyc.session.SessionManagerConfigurationProperties.SERVER_PATCHING_ALLOWED_DEFAULT_VALUE;
import static com.cyc.session.SessionManagerConfigurationProperties.SERVER_PATCHING_ALLOWED_KEY;
import static com.cyc.session.SessionManagerConfigurationProperties.SERVER_RELEASED_WHEN_ALL_SESSIONS_CLOSED_DEFAULT_VALUE;
import static com.cyc.session.SessionManagerConfigurationProperties.SERVER_RELEASED_WHEN_ALL_SESSIONS_CLOSED_KEY;
import static com.cyc.session.SessionManagerConfigurationProperties.SESSION_AUTO_CREATION_ALLOWED_DEFAULT_VALUE;
import static com.cyc.session.SessionManagerConfigurationProperties.SESSION_AUTO_CREATION_ALLOWED_KEY;

/**
 * An immutable SessionManagerConfiguration implementation.
 * 
 * @author nwinant
 */
public class ImmutableSessionManagerConfiguration 
        extends AbstractSessionManagerConfigurationProperties
        implements SessionManagerConfiguration {
  
  // Construction
  
  public static ImmutableSessionManagerConfiguration load() throws SessionManagerConfigurationException {
    try {
      return new ImmutableSessionManagerConfiguration(READER.fromSystem());
    } catch (SessionConfigurationException ex) {
      throw SessionManagerConfigurationException.fromThrowable(
              "Could not load properties to create "
                      + SessionConfigurationException.class.getSimpleName(), ex);
    }
  }
  
  // Fields
  
  private static final PropertiesReader READER = new PropertiesReader();
  
  private final DefaultSessionOptions defaultOptions;
    
  // Constructor
  
  public ImmutableSessionManagerConfiguration(Properties properties) 
          throws SessionConfigurationException {
    super(properties);
    this.defaultOptions = new DefaultSessionOptionsImpl();
    processProperties();
  }
  
  
  // Public
  
  @Override
  public boolean isGuiInteractionAllowed() {
    return getBooleanProperty(
            GUI_INTERACTION_ALLOWED_KEY, 
            GUI_INTERACTION_ALLOWED_DEFAULT_VALUE);
  }
  
  @Override
  public boolean isServerPatchingAllowed() {
    return getBooleanProperty(
            SERVER_PATCHING_ALLOWED_KEY, 
            SERVER_PATCHING_ALLOWED_DEFAULT_VALUE);
  }
  
  @Override
  public boolean isServerReleasedWhenAllSessionsAreClosed() {
    return getBooleanProperty(
            SERVER_RELEASED_WHEN_ALL_SESSIONS_CLOSED_KEY,
            SERVER_RELEASED_WHEN_ALL_SESSIONS_CLOSED_DEFAULT_VALUE);
  }
  
  @Override
  public boolean isSessionAutoCreationAllowed() {
    return getBooleanProperty(
            SESSION_AUTO_CREATION_ALLOWED_KEY, 
            SESSION_AUTO_CREATION_ALLOWED_DEFAULT_VALUE);
  }
  
  @Override
  public DefaultSessionOptions getDefaultSessionOptions() {
    return this.defaultOptions;                   // TODO: this needs to be derived from properties.
  }
  
  @Override
  public boolean isConfigurationCachingAllowed() {
    return true;                                  // TODO: this needs to be derived from a property.
  }
  
  @Override
  public boolean isSessionCachingAllowed() {
    return true;                                  // TODO: this needs to be derived from a property.
  }
  
  @Override
  public boolean equals(Object obj) {
    if ((obj == null) || !(obj instanceof SessionManagerConfiguration)) {
      return false;
    }
    return obj.hashCode() == this.hashCode();
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 43 * hash + Objects.hashCode(this.defaultOptions);
    return hash;
  }
  
  @Override
  public String toString() {
    return "[" + this.getClass().getSimpleName() + "#" + this.hashCode() + "]";
  }
  
  
  // Protected
  
  final protected void processProperties() throws SessionConfigurationException {
    if (this.isMisconfigured()) {
      final String propString = toInvalidPropertiesString();
      throw new SessionConfigurationException("Properties were misconfigured:" + propString);
    }
  }

}
