package com.cyc.query.client;

/*
 * #%L
 * File: InferenceAnswerBackedQueryAnswer.java
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
import com.cyc.base.cycobject.CycVariable;
import com.cyc.base.cycobject.InformationSource;
import com.cyc.base.exception.CycApiException;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.base.exception.CycTimeOutException;
import com.cyc.base.inference.InferenceAnswer;
import com.cyc.baseclient.connection.DefaultSublWorkerSynch;
import com.cyc.baseclient.connection.SublWorkerSynch;
import com.cyc.baseclient.cycobject.InformationSourceImpl;
import com.cyc.baseclient.exception.CycTaskInterruptedException;
import com.cyc.baseclient.inference.CycBackedInferenceAnswer;
import com.cyc.baseclient.inference.DefaultProofIdentifier;
import com.cyc.kb.KbTerm;
import com.cyc.kb.Variable;
import com.cyc.kb.client.KbObjectImpl;
import com.cyc.kb.client.KbObjectImplFactory;
import com.cyc.kb.client.KbTermImpl;
import com.cyc.kb.client.VariableImpl;
import com.cyc.kb.exception.CreateException;
import com.cyc.kb.exception.KbTypeException;
import com.cyc.nl.Paraphrase;
import com.cyc.nl.ParaphraseImpl;
import com.cyc.nl.Paraphraser;
import com.cyc.query.InferenceAnswerIdentifier;
import com.cyc.query.InferenceIdentifier;
import com.cyc.query.ParaphrasedQueryAnswer;
import com.cyc.query.ProofIdentifier;
import com.cyc.query.exception.QueryRuntimeException;
import com.cyc.session.exception.SessionCommunicationException;
import com.cyc.session.exception.SessionConfigurationException;
import com.cyc.session.exception.SessionInitializationException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author baxter
 */
public class InferenceAnswerBackedQueryAnswer 
        extends AbstractParaphrasedQueryAnswer
        implements ParaphrasedQueryAnswer {
  
  //====|    Fields    |==========================================================================//

  private final InferenceAnswer answerCyc;
  
  //====|    Construction    |====================================================================//
  
  InferenceAnswerBackedQueryAnswer(InferenceAnswerIdentifier id) {
    this(new CycBackedInferenceAnswer(id));
  }

  InferenceAnswerBackedQueryAnswer(InferenceAnswerIdentifier id, Paraphraser paraphraser) {
    this(new CycBackedInferenceAnswer(id), paraphraser);
  }

  InferenceAnswerBackedQueryAnswer(InferenceAnswer answerCyc) {
    this(answerCyc, null);
  }

  InferenceAnswerBackedQueryAnswer(InferenceAnswer answerCyc, Paraphraser paraphraser) {
    super((answerCyc != null) ? answerCyc.getId() : null, paraphraser);
    if (answerCyc == null) {
      throw new IllegalArgumentException();
    }
    this.answerCyc = answerCyc;
  }
  
  //====|    Public methods    |==================================================================//
  
  /*
  @Override
  public InferenceAnswerIdentifier getId() {
    return answerCyc.getId();
    //if this is null, return something based on the inference id, if we can find the QueryImpl
  }
  */
  
  @Deprecated
  public InferenceAnswer getAnswerCyc() {
    return answerCyc;
  }
  
  @Override
  public <T> T getBinding(Variable var) {
    try {
      return KbObjectImpl.<T>checkAndCastObject(answerCyc.getBinding((CycVariable) var.getCore()));
    } catch (CreateException ex) {
      throw new IllegalStateException(ex);
    } catch (CycConnectionException ex) {
      throw new RuntimeException(ex);
    }
  }

  @Override
  public boolean hasBinding(Variable var) {
    try {
      return answerCyc.getBindings().containsKey((CycVariable) var.getCore()) 
             && answerCyc.getBindings().get((CycVariable) var.getCore()) != null;
    } catch (CycConnectionException ex) {
      throw new RuntimeException(ex);
    }
  }
  
  /**
   * Gets the id of an arbitrary proof used to support this InferenceAnswer.
   *
   * @return
   */
  public ProofIdentifier getProofIdentifier() throws QueryRuntimeException {
    try {
      CycAccess cyc;
      cyc = CycAccessManager.getCurrentAccess();

      int timeoutMsecs = 1000;
      InferenceIdentifier inferenceIdentifier = getId().getInferenceIdentifier();
      SublWorkerSynch subLWorker = new DefaultSublWorkerSynch("(proof-suid (first (inference-answer-proofs (find-inference-answer-by-ids  "
              + inferenceIdentifier.getProblemStoreId()
              + " "
              + inferenceIdentifier.getInferenceId()
              + " "
              + getId().getAnswerId()
              + "))))",
              cyc, timeoutMsecs);
      Integer id = (Integer) subLWorker.getWork();
      return new DefaultProofIdentifier(this.getId().getInferenceIdentifier().getProblemStoreId(), id);
    } catch (CycConnectionException | CycTimeOutException | CycApiException | CycTaskInterruptedException | SessionConfigurationException | SessionCommunicationException | SessionInitializationException ex) {
      throw QueryRuntimeException.fromThrowable(ex);
    }
  }

  public Set<ProofIdentifier> getProofIdentifiers() throws SessionCommunicationException, SessionConfigurationException, SessionInitializationException, CycApiException, CycTimeOutException, CycConnectionException {
    Set<ProofIdentifier> ids = new HashSet<>();
    CycAccess cyc = CycAccessManager.getCurrentAccess();
    int timeoutMsecs = 1000;
    InferenceIdentifier inferenceIdentifier = getId().getInferenceIdentifier();
    SublWorkerSynch subLWorker = new DefaultSublWorkerSynch("(find-proof-ids-for-inference-answer "
            + inferenceIdentifier.getProblemStoreId()
            + " "
            + inferenceIdentifier.getInferenceId()
            + " "
            + getId().getAnswerId()
            + ")",
            cyc, timeoutMsecs);
    List<Integer> result = (List<Integer>) subLWorker.getWork();
    for (Integer proofId : result) {
      ids.add(new DefaultProofIdentifier(this.getId().getInferenceIdentifier().getProblemStoreId(), proofId));
    }
    return ids;
  }

  @Override
  public Set<Variable> getVariables() {
    try {
      final Set<Variable> variables = new HashSet();
      for (final CycVariable cycVariable : answerCyc.getBindings().keySet()) {
        final Variable variable;
        try {
          variable = new VariableImpl(cycVariable);
          variables.add(variable);
        } catch (KbTypeException ex) {
          throw new RuntimeException("Unable to convert " + cycVariable + " into a Variable.");
        }
      }
      return Collections.unmodifiableSet(variables);
    } catch (CycConnectionException ex) {
      throw new IllegalStateException(ex);
    }
  }

  @Override
  public Map<Variable, Object> getBindings() {
    try {
      final Map<Variable, Object> bindings = new HashMap();
      for (final CycVariable cycVariable : answerCyc.getBindings().keySet()) {
        final Variable variable;
        try {
          variable = new VariableImpl(cycVariable);
          if (hasBinding(variable)) {
            bindings.put(variable, getBinding(variable));
          }
        } catch (KbTypeException ex) {
          throw new RuntimeException("Unable to convert " + cycVariable + " into a Variable.");
        }
      }
      return Collections.unmodifiableMap(bindings);
    } catch (CycConnectionException ex) {
      throw new IllegalStateException(ex);
    }

  }

  @Override
  public Map<Variable, Paraphrase> getParaphrasedBindings() {
    try {
      final Map<Variable, Paraphrase> bindings = new HashMap();
      for (final CycVariable cycVariable : answerCyc.getBindings().keySet()) {
        final Variable variable;
        try {
          variable = new VariableImpl(cycVariable);
          Paraphrase paraphrase = (paraphraser == null) ? new ParaphraseImpl(null, getBinding(variable)) : paraphraser.paraphrase(getBinding(variable));
          bindings.put(variable, paraphrase);
        } catch (KbTypeException ex) {
          throw new RuntimeException("Unable to produce a Paraphrase for " + cycVariable + ".");
        }
      }
      return Collections.unmodifiableMap(bindings);
    } catch (CycConnectionException ex) {
      throw new IllegalStateException(ex);
    }
  }

  @Override
  public String toString() {
    return answerCyc.toString();
  }

  @Override
  public Set<KbTerm> getSources() {
    Set<KbTerm> sources = new HashSet<>();
    if (answerCyc.getId() == null) {
      throw new UnsupportedOperationException("Sources can not be retrieved from an inference that has been destroyed.  "
              + "The inference must be retained until after getSources() has been called.");
    }
    try {
      for (InformationSource source : answerCyc.getSources(InformationSourceImpl.CycCitationGenerator.DEFAULT)) {
        try {
          sources.add(KbObjectImplFactory.get(source.getCycL(), KbTermImpl.class));
        } catch (KbTypeException | CreateException ex) {
          throw new QueryRuntimeException("Unable to turn source " + source.getCycL() + " into a KB Object");
        }
      }
    } catch (CycConnectionException ex) {
      throw new QueryRuntimeException("Unable to get source for inference answer " + answerCyc.toString());
    }
    return sources;
  }
  
}
