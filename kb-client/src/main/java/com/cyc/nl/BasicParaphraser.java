package com.cyc.nl;


/*
 * #%L
 * File: BasicParaphraser.java
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

import com.cyc.base.CycAccess;
import com.cyc.base.CycAccessManager;
import com.cyc.base.cycobject.CycObject;
import com.cyc.base.cycobject.CycVariable;
import com.cyc.base.cycobject.DenotationalTerm;
import com.cyc.base.cycobject.ElMt;
import com.cyc.base.exception.CycApiException;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.base.exception.CycTimeOutException;
import com.cyc.base.inference.InferenceAnswer;
import com.cyc.baseclient.CommonConstants;
import com.cyc.baseclient.cycobject.CycVariableImpl;
import com.cyc.baseclient.inference.DefaultInferenceSuspendReason;
import com.cyc.baseclient.inference.DefaultInferenceWorkerSynch;
import com.cyc.baseclient.inference.DefaultResultSet;
import com.cyc.baseclient.inference.ResultSetInferenceAnswer;
import com.cyc.baseclient.inference.params.DefaultInferenceParameters;
import com.cyc.kb.Context;
import com.cyc.kb.KbObject;
import com.cyc.kb.KbTerm;
import com.cyc.query.parameters.InferenceParameters;
import com.cyc.session.exception.SessionCommunicationException;
import com.cyc.session.exception.SessionConfigurationException;
import com.cyc.session.exception.SessionInitializationException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

  /**
   *
 * @author daves
   */
class BasicParaphraser<E> implements Paraphraser {

  private static final Logger LOGGER = LoggerFactory.getLogger(BasicParaphraser.class);

  @Override
  public Paraphrase paraphrase(Object object) {
    try {
      LOGGER.debug("Natural Language generation is limited; NL API is not on the classpath.");
      final CycVariable x = new CycVariableImpl("X");
      final CycObject core;
      if (KbTerm.class.isAssignableFrom(object.getClass())) {
        core = (CycObject) ((KbObject) object).getCore();
      } else if (DenotationalTerm.class.isAssignableFrom(object.getClass())) {
        core = (CycObject) object;
      } else if (object instanceof String) {
        return new ParaphraseImpl((String)object, object);
      } else {
        throw new UnsupportedOperationException("BasicParaphrasers are not capable of paraphrasing " + object.getClass() + " objects.  For better paraphrase," + " be sure an NL API implementation is on your classpath.");
    }
      String paraphrase;
      if (CycAccessManager.getCurrentAccess().isOpenCyc()) {
        InferenceAnswer a = this.getFirstInferenceAnswer("(#$prettyString-Canonical " + core.cyclify() + " ?X)", CommonConstants.INFERENCE_PSC);
        paraphrase = (String) a.getBinding(x);
      } else {
        try {
        InferenceAnswer a = this.getFirstInferenceAnswer("(#$termPhrases " + core.cyclify() + " #$CharacterString ?X)", CommonConstants.INFERENCE_PSC);
        paraphrase = (String) a.getBinding(x);
        } catch (IllegalArgumentException ex) {
          //there was no paraphrase, so just parrot back the CycL.
          paraphrase = core.toString();
        }      
    }
      return new ParaphraseImpl(paraphrase, object);
    } catch (SessionConfigurationException | SessionCommunicationException | SessionInitializationException | CycConnectionException ex) {
      throw new RuntimeException("Exception while trying to paraphrase " + object + ".", ex);
    }
    }

  protected InferenceAnswer getFirstInferenceAnswer(final String querySentence, final ElMt mt) throws CycConnectionException, CycTimeOutException, CycApiException, SessionConfigurationException, SessionCommunicationException, SessionInitializationException {
    CycAccess cyc = CycAccessManager.getCurrentAccess();
    InferenceParameters params = new DefaultInferenceParameters(cyc);
    params.setMaxAnswerCount(1);
    DefaultInferenceWorkerSynch worker = new DefaultInferenceWorkerSynch(cyc.getObjectTool().makeCycSentence(querySentence), mt, params, cyc, 10000);
    ((DefaultInferenceWorkerSynch) worker).performSynchronousInference();
    DefaultInferenceSuspendReason suspendReason = worker.getSuspendReason();
    final InferenceAnswer inferenceAnswer = new ResultSetInferenceAnswer(new DefaultResultSet(worker.getAnswers(), worker), 0);
    return inferenceAnswer;
  }

  @Override
  public NlGenerationParams getParams() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void setParams(NlGenerationParams params) {
    if (params.getHideExplicitUniversals() != null ||
            params.getQuantifyVars() != null ||
            params.getUseBulletsInHtmlMode() != null ||
            params.getDomainContext() != null ||
            params.getForce() != null ||
            params.getLanguageContext() != null ||
            params.getMaxTime() != null ||
            params.getMode() != null ||
            (params.getNlPreds() != null && !params.getNlPreds().isEmpty())) {
          throw new UnsupportedOperationException("BasicParaphraser doesn't support any non-empty parameter values in an NlGenerationParams.");
    }  
  }

  @Override
  public void setForce(NlForce nlForce) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void setBlanksForVars(boolean b) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
  


  @Override
  public Context getLanguageContext() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public Context getDomainContext() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public NlForce getForce() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public GenerationMode getMode() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public List getNlPreds() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void setDomainContext(Context ctx) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void setLanguageContext(Context ctx) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
  
  @Override
  public List paraphraseWithDisambiguation(List objects) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void setNlPreds(List preds) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

}
