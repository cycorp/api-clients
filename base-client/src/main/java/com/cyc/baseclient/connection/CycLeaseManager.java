package com.cyc.baseclient.connection;

/*
 * #%L
 * File: CycLeaseManager.java
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

//// External Imports
import com.cyc.base.CycAccess;
import com.cyc.base.CycAccessManager;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventListener;
import java.util.EventObject;
import java.util.List;

//// Internal Imports
import com.cyc.base.exception.CycTimeOutException;
import com.cyc.base.connection.LeaseManager;
import com.cyc.base.cycobject.CycList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <P>CycLeaseManager manages api service leases between a Cyc image (server) and application client.
 *
 * <P>Copyright (c) 2003 - 2006 Cycorp, Inc. All rights reserved.
 * <BR>This software is the proprietary information of Cycorp, Inc.
 * <P>Use is subject to license terms.
 *
 * @author reed
 date June 27, 2005, 10:20 AM
 * @version $Id: CycLeaseManager.java 176591 2018-01-09 17:27:27Z nwinant $
 */
public class CycLeaseManager extends Thread implements LeaseManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(CycLeaseManager.class);
  
  //// Constructors
  /** Creates a new instance of CycLeaseManager.
   *
   * @param cycConnection
   */
      
  public CycLeaseManager(final CycConnectionImpl cycConnection) {
    //// Preconditions
    if (cycConnection == null) {
      throw new InvalidParameterException("cycAccess must not be null");
    }
    this.cycConnection = cycConnection;
  }
    
  //// Public Area
  /** Adds a Cyc lease manager listener.
   *
   * @param cycLeaseManagerListener a listener for Cyc lease manager events
   */
  public synchronized void addListener(
          final LeaseManagerListener cycLeaseManagerListener) {
    //// Preconditions
    if (cycLeaseManagerListener == null) {
      throw new InvalidParameterException(
              "cycLeaseManagerListener must not be null");
    }
    if (listeners.contains(cycLeaseManagerListener)) {
      throw new InvalidParameterException(
              "listener must not be currently registered");
    }
    assert listeners != null : "listeners must not be null";
    assert this.isAlive() : "the CycLeaseManager thread has died because a lease timed-out, errored or was denied";
    // otherwise, this is a SOAP client connection and leasing is never started

    listeners.add(cycLeaseManagerListener);
  }

  
  /** Removes a Cyc lease manager listener.
   *
   * @param cycLeaseManagerListener a listener for Cyc lease manager events
   */
  public synchronized void removeListener(
          final LeaseManagerListener cycLeaseManagerListener) {
    //// Preconditions
    if (cycLeaseManagerListener == null) {
      throw new InvalidParameterException(
              "cycLeaseManagerListener must not be null");
    }
    assert listeners != null : "listeners must not be null";    
    assert this.isAlive() : "the CycLeaseManager thread has died because a lease timed-out, errored or was denied";
       
    // otherwise, this is a SOAP client connection and leasing is never started

    listeners.remove(cycLeaseManagerListener);
  }

  /**
   * Returns whether or not we have a valid lease with the Cyc server.
   *
   * @return whether or not we have a valid lease with the Cyc server
   */
  public boolean hasValidLease() {
    return hasValidLease;
  }

  /**
   * Removes all Cyc lease manager listeners.
   */  
  public void removeAllListeners() {
    //// Preconditions
    assert listeners != null : "listeners must not be null";   
    assert this.isAlive() : "the CycLeaseManager thread has died because a lease timed-out, errored or was denied";
    
    // otherwise, this is a SOAP client connection and leasing is never started

    listeners.clear();
  }
  /** the Cyc api services lease request timeout in milliseconds */
  public static long CYC_API_SERVICES_LEASE_REQUEST_TIMEOUT_MILLIS = 120000;

  /** Performs periodic Cyc api service lease acquisition, and notifies listeners if the lease fails or is denied. */
  public void run() {

    //// Preconditions
    assert listeners != null : "listeners must not be null";
  //Tag: Fix CycLeaseManager    
    // assert cycAccess != null : "cycAccess must not be null";
    assert cycConnection != null : "cycConnection must not be null";


    Thread.currentThread().setName("Cyc API services lease manager");
    Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
    //while (!cycAccess.isClosed()) {
    while (!cycConnection.isClosed()) {
      // SubL side gets a lease request for twice the value of the lease duration
      final String script = "(with-immediate-execution (acquire-api-services-lease "
              + (getLeaseDurationMilliseconds() * 2) + " \""
      //            + cycAccess.getCycConnection().getUuid().toString() + "\"))";
              + cycConnection.getUuid().toString() + "\"))";
      final CycList scriptList = cycConnection.cycAccess.getObjectTool().makeCycList(script);
      String results = null;
      isLeaseRequestPending = true;
      LOGGER.debug("Requesting API services lease");
      try {
        /*
        DefaultSublWorkerSynch worker = new DefaultSublWorkerSynch(scriptList,
                cycAccess,
                CYC_API_SERVICES_LEASE_REQUEST_TIMEOUT_MILLIS);
        worker.setShouldIgnoreInvalidLeases(true);
        results = (String) worker.getWork();
        */
        
        Object[] response = {null, null};
        response = cycConnection.converse(scriptList);
        results = (String) response[1];
        
        LOGGER.trace(results);
      } catch (CycTimeOutException toe) {
        isLeaseRequestPending = false;
        LOGGER.debug(
                "Cyc communications timeout encountered when attempting " + "to renew the API services lease.\n" + toe.getMessage());
        notifyListeners(CYC_DOES_NOT_RESPOND_TO_LEASE_REQUEST);
        try {
          Thread.sleep(getLeaseDurationMilliseconds());
        } catch (InterruptedException ex) {
        }
        continue;
      } catch (Exception e) {
        isLeaseRequestPending = false;
        LOGGER.debug(
                "Cyc communications error encountered when attempting " + "to renew the API services lease.\n" + e.getMessage());
        notifyListeners(CYC_COMMUNICATION_ERROR);
        try {
          Thread.sleep(getLeaseDurationMilliseconds());
        } catch (InterruptedException ex) {
        }
        continue;
      }
      //cycAccess.getCycConnection().traceOff();
      isLeaseRequestPending = false;
      if (results.equals("api services lease denied")) {
        LOGGER.error(
                "The request to renew the API services lease was denied by the Cyc server.");
        notifyListeners(CYC_DENIES_THE_LEASE_REQUEST);
      } else {
        String currentImageID = extractImageID(results);
        if (cycImageID != null && !cycImageID.equals(currentImageID)) {
          LOGGER.info("The Cyc server image ID has changed.");
          notifyListeners(CYC_IMAGE_ID_HAS_CHANGED);
        } else {
          LOGGER.debug("API services lease renewed");
          notifyListeners(LEASE_SUCCESSFULLY_RENEWED);
        }
        cycImageID = currentImageID;
      }
      try {
        Thread.sleep(getLeaseDurationMilliseconds());
      } catch (InterruptedException e) {
      }
    }
  }

  /** Immediately renews the current lease by interrupting the sleep of this leasing thread. */
  public void immediatelyRenewLease() {
    LOGGER.trace("immedidately renewing the lease");
    interrupt();
    try {
      // give time for Cyc to renew the lease
      sleep(250);
    } catch (InterruptedException e) {
    }
  }

  /** Returns the indicator whether a lease request is currently pending.
   *
   * @return the indicator whether a lease request is currently pending
   */
  public boolean isLeaseRequestPending() {
    return isLeaseRequestPending;
  }

  /** Gets the lease duration milliseconds.
   *
   * @return the lease duration milliseconds
   */
  public long getLeaseDurationMilliseconds() {
    return leaseDurationMilliseconds;
  }

  /** Sets the lease duration milliseconds.
   *
   * @param leaseDurationMilliseconds the lease duration milliseconds
   */
  public void setLeaseDurationMilliseconds(final long leaseDurationMilliseconds) {
    //// Preconditions
    if (leaseDurationMilliseconds < 2000) {
      throw new InvalidParameterException(
              "leaseDurationMilliseconds must be at least 2000");
    }

    this.leaseDurationMilliseconds = leaseDurationMilliseconds;
  }


  /** Class that provides a Cyc lease event. */
  public class CycLeaseEventObject extends EventObject implements LeaseEventObject {

    /** Constructs a new CycLeaseEventObject instance.
     *
     * @param source the object on which the Event initially occurred
     * @param reason the Cyc lease manager event reason
     */
    public CycLeaseEventObject(final Object source,
            final LeaseManagerReason reason) {
      super(source);
      //// Preconditions
      if (reason == null) {
        throw new InvalidParameterException(
                "cycLeaseManagerReason must not be null");
      }

      this.cycLeaseManagerReason = reason;
    }

    /** Returns a string representation of this object.
     *
     * @return a string representation of this object
     */
    public String toString() {
      return "CycLeaseEvent (" + cycLeaseManagerReason.getReason() + ")";
    }

    public LeaseManagerReason getReason() {
      return cycLeaseManagerReason;
    }
    final LeaseManagerReason cycLeaseManagerReason;
  }

  /** Defines the interface for Cyc API services event listeners. */
  public interface CycLeaseManagerListener extends EventListener, LeaseManagerListener {

    /** Notifies the listener of the given Cyc API services lease event.
     *
     * @param evt the the given Cyc API services lease event
     */
    void notifyCycLeaseEvent(LeaseEventObject evt);
  };

  public String getCycImageId() {
    return cycImageID;
  }

  //// Protected Area
  //// Private Area
  private String extractImageID(final String leaseDescription) {
    assert leaseDescription.startsWith("api services lease granted by ");
    String currentImageID = leaseDescription.substring(30);
    final int firstSpaceIndex = currentImageID.indexOf(" ");
    if (firstSpaceIndex > 0) {
      currentImageID = currentImageID.substring(0, firstSpaceIndex);
    }
    return currentImageID;
  }

  /** Notifies the api service lease event listeners that an event happened with the given reason.
   *
   * @param cycLeaseManagerReason the Cyc lease manager event reason
   */
  private void notifyListeners(final LeaseManagerReason cycLeaseManagerReason) {
    //// Preconditions
    assert cycLeaseManagerReason != null : "cycLeaseManagerReason must not be null";
    assert listeners != null : "listeners must not be null";
//    assert cycAccess != null : "cycAccess must not be null";
    if ((cycLeaseManagerReason == CycLeaseManager.CYC_COMMUNICATION_ERROR) || (cycLeaseManagerReason == CycLeaseManager.CYC_DENIES_THE_LEASE_REQUEST) || (cycLeaseManagerReason == CycLeaseManager.CYC_DOES_NOT_RESPOND_TO_LEASE_REQUEST)) {
      hasValidLease = false;
    } else {
      hasValidLease = true;
    }
    final int listeners_size = listeners.size();
    for (int i = 0; i < listeners_size; i++) {
      final CycLeaseManagerListener cycLeaseManagerListener = 
              (CycLeaseManagerListener) listeners.get(i);
      //cycLeaseManagerListener.notifyCycLeaseEvent(new CycLeaseEventObject(
      //        cycAccess, cycLeaseManagerReason));
      cycLeaseManagerListener.notifyCycLeaseEvent(new CycLeaseEventObject(
              cycConnection, cycLeaseManagerReason));

    }
  }
  //// Internal Rep
  /** the Cyc api services client */
  //private final CycClient cycAccess;
  
  /** the Cyc api services client */
  private final CycConnectionImpl cycConnection;
  
  /** the Cyc image ID from the previous lease */
  private String cycImageID = null;
  /** the list of registered listeners */
  private final List listeners = Collections.synchronizedList(new ArrayList());
  /** the indicator whether a lease request is currently pending */
  private volatile boolean isLeaseRequestPending = false;
  /** the lease duration milliseconds */
  private long leaseDurationMilliseconds = 120000;
  /** assume we have a valid lease until told otherwise */
  private volatile boolean hasValidLease = true;

  //// Main
  /** Executes a test of the CycLeaseManager.
   *
   * @param args the command line arguments (unused)
   */
  public static void main(String[] args) {
    try {
      LOGGER.info("Starting.");
      final CycAccess cycAccess = CycAccessManager.getCurrentAccess();
      LOGGER.info(
              "Connected to: " + cycAccess.getHostName() + ":" + cycAccess.getBasePort());
      //      cycAccess.traceOn();
      for (int i = 0; i < 2; i++) {
        Thread.sleep(2000);
        final String script = "(sleep 1)";
        LOGGER.info("About to talk to Cyc: " + script);
        cycAccess.converse().converseVoid(script);
        LOGGER.info("Finished talking to Cyc.");
      }
      LOGGER.info("About to close CycAccess.");
      cycAccess.close();
      LOGGER.info("Closed CycAccess.");
    } catch (Exception e) {
      LOGGER.error(e.getMessage(), e);
      LOGGER.info("Finished.");
      System.exit(1);
    }
    LOGGER.info("Finished.");
    // NOTE: if the  main method hangs, then there is an issue with threads
    // lingering that should be investigated.
  }
}
