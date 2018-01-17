package com.cyc.kb.client;

/*
 * #%L
 * File: RelationImpl.java
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
import com.cyc.base.cycobject.CycObject;
import com.cyc.base.cycobject.DenotationalTerm;
import com.cyc.base.cycobject.Guid;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.baseclient.CycObjectFactory;
import static com.cyc.baseclient.connection.SublApiHelper.makeNestedSublStmt;
import static com.cyc.baseclient.connection.SublApiHelper.makeSublStmt;
import com.cyc.baseclient.cycobject.CycConstantImpl;
import com.cyc.baseclient.cycobject.CycSymbolImpl;
import com.cyc.kb.Context;
import com.cyc.kb.Fact;
import com.cyc.kb.KbCollection;
import com.cyc.kb.KbObject;
import com.cyc.kb.KbStatus;
import com.cyc.kb.Relation;
import com.cyc.kb.Sentence;
import com.cyc.kb.client.config.KbConfiguration;
import com.cyc.kb.exception.CreateException;
import com.cyc.kb.exception.InvalidNameException;
import com.cyc.kb.exception.KbException;
import com.cyc.kb.exception.KbObjectNotFoundException;
import com.cyc.kb.exception.KbRuntimeException;
import com.cyc.kb.exception.KbTypeConflictException;
import com.cyc.kb.exception.KbTypeException;
import com.cyc.kb.exception.VariableArityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A <code>RelationImpl</code> object is a facade for a <code>#$Relation</code> in Cyc KB.
 *
 * A relation relates one or more things. Relations can be used to construct formulas. Relations can
 * be used to construct atomic or non-atomic sentences and non-atomic terms. A well formed formula
 * has a relation in 0th argument position and the other arguments satisfy the semantic constraints
 * of the relation. This class will be rarely, if ever, used by itself. Instead, its subclasses
 * {@link KBPredicateImpl} and {@link KBFunctionImpl} should be used in virtually all cases.
 *
 * @param <T> type of CycObject core
 * 
 * @author Vijay Raj
 * @version $Id: RelationImpl.java 173132 2017-08-02 00:48:28Z nwinant $
 */
public class RelationImpl<T extends DenotationalTerm> extends KbIndividualImpl<T> implements Relation {

  private static final DenotationalTerm TYPE_CORE
          = new CycConstantImpl("Relation", new Guid("bd5880cd-9c29-11b1-9dad-c379636f7270"));

  static DenotationalTerm getClassTypeCore() {
    return TYPE_CORE;
  }
  private static final CycSymbolImpl WITH_MT = CycObjectFactory.makeCycSymbol("with-mt");

  private Collection<Integer> arityValues = null;
  private Boolean isVariableArity = null;

  /**
   * Not part of the KB API. This default constructor only has the effect of ensuring that there is
   * access to a Cyc server.
   */
  RelationImpl() {
    super();
  }

  public RelationImpl(Relation rel, Map<String, Object> kboData) {
    super(rel, kboData);
  }

  /**
   * Not part of the KB API. An implementation-dependent constructor.
   * <p>
   * It is used when the result of query is a CycObject and is known to be or requested to be cast
   * as an instance of Relation.
   *
   * @param cycObject the CycObject wrapped by <code>Relation</code>. The constructor verifies that
   * the CycObject is an instance of #$Relation
   *
   * @throws KbTypeException if cycObject is not or could not be made an instance of #$Relation
   */
  RelationImpl(DenotationalTerm cycObject) throws KbTypeException {
    super(cycObject);
  }

  /**
   * This not part of the public, supported KB API. finds or creates an instance of
   * CYC_NAME_OF_THE_CLASS represented by relStr in the underlying KB
   * <p>
   *
   * @param relStr the string representing an #$Relation in the KB
   *
   * @throws CreateException if the #$Relation represented by relStr is not found and could not be
   * created
   * @throws KbTypeException if the term represented by relStr is not an instance of #$Relation and
   * cannot be made into one.
   */
  RelationImpl(String relStr) throws KbTypeException, CreateException {
    super(relStr);
  }

  /**
   * This not part of the public, supported KB API. finds or creates; or finds an instance of
   * #$Relation represented by relStr in the underlying KB based on input ENUM
   * <p>
   *
   * @param relStr the string representing an instance of #$Relation in the KB
   * @param lookup the enum to specify LookupType: FIND or FIND_OR_CREATE
   *
   * @throws CreateException
   * @throws KbTypeException
   *
   * @throws KbObjectNotFoundException if the #$Relation represented by relStr is not found and
   * could not be created
   * @throws InvalidNameException if the string relStr does not conform to Cyc constant-naming
   * conventions
   *
   * @throws KbTypeException if the term represented by relStr is not an instance of #$Relation and
   * lookup is set to find only {@link LookupType#FIND}
   * @throws KbTypeConflictException if the term represented by relStr is not an instance of
   * #$Relation, and lookup is set to find or create; and if the term cannot be made an instance
   * #$Relation by asserting new knowledge.
   */
  RelationImpl(String relStr, LookupType lookup) throws KbTypeException, CreateException {
    super(relStr, lookup);
  }

  /**
   * Get the <code>Relation</code> with the name <code>nameOrId</code>. Throws exceptions if there
   * is no KB term by that name, or if it is not already an instance of #$Relation.
   *
   * @param nameOrId the string representation or the HLID of the #$Relation
   *
   * @return a new Relation
   *
   * @throws KbTypeException
   * @throws CreateException
   */
  public static RelationImpl get(String nameOrId) throws KbTypeException, CreateException {
    return KbObjectImplFactory.get(nameOrId, RelationImpl.class);
  }

  /**
   * Get the <code>Relation</code> object that corresponds to <code>cycObject</code>. Throws
   * exceptions if the object isn't in the KB, or if it's not already an instance of
   * <code>#$Relation</code>.
   *
   * @param cycObject the CycObject wrapped by Relation. The method verifies that the CycObject is
   * an instance of #$Relation
   *
   * @return a new Relation
   *
   * @throws CreateException
   * @throws KbTypeException
   */
  @Deprecated
  public static RelationImpl get(CycObject cycObject) throws KbTypeException, CreateException {
    return KbObjectImplFactory.get(cycObject, RelationImpl.class);
  }

  /**
   * Find or create a <code>Relation</code> object named <code>nameOrId</code>. If no object exists
   * in the KB with the name <code>nameOrId</code>, one will be created, and it will be asserted to
   * be an instance of <code>#$Relation</code>. If there is already an object in the KB called
   * <code>nameOrId</code>, and it is already a <code>#$Relation</code>, it will be returned. If it
   * is not already a <code>#$Relation</code>, but can be made into one by addition of assertions to
   * the KB, such assertions will be made, and the object will be returned. If the object in the KB
   * cannot be turned into a <code>#$Relation</code> by adding assertions (i.e. some existing
   * assertion prevents it from being a <code>#$Relation</code>), a
   * <code>KBTypeConflictException</code>will be thrown.
   *
   * @param nameOrId the string representation or the HLID of the #$Relation
   *
   * @return a new Relation
   *
   * @throws KbTypeException
   * @throws CreateException
   */
  public static RelationImpl findOrCreate(String nameOrId) throws CreateException, KbTypeException {
    return KbObjectImplFactory.findOrCreate(nameOrId, RelationImpl.class);
  }

  /**
   * Find or create a Relation object from <code>cycObject</code>. If <code>cycObject</code> is
   * already a <code>#$Relation</code>, an appropriate <code>Relation</code> object will be
   * returned. If <code>object</code> is not already a <code>#$Relation</code>, but can be made into
   * one by addition of assertions to the KB, such assertions will be made, and the relevant object
   * will be returned. If <code>cycObject</code> cannot be turned into a <code>#$Relation</code> by
   * adding assertions (i.e. some existing assertion prevents it from being a
   * <code>#$Relation</code>, a <code>KBTypeConflictException</code>will be thrown.
   *
   * @param cycObject the CycObject wrapped by Relation. The method verifies that the CycObject is
   * an #$Relation
   *
   * @return a new Relation
   *
   * @throws KbTypeException
   * @throws CreateException
   */
  @Deprecated
  public static RelationImpl findOrCreate(CycObject cycObject) throws CreateException, KbTypeException {
    return KbObjectImplFactory.findOrCreate(cycObject, RelationImpl.class);
  }

  /**
   * Find or create a <code>Relation</code> object named <code>nameOrId</code>, and also make it an
   * instance of <code>constraintCol</code> in the default context specified by
   * {@link KBAPIDefaultContext#forAssertion()}. If no object exists in the KB with the name
   * <code>nameOrId</code>, one will be created, and it will be asserted to be an instance of both
   * <code>#$Relation</code> and <code>constraintCol</code>. If there is already an object in the KB
   * called <code>nameOrId</code>, and it is already both a <code>#$Relation</code> and a
   * <code>constraintCol</code>, it will be returned. If it is not already both a
   * <code>#$Relation</code> and a <code>constraintCol</code>, but can be made so by addition of
   * assertions to the KB, such assertions will be made, and the object will be returned. If the
   * object in the KB cannot be turned into both a <code>#$Relation</code> and a
   * <code>constraintCol</code> by adding assertions, a <code>KBTypeConflictException</code>will be
   * thrown.
   *
   * @param nameOrId the string representation or the HLID of the #$Relation
   * @param constraintCol the collection that this #$Relation will instantiate
   *
   * @return a new Relation
   *
   * @throws KbTypeException
   * @throws CreateException
   */
  public static RelationImpl findOrCreate(String nameOrId, KbCollection constraintCol) throws CreateException, KbTypeException {
    return KbObjectImplFactory.findOrCreate(nameOrId, constraintCol, RelationImpl.class);
  }

  /**
   * Find or create a <code>Relation</code> object named <code>nameOrId</code>, and also make it an
   * instance of <code>constraintCol</code> in the default context specified by
   * {@link KBAPIDefaultContext#forAssertion()}. If no object exists in the KB with the name
   * <code>nameOrId</code>, one will be created, and it will be asserted to be an instance of both
   * <code>#$Relation</code> and <code>constraintCol</code>. If there is already an object in the KB
   * called <code>nameOrId</code>, and it is already both a <code>#$Relation</code> and a
   * <code>constraintCol</code>, it will be returned. If it is not already both a
   * <code>#$Relation</code> and a <code>constraintCol</code>, but can be made so by addition of
   * assertions to the KB, such assertions will be made, and the object will be returned. If the
   * object in the KB cannot be turned into both a <code>#$Relation</code> and a
   * <code>constraintCol</code> by adding assertions, a <code>KBTypeConflictException</code>will be
   * thrown.
   *
   * @param nameOrId the string representation or the HLID of the #$Relation
   * @param constraintColStr the string representation of the collection that this #$Relation will
   * instantiate
   *
   * @return a new Relation
   *
   * @throws KbTypeException
   * @throws CreateException
   */
  public static RelationImpl findOrCreate(String nameOrId, String constraintColStr) throws CreateException, KbTypeException {
    return KbObjectImplFactory.findOrCreate(nameOrId, constraintColStr, RelationImpl.class);
  }

  /**
   * Find or create a <code>Relation</code> object named <code>nameOrId</code>, and also make it an
   * instance of <code>constraintCol</code> in <code>ctx</code>. If no object exists in the KB with
   * the name <code>nameOrId</code>, one will be created, and it will be asserted to be an instance
   * of both <code>#$Relation</code> and <code>constraintCol</code>. If there is already an object
   * in the KB called <code>nameOrId</code>, and it is already both a <code>#$Relation</code> and a
   * <code>constraintCol</code>, it will be returned. If it is not already both a
   * <code>#$Relation</code> and a <code>constraintCol</code>, but can be made so by addition of
   * assertions to the KB, such assertions will be made, and the object will be returned. If the
   * object in the KB cannot be turned into both a <code>#$Relation</code> and a
   * <code>constraintCol</code> by adding assertions, a <code>KBTypeConflictException</code>will be
   * thrown.
   *
   * @param nameOrId the string representation or the HLID of the #$Relation
   * @param constraintCol the collection that this #$Relation will instantiate
   * @param ctx the context in which the resulting object must be an instance of constraintCol
   *
   * @return a new Relation
   *
   * @throws KbTypeException
   * @throws CreateException
   */
  public static RelationImpl findOrCreate(String nameOrId, KbCollection constraintCol, ContextImpl ctx)
          throws CreateException, KbTypeException {
    return KbObjectImplFactory.findOrCreate(nameOrId, constraintCol, ctx, RelationImpl.class);
  }

  /**
   * Find or create a <code>Relation</code> object named <code>nameOrId</code>, and also make it an
   * instance of <code>constraintCol</code> in <code>ctx</code>. If no object exists in the KB with
   * the name <code>nameOrId</code>, one will be created, and it will be asserted to be an instance
   * of both <code>#$Relation</code> and <code>constraintCol</code>. If there is already an object
   * in the KB called <code>nameOrId</code>, and it is already both a <code>#$Relation</code> and a
   * <code>constraintCol</code>, it will be returned. If it is not already both a
   * <code>#$Relation</code> and a <code>constraintCol</code>, but can be made so by addition of
   * assertions to the KB, such assertions will be made, and the object will be returned. If the
   * object in the KB cannot be turned into both a <code>#$Relation</code> and a
   * <code>constraintCol</code> by adding assertions, a <code>KBTypeConflictException</code>will be
   * thrown.
   *
   * @param nameOrId the string representation or the HLID of the term
   * @param constraintColStr the string representation of the collection that this #$Relation will
   * instantiate
   * @param ctxStr the context in which the resulting object must be an instance of constraintCol
   *
   * @return a new Relation
   *
   * @throws KbTypeException
   * @throws CreateException
   */
  public static RelationImpl findOrCreate(String nameOrId, String constraintColStr, String ctxStr)
          throws CreateException, KbTypeException {
    return KbObjectImplFactory.findOrCreate(nameOrId, constraintColStr, ctxStr, RelationImpl.class);
  }

  /**
   * Checks whether entity exists in KB and is an instance of #$Relation. If false,
   * {@link #getStatus(String)} may yield more information. This method is equivalent to
   * <code>getStatus(nameOrId).equals(KBStatus.EXISTS_AS_TYPE)</code>.
   *
   * @param nameOrId either the name or HL ID of an entity in the KB
   * @return <code>true</code> if entity exists in KB and is an instance of #$Relation
   */
  public static boolean existsAsType(String nameOrId) {
    return getStatus(nameOrId).equals(KbStatus.EXISTS_AS_TYPE);
  }

  /**
   * Checks whether entity exists in KB and is an instance of #$Relation. If false,
   * {@link #getStatus(CycObject)} may yield more information. This method is equivalent to
   * <code>getStatus(object).equals(KBStatus.EXISTS_AS_TYPE)</code>.
   *
   * @param cycObject the CycObject representation of a KB entity
   * @return <code>true</code> if entity exists in KB and is an instance of #$Relation
   */
  public static boolean existsAsType(CycObject cycObject) {
    return getStatus(cycObject).equals(KbStatus.EXISTS_AS_TYPE);
  }

  /**
   * Returns an KBStatus enum which describes whether <code>nameOrId</code> exists in the KB and is
   * an instance of <code>#$Relation</code>.
   *
   * @param nameOrId either the name or HL ID of an entity in the KB
   * @return an enum describing the existential status of the entity in the KB
   */
  public static KbStatus getStatus(String nameOrId) {
    return KbObjectImplFactory.getStatus(nameOrId, RelationImpl.class);
  }

  /**
   * Returns an KBStatus enum which describes whether <code>cycObject</code> exists in the KB and is
   * an instance of <code>#$Relation</code>.
   *
   * @param cycObject the CycObject representation of a KB entity
   * @return an enum describing the existential status of the entity in the KB
   */
  public static KbStatus getStatus(CycObject cycObject) {
    return KbObjectImplFactory.getStatus(cycObject, RelationImpl.class);
  }

  @Override
  public List<Collection<KbCollection>> getArgIsaList() {
    return getArgIsaList(KbConfiguration.getDefaultContext().forQuery());
  }

  @Override
  public List<Collection<KbCollection>> getArgIsaList(Context ctx) {
    List<Collection<KbCollection>> argIsaList = new ArrayList();
    for (Integer i = 1; i <= this.getArity(); i++) {
      argIsaList.add(this.getArgIsa(i, ctx));
    }
    return argIsaList;
  }
  
  @Override
  public Collection<KbCollection> getArgIsa(int argPos) {
    return getArgIsa(argPos, KbConfiguration.getDefaultContext().forQuery());
  }
  
  @Override
  public Collection<KbCollection> getArgIsa(int argPos, String ctxStr) {
    return getArgIsa(argPos, KbUtils.getKBObjectForArgument(ctxStr, ContextImpl.class));
  }
  
  @Override
  public Collection<KbCollection> getArgIsa(int argPos, Context ctx) {
    int valuePos = 3;
    int filter = argPos;
    int filtPos = 2;
    //return this.<KbCollection>getValuesForArg(Constants.argIsa(), 1, getPos, filter, filtPos, ctx);
    return Constants.argIsa().getValuesForArgPositionWithMatchArg(this, 1, valuePos, filter, filtPos, ctx);
  }
  
  @Override
  public Relation addArgIsa(int argPos, String colStr, String ctxStr) throws KbTypeException, CreateException {
    return addArgIsa(argPos, KbCollectionImpl.get(colStr), ContextImpl.get(ctxStr));
  }
  
  @Override
  public Relation addArgIsa(int argPos, KbCollection col, Context ctx) throws KbTypeException, CreateException {
    //addFact(ctx, Constants.argIsa(), 1, (Object) argPos, (Object) col);
    Constants.argIsa().addFact(ctx, this, argPos, col);
    return this;
  }

  @Override
  public Sentence addArgIsaSentence(int argPos, KbCollection col) throws KbTypeException, CreateException {
    return new SentenceImpl(Constants.argIsa(), this, argPos, col);
  }

  @Override
  public List<Collection<KbCollection>> getArgGenlList() {
    return getArgGenlList(KbConfiguration.getDefaultContext().forQuery());
  }

  @Override
  public List<Collection<KbCollection>> getArgGenlList(Context ctx) {
    List<Collection<KbCollection>> argGenlList = new ArrayList();
    for (Integer i = 1; i <= this.getArity(); i++) {
      argGenlList.add(this.getArgGenl(i));
    }
    return argGenlList;
  }
  
  @Override
  public Collection<KbCollection> getArgGenl(int argPos) {
    return getArgGenl(argPos, KbConfiguration.getDefaultContext().forQuery());
  }
  
  @Override
  public Collection<KbCollection> getArgGenl(int argPos, String ctxStr) {
    return getArgGenl(argPos, KbUtils.getKBObjectForArgument(ctxStr, ContextImpl.class));
  }
  
  @Override
  public Collection<KbCollection> getArgGenl(int argPos, Context ctx) {
    int valuePos = 3;
    int filter = argPos;
    int filtPos = 2;
    //return this.<KbCollection>getValuesForArg(Constants.argGenl(), 1, getPos, filter, filtPos, ctx);
    return Constants.argGenl().getValuesForArgPositionWithMatchArg(this, 1, valuePos, filter, filtPos, ctx);
  }
  
  @Override
  public Relation addArgGenl(int argPos, String colStr, String ctxStr) throws KbTypeException, CreateException {
    return addArgGenl(argPos, KbCollectionImpl.get(colStr), ContextImpl.get(ctxStr));
  }
  
  @Override
  public Relation addArgGenl(int argPos, KbCollection col, Context ctx) throws KbTypeException, CreateException {
    //addFact(ctx, Constants.argGenl(), 1, (Object) argPos, (Object) col);
    Constants.argGenl().addFact(ctx, this, argPos, col);
    return this;
  }

  //TODO: Add get/addInterArgIsa, get/addInterArgGenl
  @Override
  public List<Integer> getInterArgDifferent(Context ctx) {
    try {
      final List<Integer> differentArgs = new ArrayList<>();
      //Collection<Fact> facts = this.getFacts(Constants.getInstance().INTER_ARG_DIFF_PRED, 1, ctx);
      final Collection<Fact> facts = Constants.getInstance().INTER_ARG_DIFF_PRED.getFacts(this, 1, ctx);
      if (facts.isEmpty()) {
        return null;
      }
      final Fact first = facts.iterator().next();
      differentArgs.add(first.<Integer>getArgument(2));
      differentArgs.add(first.<Integer>getArgument(3));
      return differentArgs;
    } catch (KbException ex) {
      return null;
    }
  }

  @Override
  public Relation addInterArgDifferent(Integer argPosM, Integer argPosN, Context ctx) throws KbTypeException, CreateException {
    //addFact(ctx, Constants.getInstance().INTER_ARG_DIFF_PRED, 1, argPosM, argPosN);
    Constants.getInstance().INTER_ARG_DIFF_PRED.addFact(ctx, this, argPosM, argPosN);
    return this;
  }
  
  @Override
  public Integer getArity() throws VariableArityException {
    Context ctx = Constants.uvMt();
    if (arityValues == null) {
      try {
        //arityValues = this.<Integer>getValuesForArg(Constants.arity(), 1, 2, ctx);
        arityValues = Constants.arity().getValuesForArgPosition(this, 1, 2, ctx);
      } catch (IllegalArgumentException e) {
        throw new VariableArityException(this + " do not have an Integer arity.  Try using minArity and maxArity instead.", e);
      }
    }
    if (arityValues == null || arityValues.isEmpty()) {
      throw new IllegalArgumentException("There is no asserted arity for " + this);
    }
    return arityValues.iterator().next();
  }
  
  @Override
  public boolean isVariableArity() {
    if (isVariableArity == null) {
      isVariableArity = this.isInstanceOf(Constants.getInstance().VAR_ARITY_COL);
    }
    return isVariableArity;
  }
  
  @Override
  public Integer getArityMin() {
    try {
      final Context ctx = Constants.uvMt();
      final String command = makeSublStmt(WITH_MT, ctx.getCore(), makeNestedSublStmt(
                      SublConstants.getInstance().minArity.stringApiValue(), this.getCore()));
      final Object object = getAccess().converse().converseObject(command);
      if (object == null || object.equals(CycObjectFactory.nil)) {
        throw new IllegalArgumentException("No known min-arity for " + this);
      } else {
        return (Integer) object;
      }
    } catch (CycConnectionException e) {
      throw new KbRuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public Integer getArityMax() {
    try {
      final Context ctx = Constants.uvMt();
      final String command = makeSublStmt(WITH_MT, ctx.getCore(), makeNestedSublStmt(
                      SublConstants.getInstance().maxArity.stringApiValue(), this.getCore()));
      final Object object = getAccess().converse().converseObject(command);
      if (object == null || object.equals(CycObjectFactory.nil)) {
        throw new IllegalArgumentException("No known max-arity for " + this);
      } else {
        return (Integer) object;
      }
    } catch (CycConnectionException e) {
      throw new KbRuntimeException(e.getMessage(), e);
    }
  }
  
  /**
   * Provides a string describing the Relation's arity, for use as a substring in error messages and
   * whatnot.
   * 
   * @return a string describing the relation's arity
   */
  protected String describeArity() {
    return !isVariableArity()
            ? "requires exactly " + getArity() + " arguments"
            : "requires a minimum of " + getArityMin()
            + " and a maximum of " + getArityMax() + " arguments";
  }
  
  /**
   * Checks whether a vararg has an acceptable length wrt the Relation's arity, and throws an 
   * IllegalArgumentException if it does not.
   * 
   * @param forbidNullArgs throw an exception if any args are null
   * @param args 
   */
  protected void validateArgArity(boolean forbidNullArgs, Object... args) {
    if (args == null) {
      throw new NullPointerException("This " + getTypeString() + " " + describeArity() 
              + ", but received a null vararg");
    }
    try {
      if (!isVariableArity()) {
        if (getArity() != args.length) {
          throw new IllegalArgumentException("This " + getTypeString() + " " + describeArity()
                  + ", but " + args.length + " args were supplied");
        }
      } else if ((args.length < getArityMin()) || (args.length > getArityMax())) {
        throw new IllegalArgumentException("This " + getTypeString() + " " + describeArity() 
                + ", but " + args.length + " args were supplied");
      }
    } catch (IllegalArgumentException ex) {
      if (!Objects.toString(ex.getMessage(), "").startsWith("No known ")) {
        throw ex;
      }
      // Min or max arity is unknown, so we're going to trust that args.length is correct...
    }
    if (forbidNullArgs) {
      final StringBuilder sb = new StringBuilder();
      for (int i=0; i < args.length; i++) {
        if (args[i] == null) {
          sb.append(" arg").append(i + 1);
        }
        // TODO: should we also check for nil? - nwinant, 2017-07-26
      }
      if (sb.length() > 0) {
      throw new IllegalArgumentException("This " + getTypeString() + " " + describeArity() 
                + ", but the following args were null: " + sb);
      }
    }
  }
  
  protected void validateArgArity(Object... args) {
    validateArgArity(true, args);
  }
  
  @Override
  public Relation setArity(int arityValue) throws KbTypeException, CreateException {
    //addFact(Constants.uvMt(), Constants.arity(), 1, arityValue);
    Constants.arity().addFact(Constants.uvMt(), this, arityValue);
    return this;
  }

  @Override
  public Sentence setAritySentence(int arityValue) throws KbTypeException, CreateException {
    return new SentenceImpl(Constants.arity(), this, arityValue);
  }

  /**
   * Return the KBCollection as a KBObject of the Cyc term that underlies this class.
   *
   * @return KBCollectionImpl.get("#$Relation");
   */
  @Override
  public KbObject getType() {
    return getClassType();
  }

  /**
   * Return the KBCollection as a KBObject of the Cyc term that underlies this class.
   *
   * @return KBCollectionImpl.get("#$Relation");
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
    return "#$Relation";
  }
}
