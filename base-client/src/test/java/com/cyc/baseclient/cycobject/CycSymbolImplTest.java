package com.cyc.baseclient.cycobject;

/*
 * #%L
 * File: CycSymbolImplTest.java
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
import com.cyc.base.exception.CycConnectionException;
import com.cyc.baseclient.CycObjectFactory;
import com.cyc.baseclient.testing.TestConstants;
import com.cyc.baseclient.testing.TestIterator;
import com.cyc.baseclient.xml.XmlStringWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import static com.cyc.baseclient.cycobject.CycObjectUnitTest.doTestCycObjectRetrievable;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author baxter
 */
public class CycSymbolImplTest {

  public CycSymbolImplTest() {
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
  
  private static final TestIterator<String> NAME_TESTER = new TestIterator<String>();
  
  private static final String BRAZIL_STR = TestConstants.BRAZIL.cyclify();

  
  // Tests
  
  @Test(expected = NullPointerException.class)
  public void testNullSymbolName() {
    System.out.println("testNullSymbolName");
    CycSymbolImpl.isValidSymbolName(null);
  }
  
  @Test
  public void testValidSymbolNames() {
    System.out.println("testValidSymbolNames");
    assertEquals(0, NAME_TESTER.testValidAndInvalidObjects(
            Arrays.asList(
                    ":X", ":SYMBOL", ":x", ":symbol",
                    ":this-is-a-symbol",
                    "this-is-a-symbol",
                    "SYMBOL", "X", "VAR",
                    "symbol", "x", "var",
                    "package-name::symbol-name",
                    //"|symbol with whitespace|",
                    //"package:exported-symbol", 
                    //"#:uninterned-symbol"
                    ":", "::", ":::",
                    "::SYMBOL", ":::SYMBOL",
                    "?X", "?VAR", "??VAR", "???VAR", "?"),
            
            Arrays.asList(
                    "", " ", "       ",
                    //":", "::", ":::",
                    " :X", "     :X", ":X ", ":X      ", " :X    ",
                    " :SYMBOL", "     :SYMBOL", ":SYMBOL ", ":SYMBOL      ", " :SYMBOL    ",
                    //"::SYMBOL", ":::SYMBOL",
                    //"?X", "?VAR", "??VAR", "???VAR", "?",
                    "SYMBOL NAME",
                    "symbol name"),
            
            new TestIterator.IteratedTest<String>() {
              @Override
              public boolean isValidObject(String symbolName) {
                return CycSymbolImpl.isValidSymbolName(symbolName);
              }
            }).size());
  }

  /**
   * Tests <tt>CycSymbolImpl</tt> object behavior.
   *
   * @throws com.cyc.base.exception.CycConnectionException
   * @throws java.io.IOException
   * @throws javax.xml.parsers.ParserConfigurationException
   * @throws org.xml.sax.SAXException
   */
  @Test
  public void testCycSymbol() throws CycConnectionException, IOException, ParserConfigurationException, SAXException {
    System.out.println("\n*** testCycSymbol ***");
    CycObjectFactory.resetCycSymbolCache();
    assertEquals(CycObjectFactory.RESET_SYMBOL_CACHE_SIZE,
            CycObjectFactory.getCycSymbolCacheSize());
    String symbolName = "WHY-ISA?";
    CycSymbolImpl cycSymbol = CycObjectFactory.makeCycSymbol(symbolName);
    assertEquals(CycObjectFactory.RESET_SYMBOL_CACHE_SIZE + 1,
            CycObjectFactory.getCycSymbolCacheSize());
    assertEquals(symbolName, cycSymbol.toString());
    assertNotNull(CycObjectFactory.getCycSymbolCache(symbolName));
    CycSymbolImpl cycSymbol2 = CycObjectFactory.getCycSymbolCache(symbolName);
    assertEquals(cycSymbol, cycSymbol2);
    CycSymbolImpl cycSymbol3 = CycObjectFactory.makeCycSymbol(symbolName);
    assertEquals(cycSymbol, cycSymbol3);
    assertEquals(CycObjectFactory.RESET_SYMBOL_CACHE_SIZE + 1,
            CycObjectFactory.getCycSymbolCacheSize());
    String symbolName4 = "WHY-ISA?";
    CycSymbolImpl cycSymbol4 = CycObjectFactory.makeCycSymbol(symbolName4);
    assertEquals(cycSymbol.toString(), cycSymbol4.toString());
    assertEquals(cycSymbol, cycSymbol4);
    
    doTestCycObjectRetrievable(cycSymbol);

    // compareTo
    ArrayList symbols = new ArrayList();
    symbols.add(CycObjectFactory.makeCycSymbol("isa?"));
    symbols.add(CycObjectFactory.makeCycSymbol("define-private"));
    symbols.add(CycObjectFactory.makeCycSymbol("nil"));
    Collections.sort(symbols);
    assertEquals("[DEFINE-PRIVATE, ISA?, NIL]", symbols.toString());

    // isKeyword
    CycSymbolImpl cycSymbol5 = CycObjectFactory.makeCycSymbol("nil");
    assertFalse(cycSymbol5.isKeyword());
    CycSymbolImpl cycSymbol6 = CycObjectFactory.makeCycSymbol(":pos");
    assertTrue(cycSymbol6.isKeyword());

    // isValidSymbolName
    assertTrue(CycSymbolImpl.isValidSymbolName("t"));
    assertTrue(CycSymbolImpl.isValidSymbolName("nil"));
    assertTrue(CycSymbolImpl.isValidSymbolName("a_"));
    assertTrue(CycSymbolImpl.isValidSymbolName("a-b"));
    assertTrue(CycSymbolImpl.isValidSymbolName("a-b"));
    assertTrue(CycSymbolImpl.isValidSymbolName("a-9b"));
    assertTrue(CycSymbolImpl.isValidSymbolName("*MY-SYMBOL*"));
    assertFalse(CycSymbolImpl.isValidSymbolName(" "));
    assertFalse(CycSymbolImpl.isValidSymbolName(BRAZIL_STR));
    assertFalse(CycSymbolImpl.isValidSymbolName("\"a-string\""));

    //packages
    CycSymbolImpl symbol7 = new CycSymbolImpl("CYC", "BLAH");
    CycSymbolImpl symbol8 = new CycSymbolImpl("|CYC|", "BLAH");
    CycSymbolImpl symbol9 = new CycSymbolImpl("CYC", "|BLAH|");
    CycSymbolImpl symbol10 = new CycSymbolImpl("|CYC|", "|BLAH|");
    assertEquals("CYC", symbol7.getPackageName());
    assertEquals("CYC", symbol8.getPackageName());
    assertEquals("CYC", symbol9.getPackageName());
    assertEquals("CYC", symbol10.getPackageName());
    assertEquals("CYC", symbol7.getPackageNamePrecise());
    assertEquals("CYC", symbol8.getPackageNamePrecise());
    assertEquals("CYC", symbol9.getPackageNamePrecise());
    assertEquals("CYC", symbol10.getPackageNamePrecise());
    assertEquals("BLAH", symbol7.getSymbolName());
    assertEquals("BLAH", symbol8.getSymbolName());
    assertEquals("BLAH", symbol9.getSymbolName());
    assertEquals("BLAH", symbol10.getSymbolName());
    assertEquals("BLAH", symbol7.getSymbolNamePrecise());
    assertEquals("BLAH", symbol8.getSymbolNamePrecise());
    assertEquals("BLAH", symbol9.getSymbolNamePrecise());
    assertEquals("BLAH", symbol10.getSymbolNamePrecise());
    assertEquals(symbol7, symbol8);
    assertEquals(symbol7, symbol9);
    assertEquals(symbol7, symbol10);
    assertEquals("BLAH", symbol7.toString());
    assertEquals("BLAH", symbol8.toString());
    assertEquals("BLAH", symbol9.toString());
    assertEquals("BLAH", symbol10.toString());
    assertEquals("CYC:BLAH", symbol7.toFullStringForced());
    assertEquals("CYC:BLAH", symbol8.toFullStringForced());
    assertEquals("CYC:BLAH", symbol9.toFullStringForced());
    assertEquals("CYC:BLAH", symbol10.toFullStringForced());
    assertEquals("CYC:BLAH", symbol7.toFullString("SL"));
    assertEquals("CYC:BLAH", symbol8.toFullString("SL"));
    assertEquals("CYC:BLAH", symbol9.toFullString("SL"));
    assertEquals("CYC:BLAH", symbol10.toFullString("SL"));
    assertEquals("BLAH", symbol10.toFullString("CYC"));
    assertFalse(symbol7.isKeyword());
    assertFalse(symbol8.isKeyword());
    assertFalse(symbol9.isKeyword());
    assertFalse(symbol10.isKeyword());

    CycSymbolImpl symbol11 = new CycSymbolImpl("|CYC RuLeS|", "|BLAH BiTeS|");
    CycSymbolImpl symbol12 = new CycSymbolImpl("CYC RuLeS", "BLAH BiTeS");
    assertEquals("CYC RuLeS", symbol11.getPackageName());
    assertEquals("CYC RuLeS", symbol12.getPackageName());
    assertEquals("|CYC RuLeS|", symbol11.getPackageNamePrecise());
    assertEquals("|CYC RuLeS|", symbol12.getPackageNamePrecise());
    assertEquals("BLAH BiTeS", symbol11.getSymbolName());
    assertEquals("BLAH BiTeS", symbol12.getSymbolName());
    assertEquals("|BLAH BiTeS|", symbol11.getSymbolNamePrecise());
    assertEquals("|BLAH BiTeS|", symbol12.getSymbolNamePrecise());
    assertEquals(symbol11, symbol12);
    assertEquals("|BLAH BiTeS|", symbol11.toString());
    assertEquals("|BLAH BiTeS|", symbol12.toString());
    assertEquals("|CYC RuLeS|:|BLAH BiTeS|", symbol11.toFullStringForced());
    assertEquals("|CYC RuLeS|:|BLAH BiTeS|", symbol12.toFullStringForced());
    assertEquals("|CYC RuLeS|:|BLAH BiTeS|", symbol11.toFullString("SL"));
    assertEquals("|CYC RuLeS|:|BLAH BiTeS|", symbol12.toFullString("SL"));
    assertEquals("|BLAH BiTeS|", symbol12.toFullString("CYC RuLeS"));
    assertFalse(symbol11.isKeyword());
    assertFalse(symbol12.isKeyword());

    CycSymbolImpl symbol13 = new CycSymbolImpl("KEYWORD", "BLAH");
    CycSymbolImpl symbol14 = new CycSymbolImpl("|KEYWORD|", "BLAH");
    CycSymbolImpl symbol15 = new CycSymbolImpl("", ":BLAH");
    CycSymbolImpl symbol16 = new CycSymbolImpl(null, ":BLAH");
    assertEquals("KEYWORD", symbol13.getPackageName());
    assertEquals("KEYWORD", symbol14.getPackageName());
    assertEquals("KEYWORD", symbol15.getPackageName());
    assertEquals("KEYWORD", symbol16.getPackageName());
    assertEquals("KEYWORD", symbol13.getPackageNamePrecise());
    assertEquals("KEYWORD", symbol14.getPackageNamePrecise());
    assertEquals("KEYWORD", symbol15.getPackageNamePrecise());
    assertEquals("KEYWORD", symbol16.getPackageNamePrecise());
    assertEquals("BLAH", symbol13.getSymbolName());
    assertEquals("BLAH", symbol14.getSymbolName());
    assertEquals("BLAH", symbol15.getSymbolName());
    assertEquals("BLAH", symbol16.getSymbolName());
    assertEquals("BLAH", symbol13.getSymbolNamePrecise());
    assertEquals("BLAH", symbol14.getSymbolNamePrecise());
    assertEquals("BLAH", symbol15.getSymbolNamePrecise());
    assertEquals("BLAH", symbol16.getSymbolNamePrecise());
    assertEquals(symbol13, symbol14);
    assertEquals(symbol13, symbol15);
    assertEquals(symbol13, symbol16);
    assertEquals(":BLAH", symbol13.toString());
    assertEquals(":BLAH", symbol14.toString());
    assertEquals(":BLAH", symbol15.toString());
    assertEquals(":BLAH", symbol16.toString());
    assertEquals("KEYWORD:BLAH", symbol13.toFullStringForced());
    assertEquals("KEYWORD:BLAH", symbol14.toFullStringForced());
    assertEquals("KEYWORD:BLAH", symbol15.toFullStringForced());
    assertEquals("KEYWORD:BLAH", symbol16.toFullStringForced());
    assertEquals(":BLAH", symbol13.toFullString("SL"));
    assertEquals(":BLAH", symbol14.toFullString("SL"));
    assertEquals(":BLAH", symbol15.toFullString("SL"));
    assertEquals(":BLAH", symbol16.toFullString("SL"));
    assertEquals(":BLAH", symbol16.toFullString("KEYWORD"));
    assertTrue(symbol13.isKeyword());
    assertTrue(symbol14.isKeyword());
    assertTrue(symbol15.isKeyword());
    assertTrue(symbol16.isKeyword());

    // toXML, toXMLString, unmarshal
    XmlStringWriter xmlStringWriter = new XmlStringWriter();

    cycSymbol6.toXML(xmlStringWriter, 0, false);
    assertEquals("<symbol>:POS</symbol>\n", xmlStringWriter.toString());
    assertEquals("<symbol>:POS</symbol>\n", cycSymbol6.toXMLString());
    String cycSymbolXMLString = cycSymbol6.toXMLString();
    Object object = CycObjectFactory.unmarshal(cycSymbolXMLString);
    assertTrue(object instanceof CycSymbolImpl);
    assertEquals(cycSymbol6, (CycSymbolImpl) object);

    System.out.println("*** testCycSymbol OK ***");
  }
  
}
