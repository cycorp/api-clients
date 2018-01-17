package com.cyc.baseclient.cycobject;

/*
 * #%L
 * File: CycObjectUnitTest.java
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

import com.cyc.base.cycobject.CycConstant;
import com.cyc.base.cycobject.CycList;
import com.cyc.base.cycobject.CycObject;
import com.cyc.base.cycobject.DenotationalTerm;
import com.cyc.base.cycobject.Fort;
import com.cyc.base.cycobject.Nart;
import com.cyc.base.cycobject.Naut;
import com.cyc.base.cycobject.NonAtomicTerm;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.baseclient.CommonConstants;
import com.cyc.baseclient.CycObjectFactory;
import com.cyc.baseclient.connection.SublApiHelper;
import com.cyc.baseclient.testing.TestConstants;
import com.cyc.baseclient.testing.TestGuids;
import com.cyc.baseclient.testing.TestSentences;
import com.cyc.baseclient.testing.TestUtils;
import com.cyc.baseclient.util.CycUtils;
import com.cyc.baseclient.util.MyStreamTokenizer;
import com.cyc.baseclient.xml.XmlStringWriter;
import com.cyc.session.exception.SessionException;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import static com.cyc.baseclient.CommonConstants.YEAR_FN;
import static com.cyc.baseclient.testing.TestConstants.*;
import static com.cyc.baseclient.testing.TestUtils.assumeNotOpenCyc;
import static com.cyc.baseclient.testing.TestUtils.getCyc;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

// FIXME: TestSentences - nwinant

/**
 * Provides a suite of JUnit test cases for the <tt>com.cyc.baseclient.cycobject</tt> package.<p>
 *
 * @version $Id: UnitTest.java 135652 2011-08-30 10:24:52Z baxter $
 * @author Stephen L. Reed
 */
public class CycObjectUnitTest {    
  
  private static final String US_DOLLAR_FN_STR = TestConstants.DOLLAR.cyclify();
  
  @Before
  public void setUp() throws CycConnectionException, SessionException {
    TestUtils.ensureTestEnvironmentInitialized();
  }

  @Test
  public void doTestCycNumber() {
    System.out.println("\n*** testCycNumber ***");
    final CycNumberImpl one = new CycNumberImpl(1);
    assertTrue(one instanceof DenotationalTerm);
    assertTrue(one.equalsAtEL(1));
    final CycNumberImpl oneTwoThree = CycObjectFactory.makeCycNumber(123);
    assertTrue(oneTwoThree instanceof Comparable);
    final CycNumberImpl three = CycObjectFactory.makeCycNumber(3);
    final CycNumberImpl minusZero = CycObjectFactory.makeCycNumber(-0.0);
    // Get a couple BigIntegers larger than the largest Double:
    BigInteger bigInteger = new BigInteger("212");
    BigInteger biggerInteger = new BigInteger("213");
    while (bigInteger.doubleValue() <= Double.MAX_VALUE) {
      bigInteger = biggerInteger;
      biggerInteger = biggerInteger.multiply(biggerInteger);
    }
    final CycNumberImpl big = CycObjectFactory.makeCycNumber(bigInteger);
    final CycNumberImpl bigger = CycObjectFactory.makeCycNumber(biggerInteger);
    assertNotSame(big, bigger);
    assertTrue(bigger.isGreaterThan(big));
    final List<CycNumberImpl> numbers = Arrays.asList(three, big, minusZero, bigger);
    Collections.sort(numbers);
    assertEquals(minusZero, numbers.get(0));
    assertEquals(big, numbers.get(2));
    final CycNumberImpl doubleOne = CycObjectFactory.makeCycNumber(1.0);
    assertNotSame(one, doubleOne);
    assertFalse(one.isGreaterThan(doubleOne));
    assertFalse(doubleOne.isGreaterThan(one));
    final CycNumberImpl floatOne = CycObjectFactory.makeCycNumber(1.0F);
    assertNotSame(floatOne, doubleOne);
    assertNotSame(floatOne, one);
    assertEquals(doubleOne, doubleOne);
    final CycNumberImpl plusZero = CycObjectFactory.makeCycNumber(0.0);
    assertNotSame(minusZero, plusZero);
    assertTrue(plusZero.isGreaterThan(minusZero));
    System.out.println("*** testCycNumber OK ***");
  }

  /**
   * Tests <tt>GuidImpl</tt> object behavior.
   *
   * @throws java.io.IOException
   * @throws javax.xml.parsers.ParserConfigurationException
   * @throws org.xml.sax.SAXException
   */
  @Test
  public void testGuid() throws IOException, ParserConfigurationException, SAXException {
    System.out.println("\n*** testGuid ***");
    CycObjectFactory.resetGuidCache();
    assertEquals(0, CycObjectFactory.getGuidCacheSize());
    final String guidString = TestGuids.APPLE_TREE_GUID_STRING;
    final GuidImpl guid = CycObjectFactory.makeGuid(guidString);
    assertEquals(1, CycObjectFactory.getGuidCacheSize());
    assertEquals(guidString, guid.toString());
    final GuidImpl guid2 = CycObjectFactory.getGuidCache(guidString);
    assertEquals(guid, guid2);
    final GuidImpl guid3 = CycObjectFactory.makeGuid(guidString);
    assertEquals(guid, guid3);
    assertEquals(1, CycObjectFactory.getGuidCacheSize());

    // toXML, toXMLString, unmarshal
    final XmlStringWriter xmlStringWriter = new XmlStringWriter();

    guid.toXML(xmlStringWriter, 0, false);
    assertEquals("<guid>bd58c19d-9c29-11b1-9dad-c379636f7270</guid>\n",
            xmlStringWriter.toString());
    assertEquals("<guid>bd58c19d-9c29-11b1-9dad-c379636f7270</guid>\n",
            guid.toXMLString());
    final String guidXMLString = guid.toXMLString();
    CycObjectFactory.resetGuidCache();
    final Object object = CycObjectFactory.unmarshal(guidXMLString);
    assertTrue(object instanceof GuidImpl);
    assertEquals(guid, (GuidImpl) object);
    assertTrue(CycObjectFactory.unmarshal(guidXMLString)
            == CycObjectFactory.unmarshal(guidXMLString));

    System.out.println("*** testGuid OK ***");
  }

  /**
   * Tests <tt>Nart</tt> object behavior.
   * @throws com.cyc.base.exception.CycConnectionException
   * @throws java.io.IOException
   * @throws org.xml.sax.SAXException
   * @throws javax.xml.parsers.ParserConfigurationException
   */
  @Test
  public void testCycNart() throws CycConnectionException, IOException, ParserConfigurationException, SAXException {
    System.out.println("\n*** testCycNart ***");

    Nart cycNart = new NartImpl(DOLLAR, 1);
    //testCycObjectRetrievable(cycNart);
    Nart dollar1 = cycNart;
    assertNotNull(cycNart);
    assertEquals("(USDollarFn 1)", cycNart.toString());
    assertEquals("(" + US_DOLLAR_FN_STR + " 1)", cycNart.cyclify());

    Nart cycNart2 = new NartImpl(DOLLAR, 1);
    assertEquals(cycNart.toString(), cycNart2.toString());
    assertEquals(cycNart, cycNart2);

    // compareTo
    {
      ArrayList narts = new ArrayList();
      CycArrayList<Object> nartCycList = new CycArrayList<>();
      nartCycList.add(YEAR_FN);
      nartCycList.add(2000);
      Nart year2K = new NartImpl(nartCycList);
      narts.add(year2K);
      assertEquals("[(YearFn 2000)]", narts.toString());
      CycConstant person
              = getCyc().getLookupTool().getKnownConstantByGuid(
                      CycObjectFactory.makeGuid(TestConstants.PERSON.getGuid().getGuidString()));
      CycList nartCycList2 = new CycArrayList<>();
      nartCycList2.add(CONVEY_FN);
      nartCycList2.add(person);
      narts.add(new NartImpl(nartCycList2));
      CycList nartCycList3 = new CycArrayList<>();
      nartCycList3.add(DOLLAR);
      nartCycList3.add(1);
      narts.add(new NartImpl(nartCycList3));
      Collections.sort(narts);
      assertEquals("[(ConveyFn Person), (USDollarFn 1), (YearFn 2000)]",
              narts.toString());
    }

    // hasFunctorAndArgs
    assertTrue(dollar1.hasFunctorAndArgs());

    // toCycList()
    {
      CycList cycList = new CycArrayList<>();
      cycList.add(DOLLAR);
      cycList.add(1);
      assertEquals(cycList, dollar1.toCycList());
    }

    // check cfasl representation of narts in a list
    {
      CycList myNarts = new CycArrayList<>();
      final Object randomNart1 = getCyc().converse().converseObject("(random-nart)");
      myNarts.add(randomNart1);
      final Object randomNart2 = getCyc().converse().converseObject("(random-nart)");
      myNarts.add(randomNart2);

      for (int i = 0; i < myNarts.size(); i++) {
        assertTrue(myNarts.get(i) instanceof Nart);
      }
      String command = SublApiHelper.makeSublStmt("csetq",
              CycObjectFactory.makeCycSymbol("my-narts", false), myNarts);
      CycList myNartsBackFromCyc1 = getCyc().converse().converseList(command);
      CycList commandList = new CycArrayList();
      commandList.add(CycObjectFactory.makeCycSymbol("csetq"));
      commandList.add(CycObjectFactory.makeCycSymbol("my-narts"));
      commandList.addQuoted(myNarts);
      CycList myNartsBackFromCyc = getCyc().converse().converseList(commandList);
      for (int i = 0; i < myNartsBackFromCyc.size(); i++) {
        assertTrue(myNartsBackFromCyc.get(i) instanceof Nart);
        Nart myNartBackFromCyc = (Nart) myNartsBackFromCyc.get(i);
        assertTrue(myNartBackFromCyc.getFunctor() instanceof Fort);
        assertTrue(myNartBackFromCyc.getArguments() instanceof ArrayList);
        ArrayList args = (ArrayList) myNartBackFromCyc.getArguments();
        final Naut myNartFormula = ((Nart) myNarts.get(i)).getFormula();
        assertEquals(myNartFormula,
                ((Nart) myNartsBackFromCyc.get(i)).getFormula());
        assertEquals(myNartFormula,
                ((Nart) myNartsBackFromCyc1.get(i)).getFormula());

      }
    }

    // coerceToCycNart
    {
      NartImpl cycNart4 = new NartImpl(DOLLAR, 1);
      assertEquals(cycNart4, NartImpl.coerceToCycNart(cycNart4));
      CycList cycList4 = new CycArrayList<>();
      cycList4.add(DOLLAR);
      cycList4.add(1);
      assertEquals(cycNart2, NartImpl.coerceToCycNart(cycList4));

      // toXML, toXMLString
      XmlStringWriter xmlStringWriter = new XmlStringWriter();
      cycNart4.toXML(xmlStringWriter, 0, false);
      System.out.println(xmlStringWriter.toString());

      String cycNartXMLString = cycNart4.toXMLString();
      System.out.println("cycNartXMLString\n" + cycNartXMLString);
      Object object = CycObjectFactory.unmarshal(cycNartXMLString);
      assertTrue(object instanceof Nart);
      assertEquals(cycNart4, (Nart) object);
      NartImpl cycNart5 = new NartImpl(THE_LIST, 1, "a string");
      cycNartXMLString = cycNart5.toXMLString();
      System.out.println("cycNartXMLString\n" + cycNartXMLString);
      object = CycObjectFactory.unmarshal(cycNartXMLString);
      assertTrue(object instanceof Nart);
      assertEquals(cycNart5, (Nart) object);
    }

    // Check whether stringApiValue() behaves properly on a NART with a string argument
    {
      Nart attawapiskat = new NartImpl(CITY_NAMED_FN, "Attawapiskat",
              ONTARIO_CANADIAN_PROVINCE);

      Object result = CycUtils.evalSublWithWorker(getCyc(),
              attawapiskat.stringApiValue());
      assertTrue(result instanceof Nart);
      assertEquals(attawapiskat, (Nart) result);
    }

      // Check whether stringApiValue() behaves properly on a NART
    // with a string that contains a character that needs to be escaped in SubL
    {
      Nart hklmSam = new NartImpl(REGISTRY_KEY_FN, "HKLM\\SAM");

      Object result0 = NautImpl.convertIfPromising(CycUtils.evalSublWithWorker(getCyc(),
              hklmSam.stringApiValue()));
      assertTrue(result0 instanceof NonAtomicTerm);
      assertEquals(hklmSam.getFormula(), ((NonAtomicTerm) result0).getFormula());
    }

    /*
     CycAssertion cycAssertion = getCyc().getAssertionById(Integer.valueOf(968857));
     Nart complexNart = (Nart) cycAssertion.getFormula().second();
     System.out.println(complexNart.toString());
     System.out.println(complexNart.cyclify());
     */
    System.out.println("*** testCycNart OK ***");
  }

  /**
   * Tests StreamTokenizer CycList parsing behavior.
   */
  @Test
  public void testStreamTokenizer() {
    System.out.println("\n*** testStreamTokenizer ***");
    try {
      String string = "()";
      MyStreamTokenizer st = CycListParser.makeStreamTokenizer(string);
      assertEquals(40, st.nextToken());
      assertEquals(41, st.nextToken());
      assertEquals(MyStreamTokenizer.TT_EOF, st.nextToken());

      string = "(1)";
      st = CycListParser.makeStreamTokenizer(string);
      assertEquals(40, st.nextToken());

      int token = st.nextToken();
      assertEquals(MyStreamTokenizer.TT_WORD, token);
      assertEquals("1", st.sval);

      assertEquals(41, st.nextToken());
      assertEquals(MyStreamTokenizer.TT_EOF, st.nextToken());
      string = "(-10 -2 -1.0 -5.2E05)";
      st = CycListParser.makeStreamTokenizer(string);
      assertEquals(40, st.nextToken());

      token = st.nextToken();
      assertEquals(MyStreamTokenizer.TT_WORD, token);
      assertEquals("-10", st.sval);

      token = st.nextToken();
      assertEquals(MyStreamTokenizer.TT_WORD, token);
      assertEquals("-2", st.sval);

      token = st.nextToken();
      assertEquals(MyStreamTokenizer.TT_WORD, token);
      assertEquals("-1.0", st.sval);

      token = st.nextToken();
      assertEquals(MyStreamTokenizer.TT_WORD, token);
      assertEquals("-5.2E05", st.sval);

      assertEquals(41, st.nextToken());
      assertEquals(MyStreamTokenizer.TT_EOF, st.nextToken());

    } catch (Exception e) {
      e.printStackTrace(System.err);
      fail();
    }

    System.out.println("*** testStreamTokenizer OK ***");
  }

  /**
   * Tests the ByteArray class.
   * 
   * @throws java.io.IOException
   * @throws org.xml.sax.SAXException
   * @throws javax.xml.parsers.ParserConfigurationException
   */
  @Test
  public void testByteArray() throws IOException, ParserConfigurationException, SAXException {
    System.out.println("\n*** testByteArray ***");
    final byte[] bytes = {0, 1, 2, 3, 4, -128};
    final ByteArray byteArray1 = new ByteArray(bytes);
    assertNotNull(byteArray1);
    assertEquals(6, byteArray1.byteArrayValue().length);
    assertEquals(0, byteArray1.byteArrayValue()[0]);
    assertEquals(1, byteArray1.byteArrayValue()[1]);
    assertEquals(2, byteArray1.byteArrayValue()[2]);
    assertEquals(3, byteArray1.byteArrayValue()[3]);
    assertEquals(4, byteArray1.byteArrayValue()[4]);
    assertEquals(-128, byteArray1.byteArrayValue()[5]);
    final byte[] bytes2 = {0, 1, 2, 3, 4, -128};
    final ByteArray byteArray2 = new ByteArray(bytes2);
    assertEquals(byteArray1, byteArray1);
    assertEquals(byteArray1, byteArray2);
    final byte[] bytes3 = {0, -1, 2, 3, 4, -128};
    final ByteArray byteArray3 = new ByteArray(bytes3);
    assertFalse(byteArray1.equals(byteArray3));
    assertEquals("[ByteArray len:6 0,1,2,3,4,-128]", byteArray1.toString());

    // toXML, toXMLString, unmarshal
    final XmlStringWriter xmlStringWriter = new XmlStringWriter();
    byteArray1.toXML(xmlStringWriter, 0, false);
    final String expectedXmString
            = "<byte-vector>\n"
            + "  <length>6</length>\n"
            + "  <byte>0</byte>\n"
            + "  <byte>1</byte>\n"
            + "  <byte>2</byte>\n"
            + "  <byte>3</byte>\n"
            + "  <byte>4</byte>\n"
            + "  <byte>-128</byte>\n"
            + "</byte-vector>\n";

    assertEquals(expectedXmString, xmlStringWriter.toString());
    assertEquals(expectedXmString, byteArray1.toXMLString());
    assertEquals(byteArray1,
            CycObjectFactory.unmarshal(byteArray1.toXMLString()));
    System.out.println("*** testByteArray OK ***");
  }

  /**
   * Tests the ELMTCycList class.
   * @throws com.cyc.base.exception.CycConnectionException
   */
  @Test
  public void testELMTCycList() throws CycConnectionException {
    System.out.println("\n*** testELMTCycList ***");
    assumeNotOpenCyc();
    final CycObject mt = new NautImpl(
            getCyc().getObjectTool().makeCycList(
                    TestSentences.MT_SPACE_CYCLISTS_MT_TIME_POINT.cyclify()
            ));
    assertNotNull(getCyc().getLookupTool().getComment(CommonConstants.ISA, mt));
    System.out.println("*** testELMTCycList OK ***");
  }
  
  // Static helper methods
  
  public static void doTestCycObjectRetrievable(final CycObject obj) throws CycConnectionException {
    final String command = "(IDENTITY " + obj.stringApiValue() + ")";
    final CycObject retrievedVersion = getCyc().converse().converseCycObject(command);
    assertEquals(
            "Retrieved version of " + obj + " is not 'equals' to the original.",
            obj, retrievedVersion);
  }
  
}
