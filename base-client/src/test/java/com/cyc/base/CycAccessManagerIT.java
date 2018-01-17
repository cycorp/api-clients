package com.cyc.base;

/*
 * #%L
 * File: CycAccessManagerIT.java
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

import com.cyc.base.cycobject.CycAssertion;
import com.cyc.base.cycobject.CycList;
import com.cyc.base.cycobject.ElMt;
import com.cyc.base.cycobject.FormulaSentence;
import com.cyc.base.inference.InferenceResultSet;
import com.cyc.baseclient.CycClientManager;
import com.cyc.baseclient.cycobject.ElMtConstantImpl;
import com.cyc.baseclient.cycobject.FormulaSentenceImpl;
import com.cyc.baseclient.inference.params.DefaultInferenceParameters;
import com.cyc.query.parameters.InferenceParameters;
import com.cyc.session.CycAddress;
import com.cyc.session.CycSession;
import com.cyc.session.CycSession.ConnectionStatus;
import com.cyc.session.exception.SessionCommandException;
import com.cyc.session.exception.SessionCommunicationException;
import com.cyc.session.exception.SessionConfigurationException;
import com.cyc.session.exception.SessionException;
import com.cyc.session.exception.SessionInitializationException;
import com.cyc.session.SessionManager;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CycAccessManagerIT  {
  
  private static final String HR = "---------------------------------------------------------------------------";
  
  public static void doPrintAssertion(CycAssertion asrt) {
    try {
      final CycAccess cyc = CycAccessManager.getCurrentAccess();
      System.out.println(asrt.getFormula().toPrettyString("  "));
      final String wff = asrt.getELFormula(cyc).getNonWffAssertExplanation(cyc);
      System.out.println("wff? " + wff);
      System.out.println(HR);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }
  
  @Test
  public void testQueryForAssertions() throws Exception {
    final CycAccess cyc = CycAccessManager.getCurrentAccess();
    final String sentStr = "(queryAllowedRules SNCVA-FollowupQuery-OtherMetrics-ViperDB ?RULE)";
    final FormulaSentence sentence = FormulaSentenceImpl.makeFormulaSentence(cyc, sentStr);
    final ElMt mt = ElMtConstantImpl.makeElMtConstant(cyc.getLookupTool().getConstantByName("TestVocabularyMt"));
    final InferenceParameters params = new DefaultInferenceParameters(cyc, true);
    final InferenceResultSet rs = cyc.getInferenceTool().executeQuery(sentence, mt, params);
    System.out.println(HR);
    while (rs.next()) {
      rs.getColumnNames().forEach(col -> {
        try {
          final Object obj = rs.getObject(col);
          if (obj instanceof CycAssertion) {
            doPrintAssertion((CycAssertion) obj);
          } else {
            System.out.println("  " + col + " : " + rs.getObject(col));
          }
        } catch (Exception ex) {
          throw new RuntimeException(ex);
        }
      });
    }
    System.out.println("... Done!");
    assertTrue(false);
  }
  
  @Test
  public void testCycList_EL() throws Exception {
    final CycAccess cyc = CycAccessManager.getCurrentAccess();
    final CycList list = cyc.getObjectTool().makeCycList(CYCL_EL);
    System.out.println(HR);
    System.out.println(list.toPrettyString("  "));
    System.out.println(HR);
  }
    
  @Test
  public void testFormulaSentence_EL() throws Exception {
    final CycAccess cyc = CycAccessManager.getCurrentAccess();
    final FormulaSentence sent = FormulaSentenceImpl.makeFormulaSentence(cyc, CYCL_EL);
    final CycList list = sent.toCycList();
    System.out.println(HR);
    System.out.println(list.toPrettyString("  "));
    System.out.println(HR);
  }
  
  private static final String GNARLED_REMAINS = ""
          + "  ("
          + "    ("
          + "      (isa ?VAR0 SignalImplementationPredicate)"
          + "      (arity ?VAR0 ?VAR1)"
          + "      (?VAR0 ?VAR2 ?VAR3 ?VAR4)"
          + "      (evaluate ?VAR6"
          + "        (FormulaArgFn ?VAR1"
          + "          (?VAR0 ?VAR2 ?VAR3 ?VAR4))))"
          + "    ("
          + "      (signalValueAlongDimensionForPeriod ?VAR2 ?VAR3 ?VAR4 ?VAR0 ?VAR6)))";

  private static final String CYCL_EL = "" 
            + "(#$implies"
            + "  (#$and "
            + "    (#$isa ?SIGNAL #$SignalImplementationPredicate)"
            + "    (#$arity ?SIGNAL ?ARITY)"
            + "    (?SIGNAL ?AGENT ?METRIC-PRED ?PERIOD . ?ARGS)"
            + "    (#$evaluate ?VALUE "
            + "      (#$FormulaArgFn ?ARITY"
            + "        (?SIGNAL ?AGENT ?METRIC-PRED ?PERIOD . ?ARGS))))"
            + "  (#$signalValueAlongDimensionForPeriod ?AGENT ?METRIC-PRED ?PERIOD ?SIGNAL ?VALUE))";
  
  private static final String CYCL_HL = "" 
          + "#<AS:(#$implies"
          + "       (#$and"
          + "         (#$isa ?SIGNAL #$SignalImplementationPredicate)"
          + "         (#$arity ?SIGNAL ?ARITY)"
          + "         (?SIGNAL ?AGENT ?METRIC-PRED ?PERIOD . ?ARGS)"
          + "         (#$evaluate ?VALUE "
          + "           (#$FormulaArgFn ?ARITY"
          + "             (?SIGNAL ?AGENT ?METRIC-PRED ?PERIOD . ?ARGS))))"
          + "       (#$signalValueAlongDimensionForPeriod ?AGENT ?METRIC-PRED ?PERIOD ?SIGNAL ?VALUE)):#$InformationGMt>";
  
  @Test
  public void testSDFGSDFGSDF() throws Exception {
    final CycAccess cyc = CycAccessManager.getCurrentAccess();
    final String queryString = "(queryAllowedRules SNCVA-FollowupQuery-OtherMetrics-ViperDB ?RULE)";
    String resultStr = "((((?RULE . #<AS:(#$implies (#$and (#$isa ?SIGNAL #$SignalImplementationPredicate) (#$arity ?SIGNAL ?ARITY) (?SIGNAL ?AGENT ?METRIC-PRED ?PERIOD . ?ARGS) (#$evaluate ?VALUE (#$FormulaArgFn ?ARITY (?SIGNAL ?AGENT ?METRIC-PRED ?PERIOD . ?ARGS)))) (#$signalValueAlongDimensionForPeriod ?AGENT ?METRIC-PRED ?PERIOD ?SIGNAL ?VALUE)):#$InformationGMt>)) ((?RULE . #<AS:(#$implies (#$and (#$isa ?SIGNAL #$SignalImplementationPredicate) (#$predicateRecordsScalarIntervalForPeriod-Complete ?METRIC ?METRIC-PRED) (#$different ?ALT-CAUSE ?HYP-CAUSE) (#$financialTransitionSentenceEvokesRelativeSlotMovementType ?SENTENCE ?EFFECT) (?COMP-PRED ?N ?THRESHOLD) (#$sentenceExplanatoryHypothesisTypeAndRoleWithRelevance ?SENTENCE ?HYP-CAUSE ??HYP-ROLE #<(#$HighToVeryHighAmountFn #$Relevance)>) (#$causes-SitTypeRolePlayingSitTypeRolePlaying ?ALT-CAUSE ?ALT-ROLE ?EFFECT #$objectChangingValue) (?SIGNAL ?AGENT ?METRIC-PRED ?PERIOD . ?ARGS) (#$signalValueAlongDimensionForPeriod ?AGENT ?METRIC-PRED ?PERIOD ?SIGNAL ?N) (#$metricTypeFocalRoleInEventDetectionTypeCorrelatesToRelativeHighLowInSlots ?METRIC ?ALT-CAUSE ?ALT-ROLE ?HIGH-SLOT ?LOW-SLOT) (#$metricTypeInvestigativeThresholdWRTHighLowInSlots ?METRIC ?HIGH-SLOT ?LOW-SLOT ?COMP-PRED ?THRESHOLD) (#$equalSymbols ?SENTENCE (#$outlierWRTProportionOfSlotsForPeriodAndSignalType ?AGENT ?HIGH ?LOW ?PERIOD ?TRIPPED-SIGNAL))) (#$sentenceFollowupSuggestionForPeriod ?AGENT ?PERIOD ?SENTENCE (#$possiblyWorthInvestigating-OtherMetrics (#$outlierWRTProportionOfSlotsForPeriodAndSignalType ?AGENT ?HIGH ?LOW ?PERIOD ?TRIPPED-SIGNAL) (#$and (#$relationExistsInstance ?ALT-ROLE ?ALT-CAUSE ?AGENT) (?SIGNAL ?AGENT ?METRIC-PRED ?PERIOD . ?ARGS))))):#$FinancialEvaluationMt>))) :EXHAUST-TOTAL)";
    String str1 = 
            "(#$implies"
            + " (#$and (#$isa ?SIGNAL #$SignalImplementationPredicate) "
            + "(#$arity ?SIGNAL ?ARITY)"
            + " (?SIGNAL ?AGENT ?METRIC-PRED ?PERIOD) "
            + "(#$evaluate ?VALUE (#$FormulaArgFn ?ARITY (?SIGNAL ?AGENT ?METRIC-PRED ?PERIOD))))"
            + " (#$signalValueAlongDimensionForPeriod ?AGENT ?METRIC-PRED ?PERIOD ?SIGNAL ?VALUE)))"
            + " :INFERENCE-MODE :SHALLOW :ALLOW-INDETERMINATE-RESULTS? NIL :MAX-TRANSFORMATION-DEPTH 2)";
    String s = "'(:ALLOWED-RULES "
            + "("
            + "(#$implies"
            + " (#$and "
            + "(#$isa ?SIGNAL #$SignalImplementationPredicate) "
            + "(#$predicateRecordsScalarIntervalForPeriod-Complete ?METRIC ?METRIC-PRED) "
            + "(#$different ?ALT-CAUSE ?HYP-CAUSE) "
            + "(#$financialTransitionSentenceEvokesRelativeSlotMovementType ?SENTENCE ?EFFECT)"
            + " (?COMP-PRED ?N ?THRESHOLD) "
            + "(#$sentenceExplanatoryHypothesisTypeAndRoleWithRelevance ?SENTENCE ?HYP-CAUSE ?HYP-ROLE (#$HighToVeryHighAmountFn #$Relevance))"
            + " (#$causes-SitTypeRolePlayingSitTypeRolePlaying ?ALT-CAUSE ?ALT-ROLE ?EFFECT #$objectChangingValue)"
            + " (?SIGNAL ?AGENT ?METRIC-PRED ?PERIOD)"
            + " (#$signalValueAlongDimensionForPeriod ?AGENT ?METRIC-PRED ?PERIOD ?SIGNAL ?N) "
            + "(#$metricTypeFocalRoleInEventDetectionTypeCorrelatesToRelativeHighLowInSlots ?METRIC ?ALT-CAUSE ?ALT-ROLE ?HIGH-SLOT ?LOW-SLOT) (#$metricTypeInvestigativeThresholdWRTHighLowInSlots ?METRIC ?HIGH-SLOT ?LOW-SLOT ?COMP-PRED ?THRESHOLD) (#$equalSymbols ?SENTENCE (#$outlierWRTProportionOfSlotsForPeriodAndSignalType ?AGENT ?HIGH ?LOW ?PERIOD ?TRIPPED-SIGNAL))) (#$sentenceFollowupSuggestionForPeriod ?AGENT ?PERIOD ?SENTENCE (#$possiblyWorthInvestigating-OtherMetrics (#$outlierWRTProportionOfSlotsForPeriodAndSignalType ?AGENT ?HIGH ?LOW ?PERIOD ?TRIPPED-SIGNAL) (#$and (#$relationExistsInstance ?ALT-ROLE ?ALT-CAUSE ?AGENT) (?SIGNAL ?AGENT ?METRIC-PRED ?PERIOD))))) (#$implies (#$and (#$isa ?SIGNAL #$SignalImplementationPredicate) (#$arity ?SIGNAL ?ARITY) (?SIGNAL ?AGENT ?METRIC-PRED ?PERIOD) (#$evaluate ?VALUE (#$FormulaArgFn ?ARITY (?SIGNAL ?AGENT ?METRIC-PRED ?PERIOD)))) (#$signalValueAlongDimensionForPeriod ?AGENT ?METRIC-PRED ?PERIOD ?SIGNAL ?VALUE))) :INFERENCE-MODE :SHALLOW :ALLOW-INDETERMINATE-RESULTS? NIL :MAX-TRANSFORMATION-DEPTH 2)\n";
    final String initString = "" +
            "(((#$isa ?VAR0 #$SignalImplementationPredicate)"
            + " (#$arity ?VAR0 ?VAR1)"
            + " (?VAR0 ?VAR2 ?VAR3 ?VAR4)"
            + " (#$evaluate ?VAR6 (#$FormulaArgFn ?VAR1 (?VAR0 ?VAR2 ?VAR3 ?VAR4)))) "
            + " ((#$signalValueAlongDimensionForPeriod ?VAR2 ?VAR3 ?VAR4 ?VAR0 ?VAR6)))" ;
    final String inErr = ""
            +"(ASSERTION-EL-FORMULA "
            + "(find-assertion "
            + "(list "
            + " (list "
            + "  (list #$isa (GET-VARIABLE 0) #$SignalImplementationPredicate) "
            + "  (list #$arity (GET-VARIABLE 0) (GET-VARIABLE 1))"
            + "  (list (GET-VARIABLE 0) (GET-VARIABLE 2) (GET-VARIABLE 3) (GET-VARIABLE 4)) "
            + "  (list #$evaluate (GET-VARIABLE 6) (list #$FormulaArgFn (GET-VARIABLE 1) (list (GET-VARIABLE 0) (GET-VARIABLE 2) (GET-VARIABLE 3) (GET-VARIABLE 4))))) "
            + "(list (list #$signalValueAlongDimensionForPeriod (GET-VARIABLE 2) (GET-VARIABLE 3) (GET-VARIABLE 4) (GET-VARIABLE 0) (GET-VARIABLE 6)))) "
            + "#$InformationGMt))";
    final String str = ""
                               + "(implies \n"
                               + "    (and \n"
                               + "      (isa ?SIGNAL SignalImplementationPredicate) \n"
                               + "      (arity ?SIGNAL ?ARITY) \n"
                               + "      (?SIGNAL ?AGENT ?METRIC-PRED ?PERIOD . ?ARGS) \n"
                               + "      (evaluate ?VALUE \n"
                               + "        (FormulaArgFn ?ARITY \n"
                               + "          (?SIGNAL ?AGENT ?METRIC-PRED ?PERIOD . ?ARGS)))) \n"
                               + "    (signalValueAlongDimensionForPeriod ?AGENT ?METRIC-PRED ?PERIOD ?SIGNAL ?VALUE))";

    final String result = FormulaSentenceImpl.makeFormulaSentence(cyc, str).getNonWffAssertExplanation(cyc);
    System.out.println("result: [" + result + "]");
  }
  
  @Test
  public void testCycAccessManagerAccessor() {
    assertNotNull(CycAccessManager.getAccessManager());
    assertTrue(CycClientManager.class.isInstance(CycAccessManager.getAccessManager()));
  }
  
  @Test
  public void testGetSessionManager() throws SessionConfigurationException, SessionCommunicationException, SessionInitializationException {
    final SessionManager sessionMgr = CycAccessManager.getAccessManager().getSessionMgr();
    assertNotNull(sessionMgr);
  }
  /*
  @Test
  public void testGetSessionManagerConfig() throws SessionConfigurationException, SessionCommunicationException, SessionInitializationException {
    final SessionManager sessionMgr = CycAccessManager.getAccessManager().getSessionMgr();
    final CycSessionConfiguration config = sessionMgr.getSessionConfiguration();
    assertNotNull(config);
    final ConfigurationValidator validator = new ConfigurationValidator(config);
    validator.print();
    assertTrue(validator.isSufficient());
  }
  */
  @Test
  public void testGetAccess() throws SessionConfigurationException, SessionCommunicationException, SessionInitializationException, SessionCommandException {
    final CycAccess access = CycAccessManager.getCurrentAccess();
    assertNotNull(access);
    final String kbVersion = access.getServerInfo().getCycKbVersionString();
    System.out.println("KB version:  " + kbVersion);
    assertNotNull(kbVersion);
    assertFalse(kbVersion.trim().isEmpty());
    final String kbRevision = access.getServerInfo().getCycRevisionString();
    System.out.println("KB revision: " + kbRevision);
    assertNotNull(kbRevision);
    assertFalse(kbRevision.trim().isEmpty());
  }
  
  @Test
  public void testAccessGetCycSession() throws SessionConfigurationException, SessionCommunicationException, SessionInitializationException, SessionCommandException {
    final CycAccess access = CycAccessManager.getCurrentAccess();
    final CycSession session = access.getCycSession();
    assertNotNull(session);
    assertEquals(ConnectionStatus.CONNECTED, session.getConnectionStatus());
    assertEquals(access.getCycAddress(), session.getServerInfo().getCycAddress());
    
    final String kbVersion = session.getServerInfo().getCycKbVersionString();
    System.out.println("KB version:  " + kbVersion);
    assertNotNull(kbVersion);
    assertFalse(kbVersion.trim().isEmpty());
    assertEquals(access.getServerInfo().getCycKbVersionString(), kbVersion);
    
    final String kbRevision = session.getServerInfo().getCycRevisionString();
    System.out.println("KB revision: " + kbRevision);
    assertNotNull(kbRevision);
    assertFalse(kbRevision.trim().isEmpty());
    assertEquals(access.getServerInfo().getCycRevisionString(), kbRevision);
  }
  
  /**
   * This test assume two images, the secondary one running on localhost:3660.
   * Test test is disabled by default.
   * 
   * @throws SessionException 
   */
  //@Test
  public void testLegacyCycAccessManager() throws SessionException {
    final CycSession session1 = CycSession.getCurrent();
    final CycAccess cyc1 = CycAccessManager.getCurrentAccess();
    final CycAddress server1 = cyc1.getServerInfo().getCycAddress();
    assertEquals(session1, cyc1.getCycSession());
    assertEquals(server1, session1.getServerInfo().getCycAddress());
     
    final CycAddress server2 = CycAddress.get("localhost", 3660);
    assertNotEquals(server1, server2);
    
    final CycAccess cyc2 = CycAccessManager.get().getAccess(server2);
    final CycSession session2 = CycAccessManager.get().getSession(server2);
    assertEquals(session2, cyc2.getCycSession());
    assertEquals(server2, session2.getServerInfo().getCycAddress());
    assertNotEquals(session1, session2);
    assertNotEquals(cyc1, cyc2);
    
    assertEquals(session1, CycSession.getCurrent());
    assertEquals(cyc1, CycAccessManager.getCurrentAccess());
    assertNotEquals(session2, CycSession.getCurrent());
    assertNotEquals(cyc2, CycAccessManager.getCurrentAccess());
    
    final CycSession currSession = CycAccessManager.get().setCurrentSession(server2);
    assertNotEquals(session1, CycSession.getCurrent());
    assertNotEquals(session1, currSession);
    assertNotEquals(cyc1, CycAccessManager.getCurrentAccess());
    assertEquals(session2, CycSession.getCurrent());
    assertEquals(session2, currSession);
    assertEquals(cyc2, CycAccessManager.getCurrentAccess());
  }
  
  //@Test
  public void testCloseCycAccessOnFail() throws SessionConfigurationException, SessionInitializationException, InterruptedException{
    try {
      final CycAddress server = CycAddress.get("nonexisting-host", 3600);
      CycAccessManager.get().setCurrentSession(CycAddress.get("nonexisting-host", 3600));
      CycAccessManager.getCurrentAccess();
    } catch (SessionCommunicationException ex) {
      ex.printStackTrace(System.out);
    }
    for (int i=0; i<=70; i++) {
      System.out.print(".");
      if ((i + 1) % 10 == 0) {
        System.out.println("  " + (i + 1));
      }
      Thread.sleep(1000);
    }
    System.out.println("... Should have thrown a CycTimeOutException by now.");
  }
  
  //@Test
  public void testSetCurrentSession() throws SessionException {
    CycAddress server = CycAddress.get("localhost", 3600);
    System.out.println(server);
    CycAccessManager.get().setCurrentSession(server);
    CycSession session = CycSession.getCurrent();
    assertEquals(server, session.getConfiguration().getCycAddress());
  }
}
