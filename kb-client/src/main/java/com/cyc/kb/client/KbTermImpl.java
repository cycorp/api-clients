/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyc.kb.client;

/*
 * #%L
 * File: KbTermImpl.java
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
import com.cyc.base.cycobject.CycObject;
import com.cyc.base.cycobject.DenotationalTerm;
import com.cyc.base.cycobject.Fort;
import com.cyc.base.cycobject.Guid;
import com.cyc.base.exception.CycApiException;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.baseclient.cycobject.CycConstantImpl;
import com.cyc.kb.Context;
import com.cyc.kb.DefaultContext;
import com.cyc.kb.KbCollection;
import com.cyc.kb.KbIndividual;
import com.cyc.kb.KbObject;
import com.cyc.kb.KbStatus;
import com.cyc.kb.KbTerm;
import com.cyc.kb.Sentence;
import com.cyc.kb.client.config.KbConfiguration;
import com.cyc.kb.exception.CreateException;
import com.cyc.kb.exception.DeleteException;
import com.cyc.kb.exception.InvalidNameException;
import com.cyc.kb.exception.KbException;
import com.cyc.kb.exception.KbObjectNotFoundException;
import com.cyc.kb.exception.KbRuntimeException;
import com.cyc.kb.exception.KbTypeException;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;

import static com.cyc.kb.client.KbObjectImpl.getCore;

/**
 * A <code>KBTerm</code> is a facade for any #$CycLDenotationalTerm, but in the 
 * API its purpose is to create terms that are only known to be #$Thing. 
 *    
 * @param <T> type of CycObject core
 * 
 * @author Dave Schneider
 * @version $Id: KbTermImpl.java 176349 2017-12-19 01:38:11Z nwinant $
 */
public class KbTermImpl<T extends DenotationalTerm> extends KbObjectWithArityImpl<T> implements KbTerm {

  //====|    Fields    |==========================================================================//
  
  private static final DenotationalTerm TYPE_CORE =
          new CycConstantImpl("Thing", new Guid("bd5880f4-9c29-11b1-9dad-c379636f7270"));

  static DenotationalTerm getClassTypeCore() {
    return TYPE_CORE;
  }
  
  //====|    Construction    |====================================================================//

  /**
   * Not part of the KB API. This default constructor only has the effect of
   * ensuring that there is access to a Cyc server.
   */
  KbTermImpl() {
    super();
  }

  /**
   * Not part of the KB API. An implementation-dependent constructor.
   * <p>
   * It is used when the result of query is a CycObject and is known to be or
   * requested to be cast as an KBTerm.
   *
   * @param cycObject	the CycObject wrapped by KBTerm.
   * 
   * @throws KbTypeException 
   */
  KbTermImpl(DenotationalTerm cycObject) throws KbTypeException {
    super(cycObject);
  }

  /* *
   * EXPERIMENTAL!!! NOT PART OF THE KB API
   * @param termStr
   * @param l
   * @throws com.cyc.kb.exception.KbTypeException
   * @throws com.cyc.kb.exception.CreateException
   * /
  protected KbTermImpl(String termStr, List<Object> l) throws KbTypeException, CreateException {
    super(termStr, l);
  }
  */
  
  /**
   * This not part of the public, supported KB API. finds or creates an kb Term (#$Thing)
   * represented by termStr in the underlying KB
   * <p>
   *
   * @param termStr	the string representing a #$Thing in the KB
   * 
   * @throws CreateException if the #$Thing represented by termStr is not found
   * and could not be created
   * @throws KbTypeException is unlikely to be thrown, since everything is a #$Thing
   */
  KbTermImpl(String termStr) throws KbTypeException, CreateException  {
    super(termStr);
  }

  /**
   * This not part of the public, supported KB API. finds or creates; or finds an individual
   * represented by termStr in the underlying KB based on input ENUM
   * <p>
   *
   * @param termStr	the string representing a #$Thing in the KB
   * @param lookup the enum to specify LookupType: FIND or FIND_OR_CREATE
   * 
   * @throws CreateException 
   * @throws KbTypeException 
   *
   * @throws KbObjectNotFoundException	if the #$Thing represented by termStr is
   * not found and could not be created
   * @throws KbTypeException is unlikely to be thrown, since everything is a #$Thing
   */
  KbTermImpl(String termStr, LookupType lookup) throws KbTypeException, CreateException  {
    super(termStr, lookup);
  }
  
  protected KbTermImpl(DefaultContext contexts, KbTerm term) {
    super();
    this.setCore(term);
  }
  
  /**
   * Get the
   * <code>KBTerm</code> with the name
   * <code>nameOrId</code>. Throws exceptions if there is no KB term by that
   * name, or if it is not already an instance of #$Thing.
   *
   * @param nameOrId  the string representation or the HLID of the term
   * 
   * @return a new KBTerm
   * 
   * @throws KbTypeException
   * @throws CreateException 
   */
  public static KbTermImpl get(String nameOrId) throws KbTypeException, CreateException {
    return KbObjectImplFactory.get(nameOrId, KbTermImpl.class);
  }

  /**
   * Get the
   * <code>KBTerm</code> object that corresponds to
   * <code>cycObject</code>. Throws exceptions if the object isn't in the KB, or if
   * it's not already an instance of
   * <code>#$Thing</code>.
   *
   * @param cycObject the CycObject wrapped by KBTerm. The method
   * verifies that the CycObject is an #$Thing
   * 
   * @return a new KBTerm
   * 
   * @throws KbTypeException
   * @throws CreateException 
   */
  @Deprecated
  public static KbTermImpl get(CycObject cycObject) throws KbTypeException, CreateException {
    return KbObjectImplFactory.get(cycObject, KbTermImpl.class);
  }

  /**
   * Find or create a
   * <code>KBTerm</code> object named
   * <code>nameOrId</code>. If no object exists in the KB with the name
   * <code>nameOrId</code>, one will be created, and it will be asserted to be
   * an instance of
   * <code>#$Thing</code>. If there is already an object in the KB called
   * <code>nameOrId</code>, it will be returned.
   *
   * @param nameOrId the string representation or the HLID of the term
   * 
   * @return  a new KBTerm
   * 
   * @throws KbTypeException 
   * @throws CreateException 
   */
  public static KbTermImpl findOrCreate(String nameOrId) throws CreateException, KbTypeException {
    return KbObjectImplFactory.findOrCreate(nameOrId, KbTermImpl.class);
  }

  /**
   * Find or create a KBTerm object from
   * <code>cycObject</code>. If
   * <code>cycObject</code> exists in the KB, an appropriate
   * <code>KBTerm</code> object will be returned.
   *
   * @param cycObject the CycObject wrapped by KBTerm. The method
   * verifies that the CycObject is an #$Thing
   * 
   * @return  a new KBTerm
   * 
   * @throws KbTypeException 
   * @throws CreateException 
   */
  @Deprecated
  public static KbTermImpl findOrCreate(CycObject cycObject) throws CreateException, KbTypeException {
    return KbObjectImplFactory.findOrCreate(cycObject, KbTermImpl.class);
  }

  /**
   * Find or create a
   * <code>KBTerm</code> object named
   * <code>nameOrId</code>, and also make it an instance of
   * <code>constraintCol</code> in the default context specified by
   * {@link KBAPIDefaultContext#forAssertion()}. If no object
   * exists in the KB with the name
   * <code>nameOrId</code>, one will be created, and it will be asserted to be
   * an instance of
   * <code>constraintCol</code>. If there is already an object in the
   * KB called
   * <code>nameOrId</code>, and it is already a
   * <code>constraintCol</code>, it will be returned. If it is not
   * already a
   * <code>constraintCol</code>, but can be made so by addition of
   * assertions to the KB, such assertions will be made, and the object will be
   * returned. If the object in the KB cannot be turned into a
   * <code>constraintCol</code> by adding assertions, a
   * <code>KBTypeConflictException</code>will be thrown.
   *
   * @param nameOrId  the string representation or the HLID of the term
   * @param constraintCol the collection that this term will instantiate
   * 
   * @return a new KBTerm
   * 
   * @throws KbTypeException 
   * @throws CreateException 
   */
  public static KbTerm findOrCreate(String nameOrId, KbCollection constraintCol) 
      throws CreateException, KbTypeException {
    return KbObjectImplFactory.findOrCreate(nameOrId, constraintCol, KbTermImpl.class);
  }

  /**
   * Find or create a
   * <code>KBTerm</code> object named
   * <code>nameOrId</code>, and also make it an instance of
   * <code>constraintCol</code> in the default context specified by
   * {@link KBAPIDefaultContext#forAssertion()}. If no object
   * exists in the KB with the name
   * <code>nameOrId</code>, one will be created, and it will be asserted to be
   * an instance of
   * <code>constraintCol</code>. If there is already an object in the
   * KB called
   * <code>nameOrId</code>, and it is already a
   * <code>constraintCol</code>, it will be returned. If it is not
   * already a
   * <code>constraintCol</code>, but can be made so by addition of
   * assertions to the KB, such assertions will be made, and the object will be
   * returned. If the object in the KB cannot be turned into a
   * <code>constraintCol</code> by adding assertions, a
   * <code>KBTypeConflictException</code>will be thrown.
   *
   * @param nameOrId  the string representation or the HLID of the term
   * @param constraintColStr the string representation of the collection that 
   * this term will instantiate
   * 
   * @return a new KBTerm
   * 
   * @throws KbTypeException 
   * @throws CreateException 
   */
  public static KbTerm findOrCreate(String nameOrId, String constraintColStr) 
      throws CreateException, KbTypeException {
    return KbObjectImplFactory.findOrCreate(nameOrId, constraintColStr, KbTermImpl.class);
  }

  /**
   * Find or create a
   * <code>KBTerm</code> object named
   * <code>nameOrId</code>, and also make it an instance of
   * <code>constraintCol</code> in
   * <code>ctx</code>. If no object exists in the KB with the name
   * <code>nameOrId</code>, one will be created, and it will be asserted to be
   * an instance of
   * <code>constraintCol</code>. If there is already an object in the
   * KB called
   * <code>nameOrId</code>, and it is already a
   * <code>constraintCol</code>, it will be returned. If it is not
   * already a
   * <code>constraintCol</code>, but can be made so by addition of
   * assertions to the KB, such assertions will be made, and the object will be
   * returned. If the object in the KB cannot be turned into a
   * <code>constraintCol</code> by adding assertions, a
   * <code>KBTypeConflictException</code>will be thrown.
   *
   * @param nameOrId  the string representation or the HLID of the term
   * @param constraintCol the collection that this term will instantiate
   * @param ctx the context in which the resulting object must be an instance of
   * constraintCol
   * 
   * @return a new KBTerm
   * 
   * @throws KbTypeException 
   * @throws CreateException 
   */
  public static KbTerm findOrCreate(String nameOrId, KbCollection constraintCol, Context ctx) 
      throws CreateException, KbTypeException{
    return KbObjectImplFactory.findOrCreate(nameOrId, constraintCol, ctx, KbTermImpl.class);
  }

  /**
   * Find or create a
   * <code>KBTerm</code> object named
   * <code>nameOrId</code>, and also make it an instance of
   * <code>constraintCol</code> in
   * <code>ctx</code>. If no object exists in the KB with the name
   * <code>nameOrId</code>, one will be created, and it will be asserted to be
   * an instance of
   * <code>constraintCol</code>. If there is already an object in the
   * KB called
   * <code>nameOrId</code>, and it is already a
   * <code>constraintCol</code>, it will be returned. If it is not
   * already a
   * <code>constraintCol</code>, but can be made so by addition of
   * assertions to the KB, such assertions will be made, and the object will be
   * returned. If the object in the KB cannot be turned into a
   * <code>constraintCol</code> by adding assertions, a
   * <code>KBTypeConflictException</code>will be thrown.
   *
   * @param nameOrId the string representation or the HLID of the term
   * @param constraintColStr the string representation of the collection that 
   * this term will instantiate
   * @param ctxStr the string representation of the context in which the 
   * resulting object must be an instance of constraintCol
   * 
   * @return a new KBTerm
   * 
   * @throws KbTypeException 
   * @throws CreateException 
   */
  public static KbTerm findOrCreate(String nameOrId, String constraintColStr, String ctxStr) 
      throws CreateException, KbTypeException {
    return KbObjectImplFactory.findOrCreate(nameOrId, constraintColStr, ctxStr, KbTermImpl.class);
  }

  /**
   * Checks whether entity exists in KB and is an instance of #$Thing. If
   * false, {@link #getStatus(String)} may yield more information. This method
   * is equivalent to
   * <code>getStatus(nameOrId).equals(KBStatus.EXISTS_AS_TYPE)</code>.
   *
   * @param nameOrId either the name or HL ID of an entity in the KB
   * @return <code>true</code> if entity exists in KB and is an instance of
   * #$Thing
   */
  public static boolean existsAsType(String nameOrId)  {
    return getStatus(nameOrId).equals(KbStatus.EXISTS_AS_TYPE);
  }

  /**
   * Checks whether entity exists in KB and is an instance of #$Thing. If
   * false, {@link #getStatus(CycObject)} may yield more information. This
   * method is equivalent to
   * <code>getStatus(object).equals(KBStatus.EXISTS_AS_TYPE)</code>.
   *
   * @param cycObject the CycObject representation of a KB entity
   * @return <code>true</code> if entity exists in KB and is an instance of
   * #$Thing
   */
  public static boolean existsAsType(CycObject cycObject)  {
    return getStatus(cycObject).equals(KbStatus.EXISTS_AS_TYPE);
  }

  /**
   * Returns an KBStatus enum which describes whether
   * <code>nameOrId</code> exists in the KB and is an instance of
   * <code>#$Thing</code>.
   *
   * @param nameOrId either the name or HL ID of an entity in the KB
   * @return an enum describing the existential status of the entity in the KB
   */
  public static KbStatus getStatus(String nameOrId)  {
    return KbObjectImplFactory.getStatus(nameOrId, KbTermImpl.class);

  }

  /**
   * Returns an KBStatus enum which describes whether
   * <code>object</code> exists in the KB and is an instance of
   * <code>#$Thing</code>.
   *
   * @param cycObject the CycObject representation of a KB entity
   * @return an enum describing the existential status of the entity in the KB
   */
  public static KbStatus getStatus(CycObject cycObject)  {
    return KbObjectImplFactory.getStatus(cycObject, KbTermImpl.class);
  }

  @Override
  public <O extends Object> O replaceTerms(Map substitutions) 
          throws KbTypeException, CreateException {
    return (O) super.replaceTerms(substitutions);
  }
  
  @Override
  public boolean provablyNotInstanceOf(KbCollection col, Context ctx) {
    try {
      return getAccess().getInspectorTool().isa(this.getCore(), KbObjectImpl.getCore(col), KbObjectImpl.getCore(ctx));
    } catch (CycConnectionException e) {
      throw KbRuntimeException.fromThrowable(e);
    }
  }
  /*
  @Override
  public boolean provablyNotInstanceOf(String colStr, String ctxStr) {
    ContextImpl ctx;
    KbCollectionImpl col;
    try {
      ctx = ContextImpl.get(ctxStr);
      col = KbCollectionImpl.get(colStr);
    } catch (KbException kae){
      throw new IllegalArgumentException(kae);
    }
    return provablyNotInstanceOf(col, ctx);
  }
*/
  @Override
  public KbIndividual getCreator() {
    try {
      if (this.getCore() instanceof Fort) {
        Fort cyclist = getAccess().getLookupTool().getTermCreator((Fort) this.getCore());
        return KbIndividualImpl.get(cyclist);
      } else {
        return null;
      }
    } catch (CycConnectionException | CycApiException | KbTypeException | CreateException e) {
      return null;
    }
  }
  
  @Override
  public Date getCreationDate() {
    try {
      if (this.getCore() instanceof Fort) {
        return getAccess().getLookupTool().getTermCreationDate((Fort) this.getCore());
      } else {
        return null;
      }
    } catch (CycConnectionException | CycApiException | ParseException e) {
      return null;
    }
  }
  
  /**
   * Return the KBCollection as a KBObject of the Cyc term that 
   * underlies this class. 
   * 
   * @return KBCollectionImpl.get("#$Thing");
   */
  @Override
  public KbObject getType() {
    return getClassType();
  }
  
  /**
   * Return the KBCollection as a KBObject of the Cyc term that 
   * underlies this class. 
   * 
   * @return KBCollectionImpl.get("#$Thing");
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
    return "#$Thing";
  }
  
  @Override
  public KbTerm rename(final String name) throws InvalidNameException {
    if (getCore() instanceof CycConstant) {
      try {
        getAccess().getObjectTool().rename(((CycConstant) getCore()), name, true,
                KbConfiguration.getShouldTranscriptOperations());
      } catch (CycConnectionException e) {
        throw KbRuntimeException.fromThrowable("Unable to rename " + this + " to " + name, e);
      } catch (CycApiException cae) {
        throw InvalidNameException.fromThrowable(cae);
      }
      return this;
    } else {
      throw new UnsupportedOperationException("Couldn't rename " + getCore()
              + ". Not an atomic term (i.e. a CycConstant.) Check if the object isAtomic() before rename operation.");
    }
  }
  
  @Override
  public void delete() throws DeleteException {
    try {
      if (getCore() instanceof Fort) {
        getAccess().getUnassertTool().kill((Fort) getCore(), true, KbConfiguration.getShouldTranscriptOperations());
        this.setIsValid(false);
      } /*
       * else if (core instanceof CycAssertion) { CycAssertion ca =
       * (CycAssertion) core; if (ca.isGaf()){
       * cyc.unassertGaf(ca.getGaf(), ca.getMt()); } else { throw new
       * Exception ("Couldn't delete the fact: " + core.toString()); } }
       */ else {
        throw new DeleteException("Couldn't kill: "
                + getCore().toString()
                + ". It was not a Fort.");
      }
    } catch (CycConnectionException e) {
      throw KbRuntimeException.fromThrowable(
              "Couldn't kill the constant " + getCore().toString(), e);
    } catch (CycApiException cae) {
      throw KbRuntimeException.fromThrowable("Could not kill the constant: " + getCore()
              + " very likely because it is not in the KB. " + cae.getMessage(), cae);
    }
  }
  
  @Override
  public KbTerm instantiates(KbCollection col, Context ctx) throws KbTypeException, CreateException {
    Constants.isa().addFact(ctx, this, col);
    return this;
  }
  /*
  @Override
  public KbTerm instantiates(String colStr, String ctxStr) throws KbTypeException, CreateException {
    return instantiates(KbCollectionImpl.get(colStr), ContextImpl.get(ctxStr));
  }
  */
  @Override
  public KbTerm instantiates(KbCollection col) throws KbTypeException, CreateException {
    return instantiates(col, KbConfiguration.getDefaultContext().forAssertion());
  }

  @Override
  public Sentence instantiatesSentence(KbCollection col) throws KbTypeException, CreateException {
    return new SentenceImpl(Constants.isa(), this, (Object) col);
  }
  
  @Override
  public boolean isInstanceOf(KbCollection col) {
    try {
      return getAccess().getInspectorTool().isa(this.getCore(), (Fort) col.getCore());
    } catch (CycConnectionException e) {
      throw KbRuntimeException.fromThrowable(e);
    }
  }
  /*
  @Override
  public boolean isInstanceOf(String colStr) {
    return isInstanceOf(KbUtils.getKBObjectForArgument(colStr, KbCollectionImpl.class));
  }
  */
  @Override
  public boolean isInstanceOf(KbCollection col, Context ctx) {
    try {
      return getAccess().getInspectorTool().isa(this.getCore(), getCore(col), getCore(ctx));
    } catch (CycConnectionException e) {
      throw KbRuntimeException.fromThrowable(e);
    }
  }
  /*
  @Override
  public boolean isInstanceOf(String colStr, String ctxStr) {
    return isInstanceOf(
            KbUtils.getKBObjectForArgument(colStr, KbCollectionImpl.class), 
            KbUtils.getKBObjectForArgument(ctxStr, ContextImpl.class));
  }
  */
  @Override
  public KbTerm addQuotedIsa(KbCollection coll, Context ctx) throws KbTypeException, CreateException {
    super.addQuotedIsa(coll, ctx);
    return this;
  }
  
  @Override
  public <O> O getArgument(int getPos) throws KbTypeException, CreateException {
    return super.getArgument(getPos);
  }

}
