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
 * File: RelationServiceImplTest.java
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
import com.cyc.kb.exception.KbException;
import com.cyc.kb.spi.RelationService;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.cyc.kb.client.TestUtils.skipTest;
import static org.junit.Assert.fail;

/**
 *
 * @author nwinant
 */
public class RelationServiceImplTest {
  
  public RelationServiceImplTest() {
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
    instance = new RelationServiceImpl();
  }
  
  @After
  public void tearDown() {
  }
  
  // Fields
  
  private RelationService instance;
  
  
  // Tests
  
  @Test
  public void testGet() throws KbException {
    skipTest(this, "testGet", "This test is not yet implemented.");
    System.out.println("get");
    fail("TODO!");
  }
  
  @Test
  public void testFindOrCreate() throws KbException {
    skipTest(this, "testFindOrCreate", "This test is not yet implemented.");
    System.out.println("fFindOrCreate");
    fail("TODO!");
  }
  
}
