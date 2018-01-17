package com.cyc.query.client;

/*
 * #%L
 * File: QueryAnswersImplTest.java
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
import com.cyc.kb.Variable;
import com.cyc.query.Query;
import com.cyc.query.QueryAnswer;
import com.cyc.query.QueryAnswers;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class QueryAnswersImplTest {
  
  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }
  
  @Before
  public void setUp() {
    currentQuery = null;
  }
  
  @After
  public void tearDown() throws Exception {
    if (currentQuery != null) {
      currentQuery.close();
    }
    currentQuery = null;
  }
  
  
  // Fields
  
  private Query currentQuery = null;
  
  
  // Tests
  
  @Test
  public void testToAnswersTableStrings_WithBorder() throws Exception {
    currentQuery = Query.get(
            "(and"
            + " (isa ?ANIMAL ?TYPE)"
            + " (or (equals ?TYPE Cat) (equals ?TYPE Dog)))");
    final QueryAnswers<QueryAnswer> answers = currentQuery.getAnswers();
    final List<String> answerStrings = answers.toAnswersTableStrings(true);
    for (String str : answerStrings) {
      System.out.println(str);
    }
    assertTrue(!answers.isEmpty());
    assertTrue(answerStrings.size() > 1);
    assertEquals(answers.size() + 4, answerStrings.size());
    assertEquals("---------------------", answerStrings.get(0).substring(0, 21));
    assertEquals("|  Answer  |  ?ANIMAL", answerStrings.get(1).substring(0, 21));
    assertEquals("|  ------  |  -------", answerStrings.get(2).substring(0, 21));
    assertEquals("|       0  |  ",        answerStrings.get(3).substring(0, 14));
    assertEquals("|       1  |  ",        answerStrings.get(4).substring(0, 14));
    assertEquals("---------------------", answerStrings.get(answerStrings.size() - 1).substring(0, 21));
    final QueryAnswer answer1 = answers.get(0);
    final Object binding = answer1.getBinding(Variable.get("?ANIMAL"));
    assertEquals("|       0  |  " + binding, answerStrings.get(3).substring(0, 14) + binding);
  }
  
  @Test
  public void testToAnswersTableStrings_WithoutBorder() throws Exception {
    currentQuery = Query.get(
            "(and"
            + " (isa ?ANIMAL ?TYPE)"
            + " (or (equals ?TYPE Cat) (equals ?TYPE Dog)))");
    final QueryAnswers<QueryAnswer> answers = currentQuery.getAnswers();
    final List<String> answerStrings = answers.toAnswersTableStrings(false, ",", "_");
    for (String str : answerStrings) {
      System.out.println(str);
    }
    assertTrue(answerStrings.size() > 1);
    assertEquals(answers.size() + 2, answerStrings.size());
    assertEquals("Answer_,_?ANIMAL", answerStrings.get(0).substring(0, 16));
    assertEquals("------_,_-------", answerStrings.get(1).substring(0, 16));
    assertEquals("     0_,_",        answerStrings.get(2).substring(0, 9));
    assertEquals("     1_,_",        answerStrings.get(3).substring(0, 9));
    assertNotEquals("------------------", answerStrings.get(answerStrings.size() - 1).substring(0, 16));
    final QueryAnswer answer1 = answers.get(0);
    final Object binding = answer1.getBinding(Variable.get("?ANIMAL"));
    assertEquals("     0_,_" + binding, answerStrings.get(2).substring(0, 9) + binding);
  }
  
  @Test
  public void testQueryAnswerToPrettyBindingsStrings() throws Exception {
    final Query q = Query.get(
            "(and"
            + " (isa ?ANIMAL ?TYPE)"
            + " (or (equals ?TYPE Cat) (equals ?TYPE Dog)))");
    final List<QueryAnswer> answers = q.getAnswers();
    assertTrue(!answers.isEmpty());
    final QueryAnswer answer1 = answers.get(0);
    final List<String> bindingsStrings = answer1.toPrettyBindingsStrings();
    for (String str : bindingsStrings) {
      System.out.println(str);
    }
    assertTrue(bindingsStrings.size() > 1);
    assertEquals(answer1.getVariables().size(), bindingsStrings.size());
    assertEquals("?ANIMAL = ", bindingsStrings.get(0).substring(0, 10));
    assertEquals("?TYPE   = ", bindingsStrings.get(1).substring(0, 10));
    final Object binding = answer1.getBinding(Variable.get("?ANIMAL"));
    assertEquals("?ANIMAL = " + binding, bindingsStrings.get(0));
  }
  
}
