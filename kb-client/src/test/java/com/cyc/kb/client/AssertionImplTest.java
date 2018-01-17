/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cyc.kb.client;

/*
 * #%L
 * File: AssertionImplTest.java
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
import com.cyc.base.CycAccess;
import com.cyc.base.CycAccessManager;
import com.cyc.base.cycobject.CycAssertion;
import com.cyc.base.cycobject.CycConstant;
import com.cyc.base.cycobject.FormulaSentence;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.baseclient.cycobject.CycAssertionImpl;
import com.cyc.baseclient.cycobject.FormulaSentenceImpl;
import com.cyc.kb.Assertion;
import com.cyc.kb.Assertion.Direction;
import com.cyc.kb.Context;
import com.cyc.kb.Fact;
import com.cyc.kb.KbIndividual;
import com.cyc.kb.Sentence;
import com.cyc.kb.client.config.KbConfiguration;
import com.cyc.kb.exception.CreateException;
import com.cyc.kb.exception.DeleteException;
import com.cyc.kb.exception.KbException;
import com.cyc.kb.exception.KbObjectNotFoundException;
import com.cyc.kb.exception.KbTypeException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.cyc.kb.client.TestConstants.kbapitc;
import static com.cyc.kb.client.TestUtils.assumeKbObject;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * 
 * @author daves
 */
public class AssertionImplTest {

	/*
	 * @Rule public ExpectedException exception = ExpectedException.none();
	 */

	public AssertionImplTest() {
	}

	private static final Logger LOG = LoggerFactory.getLogger(AssertionImplTest.class.getName());

	@BeforeClass
	public static void setUpClass() throws Exception {    
      TestConstants.ensureInitialized();
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
	public void testAssertionFactoryMethods() throws Exception {
                CycAccess cyc = CycAccessManager.getCurrentAccess();
		FormulaSentence cfs = FormulaSentenceImpl
				.makeFormulaSentence(cyc, "(flyingDoneBySomething-Operate FlyingAPlane-APITest Pilot-APITest)");
		CycConstant cc = cyc.getLookupTool().find("SomeAirlineLogMt");
		CycAssertion ca = new CycAssertionImpl(cfs, cc);

		System.out.println("Assertion OC API: " + ca);
        LOG.info("Assertion OC API: " + ca);

		// Build getAssertionService from CycObject
		Assertion a = AssertionImpl.get(ca);
		System.out.println("Assertion KB API: " + a);
		assertEquals(ca, a.getCore());

		// KBObject getId works with Assertions
		String hlid = a.getId();
		System.out.println("HL ID of Assertion: " + hlid);

		// Get getAssertionService from its HLID
		Assertion aid = AssertionImpl.get(hlid);
		assertTrue(a == aid);

		// exception.expect(KBObjectNotFoundException.class);
		// The above stops all KBObjectNotFoundException after that point
		try {
			AssertionImpl.get("SomeRandomHLID");
			fail("Need a KBObjectNotFoundException.class");
		} catch (KbObjectNotFoundException kbonf) {
			// Nothing to do
		}

		// Did I get it from the cache?
		Assertion acache = AssertionImpl.get(ca);
		assertTrue(a == acache);

		// Get getAssertionService from formulaStr and ctxStr
		Assertion astr = AssertionImpl
				.get("(flyingDoneBySomething-Operate FlyingAPlane-APITest Pilot-APITest)",
						"SomeAirlineLogMt");
		assertTrue(a == astr);

		// Get getAssertionService from Sentence formula and Context ctx
		SentenceImpl s = new SentenceImpl(
				KbPredicateImpl.get("flyingDoneBySomething-Operate"),
				KbIndividualImpl.get("FlyingAPlane-APITest"),
				KbIndividualImpl.get("Pilot-APITest"));
		Context ctx = ContextImpl.get("SomeAirlineLogMt");
		Assertion asent = AssertionImpl.get(s, ctx);
		assertTrue(a == asent);
                
		// Add a new getAssertionService
        AssertionImpl atemp = (AssertionImpl) AssertionImpl.findOrCreate("(isa Pilot-APITest Person)",
				"UniversalVocabularyMt");
        atemp.delete();
        
        atemp = (AssertionImpl) AssertionImpl.findOrCreate("(isa Pilot-APITest Person)",
				"UniversalVocabularyMt");
        atemp.delete(true);
        
        KbConfiguration.getOptions().setShouldTranscriptOperations(true);
        atemp = (AssertionImpl) AssertionImpl.findOrCreate("(isa Pilot-APITest Person)",
				"UniversalVocabularyMt");
        atemp.delete(true);
        KbConfiguration.getOptions().setShouldTranscriptOperations(false);
        
        Assertion atemp1 = AssertionImpl.findOrCreate("(isa Pilot-APITest Person)",
				"UniversalVocabularyMt", null, Direction.BACKWARD);
        assertEquals(Direction.BACKWARD, atemp1.getDirection());
        atemp1.delete();
        
		Assertion atemp2 = AssertionImpl.findOrCreate("(isa Pilot-APITest Person)",
				"UniversalVocabularyMt", null, Direction.FORWARD);
		System.out.println("Assertion: " + atemp2);
		
		Direction d = atemp2.getDirection();
        assertEquals(Direction.FORWARD, d);
		System.out.println("Direction is: " + d);
		
		// Add a new getAssertionService in the default getContextService
		Assertion assertionDefContext = AssertionImpl.findOrCreate("(isa Plane-APITest AirTransportationDevice)");
		System.out.println("Assertion with default context: " + assertionDefContext);
		assertEquals(Constants.uvMt(), assertionDefContext.getContext());
	}

	/**
	 * Test of getContextService method, of class Assertion.
	 */
	@Test
	public void testContextAndFormula() throws Exception {
	
      System.out.println("context");
      SentenceImpl s = new SentenceImpl(
        KbPredicateImpl.get("flyingDoneBySomething-Operate"),
        KbIndividualImpl.get("FlyingAPlane-APITest"),
        KbIndividualImpl.get("Pilot-APITest"));      
      ContextImpl ctx = ContextImpl.get("SomeAirlineLogMt");
      Assertion asent = AssertionImpl.get(s, ctx);

      assertEquals(ctx, asent.getContext());
      assertEquals(s, asent.getFormula());
	}
    
	/* *
	 * Test of getSupportingAssertions method, of class Assertion.
	 */
        /*
	@Test
	public void testGetSupportingAssertions() throws KBApiException {
		// Covered in testIsDeducedAssertion
	}
        */
        
	/* *
	 * Test of getAllSupportingAssertions method, of class Assertion.
	 */
	/* 
           @Test
	 * public void testGetAllSupportingAssertions() throws KBApiException {
	 * System.out.println("getAllSupportingAssertions"); Assertion instance =
	 * new Assertion(); Collection expResult = null; Collection result =
	 * instance.getAllSupportingAssertions(); assertEquals(expResult, result);
	 * // TODO review the generated test code and remove the default call to
	 * fail. fail("The test case is a prototype."); }
	 */
	/**
	 * Test of isDeducedAssertion method, of class Assertion.
	 */
	/*
	@Test
	public void testIsDeducedAssertion() throws Exception {
		System.out.println("isDeducedAssertion");
		Assertion instance = new Fact(true, "UniversalVocabularyMt",
				"(isa (FruitFn AppleTree) ExistingObjectType)");
		boolean expResult = true;
		boolean result = instance.isDeducedAssertion();
		assertEquals(expResult, result);
		instance = new Fact(true, "BaseKB",
				"(genls (FruitFn RedDeliciousAppleTree) (FruitFn AppleTree))");
		assertEquals(true, instance.isDeducedAssertion());

		// asserted only
		instance = new Fact(true, "UniversalVocabularyMt",
				"(isa (FruitFn AppleTree) SpatiallyDisjointObjectType)");
		assertEquals(false, instance.isDeducedAssertion());

		// (holdsIn (DateDecodeStringFn "YYYY-MM-DDTHH:MM" "2014-03-15T10:11")
		// (artifactFoundInLocation Plane-APITest CityOfSanFranciscoCA))
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy MM dd HH:mm");
		Date d = sdf.parse("2014 03 15 10:20");
		Sentence s5 = new Sentence(Predicate.get("artifactFoundInLocation"),
				KBIndividual.get("Plane-APITest"),
				KBIndividual.get("CityOfSanFranciscoCA"));
		Sentence s6 = new Sentence(Predicate.get("holdsIn"), d, s5);
		Fact f = new Fact(Context.get("SomeAirlineLogMt"), s6);
		System.out.println("deduced: " + f.isDeducedAssertion());

		Assertion fa = new Fact(Context.get("SomeAirlineLogMt"),
				Predicate.get("endingDate"), KBIndividual.get("FlightXYZ-APITest"),
				d);
		System.out.println("Asserted? " + fa.isAssertedAssertion());

		Collection<Assertion> cola = f.getSupportingAssertions();

		assertTrue(cola.contains(fa));
	}*/
	

	@Test
	public void testIsDeducedAssertion() throws Exception {
		System.out.println("isDeducedAssertion");

		// (holdsIn (DateDecodeStringFn "YYYY-MM-DDTHH:MM" "2014-03-15T10:11")
		// (artifactFoundInLocation Plane-APITest CityOfSanFranciscoCA))
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy MM dd HH:mm");
		Date d = sdf.parse("2014 03 15 10:20");
		Sentence s5 = new SentenceImpl(TestConstants.kbapitc.artifactFoundInLocation,
				KbIndividualImpl.get("Plane-APITest"),
				KbIndividualImpl.get("TestCity002"));
		SentenceImpl s6 = new SentenceImpl(TestConstants.kbapitc.holdsIn, d, s5);
		FactImpl f = new FactImpl(ContextImpl.get("SomeAirlineLogMt"), s6);
		System.out.println("deduced: " + f.isDeducedAssertion());
        assertEquals(f + " is not a deduced assertion.", true, f.isDeducedAssertion());
        assertEquals(f + " is an asserted assertion.", false, f.isAssertedAssertion());
        
		SentenceImpl s = new SentenceImpl (TestConstants.kbapitc.endingDate, KbIndividualImpl.get("FlightXYZ-APITest"),
				d);
		Assertion fa = AssertionImpl.get(s, ContextImpl.get("SomeAirlineLogMt"));
		System.out.println("Asserted? " + fa.isAssertedAssertion());
        assertEquals(fa + " is not an asserted assertion.", true, fa.isAssertedAssertion());
        assertTrue(fa + " has supporting assertions.", fa.getSupportingAssertions().isEmpty());

		Collection<Assertion> cola = f.getSupportingAssertions();

		assertTrue(cola.contains(fa));
	}

	/**
	 * Test of isAssertedAssertion method, of class Assertion.
	 */
	@Test
  public void testIsAssertedAssertion() throws Exception {
    System.out.println("isAssertedAssertion");

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy MM dd HH:mm");
    try {
      Date d = sdf.parse("2014 03 15 10:20");
      FactImpl f = new FactImpl(ContextImpl.get("SomeAirlineLogMt"),
              TestConstants.kbapitc.endingDate,
              KbIndividualImpl.get("Plane-APITest"), d);
      System.out.println("Asserted? " + f.isAssertedAssertion());
      assertEquals(f + " is not an asserted assertion.", true, f.isAssertedAssertion());
      assertEquals(f + " is a deduced assertion.", false, f.isDeducedAssertion());
    } catch (ParseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  /**
   * Test of delete method, of class Assertion.
   */
  @Test
  public void testDelete() throws Exception {
    System.out.println("delete");
    SentenceImpl s = new SentenceImpl(
            KbPredicateImpl.get("flyingDoneBySomething-Operate"),
            KbIndividualImpl.get("FlyingAPlane-APITest"),
            KbIndividualImpl.get("Pilot-APITest"));
    ContextImpl ctx = ContextImpl.get("SomeAirlineLogMt");
    Assertion asent = AssertionImpl.get(s, ctx);
    asent.delete();
    try {
      AssertionImpl.get(s, ctx);
      fail("Need a KBObjectNotFoundException.class");
    } catch (KbObjectNotFoundException kbonf) {
    }

		// Put it back. JUnit can execute tests in arbitrary order, we need 
    // the delete getAssertionService later.
    AssertionImpl.findOrCreate(s, ctx);

    // Try to delete a forward derived getAssertionService
    // (holdsIn (DateDecodeStringFn "YYYY-MM-DDTHH:MM" "2014-03-15T10:11")
    // (artifactFoundInLocation Plane-APITest CityOfSanFranciscoCA))
    Context airlineLogMt = ContextImpl.findOrCreate("SomeAirlineLogMt");
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy MM dd HH:mm");
    Date d = sdf.parse("2014 03 15 10:20");
    Sentence s5 = new SentenceImpl(TestConstants.kbapitc.artifactFoundInLocation,
            KbIndividualImpl.get("Plane-APITest"),
            KbIndividualImpl.get("TestCity002"));
    SentenceImpl s6 = new SentenceImpl(TestConstants.kbapitc.holdsIn, d, s5);
    FactImpl f = new FactImpl(airlineLogMt, s6);
    System.out.println("deduced: " + f.isDeducedAssertion());
    assertEquals(true, f.isDeducedAssertion());
    try {
      f.delete();
      assertTrue("forward derived assertion was erroneosly removed via delete.", false); //an exception should be thrown before we get here
    } catch (DeleteException e) {
    }
    f.delete(true);
    try {
      assertTrue(AssertionImpl.get(s6, airlineLogMt) instanceof Assertion);
      assertTrue("Deleted assertion is still present!", false);
    } catch (CreateException e) {
      //We wanted the exception, since the getAssertionService isn't supposed to be there.
    }
    //repropagate a trigger gaf to make sure the getAssertionService is still there when we're done.
    FirstOrderCollectionImpl flying2Col = FirstOrderCollectionImpl.findOrCreate("Flying-Move");
    KbIndividualImpl flight = KbIndividualImpl.findOrCreate("FlightXYZ-APITest", flying2Col, airlineLogMt);
    
    // Make sure the derived getAssertionService is re-derived
    KbIndividual city2 = KbIndividualImpl.findOrCreate("TestCity002", kbapitc.city);
    FactImpl f2 = new FactImpl(airlineLogMt, KbPredicateImpl.get("toLocation"), flight, city2); 
    f2.changeDirection(Direction.BACKWARD);
    f2.changeDirection(Direction.FORWARD);
    assertTrue(AssertionImpl.get(s6, airlineLogMt) instanceof Assertion);
    
    

  }

    @Test
    public void testChangeDirection () throws KbTypeException, CreateException, CycConnectionException, KbException {	
      SentenceImpl s = new SentenceImpl(
        KbPredicateImpl.get("flyingDoneBySomething-Operate"),
        KbIndividualImpl.get("FlyingAPlane-APITest"),
        KbIndividualImpl.get("Pilot-APITest"));
      ContextImpl ctx = ContextImpl.get("SomeAirlineLogMt");
      Assertion asent = AssertionImpl.get(s, ctx);
      
      Assertion anew = asent.changeDirection(Direction.BACKWARD);
      if (asent.equals(anew)) {
        System.out.println("The assertions look identical to api");
      } else {
        System.out.println("The assertions look different to api");
      }
    }
    
    @Test
    public void testRetriggerForwardInference () throws KbTypeException, CreateException, DeleteException, KbException {
      assumeKbObject("ProprietaryTerm");
      ContextImpl ctx = ContextImpl.get("SomeAirlineLogMt");
      AssertionImpl.assertSentence("(implies (flyingDoneBySomething-Operate ?X ?Y) (quotedIsa ?X ProprietaryTerm))", "SomeAirlineLogMt", Assertion.Strength.AUTO, Direction.FORWARD);
      KbIndividual myFlying = Cyc.getKbIndividualService().findOrCreate("MyFlying", KbCollectionImpl.get("FlyingAnObject-Operate"));
      Cyc.getFactService().findOrCreate("(flyingDoneBySomething-Operate MyFlying WilliamHenryHarrison)", ctx.toString());
      Fact quotedIsaFact = Fact.get("(quotedIsa MyFlying ProprietaryTerm)", ctx.toString());
      assertTrue(quotedIsaFact instanceof Fact);
      ((FactImpl)quotedIsaFact).delete(true);
      Fact f = null;
      try {
        f = Fact.get("(quotedIsa MyFlying ProprietaryTerm)", ctx.toString());
      }catch (KbObjectNotFoundException e) {
        //we expect this to throw...
      }
      assertTrue(f ==  null);
      Fact doneByFact = Fact.get("(flyingDoneBySomething-Operate MyFlying WilliamHenryHarrison)", ctx.toString());
      ((FactImpl)doneByFact).retriggerForwardInference();
      assertTrue(Fact.get("(quotedIsa MyFlying ProprietaryTerm)", ctx.toString()) instanceof Fact);      
    }

}
