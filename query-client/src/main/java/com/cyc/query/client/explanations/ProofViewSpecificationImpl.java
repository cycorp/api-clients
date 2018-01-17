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
package com.cyc.query.client.explanations;

/*
 * #%L
 * File: ProofViewSpecificationImpl.java
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

import com.cyc.kb.Context;
import com.cyc.query.ProofView;
import com.cyc.query.ProofViewSpecification;

/**
 *
 * @author daves
 */
public class ProofViewSpecificationImpl implements ProofViewSpecification {

  private Boolean includeDetails = null;
  private Boolean includeLinear = null;
  private Boolean includeSummary = null;
  private Context domainContext = null;
  private Context languageContext = null;
  private Boolean includeAssertionBookkeeping = null;
  private Boolean includeAssertionCyclists = null;

  @Override
  public Class<ProofView> forExplanationType() {
    return ProofView.class;
  }


  @Override
  public ProofViewSpecification setIncludeDetails(boolean includeDetails) {
    this.includeDetails = includeDetails;
    return this;
  }

  @Override
  public Boolean isIncludeDetails() {
    return includeDetails;
  }

  @Override
  public ProofViewSpecification setIncludeLinear(boolean includeLinear) {
    this.includeLinear = includeLinear;
    return this;
  }

  @Override
  public Boolean isIncludeLinear() {
    return includeLinear;
  }

  @Override
  public ProofViewSpecification setIncludeSummary(boolean includeSummary) {
    this.includeSummary = includeSummary;
    return this;
  }

  @Override
  public Boolean isIncludeSummary() {
    return includeSummary;
  }

  @Override
  public ProofViewSpecification setDomainContext(Context domainContext) {
    this.domainContext = domainContext;
    return this;
  }

  @Override
  public Context getDomainContext() {
    return domainContext;
  }

  @Override
  public ProofViewSpecification setLanguageContext(Context languageContext) {
    this.languageContext = languageContext;
    return this;
  }

  @Override
  public Context getLanguageContext() {
    return languageContext;
  }

  @Override
  public ProofViewSpecification setIncludeAssertionBookkeeping(boolean includeAssertionBookkeeping) {
    this.includeAssertionBookkeeping = includeAssertionBookkeeping;
    return this;
  }

  @Override
  public Boolean isIncludeAssertionBookkeeping() {
    return includeAssertionBookkeeping;
  }

  @Override
  public ProofViewSpecification setIncludeAssertionCyclists(boolean includeAssertionCyclists) {
    this.includeAssertionCyclists = includeAssertionCyclists;
    return this;
  }

  @Override
  public Boolean isIncludeAssertionCyclists() {
    return includeAssertionCyclists;
  }

}
