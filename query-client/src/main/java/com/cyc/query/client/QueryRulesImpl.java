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
package com.cyc.query.client;

/*
 * #%L
 * File: QueryRulesImpl.java
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
import com.cyc.baseclient.cycobject.CycArrayList;
import com.cyc.kb.Context;
import com.cyc.kb.Rule;
import com.cyc.kb.Sentence;
import com.cyc.kb.Variable;
import com.cyc.kb.client.KbPredicateImpl;
import com.cyc.kb.exception.CreateException;
import com.cyc.kb.exception.KbTypeException;
import com.cyc.query.ModifiableQuerySpecification;
import com.cyc.query.Query;
import com.cyc.query.QueryRules;
import com.cyc.query.exception.QueryConstructionException;
import com.cyc.session.exception.SessionCommunicationException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author nwinant
 */
public class QueryRulesImpl implements QueryRules {
  
  // Fields
  
  public static final String ALLOWED_RULES   = ":ALLOWED-RULES";
  public static final String FORBIDDEN_RULES = ":FORBIDDEN-RULES";

  private static final Logger LOGGER = LoggerFactory.getLogger(QueryRulesImpl.class);
  
  private final ModifiableQuerySpecification query;
  private final Variable ruleVar;
  private final Context ruleCtx;
  private final Query allowedRulesQuerySentence;
  private final Query forbiddenRulesQuerySentence;
  private final Query practiceRulesQuerySentence;
  
  
  // Construction
  
  protected QueryRulesImpl(ModifiableQuerySpecification query) throws KbTypeException, CreateException, QueryConstructionException {
    this.query = query;
    this.ruleVar = Variable.get("?RULE");
    this.ruleCtx = Context.get("TestVocabularyMt"); // FIXME: what context should we use?
    this.allowedRulesQuerySentence   = createRuleQuery("queryAllowedRules");
    this.forbiddenRulesQuerySentence = createRuleQuery("queryForbiddenRules");
    this.practiceRulesQuerySentence  = createRuleQuery("queryPracticeRules");
  }
  
  private Query createRuleQuery(String predicateName) throws QueryConstructionException, KbTypeException, CreateException {
    if (query.getId() == null) {
      throw QueryConstructionException.fromThrowable("Only CycLQuerySpecifications may have queryPracticeRules", new NullPointerException());
    }
    return new QueryImpl(Sentence.get(KbPredicateImpl.get(predicateName), query.getId(), ruleVar), ruleCtx);
  }
  
  
  // Public methods
  
  @Override
  public Collection<Rule> getAllowedRules() throws SessionCommunicationException {
    return getRules(allowedRulesQuerySentence);
  }
  
  @Override
  public Collection<Rule> getForbiddenRules() throws SessionCommunicationException {
    return getRules(forbiddenRulesQuerySentence);
  }
  
  @Override
  public Collection<Rule> getPracticeRules() throws SessionCommunicationException {
    return getRules(practiceRulesQuerySentence);
  }
  
  @Override
  public void allowAllRules() {
    query.getInferenceParameters().remove(ALLOWED_RULES);
    query.getInferenceParameters().remove(FORBIDDEN_RULES);
  }
  
  @Deprecated
  public void useOnlySpecifiedRules(boolean includePracticeRules, boolean forceReload) throws QueryConstructionException, SessionCommunicationException {
    // FIXME: This shouldn't be necessary, as explained in CAPI-809 - nwinant, 2017-04-23
    if (forceReload || query.getInferenceParameters().containsKey(ALLOWED_RULES)) {
      query.getInferenceParameters().remove(ALLOWED_RULES);
      final Set rules = new LinkedHashSet();
      // TODO: should practice rules be included by default? - nwinant, 2017-04-23
      if (includePracticeRules) {
        for (Rule rule : getPracticeRules()) {
          rules.add(rule.getCore());
        }
      }
      for (Rule rule : getAllowedRules()) {
        rules.add(rule.getCore());
      }
      if (!rules.isEmpty()) {
        query.getInferenceParameters().put(ALLOWED_RULES, new CycArrayList(rules));
      }
    }
    if (forceReload || query.getInferenceParameters().containsKey(FORBIDDEN_RULES)) {
      query.getInferenceParameters().remove(FORBIDDEN_RULES);
      final CycList inferenceParameterValue = new CycArrayList();
      //for (Rule rule : getQueryPracticeRules()) {
      for (Rule rule : getForbiddenRules()) {
        inferenceParameterValue.add(rule.getCore());
      }
      if (!inferenceParameterValue.isEmpty()) {
        query.getInferenceParameters().put(FORBIDDEN_RULES, inferenceParameterValue);
      }
    }
  }
  
  @Deprecated
  @Override
  public void useOnlySpecifiedRules(boolean includePracticeRules) throws QueryConstructionException, SessionCommunicationException {
    // FIXME: This shouldn't be necessary, as explained in CAPI-809 - nwinant, 2017-04-23
    useOnlySpecifiedRules(includePracticeRules, true);
  }
  
  
  // Private
  
  private Collection<Rule> getRules(Query rulesQuery) throws SessionCommunicationException {
    final Collection<Rule> results = new ArrayList();
    rulesQuery.getAnswers()
            .forEach((a) -> results.add(a.<Rule>getBinding(ruleVar)));
    return results;
  }
  
}
