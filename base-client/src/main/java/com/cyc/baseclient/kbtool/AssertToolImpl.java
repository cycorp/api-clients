package com.cyc.baseclient.kbtool;

/*
 * #%L
 * File: AssertToolImpl.java
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
import com.cyc.base.cycobject.CycAssertion;
import com.cyc.base.cycobject.CycConstant;
import com.cyc.base.cycobject.CycList;
import com.cyc.base.cycobject.CycObject;
import com.cyc.base.cycobject.DenotationalTerm;
import com.cyc.base.cycobject.ElMt;
import com.cyc.base.cycobject.ElMtConstant;
import com.cyc.base.cycobject.FormulaSentence;
import com.cyc.base.cycobject.Fort;
import com.cyc.base.exception.CycApiException;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.base.kbtool.AssertTool;
import com.cyc.baseclient.AbstractKbTool;
import com.cyc.baseclient.CommonConstants;
import com.cyc.baseclient.CycObjectFactory;
import com.cyc.baseclient.cycobject.CycArrayList;
import com.cyc.baseclient.cycobject.ElMtConstantImpl;
import com.cyc.baseclient.cycobject.FormulaSentenceImpl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Tools for asserting facts to the Cyc KB. To unassert facts from the Cyc KB, 
 * use the {@link com.cyc.baseclient.kbtool.UnassertToolImpl}. To perform simple tasks,
 like constant creation or renaming, use the {@link com.cyc.baseclient.kbtool.ObjectToolImpl}.
 * 
 * @see com.cyc.baseclient.kbtool.UnassertToolImpl
 * @see com.cyc.baseclient.kbtool.ObjectToolImpl
 * @author nwinant
 */
public class AssertToolImpl extends AbstractKbTool implements AssertTool {
  
  public AssertToolImpl(CycAccess client) {
    super(client);
  }
  
  
  // Public
  
  @Override
  public void assertArgIsa(Fort relation,
          int argPosition,
          Fort argNIsa)
          throws CycConnectionException, CycApiException {
    // (#$argIsa relation argPosition argNIsa)
    CycArrayList sentence = new CycArrayList();
//    sentence.add(getKnownConstantByGuid_inner("bee22d3d-9c29-11b1-9dad-c379636f7270"));
    sentence.add(CommonConstants.ARG_ISA);
    sentence.add(relation);
    sentence.add(argPosition);
    sentence.add(argNIsa);
    assertGaf(sentence, CommonConstants.UNIVERSAL_VOCABULARY_MT);
  }
  
  @Override
  public void assertArg1Genl(Fort relation,
          Fort argGenl)
          throws CycConnectionException, CycApiException {
    // (#$arg1Genl relation argGenl)
    CycArrayList sentence = new CycArrayList();
    //sentence.add(getKnownConstantByGuid_inner("bd588b1d-9c29-11b1-9dad-c379636f7270"));
    sentence.add(CommonConstants.ARG_1_GENL);
    sentence.add(relation);
    sentence.add(argGenl);
    assertGaf(sentence,
            CommonConstants.UNIVERSAL_VOCABULARY_MT);
  }
  
  @Override
  public void assertArg2Genl(Fort relation,
          Fort argGenl)
          throws CycConnectionException, CycApiException {
    // (#$arg2Genl relation argGenl)
    CycArrayList sentence = new CycArrayList();
    //sentence.add(getKnownConstantByGuid_inner("bd58dcda-9c29-11b1-9dad-c379636f7270"));
    sentence.add(CommonConstants.ARG_2_GENL);
    sentence.add(relation);
    sentence.add(argGenl);
    assertGaf(sentence,
            CommonConstants.UNIVERSAL_VOCABULARY_MT);
  }
  
  @Override
  public void assertArg3Genl(Fort relation,
          Fort argGenl)
          throws CycConnectionException, CycApiException {
    // (#$arg3Genl relation argGenl)
    CycArrayList sentence = new CycArrayList();
    //sentence.add(getKnownConstantByGuid_inner("bd58b8c3-9c29-11b1-9dad-c379636f7270"));
    sentence.add(CommonConstants.ARG_3_GENL);
    sentence.add(relation);
    sentence.add(argGenl);
    assertGaf(sentence,
            CommonConstants.UNIVERSAL_VOCABULARY_MT);
  }
  
  @Override
  public void assertResultIsa(Fort denotationalFunction,
          Fort resultIsa)
          throws CycConnectionException, CycApiException {
    // (#$resultIsa denotationalFunction resultIsa)
    assertGaf(CommonConstants.UNIVERSAL_VOCABULARY_MT,
    //        getKnownConstantByGuid_inner("bd5880f1-9c29-11b1-9dad-c379636f7270"),
            CommonConstants.RESULT_ISA,
            denotationalFunction,
            resultIsa);
  }
  
  @Override
  public void assertResultGenl(Fort denotationalFunction,
          Fort resultGenl)
          throws CycConnectionException, CycApiException {
    // (#$resultGenl denotationalFunction resultGenls)
    assertGaf(CommonConstants.UNIVERSAL_VOCABULARY_MT,
//            getKnownConstantByGuid_inner("bd58d6ab-9c29-11b1-9dad-c379636f7270"),
            CommonConstants.RESULT_GENL,
            denotationalFunction,
            resultGenl);
  }
  
  @Override
  public void assertWithTranscript(CycList sentence,
          CycObject mt)
          throws CycConnectionException, CycApiException {
    assertWithTranscript(sentence.stringApiValue(), mt);
  }
  
  @Override
  public void assertWithTranscript(String sentence,
          CycObject mt)
          throws CycConnectionException, CycApiException {
    assertSentence(sentence, makeElMt_inner(mt), null, null, false, true);
  }
  
  @Override
  public void assertWithTranscriptAndBookkeeping(String sentence, CycObject mt)
          throws CycConnectionException, CycApiException {
    assertWithTranscriptAndBookkeeping(getCyc().getObjectTool().makeCycSentence(sentence), mt);
  }
  
  @Override
  public void assertWithTranscriptAndBookkeeping(FormulaSentence sentence,
          CycObject mt)
          throws CycConnectionException, CycApiException {
    assertWithTranscriptAndBookkeepingInternal(sentence, mt);
  }
  
  @Override
  public void assertWithTranscriptAndBookkeeping(CycList sentence, CycObject mt)
          throws CycConnectionException, CycApiException {
    assertWithTranscriptAndBookkeepingInternal(sentence, mt);
  }
  
  @Override
  public void assertWithBookkeepingAndWithoutTranscript(CycList sentence,
          CycObject mt)
          throws CycConnectionException, CycApiException {
    assertWithBookkeepingAndWithoutTranscript(sentence.stringApiValue(), mt);
  }
  
  @Override
  public void assertWithBookkeepingAndWithoutTranscript(String sentence,
          CycObject mt)
          throws CycConnectionException,
          CycApiException {
    assertSentence(sentence, makeElMt_inner(mt), null, null, true, false);
  }

  @Override
  public CycList<CycAssertion> assertSentence(String sentence, ElMt mt, boolean bookkeeping,
          boolean transcript)
          throws CycConnectionException, CycApiException {
    return assertSentence(sentence, mt, null, null, bookkeeping, transcript, (Fort) null);
  }
  
  @Override
  public CycList<CycAssertion> assertSentence(String sentence, ElMt mt, String strength, String direction, boolean bookkeeping,
          boolean transcript)
          throws CycConnectionException, CycApiException {
    return assertSentence(sentence, mt, strength, direction, bookkeeping, transcript, (Fort) null);
  }
  
  @Override
  public CycList<CycAssertion> assertSentence(String sentence, ElMt mt, String strength, String direction, boolean bookkeeping,
          boolean transcript,
          Fort template)
          throws CycConnectionException, CycApiException {
    List<Fort> templates = new ArrayList<>();
    if (template != null) {
      templates.add(template);
    }
    return assertSentence(sentence, mt, strength, direction, bookkeeping, transcript, false, templates);
  }

  @Override
  public CycList<CycAssertion> assertSentence(String sentence, ElMt mt, boolean bookkeeping,
          boolean transcript,
          List<Fort> templates)
          throws CycConnectionException, CycApiException {
    return assertSentence(sentence, mt, null, null, bookkeeping, transcript, false, templates);
  }
  
  @Override
  public CycList<CycAssertion> assertSentence(
          String sentence,
          ElMt mt, 
          String strength,
          String direction,
          boolean bookkeeping,
          boolean transcript,
          boolean disableWFFChecking,
          List<Fort> templates) throws CycConnectionException, CycApiException {
    if (getCurrentTransaction() != null) {
      getCurrentTransaction().noteForAssertion(sentence, mt, bookkeeping,
              transcript, disableWFFChecking, templates);
      return null;
    }
    if (strength == null){
    	strength = ":default";
    }
    
    String command; 
    if (transcript) {
    command = "(multiple-value-list (ke-assert-now\n"
            + sentence + "\n" + mt.stringApiValue() + " " + strength + " " + (direction != null ? direction : "") + "))";
    } else {
      command = "(multiple-value-list (cyc-assert\n"
            + sentence + "\n" + mt.stringApiValue() + " '(:strength " + strength + " " + (direction != null ? ":direction " + direction : "") + ")))";
    }
    if (bookkeeping) {
      command = getConverse().wrapBookkeeping(command);
    } else {
      command = getConverse().wrapCyclistAndPurpose(command);
    }
    command = getConverse().wrapForwardInferenceRulesTemplates(command, templates);
    if (disableWFFChecking) {
      command = getConverse().wrapDisableWffChecking(command);
    }
    CycList<Object> results = getConverse().converseList(command);
    boolean statusOk = !results.get(0).equals(CycObjectFactory.nil);
    if (!statusOk) {
      String message = "Assertion sentence: " + sentence + " failed in mt: " + mt.cyclify();
      if (results.size() > 1) {
        message += "\n" + sentence + "\nbecause: \n" + results.get(1);
      }
      throw new CycApiException(message);
    }
    final Object result = (results.size() > 2) ? results.get(2) : results.get(1);
    if (result instanceof CycList) {
      return (CycList) result;
    } else if (CycObjectFactory.nil.equals(result)) {
      return null;
    }
    throw new CycApiException(
            "Expected arg " + (results.size() - 1) + " of assert results to be a CycList, but was "
            + (result != null ? result.getClass() + ": " + result : null));
  }
  
  @Override
  public void assertTermDependsOn(final Fort dependentTerm,
          final Fort independentTerm, final Fort mt) throws CycConnectionException, CycApiException {
    // assert (#$termDependsOn <dependentTerm> <independentTerm>) in #$UniversalVocabularyMt
    assertGaf(mt, 
            //getKnownConstantByGuid_inner("bdf02d74-9c29-11b1-9dad-c379636f7270"),
            CommonConstants.TERM_DEPENDS_ON,
            dependentTerm, 
            independentTerm);
  }
  
  @Override
  public void assertDefiningMt(final Fort dependentTerm, final Fort mt) throws CycConnectionException, CycApiException {
    // assert (#$definingMt <dependentTerm> <mt>) in #$BaseKB
    assertGaf(CommonConstants.BASE_KB, 
            //getKnownConstantByGuid_inner("bde5ec9c-9c29-11b1-9dad-c379636f7270"), 
            CommonConstants.DEFINING_MT,
            dependentTerm, mt);
  }
  
  @Override
  public void assertArg1FormatSingleEntry(Fort relation)
          throws CycConnectionException, CycApiException {
    // (#$arg1Format relation SingleEntry)
    assertArgFormat(relation, 1, 
            //getKnownConstantByGuid_inner("bd5880eb-9c29-11b1-9dad-c379636f7270")
            CommonConstants.SINGLE_ENTRY
    );
  }
  
  @Override
  public void assertArgFormat(Fort relation, int argPosition,
          Fort argNFormat)
          throws CycConnectionException, CycApiException {
    // (#$argFormat relation argPosition argNFormat)
    FormulaSentence sentence = FormulaSentenceImpl.makeFormulaSentence(
            //getKnownConstantByGuid_inner("bd8a36e1-9c29-11b1-9dad-c379636f7270"),
            CommonConstants.ARG_FORMAT,
            relation,
            argPosition, argNFormat);
    assertGaf(sentence, CommonConstants.BASE_KB);
  }
  
  @Override
  public void assertSynonymousExternalConcept(String cycTerm,
          String informationSource,
          String externalConcept,
          String mt)
          throws CycConnectionException, CycApiException {
    assertSynonymousExternalConcept(getKnownConstantByName_inner(cycTerm),
            getKnownConstantByName_inner(informationSource),
            externalConcept,
            getKnownConstantByName_inner(mt));
  }
  
  @Override
  public void assertSynonymousExternalConcept(Fort cycTerm,
          Fort informationSource,
          String externalConcept,
          CycObject mt)
          throws CycConnectionException, CycApiException {
    FormulaSentence gaf = FormulaSentenceImpl.makeFormulaSentence(
            //getKnownConstantByGuid_inner("c0e2af4e-9c29-11b1-9dad-c379636f7270"),
            CommonConstants.SYNONYMOUS_EXTERNAL_CONCEPT,
            cycTerm, informationSource, externalConcept);
    assertGaf(gaf, makeElMt_inner(mt));
  }
  
  @Override
  public void assertIsaCollection(Fort cycFort)
          throws CycConnectionException, CycApiException {
    assertGaf(CommonConstants.UNIVERSAL_VOCABULARY_MT,
            CommonConstants.ISA,
            cycFort,
            CommonConstants.COLLECTION);
  }
  
  @Override
  public void assertIsaCollection(Fort cycFort,
          CycObject mt)
          throws CycConnectionException, CycApiException {
    ElMt elmt = makeElMt_inner(mt);
    assertGaf(elmt,
            CommonConstants.ISA,
            cycFort,
            CommonConstants.COLLECTION);
  }
  
  @Override
  public void assertGenls(String specCollectionName,
          String genlsCollectionName,
          String mtName)
          throws CycConnectionException, CycApiException {
    assertGaf(makeElMt_inner(getKnownConstantByName_inner(mtName)),
            CommonConstants.GENLS,
            getKnownConstantByName_inner(specCollectionName),
            getKnownConstantByName_inner(genlsCollectionName));
  }
  
  @Override
  public void assertGenls(String specCollectionName,
          String genlsCollectionName)
          throws CycConnectionException, CycApiException {
    assertGaf(CommonConstants.UNIVERSAL_VOCABULARY_MT,
            CommonConstants.GENLS,
            getKnownConstantByName_inner(specCollectionName),
            getKnownConstantByName_inner(genlsCollectionName));
  }
  
  @Override
  public void assertGenls(Fort specCollection,
          Fort genlsCollection)
          throws CycConnectionException, CycApiException {
    assertGaf(CommonConstants.UNIVERSAL_VOCABULARY_MT,
            CommonConstants.GENLS,
            specCollection,
            genlsCollection);
  }
  
  @Override
  public void assertGenls(Fort specCollection,
          Fort genlsCollection,
          CycObject mt)
          throws CycConnectionException, CycApiException {
    ElMt elmt = makeElMt_inner(mt);
    assertGaf(elmt,
            CommonConstants.GENLS,
            specCollection,
            genlsCollection);
  }
  
  @Override
  public void assertGenlPreds(String specPredName,
          String genlPredName)
          throws CycConnectionException, CycApiException {
    //CycConstant genlPreds = getKnownConstantByGuid_inner(
    //        "bd5b4951-9c29-11b1-9dad-c379636f7270");
    assertGaf(CommonConstants.UNIVERSAL_VOCABULARY_MT,
            CommonConstants.GENL_PREDS,
            getKnownConstantByName_inner(specPredName),
            getKnownConstantByName_inner(genlPredName));
  }
  
  @Override
  public void assertGenlPreds(Fort specPred,
          Fort genlPred)
          throws CycConnectionException, CycApiException {
    //CycConstant genlPreds = getKnownConstantByGuid_inner(
    //        "bd5b4951-9c29-11b1-9dad-c379636f7270");
    assertGaf(CommonConstants.UNIVERSAL_VOCABULARY_MT,
            CommonConstants.GENL_PREDS,
            specPred,
            genlPred);
  }
  
  @Override
  public void assertGenlMt(String specMtName,
          String genlsMtName)
          throws CycConnectionException, CycApiException {
    assertGaf(CommonConstants.UNIVERSAL_VOCABULARY_MT,
            CommonConstants.GENL_MT,
            getKnownConstantByName_inner(specMtName),
            getKnownConstantByName_inner(genlsMtName));
  }
  
  @Override
  public void assertGenlMt(Fort specMt,
          Fort genlsMt)
          throws CycConnectionException, CycApiException {
    assertGaf(CommonConstants.UNIVERSAL_VOCABULARY_MT, CommonConstants.GENL_MT, specMt, genlsMt);
  }
  
  @Override
  public void assertIsa(String cycFortName,
          String collectionName)
          throws CycConnectionException, CycApiException {
    assertGaf(CommonConstants.UNIVERSAL_VOCABULARY_MT,
            CommonConstants.ISA,
            getKnownConstantByName_inner(cycFortName),
            getKnownConstantByName_inner(collectionName));
  }
  
  @Override
  public void assertIsa(String cycFortName,
          String collectionName,
          String mtName)
          throws CycConnectionException, CycApiException {
    assertGaf(makeElMt_inner(getKnownConstantByName_inner(mtName)),
            CommonConstants.ISA,
            getKnownConstantByName_inner(cycFortName),
            getKnownConstantByName_inner(collectionName));
  }
  
  @Override
  public void assertIsa(Fort cycFort,
          Fort aCollection,
          CycObject mt)
          throws CycConnectionException, CycApiException {
    assertGaf(makeElMt_inner(mt),
            CommonConstants.ISA,
            cycFort,
            aCollection);
  }
  
  @Override
  public void assertQuotedIsa(Fort cycFort, Fort aCollection, CycObject mt)
          throws CycConnectionException, CycApiException {
    assertGaf(makeElMt_inner(mt),
            //getKnownConstantByGuid_inner("055544a2-4371-11d6-8000-00a0c9da2002"),
            CommonConstants.QUOTED_ISA,
            cycFort,
            aCollection);
  }
  
  @Override
  public void assertIsa(DenotationalTerm instance,
          DenotationalTerm aCollection)
          throws CycConnectionException, CycApiException {
    assertGaf(CommonConstants.UNIVERSAL_VOCABULARY_MT,
            CommonConstants.ISA,
            instance,
            aCollection);
  }
  
  @Override
  public void assertIsaBinaryPredicate(Fort cycFort)
          throws CycConnectionException, CycApiException {
    assertIsa(cycFort,
            CommonConstants.BINARY_PREDICATE,
            CommonConstants.UNIVERSAL_VOCABULARY_MT);
  }
  
  @Override
  public void assertIsaBinaryPredicate(Fort cycFort,
          CycObject mt)
          throws CycConnectionException, CycApiException {
    assertIsa(cycFort,
            CommonConstants.BINARY_PREDICATE,
            makeElMt_inner(mt));
  }
  
  @Override
  public void assertNameString(String cycConstantName,
          String nameString,
          String mtName)
          throws CycConnectionException, CycApiException {
    assertGaf(makeElMt_inner(getKnownConstantByName_inner(mtName)),
            //getKnownConstantByGuid_inner("c0fdf7e8-9c29-11b1-9dad-c379636f7270"),
            CommonConstants.NAME_STRING,
            getKnownConstantByName_inner(cycConstantName),
            nameString);
  }
  
  @Override
  public void assertComment(String cycConstantName,
          String comment,
          String mtName)
          throws CycConnectionException, CycApiException {
    assertGaf(makeElMt_inner(getKnownConstantByName_inner(mtName)),
            CommonConstants.COMMENT,
            getKnownConstantByName_inner(cycConstantName),
            comment);
  }
  
  @Override
  public void assertComment(Fort cycFort,
          String comment,
          CycObject mt)
          throws CycConnectionException, CycApiException {
    ElMt elmt = makeElMt_inner(mt);
    assertGaf(elmt,
            CommonConstants.COMMENT,
            cycFort,
            comment);
  }
  
  @Override
  public void assertNameString(Fort cycFort,
          String nameString,
          CycObject mt)
          throws CycConnectionException, CycApiException {
    ElMt elmt = makeElMt_inner(mt);
    assertGaf(elmt,
            //getKnownConstantByGuid_inner("c0fdf7e8-9c29-11b1-9dad-c379636f7270"),
            CommonConstants.NAME_STRING,
            cycFort,
            nameString);
  }

  /**
   * Assert a paraphrase format for the specified Fort in the #$EnglishParaphraseMt. The
 operation will be added to the KB transcript for replication and archive.
   *
   * @param relation the given term
   * @param genFormatString the genFormat string
   * @param genFormatList the genFormat argument substitution sequence
   *
   * @throws CycConnectionException if cyc server host not found on the network or a data communication error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  @Deprecated
  public void assertGenFormat(Fort relation,
          String genFormatString,
          CycList genFormatList)
          throws CycConnectionException, CycApiException {
    // (#$genFormat <relation> <genFormatString> <genFormatList>)
    CycArrayList sentence = new CycArrayList();
    //sentence.add(getKnownConstantByGuid_inner("beed06de-9c29-11b1-9dad-c379636f7270"));
    sentence.add(CommonConstants.GEN_FORMAT);
    sentence.add(relation);
    sentence.add(genFormatString);

    if (genFormatList.isEmpty()) {
      sentence.add(CycObjectFactory.nil);
    } else {
      sentence.add(genFormatList);
    }

    assertGaf(sentence,
            // #$EnglishParaphraseMt
//            makeElMt_inner(getKnownConstantByGuid_inner(
//            "bda16220-9c29-11b1-9dad-c379636f7270")));
            CommonConstants.ENGLISH_PARAPHRASE_MT);
  }
  
  @Override
  public void assertIsas(List fortNames,
          String collectionName)
          throws CycConnectionException, CycApiException {
    List forts = new ArrayList();

    for (int i = 0; i < forts.size(); i++) {
      Object fort = forts.get(i);

      if (fort instanceof String) {
        forts.add(getKnownConstantByName_inner((String) fort));
      } else if (fort instanceof Fort) {
        forts.add(fort);
      } else {
        throw new CycApiException(fort + " is neither String nor CycFort");
      }

      assertIsas(forts,
              getKnownConstantByName_inner(collectionName));
    }
  }
  
  @Override
  public void assertIsas(List forts,
          Fort collection)
          throws CycConnectionException, CycApiException {
    for (int i = 0; i < forts.size(); i++) {
      assertIsa((Fort) forts.get(i),
              collection);
    }
  }
  
  @Override
  public void assertGaf(CycObject mt,
          DenotationalTerm predicate,
          DenotationalTerm arg1,
          DenotationalTerm arg2)
          throws CycConnectionException, CycApiException {
    // (PREDICATE <CycFort> <CycFort>)
    CycArrayList sentence = new CycArrayList();
    sentence.add(predicate);
    sentence.add(arg1);
    sentence.add(arg2);
    assertWithTranscriptAndBookkeeping(sentence, mt);
  }
  
  @Override
  public void assertGaf(CycObject mt,
          Fort predicate,
          Fort arg1,
          String arg2)
          throws CycConnectionException, CycApiException {
    // (PREDICATE <CycFort> <String>)
    CycArrayList sentence = new CycArrayList();
    sentence.add(predicate);
    sentence.add(arg1);
    sentence.add(arg2);
    assertWithTranscriptAndBookkeeping(sentence, mt);
  }
  
  @Override
  public void assertGaf(CycObject mt,
          CycConstant predicate,
          Fort arg1,
          CycList arg2)
          throws CycConnectionException, CycApiException {
    // (PREDICATE <CycFort> <List>)
    CycArrayList sentence = new CycArrayList();
    sentence.add(predicate);
    sentence.add(arg1);
    sentence.add(arg2);
    assertWithTranscriptAndBookkeeping(sentence, mt);
  }
  
  @Override
  public void assertGaf(CycObject mt,
          CycConstant predicate,
          Fort arg1,
          int arg2)
          throws CycConnectionException, CycApiException {
    // (PREDICATE <CycFort> <int>)
    assertGaf(mt, predicate, arg1, (Integer) arg2);
  }
  
  @Override
  public void assertGaf(CycObject mt,
          Fort predicate,
          Fort arg1,
          Integer arg2)
          throws CycConnectionException, CycApiException {
    // (PREDICATE <CycFort> <int>)
    CycArrayList sentence = new CycArrayList();
    sentence.add(predicate);
    sentence.add(arg1);
    sentence.add(arg2);
    assertWithTranscriptAndBookkeeping(sentence, mt);
  }
  
  @Override
  public void assertGaf(CycObject mt,
          Fort predicate,
          Fort arg1,
          Double arg2)
          throws CycConnectionException, CycApiException {
    // (PREDICATE <CycFort> <int>)
    CycArrayList sentence = new CycArrayList();
    sentence.add(predicate);
    sentence.add(arg1);
    sentence.add(arg2);
    assertWithTranscriptAndBookkeeping(sentence, mt);
  }
  
  @Override
  public void assertGaf(CycObject mt,
          CycConstant predicate,
          Fort arg1,
          Fort arg2,
          Fort arg3)
          throws CycConnectionException, CycApiException {
    // (PREDICATE <CycFort> <CycFort> <CycFort>)
    CycArrayList sentence = new CycArrayList();
    sentence.add(predicate);
    sentence.add(arg1);
    sentence.add(arg2);
    sentence.add(arg3);
    assertWithTranscriptAndBookkeeping(sentence, mt);
  }
  
  @Override
  public void assertGaf(CycList gaf,
          CycObject mt)
          throws CycConnectionException, CycApiException {
    assertWithTranscriptAndBookkeeping(gaf, mt);
  }
  
  @Override
  public void assertGaf(FormulaSentence gaf, CycObject mt)
          throws CycConnectionException, CycApiException {
    assertWithTranscriptAndBookkeepingInternal(gaf, mt);
  }
  
  @Override
  public void edit(String unassertSentence, String assertSentence, ElMt mt,
          boolean bookkeeping, boolean transcript, boolean disableWFFChecking,
          List<Fort> templates) throws CycApiException, CycConnectionException {
    String command = "(multiple-value-list (" + (transcript ? "ke-edit-now" : "cyc-edit") + "\n"
            + unassertSentence + "\n" + assertSentence + "\n" + mt.stringApiValue() + "))";
    if (bookkeeping) {
      command = getConverse().wrapBookkeeping(command);
    } else {
      command = getConverse().wrapCyclistAndPurpose(command);
    }
    command = getConverse().wrapForwardInferenceRulesTemplates(command, templates);
    if (disableWFFChecking) {
      command = getConverse().wrapDisableWffChecking(command);
    }
    CycList<Object> results = getConverse().converseList(command);
    boolean statusOk = !results.get(0).equals(CycObjectFactory.nil);
    if (!statusOk) {
      throw new CycApiException("Edit failure of " + unassertSentence + " to " + assertSentence + " in mt: " + mt.cyclify()
              + "\nbecause: \n" + results.get(1));
    }
  }
  
  @Override
  public CycConstant findOrCreateNewPermanent(String constantName)
          throws CycConnectionException, CycApiException {
    return getCyc().getObjectTool().makeCycConstant(constantName);
  }
  
  @Deprecated
  @Override
  public CycConstant findOrCreateBinaryPredicate(String predicateName,
          String predicateTypeName,
          String comment,
          String arg1IsaName,
          String arg2IsaName,
          String arg1FormatName,
          String arg2FormatName,
          String genlPredsName)
          throws CycConnectionException, CycApiException {
    return findOrCreateBinaryPredicate(predicateName,
            find_inner(predicateTypeName),
            comment,
            find_inner(arg1IsaName),
            find_inner(arg2IsaName),
            find_inner(arg1FormatName),
            find_inner(arg2FormatName),
            find_inner(genlPredsName));
  }
  
  @Override
  public CycConstant findOrCreateBinaryPredicate(String predicateName,
          Fort predicateType,
          String comment,
          Fort arg1Isa,
          Fort arg2Isa,
          Fort arg1Format,
          Fort arg2Format,
          Fort genlPreds)
          throws CycConnectionException, CycApiException {
    final CycConstant predicate = findOrCreate_inner(predicateName);
    if (predicateType == null) {
      assertIsa(predicate,
              CommonConstants.BINARY_PREDICATE);
    } else {
      assertIsa(predicate,
              predicateType);
    }
    if (comment != null) {
      assertComment(predicate,
              comment,
              CommonConstants.BASE_KB);
    }
    if (arg1Isa != null) {
      assertArgIsa(predicate,
              1,
              arg1Isa);
    }
    if (arg2Isa != null) {
      assertArgIsa(predicate,
              2,
              arg2Isa);
    }
    if (arg1Format != null) {
      assertArgFormat(predicate,
              1,
              arg1Format);
    }
    if (arg2Format != null) {
      assertArgFormat(predicate,
              2,
              arg2Format);
    }
    if (genlPreds == null) {
      assertGenlPreds(predicate,
              // #$conceptuallyRelated
              //getKnownConstantByGuid_inner("bd58803e-9c29-11b1-9dad-c379636f7270"));
              CommonConstants.CONCEPTUALLY_RELATED);
    } else {
      assertGenlPreds(predicate,
              genlPreds);
    }
    return predicate;
  }

  /* *
   * Creates a new KB subset COLLECTION term.
   *
   * @param constantName the name of the new KB subset COLLECTION
   * @param comment the comment for the new KB subset COLLECTION
   *
   * @return the new KB subset COLLECTION term
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the Cyc server returns an error
   */
  // Commented out regarding BASEAPI-63 - nwinant, 2014-08-18
  /*
  public CycConstant createKbSubsetCollection(String constantName,
          String comment)
          throws CycConnectionException, CycApiException {
    //CycConstant kbSubsetCollection = getKnownConstantByName_inner(
    //        "KBSubsetCollection");
    CycConstant cycConstant = getConstantByName_inner(constantName);

    if (cycConstant == null) {
      cycConstant = findOrCreateNewPermanent(constantName);
    }

    assertIsa(cycConstant,
            CommonConstants.CYC_KB_SUBSET_COLLECTION);
    assertComment(cycConstant,
            comment,
            CommonConstants.BASE_KB);
    assertGenls(cycConstant,
            CommonConstants.THING);

    //Fort variableOrderCollection = getKnownConstantByGuid_inner(
    //        "36cf85d0-20a1-11d6-8000-0050dab92c2f");
    assertIsa(cycConstant,
//            variableOrderCollection,
            CommonConstants.VARIED_ORDER_COLLECTION,
            CommonConstants.BASE_KB);

    return cycConstant;
  }
  */
  
  @Override
  public CycConstant findOrCreateCollection(String collectionName,
          String comment,
          String commentMtName,
          String isaName,
          String genlsName)
          throws CycConnectionException, CycApiException {
    CycConstant cycCollection = findOrCreate_inner(collectionName);
    assertComment(cycCollection,
            comment,
            getKnownConstantByName_inner(commentMtName));
    assertIsa(cycCollection,
            getKnownConstantByName_inner(isaName));
    assertGenls(cycCollection,
            getKnownConstantByName_inner(genlsName));

    return cycCollection;
  }
  
  @Override
  public Fort findOrCreateCollection(String collectionName,
          String comment,
          Fort commentMt,
          Fort isa,
          Fort genls)
          throws CycConnectionException, CycApiException {
    return describeCollection(findOrCreate_inner(collectionName),
            comment,
            commentMt,
            isa,
            genls);
  }
  
  @Override
  public Fort describeCollection(Fort collection,
          String comment,
          Fort commentMt,
          Fort isa,
          Fort genls)
          throws CycConnectionException, CycApiException {
    assertComment(collection,
            comment,
            commentMt);
    assertIsa(collection,
            isa);
    assertGenls(collection,
            genls);

    return collection;
  }
  
  @Override
  public ElMtConstant createMicrotheory(String mtName,
          String comment,
          Fort isaMt,
          List genlMts)
          throws CycConnectionException, CycApiException {
    CycConstant mt = getConstantByName_inner(mtName);
    if (mt != null) {
      getCyc().getUnassertTool().kill(mt);
    }
    mt = findOrCreateNewPermanent(mtName);
    assertComment(mt,
            comment,
            CommonConstants.BASE_KB);
    assertGaf(CommonConstants.UNIVERSAL_VOCABULARY_MT,
            CommonConstants.ISA,
            mt,
            isaMt);
    final Iterator iterator = genlMts.iterator();
    while (iterator.hasNext()) {
      final Fort aGenlMt = (Fort) iterator.next();
      assertGaf(CommonConstants.UNIVERSAL_VOCABULARY_MT,
              CommonConstants.GENL_MT,
              mt,
              aGenlMt);
    }
    return (mt != null) ? ElMtConstantImpl.makeElMtConstant(mt) : null;
  }
  
  @Override
  public void describeMicrotheory(Fort mt,
          String comment,
          Fort isaMt,
          List genlMts)
          throws CycConnectionException, CycApiException {
    assertComment(mt,
            comment,
            CommonConstants.BASE_KB);
    assertGaf(CommonConstants.UNIVERSAL_VOCABULARY_MT,
            CommonConstants.ISA,
            mt,
            isaMt);
    final Iterator iterator = genlMts.iterator();
    while (iterator.hasNext()) {
      final CycArrayList gaf = new CycArrayList(3);
      gaf.add(CommonConstants.GENL_MT);
      gaf.add(mt);
      gaf.add(iterator.next());
      assertGaf(gaf, CommonConstants.UNIVERSAL_VOCABULARY_MT);
    }
  }
  
  @Override
  public ElMtConstant createMicrotheory(String mtName,
          String comment,
          String isaMtName,
          List genlMts)
          throws CycConnectionException, CycApiException {
    CycConstant mt = getConstantByName_inner(mtName);
    if (mt != null) {
      getCyc().getUnassertTool().kill(mt);
    }
    mt = findOrCreateNewPermanent(mtName);
    assertComment(mt,
            comment,
            CommonConstants.BASE_KB);
    assertIsa(mtName,
            isaMtName);
    final Iterator<String> iterator = genlMts.iterator();
    while (iterator.hasNext()) {
      final String genlMtName = iterator.next();
      assertGenlMt(mtName, genlMtName);
    }
    return (mt != null) ? ElMtConstantImpl.makeElMtConstant(mt) : null;
  }
  
  @Override
  public ElMtConstant createCollectorMt(String mtName,
          String comment,
          List genlMts)
          throws CycConnectionException, CycApiException {
    //CycConstant collectorMt = getKnownConstantByName_inner("CollectorMicrotheory");
    return createMicrotheory(mtName,
            comment,
            CommonConstants.COLLECTOR_MICROTHEORY,
            genlMts);
  }
  
  @Override
  public ElMtConstant createSpindleMt(String spindleMtName,
          String comment,
          String spindleHeadMtName,
          String spindleCollectorMtName)
          throws CycConnectionException, CycApiException {
    return createSpindleMt(spindleMtName,
            comment,
            getKnownConstantByName_inner(spindleHeadMtName),
            getKnownConstantByName_inner(spindleCollectorMtName));
  }
  
  @Override
  public ElMtConstant createSpindleMt(String spindleMtName,
          String comment,
          Fort spindleHeadMt,
          Fort spindleCollectorMt)
          throws CycConnectionException, CycApiException {
    //CycConstant spindleMt = getKnownConstantByName_inner("SpindleMicrotheory");
    final List genlMts = new ArrayList();
    genlMts.add(spindleHeadMt);
    final ElMtConstant mt = this.createMicrotheory(spindleMtName,
            comment,
            CommonConstants.SPINDLE_MICROTHEORY,
            genlMts);
    assertGaf(CommonConstants.UNIVERSAL_VOCABULARY_MT,
            CommonConstants.GENL_MT,
            spindleCollectorMt,
            mt);
    return mt;
  }
  
  @Override
  public Fort findOrCreateIndividual(String IndividualName,
          String comment,
          String commentMt,
          String isa)
          throws CycConnectionException, CycApiException {
    return findOrCreateIndividual(IndividualName,
            comment,
            getKnownConstantByName_inner(commentMt),
            getKnownConstantByName_inner(isa));
  }
  
  @Override
  public Fort findOrCreateIndividual(String IndividualName,
          String comment,
          DenotationalTerm commentMt,
          DenotationalTerm isa)
          throws CycConnectionException, CycApiException {
    Fort individual = findOrCreate_inner(IndividualName);
    assertComment(individual,
            comment,
            commentMt);
    assertIsa(individual,
            isa);

    return individual;
  }
  
  @Override
  public Fort findOrCreateIndivDenotingUnaryFunction(String unaryFunction,
          String comment,
          String commentMt,
          String arg1Isa,
          String resultIsa)
          throws CycConnectionException, CycApiException {
    return describeIndivDenotingUnaryFunction(findOrCreate_inner(
            unaryFunction),
            comment,
            getKnownConstantByName_inner(
            commentMt),
            getKnownConstantByName_inner(
            arg1Isa),
            getKnownConstantByName_inner(
            resultIsa));
  }
  
  @Override
  public Fort describeIndivDenotingUnaryFunction(Fort unaryFunction,
          String comment,
          Fort commentMt,
          Fort arg1Isa,
          Fort resultIsa)
          throws CycConnectionException, CycApiException {
    assertComment(unaryFunction,
            comment,
            commentMt);
    // (#$isa unaryFunction #$UnaryFunction)
    assertIsa(unaryFunction,
            CommonConstants.UNARY_FUNCTION);
//            getKnownConstantByGuid_inner("bd58af89-9c29-11b1-9dad-c379636f7270"));
    // (#$isa unaryFunction #$ReifiableFunction)
    assertIsa(unaryFunction,
            CommonConstants.REIFIABLE_FUNCTION);
            //getKnownConstantByGuid_inner("bd588002-9c29-11b1-9dad-c379636f7270"));
    // (#$isa unaryFunction #$IndividualDenotingFunction)
    assertIsa(unaryFunction,
            CommonConstants.INDIVIDUAL_DENOTING_FUNCTION);
            //getKnownConstantByGuid_inner("bd58fad9-9c29-11b1-9dad-c379636f7270"));
    // (#$isa unaryFunction #$Function-Denotational)
    assertIsa(unaryFunction,
            CommonConstants.FUNCTION_DENOTATIONAL);
            //getKnownConstantByGuid_inner("bd5c40b0-9c29-11b1-9dad-c379636f7270"));
    assertArgIsa(unaryFunction,
            1,
            arg1Isa);
    assertResultIsa(unaryFunction,
            resultIsa);
    return unaryFunction;
  }
  
  @Override
  public Fort findOrCreateCollectionDenotingUnaryFunction(String unaryFunction,
          String comment,
          String commentMt,
          String arg1Isa,
          String arg1GenlName,
          String resultIsa,
          String resultGenlName)
          throws CycConnectionException, CycApiException {
    final Fort arg1Genl = (arg1GenlName != null) 
            ? getKnownConstantByName_inner(arg1GenlName) 
            : null;
    final Fort resultGenl = (resultGenlName != null) 
            ? getKnownConstantByName_inner(resultGenlName)
            : null;
    return describeCollectionDenotingUnaryFunction(findOrCreate_inner(
            unaryFunction),
            comment,
            getKnownConstantByName_inner(
            commentMt),
            getKnownConstantByName_inner(
            arg1Isa),
            arg1Genl,
            getKnownConstantByName_inner(
            resultIsa),
            resultGenl);
  }

  /**
   * Describes a new COLLECTION-denoting reifiable unary function term.
   *
   * @param unaryFunction the unary function
   * @param comment the comment for the unary function
   * @param commentMt the microtheory in which the comment is asserted
   * @param arg1Isa the isa type constraint for the argument
   * @param arg1Genl the genls type constraint for the argument if it is a COLLECTION
   * @param resultIsa the isa object represented by this reified term
   * @param resultGenl the genls object represented by this reified term
   *
   * @return the new COLLECTION-denoting reifiable unary function term
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the Cyc server returns an error
   */
  @Override
  public Fort describeCollectionDenotingUnaryFunction(Fort unaryFunction,
          String comment,
          Fort commentMt,
          Fort arg1Isa,
          Fort arg1Genl,
          Fort resultIsa,
          Fort resultGenl)
          throws CycConnectionException, CycApiException {
    assertComment(unaryFunction,
            comment,
            commentMt);
    // (#$isa unaryFunction #$UnaryFunction)
    assertIsa(unaryFunction,
            CommonConstants.UNARY_FUNCTION);
            //getKnownConstantByGuid_inner("bd58af89-9c29-11b1-9dad-c379636f7270"));
    // (#$isa unaryFunction #$ReifiableFunction)
    assertIsa(unaryFunction,
            CommonConstants.REIFIABLE_FUNCTION);
            //getKnownConstantByGuid_inner("bd588002-9c29-11b1-9dad-c379636f7270"));
    // (#$isa unaryFunction #$CollectionDenotingFunction)
    assertIsa(unaryFunction,
            CommonConstants.COLLECTION_DENOTING_FUNCTION);
            //getKnownConstantByGuid_inner("bd58806a-9c29-11b1-9dad-c379636f7270"));
    // (#$isa unaryFunction #$Function-Denotational)
    assertIsa(unaryFunction,
            CommonConstants.FUNCTION_DENOTATIONAL);
            //getKnownConstantByGuid_inner("bd5c40b0-9c29-11b1-9dad-c379636f7270"));
    assertArgIsa(unaryFunction,
            1,
            arg1Isa);
    if (arg1Genl != null) {
      assertArg1Genl(unaryFunction,
              arg1Genl);
    }
    assertResultIsa(unaryFunction,
            resultIsa);
    if (resultGenl != null) {
      assertResultGenl(unaryFunction,
              resultGenl);
    }
    return unaryFunction;
  }
  
  @Override
  public Fort findOrCreateCollectionDenotingBinaryFunction(String binaryFunction,
          String comment,
          String commentMt,
          String arg1IsaName,
          String arg2IsaName,
          String arg1GenlsName,
          String arg2GenlsName,
          String resultIsa)
          throws CycConnectionException, CycApiException {
    Fort arg1Isa = null;
    Fort arg2Isa = null;
    final Fort arg1Genls;
    final Fort arg2Genls;
    if (arg1IsaName != null) {
      arg1Isa = getKnownConstantByName_inner(arg1IsaName);
    }
    if (arg2IsaName != null) {
      arg1Isa = getKnownConstantByName_inner(arg2IsaName); // FIXME: should this be arg2Isa? - nwinant, 2017-07-31
    }
    arg1Genls = (arg1GenlsName != null)
            ? getKnownConstantByName_inner(arg1GenlsName) 
            : null;
    arg2Genls = (arg2GenlsName != null) 
            ? getKnownConstantByName_inner(arg2GenlsName)
            : null;
    return describeCollectionDenotingBinaryFunction(
            findOrCreate_inner(binaryFunction),
            comment,
            getKnownConstantByName_inner(commentMt),
            arg1Isa,
            arg2Isa,
            arg1Genls,
            arg2Genls,
            getKnownConstantByName_inner(resultIsa));
  }
  
  @Override
  public Fort describeCollectionDenotingBinaryFunction(Fort binaryFunction,
          String comment,
          Fort commentMt,
          Fort arg1Isa,
          Fort arg2Isa,
          Fort arg1Genls,
          Fort arg2Genls,
          Fort resultIsa)
          throws CycConnectionException, CycApiException {
    assertComment(binaryFunction,
            comment,
            commentMt);
    // (#$isa binaryFunction #$BinaryFunction)
    assertIsa(binaryFunction,
            CommonConstants.BINARY_FUNCTION);
            //getKnownConstantByGuid_inner("c0e7247c-9c29-11b1-9dad-c379636f7270"));
    // (#$isa binaryFunction #$ReifiableFunction)
    assertIsa(binaryFunction,
            CommonConstants.REIFIABLE_FUNCTION);
            //getKnownConstantByGuid_inner("bd588002-9c29-11b1-9dad-c379636f7270"));
    // (#$isa binaryFunction #$CollectionDenotingFunction)
    assertIsa(binaryFunction,
            CommonConstants.COLLECTION_DENOTING_FUNCTION);
            //getKnownConstantByGuid_inner("bd58806a-9c29-11b1-9dad-c379636f7270"));
    // (#$isa binaryFunction #$Function-Denotational)
    assertIsa(binaryFunction,
            CommonConstants.FUNCTION_DENOTATIONAL);
            //getKnownConstantByGuid_inner("bd5c40b0-9c29-11b1-9dad-c379636f7270"));
    if (arg1Isa != null) {
      assertArgIsa(binaryFunction,
              1,
              arg1Isa);
    }
    if (arg2Isa != null) {
      assertArgIsa(binaryFunction,
              2,
              arg2Isa);
    }
    if (arg1Genls != null) {
      assertArg1Genl(binaryFunction,
              arg1Genls);
    }
    if (arg2Genls != null) {
      assertArg2Genl(binaryFunction,
              arg2Genls);
    }
    assertResultIsa(binaryFunction,
            resultIsa);
    return binaryFunction;
  }
  
  @Override
  public void ensureWffConstraints(String cycFort,
          String isaConstraintName,
          String genlsConstraintName)
          throws CycConnectionException, CycApiException {
    CycConstant cycConstant = getCyc().getLookupTool().find(cycFort);
    CycConstant isaConstraint = null;
    CycConstant genlsConstraint = null;

    if (isaConstraintName != null) {
      isaConstraint = getCyc().getLookupTool().find(isaConstraintName);
    }

    if (genlsConstraintName != null) {
      genlsConstraint = getCyc().getLookupTool().find(genlsConstraintName);
    }

    ensureWffConstraints(cycConstant, isaConstraint, genlsConstraint);
  }
  
  @Override
  public void ensureWffConstraints(Fort cycFort, Fort isaConstraint,
          Fort genlsConstraint)
          throws CycConnectionException, CycApiException {
    if ((isaConstraint != null)
            && (!getCyc().getInspectorTool().isa(cycFort, isaConstraint, CommonConstants.UNIVERSAL_VOCABULARY_MT))) {
      assertIsa(cycFort, isaConstraint);
    }

    if ((genlsConstraint != null)
            && (!getCyc().getInspectorTool().isSpecOf(cycFort, genlsConstraint, CommonConstants.UNIVERSAL_VOCABULARY_MT))) {
      assertGenls(cycFort, genlsConstraint);
    }
  }
  
  @Override
  public synchronized boolean merge(Fort killFort, Fort keepFort)
          throws CycConnectionException, CycApiException {
    final String command
            = "(ke-merge-now " + killFort.stringApiValue() + " " + keepFort.stringApiValue() + ")";
    final Object[] response = converse_inner(command);
    if (response[0].equals(Boolean.TRUE)) {
      return !response[1].equals(CycObjectFactory.nil);
    } else {
      throw new CycApiException("Failed to evaluate " + command + "\n  " + Arrays.asList(response));
    }
  }
  
  
  // Private
  
  /**
   * Asserts the given sentence with bookkeeping, and then places it on the transcript queue.
   *
   * @param sentence the given sentence for assertion
   * @param mt the microtheory in which the assertion is placed
   *
   * @throws CycConnectionException if cyc server host not found on the network or a data communication error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  private void assertWithTranscriptAndBookkeepingInternal(CycObject sentence,
          CycObject mt)
          throws CycConnectionException, CycApiException {
    assertSentence(sentence.stringApiValue(), makeElMt_inner(mt), null, null, true, true);
  }
}
