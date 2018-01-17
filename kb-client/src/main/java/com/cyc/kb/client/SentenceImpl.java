package com.cyc.kb.client;

/*
 * #%L
 * File: SentenceImpl.java
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
import com.cyc.base.cycobject.CycConstant;
import com.cyc.base.cycobject.CycList;
import com.cyc.base.cycobject.CycObject;
import com.cyc.base.cycobject.CycVariable;
import com.cyc.base.cycobject.FormulaSentence;
import com.cyc.base.cycobject.Guid;
import com.cyc.base.cycobject.Naut;
import com.cyc.base.exception.CycApiException;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.baseclient.CommonConstants;
import com.cyc.baseclient.cycobject.CycConstantImpl;
import com.cyc.baseclient.cycobject.DefaultCycObjectImpl;
import com.cyc.baseclient.cycobject.FormulaSentenceImpl;
import com.cyc.baseclient.cycobject.NautImpl;
import com.cyc.baseclient.datatype.DateConverter;
import com.cyc.kb.ArgPosition;
import com.cyc.kb.ArgUpdate;
import com.cyc.kb.Assertion;
import com.cyc.kb.Context;
import com.cyc.kb.KbObject;
import com.cyc.kb.KbTerm;
import com.cyc.kb.Quantifier;
import com.cyc.kb.Relation;
import com.cyc.kb.Sentence;
import com.cyc.kb.Variable;
import com.cyc.kb.client.quant.QuantifiedRestrictedVariable;
import com.cyc.kb.client.quant.RestrictedVariable;
import com.cyc.kb.exception.CreateException;
import com.cyc.kb.exception.KbException;
import com.cyc.kb.exception.KbRuntimeException;
import com.cyc.kb.exception.KbTypeException;
import com.cyc.session.exception.SessionCommunicationException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.cyc.kb.client.KbObjectImpl.getStaticAccess;

/**
 * A <code>Sentence</code> object is a Java representation of a syntactically well-formed
 * <code>#$CycLSentence</code>.
 *
 * {@link com.cyc.kb.KbTerm Denotational terms} (including terms formed by
 * {@link com.cyc.kb.KBFunction functions}) are not sentences, but can be included as elements of
 * sentences. A syntactically well-formed Sentence consists of a {@link Relation} followed by any
 * number of additional terms, which could be denotational terms or additional sentences. A sentence
 * need not obey the arity restrictions of the top-level Relation in order to be syntactically
 * well-formed. In other words, the only restriction that must be obeyed to construct a sentence is
 * that there must be a relation in the 0th position.
 *
 * Unlike most of the objects in the KB API which correspond directly to an object on the Cyc
 * server, sentences are merely combinations of objects that exist on the server, but the
 * combination itself need not correspond to a server-side object. They can, however, still be used
 * and understood by the server, and are used extensively to perform queries or make assertions.
 *
 * @author Vijay Raj
 * @version $Id: SentenceImpl.java 176267 2017-12-13 04:02:46Z nwinant $
 * @since 1.0
 */
public class SentenceImpl extends PossiblyNonAtomicKbObjectImpl<FormulaSentence> implements Sentence {
  
  //====|    Static methods    |==================================================================//
  
  /**
   * Conjoin sentences. Creates a list and calls {@link #and(java.lang.Iterable)}
   *
   * @param sentences list of sentences to be conjoined
   *
   * @return a new conjoined sentence
   * @throws KbTypeException
   * @throws com.cyc.kb.exception.CreateException
   */
  public static Sentence and(Sentence... sentences) throws KbTypeException, CreateException {
    return and(Arrays.asList(sentences));
  }

  /**
   * Conjoin sentences. Creates a new sentence with #$and as the relation and all other sentences as
   * the arguments.
   *
   * @param sentences list of sentences to be conjoined
   *
   * @return a new conjunction sentence
   * @throws com.cyc.kb.exception.KbTypeException
   * @throws com.cyc.kb.exception.CreateException
   */
  public static Sentence and(Iterable<Sentence> sentences) throws KbTypeException, CreateException {
    List<FormulaSentence> cfsList = new ArrayList<>();
    for (Sentence s : sentences) {
      cfsList.add(toCycSentence(s));
    }
    final FormulaSentence cfs = FormulaSentenceImpl.makeConjunction(cfsList);
    // TODO: Can we catch KBTypeException. We know all components are Sentences.
    // combination should be a Sentence
    return new SentenceImpl(cfs);
  }

  /**
   * Disjoin sentences. Creates a list and calls {@link #or(java.lang.Iterable)}
   *
   * @param sentences list of sentences to be disjoined
   *
   * @return a new disjunction sentence
   * @throws KbTypeException
   * @throws com.cyc.kb.exception.CreateException
   */
  public static Sentence or(Sentence... sentences) throws KbTypeException, CreateException {
    return or(Arrays.asList(sentences));
  }

  /**
   * Disjoin sentences. Creates a new sentence with #$or as the relation and all other sentences as
   * the arguments.
   *
   * @param sentences list of sentences to be disjoined
   *
   * @return a new disjunction sentence
   * @throws com.cyc.kb.exception.KbTypeException
   * @throws com.cyc.kb.exception.CreateException
   */
  public static Sentence or(Iterable<Sentence> sentences) throws KbTypeException, CreateException {
    List<FormulaSentence> cfsList = new ArrayList<>();
    for (Sentence s : sentences) {
      cfsList.add(toCycSentence(s));
    }
    final FormulaSentence cfs = FormulaSentenceImpl.makeDisjunction(cfsList);
    // TODO: Can we catch KBTypeException. We know all components are Sentences.
    // combination should be a Sentence    
    return new SentenceImpl(cfs);
  }

  public static Sentence implies(Collection<Sentence> posLiterals, Sentence negLiteral) throws KbTypeException, CreateException {
    return implies(and(posLiterals), negLiteral);
  }

  public static Sentence implies(Sentence posLiteral, Sentence negLiteral) throws KbTypeException, CreateException {
    final FormulaSentence conditional = FormulaSentenceImpl.makeConditional((FormulaSentence) posLiteral.getCore(), (FormulaSentence) negLiteral.getCore());
    return new SentenceImpl(conditional);
  }
  
  /**
   * Return the KBCollection as a KBObject of the Cyc term that underlies this class
   * (<code>CycLSentence</code>).
   *
   * @return KBCollectionImpl.get("#$CycLSentence");
   */
  public static KbObject getClassType() {
    try {
      return KbCollectionImpl.get(getClassTypeString());
    } catch (KbException kae) {
      throw KbRuntimeException.fromThrowable(kae);
    }
  }

  static String getClassTypeString() {
    return "#$CycLSentence";
  }
  
  private static FormulaSentenceImpl toCycSentence(Sentence sentence) {
    return (FormulaSentenceImpl) sentence.getCore();
  }
  
  /**
   * Build a FormulaSentence from the given KBObjects arguments <code>args</code>. Note that
   * <code>args</code> should either be KBObjects or Java classes, String, Number or Date. This
   * method also handles java.util.List and java.util.Set of other supported KB API or Java objects.
   * It even supports, List of List etc.
   *
   * @param args
   *
   * @return a FormulaSentence corresponding to the arguments <code>args</code>.
   */
  public static FormulaSentence convertKBObjectArrayToCycFormulaSentence(Object... args) {
    //this should never actually happen, but when it does, this is what we should do
    if (args.length == 1 && args[0] instanceof FormulaSentence) {
      return (FormulaSentence) args[0];
    }
    List<Object> outargs = new ArrayList<>();
    List<Object> tempoutargs = new ArrayList<>();
    try {
      for (Object arg : args) {
        if (arg instanceof RestrictedVariable) {
          tempoutargs.add(((RestrictedVariable) arg).getVariable().getCore());
          if (outargs.isEmpty()) {
            outargs.add(CommonConstants.AND);
          }
          outargs.add(convertKBObjectArrayToCycFormulaSentence(((RestrictedVariable) arg).getSentenceArguments().toArray()));
        } else if (arg instanceof QuantifiedRestrictedVariable) {

        } else if (arg instanceof KbObject) {
          tempoutargs.add(((KbObject) arg).getCore());
        } else if (arg instanceof List) {
          if (((List) arg).isEmpty()) {
            tempoutargs.add(THE_EMPTY_LIST);
          } else {
            FormulaSentence cfs = convertKBObjectArrayToCycFormulaSentence(((List<?>) arg).toArray());
            Naut cn = new NautImpl(getStaticAccess().getLookupTool().getKnownFortByName(
                    "TheList"), cfs.toCycList().toArray());
            tempoutargs.add(cn);
          }
        } else if (arg instanceof Set) {
          FormulaSentence cfs = convertKBObjectArrayToCycFormulaSentence(((Set<?>) arg).toArray());
          Naut cn = new NautImpl(getStaticAccess().getLookupTool().getKnownFortByName(
                  "TheSet"), cfs.toCycList().toArray());
          tempoutargs.add(cn);
        } else if (arg instanceof Date) {
          DateConverter.getInstance();
          CycObject co = DateConverter.toCycDate((Date) arg);
          tempoutargs.add(co);
        } else {
          tempoutargs.add(arg);
        }
      }
    } catch (CycConnectionException e) {
      // Low level connection exception
      throw KbRuntimeException.fromThrowable(e);
    }

    if (outargs.isEmpty()) {
      outargs.addAll(tempoutargs);
    } else {
      outargs.add(FormulaSentenceImpl.makeFormulaSentence(tempoutargs.toArray()));
    }

    return FormulaSentenceImpl.makeFormulaSentence(outargs.toArray());
  }
  
  /**
   * Introducing a static method to change the Exception thrown to KBApiException instead of
   * CycApiException.
   *
   * @param cycLString the string to parse into a FormulaSentence
   *
   * @return a FormulaSentence represented by CycL string, <code>cycLString</code>
   *
   * @throws CreateException if cycLString can not be parsed
   */
  private static FormulaSentence parseCycLStringOrId(String cycLString) throws CreateException {
    try {
      return FormulaSentenceImpl.makeFormulaSentence(getStaticAccess(), cycLString);
    } catch (CycApiException | CycConnectionException ex) {
      try {
        Object o = DefaultCycObjectImpl.fromPossibleCompactExternalId(cycLString, getStaticAccess());
        o = FormulaSentenceImpl.convertIfPromising(o);
        if (o instanceof FormulaSentence) {
          return (FormulaSentence) o;
        }
      } catch (CycConnectionException ex1) {
        throw CreateException.fromThrowable(ex1);
      }
      throw CreateException.fromThrowable(ex);
    }
  }
  
  /**
   * Returns a new list of objects based on <code>pred</code> and other <code>args</code>. Note that
   * <code>args</code> should be KBObjects,
   * {@link java.lang.String Strings}, {@link java.lang.Number Numbers}, or
   * {@link java.util.Date Dates}. This constructor also handles {@link java.util.List Lists} and
   * {@link java.util.Set Sets} (and Lists of Lits or Sets of Lists, etc.) of those supported
   * objects.
   *
   * @param pred the first argument of the formula
   * @param args the other arguments of the formula in order
   *
   * @return a new list with the supplied arguments
   */
  private static Object[] combineParams(Relation pred, Object... args) {
    final List<Object> result = new ArrayList<>();
    result.add(pred);
    result.addAll(Arrays.asList(args));
    return result.toArray();
  }
  
  //====|    Fields    |==========================================================================//

  private static final Logger LOG = LoggerFactory.getLogger(SentenceImpl.class.getCanonicalName());

  /**
   * Used in the method {@link #convertKBObjectArrayToCycFormulaSentence(java.lang.Object...)}
   */
  private static final CycConstant THE_EMPTY_LIST = new CycConstantImpl(
          "TheEmptyList", new Guid("bd79c885-9c29-11b1-9dad-c379636f7270"));
  
  // The list of KBObject or Primitive datatypes
  // This is to preserve all the KBObjects passed in to construct the sentence.
  // This is expected to be useful when Sentence has to be reconstructed when
  // handling RestrictedVariable, since the RVs have a restriction within, that
  // gets added to the sentence. 
  /**
   * NOT PART OF KB API 1.0
   */
  // 2014-10-28: This is not populated from FormulaSentence and can be assumed to be
  // non null at any time.
  private List<Object> arguments;
  
  //====|    Construction    |====================================================================//

  /**
   * Return a new <code>Sentence</code> based on the existing FormulaSentence
   * <code>cycObject</code>.
   *
   * @param cycObject	the source CycObject for the Sentence. The constructor verifies that the
   * CycObject is a #$CycLSentence
   *
   * @throws KbTypeException is thrown in cycObject is not an instance of FormulaSentence
   * @throws com.cyc.kb.exception.CreateException
   */
  public SentenceImpl(FormulaSentence cycObject) throws KbTypeException, CreateException {
    super(cycObject);
    arguments = formulaSentenceToArgList((FormulaSentence) cycObject);
  }
  
  // @TODO: This does not support typed 
  private List<Object> formulaSentenceToArgList(FormulaSentence formula) throws CreateException {
    List<Object> tempArgList = new ArrayList<>();
    for (Object o : formula.getArgs()) {
        // NOTE: There is a recursion here.
        // checkAndCastObject calls SentenceImpl(CycObject) which inturn calls
        // this method
      tempArgList.add(KbObjectImpl.checkAndCastObject(o));
        }
    return tempArgList;
  }

  /**
   * Builds a sentence based on <code>pred</code> and other <code>args</code>. Note that
   * <code>args</code> should be KBObjects,
   * {@link java.lang.String Strings}, {@link java.lang.Number Numbers}, or
   * {@link java.util.Date Dates}. This constructor also handles {@link java.util.List Lists} and
   * {@link java.util.Set Sets} (and Lists of Lits or Sets of Lists, etc.) of those supported
   * objects.
   *
   * @param pred the first argument of the formula
   * @param args the other arguments of the formula in the order they appear in the list
   *
   * @throws KbTypeException is thrown if the built cycObject is not a instance of
 FormulaSentence. This should never happen.
   * @throws com.cyc.kb.exception.CreateException
   */
  public SentenceImpl(Relation pred, Object... args) throws KbTypeException, CreateException {
    this(combineParams(pred, args));
  }


  /**
   * Builds an arbitrary sentence based on the <code>args</code> provided. Note that
   * <code>args</code> should either be KBObjects or Java classes, String, Number or Date. This
   * constructor also handles java.util.List and java.util.Set of other supported KB API or Java
   * objects. It even supports, List of List etc.
   *
   * @param args the arguments of the formula in order
   *
   * @throws KbTypeException never thrown
   * @throws com.cyc.kb.exception.CreateException
   */
  public SentenceImpl(Object... args) throws KbTypeException, CreateException {
    this(convertKBObjectArrayToCycFormulaSentence(args));
    arguments = Arrays.asList(args);
    LOG.debug("Create sentence with args: {}", Arrays.asList(args));
  }
  
  /**
   * Attempts to convert a CycL string into a FormulaSentence and thus into a KBObject, Sentence.
   * <p>
   *
   * @param sentStr	the string representing a Sentence in the KB, a CycL sentence
   * @throws com.cyc.kb.exception.KbTypeException
   *
   * @throws CreateException if the Sentence represented by sentStr could not be parsed.
   */
  public SentenceImpl(String sentStr) throws KbTypeException, CreateException {
    this(parseCycLStringOrId(sentStr));
  }
  
  //====|    Methods    |=========================================================================//

  /*
   * Creates a
   * <code>Sentence</code> based on the pre-existing term in the Cyc KB
   * with HL ID sentHlid and the name sentHlid.
   * <p>
   * See {@link Sentence#Sentence(String)} for a way to make a new
   * predicate.
   *
   * @param sentStr	the string representing a Sentence in the KB
   * @param	sentHlid	the HLID of the implementation-dependent object
   *
   * @throws KBApiException	if the Sentence represented by
   * sentHlid and having an HLID sentHlid is not found
   * @todo what happens if the hlid and the predStr don't match? Is that also an
   * Exception, or is one or the other preferred?
   */
  //  We will either use HLID or cycObjString. There is no point using both.
  // @Deprecated //deprecated in the sense of broken, not in the sense that it shouldn't be here
  // DO WE WANT THIS AT ALL???
  /*
   public Sentence(String sentStr, String sentHlid) throws KBApiException {
   super(sentStr, sentHlid);
   this.arguments = new ArrayList<Object> ();
   throw new UnsupportedOperationException("Stub method. Not tested.");
   }
   */
  
  /**
   * This not part of the public, supported KB API. Check that the candidate core object is valid
   * FormulaSentence. In the CycKB the object would be valid #$CycLSentence
   * Refer to {@link StandardKBObject#isValidCore(com.cyc.base.cycobject.CycObject) } for more
   * comments
   *
   * @return
   */
  // NOTE: We might want to do a lenient WFF check here. But it could be very expensive
  // and unnecessary. 
  @Override
  //@todo Should this be static?  Also, why is the javadoc for this not showing up.  I'm seeing javadoc from somewhere else...
  protected boolean isValidCore(CycObject cycObject) {
    return (cycObject instanceof FormulaSentence);
  }
  
  @Override
  public boolean isAssertible(Context ctx) {
    return !getCore().hasWffConstraintViolations(getAccess(), ContextImpl.asELMt(ctx));
  }

  @Override
  public String notAssertibleExplanation(Context ctx) {
    try {
      return getCore().getNonWffAssertExplanation(getAccess(), ContextImpl.asELMt(ctx));
    } catch (Exception e) {
      LOG.error(e.getMessage());
      LOG.error(Arrays.toString(e.getStackTrace()));
      return null;
    }
  }

  @Override
  public Set<ArgPosition> getArgPositionsForTerm(Object term) {
    Set<ArgPosition> returnResult = new HashSet<>();
    if (term instanceof KbObject) {
      term = ((KbObject) term).getCore();
    }
    if (getCore() instanceof FormulaSentence) {
      Set<com.cyc.kb.ArgPosition> result = getCore().getArgPositionsForTerm(term);
      result.forEach((pos) -> {
        returnResult.add(new ArgPositionImpl(pos));
      });
    }
    return returnResult;
  }

  @Override
  public Sentence setArgPosition(ArgPosition pos, Object value) throws KbTypeException, CreateException {
    FormulaSentence existing = getCore();
    Object coreValue = value;
    if (value instanceof KbObject) {
      coreValue = ((KbObject) value).getCore();
    }
    existing.setSpecifiedObject(pos, coreValue);
    return createNewSentence(existing);
  }
  // A ENUM interface that returns modified sentences for commonly used sentence operators,
  // such as #$not and #$assertedSentence.

  public interface SentenceOperator {

    // Return a new sentence with <code>sent</code> sentence wrapped with <code>this</code>
    // enumerator.
    public Sentence wrap(Sentence sent) throws KbTypeException, CreateException;
  }

  public enum SentenceOperatorImpl implements SentenceOperator {
    /**
     * Return a new sentence with <code>sent</code> sentence wrapped with #$not. #$not in queries
     * requires that a sentence be provably false. Therefore in queries it is generally more useful
     * to specify #$unknownSentence for the sub-sentence that is required to be not-provable. Use
     * {@link #UNKNOWN} for that.
     */
    NOT(Constants.getInstance().NOT_LC),
    /**
     * Return a new sentence with <code>sent</code> sentence wrapped with #$unknownSentence. If this
     * type of sentence is part of a query sentence, the query will only be provable if
     * <code>this</code> sentence is not provable in the context.
     */
    UNKNOWN(Constants.getInstance().UNKNOWN_SENT_PRED),
    /**
     * Return a new sentence with <code>sent</code> sentence wrapped with #$assertedSentence. If
     * this type of sentence is part of a query sentence, Cyc will return true only if
     * <code>this</code> sentence is explicitly asserted in the KB, not just inferrible.
     */
    ASSERTED(Constants.getInstance().ASSERTED_SENT_PRED),
    /**
     * Return a new sentence with <code>sent</code> sentence wrapped with #$checkSentence. If this
     * type of sentence is part of a query, Cyc will ensure that <code>this</code> sentence is
     * considered for solving only when all the open variables are bound, by solving other clauses
     * of the query first.
     */
    CHECK(Constants.getInstance().CHECK_SENT_PRED);

    private final Relation unaryRel;

    /**
     * Enum constructor.
     *
     * @param unaryRel the relation use to wrap the sentence in {@link #wrap(com.cyc.kb.Sentence) }
     * argument.
     */
    SentenceOperatorImpl(Relation unaryRel) {
      this.unaryRel = unaryRel;
    }

    @Override
    public Sentence wrap(Sentence sent) throws KbTypeException, CreateException {
      return new SentenceImpl(unaryRel, sent);
    }
  }
  
  @Override
  public Assertion assertIn(Context ctx) throws KbException {
    if (this.getArgument(0).equals(LogicalConnectiveImpl.get("implies"))) {
      LOG.debug("Attempting to assert the Sentence " + this + " in Context: " + ctx + " as a rule.");
      return RuleImpl.findOrCreate(this, ctx);
    } else {
      LOG.debug("Attempting to assert the Sentence " + this + " in Context: " + ctx + " as a fact.");
      return FactImpl.findOrCreate(this, ctx);
    }
  }

  /**
   * Not part of the public, supported KB API.
   *
   * @return
   * @throws com.cyc.kb.exception.KbException
   */
  public Sentence expandSentence() throws KbException {
    List<Sentence> literals = new ArrayList<>();
    literals.add(this);
    for (Object arg : arguments) {
      if (arg instanceof KbTermImpl) {
        if (((KbTermImpl) arg).isVariable()) {
          literals.add(((KbTermImpl) arg).getRestriction());
        }
      }
    }
    if (literals.isEmpty()) {
      return this;
    } else {
      return new SentenceImpl(LogicalConnectiveImpl.get("and"), literals.toArray());
    }
  }

  // @Override
  /**
   * Not part of the public, supported KB API.
   *
   * @return
   */
  public Collection<KbTerm> getListOfTypedVariables() {
    Set<KbTerm> terms = new HashSet<>();
    for (Object arg : arguments) {
      if (arg instanceof KbTerm && ((KbTermImpl) arg).isVariable()) {
        terms.add((KbTerm) arg);
      }
    }
    return terms;
  }

  /**
   * Apply each of the arg updates specified in updates, in order.
   *
   * @param updates
   * @return
   */
  @Override
  public Sentence performUpdates(List<ArgUpdate> updates) {
    try {
      if ((updates == null) || updates.isEmpty()) {
        return this;
      }
      //@todo there should be some kind of factory so we end up with the same kind of Sentence that we started with.
      Sentence result = this;

      for (ArgUpdate update : updates) {
        switch (update.getOperation()) {
          case SET:
            result = result.setArgPosition(update.getArgPosition(), update.getValue());
            break;
          case DELETE:
            throw new UnsupportedOperationException("Support for DELETE operations is not implemented.");
          //break;
          case INSERT_AFTER:
            throw new UnsupportedOperationException("Support for INSERT_AFTER operations is not implemented.");
          //break;
          case INSERT_BEFORE:
            throw new UnsupportedOperationException("Support for INSERT_BEFORE operations is not implemented.");
          //break;
        }
      }
      return result;
    } catch (KbTypeException | CreateException ex) {
      throw new RuntimeException(ex);
    }
  }
  
  @Override
  public List<Variable> getVariables(boolean includeQueryable) throws KbException {
    final List<Variable> results = new ArrayList<>();
    final List<CycVariable> cycvars = includeQueryable
            ? getCore().findQueryableVariables()
            : getCore().findFreeVariables();
    for (CycVariable v : cycvars) {
      results.add(new VariableImpl(v));
    }
    return results;
  }
  
  @Override
  public List<KbObject> getIndexicals(boolean includeAllIndexicals)
          throws KbException, SessionCommunicationException {
    final List<KbObject> allIndexicals = new ArrayList();
    final FormulaSentence sentence = getCore();
    try {
      final CycList cyclist = sentence.findIndexicals(getAccess());
      allIndexicals.addAll(KbObjectImplFactory.asKbObjectList(cyclist));
    } catch (CycConnectionException | CycApiException ex) {
      throw ex.toSessionException();
    }
    if (includeAllIndexicals) {
      return allIndexicals;
    }
    final List<KbObject> filteredIndexicals = new ArrayList();
    for (KbObject indexical : allIndexicals) {
      try {
        indexical.resolveIndexical();
      } catch (KbTypeException ex) {
        filteredIndexicals.add(indexical); // If it errors, it's not auto-resolvable
      }
    }
    return filteredIndexicals;
  }
  
  @Override
  public List<KbObject> getIndexicals() throws KbException, SessionCommunicationException {
    return getIndexicals(false);
  }
  
  protected Sentence createNewSentence(Object... args) throws KbTypeException, CreateException {
    return new SentenceImpl(args);
  }

  protected Sentence createNewSentence(Sentence sent) throws KbTypeException, CreateException {
    return createNewSentence(sent.getCore());
  }
  
  @Override
  public Sentence replaceTerms(List<Object> from, List<Object> to) throws KbTypeException, CreateException {
    List<Object> modifiedArgument = new ArrayList<>();
    for (Object arg : arguments) {
      // If a user wants to replace an entire sentence, it is allowed.
      if (from.contains(arg)) {
        int fromIdx = from.indexOf(arg);
        modifiedArgument.add(to.get(fromIdx));
      } else if (arg instanceof Sentence) {
        Sentence modSent = ((SentenceImpl) arg).replaceTerms(from, to);
        modifiedArgument.add(modSent);
      } else {
        modifiedArgument.add(arg);
      }
    }
    return createNewSentence(modifiedArgument.toArray());
  }
  
  @Override
  public Sentence replaceTerms(Map substitutions) throws KbTypeException, CreateException {
    return (Sentence) super.replaceTerms(substitutions);
  }
  
  @Override
  public Sentence quantify(KbObject variable) throws KbTypeException, CreateException {
    if (((KbObjectImpl) variable).getKboData().containsKey("quantifier")) {
      Quantifier q = (Quantifier) ((KbObjectImpl) variable).getKboData().get("quantifier");
      return new SentenceImpl(q, variable, this);
    } else {
      return this;
    }
  }

  /**
   * Return the KBCollection as a KBObject of the Cyc term that underlies this class
   * (<code>CycLSentence</code>).
   *
   * @return KBCollectionImpl.get("#$CycLSentence");
   */
  @Override
  public KbObject getType() {
    return getClassType();
  }
  
  @Override
  String getTypeString() {
    return getClassTypeString();
  }
  
  //@todo shouldn't all of these be in the Sentence Interface, with appropriate comments, include mention that they destructively modify things?
  
  public List<Object> getArguments() {
    // Make a new list to preserve immutability
    final List<Object> copiedList = new ArrayList<>();
    // The objects themselves (KB Objects) are immutable, so it is safe to just copy
    // them. Java.util.date is mutable though.
    arguments.forEach((arg) -> {
      copiedList.add(arg);
    });
    return copiedList;
  }
  
  public void setArguments(List<Object> arguments) {
    this.arguments = arguments;
  }
  
  @Override
  public Boolean isValid() {
    for (Object arg : getArguments()) {
      boolean valid = true;
      if (arg instanceof KbTerm) {
        valid = ((KbTerm) arg).isValid();
      } else if (arg instanceof Assertion) {
        valid = ((Assertion) arg).isValid();
      } else if (arg instanceof Sentence) {
        valid = ((Sentence) arg).isValid();
      }
      if (!valid) {
        return false;
      }
    }
    return true;
  }
  
  @Override
  public <O> O getArgument(int getPos) throws KbTypeException, CreateException {
    return super.getArgument(getPos);
  }

}
