package com.cyc.query.client.services;

/*
 * #%L
 * File: QueryApiServiceImpl.java
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

import com.cyc.CoreServicesLoader;
import com.cyc.query.QueryAnswer;
import com.cyc.query.QueryAnswerExplanation;
import com.cyc.query.QueryAnswerExplanationGenerator;
import com.cyc.query.QueryAnswerExplanationSpecification;
import com.cyc.query.client.QueryServiceImpl;
import com.cyc.query.spi.ProofViewService;
import com.cyc.query.spi.QueryAnswerExplanationService;
import com.cyc.query.spi.QueryApiService;
import com.cyc.query.spi.QueryService;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author nwinant
 */
public class QueryApiServiceImpl implements QueryApiService {

  //====|    Static methods    |==================================================================//
    
  private final QueryService querySvc;
  private final ProofViewService proofViewSvc;

  private List<QueryAnswerExplanationService> queryExplanationServices = null;

  //====|    Fields    |==========================================================================//
  
  private static final Logger LOG = LoggerFactory.getLogger(QueryApiServiceImpl.class);
  
  //====|    Construction    |====================================================================//
  
  public QueryApiServiceImpl() {
    this.querySvc = new QueryServiceImpl();
    this.proofViewSvc = new ProofViewServiceImpl();
  }
  
  //====|    Public methods    |==================================================================//
  
  @Override
  public QueryService getQueryService() {
    return this.querySvc;
  }

  @Override  public ProofViewService getProofViewService() {
    return this.proofViewSvc;
  }
  
  @Override
  public synchronized List<QueryAnswerExplanationService> getQueryExplanationFactoryServices(
          CoreServicesLoader loader) {
    if (queryExplanationServices == null) {
      queryExplanationServices
              = loader.loadServiceProviders(QueryAnswerExplanationService.class, true);
      if (queryExplanationServices == null) {
        throw new RuntimeException(
                "Could not find any service providers for "
                        + QueryAnswerExplanationService.class.getCanonicalName());
      }
    }
    return queryExplanationServices;
  }
  
  @Override
  public <T extends QueryAnswerExplanation> QueryAnswerExplanationService<T> findExplanationService(
          CoreServicesLoader loader, QueryAnswer answer, QueryAnswerExplanationSpecification<T> spec) {
    for (QueryAnswerExplanationService<T> svc : 
            findExplanationServicesByExplanationType(loader, spec.forExplanationType())) {
      if (svc.isSuitableForSpecification(answer, spec)) {
        return svc;
      }
    }
    throw createUnsupportedExplanationSpecException(spec);
  }
  
  
  private <T extends QueryAnswerExplanation>
          List<QueryAnswerExplanationService<T>> findExplanationServicesByExplanationType(
                  CoreServicesLoader loader,
                  Class<T> explanationClazz) {
    final List<QueryAnswerExplanationService<T>> results = new ArrayList();
    for (QueryAnswerExplanationService service : getQueryExplanationFactoryServices(loader)) {
      if (explanationClazz.equals(service.forExplanationType())) {
        results.add(service);
      }
    }
    return results;
  }
  
  private RuntimeException createUnsupportedExplanationSpecException(
          QueryAnswerExplanationSpecification spec) {
    if (spec == null) {
      return new NullPointerException(
              QueryAnswerExplanationSpecification.class.getCanonicalName() + " is null");
    }
    if (spec.forExplanationType() == null) {
      return new NullPointerException(
              spec.getClass().getCanonicalName() + "#forExplanationType() returned null");
    }
    return new UnsupportedOperationException(
            "Could not find a "
                    + QueryAnswerExplanationGenerator.class.getSimpleName() + " for " + spec);
  }
  
  /*
  public ProofViewService getProofViewFactoryService() {
    if (PROOF_VIEW_FACTORY_SERVICE == null) {
      throw new RuntimeException("Could not find a service provider for " 
              + ProofViewService.class.getCanonicalName());
    }
    return PROOF_VIEW_FACTORY_SERVICE;
  }
  
  private ProofViewService findProofViewService(boolean allowMissingServices) {
    final List<ProofViewService> pvServices = new ArrayList();
    if ((QUERY_EXPLANATION_FACTORY_SERVICES != null) || !allowMissingServices) {
      for (QueryAnswerExplanationService service : getQueryExplanationFactoryServices()) {
        if (service instanceof ProofViewService) {
          pvServices.add((ProofViewService) service);
        }
      }
    }
    if (pvServices.size() != 1) {
      final String errMsg = "Expected exactly one provider for "
              + QueryAnswerExplanationService.class.getCanonicalName()
              + "<" + ProofView.class.getSimpleName() + ">"
              + " but found " + pvServices.size() + ": " + pvServices;
      if (pvServices.isEmpty() && allowMissingServices) {
        LOGGER.warn(errMsg);
      } else {
        throw new RuntimeException(errMsg);
      }
    }
    if (pvServices.isEmpty()) {
      return null;
    }
    return pvServices.get(0);
  }
  */
  
}
