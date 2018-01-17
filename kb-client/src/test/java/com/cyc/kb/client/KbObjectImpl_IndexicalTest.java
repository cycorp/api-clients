package com.cyc.kb.client;

/*
 * #%L
 * File: KbObjectImpl_IndexicalTest.java
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

import com.cyc.Cyc;
import com.cyc.kb.Context;
import com.cyc.kb.KbObject;
import com.cyc.kb.exception.CreateException;
import com.cyc.kb.exception.KbTypeException;
import com.cyc.session.exception.SessionCommunicationException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

import static com.cyc.Cyc.Constants.INFERENCE_PSC;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


public class KbObjectImpl_IndexicalTest {

  @Before
  public void setUp() throws Exception {
    indexicalsResolvableToDates = Arrays.asList(Cyc.getKbObject("#$Now-Indexical"),
            Cyc.getKbObject("#$Now"),
            Cyc.getKbObject("#$TheYear-Indexical"),
            Cyc.getKbObject("#$Yesterday-Indexical"),
            Cyc.getKbObject("#$Tomorrow-Indexical"),
            Cyc.getKbObject("#$Today-Indexical"));
    indexicalsResolvableToMts = Arrays.asList(Cyc.getKbObject("#$QueryMt"),
            Cyc.getKbObject("#$ParaphraseDomainMt"),
            Cyc.getKbObject("#$ParaphraseLanguageMt"),
            Cyc.getKbObject("#$ParaphraseDomainMt"),
            Cyc.getKbObject("#$ParaphraseLanguageMt"));
    indexicalsResolvableToTerms = Arrays.asList(Cyc.getKbObject("#$TheUser"));
    indexicalsResolvableToStrings = Arrays.asList(Cyc.getKbObject("#$TheCurrentHostName"),
            Cyc.getKbObject("#$TheCurrentSystemNumber"),
            Cyc.getKbObject("#$TheCurrentKBNumber"));
    indexicalsResolvableToIntegers = Arrays.asList(Cyc.getKbObject("#$SecondsSince1970-Indexical"));
    unresolvableIndexicals = Arrays.asList(Cyc.getKbObject("#$ThisProblemStore"),
            Cyc.getKbObject("#$ThisInference"),
            Cyc.getKbObject("(TheNamedFn SetOrCollection \"indexical 1\")"));
    nonIndexicals = Arrays.asList(Cyc.getKbObject("#$Dog"),
            Cyc.getKbObject("#$Cat"));
  }
  
  
  // Fields
  
  public List<KbObject> indexicalsResolvableToDates;
  public List<KbObject> indexicalsResolvableToMts;
  public List<KbObject> indexicalsResolvableToTerms;
  public List<KbObject> indexicalsResolvableToStrings;
  public List<KbObject> indexicalsResolvableToIntegers;
  public List<KbObject> unresolvableIndexicals;
  public List<KbObject> nonIndexicals;
  
  
  // Tests
  
  @Test
  public void testIsIndexical() throws SessionCommunicationException {
    for (KbObject obj : indexicalsResolvableToMts) {
      System.out.println("Should be an indexical    : " + obj);
      assertTrue(obj.isIndexical());
    }
    for (KbObject obj : indexicalsResolvableToDates) {
      System.out.println("Should be an indexical    : " + obj);
      assertTrue(obj.isIndexical());
    }
    for (KbObject obj : indexicalsResolvableToTerms) {
      System.out.println("Should be an indexical    : " + obj);
      assertTrue(obj.isIndexical());
    }
    for (KbObject obj : indexicalsResolvableToStrings) {
      System.out.println("Should be an indexical    : " + obj);
      assertTrue(obj.isIndexical());
    }
    for (KbObject obj : indexicalsResolvableToIntegers) {
      System.out.println("Should be an indexical    : " + obj);
      assertTrue(obj.isIndexical());
    }
    for (KbObject obj : unresolvableIndexicals) {
      System.out.println("Should be an indexical    : " + obj);
      assertTrue(obj.isIndexical());
    }
    for (KbObject obj : nonIndexicals) {
      System.out.println("Should NOT be an indexical: " + obj);
      assertFalse(obj.isIndexical());
    }
  }
  
  @Test
  public void testResolveIndexical_Dates() 
          throws SessionCommunicationException, KbTypeException, CreateException {
    for (KbObject indexical : indexicalsResolvableToDates) {
      System.out.println("Indexical: " + indexical);
      final Object result = indexical.resolveIndexical();
      System.out.println("    Class: "
              + ((result != null) ? result.getClass().getSimpleName() : null));
      System.out.println("        => " + result);
      assertNotNull(result);
      assertTrue((result instanceof KbIndividualImpl));
      // TODO: Check whether it's *actually* a date. - nwinant, 2017-06-29
    }
  }
  
  @Test
  public void testResolveIndexical_Mts() 
          throws SessionCommunicationException, KbTypeException, CreateException {
    for (KbObject indexical : indexicalsResolvableToMts) {
      System.out.println("Indexical: " + indexical);
      final Object result = indexical.resolveIndexical();
      System.out.println("    Class: "
              + ((result != null) ? result.getClass().getSimpleName() : null));
      System.out.println("        => " + result);
      assertNotNull(result);
      assertTrue((result instanceof Context));
    }
  }
  
  @Test
  public void testResolveIndexical_Terms() 
          throws SessionCommunicationException, KbTypeException, CreateException {
    for (KbObject indexical : indexicalsResolvableToTerms) {
      System.out.println("Indexical: " + indexical);
      final Object result = indexical.resolveIndexical();
      System.out.println("    Class: "
              + ((result != null) ? result.getClass().getSimpleName() : null));
      System.out.println("        => " + result);
      assertNotNull(result);
      assertTrue(result instanceof KbObject);
    }
  }
  
  @Test
  public void testResolveIndexical_Strings() 
          throws SessionCommunicationException, KbTypeException, CreateException {
    for (KbObject indexical : indexicalsResolvableToStrings) {
      System.out.println("Indexical: " + indexical);
      final Object result = indexical.resolveIndexical();
      System.out.println("    Class: " 
              + ((result != null) ? result.getClass().getSimpleName() : null));
      System.out.println("        => " + result);
      assertNotNull(result);
      assertTrue(result instanceof String);
    }
  }
  
  @Test
  public void testResolveIndexical_Integers() 
          throws SessionCommunicationException, KbTypeException, CreateException {
    for (KbObject indexical : indexicalsResolvableToIntegers) {
      System.out.println("Indexical: " + indexical);
      final Object result = indexical.resolveIndexical();
      System.out.println("    Class: " 
              + ((result != null) ? result.getClass().getSimpleName() : null));
      System.out.println("        => " + result);
      assertNotNull(result);
      assertTrue(result instanceof Integer);
    }
  }
  
  @Test
  public void testResolveIndexical_unresolvable_Indexicals() 
          throws SessionCommunicationException, KbTypeException, CreateException {
    for (KbObject indexical : unresolvableIndexicals) {
      System.out.println("Indexical: " + indexical);
      KbTypeException result = null;
      try {
        indexical.resolveIndexical();
      } catch (KbTypeException ex) {
        System.out.println("        => " + ex.getMessage());
        result = ex;
      }
      assertNotNull(result);
    }
  }
  
  @Test
  public void testResolveIndexical_unresolvable_NonIndexicals() 
          throws SessionCommunicationException, KbTypeException, CreateException {
    for (KbObject obj : nonIndexicals) {
      System.out.println("NON-indexical: " + obj);
      KbTypeException result = null;
      try {
        obj.resolveIndexical();
      } catch (KbTypeException ex) {
        System.out.println("        => " + ex.getMessage());
        result = ex;
      }
      assertNotNull(result);
    }
  }
  
  @Test
  public void testPossiblyResolveIndexical_AutoResolvableIndexicals_with_substitutions() 
          throws SessionCommunicationException, KbTypeException, CreateException {
    final KbObject expected = INFERENCE_PSC;
    for (KbObject indexical : indexicalsResolvableToDates) {
      System.out.println("Indexical: " + indexical);
      final Map<KbObject, Object> substitutions = new HashMap();
      substitutions.put(indexical, expected);
      final KbObject result = indexical.possiblyResolveIndexical(substitutions);
      assertEquals(expected, result);
      assertNotEquals(indexical.resolveIndexical(), result);
    }
  }
  
  @Test
  public void testPossiblyResolveIndexical_AutoResolvableIndexicals_no_substitutions() 
          throws SessionCommunicationException, KbTypeException, CreateException {
    final Map<KbObject, Object> substitutions = new HashMap();
    substitutions.put(INFERENCE_PSC, INFERENCE_PSC);
    for (KbObject indexical : indexicalsResolvableToDates) {
      System.out.println("Indexical: " + indexical);
      final KbObject result = indexical.possiblyResolveIndexical(substitutions);
      assertEquals(indexical, result);
      assertNotEquals(indexical.resolveIndexical(), result);
    }
  }
  
  @Test
  public void testPossiblyResolveIndexical_UnresolvableIndexicals_with_substitutions() 
          throws SessionCommunicationException, KbTypeException, CreateException {
    final KbObject expected = INFERENCE_PSC;
    for (KbObject indexical : unresolvableIndexicals) {
      System.out.println("Indexical: " + indexical);
      final Map<KbObject, Object> substitutions = new HashMap();
      substitutions.put(indexical, expected);
      final KbObject result = indexical.possiblyResolveIndexical(substitutions);
      assertEquals(expected, result);
    }
  }
  
  @Test
  public void testPossiblyResolveIndexical_UnresolvableIndexicals_no_substitutions() 
          throws SessionCommunicationException, KbTypeException, CreateException {
    final Map<KbObject, Object> substitutions = new HashMap();
    substitutions.put(INFERENCE_PSC, INFERENCE_PSC);
    for (KbObject indexical : unresolvableIndexicals) {
      System.out.println("Indexical: " + indexical);
      KbTypeException result = null;
      try {
        indexical.possiblyResolveIndexical(substitutions);
      } catch (KbTypeException ex) {
        System.out.println("        => " + ex.getMessage());
        result = ex;
      }
      assertNotNull(result);
    }
  }
  
  @Test
  public void testPossiblyResolveIndexical_NonIndexicals_with_substitutions() 
          throws SessionCommunicationException, KbTypeException, CreateException {
    final KbObject expected = INFERENCE_PSC;
    for (KbObject obj : nonIndexicals) {
      System.out.println("NON-indexical: " + obj);
      final Map<KbObject, Object> substitutions = new HashMap();
      substitutions.put(obj, expected);
      KbTypeException result = null;
      try {
        obj.possiblyResolveIndexical(substitutions);
      } catch (KbTypeException ex) {
        System.out.println("        => " + ex.getMessage());
        result = ex;
      }
      assertNotNull(result);
    }
  }
  
  @Test
  public void testPossiblyResolveIndexical_NonIndexicals_no_substitutions() 
          throws SessionCommunicationException, KbTypeException, CreateException {
    final Map<KbObject, Object> substitutions = new HashMap();
    substitutions.put(INFERENCE_PSC, INFERENCE_PSC);
    for (KbObject obj : nonIndexicals) {
      System.out.println("NON-indexical: " + obj);
      final KbObject result = obj.possiblyResolveIndexical(substitutions);
      assertEquals(obj, result);
    }
  }
  
}
