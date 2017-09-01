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
package com.cyc.kb;

import com.cyc.base.cycobject.CycNumber;
import com.cyc.core.service.CoreServicesLoader;
import com.cyc.kb.spi.KbApiService;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

/*
 * #%L
 * File: KbFactoryTest.java
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

/**
 *
 * @author nwinant
 */
public class KbFactoryTest {
  
  // Tests
  
  @Test
  public void testGetKbFactoryServices() {
    KbApiService result = KbFactory.getInstance();
    assertNotNull(result);
    assertNotNull(result.toString());
    assertEquals(CoreServicesLoader.getKbFactoryServices(), result);
  }
  
    /**
   * Test of getApiObject method, of class KbFactory.
   */
  @Test
  public void testGetApiObject() throws Exception {
    System.out.println("getApiObject");
    String cyclOrId = "M98";
    Object expResult = 95;
    Object result = ((CycNumber)((KbObject)KbFactory.getApiObject(cyclOrId)).getCore()).getNumber();
    assertEquals(expResult, result);
    
    cyclOrId = "Mw-DY2F0";
    expResult = "cat";
    result = KbFactory.getApiObject(cyclOrId);
    assertEquals(expResult, result);
  }

  
}
