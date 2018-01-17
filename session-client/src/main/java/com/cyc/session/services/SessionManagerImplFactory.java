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
 * File: SessionManagerImplFactory.java
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

import com.cyc.session.SessionManagerConfiguration;
import com.cyc.session.SessionManagerImpl;
import com.cyc.session.exception.SessionConfigurationException;
import com.cyc.session.exception.SessionServiceException;
import com.cyc.session.spi.SessionManagerFactory;

/**
 * 
 * 
 * @author nwinant
 */
public class SessionManagerImplFactory implements SessionManagerFactory {

  private static final SessionManagerImplFactory INSTANCE = new SessionManagerImplFactory();
  
  public static SessionManagerImplFactory getInstance() {
    return INSTANCE;
  }
  
  @Override
  public SessionManagerImpl create(SessionManagerConfiguration configuration) 
          throws SessionServiceException, SessionConfigurationException {
    return new SessionManagerImpl(configuration);
  }

  /**
   * SessionManagerImplFactory should usually be ranked lowest, to make it easy to override.
   * 
   * @param o
   * @return 
   */
  @Override
  public int compareTo(SessionManagerFactory o) {
    return (o != null) ? 1 : -1;
  }
  
}
