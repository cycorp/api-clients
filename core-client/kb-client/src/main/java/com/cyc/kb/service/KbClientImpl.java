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
package com.cyc.kb.service;

import com.cyc.kb.spi.KbApiService;

/*
 * #%L
 * File: KbClientImpl.java
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

/**
 *
 * @author nwinant
 */
public class KbClientImpl implements KbApiService {
  
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
  //private final KbObjectServiceImpl               kbObjectService;
  private final KbPredicateServiceImpl            predicateService;
  private final RelationServiceImpl               relationService;
  private final RuleServiceImpl                   ruleService;
  private final SecondOrderCollectionServiceImpl  secondOrderCollectionService;
  private final SentenceServiceImpl               sentenceService;
  private final SymbolServiceImpl                 symbolService;
  private final KbTermServiceImpl                 termService;
  private final VariableServiceImpl               variableService;
  
  
  // Construction
  
  public KbClientImpl() {
    this.assertionService = new AssertionServiceImpl();
    this.binaryPredicateService = new BinaryPredicateServiceImpl();
    this.collectionService = new KbCollectionServiceImpl();
    this.contextService = new ContextServiceImpl();
    this.kbService = new KbServiceImpl(this);
    this.factService = new FactServiceImpl();
    this.firstOrderCollectionService = new FirstOrderCollectionServiceImpl();
    this.functionService = new KbFunctionServiceImpl();
    this.individualService = new KbIndividualServiceImpl();
    //this.kbObjectService = new KbObjectServiceImpl();
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
  public AssertionServiceImpl assertion() {
    return assertionService;
  }
  
  @Override
  public BinaryPredicateServiceImpl binaryPredicate() {
    return binaryPredicateService;
  }
  
  @Override
  public KbCollectionServiceImpl collection() {
    return collectionService;
  }
  
  @Override
  public ContextServiceImpl context() {
    return contextService;
  }
  
  @Override
  public KbServiceImpl kb() {
    return kbService;
  }
  
  @Override
  public FactServiceImpl fact() {
    return factService;
  }
  
  @Override
  public FirstOrderCollectionServiceImpl firstOrderCollection() {
    return firstOrderCollectionService;
  }
  
  @Override
  public KbFunctionServiceImpl function() {
    return functionService;
  }
  
  @Override
  public KbIndividualServiceImpl individual() {
    return individualService;
  }
  /*
  @Override
  public KbObjectServiceImpl kbObject() {
    return kbObjectService;
  }
  */
  @Override
  public KbPredicateServiceImpl predicate() {
    return predicateService;
  }
  
  @Override
  public RelationServiceImpl relation() {
    return relationService;
  }
  
  @Override
  public RuleServiceImpl rule() {
    return ruleService;
  }
  
  @Override
  public SecondOrderCollectionServiceImpl secondOrderCollection() {
    return secondOrderCollectionService;
  }
  
  @Override
  public SentenceServiceImpl sentence() {
    return sentenceService;
  }

  @Override
  public SymbolServiceImpl symbol() {
    return symbolService;
  }
  
  @Override
  public KbTermServiceImpl term() {
    return termService;
  }
  
  @Override
  public VariableServiceImpl variable() {
    return variableService;
  }
  
}
