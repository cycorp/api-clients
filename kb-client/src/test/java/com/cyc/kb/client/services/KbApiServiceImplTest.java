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
 * File: KbApiServiceImplTest.java
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

import com.cyc.kb.client.TestConstants;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 *
 * @author nwinant
 */
public class KbApiServiceImplTest {
  
  public KbApiServiceImplTest() {
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
    instance = new KbApiServiceImpl();
  }
  
  @After
  public void tearDown() {
  }
  
  
  // Fields
  
  private KbApiServiceImpl instance = new KbApiServiceImpl();
  
  
  // Tests
  
  @Test
  public void testGetAssertionService() {
    System.out.println("testGetAssertionService");
    AssertionServiceImpl result = instance.getAssertionService();
    assertNotNull(result);
    assertNotNull(result.toString());
  }
  
  @Test
  public void testGetBinaryPredicateService() {
    System.out.println("testGetBinaryPredicateService");
    BinaryPredicateServiceImpl result = instance.getBinaryPredicateService();
    assertNotNull(result);
    assertNotNull(result.toString());
  }
  
  @Test
  public void testGetContextService() {
    System.out.println("testGetContextService");
    ContextServiceImpl result = instance.getContextService();
    assertNotNull(result);
    assertNotNull(result.toString());
  }
  
  @Test
  public void testGetFactService() {
    System.out.println("testGetFactService");
    FactServiceImpl result = instance.getFactService();
    assertNotNull(result);
    assertNotNull(result.toString());
  }
  
  @Test
  public void testGetFirstOrderCollectionService() {
    System.out.println("testGetFirstOrderCollectionService");
    FirstOrderCollectionServiceImpl result = instance.getFirstOrderCollectionService();
    assertNotNull(result);
    assertNotNull(result.toString());
  }
  
  @Test
  public void testGetKbCollectionService() {
    System.out.println("testGetKbCollectionService");
    KbCollectionServiceImpl result = instance.getKbCollectionService();
    assertNotNull(result);
    assertNotNull(result.toString());
  }
  
  @Test
  public void testGetKbFunctionService() {
    System.out.println("testGetKbFunctionService");
    KbFunctionServiceImpl result = instance.getKbFunctionService();
    assertNotNull(result);
    assertNotNull(result.toString());
  }
  
  @Test
  public void testGetKbIndividualService() {
    System.out.println("testGetKbIndividualService");
    KbIndividualServiceImpl result = instance.getKbIndividualService();
    assertNotNull(result);
    assertNotNull(result.toString());
  }
  
  @Test
  public void testGetKbPredicateService() {
    System.out.println("testGetKbPredicateService");
    KbPredicateServiceImpl result = instance.getKbPredicateService();
    assertNotNull(result);
    assertNotNull(result.toString());
  }
  
  @Test
  public void testGetKbService() {
    System.out.println("testGetKbService");
    KbServiceImpl result = instance.getKbService();
    assertNotNull(result);
    assertNotNull(result.toString());
  }
  
  @Test
  public void testGetKbTermService() {
    System.out.println("testGetKbTermService");
    KbTermServiceImpl result = instance.getKbTermService();
    assertNotNull(result);
    assertNotNull(result.toString());
  }
  
  @Test
  public void testGetRelationService() {
    System.out.println("testGetRelationService");
    RelationServiceImpl result = instance.getRelationService();
    assertNotNull(result);
    assertNotNull(result.toString());
  }
  
  @Test
  public void testGetRuleService() {
    System.out.println("testGetRuleService");
    RuleServiceImpl result = instance.getRuleService();
    assertNotNull(result);
    assertNotNull(result.toString());
  }

  @Test
  public void testGetSecondOrderCollectionService() {
    System.out.println("testGetSecondOrderCollectionService");
    SecondOrderCollectionServiceImpl result = instance.getSecondOrderCollectionService();
    assertNotNull(result);
    assertNotNull(result.toString());
  }
  
  @Test
  public void testGetSentenceService() {
    System.out.println("testGetSentenceService");
    SentenceServiceImpl result = instance.getSentenceService();
    assertNotNull(result);
    assertNotNull(result.toString());
  }
  
  @Test
  public void testGetSymbolService() {
    System.out.println("testGetSymbolService");
    SymbolServiceImpl result = instance.getSymbolService();
    assertNotNull(result);
    assertNotNull(result.toString());
  }
  
  @Test
  public void testGetVariableService() {
    System.out.println("testGetVariableService");
    VariableServiceImpl result = instance.getVariableService();
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
