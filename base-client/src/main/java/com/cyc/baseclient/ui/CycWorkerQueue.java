package com.cyc.baseclient.ui;

/*
 * #%L
 * File: CycWorkerQueue.java
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



//// External Imports
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * <P>CycWorkerQueue is designed to execute multiple CycWorker instances serially,
 * in first-in, first-out order. Each worker will run until its thread dies.
 *
 * @author baxter
 * @version $Id: CycWorkerQueue.java 173132 2017-08-02 00:48:28Z nwinant $
 * @since March 27, 2008, 12:19 PM
 */
public class CycWorkerQueue {
  
  //// Constructors
  
  /**
   * Creates a new instance of CycWorkerQueue.
   */
  public CycWorkerQueue() {
    this("Cyc Worker Queue");
  }
  
  /**
   * Creates a new instance of CycWorkerQueue whose thread has the given name.
   * @param name
   */
  public CycWorkerQueue(final String name) {
    thread.setName(name);
    thread.start();
  }
  
  //// Public Area
  
  /** Set <tt>worker</tt> to be started as soon as all previously enqueued workers are done.
   * @param worker */
  public void enqueue(CycWorker worker) {
    workerQueue.add(worker);
  }
  
  //// Protected Area
  
  //// Private Area
  private void processQueue() {
    while (true) {
      CycWorker worker = null;
      try {
        worker = getNextWorker();
      } catch (InterruptedException ie) {
        if (worker == null) {
          continue; //Go back to start of while loop and try again.
        }
      }
      worker.start();
      try {
        worker.getThread().join(); //Block until worker's thread is done.
      } catch (InterruptedException ex) {
        ex.printStackTrace();
      } catch (NullPointerException ex) {
        //Already done.
      }
    }
  }
  
  private CycWorker getNextWorker() throws InterruptedException {
    return workerQueue.take();
  }
  
  //// Internal Rep
  private final BlockingQueue<CycWorker> workerQueue = new LinkedBlockingQueue<>();
  private final Thread thread = new Thread() {
    @Override
    public void run() {
      processQueue();
    }
  };
  
}
