package com.cyc.baseclient.cycobject;

/*
 * #%L
 * File: ElMtCycNautImpl.java
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
import com.cyc.base.exception.BaseClientRuntimeException;
import com.cyc.kb.Context;
import java.util.List;

/**
 * Provides the container for the ElMt NAUT (Epistemlogical Level Microtheory
 * Non Atomic Un-reified Term).<p>
 *
 * @version $Id: ElMtCycNautImpl.java 173132 2017-08-02 00:48:28Z nwinant $
 * @author Tony Brusseau
 */

public class ElMtCycNautImpl extends NautImpl implements ElMt {
  
  /** Creates a new instance of ElMtCycNaut */
  private ElMtCycNautImpl(List terms) {
    super(terms);
  }
  
  /**
   * Returns a new ElMtCycNautImpl.  Note, use the
   * factory method in the CycAccess to create these.
   * @param terms
   * @return a new ElMtCycNautImpl
   */
  public static ElMtCycNautImpl makeElMtCycNaut(List terms) {
    return new ElMtCycNautImpl(terms);
  }
  /**
   * Returns this object in a form suitable for use as an <tt>String</tt> api expression value.
   *
   * @return this object in a form suitable for use as an <tt>String</tt> api expression value
   */
  //@ToDo uncomment this when (list* issues has been resolved in cyclify() of CycList
  /*public String stringApiValue() {
    return "'" + super.stringApiValue();  /**
   * Returns this object in a form suitable for use as an <tt>String</tt> api expression value.
   *
   * @return this object in a form suitable for use as an <tt>String</tt> api expression value
   */
  //@ToDo uncomment this when (list* issues has been resolved in cyclify() of CycList
  /*public String stringApiValue() {
    return "'" + super.stringApiValue();
  }*/
  
  /**
   * Returns this object in a form suitable for use as an <tt>String</tt> api expression value.
   * @param context
   * @return this object in a form suitable for use as an <tt>String</tt> api expression value
   */
  public static boolean isCompatible(Context context) {
    final Object core = context.getCore();
    return (core instanceof ElMtCycNautImpl) || (core instanceof List);
  }

  public static ElMtCycNautImpl fromContext(Context context) {
    final Object core = context.getCore();
    if (core instanceof ElMtCycNautImpl) {
      return (ElMtCycNautImpl) core;
    } else if (core instanceof List) {
     return makeElMtCycNaut((List) core);
    }
    throw new BaseClientRuntimeException("Could not create " + ElMtCycNautImpl.class.getSimpleName() 
            + " from " + core);
  }
}
