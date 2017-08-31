package com.cyc.kb.client;

/*
 * #%L
 * File: FactImpl.java
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
import com.cyc.base.cycobject.CycList;
import com.cyc.base.cycobject.CycObject;
import com.cyc.base.cycobject.FormulaSentence;
import com.cyc.base.exception.CycApiException;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.baseclient.cycobject.DefaultCycObject;
import com.cyc.kb.Assertion.Direction;
import com.cyc.kb.Assertion.Strength;
import com.cyc.kb.Context;
import com.cyc.kb.Fact;
import com.cyc.kb.KbObject;
import com.cyc.kb.KbPredicate;
import com.cyc.kb.Sentence;
import com.cyc.kb.client.config.KbConfiguration;
import com.cyc.kb.exception.CreateException;
import com.cyc.kb.exception.DeleteException;
import com.cyc.kb.exception.KbException;
import com.cyc.kb.exception.KbObjectNotFoundException;
import com.cyc.kb.exception.KbRuntimeException;
import com.cyc.kb.exception.KbTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A fact object is an assertion with a variable-free sentence with no conjunctions or operators
 * (a ground atomic formula), and a facade for #$CycLGAFAssertion. In general, this class 
 * is used to create and remove arbitrary assertions from Cyc.
 * 
 * @author Vijay Raj
 * @version $Id: FactImpl.java 171787 2017-05-04 23:16:40Z nwinant $
 * @since 1.0
 */

/*
 * @todo add factory class, as well as method to confirm possible facts. Enum
 * values might include ASSERTED, FORWARD_DERIVED, TRIVIALLY_DERIVABLE, UNKNOWN
 * (or we might not want the TRIVIALLY_DERIVABLE part)
 */

// @todo DaveS review documentation
public class FactImpl extends AssertionImpl implements Fact {

  private static final Logger LOG = LoggerFactory.getLogger(FactImpl.class.getCanonicalName());
  
  /**
   * This not part of the public, supported KB API. default constructor, calls the default
   * super constructor
   * 
   * @throws KbRuntimeException
   *           if there is a problem connecting to Cyc
   */
  protected FactImpl() {
    super();
  }

  /**
   * This not part of the public, supported KB API. an implementation-dependent constructor
   * <p>
   * Return a new
   * <code>Fact</code> based on the existing CycAssertion object
   * <code>cycFact</code>. The KB assertion underlying
   * <code>cycFact</code> must already be a #$CycLGAFAssertion. The constructor
   * verifies that cycFact is CycAssertion and cycFact.isGaf() is true.
   * 
   * It is used when the result of query is a CycObject and is known to be or
   * requested to be cast as an Assertion.
   *
   * @param cycFact the CycObject wrapped by Assertion. The constructor
   * verifies that the CycObject is an #$CycLGAFAssertion.
   * 
   * @throws KbTypeException  if cycAssert (which already exists) is not 
   * a #$CycLGAFAssertion
   */
  // We have made this public for the reflection mechanism to see this class.
  // If made package private, reflection of getConstructor(CycObject.class) fails
  // TODO: Check if this is a problem for other classes as well.
  @SuppressWarnings("deprecation")
  FactImpl(CycObject cycAssert) throws KbTypeException {
    super(cycAssert);
  }

  /**
   * find or create a <code>Fact</code> using <code>factStr</code> in the
   * context represented by <code>ctxStr</code>.
   * <p>
   * As elsewhere in the KB API, <code>factStr</code> and <code>ctxStr</code>
   * may contain the CycL constant prefix "#$" on CycL terms, but the prefix is
   * not required. Thus, either of the following strings would be acceptable:
   * <ul>
   * <li>
   * "(birthDate GeorgeWashington (DayFn 22 (MonthFn February (YearFn 1732))))"
   * <li>
   * "(#$birthDate #$GeorgeWashington (#$DayFn 22 (#$MonthFn #$February (#$YearFn 1732))))"
   * </ul>
   * 
   * @param ctxStr
   *          the string representing the context where the fact will be
   *          asserted
   * @param factStr
   *          the CycL string of the fact to be asserted.
   * 
   * @throws KbException
   */
  FactImpl(String ctxStr, String factStr) throws KbException {
    this(false, ctxStr, factStr);
  }

  /**
   * find or create a fact using the arguments in <code>argList</code> in the
   * context.
   * <p>
   * Elements of <code>argList</code> are expected to be instances of
   * <code>KBObject</code>, or 'primitive' java objects (i.e. Numbers, Strings),
   * or {@link java.util.Date}. If arguments are Strings, they will not be
   * interpreted as Cyc terms, but will be treated as raw Strings.
   * 
   * @param ctx
   *          the context where the fact will be asserted
   * @param pred the predicate of the GAF
   * @param argList
   *          the arguments of the fact
   * 
   * @throws KbException
   */
  FactImpl(Context ctx, KbPredicate pred, Object... argList)
      throws KbException {
    this(false, ctx, pred, argList);
  }

  /**
   * find or create a fact using <code>sentence</code> in the context.
   * <p>
   * 
   * @param ctx
   *          the context where the fact will be asserted
   * @param sentence
   *          the sentence of the fact
   * 
   * @throws KbException
   */
  FactImpl(Context ctx, Sentence sentence) throws KbException {
    this(false, ctx, sentence);
  }

  public FactImpl(boolean findOnly, String ctxStr, String factStr)
      throws KbException {
    super();
    try {
      final CycAssertion ca = findAssertion(factStr, ctxStr);
      if (ca != null) {
        core = ca;
      } else if (findOnly == true) {
        throw new KbObjectNotFoundException(
                "Could not find the assertion: " + factStr + " in Mt: " + ctxStr);
      } else {
        // For facts, the direction is always backward.
        core = assertSentence(factStr, ctxStr, null, null);
      }
    } catch (CreateException | KbTypeException ex) {
      throw new KbException(ex);
    }
  }

  @SuppressWarnings("deprecation")
 FactImpl(boolean findOnly, Context ctx, KbPredicate pred, Object... argList)
      throws KbException {
    super();
    try {
      // Turn on the findOnly flag when Context is #$EverythingPSC or
      // #$InferencePSC
      // TODO: More generally, we need to have query-only MTs and assertion MTs
      // and
      // check if the Context is query only MT.
      if (findOnly == false
          && (ctx.equals(Constants.everythingPSCMt()) || ctx.equals(Constants
              .inferencePSCMt()))) {
        findOnly = true;
        LOG.warn(
                "Overriding 'findOnly' flag to true"
                + " because Context is either EverythingPSC or InferencePSC");
      }
      // @todo above should error rather than do something that you told it not
      // to, probably
      
      // CycFormulaSentence factSentence = constructSentence(pred, argListArray);
      final FormulaSentence factSentence
              = (FormulaSentence) new SentenceImpl(pred, argList).getCore();
      // @todo get rid of cut-n-paste between this method and the following
      // one...
      LOG.trace("fact Sentence: {}", factSentence);

      final CycAssertion ca = findAssertion(factSentence, ContextImpl.asELMt(ctx));
      if (ca != null) {
        core = ca;
      } else if (findOnly == true) {
        throw new KbObjectNotFoundException(
                "Could not find the assertion: " + factSentence + " in Mt: " + ctx);
      } else {
        // core = assertSentence(ctx, factSentence);
        // For Facts, the direction is always backward
        core = assertSentence(factSentence, ctx, null, null);
      }
    } catch (KbTypeException | CreateException ex) {
      throw new KbException(ex);
    }
  }

  @SuppressWarnings("deprecation") 
  FactImpl(boolean findOnly, Context ctx, Sentence factSentence)
      throws KbException {
    super();
    try {
      // Turn on the findOnly flag when Context is #$EverythingPSC or
      // #$InferencePSC
      // TODO: More generally, we need to have query-only MTs and assertion MTs
      // and
      // check if the Context is query only MT.
      if (findOnly == false
          && (ctx.equals(Constants.everythingPSCMt()) || ctx.equals(Constants
              .inferencePSCMt()))) {
        findOnly = true;
        LOG.warn(
                "Overriding 'findOnly' flag to true"
                + " because Context is either EverythingPSC or InferencePSC");
      }
      // @todo above should error rather than do something that you told it not
      // to, probably
      LOG.trace("fact Sentence: {}", factSentence);
      final CycAssertion ca = findAssertion(
          (FormulaSentence) factSentence.getCore(), ContextImpl.asELMt(ctx));
      if (ca != null) {
        core = ca;
      } else if (findOnly == true) {
        throw new KbObjectNotFoundException(
                "Could not find the assertion: " + factSentence + " in Mt: " + ctx);
      } else {
        // core = assertSentence(ctx,
        // (CycFormulaSentence)factSentence.getCore());
        core = assertSentence((FormulaSentence) factSentence.getCore(), ctx, null, null);
      }
    } catch (Exception ex) {
      throw new KbException(ex);
    }
  }

  /**
   * Get the <code>Fact</code> object that corresponds to
   * <code>cycFact</code>. Throws exceptions if the object isn't in the KB, or if
   * it's not already an assertion. (Currently cycFact is only checked to be an 
   * instance of CycAssertion and to be a GAF - Ground Atomic Formula. )
   * 
   * @param cycFact candidate fact object
   * 
   * @return a Fact based on cycFact
   * 
   * @throws KbTypeException if the cycFact is not an instance of assertion and 
   * a ground atomic formula
   * @throws CreateException 
   */
  @Deprecated
  public static FactImpl get(CycObject cycFact) throws KbTypeException, CreateException {
    return KbObjectFactory.get(cycFact, FactImpl.class);
  }
  
  @SuppressWarnings("deprecation")
  public static FactImpl get(String hlid) throws KbTypeException, CreateException {
    final Object result;
    // NOTE: The StandardKBObject was too geared towards Term (Constant, NAT) creation
    // Did not want to overload that with assertion creation as well. 
    // Also the get method here takes only hlid. For a factory method that takes String to 
    // find an assertion, see get(String formulaStr, String ctxStr)
    try {
      result = DefaultCycObject.fromPossibleCompactExternalId(hlid, getStaticAccess());
    } catch (CycConnectionException e){
      throw new KbRuntimeException(e.getMessage(), e);
    }
    if (result instanceof CycObject) {
      return KbObjectFactory.get((CycObject)result, FactImpl.class);
    } else {
      String msg = "Could not find any Assertion with hlid: " + hlid + " in the KB.";
      throw new KbObjectNotFoundException(msg);
    }
  }
  
  @SuppressWarnings("deprecation")
  public static FactImpl get(String formulaStr, String ctxStr) 
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
    final CycAssertion result = findAssertion(formulaStr, ctxStr);
    return convertToFoundAssertion(result, formulaStr, ctxStr, FactImpl.class);
  }
  
  @SuppressWarnings("deprecation")
  public static FactImpl get(SentenceImpl formula, Context ctx) 
          throws KbTypeException, CreateException {
    final CycAssertion result
            = findAssertion(FormulaSentence.class.cast(formula.getCore()), ContextImpl.asELMt(ctx));
    return convertToFoundAssertion(result, formula, ctx, FactImpl.class);
  }
  
  @SuppressWarnings("deprecation")
  public static FactImpl findOrCreate(String formulaStr, String ctxStr, Strength s, Direction d) 
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
    return convertToFoundOrCreatedAssertion(result, formulaStr, ctxStr, FactImpl.class);
  }
  
  public static FactImpl findOrCreate(String formulaStr, String ctxStr) 
      throws KbTypeException, CreateException {
    return FactImpl.findOrCreate(formulaStr, ctxStr, Strength.AUTO, Direction.AUTO);
  }
  
  public static FactImpl findOrCreate(String formulaStr) throws KbTypeException, CreateException {
	  return FactImpl.findOrCreate(
            formulaStr, 
            KbConfiguration.getDefaultContext().forAssertion().toString());
  }
  
  @SuppressWarnings("deprecation")
  public static FactImpl findOrCreate(Sentence formula, Context ctx, Strength s, Direction d) 
      throws KbTypeException, CreateException {
    final CycAssertion result 
            = assertSentence(FormulaSentence.class.cast(formula.getCore()), ctx, s, d);
    return convertToFoundOrCreatedAssertion(result, formula, ctx, FactImpl.class);  
  }
  
  public static FactImpl findOrCreate(Sentence formula, Context ctx) 
      throws KbTypeException, CreateException {
    return FactImpl.findOrCreate(formula, ctx, Strength.AUTO, Direction.AUTO);
  }
  
  public static FactImpl findOrCreate(Sentence formula) throws KbTypeException, CreateException {
	  return FactImpl.findOrCreate(formula, KbConfiguration.getDefaultContext().forAssertion());
  }
  
  @Override
  public <O> O getArgument(int getPos) throws KbTypeException, CreateException  {
    final CycAssertion ca = (CycAssertion) this.getCore();
    final CycList<Object> g = ca.getGaf().getArgs();
    final Object result = g.get(getPos);
    return KbObjectImpl.<O> checkAndCastObject(result);
  }
  
  @Override
  public void delete() throws DeleteException {
    final CycAssertion ca = (CycAssertion) core;
    final FormulaSentence sentence = ca.getGaf();
    final CycObject mt = ca.getMt();
    try {
      getAccess().getUnassertTool().unassertGaf(sentence, mt, true,
          KbConfiguration.getShouldTranscriptOperations());
      setIsValid(false);
    } catch (CycConnectionException ex) {
      throw new KbRuntimeException(
              "Couldn't delete the fact: " + core.toString(), ex);
    }
    try {
      if (findAssertion(
              sentence, 
              getAccess().getObjectTool().makeElMt(mt)) instanceof CycAssertion) {
        throw new DeleteException(
                "Unable to delete Fact " + sentence + " in " + mt);
      }
    } catch (CycConnectionException | CycApiException ex) {
      throw new KbRuntimeException(
              "Couldn't delete the fact: " + core.toString(), ex);
    }
  }

  // If this is a meta-fact, then the toString will not have the nice
  // "(ist ctx fact")
  @Override
  public String toString() {
    final CycAssertion ca = (CycAssertion) core;
    final String result;
    if (ca.isGaf()) {
      result = "(ist " + ca.getMt().toString() + " " + ca.getGaf().toString() + ")";
      LOG.trace("String API value of CycAssertion: {}", ca.stringApiValue());
    } else {
      result = core.toString();
    }
    return result;
  }

  /**
   * This not part of the public, supported KB API. Check that the candidate core object is
   * valid Ground Automic Formula. In the CycKB the object would be valid
   * #$CycLGAFAssertion
   * 
   * Internally this method checks if the the <code>cycObject</code> is an
   * instance of CycAssertion and it is fully grounded, i.e. has not open
   * variables.
   * 
   * @return 
   * @see StandardKBObject#StandardKBObject(CycObject)  for more comments
   */
  @Override
  protected boolean isValidCore(CycObject cycObject) {
    return (cycObject instanceof CycAssertion) && ((CycAssertion) cycObject).isGaf();
  }

  /**
   * Return the KBCollection as a KBObject of the Cyc term that 
   * underlies this class. 
   * 
   * @return KBCollectionImpl.get("#$CycLGAFAssertion");
   */
  @Override
  public KbObject getType() {
    return getClassType();
  }
  
  /**
   * Return the KBCollection as a KBObject of the Cyc term that 
   * underlies this class. 
   * 
   * @return KBCollectionImpl.get("#$CycLGAFAssertion");
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
    return "#$CycLGAFAssertion";
  }
}
