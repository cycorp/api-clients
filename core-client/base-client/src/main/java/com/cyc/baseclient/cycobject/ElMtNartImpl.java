package com.cyc.baseclient.cycobject;

/*
 * #%L
 * File: ElMtNartImpl.java
 * Project: Base Client
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

import com.cyc.base.cycobject.ElMt;
import com.cyc.base.cycobject.Nart;
import com.cyc.base.exception.BaseClientRuntimeException;
import com.cyc.kb.Context;

/**
 * Provides the container for the ElMt NART (Epistemlogical Level Microtheory
 * Non Atomic Reified Term).<p>
 *
 * @version $Id: ElMtNartImpl.java 173021 2017-07-21 18:36:21Z nwinant $
 * @author Tony Brusseau
 */
public class ElMtNartImpl extends NartImpl implements ElMt {
  
  /** Creates a new instance of ElMtNart */
  private ElMtNartImpl(Nart nart) {
    super(nart.toCycList());
  }
  
  /**
   * Returns a new ElMtNartImpl given a Nart.  Note, use the
   * factory method in the CycAccess to create these.
   */
  public static ElMtNartImpl makeElMtNart(Nart nart) {
    return new ElMtNartImpl(nart);
  }
  
  public static boolean isCompatible(Context context) {
    final Object core = context.getCore();
    return (core instanceof ElMtNartImpl) || (core instanceof Nart);
  }
  
  public static ElMtNartImpl fromContext(Context context) {
    final Object core = context.getCore();
    if (core instanceof ElMtNartImpl) {
      return (ElMtNartImpl) core;
    } else if (core instanceof Nart) {
     return makeElMtNart((Nart) core);
    }
    throw new BaseClientRuntimeException("Could not create " + ElMtNartImpl.class.getSimpleName() 
            + " from " + core);
  }
}
