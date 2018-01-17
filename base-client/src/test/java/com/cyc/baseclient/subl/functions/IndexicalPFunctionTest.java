package com.cyc.baseclient.subl.functions;

/*
 * #%L
 * File: IndexicalPFunctionTest.java
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

import com.cyc.base.cycobject.CycConstant;
import com.cyc.base.cycobject.CycObject;
import com.cyc.base.cycobject.FormulaSentence;
import com.cyc.base.exception.CycApiException;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.baseclient.cycobject.FormulaSentenceImpl;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

import static com.cyc.baseclient.subl.functions.SublFunctions.INDEXICAL_P;
import static org.junit.Assert.*;

/**
 * 
 * @author nwinant
 */
public class IndexicalPFunctionTest extends AbstractSublFunctionTest {
  
  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    indexicalSentences = Arrays.asList(FormulaSentenceImpl
            .makeFormulaSentence(access, "(TheNamedFn SetOrCollection \"indexical 1\")"),
            FormulaSentenceImpl
            .makeFormulaSentence(access, "(TheNamedFn SetOrCollection \"indexical 2\")"),
            FormulaSentenceImpl
            .makeFormulaSentence(access, "(TheNamedFn SetOrCollection \"indexical 3\")"));
    indexicalConstants = Arrays.asList(
            access.getLookupTool().getConstantByName("#$Now-Indexical"),
            access.getLookupTool().getConstantByName("#$Now"),
            access.getLookupTool().getConstantByName("#$TheYear-Indexical"),
            access.getLookupTool().getConstantByName("#$Yesterday-Indexical"),
            access.getLookupTool().getConstantByName("#$Tomorrow-Indexical"),
            access.getLookupTool().getConstantByName("#$Today-Indexical"),
            access.getLookupTool().getConstantByName("#$QueryMt"),
            access.getLookupTool().getConstantByName("#$ParaphraseDomainMt"),
            access.getLookupTool().getConstantByName("#$ParaphraseLanguageMt"),
            access.getLookupTool().getConstantByName("#$ParaphraseDomainMt"),
            access.getLookupTool().getConstantByName("#$ParaphraseLanguageMt"),
            access.getLookupTool().getConstantByName("#$TheCycProcessOwner"),
            access.getLookupTool().getConstantByName("#$TheUser"),
            access.getLookupTool().getConstantByName("#$TheCurrentHostName"),
            access.getLookupTool().getConstantByName("#$TheCurrentSystemNumber"),
            access.getLookupTool().getConstantByName("#$TheCurrentKBNumber"),
            access.getLookupTool().getConstantByName("#$SecondsSince1970-Indexical"),
            access.getLookupTool().getConstantByName("#$ThisProblemStore"),
            access.getLookupTool().getConstantByName("#$ThisInference"));
    nonIndexicals = Arrays.asList(
            access.getLookupTool().getConstantByName("#$Dog"),
            access.getLookupTool().getConstantByName("#$Cat"));
  }
  
  
  // Fields
  
  public List<FormulaSentence> indexicalSentences;
  public List<CycConstant>     indexicalConstants;
  public List<CycConstant>     nonIndexicals;
  
  
  // Tests

  @Test
  public void testEval_indexicalSentences() throws CycApiException, CycConnectionException {
    for (CycObject indexical : indexicalSentences) {
      System.out.println("Indexical    : " + indexical);
      assertTrue(INDEXICAL_P.eval(access, indexical));
    }
  }
  
  @Test
  public void testEval_indexicalConstants() throws CycApiException, CycConnectionException {
    for (CycObject indexical : indexicalConstants) {
      System.out.println("Indexical    : " + indexical);
      assertTrue(INDEXICAL_P.eval(access, indexical));
    }
  }
  
  @Test
  public void testEval_nonIndexicals() throws CycApiException, CycConnectionException {
    for (CycObject indexical : nonIndexicals) {
      System.out.println("Non-indexical: " + indexical);
      assertFalse(INDEXICAL_P.eval(access, indexical));
    }
  }
  
}
