package com.cyc.kb.client.quant;

/*
 * #%L
 * File: ForAllQuantifiedRestrictedVariable.java
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

import com.cyc.kb.client.QuantifierImpl;
import com.cyc.kb.exception.KbException;

/**
 * THIS CLASS IS NOT SUPPORTED IN KB API 1.0.
 * @author vijay
 */
class ForAllQuantifiedRestrictedVariable extends QuantifiedRestrictedVariable {

  public ForAllQuantifiedRestrictedVariable(RestrictedVariable ul) throws KbException {
    super(new QuantifierImpl("forAll"), ul);
  }
  
  /**
   * @todo
   */
  
  
  
}
