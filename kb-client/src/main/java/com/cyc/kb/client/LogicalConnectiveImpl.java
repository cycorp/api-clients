package com.cyc.kb.client;

/*
 * #%L
 * File: LogicalConnectiveImpl.java
 * Project: KB Client
 * %%
 * Copyright (C) 2013 - 2018 Cycorp, Inc
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

import com.cyc.base.cycobject.CycObject;
import com.cyc.base.cycobject.DenotationalTerm;
import com.cyc.base.cycobject.Guid;
import com.cyc.baseclient.cycobject.CycConstantImpl;
import com.cyc.kb.KbObject;
import com.cyc.kb.LogicalConnective;
import com.cyc.kb.exception.CreateException;
import com.cyc.kb.exception.KbException;
import com.cyc.kb.exception.KbRuntimeException;
import com.cyc.kb.exception.KbTypeException;


/**
 * THIS IS NOT PART OF 1.0.0.
 * 
 * @param <T> type of CycObject core
 * 
 * @author vijay
 */
public class LogicalConnectiveImpl<T extends DenotationalTerm> extends RelationImpl<T> implements LogicalConnective {
  
    private static final DenotationalTerm TYPE_CORE =
          new CycConstantImpl("LogicalConnective", new Guid("bd58b9f9-9c29-11b1-9dad-c379636f7270"));

  static DenotationalTerm getClassTypeCore() {
    return TYPE_CORE;
  }
  
  /**
   * default constructor, calls the default super constructor
   *
   * @throws Exception
   */
  @SuppressWarnings("unused")
private LogicalConnectiveImpl() {
    super();
  }

  
  protected LogicalConnectiveImpl(DenotationalTerm cycLogicalConn) throws KbTypeException {
    super(cycLogicalConn);
  }

  
  public LogicalConnectiveImpl(String logicalConnStr) throws KbTypeException, CreateException {
    super(logicalConnStr);
  }

  
  public LogicalConnectiveImpl (String logicalConnStr, LookupType lookup) throws KbTypeException, CreateException {
    super(logicalConnStr, lookup);
  }
  
  public static LogicalConnectiveImpl get(String nameOrId) throws KbTypeException, CreateException {
    return KbObjectImplFactory.get(nameOrId, LogicalConnectiveImpl.class);
  }

  @SuppressWarnings("deprecation")
  public static LogicalConnectiveImpl get(CycObject object) throws KbTypeException, CreateException {
    return KbObjectImplFactory.get(object, LogicalConnectiveImpl.class);
  }

  /**
   * Return the KBCollection as a KBObject of the Cyc term that 
   * underlies this class. 
   * 
   * @return KBCollectionImpl.get("#$LogicalConnective");
   */
  @Override
  public KbObject getType() {
    return getClassType();
  }
  
  /**
   * Return the KBCollection as a KBObject of the Cyc term that 
   * underlies this class. 
   * 
   * @return KBCollectionImpl.get("#$LogicalConnective");
   */
  public static KbObject getClassType() {
    try {
      return KbCollectionImpl.get(getClassTypeString());
    } catch (KbException kae) {
      throw KbRuntimeException.fromThrowable(kae);
    }
  }
  
  @Override
  String getTypeString() {
    return getClassTypeString();
  }
  
  static String getClassTypeString() {
    return "#$LogicalConnective";
  }
}
