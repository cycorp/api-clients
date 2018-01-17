package com.cyc.baseclient.inference.params;

/*
 * #%L
 * File: SpecifiedInferenceParameters.java
 * Project: Base Client
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

import com.cyc.base.CycAccess;
import com.cyc.base.cycobject.CycSymbol;
import com.cyc.baseclient.CycObjectFactory;
import com.cyc.baseclient.cycobject.CycArrayList;
import com.cyc.baseclient.inference.metrics.InferenceMetricsHashSet;
import com.cyc.baseclient.inference.params.OpenCycInferenceParameterEnum.OpenCycInferenceMode;
import com.cyc.query.metrics.InferenceMetrics;
import com.cyc.query.parameters.DisjunctionFreeElVarsPolicy;
import com.cyc.query.parameters.InferenceAnswerLanguage;
import com.cyc.query.parameters.InferenceMode;
import com.cyc.query.parameters.InferenceParameters;
import static com.cyc.query.parameters.InferenceParameters.ANSWER_LANGUAGE;
import static com.cyc.query.parameters.InferenceParameters.BROWSABLE;
import static com.cyc.query.parameters.InferenceParameters.CONTINUABLE;
import static com.cyc.query.parameters.InferenceParameters.DISJUNCTION_FREE_EL_VARS_POLICY;
import static com.cyc.query.parameters.InferenceParameters.EQUALITY_REASONING_DOMAIN;
import static com.cyc.query.parameters.InferenceParameters.INFERENCE_MODE;
import static com.cyc.query.parameters.InferenceParameters.INTERMEDIATE_STEP_VALIDATION_LEVEL;
import static com.cyc.query.parameters.InferenceParameters.MAX_NUMBER;
import static com.cyc.query.parameters.InferenceParameters.MAX_TIME;
import static com.cyc.query.parameters.InferenceParameters.MAX_TRANSFORMATION_DEPTH;
import static com.cyc.query.parameters.InferenceParameters.METRICS;
import static com.cyc.query.parameters.InferenceParameters.RESULT_UNIQUENESS;
import static com.cyc.query.parameters.InferenceParameters.TRANSITIVE_CLOSURE_MODE;
import com.cyc.query.parameters.ProblemReusePolicy;
import com.cyc.query.parameters.ProofValidationMode;
import com.cyc.query.parameters.ResultUniqueness;
import com.cyc.query.parameters.TransitiveClosureMode;
import java.util.Collection;
import java.util.Iterator;

/**
 * <P>
 * SpecifiedInferenceParameters is designed to be used when you want to carry
 * around inference parameters, but do not have access to a CycAccess instance.
 * It will not perform value canonicalization or other useful checks on the
 * names or values of the inference parameters. When the time comes to actually
 * run the query, this can be converted into a DefaultInferenceParameters object
 * by providing a CycAccess and calling the toDefaultInferenceParameters method.
 *
 * <P>
 * Copyright (c) 2011 Cycorp, Inc. All rights reserved.
 * <BR>This software is the proprietary information of Cycorp, Inc.
 * <P>
 * Use is subject to license terms.
 *
 * @author daves
 * @since March 15, 2011
 * @version $Id: SpecifiedInferenceParameters.java 151668 2014-06-03 21:46:52Z
 * jmoszko $
 */
public class SpecifiedInferenceParameters 
        extends InferenceParametersMap
        implements InferenceParameters {
  
  
  /* ====  Parameter names (not provided by com.cyc.query.parameters.InferenceParameters)  ====== */
  
  public static final String ALLOW_INDETERMINATE_RESULTS = ":allow-indeterminate-results?";

  public static final String CONDITIONAL_SENTENCE = ":CONDITIONAL-SENTENCE?";
  
  
  /* ====  Inference parameters values  ========================================================= */
  
  public static final String FIND_PROBLEM_STORE_BY_ID = "FIND-PROBLEM-STORE-BY-ID";
  
  public static final String LOAD_PROBLEM_STORE = "LOAD-PROBLEM-STORE";
  
  
  /* ====  Public methods: Accessors for specific properties  =================================== */
  
  @Override
  public Integer getMaxAnswerCount() {
    final Object rawValue = get(MAX_NUMBER);
    if (rawValue instanceof Integer) {
      return (Integer) rawValue;
    } else {
      return null;
    }
  }

  @Override
  public Integer getMaxTime() {
    final Object rawValue = get(MAX_TIME);
    if (rawValue instanceof Integer) {
      return (Integer) rawValue;
    } else {
      return null;
    }
  }

  @Override
  public SpecifiedInferenceParameters setMaxAnswerCount(Integer max) {
    put(MAX_NUMBER, max);
    return this;
  }

  @Override
  public SpecifiedInferenceParameters setMaxTime(Integer max) {
    put(MAX_TIME, max);
    return this;
  }
  
  /**
   * Get the set of inference metrics to gather.
   *
   * @return the inference metrics.
   */
  @Override
  public synchronized InferenceMetricsHashSet getMetrics() {
    InferenceMetricsHashSet metrics = (InferenceMetricsHashSet) get(METRICS);
    if (metrics == null) {
      metrics = new InferenceMetricsHashSet();
      setMetrics(metrics);
    }
    return metrics;
  }

  /**
   * Specify the set of inference metrics to gather.
   *
   * @param metrics
   */
  @Override
  public SpecifiedInferenceParameters setMetrics(InferenceMetrics metrics) {
    put(METRICS, metrics);
    return this;
  }

  @Override
  public SpecifiedInferenceParameters setInferenceMode(InferenceMode mode) {
    put(INFERENCE_MODE, mode.getDescription().getValue());
    return this;
  }

  @Override
  public InferenceMode getInferenceMode() {
    final Object modeSymbol = get(INFERENCE_MODE);
    return (modeSymbol == null) ? null : OpenCycInferenceMode.fromString(
            modeSymbol.toString());
  }

  @Override
  public boolean getAbductionAllowed() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public InferenceParameters setMaxTransformationDepth(Integer max) {
    put(MAX_TRANSFORMATION_DEPTH, max);
    return this;
  }

  @Override
  public Integer getMaxTransformationDepth() {
    final Object rawValue = get(MAX_TRANSFORMATION_DEPTH);
    if (rawValue instanceof Integer) {
      return (Integer) rawValue;
    } else {
      return null;
    }
  }

  @Override
  public Boolean isBrowsable() {
    return (Boolean) get(BROWSABLE);
  }

  @Override
  public SpecifiedInferenceParameters setBrowsable(boolean b) {
    final CycSymbol value = b ? CycObjectFactory.t : CycObjectFactory.nil;
    put(BROWSABLE, value);
    return this;
  }
  
  @Override
  public Boolean isContinuable() {
    return (Boolean) get(CONTINUABLE);
  }

  @Override
  public SpecifiedInferenceParameters setContinuable(boolean b) {
    final CycSymbol value = b ? CycObjectFactory.t : CycObjectFactory.nil;
    put(CONTINUABLE, value);
    return this;
  }

  @Override
  public SpecifiedInferenceParameters setResultUniqueness(ResultUniqueness value) {
    put(RESULT_UNIQUENESS, value);
    return this;
  }

  @Override
  public ResultUniqueness getResultUniqueness() {
    return getAs(RESULT_UNIQUENESS, ResultUniqueness.class);
  }

  @Override
  public SpecifiedInferenceParameters setAnswerLanguage(InferenceAnswerLanguage value) {
    put(ANSWER_LANGUAGE, value);
    return this;
  }

  @Override
  public InferenceAnswerLanguage getAnswerLanguage() {
    return getAs(ANSWER_LANGUAGE, InferenceAnswerLanguage.class);
  }

  @Override
  public SpecifiedInferenceParameters setTransitiveClosureMode(TransitiveClosureMode value) {
    put(TRANSITIVE_CLOSURE_MODE, value);
    return this;
  }

  @Override
  public TransitiveClosureMode getTransitiveClosureMode() {
    return getAs(TRANSITIVE_CLOSURE_MODE, TransitiveClosureMode.class);
  }

  @Override
  public SpecifiedInferenceParameters setProofValidationMode(ProofValidationMode value) {
    put(INTERMEDIATE_STEP_VALIDATION_LEVEL, value);
    return this;
  }

  @Override
  public ProofValidationMode getProofValidationMode() {
    return getAs(INTERMEDIATE_STEP_VALIDATION_LEVEL, ProofValidationMode.class);
  }

  @Override
  public SpecifiedInferenceParameters setDisjunctionFreeElVarsPolicy(DisjunctionFreeElVarsPolicy value) {
    put(DISJUNCTION_FREE_EL_VARS_POLICY, value);
    return this;
  }

  @Override
  public DisjunctionFreeElVarsPolicy getDisjunctionFreeElVarsPolicy() {
    return getAs(DISJUNCTION_FREE_EL_VARS_POLICY, DisjunctionFreeElVarsPolicy.class);
  }

  @Override
  public SpecifiedInferenceParameters setProblemReusePolicy(ProblemReusePolicy value) {
    put(EQUALITY_REASONING_DOMAIN, value);
    return this;
  }

  @Override
  public ProblemReusePolicy getProblemReusePolicy() {
    return getAs(EQUALITY_REASONING_DOMAIN, ProblemReusePolicy.class);
  }
  
  @Override
  public void setProblemStorePath(String path) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void setProblemStoreId(int id) {
    put(PROBLEM_STORE, new CycArrayList(FIND_PROBLEM_STORE_BY_ID, id));
  }
  
  
  /* ====  Public methods: other  =============================================================== */

  @Override
  public boolean usesLoadedProblemStore() {
    final Object value = get(PROBLEM_STORE);
    return (value instanceof CycArrayList
            && LOAD_PROBLEM_STORE.equals(((CycArrayList) value).first()));
  }
  
  @Override
  public String stringApiValue() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Object cycListApiValue() {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  
  @Override
  public Object parameterValueCycListApiValue(String key, Object val) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
  
  @Override
  public SpecifiedInferenceParameters clone() {
    SpecifiedInferenceParameters copy = new SpecifiedInferenceParameters();
    for (String key : this.keySet()) {
      Object value = this.get(key); // note: this might should be cloned
      copy.put(key, value);
    }
    return copy;
  }
  
  public DefaultInferenceParameters toDefaultInferenceParameters(CycAccess cyc) {
    DefaultInferenceParameters copy = new DefaultInferenceParameters(cyc);
    for (String key : this.keySet()) {
      Object value = this.get(key); // note: this might should be cloned
      copy.put(key, value);
    }
    return copy;
  }
  
  @Override
  public boolean equals(Object rhs) {
    // Can't guarantee that this works on subclasses right now, so use reflection to verify that 
    //     we're not looking at subclasses.
    //
    // TODO:  Generalize this to work on subclasses as well, or override #equals() in those classes
    //        in some appropriate fashion.
    return this.getClass().equals(SpecifiedInferenceParameters.class)
            && rhs.getClass().equals(SpecifiedInferenceParameters.class)
            && equalsByValue((InferenceParameters) rhs);
  }
  
  @Override
  public String toString() {
    final int maxLen = 10;
    StringBuilder builder = new StringBuilder();
    builder.append("SpecifiedInferenceParameters [");
    builder.append("map=").append(toString(entrySet(), maxLen));
    builder.append("]");
    return builder.toString();
  }
  
  /* ====  Private methods  ===================================================================== */

  private String toString(Collection<?> collection, int maxLen) {
    StringBuilder builder = new StringBuilder();
    builder.append("[");
    int i = 0;
    for (Iterator<?> iterator = collection.iterator(); iterator.hasNext() && i < maxLen; i++) {
      if (i > 0) {
        builder.append(", ");
      }
      builder.append(iterator.next());
    }
    builder.append("]");
    return builder.toString();
  }
  
}
