package com.cyc.kb.client;

/*
 * #%L
 * File: TestConstants.java
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

import com.cyc.base.CycAccess;
import com.cyc.base.CycAccessManager;
import com.cyc.base.cycobject.ElMt;
import com.cyc.kb.Assertion;
import com.cyc.kb.BinaryPredicate;
import com.cyc.kb.Context;
import com.cyc.kb.Fact;
import com.cyc.kb.FirstOrderCollection;
import com.cyc.kb.KbCollection;
import com.cyc.kb.KbFunction;
import com.cyc.kb.KbIndividual;
import com.cyc.kb.KbTerm;
import com.cyc.kb.Rule;
import com.cyc.kb.Sentence;
import com.cyc.kb.Variable;
import com.cyc.kb.client.config.KbConfiguration;
import com.cyc.kb.client.config.KbDefaultContext;
import com.cyc.kb.exception.KbException;
import com.cyc.kb.exception.KbRuntimeException;
import com.cyc.session.exception.SessionCommunicationException;
import com.cyc.session.exception.SessionConfigurationException;
import com.cyc.session.exception.SessionException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.cyc.Cyc.Constants.BASE_KB;
import static com.cyc.Cyc.Constants.UV_MT;


/**
 *
 * @author vijay
 */
public class TestConstants {
  
  public static Context baseKB;
  public static Context universalVocabularyMt;
  public static Rule flyingRule;
  public static KbTestConstants kbapitc;
  public static Context appleProductMt;
  public static KbCollection product;

  private static Logger LOG = LoggerFactory.getLogger(TestConstants.class.getName());
  
  public static CycAccess getCyc() {
    try {
      return CycAccessManager.getCurrentAccess();
    } catch (SessionException ex) {
      throw KbRuntimeException.fromThrowable(ex.getMessage(), ex);
    }
  }
  
  public static void configureCurrentSession() throws SessionCommunicationException, SessionConfigurationException {
    KbConfiguration.getOptions().setShouldTranscriptOperations(false);
    // Example usage of KBAPIConfiguration methods
    //KBAPIConfiguration.setCurrentCyclist("(#$UserOfProgramFn #$OWLImporter-Cyc #$ChrisDeaton)");
    KbConfiguration.getOptions().setCyclistName("TheUser");
    //KBAPIConfiguration.setProject(KBIndividualImpl.get("ThePurpose"));
    KbConfiguration.getOptions().setDefaultContext(new KbDefaultContext(Constants.uvMt(), Constants.inferencePSCMt()));
  }
  
  public static void ensureInitialized() throws Exception {
    try {
      LOG.info("Setting up...");
      if (TestConstants.baseKB == null) {
        configureCurrentSession();
      }
      universalVocabularyMt = new ImmutableContext("UniversalVocabularyMt");
      baseKB = new ImmutableContext("BaseKB");

      kbapitc = KbTestConstants.getInstance();
      appleProductMt  = Context.findOrCreate("AppleProductMt");
      product = KbCollection.get("Product");
      setupOEScript();
    } catch (Exception ex) {
      ex.printStackTrace(System.err);
      throw ex;
    }
  }
  
  private static void setupOEScript() throws KbException, ParseException {
    
    LOG.info("Entering the setupOEScript");
    
    KbIndividual i = KbIndividualImpl.findOrCreate("TestIndividual001");
    //i.instantiates("(#$CitizenFn #$UnitedStatesOfAmerica)", "UniversalVocabularyMt");
    i.instantiates(KbCollection.get("(#$CitizenFn #$UnitedStatesOfAmerica)"), UV_MT);
    KbCollection nonSense = KbCollectionImpl.findOrCreate("Nonsense");
    
    KbIndividual airline = KbIndividualImpl.findOrCreate("SomeAirline");
    airline.instantiates(KbCollectionImpl.get("AirlineCompany"), Constants.uvMt());
    // Create a temporary CycKBSubsetCollection to gather the newly created constants
    // Demonstrate the use of quote, quotedIsa
    //KBCollection cycSubsetCol = KBCollectionImpl.get("CycKBSubsetCollection");
    //FirstOrderCollection tempTestSubset = FirstOrderCollectionImpl.findOrCreate("ApiTestingSubset", cycSubsetCol, Constants.uvMt());
    
    FirstOrderCollection flying1Col = FirstOrderCollectionImpl.findOrCreate("FlyingAnObject-Operate");
    flying1Col.addGeneralization(KbCollectionImpl.get("ControllingSomething"), Constants.uvMt()); 
    // Change this to OperatingSomething, where we are operating a PhysicalDevice
    
    SecondOrderCollectionImpl flyingType = SecondOrderCollectionImpl.findOrCreate("FlyingTypeByTypeOfObjectFlown");
    KbCollection cPlane = KbCollectionImpl.get("CommercialAircraft");
    
    KbFunction flying1Fun = KbFunctionImpl.findOrCreate("FlyingATypeOfObject-Operate-Fn", KbCollectionImpl.get("ReifiableFunction"));
    flying1Fun.addArgGenl(1, KbCollectionImpl.get("PhysicalDevice"), Constants.uvMt());
    flying1Fun.addResultGenl(flying1Col, Constants.uvMt());
    flying1Fun.addResultIsa(flyingType, Constants.uvMt());
    flying1Fun.addResultIsa((KbCollection)FirstOrderCollectionImpl.getClassType(), Constants.uvMt());
    flying1Fun.setArity(1);
    
    KbTerm flyingAPlaneTemp = flying1Fun.findOrCreateFunctionalTerm(KbTermImpl.class, cPlane);
    flyingAPlaneTemp.instantiates((KbCollection)FirstOrderCollectionImpl.getClassType());
    FirstOrderCollection flyingAPlane = flying1Fun.findOrCreateFunctionalTerm(FirstOrderCollectionImpl.class, cPlane);
    
    BinaryPredicate flyingAnObject_operate = BinaryPredicateImpl.findOrCreate("flyingAnObject-Operate-Predicate", KbCollectionImpl.get("ActorSlot"));
    flyingAnObject_operate.addArgIsa(1, flying1Col, Constants.uvMt());
    // flying1Pred1.addArgIsa(2, KBCollection.get("PhysicalDevice"), Constants.uvMt());
    SentenceImpl sargisa2 = new SentenceImpl (KbPredicateImpl.get("argIsa"), flyingAnObject_operate, 2, KbCollectionImpl.get("PhysicalDevice"));
    AssertionImpl.findOrCreate (sargisa2, Constants.uvMt());
    flyingAnObject_operate.addGeneralization(KbPredicateImpl.get("objectActedOn"), Constants.uvMt());
    // Rule that FlyinATypeOfObject => objectActedOn is a of certain type
    
        
    BinaryPredicateImpl flyingDoneBySomething_operate = BinaryPredicateImpl.findOrCreate("flyingDoneBySomething-Operate", "ActorSlot");
    flyingDoneBySomething_operate.addArgIsa(1, flying1Col, Constants.uvMt());
    flyingDoneBySomething_operate.addArgIsa(2, KbCollectionImpl.get("IntelligentAgent"), Constants.uvMt());
    KbPredicateImpl.get("performedBy").addSpecialization(flyingDoneBySomething_operate, Constants.uvMt()); 
    
    FirstOrderCollectionImpl flying2Col = FirstOrderCollectionImpl.findOrCreate("Flying-Move");
    FirstOrderCollectionImpl move = FirstOrderCollectionImpl.findOrCreate("MovementThroughAir");
    move.addSpecialization(flying2Col, Constants.uvMt());
    
    BinaryPredicateImpl flyingDoneBySomething_move = BinaryPredicateImpl.findOrCreate("flyingDoneBySomething-Move", KbCollectionImpl.get("ActorSlot"), Constants.uvMt());
    flyingDoneBySomething_move.addArgIsa(1, flying2Col, Constants.uvMt());
    flyingDoneBySomething_move.addArgIsa(2, KbCollectionImpl.get("SomethingExisting"), Constants.uvMt());
    flyingDoneBySomething_move.addGeneralization(KbPredicateImpl.get("doneBy"), Constants.uvMt());
        
    // objectActedOn is on self in Flying-Move
    BinaryPredicate destinationList = BinaryPredicateImpl.findOrCreate("flightDestinationList");
    //destinationList.addArgIsa(2, "(ListOfTypeFn GeopoliticalEntity)", "BaseKB");
    //destinationList.addArgIsa(1, "FlyingAnObject-Operate", "BaseKB");
    destinationList.addArgIsa(2, KbCollection.get("(ListOfTypeFn GeopoliticalEntity)"), BASE_KB);
    destinationList.addArgIsa(1, KbCollection.get("FlyingAnObject-Operate"), BASE_KB);
    
    FirstOrderCollection flying3Col = FirstOrderCollectionImpl.findOrCreate("Flying-Travel");
    FirstOrderCollectionImpl travel = FirstOrderCollectionImpl.get("Travel-TripEvent");
    //travel.addSpecialization(flying3Col, Constants.uvMt());
    // The above can be rewritten using addArg2 as follows:
    BinaryPredicateImpl.get("genls").addFact(Constants.uvMt(), flying3Col, travel);
    
    BinaryPredicateImpl flying3Pred1 = BinaryPredicateImpl.findOrCreate("flyingDoneBySomeone-Travel", "ActorSlot", "UniversalVocabularyMt");
    // flying3Pred1.addArgIsa(1, flying2Col, Constants.uvMt());
    // The above can be rewritten using addFact as follows:
    // Expect: (ist UniversalVocabularyMt (argIsa flyingDoneBySomeone-Travel 1 Flying-Travel))
    //flying3Pred1.addFact(Constants.uvMt(), KbPredicateImpl.get("argIsa"), 1, 1, flying2Col);
    KbPredicateImpl.get("argIsa").addFact(Constants.uvMt(), flying3Pred1, 1, flying2Col);
    
    flying3Pred1.addArgIsa(2, KbCollectionImpl.get("Person"), Constants.uvMt());
    flying3Pred1.addGeneralization(KbPredicateImpl.get("performedBy"), Constants.uvMt());
    
    // Setup a context hierarchy
    Context airlineLogMt = ContextImpl.findOrCreate("SomeAirlineLogMt");
    //airlineLogMt.addInheritsFrom("CurrentWorldDataCollectorMt");
    airlineLogMt.addInheritsFrom(Context.get("CurrentWorldDataCollectorMt"));
    Context airlineEmpMt = ContextImpl.findOrCreate("SomeAirlineEmployeeMt");
    Context airlineEquipMt = ContextImpl.findOrCreate("SomeAirlineEquipmentMt");
    
    KbFunction equipmentMtFunc = KbFunctionImpl.findOrCreate("SomeAirlineEquipmentLogFn", KbCollectionImpl.get("UnaryFunction"));
    equipmentMtFunc.instantiates(KbCollectionImpl.get("ReifiableFunction"));
    equipmentMtFunc.setArity(1);
    equipmentMtFunc.addResultIsa((KbCollection)ContextImpl.getClassType(), Constants.uvMt());
    equipmentMtFunc.addArgIsa(1, KbCollectionImpl.get("CommercialAircraft"), Constants.uvMt());
            
    
    airlineLogMt.addInheritsFrom(airlineEmpMt);
    airlineEquipMt.addExtension(airlineLogMt);
    
    // Not asserting specializations for fromLocation and toLocation
    final KbIndividualImpl apilot = KbIndividualImpl.findOrCreate("Pilot-APITest", "AirplanePilot", "SomeAirlineEmployeeMt");
    
    final KbIndividualImpl aplane = KbIndividualImpl.findOrCreate("Plane-APITest", cPlane, airlineEquipMt);
    Fact owns = FactImpl.findOrCreate(new SentenceImpl(KbPredicateImpl.get("owns"), airline, aplane), airlineEquipMt);
    
    final Context airplaneContext = equipmentMtFunc.findOrCreateFunctionalTerm(Context.class, aplane);
            
    //KBIndividual operate = KBIndividual.findOrCreate("FlyingAPlane-APITest", flying1Col, airlineLogMt);
    final KbIndividual operate = KbIndividualImpl.findOrCreate("FlyingAPlane-APITest", flyingAPlane, airlineLogMt);
    
    flyingDoneBySomething_operate.addFact(airlineLogMt, operate, apilot);
    flyingAnObject_operate.addFact(airlineLogMt, operate, aplane);
    
    final KbIndividualImpl flight = KbIndividualImpl.findOrCreate("FlightXYZ-APITest", flying2Col, airlineLogMt);
    flyingDoneBySomething_move.addFact(airlineLogMt, flight, aplane);
    
    final KbIndividual city1 = KbIndividualImpl.findOrCreate("TestCity001", kbapitc.city);
    final SentenceImpl s = new SentenceImpl (KbPredicateImpl.get("fromLocation"), flight, city1);
    
    final Fact f1 = new FactImpl(airlineLogMt, s); // From        
    // Adding comment on a GAF
    f1.addComment("A flight from Test City 001", airlineLogMt);
    
    // Adding GAFs using fact
    final KbIndividual city2 = KbIndividualImpl.findOrCreate("TestCity002", kbapitc.city);
    final Fact f2 = new FactImpl(airlineLogMt, KbPredicateImpl.get("toLocation"), flight, city2); // To
    //f2.addComment("A flight to Test City 002", "SomeAirlineLogMt");
    f2.addComment("A flight to Test City 002", airlineLogMt);
    
    final KbCollection publicData = KbCollectionImpl.findOrCreate("SomeAirlinePublicData");
            
    flight.addQuotedIsa(KbCollectionImpl.findOrCreate("SomeAirlinePublicData"), airlineLogMt);
    f1.addQuotedIsa(publicData, airlineLogMt);
    f2.addQuotedIsa(publicData, airlineLogMt);
    
    Assertion cause = AssertionImpl.findOrCreate("(causes-EventEvent FlyingAPlane-APITest FlightXYZ-APITest)", "SomeAirlineLogMt");
    
    // artifactFoundInLocation
    // aircraftInAirport
    
    // Transport by Air
    /*
     * (implies 
    (and 
      (isa ?PLANE CommercialAircraft) 
      (flyingDoneBySomething-Move ?FLIGHT ?PLANE) 
      (endingDate ?FLIGHT ?END-DATE) 
      (toLocation ?FLIGHT ?TO)) 
    (holdsIn ?END-DATE 
      (artifactFoundInLocation ?PLANE ?TO)))
      
     */
    
    Variable varp = new VariableImpl("?PLANE");
    Variable varf = new VariableImpl("?FLIGHT");
    Variable varend = new VariableImpl("?END-DATE");
    Variable vart = new VariableImpl("?TO");
    SentenceImpl s1 = new SentenceImpl (KbPredicateImpl.get("isa"), varp, cPlane);
    SentenceImpl s2 = new SentenceImpl (flyingDoneBySomething_move, varf, varp);
    SentenceImpl s3 = new SentenceImpl (KbPredicateImpl.get("endingDate"), varf, varend);
    SentenceImpl s4 = new SentenceImpl (KbPredicateImpl.get("toLocation"), varf, vart);
    Sentence s5 = new SentenceImpl (KbPredicateImpl.get("artifactFoundInLocation"), varp, vart);
    Sentence s6 = new SentenceImpl (KbPredicateImpl.get("holdsIn"), varend, s5);
    List<Sentence> sandlist = new ArrayList<>();
    sandlist.add(s1);
    sandlist.add(s2);
    sandlist.add(s3);
    sandlist.add(s4);
    Sentence sand = SentenceImpl.and(sandlist);

    SentenceImpl rule = new SentenceImpl(LogicalConnectiveImpl.get("implies"), sand, s6);
    
    // flyingRule = AssertionImpl.findOrCreate(rule, Constants.baseKbMt(), null, Direction.FORWARD);
    // flyingRule = RuleImpl.findOrCreate(SentenceImpl.implies(sandlist, s6), Constants.baseKbMt(), KBAPIEnums.Strength.AUTO, Direction.FORWARD);
    flyingRule = (Rule) rule.assertIn(Constants.baseKbMt());
    flyingRule.changeDirection(Assertion.Direction.FORWARD);
    
    LOG.debug("Rule assertion hlid: " + flyingRule.getId());
    		
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy MM dd HH:mm");
		Date d = sdf.parse("2014 03 15 10:20");
		Fact f = FactImpl.findOrCreate(
            new SentenceImpl(KbPredicateImpl.get("endingDate"), flight, d), airlineLogMt);
    
    /*
    <Present Participle><NP>-[Verb Specialization]-Fn
    FlyingAnObject-Operate FlyingAnObject-Operate-Fn AnObject;
    
    FlyingAnObject-Operate;
    
    flyingAnObject-Operate-Predicate FlyingAnObject-Operate AnObject;
    
    flyingDoneBySomething-Operate FlyingAnObject-Operate Something;
    */
    
    // For intransitive sense, don't add anything
    // Flying;
    
    // Dying - intransitive; Working
    // KillingAnObject - instead of just Killing - monotransitive
    // GivingAnObjectToSomeone - ditransitive
    // GivingAnObject
    LOG.info("Done setupOEScript");
  }

  private static class ImmutableContext extends ContextImpl {

    public ImmutableContext(ElMt cycCtx) throws Exception {
      super(cycCtx);
    }

    private ImmutableContext(String cycName) throws Exception {
      super(cycName);
    }

    @Override //Because we want to allow equality with mutable contexts.
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (!(obj instanceof Context)) {
        return false;
      }
      ContextImpl other = (ContextImpl) obj;
      if (getCore() == null) {
        if (other.getCore() != null) {
          return false;
        }
      } else if (!getCore().equals(other.getCore())) {
        return false;
      }
      return true;
    }
  }
}
