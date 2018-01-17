package com.cyc.baseclient.cycobject;

/*
 * #%L
 * File: FormulaSentenceImplTest.java
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
import com.cyc.base.CycAccess;
import com.cyc.base.cycobject.CycConstant;
import com.cyc.base.cycobject.CycList;
import com.cyc.base.cycobject.CycObject;
import com.cyc.base.cycobject.CycSentence;
import com.cyc.base.cycobject.CycVariable;
import com.cyc.base.cycobject.ElMt;
import com.cyc.base.cycobject.FormulaSentence;
import com.cyc.base.cycobject.Fort;
import com.cyc.base.cycobject.Nart;
import com.cyc.base.cycobject.Naut;
import com.cyc.base.cycobject.NonAtomicTerm;
import com.cyc.base.exception.CycApiException;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.base.kbtool.LookupTool;
import com.cyc.baseclient.CommonConstants;
import com.cyc.baseclient.CycObjectFactory;
import com.cyc.baseclient.inference.params.DefaultInferenceParameters;
import com.cyc.baseclient.testing.TestConstants;
import com.cyc.baseclient.testing.TestSentences;
import com.cyc.baseclient.testing.TestUtils;
import com.cyc.kb.ArgPosition;
import com.cyc.session.exception.OpenCycUnsupportedFeatureException;
import com.cyc.session.exception.SessionException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.cyc.baseclient.CommonConstants.BASE_KB;
import static com.cyc.baseclient.CommonConstants.COLLECTION;
import static com.cyc.baseclient.CommonConstants.FOR_ALL;
import static com.cyc.baseclient.CommonConstants.ISA;
import static com.cyc.baseclient.CommonConstants.THERE_EXISTS;
import static com.cyc.baseclient.CommonConstants.THING;
import static com.cyc.baseclient.cycobject.ArgPositionImpl.ARG0;
import static com.cyc.baseclient.cycobject.ArgPositionImpl.ARG1;
import static com.cyc.baseclient.cycobject.ArgPositionImpl.ARG2;
import static com.cyc.baseclient.cycobject.ArgPositionImpl.ARG3;
import static com.cyc.baseclient.cycobject.ArgPositionImpl.TOP;
import static com.cyc.baseclient.cycobject.CycArrayList.makeCycList;
import static com.cyc.baseclient.cycobject.FormulaSentenceImpl.convertIfPromising;
import static com.cyc.baseclient.cycobject.FormulaSentenceImpl.makeConditional;
import static com.cyc.baseclient.cycobject.FormulaSentenceImpl.makeConjunction;
import static com.cyc.baseclient.cycobject.FormulaSentenceImpl.makeDisjunction;
import static com.cyc.baseclient.cycobject.FormulaSentenceImpl.makeFormulaSentence;
import static com.cyc.baseclient.cycobject.FormulaSentenceImpl.makeNegation;
import static com.cyc.baseclient.subl.functions.SublFunctions.INDEXICAL_P;
import static com.cyc.baseclient.testing.TestConstants.DOLLAR;
import static com.cyc.baseclient.testing.TestConstants.VAR_X;
import static com.cyc.baseclient.testing.TestConstants.VAR_Y;
import static com.cyc.baseclient.testing.TestConstants.X;
import static com.cyc.baseclient.testing.TestSentences.genlsWilliamHenryHarrisonBLO_STRING;
import static com.cyc.baseclient.testing.TestSentences.isaThingThing;
import static com.cyc.baseclient.testing.TestSentences.isaWilliamHenryHarrisonBLO_STRING;
import static com.cyc.baseclient.testing.TestUtils.assumeNotOpenCyc;
import static com.cyc.baseclient.testing.TestUtils.getCyc;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

// FIXME: TestSentences - nwinant
/**
 *
 * @author daves
 */
public class FormulaSentenceImplTest {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(FormulaSentenceImplTest.class);

  private static final String UNIVERSE_DATA_MT = TestConstants.UNIVERSE_DATA_MT.cyclify();
  private static final String SUGGESTION_FOR_PRED_RELATIVE_TO_ISA_IN_ARG 
          = TestConstants.SUGGESTION_FOR_PRED_RELATIVE_TO_ISA_IN_ARG.cyclify();
  private static final String CELESTIAL_BODY = TestConstants.CELESTIAL_BODY.cyclify();
  private static final String WEIGHT_ON_PLANET = TestConstants.WEIGHT_ON_PLANET.cyclify();
  private static final String PLANET_MARS = TestConstants.PLANET_MARS.cyclify();
  private static final String PLANET_VENUS = TestConstants.PLANET_VENUS.cyclify();
  
  private static final String US_DOLLAR_FN_STR = TestConstants.DOLLAR.cyclify();
  private static final String AND_STR = CommonConstants.AND.stringApiValue();
  private static final String PERSON_STR = TestConstants.PERSON.stringApiValue();
  private static final String ISA_STR = CommonConstants.ISA.stringApiValue();
  private static final String LIKES_AS_FRIEND_STR = TestConstants.LIKES_AS_FRIEND.cyclify();
  private static final String DOG_STR = TestConstants.DOG.cyclify();
  private static final String IMPLIES_STR = CommonConstants.IMPLIES.cyclify();
  
  public FormulaSentenceImplTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @Before
  public void setUp() throws CycConnectionException, SessionException {
    TestUtils.ensureTestEnvironmentInitialized();
  }

  @After
  public void tearDown() {
  }

  /**
   * Tests FormulaImpl functionality.
   * @throws com.cyc.base.exception.CycConnectionException
   */
  @Test
  public void testCycFormula() throws CycConnectionException {
    System.out.println("\n*** testCycFormula ***");
    final FormulaSentence isaXThing = FormulaSentenceImpl.makeFormulaSentence(
            ISA, VAR_X, THING);
    assertEquals(isaXThing.getFirstArgPositionForTerm(isaXThing),
            ArgPositionImpl.TOP);
    final Set<CycConstantImpl> gatheredConstants = isaXThing.treeGather(
            CycConstantImpl.class);
    assertEquals(2, gatheredConstants.size());
    assertTrue(isaXThing.treeContains(isaXThing));
    assertTrue(isaXThing.treeContains(ISA));
    assertTrue(isaXThing.treeContains(VAR_X));
    assertTrue(isaXThing.treeContains(THING));
    assertTrue(isaXThing.contains(ISA));
    assertTrue(isaXThing.contains(VAR_X));
    assertTrue(isaXThing.contains(THING));
    final FormulaSentence someXIsaThing = FormulaSentenceImpl.makeFormulaSentence(
            THERE_EXISTS, VAR_X, isaXThing);
    assertTrue(someXIsaThing.contains(isaXThing));
    assertTrue(someXIsaThing.treeContains(isaXThing));
    assertTrue(someXIsaThing.treeContains(ISA));
    assertTrue(someXIsaThing.treeContains(VAR_X));
    assertTrue(someXIsaThing.treeContains(THING));
    assertEquals(ARG1, isaXThing.getFirstArgPositionForTerm(VAR_X));
    assertEquals(new ArgPositionImpl(2, 2),
            someXIsaThing.getFirstArgPositionForTerm(THING));
    {
      assertEquals(new ArrayList(Arrays.asList(VAR_X)),
              new ArrayList(isaXThing.findFreeVariables()));
      assertTrue(someXIsaThing.findFreeVariables().isEmpty());
      final FormulaSentence everyXIsaThing = FormulaSentenceImpl.makeFormulaSentence(
              FOR_ALL, VAR_X, isaXThing);
      assertTrue(everyXIsaThing.findFreeVariables().isEmpty());
      final FormulaSentence isaXY = FormulaSentenceImpl.makeFormulaSentence(
              ISA, VAR_X, VAR_Y);
      assertEquals(new ArrayList(Arrays.asList(VAR_X, VAR_Y)),
              new ArrayList(isaXY.findFreeVariables()));
      final FormulaSentence conj = FormulaSentenceImpl.makeConjunction(
              isaXThing, isaXY);
      assertEquals(new ArrayList(Arrays.asList(VAR_X, VAR_Y)),
              new ArrayList(conj.findFreeVariables()));
    }
    { //getSpecifiedObject()
      FormulaImpl formula2 = new FormulaImpl(getCyc().getObjectTool().makeCycList(
              "(1 (2 3 (4)) 5)"));
      ArgPositionImpl pathSpecification = ARG0;
      Object obj = formula2.getSpecifiedObject(pathSpecification);
      Object expectedObj = 1;
      assertEquals(expectedObj, obj);

      pathSpecification = ARG1;
      obj = formula2.getSpecifiedObject(pathSpecification);
      expectedObj = formula2.getArg1();
      assertEquals(expectedObj, obj);

      pathSpecification = ARG2;
      obj = formula2.getSpecifiedObject(pathSpecification);
      expectedObj = formula2.getArg2();
      assertEquals(expectedObj, obj);

      pathSpecification = new ArgPositionImpl(1, 2, 0);
      obj = formula2.getSpecifiedObject(pathSpecification);
      expectedObj = 4;
      assertEquals(expectedObj, obj);

      // setSpecifedObject
      pathSpecification = ARG0;
      formula2.setSpecifiedObject(pathSpecification, "a");
      expectedObj = new FormulaImpl(getCyc().getObjectTool().makeCycList("(\"a\" (2 3 (4)) 5)"));
      assertEquals(expectedObj, formula2);

      pathSpecification = ARG2;
      formula2.setSpecifiedObject(pathSpecification, "b");
      expectedObj = new FormulaImpl(getCyc().getObjectTool().makeCycList(
              "(\"a\" (2 3 (4)) \"b\")"));
      assertEquals(expectedObj, formula2);

      pathSpecification = new ArgPositionImpl(1, 2, 0);
      formula2.setSpecifiedObject(pathSpecification, "c");
      expectedObj = new FormulaImpl(getCyc().getObjectTool().makeCycList(
              "(\"a\" (2 3 (\"c\")) \"b\")"));
      assertEquals(expectedObj, formula2);

      try {
        Naut cycNat = new NautImpl(DOLLAR, 1);
        formula2.addArg(cycNat);
        expectedObj = new FormulaImpl(getCyc().getObjectTool().canonicalizeList(getCyc().getObjectTool().makeCycList(
                "(\"a\" (2 3 (\"c\")) \"b\" (" + US_DOLLAR_FN_STR + " 2))")));
        pathSpecification = new ArgPositionImpl(3, 1);
        formula2.setSpecifiedObject(pathSpecification, 2);
        assertEquals(expectedObj, formula2);
        formula2.setSpecifiedObject(TOP, cycNat.getFormula());
        assertEquals(cycNat.getFormula().getArgs(), formula2.getArgs());
      } catch (CycConnectionException | CycApiException ex) {
        LOGGER.error(ex.getMessage(), ex);
      }

      // test getArgPositionsForTerm

      FormulaImpl list = new FormulaImpl(
              makeCycList(makeCycList("c", "1", "2"), "a", "b", "c",
              makeCycList("a", makeCycList("c", "10", "11"), "c", "2")));
      assertEquals(
              "((\"c\" \"1\" \"2\") \"a\" \"b\" \"c\" (\"a\" (\"c\" \"10\" \"11\") \"c\" \"2\"))",
              list.toString());
      Set<ArgPosition> result = list.getArgPositionsForTerm("a");
      assertEquals(new HashSet(Arrays.asList(ARG1,
              new ArgPositionImpl(4, 0))), result);
      Set<ArgPosition> result1 = list.getArgPositionsForTerm("c");
      assertEquals(new HashSet(Arrays.asList(new ArgPositionImpl(0, 0),
              ARG3,
              new ArgPositionImpl(4, 1, 0), new ArgPositionImpl(4, 2))), result1);
      ArgPosition result2 = list.getFirstArgPositionForTerm("d");
      assertEquals(null, result2);
    }
    System.out.println("*** testCycFormula OK ***");
  }
  
  /**
   * Test of makeFormulaSentence method, of class FormulaSentenceImpl.
   */
  @Test
  public void testMakeCycFormulaSentence() {
    System.out.println("makeCycFormulaSentence");
    makeFormulaSentence(ISA, THING, THING);
  }

  /**
   * Test of makeConjunction method, of class FormulaSentenceImpl.
   */
  @Test
  public void testMakeConjunction_CycFormulaSentenceArr() {
    System.out.println("makeConjunction");
    assertTrue(makeConjunction(isaThingThing, isaThingThing).isConjunction());
  }

  /**
   * Test of makeConjunction method, of class FormulaSentenceImpl.
   */
  @Test
  public void testMakeConjunction_Iterable() {
    System.out.println("makeConjunction");
    assertTrue(
            makeConjunction(Arrays.asList(isaThingThing, isaThingThing)).isConjunction());
  }

  /**
   * Test of makeDisjunction method, of class FormulaSentenceImpl.
   */
  @Test
  public void testMakeDisjunction() {
    System.out.println("makeDisjunction");
    makeDisjunction(Arrays.asList(isaThingThing, isaThingThing));
  }

  /**
   * Test of makeNegation method, of class FormulaSentenceImpl.
   */
  @Test
  public void testMakeNegation() {
    System.out.println("makeNegation");
    assertTrue(makeNegation(isaThingThing).isNegated());
  }

  /**
   * Test of convertIfPromising method, of class FormulaSentenceImpl.
   */
  @Test
  public void testConvertIfPromising() {
    System.out.println("convertIfPromising");
    final Object result = convertIfPromising(CycArrayList.makeCycList(ISA, THING, THING));
    assertTrue(result instanceof FormulaSentenceImpl);
  }

  /**
   * Test of isConditionalSentence method, of class FormulaSentenceImpl.
   */
  @Test
  public void testIsConditionalSentence() throws CycConnectionException {
    System.out.println("\n*** testIsConditionalSentence ***");
    assertTrue(makeConditional(isaThingThing, isaThingThing).isConditionalSentence());
    
    final FormulaSentence sentence1 = getCyc().getObjectTool()
            .makeCycSentence("(" + LIKES_AS_FRIEND_STR + " ?X ?Y)");
    assertFalse(sentence1.isConditionalSentence());
    
    final FormulaSentence sentence2 = getCyc().getObjectTool().makeCycSentence(
            "(" + IMPLIES_STR
                    + " (" + LIKES_AS_FRIEND_STR + " ?X ?Y)"
                    + " (" + ISA_STR + " ?X " + DOG_STR + "))");
    assertTrue(sentence2.isConditionalSentence());
    
    System.out.println("*** testIsConditionalSentence OK ***");
  }
  
  /**
   * Test of isConjunction method, of class FormulaSentenceImpl.
   */
  @Test
  public void testIsConjunction() {
    System.out.println("isConjunction");
    assertTrue(makeConjunction(isaThingThing).isConjunction());
  }

  /**
   * Test of isLogicalConnectorSentence method, of class FormulaSentenceImpl.
   */
  @Test
  public void testIsLogicalConnectorSentence() {
    System.out.println("isLogicalConnectorSentence");
    assertTrue(makeConjunction(isaThingThing).isLogicalConnectorSentence());
  }

  /**
   * Test of isExistential method, of class FormulaSentenceImpl.
   */
  @Test
  public void testIsExistential() {
    System.out.println("isExistential");
    final FormulaSentence sentence = makeFormulaSentence(ISA, X, THING);
    assertFalse(sentence.isExistential());
    sentence.existentiallyBind(X);
    assertTrue(sentence.isExistential());
  }

  /**
   * Test of isUniversal method, of class FormulaSentenceImpl.
   */
  @Test
  public void testIsUniversal() {
    System.out.println("isUniversal");
    assertFalse(isaThingThing.isUniversal());
  }

  /**
   * Test of getOptimizedVarNames method, of class FormulaSentenceImpl.
   *
   * @throws com.cyc.base.exception.CycConnectionException
   */
  @Test
  public void testGetOptimizedVarNames() throws CycConnectionException {
    // TODO: could this be re-enabled for OpenCyc? - nwinant, 2015-06-08
    System.out.println("getOptimizedVarNames");
    assumeNotOpenCyc();//
    final FormulaSentence sentence = makeFormulaSentence(ISA, X,
            new CycConstantImpl("SoccerBall",
                    new GuidImpl("bd58b0dd-9c29-11b1-9dad-c379636f7270")));
    Map result = sentence.getOptimizedVarNames(getCyc());
    assertTrue(result.containsKey(X));
  }

  /**
   * Test of getSimplifiedSentence method, of class FormulaSentenceImpl.
   *
   * @throws com.cyc.base.exception.CycConnectionException
   * @throws com.cyc.session.exception.OpenCycUnsupportedFeatureException
   */
  @Test
  public void testGetSimplifiedSentence_CycAccess() throws CycConnectionException, OpenCycUnsupportedFeatureException {
    System.out.println("getSimplifiedSentence");
    assumeNotOpenCyc();
    final FormulaSentence sentence = makeFormulaSentence(ISA, X,
            COLLECTION);
    assertEquals(sentence, makeConjunction(sentence).getSimplifiedSentence(
            getCyc()));
  }

  /**
   * Test of getSimplifiedSentence method, of class FormulaSentenceImpl.
   *
   * @throws com.cyc.base.exception.CycConnectionException
   * @throws com.cyc.session.exception.OpenCycUnsupportedFeatureException
   */
  @Test
  public void testGetSimplifiedSentence_CycAccess_ElMt() throws CycConnectionException, OpenCycUnsupportedFeatureException {
    System.out.println("getSimplifiedSentence");
    assumeNotOpenCyc();
    final FormulaSentence sentence = makeFormulaSentence(ISA, X,
            COLLECTION);
    assertEquals(sentence, makeConjunction(sentence).getSimplifiedSentence(
            getCyc(), BASE_KB));
  }

  /**
   * Test of getNonWffAssertExplanation method, of class FormulaSentenceImpl.
   *
   * @throws com.cyc.base.exception.CycConnectionException
   */
  @Test
  public void testGetNonWffAssertExplanation_CycAccess() throws CycConnectionException {
    System.out.println("getNonWffAssertExplanation");
    makeFormulaSentence(getCyc(), genlsWilliamHenryHarrisonBLO_STRING).getNonWffAssertExplanation(
            getCyc());
  }

  /**
   * Test of getNonWffAssertExplanation method, of class FormulaSentenceImpl.
   * @throws com.cyc.base.exception.CycConnectionException
   */
  @Test
  public void testGetNonWffAssertExplanation_CycAccess_ElMt() throws CycConnectionException {
    System.out.println("getNonWffAssertExplanation");
    makeFormulaSentence(getCyc(), genlsWilliamHenryHarrisonBLO_STRING).getNonWffAssertExplanation(
            getCyc(), BASE_KB);
  }

  /**
   * Test of getNonWffExplanation method, of class FormulaSentenceImpl.
   * @throws com.cyc.base.exception.CycConnectionException
   */
  @Test
  public void testGetNonWffExplanation_CycAccess() throws CycConnectionException {
    System.out.println("getNonWffExplanation");
    makeFormulaSentence(getCyc(), genlsWilliamHenryHarrisonBLO_STRING).getNonWffExplanation(
            getCyc());
  }

  /**
   * Test of getNonWffExplanation method, of class FormulaSentenceImpl.
   * @throws com.cyc.base.exception.CycConnectionException
   */
  @Test
  public void testGetNonWffExplanation_CycAccess_ElMt() throws CycApiException, CycConnectionException {
    System.out.println("getNonWffExplanation");
    makeFormulaSentence(getCyc(), genlsWilliamHenryHarrisonBLO_STRING).getNonWffExplanation(
            getCyc(), BASE_KB);
  }

  /**
   * Test of deepCopy method, of class FormulaSentenceImpl.
   */
  @Test
  public void testDeepCopy() {
    System.out.println("deepCopy");
    final FormulaSentence sentence = makeFormulaSentence(ISA, X,
            COLLECTION);
    assertEquals(sentence.deepCopy(), sentence);
  }

  /**
   * Test of substituteNonDestructive method, of class FormulaSentenceImpl.
   */
  @Test
  public void testSubstituteNonDestructive() throws CycConnectionException {
    System.out.println("\n*** testSubstituteNonDestructive ***");
    final FormulaSentence sentence1 = makeFormulaSentence(ISA, X, COLLECTION);
    assertEquals(makeFormulaSentence(ISA, THING, COLLECTION),
            sentence1.substituteNonDestructive(X, THING));
    assertEquals(makeFormulaSentence(ISA, X, COLLECTION), sentence1);
    
    final FormulaSentence origSentence = getCyc().getObjectTool()
            .makeCycSentence("(" + LIKES_AS_FRIEND_STR + " ?X ?Y)");
    final FormulaSentence newSentence = origSentence.substituteNonDestructive(
            CycObjectFactory.makeCycVariable("X"),
            CycObjectFactory.makeCycVariable("Z"));
    assertFalse(origSentence.equals(newSentence));
    assertFalse(origSentence.equals(getCyc().getObjectTool()
            .makeCycSentence("(" + LIKES_AS_FRIEND_STR + " ?Z ?Y)")));
    assertTrue(newSentence.equals(getCyc().getObjectTool()
            .makeCycSentence("(" + LIKES_AS_FRIEND_STR + " ?Z ?Y)")));
    System.out.println("*** testSubstituteNonDestructive OK ***");
  }
  
  /**
   * Test of substituteDestructive method, of class FormulaSentenceImpl.
   * 
   * @throws com.cyc.base.exception.CycConnectionException
   */
  @Test
  public void testSubstituteDestructive() throws CycConnectionException {
    System.out.println("\n*** testSubstituteDestructive ***");
    final FormulaSentence sentence1 = makeFormulaSentence(ISA, X, COLLECTION);
    sentence1.substituteDestructive(X, THING);
    assertEquals(makeFormulaSentence(ISA, THING, COLLECTION), sentence1);

    final FormulaSentence sentence2 = getCyc().getObjectTool()
            .makeCycSentence("(" + LIKES_AS_FRIEND_STR + " ?X ?Y)");
    sentence2.substituteDestructive(
            CycObjectFactory.makeCycVariable("X"),
            CycObjectFactory.makeCycVariable("Z"));
    assertTrue(sentence2.equalsAtEL(getCyc().getObjectTool()
            .makeCycSentence("(" + LIKES_AS_FRIEND_STR + " ?Z ?Y)")));
    System.out.println("*** testSubstituteDestructive OK ***");
  }
  
  /**
   * Test of treeSubstitute method, of class FormulaSentenceImpl.
   * 
   * @throws com.cyc.base.exception.CycConnectionException
   */
  @Test
  public void testTreeSubstitute() throws CycConnectionException {
    System.out.println("treeSubstitute");
    final CycAccess cyc = getCyc();
    final LookupTool lookupTool = cyc.getLookupTool();
    Fort theDog = lookupTool.getKnownFortByName(TestSentences.THE_DOG_STRING);
    Map<CycObject, Object> substitutions = new HashMap<>();

    substitutions.put(theDog, X);

    FormulaSentence sentence = cyc.getObjectTool().makeCyclifiedSentence(
            TestSentences.ISA_THE_DOG_DOG_STRING);

    FormulaSentence result = sentence.treeSubstitute(cyc, substitutions);
    assertNotEquals(result, sentence);

    System.out.println("...Verifying substitution into a random NART.");
    Nart nart = lookupTool.getRandomNart();
    while (nart.getArity() < 1 || !(nart.getArgument(1) instanceof CycObject)) {
      nart = lookupTool.getRandomNart();
    }
    sentence.setSpecifiedObject(ArgPositionImpl.ARG1, nart);
    assertTrue(sentence.getArg1() instanceof Nart);
    substitutions.clear();
    CycObject natArg1 = (CycObject) nart.getArgument(1);
    CycConstant cat = lookupTool.getKnownConstantByName(TestConstants.CAT.cyclify());
    substitutions.put(natArg1, cat);
    System.out.println("...Substituting " + cat + " for " + natArg1 + " in " + sentence);
    result = sentence.treeSubstitute(cyc, substitutions);
    System.out.println("...Result: " + result);
    assertTrue(cat.equalsAtEL(((NonAtomicTerm) result.getArg1()).getArgument(1)));
  }

  /**
   * Test of clone method, of class FormulaSentenceImpl.
   */
  //@Test
  public void testClone() {
    System.out.println("clone");
    FormulaSentenceImpl instance = null;
    Object expResult = null;
    Object result = instance.clone();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of compareTo method, of class FormulaSentenceImpl.
   */
  //@Test
  public void testCompareTo() {
    System.out.println("compareTo");
    Object o = null;
    FormulaSentenceImpl instance = null;
    int expResult = 0;
    int result = instance.compareTo(o);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of makeFormulaSentence method, of class FormulaSentenceImpl.
   * @throws com.cyc.base.exception.CycConnectionException
   */
  @Test
  public void testMakeCycSentence() throws CycConnectionException {
    System.out.println("makeCycSentence");
    final String isaThingThing = TestSentences.ISA_THING_THING_STRING;
    assertEquals(getCyc().getObjectTool().makeCycSentence(isaThingThing),
            makeFormulaSentence(getCyc(), isaThingThing));
  }

  /**
   * Test of isNegated method, of class FormulaSentenceImpl.
   */
  //@Test
  public void testIsNegated() {
    System.out.println("isNegated");
    FormulaSentenceImpl instance = null;
    boolean expResult = false;
    boolean result = instance.isNegated();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of existentiallyBind method, of class FormulaSentenceImpl.
   */
  //@Test
  public void testExistentiallyBind() {
    System.out.println("existentiallyBind");
    CycVariableImpl var = null;
    FormulaSentenceImpl instance = null;
    instance.existentiallyBind(var);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of existentiallyUnbind method, of class FormulaSentenceImpl.
   */
  //@Test
  public void testExistentiallyUnbind() {
    System.out.println("existentiallyUnbind");
    CycVariableImpl var = null;
    FormulaSentenceImpl instance = null;
    instance.existentiallyUnbind(var);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of splice method, of class FormulaSentenceImpl.
   * @throws com.cyc.base.exception.CycConnectionException
   */
  @Test
  public void testSplice() throws CycConnectionException, OpenCycUnsupportedFeatureException {
    System.out.println("splice");
    assumeNotOpenCyc();
    final FormulaSentence conjunction = FormulaSentenceImpl.makeConjunction(
            isaThingThing);
    final FormulaSentence toInsert = getCyc().getObjectTool().makeCycSentence(
            isaWilliamHenryHarrisonBLO_STRING);
    final FormulaSentence result = conjunction.splice(
            toInsert, ArgPositionImpl.ARG1, getCyc());
    assertTrue(result.treeContains(isaThingThing));
    assertTrue(result.treeContains(toInsert));
  }

  /**
   * Test of getCandidateReplacements method, of class FormulaSentenceImpl.
   *
   * @throws com.cyc.base.exception.CycConnectionException
   * @throws com.cyc.session.exception.OpenCycUnsupportedFeatureException
   */
  @Test
  public void testGetCandidateReplacements() throws CycConnectionException, OpenCycUnsupportedFeatureException {
    System.out.println("getCandidateReplacements");
    assumeNotOpenCyc();
    final CycConstant universeDataMt = getCyc().getLookupTool().getKnownConstantByName(
            UNIVERSE_DATA_MT);
    final FormulaSentence suggestionSentence
            = getCyc().getObjectTool().makeCycSentence(
                    "(" + SUGGESTION_FOR_PRED_RELATIVE_TO_ISA_IN_ARG + " " + WEIGHT_ON_PLANET
                    + " " + CELESTIAL_BODY + " 2 " + CELESTIAL_BODY + " 2)");
    final boolean suggestionKnown = getCyc().getInferenceTool().isQueryTrue(
            suggestionSentence, universeDataMt,
            new DefaultInferenceParameters(
                    getCyc()));
    if (!suggestionKnown) {
      getCyc().getAssertTool().assertGaf(suggestionSentence, universeDataMt);
    }
    try {
      Collection result = getCyc().getObjectTool().makeCycSentence(
              "(" + WEIGHT_ON_PLANET + " ?ME " + PLANET_MARS + ")").getCandidateReplacements(
                      ArgPositionImpl.ARG2,
                      ElMtConstantImpl.makeElMtConstant(universeDataMt), getCyc());
      assertFalse(result.isEmpty());
      assertTrue(
              result.contains(getCyc().getLookupTool().getKnownConstantByName(PLANET_VENUS)));
    } finally {
      if (!suggestionKnown) {
        getCyc().getUnassertTool().unassertGaf(suggestionSentence, universeDataMt);
      }
    }
  }

  /**
   * Test of isValidReplacement method, of class FormulaSentenceImpl.
   * @throws com.cyc.base.exception.CycConnectionException
   */
  @Test
  public void testIsValidReplacement() throws CycConnectionException {
    System.out.println("isValidReplacement");
    Object isa = isaThingThing.getArg0();
    assertFalse(isaThingThing.isValidReplacement(ArgPositionImpl.ARG2, isa,
            BASE_KB, getCyc()));
    assertTrue(isaThingThing.isValidReplacement(ArgPositionImpl.ARG1, isa,
            BASE_KB, getCyc()));
  }

  /**
   * Test of getEqualsFoldedSentence method, of class FormulaSentenceImpl.
   * @throws com.cyc.base.exception.CycConnectionException
   */
  //@Test
  public void testGetEqualsFoldedSentence_CycAccess() throws CycConnectionException {
    System.out.println("getEqualsFoldedSentence");
    CycAccess access = null;
    FormulaSentenceImpl instance = null;
    CycSentence expResult = null;
    CycSentence result = instance.getEqualsFoldedSentence(access);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getEqualsFoldedSentence method, of class FormulaSentenceImpl.
   * @throws com.cyc.base.exception.CycConnectionException
   */
  //@Test
  public void testGetEqualsFoldedSentence_CycAccess_ElMt() throws CycConnectionException {
    System.out.println("getEqualsFoldedSentence");
    CycAccess access = null;
    ElMt mt = null;
    FormulaSentenceImpl instance = null;
    CycSentence expResult = null;
    CycSentence result = instance.getEqualsFoldedSentence(access, mt);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getExpandedSentence method, of class FormulaSentenceImpl.
   * @throws com.cyc.base.exception.CycConnectionException
   */
  //@Test
  public void testGetExpandedSentence_CycAccess() throws CycConnectionException {
    System.out.println("getExpandedSentence");
    CycAccess access = null;
    FormulaSentenceImpl instance = null;
    FormulaSentenceImpl expResult = null;
    FormulaSentenceImpl result = instance.getExpandedSentence(access);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getExpandedSentence method, of class FormulaSentenceImpl.
   * @throws com.cyc.base.exception.CycConnectionException
   */
  //@Test
  public void testGetExpandedSentence_CycAccess_ElMt() throws CycConnectionException {
    System.out.println("getExpandedSentence");
    CycAccess access = null;
    ElMt mt = null;
    FormulaSentenceImpl instance = null;
    FormulaSentenceImpl expResult = null;
    FormulaSentenceImpl result = instance.getExpandedSentence(access, mt);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getCanonicalElSentence method, of class FormulaSentenceImpl.
   * @throws com.cyc.base.exception.CycConnectionException
   */
  //@Test
  public void testGetCanonicalElSentence_CycAccess() throws CycConnectionException {
    System.out.println("getCanonicalElSentence");
    CycAccess access = null;
    FormulaSentenceImpl instance = null;
    FormulaSentenceImpl expResult = null;
    FormulaSentenceImpl result = instance.getCanonicalElSentence(access);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getCanonicalElSentence method, of class FormulaSentenceImpl.
   * @throws com.cyc.base.exception.CycConnectionException
   */
  //@Test
  public void testGetCanonicalElSentence_CycAccess_Boolean() throws CycConnectionException {
    System.out.println("getCanonicalElSentence");
    CycAccess access = null;
    Boolean canonicalizeVars = null;
    FormulaSentenceImpl instance = null;
    FormulaSentenceImpl expResult = null;
    FormulaSentenceImpl result = instance.getCanonicalElSentence(access,
            canonicalizeVars);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getCanonicalElSentence method, of class FormulaSentenceImpl.
   * @throws com.cyc.base.exception.CycConnectionException
   */
  //@Test
  public void testGetCanonicalElSentence_3args() throws CycConnectionException {
    System.out.println("getCanonicalElSentence");
    CycAccess access = null;
    ElMt mt = null;
    Boolean canonicalizeVars = null;
    FormulaSentenceImpl instance = null;
    FormulaSentenceImpl expResult = null;
    FormulaSentenceImpl result = instance.getCanonicalElSentence(access, mt,
            canonicalizeVars);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }
  
  /**
   * Test of hasWffConstraintViolations method, of class FormulaSentenceImpl.
   */
  //@Test
  public void testHasWffConstraintViolations() {
    System.out.println("hasWffConstraintViolations");
    CycAccess access = null;
    ElMt mt = null;
    FormulaSentenceImpl instance = null;
    boolean expResult = false;
    boolean result = instance.hasWffConstraintViolations(access, mt);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }
  
  @Test
  public void testFindIndexicals() throws CycApiException, CycConnectionException {
    final String sentenceStr
            = "(and \n"
            + "  (TheNamedFn SetOrCollection \"indexical 1\") \n"
            + "  #$Dog \n"
            + "  (or \n"
            + "    (TheNamedFn SetOrCollection \"indexical 2\") \n"
            + "    #$Now \n"
            + "    ?VAR1) \n"
            + "  (TheNamedFn SetOrCollection \"indexical 3\") \n"
            + "  (TheNamedFn SetOrCollection \"indexical 2\") \n"
            + "  :KEY"
            + "  \"Some random string\""
            + "  ?VAR2)";
    final FormulaSentence sentence = FormulaSentenceImpl.makeFormulaSentence(getCyc(), sentenceStr);
    final CycList results = sentence.findIndexicals(getCyc());
    System.out.println("--");
    System.out.println("Results:");
    System.out.println(results.toPrettyString("  "));
    System.out.println("--");
    assertNotNull(results);
    System.out.println("Indexicals (" + results.size() + "):");
    assertEquals(5, results.size());
    for (Object indexical : results) {
      System.out.println("- " + indexical);
      assertTrue(indexical instanceof CycObject);
      assertTrue(INDEXICAL_P.eval(getCyc(), (CycObject) indexical));
    }
  }
  
  @Test
  public void testVariableNameOptimization() throws CycConnectionException {
    System.out.println("\n*** testVariableNameOptimization ***");
    assumeNotOpenCyc();
    FormulaSentence sentence = getCyc().getObjectTool().makeCycSentence(
            "(" + LIKES_AS_FRIEND_STR + " ?X ?Y)");
    Map<CycVariable, String> varMap = sentence.getOptimizedVarNames(getCyc());
    assertEquals(2, varMap.size());
    System.out.println("*** testVariableNameOptimization OK ***");
  }
  
  @Test
  public void testEqualsAtEL() throws CycConnectionException {
    System.out.println("\n*** testEqualsAtEL ***");
    final FormulaSentence sentence1 = getCyc().getObjectTool().makeCycSentence(
            "(" + LIKES_AS_FRIEND_STR + " ?X ?Y)");
    assertTrue(sentence1.equalsAtEL(getCyc().getObjectTool().makeCycSentence(
            "(" + LIKES_AS_FRIEND_STR + " ?Z ?Y)")));
    final FormulaSentence sentence2 = getCyc().getObjectTool().makeCycSentence(
            "(" + AND_STR + "(" + ISA + " ?X " + PERSON_STR + ") (" + LIKES_AS_FRIEND_STR + " ?X ?Y))");
    assertTrue(sentence2.equalsAtEL(getCyc().getObjectTool().makeCycSentence(
            "(" + AND_STR + "(" + ISA + " ?Z " + PERSON_STR + ") (" + LIKES_AS_FRIEND_STR + " ?Z ?Y))")));
    final FormulaSentence sentence3 = getCyc().getObjectTool().makeCycSentence(
            "(" + AND_STR + "(" + ISA + " ?Y " + PERSON_STR + ") (" + LIKES_AS_FRIEND_STR + " ?X ?Y))");
    assertFalse(sentence3.equalsAtEL(getCyc().getObjectTool().makeCycSentence(
            "(" + AND_STR + "(" + ISA + " ?X " + PERSON_STR + ") (" + LIKES_AS_FRIEND_STR + " ?X ?Y))")));
    System.out.println("*** testEqualsAtEL OK ***");
  }
  
  @Test
  public void testHashCode() throws CycConnectionException {
    final CycAccess access = TestUtils.getCyc();
    final String str1 = "(moleculeStateToState-OneWay (TheList (ProteinMoleculeTypeStateFn RasProtein (BindingSiteWithNameAndPStateFn \"S1S2\" UnspecifiedWRTPhosphorylation-Site 1)) (ProteinMoleculeTypeStateFn RafKinase (BindingSiteWithNameAndPStateFn \"x\" PhosphorylatedSite 1))) (TheList (ProteinMoleculeTypeStateFn RasProtein (BindingSiteWithNameAndPStateFn \"S1S2\" UnspecifiedWRTPhosphorylation-Site 1)) (ProteinMoleculeTypeStateFn RafKinase (BindingSiteWithNameAndPStateFn \"x\" UnphosphorylatedSite 1))))";
    final String str2 = "(moleculeStateToState-OneWay (TheList (ProteinMoleculeTypeStateFn RasProtein (BindingSiteWithNameAndPStateFn \"S1S2\" UnspecifiedWRTPhosphorylation-Site 1)) (ProteinMoleculeTypeStateFn RafKinase (BindingSiteWithNameAndPStateFn \"x\" UnphosphorylatedSite 1))) (TheList (ProteinMoleculeTypeStateFn RasProtein (BindingSiteWithNameAndPStateFn \"S1S2\" UnspecifiedWRTPhosphorylation-Site 1)) (ProteinMoleculeTypeStateFn RafKinase (BindingSiteWithNameAndPStateFn \"x\" PhosphorylatedSite 1))))";
    final FormulaSentence f1 = FormulaSentenceImpl.makeFormulaSentence(access, str1);
    final FormulaSentence f2 = FormulaSentenceImpl.makeFormulaSentence(access, str2);
    /*
     System.out.println("f1 arg0: " + f1.getArg0().getClass().getName() + " ... " + f1.getArg0() + ": " + f1.getArg0().hashCode());
     System.out.println("f1 arg1: " + f1.getArg1().getClass().getName() + " ... " + f1.getArg1() + ": " + f1.getArg1().hashCode());
     System.out.println("f1 arg2: " + f1.getArg2().getClass().getName() + " ... " + f1.getArg2() + ": " + f1.getArg2().hashCode());
     System.out.println("f2 arg0: " + f2.getArg0().getClass().getName() + " ... " + f2.getArg0() + ": " + f2.getArg0().hashCode());
     System.out.println("f2 arg1: " + f2.getArg1().getClass().getName() + " ... " + f2.getArg1() + ": " + f2.getArg1().hashCode());
     System.out.println("f2 arg2: " + f2.getArg2().getClass().getName() + " ... " + f2.getArg2() + ": " + f2.getArg2().hashCode());
     System.out.println("f1 hash: " + f1.hashCode() + " ! " + f1.getClass().getName());
     System.out.println("f2 hash: " + f2.hashCode() + " ! " + f2.getClass().getName());
     */
    assertNotEquals(f1.hashCode(), f2.hashCode());
  }

}
