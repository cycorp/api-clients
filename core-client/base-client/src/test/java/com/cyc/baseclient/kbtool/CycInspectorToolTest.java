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
package com.cyc.baseclient.kbtool;

/*
 * #%L
 * File: CycInspectorToolTest.java
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

import com.cyc.base.cycobject.CycObject;
import com.cyc.base.cycobject.NonAtomicTerm;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.baseclient.CommonConstants;
import com.cyc.baseclient.cycobject.NautImpl;
import com.cyc.baseclient.datatype.DateConverter;
import static com.cyc.baseclient.testing.TestUtils.getCyc;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static com.cyc.baseclient.testing.TestConstants.SAMPLE_COMPUTER_DATABASE_PROGRAM_OBJECT_CMLS;
import static com.cyc.baseclient.testing.TestConstants.SAMPLE_COMPUTER_DATABASE_SUPPLIER_CMLS;

/**
 *
 * @author daves
 */
public class CycInspectorToolTest {

  NonAtomicTerm schemaObjectFnWithSchemaIsa = new NautImpl(CommonConstants.SCHEMA_OBJECT_FN, SAMPLE_COMPUTER_DATABASE_SUPPLIER_CMLS, "Fritz the French Fry Guy");
 
  NonAtomicTerm schemaObjectFnWithoutSchemaIsa = new NautImpl(CommonConstants.SCHEMA_OBJECT_FN, SAMPLE_COMPUTER_DATABASE_PROGRAM_OBJECT_CMLS, "Fritz the French Fry Guy");
  NonAtomicTerm twelvePercent = new NautImpl(CommonConstants.PERCENT, 12);
  NonAtomicTerm july1776 = new NautImpl(CommonConstants.MONTH_FN, DateConverter.JULY, new NautImpl(CommonConstants.YEAR_FN, 1776));

  public CycInspectorToolTest() {
  }

  @BeforeClass
  public static void setUpClass() {

  }

  @AfterClass
  public static void tearDownClass() {
  }

  /**
   * Test of getKnownCategoryForNat method, of class CycInspectorTool.
   *
   * @throws com.cyc.base.exception.CycConnectionException
   */
  @Test
  public void testGetKnownCategoryForNat() throws CycConnectionException {
    //assumes that SampleComputerDatabaseSupplier-CMLS has a schemaIsa assertion.
    System.out.println("getKnownCategoryForNat");
    CycInspectorTool instance = (CycInspectorTool) (getCyc().getInspectorTool());
    CycObject expResult = CommonConstants.INDIVIDUAL;
    CycObject result = instance.getKnownCategoryForNat(schemaObjectFnWithSchemaIsa);
    assertEquals(expResult, result);

    expResult = null;
    result = instance.getKnownCategoryForNat(schemaObjectFnWithoutSchemaIsa);
    assertEquals(expResult, result);

  }

  /**
   * Test of getKnownCategoryForNat method, of class CycInspectorTool.
   *
   * @throws com.cyc.base.exception.CycConnectionException
   */
  @Test
  public void testGetCategoryForFunctor() throws CycConnectionException {
    System.out.println("getCategoryForFunctor");
    CycInspectorTool instance = (CycInspectorTool) (getCyc().getInspectorTool());
    CycObject expResult = CommonConstants.INDIVIDUAL;
    CycObject result = instance.getCategoryForFunctor(twelvePercent.getFunctor());
    assertEquals(expResult, result);

    twelvePercent = new NautImpl(CommonConstants.MONTH_FN, DateConverter.JULY, new NautImpl(CommonConstants.YEAR_FN, 1776));
    expResult = CommonConstants.INDIVIDUAL;
    result = instance.getCategoryForFunctor(twelvePercent.getFunctor());
    assertEquals(expResult, result);

  }

  /**
   * Test of getKnownCategoryForNat method, of class CycInspectorTool.
   *
   * @throws com.cyc.base.exception.CycConnectionException
   */
  @Test
  public void testCategorizeTermWrtApi() throws CycConnectionException {
    System.out.println("categorizeTermWrtApi");
    CycInspectorTool instance = (CycInspectorTool) (getCyc().getInspectorTool());
    CycObject expResult = CommonConstants.INDIVIDUAL;
    CycObject result = instance.categorizeTermWRTApi(twelvePercent);
    assertEquals(expResult, result);
    result = instance.categorizeTermWRTApi(july1776);
    assertEquals(expResult, result);
    result = instance.categorizeTermWRTApi(schemaObjectFnWithSchemaIsa);
    assertEquals(expResult, result);
    expResult = CommonConstants.THING;
    result = instance.categorizeTermWRTApi(schemaObjectFnWithoutSchemaIsa);
    assertEquals(expResult, result);

  }

}
