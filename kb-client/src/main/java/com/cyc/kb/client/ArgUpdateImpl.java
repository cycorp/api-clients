/*
 * Copyright 2016 Cycorp, Inc.
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
 * File: ArgUpdateImpl.java
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

import com.cyc.kb.ArgPosition;
import com.cyc.kb.ArgUpdate;


public class ArgUpdateImpl implements ArgUpdate {

  private final ArgPosition argPos;
  private final Object value;
  private final ArgUpdateOperation operation;
  
  public ArgUpdateImpl(ArgPosition argPos, ArgUpdateOperation operation, Object value) {
    this.argPos = argPos;
    this.value = value;
    this.operation = operation;
  }
  
  @Override
  public ArgPosition getArgPosition() {
    return argPos;
  }

  @Override
  public ArgUpdateOperation getOperation() {
    return operation;
  }

  @Override
  public Object getValue() {
    return value;
  }
  
}
