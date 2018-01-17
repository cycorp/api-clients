package com.cyc.baseclient.cycobject;

/*
 * #%L
 * File: DefaultCycObjectImplTest.java
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

import com.cyc.base.CycAccess;
import com.cyc.base.CycAccessManager;
import com.cyc.base.cycobject.CycList;
import com.cyc.base.cycobject.CycObject;
import com.cyc.base.cycobject.ElMt;
import com.cyc.base.cycobject.FormulaSentence;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.base.inference.InferenceResultSet;
import com.cyc.baseclient.CommonConstants;
import com.cyc.baseclient.inference.params.DefaultInferenceParameters;
import com.cyc.baseclient.testing.TestUtils;
import com.cyc.query.parameters.InferenceParameters;
import com.cyc.session.exception.SessionException;
import com.google.common.base.Optional;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

import static com.cyc.baseclient.testing.TestUtils.getCyc;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

// FIXME: TestSentences - nwinant

/**
 * Provides a suite of JUnit test cases for the <tt>com.cyc.baseclient.cycobject</tt> package.<p>
 *
 * @version $Id: UnitTest.java 135652 2011-08-30 10:24:52Z baxter $
 * @author Stephen L. Reed
 */
public class DefaultCycObjectImplTest {
    
  private static final String UNICODE_STRING_FN_STR = CommonConstants.UNICODE_STRING_FN.cyclify();
  
  @Before
  public void setUp() throws CycConnectionException, SessionException {
    TestUtils.ensureTestEnvironmentInitialized();
  }

  @Test
  public void testCompactExternalIds() throws CycConnectionException {
    System.out.println("\n*** testCompactExternalIds ***");
    final Object obj = CommonConstants.BASE_KB;
    final String id = "Mx4rvViBEZwpEbGdrcN5Y29ycA";
    assertEquals(id, DefaultCycObjectImpl.toCompactExternalId(obj, getCyc()));
    assertEquals(obj, DefaultCycObjectImpl.fromCompactExternalId(id, getCyc()));
    System.out.println("*** testCompactExternalIds OK ***");
  }
  
  /**
   * Tests the character support in the DefaultCycObjectImpl class.
   */
  @Test
  public void testCharacter() {
    System.out.println("\n*** testCharacter ***");
    final Character[] testChars = {'a', 'A', '\t', ' '};
    for (final Character character : testChars) {
      final boolean testResult = DefaultCycObjectImpl.isCycLObject(character);
      assertTrue("char test " + character, testResult);
    }
    for (final Character character : testChars) {
      final String cyclified = DefaultCycObjectImpl.cyclify(character);
      final boolean testResult = cyclified.startsWith("#\\");
      assertTrue("char cyclify test " + character, testResult);
    }
    for (final Character character : testChars) {
      final String cyclified = DefaultCycObjectImpl.cyclifyWithEscapeChars(character,
              false);
      final boolean testResult = cyclified.startsWith("#\\\\");
      assertTrue("char escaped cyclify test " + character, testResult);
    }
    System.out.println("*** testCharacter OK ***");
  }

  /**
   * Tests the Unicode support in the DefaultCycObjectImpl class.
   */
  @Test
  public void testUnicodeString() {
    System.out.println("\n*** testUnicodeString ***");
    String result = DefaultCycObjectImpl.cyclifyWithEscapeChars("abc", false);
    //System.out.println("abc test |"+result+"|");
    assertTrue("abc test", "\"abc\"".equals(result));

    result = DefaultCycObjectImpl.cyclifyWithEscapeChars("a\\b", false);
    //System.out.println("a\\b test |"+result+"|");
    assertTrue("a\\\\b test", "\"a\\\\b\"".equals(result));

    result = DefaultCycObjectImpl.cyclifyWithEscapeChars("a\"b", false);
    //System.out.println("a\"b test |"+result+"|");
    assertTrue("a\"c test", "\"a\\\"b\"".equals(result));

    final StringBuilder sb = new StringBuilder();
    sb.append('a');
    sb.append((char) (0x140));
    result = DefaultCycObjectImpl.cyclifyWithEscapeChars(sb.toString(), false);
    //System.out.println("a&u140 test |"+result+"|");
    assertEquals("(" + UNICODE_STRING_FN_STR + " \"a&u140;\")", result);

    result = DefaultCycObjectImpl.cyclifyWithEscapeChars(sb.toString(), true);
    //System.out.println("a&u140 test |"+result+"|");
    assertEquals("(list " + UNICODE_STRING_FN_STR + " \"a&u140;\")", result);

    CycList list = new CycArrayList();
    list.add(sb.toString());
    result = list.stringApiValue();
    //System.out.println("a&u140 test |"+result+"|");
    assertEquals("(list (list " + UNICODE_STRING_FN_STR + " \"a&u140;\"))", result);

    CycList list2 = new CycArrayList();
    list2.add(list);
    result = list2.stringApiValue();
    //System.out.println("a&u140 test |"+result+"|");
    assertEquals("(list (list (list " + UNICODE_STRING_FN_STR + " \"a&u140;\")))", result);

    System.out.println("*** testUnicodeString OK ***");
  }
  
  @Test
  public void testcyclifyWithEscapeChars() {
    final String input = "(TheSetOf ?CO (and (companyIndustryGroup-SP ?CO FoodBeverageAndTobacco-IndustryGroupSP) (hasHeadquartersInCountry ?CO China-PeoplesRepublic)))";
    final String result = DefaultCycObjectImpl.cyclifyWithEscapeChars(input);
    System.out.println("Result: " + result);
    assertNotNull(result);
  }
  
  
  
  /*
java.util.concurrent.ExecutionException: com.cyc.base.exception.BaseClientRuntimeException: Cannot cyclify (escaped): '(TheSetOf ?CO (and (companyIndustryGroup-SP ?CO FoodBeverageAndTobacco-IndustryGroupSP) (hasHeadquartersInCountry ?CO China-PeoplesRepublic)))'.
	at java.util.concurrent.CompletableFuture.reportGet(CompletableFuture.java:357) ~[?:1.8.0_144]
	at java.util.concurrent.CompletableFuture.get(CompletableFuture.java:1895) ~[?:1.8.0_144]
	at com.cyc.query.client.templates.OeTemplateProcessor.lambda$processToeTemplate$2(OeTemplateProcessor.java:309) ~[classes/:?]
	at java.util.concurrent.CompletableFuture.uniRun(CompletableFuture.java:705) [?:1.8.0_144]
	at java.util.concurrent.CompletableFuture$UniRun.tryFire(CompletableFuture.java:687) [?:1.8.0_144]
	at java.util.concurrent.CompletableFuture.postComplete(CompletableFuture.java:474) [?:1.8.0_144]
	at java.util.concurrent.CompletableFuture$AsyncRun.run(CompletableFuture.java:1632) [?:1.8.0_144]
	at java.util.concurrent.CompletableFuture$AsyncRun.exec(CompletableFuture.java:1618) [?:1.8.0_144]
	at java.util.concurrent.ForkJoinTask.doExec(ForkJoinTask.java:289) [?:1.8.0_144]
	at java.util.concurrent.ForkJoinPool$WorkQueue.runTask(ForkJoinPool.java:1056) [?:1.8.0_144]
	at java.util.concurrent.ForkJoinPool.runWorker(ForkJoinPool.java:1692) [?:1.8.0_144]
	at java.util.concurrent.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:157) [?:1.8.0_144]
Caused by: com.cyc.base.exception.BaseClientRuntimeException: Cannot cyclify (escaped): '(TheSetOf ?CO (and (companyIndustryGroup-SP ?CO FoodBeverageAndTobacco-IndustryGroupSP) (hasHeadquartersInCountry ?CO China-PeoplesRepublic)))'.
	at com.cyc.baseclient.cycobject.DefaultCycObjectImpl.cyclifyWithEscapeChars(DefaultCycObjectImpl.java:108) ~[classes/:?]
	at com.cyc.baseclient.cycobject.DefaultCycObjectImpl.stringApiValue(DefaultCycObjectImpl.java:220) ~[classes/:?]
	at com.cyc.baseclient.cycobject.CycArrayList.appendElement(CycArrayList.java:1555) ~[classes/:?]
	at com.cyc.baseclient.cycobject.CycArrayList.appendDottedElement(CycArrayList.java:1560) ~[classes/:?]
	at com.cyc.baseclient.cycobject.CycArrayList.appendSubSlice(CycArrayList.java:1544) ~[classes/:?]
	at com.cyc.baseclient.cycobject.CycArrayList.stringApiValue(CycArrayList.java:243) ~[classes/:?]
	at com.cyc.baseclient.cycobject.CycArrayList.stringApiValue(CycArrayList.java:1525) ~[classes/:?]
	at com.cyc.baseclient.cycobject.DefaultCycObjectImpl.stringApiValue(DefaultCycObjectImpl.java:209) ~[classes/:?]
	at com.cyc.baseclient.cycobject.CycArrayList.appendElement(CycArrayList.java:1555) ~[classes/:?]
	at com.cyc.baseclient.cycobject.CycArrayList.appendSubSlice(CycArrayList.java:1540) ~[classes/:?]
	at com.cyc.baseclient.cycobject.CycArrayList.stringApiValue(CycArrayList.java:243) ~[classes/:?]
	at com.cyc.baseclient.cycobject.CycArrayList.stringApiValue(CycArrayList.java:1525) ~[classes/:?]
	at com.cyc.baseclient.cycobject.DefaultCycObjectImpl.stringApiValue(DefaultCycObjectImpl.java:209) ~[classes/:?]
	at com.cyc.baseclient.connection.SublApiHelper.getAPIString(SublApiHelper.java:55) ~[classes/:?]
	at com.cyc.baseclient.connection.SublApiHelper.makeSublStmt(SublApiHelper.java:184) ~[classes/:?]
	at com.cyc.baseclient.kbtool.TemplateOeToolImpl.processTemplate(TemplateOeToolImpl.java:279) ~[classes/:?]
	at com.cyc.query.client.templates.OeTemplateProcessor$Functions.lambda$new$7(OeTemplateProcessor.java:188) ~[classes/:?]
	at java.util.concurrent.CompletableFuture$AsyncSupply.run(CompletableFuture.java:1590) ~[?:1.8.0_144]
	at java.util.concurrent.CompletableFuture$AsyncSupply.exec(CompletableFuture.java:1582) ~[?:1.8.0_144]
	... 4 more
2017-08-29T18:38:09,987 | ERROR AsynchronousSingle
*/  
  
  private static final String HR = "---------------------------------------------------------------------------";
  
  @Test
  public void testZZZZ() throws Exception {
    final String str = "(new-cyc-query\n" +
" '(#$evaluate ?ANS \n" +
"    (#$SetExtentFn \n" +
"      (#$TheSetOf ?SENT \n" +
"        (#$thereExistVars (?CO ?SLOT-HIGH ?SLOT-LOW ?PERIOD ?SIGNAL-TYPE ?EVENT-TYPE ?ROLE) \n" +
"          (#$and \n" +
"            (#$equalSymbols ?SENT \n" +
"              (#$sentenceExplanatoryHypothesisTypeAndRoleWithRelevance \n" +
"                (#$outlierWRTProportionOfSlotsForPeriodAndSignalType ?CO ?SLOT-HIGH ?SLOT-LOW ?PERIOD ?SIGNAL-TYPE) ?EVENT-TYPE ?ROLE \n" +
"                (#$HighToVeryHighAmountFn #$Relevance))) \n" +
"            (#$sentenceExplanatoryHypothesisTypeAndRoleWithRelevance \n" +
"              (#$outlierWRTProportionOfSlotsForPeriodAndSignalType ?CO ?SLOT-HIGH ?SLOT-LOW ?PERIOD ?SIGNAL-TYPE) ?EVENT-TYPE ?ROLE \n" +
"              (#$HighToVeryHighAmountFn #$Relevance)))))))\n" +
" '(#$MtSpace #$VACobraTestingAndDevelopmentDB-ForQueryingFollowups-QueryMt \n" +
"    (#$MtTimeDimFn #$Now))\n" +
"  (list\n" +
"    :INFERENCE-MODE :MINIMAL\n" +
"    :ALLOW-INDETERMINATE-RESULTS? NIL\n" +
"    :CONTINUABLE? T\n" +
"    ))";
    final CycAccess cyc = CycAccessManager.getCurrentAccess();
    final CycObject result = cyc.converse().converseCycObject(str);
    System.out.println("RESULT: " + result);
    System.out.println("        " + result.getClass());
    final CycList list = (CycList) result;
    for (Object o : list) {
      System.out.println(HR);
      System.out.println(o);
    }
    System.out.println(HR);
    System.out.println(list.size());
  }
  
  @Test
  public void testQuery() throws Exception {
    final String sentStr = "(#$evaluate ?ANS \n" +
"    (#$SetExtentFn \n" +
"      (#$TheSetOf ?SENT \n" +
"        (#$thereExistVars (?CO ?SLOT-HIGH ?SLOT-LOW ?PERIOD ?SIGNAL-TYPE ?EVENT-TYPE ?ROLE) \n" +
"          (#$and \n" +
"            (#$equalSymbols ?SENT \n" +
"              (#$sentenceExplanatoryHypothesisTypeAndRoleWithRelevance \n" +
"                (#$outlierWRTProportionOfSlotsForPeriodAndSignalType ?CO ?SLOT-HIGH ?SLOT-LOW ?PERIOD ?SIGNAL-TYPE) ?EVENT-TYPE ?ROLE \n" +
"                (#$HighToVeryHighAmountFn #$Relevance))) \n" +
"            (#$sentenceExplanatoryHypothesisTypeAndRoleWithRelevance \n" +
"              (#$outlierWRTProportionOfSlotsForPeriodAndSignalType ?CO ?SLOT-HIGH ?SLOT-LOW ?PERIOD ?SIGNAL-TYPE) ?EVENT-TYPE ?ROLE \n" +
"              (#$HighToVeryHighAmountFn #$Relevance)))))))";
    final String mtStr = "#$VACobraTestingAndDevelopmentDB-ForQueryingFollowups-QueryMt";
    
    final CycAccess cyc = CycAccessManager.getCurrentAccess();
    //final String sentStr = "(queryAllowedRules SNCVA-FollowupQuery-OtherMetrics-ViperDB ?RULE)";
    final FormulaSentence sentence = FormulaSentenceImpl.makeFormulaSentence(cyc, sentStr);
    final ElMt mt = ElMtConstantImpl.makeElMtConstant(cyc.getLookupTool().getConstantByName(mtStr));
    final InferenceParameters params = new DefaultInferenceParameters(cyc, true);
    final InferenceResultSet rs = cyc.getInferenceTool().executeQuery(sentence, mt, params);
    
    
    System.out.println(HR);
    while (rs.next()) {
      rs.getColumnNames().forEach(col -> {
        try {
          final Object obj = rs.getObject(col);
          //if (obj instanceof CycAssertion) {
          //  doPrintAssertion((CycAssertion) obj);
          //} else {
            System.out.println(col + " : " + Optional.of(obj).transform(o -> o.getClass()));
            System.out.println(obj);
            System.out.println();
            
            final long size = obj.toString().length();
            System.out.println("size: " + size + " chars");
            System.out.println("size: " + obj.toString().getBytes().length + " bytes");
            System.out.println();
            
            final String apiValue = DefaultCycObjectImpl.stringApiValue(obj);
            System.out.println("API: " + apiValue);
            System.out.println();
          //}
        } catch (Exception ex) {
          throw new RuntimeException(ex);
        }
      });
    }
    System.out.println("... Done!");
    assertTrue(true);
  }
  
  // "(((?ANS TheSet (...
  
  public static final List<String> STRINGS = Arrays.asList(
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"KRNL.F\") inventoryGrowth salesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) (OverstatingFigureTypeFn ValueOfInventory) performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"2387\") operatingIncome cashFromOperations (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) UsingOperatingAccrualsAggressivelyInAccounting orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"CVGW\") payablesGrowth salesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) (DoingByFn (DecreaseOnSlotFn (RestrictionOnSlotFn (PlayingRoleInTypeFn payer Paying) rateOfEvent)) DelayingPayment) performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"BTE\") cashFromOperations operatingIncome (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) UsingOperatingAccrualsConservativelyInAccounting orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"ERN\") cashFromOperations operatingIncome (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) UsingOperatingAccrualsConservativelyInAccounting orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType CallonPetroleumCompany salesGrowth receivablesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) EstimatingAccountsReceivableConservatively orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType AltriaGroupInc salesGrowth payablesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) (UnderstatingFigureTypeFn TradePayables) performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType HalconResourcesCorp salesGrowth inventoryGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) EstimatingValueOfInventoryConservatively orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"SUEZ.F\") receivablesGrowth salesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) ImproperRevenueRecognition performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"GPRK\") preTaxIncome incomeTaxExpense (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) DecreasingEffectiveTaxRateToUnsustainablyLowLevel objectChangingValue (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType HessCorporation preTaxIncome incomeTaxExpense (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) DecreasingEffectiveTaxRateToUnsustainablyLowLevel objectChangingValue (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"2458\") salesGrowth payablesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) (UnderstatingFigureTypeFn TradePayables) performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType CaliforniaResources-Corporation agentDepreciationExpenditures agentCapitalExpenditures (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) PracticingAConservativeDepreciationPolicy orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType MarathonOil preTaxIncome incomeTaxExpense (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) DecreasingEffectiveTaxRateToUnsustainablyLowLevel objectChangingValue (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"HINK.F\") payablesGrowth salesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) (DoingByFn (DecreaseOnSlotFn (RestrictionOnSlotFn (PlayingRoleInTypeFn payer Paying) rateOfEvent)) DelayingPayment) performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"THS\") salesGrowth inventoryGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) EstimatingValueOfInventoryConservatively orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType ClaytonWilliamsEnergyInc incomeTaxExpense preTaxIncome (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) IncreasingEffectiveTaxRateToUnsustainablyHighLevel objectChangingValue (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType MidstatesPetroleumCompanyInc normalWorkingCapital workingCapital (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) UsingWorkingCapitalAccrualsConservativelyInAccounting orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType AltriaGroupInc workingCapital normalWorkingCapital (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) UsingWorkingCapitalAccrualsAggressivelyInAccounting orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"PHX\") payablesGrowth salesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) (DoingByFn (DecreaseOnSlotFn (RestrictionOnSlotFn (PlayingRoleInTypeFn payer Paying) rateOfEvent)) DelayingPayment) performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"THS\") preTaxIncome incomeTaxExpense (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) DecreasingEffectiveTaxRateToUnsustainablyLowLevel objectChangingValue (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType TysonFoods-Corporation receivablesGrowth salesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) ImproperRevenueRecognition performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"2505\") salesGrowth payablesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) (UnderstatingFigureTypeFn TradePayables) performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"PWE\") cashFromOperations operatingIncome (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) UsingOperatingAccrualsConservativelyInAccounting orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType DevonEnergyCorp operatingIncome cashFromOperations (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) UsingOperatingAccrualsAggressivelyInAccounting orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType DiamondbackEnergy payablesGrowth salesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) (DoingByFn (DecreaseOnSlotFn (RestrictionOnSlotFn (PlayingRoleInTypeFn payer Paying) rateOfEvent)) DelayingPayment) performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType ParsleyEnergy salesGrowth receivablesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) EstimatingAccountsReceivableConservatively orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"300106\") cashFromOperations operatingIncome (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) UsingOperatingAccrualsConservativelyInAccounting orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"RICE\") salesGrowth payablesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) (UnderstatingFigureTypeFn TradePayables) performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"WPX\") receivablesGrowth salesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) ImproperRevenueRecognition performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"GRGS.F\") salesGrowth receivablesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) EstimatingAccountsReceivableConservatively orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType FlowersFoods-Corporation salesGrowth inventoryGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) EstimatingValueOfInventoryConservatively orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"BF.A\") operatingIncome cashFromOperations (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) UsingOperatingAccrualsAggressivelyInAccounting orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"SISA.F\") salesGrowth receivablesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) EstimatingAccountsReceivableConservatively orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"GTE\") operatingIncome cashFromOperations (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) UsingOperatingAccrualsAggressivelyInAccounting orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"MNHV.F\") operatingIncome cashFromOperations (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) UsingOperatingAccrualsAggressivelyInAccounting orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType ClaytonWilliamsEnergyInc normalWorkingCapital workingCapital (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) UsingWorkingCapitalAccrualsConservativelyInAccounting orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"2505\") salesGrowth inventoryGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) EstimatingValueOfInventoryConservatively orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"600701\") payablesGrowth salesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) (DoingByFn (DecreaseOnSlotFn (RestrictionOnSlotFn (PlayingRoleInTypeFn payer Paying) rateOfEvent)) DelayingPayment) performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"ARZT.F\") preTaxIncome incomeTaxExpense (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) DecreasingEffectiveTaxRateToUnsustainablyLowLevel objectChangingValue (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"KRNL.F\") payablesGrowth salesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) (DoingByFn (DecreaseOnSlotFn (RestrictionOnSlotFn (PlayingRoleInTypeFn payer Paying) rateOfEvent)) DelayingPayment) performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType JohnBSanfilippoSonInc normalWorkingCapital workingCapital (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) UsingWorkingCapitalAccrualsConservativelyInAccounting orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType MidstatesPetroleumCompanyInc payablesGrowth salesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) (DoingByFn (DecreaseOnSlotFn (RestrictionOnSlotFn (PlayingRoleInTypeFn payer Paying) rateOfEvent)) DelayingPayment) performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn VACobraCompanyMapping 207) salesGrowth payablesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) (UnderstatingFigureTypeFn TradePayables) performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType DeanFoods normalWorkingCapital workingCapital (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) UsingWorkingCapitalAccrualsConservativelyInAccounting orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"ACOP.F\") inventoryGrowth salesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) (OverstatingFigureTypeFn ValueOfInventory) performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"IMBB.F\") cashFromOperations operatingIncome (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) UsingOperatingAccrualsConservativelyInAccounting orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"THS\") salesGrowth receivablesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) EstimatingAccountsReceivableConservatively orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"600701\") operatingIncome cashFromOperations (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) UsingOperatingAccrualsAggressivelyInAccounting orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"300106\") salesGrowth payablesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) (UnderstatingFigureTypeFn TradePayables) performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType MolsonCoorsBrewingCompany salesGrowth payablesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) (UnderstatingFigureTypeFn TradePayables) performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"600127\") preTaxIncome incomeTaxExpense (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) DecreasingEffectiveTaxRateToUnsustainablyLowLevel objectChangingValue (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType RSPPermian salesGrowth receivablesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) EstimatingAccountsReceivableConservatively orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType AltriaGroupInc operatingIncome cashFromOperations (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) UsingOperatingAccrualsAggressivelyInAccounting orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"220\") incomeTaxExpense preTaxIncome (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) IncreasingEffectiveTaxRateToUnsustainablyHighLevel objectChangingValue (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType ChesapeakeEnergy operatingIncome cashFromOperations (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) UsingOperatingAccrualsAggressivelyInAccounting orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"ERF\") preTaxIncome incomeTaxExpense (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) DecreasingEffectiveTaxRateToUnsustainablyLowLevel objectChangingValue (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"SD\") receivablesGrowth salesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) ImproperRevenueRecognition performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"2458\") salesGrowth inventoryGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) EstimatingValueOfInventoryConservatively orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType FlowersFoods-Corporation salesGrowth payablesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) (UnderstatingFigureTypeFn TradePayables) performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType Cal-MaineFoodsInc incomeTaxExpense preTaxIncome (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) IncreasingEffectiveTaxRateToUnsustainablyHighLevel objectChangingValue (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType AnteroResources salesGrowth payablesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) (UnderstatingFigureTypeFn TradePayables) performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"600701\") inventoryGrowth salesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) (OverstatingFigureTypeFn ValueOfInventory) performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType Cal-MaineFoodsInc receivablesGrowth salesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) ImproperRevenueRecognition performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn VACobraCompanyMapping 207) inventoryGrowth salesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) (OverstatingFigureTypeFn ValueOfInventory) performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"PPC\") receivablesGrowth salesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) ImproperRevenueRecognition performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType Concho payablesGrowth salesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) (DoingByFn (DecreaseOnSlotFn (RestrictionOnSlotFn (PlayingRoleInTypeFn payer Paying) rateOfEvent)) DelayingPayment) performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType DeanFoods payablesGrowth salesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) (DoingByFn (DecreaseOnSlotFn (RestrictionOnSlotFn (PlayingRoleInTypeFn payer Paying) rateOfEvent)) DelayingPayment) performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"CHUD.F\") payablesGrowth salesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) (DoingByFn (DecreaseOnSlotFn (RestrictionOnSlotFn (PlayingRoleInTypeFn payer Paying) rateOfEvent)) DelayingPayment) performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType KraftHeinzCompany receivablesGrowth salesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) ImproperRevenueRecognition performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"HKHH.F\") payablesGrowth salesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) (DoingByFn (DecreaseOnSlotFn (RestrictionOnSlotFn (PlayingRoleInTypeFn payer Paying) rateOfEvent)) DelayingPayment) performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType JMSmuckerCompany normalWorkingCapital workingCapital (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) UsingWorkingCapitalAccrualsConservativelyInAccounting orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType FarmerBrothersCompany preTaxIncome incomeTaxExpense (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) DecreasingEffectiveTaxRateToUnsustainablyLowLevel objectChangingValue (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"ERN\") payablesGrowth salesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) (DoingByFn (DecreaseOnSlotFn (RestrictionOnSlotFn (PlayingRoleInTypeFn payer Paying) rateOfEvent)) DelayingPayment) performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType WhitingPetroleumCorporation receivablesGrowth salesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) ImproperRevenueRecognition performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType RSPPermian incomeTaxExpense preTaxIncome (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) IncreasingEffectiveTaxRateToUnsustainablyHighLevel objectChangingValue (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"PCRT.F\") operatingIncome cashFromOperations (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) UsingOperatingAccrualsAggressivelyInAccounting orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType GulfportEnergyCorp workingCapital normalWorkingCapital (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) UsingWorkingCapitalAccrualsAggressivelyInAccounting orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"ASBR.F\") payablesGrowth salesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) (DoingByFn (DecreaseOnSlotFn (RestrictionOnSlotFn (PlayingRoleInTypeFn payer Paying) rateOfEvent)) DelayingPayment) performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"ARZT.F\") receivablesGrowth salesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) ImproperRevenueRecognition performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"SD\") operatingIncome cashFromOperations (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) UsingOperatingAccrualsAggressivelyInAccounting orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"REN\") salesGrowth receivablesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) EstimatingAccountsReceivableConservatively orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType Cal-MaineFoodsInc inventoryGrowth salesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) (OverstatingFigureTypeFn ValueOfInventory) performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"GRGS.F\") salesGrowth inventoryGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) EstimatingValueOfInventoryConservatively orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"300106\") salesGrowth inventoryGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) EstimatingValueOfInventoryConservatively orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType LanceInc salesGrowth receivablesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) EstimatingAccountsReceivableConservatively orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType Concho agentCapitalExpenditures agentDepreciationExpenditures (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) UnderestimatingDepreciationExpenses orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"IDBH.F\") salesGrowth receivablesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) EstimatingAccountsReceivableConservatively orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"CGPZ.F\") receivablesGrowth salesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) ImproperRevenueRecognition performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"VNOM\") payablesGrowth salesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) (DoingByFn (DecreaseOnSlotFn (RestrictionOnSlotFn (PlayingRoleInTypeFn payer Paying) rateOfEvent)) DelayingPayment) performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"SWMA.F\") salesGrowth inventoryGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) EstimatingValueOfInventoryConservatively orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"PWE\") receivablesGrowth salesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) ImproperRevenueRecognition performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType MolsonCoorsBrewingCompany salesGrowth receivablesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) EstimatingAccountsReceivableConservatively orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"1262\") cashFromOperations operatingIncome (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) UsingOperatingAccrualsConservativelyInAccounting orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"GTE\") salesGrowth inventoryGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) EstimatingValueOfInventoryConservatively orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"GNCG.F\") preTaxIncome incomeTaxExpense (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) DecreasingEffectiveTaxRateToUnsustainablyLowLevel objectChangingValue (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"AKEJ.F\") operatingIncome cashFromOperations (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) UsingOperatingAccrualsAggressivelyInAccounting orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"ACOP.F\") salesGrowth payablesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) (UnderstatingFigureTypeFn TradePayables) performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"MTDR\") salesGrowth payablesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) (UnderstatingFigureTypeFn TradePayables) performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"ISRL\") incomeTaxExpense preTaxIncome (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) IncreasingEffectiveTaxRateToUnsustainablyHighLevel objectChangingValue (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType Ingredion incomeTaxExpense preTaxIncome (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) IncreasingEffectiveTaxRateToUnsustainablyHighLevel objectChangingValue (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType ConAgraFoodsInc cashFromOperations operatingIncome (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) UsingOperatingAccrualsConservativelyInAccounting orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"DMLP\") salesGrowth payablesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) (UnderstatingFigureTypeFn TradePayables) performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType FlowersFoods-Corporation agentDepreciationExpenditures agentCapitalExpenditures (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) PracticingAConservativeDepreciationPolicy orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"SWMA.F\") salesGrowth payablesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) (UnderstatingFigureTypeFn TradePayables) performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType ReynoldsAmericanInc operatingIncome cashFromOperations (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) UsingOperatingAccrualsAggressivelyInAccounting orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType ConAgraFoodsInc incomeTaxExpense preTaxIncome (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) IncreasingEffectiveTaxRateToUnsustainablyHighLevel objectChangingValue (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType AnadarkoPetroleumCorp workingCapital normalWorkingCapital (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) UsingWorkingCapitalAccrualsAggressivelyInAccounting orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType ParsleyEnergy workingCapital normalWorkingCapital (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) UsingWorkingCapitalAccrualsAggressivelyInAccounting orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType ApacheCorp normalWorkingCapital workingCapital (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) UsingWorkingCapitalAccrualsConservativelyInAccounting orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"BATM.F\") salesGrowth inventoryGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) EstimatingValueOfInventoryConservatively orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType ParsleyEnergy salesGrowth payablesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) (UnderstatingFigureTypeFn TradePayables) performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType CocaColaCo preTaxIncome incomeTaxExpense (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) DecreasingEffectiveTaxRateToUnsustainablyLowLevel objectChangingValue (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType EPEnergy normalWorkingCapital workingCapital (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) UsingWorkingCapitalAccrualsConservativelyInAccounting orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"ACOP.F\") salesGrowth receivablesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) EstimatingAccountsReceivableConservatively orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType FlowersFoods-Corporation normalWorkingCapital workingCapital (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) UsingWorkingCapitalAccrualsConservativelyInAccounting orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"600127\") inventoryGrowth salesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) (OverstatingFigureTypeFn ValueOfInventory) performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"GPDN.F\") salesGrowth inventoryGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) EstimatingValueOfInventoryConservatively orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType DeanFoods salesGrowth inventoryGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) EstimatingValueOfInventoryConservatively orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType JMSmuckerCompany salesGrowth inventoryGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) EstimatingValueOfInventoryConservatively orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"COJP.F\") incomeTaxExpense preTaxIncome (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) IncreasingEffectiveTaxRateToUnsustainablyHighLevel objectChangingValue (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType LanceInc incomeTaxExpense preTaxIncome (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) IncreasingEffectiveTaxRateToUnsustainablyHighLevel objectChangingValue (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"CCHB.F\") salesGrowth payablesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) (UnderstatingFigureTypeFn TradePayables) performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"LW\") cashFromOperations operatingIncome (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) UsingOperatingAccrualsConservativelyInAccounting orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType CobaltInternationalEnergyInc cashFromOperations operatingIncome (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) UsingOperatingAccrualsConservativelyInAccounting orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"ASAG.F\") cashFromOperations operatingIncome (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) UsingOperatingAccrualsConservativelyInAccounting orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType Cal-MaineFoodsInc workingCapital normalWorkingCapital (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) UsingWorkingCapitalAccrualsAggressivelyInAccounting orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"PCRT.F\") inventoryGrowth salesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) (OverstatingFigureTypeFn ValueOfInventory) performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType KraftHeinzCompany operatingIncome cashFromOperations (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) UsingOperatingAccrualsAggressivelyInAccounting orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType JohnBSanfilippoSonInc salesGrowth inventoryGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) EstimatingValueOfInventoryConservatively orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"KJWN.F\") incomeTaxExpense preTaxIncome (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) IncreasingEffectiveTaxRateToUnsustainablyHighLevel objectChangingValue (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType AdvantageOilAndGasLtd salesGrowth receivablesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) EstimatingAccountsReceivableConservatively orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType ApacheCorp agentDepreciationExpenditures agentCapitalExpenditures (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) PracticingAConservativeDepreciationPolicy orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"QEP\") receivablesGrowth salesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) ImproperRevenueRecognition performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType EncanaCorporation salesGrowth payablesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) (UnderstatingFigureTypeFn TradePayables) performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"TSLL.F\") salesGrowth inventoryGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) EstimatingValueOfInventoryConservatively orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType Cal-MaineFoodsInc cashFromOperations operatingIncome (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) UsingOperatingAccrualsConservativelyInAccounting orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType BillBarrettCorp payablesGrowth salesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) (DoingByFn (DecreaseOnSlotFn (RestrictionOnSlotFn (PlayingRoleInTypeFn payer Paying) rateOfEvent)) DelayingPayment) performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"1262\") payablesGrowth salesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) (DoingByFn (DecreaseOnSlotFn (RestrictionOnSlotFn (PlayingRoleInTypeFn payer Paying) rateOfEvent)) DelayingPayment) performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType CaliforniaResources-Corporation operatingIncome cashFromOperations (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) UsingOperatingAccrualsAggressivelyInAccounting orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"KNKZ.F\") cashFromOperations operatingIncome (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) UsingOperatingAccrualsConservativelyInAccounting orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"SWMA.F\") operatingIncome cashFromOperations (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) UsingOperatingAccrualsAggressivelyInAccounting orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"XOG\") cashFromOperations operatingIncome (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) UsingOperatingAccrualsConservativelyInAccounting orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType Energen inventoryGrowth salesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) (OverstatingFigureTypeFn ValueOfInventory) performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"YKLT.F\") incomeTaxExpense preTaxIncome (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) IncreasingEffectiveTaxRateToUnsustainablyHighLevel objectChangingValue (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"DMLP\") operatingIncome cashFromOperations (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) UsingOperatingAccrualsAggressivelyInAccounting orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"151\") operatingIncome cashFromOperations (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) UsingOperatingAccrualsAggressivelyInAccounting orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"151\") salesGrowth receivablesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) EstimatingAccountsReceivableConservatively orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"TSLL.F\") salesGrowth receivablesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) EstimatingAccountsReceivableConservatively orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"PCRT.F\") payablesGrowth salesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) (DoingByFn (DecreaseOnSlotFn (RestrictionOnSlotFn (PlayingRoleInTypeFn payer Paying) rateOfEvent)) DelayingPayment) performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"COJP.F\") salesGrowth payablesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) (UnderstatingFigureTypeFn TradePayables) performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType WhitingPetroleumCorporation agentDepreciationExpenditures agentCapitalExpenditures (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) PracticingAConservativeDepreciationPolicy orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"VTSY.F\") preTaxIncome incomeTaxExpense (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) DecreasingEffectiveTaxRateToUnsustainablyLowLevel objectChangingValue (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"ASBR.F\") receivablesGrowth salesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) ImproperRevenueRecognition performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"600300\") preTaxIncome incomeTaxExpense (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) DecreasingEffectiveTaxRateToUnsustainablyLowLevel objectChangingValue (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"BF.B\") operatingIncome cashFromOperations (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) UsingOperatingAccrualsAggressivelyInAccounting orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"GPDN.F\") operatingIncome cashFromOperations (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) UsingOperatingAccrualsAggressivelyInAccounting orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"REI\") incomeTaxExpense preTaxIncome (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) IncreasingEffectiveTaxRateToUnsustainablyHighLevel objectChangingValue (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"VET\") incomeTaxExpense preTaxIncome (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) IncreasingEffectiveTaxRateToUnsustainablyHighLevel objectChangingValue (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType GulfportEnergyCorp salesGrowth payablesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) (UnderstatingFigureTypeFn TradePayables) performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType MondelezInternational preTaxIncome incomeTaxExpense (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) DecreasingEffectiveTaxRateToUnsustainablyLowLevel objectChangingValue (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"BATM.F\") salesGrowth receivablesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) EstimatingAccountsReceivableConservatively orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"JAPA.F\") cashFromOperations operatingIncome (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) UsingOperatingAccrualsConservativelyInAccounting orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType CampbellSoup-Corporation preTaxIncome incomeTaxExpense (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) DecreasingEffectiveTaxRateToUnsustainablyLowLevel objectChangingValue (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType JohnBSanfilippoSonInc cashFromOperations operatingIncome (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) UsingOperatingAccrualsConservativelyInAccounting orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType EncanaCorporation salesGrowth receivablesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) EstimatingAccountsReceivableConservatively orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"EPM\") cashFromOperations operatingIncome (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) UsingOperatingAccrualsConservativelyInAccounting orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType KelloggCo preTaxIncome incomeTaxExpense (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) DecreasingEffectiveTaxRateToUnsustainablyLowLevel objectChangingValue (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType Ingredion workingCapital normalWorkingCapital (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) UsingWorkingCapitalAccrualsAggressivelyInAccounting orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType SMEnergy agentCapitalExpenditures agentDepreciationExpenditures (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) UnderestimatingDepreciationExpenses orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType Ingredion payablesGrowth salesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) (DoingByFn (DecreaseOnSlotFn (RestrictionOnSlotFn (PlayingRoleInTypeFn payer Paying) rateOfEvent)) DelayingPayment) performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"220\") receivablesGrowth salesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) ImproperRevenueRecognition performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"PHX\") receivablesGrowth salesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) ImproperRevenueRecognition performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType MolsonCoorsBrewingCompany inventoryGrowth salesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) (OverstatingFigureTypeFn ValueOfInventory) performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"GRGS.F\") salesGrowth payablesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) (UnderstatingFigureTypeFn TradePayables) performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"ASBR.F\") inventoryGrowth salesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) (OverstatingFigureTypeFn ValueOfInventory) performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType WhitingPetroleumCorporation incomeTaxExpense preTaxIncome (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) IncreasingEffectiveTaxRateToUnsustainablyHighLevel objectChangingValue (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"2505\") incomeTaxExpense preTaxIncome (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) IncreasingEffectiveTaxRateToUnsustainablyHighLevel objectChangingValue (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType ApacheCorp operatingIncome cashFromOperations (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) UsingOperatingAccrualsAggressivelyInAccounting orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType CNCanadianNaturalResourcesLtd preTaxIncome incomeTaxExpense (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) DecreasingEffectiveTaxRateToUnsustainablyLowLevel objectChangingValue (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"POST\") incomeTaxExpense preTaxIncome (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) IncreasingEffectiveTaxRateToUnsustainablyHighLevel objectChangingValue (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType CobaltInternationalEnergyInc agentCapitalExpenditures agentDepreciationExpenditures (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) UnderestimatingDepreciationExpenses orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"FNEV.F\") cashFromOperations operatingIncome (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) UsingOperatingAccrualsConservativelyInAccounting orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"SISA.F\") salesGrowth inventoryGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) EstimatingValueOfInventoryConservatively orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"XOG\") salesGrowth inventoryGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) EstimatingValueOfInventoryConservatively orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"ASBR.F\") incomeTaxExpense preTaxIncome (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) IncreasingEffectiveTaxRateToUnsustainablyHighLevel objectChangingValue (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"NRYY.F\") operatingIncome cashFromOperations (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) UsingOperatingAccrualsAggressivelyInAccounting orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"PVAC\") receivablesGrowth salesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) ImproperRevenueRecognition performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"ERN\") inventoryGrowth salesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) (OverstatingFigureTypeFn ValueOfInventory) performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn VACobraCompanyMapping 207) salesGrowth receivablesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) EstimatingAccountsReceivableConservatively orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"600701\") salesGrowth receivablesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) EstimatingAccountsReceivableConservatively orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"NRYY.F\") inventoryGrowth salesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) (OverstatingFigureTypeFn ValueOfInventory) performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"RICE\") salesGrowth receivablesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) EstimatingAccountsReceivableConservatively orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType BillBarrettCorp agentDepreciationExpenditures agentCapitalExpenditures (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) PracticingAConservativeDepreciationPolicy orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"CGPZ.F\") incomeTaxExpense preTaxIncome (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) IncreasingEffectiveTaxRateToUnsustainablyHighLevel objectChangingValue (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType RangeResourcesCorporation salesGrowth inventoryGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) EstimatingValueOfInventoryConservatively orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"BATM.F\") salesGrowth payablesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) (UnderstatingFigureTypeFn TradePayables) performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType GulfportEnergyCorp salesGrowth receivablesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) EstimatingAccountsReceivableConservatively orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType AnadarkoPetroleumCorp inventoryGrowth salesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) (OverstatingFigureTypeFn ValueOfInventory) performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"JAPA.F\") payablesGrowth salesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) (DoingByFn (DecreaseOnSlotFn (RestrictionOnSlotFn (PlayingRoleInTypeFn payer Paying) rateOfEvent)) DelayingPayment) performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"IDBH.F\") inventoryGrowth salesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) (OverstatingFigureTypeFn ValueOfInventory) performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType RSPPermian salesGrowth payablesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) (UnderstatingFigureTypeFn TradePayables) performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"KRNL.F\") receivablesGrowth salesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) ImproperRevenueRecognition performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType WilmarIndustriesInc preTaxIncome incomeTaxExpense (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) DecreasingEffectiveTaxRateToUnsustainablyLowLevel objectChangingValue (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"CBCF.F\") cashFromOperations operatingIncome (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) UsingOperatingAccrualsConservativelyInAccounting orgPerformer (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"MNHV.F\") receivablesGrowth salesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) ImproperRevenueRecognition performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType (SchemaObjectFn StockTickerSymbols-ReifiedMapping \"GPDN.F\") salesGrowth payablesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) (UnderstatingFigureTypeFn TradePayables) performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType ApacheCorp preTaxIncome incomeTaxExpense (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) DecreasingEffectiveTaxRateToUnsustainablyLowLevel objectChangingValue (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType LanceInc inventoryGrowth salesGrowth (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) (OverstatingFigureTypeFn ValueOfInventory) performedBy (HighToVeryHighAmountFn Relevance))",
"(sentenceExplanatoryHypothesisTypeAndRoleWithRelevance (outlierWRTProportionOfSlotsForPeriodAndSignalType ConocoPhillips preTaxIncome incomeTaxExpense (TheNormalizedFiscalQuarterWithEndingDateFn (DayFn 31 (MonthFn December (YearFn 2016)))) xRankAlongDimensionForPeriod) DecreasingEffectiveTaxRateToUnsustainablyLowLevel objectChangingValue (HighToVeryHighAmountFn Relevance))"

  
  );
          
  
  
  
}
