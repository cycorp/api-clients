package com.cyc.baseclient;

/*
 * #%L
 * File: AbstractKbTool.java
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

import com.cyc.base.CommandTool;
import com.cyc.base.CycAccess;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.base.cycobject.CycObject;
import com.cyc.base.cycobject.ElMt;
import com.cyc.base.exception.CycApiException;
import com.cyc.base.cycobject.CycList;
import com.cyc.base.cycobject.DenotationalTerm;
import com.cyc.baseclient.cycobject.CycConstantImpl;
import com.cyc.baseclient.cycobject.CycSymbolImpl;
import static com.cyc.baseclient.CycObjectFactory.makeCycSymbol;

/**
 * This class is the basis for all of the kbtools ({@link com.cyc.baseclient.kbtool})
 * in the Base Client.
 * 
 * @author nwinant
 */
public class AbstractKbTool {
  
  protected AbstractKbTool(CycAccess client) {
    this.client = client;
  }
  
  
  // Protected
  
  protected Object[] converse_inner(Object command) throws CycConnectionException, CycApiException {
    // FIXE: do we need this? Looks like it's only being used in a couple of methods in CycAssertTool & CycComparisonTool - nwinant, 2014-08-06
    return getCycClient().converse(command);
  }
  
  protected CycConstantImpl find_inner(String constantName) throws CycConnectionException {
    // FIXME: do we need this? Looks like it's only being used by CycAssertTool#findOrCreateBinaryPredicate - nwinant, 2014-08-06
    return (CycConstantImpl) getCyc().getLookupTool().find(constantName);
  }
  
  protected CycConstantImpl findOrCreate_inner(String constantName) throws CycConnectionException {
    return (CycConstantImpl) getCyc().getLookupTool().findOrCreate(constantName);
  }
  
  protected CycConstantImpl getKnownConstantByName_inner(String name) throws CycConnectionException, CycConnectionException, CycConnectionException, CycConnectionException {
    return (CycConstantImpl) getCyc().getLookupTool().getKnownConstantByName(name);
  }
  
  protected CycConstantImpl getConstantByName_inner(String name) throws CycConnectionException {
    return (CycConstantImpl) getCyc().getLookupTool().getConstantByName(name);
  }
  
  public CycList<Object> makeCycList_inner(String string) throws CycApiException {
    return getCyc().getObjectTool().makeCycList(string);
  }
  protected ElMt makeElMt_inner(CycObject mt) throws CycConnectionException {
    return getCyc().getObjectTool().makeElMt(mt);
  }
  
  protected static KbTransaction getCurrentTransaction() {
    return CycClient.getCurrentTransaction();
  }
  
  protected CycAccess getCyc() {
    return this.client;
  }
  
  protected CommandTool getConverse() {
    return getCyc().converse();
  }
  
  
  // Private
  
  private CycClient getCycClient() {
    return CycClientManager.getClientManager().fromCycAccess(getCyc());
  }
  
  
  // Internal
  
  protected static final CycSymbolImpl WITH_ALL_MTS = makeCycSymbol("with-all-mts");

  final private CycAccess client;
}
