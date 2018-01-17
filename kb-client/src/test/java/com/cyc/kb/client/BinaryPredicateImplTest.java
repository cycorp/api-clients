package com.cyc.kb.client;

/*
 * #%L
 * File: BinaryPredicateImplTest.java
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

import com.cyc.base.CycAccess;
import com.cyc.base.CycAccessManager;
import com.cyc.base.cycobject.CycConstant;
import com.cyc.kb.BinaryPredicate;
import com.cyc.kb.KbCollection;
import com.cyc.kb.exception.CreateException;
import com.cyc.kb.exception.DeleteException;
import com.cyc.kb.exception.KbRuntimeException;
import com.cyc.kb.exception.KbTypeException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.cyc.Cyc.Constants.UV_MT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BinaryPredicateImplTest {

	private static KbIndividualImpl individual;

	@BeforeClass
	public static void setUp() throws Exception {
      TestConstants.ensureInitialized();
      individual = KbIndividualImpl.findOrCreate("TestIndividual001");
      //individual.instantiates("Person", "UniversalVocabularyMt");
      individual.instantiates(KbCollection.get("Person"), UV_MT);
	}

	@AfterClass
	public static void tearDown() throws Exception {
	}

	@Test
	public void testBinaryPredicateString() throws KbTypeException, CreateException, DeleteException {
		try {
          KbPredicateImpl.get("isa");
			BinaryPredicate comment = Constants.getInstance().COMMENT_PRED;
			comment.addFact(Constants.uvMt(), individual, "TestIndividual001 is a person");
			individual.delete();
			java.util.Collection<String> comments = individual.getComments();
			assertEquals(comments.size(), 0);
		} catch (KbRuntimeException ex) {
			assertEquals(ex.getMessage(),
					"The reference to TestIndividual001 object is stale. Possibly because it was delete using x.delete() method.");
		}
	}
	
	@Test
	public void testStaticMethods() throws Exception {
		System.out.println("existsAsType; getStatus");
                CycAccess cyc = CycAccessManager.getCurrentAccess();
		assertTrue(BinaryPredicateImpl.existsAsType("flyingDoneBySomeone-Travel"));
		
		CycConstant cc1 = cyc.getLookupTool().getKnownConstantByName("flyingDoneBySomeone-Travel");
		assertTrue(BinaryPredicateImpl.existsAsType(cc1));
		
		assertFalse(BinaryPredicateImpl.existsAsType("FlyingAnObject-Operate"));
		
		CycConstant cc2 = cyc.getLookupTool().getKnownConstantByName("FlyingAnObject-Operate");
		assertFalse(BinaryPredicateImpl.existsAsType(cc2));
		
        KbPredicateImpl.get("isa");
	}
	

}
