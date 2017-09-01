package com.cyc.kb.client;

/*
 * #%L
 * File: Constants.java
 * Project: KB Client
 * %%
 * Copyright (C) 2013 - 2017 Cycorp, Inc
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


import com.cyc.base.annotation.CycObjectLibrary;
import com.cyc.base.annotation.CycTerm;
import com.cyc.kb.BinaryPredicate;
import com.cyc.kb.Context;
import com.cyc.kb.FirstOrderCollection;
import com.cyc.kb.KbCollection;
import com.cyc.kb.KbFunction;
import com.cyc.kb.KbPredicate;
import com.cyc.kb.LogicalConnective;
import com.cyc.kb.exception.KbException;
import com.cyc.kb.exception.KbRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A convenience class for frequently accessed Cyc constants. It should be rare
 * to use this class in application making use of the KB API.
 * 
 * @author Vijay Raj
 * @version $Id: Constants.java 173072 2017-07-27 01:21:15Z nwinant $
 * @since 1.0
 */
@CycObjectLibrary(accessor="getInstance")
public class Constants {
  
  private static final Logger LOG = LoggerFactory.getLogger(Constants.class);
  private static Constants instance;
  
  //====|    Contexts (& related)    |============================================================//
  
  @CycTerm(cycl="#$DataMicrotheory")
  public final FirstOrderCollection DATA_MT = FirstOrderCollectionImpl.get("DataMicrotheory");
  
  @CycTerm(cycl="#$BaseKB")
  public final Context BASE_KB_CTX = ContextImpl.get("BaseKB");
  
  @CycTerm(cycl="#$UniversalVocabularyMt")
  public final Context UV_MT_CTX = ContextImpl.get("UniversalVocabularyMt");
  
  @CycTerm(cycl="#$InferencePSC")
  public final Context INFERENCE_PSC_CTX = ContextImpl.get("InferencePSC");
  
  @CycTerm(cycl="#$EverythingPSC")
  public final Context EVERYTHING_PSC_CTX = ContextImpl.get("EverythingPSC");
  
  //====|    KbPredicates & BinaryPredicates    |=================================================//
  
  @CycTerm(cycl="#$isa")
  public final BinaryPredicate ISA_PRED  = BinaryPredicateImpl.get("isa");
  
  @CycTerm(cycl="#$different")
  public final KbPredicate DIFFERENT = KbPredicateImpl.get("different");
  
  @CycTerm(cycl="#$genls")
  public final BinaryPredicate GENLS_PRED = BinaryPredicateImpl.get("genls");
  
  @CycTerm(cycl="#$genlMt")
  public final BinaryPredicate GENLMT_PRED = BinaryPredicateImpl.get("genlMt");
  
  @CycTerm(cycl="#$genlPreds")
  public final BinaryPredicate GENLPREDS_PRED = BinaryPredicateImpl.get("genlPreds");
  
  @CycTerm(cycl="#$genlInverse")
  public final KbPredicate GENLINVERSEPREDS_PRED = KbPredicateImpl.get("genlInverse");
  
  @CycTerm(cycl="#$quotedIsa")
  public final BinaryPredicate QUOTEDISA_PRED = BinaryPredicateImpl.get("quotedIsa");
  
  @CycTerm(cycl="#$argIsa")
  public final KbPredicate ARGISA_PRED = KbPredicateImpl.get("argIsa");
  
  @CycTerm(cycl="#$argGenl")
  public final KbPredicate ARGGENL_PRED = KbPredicateImpl.get("argGenl");
  
  @CycTerm(cycl="#$arity")
  public final BinaryPredicate ARITY_PRED = BinaryPredicateImpl.get("arity");
  
  @CycTerm(cycl="#$resultIsa")
  public final BinaryPredicate RESULTISA_PRED = BinaryPredicateImpl.get("resultIsa");
  
  @CycTerm(cycl="#$resultGenl")
  public final BinaryPredicate RESULTGENL_PRED = BinaryPredicateImpl.get("resultGenl");
  
  @CycTerm(cycl="#$mtMonad")
  public final KbPredicate MTMONAD_PRED = KbPredicateImpl.get("mtMonad");
  
  @CycTerm(cycl="#$mtTimeIndex")
  public final KbPredicate MTTIMEIDX_PRED = KbPredicateImpl.get("mtTimeIndex");
  
  @CycTerm(cycl="#$comment")
  public final BinaryPredicate COMMENT_PRED = BinaryPredicateImpl.get("comment");
  
  @CycTerm(cycl="#$interArgDifferent")
  public final KbPredicate INTER_ARG_DIFF_PRED = KbPredicateImpl.get("interArgDifferent");
  
  @CycTerm(cycl="#$assertedSentence")
  public final KbPredicate ASSERTED_SENT_PRED = KbPredicateImpl.get("assertedSentence");
  
  @CycTerm(cycl="#$checkSentence")
  public final KbPredicate CHECK_SENT_PRED = KbPredicateImpl.get("checkSentence");

  @CycTerm(cycl="#$unknownSentence")
  public final KbPredicate UNKNOWN_SENT_PRED = KbPredicateImpl.get("unknownSentence");
  
  //====|    KbFunctions    |=====================================================================//
  
  @CycTerm(cycl="#$Quote")
  public final KbFunction QUOTE_FUNC = KbFunctionImpl.get("Quote");
  
  @CycTerm(cycl="#$TheList")
  public final KbFunction THELIST_FUNC = KbFunctionImpl.get("TheList");
  
  @CycTerm(cycl="#$TheSet")
  public final KbFunction THESET_FUNC = KbFunctionImpl.get("TheSet");
  
  //====|    LogicalConnectives    |==============================================================//
  
  @CycTerm(cycl="#$not")
  public final LogicalConnective NOT_LC = LogicalConnectiveImpl.get("not");
  
  //====|    KbCollections    |===================================================================//
  
  @CycTerm(cycl="#$ReifiableFunction")
  public final KbCollection REIFIABLE_FUNC = KbCollectionImpl.get("ReifiableFunction");
  
  @CycTerm(cycl="#$VariableArityFunction")
  public final KbCollection VAR_ARITY_COL = KbCollectionImpl.get("VariableArityFunction");
  
  @CycTerm(cycl="#$VariableArityPredicate")
  public final KbCollection VAR_ARITY_PRED = KbCollectionImpl.get("VariableArityPredicate");

  @CycTerm(cycl="#$UnreifiableFunction")
  public final KbCollection UNREIFIABLE_FUNC_COL = KbCollectionImpl.get("UnreifiableFunction");
  
  // There is no way to get to these variables
  // Preferred way is to get them from their respective classes using getType or getClassType
  /*
  private final KBCollection THING_COL = new KBCollectionImpl("#$Thing");
  private final KBCollection INDIVIDUAL_COL = new KBCollectionImpl("#$Individual");
  private final KBCollection RELATION_COL = new KBCollectionImpl("#$Relation");
  private final KBCollection FUNCTION_COL = new KBCollectionImpl("#$Function-Denotational");
  private final KBCollection PREDICATE_COL = new KBCollectionImpl("#$Predicate");
  private final KBCollection BPRED_COL = new KBCollectionImpl("#$BinaryPredicate");
  private final KBCollection SCOPE_REL_COL = new KBCollectionImpl("#$ScopingRelation");
  private final KBCollection QUANTIFIER_COL = new KBCollectionImpl("#$Quantifier");
  private final KBCollection LOG_CON_COL = new KBCollectionImpl("#$LogicalConnective");
  private final KBCollection COLLECTION_COL = new KBCollectionImpl("#$Collection");
  private final KBCollection FIRST_ORD_COL = new KBCollectionImpl("#$FirstOrderCollection");
  private final KBCollection SECOND_ORD_COL = new KBCollectionImpl("#$SecondOrderCollection");
  private final KBCollection ASSERTION_COL = new KBCollectionImpl("#$CycLAssertion");
  private final KBCollection GAF_COL = new KBCollectionImpl("#$CycLGAFAssertion");
  private final KBCollection VARIABLE_COL = new KBCollectionImpl("#$CycLVariable");
  private final KBCollection SYMBOL_COL = new KBCollectionImpl("#$CycLSubLSymbol");
  */
  
  private Constants() throws KbException {
    super();
  }

  /**
   * This not part of the public, supported KB API
   * 
   * @return a instance of Constants class which instantiates the following commonly used
   * KB terms.
   * 
   * Contexts:
   * #$UniversalVocabularyMt
   * #$BaseKB
   * #$EverythingPSC
   * #$InferencePSC
   * 
   * Predicates
   * #$isa
   * #$genls
   * #$genlMt
   * #$genlPreds
   * #$quotedIsa
   * 
   * @throws KbRuntimeException
   */
  public static Constants getInstance() throws KbRuntimeException {
    try {
      if (instance == null) {
        LOG.info("Instantiating...");
        instance = new Constants();
        LOG.info("... Instantiated.");
      }
      return instance;
    } catch (KbException e) {
      final String msg
              = "One of the private final fields in " + Constants.class.getCanonicalName()
              + " could not be instantiated, cannot proceed further.";
      LOG.error(msg, e);
      throw new KbRuntimeException(msg, e);
    }
  }
  
  //====|    Main contexts    |===================================================================//
  
  /**
   * @return FirstOrderCollectionImpl.get("DataMicrotheory")
   */
  public static FirstOrderCollection dataMt() {
    return getInstance().DATA_MT;
  }

  /**
   * @return ContextImpl.get("BaseKB")
   */
  public static Context baseKbMt() {
    return getInstance().BASE_KB_CTX;
  }

  /**
   * @return ContextImpl.get("EverythingPSC")
   */
  public static Context everythingPSCMt() {
    return getInstance().EVERYTHING_PSC_CTX;
  }

  /**
   * @return ContextImpl.get("InferencePSC")
   */
  public static Context inferencePSCMt() {
    return getInstance().INFERENCE_PSC_CTX;
  }

  /**
   * @return ContextImpl.get("UniversalVocabularyMt")
   */
  public static Context uvMt() {
    return getInstance().UV_MT_CTX;
  }

  ///====|    Main predicates    |================================================================//
  
  /**
   * @return KbPredicateImpl.get("isa")
   */
  public static BinaryPredicate isa() {
    return getInstance().ISA_PRED;
  }
  
  /**
   * @return KbPredicateImpl.get("different")
   */
  public static KbPredicate different() {
    return getInstance().DIFFERENT;
  }
  
  /**
   * @return KbPredicateImpl.get("genls")
   */
  public static BinaryPredicate genls() {
    return getInstance().GENLS_PRED;
  }
  
  /**
   * @return KbPredicateImpl.get("genlMt")
   */
  public static BinaryPredicate genlMt() {
    return getInstance().GENLMT_PRED;
  }

  /**
   * @return KbPredicateImpl.get("genlPreds")
   */
  public static BinaryPredicate genlPreds() {
    return getInstance().GENLPREDS_PRED;
  }
  
  /**
   * @return BinaryPredicateImpl.get("quotedIsa")
   */
  public static BinaryPredicate quotedIsa() {
    return getInstance().QUOTEDISA_PRED;
  }
  
  /**
   * @return KbPredicateImpl.get("argIsa")
   */
  public static KbPredicate argIsa() {
    return getInstance().ARGISA_PRED;
  }
  
  /**
   * @return KbPredicateImpl.get("argGenl")
   */
  public static KbPredicate argGenl() {
    return getInstance().ARGGENL_PRED;
  }

  /**
   * @return BinaryPredicateImpl.get("arity")
   */
  public static BinaryPredicate arity() {
    return getInstance().ARITY_PRED;
  }

  /**
   * @return KbPredicateImpl.get("resultIsa")
   */
  public static BinaryPredicate resultIsa() {
    return getInstance().RESULTISA_PRED;
  }

  /**
   * @return KbPredicateImpl.get("resultGenl")
   */
  public static BinaryPredicate resultGenl() {
    return getInstance().RESULTGENL_PRED;
  }

  /**
   * @return KbPredicateImpl.get("resultGenl")
   */
  public static KbPredicate mtMonad() {
    return getInstance().MTMONAD_PRED;
  }

  /**
   * @return KbPredicateImpl.get("resultGenl")
   */
  public static KbPredicate mtTimeIndex() {
    return getInstance().MTTIMEIDX_PRED;
  }
  
  ///====|    Main collections    |===============================================================//
  
}
