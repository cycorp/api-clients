package com.cyc.kb.client;

/*
 * #%L
 * File: KbFunctionImplTest.java
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

import com.cyc.base.cycobject.DenotationalTerm;
import com.cyc.base.cycobject.Naut;
import com.cyc.kb.Context;
import com.cyc.kb.Fact;
import com.cyc.kb.KbCollection;
import com.cyc.kb.KbFunction;
import com.cyc.kb.KbIndividual;
import com.cyc.kb.KbPredicate;
import com.cyc.kb.KbTerm;
import com.cyc.kb.Variable;
import static com.cyc.kb.client.TestUtils.assumeNotEnterpriseCyc;
import com.cyc.kb.exception.CreateException;
import com.cyc.kb.exception.DeleteException;
import com.cyc.kb.exception.KbException;
import com.cyc.kb.exception.KbRuntimeException;
import com.cyc.kb.exception.KbTypeException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class KbFunctionImplTest {

  private static final Logger LOG = LoggerFactory.getLogger(KbFunctionImplTest.class);
  private static KbFunctionImpl<DenotationalTerm> appfn;
  private static KbCollection iPad;

  @BeforeClass
  public static void setUp() throws Exception {
    TestConstants.ensureInitialized();
    appfn = KbFunctionImpl.findOrCreate("AppFn");
    KbPredicateImpl.findOrCreate("arity").addFact(ContextImpl.findOrCreate("AppleProductMt"), appfn, 1);
    appfn.addResultIsa("FirstOrderCollection", "UniversalVocabularyMt");
    appfn.instantiates(KbCollectionImpl.get("ReifiableFunction"));
  }
  
  @AfterClass
  public static void tearDown() throws Exception {
  }

  @Test
  public void testFunction() throws KbException, UnknownHostException, IOException {
    String str = "AgeFn";
    KbCollection constrainingCol = KbCollectionImpl.get("#$UnaryFunction");
    Context ctx = ContextImpl.get("#$UniversalVocabularyMt");

    KbFunctionImpl f = KbFunctionImpl.findOrCreate(str, constrainingCol, ctx);
    assertEquals(f.getCore().cyclify(), "#$AgeFn");
  }

  @Test
  public void testFunctionString() throws KbTypeException, CreateException {
    KbFunctionImpl f = KbFunctionImpl.findOrCreate("AppFn");
    KbPredicateImpl.get("arity").addFact(ContextImpl.findOrCreate("AppleProductMt"), f, 1);
  }

  @Test
  public void testArgIsa() throws KbException {
    KbFunctionImpl f = KbFunctionImpl.findOrCreate("AppFn");
    KbCollection c = KbCollectionImpl.findOrCreate("iProduct");
    c.addGeneralization("Product", "AppleProductMt");

    f.addArgIsa(1, "iProduct", "AppleProductMt");
    assertEquals(f.getArgIsa(1, "AppleProductMt").iterator().next().toString(), "iProduct");
    new FactImpl("AppleProductMt", "(argIsa AppFn 1 iProduct)").delete();
    try {
      FactImpl factImpl = new FactImpl(true, "AppleProductMt", "(arg1Isa AppFn iProduct)");
      fail("This shouldn't be true any more.");
    } catch (KbException ex) {
    }
  }

  @Test
  public void testArgGenl() throws KbException {
    KbFunctionImpl f = KbFunctionImpl.findOrCreate("AppFn");
    f.addArgGenl(1, "Product", "AppleProductMt");
    assertTrue(f.getArgGenl(1, "AppleProductMt").contains(KbCollectionImpl.get("Product")));
  }

  @Test
  public void testResultIsa() throws KbException {
    KbFunction f = KbFunctionImpl.findOrCreate("AppFn");
    final String computerProgramTypeByPlatform = "ComputerProgramTypeByPlatform";
    f.addResultIsa(computerProgramTypeByPlatform, "AppleProductMt");
    final Collection<KbCollection> resultIsas = f.getResultIsa("AppleProductMt");
    assertTrue("resultIsas for " + f + " were " + resultIsas
            + ". Couldn't find " + computerProgramTypeByPlatform,
            resultIsas.contains(SecondOrderCollectionImpl.get(computerProgramTypeByPlatform)));
  }

  @Test
  public void testGenObject() throws KbException, Exception {
    KbFunctionImpl<DenotationalTerm> f = KbFunctionImpl.get("FruitFn");
    KbCollection c = f.<KbCollection>findOrCreateFunctionalTerm(KbCollection.class,
            KbCollectionImpl.get("AppleTree"));
    LOG.debug("Returned function: {0}", c.toString());

    /*
    KBFunctionImpl f2 = KBFunctionImpl.get("AVWorkWithIDFn");
    KBIndividual ct = f2.<KBIndividual>findOrCreateFunctionalTerm(KBIndividual.class,
            507995);
    LOGGER.fine("Returned func extent ct: " + ct.toString());
    assertTrue(ct + " should be an instance of CycNart, but isn't.", ct.getCore() instanceof Nart);
    */
    
    KbFunctionImpl<DenotationalTerm> f3 = KbFunctionImpl.get("SomeAirlineEquipmentLogFn");    
    Context ctx2 = f3.<Context>findOrCreateFunctionalTerm(Context.class, KbIndividualImpl.get("Plane-APITest"));
    final Collection<Context> supers = ctx2.getInheritsFrom();
    LOG.debug("Returned func extent mt: {0}", supers);
    assertTrue(supers.contains(ContextImpl.findOrCreate("BaseKB")));

    KbIndividual i = KbIndividualImpl.findOrCreate("TestIndividual001");
    i.instantiates(KbCollectionImpl.get("Person"), ctx2);

    i.instantiates("MaleHuman", 
            "(SomeAirlineEquipmentLogFn Plane-APITest)");
    Fact a = new FactImpl("(SomeAirlineEquipmentLogFn Plane-APITest)",
            "(#$isa #$TestIndividual001 (#$CitizenFn #$UnitedStatesOfAmerica))");

    // Apple Ontology related
    KbCollection ipad = KbCollectionImpl.findOrCreate("iPad-Test");
    ipad.addGeneralization(KbCollectionImpl.findOrCreate("iProduct"), ContextImpl.findOrCreate("AppleProductMt"));

    appfn.addArgGenl(1, "iProduct", "AppleProductMt");
    KbTerm ipadapp = appfn.<KbTerm>findOrCreateFunctionalTerm(KbTerm.class, ipad);
    ipadapp.instantiates((KbCollection)KbCollectionImpl.getClassType());
    ipadapp.instantiates("ComputerProgramTypeByPlatform", "AppleProductMt");

    KbFunctionImpl f4 = KbFunctionImpl.get("MtSpace");
    f4.<Context>findOrCreateFunctionalTerm(Context.class, new Date());
    f3.<Context>findOrCreateFunctionalTerm(Context.class, KbIndividualImpl.get("Plane-APITest"));
  }

  @Test
  public void testUnreifiableFunctionWithDateInput() throws KbException {
    KbFunctionImpl<DenotationalTerm> f4 = KbFunctionImpl.get("MtDim");
    KbPredicate p = KbPredicateImpl.get("mtTimeIndex");
    Context c = f4.<Context>findOrCreateFunctionalTerm(Context.class, p, new Date());
    System.out.println("Context : " + c);

    KbFunction usd = KbFunctionImpl.findOrCreate("USDollarFn");
    KbFunction dollars = usd.findOrCreateFunctionalTerm(KbFunction.class, 2012);
    System.out.println("Creating functional term 2012 Dollars: " + dollars);
    dollars.addResultIsa(KbCollectionImpl.get("MonetaryValue"), Constants.uvMt());
    Fact f = FactImpl.findOrCreate(new SentenceImpl(KbPredicateImpl.get("argsIsa"), dollars, KbCollectionImpl.get("NumericInterval")), Constants.uvMt());
    
    KbIndividual m1 = dollars.findOrCreateFunctionalTerm(KbIndividualImpl.class, 5);
    System.out.println("Ind 1: " + m1);

    KbIndividualImpl m2 = KbIndividualImpl.findOrCreate("((USDollarFn 2012) 5)");
    System.out.println("Ind 2: " + m2);
    assertTrue(m2 + " should be a NAUT, but is not.", m2.getCore() instanceof Naut);
  }

  @Test
  public void testUnreifiableFunctionWithDateInput2() throws KbException {
  
    KbFunctionImpl<DenotationalTerm> f1 = KbFunctionImpl.get ("USDollarFn");
    KbFunctionImpl<DenotationalTerm> f2 = f1.findOrCreateFunctionalTerm(KbFunctionImpl.class, 2012);
    f2.addResultIsa(KbCollectionImpl.get("MonetaryValue"), ContextImpl.get("UniversalVocabularyMt"));
    FactImpl.findOrCreate(new SentenceImpl(KbPredicateImpl.get("argsIsa"), f2, KbCollectionImpl.get("NumericInterval")));
    KbIndividual i1 = f2.findOrCreateFunctionalTerm(KbIndividualImpl.class, 10000000);
    
    KbPredicate revenueForPeriodByAccountingCOC = KbPredicateImpl.get("revenueForPeriodByAccountingCOC");
    KbIndividualImpl w = KbIndividualImpl.get("Walmart-CommercialOrganization");
    
    KbFunctionImpl<DenotationalTerm> fy = KbFunctionImpl.get("FiscalYearFn");
    KbIndividual i2 = fy.findOrCreateFunctionalTerm(KbIndividualImpl.class, w, 2012);
    KbIndividual coc = KbIndividualImpl.findOrCreate("(#$AccountingCodeOfTypeTypicallyUsedByAgentFn #$GenerallyAcceptedAccountingPrinciples #$Walmart-CommercialOrganization)");
    revenueForPeriodByAccountingCOC.addFact(Constants.uvMt(), w, i1, i2, coc);
  }
  
  @Test
  public void testDelete() {
    try {
      KbFunctionImpl f = KbFunctionImpl.findOrCreate("SomeRandomConstant");
      f.delete();
      assertEquals(f.getComments().size(), 0);
    } catch (KbRuntimeException ex) {
      assertEquals(ex.getMessage(), "The reference to SomeRandomConstant object is stale. Possibly because it was delete using x.delete() method.");
    } catch (CreateException | KbTypeException | DeleteException e) {
      fail("Failed to delete something.");
    }
  }
  
  /**
   * TODO: This test fails in EnterpriseCyc 1.7-preview; it should be rewritten to use vocabulary 
   *       present in all Cyc releases.
   * 
   * @throws Exception 
   */
  @Test 
  public void testFunctionCreateKBTerm() throws Exception {
    assumeNotEnterpriseCyc();
    //KBFunctionImpl fin = KBFunctionImpl.get("FindObjectByCompactHLExternalIDStringFn");
    KbFunctionImpl phys = KbFunctionImpl.get("ThePhysicalFieldValueFn");

    KbIndividualImpl ps = KbIndividualImpl.findOrCreate("DreamStore-EVIDENCE-PS");
    ps.isInstanceOf(KbCollectionImpl.get("PhysicalSchema"), ContextImpl.get("UniversalVocabularyMt"));
    phys.findOrCreateFunctionalTerm(KbIndividualImpl.class, ps, "SOMETHING");

  //fin.findOrCreateFunctionalTerm(KBTermImpl.class, 
    //      phys.findOrCreateFunctionalTerm(KBIndividualImpl.class, ps, "SOMETHING"));
    KbFunctionImpl lsf = KbFunctionImpl.get("TheLogicalFieldValueFn");
    KbIndividualImpl ls = KbIndividualImpl.findOrCreate("DreamStore-EVIDENCE-LS");
    ls.isInstanceOf(KbCollectionImpl.get("LogicalSchema"), ContextImpl.get("UniversalVocabularyMt"));

    //(TheLogicalFieldValueFn DreamStore-EVIDENCE-LS Set-Mathematical 1)
    lsf.findOrCreateFunctionalTerm(KbTermImpl.class, ls, KbCollectionImpl.get("Set-Mathematical"), 1);

    KbFunction elIBF = KbFunctionImpl.get("ELInferenceBindingFn");
    Variable v = new VariableImpl("?X");
    List<Object> nestedlist = new ArrayList<>();

    KbFunction paren = KbFunctionImpl.get("ParenthesizedMathFn");
    KbFunction mathQ = KbFunctionImpl.get("MathQuantFn");
    KbIndividual mathQ1 = mathQ.findOrCreateFunctionalTerm(KbIndividual.class, 1);
    KbIndividual paren1 = paren.findOrCreateFunctionalTerm(KbIndividual.class, mathQ1);
    nestedlist.add(mathQ1);
    nestedlist.add(paren1);

    KbIndividual elibf1 = elIBF.findOrCreateFunctionalTerm(KbIndividual.class, v, nestedlist);
    KbIndividual expected = KbIndividualImpl.get("(ELInferenceBindingFn ?X (TheList (MathQuantFn 1) (ParenthesizedMathFn (MathQuantFn 1))))");
    assertEquals(expected, elibf1);
  }
}
