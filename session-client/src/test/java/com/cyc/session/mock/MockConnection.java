package com.cyc.session.mock;

/*
 * #%L
 * File: MockConnection.java
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

import com.cyc.session.CycSession;
import com.cyc.session.CycAddress;

/**
 *
 * @author nwinant
 */
public class MockConnection {
  
  final private CycSession session;
  
  public MockConnection(CycSession session) {
    this.session = session;
  }
  
  public CycAddress getServer() {
    return this.getCycSession().getServerInfo().getCycAddress();
  }

  public CycSession getCycSession() {
    return this.session;
  }
}
