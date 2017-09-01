/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyc.xml.query;

/*
 * #%L
 * File: ProofViewTestConstants.java
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

import com.cyc.base.exception.CycConnectionException;
import com.cyc.kb.Context;
import com.cyc.kb.client.Constants;
import com.cyc.kb.exception.KbException;
import com.cyc.query.QueryAnswer;
import com.cyc.query.client.QueryImpl;
import com.cyc.query.client.QueryTestConstants;
import com.cyc.query.exception.QueryConstructionException;
import com.cyc.session.exception.SessionException;

/**
 *
 * @author baxter
 */
public class ProofViewTestConstants {

  // Fields
  
  public static final Context DOMAIN_CONTEXT = Constants.baseKbMt();
  public static final Context LANGUAGE_CONTEXT = QueryTestConstants.getInstance().englishParaphraseMt;
  public static QueryAnswer currentAnswer = null;
  
  public static void setup()
          throws CycConnectionException, QueryConstructionException, KbException, SessionException {
    final QueryImpl query = new QueryImpl(
            QueryTestConstants.getInstance().genlsEmuBird, 
            Constants.inferencePSCMt());
    query.retainInference();
    currentAnswer = query.getAnswer(0);
    System.out.println("Performed inference. Got answer: " + currentAnswer);
  }
  
  public static void teardown() {
    try {
      currentAnswer.getId().getInferenceIdentifier().close();
    } catch (Exception e) {
    }
  }
  
}
