package com.cyc.baseclient.cycobject;

/*
 * #%L
 * File: CycConstantImplTest.java
 * Project: Base Client
 * %%
 * Copyright (C) 2013 - 2018 Cycorp, Inc.
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
import com.cyc.base.cycobject.CycConstant;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.baseclient.CycObjectFactory;
import com.cyc.baseclient.xml.XmlStringWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import static com.cyc.baseclient.CommonConstants.COLLECTION;
import static com.cyc.baseclient.cycobject.CycObjectUnitTest.doTestCycObjectRetrievable;
import static com.cyc.baseclient.testing.TestConstants.BRAZIL;
import static com.cyc.baseclient.testing.TestConstants.CAT;
import static com.cyc.baseclient.testing.TestConstants.DOG;
import static com.cyc.baseclient.testing.TestConstants.TAME_ANIMAL;
import static com.cyc.baseclient.testing.TestConstants.TRANSPORTATION_DEVICE_VEHICLE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author baxter
 */
public class CycConstantImplTest {

  public CycConstantImplTest() {
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

  
  // Fields
  
  // Tests
  
  /**
   * Tests the makeValidConstantName method.
   */
  @Test
  public void testMakeValidConstantName() {
    System.out.println("\n**** testMakeValidConstantName ****");
    
    String candidateName = "abc";
    assertEquals(candidateName, CycConstantImpl.makeValidConstantName(candidateName));
    candidateName = "()[]//abc";
    
    String expectedValidName = "______abc";
    assertEquals(expectedValidName, CycConstantImpl.makeValidConstantName(candidateName));
    System.out.println("**** testMakeValidConstantName OK ****");
  }
  
  /**
   * Tests <tt>CycConstantImpl</tt> object behavior.
   * @throws com.cyc.base.exception.CycConnectionException
   * @throws java.io.IOException
   * @throws javax.xml.parsers.ParserConfigurationException
   * @throws org.xml.sax.SAXException
   */
  @Test
  public void testCycConstant() 
          throws CycConnectionException, IOException, ParserConfigurationException, SAXException {
    System.out.println("\n*** testCycConstant ***");
    
    CycObjectFactory.resetCycConstantCaches();
    assertEquals(0, CycObjectFactory.getCycConstantCacheByNameSize());
    final String guidString = TAME_ANIMAL.getGuid().getGuidString();
    final String constantName = TAME_ANIMAL.getName();
    CycObjectFactory.addCycConstantCache(TAME_ANIMAL);
    assertNotNull(TAME_ANIMAL);
    assertEquals(1, CycObjectFactory.getCycConstantCacheByNameSize());
    
    // Attempt to create a duplicate returns the cached existing object.
    final CycConstant cycConstant2 = new CycConstantImpl(
            constantName, CycObjectFactory.makeGuid(guidString));
    CycObjectFactory.addCycConstantCache(cycConstant2);
    assertEquals(1, CycObjectFactory.getCycConstantCacheByNameSize());
    assertEquals(TAME_ANIMAL, cycConstant2);
    
    final CycConstant cycConstant3 = new CycConstantImpl(
            constantName, CycObjectFactory.makeGuid(guidString));
    CycObjectFactory.addCycConstantCache(cycConstant3);
    assertEquals(TAME_ANIMAL.toString(), cycConstant3.toString());
    assertEquals(TAME_ANIMAL.cyclify(), cycConstant3.cyclify());
    assertEquals(TAME_ANIMAL, cycConstant3);
    doTestCycObjectRetrievable(TAME_ANIMAL);
    
    // compareTo
    final ArrayList constants = new ArrayList();
    constants.add(DOG);
    constants.add(CAT);
    constants.add(BRAZIL);
    constants.add(COLLECTION);
    Collections.sort(constants);
    assertEquals("[Brazil, Cat, Collection, Dog]", constants.toString());
    
    final CycConstantImpl cycConstant4 = TRANSPORTATION_DEVICE_VEHICLE;
    final XmlStringWriter xmlStringWriter = new XmlStringWriter();
    cycConstant4.toXML(xmlStringWriter, 0, false);
    final String expectedXML = "<constant>\n"
                                       + "  <guid>c0bce169-9c29-11b1-9dad-c379636f7270</guid>\n"
                                       + "  <name>TransportationDevice-Vehicle</name>\n"
                                       + "</constant>\n";
    assertEquals(expectedXML, xmlStringWriter.toString());
    assertEquals(expectedXML, cycConstant4.toXMLString());
    final String cycConstantXMLString = cycConstant4.toXMLString();
    CycObjectFactory.resetCycConstantCaches();
    final Object object = CycObjectFactory.unmarshal(cycConstantXMLString);
    assertTrue(object instanceof CycConstantImpl);
    assertEquals(cycConstant4, (CycConstantImpl) object);
    
    System.out.println("*** testCycConstant OK ***");
  }

}
