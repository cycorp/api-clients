package com.cyc.baseclient;

/*
 * #%L
 * File: LegacyCycClientManager.java
 * Project: Base Client
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
import com.cyc.session.LegacyCycAccessManager;
import com.cyc.session.exception.SessionCommunicationException;
import com.cyc.session.exception.SessionConfigurationException;
import com.cyc.session.exception.SessionInitializationException;

/**
 * <p>Deprecated: This class will be removed before final 1.0.0 release
 * 
 * @author nwinant
 */
@Deprecated
public class LegacyCycClientManager extends LegacyCycAccessManager {
  
  // Public
  
  public CycClientSession setCurrentSession(CycAddress server) throws SessionConfigurationException, SessionCommunicationException, SessionInitializationException {
    CycClientSession session = getSession(server);
    return this.setCurrentSession(session);
  }
  
  public CycClientSession getSession(CycAddress server) throws SessionConfigurationException, SessionCommunicationException, SessionInitializationException {
    if (hasCurrentSession() && server.equals(getCurrentSession().getServerInfo().getCycAddress())) {
      return getCurrentSession();
    }
    return createSession(server);
  }
  
  public CycClient getAccess(CycAddress server) throws SessionConfigurationException, SessionCommunicationException, SessionInitializationException {
    return this.getSession(server).getAccess();
  }
  
  
  public boolean hasCurrentAccess() {
    return hasCurrentSession();
  }
}
