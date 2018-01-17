/*
 * Copyright 2017 Cycorp, Inc.
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
 * File: KbContentLogger.java
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

import com.cyc.base.cycobject.CycAssertion;
import com.cyc.base.cycobject.CycList;
import com.cyc.base.cycobject.ElMt;
import com.cyc.base.cycobject.FormulaSentence;
import com.cyc.kb.Assertion;
import com.cyc.kb.Context;
import com.cyc.kb.Sentence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A set of loggers to make it easy to filter for KB-specific events (lookups, assertions, etc.) 
 * 
 * <p>These loggers should make it possible for an application to maintain two log files: one which
 * contains pretty much everything related to the KB, and another log consisting only of the actual
 * changes which the application has made to the KB.
 * 
 * <p>Generally, the "-ED" loggers (CREATED, ASSERTED, DELETED, EDITED) should only log operations
 * that specifically attempt to alter the KB and the results of those attempts. I.e., an operation 
 * which checks whether a term is in the KB before killing it should go to the FIND logger, not the 
 * DELETE logger. 
 * 
 * <p>Additionally, any operation that reports a confirmed change to the KB should be logged at the
 * INFO level (or higher) and any related messages (e.g., a message logging an assertion 
 * <em>before</em> it's asserted) should be logged at least one level lower (DEBUG or TRACE). For 
 * an example, see KbContentLogger#logAssertResult(FormulaSentence, ElMt, Strength, Direction, CycAssertion):
 * 
 * @todo improve coverage, this is currently used only by AssertionImpl - nwinant, 2017-05-02
 * 
 * @author nwinant
 */
public class KbContentLogger {
  
  // Fields
  
  private static final String BASE_KB_LOGGER_NAME = "com.cyc.kb.CONTENT";
  
  public static final Logger KB_FIND_LOGGER 
          = LoggerFactory.getLogger(BASE_KB_LOGGER_NAME + ".FIND");
  
  private static final String BASE_KB_CHANGES_LOGGER_NAME = BASE_KB_LOGGER_NAME + ".CHANGES";
  
  private static final Logger KB_ASSERTED_LOGGER
          = LoggerFactory.getLogger(BASE_KB_CHANGES_LOGGER_NAME + ".ASSERTED");
  
  private static final Logger KB_CREATED_LOGGER
          = LoggerFactory.getLogger(BASE_KB_CHANGES_LOGGER_NAME + ".CREATED");
  
  private static final Logger KB_DELETED_LOGGER
          = LoggerFactory.getLogger(BASE_KB_CHANGES_LOGGER_NAME + ".DELETED");
  
  private static final Logger KB_EDITED_LOGGER
          = LoggerFactory.getLogger(BASE_KB_CHANGES_LOGGER_NAME + ".EDITED");
  
  private static final KbContentLogger THIS = new KbContentLogger();
  
  
  // Static methods
  
  public static KbContentLogger getInstance() {
    return THIS;
  }
  
  
  // Construction
  
  private KbContentLogger() {}
  
  
  // Public methods
  
  public String getAssertionLogMarker(CycList assertSentence, ElMt mt) {
    return String.format("%1$-19s",
            "[" + Integer.toHexString(mt.hashCode())
            + "." + Integer.toHexString(assertSentence.hashCode()) + "]");
  }
  
  public String getAssertionLogMarker(FormulaSentence assertSentence, ElMt mt) {
    return getAssertionLogMarker(assertSentence.toCycList(), mt);
  }
  
  public void logAssertAttempt(
          FormulaSentence assertSentence,
          ElMt mt,
          Assertion.Strength strength,
          Assertion.Direction direction) {
    final String assertionLogMarker = getAssertionLogMarker(assertSentence, mt);
    KB_ASSERTED_LOGGER.trace("{}  Attempting to assert formula '{}' in context '{}'",
            assertionLogMarker, assertSentence, mt);
  }
  
  public void logAssertResult(
          FormulaSentence assertSentence, 
          ElMt mt,
          Assertion.Strength strength, 
          Assertion.Direction direction,
          CycAssertion result) {
    final String assertionLogMarker = getAssertionLogMarker(assertSentence, mt);
    if (result != null) {
      KB_ASSERTED_LOGGER.info(   "{}  ASSERTED formula:", assertionLogMarker);
      final String[] lines = cyclistToStrings(result.getFormula());
      for (String line : lines) {
        KB_ASSERTED_LOGGER.info( "{}           {}", assertionLogMarker, line);
      }
      KB_ASSERTED_LOGGER.info(   "{}           In {}", assertionLogMarker, result.getMt());
      KB_ASSERTED_LOGGER.info(   "{}           Strength:  {}", assertionLogMarker, strength);
      KB_ASSERTED_LOGGER.info(   "{}           Direction: {}", assertionLogMarker, direction);
      KB_ASSERTED_LOGGER.trace(  "{}  Raw: {}", assertionLogMarker, result);
    } else {
      KB_ASSERTED_LOGGER.debug(  "{}  FAILED to assert:", assertionLogMarker);
      final String[] lines = cyclistToStrings(assertSentence.toCycList());
      for (String line : lines) {
        KB_ASSERTED_LOGGER.debug("{}           {}", assertionLogMarker, line);
      }
      KB_ASSERTED_LOGGER.debug(  "{}           In {}", assertionLogMarker, mt);
      KB_ASSERTED_LOGGER.debug(  "{}           Strength:  {}", assertionLogMarker, strength);
      KB_ASSERTED_LOGGER.debug(  "{}           Direction: {}", assertionLogMarker, direction);
    }
  }
  
  public void logAssertResult(Assertion assertion) {
    logAssertResult(
            (FormulaSentence) assertion.getFormula().getCore(),
            ContextImpl.asELMt(assertion.getContext()),
            null,
            assertion.getDirection(),
            (CycAssertion) assertion.getCore());
  }
  
  public void logSupport(CycList assertSentence,  ElMt mt, boolean successful) {
    final String assertionLogMarker = getAssertionLogMarker(assertSentence, mt);
    if (successful) {
      KB_ASSERTED_LOGGER.info(   "{}  ASSERTED support:", assertionLogMarker);
      final String[] lines = cyclistToStrings(assertSentence);
      for (String line : lines) {
        KB_ASSERTED_LOGGER.info( "{}           {}", assertionLogMarker, line);
      }
      KB_ASSERTED_LOGGER.info(   "{}           In {}", assertionLogMarker, mt);
    } else {
      KB_ASSERTED_LOGGER.debug(  "{}  FAILED to assert:", assertionLogMarker);
      final String[] lines = cyclistToStrings(assertSentence);
      for (String line : lines) {
        KB_ASSERTED_LOGGER.debug("{}           {}", assertionLogMarker, line);
      }
      KB_ASSERTED_LOGGER.debug(  "{}           In {}", assertionLogMarker, mt);
    }
  }
  
  public void logSupport(Sentence sentence, Context context, boolean successful) {
    logSupport(
            ((FormulaSentence) sentence.getCore()).toCycList(),
            (ElMt) context.getCore(),
            successful);
  }
  
  
  // Private methods
  
  private String[] cyclistToStrings(CycList list) {
    final String str = list.toPrettyCyclifiedString("");
    return str.split("\n");
  }
  
}
