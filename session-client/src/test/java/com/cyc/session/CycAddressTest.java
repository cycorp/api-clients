/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cyc.session;

/*
 * #%L
 * File: CycAddressTest.java
 * Project: Session Client
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author nwinant
 */
public class CycAddressTest {
  
  public CycAddressTest() {
  }
  
  @BeforeClass
  public static void setUpClass() {
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
  public void testEquals() {
    assertEquals(CycAddress.get("localhost", 3600), CycAddress.get("localhost", 3600));
    assertEquals(CycAddress.get("localhost", 3620), CycAddress.get("localhost", 3620));
    assertEquals(CycAddress.get("fredscomputer", 3620), CycAddress.get("fredscomputer", 3620));
    assertEquals(CycAddress.get("localhost", 3600, 1), CycAddress.get("localhost", 3600, 1));
    assertEquals(CycAddress.get("localhost", 3620, 1), CycAddress.get("localhost", 3620, 1));
    assertEquals(CycAddress.get("localhost", 3600, 0), CycAddress.get("localhost", 3600, 0));
    assertEquals(CycAddress.get("localhost", 3600, -1), CycAddress.get("localhost", 3600, -1));
    assertEquals(CycAddress.get("fredscomputer", 3620, 1), CycAddress.get("fredscomputer", 3620, 1));
    assertEquals(CycAddress.get("fredscomputer", 3620, 2), CycAddress.get("fredscomputer", 3620, 2));
    assertNotEquals(CycAddress.get("localhost", 3600), CycAddress.get("localhost", 3620));
    assertNotEquals(CycAddress.get("localhost", 3600), CycAddress.get("fredscomputer", 3600));
    assertNotEquals(CycAddress.get("localhost", 3600), CycAddress.get("fredscomputer", 3620));
    assertNotEquals(CycAddress.get("localhost", 3600, 1), CycAddress.get("localhost", 3600));
    assertNotEquals(CycAddress.get("localhost", 3600, 1), CycAddress.get("localhost", 3620));
    assertNotEquals(CycAddress.get("localhost", 3620, 1), CycAddress.get("localhost", 3620));
    assertNotEquals(CycAddress.get("fredscomputer", 3620, 1), CycAddress.get("fredscomputer", 3620));
    assertNotEquals(CycAddress.get("localhost", 3600, 0), CycAddress.get("localhost", 3600, 1));
    assertNotEquals(CycAddress.get("localhost", 3600, -1), CycAddress.get("localhost", 3600, 1));
    assertNotEquals(CycAddress.get("localhost", 3600), CycAddress.get("localhost", 3620, 1));
    assertNotEquals(CycAddress.get("localhost", 3600), CycAddress.get("fredscomputer", 3600, 1));
    assertNotEquals(CycAddress.get("localhost", 3600), CycAddress.get("fredscomputer", 3620, 1));
  }

  @Test
  public void testResolveHostName() {
    final String localhost = "localhost";
    final String localIP = "127.0.01";
    
    final CycAddress server1 = CycAddress.get(localhost, 3600);
    System.out.println("Resolved " + server1.getHostName() + " to " +server1.getResolvedHostName());
    assertNotEquals(localhost, server1.getResolvedHostName());
    assertNotEquals(localIP, server1.getResolvedHostName());

    final CycAddress server2 = CycAddress.get(localIP, 3600);
    System.out.println("Resolved " + server2.getHostName() + " to " +server2.getResolvedHostName());
    assertNotEquals(localhost, server2.getResolvedHostName());
    assertNotEquals(localIP, server2.getResolvedHostName());
    
    assertNotEquals(server1, server2);
    assertNotEquals(server1.getHostName(), server2.getHostName());
    assertEquals(server1.getResolvedHostName(), server2.getResolvedHostName());
  }
  
  @Test
  public void testIsDefined() {
    assertFalse(CycAddress.get(null, 3600).isDefined());
    assertFalse(CycAddress.get(" ", 3600).isDefined());
    assertTrue(CycAddress.get("somehost", 3600).isDefined());
  }
  
  @Test 
  public void testIsValidString() {
    assertTrue(CycAddress.isValidString("fredscomputer:40"));
    assertTrue(CycAddress.isValidString("fredscomputer:4000"));
    assertTrue(CycAddress.isValidString("fredscomputer:40000"));
    assertFalse(CycAddress.isValidString("fredscomputer:400000"));
    assertFalse(CycAddress.isValidString("fredscomputer"));
  }
  
  @Test 
  public void testIsValidString_concurrencyLevel() {
    assertTrue(CycAddress.isValidString("fredscomputer:40:1"));
    assertTrue(CycAddress.isValidString("fredscomputer:40:4"));
    assertTrue(CycAddress.isValidString("fredscomputer:40:40"));
    assertTrue(CycAddress.isValidString("fredscomputer:40:41"));
    assertFalse(CycAddress.isValidString("fredscomputer:40:"));
    assertFalse(CycAddress.isValidString("fredscomputer:40:0"));
    assertFalse(CycAddress.isValidString("fredscomputer:40:-1"));
    assertFalse(CycAddress.isValidString("fredscomputer:40:4:"));
  }
  
  @Test 
  public void testConstructor_concurrencyLevel() {
    assertFalse(CycAddress.get("localhost", 3600).getConcurrencyLevel().isPresent());
    assertFalse(new CycAddressImpl("localhost", 3600,(Optional) null).getConcurrencyLevel().isPresent());
    assertTrue(CycAddress.get("localhost", 3600, 4).getConcurrencyLevel().isPresent());
    assertTrue(new CycAddressImpl("localhost", 3600, Optional.of(4)).getConcurrencyLevel().isPresent());
    assertTrue(CycAddress.get("localhost", 3600, 0).getConcurrencyLevel().isPresent());
    assertTrue(CycAddress.get("localhost", 3600, -1).getConcurrencyLevel().isPresent());
    assertTrue(CycAddress.get("localhost", 3600, 1).isDefined());
    assertTrue(CycAddress.get("localhost", 3600, 2).isDefined());
    assertTrue(CycAddress.get("localhost", 3600, 3).isDefined());
    assertTrue(CycAddress.get("localhost", 3600, 4).isDefined());
    assertTrue(CycAddress.get("localhost", 3600, 123).isDefined());
    assertFalse(CycAddress.get("localhost", 3600, 0).isDefined());
    assertFalse(CycAddress.get("localhost", 3600, -1).isDefined());
    assertEquals((Integer) 0, CycAddress.get("localhost", 3600, 0).getConcurrencyLevel().get());
    assertEquals((Integer) 1, CycAddress.get("localhost", 3600, 1).getConcurrencyLevel().get());
    assertEquals((Integer) 2, CycAddress.get("localhost", 3600, 2).getConcurrencyLevel().get());
    assertEquals((Integer) 3, CycAddress.get("localhost", 3600, 3).getConcurrencyLevel().get());
    assertEquals((Integer) 4, CycAddress.get("localhost", 3600, 4).getConcurrencyLevel().get());
    assertEquals((Integer) 123, CycAddress.get("localhost", 3600, 123).getConcurrencyLevel().get());
    assertEquals((Integer) 1, 
            new CycAddressImpl("localhost", 3600, Optional.of(1)).getConcurrencyLevel().get());
    assertEquals((Integer) 2, 
            new CycAddressImpl("localhost", 3600, Optional.of(2)).getConcurrencyLevel().get());
    assertEquals((Integer) 3, 
            new CycAddressImpl("localhost", 3600, Optional.of(3)).getConcurrencyLevel().get());
    assertEquals((Integer) 4, 
            new CycAddressImpl("localhost", 3600, Optional.of(4)).getConcurrencyLevel().get());
    assertEquals((Integer) 123, 
            new CycAddressImpl("localhost", 3600, Optional.of(123)).getConcurrencyLevel().get());
    assertEquals(0,
            (Object) new CycAddressImpl("localhost", 3600, Optional.of(0)).getConcurrencyLevel().get());
    assertEquals(-1,
            (Object) new CycAddressImpl("localhost", 3600, Optional.of(-1)).getConcurrencyLevel().get());
  }
  
  @Test 
  public void testFromString_concurrencyLevel() {
    assertFalse(CycAddress.fromString("localhost:3600").getConcurrencyLevel().isPresent());
    assertTrue(CycAddress.fromString("localhost:3600:4").getConcurrencyLevel().isPresent());
    assertTrue(CycAddress.fromString("localhost:3600:0").getConcurrencyLevel().isPresent());
    assertTrue(CycAddress.fromString("localhost:3600:-1").getConcurrencyLevel().isPresent());
    assertTrue(CycAddress.fromString("localhost:3600:1").isDefined());
    assertTrue(CycAddress.fromString("localhost:3600:2").isDefined());
    assertTrue(CycAddress.fromString("localhost:3600:3").isDefined());
    assertTrue(CycAddress.fromString("localhost:3600:4").isDefined());
    assertTrue(CycAddress.fromString("localhost:3600:123").isDefined());
    assertFalse(CycAddress.fromString("localhost:3600:0").isDefined());
    assertFalse(CycAddress.fromString("localhost:3600:-1").isDefined());
    assertEquals((Integer) 1, CycAddress.fromString("localhost:3600:1").getConcurrencyLevel().get());
    assertEquals((Integer) 2, CycAddress.fromString("localhost:3600:2").getConcurrencyLevel().get());
    assertEquals((Integer) 3, CycAddress.fromString("localhost:3600:3").getConcurrencyLevel().get());
    assertEquals((Integer) 4, CycAddress.fromString("localhost:3600:4").getConcurrencyLevel().get());
    assertEquals(0, (Object)CycAddress.fromString("localhost:3600:0").getConcurrencyLevel().get());
    assertEquals(-1, (Object)CycAddress.fromString("localhost:3600:-1").getConcurrencyLevel().get());
  }
  
  @Test
  public void testToString_concurrencyLevel() {
    final List<String> validStrings = Arrays.asList(
            "localhost:3600:1",
            "localhost:3600:4",
            "localhost:3600:2",
            "localhost:3600:123",
            "localhost:3600:3");
    validStrings.forEach(str -> {
      assertEquals("Error with valid string '" + str + "';",
              str,
              CycAddress.fromString(str).toString());
    });
    final List<String> invalidStrings = Arrays.asList(
            "localhost:3600:0",
            "localhost:3600:-1");
    invalidStrings.forEach(str -> {
      assertEquals("Error with invalid string '" + str + "';",
              "[Invalid concurrency level]",
              CycAddress.fromString(str).toString());
    });
  }
  
  @Test 
  public void testFromAddressWithDefaults() {
    assertEquals(CycAddress.get("localhost", 3600, 1),
            CycAddress.fromAddressWithDefaults(CycAddress.get("localhost", 3600), 1));
    
    assertEquals(CycAddress.get("localhost", 3600, 0),
            CycAddress.fromAddressWithDefaults(CycAddress.get("localhost", 3600), 0));
    
    assertEquals(CycAddress.get("localhost", 3600, -1),
            CycAddress.fromAddressWithDefaults(CycAddress.get("localhost", 3600), -1));
    
    assertEquals(CycAddress.get("localhost", 3600, 1),
            CycAddress.fromAddressWithDefaults(CycAddress.get("localhost", 3600, 1), 1));
    
    assertEquals(CycAddress.get("localhost", 3600, 0),
            CycAddress.fromAddressWithDefaults(CycAddress.get("localhost", 3600, 0), 1));
    
    assertEquals(CycAddress.get("localhost", 3600, 1),
            CycAddress.fromAddressWithDefaults(CycAddress.get("localhost", 3600, 1), 0));
    
    assertEquals(CycAddress.get("localhost", 3600, -1),
            CycAddress.fromAddressWithDefaults(CycAddress.get("localhost", 3600, -1), 1));
    
    assertEquals(CycAddress.get("localhost", 3600, 1),
            CycAddress.fromAddressWithDefaults(CycAddress.get("localhost", 3600, 1), -1));
    
    assertEquals(CycAddress.get("localhost", 80, 1),
            CycAddress.fromAddressWithDefaults(CycAddress.get("localhost", 80), 1));
    
    assertEquals(CycAddress.get("localhost", 80, 0),
            CycAddress.fromAddressWithDefaults(CycAddress.get("localhost", 80), 0));
    
    assertEquals(CycAddress.get("localhost", 80, -1),
            CycAddress.fromAddressWithDefaults(CycAddress.get("localhost", 80), -1));
    
    assertEquals(CycAddress.get("localhost", 80, 1),
            CycAddress.fromAddressWithDefaults(CycAddress.get("localhost", 80, 1), 1));
    
    assertEquals(CycAddress.get("localhost", 80, 0),
            CycAddress.fromAddressWithDefaults(CycAddress.get("localhost", 80, 0), 1));
    
    assertEquals(CycAddress.get("localhost", 80, 1),
            CycAddress.fromAddressWithDefaults(CycAddress.get("localhost", 80, 1), 0));
    
    assertEquals(CycAddress.get("localhost", 80, -1),
            CycAddress.fromAddressWithDefaults(CycAddress.get("localhost", 80, -1), 1));
    
    assertEquals(CycAddress.get("localhost", 80, 1),
            CycAddress.fromAddressWithDefaults(CycAddress.get("localhost", 80, 1), -1));    
    
    assertEquals(CycAddress.get("fredscomputer", 8080, 1),
            CycAddress.fromAddressWithDefaults(CycAddress.get("fredscomputer", 8080), 1));
    
    assertEquals(CycAddress.get("fredscomputer", 8080, 0),
            CycAddress.fromAddressWithDefaults(CycAddress.get("fredscomputer", 8080), 0));
    
    assertEquals(CycAddress.get("fredscomputer", 8080, -1),
            CycAddress.fromAddressWithDefaults(CycAddress.get("fredscomputer", 8080), -1));
    
    assertEquals(CycAddress.get("fredscomputer", 8080, 1),
            CycAddress.fromAddressWithDefaults(CycAddress.get("fredscomputer", 8080, 1), 1));
    
    assertEquals(CycAddress.get("fredscomputer", 8080, 0),
            CycAddress.fromAddressWithDefaults(CycAddress.get("fredscomputer", 8080, 0), 1));
    
    assertEquals(CycAddress.get("fredscomputer", 8080, 1),
            CycAddress.fromAddressWithDefaults(CycAddress.get("fredscomputer", 8080, 1), 0));
    
    assertEquals(CycAddress.get("fredscomputer", 8080, -1),
            CycAddress.fromAddressWithDefaults(CycAddress.get("fredscomputer", 8080, -1), 1));
    
    assertEquals(CycAddress.get("fredscomputer", 8080, 1),
            CycAddress.fromAddressWithDefaults(CycAddress.get("fredscomputer", 8080, 1), -1));
  }

}
