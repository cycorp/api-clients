package com.cyc.query.client;

/*
 * #%L
 * File: QueryReaderTest.java
 * Project: Query Client
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
import com.cyc.baseclient.cycobject.DefaultCycObjectImpl;
import com.cyc.kb.KbIndividual;
import com.cyc.kb.Sentence;
import com.cyc.kb.Variable;
import com.cyc.kb.client.BinaryPredicateImpl;
import com.cyc.kb.exception.KbException;
import com.cyc.query.Query;
import com.cyc.query.QueryAnswer;
import com.cyc.query.exception.QueryConstructionException;
import com.cyc.session.exception.SessionCommunicationException;
import com.cyc.session.exception.UnsupportedCycOperationException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.xml.bind.JAXBException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.cyc.Cyc.Constants.INFERENCE_PSC;
import static com.cyc.query.client.TestUtils.assumeCycSessionRequirements;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author baxter
 */
public class QueryReaderTest {

  public QueryReaderTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  @Test
  public void testLoadIstQuery() 
          throws QueryConstructionException, KbException,
          SessionCommunicationException, UnsupportedCycOperationException {
    assumeCycSessionRequirements(QueryImpl.QUERY_LOADER_REQUIREMENTS);
    System.out.println("testLoadIstQuery");
    final Query q1 = Query.get(
            Sentence.get(BinaryPredicateImpl.get("ist"), INFERENCE_PSC, TestUtils.xIsaBird()),
            INFERENCE_PSC);
    try {
      q1.saveAs("QueryForTestLoadIstQuery");
      final KbIndividual queryObj = KbIndividual.get("QueryForTestLoadIstQuery");
      final Query q = Query.get(queryObj); // TODO: this test is throwing QueryConstructionExceptions
      System.out.println(q.getQuerySentence());
      q.setMaxAnswerCount(1);
      assertTrue("Failed to get any answers.", q.getAnswerCount() > 0);
    } finally {
      q1.getId().delete();
    }
  }

  @Test
  public void testLoadSortedQuery() 
          throws QueryConstructionException, KbException,
          SessionCommunicationException, UnsupportedCycOperationException {
    assumeCycSessionRequirements(QueryImpl.QUERY_LOADER_REQUIREMENTS);
    System.out.println("testLoadSortedQuery");
    final Query q1 = Sentence
            .get(BinaryPredicateImpl.get("ist"), INFERENCE_PSC, TestUtils.xIsaBird())
            .toQuery(INFERENCE_PSC);
    try {
      q1.saveAs("QueryForTestLoadSortedQuery");
      final KbIndividual queryObj = KbIndividual.get("#$DemonstrateResultOrderingWithLengths-KBQ");
      final Query q = Query.get(queryObj); // TODO: this test is throwing QueryConstructionExceptions
      System.out.println(q.getQuerySentence());
      System.out.println("Sorted order: " + q.getInferenceParameters().get(":RESULT-SORT-ORDER"));
      System.out.println("Answers: " + q.getAnswers());
      for (QueryAnswer a : q.getAnswers()) {
        System.out.println("- " + a.getBinding(Variable.get("?ELT")));
      }
      assertTrue("Failed to get any answers.", q.getAnswerCount() > 0);
    } finally {
      q1.getId().delete();
    }
  }
  
  @Test
  public void testQueryFromSimpleXML() 
          throws JAXBException, KbException, QueryConstructionException {
    System.out.println("queryFromSimpleXML");
    final String expectedSentenceStr = "(and (?X Dog))";
    final Sentence expectedSentence = Sentence.get(expectedSentenceStr);
    final InputStream stream = new ByteArrayInputStream(SIMPLE_QUERY_XML.getBytes());
    final QueryReader instance = new ValidatingQueryReader();
    instance.hasMungedLists = true;
    final Query query = instance.queryFromXML(stream);
    final Object sortOrder = query.getInferenceParameters().get(":RESULT-SORT-ORDER");
    System.out.println("query: " + query.getQuerySentence());
    System.out.println("sort order: " + DefaultCycObjectImpl.cyclify(sortOrder));
    //final QueryTestConstants testConstants = QueryTestConstants.getInstance();
    assertEquals("Wrong context.", INFERENCE_PSC, query.getContext());
    assertEquals("Wrong formula.", expectedSentence, query.getQuerySentence());
    // TODO: Adds max time property to XML. - nwinant, 2017-06-21
    assertEquals("Wrong max time.", null, (Object) query.getMaxTime());
  }
  
  private static final String SIMPLE_QUERY_XML
          = "<cyclQuery xmlns=\"http://www.opencyc.org/xml/cyclQuery/\"><queryID>\n"
          + "  <constant xmlns=\"http://www.opencyc.org/xml/cycML/\">\n"
          + "   <guid>4becc241-0f05-11e7-9adc-90b11c81ea59</guid>\n"
          + "   <name>DemonstrateResultOrderingWithLengths-KBQ</name>\n"
          + "  </constant>\n"
          + " </queryID><queryFormula>\n"
          + "  <sentence xmlns=\"http://www.opencyc.org/xml/cycML/\">\n"
          + " <and>\n"
          + "  <constant>\n"
          + "   <guid>bd5880f9-9c29-11b1-9dad-c379636f7270</guid>\n"
          + "   <name>and</name>\n"
          + "  </constant>\n"
          + "  <sentence>\n"
          + "   <predicate>\n"
          + "    <variable>?X</variable>\n"
          + "   </predicate>\n"
          + "   <constant>\n"
          + "    <guid>bd58daa0-9c29-11b1-9dad-c379636f7270</guid>\n"
          + "    <name>Dog</name>\n"
          + "   </constant>\n"
          + "  </sentence>\n"
          + " </and>\n"
          + "</sentence>"
          + " </queryFormula><queryMt>\n"
          + "  <constant xmlns=\"http://www.opencyc.org/xml/cycML/\">\n"
          + "   <guid>bd58915a-9c29-11b1-9dad-c379636f7270</guid>\n"
          + "   <name>InferencePSC</name>\n"
          + "  </constant>\n"
          + " </queryMt>"
          + "<queryInferenceProperties><queryInferenceProperty><propertySymbol>RESULT-SORT-ORDER\n"
          + "   </propertySymbol><propertyValue>\n"
          + "         <sentence xmlns=\"http://www.opencyc.org/xml/cycML/\">\n"
          + " <and>\n"
          + "<constant>\n"
          + "           <guid>704e2e28-0aba-4a8d-8434-9b07d71191e6</guid>\n"
          + "           <name>JustATemporaryPredicate</name>\n"
          + "          </constant>"
          + "  <sentence>\n"
          + "   <predicate>\n"
          + "    <variable>?X</variable>\n"
          + "   </predicate>\n"
          + "   <constant>\n"
          + "    <guid>bd58daa0-9c29-11b1-9dad-c379636f7270</guid>\n"
          + "    <name>Dog</name>\n"
          + "   </constant>\n"
          + "  </sentence>\n"
          + " </and>\n"
          + "</sentence>"
          + "</propertyValue>\n"
          + "  </queryInferenceProperty>\n"
          + " </queryInferenceProperties></cyclQuery>";
  
  @Test
  public void testQueryFromXMLHack() throws JAXBException, KbException, QueryConstructionException {
    System.out.println("queryFromXMLHack");
    final String expectedSentenceStr
            = "(#$elementOf ?ELT \n"
            + "  (#$TheSet \n"
            + "    (#$Foot-UnitOfMeasure 5200) \n"
            + "    (#$Inch 2) \n"
            + "    (#$LightYear 7) \n"
            + "    ((#$Centi #$Meter) 7)))";
    final Sentence expectedSentence = Sentence.get(expectedSentenceStr);
    final InputStream stream = new ByteArrayInputStream(HACKED_XML.getBytes());
    final QueryReader instance = new ValidatingQueryReader();
    final Query query = instance.queryFromXML(stream);
    final Object sortOrder = query.getInferenceParameters().get(":RESULT-SORT-ORDER");
    System.out.println("Sort order: " + sortOrder);
    assertEquals("Wrong context.", INFERENCE_PSC, query.getContext());
    // TODO: The actual Sentences are not found to be equal. Why? - nwinant, 2017-06-21
    assertEquals("Wrong formula.", expectedSentence.toString(), query.getQuerySentence().toString());
    // TODO: Adds max time property to XML. - nwinant, 2017-06-21
    assertEquals("Wrong max time.", null, (Object) query.getMaxTime());
  }
  
  private static final String HACKED_XML
          = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\" ?>\n"
          + "<cyclQuery xmlns=\"http://www.opencyc.org/xml/cyclQuery/\"><queryID>\n"
          + "  <constant xmlns=\"http://www.opencyc.org/xml/cycML/\">\n"
          + "   <guid>4becc241-0f05-11e7-9adc-90b11c81ea59</guid>\n"
          + "   <name>DemonstrateResultOrderingWithLengths-KBQ</name>\n"
          + "  </constant>\n"
          + " </queryID><queryFormula>\n"
          + "  <sentence xmlns=\"http://www.opencyc.org/xml/cycML/\">\n"
          + "   <predicate>\n"
          + "    <constant>\n"
          + "     <guid>c0659a2b-9c29-11b1-9dad-c379636f7270</guid>\n"
          + "     <name>elementOf</name>\n"
          + "    </constant>\n"
          + "   </predicate>\n"
          + "   <variable>?ELT</variable>\n"
          + "   <function reified=\"false\">\n"
          + "    <constant>\n"
          + "     <guid>bd58e476-9c29-11b1-9dad-c379636f7270</guid>\n"
          + "     <name>TheSet</name>\n"
          + "    </constant>\n"
          + "    <function reified=\"false\">\n"
          + "     <constant>\n"
          + "      <guid>bd58a0e3-9c29-11b1-9dad-c379636f7270</guid>\n"
          + "      <name>Foot-UnitOfMeasure</name>\n"
          + "     </constant>\n"
          + "     <number>5200</number>\n"
          + "    </function>\n"
          + "    <function reified=\"false\">\n"
          + "     <constant>\n"
          + "      <guid>bd58a120-9c29-11b1-9dad-c379636f7270</guid>\n"
          + "      <name>Inch</name>\n"
          + "     </constant>\n"
          + "     <number>2</number>\n"
          + "    </function>\n"
          + "    <function reified=\"false\">\n"
          + "     <constant>\n"
          + "      <guid>bd58c62c-9c29-11b1-9dad-c379636f7270</guid>\n"
          + "      <name>LightYear</name>\n"
          + "     </constant>\n"
          + "     <number>7</number>\n"
          + "    </function>\n"
          + "    <function reified=\"false\">\n"
          + "     <function reified=\"false\">\n"
          + "      <constant>\n"
          + "       <guid>bd588ad1-9c29-11b1-9dad-c379636f7270</guid>\n"
          + "       <name>Centi</name>\n"
          + "      </constant>\n"
          + "      <constant>\n"
          + "       <guid>bd58d1a7-9c29-11b1-9dad-c379636f7270</guid>\n"
          + "       <name>Meter</name>\n"
          + "      </constant>\n"
          + "     </function>\n"
          + "     <number>7</number>\n"
          + "    </function>\n"
          + "   </function>\n"
          + "  </sentence>\n"
          + " </queryFormula><queryMt>\n"
          + "  <constant xmlns=\"http://www.opencyc.org/xml/cycML/\">\n"
          + "   <guid>bd58915a-9c29-11b1-9dad-c379636f7270</guid>\n"
          + "   <name>InferencePSC</name>\n"
          + "  </constant>\n"
          + " </queryMt><queryInferenceProperties><queryInferenceProperty><propertySymbol>RESULT-SORT-ORDER\n"
          + "   </propertySymbol><propertyValue>\n"
          + "        <constant xmlns=\"http://www.opencyc.org/xml/cycML/\">"
          + "<guid>bd5880b2-9c29-11b1-9dad-c379636f7270</guid>"
          + "<name>greaterThan</name>"
          + "</constant>"
          + "</propertyValue>\n"
          + "  </queryInferenceProperty>\n"
          + " </queryInferenceProperties>\n"
          + "</cyclQuery>";
  
  /**
   * Test of queryFromXML method, of class QueryReader.
   * 
   * @throws javax.xml.bind.JAXBException
   * @throws com.cyc.kb.exception.KbException
   * @throws com.cyc.query.exception.QueryConstructionException
   */
  @Test
  public void testQueryFromXML() throws JAXBException, KbException, QueryConstructionException {
    System.out.println("queryFromXML");
    InputStream stream = new ByteArrayInputStream(XML.getBytes());
    QueryReader instance = new ValidatingQueryReader();
    Query query = instance.queryFromXML(stream);
    final QueryTestConstants testConstants = QueryTestConstants.getInstance();
    assertEquals("Wrong context.", testConstants.generalCycKECollector, query.getContext());
    assertEquals("Wrong formula.", testConstants.academyAwardWinners, query.getQuerySentence());
    assertEquals("Wrong max time.", 60, (Object) query.getMaxTime());
  }
  
  private static final String XML
          = "<?xml version=\"1.0\" encoding=\"US-ASCII\" standalone=\"no\"?>"
          + "<cyclQuery xmlns=\"http://www.opencyc.org/xml/cyclQuery/\"><queryID>"
          + "<function reified=\"true\" xmlns=\"http://www.opencyc.org/xml/cycML/\">"
          + " <constant>"
          + "  <guid>8a8a8d13-4760-11db-8fd2-0002b3a85161</guid>"
          + "  <name>QueryTemplateFromSentenceAndIDFn</name>"
          + " </constant>"
          + " <function reified=\"false\">"
          + "  <constant>"
          + "   <guid>80605b12-436e-11d6-8000-00a0c9da2002</guid>"
          + "   <name>Quote</name>"
          + "  </constant>"
          + "  <sentence>"
          + "   <predicate>"
          + "    <constant>"
          + "     <guid>c090f65d-9c29-11b1-9dad-c379636f7270</guid>"
          + "     <name>academyAwardWinner</name>"
          + "    </constant>"
          + "   </predicate>"
          + "   <variable>?X</variable>"
          + "   <variable>?Y</variable>"
          + "   <variable>?Z</variable>"
          + "  </sentence>"
          + " </function>"
          + " <string>e0d0803c-430e-11e2-9de9-00219b4436b2</string>"
          + "</function>"
          + "  </queryID><queryFormula>"
          + "   <sentence xmlns=\"http://www.opencyc.org/xml/cycML/\">"
          + "    <predicate>"
          + "     <constant>"
          + "      <guid>c090f65d-9c29-11b1-9dad-c379636f7270</guid>"
          + "      <name>academyAwardWinner</name>"
          + "     </constant>"
          + "    </predicate>"
          + "    <variable>?X</variable>"
          + "    <variable>?Y</variable>"
          + "    <variable>?Z</variable>"
          + "   </sentence>"
          + "  </queryFormula><queryMt>"
          + "   <function reified=\"true\" xmlns=\"http://www.opencyc.org/xml/cycML/\">"
          + "    <constant>"
          + "     <guid>d5d71b27-24c5-4b0d-bcb5-072449b3e77e</guid>"
          + "     <name>AssistedReaderSourceSpindleCollectorForTaskFn</name>"
          + "    </constant>"
          + "    <constant>"
          + "     <guid>18ea376c-b788-11db-8000-000ea663fab7</guid>"
          + "     <name>GeneralCycKETask-Allotment</name>"
          + "    </constant>"
          + "   </function>"
          + "  </queryMt><queryComment>"
          + "   <string xmlns=\"http://www.opencyc.org/xml/cycML/\">Z is X made of Y.</string>"
          + "  </queryComment><queryInferenceProperties><queryInferenceProperty><propertySymbol>MAX-TRANSFORMATION-DEPTH"
          + "    </propertySymbol><propertyValue>"
          + "     <number xmlns=\"http://www.opencyc.org/xml/cycML/\">0</number>"
          + "    </propertyValue>"
          + "   </queryInferenceProperty><queryInferenceProperty><propertySymbol>ALLOW-INDETERMINATE-RESULTS?"
          + "    </propertySymbol><propertyValue>"
          + "     <symbol xmlns=\"http://www.opencyc.org/xml/cycML/\">"
          + "      <package>COMMON-LISP</package>"
          + "      <name>T</name>"
          + "     </symbol>"
          + "    </propertyValue>"
          + "   </queryInferenceProperty><queryInferenceProperty><propertySymbol>NEW-TERMS-ALLOWED?"
          + "    </propertySymbol><propertyValue>"
          + "     <symbol xmlns=\"http://www.opencyc.org/xml/cycML/\">"
          + "      <package>COMMON-LISP</package>"
          + "      <name>T</name>"
          + "     </symbol>"
          + "    </propertyValue>"
          + "   </queryInferenceProperty><queryInferenceProperty><propertySymbol>MAX-TIME"
          + "    </propertySymbol><propertyValue>"
          + "     <number xmlns=\"http://www.opencyc.org/xml/cycML/\">60</number>"
          + "    </propertyValue>"
          + "   </queryInferenceProperty><queryInferenceProperty><propertySymbol>DISJUNCTION-FREE-EL-VARS-POLICY"
          + "    </propertySymbol><propertyValue>"
          + "     <symbol xmlns=\"http://www.opencyc.org/xml/cycML/\">"
          + "      <package>KEYWORD</package>"
          + "      <name>COMPUTE-INTERSECTION</name>"
          + "     </symbol>"
          + "    </propertyValue>"
          + "   </queryInferenceProperty><queryInferenceProperty><propertySymbol>PRODUCTIVITY-LIMIT"
          + "    </propertySymbol><propertyValue>"
          + "     <number xmlns=\"http://www.opencyc.org/xml/cycML/\">2000000</number>"
          + "    </propertyValue>"
          + "   </queryInferenceProperty><queryInferenceProperty><propertySymbol>COMPUTE-ANSWER-JUSTIFICATIONS?"
          + "    </propertySymbol><propertyValue>"
          + "     <symbol xmlns=\"http://www.opencyc.org/xml/cycML/\">"
          + "      <package>COMMON-LISP</package>"
          + "      <name>T</name>"
          + "     </symbol>"
          + "    </propertyValue>"
          + "   </queryInferenceProperty>"
          + "  </queryInferenceProperties>"
          + " </cyclQuery>";
  
}
