package com.cyc.baseclient.ui;

/*
 * #%L
 * File: SublInteractor.java
 * Project: Base Client
 * %%
 * Copyright (C) 2013 - 2018 Cycorp, Inc.
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

//// Internal Imports
import com.cyc.baseclient.util.EvictingList;
import com.cyc.base.CycAccess;
import com.cyc.baseclient.connection.DefaultSublWorkerSynch;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * <P>SubLInteractor is designed to...
 *
 * @author baxter, Oct 8, 2008, 1:19:31 PM
 * @version $Id: SublInteractor.java 176591 2018-01-09 17:27:27Z nwinant $
 */
public class SublInteractor {
  
  //====|    Fields    |==========================================================================//
  
  private static final int DEFAULT_HISTORY_SIZE = 100;
  
  private static final Logger LOG = LoggerFactory.getLogger(SublInteractorPanel.class);
  
  private static final String LOGGER_PATH = SublInteractorPanel.class.getPackage().getName();
  
  private static final Logger COMMAND_LOG = LoggerFactory.getLogger(LOGGER_PATH + ".subl.COMMANDS");
  
  private static final Logger HISTORY_LOG = LoggerFactory.getLogger(LOGGER_PATH + ".subl.HISTORY");
  
  private final CycAccess cycAccess;
  private final List<SublInteractionResult> history;
  
  private int timeoutMsecs = 0;
  private boolean hasQuit = false;
  private DefaultSublWorkerSynch worker;
  
  //====|    Construction    |====================================================================//
  
  /**
   * Creates a new instance of SubLInteractor.
   * 
   * @param cycAccess
   * @param historySize maximum history size; if <tt>0</tt>, no history will be recorded
   */
  public SublInteractor(CycAccess cycAccess, int historySize) {
    this.cycAccess = cycAccess;
    this.history = new EvictingList<>(historySize);
  }
  
  /**
   * Creates a new instance of SubLInteractor.
   * 
   * @param cycAccess
   */
  public SublInteractor(CycAccess cycAccess) {
    this(cycAccess, DEFAULT_HISTORY_SIZE);
  }
  
  //====|    Public methods    |==================================================================//
  
  /**
   * Evaluate subl string and return a list of the results.
   * 
   * @param subl
   * @return List of the Objects return values from evaluating text.
   * @throws java.lang.Exception
   */
  public List submitSubL(String subl) throws Exception {
    try {
      final List result = processSubL(subl);
      recordHistory(new SublInteractionResult(subl, result));
      return result;
    } catch (Exception ex) {
      recordHistory(new SublInteractionResult(subl, ex));
      LOG.error("Error: {}", ex);
      throw ex;
    }
  }
  
  public List<SublInteractionResult> getHistory() {
    return Collections.unmodifiableList(history);
  }
  
  public void cancelLastCommand() {
    try {
      worker.abort();
    } catch (Exception ex) {
      LOG.error("Error: {}", ex);
    }
  }
  
  public void quit() {
    LOG.debug("Quitting...");
    cancelLastCommand();
    this.hasQuit = true;
    LOG.debug("... Quit!");
  }
  
  public boolean hasQuit() {
    return this.hasQuit;
  }
  
  public void setTimeoutMsecs(int msecs) {
    timeoutMsecs = msecs;
  }
  
  public CycAccess getCycAccess() {
    return cycAccess;
  }
  
  //====|    Internal methods    |================================================================//
  
  private List processSubL(String text) throws Exception {
    worker = new DefaultSublWorkerSynch(
            "(multiple-value-list " + text + ")", cycAccess, timeoutMsecs);
    final Object result = worker.getWork();
    if (result instanceof List) {
      return (List) result;
    } else {
      final Object[] results = {result};
      return Arrays.asList(results);
    }
  }
  
  private void recordHistory(SublInteractionResult interaction) {
    LOG.trace("{}", interaction);
    history.add(interaction);
    if (interaction.isSuccessful()) {
      COMMAND_LOG.info("{}", interaction);
      HISTORY_LOG.info("{}", interaction);
    } else {
      COMMAND_LOG.error("{}", interaction);
      HISTORY_LOG.error("{}", interaction);
    }
  }
  
}
