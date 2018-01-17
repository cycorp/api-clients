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

import com.cyc.kb.spi.KbApiService;

/*
 * #%L
 * File: KbApiServiceImpl.java
 * Project: KB Client
 * %%
 * Copyright (C) 2013 - 2018 Cycorp, Inc
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

/**
 *
 * @author nwinant
 */
public class KbApiServiceImpl implements KbApiService {
  
  // Fields
  
  private final AssertionServiceImpl              assertionService;
  private final BinaryPredicateServiceImpl        binaryPredicateService;
  private final KbCollectionServiceImpl           collectionService;
  private final ContextServiceImpl                contextService;
  private final KbServiceImpl                     kbService;
  private final FactServiceImpl                   factService;
  private final FirstOrderCollectionServiceImpl   firstOrderCollectionService;
  private final KbFunctionServiceImpl             functionService;
  private final KbIndividualServiceImpl           individualService;
  private final KbPredicateServiceImpl            predicateService;
  private final RelationServiceImpl               relationService;
  private final RuleServiceImpl                   ruleService;
  private final SecondOrderCollectionServiceImpl  secondOrderCollectionService;
  private final SentenceServiceImpl               sentenceService;
  private final SymbolServiceImpl                 symbolService;
  private final KbTermServiceImpl                 termService;
  private final VariableServiceImpl               variableService;
  
  
  // Construction
  
  public KbApiServiceImpl() {
    this.assertionService = new AssertionServiceImpl();
    this.binaryPredicateService = new BinaryPredicateServiceImpl();
    this.collectionService = new KbCollectionServiceImpl();
    this.contextService = new ContextServiceImpl();
    this.kbService = new KbServiceImpl(this);
    this.factService = new FactServiceImpl();
    this.firstOrderCollectionService = new FirstOrderCollectionServiceImpl();
    this.functionService = new KbFunctionServiceImpl();
    this.individualService = new KbIndividualServiceImpl();
    this.predicateService = new KbPredicateServiceImpl();
    this.relationService = new RelationServiceImpl();
    this.ruleService = new RuleServiceImpl();
    this.secondOrderCollectionService = new SecondOrderCollectionServiceImpl();
    this.sentenceService = new SentenceServiceImpl();
    this.symbolService = new SymbolServiceImpl();
    this.termService = new KbTermServiceImpl();
    this.variableService = new VariableServiceImpl();
  }
  
  
  // Public
  
  @Override
  public AssertionServiceImpl getAssertionService() {
    return assertionService;
  }
  
  @Override
  public BinaryPredicateServiceImpl getBinaryPredicateService() {
    return binaryPredicateService;
  }
  
  @Override
  public KbCollectionServiceImpl getKbCollectionService() {
    return collectionService;
  }
  
  @Override
  public ContextServiceImpl getContextService() {
    return contextService;
  }
  
  @Override
  public KbServiceImpl getKbService() {
    return kbService;
  }
  
  @Override
  public FactServiceImpl getFactService() {
    return factService;
  }
  
  @Override
  public FirstOrderCollectionServiceImpl getFirstOrderCollectionService() {
    return firstOrderCollectionService;
  }
  
  @Override
  public KbFunctionServiceImpl getKbFunctionService() {
    return functionService;
  }
  
  @Override
  public KbIndividualServiceImpl getKbIndividualService() {
    return individualService;
  }
  
  @Override
  public KbPredicateServiceImpl getKbPredicateService() {
    return predicateService;
  }
  
  @Override
  public RelationServiceImpl getRelationService() {
    return relationService;
  }
  
  @Override
  public RuleServiceImpl getRuleService() {
    return ruleService;
  }
  
  @Override
  public SecondOrderCollectionServiceImpl getSecondOrderCollectionService() {
    return secondOrderCollectionService;
  }
  
  @Override
  public SentenceServiceImpl getSentenceService() {
    return sentenceService;
  }

  @Override
  public SymbolServiceImpl getSymbolService() {
    return symbolService;
  }
  
  @Override
  public KbTermServiceImpl getKbTermService() {
    return termService;
  }
  
  @Override
  public VariableServiceImpl getVariableService() {
    return variableService;
  }
  
}
