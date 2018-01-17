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

import com.cyc.base.cycobject.CycConstant;
import com.cyc.base.cycobject.CycList;
import com.cyc.base.cycobject.CycSymbol;
import com.cyc.base.cycobject.ElMt;
import com.cyc.base.cycobject.Fort;
import com.cyc.baseclient.cycobject.CycArrayList;
import com.cyc.baseclient.inference.params.SpecifiedInferenceParameters;
import com.cyc.baseclient.kbtool.TemplateOeToolImpl.TemplateOeSpecificationParameter;
import com.cyc.baseclient.kbtool.TemplateOeToolImpl.ToeProcessingOverride;
import com.cyc.kb.Context;
import com.cyc.kb.KbCollection;
import com.cyc.kb.KbIndividual;
import com.cyc.kb.client.ContextImpl;
import com.cyc.kb.exception.CreateException;
import com.cyc.kb.exception.KbTypeException;
import com.cyc.query.client.templates.OeTemplateResult.OeTemplateResultField;
import com.cyc.query.exception.QueryRuntimeException;
import com.cyc.query.parameters.InferenceParameters;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.cyc.baseclient.kbtool.TemplateOeToolImpl.ToeProcessingOverride.MAX_NUM_ERRORING_SEQUENTIAL_TOE_ASSERTS;
import static com.cyc.baseclient.kbtool.TemplateOeToolImpl.ToeProcessingOverride.MAX_NUM_ERRORS;
import static com.cyc.baseclient.kbtool.TemplateOeToolImpl.ToeProcessingOverride.SKIP_ALL_TOE_ASSERT_PROCESSING;
import static com.cyc.baseclient.kbtool.TemplateOeToolImpl.ToeProcessingOverride.SKIP_TOE_ASSERT_SENTENCE;
import static java.util.stream.Collectors.toList;

/*
 * #%L
 * File: OeTemplateProcessorConfig.java
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

public class OeTemplateProcessorConfig {
    
  //====|    Construction    |====================================================================//
  
  public static OeTemplateProcessorConfig create() {
    return new OeTemplateProcessorConfig();
  }
  
  //====|    InferenceParameter Overrides    |====================================================//
  
  private InferenceParameters inferenceParameters = new SpecifiedInferenceParameters();

  public InferenceParameters getInferenceParameterOverrides() {
    return this.inferenceParameters;
  }
  
  public void setInferenceParameterOverrides(InferenceParameters value) {
    this.inferenceParameters = value;
  }
  
  //====|    Focal Mt & Predicates    |===========================================================//
  
  private static final KbCollection PREDICATE;
  private Context focalContext = null;
  private KbCollection focalPredicateCollection = null;
  
  static {
    try {
      PREDICATE = KbCollection.get("Predicate");
    } catch (KbTypeException | CreateException ex) {
      throw QueryRuntimeException.fromThrowable(ex);
    }
  }
  
  ElMt getFocalMt() {
    return getFocalContext() != null ? ContextImpl.asELMt(getFocalContext()) : null;
  }
  
  public Context getFocalContext() {
    return this.focalContext;
  }
  
  public OeTemplateProcessorConfig setFocalContext(Context value) {
    this.focalContext = value;
    return this;
  }
  
  Fort getFocalPredicateFort() {
    return getFocalPredicateCollection() != null
           ? (Fort) getFocalPredicateCollection().getCore() : null;
  }
  
  public KbCollection getFocalPredicateCollection() {
    return this.focalPredicateCollection;
  }
  
  public OeTemplateProcessorConfig setFocalPredicateCollection(KbCollection value) {
    if (value != null && !PREDICATE.equals(value) && !PREDICATE.isGeneralizationOf(value)) {
      throw new QueryRuntimeException(
              "Focal predicate collection must be #$Predicate or a spec thereof.");
    }
    this.focalPredicateCollection = value;
    return this;
  }
  
  //====|    Result fields    |===================================================================//
  
  private List<OeTemplateResultField> resultFields = OeTemplateResult.ALL_FIELDS;

  public List<OeTemplateResultField> getResultFields() {
    return this.resultFields;
  }
  
  public OeTemplateProcessorConfig setResultFields(List<OeTemplateResultField> value) {
    this.resultFields = value;
    return this;
  }
  
  List<CycSymbol> getResultFieldsAsSymbols() {
    return getResultFields().stream()
            .map((field) -> {
              return (CycSymbol) field.toSymbol().getCore();
            })
            .collect(toList());
  }
  
  //====|    TOE processing overrides    |========================================================//
  
  // Fields
  
  private static final List<CycSymbol>   VALID_KEYS
          = Arrays.stream(ToeProcessingOverride.values())
                  .map(ToeProcessingOverride::toSymbol)
                  .collect(toList());
          
  private static final List<CycConstant> VALID_TERMS
          = Arrays.stream(TemplateOeSpecificationParameter.values())
                  .map(TemplateOeSpecificationParameter::toTerm)
                  .collect(toList());
  
  private final CycList<Object> alist = new CycArrayList<>();
  
  // Getters & setters
  
  public OeTemplateProcessorConfig setMaxNumErrors(Integer value) {
    setPair(MAX_NUM_ERRORS, value);
    return this;
  }
  
  public OeTemplateProcessorConfig setMaxNumErroringSequentialAssertions(Integer value) {
    setPair(MAX_NUM_ERRORING_SEQUENTIAL_TOE_ASSERTS, value);
    return this;
  }
  
  public OeTemplateProcessorConfig setSkipAllToeAssertProcessing(Boolean value) {
    setPair(SKIP_ALL_TOE_ASSERT_PROCESSING, value);
    return this;
  }
  
  public OeTemplateProcessorConfig setSkipToeAssertSentence(Boolean value) {
    setPair(SKIP_TOE_ASSERT_SENTENCE, value);
    return this;
  }
  
  public OeTemplateProcessorConfig addOeSpecificationParameter(KbIndividual key, Object value) {
    if (!VALID_TERMS.contains((CycConstant) key.getCore())) {
      throw new QueryRuntimeException("Invalid #$TemplateOESpecificationParameter: " + key);
    }
    throw new UnsupportedOperationException();  // TODO: implement! - nwinant, 2017-08-09
    /*
    this.alist.
    setPair(TEMPLATE_OE_SPECIFICATION_PARAMETERS, value);
    return this;
    */
  }
  
  // Private
  
  private Boolean getBool(ToeProcessingOverride key) {
    throw new UnsupportedOperationException();  // TODO: implement! - nwinant, 2017-08-09
  }
  
  private Integer getInt(ToeProcessingOverride key) {
    throw new UnsupportedOperationException();  // TODO: implement! - nwinant, 2017-08-09
  }
  
  private CycList getList(ToeProcessingOverride key) {
    throw new UnsupportedOperationException();  // TODO: implement! - nwinant, 2017-08-09
  }
  
  private boolean setPair(ToeProcessingOverride key, Object value) {
    CycSymbol symbol = key.toSymbol();
    alist.removePairs(symbol);
    return (value != null) ? alist.addPair(symbol, value) : true;
  }
  
  CycList getProcessingOverrides() {
    final CycList results = new CycArrayList();
    final Map map = alist.toMap();
    map.keySet().stream()
            .filter((key) -> {
              return VALID_KEYS.contains((CycSymbol) key);
            })
            .forEach((key) -> {
              results.addPair(key, map.get(key));
            });
    return this.alist;
  }
  
  //====|    Results polling    |=================================================================//
  
  // Fields
  
  private int resultsBatchSize = 100;
  private int timeoutSec = 5;
  private int pollingIntervalMillis = 500;
  private int pollingMaxDurationSecs = 15;
  
  // Getters & setters
  
  public int getResultsBatchSize() {
    return this.resultsBatchSize;
  }

  public OeTemplateProcessorConfig setResultsBatchSize(int value) {
    this.resultsBatchSize = value;
    return this;
  }

  public int getTimeoutSec() {
    return this.timeoutSec;
  }

  public OeTemplateProcessorConfig setTimeoutSec(int value) {
    this.timeoutSec = value;
    return this;
  }

  public int getPollingIntervalMillis() {
    return this.pollingIntervalMillis;
  }
  
  public OeTemplateProcessorConfig setPollingIntervalMillis(int value) {
    this.pollingIntervalMillis = value;
    return this;
  }
  
  public int getPollingMaxDurationSecs() {
    return this.pollingMaxDurationSecs;
  }
  
  public OeTemplateProcessorConfig setPollingMaxDurationSecs(int value) {
    this.pollingMaxDurationSecs = value;
    return this;
  }
  
  public OeTemplateProcessorConfig setResultFields(OeTemplateResultField... value) {
    return setResultFields(Arrays.asList(value));
  }
  
  //====|    Other methods    |===================================================================//

  @Override
  public String toString() {
    return !alist.isEmpty() ? alist.stringApiValue() : "";
  }
  
}
