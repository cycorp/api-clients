/*
 * Copyright 2017 Cycorp, Inc..
 *
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
 */

package com.cyc.session.internal;

/*
 * #%L
 * File: SessionApiServiceImpl.java
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

import com.cyc.session.exception.SessionConfigurationException;
import com.cyc.session.exception.SessionRuntimeException;
import com.cyc.session.exception.SessionServiceException;
import com.cyc.session.spi.SessionApiService;
import com.cyc.session.spi.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author nwinant
 */
public class SessionApiServiceImpl implements SessionApiService {
  
  //====|    Fields    |==========================================================================//
  
  private static final Logger LOG = LoggerFactory.getLogger(SessionApiServiceImpl.class);
  
  private SessionManagerImpl svc = null;
  
  //====|    Construction    |====================================================================//
  
  public SessionApiServiceImpl() throws SessionServiceException, SessionConfigurationException {
  }
  
  //====|    Public methods    |==================================================================//
  
  @Override
  public synchronized SessionManager getSessionManager() {
    if (svc == null) {
      try {
        svc = new SessionManagerImpl();
      } catch (SessionServiceException | SessionConfigurationException ex) {
        throw new SessionRuntimeException(ex);
      }
    }
    return this.svc;
  }
  
}
