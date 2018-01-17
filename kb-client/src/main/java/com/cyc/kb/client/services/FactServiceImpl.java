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

/*
 * #%L
 * File: FactServiceImpl.java
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

import com.cyc.kb.Assertion.Direction;
import com.cyc.kb.Assertion.Strength;
import com.cyc.kb.Context;
import com.cyc.kb.Fact;
import com.cyc.kb.Sentence;
import com.cyc.kb.client.FactImpl;
import com.cyc.kb.client.config.KbConfiguration;
import com.cyc.kb.exception.CreateException;
import com.cyc.kb.exception.KbTypeException;
import com.cyc.kb.spi.FactService;

/**
 *
 * @author nwinant
 */
public class FactServiceImpl implements FactService {

  @Override
  public Fact get(String hlid) throws KbTypeException, CreateException {
    return FactImpl.get(hlid);
  }
  
  @Override
  public Fact get(String formulaStr, String ctxStr) throws KbTypeException, CreateException {
    return FactImpl.get(formulaStr, ctxStr);
  }
  
  @Override
  public Fact get(Sentence formula, Context ctx) throws KbTypeException, CreateException {
    return FactImpl.get(formula, ctx);
  }
  
  @Override
  public Fact findOrCreate(String formulaStr, String ctxStr, Strength s, Direction d) throws CreateException, KbTypeException {
    return FactImpl.findOrCreate(formulaStr, ctxStr, s, d);
  }
  
  @Override
  public Fact findOrCreate(Sentence formula, Context ctx, Strength s, Direction d) throws CreateException, KbTypeException {
    return FactImpl.findOrCreate(formula, ctx, s, d);
  }
  
  @Override
  public Fact findOrCreate(String formulaStr) throws CreateException, KbTypeException {
    return FactImpl.findOrCreate(formulaStr, KbConfiguration.getDefaultContext().forAssertion().toString());
  }

  @Override
  public Fact findOrCreate(String formulaStr, String ctxStr) throws CreateException, KbTypeException {
    return FactImpl.findOrCreate(formulaStr, ctxStr, Strength.AUTO, Direction.AUTO);
  }

  @Override
  public Fact findOrCreate(Sentence formula) throws KbTypeException, CreateException {
    return FactImpl.findOrCreate(formula, KbConfiguration.getDefaultContext().forAssertion());
  }

  @Override
  public Fact findOrCreate(Sentence formula, Context ctx) throws KbTypeException, CreateException {
    return FactImpl.findOrCreate(formula, ctx, Strength.AUTO, Direction.AUTO);
  }
  
}
