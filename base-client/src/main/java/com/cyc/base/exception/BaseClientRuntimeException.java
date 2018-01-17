package com.cyc.base.exception;

import com.cyc.session.exception.SessionRuntimeException;

/*
 * #%L
 * File: BaseClientRuntimeException.java
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
 * Class BaseClientRuntimeException indicates an error condition...
 *
 * @version $Id: BaseClientRuntimeException.java 175669 2017-11-01 23:47:32Z nwinant $
 * @author Stephen L. Reed
 */
public class BaseClientRuntimeException 
        extends RuntimeException
        implements SessionCompatibleBaseClientException {
  
  private final String rawMsg;
  
  /**
   * Construct a CycApiException object with no specified message.
   */
  public BaseClientRuntimeException() {
    super();
    this.rawMsg = null;
  }
  
  /**
   * Construct a CycApiException object with a specified message.
   * @param msg a message describing the exception.
   */
  public BaseClientRuntimeException(String msg) {
    super(msg);
    this.rawMsg = msg;
  }
  
  /**
   * Construct a CycApiException object with a specified message
   * and throwable.
   * @param msg the message string
   * @param cause the throwable that caused this exception
   */
  public BaseClientRuntimeException(String msg, Throwable cause) {
    super(msg, cause);
    this.rawMsg = msg;
  }
  
  /**
   * Construct a CycApiException object with a specified throwable.
   * @param cause the throwable that caused this exception
   */
  public BaseClientRuntimeException(Throwable cause) {
    super(cause);
    this.rawMsg = null;
  }
  
  @Override
  public SessionRuntimeException toSessionException(String msg) {
    return new SessionRuntimeException(msg, this);
  }
  
  @Override
  public SessionRuntimeException toSessionException() {
    return new SessionRuntimeException(this);
  }
  
  String getRawMessage() {
    return this.rawMsg;
  }
  
}
