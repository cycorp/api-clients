/*
 */
package com.cyc.query.client;

/*
 * #%L
 * File: TestUtils.java
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
import static com.cyc.baseclient.datatype.StringUtils.printlns;
import com.cyc.kb.Context;
import com.cyc.kb.KbIndividual;
import com.cyc.kb.KbIndividualFactory;
import com.cyc.kb.KbStatus;
import com.cyc.kb.Sentence;
import com.cyc.kb.Variable;
import com.cyc.kb.client.Constants;
import com.cyc.kb.client.KbObjectImpl;
import com.cyc.kb.client.KbObjectImplFactory;
import com.cyc.kb.client.SentenceImpl;
import com.cyc.kb.client.VariableImpl;
import com.cyc.kb.client.config.KbConfiguration;
import com.cyc.kb.exception.KbException;
import com.cyc.kb.exception.KbRuntimeException;
import com.cyc.query.Query;
import com.cyc.query.QueryAnswer;
import com.cyc.query.QueryAnswers;
import com.cyc.query.QueryFactory;
import com.cyc.query.exception.QueryConstructionException;
import com.cyc.query.exception.QueryException;
import com.cyc.session.CycSession;
import com.cyc.session.CycSessionManager;
import com.cyc.session.compatibility.CycSessionRequirement;
import com.cyc.session.compatibility.CycSessionRequirementList;
import com.cyc.session.compatibility.NotOpenCycRequirement;
import com.cyc.session.exception.SessionCommunicationException;
import com.cyc.session.exception.SessionConfigurationException;
import com.cyc.session.exception.SessionException;
import com.cyc.session.exception.SessionInitializationException;
import com.cyc.session.internal.TestEnvironmentProperties;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;


/**
 *
 * @author baxter
 */
public class TestUtils {

  //====|  Test fields  |=========================================================================//
  
  public static final Variable X;
  public static final Sentence X_ISA_EMU;
  public static final Context INFERENCE_PSC;
  public static Query currentQuery;
  
  static {
    try {
      KbConfiguration.getOptions().setShouldTranscriptOperations(false);
      X = new VariableImpl("?X");
      X_ISA_EMU = new SentenceImpl(Constants.isa(), X, QueryTestConstants.emu());
      INFERENCE_PSC = Constants.inferencePSCMt();
    } catch (KbRuntimeException | KbException e) {
      throw new RuntimeException("Failed to initialize test constants.", e);
    }
  }
    
  public static CycSession getSession() {
    try {
      return CycSessionManager.getCurrentSession();
    } catch (SessionConfigurationException
            | SessionCommunicationException
            | SessionInitializationException e) {
      throw new RuntimeException("Failed retrieve current CycSession.", e);
    }
  }
  
  public static CycAccess getCyc() {
    try {
      return CycAccessManager.getCurrentAccess();
    } catch (SessionConfigurationException
            | SessionCommunicationException
            | SessionInitializationException e) {
      throw new RuntimeException("Failed retrieve current CycAccess.", e);
    }
  }
  
  public static Sentence xIsaBird() throws KbException {
    return new SentenceImpl(Constants.isa(), X, QueryTestConstants.bird());
  }
  
  protected static Query constructXIsaBirdQuery() throws QueryConstructionException {
    try {
      return QueryFactory.getQuery(xIsaBird(), INFERENCE_PSC);
    } catch (KbException ex) {
      throw new QueryConstructionException(ex);
    }
  }

  protected static void closeTestQuery() {
    if (currentQuery != null) {
      currentQuery.close();
    }
  }
  
  /**
   * If an error occurs when initializing a field, we still attempt to initialize the other fields.
   * 
   * In this way, one test isn't blocked by an unrelated failure in another; e.g., a missing
   * constant for a QueryGraph which other tests don't use. - nwinant, 2017-04-07
   * @param nameOrId
   * @return 
   */
  public static KbIndividual attemptToLoadKbIndividual(String nameOrId) {
    try {
      return KbIndividualFactory.get(nameOrId);
    } catch (KbException ex) {
      ex.printStackTrace(System.err);
    }
    return null;
  }
  
  //====|  Test requirement utility methods  |====================================================//
  
  public static void assumeKBObject(String nameOrId, Class<? extends KbObjectImpl> clazz) {
    // TODO: this should be moved into a CycSessionRequirement.
    org.junit.Assume.assumeTrue(KbObjectImplFactory.getStatus(nameOrId, clazz).equals(KbStatus.EXISTS_AS_TYPE));
  }
  
  public static void assumeCycSessionRequirement(CycSessionRequirement requirement) {
    // TODO: move this into some central test library
    try {
      org.junit.Assume.assumeTrue(requirement.checkCompatibility(getSession()).isCompatible());
    } catch (SessionException ex) {
      ex.printStackTrace(System.err);
      throw new RuntimeException(ex);
    }
  }
  
  public static void assumeCycSessionRequirements(CycSessionRequirementList requirements) {
    // TODO: move this into some central test library
    try {
      org.junit.Assume.assumeTrue(requirements.checkCompatibility().isCompatible());
    } catch (SessionException ex) {
      ex.printStackTrace(System.err);
      throw new RuntimeException(ex);
    }
  }
  
  public static void assumeNotOpenCyc() {
    // TODO: move this into some central test library
    
    // To toggle #areOpenCycTestsForcedToRun, edit "cyc.test.forceOpenCycTestsToRun" in the pom.xml
    if (!TestEnvironmentProperties.get().areOpenCycTestsForcedToRun()) {
      assumeCycSessionRequirement(NotOpenCycRequirement.NOT_OPENCYC);
    }
  }
  
  //====|  Printing methods  |====================================================================//
  
  /**
   * Retrieves QueryAnswers from a Query, running the Query if necessary, and then pretty-prints 
   * those answers to a PrintStream.
   * 
   * @param query the Query from which to retrieve answers.
   * @param asTable should answers be formatting as a table?
   * @param out the PrintStream to which answers should be printed.
   * @return the QueryAnswers.
   * @throws SessionCommunicationException 
   */
  public static QueryAnswers<QueryAnswer> printQueryAnswersFromQuery(
          Query query, boolean asTable, PrintStream out)
          throws SessionCommunicationException {
    final QueryAnswers<QueryAnswer> answers = query.getAnswers();
    System.out.println("Number of answers: " + answers.size());
    if (asTable) {
      answers.printAnswersTable(out, true);
    } else {
      System.out.println("----------");
      for (QueryAnswer answer : answers) {
        System.out.println(answer.toPrettyBindingsStrings());
      }
      System.out.println("----------");
    }
    System.out.println("Number of answers: " + answers.size());
    return answers;
  }

  /**
   * Retrieves QueryAnswers from a Query, running the Query if necessary, and then pretty-prints 
   * those answers to stdout.
   * 
   * @param query the Query from which to retrieve answers.
   * @param asTable should answers be formatting as a table?
   * @return the QueryAnswers.
   * @throws SessionCommunicationException 
   */
  public static QueryAnswers<QueryAnswer> printQueryAnswersFromQuery(Query query, boolean asTable)
          throws SessionCommunicationException {
    return TestUtils.printQueryAnswersFromQuery(query, asTable, System.out);
  }
  
  public static Query printQueryDetails(Query q, PrintStream out) throws KbException, QueryException, SessionException {
    out.println("q: " + q.getQuerySentence());
    out.println("c: " + q.getContext());
    final Map infParms = new HashMap();
    for (String key : q.getInferenceParameters().keySet()) {
      infParms.put(key, q.getInferenceParameters().get(key));
    }
    out.println("params:");
    printlns(infParms, out);
    return q;
  }
  
  public static QueryAnswer printQueryAnswer(QueryAnswer answer, PrintStream out) {
    printlns(answer.getBindings(), out);
    out.println("--");
    return answer;
  }
    
}
