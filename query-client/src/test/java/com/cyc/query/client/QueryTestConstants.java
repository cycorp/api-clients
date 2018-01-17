package com.cyc.query.client;

/*
 * #%L
 * File: QueryTestConstants.java
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
import com.cyc.Cyc;
import com.cyc.base.annotation.CycObjectLibrary;
import com.cyc.base.annotation.CycTerm;
import com.cyc.baseclient.CommonConstants;
import com.cyc.kb.Context;
import com.cyc.kb.KbCollection;
import com.cyc.kb.KbIndividual;
import com.cyc.kb.KbPredicate;
import com.cyc.kb.KbTerm;
import com.cyc.kb.Sentence;
import com.cyc.kb.Variable;
import com.cyc.kb.client.BinaryPredicateImpl;
import com.cyc.kb.exception.CreateException;
import com.cyc.kb.exception.KbException;
import com.cyc.kb.exception.KbRuntimeException;
import com.cyc.kb.exception.KbTypeException;

import static com.cyc.Cyc.Constants.GENLS;
import static com.cyc.Cyc.Constants.ISA;

/**
 *
 * @author baxter
 */
@CycObjectLibrary(accessor = "getInstance")
public class QueryTestConstants {

  public static synchronized QueryTestConstants getInstance() throws KbRuntimeException {
    try {
      if (instance == null) {
        instance = new QueryTestConstants();
      }
    } catch (KbException e) {
      throw new KbRuntimeException(
              "Once of the private final fields in com.cyc.query.QueryApiTestConstants could not be instantiated, can not proceed further.",
              e);
    }
    return instance;
  }

  @CycTerm(cycl = "#$Bird")
  private final KbCollection bird = KbCollection.get("Bird");

  @CycTerm(cycl = "#$Emu")
  private final KbCollection emu = KbCollection.get("Emu");

  @CycTerm(cycl = "#$Zebra")
  private final KbCollection zebra = KbCollection.get("Zebra");

  @CycTerm(cycl = "#$GeneralCycKETask-Allotment")
  public final KbIndividual generalCycKE = KbIndividual.get("GeneralCycKETask-Allotment");

  @CycTerm(cycl = "#$AbrahamLincoln")
  public final KbIndividual abrahamLincoln = KbIndividual.get("AbrahamLincoln");

  @CycTerm(cycl = "#$Plant")
  public final KbCollection plant = KbCollection.get("Plant");

  @CycTerm(cycl = "#$Animal")
  private final KbCollection animal = KbCollection.get("Animal");

  // (#$TheFn #$Animal) isn't in the OCyc 5.0 KB, so we add it if necessary - nwinant, 2015-04-17
  @CycTerm(cycl = "(#$TheFn #$Animal)")
  public final KbIndividual theAnimal = KbIndividual.findOrCreate("(TheFn Animal)");

  public final Sentence theAnimalIsAnAnimal = Sentence.get(ISA, theAnimal, animal);

  public final Sentence xIsAnAnimal = Sentence.get(ISA, Variable.get("X"), animal);

  @CycTerm(cycl = "#$indexicalReferent")
  public final KbPredicate indexicalReferent = BinaryPredicateImpl.get("indexicalReferent");

  @CycTerm(cycl = "#$Now-Indexical")
  public final KbIndividual nowIndexical = KbIndividual.get("Now-Indexical");
  public final Sentence whatTimeIsIt
          = Sentence.get(indexicalReferent, nowIndexical, Variable.get("NOW"));

  public final Sentence yOwnsX = Sentence.get(BinaryPredicateImpl.get("owns"),
          Variable.get("Y"), Variable.get("X"));

  @CycTerm(cycl = "#$cellHasNumberOfChromosomes")
  public final KbPredicate cellHasNumberOfChromosomes = KbPredicate.get("cellHasNumberOfChromosomes");

  @CycTerm(cycl = "#$assertionSentence")
  public final KbPredicate assertionSentence = KbPredicate.get("assertionSentence");

  @CycTerm(cycl = "#$testQuerySpecification")
  public final KbPredicate testQuerySpecification = KbPredicate.get("testQuerySpecification");

  @CycTerm(cycl = "#$testAnswers-Cardinality-Exact")
  public final KbPredicate testAnswersCardinalityExact = KbPredicate.get(
          "testAnswers-Cardinality-Exact");

  @CycTerm(cycl = "#$testAnswers-Cardinality-Min")
  public final KbPredicate testAnswersCardinalityMin = KbPredicate.get(
          "testAnswers-Cardinality-Min");

  @CycTerm(cycl = "#$testAnswersCycL-Exact")
  public final KbPredicate testAnswersCycLExact = KbPredicate.get("testAnswersCycL-Exact");

  @CycTerm(cycl = "#$testAnswersCycL-Wanted")
  public final KbPredicate testAnswersCycLWanted = KbPredicate.get("testAnswersCycL-Wanted");

  @CycTerm(cycl = "#$equals")
  public KbPredicate cycEquals = KbPredicate.get("equals");

  @CycTerm(cycl = "#$comment")
  public KbPredicate comment = KbPredicate.get("comment");

  @CycTerm(cycl = "#$evaluate")
  public KbPredicate evaluate = KbPredicate.get("evaluate");

  @CycTerm(cycl = "#$academyAwardWinner")
  public KbPredicate academyAwardWinner = KbPredicate.get("academyAwardWinner");

  @CycTerm(cycl = "#$True")
  public KbIndividual cycTrue = KbIndividual.get("True");

  @CycTerm(cycl = "(#$DifferenceFn 3 1)")
  public KbIndividual threeMinusOne = KbIndividual.get("(#$DifferenceFn 3 1)");

  @CycTerm(cycl = "(#$DifferenceFn 1 0.5)")
  public KbIndividual oneMinusPointFive = KbIndividual.get("(#$DifferenceFn 1 0.5)");

  @CycTerm(cycl = "#$Thing")
  public KbCollection thing = KbCollection.get("Thing");

  @CycTerm(cycl = "#$BillClinton")
  public KbIndividual billClinton = KbIndividual.get("BillClinton");

  @CycTerm(cycl = "(#$AssistedReaderSourceSpindleCollectorForTaskFn #$GeneralCycKETask-Allotment)")
  public Context generalCycKECollector = Context.get(
          "(AssistedReaderSourceSpindleCollectorForTaskFn GeneralCycKETask-Allotment)");

  public final Sentence genlsEmuBird = Sentence.get(GENLS, emu, bird);
  public final Sentence genlsAnimalX = Sentence.get(GENLS, animal, Variable.get("X"));
  public final Sentence academyAwardWinners = Sentence.get(
          academyAwardWinner, Variable.get("X"), Variable.get("Y"), Variable.get("Z"));
  public final Sentence genlsSpecGenls = Cyc.getSentenceService()
          .and(Sentence.get(GENLS, Variable.get("?SPEC"), Variable.get("?GENLS")),
          Sentence.get(GENLS, animal, Variable.get("?SPEC")),
          Sentence.get(GENLS, Variable.get("?SPEC"), Variable.get("?GENLS")));
  
  public final Sentence whatIsAbe = Sentence.get(ISA, abrahamLincoln, Variable.get("TYPE"));

  public final Sentence queryAnimals = Sentence.get(
          CommonConstants.ELEMENT_OF, Variable.get("N"),
          KbTerm.get("(TheSet Emu Zebra)"));

  @CycTerm(cycl="#$GeneralCycKETask-Allotment")
  public final KbIndividual GENERAL_CYC_KE_TASK_ALLOTMENT = KbIndividual.get("GeneralCycKETask-Allotment");
  
  
  @CycTerm(cycl="#$cellHasNumberOfChromosomes")
  public final KbPredicate CELL_HAS_NUMBER_OF_CHROMOSOMES =  KbPredicate.get("cellHasNumberOfChromosomes");
  private static QueryTestConstants instance = null;

  @CycTerm(cycl = "#$EnglishParaphraseMt")
  public Context englishParaphraseMt = Context.get("EnglishParaphraseMt");

  @CycTerm(cycl = "#$PeopleDataMt")
  public Context peopleDataMt = Context.get("PeopleDataMt");

  private QueryTestConstants() throws KbTypeException, CreateException, KbException {
    super();
  }

  public static KbCollection bird() throws KbException {
    return getInstance().bird;
  }

  public static KbCollection emu() throws KbException {
    return getInstance().emu;
  }

  public static KbCollection zebra() throws KbException {
    return getInstance().zebra;
  }

  public static KbCollection animal() throws KbException {
    return getInstance().animal;
  }

}
