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
package com.cyc.query.client.templates;

/*
 * #%L
 * File: OeTemplateResult.java
 * Project: Query Client
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

import com.cyc.base.cycobject.CycAssertion;
import com.cyc.base.cycobject.CycList;
import com.cyc.base.cycobject.CycObject;
import com.cyc.base.cycobject.CycSymbol;
import com.cyc.baseclient.CycObjectFactory;
import com.cyc.baseclient.cycobject.CycArrayList;
import com.cyc.baseclient.cycobject.CycSymbolImpl;
import com.cyc.baseclient.inference.DefaultResultSet;
import com.cyc.baseclient.kbtool.TemplateOeToolImpl.IpcQueueElement;
import com.cyc.kb.Assertion;
import com.cyc.kb.Context;
import com.cyc.kb.Sentence;
import com.cyc.kb.Symbol;
import com.cyc.kb.client.AssertionImpl;
import com.cyc.kb.client.ContextImpl;
import com.cyc.kb.client.KbContentLogger;
import com.cyc.kb.client.SentenceImpl;
import com.cyc.kb.client.SymbolImpl;
import com.cyc.kb.exception.KbTypeException;
import com.cyc.query.QueryAnswer;
import com.cyc.query.client.BindingsBackedQueryAnswer;
import com.cyc.query.exception.QueryRuntimeException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author nwinant
 */
public class OeTemplateResult {
  
  //====|    Enums    |===========================================================================//
  
  public enum OeTemplateResultField {
    
    ERRORS, 
    BINDINGS,
    ASSERTION_RESULT,
    ASSERTION_MT, 
    ASSERTION_SENTENCE;
    
    private final Symbol symbol;
    
    private OeTemplateResultField() {
      try {
        this.symbol = new SymbolImpl(IpcQueueElement.valueOf(this.toString()).toSymbol());
      } catch (KbTypeException ex) {
        throw QueryRuntimeException.fromThrowable(ex);
      }
    }
    
    public Symbol toSymbol() {
      return this.symbol;
    }
    
    CycSymbol toCycSymbol() {
      return new CycSymbolImpl(toSymbol().toString());
    }
  }
  
  //====|    Fields    |==========================================================================//
  
  private static final Logger LOG = LoggerFactory.getLogger(OeTemplateResult.class);
  
  public static final List<OeTemplateResultField> ALL_FIELDS
          = Collections.unmodifiableList(Arrays.asList(OeTemplateResultField.values()));
  
  public static final List<OeTemplateResultField> QUERY_FIELDS
          = Collections.unmodifiableList(Arrays.asList(
                  OeTemplateResultField.BINDINGS,
                  OeTemplateResultField.ERRORS));
  
  public static final List<OeTemplateResultField> ERROR_FIELDS
          = Collections.unmodifiableList(Arrays.asList(OeTemplateResultField.ERRORS));
  
  private final CycList plist;
  private final DefaultResultSet rs;
  private final QueryAnswer queryAnswer;
  private final List<Object> errors;
  private final Assertion assertionResult;
  private final Sentence assertionSentence;
  private final Context assertionMt;
  
  //====|    Construction    |====================================================================//
  
  OeTemplateResult(CycList plist) {
    if (plist.isEmpty()) {
      throw new QueryRuntimeException("Result is empty: " + plist);
    } else if (!plist.isPlist()) {
      throw new QueryRuntimeException("Result is not a PLIST: " + plist);
    }
    this.plist = plist;
    if (hasPlistValue(OeTemplateResultField.BINDINGS)) {
      final Object bindings = getPlistValue(OeTemplateResultField.BINDINGS);
      if (!(bindings instanceof List)) {
        throw new QueryRuntimeException(
                "Value of " + OeTemplateResultField.BINDINGS + " is not a list: " + bindings);
      }
      /*
      //final Map<Variable, Object> newBindings = new LinkedHashMap<>();
      ((List) bindings).forEach((e) -> {
        if (!(e instanceof List)) {
          throw new QueryRuntimeException(
                  "Element of " + OeTemplateResultField.BINDINGS + " is not a list: " + e);
        }
        final List l = (List) e;
        if (l.size() < 2) {
          throw new QueryRuntimeException(
                  "Binding for " + OeTemplateResultField.BINDINGS
                          + " must have at least 2 elements: " + e);
        } else {
          try {
            newBindings.put(
                    new VariableImpl((CycVariable) l.get(0)),
                    l.size() > 2 ? Collections.unmodifiableList(l.subList(1, l.size())) : l.get(1));
          } catch (KbTypeException ex) {
            throw QueryRuntimeException.fromThrowable(ex);
          }
        }
      });
      */
      this.rs = new DefaultResultSet(!CycObjectFactory.nil.equals(bindings)
                                             ? CycArrayList.list(bindings)
                                             : Collections.emptyList());
      if (rs.getCurrentRowCount() != 1) {
        throw new QueryRuntimeException(
                "Expected exactly 1 row, but found " + rs.getCurrentRowCount() + " in " + rs);
      }
      try {
        this.queryAnswer = BindingsBackedQueryAnswer.fromInferenceResults(rs, 1);
      } catch (KbTypeException ex) {
        throw QueryRuntimeException.fromThrowable(ex);
      }
    } else {
      this.rs = null;
      this.queryAnswer = null;
    }
    {
      List<Object> result = null;
      final Object rawValue = getPlistValue(OeTemplateResultField.ERRORS);
      if (rawValue != null) {
        result = Collections.unmodifiableList((rawValue instanceof List)
                                                      ? (List) rawValue
                                                      : Arrays.asList(rawValue));
      }
      this.errors = result;
    }
    {
      Assertion result = null;
      final Object rawValue = getPlistValue(OeTemplateResultField.ASSERTION_RESULT);
      if (rawValue instanceof CycAssertion) {
        try {
          result = AssertionImpl.get((CycAssertion) rawValue);
          //KbContentLogger.getInstance().logAssertResult((CycAssertion) rawValue);
          KbContentLogger.getInstance().logAssertResult(result);
        } catch (Throwable t) {
          LOG.error("Error converting CycAssertion to KbObject Assertion: " + t.getMessage(), t);
        }
      }
      this.assertionResult = result;
    }
    {
      Context result = null;
      final Object rawValue = getPlistValue(OeTemplateResultField.ASSERTION_MT);
      if (rawValue instanceof CycObject) {
        try {
          result = (Context) ContextImpl.get((CycObject) rawValue);
        } catch (Throwable t) {
          LOG.error("Error converting CycObject to KbObject Context: " + t.getMessage(), t);
        }
      }
      this.assertionMt = result;
    }
    {
      Sentence result = null;
      final Object rawValue = getPlistValue(OeTemplateResultField.ASSERTION_SENTENCE);
      if (rawValue instanceof CycList) {
        try {
          result = (Sentence) SentenceImpl.get((CycList) rawValue);
          if (assertionResult == null) {
            KbContentLogger.getInstance()
                    .logSupport((CycList) rawValue, ContextImpl.asELMt(assertionMt), true);
          }
        } catch (Throwable t) {
          LOG.error("Error converting CycList to KbObject Sentence: " + t.getMessage(), t);
        }
      }
      this.assertionSentence = result;
    }
    
  }
  
  //====|    Public methods    |==================================================================//
  
  public boolean hasBindings() {
    return getInferenceResults() != null && getInferenceResults().getCurrentRowCount() >= 1;
  }
  
  public DefaultResultSet getInferenceResults() {
    return this.rs;
  }
  
  public boolean hasErrors() {
    return getErrors() != null && !getErrors().isEmpty();
  }
  
  public List<Object> getErrors() {
    return this.errors;
  }
  
  public Assertion getAssertionResult() {
    return this.assertionResult;
  }
  
  public Sentence getAssertionSentence() {
    return this.assertionSentence;
  }
  
  public Context getAssertionMt() {
    return this.assertionMt;
  }
  
  public String toPrettyString(String indent) {
    return plist.toPrettyString(indent);
  }
    
  public synchronized QueryAnswer toQueryAnswer() {
    if (!hasBindings()) {
      throw new QueryRuntimeException(
              getClass().getSimpleName() + " has no bindings;"
                      + " cannot be converted to a " + QueryAnswer.class.getSimpleName());
    }
    return this.queryAnswer;
  }
  
  //====|    Internal methods    |================================================================//

  private Object getPlistValue(OeTemplateResultField field) {
    return plist.getf(field.toCycSymbol(), null, true);
  }
  
  private boolean hasPlistValue(OeTemplateResultField field) {
    return getPlistValue(field) != null;
  }
  
}
