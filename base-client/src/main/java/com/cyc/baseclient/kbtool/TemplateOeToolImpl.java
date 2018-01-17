/*
 * Copyright 2017 Cycorp, Inc..
 *
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
 */
package com.cyc.baseclient.kbtool;

import com.cyc.base.CycAccess;
import com.cyc.base.cycobject.CycConstant;
import com.cyc.base.cycobject.CycList;
import com.cyc.base.cycobject.CycObject;
import com.cyc.base.cycobject.CycSymbol;
import com.cyc.base.cycobject.DenotationalTerm;
import com.cyc.base.cycobject.ElMt;
import com.cyc.base.cycobject.Fort;
import com.cyc.base.cycobject.Guid;
import com.cyc.base.exception.CycApiException;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.baseclient.AbstractKbTool;
import com.cyc.baseclient.cycobject.CycArrayList;
import com.cyc.baseclient.cycobject.CycConstantImpl;
import com.cyc.baseclient.cycobject.CycSymbolImpl;
import com.cyc.baseclient.inference.params.DefaultInferenceParameters;
import com.cyc.baseclient.inference.params.SpecifiedInferenceParameters;
import com.cyc.query.parameters.InferenceParameters;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.cyc.baseclient.CommonConstants.INFERENCE_PSC;
import static com.cyc.baseclient.CommonConstants.PREDICATE;
import static com.cyc.baseclient.connection.SublApiHelper.makeSublStmt;
import static java.util.stream.Collectors.toList;

/*
 * #%L
 * File: TemplateOeToolImpl.java
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

/**
 *
 * @author nwinant
 */
public class TemplateOeToolImpl extends AbstractKbTool {
  
  //====|    Enums    |===========================================================================//
  
  public enum ToeProcessingOverride {
    
    /**
     * The max number of total errors when asserting before we quit; default is unlimited.
     */
    MAX_NUM_ERRORS,
    
    /**
     * The max number of errors we get in a row before we quit; default is unlimited.
     */
    MAX_NUM_ERRORING_SEQUENTIAL_TOE_ASSERTS,
    
    /**
     * Skips asserting, writing out explanation sentences, and writing out sets of supports; default
     * is false.
     */
    SKIP_ALL_TOE_ASSERT_PROCESSING,
    
    /**
     * Skips asserting; still writes out explanation sentences and supports; default is false.
     */
    SKIP_TOE_ASSERT_SENTENCE,
    
    /**
     * ALIST of <tt>#$TemplateOESpecificationParameters</tt> that we want to override.
     * <p>
     * Currently, we only support the following:
     * <pre>
     * #$WriteOutExplanationStringsParameter
     * #$WriteOutExplanationStringsDestinationMtParameter
     * #$WriteOutSetOfSupportsParameter
     * #$WriteOutSetOfSupportsDestinationMtParameter
     * #$WriteOutSetOfSupportsWithExplanationStringTogetherParameter
     * </pre>
     * 
     * @see TemplateOeSpecificationParameter
     */
    TEMPLATE_OE_SPECIFICATION_PARAMETERS;

    private final CycSymbol symbol;
    
    private ToeProcessingOverride() {
      this.symbol = new CycSymbolImpl(":" + this.toString().replace("_", "-"));
    }
    
    public CycSymbol toSymbol() {
      return this.symbol;
    }
  }
  
  /**
   * <tt>#$TemplateOESpecificationParameters</tt> which may be overridden via
   * {@link ToeProcessingOverride#TEMPLATE_OE_SPECIFICATION_PARAMETERS}.
   * <p>
   */
  public enum TemplateOeSpecificationParameter {

    WRITE_OUT_EXPLANATION_STRINGS_PARAMETER(
            "#$WriteOutExplanationStringsParameter",
            "45a7834e-830f-4385-8736-0442ad123aa4"),
    
    WRITE_OUT_EXPLANATION_STRINGS_DESTINATION_MT_PARAMETER(
            "#$WriteOutExplanationStringsDestinationMtParameter",
            "4980386b-625f-4edb-afee-26b97978adc6"),
    
    WRITE_OUT_SETOFSUPPORTS_PARAMETER(
            "#$WriteOutSetOfSupportsParameter",
            "4075c5a3-e7db-46a6-b8f5-3579354aee81"),
    
    WRITE_OUT_SET_OF_SUPPORTS_DESTINATION_MT_PARAMETER(
            "#$WriteOutSetOfSupportsDestinationMtParameter",
            "5e9c7449-17db-4bad-826c-b6878495e847"),
    
    WRITE_OUT_SET_OF_SUPPORTS_WITH_EXPLANATION_STRING_TOGETHER_PARAMETER(
            "#$WriteOutSetOfSupportsWithExplanationStringTogetherParameter",
            "0ab32ef4-8604-445a-aa81-8c9ae4026745");
    
    private final CycConstant term;
    
    private TemplateOeSpecificationParameter(String name, String guidString) {
      this.term = new CycConstantImpl(name, new Guid(guidString));
    }
    
    public CycConstant toTerm() {
      return this.term;
    }
  }
  
  /**
   * 
   * @see TemplateOeToolImpl#ALL_IPC_QUEUE_ELEMENTS
   */
  public enum IpcQueueElement {
    ERRORS, 
    BINDINGS,
    ASSERTION_RESULT,
    ASSERTION_MT, 
    ASSERTION_SENTENCE;
    
    private final CycSymbol symbol;
    
    private IpcQueueElement() {
      this.symbol = new CycSymbolImpl(":" + this.toString().replace("_", "-"));
    }
    
    public CycSymbol toSymbol() {
      return this.symbol;
    }
    
    public static List<CycSymbol> toSymbols(IpcQueueElement... elements) {
      return Arrays.stream(elements).map(IpcQueueElement::toSymbol).collect(toList());
    }
    
  }
  
  //====|    Fields    |==========================================================================//
  
  public static final String CREATE_NEW_QUEUE_ITERATIVE_TOE_ASSERT_VIA_KBQ_FUNC
          = "create-new-queue-iterative-toe-assert-via-kbq";
  
  /**
   * SubL function to process a KBQ and any TOE-assert templates it might have.
   */
  public static final String ITERATIVE_TOE_ASSERT_VIA_KBQ_FUNC = "iterative-toe-assert-via-kbq";
  
  public static final String CURRENT_QUEUE_SIZE_ITERATIVE_TOE_ASSERT_VIA_KBQ_FUNC
          = "current-queue-size-iterative-toe-assert-via-kbq";
  
  public static final String QUEUE_ITERATIVE_TOE_ASSERT_VIA_KBQ_EMPTY_P_FUNC
          = "queue-iterative-toe-assert-via-kbq-empty-p";
  
  public static final String DEQUEUE_ITERATIVE_TOE_ASSERT_VIA_KBQ_FUNC
          = "dequeue-iterative-toe-assert-via-kbq";
  
  public static final String DEQUEUE_N_WITH_TIMEOUT_ITERATIVE_TOE_ASSERT_VIA_KBQ_FUNC
          = "dequeue-n-with-timeout-iterative-toe-assert-via-kbq";
  
  /**
   * SubL function to convert integer identifier that maps to an ipc-queue in
   * {@code *id-to-ipc-queue-mapping-for-iterative-toe-assert-via-kbq*}.
   */
  public static final String LOOK_UP_IPC_QUEUE_FOR_ITERATIVE_TOE_ASSERT_VIA_KBQ_FUNC
          = "look-up-ipc-queue-for-iterative-toe-assert-via-kbq";
  
  /**
   * List containing all elements of {@link IpcQueueElement}.
   */
  public static final List<CycSymbol> ALL_IPC_QUEUE_ELEMENTS
          = Collections.unmodifiableList(IpcQueueElement.toSymbols(
                  IpcQueueElement.values()));

  public static final List<CycSymbol> IPC_QUEUE_QUERY_ELEMENTS
          = Collections.unmodifiableList(IpcQueueElement.toSymbols(
                  IpcQueueElement.BINDINGS,
                  IpcQueueElement.ERRORS));

  public static final List<CycSymbol> IPC_QUEUE_ERROR_ELEMENTS
          = Collections.unmodifiableList(IpcQueueElement.toSymbols(
                  IpcQueueElement.ERRORS));

  public static final Map<CycObject, Object> EMPTY_SUBSTITUTIONS = Collections.EMPTY_MAP;
  
  private static final Logger LOG = LoggerFactory.getLogger(TemplateOeToolImpl.class);
  
  //====|    Construction    |====================================================================//
  
  public TemplateOeToolImpl(CycAccess client) {
    super(client);
  }
  
  //====|    Methods    |=========================================================================//
  
  public int getNewQueueId() throws CycConnectionException {
    final String cmd = makeSublStmt(CREATE_NEW_QUEUE_ITERATIVE_TOE_ASSERT_VIA_KBQ_FUNC);
    LOG.debug("Command: {}", cmd);
    final int result = getCyc().converse().converseInt(cmd);
    LOG.debug("      -> {}", result);
    return result;
  }
  
  /**
   * This function takes a query specification and iteratively uses the bindings from the query to
   * make assertions. These assertions come from instances of {@code #$TOEAssertSpecification}
   * associated with query spec via the predicate {@code #$queryAssociatedTOEAssertSpecification} in
   * any microtheory.
   * <p>
   * All parameters except {@code querySpec} and {@code ipcQueueIdentifier} have defaults for null.
   *
   * @param querySpec                    the query specification to process, an instance of
   *                                     {@code #$CycLQuerySpecification}. These can be created via
   *                                     {@link #getNewQueueId()}.
   * @param focalMt                      all assertions for predicates contained within
   *                                     {@code focalPredicateCollection} will be retrieved from
   *                                     this mt. Defaults to {@code #$InferencePSC}.
   * @param focalPredicateCollection     the Collection of predicates for which to apply the
   *                                     {@code focalMt}. Must be {@code #$Predicate} or a spec
   *                                     thereof. Defaults to {@code #$Predicate}.
   * @param overridingQueryProperties    a PLIST of overriding properties for the query. Defaults to
   *                                     no overridden properties.
   * @param substitutions                an ALIST of {@code (old . new)} pairs to deal with
   *                                     indexicals. Defaults to no substitutions.
   * @param ipcQueueIdentifier           an integer identifier used to specify which ipc-queue to
   *                                     setf results onto
   * @param ipcQueueElementSpecification a list of keywords specifying what should be placed on the
   *                                     ipc-queue. Defaults to all queue elements.
   * @param toeProcessingOverrides       an ALIST of overrides that change how we deal with
   *                                     processing the assertions. Defaults to no TOE processing
   *                                     overrides.
   *
   * @return the final InferenceSuspendReason
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException        if the api request results in a cyc server error
   */
  public CycObject processTemplate(final DenotationalTerm querySpec,
                                   final ElMt focalMt,
                                   final Fort focalPredicateCollection,
                                   final InferenceParameters overridingQueryProperties,
                                   final Map<CycObject, Object> substitutions,
                                   final int ipcQueueIdentifier,
                                   final List<CycSymbol> ipcQueueElementSpecification,
                                   final CycList<Object> toeProcessingOverrides)
          throws CycConnectionException, CycApiException {
    if (querySpec == null) {
      throw CycApiException.from(new NullPointerException("Query specification is null"));
    }
    // Arguments, after sanity checks:
    final ElMt focalMtArg;
    final Fort focalPredicateCollectionArg;
    final Object overridingQueryPropertiesArg;
    final CycList substitutionsArg;
    final int ipcQueueIdentifierArg = ipcQueueIdentifier;
    final List<CycSymbol> ipcQueueElementSpecificationArg;
    final CycList<Object> toeProcessingOverridesArg;
    {
      // Focal mts
      // NOTE: Ideally, we would supply nothing when these are null,
      //       but #$Predicate & #$InferencePSC are reasonable defaults - nwinant, 2017-11-02
      focalPredicateCollectionArg = (focalMt != null) && (focalPredicateCollection != null)
                                            ? focalPredicateCollection
                                            : PREDICATE;
      focalMtArg = (focalMt != null)
                           ? focalMt
                           : INFERENCE_PSC;
      LOG.trace("focalMt                  : {} (originally: {})",
              focalMtArg, focalMt);
      LOG.trace("focalPredicateCollection : {} (originally: {})",
              focalPredicateCollectionArg, focalPredicateCollection);
      // Inference parameters
      if (overridingQueryProperties instanceof SpecifiedInferenceParameters
                  && !(overridingQueryProperties instanceof DefaultInferenceParameters)) {
        overridingQueryPropertiesArg = DefaultInferenceParameters
                .fromSpecifiedInferenceParameters(
                        getCyc(),
                        (SpecifiedInferenceParameters) overridingQueryProperties).cycListApiValue();
      } else {
        overridingQueryPropertiesArg = (overridingQueryProperties != null)
                                         ? overridingQueryProperties.cycListApiValue()
                                         : new SpecifiedInferenceParameters().cycListApiValue();
      }
      // Substitutions
      substitutionsArg = CycArrayList.fromMap((substitutions != null)
                                                      ? substitutions
                                                      : EMPTY_SUBSTITUTIONS);
      // ipc-queue element specification
      ipcQueueElementSpecificationArg = (ipcQueueElementSpecification != null)
                                                ? ipcQueueElementSpecification
                                                : ALL_IPC_QUEUE_ELEMENTS;
      // TOE processing overrides
      toeProcessingOverridesArg = (toeProcessingOverrides != null)
                                          ? toeProcessingOverrides
                                          : new CycArrayList();
    }
    // Build & run SubL
    final String cmd = makeSublStmt(
            ITERATIVE_TOE_ASSERT_VIA_KBQ_FUNC,
            querySpec,
            focalMtArg,
            focalPredicateCollectionArg,
            overridingQueryPropertiesArg,
            substitutionsArg,
            ipcQueueIdentifierArg,
            ipcQueueElementSpecificationArg,
            toeProcessingOverridesArg);
    LOG.debug("Command: {}", cmd);
    final CycObject result = getCyc().converse().converseCycObject(cmd);
    LOG.debug("      -> {}", result);
    return result;
  }
  
  /**
   * This function takes a query specification and iteratively uses the bindings from the query to
   * make assertions. All parameters except {@code querySpec} and {@code ipcQueueIdentifier} have
   * defaults for null.
   *
   * @param querySpec                    the query specification to process
   * @param focalMt                      the mt for {@code focalPredicateCollection} assertions
   * @param focalPredicateCollection     the Collection of predicates for {@code focalMt}
   * @param overridingQueryProperties    a PLIST of overriding properties for the query
   * @param substitutions                an ALIST of {@code (old . new)} indexical pairs
   * @param ipcQueueIdentifier           an integer identifier used to specify the ipc-queue
   * @param ipcQueueElementSpecification a list of keywords specifying contents of the ipc-queue
   *
   * @return the final InferenceSuspendReason
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException        if the api request results in a cyc server error
   *
   * @see #processTemplate(com.cyc.base.cycobject.DenotationalTerm, com.cyc.base.cycobject.ElMt,
   * com.cyc.base.cycobject.Fort, com.cyc.query.parameters.InferenceParameters, java.util.Map, int,
   * java.util.List, com.cyc.base.cycobject.CycList)
   */
  private CycObject processTemplate(final DenotationalTerm querySpec,
                                    final ElMt focalMt,
                                    final Fort focalPredicateCollection,
                                    final InferenceParameters overridingQueryProperties,
                                    final Map<CycObject, Object> substitutions,
                                    final int ipcQueueIdentifier,
                                    final List<CycSymbol> ipcQueueElementSpecification)
          throws CycConnectionException, CycApiException {
    return processTemplate(
            querySpec, focalMt, focalPredicateCollection, overridingQueryProperties, substitutions,
            ipcQueueIdentifier, ipcQueueElementSpecification, null);
  }
  
  /**
   * This function takes a query specification and iteratively uses the bindings from the query to
   * make assertions. All parameters except {@code querySpec} and {@code ipcQueueIdentifier} have
   * defaults for null.
   *
   * @param querySpec                    the query specification to process
   * @param overridingQueryProperties    a PLIST of overriding properties for the query
   * @param substitutions                an ALIST of {@code (old . new)} indexical pairs
   * @param ipcQueueIdentifier           an integer identifier used to specify the ipc-queue
   * @param ipcQueueElementSpecification a list of keywords specifying contents of the ipc-queue
   *
   * @return the final InferenceSuspendReason
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException        if the api request results in a cyc server error
   *
   * @see #processTemplate(com.cyc.base.cycobject.DenotationalTerm, com.cyc.base.cycobject.ElMt,
   * com.cyc.base.cycobject.Fort, com.cyc.query.parameters.InferenceParameters, java.util.Map, int,
   * java.util.List, com.cyc.base.cycobject.CycList)
   */
  public CycObject processTemplate(final DenotationalTerm querySpec,
                                   final InferenceParameters overridingQueryProperties,
                                   final Map<CycObject, Object> substitutions,
                                   final int ipcQueueIdentifier,
                                   final List<CycSymbol> ipcQueueElementSpecification)
          throws CycConnectionException, CycApiException {
    return processTemplate(
            querySpec, null, null, overridingQueryProperties, substitutions,
            ipcQueueIdentifier, ipcQueueElementSpecification, null);
  }
  
  /**
   * This function takes a query specification and iteratively uses the bindings from the query to
   * make assertions. All parameters except {@code querySpec} and {@code ipcQueueIdentifier} have
   * defaults for null.
   *
   * @param querySpec                    the query specification to process
   * @param overridingQueryProperties    a PLIST of overriding properties for the query
   * @param substitutions                an ALIST of {@code (old . new)} indexical pairs
   * @param ipcQueueIdentifier           an integer identifier used to specify the ipc-queue
   *
   * @return the final InferenceSuspendReason
   *
   * @throws CycConnectionException if a communications error occurs
   * @throws CycApiException        if the api request results in a cyc server error
   *
   * @see #processTemplate(com.cyc.base.cycobject.DenotationalTerm, com.cyc.base.cycobject.ElMt,
   * com.cyc.base.cycobject.Fort, com.cyc.query.parameters.InferenceParameters, java.util.Map, int,
   * java.util.List, com.cyc.base.cycobject.CycList)
   */
  public CycObject processTemplate(final DenotationalTerm querySpec,
                                   final InferenceParameters overridingQueryProperties,
                                   final Map<CycObject, Object> substitutions,
                                   final int ipcQueueIdentifier)
          throws CycConnectionException, CycApiException {
    return processTemplate(
            querySpec, null, null, overridingQueryProperties, substitutions,
            ipcQueueIdentifier, null);
  }
  
  public int getNumberResultsRemaining(int queueId) throws CycConnectionException {
    final String cmd = makeSublStmt(
            CURRENT_QUEUE_SIZE_ITERATIVE_TOE_ASSERT_VIA_KBQ_FUNC, queueId);
    LOG.debug("Command: {}", cmd);
    final int result = getCyc().converse().converseInt(cmd);
    LOG.debug("      -> {}", result);
    return result;
  }
  
  /**
   * Returns whether more results are available on the ipc-queue.
   * 
   * @param queueId
   * @return
   * @throws CycConnectionException 
   */
  public boolean hasMoreResults(int queueId) throws CycConnectionException {
    final String cmd = makeSublStmt(
            QUEUE_ITERATIVE_TOE_ASSERT_VIA_KBQ_EMPTY_P_FUNC, queueId);
    LOG.debug("Command: {}", cmd);
    final boolean isEmpty = getCyc().converse().converseBoolean(cmd);
    final boolean result = !isEmpty;
    LOG.debug("      -> {} (raw result: {})", result, isEmpty);
    return result;
  }
  
  /**
   * Returns a single result, represented by a PLIST of {@link IpcQueueElement}s and values.
   * 
   * @param queueId
   * @return
   * @throws CycConnectionException 
   */
  public CycList<?> getResult(final int queueId) throws CycConnectionException {
    final String cmd = makeSublStmt(DEQUEUE_ITERATIVE_TOE_ASSERT_VIA_KBQ_FUNC, queueId);
    return getCyc().converse().converseList(cmd);
  }
  
  /**
   * Returns up to n results, per {@code maxResultBatchSize}, with a timeout of {@code timeoutSecs}.
   * 
   * @param queueId
   * @param maxResultBatchSize
   * @param timeoutSecs
   * @return
   * @throws CycConnectionException 
   */
  public CycList<CycList> getResults(final int queueId,
                                     final int maxResultBatchSize, 
                                     final int timeoutSecs) throws CycConnectionException {
    final String cmd = makeSublStmt(
            DEQUEUE_N_WITH_TIMEOUT_ITERATIVE_TOE_ASSERT_VIA_KBQ_FUNC, 
            queueId, maxResultBatchSize, timeoutSecs);
    LOG.debug("Command: {}", cmd);
    final CycList<CycList> result = getCyc().converse().converseList(cmd);
    LOG.debug("      -> {}", result);
    return result;
  }
  
}
