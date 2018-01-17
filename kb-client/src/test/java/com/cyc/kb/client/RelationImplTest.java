package com.cyc.kb.client;

/*
 * #%L
 * File: RelationImplTest.java
 * Project: KB Client
 * %%
 * Copyright (C) 2013 - 2018 Cycorp, Inc
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

import com.cyc.kb.exception.KbException;
import java.io.IOException;
import java.net.UnknownHostException;
import org.junit.AfterClass;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;

public class RelationImplTest {

  @BeforeClass
  public static void setUp() throws Exception {
    TestConstants.ensureInitialized();
  }

  @AfterClass
  public static void tearDown() throws Exception {
  }

  @Test
  public void testArity() throws KbException, UnknownHostException, IOException, Exception {
    KbPredicateImpl isa = KbPredicateImpl.get("isa");
    assertTrue(isa.getArityMin() == 2);
    assertTrue(isa.getArityMax() == 2);
    assertTrue(isa.getArity() == 2);
    final String predName = "testArityTestPred";
    assertFalse(KbPredicateImpl.existsAsType(predName));
    KbPredicateImpl np = KbPredicateImpl.findOrCreate(predName);
    FactImpl factImpl = new FactImpl("BaseKB", "(#$arityMax " + predName + " 12)");
    FactImpl factImpl1 = new FactImpl("BaseKB", "(#$arityMin " + predName + " 1)");

    assertTrue(np.getArityMin() == 1);
    assertTrue("Got " + np.getArityMax() + " as max arity, but expected 12", np.getArityMax() == 12);
    //assertTrue(p.setArity() == 2);

    np.delete();
  }
}
