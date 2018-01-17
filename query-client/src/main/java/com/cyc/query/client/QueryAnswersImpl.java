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
package com.cyc.query.client;

/*
 * #%L
 * File: QueryAnswersImpl.java
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

import com.cyc.base.inference.InferenceResultSet;
import com.cyc.kb.Variable;
import com.cyc.kb.exception.KbTypeException;
import com.cyc.query.QueryAnswer;
import com.cyc.query.QueryAnswers;
import com.cyc.query.exception.QueryRuntimeException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

import static java.util.stream.Collectors.toList;

/**
 *
 * @author nwinant
 * @param <E>
 */
public class QueryAnswersImpl<E extends QueryAnswer>
        extends ArrayList<E>
        implements QueryAnswers<E> {
  
  //====|    Factory methods    |=================================================================//
  
  public static QueryAnswers<QueryAnswer> fromInferenceResults(InferenceResultSet resultSet) 
          throws KbTypeException {
    final List<QueryAnswer> answers = new ArrayList<>();
    for (int i = 0; i < resultSet.getCurrentRowCount(); i++) {
      answers.add(BindingsBackedQueryAnswer.fromInferenceResults(resultSet, i));
    }
    return new QueryAnswersImpl<>(answers);
  }
  
  //====|    Construction    |====================================================================//
  
  public QueryAnswersImpl(Collection<E> answers) {
    super(answers);
  }
  
  public QueryAnswersImpl(int size) {
    super(size);
  }
  
  public QueryAnswersImpl() {
    super();
  }
  
  //====|    Public methods    |==================================================================//
  
  @Override
  public <T> List<T> getBindingsForVariable(Variable var) {
    final List<T> results = new ArrayList();
    this.forEach((answer) -> {
      results.add(answer.<T>getBinding(var));
    });
    return results;
  }
  
  @Override
  public <T> Set<T> getUniqueBindingsForVariable(Variable var) {
    return new HashSet(getBindingsForVariable(var));
  }
  
  @Override
  public <T> List<T> getBindingsForOnlyVariable() {
    final Variable var = findSoleVariable();
    return (var != null) ? this.<T>getBindingsForVariable(var) : new ArrayList();
  }
  
  @Override
  public <T> Set<T> getUniqueBindingsForOnlyVariable() {
    return new HashSet(getBindingsForOnlyVariable());
  }
  
  @Override
  public Optional<E> getOnlyAnswer() throws QueryRuntimeException {
    if (size() > 1) {
      throw new QueryRuntimeException("Expected exactly one answer, but found " + size());
    }
    return (size() == 1) ? Optional.of(get(0)) : Optional.empty();
  }
  
  @Override
  public <T> Optional<T> getOnlyBindingForVariable(Variable var) {
    if (size() > 1) {
      final List<String> bindings = new ArrayList();
      this.forEach((answer) -> {
        bindings.add("" + answer.getBinding(var));
      });
      final String msg = "Expected exactly one binding for " + var
                                 + " but found " + size() + ": " + bindings;
      throw new QueryRuntimeException(msg);
    }
    return (size() == 1) ? Optional.of(get(0).<T>getBinding(var)) : Optional.empty();
  }

  @Override
  public <T> Optional<T> getOnlyBindingForOnlyVariable() {
    return this.<T>getOnlyBindingForVariable(findSoleVariable());
  }

  @Override
  public List<String> toAnswersTableStrings(boolean includeOuterBorder, String colBorder, String colPadding) {
    final List<String> results = new ArrayList();
    final List<Variable> vars = (!isEmpty())
            ? new ArrayList(get(0).getVariables()) 
            : new ArrayList();
    final int[] longestVarLength = new int[vars.size()];
    for (int i = 0; i < vars.size(); i++) {
      final int currLongest = longestVarLength[i];
      final int varLength = vars.get(i).toString().length();
      longestVarLength[i] = (varLength > currLongest) ? varLength : currLongest;
    }
    this.forEach((answer) -> {
      for (int i = 0; i < vars.size(); i++) {
        final int currLongest = longestVarLength[i];
        final int varLength = (answer.hasBinding(vars.get(i))) ? ("" + answer.getBinding(vars.get(i))).length() :  0;
        longestVarLength[i] = (varLength > currLongest) ? varLength : currLongest;
      }
    });
    {
      final StringBuilder header = new StringBuilder();
      final StringBuilder headHr = new StringBuilder();
      if (includeOuterBorder) {
        header.append(colBorder).append(colPadding);
        headHr.append(colBorder).append(colPadding);
      }
      header.append("Answer");
      headHr.append("------");
      for (int i = 0; i < vars.size(); i++) {
        final int maxPadding = longestVarLength[i];
        final Variable var = vars.get(i);
        header.append(colPadding).append(colBorder);
        headHr.append(colPadding).append(colBorder);
        header.append(colPadding).append(String.format("%1$-" + maxPadding + "s", var));
        headHr.append(colPadding).append(String.format("%1$-" + maxPadding + "s", "").replace(' ', '-'));
      }
      if (includeOuterBorder) {
        header.append(colPadding).append(colBorder);
        headHr.append(colPadding).append(colBorder);
        results.add(String.format("%1$-" + headHr.toString().length() + "s", "").replace(' ', '-'));
      }
      results.add(header.toString());
      results.add(headHr.toString());
    }
    for (int ansNum = 0; ansNum < this.size(); ansNum++) {
      final QueryAnswer answer = this.get(ansNum);
      final StringBuilder row = new StringBuilder();
      if (includeOuterBorder) {
        row.append(colBorder).append(colPadding);
      }
      row.append(String.format("%1$6d", ansNum));
      for (int i = 0; i < vars.size(); i++) {
        row.append(colPadding).append(colBorder);
        if (answer.hasBinding(vars.get(i))) {
          row.append(colPadding).append(
                  StringUtils.rightPad("" + answer.getBinding(vars.get(i)), longestVarLength[i]));
        } else {
          row.append(colPadding).append(StringUtils.rightPad("", longestVarLength[i]));
        }
      }
      if (includeOuterBorder) {
        row.append(colPadding).append(colBorder);
      }
      results.add(row.toString());
    }
    if (includeOuterBorder) {
      results.add(results.get(0));
    }
    return results;
  }
  
  @Override
  public List<String> toAnswersTableStrings(boolean includeOuterBorder) {
    return toAnswersTableStrings(includeOuterBorder, "|", "  ");
  }
  
  @Override
  public void printAnswersTable(PrintStream out, 
                                boolean includeOuterBorder,
                                String colBorder, 
                                String colPadding) {
    toAnswersTableStrings(includeOuterBorder, colPadding, colBorder).forEach(out::println);
  }
  
  @Override
  public void printAnswersTable(PrintStream out, boolean includeOuterBorder) {
    toAnswersTableStrings(includeOuterBorder).forEach(out::println);
  }
  
  @Override
  public List<String> toBindingsStringsForVariable(Variable var) {
    return QueryAnswersImpl.this.getBindingsForVariable(var).stream()
            .map(Object::toString)
            .sorted()                      // TODO: should we sort by default? - nwinant, 2017-08-22
            .collect(toList());
  }
  
  //====|    Internal    |========================================================================//
  
  private Variable findSoleVariable() {
    if (!isEmpty()) {
      if (get(0).getVariables().size() == 1) {
        return get(0).getVariables().iterator().next();
      }
      throw new QueryRuntimeException(
              "Expected exactly one variable but found " + get(0).getVariables().size());
    }
    return null;
  }
  
}
