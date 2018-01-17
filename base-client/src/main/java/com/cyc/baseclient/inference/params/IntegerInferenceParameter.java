package com.cyc.baseclient.inference.params;

import com.cyc.query.parameters.InferenceParameter;

/*
 * #%L
 * File: IntegerInferenceParameter.java
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

/**
 * <P>InferenceParameter is designed to...
 *
 * <P>Copyright (c) 2004 - 2006 Cycorp, Inc.  All rights reserved.
 * <BR>This software is the proprietary information of Cycorp, Inc.
 * <P>Use is subject to license terms.
 *
 * @author tbrussea
 * @since August 2, 2005, 10:25 AM
 * @version $Id: IntegerInferenceParameter.java 176591 2018-01-09 17:27:27Z nwinant $
 */
public interface IntegerInferenceParameter extends InferenceParameter {
  long getMinValue();
  long getMaxValue();
}
