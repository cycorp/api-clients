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
package com.cyc.query.client.templates;

/*
 * #%L
 * File: OeTemplateResults.java
 * Project: Query Client
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

import com.cyc.base.cycobject.CycList;
import com.cyc.query.QueryAnswer;
import com.cyc.query.QueryAnswers;
import com.cyc.query.client.QueryAnswersImpl;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.LF;

/**
 *
 * @author nwinant
 */
public class OeTemplateResults
        extends ArrayList<OeTemplateResult>
        implements List<OeTemplateResult> {
  
  //====|    Construction    |====================================================================//
  
  public OeTemplateResults() { }
  
  OeTemplateResults(CycList<? extends CycList> results) {
    if (!results.isPlist()) {
      results.forEach((result) -> {
        this.add(new OeTemplateResult(result));
      });
    } else if (!results.isEmpty()) {
      this.add(new OeTemplateResult(results));
    }
  }
  
  //====|    Methods    |=========================================================================//
  
  public String toPrettyString(String indent) {
    // TODO: could be improved - nwinant, 2017-08-11
    final StringBuilder sb = new StringBuilder();
    final String hr = "---------------------------" + LF;
    final String resultPrefix = indent;
    //final String resultIndent = "";
    final String resultIndent = resultPrefix;
    sb.append(hr);
    this.forEach((r) -> {
      sb//.append(resultPrefix)
              .append(r.toPrettyString(resultIndent))
              .append(LF)
              .append(hr);
    });
    return sb.toString();
  }
  
  public QueryAnswers<QueryAnswer> toQueryAnswers() {
    return new QueryAnswersImpl(
            stream()
                    .filter(OeTemplateResult::hasBindings)
                    .map(OeTemplateResult::toQueryAnswer)
                    .collect(toSet()));
  }
  
}
