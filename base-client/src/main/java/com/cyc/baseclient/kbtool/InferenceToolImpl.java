package com.cyc.baseclient.kbtool;

/*
 * #%L
 * File: InferenceToolImpl.java
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
import com.cyc.base.cycobject.CycList;
import com.cyc.base.cycobject.CycObject;
import com.cyc.base.cycobject.CycSymbol;
import com.cyc.base.cycobject.CycVariable;
import com.cyc.base.cycobject.ElMt;
import com.cyc.base.cycobject.FormulaSentence;
import com.cyc.base.exception.CycApiException;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.base.exception.CycTimeOutException;
import com.cyc.base.inference.InferenceResultSet;
import com.cyc.base.inference.InferenceWorkerSynch;
import com.cyc.base.kbtool.InferenceTool;
import com.cyc.baseclient.AbstractKbTool;
import com.cyc.baseclient.CycObjectFactory;
import com.cyc.baseclient.connection.DefaultSublWorkerSynch;
import com.cyc.baseclient.connection.SublWorkerSynch;
import com.cyc.baseclient.cycobject.CycArrayList;
import com.cyc.baseclient.cycobject.CycSymbolImpl;
import com.cyc.baseclient.exception.CycApiServerSideException;
import com.cyc.baseclient.inference.DefaultInferenceSuspendReason;
import com.cyc.baseclient.inference.DefaultInferenceWorkerSynch;
import com.cyc.baseclient.inference.DefaultResultSet;
import com.cyc.baseclient.inference.params.DefaultInferenceParameterDescriptions;
import com.cyc.baseclient.inference.params.DefaultInferenceParameters;
import com.cyc.baseclient.inference.params.InferenceParameterDescriptions;
import com.cyc.baseclient.util.LruCache;
import com.cyc.query.parameters.InferenceParameters;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.cyc.baseclient.CycObjectFactory.makeCycSymbol;
import static com.cyc.baseclient.connection.SublApiHelper.makeNestedSublStmt;
import static com.cyc.baseclient.connection.SublApiHelper.makeSublStmt;

/**
 * Tools for performing inferences over the Cyc KB. To lookup
 * facts in the Cyc KB, use the {@link com.cyc.baseclient.kbtool.LookupToolImpl}.
 * 
 * @see com.cyc.baseclient.kbtool.LookupToolImpl
 * @author nwinant
 */
public class InferenceToolImpl extends AbstractKbTool implements InferenceTool {
  // TODO: more specific return types?
  
  public InferenceToolImpl(CycAccess client) {
    super(client);
  }
  
  
  // Public
  
  @Override
  public String queryPropertiesToString(
          final InferenceParameters queryProperties) {
    final InferenceParameters tempQueryProperties = (queryProperties == null) ? getHLQueryProperties() : queryProperties;
    final CycArrayList parameterList = new CycArrayList();
    for (final Iterator<Map.Entry<String, Object>> iter = tempQueryProperties.entrySet().iterator(); iter.hasNext();) {
      Map.Entry<String, Object> mapEntry = iter.next();
      String queryParameterKeyword = mapEntry.getKey();
      CycSymbol queryParameterKeywordSymbol = new CycSymbolImpl(queryParameterKeyword);
      parameterList.add(queryParameterKeywordSymbol);
      final Object rawValue = mapEntry.getValue();
      parameterList.add(tempQueryProperties.parameterValueCycListApiValue(
              queryParameterKeyword, rawValue));
    }
    return parameterList.stringApiValue();
  }
  
  /**
   * Asks a Cyc query (new inference parameters) and returns the binding list.
   *
   * @param query the query expression
   * @param mt the inference microtheory
   * @param queryProperties queryProperties the list of query property keywords and values
   *
   * @return the binding list resulting from the given query
   * @throws com.cyc.base.exception.CycConnectionException
   *
   * <p>Deprecated: use <code>executeQuerySynch</code>
   */
  @Deprecated
  public CycList askNewCycQuery(final CycList query,
                                final CycObject mt,
                                final InferenceParameters queryProperties)
          throws CycConnectionException, CycApiException {
    final String script
            = "(new-cyc-query " + query.stringApiValue()
                      + " " + makeElMt_inner(mt).stringApiValue()
                      + " " + queryPropertiesToString(queryProperties) + ")";
    return getConverse().converseList(script);
  }
  
  @Override
  public InferenceResultSet executeQuery(final String query,
          final Object mt, final InferenceParameters queryProperties,
          final long timeoutMsecs)
          throws CycConnectionException, com.cyc.base.exception.CycApiException, CycTimeOutException {
    InferenceWorkerSynch worker =
            new DefaultInferenceWorkerSynch(query, getCyc().getObjectTool().makeElMt(mt), queryProperties,
            getCyc(), timeoutMsecs);
    InferenceResultSet rs = worker.executeQuerySynch();
    return rs;
  }
  
  @Override
    public InferenceResultSet executeQuery(
          final CycList query,
          final CycObject mt,
          final InferenceParameters queryProperties,
          final long timeoutMsecs)
          throws CycConnectionException,
                 com.cyc.base.exception.CycApiException,
                 CycTimeOutException {
    // Use new-cyc-query if args are okay for it:
    if (isOkForNewCycQuery(queryProperties, timeoutMsecs)) {
        final String command = makeSublStmt("first-n", 2, makeNestedSublStmt("multiple-value-list",
                               makeNestedSublStmt("new-cyc-query", query, mt,
                               (queryProperties != null) ? queryProperties : new DefaultInferenceParameters(
                               getCyc()))));
      final CycList results = getConverse().converseList(command);
      final CycObject bindings = (CycObject) results.get(0);
      final DefaultInferenceSuspendReason haltReason = DefaultInferenceSuspendReason.fromCycSuspendReason(
              results.get(1));
      if (haltReason.isError()) {
        throw new CycApiServerSideException(
                "Inference halted due to error:\n" + haltReason);
      }
      return new DefaultResultSet(CycObjectFactory.nil.equals(bindings)
              ? Collections.emptyList() : (CycArrayList) bindings);
    } else {
      InferenceWorkerSynch worker =
              new DefaultInferenceWorkerSynch(query, (ElMt) mt, queryProperties,
              getCyc(), timeoutMsecs);
      return worker.executeQuerySynch();
    }
  }
  
  @Override
  public InferenceResultSet executeQuery(final FormulaSentence query,
                                         final ElMt mt,
                                         final InferenceParameters queryProperties,
                                         final long timeoutMsecs)
          throws CycConnectionException,
                 com.cyc.base.exception.CycApiException,
                 CycTimeOutException {
    return executeQuery(query.getArgs(), mt, queryProperties, timeoutMsecs);
  }
  
  @Override
  public InferenceResultSet executeQuery(final FormulaSentence query,
          final ElMt mt, final InferenceParameters queryProperties)
          throws CycConnectionException, com.cyc.base.exception.CycApiException, CycTimeOutException {
    return executeQuery(query, mt, queryProperties, 0);
  }
  
  /**
   * Asks a Cyc query (new inference parameters) and returns an XML stream according
 to the specifications in the CycArrayList xmlSpec.
   *
   * @param query the query expression
   * @param mt the inference microtheory
   * @param queryProperties queryProperties the list of query property keywords and values
   * @param xmlSpec the specification of elements, attributes, sort order and bindings for the XML that the method returns
   *
   * @return the binding list from the query in the XML format specified by xmlSpec
   *
   * @throws CycConnectionException
   */
  public String queryResultsToXMLString(CycList query,
          CycObject mt,
          InferenceParameters queryProperties,
          CycList xmlSpec)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return queryResultsToXMLStringInternal(query, mt, queryProperties, xmlSpec);
  }

  /**
   * Asks a Cyc query (new inference parameters) and returns an XML stream according
   * to the specifications in the CycArrayList xmlSpec.
   *
   * @param query the query expression
   * @param mt the inference microtheory
   * @param queryProperties queryProperties the list of query property keywords and values
   * @param xmlSpec the specification of elements, attributes, sort order and bindings for the XML that the method returns
   *
   * @return the binding list from the query in the XML format specified by xmlSpec
   *
   * @throws CycConnectionException
   */
  public String queryResultsToXMLString(FormulaSentence query,
          CycObject mt,
          InferenceParameters queryProperties,
          CycList xmlSpec)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return queryResultsToXMLStringInternal(query, mt, queryProperties, xmlSpec);
  }
  
  @Override
  public boolean isQueryTrue(CycList query, CycObject mt,
          InferenceParameters queryProperties)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return isQueryTrue(query, mt, queryProperties, 0);
  }
  
  @Override
  public boolean isQueryTrue(FormulaSentence query, CycObject mt,
          InferenceParameters queryProperties)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return isQueryTrue(query, mt, queryProperties, 0);
  }
  
  @Override
  public boolean isQueryTrue(CycList query, CycObject mt,
          InferenceParameters queryProperties, long timeoutMsecs)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    try (InferenceResultSet rs
            = executeQuery(query, makeElMt_inner(mt), queryProperties, timeoutMsecs)) {
      return rs.getTruthValue();
    }
  }

  @Override
  public boolean isQueryTrue(FormulaSentence query, CycObject mt,
          InferenceParameters queryProperties, long timeoutMsecs)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    try (InferenceResultSet rs
            = executeQuery(query, makeElMt_inner(mt), queryProperties, timeoutMsecs)) {
      return rs.getTruthValue();
    }
  }
  
  @Override
  public CycArrayList<Object> queryVariable(final CycVariable variable,
          final CycList query, final CycObject mt,
          final InferenceParameters queryProperties)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return queryVariable(variable, query, mt, queryProperties, 0);
  }
  
  @Override
  public CycArrayList<Object> queryVariable(final CycVariable queryVariable,
          final FormulaSentence query, final CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return queryVariable(queryVariable, query, mt,
            new DefaultInferenceParameters(getCyc()));
  }
  
  @Override
  public CycArrayList<Object> queryVariable(final CycVariable queryVariable,
          final FormulaSentence query, final CycObject mt,
          final InferenceParameters queryProperties)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    final InferenceResultSet rs = executeQuery(query, makeElMt_inner(mt),
            queryProperties);
    return queryVariableLow(queryVariable, rs);
  }
  
  @Override
  public CycArrayList<Object> queryVariable(final CycVariable queryVariable,
          final String query, final Object mt,
          final InferenceParameters queryProperties,
          final long timeoutMsecs)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    final InferenceResultSet rs = executeQuery(query, mt, queryProperties,
            timeoutMsecs);
    return queryVariableLow(queryVariable, rs);
  }
  
  @Override
  public CycArrayList<Object> queryVariable(final CycVariable variable,
          final CycList query, final CycObject mt,
          final InferenceParameters queryProperties, long timeoutMsecs)
          throws CycConnectionException, com.cyc.base.exception.CycApiException, CycTimeOutException {
    try {
      final InferenceResultSet rs
              = executeQuery(query, makeElMt_inner(mt), queryProperties, timeoutMsecs);
      return queryVariableLow(variable, rs);
    } finally {
      return new CycArrayList<>();
    }
  }

  @Override
  public CycArrayList<Object> queryVariableLow(final CycVariable queryVariable,
          final InferenceResultSet rs)
          throws CycConnectionException, com.cyc.base.exception.CycApiException, CycTimeOutException {
    CycArrayList result = new CycArrayList();
    try {
      if (rs.getCurrentRowCount() == 0) {
        return result;
      }
      int colIndex = rs.findColumn(queryVariable);
      if (colIndex < 0) {
        throw new com.cyc.base.exception.CycApiException("Unable to find variable: " + queryVariable);
      }
      while (rs.next()) {
        result.add(rs.getObject(colIndex));
      }
      return result;
    } finally {
      if (rs != null) {
        rs.close();
      }
    }
  }
  
  @Override
  public CycArrayList<Object> queryVariable(final CycVariable variable,
          final FormulaSentence query, final CycObject mt,
          final InferenceParameters queryProperties, long timeoutMsecs)
          throws CycConnectionException, com.cyc.base.exception.CycApiException, CycTimeOutException {
    InferenceResultSet rs = executeQuery(query, makeElMt_inner(mt), queryProperties,
            timeoutMsecs);
    return queryVariableLow(variable, rs);
  }
  
  @Override
  public CycList queryVariable(final CycVariable variable,
          final CycList query,
          final CycObject mt,
          final InferenceParameters queryProperties,
          final String inferenceProblemStoreName)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    if (variable == null) {
      throw new NullPointerException("queryVariables must not be null");
    }
    if (query == null) {
      throw new NullPointerException("query must not be null");
    }
    if (query.isEmpty()) {
      throw new IllegalArgumentException("query must not be empty");
    }
    if (mt == null) {
      throw new NullPointerException("mt must not be null");
    }
    if (inferenceProblemStoreName == null) {
      throw new NullPointerException(
              "inferenceProblemStoreName must not be null");
    }
    if (inferenceProblemStoreName.length() == 0) {
      throw new IllegalArgumentException(
              "inferenceProblemStoreName must not be an empty list");
    }
    final InferenceParameters tempQueryProperties = (queryProperties == null) ? getHLQueryProperties() : queryProperties;
    tempQueryProperties.put(":problem-store", makeCycSymbol(
            "problem-store", false));
    final String script =
            "(clet ((problem-store (find-problem-store-by-name \"" + inferenceProblemStoreName + "\")))"
            + "  (query-variable " + variable.stringApiValue() + " "
            + query.stringApiValue() + " " + makeElMt_inner(mt).stringApiValue() + " " + queryPropertiesToString(
            tempQueryProperties) + "))";
    return getConverse().converseList(script);
  }
  
  @Override
  public CycList<Object> queryVariables(final CycList<CycVariable> variables,
          final CycList<Object> query,
          final CycObject mt,
          final InferenceParameters queryProperties)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    // TODO: use inference property timeout rather than 0 if given
    return queryVariablesLow(variables, query, mt, queryProperties, 0);
  }
  
  @Override
  public CycArrayList<Object> queryVariables(final CycList<CycVariable> variables,
          final FormulaSentence query,
          final CycObject mt,
          final InferenceParameters queryProperties)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return queryVariablesLow(variables, query, mt, queryProperties, 0);
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public CycArrayList<Object> queryVariables(final CycList<CycVariable> variables,
          final String query,
          final Object mt,
          final InferenceParameters queryProperties,
          final long timeoutMsecs)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    final String command = makeSublStmt("query-template", variables, query,
            getCyc().getObjectTool().makeElMt(mt), queryProperties);
    SublWorkerSynch worker = new DefaultSublWorkerSynch(command, getCyc(), timeoutMsecs);
    if (CycObjectFactory.nil.equals(worker.getWork())) {
      return new CycArrayList<>();
    }
    return (CycArrayList<Object>) worker.getWork();
  }
  
  @Override
  public CycArrayList queryVariables(final CycList queryVariables,
          final CycList query,
          final CycObject mt,
          final InferenceParameters queryProperties,
          final String inferenceProblemStoreName)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    if (query.isEmpty()) {
      throw new IllegalArgumentException("query must not be empty");
    }
    return queryVariablesInternal(queryVariables, query, mt, queryProperties,
            inferenceProblemStoreName, 0);
  }
  
  @Override
  public CycArrayList queryVariables(final CycList<CycVariable> queryVariables,
          final FormulaSentence query,
          final CycObject mt,
          final InferenceParameters queryProperties,
          final String inferenceProblemStoreName)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    return queryVariablesInternal(queryVariables, query, mt, queryProperties,
            inferenceProblemStoreName, 0);
  }
  
  @Override
  public CycList askCycQuery(CycList query,
          CycObject mt,
          Object maxTransformationDepth,
          Object maxNumber,
          Object maxTimeSeconds,
          Object maxProofDepth)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    HashMap queryProperties = new HashMap();
    queryProperties.put(makeCycSymbol(
            ":max-transformation-depth"),
            maxTransformationDepth);
    queryProperties.put(makeCycSymbol(
            ":max-number"),
            maxNumber);
    queryProperties.put(makeCycSymbol(
            ":max-time"),
            maxTimeSeconds);
    queryProperties.put(makeCycSymbol(
            ":max-proof-depth"),
            maxProofDepth);
    queryProperties.put(makeCycSymbol(
            ":forget-extra-results?"),
            CycObjectFactory.t);

    return askCycQuery(query,
            mt,
            queryProperties);
  }

  /**
   * Asks a Cyc query and returns the binding list.
   *
   * <p>Deprecated: use <code>executeQuerySynch</code>
   * @param query the query expression
   * @param mt the inference microtheory
   * @param queryProperties queryProperties the list of query property keywords and values
   *
   * @return the binding list resulting from the given query
   *
   * @throws CycConnectionException
   */
  @Deprecated
  public CycList askCycQuery(CycList query, CycObject mt,
          HashMap queryProperties)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    CycArrayList parameterList = new CycArrayList();
    Iterator iter = queryProperties.entrySet().iterator();

    if (iter.hasNext()) {
      while (iter.hasNext()) {
        Map.Entry mapEntry = (Map.Entry) iter.next();
        CycSymbolImpl queryParameterKeyword = (CycSymbolImpl) mapEntry.getKey();
        parameterList.add(queryParameterKeyword);

        Object queryParameterValue = mapEntry.getValue();
        parameterList.add(queryParameterValue);
      }
    }
    String command = makeSublStmt(CYC_QUERY, query, makeElMt_inner(mt), parameterList);

    return getConverse().converseList(command);
  }
  
  @Override
  public CycList queryVariable(final CycList query,
          final CycVariable variable,
          final CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    StringBuilder queryBuffer = new StringBuilder();
    queryBuffer.append("(clet ((*cache-inference-results* nil) ");
    queryBuffer.append("       (*compute-inference-results* nil) ");
    queryBuffer.append("       (*unique-inference-result-bindings* t) ");
    queryBuffer.append("       (*generate-readable-fi-results* nil)) ");
    queryBuffer.append("  (without-wff-semantics ");
    queryBuffer.append("    (ask-template ").append(variable.stringApiValue()).append(
            " ");
    queryBuffer.append("                  ").append(query.stringApiValue()).append(
            " ");
    queryBuffer.append("                  ").append(makeElMt_inner(
            mt).stringApiValue()).append(" ");
    queryBuffer.append("                  0 nil nil nil)))");

    CycList answer = getConverse().converseList(queryBuffer.toString());

    return getCyc().getObjectTool().canonicalizeList(answer);
  }

  /**
   * Returns a list of bindings for a query with a single unbound variable.
   *
   * <p>Deprecated: use queryVariable
   * @param query the query to be asked in the knowledge base
   * @param variable the single unbound variable in the query for which bindings are sought
   * @param mt the microtheory in which the query is asked
   *
   * @return a list of bindings for the query
   * 
   * @throws CycConnectionException
   */
  @Deprecated
  public CycList askWithVariable(CycList query,
          CycVariable variable,
          CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    StringBuilder queryBuffer = new StringBuilder();
    queryBuffer.append("(clet ((*cache-inference-results* nil) ");
    queryBuffer.append("       (*compute-inference-results* nil) ");
    queryBuffer.append("       (*unique-inference-result-bindings* t) ");
    queryBuffer.append("       (*generate-readable-fi-results* nil)) ");
    queryBuffer.append("  (without-wff-semantics ");
    queryBuffer.append("    (ask-template ").append(variable.stringApiValue()).append(
            " ");
    queryBuffer.append("                  ").append(query.stringApiValue()).append(
            " ");
    queryBuffer.append("                  ").append(makeElMt_inner(
            mt).stringApiValue()).append(" ");
    queryBuffer.append("                  0 nil nil nil)))");

    CycList answer = getConverse().converseList(queryBuffer.toString());

    return getCyc().getObjectTool().canonicalizeList(answer);
  }

  /**
   * Returns a list of bindings for a query with unbound variables. The bindings each consist of a
   * list in the order of the unbound variables list parameter, in which each bound term is the
   * binding for the corresponding variable.
   *
   * <p>Deprecated: use queryVariables
   * @param query the query to be asked in the knowledge base
   * @param variables the list of unbound variables in the query for which bindings are sought
   * @param mt the microtheory in which the query is asked
   *
   * @return a list of bindings for the query
   *
   * @throws CycConnectionException
   */
  @Deprecated
  public CycList askWithVariables(CycList query,
          List variables,
          CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    StringBuilder queryBuffer = new StringBuilder();
    queryBuffer.append("(clet ((*cache-inference-results* nil) ");
    queryBuffer.append("       (*compute-inference-results* nil) ");
    queryBuffer.append("       (*unique-inference-result-bindings* t) ");
    queryBuffer.append("       (*generate-readable-fi-results* nil)) ");
    queryBuffer.append("  (without-wff-semantics ");
    queryBuffer.append("    (ask-template ").append(
            (new CycArrayList(variables)).stringApiValue()).append(" ");
    queryBuffer.append("                  ").append(query.stringApiValue()).append(
            " ");
    queryBuffer.append("                  ").append(mt.stringApiValue()).append(
            " ");
    queryBuffer.append("                  0 nil nil nil)))");

    CycList bindings = getConverse().converseList(queryBuffer.toString());
    CycArrayList canonicalBindings = new CycArrayList();
    Iterator iter = bindings.iterator();

    while (iter.hasNext()) {
      canonicalBindings.add(getCyc().getObjectTool().canonicalizeList((CycArrayList) iter.next()));
    }
    return canonicalBindings;
  }
  
  @Override
  public boolean isQueryTrue(CycList query, CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    String command = makeSublStmt(CYC_QUERY, getCyc().getObjectTool().canonicalizeList(query), makeElMt_inner(
            mt));
    CycList response = getConverse().converseList(command);

    return response.size() > 0;
  }
  
  @Override
  public boolean isQueryTrue_Cached(CycList query,
          CycObject mt)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    final Boolean isQueryTrue = askCache.get(query);
    if (isQueryTrue != null) {
      return isQueryTrue;
    }
    final boolean answer = isQueryTrue(query, makeElMt_inner(mt));
    askCache.put(query, answer);
    return answer;
  }
  
  @Override
  public void initializeNamedInferenceProblemStore(
          final String name, 
          final InferenceParameters queryProperties)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    //// Preconditions
    if (name == null) {
      throw new NullPointerException("name must not be null");
    }
    if (name.length() == 0) {
      throw new IllegalArgumentException("name must not be an empty string");
    }
    final InferenceParameters tempQueryProperties = (queryProperties == null) ? getHLQueryProperties() : queryProperties;
    final String command =
            "(progn "
            + "  (find-or-create-problem-store-by-name \"" + name + "\" (filter-plist " + queryPropertiesToString(
            tempQueryProperties) + "'problem-store-property-p)) "
            + "  nil)";
    getConverse().converseVoid(command);
  }
  
  @Override
  public void destroyInferenceProblemStoreByName(final String name) 
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    //// Preconditions
    if (name == null) {
      throw new NullPointerException("name must not be null");
    }
    if (name.length() == 0) {
      throw new IllegalArgumentException("name must not be an empty string");
    }
    final String command = "(destroy-problem-store-by-name \"" + name + "\")";
    getConverse().converseVoid(command);
  }
  
  /*
  @Override
  public void destroyAllProblemStores() throws CycConnectionException, CycApiException {
    final String command = "(destroy-all-problem-stores)";
    getConverse().converseVoid(command);
  }

  @Override
  public void destroyMostProblemStores(int numberRemaining) throws CycConnectionException, CycApiException {
    final String command = "(destroy-most-problem-stores " + numberRemaining + ")";
    getConverse().converseVoid(command);
  }
  */
  
  @Override
  public InferenceParameters getHLQueryProperties() {
    synchronized (defaultQueryProperties) {
      if (!queryPropertiesInitialized) {
        initializeQueryProperties();
      }
      return (InferenceParameters) defaultQueryProperties.clone();
    }
  }
  
  @Override
  public boolean areQueriesEqualAtEL(Object obj1, Object obj2) throws CycConnectionException {
    String command = makeSublStmt("queries-equal-at-el?", obj1, obj2);
    // execute the SubL function-call and access the response
    Object response = getConverse().converseObject(command);
    return !response.equals(CycObjectFactory.nil);
  }
  
  
  // Private
  
  private CycArrayList queryVariablesInternal(final CycList queryVariables,
          final CycObject query,
          final CycObject mt,
          final InferenceParameters queryProperties,
          final String inferenceProblemStoreName,
          final long timeoutMsecs)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    //// Preconditions
    if (queryVariables == null) {
      throw new NullPointerException("queryVariables must not be null");
    }
    if (queryVariables.isEmpty()) {
      throw new IllegalArgumentException("queryVariables must not be empty");
    }
    if (query == null) {
      throw new NullPointerException("query must not be null");
    }
    if (mt == null) {
      throw new NullPointerException("mt must not be null");
    }
    if (inferenceProblemStoreName == null) {
      throw new NullPointerException(
              "inferenceProblemStoreName must not be null");
    }
    if (inferenceProblemStoreName.length() == 0) {
      throw new IllegalArgumentException(
              "inferenceProblemStoreName must not be an empty list");
    }
    final InferenceParameters tempQueryProperties = (queryProperties == null) ? getHLQueryProperties() : queryProperties;
    tempQueryProperties.put(":problem-store", makeCycSymbol(
            "problem-store", false));
    final String command =
            "(clet ((problem-store (find-problem-store-by-name \"" + inferenceProblemStoreName + "\")))"
            + "  (query-template " + queryVariables.stringApiValue() + " "
            + query.stringApiValue() + " " + makeElMt_inner(mt).stringApiValue() + " " + queryPropertiesToString(
            tempQueryProperties) + "))";
    SublWorkerSynch worker = new DefaultSublWorkerSynch(command, getCyc(), timeoutMsecs);
    if (CycObjectFactory.nil.equals(worker.getWork())) {
      return new CycArrayList<>();
    }
    return (CycArrayList) worker.getWork();
  }
  
  private CycArrayList<Object> queryVariablesLow(
          final CycList<CycVariable> queryVariables,
          final CycObject query,
          final CycObject mt,
          final InferenceParameters queryProperties,
          final long timeoutMsecs)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    final String command = makeSublStmt("query-template", queryVariables, query,
            makeElMt_inner(mt), queryProperties);
    SublWorkerSynch worker = new DefaultSublWorkerSynch(command, getCyc(), timeoutMsecs);
    if (CycObjectFactory.nil.equals(worker.getWork())) {
      return new CycArrayList<>();
    }
    return (CycArrayList<Object>) worker.getWork();
  }

  
  private String queryResultsToXMLStringInternal(CycObject query,
          CycObject mt,
          InferenceParameters queryProperties,
          CycList xmlSpec)
          throws CycConnectionException, com.cyc.base.exception.CycApiException {
    String xmlSpecString = (xmlSpec == null) ? ":default" : xmlSpec.stringApiValue();
    final String script =
            "(query-results-to-xml-string " + query.stringApiValue() + " " + makeElMt_inner(
            mt).stringApiValue() + " " + queryPropertiesToString(queryProperties) + " " + xmlSpecString + ")";
    return getConverse().converseString(script);
  }
  
  private boolean isOkForNewCycQuery(final InferenceParameters queryProperties,
          final long timeoutMsecs) {
    if (timeoutMsecs != 0) {
      return false; //timeouts require subl workers.
    } else if (queryProperties == null) {
      return true; //default properties are okay.
    } else return !queryProperties.containsKey(":RETURN"); //we rely on the standard return format.
  }
  
  /** 
   * Initializes the query properties. 
   */
  private void initializeQueryProperties() {
    defaultQueryProperties.put(":allowed-rules", makeCycSymbol(
            ":all"));
    defaultQueryProperties.put(":result-uniqueness",
            makeCycSymbol(":bindings"));
    defaultQueryProperties.put(":allow-hl-predicate-transformation?", false);
    defaultQueryProperties.put(":allow-unbound-predicate-transformation?", false);
    defaultQueryProperties.put(":allow-evaluatable-predicate-transformation?", false);
    defaultQueryProperties.put(":intermediate-step-validation-level", makeCycSymbol(":all"));
    defaultQueryProperties.put(":negation-by-failure?", false);
    defaultQueryProperties.put(":allow-indeterminate-results?", true);
    defaultQueryProperties.put(":allow-abnormality-checking?", true);
    defaultQueryProperties.put(":disjunction-free-el-vars-policy",
            makeCycSymbol(":compute-intersection"));
    defaultQueryProperties.put(":allowed-modules", makeCycSymbol(":all"));
    defaultQueryProperties.put(":completeness-minimization-allowed?", true);
    defaultQueryProperties.put(":direction", makeCycSymbol(":backward"));
    defaultQueryProperties.put(":equality-reasoning-method", makeCycSymbol(":czer-equal"));
    defaultQueryProperties.put(":equality-reasoning-domain", makeCycSymbol(":all"));
    defaultQueryProperties.put(":max-problem-count", Long.valueOf(100000));
    defaultQueryProperties.put(":transformation-allowed?", false);
    defaultQueryProperties.put(":add-restriction-layer-of-indirection?", true);
    defaultQueryProperties.put(":evaluate-subl-allowed?", true);
    defaultQueryProperties.put(":rewrite-allowed?", false);
    defaultQueryProperties.put(":abduction-allowed?", false);
    defaultQueryProperties.put(":removal-backtracking-productivity-limit", Long.valueOf(2000000));
    // dynamic query properties
    defaultQueryProperties.put(":max-number", null);
    defaultQueryProperties.put(":max-time", 120);
    defaultQueryProperties.put(":max-transformation-depth", 0);
    defaultQueryProperties.put(":block?", false);
    defaultQueryProperties.put(":max-proof-depth", null);
    defaultQueryProperties.put(":cache-inference-results?", false);
    defaultQueryProperties.put(":answer-language", makeCycSymbol(":el"));
    defaultQueryProperties.put(":continuable?", false);
    defaultQueryProperties.put(":browsable?", false);
    defaultQueryProperties.put(":productivity-limit", Long.valueOf(2000000));

    final CycArrayList<CycSymbolImpl> queryPropertiesList = new CycArrayList(
            defaultQueryProperties.keySet());
    final String command = makeSublStmt("mapcar", makeCycSymbol(
            "query-property-p"), queryPropertiesList);
    try {
      CycList results = getConverse().converseList(command);
      for (int i = 0, size = results.size(); i < size; i++) {
        if (results.get(i).equals(CycObjectFactory.nil)) {
          final String badProperty = queryPropertiesList.get(i).toCanonicalString();
          System.err.println(badProperty + " is not a query-property-p");
          defaultQueryProperties.remove(badProperty);
        }
      }
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }
    queryPropertiesInitialized = true;
  }

  /** Migrate to this once inference parameter definitions are included in OpenCyc KB. */
  private void initializeQueryPropertiesNew() {
    synchronized (defaultQueryProperties) {
      defaultQueryProperties.clear();
      try {
        final InferenceParameterDescriptions desc = DefaultInferenceParameterDescriptions.loadInferenceParameterDescriptions(
                getCyc(), 10000);
        final InferenceParameters defaults = desc.getDefaultInferenceParameters();
        final CycList allQueryProperties = getConverse().converseList(makeSublStmt(
                "ALL-QUERY-PROPERTIES"));
        for (final Object property : allQueryProperties) {
          if (property instanceof CycSymbolImpl && defaults.containsKey(
                  property.toString())) {
            final Object value = defaults.get(property.toString());
            defaultQueryProperties.put(property.toString(), value);
          }
        }
      } catch (CycConnectionException | com.cyc.base.exception.CycApiException ex) {
        LOGGER.error(ex.getMessage(), ex);
      }
    }
    queryPropertiesInitialized = true;
  }
  
  
  // Internal
  
  private static final Logger LOGGER = LoggerFactory.getLogger(InferenceToolImpl.class);
  
  private static final CycSymbolImpl CYC_QUERY = makeCycSymbol("cyc-query");

  /** 
   * default query properties, initialized by initializeQueryProperties().
   */
  private final InferenceParameters defaultQueryProperties = new DefaultInferenceParameters(
          getCyc());
  
  private boolean queryPropertiesInitialized = false;
  
  /** 
   * Least Recently Used Cache of ask results.
   */
  private final Map<CycList, Boolean> askCache = new LruCache<>(500, 5000, true);
}
