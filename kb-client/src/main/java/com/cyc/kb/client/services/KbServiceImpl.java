/*
 * Copyright 2015 Cycorp, Inc.
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
package com.cyc.kb.client.services;

/*
 * #%L
 * File: KbServiceImpl.java
 * Project: KB Client
 * %%
 * Copyright (C) 2013 - 2017 Cycorp, Inc
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
import com.cyc.base.CycAccessManager;
import com.cyc.base.cycobject.CycObject;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.baseclient.cycobject.CycSymbolImpl;
import com.cyc.baseclient.cycobject.CycVariableImpl;
import com.cyc.baseclient.cycobject.DefaultCycObjectImpl;
import com.cyc.kb.KbObject;
import com.cyc.kb.KbStatus;
import com.cyc.kb.client.AssertionImpl;
import com.cyc.kb.client.KbObjectImpl;
import com.cyc.kb.client.KbObjectImplFactory;
import com.cyc.kb.client.KbTermImpl;
import com.cyc.kb.exception.CreateException;
import com.cyc.kb.exception.KbObjectNotFoundException;
import com.cyc.kb.exception.KbRuntimeException;
import com.cyc.kb.exception.KbTypeException;
import com.cyc.kb.spi.KbApiService;
import com.cyc.kb.spi.KbService;
import com.cyc.session.exception.SessionException;
import java.util.Optional;

import static com.cyc.kb.KbObject.hasValidKbApiObjectType;


/**
 *
 * @author nwinant
 */
public class KbServiceImpl implements KbService {

  final private KbApiService services;

  public KbServiceImpl(KbApiService services) {
    this.services = services;
  }

  // Public
  @Override
  public boolean existsInKb(String nameOrId) {
    final String string = cleanString(nameOrId);
    return !(KbStatus.DOES_NOT_EXIST.equals(KbObjectImplFactory.getStatus(string, KbTermImpl.class))
            && KbStatus.DOES_NOT_EXIST.equals(KbObjectImplFactory.getStatus(string, AssertionImpl.class)));
  }

  @Override
  public KbObject getKbObject(String nameOrIdOrCycl) throws CreateException, KbTypeException {
    final String string = cleanString(nameOrIdOrCycl);
    if (isVariable(string)) {
      return services.getVariableService().get(string);
    }
    if (isKeyword(string)) {
      return services.getSymbolService().get(string);
    }
    if (isSentence(string)) {
      return services.getSentenceService().get(string);
    }
    try {
      return KbObjectImpl.get(string);  // TODO: should this be replaced with a call to a service?
    } catch (KbObjectNotFoundException ex) {
      throw new KbObjectNotFoundException("Could not retrieve term for " + nameOrIdOrCycl, ex);
    }
  }

  @Override
  public Object getApiObject(Object nameOrIdOrCycl) throws CreateException, KbTypeException {
    final String str = "(((#$isa ?VAR0 #$SignalImplementationPredicate) (#$arity ?VAR0 ?VAR1) (?VAR0 ?VAR2 ?VAR3 ?VAR4) (#$evaluate ?VAR6 (#$FormulaArgFn ?VAR1 (?VAR0 ?VAR2 ?VAR3 ?VAR4)))) ((#$signalValueAlongDimensionForPeriod ?VAR2 ?VAR3 ?VAR4 ?VAR0 ?VAR6)))";
    if (str.equals("" + nameOrIdOrCycl)) {
      System.out.println("...");
    }
    if (nameOrIdOrCycl instanceof CycObject) {
      return KbObjectImpl.get((CycObject)nameOrIdOrCycl);
    } else if (nameOrIdOrCycl instanceof String) {
      return getApiObject((String) nameOrIdOrCycl);
    } else if (hasValidKbApiObjectType(nameOrIdOrCycl)) {
      return nameOrIdOrCycl;
    } else throw new KbTypeException(
              "Unable to make an API object from " + nameOrIdOrCycl
                      + Optional.of(nameOrIdOrCycl).map(o -> " (" + o.getClass() + ")").orElse("")
                      + " because it's neither a String nor a CycObject.");
  }
  
  @Override
  public Object getApiObject(String nameOrIdOrCycl) throws CreateException, KbTypeException {
    final String string = cleanString(nameOrIdOrCycl);
    if (isVariable(string)) {
      return services.getVariableService().get(string);
    }
    if (isKeyword(string)) {
      return services.getSymbolService().get(string);
    }
    if (isSentence(string)) {
      return services.getSentenceService().get(string);
    }
    Object javaObj = fromJavaObject(nameOrIdOrCycl);
    if (javaObj != null) {
      return javaObj;
    }
    try {
      return KbObjectImpl.get(string);  // TODO: should this be replaced with a call to a service?
    } catch (KbObjectNotFoundException ex) {
      throw new KbObjectNotFoundException("Could not retrieve term for " + nameOrIdOrCycl, ex);
    }
  }
  
  @Override
  public Object getApiObjectDwim(String nameOrIdOrCycl) throws CreateException, KbTypeException {
    /*
    // From KbFactory#getApiObjectDwim(String):
    if (nameOrIdOrCycl.startsWith("{") && nameOrIdOrCycl.endsWith("}")) {
      nameOrIdOrCycl = "(TheSet " + nameOrIdOrCycl.substring(1, nameOrIdOrCycl.length() - 1) + ")";
    }
    if (nameOrIdOrCycl.startsWith("[") && nameOrIdOrCycl.endsWith("]")) {
      nameOrIdOrCycl = "(TheList " + nameOrIdOrCycl.substring(1, nameOrIdOrCycl.length() - 1) + ")";
    }
    return KB_SERVICE.getApiObject(nameOrIdOrCycl);
    */
    if (nameOrIdOrCycl.startsWith("{") && nameOrIdOrCycl.endsWith("}")) {
      nameOrIdOrCycl = "(#$TheSet " + nameOrIdOrCycl.substring(1, nameOrIdOrCycl.length()) + ")";
    }
    if (nameOrIdOrCycl.startsWith("[") && nameOrIdOrCycl.endsWith("]")) {
      nameOrIdOrCycl = "(#$TheList " + nameOrIdOrCycl.substring(1, nameOrIdOrCycl.length()) + ")";
    }
    return getApiObject(nameOrIdOrCycl);
  }

  // Private
  private String cleanString(String inputString) {
    if (inputString == null) {
      NullPointerException npe = new NullPointerException("String cannot be null");
      throw new KbRuntimeException(npe);
    }
    final String trimmedString = inputString.trim();
    if (trimmedString.isEmpty()) {
      throw new KbRuntimeException("String cannot be empty");
    }
    return trimmedString;
  }

  private boolean isVariable(String nameOrId) {
    return nameOrId.startsWith("?")
            && CycVariableImpl.isValidVariableName(nameOrId.toUpperCase());
  }

  private boolean isKeyword(String string) {
    return string.startsWith(":")
            && CycSymbolImpl.isValidSymbolName(string);
  }

  private String getInnerStringWithoutPrefix(String string) {
    if (!string.startsWith("(")) {
      return null;
    }
    final String inner = string.substring(1).trim();
    return (inner.startsWith("#$")) ? inner.substring(2) : inner;
  }

  private boolean isSentence(String string) {
    final String inner = getInnerStringWithoutPrefix(string);
    return (inner != null) && Character.isLowerCase(inner.charAt(0));
  }

  /*
  private boolean isNonAtomicTerm(String string) {
    final String inner = getInnerStringWithoutPrefix(string);
    return (inner != null) && Character.isUpperCase(inner.charAt(0));
  }
   */
  
  private Object fromJavaObject(String string) throws KbTypeException {
    if (string.startsWith("\"") && string.endsWith("\"")) {
      return string.substring(1, string.length() - 1);
    }
    try {
      return Long.valueOf(string);
    } catch (NumberFormatException ex) {
      //do nothing; it wasn't a long
    }
    try {
      return Double.parseDouble(string);
    } catch (NumberFormatException ex) {
      //do nothing; it wasn't a double
    }

    //maybe it's the hlid of a number or string.
    try {
      Object result = DefaultCycObjectImpl.fromPossibleCompactExternalId(string, CycAccessManager.getCurrentAccess());
      if (result instanceof String || result instanceof Number) {
        return result;
      }
    } catch (CycConnectionException | SessionException ex) {
      throw new KbTypeException("Exception encountered while trying to interpret '" + string + "' as a java object.", ex);
    }
    return null;
  }

  @Override
  public void clearCache() {
    KbObjectImplFactory.clearKBObjectCache();
  }
}
