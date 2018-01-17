package com.cyc.baseclient.kbtool;

/*
 * #%L
 * File: RkfToolImpl.java
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
import com.cyc.base.cycobject.CycList;
import com.cyc.base.cycobject.Fort;
import com.cyc.base.exception.CycApiException;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.baseclient.AbstractKbTool;
import static com.cyc.baseclient.connection.SublApiHelper.makeSublStmt;
import com.cyc.session.compatibility.CycSessionRequirementList;
import com.cyc.session.compatibility.NotOpenCycRequirement;
import com.cyc.session.exception.OpenCycUnsupportedFeatureException;

/**
 * Tools from the RKF project.
 * 
 * <p>Deprecated: Will either by moved to the KnowledgeManagement API, or deleted.
 * 
 * @author nwinant
 */
@Deprecated
public class RkfToolImpl extends AbstractKbTool {
  
  public static final CycSessionRequirementList<OpenCycUnsupportedFeatureException> RKF_REQUIREMENTS
          = CycSessionRequirementList.fromList(
                  NotOpenCycRequirement.NOT_OPENCYC
          );
  
  
  public RkfToolImpl(CycAccess client) {
    super(client);
  }
  
  
  // Public
  
  /**
   * Returns a list of parsing expressions, each consisting of a parsing span expression, and a
   * list of parsed terms.
   * <pre>
   * (RKF-PHRASE-READER "penguins" #$RKFEnglishLexicalMicrotheoryPSC #$InferencePSC)
   * ==>
   * (((0) (#$Penguin #$PittsburghPenguins)))
   * </pre>
   *
   * @param text the phrase to be parsed
   * @param parsingMt the microtheory in which lexical info is asked
   * @param domainMt the microtherory in which the info about candidate terms is asked
   *
   * @return a parsing expression consisting of a parsing span expression, and a list of parsed
   * terms
   *
   * @throws CycConnectionException if a data communication error occurs
   * @throws CycApiException if the api request results in a cyc server error
   * @throws com.cyc.session.exception.OpenCycUnsupportedFeatureException
   */
  public CycList rkfPhraseReader(String text, String parsingMt, String domainMt)
          throws CycConnectionException, CycApiException, OpenCycUnsupportedFeatureException {
    return rkfPhraseReader(text,
            getKnownConstantByName_inner(parsingMt),
            getKnownConstantByName_inner(domainMt));
  }

  /**
   * Returns a list of parsing expressions, each consisting of a parsing span expression, and a
   * list of parsed terms.
   * <pre>
   * (RKF-PHRASE-READER "penguins" #$RKFEnglishLexicalMicrotheoryPSC #$InferencePSC)
   * ==>
   * (((0) (#$Penguin #$PittsburghPenguins)))
   * </pre>
   *
   * @param text the phrase to be parsed
   * @param parsingMt the microtheory in which lexical info is asked
   * @param domainMt the microtherory in which the info about candidate terms is asked
   *
   * @return a parsing expression consisting of a parsing span expression, and a list of parsed
   * terms
   *
   * @throws CycConnectionException if a data communication error occurs
   * @throws CycApiException if the api request results in a cyc server error
   * @throws com.cyc.session.exception.OpenCycUnsupportedFeatureException
   */
  public CycList rkfPhraseReader(String text, Fort parsingMt, Fort domainMt) 
          throws CycConnectionException, CycApiException, OpenCycUnsupportedFeatureException {
    RKF_REQUIREMENTS.throwRuntimeExceptionIfIncompatible();
    String command = makeSublStmt("rkf-phrase-reader", text, parsingMt, domainMt);
    return getConverse().converseList(command);
  }
}
