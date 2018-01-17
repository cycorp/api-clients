package com.cyc.baseclient.inference;

/*
 * #%L
 * File: KbQueryFactory.java
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
import com.cyc.base.cycobject.CycObject;
import com.cyc.base.cycobject.DenotationalTerm;
import com.cyc.base.cycobject.ElMt;
import com.cyc.base.cycobject.FormulaSentence;
import com.cyc.base.exception.CycApiException;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.base.inference.InferenceWorker;
import com.cyc.baseclient.CycObjectFactory;
import com.cyc.baseclient.connection.SublApiHelper;
import com.cyc.baseclient.cycobject.CycSymbolImpl;
import com.cyc.baseclient.cycobject.CycVariableImpl;
import com.cyc.baseclient.inference.params.DefaultInferenceParameters;
import com.cyc.query.parameters.InferenceParameters;
import java.util.Collections;
import java.util.Map;

/**
 *
 * KBQueryFactory provides factory methods for inference workers where the query information--the
 * sentence, the ElMt and the inference parameters-- are backed by a KBQ term in the CYC knowledge
 * base.
 *
 * @author rck
 * @since 2010-07-08
 * @version $Id: KbQueryFactory.java 176591 2018-01-09 17:27:27Z nwinant $
 */
public class KbQueryFactory {

  static private final KbQueryFactory INSTANCE = new KbQueryFactory();

  static public KbQueryFactory getInstance() {
    return INSTANCE;
  }
  //// SubL API entry points
  final static public CycSymbolImpl KBQ_SENTENCE = CycObjectFactory.makeCycSymbol("KBQ-SENTENCE");
  final static public CycSymbolImpl KBQ_ELMT = CycObjectFactory.makeCycSymbol("KBQ-MT");
  final static public CycSymbolImpl KBQ_PROPERTIES = CycObjectFactory.makeCycSymbol("KBQ-QUERY-PROPERTIES");
  
  final static private Map<CycVariableImpl, Object> NO_SUBSTITUTIONS = Collections.emptyMap();

  /**
   *
   * Create a new instance of InferenceWorker, either synchronous or asynchronous, that will run the
   * query in the ElMt and with the parameters specified by the CYC query in the Cyc KB and uniquely
   * denoted by the KBQ parameter.
   *
   * @param access the CycAccess object that specifies the CYC server that hosts the reified query
   * and that will perform the inference (i.e. to which the inference worker is bound)
   * @param kbq the Cyc term that specifies the query
   * @param timeoutMsecs the timeout parameter for the inference worker in milli-seconds
   * @param sync if <tt>true</tt>, returns an instance of DefaultInferenceWorkerSynch; otherwise an
   * instance of DefaultInferenceWorker
   * @return an instance of DefaultInferenceWorker, either synchronized or not, depending on the
   * value of <tt>sync</tt>
   * @throws CycConnectionException if the converse operation throws an IOException
   * @throws CycApiException if the converse operation throws a CycApiException
   */
  public InferenceWorker getInferenceWorker(CycAccess access, DenotationalTerm kbq,
          long timeoutMsecs, boolean sync) throws CycApiException, CycConnectionException {
    return getInferenceWorkerWithSubstitutions(access, kbq, NO_SUBSTITUTIONS, timeoutMsecs, sync);
  }

  /**
   *
   * Create a new instance of InferenceWorker, either synchronous or asynchronous, that will run the
   * query in the ElMt and with the parameters specified by the CYC query in the Cyc KB and uniquely
   * denoted by the KBQ parameter.
   *
   * @param access the CycAccess object that specifies the CYC server that hosts the reified query
   * and that will perform the inference (i.e. to which the inference worker is bound)
   * @param kbq the Cyc term that specifies the query
   * @param timeoutMsecs the timeout parameter for the inference worker in milli-seconds
   * @param sync if <tt>true</tt>, returns an instance of DefaultInferenceWorkerSynch; otherwise an
   * instance of DefaultInferenceWorker
   * @return an instance of DefaultInferenceWorker, either synchronized or not, depending on the
   * value of <tt>sync</tt>
   * @throws CycConnectionException if the converse operation throws an IOException
   * @throws CycApiException if the converse operation throws a CycApiException
   */
  static public InferenceWorker prepareKbQuery(CycAccess access, DenotationalTerm kbq, long timeoutMsecs, boolean sync)
          throws CycConnectionException, CycApiException {
    return prepareKbQueryTemplate(access, kbq, NO_SUBSTITUTIONS, timeoutMsecs, sync);
  }
  
  /**
   * Create a new instance of InferenceWorker, either synchronous or asynchronous, that will run the
   * query in the ElMt and with the parameters specified by the CYC query in the Cyc KB and uniquely
   * denoted by the KBQ parameter. After the query sentence has been loaded, apply the substitutions
   * spelled out in the substitution map, equating variables with bindings.
   *
   * @param access the CycAccess object that specifies the CYC server that hosts the reified query
   * and that will perform the inference (i.e. to which the inference worker is bound)
   * @param kbq the Cyc term that specifies the query
   * @param substitutions a mapping from CycVariableImpl to Objects; each mapping is converted into
   * an equalSymbols clause and conjoined to the query sentence
   * @param timeoutMsecs the timeout parameter for the inference worker in milli-seconds
   * @param sync if <tt>true</tt>, returns an instance of DefaultInferenceWorkerSynch; otherwise an
   * instance of DefaultInferenceWorker
   * @return an instance of DefaultInferenceWorker, either synchronized or not, depending on the
   * value of <tt>sync</tt>
   * @throws CycConnectionException if the converse operation throws an IOException
   * @throws CycApiException if the converse operation throws a CycApiException
   */
  public InferenceWorker getInferenceWorkerWithSubstitutions(CycAccess access,
          DenotationalTerm kbq, Map<CycVariableImpl, Object> substitutions, long timeoutMsecs,
          boolean sync) throws CycApiException, CycConnectionException {
    final FormulaSentence sentence = loadKbqSentence(access, kbq);
    final ElMt elmt = loadKbqElMt(access, kbq);
    final InferenceParameters properties = loadKbqProperties(access, kbq);
    if (substitutions != null) {
      sentence.applySubstitutionsDestructive(substitutions);
    }
    return (sync) ? new DefaultInferenceWorkerSynch(sentence, elmt, properties, access, timeoutMsecs)
            : new DefaultInferenceWorker(sentence, elmt, properties, access, timeoutMsecs);
  }

  /**
   * Create a new instance of InferenceWorker, either synchronous or asynchronous, that will run the
   * query in the ElMt and with the parameters specified by the CYC query in the Cyc KB and uniquely
   * denoted by the KBQ parameter. After the query sentence has been loaded, apply the substitutions
   * spelled out in the substitution map, equating variables with bindings.
   *
   * @param access the CycAccess object that specifies the CYC server that hosts the reified query
   * and that will perform the inference (i.e. to which the inference worker is bound)
   * @param kbq the Cyc term that specifies the query
   * @param substitutions a mapping from CycVariableImpl to Objects; each mapping is converted into
   * an equalSymbols clause and conjoined to the query sentence
   * @param timeoutMsecs the timeout parameter for the inference worker in milli-seconds
   * @param sync if <tt>true</tt>, returns an instance of DefaultInferenceWorkerSynch; otherwise an
   * instance of DefaultInferenceWorker
   * @return an instance of DefaultInferenceWorker, either synchronized or not, depending on the
   * value of <tt>sync</tt>
   * @throws CycConnectionException if the converse operation throws an IOException
   * @throws CycApiException if the converse operation throws a CycApiException
   */
  static public InferenceWorker prepareKbQueryTemplate(CycAccess access, DenotationalTerm kbq,
          Map<CycVariableImpl, Object> substitutions, long timeoutMsecs, boolean sync)
          throws CycConnectionException, CycApiException {
    return getInstance().getInferenceWorkerWithSubstitutions(access, kbq, substitutions, timeoutMsecs, sync);
  }

  /**
   * Similar to <code>getInferenceWorkerWithSubstitutions</code>, but performs a tree substitution
   * on the query sentence.
   *
   * @see #getInferenceWorkerWithSubstitutions(com.cyc.base.CycAccess,
   * com.cyc.base.cycobject.DenotationalTerm, java.util.Map, long, boolean)
   *
   * @param access the CycAccess object that specifies the CYC server that hosts the reified query
   * and that will perform the inference (i.e. to which the inference worker is bound)
   * @param kbq the Cyc term that specifies the query
   * @param substitutions a mapping from CycObject to Objects
   * @param timeoutMsecs the timeout parameter for the inference worker in milli-seconds
   * @param sync if <tt>true</tt>, returns an instance of DefaultInferenceWorkerSynch; otherwise an
   * instance of DefaultInferenceWorker
   * @return an instance of DefaultInferenceWorker, either synchronized or not, depending on the
   * value of <tt>sync</tt>
   * @throws CycApiException if the converse operation throws a CycApiException
   * @throws com.cyc.base.exception.CycConnectionException
   */
  public InferenceWorker getInferenceWorkerWithTreeSubstitutions(CycAccess access,
          DenotationalTerm kbq, Map<CycObject, Object> substitutions, long timeoutMsecs,
          boolean sync) throws CycApiException, CycConnectionException {
    final FormulaSentence sentence = loadKbqSentence(access, kbq);
    final ElMt elmt = loadKbqElMt(access, kbq);
    final InferenceParameters properties = loadKbqProperties(access, kbq);
    FormulaSentence subsSentence = sentence.treeSubstitute(access, substitutions);
    return (sync) ? new DefaultInferenceWorkerSynch(subsSentence, elmt, properties, access, timeoutMsecs)
            : new DefaultInferenceWorker(subsSentence, elmt, properties, access, timeoutMsecs);
  }

  /**
   * Similar to <code>prepareKbQueryTemplate</code>, but performs a tree substitution on the query
   * sentence.
   *
   * @see #prepareKbQueryTemplate(com.cyc.base.CycAccess, com.cyc.base.cycobject.DenotationalTerm, java.util.Map, long, boolean)
   * @param access the CycAccess object that specifies the CYC server that hosts the reified query
   * and that will perform the inference (i.e. to which the inference worker is bound)
   * @param kbq the Cyc term that specifies the query
   * @param substitutions a mapping from CycObject to Objects
   * @param timeoutMsecs the timeout parameter for the inference worker in milli-seconds
   * @param sync if <tt>true</tt>, returns an instance of DefaultInferenceWorkerSynch; otherwise an
   * instance of DefaultInferenceWorker
   * @return an instance of DefaultInferenceWorker, either synchronized or not, depending on the
   * value of <tt>sync</tt>
   * @throws CycApiException if the converse operation throws a CycApiException
   * @throws com.cyc.base.exception.CycConnectionException
   */
  static public InferenceWorker prepareKbQueryTreeTemplate(CycAccess access, DenotationalTerm kbq,
          Map<CycObject, Object> substitutions, long timeoutMsecs, boolean sync)
          throws CycConnectionException, CycApiException {
    return getInstance().getInferenceWorkerWithTreeSubstitutions(access, kbq, substitutions, timeoutMsecs, sync);
  }

  protected FormulaSentence loadKbqSentence(CycAccess access, DenotationalTerm kbq)
          throws CycApiException, CycConnectionException {
    try {
      final String command = SublApiHelper.makeSublStmt(KBQ_SENTENCE, kbq);
      return access.converse().converseSentence(command);
    } catch (CycApiException xcpt) {
      throw new CycApiException("Could not load query sentence for KBQ " + kbq.cyclify(), xcpt);
    }
  }

  protected ElMt loadKbqElMt(CycAccess access, DenotationalTerm kbq)
          throws CycApiException, CycConnectionException {
    try {
      final String command = SublApiHelper.makeSublStmt(KBQ_ELMT, kbq);
      return access.getObjectTool().makeElMt(access.converse().converseCycObject(command));
    } catch (CycApiException xcpt) {
      throw new CycApiException("Could not load query MT for KBQ " + kbq.cyclify(), xcpt);
    }
  }

  protected InferenceParameters loadKbqProperties(CycAccess access, DenotationalTerm kbq)
          throws CycApiException, CycConnectionException {
    try {
      InferenceParameters properties = new DefaultInferenceParameters(access);
      final String command = SublApiHelper.makeSublStmt(KBQ_PROPERTIES, kbq);
      properties.updateFromPlist(access.converse().converseList(command));
      return properties;
    } catch (CycApiException xcpt) {
      throw new CycApiException("Could not load query inference properties for KBQ " + kbq.cyclify(), xcpt);
    }
  }
}
