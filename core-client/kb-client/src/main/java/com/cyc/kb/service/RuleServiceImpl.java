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

/*
 * #%L
 * File: RuleServiceImpl.java
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

import com.cyc.kb.Assertion.Direction;
import com.cyc.kb.Assertion.Strength;
import com.cyc.kb.Context;
import com.cyc.kb.Rule;
import com.cyc.kb.Sentence;
import com.cyc.kb.client.RuleImpl;
import com.cyc.kb.client.config.KbConfiguration;
import com.cyc.kb.exception.CreateException;
import com.cyc.kb.exception.KbObjectNotFoundException;
import com.cyc.kb.exception.KbTypeException;
import com.cyc.kb.spi.RuleService;

/**
 *
 * @author nwinant
 */
public class RuleServiceImpl implements RuleService {

  @Override
  public Rule get(String hlid) throws KbTypeException, CreateException {
    return RuleImpl.get(hlid);
  }
  
  @Override
  public Rule get(String formulaStr, String ctxStr) throws KbTypeException, CreateException {
    return RuleImpl.get(formulaStr, ctxStr);
  }
  
  @Override
  public Rule get(Sentence formula, Context ctx) throws KbTypeException, CreateException {
    return RuleImpl.get(formula, ctx);
  }
  
  @Override
  public Rule get(Sentence antecedent, Sentence consequent, Context ctx) 
          throws KbTypeException, CreateException, KbObjectNotFoundException {
    return RuleImpl.get(antecedent, consequent, ctx);
  }
  
  @Override
  public Rule findOrCreate(String formulaStr, String ctxStr, Strength s, Direction d) 
          throws CreateException, KbTypeException {
    return RuleImpl.findOrCreate(formulaStr, ctxStr, s, d);
  }
  
  @Override
  public Rule findOrCreate(Sentence formula, Context ctx, Strength s, Direction d)
          throws CreateException, KbTypeException {
    return RuleImpl.findOrCreate(formula, ctx, s, d);
  }
  
  @Override
  public Rule findOrCreate(String formulaStr) 
          throws CreateException, KbTypeException {
    return RuleImpl.findOrCreate(
            formulaStr, KbConfiguration.getDefaultContext().forAssertion().toString());
  }

  @Override
  public Rule findOrCreate(String formulaStr, String ctxStr) 
          throws CreateException, KbTypeException {
    return RuleImpl.findOrCreate(formulaStr, ctxStr, Strength.AUTO, Direction.AUTO);
  }

  @Override
  public Rule findOrCreate(Sentence formula) 
          throws KbTypeException, CreateException {
    return RuleImpl.findOrCreate(formula, KbConfiguration.getDefaultContext().forAssertion());
  }

  @Override
  public Rule findOrCreate(Sentence formula, Context ctx) 
          throws KbTypeException, CreateException {
    return RuleImpl.findOrCreate(formula, ctx, Strength.AUTO, Direction.AUTO);
  }
  
  @Override
  public Rule findOrCreate(Sentence antecedent, Sentence consequent, Context ctx) 
          throws KbTypeException, CreateException {
    return RuleImpl.findOrCreate(antecedent, consequent, ctx);
  }

}
