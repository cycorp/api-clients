package com.cyc.kb.client;

/*
 * #%L
 * File: RuleImplTest.java
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

import com.cyc.Cyc;
import com.cyc.kb.Assertion;
import com.cyc.kb.KbCollection;
import com.cyc.kb.Rule;
import com.cyc.kb.Sentence;
import com.cyc.kb.Variable;
import com.cyc.kb.exception.CreateException;
import com.cyc.kb.exception.KbException;
import com.cyc.kb.exception.KbTypeException;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.cyc.kb.client.TestConstants.flyingRule;
import static com.cyc.kb.client.TestUtils.assertPointerEqual;
import static org.junit.Assert.*;

/**
 *
 * @author vijay
 */
public class RuleImplTest {
  
  public RuleImplTest() {
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
  }
  
  @After
  public void tearDown() {
  }

  
  @Test
  public void testAddRule() throws KbTypeException, CreateException, KbException {
        
    KbCollection cPlane = KbCollectionImpl.get("CommercialAircraft");
    BinaryPredicateImpl flying2Pred1 = BinaryPredicateImpl.findOrCreate("flyingDoneBySomething-Move");
    Variable varp = new VariableImpl("?PLANE");
    Variable varf = new VariableImpl("?FLIGHT");
    Variable varend = new VariableImpl("?END-DATE");
    Variable vart = new VariableImpl("?TO");
    SentenceImpl s1 = new SentenceImpl (KbPredicateImpl.get("isa"), varp, cPlane);
    SentenceImpl s2 = new SentenceImpl (flying2Pred1, varf, varp);
    SentenceImpl s3 = new SentenceImpl (KbPredicateImpl.get("endingDate"), varf, varend);
    SentenceImpl s4 = new SentenceImpl (KbPredicateImpl.get("toLocation"), varf, vart);
    Sentence s5 = new SentenceImpl (KbPredicateImpl.get("artifactFoundInLocation"), varp, vart);
    Sentence s6 = new SentenceImpl (KbPredicateImpl.get("holdsIn"), varend, s5);
    List<Sentence> sandlist = new ArrayList<>();
    sandlist.add(s1);
    sandlist.add(s2);
    sandlist.add(s3);
    sandlist.add(s4);
    
    Rule flyingRule = RuleImpl.get(SentenceImpl.and(sandlist), s6, Constants.baseKbMt());
    assertEquals(SentenceImpl.and(sandlist), flyingRule.getAntecedent());
    assertEquals(s6, flyingRule.getConsequent());
  }
  
  @Test
  public void testRuleFactories_getByCore() throws KbTypeException, CreateException {
    final Rule expected = flyingRule;
    assertPointerEqual(expected, RuleImpl.get(KbObjectImpl.getCore(flyingRule)));    
    assertPointerEqual(expected, AssertionImpl.get(KbObjectImpl.getCore(flyingRule)));
    assertPointerEqual(expected, KbObjectImpl.get(KbObjectImpl.getCore(flyingRule)));
  }
  
  
  //@Test
  public void testRuleFactories_getByStringApiValue() throws KbTypeException, CreateException {
    //FIXME: these all currently fail, but should not - nwinant, 2017-04-13
    
    final Rule expected = flyingRule;
    System.out.println(flyingRule.stringApiValue());
    //assertPointerEqual(expected, RuleImpl.get(flyingRule.stringApiValue()));
    //assertPointerEqual(expected, AssertionImpl.get(flyingRule.stringApiValue()));
    assertPointerEqual(expected, KbObjectImpl.get(flyingRule.stringApiValue()));
    assertPointerEqual(expected, Rule.get(flyingRule.stringApiValue()));
    assertPointerEqual(expected, Assertion.get(flyingRule.stringApiValue()));
    assertPointerEqual(expected, Cyc.getKbObject(flyingRule.stringApiValue()));
    assertPointerEqual(expected, Cyc.getApiObject(flyingRule.stringApiValue()));
  }
  
}
