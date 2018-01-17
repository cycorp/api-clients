/*
 * Copyright 2016 Cycorp, Inc..
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
 * File: InferenceParametersBean.java
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

import com.cyc.base.CycAccessManager;
import com.cyc.baseclient.inference.params.DefaultInferenceParameters;
import com.cyc.baseclient.inference.params.SpecifiedInferenceParameters;
import com.cyc.query.parameters.InferenceParameterGetter;
import com.cyc.query.parameters.InferenceParameters;
import com.cyc.session.exception.SessionCommunicationException;
import com.cyc.session.exception.SessionConfigurationException;
import com.cyc.session.exception.SessionInitializationException;

/**
 * @todo Should this be made accessible at the API level via a factory or something? - nwinant, 2016-12-11
 * 
 * @author nwinant
 */
public class InferenceParametersBean extends SpecifiedInferenceParameters
        implements InferenceParameters {
  
  public DefaultInferenceParameters toDefaultInferenceParameters() 
          throws SessionConfigurationException, SessionCommunicationException, 
          SessionInitializationException {
    final DefaultInferenceParameters copy = new DefaultInferenceParameters(
            CycAccessManager.getCurrentAccess());
    for (String key : this.keySet()) {
      copy.put(key, this.get(key)); // note: this might should be cloned
    }
    return copy;
  }
  
  public static InferenceParametersBean fromInferenceParameters(
          final InferenceParameterGetter parameters) {
    if (parameters == null) {
      throw new NullPointerException(
              InferenceParameters.class.getSimpleName() + " argument is null");
    }
    if (parameters instanceof InferenceParametersBean) {
      return (InferenceParametersBean) parameters;
    }
    final InferenceParametersBean copy = new InferenceParametersBean();
    for (String key : parameters.keySet()) {
      copy.put(key, parameters.get(key)); // note: this might should be cloned
    }
    return copy;
  }
  
  @Override
  public boolean equals(Object other) {
    return (other != null)
            && this.getClass().equals(other.getClass())
            && equalsByValue((SpecifiedInferenceParameters) other);
  }
  
}
