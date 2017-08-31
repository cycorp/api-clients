package  com.cyc.baseclient.exception;

import com.cyc.base.exception.BaseClientRuntimeException;

/*
 * #%L
 * File: CycTaskInterruptedException.java
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

/**
 * Implements an exception class for notification when a Cyc server 
 * communication has timed out. When this is thrown, the outstanding
 * task on the Cyc server is aborted.
 *
 * @version $Id: CycTaskInterruptedException.java 169909 2017-01-11 23:21:20Z nwinant $
 * @author Eric E. Allen<br>
 */
public class CycTaskInterruptedException extends BaseClientRuntimeException {
    
  /**
   * Construct a TimeOutException object with the 
   * specified throwable.
   * @param ie the throwable that caused this exception
   */
  public CycTaskInterruptedException(InterruptedException ie) {
    super(ie);
  }
  
}
