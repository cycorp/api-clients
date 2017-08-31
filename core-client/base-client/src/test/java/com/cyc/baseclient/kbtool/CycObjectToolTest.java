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
 * File: CycObjectToolTest.java
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
import com.cyc.base.cycobject.CycObject;
import com.cyc.base.cycobject.FormulaSentence;
import com.cyc.base.cycobject.Naut;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.base.kbtool.ObjectTool;
import com.cyc.baseclient.testing.TestUtils;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author daves
 */
public class CycObjectToolTest {
  
  public CycObjectToolTest() {
  }

  @BeforeClass
  public static void setUpClass() {

  }

  @AfterClass
  public static void tearDownClass() {
  }
  
  @Before
  public void setUp() throws CycConnectionException {
    cyc = TestUtils.getCyc();
    objectTool = cyc.getObjectTool();
  }

  @After
  public void tearDown() {
  }
  
  
  // Fields
  
  CycAccess cyc;
  ObjectTool objectTool;
  
  
  // Tests
  
  @Test
  public void testToMostSpecificCycObject_CycList_FormulaSentence() {
    System.out.println("testToMostSpecificCycObject_CycList_FormulaSentence");
    final String          str      = "(#$objectFoundInLocation ?WHAT #$CityOfAustinTX)";
    final FormulaSentence expected = objectTool.makeCycSentence(str);
    final CycObject       result   = objectTool.toMostSpecificCycObject(expected.toCycList());
    assertEquals(expected, result);
  }
  
  @Test
  public void testToMostSpecificCycObject_CycList_Naut() {
    System.out.println("testToMostSpecificCycObject_CycList_Naut");
    final String    str      = "(#$CityNamedFn \"swaziville\" #$Swaziland)";
    final Naut      expected = objectTool.makeCycNaut(str);
    final CycObject result   = objectTool.toMostSpecificCycObject(expected.toCycList());
    assertEquals(expected, result);
  }
  
}
