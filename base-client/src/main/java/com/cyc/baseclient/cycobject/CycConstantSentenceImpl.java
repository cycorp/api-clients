package com.cyc.baseclient.cycobject;

/*
 * #%L
 * File: CycConstantSentenceImpl.java
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

//// External Imports

import com.cyc.base.cycobject.CycSentence;


/** 
 * CycConstantSentenceImpl is designed to be an object that represents Sentences that
 are single terms (the only known instance of this is #$False.)
 *
 * @author daves, March 3, 2011, 10:05:43 AM
 * @version $Id: CycConstantSentenceImpl.java 176591 2018-01-09 17:27:27Z nwinant $
 *
 */
public class CycConstantSentenceImpl extends CycConstantImpl implements CycSentence {

  /**
   * Create and return a new CycConstantSentence from constant
   * @param constant
   */
  public CycConstantSentenceImpl(CycConstantImpl constant) {
    super(constant.getName(), constant.getGuid());
    // TODO: should this throw an exception if it's not #$False?
  }


  @Override
  public boolean isConditionalSentence() {
    return false;
  }


  @Override
  public boolean isNegated() {
    return false;
  }

  @Override
  public boolean isConjunction() {
    return false;
  }
 
  @Override
  public boolean isLogicalConnectorSentence() {
    return false;
  }

  @Override
  public boolean isExistential() {
    return false;
  }

  @Override
  public boolean isUniversal() {
    return false;
  }

}
