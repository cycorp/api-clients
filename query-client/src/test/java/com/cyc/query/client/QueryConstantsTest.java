package com.cyc.query.client;

/*
 * #%L
 * File: QueryConstantsTest.java
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

import com.cyc.base.exception.CycConnectionException;
import com.cyc.baseclient.CycObjectLibraryLoader;
import com.cyc.baseclient.CycObjectLibraryLoader.CycLibraryField;
import com.cyc.kb.KbObject;
import com.cyc.kb.KbObjectLibraryLoader;
import java.util.Collection;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author baxter
 */
public class QueryConstantsTest {

  public QueryConstantsTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  @Test
  public void testKBObjectLibraryLoader() throws CycConnectionException {
    final KbObjectLibraryLoader loader = new KbObjectLibraryLoader();
    final Collection<KbObject> allObj = loader.getAllKBObjectsForClass(QueryConstants.class);
    for (KbObject o : allObj) {
      System.out.println("  - " + o);
    }
    assertFalse(allObj.isEmpty());
    if (TestUtils.getCyc().isOpenCyc()) {
      assertEquals(7, allObj.size());
    } else {
      assertEquals(8, allObj.size());
    }
    
    // Do a deeper inspection of constants classes by name:
    validateCycLibrary(loader, QueryConstants.class);
    validateCycLibrary(loader, QueryTestConstants.class);
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
