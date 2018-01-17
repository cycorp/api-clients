package com.cyc.kb.client;

/*
 * #%L
 * File: KbPredicateImplTest.java
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
import com.cyc.kb.Context;
import com.cyc.kb.Fact;
import com.cyc.kb.KbCollection;
import com.cyc.kb.KbPredicate;
import com.cyc.kb.KbTerm;
import com.cyc.kb.exception.CreateException;
import com.cyc.kb.exception.DeleteException;
import com.cyc.kb.exception.KbException;
import com.cyc.kb.exception.KbRuntimeException;
import com.cyc.kb.exception.KbTypeException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.cyc.kb.client.TestConstants.appleProductMt;
import static com.cyc.kb.client.TestConstants.product;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class KbPredicateImplTest {

  private static final Set<KbTerm> TEST_TERMS = new HashSet<>();
  
  @BeforeClass
  public static void setUp() throws Exception {
    TestConstants.ensureInitialized();
    if (!ContextImpl.existsAsType("AppleProductMt")) {
      TEST_TERMS.add(ContextImpl.findOrCreate("AppleProductMt"));
    }
    if (!KbPredicateImpl.existsAsType("iLikes")) {
      TEST_TERMS.add(KbPredicateImpl.findOrCreate("iLikes"));
    }
  }

  @AfterClass
  public static void tearDown() throws Exception {
    for (KbTerm obj : TEST_TERMS) {
      obj.delete();
    }
  }

  @Test
  public void testPredicate() throws KbException, UnknownHostException, IOException {
    String str = "age";

    KbIndividualImpl i = KbIndividualImpl.get(str);
    assertEquals(i.getCore().cyclify(), "#$age");
  }

  @Test
  public void testGenls() throws Exception {
    KbPredicate p = KbPredicateImpl.get("iLikes");
    Context ctx = ContextImpl.get("AppleProductMt");
    //p.addGeneralization("likesObject", "AppleProductMt");
    //assertTrue(p.getGeneralizations("AppleProductMt").contains(KbPredicateImpl.get("likesObject")));
    p.addGeneralization(KbPredicate.get("likesObject"), appleProductMt);
    assertTrue(p.getGeneralizations(appleProductMt).contains(KbPredicateImpl.get("likesObject")));
  }

  @Test
  public void testSpecs() throws KbException {
    KbPredicate p = KbPredicateImpl.get("likesObject");
    //p.addSpecialization("iLikes", "AppleProductMt");
    //assertTrue(p.getSpecializations("AppleProductMt").contains(KbPredicateImpl.get("iLikes")));
    p.addSpecialization(KbPredicate.get("iLikes"), appleProductMt);
    assertTrue(p.getSpecializations(appleProductMt).contains(KbPredicateImpl.get("iLikes")));
  }

  @Test
  public void testArgIsa() throws KbException {
    KbPredicateImpl p = KbPredicateImpl.get("iLikes");
    //p.addArgIsa(1, "Person", "AppleProductMt");
    //assertEquals(p.getArgIsa(1, "AppleProductMt").iterator().next().toString(), "Person");
    p.addArgIsa(1, KbCollection.get("Person"), appleProductMt);
    assertEquals(p.getArgIsa(1, appleProductMt).iterator().next().toString(), "Person");

    KbCollection iprod = new KbCollectionImpl("iProduct");
    //iprod.addGeneralization("Product", "AppleProductMt");
    iprod.addGeneralization(product, appleProductMt);
    p.addArgIsa(2, iprod, new ContextImpl("AppleProductMt"));
  }

  @Test
  public void testArgGenl() throws KbException {
    KbPredicateImpl p = KbPredicateImpl.get("iLikes");

    // TODO: This assertion does not make sense logically (the arg2 of iLikes
    // is an Individual, not a Collection). Find a better assertion.
    //p.addArgGenl(2, "Product", "AppleProductMt");
    //assertEquals(p.getArgGenl(2, "AppleProductMt").iterator().next().toString(), "Product");
    p.addArgGenl(2, product, appleProductMt);
    assertEquals(p.getArgGenl(2, appleProductMt).iterator().next().toString(), "Product");
  }
  
  /*
  @Test
  public void testChaining() throws KBTypeConflictException, InvalidNameException, KBApiException {
      BinaryPredicate p1 = BinaryPredicate.findOrCreate("iLikes34");
      testTerms.add(p1);
      BinaryPredicate p2 = p1.addArgGenl(1, "Person", "BaseKB").addGeneralization("likesAsFriend", "BaseKB");
      assertEquals("Chained methods did not return the original predicate", p1, p2);
      assertTrue("Chained methods did not return the original predicate", p2.getGeneralizations().contains(Predicate.get("likesAsFriend")));
              
  }
  */
  
  @Test
  public void testDelete() {
    try {
      KbPredicateImpl p = KbPredicateImpl.findOrCreate("iLikes2");
      p.delete();

      KbCollectionImpl.findOrCreate("iProduct2").delete();
      ContextImpl.findOrCreate("AppleProductMt2").delete();

      assertEquals(p.getComments().size(), 0);
    } catch (KbRuntimeException ex) {
      assertEquals(ex.getMessage(),
              "The reference to iLikes2 object is stale. Possibly because it was delete using x.delete() method.");
    } catch (CreateException | KbTypeException | DeleteException e) {
      e.printStackTrace(System.out);
      fail("Failed to delete predicate");
    }
  }

  @Test
  public void testGetExtent() throws KbTypeException, CreateException {
    List<Fact> expected = new ArrayList();
    expected.add(FactImpl.get("(owns SomeAirline Plane-APITest)", "SomeAirlineEquipmentMt"));
    Collection<Fact> predExtent = KbPredicateImpl.get("owns").getExtent(ContextImpl.get("SomeAirlineEquipmentMt"));
    assertTrue("Could not find any assertions with owns as its predicate", predExtent.size() > 0);
    assertTrue(predExtent.containsAll(expected));
  }
}
