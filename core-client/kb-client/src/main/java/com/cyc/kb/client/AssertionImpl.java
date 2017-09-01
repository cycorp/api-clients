package com.cyc.kb.client;

/*
 * #%L
 * File: AssertionImpl.java
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

import com.cyc.base.CycAccess;
import com.cyc.base.cycobject.CycAssertion;
import com.cyc.base.cycobject.CycList;
import com.cyc.base.cycobject.CycObject;
import com.cyc.base.cycobject.CycSymbol;
import com.cyc.base.cycobject.ElMt;
import com.cyc.base.cycobject.FormulaSentence;
import com.cyc.base.exception.CycApiException;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.baseclient.CycObjectFactory;
import com.cyc.baseclient.cycobject.CycSymbolImpl;
import com.cyc.baseclient.cycobject.DefaultCycObjectImpl;
import com.cyc.baseclient.cycobject.FormulaSentenceImpl;
import com.cyc.baseclient.inference.params.DefaultInferenceParameters;
import com.cyc.baseclient.inference.params.OpenCycInferenceParameterEnum;
import com.cyc.kb.Assertion;
import com.cyc.kb.Assertion.Direction;
import com.cyc.kb.Assertion.Strength;
import com.cyc.kb.Context;
import com.cyc.kb.KbCollection;
import com.cyc.kb.KbObject;
import com.cyc.kb.Sentence;
import static com.cyc.kb.client.KbContentLogger.KB_FIND_LOGGER;
import static com.cyc.kb.client.KbObjectImpl.getStaticAccess;
import com.cyc.kb.client.config.KbConfiguration;
import com.cyc.kb.exception.CreateException;
import com.cyc.kb.exception.DeleteException;
import com.cyc.kb.exception.InvalidFormulaInContextException;
import com.cyc.kb.exception.KbException;
import com.cyc.kb.exception.KbObjectNotFoundException;
import com.cyc.kb.exception.KbRuntimeException;
import com.cyc.kb.exception.KbTypeException;
import com.cyc.query.parameters.InferenceParameters;
import java.util.ArrayList;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An Assertion object is a facade for a
 * <code>#$CycLAssertion</code> in Cyc KB. An assertion is a semantically well
 * formed Sentence, in a specific Context.
 * <p>
 * Sub-classes include Fact and Rule.
 *
 * @author Vijay Raj
 * @version $Id: AssertionImpl.java 173082 2017-07-28 15:36:55Z nwinant $
 * @since 1.0
 */
public class AssertionImpl extends PossiblyNonAtomicKbObjectImpl<CycAssertion> implements Assertion {
  
  /**
   * In some cases, the act of asserting a formula may be successful <em>without actually 
   * creating an assertion in the KB.</em> For example, a <code>#$SKSIContentMicrotheory</code>
   * provides a "window" into an external knowledge source, and any knowledge asserted into a
   * #$SKSIContentMicrotheory will be stored in that external knowledge source. In other words, an 
   * assertion into a particular Context may cause rows to be added to, e.g., a relational database
   * where Cyc may have retrieve and incorporate them into query answers, but without any actual
   * assertion object being created in Cyc's KB. In such cases -- where the asserted sentence has
   * been successfully stored, but there is no Assertion object to return -- #get() and
   * #findOrCreate() should return <code>null</code> instead of throwing an exception.
   */
  private static final boolean TREAT_EXTERNAL_SKS_KNOWLEDGE_AS_ASSERTIONS = true;
  
  private static final Logger LOG = LoggerFactory.getLogger(AssertionImpl.class.getCanonicalName());
  private static final KbContentLogger KB_LOG = KbContentLogger.getInstance();
  
  public static boolean VERBOSE_ASSERT_ERRORS_DEFAULT = true;
  
  /**
   * This not part of the public, supported KB API. default constructor, calls the default
   * super constructor
   *
   * @throws KbRuntimeException if there is a problem connecting to Cyc
   */
  AssertionImpl() {
    super();
  }
  
  /**
   * This not part of the public, supported KB API. an implementation-dependent constructor
   * <p>
   * Return a new
   * <code>Assertion</code> based on the existing CycAssertion object
   * <code>cycAssert</code>. The KB assertion underlying
   * <code>cycAssert</code> must already be a #$CycLAssertion.
   * 
   * It is used when the result of query is a CycObject and is known to be or
   * requested to be cast as an Assertion.
   *
   * @param cycAssert	the CycObject wrapped by Assertion. The constructor
   * verifies that the CycObject is an #$CycLAssertion.
   * 
   * @throws KbTypeException  if cycAssert (which already exists) is not 
   * a #$CycLAssertion
   */
  // We have made this public for the reflection mechanism to see this class.
  // If made package private, reflection of getConstructor(CycObject.class) fails
  @Deprecated
  AssertionImpl(CycAssertion cycAssert) throws KbTypeException {
    super(cycAssert);
  }
  
  /**
   * Get the <code>Assertion</code> object that corresponds to
   * <code>cycAssert</code>. Throws exceptions if the object isn't in the KB, or if
   * it's not already an assertion. (Currently cycAssert is only checked to be an 
   * instance of CycAssertion. )
   * 
   * @param cycAssert candidate assertion object
   * 
   * @return an Assertion based on cycAssert
   * 
   * @throws KbTypeException if the cycAssert is not an instance of assertion
   * @throws CreateException 
   */
  @Deprecated
  public static Assertion get(CycObject cycAssert) throws KbTypeException, CreateException {
    return KbObjectImplFactory.get(cycAssert, AssertionImpl.class);
  }
  
  @SuppressWarnings("deprecation")
  public static Assertion get(String hlid) throws KbTypeException, CreateException {
    // NOTE: The StandardKBObject was too geared towards Term (Constant, NAT) creation
    // Did not want to overload that with assertion creation as well. 
    // Also the get method here takes only hlid. For a factory method that takes String to 
    // find an assertion, see get(String formulaStr, String ctxStr)
    final Object result;
    try {
      result = DefaultCycObjectImpl.fromPossibleCompactExternalId(hlid, getStaticAccess());
    } catch (CycConnectionException e){
      throw new KbRuntimeException(e.getMessage(), e);
    }
    if (result instanceof CycAssertion) {
      LOG.debug("Found assertion: {} using HLID: {}", result, hlid);
      return KbObjectImplFactory.get((CycObject)result, AssertionImpl.class);
    } else {
      String msg = "Could not find any Assertion with hlid: " + hlid + " in the KB.";
      LOG.error(msg);
      throw new KbObjectNotFoundException(msg);
    }
  }
    
  @SuppressWarnings("deprecation")
  public static Assertion get(Sentence formula, Context ctx) 
          throws KbTypeException, CreateException {
    //@todo: There are two purposes of caching
    // 1. Reduce round trip to KB
    // 2. Use the same object if core is equal.
    // Since the cache key is cyclify() and hlid, we have to find the assertion
    // using formulaStr and ctxStr to get any of the cache keys. Which means we have to
    // do one trip to the KB anyways. But we still use KBObjectFactory.get to reuse the
    // same KBObject.       
    // A separate KBObjectFactory method that takes the ist sentence of formula and mt,
    // could also eliminate the lookup step.
    final CycAssertion result
            = findAssertion(FormulaSentence.class.cast(formula.getCore()), ContextImpl.asELMt(ctx));
    return convertToFoundAssertion(result, formula, ctx, AssertionImpl.class);
  }
  
  @SuppressWarnings("deprecation")
  public static Assertion get(String formulaStr, String ctxStr)
          throws KbTypeException, CreateException {
    final CycAssertion result = findAssertion(formulaStr, ctxStr);
    return convertToFoundAssertion(result, formulaStr, ctxStr, AssertionImpl.class);
  }
    
  protected static <O extends AssertionImpl> O throwAssertException(
          Sentence formula, Context ctx, Exception ex, boolean verbose) {
    if (ex instanceof KbRuntimeException) {
      throw (KbRuntimeException) ex;
    }
    final String msg = "Could not assert " + formula + " in " + ctx;
    if (verbose) {
      final String explanations = formula.notAssertibleExplanation(ctx);
      throw new KbRuntimeException(msg + ":\n" + explanations, ex);
    } else {
      throw new KbRuntimeException(msg, ex);
    }
  }
  
  @SuppressWarnings("deprecation")
  public static Assertion findOrCreate(String formulaStr, String ctxStr, Strength s, Direction d) 
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
    return convertToFoundOrCreatedAssertion(result, formulaStr, ctxStr, AssertionImpl.class);
  }
  
  public static Assertion findOrCreate(String formulaStr, String ctxStr) 
      throws KbTypeException, CreateException {
	  return findOrCreate(formulaStr, ctxStr, Strength.AUTO, Direction.AUTO);
  }
  
  public static Assertion findOrCreate(String formulaStr) throws KbTypeException, CreateException {
	  return findOrCreate(formulaStr, KbConfiguration.getDefaultContext().forAssertion().toString());
  }
  
  @SuppressWarnings("deprecation")
  public static Assertion findOrCreate(
          Sentence formula, Context ctx, Strength s, Direction d, boolean verbose) 
      throws KbTypeException, CreateException {
    try {
      final CycAssertion result 
              = assertSentence(FormulaSentence.class.cast(formula.getCore()), ctx, s, d);
        return convertToFoundOrCreatedAssertion(result, formula, ctx, AssertionImpl.class);
    } catch (CycApiException ex) {
      return throwAssertException(formula, ctx, ex, verbose);
    }
  }
  
  @SuppressWarnings("deprecation")
  public static Assertion findOrCreate(Sentence formula, Context ctx, Strength s, Direction d)
          throws KbTypeException, CreateException {
    return findOrCreate(formula, ctx, s, d, VERBOSE_ASSERT_ERRORS_DEFAULT);
  }
  
  public static Assertion findOrCreate(Sentence formula, Context ctx, boolean verbose) 
          throws KbTypeException, CreateException {
	  return findOrCreate(formula, ctx, Strength.AUTO, Direction.AUTO, verbose);
  }
  
  public static Assertion findOrCreate(Sentence formula, Context ctx) 
          throws KbTypeException, CreateException {
	  return findOrCreate(formula, ctx, VERBOSE_ASSERT_ERRORS_DEFAULT);
  }
  
  public static Assertion findOrCreate(Sentence formula, boolean verbose) 
          throws KbTypeException, CreateException {
	  return findOrCreate(formula, KbConfiguration.getDefaultContext().forAssertion(), verbose);
  }
  
  public static Assertion findOrCreate(Sentence formula) throws KbTypeException, CreateException {
	  return findOrCreate(formula, VERBOSE_ASSERT_ERRORS_DEFAULT);
  }
  
  @Override
  public Sentence getFormula() {
    try {
      final FormulaSentence assertionFormula = getCore().getELFormula(getAccess());
      return new SentenceImpl(assertionFormula);
    } catch (CycApiException | CycConnectionException | KbTypeException | CreateException ex) {
      LOG.error(ex.getMessage());
      throw new KbRuntimeException(ex.getMessage(), ex);
    }
  }
  
  @Override
  @SuppressWarnings("deprecation")
  public Context getContext() {
    final CycAssertion ca = getCore();
    final ContextImpl ctx;
    try {
      ctx = ContextImpl.get(ca.getMt());
    } catch (CreateException | KbTypeException te) {
      // The assertion is already created, that means the context should
      // already be there. 
      throw new KbRuntimeException(te.getMessage(), te);
    }
    return ctx;
  }
  
  @Override
  public Collection<Assertion> getSupportingAssertions() throws KbTypeException, CreateException {
    try {
      //String command = SublConstants.getInstance().assertionAssertedAssertionSupports.buildCommand(this.getCore());
      //CycList<?> result = getAccess().converse().converseList(command);
      final CycList<?> result = SublConstants.getInstance()
              .assertionAssertedAssertionSupports.eval(getAccess(), this.getCore());
      final Collection<Assertion> asserts = new ArrayList<>();
      for (Object o : result) {
        asserts.add(AssertionImpl.get((CycObject) o));
      }
      return asserts;
    } catch (CycConnectionException e) {
      throw new KbRuntimeException(e);
    }
  }

  /* *
   *
   * @return the collection of all assertions that directly or indirectly
   * support this assertion. Effectively this is a transitive version of
   * {@link getSupportingAssertions}.
   */
  /*  this needs to be completed, and it should be basically the same as getSupportingAssertions with a high depth limit.
   public Collection<Assertion> getAllSupportingAssertions () {
   throw new UnsupportedOperationException();
   }
   */
  
  @Override
  public Boolean isDeducedAssertion() {
    try {
      final String command 
              = SublConstants.getInstance().deducedAssertionQ.buildCommand(getCore());
      return getAccess().converse().converseBoolean(command);
    } catch (CycConnectionException e) {
      throw new KbRuntimeException(e);
    }
  }
  
  @Override
  public Boolean isGroundAtomicFormula() {
	 return ((CycAssertion) this.getCore()).isGaf();
  }
  
  @Override
  public Boolean isAssertedAssertion() {
    try {
      final String command 
              = SublConstants.getInstance().assertedAssertionQ.buildCommand(getCore());
      return getAccess().converse().converseBoolean(command);
    } catch (CycConnectionException e) {
      throw new KbRuntimeException(e);
    }
  }
  
  @Override
  public Direction getDirection() {
    try {
      final String command
              = SublConstants.getInstance().assertionDirection.buildCommand(getCore());
      final CycObject co = getAccess().converse().converseCycObject(command);
      if (co instanceof CycSymbol) {
        final CycSymbol cs = (CycSymbol) co;
        if (cs.equals(new CycSymbolImpl(":BACKWARD"))) {
          return Direction.BACKWARD;
        } else if (cs.equals(new CycSymbolImpl(":FORWARD"))) {
          return Direction.FORWARD;
        } else {
          // This should never happen for CycAssertion, so a runtime exception
          throw new KbRuntimeException("Unknown or :CODE Direction");
        }
      } else {
        throw new KbRuntimeException("Unknown Direction");
      }
    } catch (CycConnectionException e) {
      throw new KbRuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public Assertion changeDirection(Direction d) throws KbException {
    try {
      if (this.getDirection().equals(d)) {
        LOG.info(
                "The input direction {} is the same as the assertion direction. Nothing to do.", d);
        return this;
      }
      if (KbConfiguration.getCurrentCyclist() == null) {
        throw new KbException("Set the Cyclist using KBAPIConfiguration.setCurrentCyclist()");
      }
      final String command = "(clet "
              + "((*the-cyclist* " + KbConfiguration.getCurrentCyclist().stringApiValue() + ")) "
              + "(" + SublConstants.getInstance().keChangeAssertionDirectionNow.stringApiValue()
              + " " + this.getCore().stringApiValue() + " :" + d.name() + "))";
      final CycObject co = getAccess().converse().converseCycObject(command);
      if (co instanceof CycAssertion) {
        LOG.debug("Changed the assertion direction of " + this + " to: " + d);
        // I'm guessing we were trying to force a ClassCastException here: - nwinant, 2017-05-04
        //final CycAssertion ca = (CycAssertion) co;
        return this;
      } else {
        throw new KbRuntimeException("Failed to change the direction of the assertion: " + this);
      }
    } catch (CycConnectionException e) {
      throw new KbRuntimeException(e.getMessage(), e);
    }
  }
  
  /**
   * Retrigger forward inference against this assertion. In general when adding or removing
   * assertions, Cyc's truth-maintenance system automatically handles this, but there are
   * exceptional cases (e.g. where resource constraints prevented forward inference from running
   * exhaustively) where it needs to be done manually. This is also known as repropagating the
   * assertion.
   *
   * @return the assertion that had its forward inference retriggered.
   * @throws KbException
   */
  public Assertion retriggerForwardInference() throws KbException {
    try {
      if (KbConfiguration.getCurrentCyclist() == null) {
        throw new KbException("Set the Cyclist using KBAPIConfiguration.setCurrentCyclist()");
      }
      final String command = "(clet "
              + "((*the-cyclist* " + KbConfiguration.getCurrentCyclist().stringApiValue() + ")) "
              + "(" + SublConstants.getInstance().keRepropagateAssertionNow.stringApiValue() 
              + " " + this.getCore().stringApiValue() + "))";
      final CycObject co = getAccess().converse().converseCycObject(command);
      if (co instanceof CycAssertion) {
        LOG.debug("Forward inference retriggered against " + this);
        // I'm guessing we were trying to force a ClassCastException here: - nwinant, 2017-05-04
        //final CycAssertion ca = (CycAssertion) co;
        return this;
      } else {
        throw new KbRuntimeException("Failed to retrigger forward inference against " + this);
      }
    } catch (CycConnectionException e) {
      throw new KbRuntimeException(e.getMessage(), e);
    }
  }

  /**
   * This not part of the public, supported KB API. Check that the candidate core
   * object is valid CycAssertion. In the CycKB the object would be
   * valid #$CycLAssertion
   * 
   * @return true if the core is valid for a given class of KBObject
   * @see StandardKBObject#isValidCore(CycObject)  for more comments
   */
  @Override
  protected boolean isValidCore(CycObject cycObject) {
    return cycObject instanceof CycAssertion;
  }
    
  /**
   * Return the KBCollection as a KBObject of the Cyc term that 
   * underlies this class. 
   * 
   * @return KBCollectionImpl.get("#$CycLAssertion");
   */
  @Override
  public KbObject getType() {
    return getClassType();
  }
  
  /**
   * Return the KBCollection as a KBObject of the Cyc term that 
   * underlies this class. 
   * 
   * @return KBCollectionImpl.get("#$CycLAssertion");
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
    return "#$CycLAssertion";
  }
  
  //TODO preserve this and make it work
  /*
   * private CycAssertion findAssertion(Context ctx, Object...argList) throws Exception {
   * List<Object> argListArray = new ArrayList<Object> (Arrays.asList(argList));
   *
   * // TODO: handle all types of arguments in the constructAssertion
   * // It could be anything from String, or CycL in string form, or SemanticObjects
   * String factString = constructFact(argListArray);
   * String cyclifiedFactString = cyc.cyclifyString(factString);
   *
   * return findAssertion(ctx.stringApiValue(), cyclifiedFactString);
   * }
   */
  
  /**
   * Finds an assertion with <code>factSentence</code> in the context <code>ctx</code>. In contrast 
   * to many other methods, this method requires that the assertion be found in the specified
   * context, not merely in some context visible from the specified context.
   * <p>
   *
   * @param factSentenceStr	the CycL string of the sentence to be asserted
   * @param ctxStr	the string representing the context
   * 
   * @return the assertion, if it exists. Returns null if the assertion isn't in the KB.
   *
   */
  static CycAssertion findAssertion(String factSentenceStr, String ctxStr) {
    try {
      final CycAccess cyc = getStaticAccess();
      final FormulaSentence factFormulaSentence 
              = FormulaSentenceImpl.makeCycSentence(cyc, factSentenceStr);
      return findAssertion(
              factFormulaSentence, 
              cyc.getObjectTool().makeElMt(cyc.cyclifyString(ctxStr)));
    } catch (CycApiException ex) {
      // TODO: Why was this previously commented out & returning null? - nwinant, 2017-05-04
      throw new KbRuntimeException(ex.getMessage(), ex);
      //return null;
    } catch (CycConnectionException ex) {
      throw new KbRuntimeException(ex.getMessage(), ex);
    }
  }
  
  /**
   * Finds an assertion with <code>factSentence</code> in the context <code>ctx</code>. In contrast 
   * to many other methods, this method requires that the assertion be found in the specified 
   * context, not merely in some context visible from the specified context.
   * <p>
   *
   * @param assertSentence	the CycL string of the fact to be asserted
   * @param ctx				the string representing the context
   * 
   * @return the assertion, if it exists. Returns null if the assertion isn't in the KB.
   * 
   * @TODO Major issues.
   * 1. This asks with-inference-mt-relevance : which means the returned assertions are visible in ctx
   * not necessarily asserted in ctx
   * 2. This calls find-assertion-cycl which returns some assertion if multiple assertions with the same
   * formula and same visibility are found
   * Need to decide what we want to do
   */
  static CycAssertion findAssertion(FormulaSentence assertSentence, ElMt ctx) {
    final String assertionLogMarker = KB_LOG.getAssertionLogMarker(assertSentence, ctx);
    KB_FIND_LOGGER.debug("{}  Looking up assertion '{}' in mt '{}'",
            assertionLogMarker, assertSentence, ctx);
    final String command
            = "(" + SublConstants.getInstance().withInferenceMtRelevance.stringApiValue()
            + " " + ctx.stringApiValue()
            + " (" + SublConstants.getInstance().findAssertionCycl.stringApiValue()
            + " " + assertSentence.stringApiValue() + " ))";
    LOG.trace("Assertion lookup command:  {}", command);
    KB_FIND_LOGGER.trace("{}  Assertion lookup command:  {}", assertionLogMarker, command);
    final Object result;
    try {
      result = getStaticAccess().converse().converseObject(command);
    } catch (CycApiException | CycConnectionException ex) {
      throw new KbRuntimeException(ex.getMessage(), ex);
    }
    LOG.trace("Assertion lookup response: {}", result);
    KB_FIND_LOGGER.trace("{}  Assertion lookup response: {}", assertionLogMarker, result);
    if (result instanceof CycAssertion) {
      LOG.debug("Found assertion {} using formula sentence: {} in mt: {}",
              result, assertSentence, ctx);
      KB_FIND_LOGGER.debug("{}  Found assertion '{}' in mt '{}' using formula sentence {}",
              assertionLogMarker, result, assertSentence, ctx);
      return (CycAssertion) result;
    }
    if (result.equals(CycObjectFactory.nil)) {
      String msg = "Couldn't find assertion '" + assertSentence + "' in context '" + ctx + "'";
      LOG.trace(msg);
      KB_FIND_LOGGER.debug("{}  {}", assertionLogMarker, msg);
      // throw new KBObjectNotFoundException(msg);
      return null;
    }
    throw new KbRuntimeException("Unknown error in converseObject result parsing.");
  }
  
  /**
   * Creates an assertion. This method <em>only</em> creates an assertion, it does absolutely no
   * checking for pre-existing assertions.
   * 
   * @param assertSentence	the CycL sentence to be asserted
   * @param ctx		          the context where the fact will be asserted
   * @param s               the strength of the assertion if asserted
   * @param d               the strength of the assertion if asserted
   * @return 
   */
  private static void makeAssertion(
          FormulaSentence assertSentence, ElMt mt, Strength s, Direction d) 
          throws CycConnectionException {
    if (d == null || d.equals(Direction.AUTO)) {
      d = Direction.FORWARD;
    }
    if (s == null || s.equals(Strength.AUTO)) {
      s = Strength.DEFAULT;
    }
    LOG.debug("Attempting to assert formula '{}' in context '{}'", assertSentence, mt);
    KB_LOG.logAssertAttempt(assertSentence, mt, s, d);
    getStaticAccess().getAssertTool().assertSentence(
            assertSentence.stringApiValue(),
            mt,
            ":" + s.name(),
            ":" + d.name(),
            true,
            KbConfiguration.getShouldTranscriptOperations());
  }
  
  /**
   * Find or create an assertion using <code>factSentence</code> in the context <code>ctx</code>
   *
   * @param fact	the CycL sentence to be asserted
   * @param ctx		the context where the fact will be asserted
   * @param s   	the strength of the assertion if asserted
   * @param d		the strength of the assertion if asserted
   */
  // @TODO: Clarify the strength and direction if "found". See SEMAPI-43
  @SuppressWarnings("deprecation")
  static CycAssertion assertSentence(
          FormulaSentence assertSentence, Context ctx, Strength s, Direction d) {
    try {
      final CycAssertion foundAssertion = findAssertion(assertSentence, ContextImpl.asELMt(ctx));
      if (foundAssertion != null) {
        return foundAssertion;
      }
      final ElMt mt = getStaticAccess().getObjectTool().makeElMt(ctx.getCore());
      makeAssertion(assertSentence, mt, s, d);
      final CycAssertion newAssertion = findAssertion(assertSentence, mt);
      KB_LOG.logAssertResult(assertSentence, mt, s, d, newAssertion);
      return newAssertion;
    } catch (CycConnectionException ex) {
      throw new KbRuntimeException(ex.getMessage(), ex);
    }
  }
  
  /*
  // TODO: review these methods - nwinant, 2017-07-19
  
  public static boolean createAssertion(
          FormulaSentence assertSentence, Context ctx, Strength s, Direction d) {
    try {
      final ElMt mt = getStaticAccess().getObjectTool().makeElMt(ctx.getCore());
      final CycAssertion foundAssertion = findAssertion(assertSentence, mt);
      if (foundAssertion != null) {
        return false;
      }
      makeAssertion(assertSentence, mt, s, d);
      return true; // If the asserting doesn't throw an exception, it was successful.
    } catch (CycConnectionException exception) {
      throw new KbRuntimeException(exception.getMessage(), exception);
    }
  }

  public static boolean createAssertion(String factStr, String ctxStr, Strength s, Direction d)
          throws KbTypeException, CreateException {
    try {
      final FormulaSentence factSentence
              = FormulaSentenceImpl.makeCycSentence(getStaticAccess(), factStr);
      final Context ctx = ContextImpl.get(ctxStr);
      return createAssertion(factSentence, ctx, s, d);
    } catch (CycConnectionException exception) {
      throw new KbRuntimeException(exception.getMessage(), exception);
    }
  }

  public static boolean createAssertion(String factStr, String ctxStr)
          throws KbTypeException, CreateException {
    return createAssertion(factStr, ctxStr, Strength.AUTO, Direction.AUTO);
  }
  */
  
  /**
   * find or create a fact using factStr in the context represented by ctxStr
   * <p>
   *
   * @param factStr	the CycL string of the fact to be asserted
   * @param ctxStr	the string representing the context where the fact will be
   * asserted
   * @param s   	the strength of the assertion if asserted
   * @param d		the strength of the assertion if asserted
   *
   * @throws CreateException 
   * @throws KbTypeException 
   * 
   * @TODO: Clarify the strength and direction if "found". See SEMAPI-43
   */
  static CycAssertion assertSentence(String factStr, String ctxStr, Strength s, Direction d) 
      throws KbTypeException, CreateException {
    try {
      final FormulaSentence factSentence
              = FormulaSentenceImpl.makeCycSentence(getStaticAccess(), factStr);
      final Context ctx = ContextImpl.get(ctxStr);
      return assertSentence(factSentence, ctx, s, d);
    } catch (CycConnectionException exception) {
      throw new KbRuntimeException(exception.getMessage(), exception);
    }
  }
  
  /**
   * Returns <code>true</code> if Cyc can prove a FormulaSentence in an Mt, regardless of whether it
   * formally exists as an assertion.
   * 
   * @see AssertionImpl#TREAT_EXTERNAL_SKS_KNOWLEDGE_AS_ASSERTIONS for more background on this.
   * 
   * @param assertSentence
   * @param mt
   * @return 
   */
  static boolean isSentenceTriviallyProvable(FormulaSentence assertSentence, ElMt mt) {
    try {
      // TODO: Revise this implementation - nwinant, 2017-05-04
      final InferenceParameters params = new DefaultInferenceParameters(getStaticAccess())
              .setMaxTransformationDepth(0)
              .setInferenceMode(OpenCycInferenceParameterEnum.OpenCycInferenceMode.MINIMAL_MODE)
              .setMaxTime(3);
      return getStaticAccess().getInferenceTool().isQueryTrue(assertSentence, mt, params);
    } catch (CycConnectionException ex) {
      throw new KbRuntimeException(ex.getMessage(), ex);
    }
  }
  
  static boolean isSentenceTriviallyProvable(Sentence formula, Context ctx) {
    return AssertionImpl.isSentenceTriviallyProvable(FormulaSentence.class.cast(formula.getCore()), ContextImpl.asELMt(ctx));
  }
  
  static boolean isSentenceKnown(String formulaStr, String ctxStr)
          throws KbTypeException, CreateException {
    try {
      final FormulaSentence formulaSentence = FormulaSentenceImpl
              .makeCycSentence(getStaticAccess(), formulaStr);
      final Context ctx = ContextImpl.get(ctxStr);
      return AssertionImpl.isSentenceTriviallyProvable(formulaSentence, ContextImpl.asELMt(ctx));
    } catch (CycConnectionException exception) {
      throw new KbRuntimeException(exception.getMessage(), exception);
    }
  }
  
  static boolean isSentenceKnown(Object formula, Object ctx)
          throws KbTypeException, CreateException {
    if ((formula instanceof FormulaSentence) && (ctx instanceof ElMt)) {
      return AssertionImpl.isSentenceTriviallyProvable((FormulaSentence) formula, (ElMt) ctx);
    }
    if ((formula instanceof Sentence) && (ctx instanceof Context)) {
      return AssertionImpl.isSentenceTriviallyProvable((Sentence) formula, (Context) ctx);
    }
    if ((formula instanceof String) && (ctx instanceof String)) {
      return AssertionImpl.isSentenceKnown((String) formula, (String) ctx);
    }
    throw new ClassCastException("Type of formula and/or context is invalid");
  }
    
  static <O extends KbObjectImpl> O convertToFoundAssertion(
          CycAssertion result,
          Object formula, 
          Object context,
          Class<O> resultClazz,
          boolean treatExternalSksKnowledgeAsAssertions) 
          throws KbObjectNotFoundException, KbTypeException, CreateException {
    if (result != null) {
      return KbObjectImplFactory.get(result, resultClazz);
    }
    if (!treatExternalSksKnowledgeAsAssertions 
            || !AssertionImpl.isSentenceKnown(formula, context)) {
      throw new KbObjectNotFoundException(
            "Could not find an assertion: " + formula + " in context: " + context);
    }
    return null;
  }
  
  static <O extends KbObjectImpl> O convertToFoundAssertion(
          CycAssertion result, 
          Object formula, 
          Object context, 
          Class<O> resultClazz) 
          throws KbObjectNotFoundException, KbTypeException, CreateException {
    return convertToFoundAssertion(
            result, formula, context, resultClazz, TREAT_EXTERNAL_SKS_KNOWLEDGE_AS_ASSERTIONS);
  }
  
  static <O extends KbObjectImpl> O convertToFoundOrCreatedAssertion(
          CycAssertion result,
          Object formula,
          Object context,
          Class<O> resultClazz,
          boolean treatExternalSksKnowledgeAsAssertions)
          throws InvalidFormulaInContextException, KbTypeException, CreateException {
    if (result != null) {
      return KbObjectImplFactory.get(result, resultClazz);
    }
    if (!treatExternalSksKnowledgeAsAssertions
            || !AssertionImpl.isSentenceKnown(formula, context)) {
      throw new InvalidFormulaInContextException(
              "Could not find or create the assertion: " + formula + " in context: " + context);
    }
    return null;
  }
  
  static <O extends KbObjectImpl> O convertToFoundOrCreatedAssertion(
          CycAssertion result,
          Object formula,
          Object context, 
          Class<O> resultClazz)
          throws InvalidFormulaInContextException, KbTypeException, CreateException {
    return convertToFoundOrCreatedAssertion(
            result, formula, context, resultClazz, TREAT_EXTERNAL_SKS_KNOWLEDGE_AS_ASSERTIONS);
  }
  
  @Override
  public Assertion addQuotedIsa(KbCollection coll, Context ctx) throws KbTypeException, CreateException {
    super.addQuotedIsa(coll, ctx);
    return this;
  }
  
  @Deprecated
  public void delete(boolean force) throws DeleteException {
    if (!force) {
      delete();
    } else {
      try {
        getAccess().getUnassertTool().blastAssertion(
                (CycAssertion) this.getCore(),
                true,
                KbConfiguration.getShouldTranscriptOperations());
        setIsValid(false);
      } catch (CycConnectionException | CycApiException ex) {
        LOG.warn("Unable to forcefully delete assertion {}", this);
        throw new KbRuntimeException("Couldn't forcefully delete fact: " + getCore().toString(), ex);
      }
    }
  }
  
  @Override
  public void delete() throws DeleteException {
    final CycAssertion ca = getCore();
    try {
      getAccess().getUnassertTool()
              .unassertAssertion(ca, true, KbConfiguration.getShouldTranscriptOperations());
      setIsValid(false);
    } catch (CycConnectionException ex) {
      throw new KbRuntimeException(
              "Couldn't delete the fact: " + getCore().toString(), ex);
    }
    try {
      if (findAssertion(
              ca.getELFormula(getAccess()),
              getAccess().getObjectTool().makeElMt(ca.getMt())) instanceof CycAssertion) {
        LOG.error("Unable to delete assertion: {}", ca);
        throw new DeleteException("Unable to delete assertion: " + ca);
      }
    } catch (CycConnectionException ex) {
      throw new KbRuntimeException("Couldn't delete the fact: " + getCore().toString(), ex);
    }
  }
  
  /*
  @Override
  public void delete() throws DeleteException {
    try {
      if (core instanceof Fort) {
        getAccess().getUnassertTool().kill((Fort) core, true, KbConfiguration.getShouldTranscriptOperations());
        setIsValid(false);
      } / *
       * else if (core instanceof CycAssertion) { CycAssertion ca =
       * (CycAssertion) core; if (ca.isGaf()){
       * cyc.unassertGaf(ca.getGaf(), ca.getMt()); } else { throw new
       * Exception ("Couldn't delete the fact: " + getCore().toString()); } }
       * / else {
        throw new DeleteException("Couldn't kill: "
                + getCore().toString()
                + ". It was not a Fort.");
      }
    } catch (CycConnectionException e) {
      throw new KbRuntimeException(
              "Couldn't kill the constant " + getCore().toString(), e);
    } catch (CycApiException cae) {
      throw new KbRuntimeException("Could not kill the constant: " + core
              + " very likely because it is not in the KB. " + cae.getMessage(), cae);
    }
  }
  */
  
}
