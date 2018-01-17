package com.cyc.baseclient.kbtool;

/*
 * #%L
 * File: InspectorToolImpl.java
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
import com.cyc.base.cycobject.CycConstant;
import com.cyc.base.cycobject.CycList;
import com.cyc.base.cycobject.CycObject;
import com.cyc.base.cycobject.CycSymbol;
import com.cyc.base.cycobject.CycVariable;
import com.cyc.base.cycobject.ElMt;
import com.cyc.base.cycobject.Formula;
import com.cyc.base.cycobject.FormulaSentence;
import com.cyc.base.cycobject.Fort;
import com.cyc.base.cycobject.Naut;
import com.cyc.base.cycobject.NonAtomicTerm;
import com.cyc.base.exception.CycApiException;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.base.exception.CycTimeOutException;
import com.cyc.base.inference.InferenceResultSet;
import com.cyc.base.kbtool.InspectorTool;
import com.cyc.baseclient.AbstractKbTool;
import com.cyc.baseclient.CommonConstants;
import com.cyc.baseclient.CycObjectFactory;
import com.cyc.baseclient.cycobject.CycSymbolImpl;
import com.cyc.baseclient.cycobject.CycVariableImpl;
import com.cyc.baseclient.cycobject.DefaultCycObjectImpl;
import com.cyc.baseclient.cycobject.ElMtConstantImpl;
import com.cyc.baseclient.cycobject.FormulaSentenceImpl;
import com.cyc.baseclient.cycobject.NautImpl;
import com.cyc.baseclient.datatype.Pair;
import com.cyc.baseclient.inference.params.DefaultInferenceParameters;
import com.cyc.baseclient.util.LruCache;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static com.cyc.baseclient.CommonConstants.GENERIC_INSTANCE_FN;
import static com.cyc.baseclient.CycObjectFactory.makeCycSymbol;
import static com.cyc.baseclient.connection.SublApiHelper.makeNestedSublStmt;
import static com.cyc.baseclient.connection.SublApiHelper.makeSublStmt;
import static com.cyc.baseclient.subl.functions.SublFunctions.CATEGORIZE_TERM_WRT_API;

/**
 * Tools for examining individual CycObjects. To examine the relationship between different
 * CycObjects, use the {@link com.cyc.baseclient.kbtool.ComparisonToolImpl}.
 *
 * @see com.cyc.baseclient.kbtool.ComparisonToolImpl
 * @author nwinant
 */
public class InspectorToolImpl extends AbstractKbTool implements InspectorTool {

  public InspectorToolImpl(CycAccess client) {
    super(client);
  }

  // Public
  
  @Override
  public int countAllInstances(Fort collection,
          CycObject mt)
          throws CycConnectionException, CycApiException {
    return getConverse().converseInt("(count-all-instances " + collection.stringApiValue() + " "
            + makeElMt_inner(mt).stringApiValue() + ")");
  }
  
  @Override
  public int countAllInstances_Cached(Fort collection, CycObject mt)
          throws CycConnectionException, CycApiException {
    Integer countAllInstances = countAllInstancesCache.get(collection);

    if (countAllInstances != null) {
      return countAllInstances;
    }

    final int answer = countAllInstances(collection, makeElMt_inner(mt));
    countAllInstancesCache.put(collection, answer);

    return answer;
  }

  @Override
  public boolean isConstantInKB(CycConstant obj)
          throws CycConnectionException, CycApiException {
    if (obj != null) {
      final CycConstant c = this.getCyc().getLookupTool().getConstantByGuid(obj.getGuid());
      return obj.equals(c) && obj.getName().equals(c.getName());
    }
    return false;
  }

  @Override
  public boolean isElMtInKB(ElMt obj)
          throws CycConnectionException, CycApiException {
    if (obj != null) {
      if (ElMtConstantImpl.class.isInstance(obj)) {
        return isConstantInKB((ElMtConstantImpl) obj);
      }
    }
    return false;
  }
  
  @Override
  public boolean isa(CycObject term,
          String collectionName)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return isa(term,
            getKnownConstantByName_inner(collectionName));
  }
  
  @Override
  public boolean isa(CycObject term,
          Fort collection)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseBoolean("(fif (fboundp 'isa-in-any-mt-cached?) (isa-in-any-mt-cached? " + term.stringApiValue() + " "
            + collection.stringApiValue() + ") (isa-in-any-mt? " + term.stringApiValue() + " "
            + collection.stringApiValue() + "))");
  }
  
  @Override
  public boolean isa(CycObject term,
          CycObject collection,
          CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    final String command = "(isa? " + term.stringApiValue() + " "
            + collection.stringApiValue() + " " + makeElMt_inner(mt).stringApiValue() + ")";

    return getConverse().converseBoolean(command);
  }

  @Override
  public CycObject categorizeTermWRTApi(CycObject term) throws CycConnectionException {
    // TODO: investigate doing this for NARTs as well.

    if (term instanceof Naut) {
      Naut cycNaut = (Naut) term;
      CycObject functor = (CycObject) cycNaut.getArg0();
      CycObject nautCategory = getCategoryForFunctor(functor);
      if (nautCategory == null) {
        nautCategory = getKnownCategoryForNat(cycNaut);
      }
      if (nautCategory != null) {
        return nautCategory;
      }
    }
    return CATEGORIZE_TERM_WRT_API.eval(getCyc(), term);
  }

  CycObject getCategoryForFunctor(CycObject functor) {
    final Map<CycObject, CycObject> functorMap = getCategorizedFunctorMap();
    CycObject mapValue = functorMap.get(functor);
    if (mapValue != null && !(mapValue instanceof CycSymbol)) {
      return mapValue;
    } else if (mapValue == null) {
      //run query using: 
      // executeQuery
      //(and (resultIsa <FUNCTOR> ?RESULT)
      //       (unknownSentence (thereExists ?DIFFICULTY (or (resultIsaArg ?FUNCTION ?DIFFICULTY)
      //                                                     (resultIsaArgIsa ?FUNCTION ?DIFFICULTY)))))
      //if there are bindings for result, check the categories of (GenericInstanceFn ?RESULT), and if they're all the same,
      //use them.  It might also work to use the single category from (GenericInstanceFn (CollectionUnionFn ?RESULTS))
    }
    // TODO: if it's not in the map, we should see if we can add it to the map.  We can add it
    //if there are no resultIsaArg or resultIsaArgIsa assertions on the functor.  In that case, 
    //we could categorize a GenericInstanceFn of the resultIsa, and cache that value
    //for all uses of the functor.      
    return null;
  }

  private Map<CycObject, CycObject> getCategorizedFunctorMap() {
    if (categorizedFunctorMap == null) {
      categorizedFunctorMap = new HashMap<>();
      categorizedFunctorMap.put(CommonConstants.PERCENT, CommonConstants.INDIVIDUAL);
      categorizedFunctorMap.put(CommonConstants.YEAR_FN, CommonConstants.INDIVIDUAL);
      categorizedFunctorMap.put(CommonConstants.MONTH_FN, CommonConstants.INDIVIDUAL);
      categorizedFunctorMap.put(CommonConstants.DAY_FN, CommonConstants.INDIVIDUAL);
      categorizedFunctorMap.put(CommonConstants.HOUR_FN, CommonConstants.INDIVIDUAL);
      categorizedFunctorMap.put(CommonConstants.MINUTE_FN, CommonConstants.INDIVIDUAL);
      categorizedFunctorMap.put(CommonConstants.SECOND_FN, CommonConstants.INDIVIDUAL);
    }
    return categorizedFunctorMap;

  }

  CycObject getKnownCategoryForNat(NonAtomicTerm nat) {
    if (nat.getFunctor().equals(CommonConstants.SCHEMA_OBJECT_FN)) {
      CycObject schema = (CycObject) nat.getArgument(1);
      if (!schemaIsaMap.containsKey(schema)) {
        CycVariable isaVar = new CycVariableImpl("ISA");
        FormulaSentence query = FormulaSentenceImpl.makeFormulaSentence(
                CommonConstants.SCHEMA_ISA, schema, isaVar);
        try {
          InferenceResultSet result = getCyc().getInferenceTool().executeQuery(query, CommonConstants.INFERENCE_PSC, new DefaultInferenceParameters(getCyc()));
          result.beforeFirst();
          CycObject schemaType = null;
          while (result.next()) {
            CycObject schemaIsa = result.getCycObject(isaVar);
            CycObject genericInstance = new NautImpl(GENERIC_INSTANCE_FN, schemaIsa);
            CycObject type = categorizeTermWRTApi(genericInstance);
            if (schemaType != null && !schemaType.equals(type)) {
              //there are multiple schemaIsas, so put in null to specify that we can't tell, and we shouldn't ask again.
              schemaIsaMap.put(schema, null);
              return null;
            }
            schemaType = type;
          }
          schemaIsaMap.put(schema, schemaType);
        } catch (CycConnectionException | CycApiException | CycTimeOutException | SQLException ex) {
          throw new CycApiException(ex);
        }
      }
      return schemaIsaMap.get(schema);
    }
    return null;
  }
  
  @Override
  public boolean isQuotedIsa(final CycObject term, final CycObject collection)
          throws CycConnectionException, com.cyc.base.exception.CycApiException, CycTimeOutException {
    return isQuotedIsa(term, collection, 0);
  }

  /**
   * Returns true if the quoted CycFort TERM is a instance of CycFort COLLECTION, in any
   * microtheory. Method implementation optimised for the binary api.
   *
   * @param term the term
   * @param collection the COLLECTION
   * @param timeoutMsecs the time in milliseconds to wait before giving up, set to zero to wait
   * forever.
   *
   * @return <tt>true</tt> if the quoted CycFort TERM is a instance of CycFort COLLECTION
   * @throws com.cyc.base.exception.CycConnectionException
   * @throws CycApiException if the api request results in a cyc server error
   * @throws CycTimeOutException if the calculation times out
   */
  public boolean isQuotedIsa(final CycObject term, final CycObject collection,
          long timeoutMsecs)
          throws CycConnectionException, com.cyc.base.exception.CycApiException, CycTimeOutException {
    //// Preconditions
    if (term == null) {
      throw new NullPointerException("term must not be null");
    }
    if (collection == null) {
      throw new NullPointerException("collection must not be null");
    }
    final FormulaSentence query = FormulaSentenceImpl.makeFormulaSentence(
            CommonConstants.QUOTED_ISA, term, collection);
    return getCyc().getInferenceTool().isQueryTrue(query, CommonConstants.INFERENCE_PSC, null, timeoutMsecs);
  }
  
  @Override
  public boolean isQuotedIsa(final CycObject term, final CycObject collection,
          final CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException, CycTimeOutException {
    return isQuotedIsa(term, collection, mt, 0);
  }
  
  public boolean isQuotedIsa(final CycObject term, final CycObject collection,
          final CycObject mt, long timeoutMsecs)
          throws CycConnectionException, com.cyc.base.exception.CycApiException, CycTimeOutException {
    if (term == null) {
      throw new NullPointerException("Term must not be null.");
    }
    if (collection == null) {
      throw new NullPointerException("Collection must not be null.");
    }
    if (mt == null) {
      throw new NullPointerException("Microtheory must not be null.");
    }
    final FormulaSentence query = FormulaSentenceImpl.makeFormulaSentence(
            CommonConstants.QUOTED_ISA, term, collection);
    return getCyc().getInferenceTool().isQueryTrue(query, makeElMt_inner(mt), null, timeoutMsecs);
  }
  
  @Override
  public boolean isBackchainRequired(CycConstant predicate,
          CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
//    CycConstantImpl backchainRequired = getKnownConstantByGuid_inner(
//            "beaa3d29-9c29-11b1-9dad-c379636f7270");

    return getCyc().getLookupTool().hasSomePredicateUsingTerm(CommonConstants.BACKCHAIN_REQUIRED,
            predicate, 1, makeElMt_inner(mt));
  }
  
  @Override
  public boolean isBackchainEncouraged(CycConstant predicate,
          CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    // CycConstantImpl backchainEncouraged = getKnownConstantByGuid_inner(
    //         "c09d1cea-9c29-11b1-9dad-c379636f7270");

    return getCyc().getLookupTool().hasSomePredicateUsingTerm(CommonConstants.BACKCHAIN_ENCOURAGED,
            predicate, 1, makeElMt_inner(mt));
  }
  
  @Override
  public boolean isBackchainDiscouraged(CycConstant predicate,
          CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    //CycConstantImpl backchainDiscouraged = getKnownConstantByGuid_inner(
    //        "bfcbce14-9c29-11b1-9dad-c379636f7270");

    return getCyc().getLookupTool().hasSomePredicateUsingTerm(CommonConstants.BACKCHAIN_DISCOURAGED,
            predicate, 1, makeElMt_inner(mt));
  }
  
  @Override
  public boolean isBackchainForbidden(CycConstant predicate,
          CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    //CycConstantImpl backchainForbidden = getKnownConstantByGuid_inner(
    //        "bfa4e9d2-9c29-11b1-9dad-c379636f7270");

    return getCyc().getLookupTool().hasSomePredicateUsingTerm(CommonConstants.BACKCHAIN_FORBIDDEN,
            predicate, 1, makeElMt_inner(mt));
  }
  
  @Override
  public boolean isIrreflexivePredicate(CycConstant predicate,
          CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    //CycConstantImpl irreflexiveBinaryPredicate = getKnownConstantByGuid_inner(
    //        "bd654be7-9c29-11b1-9dad-c379636f7270");

    return this.isa(predicate,
            CommonConstants.IRREFLEXIVE_BINARY_PREDICATE,
            makeElMt_inner(mt));
  }
  
  @Override
  public boolean isWellFormedFormula(CycList cycList)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return isWellFormedFormulaInternal(cycList);
  }
  
  @Override
  public boolean isWellFormedFormula(Formula formula)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return isWellFormedFormulaInternal(formula);
  }
  
  @Override
  public boolean isGafValidAssertion(CycList gaf, ElMt mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    String command = makeSublStmt("find-gaf", gaf, mt);
    Object response = getConverse().converseObject(command);
    return !response.equals(CycObjectFactory.nil);
  }
  
  @Override
  public boolean isGafValidAssertion(FormulaSentence gaf, ElMt mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return isGafValidAssertion(gaf.getArgs(), mt);
  }
  
  @Override
  public boolean isAssertionValid(CycList hlFormula, Fort mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    String command = makeSublStmt("find-assertion", hlFormula, mt);
    Object response = getConverse().converseObject(command);
    return !response.equals(CycObjectFactory.nil);
  }
  
  @Override
  public boolean isBinaryPredicate(final CycObject cycObject)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    //// Preconditions
    if (cycObject == null) {
      throw new NullPointerException("cycObject must not be null");
    }
    verifyPossibleDenotationalTerm(cycObject);
    return getConverse().converseBoolean(
            "(binary-predicate? " + cycObject.stringApiValue() + ")");
  }
  
  @Override
  public boolean isValidConstantName(String candidateName)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseBoolean("(new-constant-name-spec-p " + DefaultCycObjectImpl.stringApiValue(candidateName) + ")");
  }
  
  @Override
  public boolean isConstantNameAvailable(String candidateName)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseBoolean("(constant-name-available \"" + candidateName + "\")");
  }
  
  @Override
  public boolean isQuotedCollection(Fort cycFort)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    throw new com.cyc.base.exception.CycApiException(
            "quotedCollection is no longer supported, see Quote");
  }
  
  @Override
  public boolean isQuotedCollection(Fort cycFort,
          CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    throw new com.cyc.base.exception.CycApiException(
            "quotedCollection is no longer supported, see Quote");
  }
  
  @Override
  public boolean isGround(CycObject expression) throws CycConnectionException {
    return getConverse().converseBoolean("(ground? " + DefaultCycObjectImpl.stringApiValue(
            expression) + ")");
  }

  /* *
   * Returns true if cycConstant is a PublicConstant.
   *
   * @param cycConstant the given constant
   *
   * @return true if cycConstant is a PublicConstant
   *
   * @throws UnknownHostException if cyc server host not found on the network
   * @throws IOException if a data communication error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  // Commented out regarding BASEAPI-63 - nwinant, 2014-08-18
  /*
  @Override
  public boolean isPublicConstant(CycConstant cycConstant)
          throws CycConnectionException, com.cyc.base.CycApiException {
    return getConverse().converseBoolean("(isa-in-any-mt? " + cycConstant.stringApiValue()
            + " " + PUBLIC_CONSTANT + ")");
  }
   */
  
  @Override
  public boolean isMicrotheory(CycObject term)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseBoolean(
            "(fif (fboundp 'isa-in-any-mt-cached?) "
            + "(isa-in-any-mt-cached? " + term.stringApiValue() + " " + MICROTHEORY + ")"
            + "(isa-in-any-mt? " + term.stringApiValue() + " " + MICROTHEORY + "))");
  }
  
  @Override
  public boolean isCollection(final CycObject cycObject)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    //// Preconditions
    if (cycObject == null) {
      throw new NullPointerException("cycObject must not be null");
    }
    verifyPossibleDenotationalTerm(cycObject);
    return getConverse().converseBoolean(
            "(fif (fboundp 'isa-in-any-mt-cached?) "
            + "(isa-in-any-mt-cached? " + cycObject.stringApiValue() + " " + COLLECTION + ")"
            + "(isa-in-any-mt? " + cycObject.stringApiValue() + " " + COLLECTION + "))");
  }
  
  @Override
  public boolean isCollection(final Object obj)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    //// Preconditions
    if (obj == null) {
      throw new NullPointerException("term must not be null");
    }
    if (obj instanceof CycObject) {
      return isCollection((CycObject) obj);
    } else {
      return false;
    }
  }
  
  @Override
  public boolean isCollection_Cached(CycObject cycObject)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    //// Preconditions
    if (cycObject == null) {
      throw new NullPointerException("cycObject must not be null");
    }
    verifyPossibleDenotationalTerm(cycObject);
    Boolean isCollection = isCollectionCache.get(cycObject);

    if (isCollection != null) {
      return isCollection;
    }

    final boolean answer = isCollection(cycObject);
    isCollectionCache.put(cycObject, answer);

    return answer;
  }

  @Override
  public boolean isCollection_Cached(Object term)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    if (term instanceof CycObject) {
      return isCollection_Cached((CycObject) term);
    } else {
      return false;
    }
  }
  
  @Override
  public boolean isIndividual(final CycObject cycObject)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    //// Preconditions
    if (cycObject == null) {
      throw new NullPointerException("cycObject must not be null");
    }
    verifyPossibleDenotationalTerm(cycObject);
    return getConverse().converseBoolean(
            "(fif (fboundp 'isa-in-any-mt-cached?) "
            + "(isa-in-any-mt-cached? " + cycObject.stringApiValue() + " " + INDIVIDUAL + ")"
            + "(isa-in-any-mt? " + cycObject.stringApiValue() + " " + INDIVIDUAL + "))");
  }
  
  @Override
  public boolean isFunction(CycObject cycObj)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseBoolean("(fif (fboundp 'isa-in-any-mt-cached?)"
            + "(isa-in-any-mt-cached? " + cycObj.stringApiValue() + " " + FUNCTION_DENOTATIONAL + ")"
            + "(isa-in-any-mt? " + cycObj.stringApiValue() + " " + FUNCTION_DENOTATIONAL + "))");
  }
  
  @Override
  public boolean isReifiableFunction(CycObject cycObj)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseBoolean("(fif (fboundp 'isa-in-any-mt-cached?)"
            + "(isa-in-any-mt-cached? " + cycObj.stringApiValue() + " " + REIFIABLE_FUNCTION + ")"
            + "(isa-in-any-mt? " + cycObj.stringApiValue() + " " + REIFIABLE_FUNCTION + "))");
  }
  
  @Override
  public boolean isEvaluatablePredicate(Fort predicate)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    final String command = makeSublStmt("with-all-mts", makeNestedSublStmt(
            "evaluatable-predicate?", predicate));
    return getConverse().converseBoolean(command);
  }
  
  @Override
  public boolean isPredicate(CycObject cycObject)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    //// Preconditions
    if (cycObject == null) {
      throw new NullPointerException("cycObject must not be null");
    }
    verifyPossibleDenotationalTerm(cycObject);
    return getConverse().converseBoolean(
            "(fif (fboundp 'isa-in-any-mt-cached?) "
            + "(isa-in-any-mt-cached? " + cycObject.stringApiValue() + " " + PREDICATE + ")"
            + "(isa-in-any-mt? " + cycObject.stringApiValue() + " " + PREDICATE + "))");
  }

  @Override
  public boolean isPredicate(final Object object) throws CycConnectionException, com.cyc.base.exception.CycApiException {
    if (object == null) {
      throw new NullPointerException("cycObject must not be null");
    }
    if (object instanceof CycObject) {
      return isPredicate((CycObject) object);
    } else {
      return false;
    }
  }
  
  @Override
  public boolean isUnaryPredicate(CycObject cycObject)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    //// Preconditions
    if (cycObject == null) {
      throw new NullPointerException("cycObject must not be null");
    }
    verifyPossibleDenotationalTerm(cycObject);
    // TODO: convert to simple call to isa-in-any-mt-cached? once that has propogated...
    return getConverse().converseBoolean("(fif (fboundp 'isa-in-any-mt-cached?) "
            + "(isa-in-any-mt-cached? " + cycObject.stringApiValue() + " " + UNARY_PREDICATE + ")"
            + "(isa-in-any-mt? " + cycObject.stringApiValue() + " " + UNARY_PREDICATE + "))");
  }
  
  @Override
  public boolean isFormulaWellFormed(CycList formula,
          CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return isFormulaWellFormedInternal(formula, mt);
  }
  
  @Override
  public boolean isFormulaWellFormed(Formula formula,
          CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return isFormulaWellFormedInternal(formula, mt);
  }
  
  @Override
  public boolean isCycLNonAtomicReifableTerm(CycList formula)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return isCycLNonAtomicReifableTerm((CycObject) formula);
  }
  
  @Override
  public boolean isCycLNonAtomicReifableTerm(CycObject formula)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseBoolean("(cycl-nart-p " + formula.stringApiValue() + ")");
  }
  
  @Override
  public boolean isCycLNonAtomicUnreifableTerm(CycObject formula)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseBoolean("(cycl-naut-p " + formula.stringApiValue() + ")");
  }
  
  @Override
  public boolean isFunctionBound(String symbolName)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    CycSymbolImpl cycSymbol = makeCycSymbol(
            symbolName);

    return isFunctionBound(cycSymbol);
  }
  
  @Override
  public boolean isFunctionBound(CycSymbol cycSymbol)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    String command = makeSublStmt("boolean", makeNestedSublStmt("fboundp",
            cycSymbol));
    return getConverse().converseBoolean(command);
  }
  
  @Override
  public boolean isSpecOf(CycObject spec,
          CycObject genl)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return isGenlOf(genl,
            spec);
  }
  
  @Override
  public boolean isSpecOf(CycObject spec,
          CycObject genl,
          CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return isGenlOf(genl,
            spec,
            mt);
  }
  
  @Override
  public boolean isGenlOf(CycObject genl,
          CycObject spec)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseBoolean("(genl-in-any-mt? " + spec.stringApiValue() + " "
            + genl.stringApiValue() + ")");
  }
  
  @Override
  public boolean isGenlOf_Cached(CycObject genl,
          CycObject spec)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    final Pair args = new Pair(genl, spec);
    Boolean isGenlOf = isGenlOfCache.get(args);
    if (isGenlOf != null) {
      return isGenlOf;
    }
    final boolean answer = isGenlOf(genl, spec);
    isGenlOfCache.put(args, answer);
    return answer;
  }
  
  @Override
  public boolean isGenlOf(CycObject genl,
          CycObject spec,
          CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseBoolean("(genl? " + spec.stringApiValue() + " " + genl.stringApiValue() + " "
            + makeElMt_inner(mt).stringApiValue() + ")");
  }
  
  @Override
  public boolean isGenlPredOf(Fort genlPred,
          Fort specPred,
          CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseBoolean("(genl-predicate? " + specPred.stringApiValue() + " "
            + genlPred.stringApiValue() + " " + makeElMt_inner(
                    mt).stringApiValue() + ")");
  }
  
  @Override
  public boolean isGenlPredOf(Fort genlPred,
          Fort specPred)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseBoolean("(with-all-mts (genl-predicate? " + specPred.stringApiValue() + " "
            + genlPred.stringApiValue() + "))");
  }
  
  @Override
  public boolean isGenlInverseOf(Fort genlPred,
          Fort specPred,
          CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseBoolean("(genl-inverse? " + specPred.stringApiValue() + " "
            + genlPred.stringApiValue() + " " + makeElMt_inner(
                    mt).stringApiValue() + ")");
  }
  
  @Override
  public boolean isGenlInverseOf(Fort genlPred,
          Fort specPred)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseBoolean("(with-all-mts (genl-inverse? " + specPred.stringApiValue() + " "
            + genlPred.stringApiValue() + "))");
  }
  
  @Override
  public boolean isGenlMtOf(CycObject genlMt,
          CycObject specMt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseBoolean("(genl-mt? " + makeElMt_inner(specMt).stringApiValue() + " "
            + makeElMt_inner(genlMt).stringApiValue() + ")");
  }

  // Private
  private boolean isWellFormedFormulaInternal(CycObject cycList)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseBoolean(makeSublStmt(WITH_ALL_MTS, makeNestedSublStmt(EL_WFF,
            cycList)));
  }

  private boolean isFormulaWellFormedInternal(CycObject formula,
          CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return getConverse().converseBoolean("(el-formula-ok? " + formula.stringApiValue() + " "
            + makeElMt_inner(mt).stringApiValue() + ")");
  }

  private void verifyPossibleDenotationalTerm(CycObject cycObject) throws IllegalArgumentException {
    ((LookupToolImpl) getCyc().getLookupTool()).verifyPossibleDenotationalTerm(cycObject);
  }

  // Internal
  private static final CycSymbolImpl EL_WFF = makeCycSymbol("el-wff?");

  /**
   * Least Recently Used Cache of isCollection results.
   */
  private final Map<CycObject, Boolean> isCollectionCache = new LruCache<>(
          500, 5000, true);

  /**
   * Least Recently Used Cache of isGenlOf results.
   */
  private final Map<Pair, Boolean> isGenlOfCache = new LruCache<>(
          500, 5000, true);

  /**
   * Least Recently Used Cache of countAllInstances results.
   */
  private final Map<Fort, Integer> countAllInstancesCache = new LruCache<>(
          500, 5000, true);

  private Map<CycObject, CycObject> categorizedFunctorMap = null;
//a map from schema to the categories for that schema (via schemaIsa)
  private final Map<CycObject, CycObject> schemaIsaMap = new HashMap<>();

  private static final String MICROTHEORY = CommonConstants.MICROTHEORY.cyclify();
  // Commented out regarding BASEAPI-63 - nwinant, 2014-08-18
  //  private static final String PUBLIC_CONSTANT = CommonConstants.PUBLIC_CONSTANT.cyclify();
  private static final String COLLECTION = CommonConstants.COLLECTION.cyclify();
  private static final String INDIVIDUAL = CommonConstants.INDIVIDUAL.cyclify();
  private static final String FUNCTION_DENOTATIONAL = CommonConstants.FUNCTION_DENOTATIONAL.cyclify();
  private static final String REIFIABLE_FUNCTION = CommonConstants.REIFIABLE_FUNCTION.cyclify();
  private static final String PREDICATE = CommonConstants.PREDICATE.cyclify();
  private static final String UNARY_PREDICATE = CommonConstants.UNARY_PREDICATE.cyclify();
}
