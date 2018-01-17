package com.cyc.query.client;

/*
 * #%L
 * File: AbstractParaphrasedQueryAnswer.java
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
import com.cyc.kb.Variable;
import com.cyc.nl.Paraphrase;
import com.cyc.nl.Paraphraser;
import com.cyc.query.InferenceAnswerIdentifier;
import com.cyc.query.ParaphrasedQueryAnswer;
import com.cyc.query.exception.QueryRuntimeException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author nwinant
 */
public abstract class AbstractParaphrasedQueryAnswer implements ParaphrasedQueryAnswer {
  
  //====|    Fields    |==========================================================================//
  
  protected final InferenceAnswerIdentifier id;
  protected final Paraphraser paraphraser;
  protected final Map<Variable, Paraphrase> bindingParaphrases;
  
  //====|    Construction    |====================================================================//
  
  protected AbstractParaphrasedQueryAnswer(InferenceAnswerIdentifier id, Paraphraser paraphraser) {
    this.id = id;
    this.paraphraser = paraphraser;
    this.bindingParaphrases = new HashMap<>();
  }
  
  //====|    Public methods    |==================================================================//
  
  @Override
  public InferenceAnswerIdentifier getId() {
    if (id == null) {
      throw new UnsupportedOperationException();
    } else {
      return id;
    }
  }
  
  @Override
  public Paraphraser getParaphraser() {
    return paraphraser;
  }
  
  @Override
  public Paraphrase getBindingParaphrase(Variable var) {
    if (!(paraphraser instanceof Paraphraser)) {
      throw new UnsupportedOperationException(
              "Unable to paraphrase when no paraphraser has been provided.");
    }
    if (!bindingParaphrases.containsKey(var)) {
      bindingParaphrases.put(var, paraphraser.paraphrase((getBinding(var))));
    }
    return bindingParaphrases.get(var);
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
  
  @Override
  public <T> T getOnlyBinding() throws QueryRuntimeException {
    return getBinding(findSoleVariable());
  }

  //====|    Internal    |==========================================================================//
  
  protected Variable findSoleVariable() {
    if (getVariables().size() == 1) {
      return getVariables().iterator().next();
    }
    throw new QueryRuntimeException(
            "Expected exactly one variable but found " + getVariables().size());
  }
  
}
