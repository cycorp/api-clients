package com.cyc.query.client;

/*
 * #%L
 * File: BindingsBackedQueryAnswer.java
 * Project: Query Client
 * %%
 * Copyright (C) 2013 - 2018 Cycorp, Inc.
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
import com.cyc.Cyc;
import com.cyc.base.inference.InferenceResultSet;
import com.cyc.kb.KbTerm;
import com.cyc.kb.Variable;
import com.cyc.kb.client.KbObjectImpl;
import com.cyc.kb.exception.CreateException;
import com.cyc.kb.exception.KbTypeException;
import com.cyc.nl.Paraphrase;
import com.cyc.nl.ParaphraseImpl;
import com.cyc.nl.Paraphraser;
import com.cyc.query.InferenceAnswerIdentifier;
import com.cyc.query.ParaphrasedQueryAnswer;
import com.cyc.query.exception.QueryRuntimeException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author baxter
 */
public class BindingsBackedQueryAnswer
        extends AbstractParaphrasedQueryAnswer
        implements ParaphrasedQueryAnswer {
  
  //====|    Factory methods    |=================================================================//
  
  public static BindingsBackedQueryAnswer fromInferenceResults(InferenceResultSet rs,
                                                               int rowNum,
                                                               Paraphraser paraphraser)
          throws KbTypeException {
    final Map<Variable, Object> bindings = new HashMap<>();
    synchronized (rs) {
      try {
        rs.absolute(rowNum);
        for (String colName : rs.getColumnNames()) {
          final Object rawValue = rs.getObject(colName);
          final Object value;
          try {
            value = Cyc.getApiObject(rawValue);
          } catch (KbTypeException | CreateException ex) {
            throw QueryRuntimeException
                    .fromThrowable("Could not convert to KB API object: " + rawValue, ex);
          }
          bindings.put(Variable.get(colName), value);
        }
      } catch (SQLException ex) {
        throw QueryRuntimeException.fromThrowable(ex);
      }
    }
    return new BindingsBackedQueryAnswer(bindings, null, paraphraser);
  }

  public static BindingsBackedQueryAnswer fromInferenceResults(InferenceResultSet rs, int rowNum)
          throws KbTypeException {
    return fromInferenceResults(rs, rowNum, null);
  }
  
  //====|    Fields    |==========================================================================//
    
  private final Map<Variable, Object> bindings;
  
  //====|    Construction    |====================================================================//
  
  public BindingsBackedQueryAnswer(Map<Variable, Object> bindings) {
    this(bindings, null, null);
  }

  public BindingsBackedQueryAnswer(Map<Variable, Object> bindings, InferenceAnswerIdentifier id) {
    this(bindings, id, null);
  }

  public BindingsBackedQueryAnswer(
          Map<Variable, Object> bindings, InferenceAnswerIdentifier id, Paraphraser paraphraser) {
    super(id, paraphraser);
    this.bindings = bindings;
  }
  
  //====|    Public methods    |==================================================================//
  
  @Override
  public Set<Variable> getVariables() {
    return bindings.keySet();
  }

  @Override
  public <T> T getBinding(Variable var) {
    try {
      return KbObjectImpl.<T>checkAndCastObject(bindings.get(var));
    } catch (CreateException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public boolean hasBinding(Variable var) {
    return (bindings.containsKey(var));
  }

  @Override
  public Map<Variable, Object> getBindings() {
    return Collections.unmodifiableMap(bindings);
  }
  
  @Override
  public String toString() {
    if (id == null) {
      return "Query Answer with " + bindings.size() + " Binding"
              + ((bindings.size() == 1) ? "" : "s");
    } else {
      return id.toString();
    }
  }

  @Override
  public Set<KbTerm> getSources() {
    if (getId() == null) {
      throw new QueryRuntimeException(
              "Unable to get sources for BindingsBackedQueryAnswer without an inference answer id");
    }
    return new InferenceAnswerBackedQueryAnswer(getId()).getSources();
  }
  
  @Override
  public Map<Variable, Paraphrase> getParaphrasedBindings() {
    if (bindingParaphrases.size() != bindings.size()) {
      for (Map.Entry<Variable, Object> binding : bindings.entrySet()) {
        if (!bindingParaphrases.containsKey(binding.getKey())) {
          Paraphrase paraphrase = (paraphraser == null) ? new ParaphraseImpl(null, binding.getValue()) : paraphraser.paraphrase(binding.getValue());
          bindingParaphrases.put(binding.getKey(), paraphrase);
        }
      }
    }
    return bindingParaphrases;
  }
  
}
