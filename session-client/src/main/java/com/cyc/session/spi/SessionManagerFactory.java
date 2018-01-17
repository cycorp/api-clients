package com.cyc.session.spi;

/*
 * #%L
 * File: SessionManagerFactory.java
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
import com.cyc.session.SessionManagerConfiguration;
import com.cyc.session.exception.SessionConfigurationException;
import com.cyc.session.exception.SessionServiceException;

/**
 * Responsible for creating configured instances of {@link SessionManager}.
 */
public interface SessionManagerFactory extends Comparable<SessionManagerFactory> {
  
  public SessionManager create(SessionManagerConfiguration configuration) 
          throws SessionServiceException, SessionConfigurationException;
  
}
