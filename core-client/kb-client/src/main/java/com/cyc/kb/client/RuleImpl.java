/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyc.kb.client;

/*
 * #%L
 * File: RuleImpl.java
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
import com.cyc.base.cycobject.CycObject;
import com.cyc.base.cycobject.FormulaSentence;
import com.cyc.base.cycobject.Guid;
import com.cyc.base.exception.CycApiException;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.baseclient.cycobject.CycConstantImpl;
import com.cyc.baseclient.cycobject.DefaultCycObjectImpl;
import com.cyc.kb.Assertion.Direction;
import com.cyc.kb.Assertion.Strength;
import com.cyc.kb.Context;
import com.cyc.kb.Rule;
import com.cyc.kb.Sentence;
import com.cyc.kb.client.config.KbConfiguration;
import com.cyc.kb.exception.CreateException;
import com.cyc.kb.exception.KbObjectNotFoundException;
import com.cyc.kb.exception.KbRuntimeException;
import com.cyc.kb.exception.KbTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Rule object is a facade for #$CycLRuleAssertion. A rule is a semantically well formed sentence,
 * where the ANTECEDENT implies the CONSEQUENT. The sentence is non-atomic and may contain open
 * variables.
 *
 * @author vijay
 * @version $Id: RuleImpl.java 173082 2017-07-28 15:36:55Z nwinant $
 * @since 1.0
 */
public class RuleImpl extends AssertionImpl implements Rule {

  private static final CycConstant IMPLIES 
          = new CycConstantImpl("implies", new Guid("bd5880f8-9c29-11b1-9dad-c379636f7270"));

  private static final Logger LOG = LoggerFactory.getLogger(RuleImpl.class.getCanonicalName());

  /**
   * This is not part of the public, supported KB API.
   *
   * @throws KbRuntimeException if there is a problem connecting to Cyc
   */
  RuleImpl() {
    super();
  }

  /**
   * This is not part of the public, supported KB API.
   * <p>
   * Return a new <code>Rule</code> based on the existing CycAssertion object
   * <code>cycAssert</code>. The KB assertion underlying <code>cycAssert</code> must already be a
   * #$CycLAssertion.
   *
   * It is used when the result of query is a CycObject and is known to be or requested to be cast
   * as a Rule.
   *
   * @param cycAssert	the CycObject wrapped by Rule. The constructor verifies that the CycObject is
   * an #$CycLAssertion with #$implies operator.
   *
   * @throws KbTypeException if cycAssert (which already exists) is not a #$CycLAssertion with
   * #$implies operator
   */
  @Deprecated
  RuleImpl(CycAssertion cycAssert) throws KbTypeException {
    super(cycAssert);
  }

  /**
   * Get the <code>Rule</code> that corresponds to <code>cycAssert</code>. Throws exceptions
   * if the object isn't in the KB, or if it is not actually a rule (i.e. it's not an instance of
   * CycAssertion with the #$implies operator. )
   *
   * @param cycAssert assertion object
   *
   * @return an Rule object encapsulating on cycAssert
   *
   * @throws KbTypeException if cycAssert is not an instance of assertion with the #$implies
   * operator
   * @throws CreateException
   */
  @Deprecated
  public static Rule get(CycObject cycAssert) throws KbTypeException, CreateException {
    return KbObjectImplFactory.get(cycAssert, RuleImpl.class);
  }
  
  @SuppressWarnings("deprecation")
  public static Rule get(String hlid) throws KbTypeException, CreateException {
    final Object result;
    // NOTE: The StandardKBObject was too geared towards Term (Constant, NAT) creation
    // Did not want to overload that with assertion creation as well. 
    // Also the get method here takes only hlid. For a factory method that takes String to 
    // find an assertion, see get(String formulaStr, String ctxStr)
    try {
      result = DefaultCycObjectImpl.fromPossibleCompactExternalId(hlid, getStaticAccess());
    } catch (CycConnectionException e) {
      throw new KbRuntimeException(e.getMessage(), e);
    }
    if (result instanceof CycAssertion) {
      LOG.debug("Found assertion: {} using HLID: {}", result, hlid);
      return KbObjectImplFactory.get((CycObject) result, RuleImpl.class);
    } else {
      String msg = "Could not find any Assertion with hlid: " + hlid + " in the KB.";
      LOG.error(msg);
      throw new KbObjectNotFoundException(msg);
    }
  }
  
  @SuppressWarnings("deprecation")
  public static Rule get(String formulaStr, String ctxStr) throws KbTypeException, CreateException {
    // @todo: There are two purposes of caching
    // 1. Reduce round trip to KB
    // 2. Use the same object if core is equal.
    // Since the cache key is cyclify() and hlid, we have to find the assertion
    // using formulaStr and ctxStr to get any of the cache keys. Which means we have to
    // do one trip to the KB anyways. But we still use KBObjectFactory.get to reuse the
    // same KBObject.
    // A separate KBObjectFactory method that takes the ist sentence of formula and mt,
    // could also eliminate the lookup step.
    final CycAssertion result = findAssertion(formulaStr, ctxStr);
    return convertToFoundAssertion(result, formulaStr, ctxStr, RuleImpl.class);
  }
  
  @SuppressWarnings("deprecation")
  public static Rule get(Sentence formula, Context ctx) throws KbTypeException, CreateException {
    final CycAssertion result 
            = findAssertion(FormulaSentence.class.cast(formula.getCore()), ContextImpl.asELMt(ctx));
    return convertToFoundAssertion(result, formula, ctx, RuleImpl.class);
  }
  
  public static Rule get(Sentence antecedent, Sentence consequent, Context ctx)
          throws KbTypeException, CreateException {
    return RuleImpl.get(SentenceImpl.implies(antecedent, consequent), ctx);
  }
  
  @SuppressWarnings("deprecation")
  public static Rule findOrCreate(String formulaStr, String ctxStr, Strength s, Direction d)
          throws KbTypeException, CreateException {
    // @todo: There are two purposes of caching
    // 1. Reduce round trip to KB
    // 2. Use the same object if core is equal.
    // Since the cache key is cyclify() and hlid, we have to find the assertion
    // using formulaStr and ctxStr to get any of the cache keys. Which means we have to
    // do one trip to the KB anyways. But we still use KBObjectFactory.get to reuse the
    // same KBObject.
    // A separate KBObjectFactory method that takes the ist sentence of formula and mt,
    // could also eliminate the lookup step.
    // The assertSentence tries to find the assertion anyways, before actually trying
    // to assert.
    final CycAssertion result = assertSentence(formulaStr, ctxStr, s, d);
    return convertToFoundOrCreatedAssertion(result, formulaStr, ctxStr, RuleImpl.class);
  }
  
  public static Rule findOrCreate(String formulaStr, String ctxStr)
          throws KbTypeException, CreateException {
    return RuleImpl.findOrCreate(formulaStr, ctxStr, Strength.AUTO, Direction.BACKWARD);
  }
  
  public static Rule findOrCreate(String formulaStr) throws KbTypeException, CreateException {
    return RuleImpl.findOrCreate(
            formulaStr, 
            KbConfiguration.getDefaultContext().forAssertion().toString());
  }
  
  @SuppressWarnings("deprecation")
  public static Rule findOrCreate(
          Sentence formula, Context ctx, Strength s, Direction d, boolean verbose)
          throws KbTypeException, CreateException {
    try {
      final CycAssertion result 
              = assertSentence(FormulaSentence.class.cast(formula.getCore()), ctx, s, d);
      return convertToFoundOrCreatedAssertion(result, formula, ctx, RuleImpl.class);
    } catch (CycApiException ex) {
      return throwAssertException(formula, ctx, ex, verbose);
    }
  }
  
  @SuppressWarnings("deprecation")
  public static Rule findOrCreate(Sentence formula, Context ctx, Strength s, Direction d)
          throws KbTypeException, CreateException {
    return RuleImpl.findOrCreate(formula, ctx, s, d, VERBOSE_ASSERT_ERRORS_DEFAULT);
  }
  
  public static Rule findOrCreate(Sentence formula, Context ctx, boolean verbose)
          throws KbTypeException, CreateException {
    return RuleImpl.findOrCreate(formula, ctx, Strength.AUTO, Direction.BACKWARD, verbose);
  }
  
  public static Rule findOrCreate(Sentence formula, Context ctx)
          throws KbTypeException, CreateException {
    return RuleImpl.findOrCreate(formula, ctx, VERBOSE_ASSERT_ERRORS_DEFAULT);
  }
  
  public static Rule findOrCreate(Sentence formula, boolean verbose) 
          throws KbTypeException, CreateException {
    return RuleImpl
            .findOrCreate(formula, KbConfiguration.getDefaultContext().forAssertion(), verbose);
  }
  
  public static Rule findOrCreate(Sentence formula) throws KbTypeException, CreateException {
    return RuleImpl.findOrCreate(formula, VERBOSE_ASSERT_ERRORS_DEFAULT);
  }
  
  public static Rule findOrCreate(
          Sentence antecedent, Sentence consequent, Context ctx, boolean verbose)
          throws KbTypeException, CreateException {
    return RuleImpl.findOrCreate(SentenceImpl.implies(antecedent, consequent), ctx, verbose);
  }
  
  public static Rule findOrCreate(Sentence antecedent, Sentence consequent, Context ctx)
          throws KbTypeException, CreateException {
    return RuleImpl.findOrCreate(antecedent, consequent, ctx, VERBOSE_ASSERT_ERRORS_DEFAULT);
  }
  
  @Override
  public Sentence getAntecedent() {
    CycAssertion ca = (CycAssertion) this.getCore();
    try {
      final FormulaSentence result = (FormulaSentence) ca.getELFormula(getAccess()).getArg1();
      return KbObjectImpl.<Sentence>checkAndCastObject(result);
    } catch (CycApiException | CycConnectionException | CreateException ex) {
      throw new KbRuntimeException(ex.getMessage(), ex);
    }
  }

  @Override
  public Sentence getConsequent() {
    final CycAssertion ca = (CycAssertion) this.getCore();
    try {
      final FormulaSentence result = (FormulaSentence) ca.getELFormula(getAccess()).getArg2();
      return KbObjectImpl.<Sentence>checkAndCastObject(result);
    } catch (CycApiException | CycConnectionException | CreateException ex) {
      throw new KbRuntimeException(ex.getMessage(), ex);
    }
  }
  
  /**
   * This is not part of the supported, public KB API. Check that the candidate core object is a
   * valid implication formula, a non-atomic sentence with possibly open sentences. In the CycKB the
   * object would be valid #$CycLRuleAssertion
   *
   * Internally this method checks if the the <code>cycObject</code> is an instance of CycAssertion
   * and the operator is #$implies.
   *
   * @return 
   * @see StandardKBObject#StandardKBObject(CycObject) for more comments
   */
  @Override
  protected boolean isValidCore(CycObject cycObject) {
    if ((cycObject instanceof CycAssertion) && !((CycAssertion) cycObject).isGaf()) {
      try {
        final CycObject operator = ((CycAssertion) cycObject).getArg0(getAccess());
        return operator.equals(IMPLIES);
      } catch (CycApiException | CycConnectionException ex) {
        throw new KbRuntimeException(ex.getMessage(), ex);
      }
    }
    return false;
  }
  
}
