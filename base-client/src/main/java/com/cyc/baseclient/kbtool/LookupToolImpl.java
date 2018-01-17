package com.cyc.baseclient.kbtool;

/*
 * #%L
 * File: LookupToolImpl.java
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
import com.cyc.base.cycobject.CycAssertion;
import com.cyc.base.cycobject.CycConstant;
import com.cyc.base.cycobject.CycList;
import com.cyc.base.cycobject.CycObject;
import com.cyc.base.cycobject.CycSentence;
import com.cyc.base.cycobject.CycVariable;
import com.cyc.base.cycobject.DenotationalTerm;
import com.cyc.base.cycobject.ElMt;
import com.cyc.base.cycobject.FormulaSentence;
import com.cyc.base.cycobject.Fort;
import com.cyc.base.cycobject.Guid;
import com.cyc.base.cycobject.Nart;
import com.cyc.base.cycobject.Naut;
import com.cyc.base.exception.BaseClientRuntimeException;
import com.cyc.base.exception.CycApiException;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.base.exception.CycTimeOutException;
import com.cyc.base.kbtool.LookupTool;
import com.cyc.baseclient.AbstractKbTool;
import com.cyc.baseclient.CommonConstants;
import com.cyc.baseclient.CycObjectFactory;
import com.cyc.baseclient.connection.SublApiHelper;
import com.cyc.baseclient.cycobject.CycArrayList;
import com.cyc.baseclient.cycobject.CycAssertionImpl;
import com.cyc.baseclient.cycobject.CycConstantImpl;
import com.cyc.baseclient.cycobject.CycListParser;
import com.cyc.baseclient.cycobject.CycSymbolImpl;
import com.cyc.baseclient.cycobject.DefaultCycObjectImpl;
import com.cyc.baseclient.cycobject.FormulaSentenceImpl;
import com.cyc.baseclient.cycobject.GuidImpl;
import com.cyc.baseclient.cycobject.NartImpl;
import com.cyc.baseclient.cycobject.NautImpl;
import com.cyc.baseclient.datatype.CycStringUtils;
import com.cyc.baseclient.inference.params.DefaultInferenceParameters;
import com.cyc.session.exception.OpenCycUnsupportedFeatureException;
import java.io.IOException;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.cyc.base.cycobject.CycConstant.HD;
import static com.cyc.baseclient.CycObjectFactory.makeCycSymbol;
import static com.cyc.baseclient.connection.SublApiHelper.makeNestedSublStmt;
import static com.cyc.baseclient.connection.SublApiHelper.makeSublStmt;

/**
 * Tools for looking up CycObjects in the Cyc KB. To perform more complex inferences
 over the Cyc KB, use the {@link com.cyc.baseclient.kbtool.InferenceToolImpl}.
 * 
 * @see com.cyc.baseclient.kbtool.InferenceToolImpl
 * @author nwinant
 */
public class LookupToolImpl extends AbstractKbTool implements LookupTool {
  
  public LookupToolImpl(CycAccess client) {
    super(client);
  }
  
  
  // Public
  
  @Override
  public CycConstant find(String constantName)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    if (constantName == null) {
      return null;
    }

    return getConstantByName(constantName);
  }
  
  @Override
  public CycConstant findOrCreate(String constantName)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getCyc().getObjectTool().makeCycConstant(constantName);
  }
  
  @Override
  public CycConstantImpl getKnownConstantByName(String constantName)
          throws CycConnectionException, CycApiException {
    CycConstantImpl cycConstant = getConstantByName(constantName);

    if (cycConstant == null) {
      throw new com.cyc.base.exception.CycApiException("Expected constant not found " + constantName);
    }

    return cycConstant;
  }
  
  @Override
  public Fort getKnownFortByName(String fortName)
          throws CycConnectionException, CycApiException {
    Fort fort;
    if (fortName.contains(")")) {
      final CycArrayList terms = new CycListParser(getCyc()).read(fortName);
      fort = getCycNartFromCons(terms);
    } else {
      fort = getKnownConstantByName(fortName);
    }
    if (fort == null) {
      throw new com.cyc.base.exception.CycApiException("'" + fortName + "' is not a valid FORT name.");
    }
    return fort;
  }
  
  @Override
  public DenotationalTerm getTermByName(String termName)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    DenotationalTerm term;
    if (termName.contains(")")) {
      final CycArrayList terms = new CycListParser(getCyc()).read(termName);
      term = getCycNartFromCons(terms);
      if (term == null && getCyc().getInspectorTool().isFunction((CycObject)terms.first())) {          
          term = new NautImpl(terms);
      }
    } else {
      term = getKnownConstantByName(termName);
    }
    if (term == null) {
      throw new com.cyc.base.exception.CycApiException("'" + termName + "' is not a valid FORT name.");
    }
    return term;
  }
  
  @Override
  public List findConstantsForNames(List constantNames)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    if ((constantNames == null) || (constantNames.size() <= 0)) {
      return null;
    }
    StringBuffer command = new StringBuffer(
            "(MAPCAR (QUOTE FIND-CONSTANT) (LIST");
    for (Iterator iter = constantNames.iterator(); iter.hasNext();) {
      command.append(" \"");
      String curConstName = CycStringUtils.escapeDoubleQuotes("" + iter.next());
      command.append(curConstName);
      command.append("\"");
    }
    command.append("))");
    final Object result = getConverse().converseCycObject("" + command);
    if (!(result instanceof CycArrayList)) {
      return null;
    }
    return (CycArrayList) result;
  }

  @Override
  public List findConstantsForGuids(List constantGuids)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    if ((constantGuids == null) || (constantGuids.size() <= 0)) {
      return null;
    }
    List result = new ArrayList();
    // @ToDo this is very inefficient...we need to find a way to do this
    // with a single round trip to Cyc. --Tony
    for (Iterator iter = constantGuids.iterator(); iter.hasNext();) {
      CycConstantImpl item = (CycConstantImpl) iter.next();
      try {
        result.add(getConstantByGuid(item.getGuid()));
      } catch (Exception e) {
        result.add(CycObjectFactory.nil);
      }
    }
    return result;
  }
  
  @Override
  public CycConstantImpl getConstantByName(final String constantName)
          throws CycConnectionException, CycApiException {
    String name = constantName;
    if (constantName.startsWith(HD)) {
      name = name.substring(2);
    }
    CycConstantImpl answer = CycObjectFactory.getCycConstantCacheByName(name);
    if (answer != null) { // TODO: sometimes the cache is stale, and we ought to be able to force a look in the KB.
      return answer;
    }
    final String command = makeSublStmt("find-constant", name);
    final Object answerObject = getConverse().converseObject(command);
    if (answerObject instanceof CycConstantImpl) {
      answer = (CycConstantImpl) answerObject;
      CycObjectFactory.addCycConstantCache(answer);
      return answer;
    }
    return null;
  }
  
  @Override
  public Guid getConstantGuid(String constantName)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    final String command =
            makeSublStmt("guid-to-string",
            makeNestedSublStmt("constant-external-id",
            makeNestedSublStmt("find-constant", constantName)));
    return CycObjectFactory.makeGuid(getConverse().converseString(command));
  }
  
  @Override
  public CycAssertionImpl getAssertionById(Integer id)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    final String command = makeSublStmt("find-assertion-by-id", id);
    Object obj = getConverse().converseObject(command);

    if (obj.equals(new CycSymbolImpl("NIL"))) {
      return null;
    } else if (!(obj instanceof CycAssertionImpl)) {
      throw new BaseClientRuntimeException(obj + " is not a CycAssertion");
    } else {
      return (CycAssertionImpl) obj;
    }
  }
  
  @Override
  public String getConstantName(Guid guid)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    // Optimized for the binary api.

    String command = makeSublStmt("constant-name",
            makeNestedSublStmt("find-constant-by-external-id",
            makeNestedSublStmt("string-to-guid"), guid.toString()));

    return getConverse().converseString(command);
  }
  
  @Override
  public CycConstantImpl getKnownConstantByGuid(String guidString)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    GuidImpl guid = CycObjectFactory.makeGuid(guidString);

    return getKnownConstantByGuid(guid);
  }
  
  @Override
  public CycConstantImpl getKnownConstantByGuid(Guid guid)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    CycConstantImpl cycConstant = getConstantByGuid(guid);

    if (cycConstant == null) {
      throw new com.cyc.base.exception.CycApiException("Expected constant not found " + guid);
    }

    return cycConstant;
  }
  
  @Override
  public CycConstantImpl getConstantByGuid(Guid guid)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    CycConstantImpl answer = CycObjectFactory.getCycConstantCacheByGuid(
            guid);
    if (answer != null) {
      return answer;
    }
    final String command =
            makeSublStmt("find-constant-by-external-id",
            makeNestedSublStmt("string-to-guid", guid.getGuidString()));
    final Object answerObject = getConverse().converseObject(command);
    if (answerObject instanceof CycConstantImpl) {
      answer = (CycConstantImpl) answerObject;
      CycObjectFactory.addCycConstantCache(answer);
      return answer;
    }
    return null;
  }
  
  @Override
  public CycList getArg2s(String predicate, String arg1, String mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getArg2s(getKnownConstantByName(predicate),
            getKnownConstantByName(arg1),
            getKnownConstantByName(mt));
  }
  
  @Override
  public CycList getArg2s(String predicate, Fort arg1, CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getArg2s(getKnownConstantByName(predicate), arg1, makeElMt_inner(mt));
  }
  
  @Override
  public CycList getArg2s(Fort predicate, Fort arg1, CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {

    CycVariable variable = CycObjectFactory.makeCycVariable("?arg2");
    final FormulaSentence query = FormulaSentenceImpl.makeFormulaSentence(
            predicate, arg1, variable);

    return getCyc().getInferenceTool().queryVariable(variable, query, makeElMt_inner(mt));
  }
  
  @Override
  public Object getArg2(String predicate, String arg1, String mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getArg2(getKnownConstantByName(predicate),
            getKnownConstantByName(arg1),
            getKnownConstantByName(mt));
  }
  
  @Override
  public Object getArg2(String predicate, Fort arg1, CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getArg2(getKnownConstantByName(predicate), arg1, makeElMt_inner(mt));
  }
  
  @Override
  public Object getArg2(Fort predicate, Fort arg1, CycObject mt)
          throws CycConnectionException, CycApiException {

    CycVariable variable = CycObjectFactory.makeCycVariable("?arg2");
    final FormulaSentence query = FormulaSentenceImpl.makeFormulaSentence(
            predicate, arg1, variable);
    final DefaultInferenceParameters params = new DefaultInferenceParameters(
            getCyc());
    params.setMaxAnswerCount(1);
    CycList answer = getCyc().getInferenceTool().queryVariable(variable, query, makeElMt_inner(mt), params);

    if (answer.size() > 0) {
      return answer.get(0);
    } else {
      return null;
    }
  }
  
  @Override
  public Object getAssertionArg2(String predicate, String arg1, String mt)
          throws CycConnectionException, CycApiException {
    return getAssertionArg2(getKnownConstantByName(predicate),
            getKnownConstantByName(arg1),
            getKnownConstantByName(mt));
  }
  
  @Override
  public Object getAssertionArg2(Fort predicate, Fort arg1, CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseObject(makeSublStmt(FPRED_VALUE_IN_MT, arg1, predicate,
            makeElMt_inner(mt)));
  }
  
  @Override
  public Object getArg1(String predicate, String arg2, String mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getArg1(getKnownConstantByName(predicate),
            getKnownConstantByName(arg2),
            getKnownConstantByName(mt));
  }
  
  @Override
  public Object getArg1(Fort predicate, DenotationalTerm arg2,
          CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    CycList answer = getArg1s(predicate, arg2, makeElMt_inner(mt));
    if (answer.size() > 0) {
      return answer.get(0);
    } else {
      return null;
    }
  }
  
  @Override
  public CycList getArg1s(String predicate, String arg2, String mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getArg1s(getKnownConstantByName(predicate),
            getKnownConstantByName(arg2),
            getKnownConstantByName(mt));
  }
  
  @Override
  public CycList getArg1s(Fort predicate, DenotationalTerm arg2,
          CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {

    CycVariable variable = CycObjectFactory.makeCycVariable("?arg1");
    final FormulaSentence query = FormulaSentenceImpl.makeFormulaSentence(
            predicate, variable, arg2);

    return getCyc().getInferenceTool().queryVariable(variable, query, makeElMt_inner(mt));
  }
  
  @Override
  public CycList getAllAssertionsInMt(CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseList(makeSublStmt("gather-mt-index", mt));
  }
  
  @Override
  public CycList getDenotsOfString(String denotationString)
          throws CycConnectionException, CycApiException {
    String command = makeSublStmt("denots-of-string", denotationString);
    return getConverse().converseList(command);
  }
  
  @Override
  public CycList getDenotsOfString(String denotationString, CycList collections)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    CycList setArgs = collections.addToBeginning(CommonConstants.THE_SET);
    Naut collectionUnion = new NautImpl(CommonConstants.COLLECTION_UNION_FN, setArgs);
    String command = makeSublStmt("typed-denots-of-string", denotationString, collectionUnion);
    return getConverse().converseList(command);
  }
  
  @Deprecated
  @Override
  public CycList getMWSDenotsOfString(CycList multiWordDenotationString)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    String command = makeSublStmt("mws-denots-of-string",
            multiWordDenotationString);
    return getConverse().converseList(command);
  }
  
  @Deprecated
  @Override
  public CycList getMWSDenotsOfString(CycList multiWordDenotationString,
          CycList collections)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    String command = makeSublStmt("mws-denots-of-string",
            multiWordDenotationString);

    CycList terms = getConverse().converseList(command);
    CycArrayList result = new CycArrayList();
    Iterator collectionsIterator = collections.iterator();

    while (collectionsIterator.hasNext()) {
      Fort oneCollection = (Fort) collectionsIterator.next();
      Iterator termsIter = terms.iterator();

      while (termsIter.hasNext()) {
        Fort term = (Fort) termsIter.next();

        if (getCyc().getInspectorTool().isa(term, oneCollection)) {
          result.add(term);
        }
      }
    }

    return result;
  }
  
  @Deprecated
  @Override
  public String getExternalIDString(final CycObject cycObject)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return DefaultCycObjectImpl.toCompactExternalId(cycObject, getCyc());
  }
  
  @Override
  public String getComment(CycObject cycObject)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    //// Preconditions
    if (cycObject == null) {
      throw new NullPointerException("cycObject must not be null");
    }
    verifyPossibleDenotationalTerm(cycObject);
    if (cycObject instanceof CycArrayList) {
      return null;
    }
    String script = "(clet ((comment-string \n" + "         (with-all-mts (comment "
            + cycObject.stringApiValue() + ")))) \n" + "  (fif comment-string \n"
            + "       (string-substitute \" \" \"\\\"\" comment-string) \n"
            + "       \"\"))";

    return getConverse().converseString(script);
  }
  
  @Override
  public String getComment(final CycObject cycObject,
          final CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    //// Preconditions
    if (cycObject == null) {
      throw new NullPointerException("cycObject must not be null");
    }
    if (mt == null) {
      throw new NullPointerException("mt must not be null");
    }
    String script =
            "(clet ((comment-string \n"
            + "         (comment " + cycObject.stringApiValue() + " " +  makeElMt_inner(
            mt).stringApiValue() + "))) \n"
            + "  (fif comment-string \n"
            + "       (string-substitute \" \" \"\\\"\" comment-string) \n"
            + "       \"\"))";

    return getConverse().converseString(script);
  }
  
  @Override
  public CycList getIsas(CycObject cycObject)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    //// Preconditions
    if (cycObject == null) {
      throw new NullPointerException("cycObject must not be null");
    }
    verifyPossibleDenotationalTerm(cycObject);
    return getConverse().converseList("(remove-duplicates (with-all-mts (isa " + cycObject.stringApiValue()
            + ")))");
  }
  
  @Override
  public CycList getIsas(final CycObject cycObject,
          CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    //// Preconditions
    if (cycObject == null) {
      throw new NullPointerException("cycObject must not be null");
    }
    verifyPossibleDenotationalTerm(cycObject);
    if (mt == null) {
      throw new NullPointerException("mt must not be null");
    }
    return getConverse().converseList("(isa " + cycObject.stringApiValue()
            + " " +  makeElMt_inner(mt).stringApiValue()
            + ")");
  }
  
  @Override
  public CycList getGenls(CycObject cycObject)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    //// Preconditions
    if (cycObject == null) {
      throw new NullPointerException("cycObject must not be null");
    }
    verifyPossibleDenotationalTerm(cycObject);
    return getConverse().converseList("(remove-duplicates (with-all-mts (genls " + cycObject.stringApiValue()
            + ")))");
  }
  
  @Override
  public CycList getGenls(final CycObject cycObject,
          final CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    //// Preconditions
    if (cycObject == null) {
      throw new NullPointerException("cycObject must not be null");
    }
    verifyPossibleDenotationalTerm(cycObject);
    if (mt == null) {
      throw new NullPointerException("mt must not be null");
    }
    return getConverse().converseList("(genls " + cycObject.stringApiValue() + " "
            + makeElMt_inner(mt).stringApiValue() + ")");
  }
  
  @Override
  public CycList getMinGenls(Fort cycFort)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseList("(remove-duplicates (with-all-mts (min-genls "
            + cycFort.stringApiValue() + ")))");
  }
  
  @Override
  public CycList getMinGenls(Fort cycFort,
          CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseList("(min-genls " + cycFort.stringApiValue() + " "
            + makeElMt_inner(mt).stringApiValue() + ")");
  }
  
  @Override
  public CycList getSpecs(Fort cycFort)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseList("(remove-duplicates (with-all-mts (specs " + cycFort.stringApiValue()
            + ")))");
  }
  
  @Override
  public CycList getSpecs(Fort cycFort,
          CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseList("(specs " + cycFort.stringApiValue() + " "
            + makeElMt_inner(mt).stringApiValue() + ")");
  }
  
  @Override
  public CycList getMaxSpecs(Fort cycFort)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseList("(remove-duplicates (with-all-mts (max-specs "
            + cycFort.stringApiValue() + ")))");
  }
  
  @Override
  public CycList getMaxSpecs(Fort cycFort,
          CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseList("(max-specs " + cycFort.stringApiValue() + " "
            + makeElMt_inner(mt).stringApiValue() + ")");
  }
  
  @Override
  public CycList getGenlSiblings(Fort cycFort)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseList("(remove-duplicates (with-all-mts (genl-siblings "
            + cycFort.stringApiValue() + ")))");
  }
  
  @Override
  public CycList getGenlSiblings(Fort cycFort,
          CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseList("(genl-siblings " + cycFort.stringApiValue() + " "
            + makeElMt_inner(mt).stringApiValue() + ")");
  }
  
  @Override
  public CycList getSiblings(Fort cycFort)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getSpecSiblings(cycFort);
  }
  
  @Override
  public CycList getSiblings(Fort cycFort,
          CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getSpecSiblings(cycFort,
            mt);
  }
  
  @Override
  public CycList getSpecSiblings(Fort cycFort)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseList("(remove-duplicates (with-all-mts (spec-siblings "
            + cycFort.stringApiValue() + ")))");
  }
  
  @Override
  public CycList getSpecSiblings(Fort cycFort,
          CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseList("(spec-siblings " + cycFort.stringApiValue() + " "
            + makeElMt_inner(mt).stringApiValue() + ")");
  }
  
  @Override
  public CycList getAllGenls(Fort cycFort)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseList("(all-genls-in-any-mt " + cycFort.stringApiValue() + ")");
  }
  
  @Override
  public CycList getAllGenls(CycObject cycObject,
          CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    //// Preconditions
    if (cycObject == null) {
      throw new NullPointerException("cycObject must not be null");
    }
    verifyPossibleDenotationalTerm(cycObject);
    if (mt == null) {
      throw new NullPointerException("mt must not be null");
    }
    return getConverse().converseList("(all-genls " + cycObject.stringApiValue() + " "
            + makeElMt_inner(mt).stringApiValue() + ")");
  }
  
  @Override
  public CycList getAllSpecs(Fort cycFort)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseList("(remove-duplicates (with-all-mts (all-specs "
            + cycFort.stringApiValue() + ")))");
  }
  
  @Override
  public CycList getAllSpecs(Fort cycFort,
          CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseList("(all-specs " + cycFort.stringApiValue() + " "
            + makeElMt_inner(mt).stringApiValue() + ")");
  }
  
  @Override
  public CycList getAllGenlsWrt(Fort spec,
          Fort genl)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseList("(remove-duplicates (with-all-mts (all-genls-wrt "
            + spec.stringApiValue() + " " + genl.stringApiValue() + ")))");
  }
  
  @Override
  public CycList getAllGenlsWrt(Fort spec,
          Fort genl,
          CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseList("(all-genls-wrt " + spec.stringApiValue() + " " + genl.stringApiValue()
            + " " + makeElMt_inner(mt).stringApiValue() + ")");
  }
  
  @Override
  public CycList getAllDependentSpecs(Fort cycFort)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseList("(remove-duplicates (with-all-mts (all-dependent-specs "
            + cycFort.stringApiValue() + ")))");
  }
  
  @Override
  public CycList getAllDependentSpecs(Fort cycFort,
          CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseList("(all-dependent-specs " + cycFort.stringApiValue() + " "
            + makeElMt_inner(mt).stringApiValue() + ")");
  }
  
  @Override
  public List<CycAssertion> getAllTermAssertions(Fort cycFort) throws CycConnectionException, com.cyc.base.exception.CycApiException {
    final String command = SublApiHelper.makeSublStmt("all-term-assertions",
            cycFort);
    return getConverse().converseList(command);
  }
  
  @Override
  public CycList getSampleLeafSpecs(Fort cycFort,
          int numberOfSamples)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseList("(with-all-mts (sample-leaf-specs " + cycFort.stringApiValue() + " "
            + numberOfSamples + "))");
  }
  
  @Override
  public CycList getSampleLeafSpecs(Fort cycFort,
          int numberOfSamples,
          CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseList("(sample-leaf-specs " + cycFort.stringApiValue() + " " + numberOfSamples
            + " " + makeElMt_inner(mt).stringApiValue() + ")");
  }
  
  @Override
  public Fort getMinCol(final CycList collections)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return (Fort) getConverse().converseObject("(with-all-mts (min-col " + collections.stringApiValue()
            + "))");
  }
  
  @Override
  public Fort getMinCol(final CycList collections, final CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    //// Preconditions
    if (collections == null) {
      throw new NullPointerException("collections must not be null");
    }
    if (mt == null) {
      throw new NullPointerException("mt must not be null");
    }
    return (Fort) getConverse().converseObject("(with-inference-mt-relevance " + makeElMt_inner(
            mt).stringApiValue()
            + " (min-col " + collections.stringApiValue() + "))");
  }
  
  @Override
  public CycList getMaxCols(final CycList collections)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    //// Preconditions
    assert collections != null : "collections cannot be null";

    return getConverse().converseList("(with-all-mts (max-cols " + collections.stringApiValue()
            + "))");
  }
  
  @Override
  public CycList getMaxCols(final CycList collections, final CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    //// Preconditions
    if (collections == null) {
      throw new NullPointerException("collections must not be null");
    }
    if (mt == null) {
      throw new NullPointerException("mt must not be null");
    }
    return getConverse().converseList("(with-inference-mt-relevance " + makeElMt_inner(mt).stringApiValue() + " (max-cols " + collections.stringApiValue()
            + "))");
  }
  
  @Override
  public CycList getMinCols(final CycList collections)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    //// Preconditions
    assert collections != null : "collections cannot be null";

    return getConverse().converseList("(with-all-mts (min-cols " + collections.stringApiValue()
            + "))");
  }
  
  @Override
  public CycList getMinCols(final CycList collections, final CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    //// Preconditions
    if (collections == null) {
      throw new NullPointerException("collections must not be null");
    }
    if (mt == null) {
      throw new NullPointerException("mt must not be null");
    }
    return getConverse().converseList("(with-inference-mt-relevance " + makeElMt_inner(mt).stringApiValue()
            + " (min-cols " + collections.stringApiValue() + "))");
  }
  
  @Override
  public CycList getWhyGenl(Fort spec,
          Fort genl)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseList("(with-all-mts (why-genl? " + spec.stringApiValue() + " "
            + genl.stringApiValue() + "))");
  }
  
  @Override
  public CycList getWhyGenl(Fort spec,
          Fort genl,
          CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseList("(why-genl? " + spec.stringApiValue() + " " + genl.stringApiValue()
            + " " + makeElMt_inner(mt).stringApiValue() + ")");
  }
  
  @Override
  public CycList getWhyCollectionsIntersect(Fort collection1,
          Fort collection2)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseList(
            "(with-all-mts (why-collections-intersect? "
            + collection1.stringApiValue() + " " + collection2.stringApiValue() + "))");
  }
  
  @Override
  public CycList getWhyCollectionsIntersect(Fort collection1,
          Fort collection2,
          CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseList("(why-collections-intersect? " + collection1.stringApiValue() + " "
            + collection2.stringApiValue() + " " + makeElMt_inner(
            mt).stringApiValue() + ")");
  }
  
  @Override
  public CycList getCollectionLeaves(Fort cycFort)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseList(
            "(with-all-mts (collection-leaves " + cycFort.stringApiValue() + "))");
  }
  
  @Override
  public CycList getCollectionLeaves(Fort cycFort,
          CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseList("(collection-leaves " + cycFort.stringApiValue() + " "
            + makeElMt_inner(mt).stringApiValue() + ")");
  }
  
  @Override
  public CycList getLocalDisjointWith(CycObject cycFort)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseList(
            "(with-all-mts (local-disjoint-with " + cycFort.stringApiValue() + "))");
  }
  
  @Override
  public CycList getLocalDisjointWith(CycObject cycFort,
          CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseList("(local-disjoint-with " + cycFort.stringApiValue() + " "
            + makeElMt_inner(mt).stringApiValue() + ")");
  }
  
  @Override
  public CycList getMinIsas(CycObject cycFort)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseList(
            "(with-all-mts (min-isa " + cycFort.stringApiValue() + "))");
  }
  
  @Override
  public CycList getMinIsas(CycObject cycFort,
          CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseList("(min-isa " + cycFort.stringApiValue() + " "
            + makeElMt_inner(mt).stringApiValue() + ")");
  }
  
  @Override
  public CycList getInstances(CycObject cycFort)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    CycList result = getConverse().converseList(
            "(with-all-mts (instances " + cycFort.stringApiValue() + "))");
    return result;
  }
  
  @Override
  public CycList getInstances(CycObject cycFort,
          CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseList("(instances " + cycFort.stringApiValue() + " "
            + makeElMt_inner(mt).stringApiValue() + ")");
  }
  
  @Override
  public CycList getInstanceSiblings(CycObject cycFort)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseList(
            "(with-all-mts (instance-siblings " + cycFort.stringApiValue() + "))");
  }
  
  @Override
  public CycList getInstanceSiblings(CycObject cycFort,
          CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseList("(instance-siblings " + cycFort.stringApiValue() + " "
            + makeElMt_inner(mt).stringApiValue() + ")");
  }
  
  @Override
  public CycList getAllIsa(CycObject cycFort)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    String command = "(all-isa-in-any-mt " + cycFort.stringApiValue() + ")";
    CycList result = getConverse().converseList(command);
    return result;
  }
  
  @Override
  public CycList getAllIsa(CycObject cycFort,
          CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseList("(all-isa " + cycFort.stringApiValue() + " "
            + makeElMt_inner(mt).stringApiValue() + ")");
  }
  
  @Override
  public CycList getAllInstances(CycObject cycFort)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseList(
            "(all-instances-in-all-mts " + cycFort.stringApiValue() + ")");
  }
  
  @Override
  public CycList getAllInstances(CycObject cycFort,
          CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseList("(all-instances " + cycFort.stringApiValue() + " "
            + makeElMt_inner(mt).stringApiValue() + ")");
  }
  
  @Override
  public CycList getAllQuotedInstances(final CycObject cycFort,
          final CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getAllQuotedInstances(cycFort, mt, 0);
  }
  
  @Override
  public CycList getAllQuotedInstances(final CycObject cycFort,
          final CycObject mt,
          final long timeoutMsecs)
          throws CycConnectionException, com.cyc.base.exception.CycApiException, CycTimeOutException {
    final CycVariable queryVariable = CycObjectFactory.makeCycVariable(
            "?QUOTED-INSTANCE");
    final FormulaSentence query = FormulaSentenceImpl.makeFormulaSentence(
            CommonConstants.QUOTED_ISA, queryVariable, cycFort);
    return getCyc().getInferenceTool().queryVariable(queryVariable, query, makeElMt_inner(mt), null, timeoutMsecs);
  }
  
  @Override
  public CycList getWhyIsa(CycObject spec,
          CycObject genl)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseList("(with-all-mts (why-isa? " + spec.stringApiValue() + " "
            + genl.stringApiValue() + "))");
  }
  
  @Override
  public CycList getWhyIsa(CycObject spec,
          CycObject genl,
          CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseList("(why-isa? " + spec.stringApiValue() + " " + genl.stringApiValue() + " "
            + makeElMt_inner(mt).stringApiValue() + ")");
  }
  
  @Override
  public CycList getGenlPreds(final CycObject predicate)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    if (predicate instanceof CycArrayList) {
      final String script =
              "(clet ((canonicalized-predicate (canonicalize-term " + predicate.stringApiValue() + ")))"
              + "  (pif (fort-p canonicalized-predicate)"
              + "    (remove-duplicates (with-all-mts (genl-predicates canonicalized-predicate)))"
              + "    nil))";
      return getConverse().converseList(script);
    } else {
      return getConverse().converseList("(remove-duplicates (with-all-mts (genl-predicates "
              + predicate.stringApiValue() + ")))");
    }
  }
  
  @Override
  public CycList getGenlPreds(final CycObject predicate,
          final CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    if (predicate instanceof CycArrayList) {
      final String script =
              "(clet ((canonicalized-predicate (canonicalize-term " + predicate.stringApiValue() + ")))"
              + "  (pif (fort-p canonicalized-predicate)"
              + "    (remove-duplicates (with-all-mts (genl-predicates canonicalized-predicate " + makeElMt_inner(
              mt).stringApiValue() + ")))"
              + "    nil))";
      return getConverse().converseList(script);
    } else {
      return getConverse().converseList("(genl-predicates " + predicate.stringApiValue() + " " + makeElMt_inner(
              mt).stringApiValue() + ")");
    }
  }
  
  @Override
  public CycList getAllGenlPreds(CycConstant predicate)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseList("(remove-duplicates (with-all-mts (all-genl-predicates "
            + predicate.stringApiValue() + ")))");
  }
  
  @Override
  public CycList getAllGenlPreds(CycConstant predicate,
          CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseList("(all-genl-predicates " + predicate.stringApiValue() + " "
            + makeElMt_inner(mt).stringApiValue() + ")");
  }
  
  @Override
  public CycList getAllSpecPreds(Fort cycFort)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseList("(remove-duplicates (with-all-mts (all-spec-predicates "
            + cycFort.stringApiValue() + ")))");
  }
  
  @Override
  public CycList getAllSpecPreds(Fort cycFort,
          CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseList("(all-spec-predicates " + cycFort.stringApiValue() + " "
            + makeElMt_inner(mt).stringApiValue() + ")");
  }
  
  @Override
  public CycList getAllSpecInverses(Fort cycFort)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseList("(remove-duplicates (with-all-mts (all-spec-inverses "
            + cycFort.stringApiValue() + ")))");
  }
  
  @Override
  public CycList getAllSpecInverses(Fort cycFort,
          CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseList("(all-spec-inverses " + cycFort.stringApiValue() + " "
            + makeElMt_inner(mt).stringApiValue() + ")");
  }
  
  @Override
  public CycList getAllSpecMts(CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseList("(all-spec-mts " + makeElMt_inner(mt).stringApiValue() + ")");
  }
  
  @Override
  public CycList getArg1Isas(CycObject cycObject)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    //// Preconditions
    if (cycObject == null) {
      throw new NullPointerException("cycObject must not be null");
    }
    verifyPossibleDenotationalTerm(cycObject);
    return getConverse().converseList("(remove-duplicates (with-all-mts (arg1-isa "
            + cycObject.stringApiValue() + ")))");
  }
  
  @Override
  public CycList getArg1Isas(final CycObject predicate,
          final CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    //// Preconditions
    if (predicate == null) {
      throw new NullPointerException("cycObject must not be null");
    }
    if (mt == null) {
      throw new NullPointerException("mt must not be null");
    }
    return getConverse().converseList("(arg1-isa " + predicate.stringApiValue() + " "
            + makeElMt_inner(mt).stringApiValue() + ")");
  }
  
  @Override
  public CycList getArg2Isas(CycObject cycObject)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    //// Preconditions
    if (cycObject == null) {
      throw new NullPointerException("cycObject must not be null");
    }
    verifyPossibleDenotationalTerm(cycObject);
    return getConverse().converseList("(remove-duplicates (with-all-mts (arg2-isa "
            + cycObject.stringApiValue() + ")))");
  }
  
  @Override
  public CycList getArg2Isas(CycObject cycObject,
          CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    //// Preconditions
    if (cycObject == null) {
      throw new NullPointerException("cycObject must not be null");
    }
    verifyPossibleDenotationalTerm(cycObject);
    if (mt == null) {
      throw new NullPointerException("mt must not be null");
    }
    return getConverse().converseList("(arg2-isa " + cycObject.stringApiValue() + " "
            + makeElMt_inner(mt).stringApiValue() + ")");
  }
  
  @Override
  public CycList getArg3Isas(Fort predicate)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseList("(remove-duplicates (with-all-mts (arg3-isa "
            + predicate.stringApiValue() + ")))");
  }
  
  @Override
  public CycList getArg3Isas(Fort predicate,
          CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseList("(arg3-isa " + predicate.stringApiValue() + " "
            + makeElMt_inner(mt).stringApiValue() + ")");
  }
  
  @Override
  public CycList getArg4Isas(Fort predicate)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseList("(remove-duplicates (with-all-mts (arg4-isa "
            + predicate.stringApiValue() + ")))");
  }
  
  @Override
  public CycList getArg4Isas(Fort predicate,
          CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseList("(arg4-isa " + predicate.stringApiValue() + " "
            + makeElMt_inner(mt).stringApiValue() + ")");
  }
  
  @Override
  public CycList getArgNIsas(Fort predicate,
          int argPosition)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    String command = "(remove-duplicates \n" + "  (with-all-mts \n" + "    (argn-isa "
            + predicate.stringApiValue() + " " + Integer.toString(
            argPosition) + ")))";

    return getConverse().converseList(command);
  }
  
  @Override
  public CycList getArgNIsas(Fort predicate,
          int argPosition,
          CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    String command = "(remove-duplicates \n" + "  (with-all-mts \n" + "    (argn-isa \n"
            + "      " + predicate.stringApiValue() + "      "
            + Integer.toString(argPosition) + "      " + makeElMt_inner(
            mt).stringApiValue()
            + ")))";

    return getConverse().converseList(command);
  }
  
  @Override
  public CycList getInterArgIsa1_2s(Fort predicate)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    String command = "(remove-duplicates \n" + "  (with-all-mts \n" + "    (inter-arg-isa1-2 "
            + predicate.stringApiValue() + ")))";

    return getConverse().converseList(command);
  }
  
  @Override
  public CycList getInterArgIsa1_2s(Fort predicate,
          CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    String command = "(remove-duplicates \n" + "  (with-all-mts \n" + "    (inter-arg-isa1-2 "
            + "      " + predicate.stringApiValue() + "      "
            + makeElMt_inner(mt).stringApiValue() + ")))";

    return getConverse().converseList(command);
  }
  
  @Override
  public CycList getInterArgIsa1_2_forArg2(Fort predicate,
          Fort arg1)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    CycArrayList result = new CycArrayList();
    ListIterator constraintPairs = getInterArgIsa1_2s(predicate).listIterator();

    while (constraintPairs.hasNext()) {
      CycArrayList pair = (CycArrayList) constraintPairs.next();

      if (pair.first().equals(arg1)) {
        result.add(pair.second());
      }
    }

    return result;
  }
  
  @Override
  public CycList getInterArgIsa1_2_forArg2(Fort predicate,
          Fort arg1,
          CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    CycArrayList result = new CycArrayList();
    ListIterator constraintPairs = getInterArgIsa1_2s(predicate,
            mt).listIterator();

    while (constraintPairs.hasNext()) {
      CycArrayList pair = (CycArrayList) constraintPairs.next();

      if (pair.first().equals(arg1)) {
        result.add(pair.second());
      }
    }

    return result;
  }
  
  @Override
  public CycList getResultIsas(Fort function)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseList("(remove-duplicates (with-all-mts (result-isa "
            + function.stringApiValue() + ")))");
  }
  
  @Override
  public CycList getResultIsas(Fort function,
          CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseList("(result-isa " + function.stringApiValue() + " "
            + makeElMt_inner(mt).stringApiValue() + ")");
  }
  
  @Override
  public CycList getArgNGenls(Fort predicate,
          int argPosition)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseList("(remove-duplicates (with-all-mts (argn-genl "
            + predicate.stringApiValue() + " " + argPosition + ")))");
  }
  
  @Override
  public CycList getArgNGenls(Fort predicate,
          int argPosition,
          CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseList("(argn-genl " + predicate.stringApiValue() + " " + argPosition + " "
            + makeElMt_inner(mt).stringApiValue() + ")");
  }
  
  @Override
  public CycList getArg1Formats(CycObject cycObject)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    //// Preconditions
    if (cycObject == null) {
      throw new NullPointerException("cycObject must not be null");
    }
    verifyPossibleDenotationalTerm(cycObject);
    return getConverse().converseList(
            "(with-all-mts (arg1-format " + cycObject.stringApiValue() + "))");
  }
  
  @Override
  public CycList getArg1Formats(CycObject cycObject,
          CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    //// Preconditions
    if (cycObject == null) {
      throw new NullPointerException("cycObject must not be null");
    }
    verifyPossibleDenotationalTerm(cycObject);
    if (cycObject == null) {
      throw new NullPointerException("cycObject must not be null");
    }
    return getConverse().converseList("(arg1-format " + cycObject.stringApiValue() + " "
            + makeElMt_inner(mt).stringApiValue() + ")");
  }
  
  @Override
  public CycList getArg2Formats(final CycObject cycObject)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    //// Preconditions
    if (cycObject == null) {
      throw new NullPointerException("cycObject must not be null");
    }
    verifyPossibleDenotationalTerm(cycObject);
    return getConverse().converseList(
            "(with-all-mts (arg2-format " + cycObject.stringApiValue() + "))");
  }
  
  @Override
  public CycList getArg2Formats(final CycObject cycObject,
          final CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    //// Preconditions
    if (cycObject == null) {
      throw new NullPointerException("cycObject must not be null");
    }
    verifyPossibleDenotationalTerm(cycObject);
    if (cycObject == null) {
      throw new NullPointerException("cycObject must not be null");
    }
    return getConverse().converseList("(arg2-format " + cycObject.stringApiValue() + " "
            + makeElMt_inner(mt).stringApiValue() + ")");
  }
  
  @Override
  public CycList getDisjointWiths(CycObject cycObject)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    //// Preconditions
    if (cycObject == null) {
      throw new NullPointerException("cycObject must not be null");
    }
    verifyPossibleDenotationalTerm(cycObject);
    return getConverse().converseList("(remove-duplicates (with-all-mts (local-disjoint-with "
            + cycObject.stringApiValue() + ")))");
  }
  
  @Override
  public CycList getDisjointWiths(Fort cycFort,
          CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseList("(local-disjoint-with " + cycFort.stringApiValue() + " "
            + makeElMt_inner(mt).stringApiValue() + ")");
  }

  /**
   * Gets a list of the coExtensionals for a Fort. Limited to 120 seconds.
   *
   * @param cycObject the given COLLECTION term
   *
   * @return a list of the coExtensionals for a Fort
   * @throws com.cyc.base.exception.CycConnectionException
   *


   * @throws CycApiException if the api request results in a cyc server error
   */
  public CycList getCoExtensionals(CycObject cycObject)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getCoExtensionals(cycObject, 0);
  }

  /**
   * Gets a list of the coExtensionals for a Fort. Limited to 120 seconds.
   *
   * @param cycObject the given COLLECTION term
   * @param timeoutMsecs the time to wait before giving up
   *
   * @return a list of the coExtensionals for a Fort
   * @throws com.cyc.base.exception.CycConnectionException
   *


   * @throws CycApiException if the api request results in a cyc server error
   * @throws CycTimeOutException if the calculation times out
   */
  public CycList getCoExtensionals(CycObject cycObject, long timeoutMsecs)
          throws CycConnectionException, com.cyc.base.exception.CycApiException, CycTimeOutException {
    return getCoExtensionals(cycObject, CommonConstants.INFERENCE_PSC, timeoutMsecs);
  }

  /**
   * Gets a list of the coExtensionals for a Fort. Limited to 120 seconds.
   *
   * @param cycObject the given COLLECTION term
   * @param mt the microtheory
   *
   * @return a list of the coExtensionals for a Fort
   * @throws com.cyc.base.exception.CycConnectionException
   *


   * @throws CycApiException if the api request results in a cyc server error
   */
  public CycList getCoExtensionals(CycObject cycObject, CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException, CycTimeOutException {
    return getCoExtensionals(cycObject, mt, 0);
  }

  /**
   * Gets a list of the coExtensionals for a Fort.
   *
   * @param cycObject the given COLLECTION term
   * @param mt the relevant mt for inference
   * @param timeoutMsecs
   *
   * @return a list of the coExtensionals for a Fort
   * @throws com.cyc.base.exception.CycConnectionException
   *


   * @throws CycApiException if the api request results in a cyc server error
   */
  public CycList getCoExtensionals(CycObject cycObject,
          CycObject mt, long timeoutMsecs)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    //// Preconditions
    if (cycObject == null) {
      throw new NullPointerException("cycObject must not be null");
    }
    verifyPossibleDenotationalTerm(cycObject);
    CycList answer = null;
    try {
      final String queryString =
              AND
              + "  (" + DIFFERENT + "  " + cycObject.cyclify() + " ?X) "
              + "  (" + OR + " (" + COEXTENSIONAL + " " + cycObject.cyclify() + " ?X) "
              + "    (" + COEXTENSIONAL_SET_OR_COLLECTIONS + " " + cycObject.cyclify() + " ?X)))";
      final FormulaSentence query = getCyc().getObjectTool().makeCycSentence(queryString);
      final CycVariable queryVariable = CycObjectFactory.makeCycVariable("?X");
      answer = getCyc().getInferenceTool().queryVariable(queryVariable, query, makeElMt_inner(mt), null,
              timeoutMsecs);
    } catch (CycConnectionException e) {
      if (IOException.class.isInstance(e)) {
        LOGGER.debug("getCoExtensionals - ignoring:\n" + e.getMessage());
        return new CycArrayList();
      } else {
        throw e;
      }
    }

    return getCyc().getObjectTool().canonicalizeList(answer);
  }
  
  @Override
  public Nart getCycNartFromCons(CycList elCons) throws CycConnectionException {
    if (getCyc().getInspectorTool().isReifiableFunction((CycObject) (elCons.get(0)))) {
      return new NartImpl(elCons);
    } else {
      return null;
    }
  }
  
  /* *
   * Gets a list of the public Cyc constants.
   *
   * @return a list of the public Cyc constants
   *

   * @throws IOException if a data communication error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  // Commented out regarding BASEAPI-63 - nwinant, 2014-08-18
  /*
  public CycList getPublicConstants()
          throws CycConnectionException, com.cyc.base.CycApiException {
    return getKbSubset(CommonConstants.PUBLIC_CONSTANT);
  }
  */
  
  /**
   * Gets a list of the elements of the given CycKBSubsetCollection.
   *
   * @param cycKbSubsetCollection the given CycKBSubsetCollection
   *
   * @return a list of the elements of the given CycKBSubsetCollection
   * @throws com.cyc.base.exception.CycConnectionException
   * @throws CycApiException if the api request results in a cyc server error
   */
  public CycList getKbSubset(Fort cycKbSubsetCollection)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    CycList answer = getConverse().converseList("(ask-template '?X '(" + ISA + " ?X "
            + cycKbSubsetCollection.stringApiValue() + ") " + EVERYTHING_PSC + ")");

    return getCyc().getObjectTool().canonicalizeList(answer);
  }
  
  /**
   * Gets a list of the backchaining implication rules which might apply to the given rule.
   *
   * @param predicate the PREDICATE for which backward chaining implication rules are sought
   * @param formula the literal for which backward chaining implication rules are sought
   * @param mt the microtheory (and its genlMts) in which the search for backchaining implication
 rules takes place
   *
   * @return a list of the backchaining implication rules which might apply to the given PREDICATE
   * @throws com.cyc.base.exception.CycConnectionException
   * @throws CycApiException if the api request results in a cyc server error
   */
  public CycList getBackchainImplicationRules(CycConstant predicate,
          CycList formula, CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    StringBuilder command = new StringBuilder();

    if (makeElMt_inner(mt).equals(CommonConstants.INFERENCE_PSC) || makeElMt_inner(mt).equals(
            CommonConstants.EVERYTHING_PSC)) {
      command.append("(clet (backchain-rules formula) ");
      command.append("  (with-all-mts ");
      command.append("    (do-predicate-rule-index (rule ").
              append(predicate.stringApiValue()).
              append(" :pos nil :backward) ");
      command.append("       (csetq formula (assertion-el-formula rule)) ");
      command.append("       (pwhen (cand (eq (first formula) ").append(IMPLIES).append(") ");
      command.append("                    (unify-el-possible ").
              append(formula.stringApiValue()).
              append(" ");
      command.append(
              "                                          (third formula))) ");
      command.append("         (cpush formula backchain-rules)))) ");
      command.append("   backchain-rules)");
    } else {
      command.append("(clet (backchain-rules formula) ");
      command.append("  (with-inference-mt-relevance ").
              append(makeElMt_inner(mt).stringApiValue()).
              append(" ");
      command.append("    (do-predicate-rule-index (rule ").
              append(predicate.stringApiValue()).
              append(" :pos nil :backward) ");
      command.append("       (csetq formula (assertion-el-formula rule)) ");
      command.append("       (pwhen (cand (eq (first formula) ").append(IMPLIES).append(") ");
      command.append("                    (unify-el-possible ").
              append(formula.stringApiValue()).
              append(" ");
      command.append(
              "                                          (third formula))) ");
      command.append("         (cpush formula backchain-rules)))) ");
      command.append("   backchain-rules)");
    }

    //getCyc().traceOn();
    return getConverse().converseList(command.toString());
  }

  /**
   * Gets a list of the forward chaining implication rules which might apply to the given rule.
   *
   * @param predicate the PREDICATE for which forward chaining implication rules are sought
   * @param formula the literal for which forward chaining implication rules are sought
   * @param mt the microtheory (and its genlMts) in which the search for forward chaining rules
 takes place
   *
   * @return a list of the forward chaining implication rules which might apply to the given
 PREDICATE
   * @throws com.cyc.base.exception.CycConnectionException
   * @throws CycApiException if the api request results in a cyc server error
   */
  public CycList getForwardChainRules(CycConstant predicate,
          CycList formula, CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    StringBuilder command = new StringBuilder();
    if (makeElMt_inner(mt).equals(CommonConstants.INFERENCE_PSC) || makeElMt_inner(mt).equals(
            CommonConstants.EVERYTHING_PSC)) {
      command.append("(clet (backchain-rules formula) ");
      command.append("  (with-all-mts ");
      command.append("    (do-predicate-rule-index (rule ").append(
              predicate.stringApiValue()).append(" :pos nil :forward) ");
      command.append("       (csetq formula (assertion-el-formula rule)) ");
      command.append("       (pwhen (cand (eq (first formula) ").append(IMPLIES).append(") ");
      command.append("                    (unify-el-possible ").append(
              formula.stringApiValue()).append(" ");
      command.append(
              "                                          (third formula))) ");
      command.append("         (cpush formula backchain-rules)))) ");
      command.append("   backchain-rules)");
    } else {
      command.append("(clet (backchain-rules formula) ");
      command.append("  (with-inference-mt-relevance ").append(
              makeElMt_inner(mt).stringApiValue()).append(" ");
      command.append("    (do-predicate-rule-index (rule ").append(
              predicate.stringApiValue()).append(" :pos nil :forward) ");
      command.append("       (csetq formula (assertion-el-formula rule)) ");
      command.append("       (pwhen (cand (eq (first formula) ").append(IMPLIES).append(") ");
      command.append("                    (unify-el-possible ").append(
              formula.stringApiValue()).append(" ");
      command.append(
              "                                          (third formula))) ");
      command.append("         (cpush formula backchain-rules)))) ");
      command.append("   backchain-rules)");
    }

    return getConverse().converseList(command.toString());
  }

  /**
   * Gets a list of the backchaining implication rules which might apply to the given PREDICATE.
   *
   * @param predicate the PREDICATE for which backchaining rules are sought
   * @param mt the microtheory (and its genlMts) in which the search for backchaining rules takes
 place
   *
   * @return a list of the backchaining implication rules which might apply to the given PREDICATE
   * @throws com.cyc.base.exception.CycConnectionException
   * @throws CycApiException if the api request results in a cyc server error
   */
  public CycList getBackchainRules(CycConstant predicate, CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    StringBuilder command = new StringBuilder();
    if (makeElMt_inner(mt).equals(CommonConstants.INFERENCE_PSC) || makeElMt_inner(mt).equals(
            CommonConstants.EVERYTHING_PSC)) {
      command.append("(clet (backchain-rules) ");
      command.append("  (with-all-mts ");
      command.append("    (do-predicate-rule-index (rule ").append(
              predicate.stringApiValue()).append(" ");
      command.append("                                :sense :pos ");
      command.append("                                :done nil ");
      command.append("                                :direction :backward) ");
      command.append(
              "       (pwhen (eq (first (assertion-el-formula rule)) ").append(IMPLIES).append(") ");
      command.append(
              "         (cpush (assertion-el-formula rule) backchain-rules)))) ");
      command.append("   backchain-rules)");
    } else {
      command.append("(clet (backchain-rules) ");
      command.append("  (with-inference-mt-relevance ").append(
              makeElMt_inner(mt).stringApiValue()).append(" ");
      command.append("    (do-predicate-rule-index (rule ").append(
              predicate.stringApiValue()).append(" ");
      command.append("                                :sense :pos ");
      command.append("                                :done nil ");
      command.append("                                :direction :backward) ");
      command.append(
              "       (pwhen (eq (first (assertion-el-formula rule)) ").append(IMPLIES).append(") ");
      command.append(
              "         (cpush (assertion-el-formula rule) backchain-rules)))) ");
      command.append("   backchain-rules)");
    }

    //getCyc().traceOn();
    return getConverse().converseList(command.toString());
  }

  /**
   * Gets a list of the forward chaining implication rules which might apply to the given
 PREDICATE.
   *
   * @param predicate the PREDICATE for which forward chaining rules are sought
   * @param mt the microtheory (and its genlMts) in which the search for forward chaining rules
 takes place
   *
   * @return a list of the forward chaining implication rules which might apply to the given
 PREDICATE
   * @throws com.cyc.base.exception.CycConnectionException
   * @throws CycApiException if the api request results in a cyc server error
   */
  public CycList getForwardChainRules(CycConstant predicate,
          CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    StringBuilder command = new StringBuilder();

    if (makeElMt_inner(mt).equals(CommonConstants.INFERENCE_PSC) || makeElMt_inner(mt).equals(
            CommonConstants.EVERYTHING_PSC)) {
      command.append("(clet (backchain-rules) ");
      command.append("  (with-all-mts ");
      command.append("    (do-predicate-rule-index (rule ").append(
              predicate.stringApiValue()).append(" ");
      command.append("                                :sense :pos ");
      command.append("                                :done nil ");
      command.append("                                :direction :forward) ");
      command.append(
              "       (pwhen (eq (first (assertion-el-formula rule)) ").append(IMPLIES).append(") ");
      command.append(
              "         (cpush (assertion-el-formula rule) backchain-rules)))) ");
      command.append("   backchain-rules)");
    } else {
      command.append("(clet (backchain-rules) ");
      command.append("  (with-inference-mt-relevance ").append(
              makeElMt_inner(mt).stringApiValue()).append(" ");
      command.append("    (do-predicate-rule-index (rule ").append(
              predicate.stringApiValue()).append(" ");
      command.append("                                :sense :pos ");
      command.append("                                :done nil ");
      command.append("                                :direction :forward) ");
      command.append(
              "       (pwhen (eq (first (assertion-el-formula rule)) ").append(IMPLIES).append(") ");
      command.append(
              "         (cpush (assertion-el-formula rule) backchain-rules)))) ");
      command.append("   backchain-rules)");
    }

    return getConverse().converseList(command.toString());
  }
  
  @Override
  public boolean hasSomePredicateUsingTerm(CycConstant predicate,
          Fort term,
          Integer argumentPosition,
          CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    String command;
    if (makeElMt_inner(mt).equals(CommonConstants.INFERENCE_PSC) || makeElMt_inner(mt).equals(CommonConstants.EVERYTHING_PSC)) {
      command = makeSublStmt(SOME_PRED_VALUE_IN_ANY_MT, term, predicate,
              argumentPosition);
    } else {
      command = makeSublStmt(SOME_PRED_VALUE_IN_RELEVANT_MTS, term, predicate,
              makeElMt_inner(mt), argumentPosition);
    }

    //getCyc().traceOn();
    return getConverse().converseBoolean(command);
  }
  
  @Override
  public int getArity(Fort predicate)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    String command = makeSublStmt(WITH_ALL_MTS,
            makeNestedSublStmt("arity", predicate));
    Object object = getConverse().converseObject(command);
    if (object instanceof Integer) {
      return (Integer) object;
    } else {
      return 0;
    }
  }
  
  @Override
  public CycList getArg2s(Fort predicate,
          Object arg1)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {

    CycVariable variable = CycObjectFactory.makeCycVariable("?arg2");
    final FormulaSentence query = FormulaSentenceImpl.makeFormulaSentence(
            predicate, arg1, variable);

    return getCyc().getInferenceTool().queryVariable(variable, query, CommonConstants.INFERENCE_PSC);
  }
  
  @Override
  public Object getArg2(Fort predicate,
          Object arg1)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    CycList arg2s = getArg2s(predicate, arg1);
    if (arg2s.isEmpty()) {
      return null;
    } else {
      return arg2s.first();
    }
  }

  /**
   * Returns the list of gafs in which the PREDICATE is a element of the given list of PREDICATEs
 and in which the given term appears in the first argument position.
   *
   * @param cycObject the given term
   * @param predicates the given list of PREDICATEs
   * @param mt the relevant inference microtheory
   *
   * @return the list of gafs in which the PREDICATE is a element of the given list of PREDICATEs
 and in which the given term appears in the first argument position
   * @throws com.cyc.base.exception.CycConnectionException
   * @throws CycApiException if the Cyc server returns an error
   */
  public CycList getGafsForPredicates(final CycObject cycObject,
          final List predicates,
          final CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    //// Preconditions
    if (cycObject == null) {
      throw new NullPointerException("cycObject must not be null");
    }
    verifyPossibleDenotationalTerm(cycObject);
    if (predicates == null) {
      throw new NullPointerException("predicates must not be null");
    }
    if (predicates == null) {
      throw new NullPointerException("predicates must not be null");
    }
    final CycArrayList result = new CycArrayList();

    for (int i = 0; i < predicates.size(); i++) {
      result.addAllNew(getGafs(cycObject,
              (Fort) predicates.get(
              i),
              makeElMt_inner(mt)));
    }

    return result;
  }
  
  @Override
  public CycList getGafs(final CycObject cycFort,
          final CycObject predicate,
          final CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    if (cycFort == null) {
      throw new NullPointerException("cycFort must not be null");
    }
    if (predicate == null) {
      throw new NullPointerException("predicate must not be null");
    }
    if (mt == null) {
      throw new NullPointerException("mt must not be null");
      // TODO handle the case where the cycObject is a NAUT,
      //getGafsForNaut
    }
    final CycArrayList gafs = new CycArrayList();
    final String command = "(with-inference-mt-relevance " + makeElMt_inner(mt).stringApiValue() + "\n"
            + "  (pred-values-in-relevant-mts " + cycFort.stringApiValue() + " "
            + predicate.stringApiValue() + "))";
    final CycList values = getConverse().converseList(command);

    for (int i = 0; i < values.size(); i++) {
      final CycArrayList gaf = new CycArrayList();
      gaf.add(predicate);
      gaf.add(cycFort);
      gaf.add(values.get(i));
      gafs.add(gaf);
    }

    return gafs;
  }

  /**
   * Returns the list of gafs in which the PREDICATE is a element of the given list of PREDICATEs
 and in which the given term appears in the first argument position.
   *
   * @param cycObject the given term
   * @param predicates the given list of PREDICATEs
   *
   * @return the list of gafs in which the PREDICATE is a element of the given list of PREDICATEs
 and in which the given term appears in the first argument position
   * @throws com.cyc.base.exception.CycConnectionException
   * @throws CycApiException if the Cyc server returns an error
   */
  public CycList getGafsForPredicates(final CycObject cycObject,
          final List predicates)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    //// Preconditions
    if (cycObject == null) {
      throw new NullPointerException("cycObject must not be null");
    }
    verifyPossibleDenotationalTerm(cycObject);
    if (predicates == null) {
      throw new NullPointerException("predicates must not be null");
    }
    final CycArrayList result = new CycArrayList();
    for (int i = 0; i < predicates.size(); i++) {
      result.addAllNew(getGafs(cycObject, (CycObject) predicates.get(i)));
    }

    return result;
  }
  
  @Override
  public CycList getGafs(final CycObject cycObject,
          final CycObject predicate)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    //// Preconditions
    if (cycObject == null) {
      throw new NullPointerException("cycObject must not be null");
    }
    verifyPossibleDenotationalTerm(cycObject);
    if (cycObject instanceof CycArrayList) {
      return getGafsForNaut((CycArrayList) cycObject, predicate);
    }
    final CycArrayList gafs = new CycArrayList();
    // TODO handle the case where the cycObject is a NAUT,
    //getGafsForNaut
    final String command = "(with-all-mts \n" + "  (pred-values-in-relevant-mts (canonicalize-term "
            + cycObject.stringApiValue() + ") " + "(canonicalize-term "
            + predicate.stringApiValue() + ")))";
    final CycList values = getConverse().converseList(command);
    for (int i = 0; i < values.size(); i++) {
      final CycArrayList gaf = new CycArrayList();
      gaf.add(predicate);
      gaf.add(cycObject);
      gaf.add(values.get(i));
      gafs.add(gaf);
    }
    return gafs;
  }

  /**
   * Returns the list of gafs in which the PREDICATE is the given PREDICATE and in which the given
 NAUT appears in the first argument position.
   *
   * @param naut the given NAUT
   * @param predicate the given PREDICATE
   *
   * @return the list of gafs in which the PREDICATE is a element of the given list of PREDICATEs
 and in which the given term appears in the first argument position
   * @throws com.cyc.base.exception.CycConnectionException
   * @throws CycApiException if the Cyc server returns an error
   */
  public CycList getGafsForNaut(final CycList naut,
          final CycObject predicate)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    final String command =
            "(clet (assertions) "
            + "  (do-gaf-arg-index-naut (assertion " + naut.stringApiValue() + ")"
            + "    (pwhen (equal (formula-arg1 assertion) " + predicate.stringApiValue() + ")"
            + "      (cpush assertion assertions))) "
            + "  assertions)";
    final CycList gafs = getConverse().converseList(command);

    //// Postconditions
    assert gafs != null : "gafs cannot be null";

    return gafs;
  }
  
  @Override
  public CycList getPredicateValueTuplesInMt(Fort term, Fort predicate,
          int indexArg, CycList gatherArgs, CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    CycArrayList tuples = new CycArrayList();
    String command = makeSublStmt("pred-value-tuples-in-mt", term, predicate,
            indexArg,
            gatherArgs, makeElMt_inner(mt));
    return getConverse().converseList(command);
  }

  /**
   * Gets the list of mappings from the specified information source given the inference
 microtheory. Each returned list item is the pair consisting of external concept string and
 synonymous Cyc term.
   *
   * @param informationSource the external indexed information source
   * @param mt the assertion microtheory
   *
   * @return list of mappings from the specified information source given the inference
 microtheory
   * @throws com.cyc.base.exception.CycConnectionException
   * @throws CycApiException if the Cyc server returns an error
   */
  public CycList getSynonymousExternalConcepts(String informationSource,
          String mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getSynonymousExternalConcepts(getKnownConstantByName(
            informationSource),
            getKnownConstantByName(mt));
  }

  /**
   * Gets the list of mappings from the specified information source given the inference
 microtheory. Each returned list item is the pair consisting of external concept string and
 synonymous Cyc term.
   *
   * @param informationSource the external indexed information source
   * @param mt the assertion microtheory
   *
   * @return the list of mappings from the specified information source given the inference
 microtheory
   * @throws com.cyc.base.exception.CycConnectionException
   * @throws CycApiException if the Cyc server returns an error
   */
  public CycList getSynonymousExternalConcepts(Fort informationSource,
          CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    CycArrayList variables = new CycArrayList();
    CycVariable cycTermVar = CycObjectFactory.makeCycVariable("?cyc-term");
    variables.add(cycTermVar);

    CycVariable externalConceptVar = CycObjectFactory.makeCycVariable(
            "?externalConcept");
    variables.add(externalConceptVar);

    final FormulaSentence query = FormulaSentenceImpl.makeFormulaSentence(
            CommonConstants.SYNONYMOUS_EXTERNAL_CONCEPT,
            cycTermVar, informationSource, externalConceptVar);

    return getCyc().getInferenceTool().queryVariables(variables, query, makeElMt_inner(mt),
            new DefaultInferenceParameters(getCyc()));
  }
  
  @Override
  public CycList getNameStrings(Fort cycFort,
          CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    // (#$nameString <cycFort> ?name-string)

    CycVariable variable = CycObjectFactory.makeCycVariable("?name-string");
    final FormulaSentence query = FormulaSentenceImpl.makeFormulaSentence(
            CommonConstants.NAME_STRING,
            cycFort, variable);

    return getCyc().getInferenceTool().queryVariable(variable, query, makeElMt_inner(mt));
  }

  @Override
  public CycSentence getSimplifiedSentence(FormulaSentence sentence) 
          throws CycConnectionException, OpenCycUnsupportedFeatureException {
    return sentence.getSimplifiedSentence(getCyc());
  }

  @Override
  public CycSentence getSimplifiedSentence(FormulaSentence sentence, ElMt mt) 
          throws CycConnectionException, OpenCycUnsupportedFeatureException {
    return sentence.getSimplifiedSentence(getCyc(), mt);
  }
  
  @Override
  public CycConstantImpl getRandomConstant()
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return (CycConstantImpl) getConverse().converseObject("(random-constant)");
  }
  
  @Override
  public Nart getRandomNart()
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return (Nart) getConverse().converseObject("(random-nart)");
  }
  
  @Override
  public CycAssertionImpl getRandomAssertion()
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return (CycAssertionImpl) getConverse().converseObject("(random-assertion)");
  }
  
  @Override
  public Long getAssertionDate(CycAssertion cycAssertion)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    String command = makeSublStmt("asserted-when", cycAssertion);
    Object date = getConverse().converseObject(command);
    if (date instanceof Integer) {
      return ((Integer) date).longValue();
    }
    if (date instanceof Long) {
      return (Long) date;
    }
    if (date instanceof BigInteger) {
      return ((BigInteger)date).longValue();
    }
    if (date.equals(CycObjectFactory.nil)) {
      return 0L;
    } else {
      throw new com.cyc.base.exception.CycApiException(
              "unexpected type of date returned " + date.toString());
    }
  }

  @Override
  public Fort getTermCreator(Fort term) throws CycConnectionException, CycApiException {
    String command = makeSublStmt("creator", term);
    CycObject co = getConverse().converseCycObject(command);
    if (co instanceof Fort) {
      return (Fort)co;
    } else {
      return null;
    }
  }
  
  @Override
  public Fort getAssertionCreator(CycAssertion assertion) throws CycConnectionException, CycApiException {
    String command = makeSublStmt("creator", assertion);
    CycObject co = getConverse().converseCycObject(command);
    if (co instanceof Fort) {
      return (Fort)co;
    } else {
      return null;
    }
  }
  
  @Override
  public Date getAssertionCreationDate(CycAssertion cycAssertion)
          throws CycConnectionException, com.cyc.base.exception.CycApiException, ParseException {
    Long num = getAssertionDate(cycAssertion);
    if (num == 0L){
      return null;
    }
    SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
    return sf.parse(num.toString());
  }
  
  @Override
  public Date getTermCreationDate(Fort term)
          throws CycConnectionException, com.cyc.base.exception.CycApiException, ParseException {
    String command = makeSublStmt("created-when", term);
    Object date = getConverse().converseObject(command);
    Long num = 0L;
    if (date instanceof Integer) {
      num = ((Integer) date).longValue();
    } else if (date instanceof Long) {
      num =  (Long) date;
    } else if (date instanceof BigInteger) {
      num = ((BigInteger)date).longValue();
    } else {
      throw new com.cyc.base.exception.CycApiException(
              "unexpected type of date returned " + date.toString());
    }
    
    if (num == 0l){
      return null;
    }
    SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
    return sf.parse(num.toString());
  }
  
  @Override
  public CycList getPredExtent(CycObject pred, CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    String command = "(GATHER-PREDICATE-EXTENT-INDEX " + pred.stringApiValue() + (mt != null ? " "+mt.stringApiValue() : "") + ")";
    CycList result = getConverse().converseList(command);
    return result;
  }
  
  // Private
  /*
  private String getGeneratedPhrase(CycObject cycObject, boolean precise,
          CycObject languageMt)
          throws CycConnectionException, com.cyc.baseclient.api.CycApiException {
    //// Preconditions
    if (cycObject == null) {
      throw new NullPointerException("cycObject must not be null");
    }
    final NLFormat nlf = NLFormat.getInstance(getCyc());
    nlf.setPrecise(precise);
    if (languageMt != null) {
      nlf.setFormatLanguageMt(languageMt);
    }
    return nlf.format(cycObject);
  }
  */
  
  // Protected
  
  protected void verifyPossibleDenotationalTerm(CycObject cycObject) throws IllegalArgumentException {
    if (!(cycObject instanceof DenotationalTerm || cycObject instanceof CycList)) {
      throw new IllegalArgumentException(
              "cycObject must be a Cyc denotational term " + cycObject.cyclify());
    }
  }
  
  
  // Internal
  
  private static final Logger LOGGER = LoggerFactory.getLogger(LookupToolImpl.class);
  
  private static final CycSymbolImpl SOME_PRED_VALUE_IN_ANY_MT = makeCycSymbol(
          "some-pred-value-in-any-mt");
  
  private static final CycSymbolImpl SOME_PRED_VALUE_IN_RELEVANT_MTS = makeCycSymbol(
          "some-pred-value-in-relevant-mts");
  
  private static final CycSymbolImpl FPRED_VALUE_IN_MT = makeCycSymbol("fpred-value-in-mt");
  
  private static final String ISA = CommonConstants.ISA.cyclify();
  private static final String IMPLIES = CommonConstants.IMPLIES.cyclify();
  private static final String AND = CommonConstants.AND.cyclify();
  private static final String DIFFERENT = CommonConstants.DIFFERENT.cyclify();
  private static final String OR = CommonConstants.OR.cyclify();
  private static final String COEXTENSIONAL = CommonConstants.CO_EXTENSIONAL.cyclify();
  private static final String COEXTENSIONAL_SET_OR_COLLECTIONS = CommonConstants.COEXTENSIONAL_SET_OR_COLLECTIONS.cyclify();
  private static final String EVERYTHING_PSC = CommonConstants.EVERYTHING_PSC.cyclify();
}
