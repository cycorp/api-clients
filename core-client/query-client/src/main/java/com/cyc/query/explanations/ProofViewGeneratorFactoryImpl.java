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
package com.cyc.query.explanations;

/*
 * #%L
 * File: ProofViewGeneratorFactoryImpl.java
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

import com.cyc.base.exception.CycConnectionException;
import com.cyc.query.ProofView;
import com.cyc.query.ProofViewSpecification;
import com.cyc.query.QueryAnswer;
import com.cyc.query.QueryAnswerExplanationSpecification;
import com.cyc.query.exception.QueryRuntimeException;
import com.cyc.query.spi.ProofViewFactoryService;
import com.cyc.session.exception.OpenCycUnsupportedFeatureException;

/**
 *
 * @author nwinant
 */
public class ProofViewGeneratorFactoryImpl implements ProofViewFactoryService {
  
  @Override
  public Class forExplanationType() {
    return ProofView.class;
  }
  
  @Override
  public boolean isSuitableForSpecification(QueryAnswer answer, QueryAnswerExplanationSpecification<ProofView> spec) {
    return spec instanceof ProofViewSpecification;
  }
  
  @Override
  public ProofViewSpecification getSpecification() {
    return new ProofViewSpecificationImpl();
  }
  
  @Override
  public ProofViewGeneratorImpl getExplanationGenerator(QueryAnswer answer, QueryAnswerExplanationSpecification<ProofView> spec) {
    try {
      return (ProofViewGeneratorImpl) new ProofViewGeneratorImpl(answer, (ProofViewSpecification) spec);
    } catch (CycConnectionException | OpenCycUnsupportedFeatureException ex) {
      throw new QueryRuntimeException(ex);
    }
  }
  
  @Override
  public ProofViewImpl getExplanation(QueryAnswer answer, QueryAnswerExplanationSpecification<ProofView> spec) {
    final ProofViewGeneratorImpl generator = getExplanationGenerator(answer, spec);
    generator.generate();
    return (ProofViewImpl) generator.getExplanation();
  }

  
}
