/*
 * Copyright 2015 Cycorp, Inc.
 *
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
 */

package com.cyc.kb.client;

/*
 * #%L
 * File: TestUtils.java
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

import com.cyc.Cyc;
import com.cyc.session.CycServerReleaseType;
import com.cyc.session.CycSession;
import com.cyc.session.TestEnvironmentProperties;
import com.cyc.session.compatibility.CycSessionRequirement;
import com.cyc.session.compatibility.CycSessionRequirementList;
import com.cyc.session.compatibility.MinimumPatchRequirement;
import com.cyc.session.compatibility.NotEnterpriseCycRequirement;
import com.cyc.session.compatibility.NotOpenCycRequirement;
import com.cyc.session.exception.SessionCommunicationException;
import com.cyc.session.exception.SessionConfigurationException;
import com.cyc.session.exception.SessionException;
import com.cyc.session.exception.SessionInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author nwinant
 */
public class TestUtils {
  
  private static final Logger LOG = LoggerFactory.getLogger(TestUtils.class);

  public static final CycSessionRequirement NOT_RCYC_4_0Q = new MinimumPatchRequirement(
          "Query loading is unsupported on ResearchCyc 4.0q and earlier",
          CycServerReleaseType.RESEARCHCYC, 154917);

  public static void assumeCycSessionRequirement(CycSessionRequirement requirement) {
    // TODO: move this into some central test library
    try {
      org.junit.Assume.assumeTrue(requirement.checkCompatibility(getSession()).isCompatible());
    } catch (SessionException ex) {
      ex.printStackTrace(System.err);
      throw new RuntimeException(ex);
    }
  }
  
  public static void assumeCycSessionRequirements(CycSessionRequirementList requirements) {
    // TODO: move this into some central test library
    try {
      org.junit.Assume.assumeTrue(requirements.checkCompatibility().isCompatible());
    } catch (SessionException ex) {
      ex.printStackTrace(System.err);
      throw new RuntimeException(ex);
    }
  }
  
  public static void assumeNotOpenCyc() {
    // TODO: move this into some central test library
    
    // To toggle #areOpenCycTestsForcedToRun, edit "cyc.test.forceOpenCycTestsToRun" in the pom.xml
    if (!TestEnvironmentProperties.get().areOpenCycTestsForcedToRun()) {
      assumeCycSessionRequirement(NotOpenCycRequirement.NOT_OPENCYC);
    }
  }
  
  public static void assumeNotEnterpriseCyc() {
    // TODO: add TestEnvironmentProperties#areEnterpriseCycTestsForcedToRun
    assumeCycSessionRequirement(NotEnterpriseCycRequirement.NOT_ENTERPRISECYC);
  }
  
  public static void skipTest(Object testCase, String testName, String msg) {
    LOG.warn("Skipping {}#{}: {}", testCase.getClass().getSimpleName(), testName, msg);
    org.junit.Assume.assumeTrue(msg, false);
  }
  
  public static void assumeKbObject(String nameOrId) {
    // TODO: move this into some central test library
    org.junit.Assume.assumeTrue(
            "Test requires " + nameOrId + ", which is not present in tested KB.", 
            Cyc.existsInKb(nameOrId));
  }
  
  public static void assumeKbObjects(String... namesOrIds) {
    // TODO: move this into some central test library
    for (String nameOrId : namesOrIds) {
      assumeKbObject(nameOrId);
    }
  }
  
  private static CycSession getSession() throws SessionConfigurationException, SessionInitializationException, SessionCommunicationException {
    return CycSession.getCurrent();
  }
  
  /**
   * Tests whether to objects are pointer-equal. Before testing for pointer-equality, however, it
   * calls {@link org.junit.Assert.assertEquals}, which provides a much more describe response on 
   * failure; e.g.:
   * 
   * <code>
   * java.lang.AssertionError: expected:<expected#toString()> but was:<result#toString()>
   * </code>
   * 
   * @param expected
   * @param result 
   */
  public static void assertPointerEqual(Object expected, Object result) {
    // TODO: move this into some central test library
    assertEquals(expected, result);
    assertTrue(expected == result);
  }
  
}
