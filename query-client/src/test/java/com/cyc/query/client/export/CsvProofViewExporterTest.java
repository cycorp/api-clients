package com.cyc.query.client.export;

/*
 * #%L
 * File: CsvProofViewExporterTest.java
 * Project: Query Client
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

import com.cyc.kb.Context;
import com.cyc.kb.KbIndividual;
import com.cyc.kb.Variable;
import com.cyc.km.query.export.CsvProofViewExporter;
import com.cyc.nl.NonParaphrasingParaphraser;
import com.cyc.nl.Paraphraser;
import com.cyc.query.ParaphrasedQueryAnswer;
import com.cyc.query.ProofView;
import com.cyc.query.ProofViewSpecification;
import com.cyc.query.Query;
import java.io.PrintStream;
import java.util.Date;
import java.util.List;
import org.junit.Test;

import static com.cyc.Cyc.Constants.BASE_KB;
import static com.cyc.query.client.TestUtils.printQueryAnswer;
import static com.cyc.query.client.TestUtils.printQueryDetails;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

/**
 * 
 * @author nwinant
 */
public class CsvProofViewExporterTest {
  
  private void processProofView(ProofView proofView) throws Exception {
    CsvProofViewExporter.toCsv(proofView, System.out);
  }
  
  public Query getQueryFromKBQ() throws Exception {
    assumeTrue("TemplateOe tests are currently disabled.", false); // FIXME: revise tests & re-enable - nwinant, 2017-08-09
    final KbIndividual qFn = null;
    return Query.get(qFn);
  }
  
  public void run(final PrintStream out) throws Exception {
    System.out.println("Starting...");
    
    final Query q = getQueryFromKBQ();
    q.getInferenceParameters()
            .setBrowsable(true)
            .setContinuable(true);
    printQueryDetails(q, out);
    
    final Paraphraser paraphraser = new NonParaphrasingParaphraser();
    
    System.out.println("Query starting...");
    final List<ParaphrasedQueryAnswer> answers = q.getAnswers(paraphraser);
    System.out.println("QueryAnswers found: " + answers.size());
    
    final ProofViewSpecification proofViewSpec = ProofViewSpecification.get()
            .setDomainContext(BASE_KB)
            .setIncludeAssertionBookkeeping(true)
            .setIncludeAssertionCyclists(true)
            .setIncludeDetails(true)
            .setIncludeLinear(true)
            .setIncludeSummary(true)
            .setLanguageContext(Context.get("EnglishParaphraseMt")) // TODO: add this field to ContextFactory - nwinant, 2017-03-10
            ;
    
    for (ParaphrasedQueryAnswer answer : answers) {
      boolean selected = false;
      for (Variable var : answer.getVariables()) {
        final String value = answer.getBinding(var).toString();
        System.out.println("VALUE: " + value);
        if (value.contains("value")) { // TODO: update this value
          selected = true;
        }
      }
      if (selected) {
        System.out.println(">>>>>  -----------------------------------------------------------------------------------------------------------------");
        System.out.println();
        System.out.println();
        printQueryAnswer(answer, out);
        System.out.println();
        
        System.out.println("Generating proofview (via Cyc#getProofView()...");
        /*
        final ProofViewGeneratorImpl generator = new ProofViewGeneratorImpl(answer, proofViewSpec);
        generator.generate();
        */
        System.out.println();
        System.out.println();
        
        //processProofView(generator.getExplanation(), out);
        processProofView(ProofView.getProofView(answer, proofViewSpec));
        
        System.out.println();
        System.out.println();
        System.out.println("<<<<<  -----------------------------------------------------------------------------------------------------------------");
      }
    }
  }

  public void run() throws Exception {
    System.out.println("Beginning on " + new Date());
    run(System.out);
    System.out.println("... Ending on " + new Date());
  }
  
  @Test
  public void testRun() throws Exception {
    run();
    assertTrue("Passed!", true); // TODO: add some kind of meaningful test - nwinant, 2017-03-22
  }
  
  
  // Main
  
  public static void main(String[] args) {
    final String clsName = CsvProofViewExporterTest.class.getSimpleName();
    try {
      System.out.println(clsName + " Starting...");
      CsvProofViewExporterTest me = new CsvProofViewExporterTest();
      me.run();
    } catch (Throwable t) {
      t.printStackTrace(System.err);
      System.err.println("... DEAD!");
      System.exit(1);
    } finally {
      System.out.println("... " + clsName + " Done!");
      System.exit(0);
    }
  }
  
}
