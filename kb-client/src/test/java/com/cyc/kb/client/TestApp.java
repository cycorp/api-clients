package com.cyc.kb.client;

/*
 * #%L
 * File: TestApp.java
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


import com.cyc.kb.BinaryPredicate;
import com.cyc.kb.Context;
import com.cyc.kb.KbCollection;
import com.cyc.kb.KbIndividual;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TestApp {

  @BeforeClass
  public static void setUp() throws Exception {
    TestConstants.ensureInitialized();
  }

  @AfterClass
  public static void tearDown() throws Exception {
  }

  @Test
  public void testConnection() throws Exception {
    final Context airlineLogMt = ContextImpl.findOrCreate("SomeAirlineLogMt");
    final KbCollection d = KbCollectionImpl.get("Dog");
    final KbCollection f = KbCollectionImpl.get("(FruitFn AppleTree)");
    final Context m = ContextImpl.get("BiologyMt");
    
    KbCollectionImpl w = KbCollectionImpl.get("Flying-Move");
    KbIndividual expected = KbIndividualImpl.get("FlightXYZ-APITest");
    //assertTrue(w.<KbIndividual>getValues("isa", 2, 1, "SomeAirlineLogMt").contains(expected));
    assertTrue(Constants.isa().getValuesForArgPosition(w, 2, 1, airlineLogMt).contains(expected));
    assertTrue(w.<KbIndividual>getInstances("SomeAirlineLogMt").contains(expected));

    // LOG.info("values of d: " + d.getValues("ownerOfType"));
    // LOG.info("values of m: " +
    // m.getValues("genlMt").get(2).getValues("genlMt"));
  }

  @Test
  public void testHelloWorlds() throws Exception {
    final KbCollectionImpl p = KbCollectionImpl.get("Planet");
    final Context iauStandardsMt = ContextImpl.findOrCreate("IAUStandardsAndDefinitionsMt");

    Collection<KbIndividual> ps1 = p.<KbIndividual>getInstances("IAUStandardsAndDefinitionsMt");
    System.out.println("Instance of planets are: " + ps1.toString());

    //Collection<KbIndividual> ps2 = p.<KbIndividual>getValues(
    //        "isa", 2, 1, "IAUStandardsAndDefinitionsMt");
    Collection<KbIndividual> ps2 = Constants.isa()
            .getValuesForArgPosition(p, 2, 1, iauStandardsMt);
    System.out.println("Instance of planets are: " + ps2.toString());

    //KbIndividualImpl aPlanet = (KbIndividualImpl)p.<KbIndividual>getValues("isa", 2, 1, 
    //        "IAUStandardsAndDefinitionsMt").toArray()[0]; 
    KbIndividualImpl aPlanet = (KbIndividualImpl) Constants.isa()
            .getValuesForArgPosition(p, 2, 1, iauStandardsMt).toArray()[0];
    
    //Collection<KbCollection> d = aPlanet.<KbCollection>getValues("isa",
    //        1, 2, "IAUStandardsAndDefinitionsMt");
    Collection<KbCollection> d = Constants.isa().getValuesForArgPosition(aPlanet, 1, 2, iauStandardsMt);
    System.out.println("Instance of planets are: " + d.toString());
    
    //Collection<String> str = p.<String>getValues("comment",
    //        1, 2, "UniversalVocabularyMt");
    Collection<String> strings = Constants.getInstance().COMMENT_PRED
            .getValuesForArgPosition(p, 1, 2, Constants.uvMt());
    System.out.println("Comment of planets are: " + strings);
    System.out.println("Comment of planets are (2): " + p.getComments(
            "UniversalVocabularyMt"));    
    
    KbIndividualImpl pluto = KbIndividualImpl.get("Mx4rvVjS-pwpEbGdrcN5Y29ycA");
    //Collection<Double> flt = pluto.<Double>getValues("orbitalEccentricity", 1, 2, 
    //        "UniverseDataMt");
    Collection<Double> flt = BinaryPredicate.get("orbitalEccentricity")
            .getValuesForArg(pluto, null, ContextImpl.get("UniverseDataMt"));
    System.out.println("Orbital eccentricity of planets are: " + flt);
    
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy MM dd HH:mm");
    Date date = sdf.parse("2014 03 15 10:20");
    KbIndividual kbDate = KbIndividual.get("(MinuteFn 20 (HourFn 10 (DayFn 15 (MonthFn March (YearFn 2014)))))");

    KbIndividualImpl i = KbIndividualImpl.findOrCreate("FlightXYZ-APITest");
    //Collection<Date> dates = i.<Date>getValues("endingDate", 1, 2, "SomeAirlineLogMt");
    Collection<Date> dates = BinaryPredicate.get("endingDate")
            .getValuesForArg(i, null, ContextImpl.get("SomeAirlineLogMt"));
    assertTrue(dates.contains(kbDate));
  }
}
