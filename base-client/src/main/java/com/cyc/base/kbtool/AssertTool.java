package com.cyc.base.kbtool;

/*
 * #%L
 * File: AssertTool.java
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
import java.util.List;

/**
 * Tools for asserting facts to the Cyc KB. To unassert facts from the Cyc KB, 
 * use the {@link com.cyc.base.kbtool.UnassertTool}. To perform simple tasks,
 * like constant creation or renaming, use the {@link com.cyc.base.kbtool.ObjectTool}.
 * 
 * @see com.cyc.base.kbtool.UnassertTool
 * @see com.cyc.base.kbtool.ObjectTool
 * @author nwinant
 */
public interface AssertTool {
  
  /**
   * Assert an argument contraint for the given relation and argument position. The operation will
   * be added to the KB transcript for replication and archive.
   *
   * @param relation the given relation
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  void assertArg1FormatSingleEntry(Fort relation) throws CycConnectionException, CycApiException;

  /**
   * Assert an argument one genls contraint for the given relation. The operation will be added to
   * the KB transcript for replication and archive.
   *
   * @param relation the given relation
   * @param argGenl the argument constraint
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  void assertArg1Genl(Fort relation, Fort argGenl) throws CycConnectionException, CycApiException;

  /**
   * Assert an argument two genls contraint for the given relation. The operation will be added to
   * the KB transcript for replication and archive.
   *
   * @param relation the given relation
   * @param argGenl the argument constraint
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  void assertArg2Genl(Fort relation, Fort argGenl) throws CycConnectionException, CycApiException;

  /**
   * Assert an argument three genls contraint for the given relation. The operation will be added
   * to the KB transcript for replication and archive.
   *
   * @param relation the given relation
   * @param argGenl the argument constraint
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  void assertArg3Genl(Fort relation, Fort argGenl) throws CycConnectionException, CycApiException;

  /**
   * Assert an argument format contraint for the given relation and argument position. The
   * operation will be added to the KB transcript for replication and archive.
   *
   * @param relation the given relation
   * @param argPosition the given argument position
   * @param argNFormat the argument format constraint
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  void assertArgFormat(Fort relation, int argPosition, Fort argNFormat) 
          throws CycConnectionException, CycApiException;

  /**
   * Assert an argument isa contraint for the given relation and argument position. The operation
   * will be added to the KB transcript for replication and archive.
   *
   * @param relation the given relation
   * @param argPosition the given argument position
   * @param argNIsa the argument constraint
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  void assertArgIsa(Fort relation, int argPosition, Fort argNIsa)
          throws CycConnectionException, CycApiException;

  /**
   * Assert a comment for the specified CycConstant in the specified microtheory MT. The operation
   * will be added to the KB transcript for replication and archive.
   *
   * @param cycConstantName the name of the given term
   * @param comment the comment string
   * @param mtName the name of the microtheory in which the comment is asserted
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  void assertComment(String cycConstantName, String comment, String mtName)
          throws CycConnectionException, CycApiException;

  /**
   * Assert a comment for the specified Fort in the specified microtheory. The operation will be
   * added to the KB transcript for replication and archive.
   *
   * @param cycFort the given term
   * @param comment the comment string
   * @param mt the comment assertion microtheory
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  void assertComment(Fort cycFort, String comment, CycObject mt)
          throws CycConnectionException, CycApiException;


  /**
   * Asserts that the given term is defined in the given mt. If the mt is
   * subsequently killed, then the truth maintenance kills the dependent term.
   *
   * @param dependentTerm the dependent term
   * @param mt the defining microtheory
   * 
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the Cyc server returns an error
   */
  void assertDefiningMt(Fort dependentTerm, Fort mt)
          throws CycConnectionException, CycApiException;

  /**
   * Asserts a ground atomic formula (gaf) in the specified microtheory MT. The operation will be
   * added to the KB transcript for replication and archive.
   *
   * @param mt the microtheory in which the assertion is made
   * @param predicate the binary predicate of the assertion
   * @param arg1 the first argument of the predicate
   * @param arg2 the second argument of the predicate
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  void assertGaf(
          CycObject mt, DenotationalTerm predicate, DenotationalTerm arg1, DenotationalTerm arg2)
          throws CycConnectionException, CycApiException;

  /**
   * Asserts a ground atomic formula (gaf) in the specified microtheory MT. The operation will be
   * added to the KB transcript for replication and archive.
   *
   * @param mt the microtheory in which the assertion is made
   * @param predicate the binary predicate of the assertion
   * @param arg1 the first argument of the predicate
   * @param arg2 the second argument of the predicate, which is a string
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  void assertGaf(CycObject mt, Fort predicate, Fort arg1, String arg2)
          throws CycConnectionException, CycApiException;

  /**
   * Asserts a ground atomic formula (gaf) in the specified microtheory MT. The operation will be
   * added to the KB transcript for replication and archive.
   *
   * @param mt the microtheory in which the assertion is made
   * @param predicate the binary predicate of the assertion
   * @param arg1 the first argument of the predicate
   * @param arg2 the second argument of the predicate, which is a CycList
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  void assertGaf(CycObject mt, CycConstant predicate, Fort arg1, CycList arg2)
          throws CycConnectionException, CycApiException;

  /**
   * Asserts a ground atomic formula (gaf) in the specified microtheory MT. The operation will be
   * added to the KB transcript for replication and archive.
   *
   * @param mt the microtheory in which the assertion is made
   * @param predicate the binary predicate of the assertion
   * @param arg1 the first argument of the predicate
   * @param arg2 the second argument of the predicate, which is an int
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  void assertGaf(CycObject mt, CycConstant predicate, Fort arg1, int arg2)
          throws CycConnectionException, CycApiException;

  /**
   * Asserts a ground atomic formula (gaf) in the specified microtheory. The operation will be
   * added to the KB transcript for replication and archive.
   *
   * @param mt the microtheory in which the assertion is made
   * @param predicate the binary predicate of the assertion
   * @param arg1 the first argument of the predicate
   * @param arg2 the second argument of the predicate, which is an Integer
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  void assertGaf(CycObject mt, Fort predicate, Fort arg1, Integer arg2)
          throws CycConnectionException, CycApiException;

  /**
   * Asserts a ground atomic formula (gaf) in the specified microtheory. The operation will be
   * added to the KB transcript for replication and archive.
   *
   * @param mt the microtheory in which the assertion is made
   * @param predicate the binary predicate of the assertion
   * @param arg1 the first argument of the predicate
   * @param arg2 the second argument of the predicate, which is a Double
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  void assertGaf(CycObject mt, Fort predicate, Fort arg1, Double arg2)
          throws CycConnectionException, CycApiException;

  /**
   * Asserts a ground atomic formula (gaf) in the specified microtheory MT. The operation and its
   * bookkeeping info will be added to the KB transcript for replication and archive.
   *
   * @param mt the microtheory in which the assertion is made
   * @param predicate the ternary predicate of the assertion
   * @param arg1 the first argument of the predicate
   * @param arg2 the second argument of the predicate
   * @param arg3 the third argument of the predicate
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  void assertGaf(CycObject mt, CycConstant predicate, Fort arg1, Fort arg2, Fort arg3)
          throws CycConnectionException, CycApiException;

  /**
   * Asserts a ground atomic formula (gaf) in the specified microtheory MT. The operation will be
   * added to the KB transcript for replication and archive.
   *
   * @param gaf the gaf in the form of a CycList
   * @param mt the microtheory in which the assertion is made
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  void assertGaf(CycList gaf, CycObject mt) throws CycConnectionException, CycApiException;

  /**
   * Asserts a ground atomic formula (gaf) in the specified microtheory MT. The operation will be
   * added to the KB transcript for replication and archive.
   *
   * @param gaf the gaf in the form of a FormulaSentence
   * @param mt the microtheory in which the assertion is made
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  void assertGaf(FormulaSentence gaf, CycObject mt) throws CycConnectionException, CycApiException;
  
  /**
   * Assert that the more general micortheory is a genlMt of the more specialized microtheory,
   * asserted in the UniversalVocabularyMt The operation will be added to the KB transcript for
   * replication and archive.
   *
   * @param specMtName the name of the more specialized microtheory
   * @param genlMtName the name of the more generalized microtheory
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  void assertGenlMt(String specMtName, String genlMtName)
          throws CycConnectionException, CycApiException;

  /**
   * Assert that the more general microtheory is a genlMt of the more specialized microtheory,
   * asserted in the UniversalVocabularyMt. The operation will be added to the KB transcript for
   * replication and archive.
   *
   * @param specMt the more specialized microtheory
   * @param genlMt the more generalized microtheory
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  void assertGenlMt(Fort specMt, Fort genlMt) throws CycConnectionException, CycApiException;

  /**
   * Assert that the more general predicate is a genlPreds of the more specialized predicate,
   * asserted in the UniversalVocabularyMt. The operation will be added to the KB transcript for
   * replication and archive.
   *
   * @param specPredName the name of the more specialized predicate
   * @param genlPredName the name of the more generalized predicate
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  void assertGenlPreds(String specPredName, String genlPredName)
          throws CycConnectionException, CycApiException;

  /**
   * Assert that the more general predicate is a genlPreds of the more specialized predicate,
   * asserted in the UniversalVocabularyMt The operation will be added to the KB transcript for
   * replication and archive.
   *
   * @param specPred the more specialized predicate
   * @param genlPred the more generalized predicate
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  void assertGenlPreds(Fort specPred, Fort genlPred) throws CycConnectionException, CycApiException;

  /**
   * Assert that the genlsCollection is a genls of specCollection, in the specified defining
   * microtheory MT. The operation will be added to the KB transcript for replication and archive.
   *
   * @param specCollectionName the name of the more specialized collection
   * @param genlsCollectionName the name of the more generalized collection
   * @param mtName the assertion microtheory name
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  void assertGenls(String specCollectionName, String genlsCollectionName, String mtName) 
          throws CycConnectionException, CycApiException;

  /**
   * Assert that the genlsCollection is a genls of specCollection, in the UniversalVocabularyMt. The
   * operation will be added to the KB transcript for replication and archive.
   *
   * @param specCollectionName the name of the more specialized collection
   * @param genlsCollectionName the name of the more generalized collection
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  void assertGenls(String specCollectionName, String genlsCollectionName) 
          throws CycConnectionException, CycApiException;

  /**
   * Assert that the genlsCollection is a genls of specCollection, in the UniveralVocabularyMt. The
   * operation will be added to the KB transcript for replication and archive.
   *
   * @param specCollection the more specialized collection
   * @param genlsCollection the more generalized collection
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  void assertGenls(Fort specCollection, Fort genlsCollection)
          throws CycConnectionException, CycApiException;

  /**
   * Assert that the genlsCollection is a genls of specCollection, in the specified defining
   * microtheory MT. The operation will be added to the KB transcript for replication and archive.
   *
   * @param specCollection the more specialized collection
   * @param genlsCollection the more generalized collection
   * @param mt the assertion microtheory
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  void assertGenls(Fort specCollection, Fort genlsCollection, CycObject mt)
          throws CycConnectionException, CycApiException;

  /**
   * Assert that the cycFort is a collection in the UniversalVocabularyMt. The operation will be
   * added to the KB transcript for replication and archive.
   *
   * @param cycFortName the collection element name
   * @param collectionName the collection name
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  void assertIsa(String cycFortName, String collectionName)
          throws CycConnectionException, CycApiException;

  /**
   * Assert that the cycFort is a collection, in the specified defining microtheory MT. The
   * operation will be added to the KB transcript for replication and archive.
   *
   * @param cycFortName the collection element name
   * @param collectionName the collection name
   * @param mtName the assertion microtheory name
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  void assertIsa(String cycFortName, String collectionName, String mtName) 
          throws CycConnectionException, CycApiException;

  /**
   * Assert that the cycFort is a collection, in the specified defining microtheory MT. The
   * operation will be added to the KB transcript for replication and archive.
   *
   * @param cycFort the collection element
   * @param aCollection the collection
   * @param mt the assertion microtheory
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  void assertIsa(Fort cycFort, Fort aCollection, CycObject mt)
          throws CycConnectionException, CycApiException;

  /**
   * Assert that the cycFort is a collection, in the UniversalVocabularyMt. The operation will be
   * added to the KB transcript for replication and archive.
   *
   * @param instance the collection element
   * @param aCollection the collection
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  void assertIsa(DenotationalTerm instance, DenotationalTerm aCollection)
          throws CycConnectionException, CycApiException;

  /**
   * Assert that the specified CycConstant is a #$BinaryPredicate in the specified defining
   * microtheory. The operation will be added to the KB transcript for replication and archive.
   *
   * @param cycFort the given term
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  void assertIsaBinaryPredicate(Fort cycFort) throws CycConnectionException, CycApiException;

  /**
   * Assert that the specified CycConstant is a #$BinaryPredicate in the specified defining
   * microtheory. The operation will be added to the KB transcript for replication and archive.
   *
   * @param cycFort the given term
   * @param mt the defining microtheory
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  void assertIsaBinaryPredicate(Fort cycFort, CycObject mt) 
          throws CycConnectionException, CycApiException;

  /**
   * Assert that the specified CycFort is a collection in the UniversalVocabularyMt. The
   * operation will be added to the KB transcript for replication and archive.
   *
   * @param cycFort the given collection term
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  void assertIsaCollection(Fort cycFort) throws CycConnectionException, CycApiException;

  /**
   * Assert that the specified CycFort is a collection in the specified defining microtheory.
   * The operation will be added to the KB transcript for replication and archive.
   *
   * @param cycFort the given collection term
   * @param mt the assertion microtheory
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  void assertIsaCollection(Fort cycFort, CycObject mt)
          throws CycConnectionException, CycApiException;

  /**
   * Asserts each of the given list of forts to be instances of the given collection in the
   * UniversalVocabularyMt
   *
   * @param fortNames the list of forts
   * @param collectionName
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the Cyc server returns an error
   */
  void assertIsas(List fortNames, String collectionName)
          throws CycConnectionException, CycApiException;

  /**
   * Asserts each of the given list of forts to be instances of the given collection in the
   * UniversalVocabularyMt
   *
   * @param forts the list of forts
   * @param collection
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the Cyc server returns an error
   */
  void assertIsas(List forts, Fort collection) throws CycConnectionException, CycApiException;

  /**
   * Assert a nameString for the specified CycConstant in the specified lexical microtheory. The
   * operation will be added to the KB transcript for replication and archive.
   *
   * @param cycConstantName the name of the given term
   * @param nameString the given name string for the term
   * @param mtName the name of the microtheory in which the name string is asserted
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  void assertNameString(String cycConstantName, String nameString, String mtName)
          throws CycConnectionException, CycApiException;

  /**
   * Assert a name string for the specified Fort in the specified microtheory. The operation
   * will be added to the KB transcript for replication and archive.
   *
   * @param cycFort the given term
   * @param nameString the name string
   * @param mt the name string assertion microtheory
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  void assertNameString(Fort cycFort, String nameString, CycObject mt) 
          throws CycConnectionException, CycApiException;

  /**
   * Assert that the cycFort term itself is a collection, in the given mt. The operation will be
   * added to the KB transcript for replication and archive.
   *
   * @param cycFort the collection element
   * @param aCollection the collection
   * @param mt the assertion microtheory
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  void assertQuotedIsa(Fort cycFort, Fort aCollection, CycObject mt)
          throws CycConnectionException, CycApiException;

  /**
   * Assert the genls result contraint for the given denotational function. The operation will be
   * added to the KB transcript for replication and archive.
   *
   * @param denotationalFunction the given denotational function
   * @param resultGenl the function's genls result constraint
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  void assertResultGenl(Fort denotationalFunction, Fort resultGenl)
          throws CycConnectionException, CycApiException;

  /**
   * Assert the isa result contraint for the given denotational function. The operation will be
   * added to the KB transcript for replication and archive.
   *
   * @param denotationalFunction the given denotational function
   * @param resultIsa the function's isa result constraint
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  void assertResultIsa(Fort denotationalFunction, Fort resultIsa)
          throws CycConnectionException, CycApiException;
  
  /**
   * Assert a sentence to Cyc.
   * 
   * @see #assertSentence(java.lang.String, com.cyc.base.cycobject.ElMt, java.lang.String, java.lang.String, boolean, boolean, boolean, java.util.List) 
   * 
   * @param sentence
   * @param mt
   * @param bookkeeping
   * @param transcript
   * 
   * @return the resulting assertion(s) (Note special cases in the method documentation.)
   * 
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  public CycList<CycAssertion> assertSentence(
          String sentence,
          ElMt mt, 
          boolean bookkeeping, 
          boolean transcript) throws CycConnectionException, CycApiException;
  
  /**
   * Assert a sentence to Cyc.
   * 
   * @see #assertSentence(java.lang.String, com.cyc.base.cycobject.ElMt, java.lang.String, java.lang.String, boolean, boolean, boolean, java.util.List) 
   * 
   * @param sentence
   * @param mt
   * @param strength
   * @param direction
   * @param bookkeeping
   * @param transcript
   * 
   * @return the resulting assertion(s) (Note special cases in the method documentation.)
   * 
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  public CycList<CycAssertion> assertSentence(
          String sentence, 
          ElMt mt,
          String strength, 
          String direction,
          boolean bookkeeping,
          boolean transcript) throws CycConnectionException, CycApiException;
  
  /**
   * Assert a sentence to Cyc.
   * 
   * @see #assertSentence(java.lang.String, com.cyc.base.cycobject.ElMt, java.lang.String, java.lang.String, boolean, boolean, boolean, java.util.List) 
   * 
   * @param sentence
   * @param mt
   * @param strength
   * @param direction
   * @param bookkeeping
   * @param transcript
   * @param template
   * 
   * @return the resulting assertion(s) (Note special cases in the method documentation.)
   * 
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  public CycList<CycAssertion> assertSentence(
          String sentence, 
          ElMt mt,
          String strength, 
          String direction,
          boolean bookkeeping,
          boolean transcript,
          Fort template) throws CycConnectionException, CycApiException;

  /**
   * Assert a sentence to Cyc.
   * 
   * @see #assertSentence(java.lang.String, com.cyc.base.cycobject.ElMt, java.lang.String, java.lang.String, boolean, boolean, boolean, java.util.List) 
   * 
   * @param sentence
   * @param mt
   * @param bookkeeping
   * @param transcript
   * @param templates
   * 
   * @return the resulting assertion(s) (Note special cases in the method documentation.)
   * 
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  public CycList<CycAssertion> assertSentence(
          String sentence, 
          ElMt mt, 
          boolean bookkeeping,
          boolean transcript,
          List<Fort> templates) throws CycConnectionException, CycApiException;
  
  /**
   * Assert a sentence to Cyc. If successful, this will normally return a CycList with the resulting
   * assertion(s), but there are some special cases:
   * 
   * <p>It's possible for an assert to meaningfully succeed without generating an actual assertion 
   * object; e.g., when the asserted knowledge is stored to a remote database via SKSI, there's no 
   * assertion object in the KB, but the operation was still meaningfully successful. In such cases,
   * an empty list will be returned.
   * 
   * <p>If the assert is not <em>immediately</em> performed, but is instead noted for a KB 
   * transaction, <code>null</code> will be returned. For more details, see 
   * {@link com.cyc.baseclient.KbTransaction#noteForAssertion(java.lang.String, com.cyc.base.cycobject.ElMt, boolean, boolean, boolean, java.util.List) }.
   * 
   * <p>If the assert fails, a CycApiException will be thrown.
   * 
   * @param sentence
   * @param mt
   * @param strength
   * @param direction
   * @param bookkeeping
   * @param transcript
   * @param disableWFFChecking
   * @param templates
   * 
   * @return the resulting assertion(s) (Note special cases in the method documentation.)
   * 
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  public CycList<CycAssertion> assertSentence(
          String sentence, 
          ElMt mt, 
          String strength, 
          String direction, 
          boolean bookkeeping,
          boolean transcript, 
          boolean disableWFFChecking,
          List<Fort> templates) throws CycConnectionException, CycApiException;
  
  /**
   * Asserts that the given term is mapped to the given Cyc term.
   *
   * @param cycTerm the mapped Cyc term
   * @param informationSource the external indexed information source
   * @param externalConcept the external concept within the information source
   * @param mt the assertion microtheory
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the Cyc server returns an error
   */
  void assertSynonymousExternalConcept(
          String cycTerm, String informationSource, String externalConcept, String mt) 
          throws CycConnectionException, CycApiException;

  /**
   * Asserts that the given term is mapped to the given Cyc term.
   *
   * @param cycTerm the mapped Cyc term
   * @param informationSource the external indexed information source
   * @param externalConcept the external concept within the information source
   * @param mt the assertion microtheory
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the Cyc server returns an error
   */
  void assertSynonymousExternalConcept(
          Fort cycTerm, Fort informationSource, String externalConcept, CycObject mt)
          throws CycConnectionException, CycApiException;

  /**
   * Asserts that the given term is dependent upon the given independent term. If the latter is
   * killed, then the truth maintenance kills the dependent term.
   *
   * @param dependentTerm the dependent term
   * @param independentTerm the independent term
   * @param mt the assertion microtheory
   * 
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the Cyc server returns an error
   */
  void assertTermDependsOn(Fort dependentTerm, Fort independentTerm, Fort mt) 
          throws CycConnectionException, CycApiException;

  /**
   * Asserts the given sentence with bookkeeping and without placing it on the transcript queue.
   *
   * @param sentence the given sentence for assertion
   * @param mt the microtheory in which the assertion is placed
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  void assertWithBookkeepingAndWithoutTranscript(CycList sentence, CycObject mt) 
          throws CycConnectionException, CycApiException;

  /**
   * Asserts the given sentence with bookkeeping and without placing it on the transcript queue.
   *
   * @param sentence the given sentence for assertion
   * @param mt the microtheory in which the assertion is placed
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  void assertWithBookkeepingAndWithoutTranscript(String sentence, CycObject mt) 
          throws CycConnectionException, CycApiException;

  /**
   * Asserts the given sentence, and then places it on the transcript queue.
   *
   * @param sentence the given sentence for assertion
   * @param mt the microtheory in which the assertion is placed
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  void assertWithTranscript(CycList sentence, CycObject mt)
          throws CycConnectionException, CycApiException;

  /**
   * Asserts the given sentence, and then places it on the transcript queue.
   *
   * @param sentence the given sentence for assertion
   * @param mt the microtheory in which the assertion is placed
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  void assertWithTranscript(String sentence, CycObject mt)
          throws CycConnectionException, CycApiException;

  /**
   * Asserts the given sentence with bookkeeping, and then places it on the transcript queue.
   *
   * @param sentence the given sentence for assertion
   * @param mt the microtheory in which the assertion is placed
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  void assertWithTranscriptAndBookkeeping(String sentence, CycObject mt)
          throws CycConnectionException, CycApiException;

  /**
   * Asserts the given sentence with bookkeeping, and then places it on the transcript queue.
   *
   * @param sentence the given sentence for assertion
   * @param mt the microtheory in which the assertion is placed
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  void assertWithTranscriptAndBookkeeping(FormulaSentence sentence, CycObject mt) 
          throws CycConnectionException, CycApiException;

  /**
   * Asserts the given sentence with bookkeeping, and then places it on the transcript queue.
   *
   * @param sentence the given sentence for assertion
   * @param mt the microtheory in which the assertion is placed
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  void assertWithTranscriptAndBookkeeping(CycList sentence, CycObject mt) 
          throws CycConnectionException, CycApiException;
  
  /**
   * Finds or creates a new binary predicate term.
   *
   * @param predicateName the name of the binary predicate
   * @param predicateTypeName the type of binary predicate, for example
   * #$TransitiveBinaryPredicate, which when null defaults to #$BinaryPredicate
   * @param comment the comment for the new binary predicate, or null
   * @param arg1IsaName the argument position one type constraint, or null
   * @param arg2IsaName the argument position two type constraint, or null
   * @param arg1FormatName the argument position one format constraint, or null
   * @param arg2FormatName the argument position two format constraint, or null
   * @param genlPredsName the more general binary predicate of which this new predicate is a
   * specialization, that when null defaults to #$conceptuallyRelated
   *
   * @return the new binary predicate term
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the Cyc server returns an error
   */
  CycConstant findOrCreateBinaryPredicate(
          String predicateName,
          String predicateTypeName,
          String comment, 
          String arg1IsaName, 
          String arg2IsaName, 
          String arg1FormatName, 
          String arg2FormatName,
          String genlPredsName) throws CycConnectionException, CycApiException;

  /**
   * Finds or creates a new binary predicate term.
   *
   * @param predicateName the name of the binary predicate
   * @param predicateType the type of binary predicate, for example #$TransitiveBinaryPredicate,
   * which when null defaults to #$BinaryPredicate
   * @param comment the comment for the new binary predicate, or null
   * @param arg1Isa the argument position one type constraint, or null
   * @param arg2Isa the argument position two type constraint, or null
   * @param arg1Format the argument position one format constraint, or null
   * @param arg2Format the argument position two format constraint, or null
   * @param genlPreds the more general binary predicate of which this new predicate is a
   * specialization, that when null defaults to #$conceptuallyRelated
   *
   * @return the new binary predicate term
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the Cyc server returns an error
   */
  CycConstant findOrCreateBinaryPredicate(
          String predicateName,
          Fort predicateType,
          String comment,
          Fort arg1Isa,
          Fort arg2Isa,
          Fort arg1Format,
          Fort arg2Format,
          Fort genlPreds) throws CycConnectionException, CycApiException;

  /**
   * Finds or creates a new collection term.
   *
   * @param collectionName the name of the collection
   * @param comment the comment for the collection
   * @param commentMtName the name of the microtheory in which the comment is asserted
   * @param isaName the name of the collection of which the new collection is an instance
   * @param genlsName the name of the collection of which the new collection is a subset
   *
   * @return the new collection term
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the Cyc server returns an error
   */
  CycConstant findOrCreateCollection(
          String collectionName, 
          String comment, 
          String commentMtName, 
          String isaName,
          String genlsName) throws CycConnectionException, CycApiException;

  /**
   * Finds or creates a new collection term.
   *
   * @param collectionName the name of the collection
   * @param comment the comment for the collection
   * @param commentMt the microtheory in which the comment is asserted
   * @param isa the collection of which the new collection is an instance
   * @param genls the collection of which the new collection is a subset
   *
   * @return the new collection term
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the Cyc server returns an error
   */
  Fort findOrCreateCollection(
          String collectionName, String comment, Fort commentMt, Fort isa, Fort genls) 
          throws CycConnectionException, CycApiException;

  /**
   * Describes a collection term.
   *
   * @param collection the collection
   * @param comment the comment for the collection
   * @param commentMt the microtheory in which the comment is asserted
   * @param isa the collection of which the new collection is an instance
   * @param genls the collection of which the new collection is a subset
   *
   * @return the new collection term
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the Cyc server returns an error
   */
  Fort describeCollection(Fort collection, String comment, Fort commentMt, Fort isa, Fort genls)
          throws CycConnectionException, CycApiException;

  /**
   * Finds or creates a new collection-denoting reifiable binary function term.
   *
   * @param binaryFunction the binary function
   * @param comment the comment for the binary function
   * @param commentMt the microtheory in which the comment is asserted
   * @param arg1IsaName the collection of which the new binary function is an instance
   * @param arg2GenlsName the kind of objects this binary function takes as its first argument, or
   * null
   * @param arg2IsaName the kind of objects this binary function takes as its second argument, or
   * null
   * @param arg1GenlsName the general collections this binary function takes as its first argument,
   * or null
   * @param resultIsa the kind of object represented by this reified term
   *
   * @return the new collection-denoting reifiable binary function term
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the Cyc server returns an error
   */
  Fort findOrCreateCollectionDenotingBinaryFunction(
          String binaryFunction,
          String comment, 
          String commentMt, 
          String arg1IsaName,
          String arg2IsaName, 
          String arg1GenlsName,
          String arg2GenlsName, 
          String resultIsa) throws CycConnectionException, CycApiException;

  /**
   * Describes a collection-denoting reifiable binary function term.
   *
   * @param binaryFunction the binary function
   * @param comment the comment for the binary function
   * @param commentMt the microtheory in which the comment is asserted
   * @param arg1Isa the kind of objects this binary function takes as its first argument, or null
   * @param arg2Isa the kind of objects this binary function takes as its first argument, or null
   * @param arg1Genls the general collections this binary function takes as its first argument, or
   * null
   * @param arg2Genls the general collections this binary function takes as its second argument, or
   * null
   * @param resultIsa the kind of object represented by this reified term
   *
   * @return the new collection-denoting reifiable binary function term
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the Cyc server returns an error
   */
  Fort describeCollectionDenotingBinaryFunction(
          Fort binaryFunction,
          String comment,
          Fort commentMt,
          Fort arg1Isa,
          Fort arg2Isa,
          Fort arg1Genls,
          Fort arg2Genls,
          Fort resultIsa) throws CycConnectionException, CycApiException;

  /**
   * Finds or creates a new collection-denoting reifiable unary function term.
   *
   * @param unaryFunction the unary function
   * @param comment the comment for the unary function
   * @param commentMt the microtheory in which the comment is asserted
   * @param arg1Isa the isa type constraint for the argument
   * @param arg1GenlName the genls type constraint for the argument if it is a collection
   * @param resultIsa the isa object represented by this reified term
   * @param resultGenlName the genls object represented by this reified term
   *
   * @return the new collection-denoting reifiable unary function term
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the Cyc server returns an error
   */
  Fort findOrCreateCollectionDenotingUnaryFunction(
          String unaryFunction,
          String comment,
          String commentMt,
          String arg1Isa,
          String arg1GenlName,
          String resultIsa,
          String resultGenlName) throws CycConnectionException, CycApiException;

  /**
   * Describes a collection-denoting reifiable unary function term.
   *
   * @param unaryFunction the unary function
   * @param comment the comment for the unary function
   * @param commentMt the microtheory in which the comment is asserted
   * @param arg1Isa the isa type constraint for the argument
   * @param arg1Genl the genls type constraint for the argument if it is a collection
   * @param resultIsa the isa object represented by this reified term
   * @param resultGenl the genls object represented by this reified term
   *
   * @return the new collection-denoting reifiable unary function term
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the Cyc server returns an error
   */
  Fort describeCollectionDenotingUnaryFunction(
          Fort unaryFunction, 
          String comment,
          Fort commentMt, 
          Fort arg1Isa,
          Fort arg1Genl, 
          Fort resultIsa, 
          Fort resultGenl) throws CycConnectionException, CycApiException;

  /**
   * Creates a new Collector microtheory and links it more general mts.
   *
   * @param mtName the name of the new collector microtheory
   * @param comment the comment for the new collector microtheory
   * @param genlMts the list of more general microtheories
   *
   * @return the new microtheory
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the Cyc server returns an error
   */
  ElMtConstant createCollectorMt(String mtName, String comment, List genlMts)
          throws CycConnectionException, CycApiException;

  /**
   * Finds or creates a new individual-denoting reifiable unary function term.
   *
   * @param unaryFunction the function
   * @param comment the comment for the unary function
   * @param commentMt the microtheory in which the comment is asserted
   * @param arg1Isa the kind of objects this unary function takes as its argument
   * @param resultIsa the kind of object represented by this reified term
   *
   * @return the new individual-denoting reifiable unary function term
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the Cyc server returns an error
   */
  Fort findOrCreateIndivDenotingUnaryFunction(
          String unaryFunction, String comment, String commentMt, String arg1Isa, String resultIsa)
          throws CycConnectionException, CycApiException;

  /**
   * Describes an individual-denoting reifiable unary function term.
   *
   * @param unaryFunction the function
   * @param comment the comment for the unary function
   * @param commentMt the microtheory in which the comment is asserted
   * @param arg1Isa the kind of objects this unary function takes as its argument
   * @param resultIsa the kind of object represented by this reified term
   *
   * @return the new individual-denoting reifiable unary function term
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the Cyc server returns an error
   */
  Fort describeIndivDenotingUnaryFunction(
          Fort unaryFunction, String comment, Fort commentMt, Fort arg1Isa, Fort resultIsa) 
          throws CycConnectionException, CycApiException;

  /**
   * Finds or creates a new individual term.
   *
   * @param IndividualName the name of the individual term
   * @param comment the comment for the individual
   * @param commentMt the microtheory in which the comment is asserted
   * @param isa the collection of which the new individual is an instance
   *
   * @return the new individual term
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the Cyc server returns an error
   */
  Fort findOrCreateIndividual(String IndividualName, String comment, String commentMt, String isa) 
          throws CycConnectionException, CycApiException;

  /**
   * Finds or creates a new individual term.
   *
   * @param IndividualName the name of the individual term
   * @param comment the comment for the individual
   * @param commentMt the microtheory in which the comment is asserted
   * @param isa the collection of which the new individual is an instance
   *
   * @return the new individual term
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the Cyc server returns an error
   */
  Fort findOrCreateIndividual(
          String IndividualName, String comment, DenotationalTerm commentMt, DenotationalTerm isa)
          throws CycConnectionException, CycApiException;

  /**
   * Create a microtheory MT, with a comment, isa MT-TYPE and Fort genlMts. An existing
   * microtheory with the same name is killed first, if it exists.
   *
   * @param mtName the name of the microtheory term
   * @param comment the comment for the new microtheory
   * @param isaMt the type of the new microtheory
   * @param genlMts the list of more general microtheories
   *
   * @return the new microtheory term
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  ElMtConstant createMicrotheory(String mtName, String comment, Fort isaMt, List genlMts)
          throws CycConnectionException, CycApiException;

  /**
   * Describe a microtheory MT, with a comment, isa MT-TYPE and Fort genlMts.
   *
   * @param mt the microtheory term
   * @param comment the comment for the new microtheory
   * @param isaMt the type of the new microtheory
   * @param genlMts the list of more general microtheories
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  void describeMicrotheory(Fort mt, String comment, Fort isaMt, List genlMts)
          throws CycConnectionException, CycApiException;

  /**
   * Create a microtheory MT, with a comment, isa MT-TYPE and Fort genlMts. An existing
   * microtheory with the same name is killed first, if it exists.
   *
   * @param mtName the name of the microtheory term
   * @param comment the comment for the new microtheory
   * @param isaMtName the type (as a string) of the new microtheory
   * @param genlMtNames
   *
   * @return the new microtheory term
   *
   * @throws CycConnectionException if cyc server host not found on the network
   * @throws CycApiException if the api request results in a cyc server error
   */
  ElMtConstant createMicrotheory(
          String mtName, String comment, String isaMtName, List<String> genlMtNames)
          throws CycConnectionException, CycApiException;

  /**
   * Finds or creates a new permanent Cyc constant in the KB with the specified name. The operation will be
   * added to the KB transcript for replication and archive.
   *
   * @param constantName the name of the constant
   *
   * @return the new constant term
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  CycConstant findOrCreateNewPermanent(String constantName)
          throws CycConnectionException, CycApiException;

  /**
   * Creates a new spindle microtheory in the given spindle system.
   *
   * @param spindleMtName the name of the new spindle microtheory
   * @param comment the comment for the new spindle microtheory
   * @param spindleHeadMtName the name of the spindle head microtheory
   * @param spindleCollectorMtName the name of the spindle head microtheory
   *
   * @return the new spindle microtheory in the given spindle system
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the Cyc server returns an error
   */
  ElMtConstant createSpindleMt(
          String spindleMtName, 
          String comment,
          String spindleHeadMtName, 
          String spindleCollectorMtName) throws CycConnectionException, CycApiException;

  /**
   * Creates a new spindle microtheory in the given spindle system.
   *
   * @param spindleMtName the name of the new spindle microtheory
   * @param comment the comment for the new spindle microtheory
   * @param spindleHeadMt the spindle head microtheory
   * @param spindleCollectorMt the spindle head microtheory
   *
   * @return the new spindle microtheory in the given spindle system
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the Cyc server returns an error
   */
  ElMtConstant createSpindleMt(
          String spindleMtName, 
          String comment,
          Fort spindleHeadMt, 
          Fort spindleCollectorMt) throws CycConnectionException, CycApiException;
  
  /**
   * Edit the sentence contained in unassertSentence to instead be
   * assertSentence. If the Mt is null, unassertSentence and assertSentence
   * must be contextualized sentences (i.e. the Mt must be specified using
   * #$ist). If unassertSentence is an empty conjunction, this amounts to "assert", while
   * if assertSentence is an empty conjunction, it amounts to unassert.  This is transactional--if
   * the edit can't be carried out, the old assertion is left in place and a CycAPIException is thrown.
   *
   * @param unassertSentence
   * @param assertSentence
   * @param mt
   * @param bookkeeping
   * @param transcript
   * @param disableWFFChecking
   * @param templates
   * @throws CycConnectionException if a data communication error occurs
   */
  void edit(
          String unassertSentence, 
          String assertSentence, 
          ElMt mt, 
          boolean bookkeeping, 
          boolean transcript, 
          boolean disableWFFChecking, 
          List<Fort> templates) throws CycApiException, CycConnectionException;

  /**
   * Ensures that the given term meets the given isa and genl wff constraints in the
   * UniversalVocabularyMt.
   *
   * @param cycFort the given term
   * @param isaConstraintName the given isa type constraint, or null
   * @param genlsConstraintName the given genls type constraint, or null
   *
   * @throws CycConnectionException if a communications error occurs or the Cyc server cannot be found
   * @throws CycApiException if the Cyc server returns an error
   */
  void ensureWffConstraints(String cycFort, String isaConstraintName, String genlsConstraintName) 
          throws CycConnectionException, CycApiException;

  /**
   * Ensures that the given term meets the given isa and genl wff constraints in the
   * UniversalVocabularyMt.
   *
   * @param cycFort the given term
   * @param isaConstraint the given isa type constraint, or null
   * @param genlsConstraint the given genls type constraint, or null
   *
   * @throws CycConnectionException if a communications error occurs or the Cyc server cannot be found
   * @throws CycApiException if the Cyc server returns an error
   */
  void ensureWffConstraints(Fort cycFort, Fort isaConstraint, Fort genlsConstraint) 
          throws CycConnectionException, CycApiException;
  
  /**
   * Merge assertions of KILL-FORT onto KEEP-FORT and kill KILL-FORT.
   *
   * <p>NOTE: Assumes cyclist is ok.
   * 
   * <p>NOTE: The salient property of this function is that it never throws an error.
   * 
   * @param killFort fort to kill
   * @param keepFort fort to keep
   * 
   * @return 0 boolean ;; t if success, o/w nil
   * @return 1 list ;; error list of form (ERROR-TYPE ERROR-STRING) otherwise.
   * 
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException if the Cyc server returns an error
   */
  boolean merge(Fort killFort, Fort keepFort) throws CycConnectionException, CycApiException;
}
