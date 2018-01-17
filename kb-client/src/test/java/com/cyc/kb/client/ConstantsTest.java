package com.cyc.kb.client;

/*
 * #%L
 * File: ConstantsTest.java
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

import com.cyc.baseclient.CycObjectLibraryLoader;
import com.cyc.baseclient.CycObjectLibraryLoader.CycLibraryField;
import com.cyc.kb.Context;
import com.cyc.kb.KbObject;
import com.cyc.kb.KbObjectLibraryLoader;
import java.util.Collection;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author vijay
 */
public class ConstantsTest {
  
    @BeforeClass
    public static void setUp() throws Exception {
        TestConstants.ensureInitialized();
    }

    @AfterClass
    public static void tearDown() throws Exception {
    }
    
    @Test
    public void testGetInstance() throws Exception {
        System.out.println("getInstance");
    }

    @Test
    public void testDataMt() throws Exception {
        System.out.println("dataMt");
    }

    @Test
    public void testBaseKbMt() throws Exception {
        System.out.println("baseKbMt");
    }

    @Test
    public void testEverythingPSCMt() throws Exception {
        System.out.println("everythingPSCMt");
        Context result = Constants.everythingPSCMt();
        assertEquals(ContextImpl.get("EverythingPSC"), result);
    }

    @Test
    public void testInferencePSCMt() throws Exception {
        System.out.println("inferencePSCMt");
        Context result = Constants.inferencePSCMt();
        assertEquals(ContextImpl.get("InferencePSC"), result);
        assertFalse(ContextImpl.get("EverythingPSC").equals(result));
    }
    
  @Test
  public void testKbObjectLibraryLoader() {
    final KbObjectLibraryLoader loader = new KbObjectLibraryLoader();
    final Collection<KbObject> allObj = loader.getAllKBObjectsForClass(Constants.class);
    for (KbObject o : allObj) {
      System.out.println("  - " + o);
    }
    assertTrue(allObj.isEmpty()); // Fields on com.cyc.kb.client.Constants are no longer public - nwinant, 2017-12-18
    //assertFalse(allObj.isEmpty());
    //assertEquals(32, allObj.size());
    
    // Do a deeper inspection of constants classes by name:
    //validateCycLibrary(loader, Constants.class);
  }
  
  
  // Protected
  
  protected void validateCycLibrary(KbObjectLibraryLoader loader, final Class<?> clazz) {
    final CycObjectLibraryLoader.CycLibraryFieldHandler handler = new CycObjectLibraryLoader.CycLibraryFieldHandler() {
      private int numFields = 0;
      private int numUnannotatedFields = 0;

      @Override
      public void onLibraryEvaluationBegin(Class libraryClass) {
        System.out.println("Validating CycLibrary " + clazz.getName() + "...");
      }

      @Override
      public void onFieldEvaluation(CycLibraryField cycField, String value, Boolean equivalent) {
        numFields++;
        final String fieldString = "  - " + cycField.getField().getType().getSimpleName() + ": " + cycField.getField().getName();
        final String valueString = "    " + value;
        if (cycField.isAnnotated()) {
          System.out.println(fieldString);
          System.out.println(valueString);
          assertEquals(cycField.getCycl(), value);
        } else {
          numUnannotatedFields++;
          System.out.println(fieldString + "      [WARNING! Field is not annotated]");
          System.out.println(valueString);
        }
      }

      @Override
      public void onException(CycLibraryField cycField, Exception ex) {
        throw new RuntimeException(ex);
      }

      @Override
      public void onLibraryEvaluationEnd(Class libraryClass, Collection<CycLibraryField> processedFields) {
        System.out.println("  " + numFields + " fields evaluated.");
        if (numUnannotatedFields == 0) {
          System.out.println("  All fields annotated, which is good!");
        } else {
          System.out.println("  " + numUnannotatedFields + " FIELDS WITHOUT ANNOTATION.");
        }
      }
    };
    loader.processAllFieldsForClass(clazz, handler);
  }
  
}
