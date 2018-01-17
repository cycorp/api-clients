package com.cyc.query.client;

/*
 * #%L
 * File: QueryImpl.java
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
import com.cyc.base.CycAccess;
import com.cyc.base.CycAccessManager;
import com.cyc.base.cycobject.CycList;
import com.cyc.base.cycobject.CycObject;
import com.cyc.base.cycobject.CycVariable;
import com.cyc.base.cycobject.DenotationalTerm;
import com.cyc.base.cycobject.ElMt;
import com.cyc.base.cycobject.FormulaSentence;
import com.cyc.base.exception.CycApiException;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.base.inference.InferenceAnswer;
import com.cyc.base.inference.InferenceWorker;
import com.cyc.baseclient.CycObjectFactory;
import com.cyc.baseclient.cycobject.ArgPositionImpl;
import com.cyc.baseclient.cycobject.FormulaSentenceImpl;
import com.cyc.baseclient.inference.CycBackedInferenceAnswer;
import com.cyc.baseclient.inference.DefaultResultSet;
import com.cyc.baseclient.inference.ResultSetInferenceAnswer;
import com.cyc.baseclient.inference.SpecifiedInferenceAnswerIdentifier;
import com.cyc.baseclient.inference.metrics.InferenceMetricsValuesImpl;
import com.cyc.baseclient.inference.params.DefaultInferenceParameters;
import com.cyc.baseclient.parser.CyclParserUtil;
import com.cyc.baseclient.parser.InvalidConstantGuidException;
import com.cyc.baseclient.parser.InvalidConstantNameException;
import com.cyc.baseclient.parser.ParseException;
import com.cyc.baseclient.parser.UnsupportedVocabularyException;
import com.cyc.kb.ArgPosition;
import com.cyc.kb.Context;
import com.cyc.kb.KbIndividual;
import com.cyc.kb.KbObject;
import com.cyc.kb.Sentence;
import com.cyc.kb.Variable;
import com.cyc.kb.client.ContextImpl;
import com.cyc.kb.client.KbTermImpl;
import com.cyc.kb.client.KbUtils;
import com.cyc.kb.client.SentenceImpl;
import com.cyc.kb.client.config.KbConfiguration;
import com.cyc.kb.exception.CreateException;
import com.cyc.kb.exception.KbException;
import com.cyc.kb.exception.KbTypeException;
import com.cyc.nl.Paraphraser;
import com.cyc.query.InferenceIdentifier;
import com.cyc.query.InferenceStatus;
import com.cyc.query.InferenceSuspendReason;
import com.cyc.query.ParaphrasedQueryAnswer;
import com.cyc.query.Query;
import com.cyc.query.QueryAnswer;
import com.cyc.query.QueryAnswers;
import com.cyc.query.QueryListener;
import com.cyc.query.QueryResultSet;
import com.cyc.query.exception.QueryConstructionException;
import com.cyc.query.exception.QueryRuntimeException;
import com.cyc.query.metrics.InferenceMetricsValues;
import com.cyc.query.parameters.InferenceMode;
import com.cyc.query.parameters.InferenceParameters;
import com.cyc.session.CycServerReleaseType;
import com.cyc.session.CycSession;
import com.cyc.session.compatibility.CycSessionRequirementList;
import com.cyc.session.compatibility.MinimumPatchRequirement;
import com.cyc.session.compatibility.NotOpenCycRequirement;
import com.cyc.session.exception.OpenCycUnsupportedFeatureException;
import com.cyc.session.exception.SessionCommandException;
import com.cyc.session.exception.SessionCommunicationException;
import com.cyc.session.exception.SessionConfigurationException;
import com.cyc.session.exception.SessionInitializationException;
import com.cyc.session.exception.UnsupportedCycOperationException;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.cyc.Cyc.Constants.INFERENCE_PSC;
import static com.cyc.Cyc.Constants.UV_MT;
import static com.cyc.baseclient.connection.SublApiHelper.makeSublStmt;

/**
 * <code>QueryImpl</code> is designed to represent queries posed to Cyc and provide access to their
 * results.
 * <p>
 * In general, the process of getting an answer from Cyc is:
 * <ol>
 * <li> Create a <code>QueryImpl</code> object and set relevant fields on it.
 * <li> Access the answers via methods like {@link #getAnswersCyc()}, {@link #getResultSet()}, or by
 * adding a listener via {@link #addListener(com.cyc.query.QueryListener)} and starting the query
 * via {@link #performInference()}.
 * <li> To avoid filling up memory on the Cyc server, {@link #close()} the QueryImpl when you are
 * done with it, which will free up any lingering associated inference resources on the Cyc image.
 * Queries are also closed when their {@link #finalize()} method is invoked, notably when they are
 * garbage collected.
 * </ol>
 *
 * @author Vijay Raj
 * @author David Baxter
 *
 */
public class QueryImpl implements Query, Closeable {

  //====|    Static methods    |==================================================================//
  
  /**
   * Returns a QueryImpl object defined by a query term (i.e. an instance of
   * #$CycLQuerySpecification). Use of this method is equivalent to calling
   * {@link Query#Query(com.cyc.kb.KBIndividual)}.
   *
   *
   * @param id an instance of #$CycLQuerySpecification
   * @return a QueryImpl object defined by queryTerm
   * @throws KbException if there is a problem creating the QueryImpl object
   * @throws QueryConstructionException if there is some other kind of problem
   * <p>
   * <b>Note:</b> {@link QueryConstructionException} is thrown if the specified query term has a
   * sentence whose outermost operator is #$ist and the query is loaded from a Cyc server with a
   * system level under 10.154917 (Nov. 2014). A workaround is to edit the query in the KB, removing
   * the #$ist from the query's sentence, and specifying it as the query mt using
   * #$microtheoryParameterValueInSpecification.
   */
  public static Query load(KbIndividual id)
          throws UnsupportedCycOperationException, QueryConstructionException, KbException {
    try {
      QUERY_LOADER_REQUIREMENTS.throwExceptionIfIncompatible();
      final Query q = new QueryReader().queryFromTerm(id);
      
      //q.getRules().useOnlySpecifiedRules(false); // This is no longer necessary, as explained in CAPI-809. See also: CAPI-803 - nwinant, 2017-04-23s
      
      return q;
    } catch (UnsupportedCycOperationException | KbException ex) {
      throw ex;
    } catch (JAXBException |NullPointerException 
            | SessionCommunicationException | SessionInitializationException
            | SessionCommandException | SessionConfigurationException
            | QueryConstructionException | QueryRuntimeException ex) {
      throw QueryConstructionException.fromThrowable("Could not load a query for " + id, ex);
    }
  }
  
  /**
   * Returns a new QueryImpl loaded from a term in Cyc specifying its properties. Terms in the
   * specified query can be replaced with others by providing a non-empty <code>indexicals</code>
   * map.
   *
   * @param id the Cyc term
   * @param indexicals A map of substitutions to be made.
   * @return the QueryImpl specified by <code>id</code>
   * @throws QueryRuntimeException
   * <p>
   * <b>Note:</b> {@link QueryConstructionException} is thrown if the specified query term has a
   * sentence whose outermost operator is #$ist and the query is loaded from a Cyc server with a
   * system level under 10.154917 (Nov. 2014). A workaround is to edit the query in the KB, removing
   * the #$ist from the query's sentence, and specifying it as the query mt using
   * #$microtheoryParameterValueInSpecification.
   */
  public static QueryImpl load(KbIndividual id, Map<KbObject, Object> indexicals) {
    final Map<CycObject, Object> kboToCoMap = KbUtils.convertKBObjectMapToCoObjectMap(indexicals);
    final QueryImpl q;
    try {
      q = loadCycObjectMap(id, kboToCoMap);
    } catch (UnsupportedCycOperationException | QueryConstructionException | KbException ex) {
      throw QueryRuntimeException.fromThrowable(ex);
    }
    return q;
  }

  /**
   * Returns a QueryImpl object defined by a CycLQuerySpecification term, and substitutes in
   * relevant values from the indexicals Map.
   *
   * @param idStr The instance of CycLQuerySpecification
   * @param indexicals A map from terms in the query (as loaded from the KB) to the actual values
   * that should be queried with.
   * @throws QueryConstructionException
   * <p>
   * <b>Note:</b> {@link QueryConstructionException} is thrown if the specified query term has a
   * sentence whose outermost operator is #$ist and the query is loaded from a Cyc server with a
   * system level under 10.154917 (Nov. 2014). A workaround is to edit the query in the KB, removing
   * the #$ist from the query's sentence, and specifying it as the query mt using
   * #$microtheoryParameterValueInSpecification.
   *
   * @throws KbTypeException if <code>idStr</code> does not identify a KBIndividual.
   *
   * @return a QueryImpl object defined by idStr
   */
  public static QueryImpl load(String idStr, Map<String, String> indexicals)
          throws QueryConstructionException, KbTypeException, UnsupportedCycOperationException {
    try {
      final QueryImpl q = (QueryImpl) load(KbIndividual.get(idStr));
      replaceIndexicals(indexicals, q);
      return q;
    } catch (KbException e) {
      throw QueryRuntimeException.fromThrowable("Exception thrown while trying to load " + idStr, e);
    }
  }

  /**
   *
   * @return the number of queries that were closed.
   */
  public static synchronized int closeAllUnclosedQueries() {
    int count = 0;
    for (final QueryImpl q : UNCLOSED_QUERIES) {
      q.close();
      count++;
    }
    if (count > 0) {
      LOGGER.info("Closed {} queries.", count);
    }
    return count;
  }

  /**
   * Returns a QueryImpl object defined by a CycLQuerySpecification term, and substitutes in
   * relevant values from the indexicals Map. This is the equivalent of calling
   * {@link #load(com.cyc.kb.KBIndividual)} and then calling
   * {@link #substituteTermsWithCycObject(java.util.Map)} on it.
   *
   *
   * @param id The instance of CycLQuerySpecification
   * @param indexicals A map from terms in the query (as loaded from the KB) to the actual values
   * that should be queried with.
   *
   * @return a QueryImpl object defined by id
   * @throws com.cyc.query.exception.QueryConstructionException
   * @throws com.cyc.kb.exception.KbException
   *
   * @deprecated Use {@link #load(KBIndividual,Map)}
   */
  @Deprecated
  static QueryImpl loadCycObjectMap(KbIndividual id, Map<CycObject, Object> indexicals)
          throws UnsupportedCycOperationException, QueryConstructionException, KbException {
    QueryImpl q = (QueryImpl) load(id);
    replaceIndexicalsWithCycObject(q, indexicals);
    return q;
  }

  @Deprecated
  private static void replaceIndexicalsWithCycObject(
          QueryImpl q, 
          Map<CycObject, Object> indexicals) {
    try {
      FormulaSentence cfs = q.getQuerySentenceCyc().treeSubstitute(
              getAccess(), indexicals);
      q.setQuerySentence(cfs);
      q.substituteTermsWithCycObject(indexicals);
    } catch (SessionCommunicationException
            | SessionConfigurationException | SessionInitializationException
            | CycApiException | CycConnectionException e) {
      throw QueryRuntimeException.fromThrowable("Exception thrown during indexical replacement", e);
    }
  }

  /*
  private static void replaceIndexicals(QueryImpl q, Map<KbObject, Object> indexicals) {
    Map<CycObject, Object> kboToCoMap = KbUtils.convertKBObjectMapToCoObjectMap(indexicals);
    replaceIndexicalsWithCycObject(q, kboToCoMap);
  }
   */
  private static void replaceIndexicals(Map<String, String> indexicals, QueryImpl q) {
    final Map<CycObject, Object> idx = new HashMap<>();
    for (Map.Entry<String, String> kv : indexicals.entrySet()) {
      //this.indexicals.setf(cyc.getKnownFortByName(kv.getKey()),
      //        CycObjectFactory.makeCycVariable(kv.getValue()));
      idx.put((CycObject) getCycObject(kv.getKey()), getCycObject(kv.getValue()));
    }
    replaceIndexicalsWithCycObject(q, idx);
  }

  private static Object getCycObject(String str) {
    if (str.startsWith("?")) {
      return CycObjectFactory.makeCycVariable(str);
    } else if (str.startsWith(":")) {
      return CycObjectFactory.makeCycSymbol(str);
    } else {
      try {
        return getAccess().getLookupTool().getKnownFortByName(getAccess().cyclifyString(str));
      } catch (SessionCommunicationException
              | SessionConfigurationException | SessionInitializationException
              | CycConnectionException | CycApiException e) {
        return str;
      }
    }
  }

  private static CycAccess getAccess()
          throws SessionConfigurationException,
                 SessionCommunicationException,
                 SessionInitializationException {
    return CycAccessManager.getCurrentAccess();
  }

  private static ContextImpl constructContext(String ctxStr) throws QueryConstructionException {
    try {
      return ContextImpl.get(ctxStr);
    } catch (KbTypeException | CreateException ex) {
      throw QueryConstructionException.fromThrowable(ex);
    }
  }

  private static DefaultInferenceParameters constructParams(String queryParams)
          throws QueryConstructionException {
    try {
      return queryParams.isEmpty() ? null : new DefaultInferenceParameters(
              getAccess(), queryParams);
    } catch (SessionConfigurationException
            | SessionCommunicationException
            | SessionInitializationException ex) {
      throw QueryConstructionException.fromThrowable(ex);
    }
  }

  private static FormulaSentenceImpl constructSentence(String queryStr)
          throws QueryConstructionException {
    try {
      return CyclParserUtil.parseCycLSentence(queryStr, true,
                                              getAccess());
    } catch (SessionConfigurationException | SessionInitializationException
            | SessionCommunicationException | IOException
            | ParseException | UnsupportedVocabularyException
            | CycConnectionException | CycApiException
            | InvalidConstantNameException | InvalidConstantGuidException ex) {
      throw QueryConstructionException.fromThrowable(ex);
    }
  }

  private static ContextImpl constructContext(CycObject ctx) throws QueryConstructionException {
    try {
      return ContextImpl.get(ctx);
    } catch (KbTypeException | CreateException ex) {
      throw QueryConstructionException.fromThrowable(ex);
    }
  }

  /*
  private static KbIndividualImpl constructQueryIdTerm(final DenotationalTerm id)
          throws QueryConstructionException {
    try {
      return KbIndividualImpl.get(id.cyclify());
    } catch (KbTypeException | CreateException ex) {
      throw QueryConstructionException.fromThrowable(ex);
    }
  }
   */
  
  //====|    Static fields    |===================================================================//
  
  public static final CycSessionRequirementList QUERY_LOADER_REQUIREMENTS
          = CycSessionRequirementList.fromList(
                  new MinimumPatchRequirement(
                          "Query loading is unsupported on ResearchCyc 4.0q and earlier",
                          CycServerReleaseType.RESEARCHCYC, 154917));

  public static final CycSessionRequirementList<OpenCycUnsupportedFeatureException>
          QUERY_COMPARISON_REQUIREMENTS
          = CycSessionRequirementList.fromList(
                  NotOpenCycRequirement.NOT_OPENCYC);

  /**
   * Do not use this value directly; instead, call {@link QueryImpl#getQueryWorkerTimeoutMillis()}.
   * It is currently set to 3 minutes.
   */
  public static final long DEFAULT_QUERY_WORKER_TIMEOUT_MILLIS = 3 * 60 * 1000;

  private static final Logger LOGGER = LoggerFactory.getLogger(QueryImpl.class);
  private static final String RESULT_SORT_ORDER = ":RESULT-SORT-ORDER";
  private static final String RETURN = ":RETURN";
  private static final String COMPUTE_ANSWER_JUSTIFICATIONS = ":COMPUTE-ANSWER-JUSTIFICATIONS?";
  private static final Set<QueryImpl> UNCLOSED_QUERIES
          = Collections.newSetFromMap(new ConcurrentHashMap<>());

  //====|    Instance fields    |=================================================================//
  
  private final CycSession session;
  private final FormulaSentence originalQuerySentence;
  private final CycAccess cyc;
  private final Set<QueryListener> listeners = new HashSet<>();
  private final Set<String> categories = new HashSet<>();
  private final QueryInference inference;

  /**
   * Do not use this value directly; instead, call {@link QueryImpl#getQueryWorkerTimeoutMillis()}.
   */
  private long defaultQueryWorkerTimeoutMillis = DEFAULT_QUERY_WORKER_TIMEOUT_MILLIS;

  private Context ctx = null;
  private FormulaSentence querySentence = null;
  private InferenceParameters params = null;
  private KbIndividual id = null;
  private Map<KbObject, Object> substitutions = null;
  private List<QueryAnswer> answers = new ArrayList<>();
  private QueryRulesImpl rules = null;
  private boolean retainInference = false;

  //====|    Construction    |====================================================================//
  
  /**
   * Returns a new query with the specified context, sentence, and parameters.
   *
   * @param sent The CycL sentence to be queried
   * @param ctx the Context in which to run the query
   * @param params
   * @throws com.cyc.query.exception.QueryConstructionException
   */
  @Deprecated
  private QueryImpl(FormulaSentence sent, Context ctx, InferenceParameters params)
          throws QueryConstructionException {
    try {
      this.session = CycSession.getCurrent();
      this.inference = new QueryInference(this);
      this.cyc = CycAccessManager.getAccessManager().fromSession(session);
      this.ctx = ctx;
      this.originalQuerySentence = sent;
      this.querySentence = sent.deepCopy();
      if (params == null) {
        this.params = getDefaultInferenceParameters();
      } else {
        this.params = params;
      }
      UNCLOSED_QUERIES.add(this);
    } catch (SessionConfigurationException
            | SessionCommunicationException
            | SessionInitializationException ex) {
      throw QueryConstructionException.fromThrowable(ex);
    }
  }

  /**
   * constructs a Query working with the string queryStr.
   * <p>
   * The query is executed in InferencePSC with a default timeout and default inference parameters.
   *
   * @param queryStr	the string representing the CycL query
   * @see com.cyc.query.Query#TIMEOUT
   *
   * @throws QueryConstructionException
   */
  public QueryImpl(String queryStr) throws QueryConstructionException {
    this(constructSentence(queryStr), INFERENCE_PSC);
  }

  /**
   * Returns a query object defined by queryStr asked in Microtheory ctxStr, with default inference
   * parameters.
   *
   * @param queryStr The query string.
   * @param ctxStr The Microtheory where the query is asked.
   *
   * @throws QueryConstructionException
   *
   */
  public QueryImpl(String queryStr, String ctxStr) throws QueryConstructionException {
    this(queryStr, ctxStr, "");
  }

  /**
   * Returns a query object defined by queryStr asked in Microtheory ctxStr, with inference
   * parameters, queryParams.
   *
   * @param queryStr The query string.
   * @param ctxStr The Microtheory where the query is asked.
   * @param queryParams The inference parameters to use for the query. This string should consist of
   * a series of keywords followed by the values for those keywords. The keywords can be found by
   * looking for the #$sublIdentifier for the desired instance of InferenceParameter in the Cyc KB.
   * For example, to limit a query to single-depth transformation and to allow at most 5 seconds per
   * query, use the string ":max-transformation-depth 1 :max-time 5".
   *
   * @throws QueryConstructionException
   *
   */
  public QueryImpl(String queryStr, String ctxStr, String queryParams)
          throws QueryConstructionException {
    this(constructSentence(queryStr), constructContext(ctxStr), constructParams(queryParams));
  }

  /**
   * Construct a new Query with the specified sentence and context.
   *
   * @param sent
   * @param ctx
   * @throws QueryConstructionException
   */
  public QueryImpl(Sentence sent, ElMt ctx) throws QueryConstructionException {
    this((FormulaSentence) sent.getCore(), ctx);
  }

  /**
   *
   * @param sent
   * @param ctx
   * @param params
   * @throws com.cyc.query.exception.QueryConstructionException
   */
  public QueryImpl(Sentence sent, Context ctx, InferenceParameters params)
          throws QueryConstructionException {
    this((FormulaSentence) sent.getCore(), ctx, params);
  }

  /**
   *
   * @param sent
   * @param ctx
   * @throws QueryConstructionException
   */
  public QueryImpl(Sentence sent, Context ctx) throws QueryConstructionException {
    this((FormulaSentence) sent.getCore(), ctx);
  }

  /**
   * Construct a new Query with the specified sentence, context, and inference parameters.
   *
   * @param sent
   * @param ctx
   * @param params
   * @throws QueryConstructionException
   */
  public QueryImpl(Sentence sent, ElMt ctx, InferenceParameters params) throws
          QueryConstructionException {
    this((FormulaSentence) sent.getCore(), ctx, params);
  }

  /**
   * Returns a new query with the specified context and sentence, and default parameters.
   *
   * @param sent The CycL sentence to be queried
   * @param ctx the Context in which to run the query
   *
   * @throws QueryConstructionException
   *
   */
  @Deprecated
  private QueryImpl(FormulaSentence sent, Context ctx) throws QueryConstructionException {
    this(sent, ctx, null);
  }

  /**
   * Returns a new query with the specified context, sentence, and parameters.
   *
   * @param sent The CycL sentence to be queried
   * @param ctx the Context in which to run the query
   * @param params
   * @throws QueryConstructionException
   *
   * @deprecated Use {@link #Query(FormulaSentence,Context,InferenceParameters)}
   *
   */
  @Deprecated
  private QueryImpl(FormulaSentence sent, ElMt ctx, InferenceParameters params)
          throws QueryConstructionException {
    this(sent, constructContext(ctx), params);
  }

  /**
   * Returns a new query with the specified context and sentence, and default parameters.
   *
   * @param sent The CycL sentence to be queried
   * @param ctx the Context in which to run the query
   *
   * @throws QueryConstructionException
   * @deprecated Use {@link #Query(FormulaSentence,Context)}
   *
   */
  @Deprecated
  private QueryImpl(FormulaSentence sent, ElMt ctx) throws QueryConstructionException {
    this(sent, constructContext(ctx));
  }

  //====|    Public methods    |==================================================================//
  
  /**
   * Run this query and return the results.
   *
   * @return the results of running the query.
   * @throws QueryRuntimeException if an exception is thrown during inference.
   *
   */
  @Override
  public com.cyc.query.QueryResultSet performInference() throws QueryRuntimeException {
    try {
      return inference.performInference();
    } catch (Exception e) {
      throw QueryRuntimeException.fromThrowable("Exception thrown during inference", e);
    }
  }

  /**
   * Get the Cyc term that defines this query. To change the Id of an existing query, see
   * {@link #saveAs(String)}
   *
   * @return the id term.
   */
  @Override
  public KbIndividual getId() {
    return id;
  }

  /**
   * Set the Cyc term that underlies this query in the KB. Note that setting a new ID will not be
   * reflected immediately in the KB. Instead, the change will only be reflected in the KB when the
   * query is saved. Setting a new Id on a query is similar to calling {@link #saveAs(String)}, in
   * that neither method will change the original QueryImpl specification in the KB.
   *
   * In general, this method should be avoided. <code>QueryImpl</code> is saved.
   *
   * @param id
   */
  @Deprecated
  void setId(KbIndividual id) {
    this.id = id;
  }

  /**
   * Ensure that any required Semantic Knowledge Source removal modules for this query have been
   * registered on the Cyc Server and made available for inference.
   * <p>
   * This should be done prior to running a query or set of queries that relies on real-time access
   * to external knowledge sources.
   * <p>
   * Required knowledge sources are noted in the KB using the predicate sksiModulesNeeded.
   */
  @Override
  public void registerRequiredSksModules() {
    try {
      getCycAccess().converse().converseVoid(
              "(ensure-sksi-modules-needed " + getId().stringApiValue() + ")");
    } catch (CycConnectionException | CycApiException e) {
      throw QueryRuntimeException.fromThrowable("Exception thrown during SKS module registration", e);
    }
  }

  /**
   * Saves this QueryImpl as the term which is its current ID.
   *
   * @see Query#saveAs(String)
   * @see Query#getId()
   */
  @Override
  public void save() {
    final String fn = KbConfiguration.getShouldTranscriptOperations()
                              ? "update-kbq-definition" : "update-kbq-definition-silent";
    try {
      final DenotationalTerm idCycTerm = (DenotationalTerm) getCycAccess().getLookupTool()
              .getKnownFortByName(getId().stringApiValue());
      final String command = makeSublStmt(fn, idCycTerm,
                                                        getQuerySentenceCyc(), ContextImpl.asELMt(
                                                                getContext()), params);
      getCycAccess().converse().converseVoid(command);
    } catch (CycConnectionException | CycApiException e) {
      throw QueryRuntimeException.fromThrowable("Exception thrown while saving query", e);
    }
  }

  /**
   * Saves this QueryImpl as a new query term with the specified name.
   *
   * @param name The name by which to save the query.
   *
   * @return the new term
   * @throws com.cyc.kb.exception.KbException
   * @throws com.cyc.query.exception.QueryConstructionException
   * @throws com.cyc.session.exception.SessionCommunicationException
   * @see Query#save()
   */
  @Override
  public KbIndividual saveAs(String name) throws KbException, SessionCommunicationException,
                                                 QueryConstructionException {
    if (KbTermImpl.existsAsType(name)) {
      throw new CreateException("The name " + name + " is already used.");
    }
    KbIndividual newID = KbIndividual.findOrCreate(name);
    newID.instantiates(QueryConstants.getInstance().CYCL_QUERY_SPECIFICATION, UV_MT);
    setId(newID);
    save();
    return newID;
  }

  /**
   * Returns the categories to which this query belongs. Categories are associated with queries via
   * {@link #addCategory(com.cyc.query.Query.Category)}.
   *
   * @return the categories to which this query belongs.
   */
  @Override
  public Collection<String> getCategories() {
    return Collections.unmodifiableCollection(categories);
  }

  /**
   * Add a new category to which this query belongs.
   *
   * @param category
   */
  @Override
  public void addCategory(String category) {
    categories.add(category);
  }

  /**
   * Get the inference identifier for this query.
   *
   * @return the identifier, or null if inference has not been started.
   * @throws com.cyc.session.exception.SessionCommunicationException if there is a problem
   * communicating with Cyc.
   */
  @Override
  public InferenceIdentifier getInferenceIdentifier() throws SessionCommunicationException {
    return inference.getInferenceIdentifier();
  }

  /**
   * Get the metrics values for this QueryImpl.
   *
   * @throws com.cyc.session.exception.SessionCommunicationException if there is a problem
   * communicating with Cyc.
   * @return the metrics values.
   */
  @Override
  public InferenceMetricsValues getMetricsValues() throws SessionCommunicationException {
    try {
      return InferenceMetricsValuesImpl.fromInference(getInferenceIdentifier());
    } catch (CycConnectionException ex) {
      throw SessionCommunicationException.fromThrowable(ex);
    }
  }

  /**
   * Return the inference parameters for this query.
   *
   * @return the inference parameters.
   */
  @Override
  public synchronized InferenceParameters getInferenceParameters() {
    return params;
  }

  /**
   * Adds a listener to this query.
   *
   * @param listener
   * @return this query.
   */
  @Override
  public QueryImpl addListener(QueryListener listener) {
    listeners.add(listener);
    return this;
  }

  /**
   * Designates var as a variable to return bindings for.
   *
   * @param var
   * @return this query.
   * @throws IllegalArgumentException if var is not found in this query.
   * @throws IllegalStateException if query has already been started.
   */
  @Override
  public QueryImpl addQueryVariable(Variable var) {
    QueryImpl q = addQueryVariable((CycVariable) var.getCore());
    return q;
  }

  /**
   * Bind a query variable to a specified value. All occurrences of the variable in this query's
   * sentence will be replaced with the specified value.
   *
   * @param var must be a query variable in this query.
   * @param replacement the value to substitute for var.
   */
  @Override
  public void bindVariable(Variable var, Object replacement) {
    bindVariable((CycVariable) var.getCore(), replacement);
  }

  /* *
   * Bind a query variable to a specified value.
   *
   * @param varName The name of the variable, with or without the '?' prefix.
   * @param replacement
   * /
  @Override
  public void bindVariable(String varName, Object replacement) {
    bindVariable(CycObjectFactory.makeCycVariable(varName), replacement);
  }
  */

  /**
   * Designates var as a variable to <i>not</i> return bindings for.
   *
   * @param var
   * @return this QueryImpl
   * @throws IllegalArgumentException if var is not found in this query.
   * @throws IllegalStateException if query has already been started.
   */
  @Override
  public QueryImpl removeQueryVariable(Variable var) {
    QueryImpl q = removeQueryVariable((CycVariable) var.getCore());
    return q;
  }

  @Override
  public Set<Variable> getQueryVariables() throws KbException {
    return new HashSet(this.getQuerySentence().getVariables(true));
  }

  /**
   * Continues the query. Can be used if a query has not been started yet, has stopped due to
   * reaching the specified number of answers, or has used its alloted time or other resources and
   * is continuable.
   * <p>
   * Any resource constraints, e.g. time or number, get to "start over," so if the inference has
   * already used its alloted 5 seconds, or found the specified three answers, continuing it will
   * allow it to run for up to
   * <i>another</i>
   * 5 seconds, or until it finds up to <i>another</i> three answers.
   * <p>
   * Returns when the inference has stopped running.
   *
   * @see #setMaxAnswerCount(Integer)
   * @see #setMaxTime(Integer)
   * @see #isContinuable()
   * @see #setContinuable(boolean)
   *
   * @see InferenceWorker#continueInference(com.cyc.base.inference.InferenceParameters)
   */
  @Override
  public void continueQuery() {
    try {
      inference.continueInference();
    } catch (Exception e) {
      throw QueryRuntimeException.fromThrowable("Exception thrown while continuing query", e);
    }
  }

  /**
   * Identifies redundant clauses in this query.
   *
   * For instance, if one clause isa (isa ?X Dog) and another is (isa ?X GreatDane), that pair is
   * considered redundant. This method provides no guidance as to what can or should be done to
   * resolve the redundancy, and in fact it may be virtually harmless, as Cyc can often solve such a
   * query almost as efficiently as it can solve the more specific clause of the pair.
   *
   * @return a collection of pairs of any such clauses
   * @throws KbException
   * @throws com.cyc.session.exception.SessionCommunicationException if there is a problem
   * communicating with Cyc
   * @throws com.cyc.session.exception.OpenCycUnsupportedFeatureException when run against an
   * OpenCyc server
   */
  @Override
  public Collection<Collection<Sentence>> findRedundantClauses() throws KbException,
                                                                        SessionCommunicationException,
                                                                        OpenCycUnsupportedFeatureException {
    try {
      QUERY_COMPARISON_REQUIREMENTS.throwRuntimeExceptionIfIncompatible();
      Collection<Collection<FormulaSentence>> cycClauseCollections = findRedundantClausesCFS();
      Collection<Collection<Sentence>> clauses = new HashSet<>();
      Collection<Sentence> clauseCollections = new HashSet<>();
      for (Collection<FormulaSentence> c : cycClauseCollections) {
        for (FormulaSentence s : c) {
          clauseCollections.add(new SentenceImpl(s));
        }
        clauses.add(clauseCollections);
      }
      return clauses;
    } catch (CycConnectionException ex) {
      throw SessionCommunicationException.fromThrowable(ex);
    }
  }

  /**
   * Identifies unconnected clauses in this query. Generally, all clauses of a query will be
   * connected by a chain of variables that connect them together. Queries with unconnected clauses
   * are effectively separate queries, and running queries with disconnected clauses generally
   * results in a cartesian product of the answer sets of the two separate queries.
   *
   * @return a collection of the arg positions of any such clauses
   * @throws SessionCommunicationException if there is a problem communicating with Cyc.
   * @throws com.cyc.session.exception.OpenCycUnsupportedFeatureException when run against an
   * OpenCyc server
   */
  @Override
  public Collection<ArgPosition> findUnconnectedClauses() throws SessionCommunicationException,
                                                                 OpenCycUnsupportedFeatureException {
    try {
      QUERY_COMPARISON_REQUIREMENTS.throwRuntimeExceptionIfIncompatible();
      final Set<ArgPosition> argPositions = new HashSet<>();
      for (final Object obj : getCycAccess().converse().converseList(makeSublStmt(
              "find-unconnected-literals", querySentence))) {
        argPositions.add(new ArgPositionImpl((List<Integer>) obj));
      }
      return argPositions;
    } catch (CycConnectionException | CycApiException ex) {
      throw SessionCommunicationException.fromThrowable(ex);
    }
  }

  /**
   * Conjoin this sentence with otherQuery, attempting to unify and rename variables. Typically, two
   * different variables will unify into a single variable, causing all the uses of one of the
   * variables to be renamed with the name of the other. In some cases, additional renaming may
   * happen (e.g. if the queries contain mnemonic variables that become more tightly constrained as
   * a result of the unification, a new mnemonic variable may be used in place of both of the
   * original variables).
   *
   * @param otherQuery
   * @return the new query
   * @throws QueryConstructionException if there was a problem constructing the new query.
   * @throws com.cyc.session.exception.SessionCommunicationException
   * @throws com.cyc.session.exception.OpenCycUnsupportedFeatureException when run against an
   * OpenCyc server
   */
  @Override
  public Query merge(Query otherQuery) throws QueryConstructionException,
                                              SessionCommunicationException,
                                              OpenCycUnsupportedFeatureException {
    try {
      QUERY_COMPARISON_REQUIREMENTS.throwRuntimeExceptionIfIncompatible();
      QueryImpl otherQueryImpl = (QueryImpl) otherQuery;
      final String command = makeSublStmt("combine-queries",
                                                        querySentence, ContextImpl.asELMt(ctx),
                                                        params, otherQueryImpl.querySentence,
                                                        ContextImpl.asELMt(otherQueryImpl.ctx),
                                                        otherQueryImpl.params);
      final CycList newStuff = getCycAccess().converse().converseList(command);
      final Object paramsObj = newStuff.third();
      final List paramsList = CycObjectFactory.nil.equals(paramsObj)
                                      ? Collections.emptyList()
                                      : (List) paramsObj;
      final List sentenceAsList = (List) newStuff.first();
      return new QueryImpl(
              new FormulaSentenceImpl(sentenceAsList),
              constructContext((CycObject) newStuff.second()),
              DefaultInferenceParameters.fromPlist(getCycAccess(), paramsList));
    } catch (CycConnectionException | CycApiException ex) {
      throw SessionCommunicationException.fromThrowable(ex);
    }
  }

  /**
   * Set the inference mode for this QueryImpl.
   *
   * @param mode
   * @return this QueryImpl
   *
   * @see
   * com.cyc.base.inference.InferenceParameters#setInferenceMode(com.cyc.base.inference.InferenceMode)
   */
  @Override
  public QueryImpl setInferenceMode(InferenceMode mode) {
    getInferenceParameters().setInferenceMode(mode);
    return this;
  }

  /**
   * Check whether this QueryImpl is continuable. Queries that have not yet been run are considered
   * continuable, as well as ones whose parameters have the continuable flag set to
   * <code>true</code>.
   *
   * @see InferenceParameters#setContinuable(boolean)
   * @see #continueQuery()
   *
   * @return true iff it can be continued.
   */
  @Override
  public boolean isContinuable() {
    return !inference.hasBeenStarted()
                   || (getInferenceParameters().isContinuable() != null
                       && getInferenceParameters().isContinuable());
  }

  /**
   * Set the maximum number of answers (or sets of answers) that Cyc will attempt to find for this
   * QueryImpl. In some cases (such as when a set of answers is retrieved in a batch), more answers
   * than this may actually be returned. Once this number of answers has been reached, Cyc will not
   * actively look for additional answers.
   *
   * @param max
   * @return this QueryImpl
   *
   * @see com.cyc.base.inference.InferenceParameters#setMaxNumber(Integer)
   */
  @Override
  public QueryImpl setMaxAnswerCount(Integer max) {
    getInferenceParameters().setMaxAnswerCount(max);
    return this;
  }

  /**
   * Set the Context of this QueryImpl.
   *
   * @param ctx
   * @return this object.
   */
  @Override
  public QueryImpl setContext(final Context ctx) {
    this.ctx = ctx;
    return this;
  }

  /**
   * Set the soft timeout for this QueryImpl in seconds.
   *
   * @param max
   * @return this QueryImpl
   *
   * @see com.cyc.base.inference.InferenceParameters#setMaxTime(Integer)
   */
  @Override
  public QueryImpl setMaxTime(Integer max) {
    getInferenceParameters().setMaxTime(max);
    return this;
  }

  @Override
  public Integer getMaxTransformationDepth() {
    return getInferenceParameters().getMaxTransformationDepth();
  }

  @Override
  public QueryImpl setMaxTransformationDepth(Integer i) {
    getInferenceParameters().setMaxTransformationDepth(i);
    return this;
  }

  /**
   * Set the inference parameters for this query.
   *
   * @param params the inference parameters
   * @return this QueryImpl object.
   */
  @Override
  public QueryImpl setInferenceParameters(final InferenceParameters params) {
    if (params != null) {
      this.params = params;
    } else {
      this.params = getDefaultInferenceParameters();
    }
    return this;
  }

  /**
   * Sets the hypothesized clause of this QueryImpl. When the query is run, Cyc will assume that
   * this clause is true. If the clause is independently known to be false in the query context, the
   * query will be considered tautologous, and will fail.
   *
   * @param sentence
   * @return this QueryImpl.
   * @see Query#getQuerySentenceHypothesizedClause()
   * @throws IllegalStateException if query has already been started.
   */
  @Override
  public QueryImpl setQuerySentenceHypothesizedClause(Sentence sentence) {
    QueryImpl q = setQuerySentenceHypothesizedClause((FormulaSentenceImpl) sentence.getCore());
    return q;
  }

  /**
   * Sets the main (i.e. non-hypothesized) clause of this QueryImpl
   *
   * @param sentence
   * @return this QueryImpl.
   * @see Query#getQuerySentenceMainClause()
   * @throws IllegalStateException if query has already been started.
   */
  @Override
  public QueryImpl setQuerySentenceMainClause(Sentence sentence) {
    QueryImpl q = setQuerySentenceMainClause((FormulaSentenceImpl) sentence.getCore());
    return q;
  }

  /**
   * Designates vars as the variables to return bindings for.
   *
   * @param vars
   * @return this query.
   * @throws IllegalArgumentException if any of vars is not found in this query.
   * @throws IllegalStateException if query has already been started.
   */
  @Override
  public QueryImpl setQueryVariables(Collection<Variable> vars) {
    Collection<CycVariable> cycvars = new HashSet<>();
    for (Variable v : vars) {
      cycvars.add((CycVariable) v.getCore());
    }
    QueryImpl q = setQueryVariablesCyc(cycvars);
    return q;
  }

  @Override
  public Query setSubstitutions(Map<KbObject, Object> substitutions) {
    this.substitutions = Collections.unmodifiableMap(substitutions);
    final Map<CycObject, Object> kboToCoMap = KbUtils
            .convertKBObjectMapToCoObjectMap(this.substitutions);
    this.querySentence = this.originalQuerySentence.deepCopy();
    this.querySentence.applySubstitutionsDestructive(kboToCoMap);
    return this;
  }

  @Override
  public Query addSubstitutions(Map<KbObject, Object> addedSubstitutions) {
    final Map<KbObject, Object> newSubs = new LinkedHashMap();
    if (this.substitutions instanceof Map) {
      newSubs.putAll(this.substitutions);
    }
    if (addedSubstitutions instanceof Map) {
      newSubs.putAll(addedSubstitutions);
    }
    return setSubstitutions(newSubs);
  }

  /**
   * Starts the query.
   *
   *
   * @throws SessionCommunicationException if there is a problem communicating with Cyc.
   */
  @Override
  public void start() throws SessionCommunicationException {
    try {
      inference.start();
    } catch (IOException | CycConnectionException e) {
      throw SessionCommunicationException.fromThrowable(e);
    }
  }

  /**
   * Issues a request that the query stop immediately.
   *
   * @param patience If non-null, the query will be forcibly aborted if it does not stop before this
   * many seconds have elapsed.
   */
  @Override
  public void stop(final Integer patience) {
    inference.stop(patience);
  }

  /**
   * Get the Cyc session to be used for this query.
   *
   * @return a CycSession for this query.
   */
  @Override
  public final CycSession getCycSession() {
    return this.session;
  }

  /**
   * Specify that this inference should be retained by Cyc until the QueryImpl is closed. This can
   * be called before the query has been started, and must be called before the query has finished
   * running.
   *
   * @see Query#close()
   */
  @Override
  public void retainInference() {
    this.retainInference = true;
  }

  /**
   * Returns the number of answers found for this query. For running queries, the value returned by
   * this method may change as additional answers are found.
   *
   * @return the number of answers found for this query.
   */
  @Override
  public int getAnswerCount() {
    if (!getQueryVariablesCyc().isEmpty()) {
      return getResultSet().getCurrentRowCount();
    } else if (isTrue()) {
      return 1;
    } else {
      return 0;
    }
  }

  /**
   * Returns the list of answers for this query. For running queries, the value returned by this
   * method may change as additional answers are found.
   *
   * @return the list of answers
   * @throws SessionCommunicationException if there is a problem communicating with Cyc.
   */
  @Override
  public QueryAnswers<QueryAnswer> getAnswers() throws SessionCommunicationException {
    final QueryAnswers<QueryAnswer> localAnswers = new QueryAnswersImpl<>(answers);
    for (int i = localAnswers.size(); i < getAnswerCount(); i++) {
      localAnswers.add(getAnswer(i));
    }
    return localAnswers;
  }

  @Override
  public QueryAnswers<ParaphrasedQueryAnswer> getAnswers(Paraphraser paraphraser)
          throws SessionCommunicationException {
    final QueryAnswers<ParaphrasedQueryAnswer> localAnswers = new QueryAnswersImpl<>();
    for (int i = 0; i < getAnswerCount(); i++) {
      localAnswers.add(getAnswer(i, paraphraser));
    }
    return localAnswers;
  }

  /**
   * Returns the nth answer for this query. For the first answer, n == 0.
   *
   * @param n
   * @return the answer.
   * @throws SessionCommunicationException if there is a problem communicating with Cyc.
   */
  @Override
  public QueryAnswer getAnswer(final int n) throws SessionCommunicationException {
    if (answers.size() > n) {
      return answers.get(n);
    }
    final InferenceAnswer answerCyc = getAnswerCyc(n);
    return (answerCyc == null) ? null : new InferenceAnswerBackedQueryAnswer(answerCyc) {
      //Inner class is used so pointer to parent QueryImpl is maintained.
      // That prevents the QueryImpl from being closed and the inference from being destroyed
      // so long as this answer stays around.
    };
  }

  /**
   * Returns the nth answer for this query. For the first answer, n == 0.
   *
   * @param n
   * @return the answer.
   * @throws SessionCommunicationException if there is a problem communicating with Cyc.
   */
  @Override
  public ParaphrasedQueryAnswer getAnswer(final int n, final Paraphraser paraphraser)
          throws SessionCommunicationException {
    if (answers.size() > n
                && answers.get(n) instanceof ParaphrasedQueryAnswer
                && ((ParaphrasedQueryAnswer) answers.get(n)).getParaphraser().equals(paraphraser)) {
      return (ParaphrasedQueryAnswer) answers.get(n);
    }
    final InferenceAnswer answerCyc = getAnswerCyc(n);
    if (answerCyc == null) {
      return null;
    }
    ParaphrasedQueryAnswer answer = new InferenceAnswerBackedQueryAnswer(answerCyc, paraphraser) {
      //Inner class is used so pointer to parent QueryImpl is maintained.
      // That prevents the QueryImpl from being closed and the inference from being destroyed
      // so long as this answer stays around.
    };
    //make sure the list is big enough.
    for (int index = answers.size(); index < n; index++) {
      answers.add(index, null);
    }
    answers.add(n, answer);
    return answer;
  }

  /**
   * Returns the Context of this QueryImpl.
   *
   * @return the Context of this QueryImpl
   */
  @Override
  public Context getContext() {
    return ctx;
  }

  /**
   *
   * @return @throws KbException
   */
  @Override
  public Sentence getQuerySentence() throws KbException {
    Sentence s = new SentenceImpl(getQuerySentenceCyc());
    return s;
  }

  /**
   *
   * @param querySentence
   */
  @Override
  public QueryImpl setQuerySentence(Sentence querySentence) {
    setQuerySentence((FormulaSentence) querySentence.getCore());
    return this;
  }

  /**
   *
   * @return @throws KbException
   */
  @Override
  public Sentence getQuerySentenceMainClause() throws KbException {
    Sentence s = new SentenceImpl(getQuerySentenceMainClauseCyc());
    return s;
  }

  /**
   *
   * @return @throws KbException
   */
  @Override
  public Sentence getQuerySentenceHypothesizedClause() throws KbException {
    Sentence s = new SentenceImpl(getQuerySentenceHypothesizedClauseCyc());
    return s;
  }

  @Override
  public Map<KbObject, Object> getSubstitutions() {
    return substitutions;
  }

  @Override
  public Sentence getOriginalQuerySentence() throws KbException {
    return new SentenceImpl(this.originalQuerySentence);
  }

  /**
   * Get the CycL sentence from the specified answer to this query. Substitutes the set of bindings
   * from answer into the query sentence.
   *
   * @param answer
   * @return the answer sentence
   * @throws KbException
   */
  @Override
  public Sentence getAnswerSentence(QueryAnswer answer) throws KbException {
    Sentence sentence = getQuerySentenceMainClause();
    final List<Object> from = new ArrayList<>();
    final List<Object> to = new ArrayList<>();
    for (final Variable var : getQueryVariables()) {
      from.add(var);
      to.add(answer.getBinding(var));
    }
    return sentence.replaceTerms(from, to);
  }

  /**
   * Forget all results for this query. All settings on the QueryImpl are retained, including the
   * query sentence, context, and inference parameters. After a QueryImpl has been cleared, it can
   * be re-run, with possibly different results.
   *
   * @return this QueryImpl
   */
  @Override
  public QueryImpl clearResults() {
    inference.clear();
    return this;
  }

  /**
   * Returns the soft timeout for this QueryImpl in seconds.
   *
   * @return the soft timeout for this QueryImpl in seconds.
   *
   * @see com.cyc.base.inference.InferenceParameters#getMaxTime()
   */
  @Override
  public Integer getMaxTime() {
    if (params == null) {
      return null;
    } else {
      return params.getMaxTime();
    }
  }

  /**
   * Returns the maximum number of answers (or sets of answers) that Cyc will attempt to find for
   * this QueryImpl. In some cases (such as when a set of answers is retrieved in a batch), more
   * answers than this may actually be returned. Once this number of answers has been reached, Cyc
   * will not actively look for additional answers.
   *
   * @return the number cutoff for this QueryImpl.
   *
   * @see com.cyc.base.inference.InferenceParameters#getMaxNumber()
   */
  @Override
  public Integer getMaxAnswerCount() {
    if (params == null) {
      return null;
    } else {
      return params.getMaxAnswerCount();
    }
  }

  /**
   * Returns the inference mode for this QueryImpl.
   *
   * @return the inference mode for this QueryImpl.
   *
   * @see com.cyc.base.inference.InferenceParameters#getInferenceMode()
   */
  @Override
  public InferenceMode getInferenceMode() {
    if (params == null) {
      return null;
    } else {
      return params.getInferenceMode();
    }
  }

  /**
   * Return the InferenceStatus for this QueryImpl.
   *
   * @return the InferenceStatus for this QueryImpl.
   */
  @Override
  public InferenceStatus getStatus() {
    return this.inference.getInferenceStatus();
  }

  /**
   * Return the reason why this QueryImpl was suspended (if it was).
   *
   * @return the reason, or null if this QueryImpl was not suspended.
   * @see DefaultInferenceSuspendReasonforexamples.
   */
  @Override
  public InferenceSuspendReason getSuspendReason() {
    return this.inference.getSuspendReason();
  }

  /**
   *
   * @return true iff this query has been proven true.
   * @throws RuntimeException if the query has open variables.
   * @see com.cyc.base.inference.InferenceResultSet#getTruthValue()
   */
  @Override
  public boolean isTrue() {
    return getResultSet().getTruthValue();
  }

  /**
   * Is this query either True (if a boolean query) or does it have bindings (if non-boolean)
   *
   * @return True if there are bindings (or it's a true boolean query), false if there are no
   * bindings (or it's a false boolean query).
   *
   */
  @Override
  public boolean isProvable() {
    return getAnswerCount() > 0;
  }

  /**
   * Closes this query's result set, and releases resources on the Cyc server. See
   * {@link com.cyc.query.KBInferenceResultSet#close()} for more details on what happens when a
   * query is closed.
   * <p>
   * It is good practice to always invoke this method explicitly as soon as a QueryImpl is no longer
   * needed, as they can take up significant amounts of memory on the Cyc server.
   *
   * @see com.cyc.query.KBInferenceResultSet#close()
   */
  @Override
  public void close() {
    inference.close();
    UNCLOSED_QUERIES.remove(this);
  }

  /**
   * Returns the timeout for the {@link #close()} method.
   *
   * @return timeout in milliseconds
   */
  @Override
  public long getCloseTimeout() {
    return inference.closeTimeoutMS;
  }

  /**
   * Sets the timeout for the {@link #close()} method.
   *
   * @param timeoutMS timeout in milliseconds
   * @return
   */
  @Override
  public Query setCloseTimeout(long timeoutMS) {
    inference.closeTimeoutMS = timeoutMS;
    return this;
  }

  /**
   *
   * @return this query's result set. The QueryResultSetImpl is an object that may be updated
   * dynamically for running queries. This contrasts with {@link #getAnswersCyc()} which returns a
   * static list of the answers at the time is was called.
   *
   * @see com.cyc.query.client.QueryResultSetImpl
   */
  @Override
  public synchronized QueryResultSet getResultSet() {
    try {
      return inference.getResultSet();
    } catch (Exception e) {
      throw QueryRuntimeException.fromThrowable("Exception thrown while getting result set", e);
    }
  }

  @Override
  public Set<KbObject> getUnresolvedIndexicals() throws KbException, SessionCommunicationException {
    return new HashSet(getQuerySentence().getIndexicals());
  }

  @Override
  public QueryRulesImpl getRules() throws QueryConstructionException, KbException {
    if (this.rules == null) {
      this.rules = new QueryRulesImpl(this);
    }
    return this.rules;
  }

  @Override
  public String toString() {
    return "Query: " + querySentence;
  }

  @Override
  public int hashCode() {
    int hash = 1;
    hash = hash * 17 + (ctx == null ? 0 : ctx.hashCode());
    hash = hash * 31 + (params == null ? 0 : params.hashCode());
    hash = hash * 13 + (querySentence == null ? 0 : querySentence.hashCode());
    return hash;
  }

  @Override
  public boolean equals(Object o) {
    if ((o == null) || !(o instanceof QueryImpl)) {
      return false;
    }
    return this.hashCode() == o.hashCode();
  }

  /**
   * Do not use this value directly; instead, call {@link QueryImpl#getQueryWorkerTimeoutMillis()}.
   *
   * @return the number of milliseconds that a QueryWorker will wait before throwing an exception if
   * no soft timeout is specified in the query's inference parameters.
   */
  public long getDefaultQueryWorkerTimeoutMillis() {
    return this.defaultQueryWorkerTimeoutMillis;
  }

  public void setDefaultQueryWorkerTimeoutMillis(long queryWorkerTimeoutMillis) {
    this.defaultQueryWorkerTimeoutMillis = queryWorkerTimeoutMillis;
  }

  /**
   * Returns the number of milliseconds that a QueryWorker will wait, once started, before throwing
   * an exception. A value of 0 milliseconds means to wait forever.
   *
   * <p>
   * If a soft timeout is specified in the inference parameters, the QueryWorker's hard timeout will
   * be twice that duration to allow for additional overhead on top of the time required for the
   * actual inference. If no soft timeout is specified in the inference parameters, this method will
   * return the value of {@link QueryImpl#getDefaultQueryWorkerTimeoutMillis()}.
   *
   * @return the number of milliseconds that a QueryWorker will wait before throwing an exception.
   */
  public long getQueryWorkerTimeoutMillis() {
    if (getInferenceParameters() != null) {
      final Integer maxTime = getInferenceParameters().getMaxTime();
      if (maxTime != null) {
        return maxTime * 2 * 1000;
      }
    }
    return getDefaultQueryWorkerTimeoutMillis();
  }

  //====|    Internal methods    |================================================================//
  @Override
  protected void finalize() throws Throwable {
    close();
    super.finalize();
  }

  /**
   * Get the Cyc image to be used for this query.
   *
   * @return a CycAccess for the Cyc image.
   */
  protected final CycAccess getCycAccess() {
    return this.cyc;
  }

  /**
   * Returns the nth answer for this query. For the first answer, n == 0.
   *
   * @param n
   * @return the answer.
   * @throws SessionCommunicationException if there is a problem communicating with Cyc.
   */
  @Deprecated
  InferenceAnswer getAnswerCyc(final int n) throws SessionCommunicationException {
    if (n >= getAnswerCount()) {
      throw new IllegalArgumentException("Can't get answer " + n
                                                 + ". Query has only " + getAnswerCount()
                                                 + " answers.");
    }
    final InferenceIdentifier inferenceIdentifier = getInferenceIdentifier();
    if (inferenceIdentifier != null) {
      return new CycBackedInferenceAnswer(
              new SpecifiedInferenceAnswerIdentifier(inferenceIdentifier, n));
    } else if (getResultSet() != null) {
      final QueryResultSetImpl resultSet = (QueryResultSetImpl) getResultSet();
      return new ResultSetInferenceAnswer((DefaultResultSet) resultSet.getInferenceResultSet(), n);
    } else {
      return null;
    }
  }

  /**
   * Get the CycL sentence of this query.
   *
   * @return the query sentence
   */
  @Deprecated
  FormulaSentence getQuerySentenceCyc() {
    return querySentence;
  }

  /**
   * Returns the main (that is, non-hypothesized) clause of this QueryImpl. All valid queries have a
   * main sentence clause.
   *
   * @return the main (i.e. non-hypothesized) clause of this query.
   */
  @Deprecated
  FormulaSentence getQuerySentenceMainClauseCyc() {
    if (querySentence.isConditionalSentence()) {
      return (FormulaSentence) querySentence.getArg2();
    } else {
      return querySentence;
    }
  }

  /**
   * Returns the hypothesized clause of this QueryImpl, if any. Most queries have no hypothesized
   * clause, in which case this method will return null.
   *
   * @return the hypothesized clause of this query, if any, or null if there is none.
   */
  @Deprecated
  FormulaSentence getQuerySentenceHypothesizedClauseCyc() {
    if (querySentence.isConditionalSentence()) {
      return (FormulaSentence) querySentence.getArg1();
    } else {
      return null;
    }
  }

  private QueryImpl addQueryVariable(CycVariable var) {
    if (!querySentence.treeContains(var)) {
      throw new IllegalArgumentException(
              var + " is not contained in " + querySentence);
    }
    if (inference.hasBeenStarted()) {
      throw new IllegalStateException("Query has already been started.");
    }
    if (!getQueryVariablesCyc().contains(var)) {
      querySentence.existentiallyUnbind(var);
    }
    return this;
  }

  private QueryImpl removeQueryVariable(CycVariable var) {
    if (!querySentence.treeContains(var)) {
      throw new IllegalArgumentException(
              var + " is not contained in " + querySentence);
    }
    if (inference.hasBeenStarted()) {
      throw new IllegalStateException("Query has already been started.");
    }
    if (getQueryVariablesCyc().contains(var)) {
      querySentence.existentiallyBind(var);
    }
    return this;
  }

  private List<CycVariable> getQueryVariablesCyc() {
    return querySentence.findQueryableVariables();
  }

  /**
   * Designates vars as the variables to return bindings for.
   *
   * @param vars
   * @return this query.
   * @throws IllegalArgumentException if any of vars is not found in this query.
   * @throws IllegalStateException if query has already been started.
   */
  private QueryImpl setQueryVariablesCyc(Collection<CycVariable> vars) {
    for (final CycVariable var : getQueryVariablesCyc()) {
      removeQueryVariable(var);
    }
    for (final CycVariable var : vars) {
      addQueryVariable(var);
    }
    return this;
  }

  private void bindVariable(CycVariable var, Object replacement) {
    if (!getQueryVariablesCyc().contains(var)) {
      throw new IllegalArgumentException(
              var + " is not a query variable in " + getQuerySentenceCyc());
    }
    if (replacement instanceof KbObject) {
      replacement = ((KbObject) replacement).getCore();
    }
    getQuerySentenceCyc().substituteDestructive(var, replacement);
  }

  private DefaultInferenceParameters getDefaultInferenceParameters() {
    return new DefaultInferenceParameters(getCycAccess(), true);
  }

  /**
   * Substitute specified terms for specified indexicals in the query sentence.
   *
   * @param indexicals - a map from indexicals to their replacements.
   */
  @Deprecated
  private void substituteTermsWithCycObject(Map<CycObject, Object> indexicals) {
    querySentence.applySubstitutionsDestructive(indexicals);
  }

  /**
   * Identifies redundant clauses in this query.
   *
   * For instance, if one clause isa (isa ?X Dog) and another is (isa ?X GreatDane), that pair is
   * considered redundant. This method provides no guidance as to what can or should be done to
   * resolve the redundancy, and in fact it may be virtually harmless, as Cyc can often solve such a
   * query almost as efficiently as it can solve the more specific clause of the pair.
   *
   * @return a collection of pairs of any such clauses
   * @throws com.cyc.base.exception.CycConnectionException
   */
  @Deprecated
  private Collection<Collection<FormulaSentence>> findRedundantClausesCFS()
          throws CycConnectionException {
    final Set<Collection<FormulaSentence>> clausePairs = new HashSet<>();
    for (final Object obj : getCycAccess().converse().converseList(makeSublStmt(
            "find-redundant-literals",
            querySentence, ContextImpl.asELMt(ctx)))) {
      final CycList dottedPair = (CycList) obj;
      final FormulaSentence sentence1 = new FormulaSentenceImpl(
              (CycList) dottedPair.first());
      final FormulaSentence sentence2 = new FormulaSentenceImpl(
              (CycList) dottedPair.rest());
      clausePairs.add(Arrays.asList(sentence1, sentence2));
    }
    return clausePairs;
  }

  /**
   * Sets the hypothesized clause of this QueryImpl. When the query is run, Cyc will assume that
   * this clause is true. If the clause is independently known to be false in the query context, the
   * query will be considered tautologous, and will fail.
   *
   * @param sentence
   * @return this QueryImpl.
   * @see Query#getQuerySentenceHypothesizedClause()
   * @throws IllegalStateException if query has already been started.
   */
  @Deprecated
  private QueryImpl setQuerySentenceHypothesizedClause(
          FormulaSentenceImpl sentence) {
    if (querySentence.isConditionalSentence()) {
      querySentence.setSpecifiedObject(ArgPositionImpl.ARG1, sentence);
    } else {
      querySentence = FormulaSentenceImpl.makeConditional(sentence, querySentence);
    }
    return this;
  }

  /**
   * Sets the main (i.e. non-hypothesized) clause of this QueryImpl
   *
   * @param sentence
   * @return this QueryImpl.
   * @see Query#getQuerySentenceMainClause()
   * @throws IllegalStateException if query has already been started.
   */
  @Deprecated
  private QueryImpl setQuerySentenceMainClause(
          FormulaSentenceImpl sentence) {
    if (querySentence.isConditionalSentence()) {
      querySentence.setSpecifiedObject(ArgPositionImpl.ARG2, sentence);
    } else {
      querySentence = sentence;
    }
    return this;
  }

  /* *
   * Get the CycL sentence from the specified answer to this query. Substitutes the set of bindings
   * from answer into the query sentence.
   *
   * @param answer
   * @throws CycConnectionException if there is a problem communicating with Cyc.
   * @return the answer sentence
   * /
  @Deprecated
  private FormulaSentence getAnswerSentenceCyc(InferenceAnswer answer)
          throws CycConnectionException {
    final FormulaSentence sentence = getQuerySentenceMainClauseCyc().deepCopy();
    for (final CycVariable var : getQueryVariablesCyc()) {
      sentence.substituteDestructive(var, answer.getBinding(var));
    }
    return sentence;
  }

  /**
   * Returns the list of answers for this query. For running queries, the value returned by this
   * method may change as additional answers are found.
   *
   * @return the list of answers
   * @throws SessionCommunicationException if there is a problem communicating with Cyc.
   * /
  @Deprecated
  private List<InferenceAnswer> getAnswersCyc() throws SessionCommunicationException {
    final List<InferenceAnswer> localAnswers = new ArrayList<>(
            getAnswerCount());
    for (int i = 0; i < getAnswerCount(); i++) {
      localAnswers.add(getAnswerCyc(i));
    }
    return localAnswers;
  }
   */
  boolean requiresInferenceWorker() {
    final InferenceParameters inferenceParameters = getInferenceParameters();
    // result-sort-ordering requires that all answers be returned at once instead of dribbling in as 
    // they do with async queries.
    // TODO: add requiresSynchronousQuery (which might check Cyc to see which params require 
    //       synchronicity. - daves 2017-03-30
    if (inferenceParameters.containsKey(RESULT_SORT_ORDER)) {
      return false;
    } else if (inferenceParameters.containsKey(RETURN)) {
      return true;
    } else if (Boolean.TRUE.equals(inferenceParameters.isContinuable())) {
      return true;
    } else if (Boolean.TRUE.equals(inferenceParameters.isBrowsable())) {
      return true;
    } else if (!listeners.isEmpty()) {
      return true;
    } else if (retainInference == true) {
      return true;
    } else {
      return CycObjectFactory.t.equals(inferenceParameters.get(COMPUTE_ANSWER_JUSTIFICATIONS));
    }
  }

  /**
   * Set the CycL sentence of this query.
   *
   * @param querySentence
   */
  @Deprecated
  private void setQuerySentence(FormulaSentence querySentence) {
    this.querySentence = querySentence;
  }

  // TODO: remove? Added for extraction of QueryInference. - nwinant, 2017-08-01
  InferenceParameters getParams() {
    return params;
  }

  // TODO: remove? Added for extraction of QueryInference. - nwinant, 2017-08-01
  Set<QueryListener> getListeners() {
    return listeners;
  }

}
