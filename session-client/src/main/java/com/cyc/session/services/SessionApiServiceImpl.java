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

package com.cyc.session.services;

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

import com.cyc.session.ImmutableSessionManagerConfiguration;
import com.cyc.session.SessionManager;
import com.cyc.session.SessionManagerConfiguration;
import com.cyc.session.SessionManagerImpl;
import com.cyc.session.exception.SessionConfigurationException;
import com.cyc.session.exception.SessionManagerConfigurationException;
import com.cyc.session.exception.SessionManagerException;
import com.cyc.session.exception.SessionServiceException;
import com.cyc.session.spi.SessionApiService;
import com.cyc.session.spi.SessionManagerFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author nwinant
 */
public class SessionApiServiceImpl implements SessionApiService {
  
  //====|    SessionApiServiceInstance    |=======================================================//
  
  private static class SessionManagerHolder {
    
    /**
      * Initialization-on-demand holder for singleton.
      *
      * @see
      * <a href="https://en.wikipedia.org/wiki/Initialization-on-demand_holder_idiom">Wikipedia</a>
      */
    private static final SessionManagerHolder INSTANCE = new SessionManagerHolder();
        
    public SessionManager manager = null;
    
    private SessionManagerHolder() {
      try {
        load(loadDefaultConfiguration());
      } catch (SessionManagerConfigurationException 
              | SessionConfigurationException ex) {
        throw SessionServiceException.fromThrowable(SessionManagerConfiguration.class, ex);
      }
    }
    
    public synchronized SessionManager reload(SessionManagerConfiguration configuration) 
            throws SessionServiceException, SessionManagerException, SessionConfigurationException {
      final SessionManager currMgr = manager;
      manager = null;
      if ((currMgr != null) && !currMgr.isClosed()) {
        try {
          currMgr.close();
        } catch (IOException ex) {
          throw SessionManagerException.fromThrowable("Error closing current SessionManager", ex);
        }
      }
      return load(configuration);
    }
    
    /**
     * Selects a SessionManager instance from all available implementations. The default
     * {@link SessionManagerImpl} implementation, if present, should be sorted as the last element.
     *
     * @return the selected SessionManager
     *
     * @throws com.cyc.session.SessionServiceException
     * @throws SessionConfigurationException
     */
    private synchronized SessionManager load(SessionManagerConfiguration configuration) 
            throws SessionServiceException, SessionConfigurationException {
      final Class clazz = SessionManagerFactory.class;
      LOG.debug("Attempting to find {} service providers...", clazz);
      final List<SessionManagerFactory> factories = new ArrayList<>();
      final String clazzName = clazz.getCanonicalName();
      final ServiceLoader<SessionManagerFactory> svcLoader = ServiceLoader.load(clazz);
      final Iterator<SessionManagerFactory> iter = svcLoader.iterator();
      while (iter.hasNext()) {
        factories.add(iter.next());
      }
      if (factories.isEmpty()) {
        throw new SessionServiceException(SessionManagerConfiguration.class,
                "No providers found for " + clazzName);
      } else if (factories.size() > 1) {
        LOG.warn("Loaded {} {} providers: {}", factories.size(), clazzName, factories);
      } else {
        LOG.debug("Loaded one {} provider: {}", clazzName, factories.get(0));
      }
      Collections.sort(factories);
      if (factories.isEmpty()) {
        throw new SessionServiceException(
                SessionManager.class,
                "Could not find any " + SessionManager.class.getSimpleName()
                        + " implementations to load. This should never happen.");
      }
      factories.forEach(sessionMgr
              -> LOG.debug("Found {}: {}", SessionManager.class.getSimpleName(), sessionMgr));
      manager = selectAndCreateSessionManager(factories, configuration);
      LOG.debug("Loaded {}: {}", SessionManager.class.getSimpleName(), manager);
      return manager;
    }
    
    private SessionManager selectAndCreateSessionManager(
            List<SessionManagerFactory> factories, SessionManagerConfiguration configuration)
            throws SessionServiceException, SessionConfigurationException {
      return factories.get(0).create(configuration);
    }
    
  }
  
  //====|    Fields    |==========================================================================//
  
  private static final Logger LOG = LoggerFactory.getLogger(SessionApiServiceImpl.class);
  
  //====|    Construction    |====================================================================//
  
  public SessionApiServiceImpl() {
    try {
      Object tmp = SessionManagerHolder.INSTANCE; // Trivial call to ensure instance is initialized.
    } catch (ExceptionInInitializerError ex) {
      if (ex.getCause() instanceof SessionServiceException) {
        final Class iface = ((SessionServiceException) ex.getCause()).getInterfaceClass();
        throw SessionServiceException.fromThrowable(iface, ex);
      }
      throw SessionServiceException.fromThrowable(SessionApiService.class, ex);
    }
  }
  
  //====|    Public methods    |==================================================================//
  
  @Override
  public SessionManager getSessionManager() {
    return SessionManagerHolder.INSTANCE.manager;
  }
  
  @Override
  public synchronized SessionManager reloadSessionManager(SessionManagerConfiguration configuration) 
          throws SessionServiceException, SessionManagerException, SessionManagerConfigurationException {
    try {
      return SessionManagerHolder.INSTANCE.reload(configuration);
    } catch (SessionConfigurationException ex) {
      throw SessionManagerConfigurationException.fromThrowable(ex);
    }
  }
  
  @Override
  public synchronized SessionManager reloadSessionManager() 
          throws SessionServiceException, SessionManagerException, SessionManagerConfigurationException {
    final SessionManagerConfiguration configuration = loadDefaultConfiguration();
    return reloadSessionManager(configuration);
  }
  
  private static SessionManagerConfiguration loadDefaultConfiguration()
          throws SessionManagerConfigurationException {
    return ImmutableSessionManagerConfiguration.load();
  }
  
}
