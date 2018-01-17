package com.cyc.kb.client;

/*
 * #%L
 * File: KbObjectImpl.java
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
import com.cyc.Cyc;
import com.cyc.base.CycAccess;
import com.cyc.base.CycAccessManager;
import com.cyc.base.cycobject.CycAssertion;
import com.cyc.base.cycobject.CycConstant;
import com.cyc.base.cycobject.CycList;
import com.cyc.base.cycobject.CycObject;
import com.cyc.base.cycobject.CycSymbol;
import com.cyc.base.cycobject.CycVariable;
import com.cyc.base.cycobject.DenotationalTerm;
import com.cyc.base.cycobject.Fort;
import com.cyc.base.cycobject.Guid;
import com.cyc.base.cycobject.Naut;
import com.cyc.base.exception.CycApiException;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.baseclient.CycServerInfoImpl;
import com.cyc.baseclient.cycobject.CycArrayList;
import com.cyc.baseclient.cycobject.CycConstantImpl;
import com.cyc.baseclient.cycobject.DefaultCycObjectImpl;
import com.cyc.baseclient.cycobject.FormulaSentenceImpl;
import com.cyc.baseclient.cycobject.NautImpl;
import com.cyc.baseclient.datatype.DateConverter;
import com.cyc.baseclient.subl.functions.CycEvaluateFunction.UnevaluatableExpressionException;
import com.cyc.kb.Context;
import com.cyc.kb.KbCollection;
import com.cyc.kb.KbFunction;
import com.cyc.kb.KbIndividual;
import com.cyc.kb.KbObject;
import com.cyc.kb.KbPredicate;
import com.cyc.kb.KbTerm;
import com.cyc.kb.Sentence;
import com.cyc.kb.Symbol;
import com.cyc.kb.exception.CreateException;
import com.cyc.kb.exception.KbException;
import com.cyc.kb.exception.KbRuntimeException;
import com.cyc.kb.exception.KbServerSideException;
import com.cyc.kb.exception.KbTypeException;
import com.cyc.kb.exception.VariableArityException;
import com.cyc.nl.Paraphraser;
import com.cyc.nl.ParaphraserFactory;
import com.cyc.session.CycSession;
import com.cyc.session.CycSessionManager;
import com.cyc.session.exception.SessionCommunicationException;
import com.cyc.session.exception.SessionConfigurationException;
import com.cyc.session.exception.SessionException;
import com.cyc.session.exception.SessionInitializationException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.cyc.baseclient.subl.functions.CycEvaluateFunction.CYC_EVALUATE_INDEXICAL;
import static com.cyc.baseclient.subl.functions.SublFunctions.INDEXICAL_P;
import static com.cyc.kb.KbObject.hasValidKbApiObjectType;

/**
 * The base class for all the other classes in this package. Each KBObject is
 * basically a facade for an object in the Cyc KB, and as such it provides
 * common methods to make, retrieve, and remove CycL Assertions.
 * <p>
 *
 * @param <T> type of CycObject core
 * 
 * @author Vijay Raj
 * @version "$Id: KbObjectImpl.java 175540 2017-10-26 19:59:02Z nwinant $"
 */
public class KbObjectImpl<T extends CycObject> implements KbObject {
  
  //====|    Static fields    |===================================================================//
  
  private static final Logger LOG = LoggerFactory.getLogger(KbObjectImpl.class.getCanonicalName());
  
  private static final CycConstant THE_LIST = new CycConstantImpl(
          "TheList", new Guid("bdcc9f7c-9c29-11b1-9dad-c379636f7270"));
  
  private static final CycConstant THE_EMPTY_LIST = new CycConstantImpl(
          "TheEmptyList", new Guid("bd79c885-9c29-11b1-9dad-c379636f7270"));
  
  private static final CycConstant THE_SET = new CycConstantImpl(
          "TheSet", new Guid("bd58e476-9c29-11b1-9dad-c379636f7270"));
  
  private static final CycConstant THE_EMPTY_SET = new CycConstantImpl(
          "TheEmptySet", new Guid("bdf8edae-9c29-11b1-9dad-c379636f7270"));
  
  //====|    Static factory methods    |==========================================================//
  
  /**
   * Attempts to return a CycObject (or a Java primitive object) based on the 
   * KBObject (or a Java primitive object). 
   * 
   * A CycObject or a subclass of it, is the primary representation of the BaseClient.
   * It represents the same concept in the KB as the KBObject. The CycObject is
   * useful when the user has to do something the KB API does not support. 
   * 
   * @param arg the inputs KBObject that will be converted to a CycObject
   * 
   * @return the CycObject representation of the input ARG
   */
  public static Object convertKBObjectToCycObject(Object arg) {
    if (arg instanceof KbObject) {
      return ((KbObject) arg).getCore();
    } else if (arg instanceof List) {
      if (((List) arg).isEmpty()) {
        return THE_EMPTY_LIST;
      } else {
        CycList cl = new CycArrayList();
        ((List) arg).forEach((listElem) -> {
          cl.add(convertKBObjectToCycObject(listElem));
        });
        Naut cn = new NautImpl(THE_LIST, cl.toArray());
        return cn;
      }
    } else if (arg instanceof Set) {
      if (((Set)arg).isEmpty()) {
        return THE_EMPTY_SET;
      } else {
        CycList cl = new CycArrayList();
        ((Set) arg).forEach((setElem) -> {
          cl.add(convertKBObjectToCycObject(setElem));
        });
        Naut cn = new NautImpl(THE_SET, cl.toArray());
        return cn;
      }
    } else if (arg instanceof Date) {
      DateConverter.getInstance();
      CycObject co = DateConverter.toCycDate((Date) arg);
      return co;
    } else {
      return arg;
    }
  }
  
  /**
   * Checks whether an Object could likely be converted to a valid CycList. Specifically, it checks
   * whether the object is a non-empty List whose first element is a CycConstant.
   * 
   * @param o
   * @return 
   */
  private static boolean isPotentiallyConvertibleToValidCycList(Object o) {
    return o instanceof List
           && !((List) o).isEmpty() 
           && ((List) o).get(0) instanceof CycConstant;
  }
  
  /**
   * Attempts to return an Object (expected to be of type T) for the input
   * Object <code>o</code>.
   *
   * For basic Java objects like String, Number and Date, the object is returned
   * without any modification. CycObjects are converted to KBObjects, of the
   * most specific type possible.
   *
   * @param <T>
   * @param o object to be mapped to KBObject
   *
   * @return the KBObject constructed.
   * @throws CreateException
   */
  public static <T> T checkAndCastObject(Object o) throws CreateException {
    LOG.trace("Attempting to coerce: {}", o);
    if (o instanceof CycObject) {
      return (T) KbObjectImpl.convertCycObject((CycObject) o);
    } else if (hasValidKbApiObjectType(o)) {
      return (T) o;
    } else if (isPotentiallyConvertibleToValidCycList(o)) {
      return (T) KbObjectImpl.convertCycObject(new CycArrayList((List) o));
    }
    throw new IllegalArgumentException("Unable to coerce " + o + "(" + o.getClass() + ").");
  }
  
  @Deprecated
  public static KbObjectImpl from(KbObject obj) {
    return (KbObjectImpl) obj;
  }
  
  private static Object convertCycObject(CycObject cyco) throws CreateException {
    try {
      // First try converting to a Set, List, or Date:
      if (isPotentiallyConvertibleToValidCycList(cyco)) {
        final CycList cl = (CycList) cyco;
        try {
          final KbTerm kbt = KbObjectImplFactory.findOrCreate((CycConstant) cl.get(0), KbTermImpl.class);
          if (kbt instanceof KbFunction) {
            final KbFunction kbf = (KbFunction) kbt;
            // Do not check arity if its a VariableArityFunction, since that check throws an exception
            if ((kbf.isVariableArity() || kbf.getArity() == cl.size() - 1)
                    && kbf.isUnreifiable()) {
              cyco = new NautImpl(cl);
            }
          } else if (kbt instanceof KbPredicate) {
            final KbPredicate kbp = (KbPredicate) kbt;
            // Do not check arity if its a VariableArityFunction, since that check throws an exception
            if (kbp.isVariableArity() || kbp.getArity() == cl.size() - 1) {
              cyco = new FormulaSentenceImpl(cl);
            } else if (kbt instanceof LogicalConnectiveImpl || kbt instanceof QuantifierImpl) {
              cyco = new FormulaSentenceImpl(cl);
            }
          }
        } catch (KbTypeException | CreateException | VariableArityException e) {
          // ignore and move on
        }
      }
      // handle an empty list
      if (cyco instanceof CycConstant) {
        final CycConstant c = (CycConstant) cyco;
        if (c.equals(THE_EMPTY_LIST)) {
          return new ArrayList<>();
        } else if (c.equals(THE_EMPTY_SET)) {
          return new HashSet<>();
        }
      }
      if (cyco instanceof Naut) {
        final Naut cn = (Naut) cyco;
        final DenotationalTerm functor = (cn).getFunctor();
        if (functor.equals(THE_SET) || functor.equals(THE_LIST)) {
          final Collection<Object> c = functor.equals(THE_SET) ? new HashSet<>()
                  : new ArrayList<>();
          for (Object item : cn.getArguments()) {
            // TODO: Build a KBObject out of the Object item
            c.add(KbObjectImpl.checkAndCastObject(item));
          }
          try {
            return c;
          } catch (ClassCastException ex) {
            //Guess we weren't looking for a Set/List.
          }
        } else if (shouldConvertToJavaDates() && DateConverter.isCycDate(cn)) {
          try {
            return DateConverter.parseCycDate(cn);
          } catch (ClassCastException ex) {
            System.out.println("Class Cast exception on a date.");
            //Guess we weren't looking for a Date.
          }
        }
      }
      //return convertToKBObject(cyco);
      return Cyc.getApiObject(cyco);
    } catch (KbTypeException ex) {
      throw new CreateException(ex);
    }
  }
  
  private static boolean shouldConvertToJavaDates() throws CreateException {
    try {
      return CycSessionManager.getCurrentSession().getOptions().getShouldConvertToJavaDates();
    } catch (SessionConfigurationException | SessionCommunicationException | SessionInitializationException ex) {
      throw new CreateException(ex);
    }
  }
  /*
  private static KbObject convertToKBObject(CycObject cyco) throws CreateException {
    try {
      if (cyco instanceof CycVariable) {
        return new VariableImpl((CycVariable) cyco);
      } else if (cyco instanceof CycSymbol) {
        return new SymbolImpl((CycSymbol) cyco);
      } else if (cyco instanceof CycAssertion) {
        return AssertionImpl.get(cyco);
      } else if (cyco instanceof FormulaSentence) {
        return new SentenceImpl((FormulaSentence) cyco);
      }
      // Find most specific type, convert it to that and cast to T:
      CycObject tightCol = null;
      try {
        tightCol = getStaticAccess().getInspectorTool().categorizeTermWRTApi(cyco);
      } catch (CycConnectionException cce) {
        throw new KbRuntimeException(cce.getMessage(), cce);
      }
      
      if (tightCol != null && KbObjectImplFactory.CYC_OBJECT_TO_KB_API_CLASS.get(tightCol) != null) {
        return KbObjectImplFactory.get(cyco, KbObjectImplFactory.CYC_OBJECT_TO_KB_API_CLASS.get(tightCol));
      } else {
        return KbObjectImpl.get(cyco);
      }
    } catch (KbTypeException ex) {
      throw new CreateException(ex);
    }
  }
  */
  
  //====|    Static methods    |==================================================================//
  
  @Deprecated
  public static CycObject getCore(Object obj) {
    if (obj instanceof KbObjectImpl) {
      return KbObjectImpl.from((KbObject) obj).getCore();
    }
    return (CycObject) obj;
  }
  
  /**
   * Return the KBCollection as a KBObject of the Cyc term that underlies this
   * class. For example, calling this on a <code>KBCollection</code> object will
   * return KBCollectionImpl.get("#$Collection").
   *
   * @return the KBCollection of the underlying Cyc term of the class.
   */
  public static KbObject getClassType() {
    try {
      return KbCollectionImpl.get(getClassTypeString());
    } catch (KbException kae) {
      throw new KbRuntimeException(kae.getMessage(), kae);
    }
  }

  public static enum KbUriType {
    // TODO: This could also include an API_WS, KEA, OWL, etc. - nwinant, 2015-07-03
    CYC_BROWSER
  }
  
  /**
   * Get the <code>KBObject</code> that corresponds to <code>cycObject</code>.
   * Throws exceptions if the object isn't in the KB.
   *
   * This is a very general factory method to build a KBObject from any
   * arbitrary CycObject without any kind of semantic check on the CycObject.
   * This is used as a "catch all" in the API, but otherwise it should never be
   * used.
   *
   * @param cycObject the underlying CycObject to be wrapped
   *
   * @return the KBObject wrapping the cycObject.
   *
   * @throws CreateException
   * @throws com.cyc.kb.exception.KbTypeException
   * @todo update documentation to state what this really does
   */
  @SuppressWarnings("deprecation")
  public static KbObject get(CycObject cycObject) throws CreateException, KbTypeException {
    try {
      return KbObjectImplFactory.get(cycObject, KbObjectImpl.class);
    } catch (KbTypeException te) {
      // This type is not possible, since we are not checking for a specific Cyc collection
      // Fix API if this ever happens
      throw new KbRuntimeException(te.getMessage(), te);
    }
  }

  public static KbObject get(String nameOrId) throws CreateException, KbTypeException {
    try {
      return KbObjectImplFactory.get(nameOrId, KbObjectImpl.class);
    } catch (KbTypeException te){
      // This type is not possible, since we are not checking for a specific Cyc collection
      // Fix API if this ever happens
      throw new KbRuntimeException(te.getMessage(), te);
    }
  }
  
  protected static CycAccess getStaticAccess() {
    try {
      return CycAccessManager.getCurrentAccess();
    } catch (SessionException ex) {
      throw new KbRuntimeException(ex.getMessage(), ex);
    }
  }
  
  /**
   * For a given class <code>c</code> that extends <code>KBObject</code>, return
   * the <code>KBCollection</code> that the class corresponds to.
   *
   * For Example, getBaseCycType(Context.class) will return
   * KBCollection.get("Context");
   *
   * @param c the subclass of KBObject whose underlying #$Collection is desired
   *
   * @return the KBCollection representation of underlying #$Collection backing
   * the class
   */
  static KbCollection getBaseCycType(Class<? extends KbObjectImpl> c) {
    Method getTypeString = null;
    try {
      getTypeString = c.getDeclaredMethod("getClassTypeString");
    } catch (NoSuchMethodException e) {
      throw new IllegalArgumentException(c
              + " does not have a known base Cyc type.");
    }
    try {
      return KbCollectionImpl.get((String) getTypeString.invoke((Object[]) null, (Object[]) null));
    } catch (KbException te) {
      // We expect the getTypeString return string to be in the KB since it is
      // a fundamental concept. CreateException and KBTypeException are possible
      // but can't recover from such an exception anyways.
      throw new KbRuntimeException(te.getMessage(), te);
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw new KbRuntimeException(e.getMessage(), e);
    }
  }

  /**
   * For a given class <code>c</code> that extends <code>KBObject</code>, return
   * the <code>CycObject</code>, a CycDenotationalTerm that the class
   * corresponds to.
   *
   * For Example, getBaseCycTypeCore(Context.class) will return new
   * CycConstantImpl("Individual", new
   * Guid("bd58da02-9c29-11b1-9dad-c379636f7270"))
   *
   * @param c the subclass of KBObject whose underlying #$Collection is desired
   *
   * @return the CycObject representation of underlying #$Collection backing the
   * class
   */
  static CycObject getBaseCycTypeCore(Class<? extends KbObjectImpl> c) {
    try {
      final Method getType = c.getDeclaredMethod("getClassTypeCore");
      return (CycObject) getType.invoke((Object[]) null, (Object[]) null);
    } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      // We expect NoSuchMethodException and IllegalArgumentException both of which are 
      // internal API errors. So just throw RuntimeException
      throw new KbRuntimeException(e.getMessage(), e);
    }
  }
  
  static String getClassTypeString() {
    return "#$Thing";
  }
  
  //====|    Fields    |==========================================================================//
  
  /* *
   * !!!EXPERIMENTAL!! Can change any time without notice.
   * /
  private final List<Object> quantification = new ArrayList<>();
  */
  
  /**
   * The CORE object wrapped by all KBObjects. The type of object wrapped by
   * each subclass of KBObject will be a subclass of CycObject class.
   *
   * INTERNAL DEVELOPERS: There is a strong assumption in the KB API that the
   * CycObjects are immutable. No defensive copy is made when KB API objects are
   * constructed or when returned by getCore()!!
   */
  private T core;
  
  /**
   * Set the flag to false after API provide delete operation.
   */
  //should change to true when the KBObject is deleted from the KB, or otherwise invalidated.
  private boolean isValid = true;

  /**
   * Cache for {@link #isIndexical() }
   */
  private Boolean indexical = null;
  
  //====|    Construction    |====================================================================//
  
  /**
   * Not part of the KB API. This default constructor only has the effect of
   * ensuring that there is access to a Cyc server.
   */
  KbObjectImpl() {}
  
  /**
   * Not part of the KB API. Base class constructor currently used only for unit
   * testing.
   * <p>
   *
   * @param co The <code>CycObject</code> being wrapped.
   *
   * @throws KbRuntimeException if there is a problem connecting to Cyc.
   */
  // We will not document the run time exceptions all the way up the stack.
  @Deprecated
  KbObjectImpl(CycObject co) throws KbTypeException {
    this();
    setCore(co);
  }
  
  //====|    API methods    |=====================================================================//
  
  @Override
  public String getId() {
    try {
      return DefaultCycObjectImpl.toCompactExternalId(core, getAccess());
    } catch (CycConnectionException e) {
      // DefaultCycObjectImpl.toCompactExternalId throws exception if core is
      // null
      // or not a CycObject.
      // This should never happen in our case.
      // TODO: Check for null core in the constructor. Happens in Facts
      throw new KbRuntimeException(e);
    }
    //return "";
  }
  
  /**
   * Return the KBCollection as a KBObject of the Cyc term that underlies this
   * class. For example, calling this on a <code>KBCollection</code> object will
   * return KBCollectionImpl.get("#$Collection").
   *
   * @return the KBCollection of the underlying Cyc term of the class.
   */
  @Override
  public KbObject getType() {
    return getClassType();
  }
  
  // TODO: Make sure that CycObject and all its subclasses are immutable. 
  // JIRA: BASEAPI-17
  @Override
  public T getCore() {
    return core;
  }
  
  @Override
  public Boolean isAtomic() {
    return this.getCore() instanceof CycConstant
            || this.getCore() instanceof CycVariable
            || this.getCore() instanceof CycSymbol;
  }
  
  @Override
  public Boolean isAssertion() {
    return this.getCore() instanceof CycAssertion;
  }
  
  @Override
  public Boolean isCollection() {
    return this.getCore() instanceof KbCollection;
  }
  
  @Override
  public Boolean isContext() {
    return this.getCore() instanceof Context;
  }
  
  @Override
  public Boolean isFunction() {
    return this.getCore() instanceof KbFunction;
  }
  
  @Override
  public Boolean isIndividual() {
    return this.getCore() instanceof KbIndividual;
  }
  
  @Override
  public Boolean isPredicate() {
    return this.getCore() instanceof KbPredicate;
  }

  @Override
  public Boolean isSentence() {
    return this.getCore() instanceof Sentence;
  }

  @Override
  public Boolean isSymbol() {
    return this.getCore() instanceof Symbol;
  }

  @Override
  public Boolean isTerm() {
    return this.getCore() instanceof KbTerm;
  }
  
  @Override
  public Boolean isVariable() {
    return this.getCore() instanceof CycVariable;
  }
  
  @Override
  public Collection<KbCollection> getQuotedIsa() {
    //return this.<KbCollection>getValues(Constants.quotedIsa(), 1, 2, Constants.inferencePSCMt());
    return Constants.quotedIsa().getValuesForArgPosition(this, 1, 2, Constants.inferencePSCMt());
  }
  
  @Override
  public boolean isQuotedInstanceOf(KbCollection col) {
    try {
      return getAccess().getInspectorTool().isQuotedIsa(this.getCore(), (Fort) col.getCore());
    } catch (CycConnectionException ioe) {
      throw new KbRuntimeException(ioe.getMessage(), ioe);
    }
  }
  
  @Override
  public boolean isQuotedInstanceOf(String colStr) {
    return isQuotedInstanceOf(KbUtils.getKBObjectForArgument(colStr, KbCollectionImpl.class));
  }
  
  @Override
  public boolean isQuotedInstanceOf(KbCollection col, Context ctx) {
    try {
      return getAccess().getInspectorTool().isQuotedIsa(this.getCore(), getCore(col), getCore(ctx));
    } catch (CycConnectionException ioe) {
      throw new KbRuntimeException(ioe.getMessage(), ioe);
    }
  }
  
  @Override
  public boolean isQuotedInstanceOf(String colStr, String ctxStr) {
    return isQuotedInstanceOf(
            KbUtils.getKBObjectForArgument(colStr, KbCollectionImpl.class), 
            KbUtils.getKBObjectForArgument(ctxStr, ContextImpl.class));
  }
  
  @Override
  public boolean isIndexical() throws SessionCommunicationException {
    // TODO: should we cache this value? - nwinant, 2017-07-05
    if (this.indexical == null) {
      try {
        this.indexical = INDEXICAL_P.eval(getAccess(), getCore());
      } catch (CycConnectionException | CycApiException ex) {
        throw ex.toSessionException();
      }
    }
    return this.indexical;
  }
  
  @Override
  public <O> O resolveIndexical()
          throws SessionCommunicationException, KbTypeException, CreateException {
    try {
      final Object referent = CYC_EVALUATE_INDEXICAL.eval(getAccess(), getCore());
      return (O) KbObjectImpl.checkAndCastObject(referent);
    } catch (UnevaluatableExpressionException ex) {
      final String msg = isIndexical()
              ? "Cannot resolve indexical " + this
              : "Cannot resolve; is not an indexical: " + this;
      throw new KbTypeException(msg, ex);
    } catch (CycConnectionException | CycApiException ex) {
      throw ex.toSessionException();
    }
  }
  
  @Override
  public <O> O possiblyResolveIndexical(Map<KbObject, Object> substitutions)
          throws SessionCommunicationException, KbTypeException {
    final Object referent = substitutions.get(this);
    if (referent != null) {
      if (!isIndexical()) {
        throw new KbTypeException("Found referent for non-indexical: " + this + " -> " + referent);
      }
      if (this.equals(referent)) {
        throw new KbTypeException(
                "Indexical cannot have itself as a referent: " + this + " -> " + referent);
      }
      LOG.debug("Found possible referent for {} -> {}", this, referent);
      try {
        return (O) referent;
      } catch (ClassCastException ex) {
        throw new KbTypeException(
                "Referent is not of expected class: " + this + " -> " + referent, ex);
      }
    }
    if (isIndexical()) {
      try {
        resolveIndexical();
      } catch (KbTypeException | CreateException ex) {
        throw new KbTypeException("Indexical is not resolvable or auto-resolvable: " + this, ex);
      }
    }
    return (O) this;
  }
  
  @Override
  public boolean isQuoted() throws KbTypeException, CreateException {
    return false;
  }
  
  @Override
  public KbIndividual quote() throws KbTypeException, CreateException {
    return Constants.getInstance().QUOTE_FUNC
            .findOrCreateFunctionalTerm(KbIndividualImpl.class, this);
  }
  
  @Override
  public <O> O unquote() throws KbTypeException, CreateException {
    throw new KbTypeException("This object is not quoted, so it cannot be unquoted: " + this);
  }
  
  @Override
  public KbIndividual toQuoted() throws KbTypeException, CreateException {
    return !isQuoted() ? quote() : (KbIndividual) this;
  }
  
  @Override
  public <O> O toUnquoted() throws KbTypeException, CreateException {
    Object result = this;
    while ((result instanceof KbObject) && ((KbObject) result).isQuoted()) {
      result = ((KbObject) result).unquote();
    }
    return (O) result;
  }
  
  /**
   * This method returns a getSentenceService with the type restriction of the KbObject or
 its subclasses, even the ones extended outside of KB API. For example, the
   * restriction for a KbPredicate object would be {@code (isa <THIS PRED> #$Predicate)}.
   *
   * This is most useful for building sentences of KB Object typed-variables,
   * for use in rules and queries.
   * 
   * @todo Consider promoting this to the public API, perhaps after a little cleanup - nwinant, 2017-07-26
   *
   * @return  the restriction getSentenceService for the class
   */
  public Sentence getRestriction() {
    try {
      return isVariable()
              ? new SentenceImpl(Constants.isa(), this, this.getType())
              : null;
    } catch (KbTypeException | CreateException kte) {
      throw new KbRuntimeException(kte.getMessage(), kte);
    }
  }
  
  @Override
  public String stringApiValue() {
    return core.stringApiValue();
  }
  
  @Override
  public String toString() {
    return core.toString();
  }
  
  @Override
  public String toNlString() throws SessionException {
    Paraphraser p = ParaphraserFactory
            .getInstance(ParaphraserFactory.ParaphrasableType.KBOBJECT);
    return p.paraphrase(this).getString();
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((core == null) ? 0 : core.hashCode());
    return result;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    KbObjectImpl other = (KbObjectImpl) obj;
    if (core == null) {
      if (other.core != null) {
        return false;
      }
    } else if (!core.equals(other.core)) {
      return false;
    }
    return true;
  }
  
  @Override
  public boolean equalsSemantically(Object object) {
    if (this == object) {
      return true;
    }
    if (object == null || !(object instanceof KbObjectImpl)) {
      return false;
    }
    KbObjectImpl other = (KbObjectImpl) object;
    if (core == null) {
      if (other.core != null) {
        return false;
      }
    } else if (!core.equals(other.core)) {
      return false;
    }
    return true;
  }
  
  //====|    Implementation methods    |==========================================================//
  
  /**
   * Retrieves the current CycSession.
   * @return CycSession
   * @throws KbRuntimeException if there is a problem retrieving the current CycSession.
   */
  protected CycSession getSession() {
    try {
      return CycSessionManager.getCurrentSession();
    } catch (SessionConfigurationException | SessionCommunicationException | SessionInitializationException ex) {
      throw new KbRuntimeException("Encountered a problem with the current CycSession.", ex);
    }
  }
  
  /**
   * Retrieves the current CycAccess.
   * @return CycAccess
   * @throws KbRuntimeException if there is a problem connecting to Cyc.
   */
  protected CycAccess getAccess() {
    try {
      return CycAccessManager.getAccessManager().fromSession(getSession());
    } catch (Exception ex) {
      throw new KbRuntimeException("Encountered a problem with the current CycAccess.", ex);
    }
  }
  
  /* *
   * Gets the asserted facts visible from <code>ctx</code>, using the predicate
   * <code>pred</code>, with <code>matchArg</code> at the position
   * <code>matchArgPos</code> of the getFactService. Ignores <code>this</code> object.
   * <p>
   *
   * @param pred	the Predicate of the returned getFactService
   * @param matchArg	the Object in the matchArgPos
   * @param matchArgPos
   * @param ctx the Context. If null, returns facts from the default getContextService
   * {@link KBAPIDefaultContext#forQuery()}
   *
   * @return a collection of facts, empty if none are found
   * /
  @SuppressWarnings("deprecation")
  protected Collection<Fact> getFacts(KbPredicate pred, KbObject matchArg, int matchArgPos, Context ctx) {
    try {
      final String ctxStr = (ctx == null) ? KbConfiguration.getDefaultContext().forQuery().stringApiValue() //"#$BaseKB"
              : ctx.stringApiValue();
      String command = "(" + SublConstants.getInstance().withInferenceMtRelevance.stringApiValue() + " " + ctxStr
              + " (" + SublConstants.getInstance().gatherGafArgIndex.stringApiValue() + " "
              + matchArg.stringApiValue() + " " + matchArgPos + " "
              + pred.stringApiValue() + "))";
      
      LOG.trace("getfacts: {}", command);
      Object res = getAccess().converse().converseObject(command);
      LOG.trace("getfacts response: {}", res);
      Set<Fact> facts = new HashSet<>();
      if (!CycObjectFactory.nil.equals(res)) {
        CycList<CycAssertion> assertList = (CycList<CycAssertion>) res;
        for (Object o : assertList) {
          if (o instanceof CycAssertion) {
            try {
              facts.add(FactImpl.get((CycAssertion) o));
            } catch (KbException kbe) {
              // Nothing to do. We did get the facts we are building
              // but something went wrong. Just don't add it to the list.
            }
          }
        }
      }
      return facts;
    } catch (CycConnectionException ex) {
      throw new KbRuntimeException(ex);
    } catch (CycApiException ex) {
      throw new KbRuntimeException(ex.getMessage(), ex);
    }
  }
  */
  
  /*
  // (Quantifier OTHER_OPTIONAL_VARS Sentence)
  public List<Object> getQuantification() throws KbException {
    if (this.isVariable()) {
      // By default return "some" (#$thereExits) quantifer for object
      if (this.quantification.isEmpty()) {
        return new ArrayList<>();
        /*
         List<Object> nl = new ArrayList<Object>();
         nl.add(Quantifier.get("thereExists"));
         nl.add(this);
         return nl;
         * /
      } else {
        List<Object> nl = new ArrayList<>();
        nl.addAll(this.quantification);
        nl.add(this);
        return nl;
      }
      /*
       List<Object> l = new ArrayList<Object>();
       l.add(Predicate.get("thereExists"));
       l.add(this);
       return l;
       * /
    } else {
      return new ArrayList<>();
    }
  }
  */
  
  /*
  // In the interest of making immutable objects we will not 
  // implement setQuantification()
  public void setQuantification() {
    throw new UnsupportedOperationException();
  }
  */
  
  /**
   * This not part of the public, supported KB API. Check that the candidate core
   * object is valid for this type. This is part of the bootstrapping process,
   * where we check the core before we build a KB Object. At this point, we can 
   * not use any KB API methods or {@link #getCore()}.
   *
   * This checks the validity of only KBTerm subclasses. The core validity for
   * Assertion and its subclasses, Sentence, Variable and
   * Symbol are handled by the individual classes
   *
   * If a Variable is wrapped by any subclass of KBTerm, then it is
   * always considered valid core. This is to allow representing a
   * #$CycLDenotationalTerm and #$CycLVariable of a given type
   * (#$Collection) using same KBTerm subclass.
   *
   * @param cycObject
   *          the object that is checked for type given by getTypeString()
   *
   * @return if a cycObject is of a given type
   *
   * @throws KbRuntimeException
   *           if there is a problem connecting to Cyc
   * @throws KbServerSideException
   *           if there is problem executing a SubL command
   *
   * NOTE: This does not throw KBTypeException just because there is a
   *           server error.
   */
  protected boolean isValidCore(CycObject cycObject) throws KbRuntimeException,
          KbServerSideException {
    try {
      if (cycObject instanceof CycVariable) {
        return true;
      } else {
        String command = "(" + SublConstants.getInstance().quickQuietHasTypeQ.stringApiValue() 
                + " " + cycObject.stringApiValue() + " " + getTypeString() + ")";
        return CycAccessManager.getCurrentAccess().converse().converseBoolean(command);
      }
    } catch (CycApiException e) {
      throw new KbServerSideException(e.getMessage(), e);
    } catch (CycConnectionException | SessionConfigurationException | SessionCommunicationException | SessionInitializationException e) {
      throw new KbRuntimeException(e.getMessage(), e);
    }
  }
  
  /**
   * Checks if a given cycObject is a valid core for a class type given by
   * getTypeString() and sets the core.
   *
   * @param cycObject the object to be checked and assigned
   *
   * @throws KbTypeException if cycObject does not satisfy the type
   */
  void setCore(CycObject cycObject) throws KbTypeException {
    if (isValidCore(cycObject)) {
      core = (T) cycObject;
    } else {
      String msg = "The term \"" + cycObject.toString() + "\" is not a "
              + getTypeString() + ".";
      LOG.trace(msg);
      throw new KbTypeException(msg);
    }
  }
  
  public Boolean isValid() {
    return isValid;
  }
  
  /**
   * Package private method to set the validity of object from subclasses, for
   * example Assertion class.
   *
   * @param valid
   */
  void setIsValid(boolean valid) {
    isValid = valid;
  }
  
  String getTypeString() {
    return getClassTypeString();
  }
  
  private KbCollection typeCore = null;
  
  private Map<String, Object> kboData = new HashMap<>();

  public KbCollection getTypeCore() {
    return typeCore;
  }

  public void setTypeCore(KbCollection typeCore) {
    this.typeCore = typeCore;
  }

  public Map<String, Object> getKboData() {
    return kboData;
  }

  public void setKboData(Map<String, Object> kboData) {
    this.kboData = kboData;
  }
  
  public URI toURI(KbUriType urlType) throws URISyntaxException {
    // TODO: This should be moved somewhere else. It could be added to the KBObject interface
    //       in the Core API Spec, or moved altogether to the KM API. - nwinant, 2015-07-03
    final CycServerInfoImpl serverInfo = (CycServerInfoImpl) getAccess().getServerInfo();
    if (KbUriType.CYC_BROWSER.equals(urlType)) {
      return new URI(serverInfo.getBaseBrowserUrl() + "/cgi-bin/cg?cb-cf&" + this.getId());
    }
    throw new KbRuntimeException("No such " + KbUriType.class.getSimpleName() + " known: " + urlType);
  }
  
}
