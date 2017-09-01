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
package com.cyc.kb.service;

/*
 * #%L
 * File: KbClientImplTest.java
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

import com.cyc.kb.client.TestConstants;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author nwinant
 */
public class KbClientImplTest {
  
  public KbClientImplTest() {
  }
  
  @BeforeClass
  public static void setUpClass() throws Exception {
    TestConstants.ensureInitialized();
  }
  
  @AfterClass
  public static void tearDownClass() {
  }
  
  @Before
  public void setUp() {
    instance = new KbClientImpl();
  }
  
  @After
  public void tearDown() {
  }
  
  
  // Fields
  
  private KbClientImpl instance = new KbClientImpl();
  
  
  // Tests
  
  @Test
  public void testGetAssertionService() {
    System.out.println("getAssertionService");
    AssertionServiceImpl result = instance.assertion();
    assertNotNull(result);
    assertNotNull(result.toString());
  }
  
  @Test
  public void testGetBinaryPredicateService() {
    System.out.println("getBinaryPredicateService");
    BinaryPredicateServiceImpl result = instance.binaryPredicate();
    assertNotNull(result);
    assertNotNull(result.toString());
  }
  
  @Test
  public void testGetCollectionService() {
    System.out.println("getCollectionService");
    KbCollectionServiceImpl result = instance.collection();
    assertNotNull(result);
    assertNotNull(result.toString());
  }
  
  @Test
  public void testGetContextService() {
    System.out.println("getContextService");
    ContextServiceImpl result = instance.context();
    assertNotNull(result);
    assertNotNull(result.toString());
  }
  
  @Test
  public void testGetConvenienceService() {
    System.out.println("getConvenienceService");
    KbServiceImpl result = instance.kb();
    assertNotNull(result);
    assertNotNull(result.toString());
  }
  
  @Test
  public void testGetFactService() {
    System.out.println("getFactService");
    FactServiceImpl result = instance.fact();
    assertNotNull(result);
    assertNotNull(result.toString());
  }
  
  @Test
  public void testGetFirstOrderCollectionService() {
    System.out.println("getFirstOrderCollectionService");
    FirstOrderCollectionServiceImpl result = instance.firstOrderCollection();
    assertNotNull(result);
    assertNotNull(result.toString());
  }
  
  @Test
  public void testGetFunctionService() {
    System.out.println("getFunctionService");
    KbFunctionServiceImpl result = instance.function();
    assertNotNull(result);
    assertNotNull(result.toString());
  }
  
  @Test
  public void testGetIndividualService() {
    System.out.println("getIndividualService");
    KbIndividualServiceImpl result = instance.individual();
    assertNotNull(result);
    assertNotNull(result.toString());
  }
  /*
  @Test
  public void testGetKbObjectService() {
    System.out.println("getKbObjectService");
    KbObjectServiceImpl result = instance.kbObject();
    assertNotNull(result);
    assertNotNull(result.toString());
  }
  */
  @Test
  public void testGetPredicateService() {
    System.out.println("getPredicateService");
    KbPredicateServiceImpl result = instance.predicate();
    assertNotNull(result);
    assertNotNull(result.toString());
  }
  
  @Test
  public void testGetRelationService() {
    System.out.println("getRelationService");
    RelationServiceImpl result = instance.relation();
    assertNotNull(result);
    assertNotNull(result.toString());
  }
  
  @Test
  public void testGetRuleService() {
    System.out.println("getRuleService");
    RuleServiceImpl result = instance.rule();
    assertNotNull(result);
    assertNotNull(result.toString());
  }

  @Test
  public void testGetSecondOrderCollectionService() {
    System.out.println("getSecondOrderCollectionService");
    SecondOrderCollectionServiceImpl result = instance.secondOrderCollection();
    assertNotNull(result);
    assertNotNull(result.toString());
  }
  
  @Test
  public void testGetSentenceService() {
    System.out.println("getSentenceService");
    SentenceServiceImpl result = instance.sentence();
    assertNotNull(result);
    assertNotNull(result.toString());
  }
  
  @Test
  public void testGetSymbolService() {
    System.out.println("getSymbolService");
    SymbolServiceImpl result = instance.symbol();
    assertNotNull(result);
    assertNotNull(result.toString());
  }
  
  @Test
  public void testGetTermService() {
    System.out.println("getTermService");
    KbTermServiceImpl result = instance.term();
    assertNotNull(result);
    assertNotNull(result.toString());
  }
  
  @Test
  public void testGetVariableService() {
    System.out.println("getVariableService");
    VariableServiceImpl result = instance.variable();
    assertNotNull(result);
    assertNotNull(result.toString());
  }
  /*
  @Test
  public void testServiceEquality() {
    System.out.println("testServiceEquality");
    final List<KbObjectService> services = Arrays.asList(
            instance.assertion(),
            instance.binaryPredicate(),
            instance.collection(),
            instance.context(),
            instance.kb(),
            instance.fact(),
            instance.firstOrderCollection(),
            instance.function(),
            instance.individual(),
            //instance.kbObject(),
            instance.predicate(),
            instance.relation(),
            instance.rule(),
            instance.secondOrderCollection(),
            instance.sentence(),
            instance.symbol(),
            instance.term(),
            instance.variable());
    final int total = services.size();
    for (KbObjectService service : services) {
      System.out.println("Service: " + service);
      final List<KbObjectService> otherServices = new ArrayList(services);
      otherServices.remove(service);
      assertEquals(total, otherServices.size() + 1);
      for (KbObjectService otherService : otherServices) {
        assertNotEquals(otherService, service);
      }
    }
  }
  */
}
