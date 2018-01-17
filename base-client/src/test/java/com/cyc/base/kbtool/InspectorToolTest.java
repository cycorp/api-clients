/*
 * Copyright 2015 Cycorp, Inc..
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

package com.cyc.base.kbtool;

/*
 * #%L
 * File: InspectorToolTest.java
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
import com.cyc.base.CycAccessManager;
import com.cyc.base.cycobject.CycObject;
import com.cyc.base.cycobject.CycSymbol;
import com.cyc.base.cycobject.FormulaSentence;
import com.cyc.base.cycobject.Naut;
import com.cyc.baseclient.cycobject.CycAssertionImpl;
import com.cyc.baseclient.cycobject.CycConstantImpl;
import com.cyc.baseclient.cycobject.CycSymbolImpl;
import com.cyc.baseclient.cycobject.FormulaSentenceImpl;
import com.cyc.baseclient.cycobject.GuidImpl;
import com.cyc.baseclient.cycobject.NautImpl;
import com.cyc.session.exception.SessionCommunicationException;
import com.cyc.session.exception.SessionConfigurationException;
import com.cyc.session.exception.SessionInitializationException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.cyc.baseclient.CommonConstants.YEAR_FN;
import static com.cyc.baseclient.testing.TestUtils.isEnterpriseCyc;
import static org.apache.commons.lang3.StringUtils.rightPad;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author vijay
 */
public class InspectorToolTest {
  
  // Static
  
  private static CycAccess access = null;
  
  @BeforeClass
  public static void setUpClass() throws SessionConfigurationException, SessionCommunicationException, SessionInitializationException {
    access = CycAccessManager.getCurrentAccess();
  }
  
  @AfterClass
  public static void tearDownClass() {
  }
    
  // Fields
  
  private final CycObject isa
          = new CycConstantImpl("isa",
                  new GuidImpl("bd588104-9c29-11b1-9dad-c379636f7270"));

  private final CycObject binaryPredicate
          = new CycConstantImpl("BinaryPredicate",
                  new GuidImpl("bd588102-9c29-11b1-9dad-c379636f7270"));

  private final CycObject foCol
          = new CycConstantImpl("FirstOrderCollection",
                  new GuidImpl("1c8052d2-1fd3-11d6-8000-0050dab92c2f"));

  private final CycObject soCol
          = new CycConstantImpl("SecondOrderCollection",
                  new GuidImpl("1d075598-1fd3-11d6-8000-0050dab92c2f"));

  private final CycObject col
          = new CycConstantImpl("Collection",
                  new GuidImpl("bd5880cc-9c29-11b1-9dad-c379636f7270"));

  private final CycObject obama
          = new CycConstantImpl("BarackObama",
                  new GuidImpl("7cabb06c-7520-11dd-8000-0002b3a85b8f"));

  private final CycObject ind
          = new CycConstantImpl("Individual",
                  new GuidImpl("bd58da02-9c29-11b1-9dad-c379636f7270"));

  private final Naut dollar = new NautImpl(YEAR_FN, 2017);

  
  private final CycObject logicalTruthMt
          = new CycConstantImpl("LogicalTruthMt",
                  new GuidImpl("c0604f82-9c29-11b1-9dad-c379636f7270"));

  private final CycObject mt
          = new CycConstantImpl("Microthoery",
                  new GuidImpl("bd5880d5-9c29-11b1-9dad-c379636f7270"));

  private final CycObject distance
          = new CycConstantImpl("distanceBetween",
                  new GuidImpl("bd58eff2-9c29-11b1-9dad-c379636f7270"));

  private final CycObject pred
          = new CycConstantImpl("Predicate",
                  new GuidImpl("bd5880d6-9c29-11b1-9dad-c379636f7270"));

  private final CycObject func
          = new CycConstantImpl("Function-Denotational",
                  new GuidImpl("bd5c40b0-9c29-11b1-9dad-c379636f7270"));

  private final CycObject fruit
          = new CycConstantImpl("FruitFn",
                  new GuidImpl("bd58a976-9c29-11b1-9dad-c379636f7270"));

  private final CycObject thing
          = new CycConstantImpl("Thing",
                  new GuidImpl("bd5880f4-9c29-11b1-9dad-c379636f7270"));

  private final CycSymbol cs = new CycSymbolImpl("SOMEKEYWORD");
  
  private final Map<CycObject, CycObject> termsAndExpectedTypes;
    
  private final List<CycObject> allTerms;
  
  private final int termPadding;

  // Construction
  
  public InspectorToolTest() {
    final Map<CycObject, CycObject> expectedTmp = new LinkedHashMap<>();
    {
      expectedTmp.put(isa, binaryPredicate);
      expectedTmp.put(binaryPredicate, foCol);
      expectedTmp.put(foCol, soCol);
      expectedTmp.put(soCol, col);
      expectedTmp.put(obama, ind);
      expectedTmp.put(dollar, ind);
      expectedTmp.put(logicalTruthMt, mt);
      expectedTmp.put(distance, pred);
      expectedTmp.put(fruit, func);
    }
    termsAndExpectedTypes = Collections.unmodifiableMap(expectedTmp);
    allTerms = Collections.unmodifiableList(new ArrayList(termsAndExpectedTypes.keySet()));
    termPadding = termsAndExpectedTypes.keySet().stream()
            .mapToInt(e -> e.toString().length())
            .max().getAsInt();
  }
  
  // Tests
  
  /**
   * Test of InspectorTool#categorizeTermWRTApi().
   */
  @Test
  public void testCategorizeTermWRTApi() throws Exception {
    System.out.println("categorizeTermWRTApi");
    final InspectorTool instance = access.getInspectorTool();
    final List<String> failures = new ArrayList();
    for (CycObject term : allTerms) {
      final CycObject result = instance.categorizeTermWRTApi(term);
      assertTermCategorized(term, result, failures);
    }
    assertTrue(failures.isEmpty());
    // FIXME: We don't get consistent results for this across different Cyc releases - nwinant, 2017-03-30
    CycObject result = instance.categorizeTermWRTApi(cs);
    assertTrue(thing.equals(result) || ind.equals(result));
    //assertEquals(thing, result);
  }
  
  @Test
  public void testCategorizeTermsWRTApi() throws Exception {
    System.out.println("categorizeTermsWRTApi");
    final InspectorTool instance = access.getInspectorTool();
    final List<CycObject> terms = new ArrayList(allTerms);
    assertEquals(allTerms, terms);
    //Collections.shuffle(terms);
    //assertNotEquals(allTerms, terms);
    final List<CycObject> expected = new ArrayList<>();
    terms.forEach((term) -> {
      expected.add(termsAndExpectedTypes.get(term));
    });
    final List<CycObject> results = instance.categorizeTermsWRTApi(terms);
    assertNotNull("Results are null", results);
    final List<String> failures = new ArrayList();
    for (int i = 0; i < terms.size(); i++) {
      final CycObject term = terms.get(i);
      if (results.size() >= i) {
        assertTermCategorized(term, results.get(i), failures);
      } else {
        addFailure(failures,
                "Failure for " + term + " #" + (i + 1) + ": only " + results.size() + " results");
      }
    }
    assertTrue(failures.isEmpty());
    assertEquals(expected, results);
  }
  
  private void addFailure(List<String> failures, String msg) {
    System.err.println(msg);
    failures.add(msg);
  }
  
  private void assertTermCategorized(CycObject term, CycObject result, List<String> failures) {
    final CycObject expected = termsAndExpectedTypes.get(term);
    if (Objects.equals(expected, result)) {
      System.out.println("- " + rightPad("" + term, termPadding) + " : " + result);
    } else {
      addFailure(failures, "Failure for " + term + ": expected " + expected + " but got " + result);
    }
  }
  
  /**
   * Test categorization of sentences by InspectorTool#categorizeTermWRTApi().
   * 
   * <p><strong>Currently disabled.</strong> #categorizeTermWRTApi() is currently used only for the
   * KB API, and the KB API never uses that method to process sentences; they are checked at the 
   * Java level as instances of FormulaSentence.
   */
  //@Test
  public void testCategorizeSentence() throws Exception {
    final InspectorTool instance = access.getInspectorTool();
    
    final FormulaSentence isaPredicate = FormulaSentenceImpl.makeFormulaSentence(access, "(#$isa #$isa #$Predicate)");
    final CycObject resultIsaPredicate = instance.categorizeTermWRTApi(isaPredicate);
    if (access.isOpenCyc() || isEnterpriseCyc()) {
      assertEquals(ind, resultIsaPredicate);
    } else {
      assertEquals(thing, resultIsaPredicate);
    }
    
    final CycAssertionImpl caIsaPredicate = new CycAssertionImpl(isaPredicate, logicalTruthMt);
    CycObject resultCaIsaPredicate = instance.categorizeTermWRTApi(caIsaPredicate);
    if (!isEnterpriseCyc()) {
      assertEquals(thing, resultCaIsaPredicate);
    } else {
      assertEquals(ind, resultCaIsaPredicate);
    }
  }
  
}
