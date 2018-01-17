/*
 * Copyright 2015 Cycorp, Inc.
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
package com.cyc.kb.client.services;

/*
 * #%L
 * File: AssertionServiceImpl.java
 * Project: KB Client
 * %%
 * Copyright (C) 2013 - 2018 Cycorp, Inc
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
import com.cyc.base.cycobject.CycList;
import com.cyc.base.cycobject.CycObject;
import com.cyc.base.exception.CycApiException;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.baseclient.connection.SublApiHelper;
import com.cyc.baseclient.cycobject.DefaultCycObjectImpl;
import com.cyc.kb.Assertion;
import com.cyc.kb.Assertion.Direction;
import com.cyc.kb.Assertion.Strength;
import com.cyc.kb.Context;
import com.cyc.kb.KbPredicate;
import com.cyc.kb.Sentence;
import com.cyc.kb.client.AssertionImpl;
import com.cyc.kb.client.ContextImpl;
import com.cyc.kb.client.KbObjectImplFactory;
import com.cyc.kb.client.config.KbConfiguration;
import com.cyc.kb.exception.CreateException;
import com.cyc.kb.exception.KbObjectNotFoundException;
import com.cyc.kb.exception.KbTypeException;
import com.cyc.kb.spi.AssertionService;
import com.cyc.session.exception.SessionRuntimeException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author nwinant
 */
public class AssertionServiceImpl implements AssertionService {
  
  private static final Logger LOG = LoggerFactory.getLogger(AssertionServiceImpl.class);
  
  @Override
  public Assertion get(String hlid) throws KbTypeException, CreateException {
    return AssertionImpl.get(hlid);
  }
  
  @Override
  public Assertion get(String formulaStr, String ctxStr) throws KbTypeException, CreateException {
    return AssertionImpl.get(formulaStr, ctxStr);
  }
  
  @Override
  public Assertion get(Sentence formula, Context ctx) throws KbTypeException, CreateException {
    return AssertionImpl.get(formula, ctx);
  }
  
  @Override
  public Assertion findOrCreate(String formulaStr, String ctxStr, Strength s, Direction d) throws CreateException, KbTypeException {
    return AssertionImpl.findOrCreate(formulaStr, ctxStr, s, d);
  }
  
  @Override
  public Assertion findOrCreate(Sentence formula, Context ctx, Strength s, Direction d) throws CreateException, KbTypeException {
    return AssertionImpl.findOrCreate(formula, ctx, s, d);
  }
  
  @Override
  public Assertion findOrCreate(String formulaStr) throws CreateException, KbTypeException {
    return AssertionImpl.findOrCreate(formulaStr, KbConfiguration.getDefaultContext().forAssertion().toString());
  }

  @Override
  public Assertion findOrCreate(String formulaStr, String ctxStr) throws CreateException, KbTypeException {
    return AssertionImpl.findOrCreate(formulaStr, ctxStr, Strength.AUTO, Direction.AUTO);
  }

  @Override
  public Assertion findOrCreate(Sentence formula) throws KbTypeException, CreateException {
    return AssertionImpl.findOrCreate(formula, KbConfiguration.getDefaultContext().forAssertion());
  }

  @Override
  public Assertion findOrCreate(Sentence formula, Context ctx) throws KbTypeException, CreateException {
    return AssertionImpl.findOrCreate(formula, ctx, Strength.AUTO, Direction.AUTO);
  }
  
  private void validateContextAllowed(CycAccess cyc, Context ctx, boolean allowExpensiveContexts)
          throws SessionRuntimeException, IllegalArgumentException {
    try {
      if (!allowExpensiveContexts
                  && cyc.getInspectorTool().isBroadMt(ContextImpl.asELMt(ctx))) {
        throw new IllegalArgumentException(
                ctx + " is a broad mt, which is not allowed for this function call");
      }
    } catch (CycConnectionException | CycApiException ex) {
      throw ex.toSessionException();
    }
  }
  
  /**
   * Returns all of the assertions that are directly asserted within an Mt. This method specifically
   * does not return any assertions that are merely visible from the Mt.
   * <p>
   * There are cases where this method is useful, such as in general-purpose knowledge editing
   * applications for the Cyc KB, but it should be used with caution. <strong>This method is
   * currently not included in the official KB API</strong> for two reasons:
   * <ol>
   *   <li>In practice, it's rarely used: most applications never need to use it, and it would 
   *       in fact be the wrong approach.
   *       What is much more commonly useful is retrieving relevant answers to a specific question 
   *       (typically as KbTerms from a query) in which case what matters is an assertion's 
   *       <em>visibility</em> from an Mt.
   *       When applications directly interact with assertions, it is typically by retrieving the
   *       predicate extent for some relevant KbPredicate, or to create or delete specific 
   *       assertions whose Mt is either already known or easily retrievable.</li>
   *   <li>It's potentially very expensive. Retrieving the contents of a small, application-focused
   *       Mt is relatively quick, but retrieving the contents of a broad Mt like 
   *       {@code #$UniversalVocabularyMt} may take upwards of an hour or possibly even longer. Even
   *       some Mts like {@code #$CurrentWorldDataCollectorMt-NonHomocentric} may contain ~10,000 
   *       assertions.</li>
   * </ol>
   * 
   * @param ctx                    the specified Context
   * @param allowExpensiveContexts whether to allow this method to run on broad Mts. If
   *                               {@code false}, this method will throw an exception when run
   *                               against such Mts.
   *
   * @return all of the Assertions asserted directly in the specified Context
   *
   * @throws KbTypeException
   * @throws CreateException
   * @throws IllegalArgumentException if given an expensive Context when not allowed
   *
   * @see KbPredicate#getExtent() 
   * @see KbPredicate#getExtent(com.cyc.kb.Context) 
   * @see KbPredicate#getFact(com.cyc.kb.Context, java.lang.Object...) 
   * @see KbPredicate#getFacts(java.lang.Object, int, com.cyc.kb.Context) 
   * @see KbPredicate#getValuesForArgPosition(java.lang.Object, int, int, com.cyc.kb.Context) 
   * @see KbPredicate#getValuesForArgPositionWithMatchArg(java.lang.Object, int, int, java.lang.Object, int, com.cyc.kb.Context) 
   */
  public List<Assertion> getAllAssertedInContext(Context ctx, boolean allowExpensiveContexts) 
          throws KbTypeException, CreateException, IllegalArgumentException {
    try {
      final ArrayList<Assertion> results = new ArrayList();
      final CycAccess cyc = CycAccess.getCurrent();
      validateContextAllowed(cyc, ctx, allowExpensiveContexts);
      final String cmd = SublApiHelper.makeSublStmt("GATHER-MT-INDEX", ContextImpl.asELMt(ctx));
      final CycList<CycObject> cycObjects = cyc.converse().converseList(cmd);
      for (CycObject cycObject : cycObjects) {
        try {
          results.add(KbObjectImplFactory.get(cycObject, AssertionImpl.class));
        } catch (KbObjectNotFoundException ex) {
          final String id = DefaultCycObjectImpl.toCompactExternalId(cycObject, cyc);
          LOG.error("Could not convert to Assertion. Id: {}    CycL: {}", id, cycObject);
        }
      }
      results.trimToSize();
      return results;
    } catch (CycConnectionException | CycApiException ex) {
      throw ex.toSessionException();
    }
  }
  
  /**
   * Returns all of the assertions that are directly asserted within an Mt. This method specifically
   * does not return any assertions that are merely visible from the Mt, and will throw an 
   * IllegalArgumentException when called on expensive/broad Mts.
   * <p>
   * <strong>This method is currently not included in the official KB API</strong>, and it should be
   * used with caution. For more details, see 
   * {@link #getAllAssertedInContext(com.cyc.kb.Context, boolean) }.
   * 
   * @param ctx the specified Context
   *
   * @return all of the Assertions asserted directly in the specified Context
   *
   * @throws KbTypeException
   * @throws CreateException
   * @throws IllegalArgumentException if given an expensive Context
   *
   * @see KbPredicate#getExtent() 
   * @see KbPredicate#getExtent(com.cyc.kb.Context) 
   * @see KbPredicate#getFact(com.cyc.kb.Context, java.lang.Object...) 
   * @see KbPredicate#getFacts(java.lang.Object, int, com.cyc.kb.Context) 
   * @see KbPredicate#getValuesForArgPosition(java.lang.Object, int, int, com.cyc.kb.Context) 
   * @see KbPredicate#getValuesForArgPositionWithMatchArg(java.lang.Object, int, int, java.lang.Object, int, com.cyc.kb.Context) 
   */
  public List<Assertion> getAllAssertedInContext(Context ctx) 
          throws KbTypeException, CreateException, IllegalArgumentException {
    return getAllAssertedInContext(ctx, false); 
  }
  
  /**
   * Returns the number of assertions that are directly asserted within an Mt. This method 
   * specifically does not return any assertions that are merely visible from the Mt. 
   * <p>
   * <strong>This method is currently not included in the official KB API</strong>, and it should be
   * used with caution. For more details, see 
   * {@link #getAllAssertedInContext(com.cyc.kb.Context, boolean) }.
   * 
   * @param ctx                    the specified Context
   * @param allowExpensiveContexts whether to allow this method to run on broad Mts. If
   *                               {@code false}, this method will throw an exception when run
   *                               against such Mts.
   * 
   * @return a count of all of the Assertions asserted directly in the specified Context
   * 
   * @throws KbTypeException
   * @throws CreateException
   * @throws IllegalArgumentException if given an expensive Context when not allowed
   */
  public int getCountOfAllAssertedInContext(Context ctx, boolean allowExpensiveContexts)
          throws KbTypeException, CreateException, IllegalArgumentException {
    try {
      final CycAccess cyc = CycAccess.getCurrent();
      validateContextAllowed(cyc, ctx, allowExpensiveContexts);
      final String cmd = SublApiHelper.makeSublStmt("NUM-MT-CONTENTS", ContextImpl.asELMt(ctx));
      return cyc.converse().converseInt(cmd);
    } catch (CycConnectionException | CycApiException ex) {
      throw ex.toSessionException();
    }
  }
  
  /**
   * Returns the number of assertions that are directly asserted within an Mt. This method 
   * specifically does not return any assertions that are merely visible from the Mt, and will throw
   * an IllegalArgumentException when called on expensive/broad Mts.
   * <p>
   * <strong>This method is currently not included in the official KB API</strong>, and it should be
   * used with caution. For more details, see 
   * {@link #getAllAssertedInContext(com.cyc.kb.Context, boolean) }.
   * 
   * @param ctx the specified Context
   * 
   * @return a count of all of the Assertions asserted directly in the specified Context
   * 
   * @throws KbTypeException
   * @throws CreateException
   * @throws IllegalArgumentException if given an expensive Context
   */
  public int getCountOfAllAssertedInContext(Context ctx)
          throws KbTypeException, CreateException, IllegalArgumentException {
    return getCountOfAllAssertedInContext(ctx, false);
  }

}
