package com.cyc.base.exception;

import com.cyc.session.exception.SessionCommandException;
import java.util.Objects;

/*
 * #%L
 * File: CycApiException.java
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

/**
 * Class CycApiException indicates an error condition during a Cyc API call.
 * This type of exception is thrown when errors on the Java side are caught,
 * when errors on the Cyc server side are caught a CycApiServerSideException
 * is thrown instead.
 *
 * @version $Id: CycApiException.java 176591 2018-01-09 17:27:27Z nwinant $
 * @author Stephen L. Reed
 */
public class CycApiException extends BaseClientRuntimeException {
  
  public static CycApiException from(Throwable cause) {
    return (cause instanceof CycApiException) 
           ? (CycApiException) cause 
           : new CycApiException(cause);
  }
  
  public static CycApiException from(String msg, CycApiException cause) {
    return (Objects.equals(msg, cause.getRawMessage()))
           ? (CycApiException) cause 
           : new CycApiException(msg, cause);
  }
  
  public static CycApiException from(String msg, Throwable cause) {
    return new CycApiException(msg, cause);
  }
  
  /**
   * Construct a CycApiException object with no specified message.
   */
  public CycApiException() {
    super();
  }
  
  /**
   * Construct a CycApiException object with a specified message.
   * 
   * @param msg a message describing the exception.
   */
  public CycApiException(String msg) {
    super(msg);
  }
  
  /**
   * Construct a CycApiException object with a specified message
   * and throwable.
   * 
   * @param msg the message string
   * @param cause the throwable that caused this exception
   */
 public CycApiException(String msg, Throwable cause) {
    super(msg, cause);
  }
  
  /**
   * Construct a CycApiException object with a specified throwable.
   * 
   * @param cause the throwable that caused this exception
   */
  public CycApiException(Throwable cause) {
    super(cause);
  }
  
  @Override
  public SessionCommandException toSessionException(String msg) {
    return SessionCommandException.fromThrowable(msg, this);
  }
  
  @Override
  public SessionCommandException toSessionException() {
    return SessionCommandException.fromThrowable(this);
  }
  
}
