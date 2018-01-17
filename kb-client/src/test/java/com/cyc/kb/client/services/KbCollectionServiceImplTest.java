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
 * File: KbCollectionServiceImplTest.java
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

import com.cyc.baseclient.testing.TestIterator;
import com.cyc.kb.KbObject;
import com.cyc.kb.client.KbCollectionImpl;
import com.cyc.kb.client.TestConstants;
import com.cyc.kb.client.services.examples.ServiceTestExamplesInKb;
import com.cyc.kb.exception.KbException;
import com.cyc.kb.spi.KbCollectionService;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.cyc.kb.client.TestUtils.skipTest;
import static com.cyc.kb.client.services.examples.ServiceTestUtils.TEST_ITERATOR;
import static com.cyc.kb.client.services.examples.ServiceTestUtils.trimString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 *
 * @author nwinant
 */
public class KbCollectionServiceImplTest {
  
  public KbCollectionServiceImplTest() {
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
    instance = new KbCollectionServiceImpl();
  }
  
  @After
  public void tearDown() {
  }
  
  // Fields
  
  private KbCollectionService instance;
  
  
  // Tests
  
  @Test
  public void testGet() throws Exception {
    System.out.println("testGet");
    final List<String> invalidTerms = new ArrayList(ServiceTestExamplesInKb.ALL_TERMS_IN_KB);
    invalidTerms.removeAll(ServiceTestExamplesInKb.ALL_COLLECTIONS_IN_KB);
    final List<String> failures = TEST_ITERATOR.testValidAndInvalidObjects(ServiceTestExamplesInKb.ALL_COLLECTIONS_IN_KB,
            invalidTerms,
            new TestIterator.IteratedTest<String>() {
              @Override
              public boolean isValidObject(String string) throws Exception {
                KbObject expected = null;
                try { 
                  expected = KbCollectionImpl.get(trimString(string));
                } catch (KbException ex) {}
                final KbObject result;
                try {
                  result = instance.get(string);
                } catch (KbException ex) {
                  return false;
                }
                return result.equals(expected);
              }
            });
    assertEquals(0, failures.size());
  }
  
  @Test
  public void testFindOrCreate() throws KbException {
    skipTest(this, "testFindOrCreate", "This test is not yet implemented.");
    System.out.println("fFindOrCreate");
    fail("TODO!");
  }
  
}
