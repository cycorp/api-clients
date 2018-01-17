package com.cyc.kb.client;

/*
 * #%L
 * File: KbPredicateImpl.java
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
import com.cyc.base.cycobject.DenotationalTerm;
import com.cyc.base.cycobject.Fort;
import com.cyc.base.cycobject.Guid;
import com.cyc.base.exception.CycApiException;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.baseclient.CycObjectFactory;
import com.cyc.baseclient.cycobject.CycConstantImpl;
import com.cyc.kb.Context;
import com.cyc.kb.Fact;
import com.cyc.kb.KbCollection;
import com.cyc.kb.KbObject;
import com.cyc.kb.KbPredicate;
import com.cyc.kb.KbStatus;
import com.cyc.kb.Sentence;
import com.cyc.kb.client.config.KbConfiguration;
import com.cyc.kb.client.quant.QuantifiedInstanceRestrictedVariable;
import com.cyc.kb.exception.CreateException;
import com.cyc.kb.exception.InvalidNameException;
import com.cyc.kb.exception.KbException;
import com.cyc.kb.exception.KbObjectNotFoundException;
import com.cyc.kb.exception.KbRuntimeException;
import com.cyc.kb.exception.KbTypeConflictException;
import com.cyc.kb.exception.KbTypeException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A <code>KBPredicate</code> object is a facade for a <code>#$Predicate</code>
 * in Cyc KB.
 *
 * A predicate represents the property of a thing or the relationship between
 * two or more things. Predicates are used to form #$CycLAtomicSentence and are
 * in the 0th argument position. Well-formed (based on predicate arguments) and
 * closed sentences can either be true or false.
 *
 * @param <T> type of CycObject core
 * 
 * @author Vijay Raj
 * @version $Id: KbPredicateImpl.java 176267 2017-12-13 04:02:46Z nwinant $
 */
public class KbPredicateImpl<T extends DenotationalTerm> extends RelationImpl<T> implements KbPredicate {

  public static final boolean VERBOSE_ADD_FACT_ERRORS = false;
  
  private static final Logger LOG = LoggerFactory.getLogger(KbPredicateImpl.class.getName());
  private static final DenotationalTerm TYPE_CORE =
          new CycConstantImpl("Predicate", new Guid("bd5880d6-9c29-11b1-9dad-c379636f7270"));

  static DenotationalTerm getClassTypeCore() {
    return TYPE_CORE;
  }

  /**
   * Not part of the KB API. This default constructor only has the effect of
   * ensuring that there is access to a Cyc server.
   */
  KbPredicateImpl() {
    super();
  }
  
  public KbPredicateImpl (KbPredicate pred, Map<String, Object> kboData) {
    super(pred, kboData);
  }

  /**
   * Not part of the KB API. An implementation-dependent constructor.
   * <p>
   * It is used when the result of query is a CycObject and is known to be or
   * requested to be cast as an instance of KBPredicate.
   *
   * @param cycObject the CycObject wrapped by <code>KBPredicate</code>. The
   * constructor verifies that the CycObject is an instance of #$Predicate
   *
   * @throws KbTypeException if cycObject is not or could not be made an
   * instance of #$Predicate
   */
  KbPredicateImpl(DenotationalTerm cycObject) throws KbTypeException {
    super(cycObject);
  }

  /**
   * This not part of the public, supported KB API. finds or creates an instance of #$Predicate
   * represented by predStr in the underlying KB
   * <p>
   *
   * @param predStr the string representing an instance of #$Predicate in the KB
   *
   * @throws CreateException if the #$Predicate represented by predStr is not
   * found and could not be created
   * @throws KbTypeException if the term represented by predStr is not an
   * instance of #$Predicate and cannot be made into one.
   */
  KbPredicateImpl(String predStr) throws KbTypeException, CreateException {
    super(predStr);
  }

  /**
   * This not part of the public, supported KB API. finds or creates; or finds an instance of
   * #$Predicate represented by predStr in the underlying KB based on input ENUM
   * <p>
   *
   * @param predStr the string representing an instance of #$Predicate in the KB
   * @param lookup the enum to specify LookupType: FIND or FIND_OR_CREATE
   *
   * @throws CreateException
   * @throws KbTypeException
   *
   * @throws KbObjectNotFoundException if the #$Predicate represented by predStr
   * is not found and could not be created
   * @throws InvalidNameException if the string predStr does not conform to Cyc
   * constant-naming conventions
   *
   * @throws KbTypeException if the term represented by predStr is not an
   * instance of #$Predicate and lookup is set to find only
   * {@link LookupType#FIND}
   * @throws KbTypeConflictException if the term represented by predStr is not
   * an instance of #$Predicate, and lookup is set to find or create; and if the
   * term cannot be made an instance #$Predicate by asserting new knowledge.
   */
  KbPredicateImpl(String predStr, LookupType lookup) throws KbTypeException, CreateException {
    super(predStr, lookup);
  }

  /**
   * Get the
   * <code>KBPredicate</code> with the name
   * <code>nameOrId</code>. Throws exceptions if there is no KB term by that
   * name, or if it is not already an instance of #$Predicate.
   *
   * @param nameOrId the string representation or the HLID of the #$Predicate
   *
   * @return  a new KBPredicate
   *
   * @throws KbTypeException
   * @throws CreateException
   */
  public static KbPredicateImpl get(String nameOrId) throws KbTypeException, CreateException {
    return KbObjectImplFactory.get(nameOrId, KbPredicateImpl.class);
  }

  /**
   * Get the
   * <code>KBPredicate</code> object that corresponds to
   * <code>cycObject</code>. Throws exceptions if the object isn't in the KB, or if
   * it's not already an instance of
   * <code>#$Predicate</code>.
   *
   * @param cycObject the CycObject wrapped by KBPredicate. The method
   * verifies that the CycObject is an instance of #$Predicate
   *
   * @return a new KBPredicate
   *
   * @throws CreateException
   * @throws KbTypeException
   */
  @Deprecated
  public static KbPredicateImpl get(CycObject cycObject) throws KbTypeException, CreateException {
    return KbObjectImplFactory.get(cycObject, KbPredicateImpl.class);
  }

  /**
   * Find or create a
   * <code>KBPredicate</code> object named
   * <code>nameOrId</code>. If no object exists in the KB with the name
   * <code>nameOrId</code>, one will be created, and it will be asserted to be
   * an instance of
   * <code>#$Predicate</code>. If there is already an object in the KB called
   * <code>nameOrId</code>, and it is already a
   * <code>#$Predicate</code>, it will be returned. If it is not already a
   * <code>#$Predicate</code>, but can be made into one by addition of
   * assertions to the KB, such assertions will be made, and the object will be
   * returned. If the object in the KB cannot be turned into a
   * <code>#$Predicate</code> by adding assertions (i.e. some existing
   * assertion prevents it from being a
   * <code>#$Predicate</code>), a
   * <code>KBTypeConflictException</code>will be thrown.
   *
   * @param nameOrId  the string representation or the HLID of the #$Predicate
   *
   * @return a new KBPredicate
   *
   * @throws KbTypeException
   * @throws CreateException
   */
  public static KbPredicateImpl findOrCreate(String nameOrId) throws CreateException, KbTypeException {
    return KbObjectImplFactory.findOrCreate(nameOrId, KbPredicateImpl.class);
  }

  /**
   * Find or create a KBPredicate object from
   * <code>cycObject</code>. If
   * <code>cycObject</code> is already a
   * <code>#$Predicate</code>, an appropriate
   * <code>KBPredicate</code> object will be returned. If
   * <code>object</code> is not already a
   * <code>#$Predicate</code>, but can be made into one by addition of
   * assertions to the KB, such assertions will be made, and the relevant object
   * will be returned. If
   * <code>cycObject</code> cannot be turned into a
   * <code>#$Predicate</code> by adding assertions (i.e. some existing
   * assertion prevents it from being a
   * <code>#$Predicate</code>, a
   * <code>KBTypeConflictException</code>will be thrown.
   *
   * @param cycObject the CycObject wrapped by KBPredicate. The method
   * verifies that the CycObject is an #$Predicate
   *
   * @return a new KBPredicate
   *
   * @throws KbTypeException
   * @throws CreateException
   */
  @Deprecated
  public static KbPredicateImpl findOrCreate(CycObject cycObject) throws CreateException, KbTypeException {
    return KbObjectImplFactory.findOrCreate(cycObject, KbPredicateImpl.class);
  }

  /**
   * Find or create a
   * <code>KBPredicate</code> object named
   * <code>nameOrId</code>, and also make it an instance of
   * <code>constraintCol</code> in the default context specified by
   * {@link KBAPIDefaultContext#forAssertion()}. If no object
   * exists in the KB with the name
   * <code>nameOrId</code>, one will be created, and it will be asserted to be
   * an instance of both
   * <code>#$Predicate</code> and
   * <code>constraintCol</code>. If there is already an object in the
   * KB called
   * <code>nameOrId</code>, and it is already both a
   * <code>#$Predicate</code> and a
   * <code>constraintCol</code>, it will be returned. If it is not
   * already both a
   * <code>#$Predicate</code> and a
   * <code>constraintCol</code>, but can be made so by addition of
   * assertions to the KB, such assertions will be made, and the object will be
   * returned. If the object in the KB cannot be turned into both a
   * <code>#$Predicate</code> and a
   * <code>constraintCol</code> by adding assertions, a
   * <code>KBTypeConflictException</code>will be thrown.
   *
   * @param nameOrId  the string representation or the HLID of the #$Predicate
   * @param constraintCol the collection that this #$Predicate will instantiate
   *
   * @return a new KBPredicate
   *
   * @throws KbTypeException
   * @throws CreateException
   */
  public static KbPredicateImpl findOrCreate(String nameOrId, KbCollection constraintCol) throws CreateException, KbTypeException {
    return KbObjectImplFactory.findOrCreate(nameOrId, constraintCol, KbPredicateImpl.class);
  }

  /**
   * Find or create a
   * <code>KBPredicate</code> object named
   * <code>nameOrId</code>, and also make it an instance of
   * <code>constraintCol</code> in the default context specified by
   * {@link KBAPIDefaultContext#forAssertion()}. If no object
   * exists in the KB with the name
   * <code>nameOrId</code>, one will be created, and it will be asserted to be
   * an instance of both
   * <code>#$Predicate</code> and
   * <code>constraintCol</code>. If there is already an object in the
   * KB called
   * <code>nameOrId</code>, and it is already both a
   * <code>#$Predicate</code> and a
   * <code>constraintCol</code>, it will be returned. If it is not
   * already both a
   * <code>#$Predicate</code> and a
   * <code>constraintCol</code>, but can be made so by addition of
   * assertions to the KB, such assertions will be made, and the object will be
   * returned. If the object in the KB cannot be turned into both a
   * <code>#$Predicate</code> and a
   * <code>constraintCol</code> by adding assertions, a
   * <code>KBTypeConflictException</code>will be thrown.
   *
   * @param nameOrId  the string representation or the HLID of the #$Predicate
   * @param constraintColStr the string representation of the collection that
   * this #$Predicate will instantiate
   *
   * @return a new KBPredicate
   *
   * @throws KbTypeException
   * @throws CreateException
   */
  public static KbPredicateImpl findOrCreate(String nameOrId, String constraintColStr) throws CreateException, KbTypeException {
    return KbObjectImplFactory.findOrCreate(nameOrId, constraintColStr, KbPredicateImpl.class);
  }

  /**
   * Find or create a
   * <code>KBPredicate</code> object named
   * <code>nameOrId</code>, and also make it an instance of
   * <code>constraintCol</code> in
   * <code>ctx</code>. If no object exists in the KB with the name
   * <code>nameOrId</code>, one will be created, and it will be asserted to be
   * an instance of both
   * <code>#$Predicate</code> and
   * <code>constraintCol</code>. If there is already an object in the
   * KB called
   * <code>nameOrId</code>, and it is already both a
   * <code>#$Predicate</code> and a
   * <code>constraintCol</code>, it will be returned. If it is not
   * already both a
   * <code>#$Predicate</code> and a
   * <code>constraintCol</code>, but can be made so by addition of
   * assertions to the KB, such assertions will be made, and the object will be
   * returned. If the object in the KB cannot be turned into both a
   * <code>#$Predicate</code> and a
   * <code>constraintCol</code> by adding assertions, a
   * <code>KBTypeConflictException</code>will be thrown.
   *
   * @param nameOrId  the string representation or the HLID of the #$Predicate
   * @param constraintCol the collection that this #$Predicate will instantiate
   * @param ctx the context in which the resulting object must be an instance of
   * constraintCol
   *
   * @return a new KBPredicate
   *
   * @throws KbTypeException
   * @throws CreateException
   */
  public static KbPredicateImpl findOrCreate(String nameOrId, KbCollection constraintCol, ContextImpl ctx)
      throws CreateException, KbTypeException {
    return KbObjectImplFactory.findOrCreate(nameOrId, constraintCol, ctx, KbPredicateImpl.class);
  }

  /**
   * Find or create a
   * <code>KBPredicate</code> object named
   * <code>nameOrId</code>, and also make it an instance of
   * <code>constraintCol</code> in
   * <code>ctx</code>. If no object exists in the KB with the name
   * <code>nameOrId</code>, one will be created, and it will be asserted to be
   * an instance of both
   * <code>#$Predicate</code> and
   * <code>constraintCol</code>. If there is already an object in the
   * KB called
   * <code>nameOrId</code>, and it is already both a
   * <code>#$Predicate</code> and a
   * <code>constraintCol</code>, it will be returned. If it is not
   * already both a
   * <code>#$Predicate</code> and a
   * <code>constraintCol</code>, but can be made so by addition of
   * assertions to the KB, such assertions will be made, and the object will be
   * returned. If the object in the KB cannot be turned into both a
   * <code>#$Predicate</code> and a
   * <code>constraintCol</code> by adding assertions, a
   * <code>KBTypeConflictException</code>will be thrown.
   *
   * @param nameOrId the string representation or the HLID of the term
   * @param constraintColStr the string representation of the collection that
   * this #$Predicate will instantiate
   * @param ctxStr the context in which the resulting object must be an instance of
   * constraintCol
   *
   * @return a new KBPredicate
   *
   * @throws KbTypeException
   * @throws CreateException
   */
  public static KbPredicateImpl findOrCreate(String nameOrId, String constraintColStr, String ctxStr) throws CreateException, KbTypeException {
    return KbObjectImplFactory.findOrCreate(nameOrId, constraintColStr, ctxStr, KbPredicateImpl.class);
  }

  /**
   * Checks whether entity exists in KB and is an instance of #$Predicate. If
   * false, {@link #getStatus(String)} may yield more information. This method
   * is equivalent to
   * <code>getStatus(nameOrId).equals(KBStatus.EXISTS_AS_TYPE)</code>.
   *
   * @param nameOrId either the name or HL ID of an entity in the KB
   * @return <code>true</code> if entity exists in KB and is an instance of
   * #$Predicate
   */
  public static boolean existsAsType(String nameOrId) {
    return getStatus(nameOrId).equals(KbStatus.EXISTS_AS_TYPE);
  }

  /**
   * Checks whether entity exists in KB and is an instance of #$Predicate. If
   * false, {@link #getStatus(CycObject)} may yield more information. This
   * method is equivalent to
   * <code>getStatus(object).equals(KBStatus.EXISTS_AS_TYPE)</code>.
   *
   * @param cycObject the CycObject representation of a KB entity
   * @return <code>true</code> if entity exists in KB and is an instance of
   * #$Predicate
   */
  public static boolean existsAsType(CycObject cycObject) {
    return getStatus(cycObject).equals(KbStatus.EXISTS_AS_TYPE);
  }

  /**
   * Returns an KBStatus enum which describes whether <code>nameOrId</code>
   * exists in the KB and is an instance of <code>#$Predicate</code>.
   *
   * @param nameOrId either the name or HL ID of an entity in the KB
   * @return an enum describing the existential status of the entity in the KB
   */
  public static KbStatus getStatus(String nameOrId) {
    return KbObjectImplFactory.getStatus(nameOrId, KbPredicateImpl.class);

  }

  /**
   * Returns an KBStatus enum which describes whether <code>cycObject</code>
   * exists in the KB and is an instance of <code>#$Predicate</code>.
   *
   * @param cycObject the CycObject representation of a KB entity
   * @return an enum describing the existential status of the entity in the KB
   */
  public static KbStatus getStatus(CycObject cycObject) {
    return KbObjectImplFactory.getStatus(cycObject, KbPredicateImpl.class);
  }
  
  @Override
  public Collection<KbPredicate> getSpecializations() {
    return getSpecializations(KbConfiguration.getDefaultContext().forQuery());
  }
  /*
  @Override
  public Collection<KbPredicate> getSpecializations(String ctxStr) {
    return getSpecializations(KbUtils.getKBObjectForArgument(ctxStr, ContextImpl.class));
  }
  */
  @Override
  public java.util.Collection<KbPredicate> getSpecializations(Context ctx) {
    //return (this.<KbPredicate>getValues(Constants.genlPreds(), 2, 1, ctx));
    return Constants.genlPreds().getValuesForArgPosition(this, 2, 1, ctx);
  }
  /*
  @Override
  public KbPredicate addSpecialization(String moreSpecificStr, String ctxStr) throws KbTypeException, CreateException {
    KbPredicate p = KbPredicateImpl.get(moreSpecificStr);
    return addSpecialization(p, ContextImpl.get(ctxStr));
  }
  */
  @Override
  public KbPredicate addSpecialization(KbPredicate moreSpecific, Context ctx) throws KbTypeException, CreateException {
    Constants.genlPreds().addFact(ctx, moreSpecific, this);
    return this;
  }
  
  @Override
  public Collection<KbPredicate> getGeneralizations() throws KbException {
    return getGeneralizations(KbConfiguration.getDefaultContext().forQuery());
  }

  @Override
  public Sentence getGeneralizationSentence(KbPredicate moreGeneral) throws KbTypeException, CreateException{
    return new SentenceImpl (Constants.genlPreds(), this, (Object) moreGeneral);
  }

  @Override
  public Sentence getInverseGeneralizationSentence(KbPredicate moreGeneral) throws KbTypeException, CreateException {
    return new SentenceImpl (Constants.getInstance().GENLINVERSEPREDS_PRED, this, (Object) moreGeneral);
  }
  /*
  @Override
  public Collection<KbPredicate> getGeneralizations(String ctxStr) {
    return getGeneralizations(KbUtils.getKBObjectForArgument(ctxStr, ContextImpl.class));
  }
  */
  @Override
  public Collection<KbPredicate> getGeneralizations(Context ctx) {
    //return (this.<KbPredicate>getValues(Constants.genlPreds(), 1, 2, ctx));
    return Constants.genlPreds().getValuesForArgPosition(this, 1, 2, ctx);
  }
  /*
  @Override
  public KbPredicate addGeneralization(String moreGeneralStr, String ctxStr) throws KbTypeException, CreateException {
    KbPredicate p = KbPredicateImpl.get(moreGeneralStr);
    return addGeneralization(p, ContextImpl.get(ctxStr));
  }
  */
  @Override
  public KbPredicate addGeneralization(KbPredicate moreGeneral, Context ctx) throws KbTypeException, CreateException {
    Constants.genlPreds().addFact(ctx, this, moreGeneral);
    return this;
  }
  
  @Override
  public boolean isGeneralizationOf(KbPredicate moreSpecific, Context ctx) {
    try {
      return getAccess().getInspectorTool().isGenlPredOf((Fort) getCore(), (Fort) moreSpecific.getCore(), getCore(ctx));
    } catch (CycConnectionException e) {
      throw KbRuntimeException.fromThrowable(e);
    }
  }

  @Override
  public List<Fact> getExtent() {
    return getExtent(null);
  }

  @Override
  public List<Fact> getExtent(Context ctx) {
    final List<Fact> kbFacts = new ArrayList<>();
    try {
      final CycList assertions = getAccess().getLookupTool().getPredExtent(this.getCore(), (ctx != null ? getCore(ctx) : null));
      for (Object o : assertions) {
        if (o instanceof CycAssertion) {
          try {
            kbFacts.add(FactImpl.get((CycAssertion) o));
          } catch (KbTypeException | CreateException e) {
            // ignore
          }
        }
      }
    } catch (CycConnectionException ex) {
      throw KbRuntimeException.fromThrowable(ex);
    }
    return kbFacts;
  }

  /**
   * Return the KBCollection as a KBObject of the Cyc term that underlies this
   * class.
   *
   * @return KBCollectionImpl.get("#$Predicate");
   */
  @Override
  public KbObject getType() {
    return getClassType();
  }

  /**
   * Return the KBCollection as a KBObject of the Cyc term that 
   * underlies this class. 
   *
   * @return KBCollectionImpl.get("#$Predicate");
   */
  public static KbObject getClassType() {
    try {
      return KbCollectionImpl.get(getClassTypeString());
    } catch (KbException kae) {
      throw KbRuntimeException.fromThrowable(kae);
    }
  }

  @Override
  String getTypeString() {
    return getClassTypeString();
  }

  static String getClassTypeString() {
    return "#$Predicate";
  }
  
  // KB API does not do any introspection.. so if we want to use Query API, we should construct
  // a fully qualified sentence for the given predicate. That can only be possible when all
  // other variables are passed in.
  //
  @Override
  public Sentence getSentence(Object... args) throws KbTypeException, CreateException {
    validateArgArity(args);
    return new SentenceImpl(this, args);
  }
  
  @Override
  public Fact getFact(Context ctx, Object... args) throws KbTypeException, CreateException {
    final Sentence s = getSentence(args);
    return FactImpl.get(s, ctx);
  }
  
  @Override
  public Boolean isAsserted(Context ctx, Object... args) {
    try {
      getFact(ctx, args);
      return true;
    } catch (KbException e) {
      return false;
    }
  }
  
  @Override
  public Collection<Fact> getFacts(Object arg, int argPos, Context ctx) {
    try {
      final String ctxStr = (ctx == null) ? KbConfiguration.getDefaultContext().forQuery().stringApiValue() //"#$BaseKB"
              : ctx.stringApiValue();
      final String command = "(" + SublConstants.getInstance().withInferenceMtRelevance.stringApiValue() + " " + ctxStr
              + " (" + SublConstants.getInstance().gatherGafArgIndex.stringApiValue() + " "
              + objectToApiString(arg) + " " + argPos + " "
              + this.stringApiValue() + "))";
      LOG.trace("getFacts: {}", command);
      final Object res = getAccess().converse().converseObject(command);
      LOG.trace("getFacts response: {}", res);
      final Set<Fact> facts = new HashSet<>();
      if (!CycObjectFactory.nil.equals(res)) {
        final CycList<CycAssertion> assertList = (CycList<CycAssertion>) res;
        for (Object o : assertList) {
          if (o instanceof CycAssertion) {
            try {
              facts.add(FactImpl.get((CycAssertion) o));
            } catch (KbException kbe) {
              // Nothing to do. We did get the facts we are building
              // but something went wrong. Just don't add it to the list.
              LOG.error("Error attempting to get fact {}" + o, kbe);
            }
          }
        }
      }
      return facts;
    } catch (CycConnectionException ex) {
      throw KbRuntimeException.fromThrowable(ex);
    } catch (CycApiException ex) {
      throw KbRuntimeException.fromThrowable(ex);
    }
  }
  
  @Override
  public Fact addFact(Context ctx, Object... args) throws KbTypeException, CreateException {
    if (args.length == 2) {
      if (args[0] instanceof QuantifiedInstanceRestrictedVariable
              || args[1] instanceof QuantifiedInstanceRestrictedVariable) {
        validateArgArity(args);
        final List<Object> argsWithPredicate = Arrays.asList(this, args[0], args[1]);
        try {
          // TODO: Should we be doing this for other assertion & sentence methods? - nwinant, 2017-07-26
          TypeFactImpl fact = new TypeFactImpl(ctx, argsWithPredicate.toArray());
          return FactImpl.findOrCreate(fact.getFormula(), fact.getContext());
        } catch (KbException kbe) {
          throw CreateException.fromThrowable(kbe);
        }
      }
    }
    final Sentence s = getSentence(args);
    return FactImpl.findOrCreate(s, ctx, VERBOSE_ADD_FACT_ERRORS);
  }
  
  @Override
  public <O> Collection<O> getValuesForArgPosition(
          Object arg, int argPosition, int valuePosition, Context ctx) {
    final Set<O> results = new HashSet<>();
    final Collection<Fact> facts = getFacts(arg, argPosition, ctx);
    for (Fact fact : facts) {
      final CycAssertion ca = (CycAssertion) fact.getCore();
      final CycList<Object> g = ca.getGaf().getArgs();
      final Object o = g.get(valuePosition);
      try {
        results.add(KbObjectImpl.<O>checkAndCastObject(o));
      } catch (KbException kbe) {
        // Don't do anything. 
        LOG.error("Error attempting to checkAndCastObject {}" + o, kbe);
      }
      // TODO: Need to unify casting and typing of KBObject. And individual types.
      // TODO: Need to decide what exception to throw if an KBObject can't be typed into a subclass here.
      // TODO: Need to decide if instanceof check should be present
    }
    LOG.debug("Results from getValues: {}", results);
    return results;
  }
  
  @Override
  public <O> Collection<O> getValuesForArgPositionWithMatchArg(
          Object arg, int argPos, int valuePos, Object matchArg, int matchArgPos, Context ctx) {
    final Set<O> results = new HashSet<>();
    final Collection<Fact> facts = getFacts(arg, argPos, ctx);
    final Object cycAccessFilter = (matchArg instanceof KbObject)
            ? ((KbObject) matchArg).getCore()
            : matchArg;
    for (Fact fact : facts) {
      final CycAssertion ca = (CycAssertion) fact.getCore();
      final CycList<Object> g = ca.getGaf().getArgs();
      final Object o = g.get(valuePos);
      if (g.get(matchArgPos).equals(cycAccessFilter)) {
        try {
          results.add(KbObjectImpl.<O>checkAndCastObject(o));
        } catch (KbException kbe) {
          // Don't do anything. 
          LOG.error("Error attempting to checkAndCastObject {}" + o, kbe);
        }
      }
      // TODO: Need to unify casting and typing of KBObject. And individual types.
      // TODO: Need to decide what exception to throw if an KBObject can't be typed into a subclass here.
      // TODO: Need to decide if instanceof check should be present
    }
    return results;
  }
  
  private String objectToApiString(Object obj) {
    //
    // FIXME: this needs to be more robust and there's probably already a reusable function for it somewhere - nwinant, 2017-07-21
    //
    if (obj instanceof KbObject) {
      return ((KbObject) obj).stringApiValue();
    }
    return (obj instanceof String) ? "\"" + obj + "\"" : "" + obj;
  }
  
}
