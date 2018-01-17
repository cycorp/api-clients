package com.cyc.baseclient.inference.metrics;

/*
 * #%L
 * File: InferenceMetricsHashSet.java
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

import com.cyc.base.cycobject.CycList;
import com.cyc.base.cycobject.CycSymbol;
import com.cyc.baseclient.cycobject.CycArrayList;
import com.cyc.query.metrics.InferenceMetric;
import com.cyc.query.metrics.InferenceMetrics;
import java.util.HashSet;

/**
 * The preferred value type for the :METRICS inference parameter.
 * 
 * @see com.cyc.query.parameters.InferenceParameters#setMetrics(com.cyc.query.metrics.InferenceMetrics) 
 * @author baxter
 */
public class InferenceMetricsHashSet extends HashSet<InferenceMetric> implements InferenceMetrics {

  public String stringApiValue() {
    return ((CycList)cycListApiValue()).stringApiValue();
  }

  public Object cycListApiValue() {
    final CycList cyclistApiValue = new CycArrayList<CycSymbol>(this.size());
    for (final InferenceMetric metric : this) {
      cyclistApiValue.add(metric.getName());
    }
    return cyclistApiValue;
  }

  public CycSymbol getCycSymbol() {
    throw new UnsupportedOperationException();
  }
}
