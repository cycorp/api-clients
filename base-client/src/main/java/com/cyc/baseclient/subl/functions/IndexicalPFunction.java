/*
 * Copyright 2017 Cycorp, Inc..
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
package com.cyc.baseclient.subl.functions;

/*
 * #%L
 * File: IndexicalPFunction.java
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

import com.cyc.base.CycAccess;
import com.cyc.base.cycobject.CycObject;
import com.cyc.base.exception.CycApiException;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.baseclient.connection.SublApiHelper;
import com.cyc.baseclient.subl.subtypes.SublBooleanSingleArgFunction;

/**
 *
 * @author nwinant
 */
public class IndexicalPFunction extends SublBooleanSingleArgFunction<SublApiHelper.AsIsTerm> {

  public static final String FUNCTION_NAME = "indexical-p";
  
  public IndexicalPFunction() {
    super(FUNCTION_NAME);
  }
  
  public Boolean eval(CycAccess access, String arg) throws CycConnectionException, CycApiException {
    return eval(access, new SublApiHelper.AsIsTerm(arg));
  }
  
  public Boolean eval(CycAccess access, CycObject arg) throws CycConnectionException, CycApiException {
    return eval(access, arg.stringApiValue());
  }
  
}
