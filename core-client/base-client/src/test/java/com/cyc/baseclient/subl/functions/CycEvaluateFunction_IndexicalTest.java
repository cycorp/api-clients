package com.cyc.baseclient.subl.functions;

/*
 * #%L
 * File: CycEvaluateFunction_IndexicalTest.java
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

import com.cyc.base.cycobject.CycConstant;
import com.cyc.base.cycobject.CycList;
import com.cyc.base.cycobject.CycObject;
import com.cyc.base.exception.CycApiException;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.baseclient.cycobject.FormulaSentenceImpl;
import com.cyc.baseclient.datatype.DateConverter;
import static com.cyc.baseclient.subl.functions.CycEvaluateFunction.CYC_EVALUATE_INDEXICAL;
import com.cyc.baseclient.subl.functions.CycEvaluateFunction.UnevaluatableExpressionException;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author nwinant
 */
public class CycEvaluateFunction_IndexicalTest extends AbstractSublFunctionTest {
  
  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    indexicalsResolvableToDates = Arrays.asList(
            access.getLookupTool().getConstantByName("#$Now-Indexical"),
            access.getLookupTool().getConstantByName("#$Now"),
            access.getLookupTool().getConstantByName("#$TheYear-Indexical"),
            access.getLookupTool().getConstantByName("#$Yesterday-Indexical"),
            access.getLookupTool().getConstantByName("#$Tomorrow-Indexical"),
            access.getLookupTool().getConstantByName("#$Today-Indexical"));
    indexicalsResolvableToMts = Arrays.asList(
            access.getLookupTool().getConstantByName("#$QueryMt"),
            access.getLookupTool().getConstantByName("#$ParaphraseDomainMt"),
            access.getLookupTool().getConstantByName("#$ParaphraseLanguageMt"),
            access.getLookupTool().getConstantByName("#$ParaphraseDomainMt"),
            access.getLookupTool().getConstantByName("#$ParaphraseLanguageMt"));
    indexicalsResolvableToTerms = Arrays.asList(
            access.getLookupTool().getConstantByName("#$TheCycProcessOwner"),
            access.getLookupTool().getConstantByName("#$TheUser"));
    indexicalsResolvableToStrings = Arrays.asList(
            access.getLookupTool().getConstantByName("#$TheCurrentHostName"),
            access.getLookupTool().getConstantByName("#$TheCurrentSystemNumber"),
            access.getLookupTool().getConstantByName("#$TheCurrentKBNumber"));
    indexicalsResolvableToIntegers = Arrays.asList(
            access.getLookupTool().getConstantByName("#$SecondsSince1970-Indexical"));
    unevaluatableIndexicals = Arrays.asList(access.getLookupTool().getConstantByName("#$ThisProblemStore"),
            access.getLookupTool().getConstantByName("#$ThisInference"),
            FormulaSentenceImpl
            .makeCycSentence(access, "(TheNamedFn SetOrCollection \"indexical 1\")"));
    nonIndexicals = Arrays.asList(
            access.getLookupTool().getConstantByName("#$Dog"),
            access.getLookupTool().getConstantByName("#$Cat"));
  }
  
  
  // Fields
  
  public List<CycConstant> indexicalsResolvableToDates;
  public List<CycConstant> indexicalsResolvableToMts;
  public List<CycConstant> indexicalsResolvableToTerms;
  public List<CycConstant> indexicalsResolvableToStrings;
  public List<CycConstant> indexicalsResolvableToIntegers;
  public List<CycObject>   unevaluatableIndexicals;
  public List<CycConstant> nonIndexicals;
  
  
  // Tests
  
  @Test
  public void testEval_resolvable_indexical_Dates() throws CycApiException, CycConnectionException {
    for (CycObject indexical : indexicalsResolvableToDates) {
      System.out.println("Indexical: " + indexical);
      final Object result = CYC_EVALUATE_INDEXICAL.eval(access, indexical);
      System.out.println("    Class: "
              + ((result != null) ? result.getClass().getSimpleName() : null));
      System.out.println(" Cyc date? " + DateConverter.isCycDate(result));
      System.out.println("        => " + result);
      assertNotNull(result);
      assertTrue(DateConverter.isCycDate(result));
    }
  }
  
  @Test
  public void testEval_resolvable_indexical_Mts() throws CycApiException, CycConnectionException {
    for (CycObject indexical : indexicalsResolvableToMts) {
      System.out.println("Indexical: " + indexical);
      final Object result = CYC_EVALUATE_INDEXICAL.eval(access, indexical);
      System.out.println("    Class: "
              + ((result != null) ? result.getClass().getSimpleName() : null));
      System.out.println("        => " + result);
      assertNotNull(result);
      // TODO: Check whether it's *actually* an Mt. - nwinant, 2017-06-28
      assertTrue((result instanceof CycList) || (result instanceof CycConstant));
    }
  }
  
  @Test
  public void testEval_resolvable_indexical_Terms() throws CycApiException, CycConnectionException {
    for (CycObject indexical : indexicalsResolvableToTerms) {
      System.out.println("Indexical: " + indexical);
      final Object result = CYC_EVALUATE_INDEXICAL.eval(access, indexical);
      System.out.println("    Class: "
              + ((result != null) ? result.getClass().getSimpleName() : null));
      System.out.println("        => " + result);
      assertNotNull(result);
      assertTrue(result instanceof CycObject);
    }
  }
  
  @Test
  public void testEval_resolvable_indexical_Strings() throws CycApiException, CycConnectionException {
    for (CycObject indexical : indexicalsResolvableToStrings) {
      System.out.println("Indexical: " + indexical);
      final Object result = CYC_EVALUATE_INDEXICAL.eval(access, indexical);
      System.out.println("    Class: " 
              + ((result != null) ? result.getClass().getSimpleName() : null));
      System.out.println("        => " + result);
      assertNotNull(result);
      assertTrue(result instanceof String);
    }
  }
  
  @Test
  public void testEval_resolvable_indexical_Integers() throws CycApiException, CycConnectionException {
    for (CycObject indexical : indexicalsResolvableToIntegers) {
      System.out.println("Indexical: " + indexical);
      final Object result = CYC_EVALUATE_INDEXICAL.eval(access, indexical);
      System.out.println("    Class: " 
              + ((result != null) ? result.getClass().getSimpleName() : null));
      System.out.println("        => " + result);
      assertNotNull(result);
      assertTrue(result instanceof Integer);
    }
  }
  
  @Test
  public void testEval_unevaluatable_Indexicals() throws CycApiException, CycConnectionException {
    for (CycObject indexical : unevaluatableIndexicals) {
      System.out.println("Indexical: " + indexical);
      UnevaluatableExpressionException result = null;
      try {
        CYC_EVALUATE_INDEXICAL.eval(access, indexical);
      } catch (UnevaluatableExpressionException ex) {
        result = ex;
      }
      assertNotNull(result);
    }
  }
  
  @Test
  public void testEval_unevaluatable_NonIndexicals() throws CycApiException, CycConnectionException {
    for (CycObject indexical : nonIndexicals) {
      System.out.println("Indexical: " + indexical);
      UnevaluatableExpressionException result = null;
      try {
        CYC_EVALUATE_INDEXICAL.eval(access, indexical);
      } catch (UnevaluatableExpressionException ex) {
        result = ex;
      }
      assertNotNull(result);
    }
  }
  
  @Test
  public void testIsEvaluatable_indexicals() throws Exception {
    for (CycObject indexical : indexicalsResolvableToMts) {
      System.out.println("Should be evaluatable    : " + indexical);
      assertTrue(CYC_EVALUATE_INDEXICAL.isEvaluatable(access, indexical));
    }
    for (CycObject indexical : indexicalsResolvableToDates) {
      System.out.println("Should be evaluatable    : " + indexical);
      assertTrue(CYC_EVALUATE_INDEXICAL.isEvaluatable(access, indexical));
    }
    for (CycObject indexical : indexicalsResolvableToTerms) {
      System.out.println("Should be evaluatable    : " + indexical);
      assertTrue(CYC_EVALUATE_INDEXICAL.isEvaluatable(access, indexical));
    }
    for (CycObject indexical : indexicalsResolvableToStrings) {
      System.out.println("Should be evaluatable    : " + indexical);
      assertTrue(CYC_EVALUATE_INDEXICAL.isEvaluatable(access, indexical));
    }
    for (CycObject indexical : indexicalsResolvableToIntegers) {
      System.out.println("Should be evaluatable    : " + indexical);
      assertTrue(CYC_EVALUATE_INDEXICAL.isEvaluatable(access, indexical));
    }
    for (CycObject indexical : unevaluatableIndexicals) {
      System.out.println("Should NOT be evaluatable: " + indexical);
      assertFalse(CYC_EVALUATE_INDEXICAL.isEvaluatable(access, indexical));
    }
    for (CycObject indexical : nonIndexicals) {
      System.out.println("Should NOT be evaluatable: " + indexical);
      assertFalse(CYC_EVALUATE_INDEXICAL.isEvaluatable(access, indexical));
    }
  }
  
}
