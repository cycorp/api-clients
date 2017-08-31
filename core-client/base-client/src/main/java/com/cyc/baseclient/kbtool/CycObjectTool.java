package com.cyc.baseclient.kbtool;

/*
 * #%L
 * File: CycObjectTool.java
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

import com.cyc.base.CycAccess;
import com.cyc.base.cycobject.CycConstant;
import static com.cyc.base.cycobject.CycConstant.HD;
import com.cyc.base.cycobject.CycList;
import com.cyc.base.cycobject.CycObject;
import com.cyc.base.cycobject.CycSymbol;
import com.cyc.base.cycobject.DenotationalTerm;
import com.cyc.base.cycobject.ElMt;
import com.cyc.base.cycobject.FormulaSentence;
import com.cyc.base.cycobject.Fort;
import com.cyc.base.cycobject.Guid;
import com.cyc.base.cycobject.Nart;
import com.cyc.base.cycobject.Naut;
import com.cyc.base.exception.CycApiException;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.base.kbtool.ObjectTool;
import com.cyc.baseclient.AbstractKbTool;
import com.cyc.baseclient.CycObjectFactory;
import static com.cyc.baseclient.CycObjectFactory.makeCycSymbol;
import com.cyc.baseclient.connection.SublApiHelper;
import static com.cyc.baseclient.connection.SublApiHelper.makeSubLStmt;
import com.cyc.baseclient.cycobject.CycArrayList;
import com.cyc.baseclient.cycobject.CycConstantImpl;
import com.cyc.baseclient.cycobject.CycFormulaSentence;
import com.cyc.baseclient.cycobject.CycListParser;
import com.cyc.baseclient.cycobject.ElMtConstant;
import com.cyc.baseclient.cycobject.ElMtCycNaut;
import com.cyc.baseclient.cycobject.ElMtNart;
import com.cyc.baseclient.cycobject.NautImpl;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Tools for creating simple CycObjects, such as constants and lists. To perform 
 * complex assertions, use the {@link com.cyc.baseclient.kbtool.CycAssertTool}. To lookup
 * facts in the Cyc KB, use the {@link com.cyc.baseclient.kbtool.CycLookupTool}.
 * 
 * @see com.cyc.baseclient.kbtool.CycAssertTool
 * @see com.cyc.baseclient.kbtool.CycLookupTool
 * @author nwinant
 */
public class CycObjectTool extends AbstractKbTool implements ObjectTool {
  
  public CycObjectTool(CycAccess client) {
    super(client);
  }
  
  
  // Public
  
  @Override
  public ElMt canonicalizeHLMT(CycList cycList)
          throws CycConnectionException, CycApiException {
    ElMt mt;
    String command = makeSubLStmt("canonicalize-hlmt", cycList);
    final CycObject result = getConverse().converseCycObject(command);
    if (result instanceof DenotationalTerm) {
      mt = makeElMt(result);
    } else if (result instanceof List) {
      mt = ElMtCycNaut.makeElMtCycNaut((List) result);
    } else {
      throw new CycApiException("Can't canonicalize " + cycList);
    }
    return mt;
  }
  
  @Override
  public ElMt canonicalizeHLMT(Naut naut)
          throws CycConnectionException, CycApiException {
    return canonicalizeHLMT(naut.toCycList());
  }
  
  @Override
  public CycArrayList canonicalizeList(CycList cycList)
          throws CycConnectionException, CycApiException {
    CycArrayList canonicalList = new CycArrayList();
    Iterator iter = cycList.iterator();

    while (iter.hasNext()) {
      Object obj = iter.next();

      if (obj instanceof CycArrayList) {
        canonicalList.add(getHLCycTerm(((CycArrayList) obj).cyclify()));
      } else if (obj instanceof Nart) {
        canonicalList.add(getHLCycTerm(((Nart) obj).cyclify()));
      } else {
        canonicalList.add(obj);
      }
    }

    return canonicalList;
  }
  
  @Override
  public CycConstant constantNameCaseCollision(String name)
          throws CycConnectionException, CycApiException {
    Object object = getConverse().converseObject(
            "(constant-name-case-collision \"" + name + "\")");

    if (object instanceof CycConstant) {
      return (CycConstant) object;
    } else {
      return null;
    }
  }
  
  @Override
  public CycList generateDisambiguationPhraseAndTypes(CycList objects)
          throws CycConnectionException, CycApiException {
    String command = makeSubLStmt(
            makeCycSymbol("generate-disambiguation-phrases-and-types"), objects);
    return getConverse().converseList(command);
  }
  
  @Override
  public Object getHLCycTerm(String string)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseObject("(canonicalize-term  '" + string + ")");
  }
  
  @Override
  public Object getELCycTerm(String string)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseObject("(identity '" + string + ")");
  }
  
  @Override
  public CycConstantImpl makeConstantWithGuidName(String guidString, String constantName) {
    return makeConstantWithGuidName(CycObjectFactory.makeGuid(
            guidString),
            constantName);
  }
  
  @Override
  public CycConstantImpl makeConstantWithGuidName(Guid guid,
          String constantName) {
    CycConstantImpl answer = CycObjectFactory.getCycConstantCacheByGuid(guid);
    if (answer != null) {
      return answer;
    }
    answer = new CycConstantImpl(constantName, guid);
    CycObjectFactory.addCycConstantCache(answer);
    return answer;
  }
  
  @Override
  public CycConstantImpl makeCycConstant(String name) throws CycConnectionException, CycApiException {
    return makeCycConstant(name, true, true);
  }
  
  @Override
  public CycConstantImpl makeCycConstant(String name, boolean bookkeeping, boolean transcript)
          throws CycConnectionException, CycApiException {
    String constantName = name;

    if (constantName.startsWith(HD)) {
      constantName = constantName.substring(2);
    }

    CycConstantImpl cycConstant = getConstantByName_inner(name);

    if (cycConstant != null) {
      return cycConstant;
    }

    final String fn = (transcript) ? "ke-create-now" : "fi-create-int";
    String command = "(" + fn + " \"" + constantName + "\")";
    if (bookkeeping) {
      command = getConverse().wrapBookkeeping(command);
    }
    Object object = getConverse().converseObject(command);

    if (object instanceof CycConstantImpl) {
      cycConstant = (CycConstantImpl) object;
    } else {
      throw new com.cyc.base.exception.CycApiException("Cannot create new constant for " + name);
    }
    CycObjectFactory.addCycConstantCache(cycConstant);
    if (getCurrentTransaction() != null) {
      getCurrentTransaction().noteCreation(cycConstant);
    }
    return cycConstant;
  }
  
  @Override
  public CycArrayList<Object> makeCycList(String string)
          throws CycApiException {
    return (new CycListParser(this.getCyc())).read(string);
  }
  
  @Override
  public Naut makeCycNaut(String string) throws CycApiException {
    return new NautImpl(makeCycList_inner(string));
  }
  
  @Override
  public CycFormulaSentence makeCycSentence(String string) throws CycApiException {
    return new CycFormulaSentence(makeCycList_inner(string));
  }
  
  @Override
  public CycFormulaSentence makeCyclifiedSentence(String string)
          throws CycApiException, CycConnectionException {
    String cyclified = getCyc().cyclifyString(string);
    return makeCycSentence(cyclified);
  }
  
  @Override
  public ElMt makeElMt(Object object)
          throws CycConnectionException, CycApiException {
    if (object instanceof ElMt) {
      return (ElMt) object;
    } else if (object instanceof CycArrayList) {
      return canonicalizeHLMT((CycArrayList) object);
    } else if (object instanceof Naut) {
      return canonicalizeHLMT((Naut) object);
    } else if (object instanceof CycConstantImpl) {
      return ElMtConstant.makeElMtConstant((CycConstantImpl) object);
    } else if (object instanceof Nart) {
      return ElMtNart.makeElMtNart((Nart) object);
    } else if (object instanceof String) {
      String elmtString = object.toString().trim();
      if (elmtString.startsWith("(")) {
        if (!elmtString.contains("#$")) {
          elmtString = getCyc().cyclifyString(elmtString);
        }
        @SuppressWarnings("unchecked")
        CycList<Object> elmtCycList = makeCycList_inner(elmtString);
        return makeElMt_inner(elmtCycList);
      } else {
        return makeElMt(getKnownConstantByName_inner(elmtString));
      }
    } else {
      throw new IllegalArgumentException("Can't make an ElMt from " + object
              + " class: " + object.getClass().getSimpleName());
    }
  }
  
  @Override
  public ElMt makeElMt(CycObject cycObject)
          throws CycConnectionException, CycApiException {
    if (cycObject instanceof ElMt) {
      return (ElMt) cycObject;
    } else if (cycObject instanceof CycArrayList) {
      return canonicalizeHLMT((CycArrayList) cycObject);
    } else if (cycObject instanceof Naut) {
      return canonicalizeHLMT((Naut) cycObject);
    } else if (cycObject instanceof Fort) {
      return makeElMt((Fort) cycObject);
    } else {
      throw new IllegalArgumentException("Can't make an ElMt from " + cycObject
              + " class: " + cycObject.getClass().getSimpleName());
    }
  }
  
  @Override
  public ElMt makeElMt(Fort cycObject) {
    ElMt result = null;
    if (cycObject instanceof CycConstantImpl) {
      result = ElMtConstant.makeElMtConstant((CycConstantImpl) cycObject);
    } else if (cycObject instanceof Nart) {
      result = ElMtNart.makeElMtNart((Nart) cycObject);
    } else {
      throw new IllegalArgumentException("CycObject: " + cycObject.cyclify()
              + "is not a valid ElMt.");
    }
    return result;
  }
  
  @Override
  public ElMt makeElMt(String elmtString)
          throws CycConnectionException, CycApiException {
    elmtString = elmtString.trim();
    if (elmtString.startsWith("(")) {
              if (!elmtString.contains("#$")) {
          elmtString = getCyc().cyclifyString(elmtString);
        }
      CycList elmtCycList = makeCycList_inner(elmtString);
      return makeElMt_inner(elmtCycList);
    } else {
      return makeElMt(getKnownConstantByName_inner(elmtString));
    }
  }
  
  @Override
  public CycConstant makeUniqueCycConstant(final String startName)
          throws CycConnectionException, CycApiException {
    final String constantName = startName.startsWith(HD) ? startName.substring(2) : startName;
    String suffix = "";
    int suffixNum = 0;
    while (true) {
      final String command = "(constant-name-available \"" + constantName + suffix + "\")";
      if (getConverse().converseBoolean(command)) {
        break;
      }
      if (suffix.length() == 0) {
        suffixNum = ((int) (9 * Math.random())) + 1;
      } else {
        suffixNum = (suffixNum * 10) + ((int) (10 * Math.random()));
      }
      suffix = String.valueOf(suffixNum);
    }
    return makeCycConstant(startName + suffix);
  }
  
  @Override
  public CycConstantImpl makeUniqueCycConstant(String startName, String prefix)
          throws CycConnectionException, CycApiException {
    final String constantName = startName.startsWith(HD) ? startName.substring(2) : startName;
    final String command = getConverse()
            .wrapBookkeeping("(gentemp-constant \"" + constantName + "\" \"" + prefix + "\")");
    final CycConstantImpl cycConstant = (CycConstantImpl) getConverse().converseObject(command);
    CycObjectFactory.addCycConstantCache(cycConstant);
    if (getCurrentTransaction() != null) {
      getCurrentTransaction().noteCreation(cycConstant);
    }
    return cycConstant;
  }
  
  /** 
   * Record the information that <tt>focalTerm</tt> is known to have a fact sheet accessible to this CycClient 
   * @param focalTerm
   */
  public void noteTermHasPrecachedFactSheet(final CycObject focalTerm) {
    termsKnownToHavePrecachedFactSheets.add(focalTerm);
  }
  
  /**
   * @param focalTerm
   * @return true iff <tt>focalTerm</tt> is known to have a fact sheet accessible to this CycClient 
   */
  public boolean termKnownToHavePrecachedFactSheet(final CycObject focalTerm) {
    return termsKnownToHavePrecachedFactSheets.contains(focalTerm);
  }
  
  public CycList phraseStructureParse(String str)
          throws CycConnectionException, CycApiException {
    String command = makeSubLStmt(
            makeCycSymbol("ps-get-cycls-for-phrase"), str);
    return getConverse().converseList(command);
  }
  
  @Override
  public synchronized void rename(final CycConstant cycConstant,
          final String newName)
          throws CycConnectionException, CycApiException {
    rename(cycConstant, newName, true, true);
  }
  
  @Override
  public synchronized void rename(final CycConstant cycConstant,
          final String newName, final boolean bookkeeping,
          final boolean transcript)
          throws CycConnectionException, CycApiException {
    final String fn = (transcript) ? "ke-rename-now" : "rename-constant";
    String command = "(" + fn + " " + cycConstant.stringApiValue() + "\"" + newName + "\")";
    if (bookkeeping) {
      command = getConverse().wrapBookkeeping(command);
    }
    Object result = getConverse().converseObject(command);
    if (result.equals(CycObjectFactory.nil)) {
      throw new CycApiException(
              newName + " is an invalid new name for " + cycConstant.cyclify());
    }
    CycObjectFactory.removeCaches(cycConstant);
    cycConstant.setName(newName);
    CycObjectFactory.addCycConstantCache(cycConstant);
  }
  
  @Override
  public Object getSymbolValue(CycSymbol cycSymbol)
          throws CycConnectionException, CycApiException {
    return getConverse().converseObject("(symbol-value " + cycSymbol.stringApiValue() + ")");
  }
  
  @Override
  public void setSymbolValue(CycSymbol cycSymbol, Object value)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    getConverse().converseVoid(makeSubLStmt("csetq", new SublApiHelper.AsIsTerm(cycSymbol),
            value));
  }
  
  @Override
  public CycObject toMostSpecificCycObject(CycList cycObject) {
    if (cycObject != null) {
      {
        final Object possibleSentence = CycFormulaSentence.convertIfPromising(cycObject);
        if (possibleSentence != cycObject) {
          return (FormulaSentence) possibleSentence;
        }
      }
      {
        final Object possibleNaut = NautImpl.convertIfPromising(cycObject);
        if (possibleNaut != cycObject) {
          return (Naut) possibleNaut;
        }
      }
    }
    return cycObject;
  }
  
  
  // Internal
  
  final private Set<CycObject> termsKnownToHavePrecachedFactSheets = new HashSet<>();
  
}
