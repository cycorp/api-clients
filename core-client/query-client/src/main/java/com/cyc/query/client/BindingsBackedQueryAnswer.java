/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyc.query.client;

/*
 * #%L
 * File: BindingsBackedQueryAnswer.java
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
import com.cyc.kb.KbTerm;
import com.cyc.kb.Variable;
import com.cyc.kb.client.KbObjectImpl;
import com.cyc.kb.exception.CreateException;
import com.cyc.nl.Paraphrase;
import com.cyc.nl.ParaphraseImpl;
import com.cyc.nl.Paraphraser;
import com.cyc.query.InferenceAnswerIdentifier;
import com.cyc.query.ParaphrasedQueryAnswer;
import com.cyc.query.exception.QueryRuntimeException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author baxter
 */
class BindingsBackedQueryAnswer implements ParaphrasedQueryAnswer {

  private final Map<Variable, Object> bindings;
  private final Map<Variable, Paraphrase> paraphrasedBindings;
  private final InferenceAnswerIdentifier id;
  private final Paraphraser paraphraser;

  public BindingsBackedQueryAnswer(Map<Variable, Object> bindings) {
    this(bindings, null, null);
  }

  public BindingsBackedQueryAnswer(Map<Variable, Object> bindings,
          InferenceAnswerIdentifier id) {
    this(bindings, id, null);
  }

  public BindingsBackedQueryAnswer(Map<Variable, Object> bindings,
          InferenceAnswerIdentifier id, Paraphraser paraphraser) {
    this.bindings = bindings;
    this.id = id;
    this.paraphraser = paraphraser;
    this.paraphrasedBindings = new HashMap<>();
  }

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
  public Paraphraser getParaphraser() {
    return paraphraser;
  }

  @Override
  public InferenceAnswerIdentifier getId() {
    if (id == null) {
      throw new UnsupportedOperationException();
    } else {
      return id;
    }
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
      throw new QueryRuntimeException("Unable to get sources for BindingsBackedQueryAnswer without an inference answer id");
    }
    return new InferenceAnswerBackedQueryAnswer(getId()).getSources();
  }

  @Override
  public Paraphrase getBindingParaphrase(Variable var) {
    if (!(paraphraser instanceof Paraphraser)) {
      throw new UnsupportedOperationException("Unable to paraphrase when no paraphraser has been provided.");
    }
    return paraphraser.paraphrase((getBinding(var)));
  }

  @Override
  public Map<Variable, Paraphrase> getParaphrasedBindings() {
    if (paraphrasedBindings.size() != bindings.size()) {
      for (Map.Entry<Variable, Object> binding : bindings.entrySet()) {
        if (!paraphrasedBindings.containsKey(binding.getKey())) {
          Paraphrase paraphrase = (paraphraser == null) ? new ParaphraseImpl(null, binding.getValue()) : paraphraser.paraphrase(binding.getValue());
          paraphrasedBindings.put(binding.getKey(), paraphrase);
        }
      }
    }
    return paraphrasedBindings;
  }

  @Override
  public List<String> toPrettyBindingsStrings() {
    final List<String> results = new ArrayList();
    int longestVarStrLength = 0;
    for (Variable var : getVariables()) {
      final int varStrLength = var.toString().length();
      longestVarStrLength = (varStrLength > longestVarStrLength)
              ? varStrLength
              : longestVarStrLength;
    }
    for (Variable var : getVariables()) {
      results.add(
              String.format("%1$-" + longestVarStrLength + "s", var.toString())
              + " = " + getBinding(var));
    }
    return results;
  }

}
