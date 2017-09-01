package com.cyc.kb.client;

/*
 * #%L
 * File: QuantifierImpl.java
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

import com.cyc.base.cycobject.DenotationalTerm;
import com.cyc.base.cycobject.Guid;
import com.cyc.baseclient.cycobject.CycConstantImpl;
import com.cyc.kb.KbObject;
import com.cyc.kb.Quantifier;
import com.cyc.kb.exception.CreateException;
import com.cyc.kb.exception.KbException;
import com.cyc.kb.exception.KbRuntimeException;
import com.cyc.kb.exception.KbTypeException;


/**
 * THIS IS NOT PART OF 1.0.0.
 * 
 * @param <T> type of CycObject core
 * 
 * @author vijay
 */
public class QuantifierImpl<T extends DenotationalTerm> extends ScopingRelationImpl<T> implements Quantifier {

    private static final DenotationalTerm TYPE_CORE =
          new CycConstantImpl("Quantifier", new Guid("bd58c271-9c29-11b1-9dad-c379636f7270"));

  static DenotationalTerm getClassTypeCore() {
    return TYPE_CORE;
  }
  
  /**
   * default constructor, calls the default super constructor
   *
   * @throws Exception
   */
  @SuppressWarnings("unused")
  private QuantifierImpl() {
    super();
  }

  /**
   * Return a new
   * <code>Quantifier</code> based on the existing predicate
   * <code>cycBinPred</code>. The KB term underlying
   * <code>cycBinPred</code> must already be an instance of #$Quantifier.
   *
   * @param cycQuantifier	the CycObject wrapped by Quantifier. The constructor verifies that the
   * CycObject is a #$Quantifier
   * @throws KbTypeException if cycBinPred is not a #$Quantifier
   */
  protected QuantifierImpl(DenotationalTerm cycQuantifier) throws KbTypeException {
    super(cycQuantifier);
  }

  /**
   * finds or creates a Quantifier represented by binPredStr in the underlying KB
   * <p>
   *
   * @param quantifierStr	the string representing a Quantifier in the KB
   * @throws CreateException if the Quantifier represented by predStr is not found and could not be
   * created
   * @throws KbTypeException 
   */
  public QuantifierImpl(String quantifierStr) throws KbTypeException, CreateException {
    super(quantifierStr);
  }

    
  protected QuantifierImpl (String quantifierStr, LookupType lookup) throws KbTypeException, CreateException {
    super(quantifierStr, lookup);
  }
  /**
   * Get the
   * <code>Relation</code> with the name
   * <code>nameOrId</code>. Throws exceptions if there is no KB term by that name, or if it is not
   * already an instance of #$Relation.
   *
   * @param nameOrId
   * @return new Quantifier
   * @throws CreateException 
   * @throws KbTypeException
   */
  public static QuantifierImpl get(String nameOrId) throws KbTypeException, CreateException {
    return KbObjectImplFactory.get(nameOrId, QuantifierImpl.class);
  }

  /**
   * Return the KBCollection as a KBObject of the Cyc term that 
   * underlies this class. 
   * 
   * @return KBCollectionImpl.get("#$Quantifier");
   */
  @Override
  public KbObject getType() {
    return getClassType();
  }
  
  /**
   * Return the KBCollection as a KBObject of the Cyc term that 
   * underlies this class. 
   * 
   * @return KBCollectionImpl.get("#$Quantifier");
   */
  public static KbObject getClassType() {
    try {
      return KbCollectionImpl.get(getClassTypeString());
    } catch (KbException kae) {
      throw new KbRuntimeException(kae.getMessage(), kae);
    }
  }
  
  @Override
  String getTypeString() {
    return getClassTypeString();
  }
  
  static String getClassTypeString() {
    return "#$Quantifier";
  }
}
