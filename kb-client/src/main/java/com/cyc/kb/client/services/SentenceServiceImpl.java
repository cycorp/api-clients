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
 * File: SentenceServiceImpl.java
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

import com.cyc.kb.Relation;
import com.cyc.kb.Sentence;
import com.cyc.kb.client.SentenceImpl;
import com.cyc.kb.exception.CreateException;
import com.cyc.kb.exception.KbTypeException;
import com.cyc.kb.spi.SentenceService;
import java.util.Collection;

/**
 *
 * @author nwinant
 */
public class SentenceServiceImpl implements SentenceService {

  @Override
  public Sentence get(String sentStr) 
          throws KbTypeException, CreateException {
    return new SentenceImpl(sentStr);
  }

  @Override
  public Sentence get(Relation pred, Object... args) 
          throws KbTypeException, CreateException {
    return new SentenceImpl(pred, args);
  }

  @Override
  public Sentence get(Object... args) 
          throws KbTypeException, CreateException {
    return new SentenceImpl(args);
  }

  @Override
  public Sentence and(Sentence... sentences) 
          throws KbTypeException, CreateException {
    return SentenceImpl.and(sentences);
  }

  @Override
  public Sentence and(Iterable<Sentence> sentences) 
          throws KbTypeException, CreateException {
    return SentenceImpl.and(sentences);
  }

  @Override
  public Sentence implies(Collection<Sentence> posLiterals, Sentence negLiteral) 
          throws KbTypeException, CreateException {
    return SentenceImpl.implies(posLiterals, negLiteral);
  }

  @Override
  public Sentence implies(Sentence posLiteral, Sentence negLiteral) 
          throws KbTypeException, CreateException {
    return SentenceImpl.implies(posLiteral, negLiteral);
  }

  @Override
  public Sentence or(Sentence... sentences)
          throws KbTypeException, CreateException {
    return SentenceImpl.or(sentences);
  }

  @Override
  public Sentence or(Iterable<Sentence> sentences) 
          throws KbTypeException, CreateException {
    return SentenceImpl.or(sentences);
  }
  
}
