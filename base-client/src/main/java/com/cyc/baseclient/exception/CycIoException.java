package com.cyc.baseclient.exception;

import com.cyc.base.exception.BaseClientRuntimeException;

/*
 * #%L
 * File: CycIoException.java
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
 * Class CycApiException indicates an error condition during a Cyc API call.
 *
 * @version $Id: CycIoException.java 169909 2017-01-11 23:21:20Z nwinant $
 * @author Stephen L. Reed
 */

public class CycIoException extends BaseClientRuntimeException {


    /**
     * Construct a CycApiException object with no specified message.
     */
    public CycIoException() {
        super();
    }

    /**
     * Construct a CycApiException object with a specified message.
     * @param s a message describing the exception.
     */
    public CycIoException(String s) {
        super(s);
    }
    
    public CycIoException(String s, Throwable cause) {
      super(s, cause);
    }
    
    public CycIoException(Throwable cause) {
      super(cause);
    }
}
