/*
 * Copyright 2017 Cycorp, Inc.
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
package com.cyc.kb.client;

/*
 * #%L
 * File: PossiblyNonAtomicKbObjectImpl.java
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

import com.cyc.base.cycobject.CycAssertion;
import com.cyc.base.cycobject.CycConstant;
import com.cyc.base.cycobject.CycList;
import com.cyc.base.cycobject.CycObject;
import com.cyc.base.cycobject.Formula;
import com.cyc.base.cycobject.FormulaSentence;
import com.cyc.base.cycobject.Nart;
import com.cyc.base.cycobject.Naut;
import com.cyc.base.cycobject.NonAtomicTerm;
import com.cyc.base.exception.CycApiException;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.baseclient.cycobject.NautImpl;
import com.cyc.kb.Assertion;
import com.cyc.kb.Context;
import com.cyc.kb.DefaultContext;
import com.cyc.kb.Fact;
import com.cyc.kb.KbCollection;
import com.cyc.kb.KbObject;
import com.cyc.kb.KbTerm;
import com.cyc.kb.client.config.KbConfiguration;
import com.cyc.kb.exception.CreateException;
import com.cyc.kb.exception.KbRuntimeException;
import com.cyc.kb.exception.KbServerSideException;
import com.cyc.kb.exception.KbTypeException;
import com.cyc.kb.exception.StaleKbObjectException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.cyc.kb.KbObject.KbObjectWithArity;

/**
 * Provides a shared superclass for Assertion, KbTerm, and Sentence to share common methods. 
 * Any methods in here should support at least 2 of those 3 subclasses.
 * 
 * @author nwinant
 * @param <T> type of CycObject core
 */
class PossiblyNonAtomicKbObjectImpl<T extends CycObject> 
        extends StandardKbObject<T>
        implements KbObjectWithArity {
  
  //====|    Fields    |==========================================================================//
  
  //====|    Construction    |====================================================================//
  
  PossiblyNonAtomicKbObjectImpl() {
    super();
  }
  
  PossiblyNonAtomicKbObjectImpl(CycObject cycObject) throws KbTypeException {
    super(cycObject);
  }

  /*
  protected KbTermImpl(String termStr, List<Object> l) throws KbTypeException, CreateException {
    super(termStr, l);
  }
  */
  
  PossiblyNonAtomicKbObjectImpl(String termStr) throws KbTypeException, CreateException  {
    super(termStr);
  }
  
  PossiblyNonAtomicKbObjectImpl(String termStr, LookupType lookup) 
          throws KbTypeException, CreateException  {
    super(termStr, lookup);
  }
  
  protected PossiblyNonAtomicKbObjectImpl(DefaultContext contexts, KbTerm term) {
    super();
    this.setCore(term);
  }
  
  //====|    Public methods    |==================================================================//
  
  public Collection<String> getComments() {
    return getComments(KbConfiguration.getDefaultContext().forQuery());
  }
  
  public Collection<String> getComments(String ctxStr) {
    return getComments(KbUtils.getKBObjectForArgument(ctxStr, ContextImpl.class));
  }
  
  public Collection<String> getComments(Context ctx) {
    return Constants.getInstance().COMMENT_PRED.getValuesForArgPosition(this, 1, 2, ctx);
  }
  
  public Fact addComment(String comment, String ctx) throws KbTypeException, CreateException {
    return addComment(comment, ContextImpl.get(ctx));
  }
  
  public Fact addComment(String comment, Context ctx) throws KbTypeException, CreateException {
    final List<Object> argList = new ArrayList<>();
    argList.add((Object) this);
    argList.add((Object) comment);
    final SentenceImpl s 
            = new SentenceImpl(Constants.getInstance().COMMENT_PRED, argList.toArray());
    return FactImpl.findOrCreate(s, ctx);
  }
  
  public KbObject addQuotedIsa(KbCollection coll, Context ctx) 
          throws KbTypeException, CreateException {
    Constants.quotedIsa().addFact(ctx, this, coll);
    return this;
  }
  
  @Override
  public String stringApiValue() {
    if (!isValid()) {
      throw new StaleKbObjectException("The reference to " + this + " object is stale. "
              + "Possibly because it was delete using x.delete() method.");
    }
    return getCore().stringApiValue();
  }
  
  public Integer getArity() {
    if (this.getCore() instanceof CycConstant) {
      return 0;
    } else if (this.getCore() instanceof Nart) {
      Nart cn = (Nart) this.getCore();
      return cn.getArity();// .getReferencedConstants().get(getPos);
    } else if (this.getCore() instanceof Naut) {
      Naut cn = (Naut) this.getCore();
      return cn.getArity();// .getReferencedConstants().get(getPos);
    } else if (this.getCore() instanceof CycAssertion) {
      CycAssertion ca = (CycAssertion) this.getCore();
      return ((CycList<Object>) ca.getFormula().get(1)).size();
      //TODO: Careful!! No error checking what so ever!!
    } else if (this.getCore() instanceof FormulaSentence) {
      FormulaSentence cfs = (FormulaSentence) this.getCore();
      return cfs.getArity();
    } else {
      return (Integer) null;
    }
  }
  
  //====|    Internal/overridden methods    |=====================================================//
  
  @Override
  public <O> O getArgument(int getPos) throws KbTypeException, CreateException {
    Object o;
    if (this.getCore() instanceof Nart) {
      Nart cn = (Nart) this.getCore();
      o = cn.getArgument(getPos);// .getReferencedConstants().get(getPos);
    } else if (this.getCore() instanceof Naut) {
      Naut cn = (Naut) this.getCore();
      o = cn.getArgument(getPos);// .getReferencedConstants().get(getPos);
    } else if (this instanceof Assertion) {
      try {
        o = ((CycAssertion) this.getCore()).getArg(getPos, getAccess());
      } catch (CycApiException ex) {
        throw KbServerSideException.fromThrowable(ex);
      } catch (CycConnectionException ex) {
        throw KbRuntimeException.fromThrowable(ex);
      }
    } else if (this.getCore() instanceof CycList) {
      CycList<CycObject> cl = (CycList<CycObject>) this.getCore();
      o = cl.get(getPos);
    } else if (this.getCore() instanceof FormulaSentence) {
      FormulaSentence cfs = (FormulaSentence) this.getCore();
      o = cfs.getArg(getPos);
    } else if (this.getCore() instanceof CycConstant) {
      throw new UnsupportedOperationException(
              "The object: " + this.toString() + " is an Atomic term. It does not have arguments.");
    } else {
      throw new IllegalArgumentException(
              "Unable to determine the arg " + getPos + " of " + this.toString());
    }
    return (O) KbObjectImpl.checkAndCastObject(o);
  }
  
  /**
   * Replace non-destructively a set of objects with another set of objects, in a
   * Non-Atomic Term or a Sentence. Replacement is not supported within Assertions,
   * Atomic Terms, Variables and Symbols. Although, set of objects replaced can 
   * be any KBObject or Java primitive object. 
   * 
   * @param <O>
   * @param substitutions  the replacement mapping
   * 
   * @return  A new object with the substitutions made in the original object
   * 
   * @throws KbTypeException
   * @throws CreateException 
   */
  protected <O> O replaceTerms(Map substitutions) throws KbTypeException, CreateException {
    final Map substitutionCores = new HashMap();
    substitutions.keySet().forEach((key) -> {
      substitutionCores.put(
              KbObjectImpl.convertKBObjectToCycObject(key),
              KbObjectImpl.convertKBObjectToCycObject(substitutions.get(key))
      );
    });
    if (this.getCore() instanceof NonAtomicTerm) {
      NonAtomicTerm nat = (NonAtomicTerm) this.getCore();
      Formula natFormulaMod = nat.getFormula().applySubstitutionsNonDestructive(substitutionCores);
      NonAtomicTerm natMod = new NautImpl(natFormulaMod.getArgs());
      return (O) KbObjectImplFactory.findOrCreate(natMod, KbTermImpl.class);
    } else if (this.getCore() instanceof FormulaSentence) {
      final FormulaSentence f = (FormulaSentence) this.getCore();
      final FormulaSentence fMod 
              = (FormulaSentence) f.applySubstitutionsNonDestructive(substitutionCores);
      return (O) new SentenceImpl(fMod);
    } else {
      // Anything else, we won't replace, including, Atomic terms
      // Assertions, Variables, Symbols etc.
      return (O) this;
    }
  }
  
}
