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
 * File: VariableServiceImplTest.java
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

import com.cyc.kb.Variable;
import com.cyc.kb.client.TestConstants;
import com.cyc.kb.client.VariableImpl;
import com.cyc.kb.exception.KbException;
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
public class VariableServiceImplTest {
  
  public VariableServiceImplTest() {
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
    instance = new VariableServiceImpl();
  }
  
  @After
  public void tearDown() {
  }
  
  // Fields
  
  private VariableServiceImpl instance;
  
  
  // Tests

  /**
   * Test of get method, of class VariableServiceImpl.
   */
  @Test
  public void testGet() throws KbException {
    System.out.println("get");
    VariableImpl expResult = new VariableImpl("?VAR");
    Variable result = instance.get("?VAR");
    assertEquals(expResult, result);
    System.out.println(result);
  }
  
}
