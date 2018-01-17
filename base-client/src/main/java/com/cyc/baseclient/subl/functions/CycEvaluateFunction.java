/*
 * Copyright 2017 Cycorp, Inc..
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
package com.cyc.baseclient.subl.functions;

/*
 * #%L
 * File: CycEvaluateFunction.java
 * Project: Base Client
 * %%
 * Copyright (C) 2013 - 2017 Cycorp, Inc.
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
import com.cyc.base.cycobject.CycConstant;
import com.cyc.base.cycobject.CycList;
import com.cyc.base.cycobject.CycObject;
import com.cyc.base.cycobject.FormulaSentence;
import com.cyc.base.exception.CycApiException;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.baseclient.CycObjectFactory;
import com.cyc.baseclient.cycobject.CycConstantImpl;
import com.cyc.baseclient.cycobject.FormulaSentenceImpl;
import com.cyc.baseclient.cycobject.GuidImpl;
import com.cyc.baseclient.subl.subtypes.SublObjectSingleArgFunction;

import static com.cyc.baseclient.connection.SublApiHelper.makeNestedSublStmt;
import static com.cyc.baseclient.connection.SublApiHelper.makeSublStmt;

/**
 * Evaluate the evaluatable EXPRESSION and return the result.
 * 
 * E.g., <code>(CYC-EVALUATE '(#$IndexicalReferentFn #$Now-Indexical))</code>
 * 
 * @author nwinant
 */
public class CycEvaluateFunction extends SublObjectSingleArgFunction<CycObject> {
  
  
  //====|    CycEvaluateFunction_Indexical    |=================================================//
  
  /**
   * Special case of CycEvaluateFunction, which wraps the input term in #$IndexicalReferentFn.
   * 
   * @see com.cyc.baseclient.subl.functions.SublFunctions#INDEXICAL_P
   */
  public static class CycEvaluateFunction_Indexical extends CycEvaluateFunction {
    
    public static final CycConstant INDEXICAL_REFERENT_FN = new CycConstantImpl(
            "IndexicalReferentFn",
            new GuidImpl("1b46daf6-0d55-41d7-87b3-ba2021c20274"));
    
    @Override
    public Object eval(CycAccess access, CycObject arg)
            throws CycConnectionException, CycApiException {
      final FormulaSentence expr
              = FormulaSentenceImpl.makeFormulaSentence(INDEXICAL_REFERENT_FN, arg);
      return super.eval(access, expr);
    }
    
  }
  
  
  //====|    UnevaluatableExpressionException    |=================================================//
  
  /**
   * Thrown when Cyc cannot evaluate an expression via CYC-EVALUATE. This is distinct from other,
   * more general API exceptions.
   */
  public static class UnevaluatableExpressionException extends CycApiException {
    
    private final CycObject expression;

    private UnevaluatableExpressionException(String msg, CycObject expression) {
      super(msg);
      this.expression = expression;
    }

    public CycObject getExpression() {
      return this.expression;
    }

  }
  
  
  //====|    Fields    |==========================================================================//
  
  public static final String FUNCTION_NAME = "cyc-evaluate";
  
  public static final CycEvaluateFunction_Indexical CYC_EVALUATE_INDEXICAL
          = new CycEvaluateFunction_Indexical();
  
  
  //====|    Construction    |====================================================================//

  public CycEvaluateFunction() {
    super(FUNCTION_NAME);
  }
  
  
  //====|    Methods    |=========================================================================//
  
  @Override
  public String buildCommand(CycObject arg) throws CycApiException {
    if (!isArgValid(arg)) {
      handleInvalidArg(arg);
    }
    return makeSublStmt("multiple-value-list", makeNestedSublStmt(this.getSymbol(), arg));
  }
  
  /**
   * Evaluates the function and returns an Object value.
   * 
   * @param access
   * @param arg
   * @return the result of the function, as a string
   * @throws CycConnectionException
   * @throws CycApiException 
   * @throws UnevaluatableExpressionException if Cyc cannot evaluate the expression.
   */
  @Override
  public Object eval(CycAccess access, CycObject arg)
          throws CycConnectionException, CycApiException, UnevaluatableExpressionException {
    final CycList results = access.converse().converseList(buildCommand(arg));
    if ((results == null)
            || (results.size() < 2)
            || (!CycObjectFactory.t.equals(results.get(1)))) {
      String msg = "Unevaluatable: " + arg;
      //LOGGER.debug("{}\n    {}", msg, results);
      throw new UnevaluatableExpressionException(msg, arg);
    }
    return results.get(0);
  }
  
  public boolean isEvaluatable(CycAccess access, CycObject arg) 
          throws CycConnectionException, CycApiException {
    try {
      eval(access, arg);
      return true;
    } catch (UnevaluatableExpressionException ex) {
      return false;
    }
  }
  
}
