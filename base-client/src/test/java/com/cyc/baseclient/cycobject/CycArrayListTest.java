package com.cyc.baseclient.cycobject;

/*
 * #%L
 * File: CycArrayListTest.java
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
import com.cyc.base.cycobject.Nart;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.baseclient.CommonConstants;
import com.cyc.baseclient.CycObjectFactory;
import com.cyc.baseclient.datatype.Span;
import com.cyc.baseclient.testing.TestConstants;
import com.cyc.baseclient.testing.TestGuids;
import com.cyc.baseclient.testing.TestSentences;
import com.cyc.baseclient.testing.TestUtils;
import com.cyc.baseclient.util.CycUtils;
import com.cyc.baseclient.xml.Marshaller;
import com.cyc.baseclient.xml.XmlStringWriter;
import com.cyc.session.exception.SessionException;
import java.io.IOException;
import java.util.*;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import static com.cyc.baseclient.cycobject.ArgPositionImpl.*;
import static com.cyc.baseclient.testing.TestUtils.getCyc;
import static org.junit.Assert.*;

public class CycArrayListTest {
  
  // Constants
  
  private static final String AND_STR = CommonConstants.AND.stringApiValue();
  private static final String ISA_STR = CommonConstants.ISA.stringApiValue();
  
  private static final String MODERN_MILITARY_ORGANIZATION_STR 
          = TestConstants.MODERN_MILITARY_ORGANIZATION.cyclify();
  
  private static final String OBJECT_FOUND_IN_LOCATION_STR 
          = TestConstants.OBJECT_FOUND_IN_LOCATION.cyclify();
  
  private static final String ILLINOIS_STATE_STR = TestConstants.ILLINOIS_STATE.cyclify();
  private static final String BEHAVIOR_CAPABLE_STR = TestConstants.BEHAVIOR_CAPABLE.cyclify();
  
  private static final String REACTION_TO_SITUATION_TYPE_FN_STR 
          = TestConstants.REACTION_TO_SITUATION_TYPE_FN.cyclify();
  
  private static final String CHEMICAL_ATTACK_STR = TestConstants.CHEMICAL_ATTACK.cyclify();
  private static final String PERFORMED_BY_STR = TestConstants.PERFORMED_BY.cyclify();
  private static final String BRAZIL_STR = TestConstants.BRAZIL.cyclify();
  private static final String JUVENILE_FN_STR = TestConstants.JUVENILE_FN.cyclify();
  private static final String DOG_STR = TestConstants.DOG.cyclify();
  private static final String IST_ASSERTED_STR = TestConstants.IST_ASSERTED.cyclify();
  
  private static final String TOTAL_INVESTMENT_EARNINGS_FOR_STOCK_TYPE_BOUGHT_DURING_STR 
          = TestConstants.TOTAL_INVESTMENT_EARNINGS_FOR_STOCK_TYPE_BOUGHT_DURING.cyclify();
  
  private static final String TECH_STOCK_STR = TestConstants.TECH_STOCK.cyclify();
  private static final String MINUS_FN_STR = TestConstants.MINUS_FN.cyclify();
  private static final String POUND_GREAT_BRITAIN_STR = TestConstants.POUND_GREAT_BRITAIN.cyclify();
  private static final String EARLY_PART_FN_STR = TestConstants.EARLY_PART_FN.cyclify();
  
  private static final String THE_MOTLEY_FOOL_UK_CORPUS_MT_STR
          = TestConstants.THE_MOTLEY_FOOL_UK_CORPUS_MT.cyclify();
  
  private static final String YEAR_FN_STR = CommonConstants.YEAR_FN.cyclify();
  private static final String INSTANCE_NAMED_FN_STR = TestConstants.INSTANCE_NAMED_FN.cyclify();
    
  // Setup
  
  @Before
  public void setUp() throws CycConnectionException, SessionException {
    TestUtils.ensureTestEnvironmentInitialized();
  }
  
  // Tests
  
  @Test
  public void testConstruct() throws CycConnectionException {
    Object object1 = CycArrayList.construct(TestConstants.BRAZIL, CycObjectFactory.nil);
    assertNotNull(object1);
    assertTrue(object1 instanceof CycArrayList);
    assertEquals("(Brazil)", object1.toString());
  }
  
  @Test
  public void testParse_emptyList() throws CycConnectionException {
    final String listAsString = "()";
    final CycList cycList = getCyc().getObjectTool().makeCycList(listAsString);
    assertEquals(listAsString, cycList.toString());
  }
  
  @Test
  public void testParse_singleElementList() throws CycConnectionException {
    final String listAsString = "(1)";
    final CycList cycList = getCyc().getObjectTool().makeCycList(listAsString);
    assertEquals(listAsString, cycList.toString());
  }
  
  @Test
  public void testParse() throws CycConnectionException {
    String listAsString = "(1 2 3 4 5)";
    final CycList cycList1 = getCyc().getObjectTool().makeCycList(listAsString);
    assertEquals(listAsString, cycList1.toString());
    
    listAsString = "(\"1\" \"bar\" A " + BRAZIL_STR + " Z 4.25 :KEYWORD ?COLLECTION NIL)";
    final CycList cycList2 = getCyc().getObjectTool().makeCycList(listAsString);
    assertEquals(listAsString, cycList2.cyclify());
    
    listAsString = "((A))";
    final CycList cycList3 = getCyc().getObjectTool().makeCycList(listAsString);
    assertEquals(listAsString, cycList3.toString());
    
    listAsString = "((A) (B C) (((D))))";
    final CycList cycList4 = getCyc().getObjectTool().makeCycList(listAsString);
    assertEquals(listAsString, cycList4.toString());
    final CycList cycList5 = getCyc().getObjectTool().makeCycList(listAsString);
    assertEquals(cycList5.toString(), cycList4.toString());
    assertEquals(cycList5.toString(), cycList4.toString());
    assertEquals(getCyc().getObjectTool().makeCycList("(A)"), cycList5.first());
    assertEquals(getCyc().getObjectTool().makeCycList("(B C)"), cycList5.second());
    assertEquals(getCyc().getObjectTool().makeCycList("(((D)))"), cycList5.third());
    
    listAsString = "(apply #'+ '(1 2 3))";
    final CycList cycList6 = getCyc().getObjectTool().makeCycList(listAsString);
    assertEquals("(APPLY (FUNCTION +) (QUOTE (1 2 3)))",
            cycList6.toString());
    
    listAsString = "(1 2 \n"
            + " ;; a comment \n"
            + " 3 4 5)";
    final CycList cycList7 = getCyc().getObjectTool().makeCycList(listAsString);
    assertEquals(cycList1, cycList7);
    listAsString = "(" + Double.toString(1.0E-05) + ")";
    final CycList cycList8 = getCyc().getObjectTool().makeCycList(listAsString);
    assertEquals(listAsString, cycList8.cyclify());
    final CycListParser cycListParser = new CycListParser(getCyc());
    listAsString = "(1 2 3) 4 \"5 6\" 7 (8 9 10) 11 test";
    final CycList cycList9 = cycListParser.read(listAsString);
    assertEquals("(1 2 3)", cycList9.toString());
    assertEquals(" 4 \"5 6\" 7 (8 9 10) 11 test",
            cycListParser.remainingString());
    listAsString
            = "(" + IST_ASSERTED_STR + " \n"
            + "  (" + TOTAL_INVESTMENT_EARNINGS_FOR_STOCK_TYPE_BOUGHT_DURING_STR + "  \n"
            + "    " + TECH_STOCK_STR + "  \n"
            + "    (" + MINUS_FN_STR + " (" + POUND_GREAT_BRITAIN_STR + " 330000000000))  \n"
            + "    (" + EARLY_PART_FN_STR + " (" + YEAR_FN_STR + " 2000)))  \n"
            + "  " + THE_MOTLEY_FOOL_UK_CORPUS_MT_STR + "))";
    final CycList cycList19c = cycListParser.read(listAsString);
    assertTrue(cycList19c.cyclify().contains("330000000000"));
    assertTrue(DefaultCycObjectImpl.cyclify(cycList19c).contains("330000000000"));
    doTestCycListAdd();
  }
  
  @Test
  public void testTreeContains() throws CycConnectionException {  
    final CycArrayList cycList = (CycArrayList) getCyc().getObjectTool().makeCycList(
            "(DEFMACRO-IN-API MY-MACRO (A B C) (RET ` (LIST , A , B , C)))");
    assertTrue(cycList.treeContains(CycObjectFactory.backquote));
  }
  
  @Test
  public void testSubst() throws CycConnectionException {
    final CycList cycList1 = getCyc().getObjectTool().makeCycList("(b)");
    final CycList cycList2 = cycList1.subst(CycObjectFactory.makeCycSymbol("x"),
            CycObjectFactory.makeCycSymbol("a"));
    assertEquals(getCyc().getObjectTool().makeCycList("(b)"), cycList2);
    final CycList cycList20 = getCyc().getObjectTool().makeCycList("(a)");
    final CycList cycList21 = cycList20.subst(CycObjectFactory.makeCycSymbol("x"),
            CycObjectFactory.makeCycSymbol("a"));
    assertEquals(getCyc().getObjectTool().makeCycList("(x)"), cycList21);
    final CycList cycList22 = getCyc().getObjectTool().makeCycList("((a))");
    final CycList cycList23 = cycList22.subst(CycObjectFactory.makeCycSymbol("x"),
            CycObjectFactory.makeCycSymbol("a"));
    assertEquals(getCyc().getObjectTool().makeCycList("((x))"), cycList23);
    final CycList cycList24 = getCyc().getObjectTool().makeCycList("((a) (b c) (((d))))");
    final CycList cycList25 = cycList24.subst(CycObjectFactory.makeCycSymbol("x"),
            CycObjectFactory.makeCycSymbol("a"));
    assertEquals(getCyc().getObjectTool().makeCycList("((x) (b c) (((d))))"), cycList25);
  }
  
  @Test
  public void testCycList1() throws CycConnectionException {
    System.out.println("\n*** testCycList1 ***");
    
    // Simple empty list constructor.
    final ArrayList arrayList = new ArrayList();
    final CycList cycList = new CycArrayList<>(arrayList);
    assertNotNull(cycList);
    assertEquals("()", cycList.toString());
    
    // Construct list of one element.
    final ArrayList arrayList2 = new ArrayList();
    CycObjectFactory.addCycConstantCache(TestConstants.BRAZIL);
    arrayList2.add(TestConstants.BRAZIL);
    final CycList cycList2 = new CycArrayList<>(arrayList2);
    assertEquals("(Brazil)", cycList2.toString());
    assertEquals("(" + BRAZIL_STR + ")", cycList2.cyclify());
    
    // Construct list with embedded sublist.
    final ArrayList arrayList3 = new ArrayList();
    arrayList3.add(TestConstants.BRAZIL);
    arrayList3.add(cycList);
    arrayList3.add(cycList2);
    final CycList cycList3 = new CycArrayList<>(arrayList3);
    assertEquals("(Brazil () (Brazil))", cycList3.toString());
    assertEquals("(" + BRAZIL_STR + " () (" + BRAZIL_STR + "))", cycList3.cyclify());
    
    // isValid()
    assertTrue(cycList.isValid());
    assertTrue(cycList2.isValid());
    assertTrue(cycList3.isValid());
    final CycList cycList4 = new CycArrayList(new HashMap());
    assertFalse(cycList4.isValid());
  
    // first(), rest()
    ArrayList arrayList5 = new ArrayList();
    arrayList5.add(TestConstants.BRAZIL);
    final CycList cycList5 = new CycArrayList<>(arrayList5);
    assertEquals("(Brazil)", cycList5.toString());
    assertEquals("(" + BRAZIL_STR + ")", cycList5.cyclify());
    assertEquals(cycList5.first(), TestConstants.BRAZIL);
    assertTrue(((CycArrayList) (cycList5.rest())).isEmpty());
    final CycList cycList5a = new CycArrayList<>();
    cycList5a.add("a");
    cycList5a.setDottedElement("b");
    assertEquals("b", cycList5a.rest());
    
    // reverse()
    assertEquals(cycList5.toString(), cycList5.reverse().toString());
    assertEquals("((" + BRAZIL_STR + ") () " + BRAZIL_STR + ")", cycList3.reverse().cyclify());
  }
  
  @Test
  public void testReverse() throws CycConnectionException {
    // reverse of strings.
    ArrayList arrayList6 = new ArrayList();
    arrayList6.add("z");
    arrayList6.add("y");
    arrayList6.add("x");
    final CycList cycList6 = new CycArrayList(arrayList6);
    assertEquals("(\"z\" \"y\" \"x\")", cycList6.toString());
    assertEquals("(\"x\" \"y\" \"z\")", cycList6.reverse().toString());
  }
  
  @Test
  public void testImproperLists() throws CycConnectionException {
    // Improper lists.
    ArrayList arrayList1 = new ArrayList();
    arrayList1.add(10);
    final CycList cycList1 = new CycArrayList(arrayList1);
    cycList1.setDottedElement(TestConstants.BRAZIL);
    assertTrue(cycList1.size() == 2);
    assertEquals("(10 . Brazil)", cycList1.toString());
    //CycListParser.verbosity = 10;

    final CycListParser cycListParser = new CycListParser(null);
    final CycList cycList2 = cycListParser.read("(a b c)");
    assertEquals("(A B C)", cycList2.toString());

    final CycList cycList3 = getCyc().getObjectTool().makeCycList("(a . (b . (c . (d))))");
    assertEquals("(A B C D)", cycList3.toString());
    final CycList cycList4 = getCyc().getObjectTool().makeCycList("((a . b) . (c . d))");
    assertEquals("((A . B) C . D)", cycList4.toString());
    final CycList cycList5 = getCyc().getObjectTool().makeCycList("((a . (b)) . (c . (d)))");
    assertEquals("((A B) C D)", cycList5.toString());
    final CycList cycList6 = getCyc().getObjectTool().makeCycList("(a b . c)");
    assertEquals("(A B . C)", cycList6.toString());
    final CycList cycList7 = getCyc().getObjectTool().makeCycList("(a b c . d)");
    assertEquals("(A B C . D)", cycList7.toString());
  }
  
  @Test
  public void testMakeDottedPair() throws CycConnectionException {
    final CycList cycList1 = CycArrayList.makeDottedPair(TestConstants.BRAZIL, "Atlantic");
    System.out.println(cycList1.toString());
    assertEquals("(Brazil . \"Atlantic\")", cycList1.toString());
    
    final CycList cycList2 = CycArrayList.makeDottedPair(TestConstants.BRAZIL, 1);
    System.out.println(cycList2.toString());
    assertEquals("(Brazil . 1)", cycList2.toString());
    
    final CycList cycList3 = CycArrayList.makeDottedPair(TestConstants.BRAZIL,
            CycObjectFactory.makeCycSymbol("foo"));
    System.out.println(cycList3.toString());
    assertEquals("(Brazil . FOO)", cycList3.toString());
  }
  
  @Test
  public void testStringApiValue_dotted() throws CycConnectionException {
    // stringApiValue() on a dotted CycArrayList
    final CycList dottedCycList = new CycArrayList("first element", "second element");
    dottedCycList.setDottedElement("dotted element");
    System.out.println(dottedCycList.stringApiValue());
    Object resultObj = CycUtils.evalSublWithWorker(getCyc(),
            dottedCycList.stringApiValue());
    assertTrue(resultObj instanceof CycArrayList);
    System.out.println(resultObj);
    assertEquals(dottedCycList, (CycArrayList) resultObj);
    // Parse a list containing a string with a backslash
    final String script = "(identity \"abc\")";
    resultObj = CycUtils.evalSublWithWorker(getCyc(), script);
    assertTrue(resultObj instanceof String);
  }
  
  @Test
  public void testAddPair() {
    // TODO: expand & improve - nwinant, 2017-08-04
    final String expectedStr = ""
                                       + "((:KEY2 . \"value1\")"
                                       + " (:KEY2 . \"value3\")"
                                       + " (:KEY2 . \"value2\")"
                                       + " (\"KEY1\" . \"value1\")"
                                       + " (\"KEY2\" . \"value3\")"
                                       + " (\"KEY3\" . \"value2\"))";
    final CycList list = new CycArrayList();
    //assertFalse(list.isPlist());
    assertTrue(list.isProperList());
    list.addPair(new CycSymbolImpl(":KEY2"), "value1");
    list.addPair(new CycSymbolImpl(":KEY2"), "value3");
    list.addPair(new CycSymbolImpl(":KEY2"), "value2");
    list.addPair("KEY1", "value1");
    list.addPair("KEY2", "value3");
    list.addPair("KEY3", "value2");
    assertFalse(list.isPlist());
    //assertFalse(list.isProperList());
    final String resultStr = list.toString();
    System.out.println("Result: " + resultStr);
    System.out.println("Result: " + list);
    assertEquals(expectedStr, resultStr);
  }
  
  @Test
  public void testSetf() {
    // TODO: expand & improve - nwinant, 2017-08-04
    final String expectedStr = "(list"
                                       + " :KEY1 \"value1\""
                                       + " :KEY3 \"value3\""
                                       + " :KEY2 \"value2\")";
    final CycList list = new CycArrayList();
    //assertFalse(list.isPlist());
    assertTrue(list.isProperList());
    list.setf(new CycSymbolImpl(":KEY1"), "value1");
    list.setf(new CycSymbolImpl(":KEY3"), "value3");
    list.setf(new CycSymbolImpl(":KEY2"), "value2");
    assertTrue(list.isPlist());
    final String resultStr = list.stringApiValue();
    System.out.println("Result: " + resultStr);
    assertEquals(expectedStr, resultStr);
  }
  
  @Test
  public void testGetValueForKeyword() throws CycConnectionException {  
    final String listString = ""
                                      + "(fipa-transport-message\n"
                                      + "  (envelope\n"
                                      + "    :to my-remote-agent\n"
                                      + "    :from my-cyc-agent\n"
                                      + "    :date 3215361678\n"
                                      + "    :X-agent-community :coabs\n"
                                      + "    :X-cyc-image-id \"balrog-200111112091457-939\"\n"
                                      + "    :X-base-tcp-port 3600)\n"
                                      + "  (payload\n"
                                      + "    (inform\n"
                                      + "      :sender my-cyc-agent\n"
                                      + "      :receiver my-remote-agent\n"
                                      + "      :reply-to message1\n"
                                      + "      :content \"Hello from my-cyc-agent\"\n"
                                      + "      :language :cycl\n"
                                      + "      :reply-with \"my cookie\"\n"
                                      + "      :ontology cyc-api\n"
                                      + "      :protocol :fipa-request)))";
    final CycArrayList cycList = (CycArrayList) getCyc().getObjectTool().makeCycList(listString);
    assertEquals(cycList.size(), 3);
    assertFalse(cycList.isPlist());
    assertEquals(cycList.first(), CycObjectFactory.makeCycSymbol(
            "fipa-transport-message"));
    assertTrue(cycList.second() instanceof CycArrayList);
    final CycList envelope = (CycArrayList) cycList.second();
    assertEquals(CycObjectFactory.makeCycSymbol("my-remote-agent"),
            envelope.getValueForKeyword(CycObjectFactory.makeCycSymbol(":to")));
    assertEquals(CycObjectFactory.makeCycSymbol("my-cyc-agent"),
            envelope.getValueForKeyword(CycObjectFactory.makeCycSymbol(":from")));
    assertEquals(new Long("3215361678"),
            envelope.getValueForKeyword(CycObjectFactory.makeCycSymbol(":date")));
    assertEquals(CycObjectFactory.makeCycSymbol(":coabs"),
            envelope.getValueForKeyword(CycObjectFactory.makeCycSymbol(
            ":X-agent-community")));
    assertEquals("balrog-200111112091457-939",
            envelope.getValueForKeyword(CycObjectFactory.makeCycSymbol(
            ":X-cyc-image-id")));
    assertEquals(3600,
            envelope.getValueForKeyword(CycObjectFactory.makeCycSymbol(
            ":X-base-tcp-port")));
    assertNull(envelope.getValueForKeyword(CycObjectFactory.makeCycSymbol(
            ":not-there")));
    assertFalse(envelope.isPlist());
    assertTrue(cycList.third() instanceof CycArrayList);
    assertTrue(cycList.third() instanceof CycArrayList);
    final CycList payload = (CycArrayList) cycList.third();
    assertTrue(payload.second() instanceof CycArrayList);
    final CycList aclList = (CycArrayList) payload.second();
    assertEquals(CycObjectFactory.makeCycSymbol("my-cyc-agent"),
            aclList.getValueForKeyword(CycObjectFactory.makeCycSymbol(":sender")));
    assertEquals(CycObjectFactory.makeCycSymbol("my-remote-agent"),
            aclList.getValueForKeyword(CycObjectFactory.makeCycSymbol(
            ":receiver")));
    assertEquals(CycObjectFactory.makeCycSymbol("message1"),
            aclList.getValueForKeyword(CycObjectFactory.makeCycSymbol(
            ":reply-to")));
    assertEquals("Hello from my-cyc-agent",
            aclList.getValueForKeyword(
            CycObjectFactory.makeCycSymbol(":content")));
    assertEquals(CycObjectFactory.makeCycSymbol(":cycl"),
            aclList.getValueForKeyword(CycObjectFactory.makeCycSymbol(
            ":language")));
    assertEquals("my cookie",
            aclList.getValueForKeyword(CycObjectFactory.makeCycSymbol(
            ":reply-with")));
    assertEquals(CycObjectFactory.makeCycSymbol("cyc-api"),
            aclList.getValueForKeyword(CycObjectFactory.makeCycSymbol(
            ":ontology")));
    assertEquals(CycObjectFactory.makeCycSymbol(":fipa-request"),
            aclList.getValueForKeyword(CycObjectFactory.makeCycSymbol(
            ":protocol")));
    assertNull(aclList.getValueForKeyword(CycObjectFactory.makeCycSymbol(
            ":not-there")));
  }
  
  @Test
  public void testContainsDuplicates() throws CycConnectionException {
    System.out.println("\n*** testCycList2 ***");
    final CycList cycList1 = getCyc().getObjectTool().makeCycList("(a b c d)");
    assertFalse(cycList1.containsDuplicates());
    final CycList cycList2 = getCyc().getObjectTool().makeCycList("(a a c d)");
    assertTrue(cycList2.containsDuplicates());
    final CycList cycList3 = getCyc().getObjectTool().makeCycList("(a b c c)");
    assertTrue(cycList3.containsDuplicates());
    final CycList cycList4 = getCyc().getObjectTool().makeCycList("(a (b) (b) c)");
    assertTrue(cycList4.containsDuplicates());
  }
  
  @Test
  public void testList() throws CycConnectionException {
    final CycList cycList1 = CycArrayList.list(CycObjectFactory.makeCycSymbol("a"));
    assertEquals("(A)", cycList1.toString());
    final CycList cycList2 = CycArrayList.list(CycObjectFactory.makeCycSymbol("a"),
            CycObjectFactory.makeCycSymbol("b"));
    assertEquals("(A B)", cycList2.toString());
    final CycList cycList3 = CycArrayList.list(CycObjectFactory.makeCycSymbol("a"),
            CycObjectFactory.makeCycSymbol("b"),
            CycObjectFactory.makeCycSymbol("c"));
    assertEquals("(A B C)", cycList3.toString());
  }
  
  @Test
  public void testCombinationsOf() throws CycConnectionException {
    final CycList cycList = getCyc().getObjectTool().makeCycList("(1 2 3 4)");
    assertEquals("((1) (2) (3) (4))", cycList.combinationsOf(1).toString());
    assertEquals("((1 2) (1 3) (1 4) (2 3) (2 4) (3 4))",
            cycList.combinationsOf(2).toString());
    assertEquals("((1 2 3 4))",
            cycList.combinationsOf(4).toString());
    assertEquals("()",
            cycList.combinationsOf(0).toString());
    assertEquals("()",
            (new CycArrayList()).combinationsOf(4).toString());
  }
  
  @Test
  public void testRandomPermutation() throws CycConnectionException {
    final CycList cycList = getCyc().getObjectTool().makeCycList("(1 2 3 4 5 6 7 8 9 10)");
    final CycList permutedCycList = cycList.randomPermutation();
    assertEquals(10, permutedCycList.size());
    assertTrue(permutedCycList.contains(2));
    assertFalse(permutedCycList.containsDuplicates());
}
  
  @Test
  public void testDoesElementPrecedeOthers() throws CycConnectionException {
    final CycList cycList = getCyc().getObjectTool().makeCycList("(1 2 3 4 5 6 7 8 9 10)");
    assertTrue(cycList.doesElementPrecedeOthers(1,
            getCyc().getObjectTool().makeCycList("(8 7 6)")));
    assertTrue(cycList.doesElementPrecedeOthers(9,
            getCyc().getObjectTool().makeCycList("(10)")));
    assertTrue(cycList.doesElementPrecedeOthers(10,
            getCyc().getObjectTool().makeCycList("(18 17 16)")));
    assertFalse(cycList.doesElementPrecedeOthers(12,
            getCyc().getObjectTool().makeCycList("(1 2 10)")));
    assertFalse(cycList.doesElementPrecedeOthers(9,
            getCyc().getObjectTool().makeCycList("(8 7 6)")));
  }
  
  @Test
  public void testClone() throws CycConnectionException {
    final CycList cycList1 = getCyc().getObjectTool().makeCycList("(1 2 3 4 5)");
    final CycList cycList2 = (CycArrayList) cycList1.clone();
    assertEquals(cycList1, cycList2);
    assertTrue(cycList1 != cycList2);
    final CycList cycList3 = getCyc().getObjectTool().makeCycList("(1 2 3 4 5 . 6)");
    final CycList cycList4 = (CycArrayList) cycList3.clone();
    assertEquals(cycList3, cycList4);
    assertTrue(cycList3 != cycList4);
  }
  
  @Test
  public void testDeepCopy() throws CycConnectionException {
    final CycList cycList1 = getCyc().getObjectTool().makeCycList("(1 2 3 4 5)");
    final CycList cycList2 = (CycArrayList) cycList1.deepCopy();
    assertEquals(cycList1, cycList2);
    assertTrue(cycList1 != cycList2);
    final CycList cycList3 = getCyc().getObjectTool().makeCycList("(1 2 3 4 5 . 6)");
    final CycList cycList4 = (CycArrayList) cycList3.deepCopy();
    assertEquals(cycList3, cycList4);
    assertTrue(cycList3 != cycList4);
    final CycList cycList5 = getCyc().getObjectTool().makeCycList("(1 (2 3) (4 5) ((6)))");
    final CycList cycList6 = (CycArrayList) cycList5.deepCopy();
    assertEquals(cycList5, cycList6);
    assertTrue(cycList5 != cycList6);
    assertEquals(cycList5.first(), cycList6.first());
    assertTrue(cycList5.first() == cycList6.first());
    assertEquals(cycList5.second(), cycList6.second());
    assertTrue(cycList5.second() != cycList6.second());
    assertEquals(cycList5.fourth(), cycList6.fourth());
    assertTrue(cycList5.fourth() != cycList6.fourth());
    assertEquals(((CycArrayList) cycList5.fourth()).first(),
            ((CycArrayList) cycList6.fourth()).first());
    assertTrue(((CycArrayList) cycList5.fourth()).first()
            != ((CycArrayList) cycList6.fourth()).first());
  }
  
  @Test
  public void testAddNew() throws CycConnectionException {
    final CycList cycList = getCyc().getObjectTool().makeCycList("(1 2 3 4 5)");
    assertEquals(5, cycList.size());
    cycList.addNew(6);
    assertEquals(6, cycList.size());
    cycList.addNew(2);
    assertEquals(6, cycList.size());
  }
  
  @Test
  public void testAddAllNew() throws CycConnectionException {
    final CycArrayList cycList1 = (CycArrayList) getCyc().getObjectTool().makeCycList("(1 2 3 4 5)");
    assertEquals(5, cycList1.size());
    final CycList cycList2 = getCyc().getObjectTool().makeCycList("(6 7 8 9 10)");
    assertEquals(5, cycList2.size());
    cycList1.addAllNew(cycList2);
    assertEquals(10, cycList1.size());
    final CycList cycList3 = getCyc().getObjectTool().makeCycList("(2 5 8 9 11)");
    assertEquals(5, cycList3.size());
    cycList1.addAllNew(cycList3);
    assertEquals(11, cycList1.size());
  }
  
  @Test
  public void testCycList3() throws CycConnectionException, IOException, ParserConfigurationException, SAXException {
    System.out.println("\n*** testCycList3 ***");
    
    // last
    final CycList cycList1 = getCyc().getObjectTool().makeCycList("(8 7 6)");
    assertEquals(6, cycList1.last());
    // toXML, toXMLString
    final String listAsString = "(\"1\" A (" + BRAZIL_STR + " . Z) 4.25 :KEYWORD ?collection NIL . " + DOG_STR + ")";
    final CycArrayList cycList2 = (CycArrayList) getCyc().getObjectTool().makeCycList(listAsString);
    XmlStringWriter xmlStringWriter = new XmlStringWriter();
    String cycListXMLString = cycList2.toXMLString();
    Object object = CycObjectFactory.unmarshal(cycListXMLString);
    assertTrue(object instanceof CycArrayList);
    assertEquals(cycList2, (CycArrayList) object);
    final CycList cycList3
            = getCyc().getObjectTool().makeCycList("(T " + TestSentences.BIOLOGICAL_TAXON_ETC.cyclify() + ")");
    cycListXMLString = Marshaller.marshall(cycList3);
//      System.out.println(cycListXMLString);
    object = CycObjectFactory.unmarshal(cycListXMLString);
    assertTrue(object instanceof CycArrayList);
    assertEquals(cycList3, (CycArrayList) object);
    cycListXMLString
            = "\n<list>\n"
            + "  <symbol>QUOTE</symbol>\n"
            + "  <list>\n"
            + "    <symbol>A</symbol>\n"
            + "    <dotted-element>\n"
            + "      <symbol>B</symbol>\n"
            + "    </dotted-element>\n"
            + "  </list>\n"
            + "</list>\n";
    object = CycObjectFactory.unmarshal(cycListXMLString);
    assertTrue(object instanceof CycArrayList);
    final CycList cycList4 = getCyc().getObjectTool().makeCycList("(QUOTE (A . B))");
    assertEquals(cycList4, object);
  }
  
  @Test
  public void testCycList4() throws CycConnectionException {
    System.out.println("\n*** testCycList4 ***");
    
    // addQuoted
    final CycList cycList = new CycArrayList();
    cycList.add(1);
    cycList.addQuoted(CycObjectFactory.makeCycSymbol("quote-me"));
    assertEquals("(1 (QUOTE QUOTE-ME))", cycList.toString());

    // toString (with null element)
    final CycList cycList2 = new CycArrayList();
    cycList2.add(null);
    assertNull(cycList2.first());
    assertEquals("(null)", cycList2.toString());

    // treeConstants
    final CycList cycList3
            = getCyc().getObjectTool().makeCycList("(T " + TestSentences.BIOLOGICAL_TAXON_ETC.cyclify() + ")");
    cycList3.add(new NartImpl(getCyc().getLookupTool().getKnownConstantByName("FruitFn"),
            getCyc().getLookupTool().getKnownConstantByName("PumpkinPlant")));
    final CycList cycList55 = cycList3.treeConstants();
    assertEquals(7, cycList55.size());
    
    // stringApiValue()
    CycConstant ontario
            = getCyc().getLookupTool().getKnownConstantByGuid(
                    CycObjectFactory.makeGuid(TestGuids.ONTARIO_CANADIAN_PROVINCE_GUID_STRING));
    final CycList cycList4 = new CycArrayList(ontario);
    Object result56 = CycUtils.evalSublWithWorker(getCyc(),
            cycList4.stringApiValue());
    assertTrue(result56 instanceof CycArrayList);
    assertEquals(cycList4, (CycArrayList) result56);
    // Check whether stringApiValue works properly on a CycList with a NartImpl element
    CycConstant cityNamedFn
            = getCyc().getLookupTool().getKnownConstantByGuid(
                    CycObjectFactory.makeGuid(TestGuids.CITY_NAMED_FN_GUID_STRING));
    Nart attawapiskat = new NartImpl(cityNamedFn, "Attawapiskat", ontario);
    final CycList cycListWithNart = new CycArrayList(ontario, attawapiskat);
    Object resultObj = CycUtils.evalSublWithWorker(getCyc(),
            cycListWithNart.stringApiValue());
    assertTrue(resultObj instanceof CycArrayList);
    assertEquals(cycListWithNart.cyclify(), ((CycArrayList) resultObj).cyclify());
    // stringApiValue() on a CycList containing a String containing a double-quote
    final CycList cycListWithString = new CycArrayList(
            "How much \"wood\" would a \"woodchuck\" \"chuck\"?");
    resultObj = CycUtils.evalSublWithWorker(getCyc(),
            cycListWithString.stringApiValue());
    assertTrue(resultObj instanceof CycArrayList);
    assertEquals(cycListWithString, (CycArrayList) resultObj);
  }
  
  @Test
  public void testCycList5() throws CycConnectionException, IOException {
    final String script1 = "(identity \"abc\\\\\")";
    Object result1 = CycUtils.evalSublWithWorker(getCyc(), script1);
    assertTrue(result1 instanceof String);
    final CycList command = new CycArrayList();
    command.add(CycObjectFactory.makeCycSymbol("identity"));
    command.add("abc\\");
    
    final String script2 = command.cyclifyWithEscapeChars();
    Object result2 = CycUtils.evalSublWithWorker(getCyc(), script2);
    assertTrue(result2 instanceof String);

    final String xml = ((CycArrayList) CycArrayList.makeCycList(2, 3, "foo")).toXMLString();
    assertNotNull(xml);
  }

  /**
   * Tests <tt>CycArrayListVisitor</tt> object behavior.
   * @throws com.cyc.base.exception.CycConnectionException
   */
  @Test
  public void testCycListVisitor() throws CycConnectionException {
    System.out.println("\n*** testCycListVisitor ***");
    CycListParser.verbosity = 0;
    final CycList cycList1 = getCyc().getObjectTool().makeCycList("(1 . 24)");
    final CycList cycList2 = getCyc().getObjectTool().makeCycList("(1 . 23)");
    assertFalse(cycList2.equals(cycList1));

    final CycList cycList3 = getCyc().getObjectTool().makeCycList("()");
    Enumeration e1 = cycList3.cycListVisitor();
    assertFalse(e1.hasMoreElements());

    final CycList cycList4 = getCyc().getObjectTool().makeCycList("(1 \"a\" :foo " + BRAZIL_STR + ")");
    Enumeration e2 = cycList4.cycListVisitor();
    assertTrue(e2.hasMoreElements());
    Integer integer1 = 1;
    Object nextObject = e2.nextElement();
    assertTrue(nextObject instanceof Integer);
    assertTrue(((Integer) nextObject).intValue() == integer1.intValue());
    assertTrue(((Integer) nextObject) == 1);
    assertTrue(e2.hasMoreElements());
    assertEquals("a", e2.nextElement());
    assertTrue(e2.hasMoreElements());
    assertEquals(CycObjectFactory.makeCycSymbol(":foo"), e2.nextElement());
    assertTrue(e2.hasMoreElements());
    assertEquals(getCyc().getObjectTool().makeCycConstant(BRAZIL_STR),
            e2.nextElement());
    assertFalse(e1.hasMoreElements());

    final CycList cycList5 = getCyc().getObjectTool().makeCycList("((()))");
    Enumeration e3 = cycList5.cycListVisitor();
    assertFalse(e3.hasMoreElements());

    final CycList cycList8 = getCyc().getObjectTool().makeCycList("(()())");
    Enumeration e4 = cycList8.cycListVisitor();
    assertFalse(e4.hasMoreElements());

    final CycList cycList9 = getCyc().getObjectTool().makeCycList(
            "(\"a\" (\"b\") (\"c\") \"d\" \"e\")");
    Enumeration e5 = cycList9.cycListVisitor();
    assertTrue(e5.hasMoreElements());
    assertEquals("a", e5.nextElement());
    assertTrue(e5.hasMoreElements());
    assertEquals("b", e5.nextElement());
    assertTrue(e5.hasMoreElements());
    assertEquals("c", e5.nextElement());
    assertTrue(e5.hasMoreElements());
    assertEquals("d", e5.nextElement());
    assertTrue(e5.hasMoreElements());
    assertEquals("e", e5.nextElement());
    assertFalse(e5.hasMoreElements());

    final CycList cycList10 = getCyc().getObjectTool().makeCycList(
            "(\"a\" (\"b\" \"c\") (\"d\" \"e\"))");
    Enumeration e6 = cycList10.cycListVisitor();
    assertTrue(e6.hasMoreElements());
    assertEquals("a", e6.nextElement());
    assertTrue(e6.hasMoreElements());
    assertEquals("b", e6.nextElement());
    assertTrue(e6.hasMoreElements());
    assertEquals("c", e6.nextElement());
    assertTrue(e6.hasMoreElements());
    assertEquals("d", e6.nextElement());
    assertTrue(e6.hasMoreElements());
    assertEquals("e", e6.nextElement());
    assertFalse(e6.hasMoreElements());
  }
  
  @Test
  public void testGetPrettyStringDetails_1() throws Exception {
    System.out.println("\n*** testGetPrettyStringDetails_1 ***");
    final CycArrayList example = (CycArrayList) com.cyc.baseclient.parser.CyclParserUtil.parseCycLTerm(TestSentences.ISA_TOTO_DOG_STRING, true, getCyc());
    final Map<ArgPositionImpl, Span> map = example.getPrettyStringDetails();
    checkPrettyStringDetail(map, ArgPositionImpl.TOP, 0, 27);
    checkPrettyStringDetail(map, ARG0, 1, 4);
    checkPrettyStringDetail(map, ARG1, 5, 22);
    checkPrettyStringDetail(map, ARG2, 23, 26);
  }
  
  @Test
  public void testGetPrettyStringDetails_2() throws Exception {
    System.out.println("\n*** testGetPrettyStringDetails_2 ***");
    final CycArrayList example2 = (CycArrayList) com.cyc.baseclient.parser.CyclParserUtil.parseCycLTerm(
            "(" + ISA_STR + " (" + INSTANCE_NAMED_FN_STR + " \"Muffet\" (" + JUVENILE_FN_STR + " " + DOG_STR + "))"
            + " (" + JUVENILE_FN_STR + " " + DOG_STR + "))",
            true, getCyc());
    final Map<ArgPositionImpl, Span> map = example2.getPrettyStringDetails();
    checkPrettyStringDetail(map, ArgPositionImpl.TOP, 0, 74);
    checkPrettyStringDetail(map, ARG0, 1, 4);
    checkPrettyStringDetail(map, new ArgPositionImpl(1, 0), 8, 23);
    checkPrettyStringDetail(map, new ArgPositionImpl(1, 1), 24, 32);
    final ArgPositionImpl curPos1 = new ArgPositionImpl(1, 2);
    curPos1.extend(0);
    checkPrettyStringDetail(map, curPos1, 38, 48);
    final ArgPositionImpl curPos2 = new ArgPositionImpl(1, 2);
    curPos2.extend(1);
    checkPrettyStringDetail(map, curPos2, 49, 52);
    checkPrettyStringDetail(map, new ArgPositionImpl(1, 2), 37, 53);
    checkPrettyStringDetail(map, new ArgPositionImpl(1), 7, 54);
    checkPrettyStringDetail(map, new ArgPositionImpl(2, 0), 58, 68);
    checkPrettyStringDetail(map, new ArgPositionImpl(2, 1), 69, 72);
    checkPrettyStringDetail(map, ARG2, 57, 73);
  }
  
  @Test
  public void testToPrettyEscapedCyclifiedString() throws Exception {
    System.out.println("\n*** testToPrettyEscapedCyclifiedString ***");
    final CycArrayList<String> testList = new CycArrayList<>();
    final StringBuffer stringBuffer = new StringBuffer();
    stringBuffer.append('"');
    stringBuffer.append("abc");
    testList.add(stringBuffer.toString());
    final String testEscapedCyclifiedString = testList.toPrettyEscapedCyclifiedString(
            "");
    assertEquals("(\"\\\"abc\")", testEscapedCyclifiedString);
  }
  
  @Test
  public void testToHTMLPrettyString() throws CycConnectionException {  
    final CycArrayList cycList = (CycArrayList) getCyc().getObjectTool().makeCycList(
            "(QUOTE (" + AND_STR + "(" + ISA_STR + " ?UNIT " + MODERN_MILITARY_ORGANIZATION_STR +
                    ") (" + OBJECT_FOUND_IN_LOCATION_STR + " ?UNIT " + ILLINOIS_STATE_STR + 
                    ") (" + BEHAVIOR_CAPABLE_STR + " ?UNIT (" + REACTION_TO_SITUATION_TYPE_FN_STR + " " + CHEMICAL_ATTACK_STR
                    + ") " + PERFORMED_BY_STR + ")))");
    assertEquals(
            "<html><body>(QUOTE<br>&nbsp&nbsp(and<br>&nbsp&nbsp&nbsp&nbsp(isa ?UNIT ModernMilitaryOrganization)<br>&nbsp&nbsp&nbsp&nbsp(objectFoundInLocation ?UNIT Illinois-State)<br>&nbsp&nbsp&nbsp&nbsp(behaviorCapable ?UNIT<br>&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp(ReactionToSituationTypeFn ChemicalAttack) performedBy)))</body></html>",
            cycList.toHTMLPrettyString(""));
  }
  
  @Test
  public void testUnmodifiableCycList() {
    doTestEmptyCycListAdd();
    final CycArrayList<Integer> frozenList = new CycArrayList.UnmodifiableCycList<>(CycArrayList.makeCycList(
            1, 3, 2));
    doTestUnmodifiableCycListAdd(frozenList);
    doTestUnmodifiableCycListSort(frozenList);
  }
  
  // Private helper methods

  private void doTestCycListAdd() {
    // add
    final CycArrayList<Long> longList = new CycArrayList<>();
    long n = 16;
    longList.add(n);
    assertEquals(new CycArrayList<>(n), longList);

    final CycArrayList<Boolean> booleanList = new CycArrayList<>();
    booleanList.add(false);
    assertEquals(new CycArrayList<>(false), booleanList);

    final CycArrayList<Float> floatList = new CycArrayList<>();
    float f = 16.0f;
    floatList.add(f);
    assertEquals(new CycArrayList<>(f), floatList);
  }
  
  private void checkPrettyStringDetail(Map<ArgPositionImpl, Span> map,
          ArgPositionImpl curPos,
          int expectedBegin, int expectedEnd) {
    Span span = map.get(curPos);
    assertNotNull(span);
    assertEquals(expectedBegin, span.getStart());
    assertEquals(expectedEnd, span.getEnd());
  }
  
  private void doTestEmptyCycListAdd() {
    UnsupportedOperationException x = null;
    try {
      CycArrayList.EMPTY_CYC_LIST.add(4);
    } catch (UnsupportedOperationException e) {
      x = e;
    }
    assertNotNull(x);
  }

  private void doTestUnmodifiableCycListAdd(final CycList frozenList) {
    UnsupportedOperationException x = null;
    try {
      frozenList.add(4);
    } catch (UnsupportedOperationException e) {
      x = e;
    }
    assertNotNull(x);
  }

  private void doTestUnmodifiableCycListSort(final CycList frozenList) {
    UnsupportedOperationException x = null;
    try {
      Collections.sort(frozenList);
    } catch (UnsupportedOperationException e) {
      x = e;
    }
    assertNotNull(x);
  }
  
}
