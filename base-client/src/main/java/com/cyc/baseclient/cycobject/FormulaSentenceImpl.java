package com.cyc.baseclient.cycobject;

/*
 * #%L
 * File: FormulaSentenceImpl.java
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
//// External Imports
import com.cyc.base.CycAccess;
import com.cyc.base.cycobject.CycConstant;
import com.cyc.base.cycobject.CycList;
import com.cyc.base.cycobject.CycObject;
import com.cyc.base.cycobject.CycSentence;
import com.cyc.base.cycobject.CycVariable;
import com.cyc.base.cycobject.ElMt;
import com.cyc.base.cycobject.Formula;
import com.cyc.base.cycobject.FormulaSentence;
import com.cyc.base.cycobject.NonAtomicTerm;
import com.cyc.base.exception.BaseClientRuntimeException;
import com.cyc.base.exception.CycApiException;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.baseclient.CommonConstants;
import com.cyc.baseclient.CycObjectFactory;
import com.cyc.baseclient.connection.SublApiHelper;
import com.cyc.kb.ArgPosition;
import com.cyc.session.compatibility.CycSessionRequirementList;
import com.cyc.session.compatibility.NotOpenCycRequirement;
import com.cyc.session.exception.OpenCycUnsupportedFeatureException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.cyc.baseclient.connection.SublApiHelper.makeSublStmt;
import static com.cyc.baseclient.subl.functions.SublFunctions.INDEXICAL_P;

/**
 * @author baxter, Jul 6, 2009, 10:05:43 AM
 * @version $Id: FormulaSentenceImpl.java 176591 2018-01-09 17:27:27Z nwinant $
 *
 */
// TODO: make it implement CycLFormula, or get rid of CycLFormula, as appropriate
public class FormulaSentenceImpl extends FormulaImpl implements CycSentence, FormulaSentence {

  //====|    Static fields    |===================================================================//
  
  public static final CycSessionRequirementList<OpenCycUnsupportedFeatureException> ADVANCED_SENTENCE_REQUIREMENTS
        = CycSessionRequirementList.fromList(
                NotOpenCycRequirement.NOT_OPENCYC
        );
  
  private static final CycSymbolImpl PPH_OPTIMIZED_NAMES_FOR_VARIABLES
          = CycObjectFactory.makeCycSymbol("pph-optimized-names-for-variables");
  
  private static ElMt simplifierMt = null;
  
  
  //====|    Static utility methods    |==========================================================//

  private static boolean isLogicalOperatorFort(final Object obj) {
    return (CommonConstants.LOGICAL_OPERATOR_FORTS.contains(obj));
  }
  
  static synchronized ElMt getDefaultSimplifierMt() {
    if (simplifierMt == null && CommonConstants.MT_SPACE != null) {
      simplifierMt = ElMtCycNautImpl.makeElMtCycNaut(Arrays.asList(CommonConstants.MT_SPACE,
              CommonConstants.CURRENT_WORLD_DATA_MT, CommonConstants.ANYTIME_PSC));
    }
    return simplifierMt;
  }
  
  
  //====|    Static factory methods    |==========================================================//
  
  /**
   * Create and return a new CycSentence whose arguments are terms. CycArrayList arguments will be
   * converted to CycNauts or CycSentences.
   *
   * @param terms
   * @return a new FormulaSentence
   */
  public static FormulaSentence makeFormulaSentence(Iterable<? extends Object> terms) {
    // TODO: add more documentation
    return new FormulaSentenceImpl(terms);
  }

  /**
   * Build a new CycSentence from terms.
   *
   * @param terms
   * @return a new FormulaSentence
   */
  public static FormulaSentence makeFormulaSentence(Object... terms) {
    final FormulaSentenceImpl newSentence = new FormulaSentenceImpl();
    for (final Object arg : terms) {
      newSentence.addArg(arg);
    }
    return newSentence;
    //return new FormulaSentenceImpl(CycArrayList.makeCycList(terms));
  }

  /**
   * Build a new CycSentence from a String. This will add #$ wherever necessary.
   *
   * @param cyc
   * @param cycl
   * @return a new FormulaSentence
   * @throws CycApiException
   * @throws com.cyc.base.exception.CycConnectionException
   */
  public static FormulaSentence makeFormulaSentence(CycAccess cyc, String cycl)
          throws CycApiException, CycConnectionException {
    return new FormulaSentenceImpl(cyc.getObjectTool().makeCycList(cyc.cyclifyString(cycl)));
  }

  /**
   * Make a new conjunction conjoining the arguments.
   *
   * @see #isConjunction()
   * @param conjuncts
   * @return a new FormulaSentence from conjunction of input sentences
   */
  public static FormulaSentence makeConjunction(
          FormulaSentence... conjuncts) {
    return makeConjunction(Arrays.asList(conjuncts));
  }

  /**
   * Make a new conjunction from the elements of conjuncts.
   *
   * @see #isConjunction()
   * @param conjuncts
   * @return a new FormulaSentence from conjunction of input sentences
   */
  public static FormulaSentence makeConjunction(
          Iterable<FormulaSentence> conjuncts) {
    final FormulaSentenceImpl newSentence = (FormulaSentenceImpl) makeFormulaSentence(CommonConstants.AND);
    for (final Object conjunct : conjuncts) {
      newSentence.addArg(conjunct);
    }
    return newSentence;
  }

  /**
   * Make a new disjunction from the elements of conjuncts.
   *
   * @param conjuncts
   * @return a new FormulaSentence from disjunction of input sentences
   */
  public static FormulaSentence makeDisjunction(
          Iterable<FormulaSentence> conjuncts) {
    final FormulaSentenceImpl newSentence = (FormulaSentenceImpl) makeFormulaSentence(CommonConstants.OR);
    for (final Object conjunct : conjuncts) {
      newSentence.addArg(conjunct);
    }
    return newSentence;
  }

  /**
   * Make a new conditional sentence with the specified antecedent and consequent.
   *
   * @see #isConditionalSentence()
   * @param antecedent
   * @param consequent
   * @return a new conditional FormulaSentence
   */
  public static FormulaSentence makeConditional(FormulaSentence antecedent,
          FormulaSentence consequent) {
    return FormulaSentenceImpl.makeFormulaSentence(CommonConstants.IMPLIES, antecedent, consequent);
  }

  /**
   * Make a negated form of the specified sentence.
   *
   * @param sentence
   * @return a new negated FormulaSentence
   */
  public static FormulaSentence makeNegation(FormulaSentence sentence) {
    return FormulaSentenceImpl.makeFormulaSentence(CommonConstants.NOT, sentence);
  }

  /**
   * Convert obj to a FormulaSentenceImpl if it looks like it could be one.
   *
   * @param obj
   * @return a FormulaSentence if possible
   */
  static public Object convertIfPromising(final Object obj) {
    if (obj instanceof List && !(obj instanceof FormulaSentenceImpl)) {
      final List<Object> termAsList = (List) obj;
      if (termAsList.isEmpty()) {
        return obj;
      }
      Object possiblePred = termAsList.get(0);
      if (possiblePred instanceof CycConstant) {
        if (Character.isLowerCase(((CycConstant) possiblePred).getName().charAt(0))) {
          return new FormulaSentenceImpl(termAsList);
        }
      } else {
        possiblePred = NautImpl.convertIfPromising(possiblePred);
        if (possiblePred instanceof NonAtomicTerm && termAsList.size() > 1) {
          //Any nat might be a predicate, and therefore this could be a sentence.  If this is too weak, might need to add a call to Cyc.
          termAsList.set(0, possiblePred);
          return new FormulaSentenceImpl(termAsList);
        }
      }
    }
    return obj;
  }
  
  
  //====|    Construction    |====================================================================//
  
  /**
   * Create and return a new CycSentence whose arguments are terms. CycArrayList arguments will be
   * converted to CycNauts or CycSentences.
   *
   * @param terms
   */
  public FormulaSentenceImpl(Iterable<? extends Object> terms) {
    super(terms);
  }
  
  private FormulaSentenceImpl() {
  }
  
  
  //====|    Instance methods    |================================================================//
  
  @Override
  public boolean isConditionalSentence() {
    if (CommonConstants.IMPLIES.equals(getOperator())) {
      return true;
    } else
      return isConjunction() && getArity() == 1
              && ((FormulaSentenceImpl) getArg(1)).isConditionalSentence();
  }
  
  @Override
  public boolean isConjunction() {
    return (CommonConstants.AND.equals(getOperator()));
  }

  @Override
  public boolean isNegated() {
    return (CommonConstants.NOT.equals(getOperator()));
  }

  @Override
  public boolean isLogicalConnectorSentence() {
    return isLogicalOperatorFort(getOperator());
  }
  
  @Override
  public boolean isExistential() {
    final Object operator = getOperator();
    return CommonConstants.THERE_EXISTS.equals(operator)
            || CommonConstants.THERE_EXIST_VARS.equals(operator);
  }
  
  @Override
  public void existentiallyBind(CycVariable var) {
    synchronized (args) {
      final Object oldArgs = this.clone();
      args.clear();
      args.add(oldArgs);
      args.add(0, var);
      args.add(0, CommonConstants.THERE_EXISTS);
    }
  }
  
  @Override
  public void existentiallyUnbind(CycVariable var) {
    synchronized (args) {
      existentiallyUnbindSimple(var);
      existentiallyUnbindMultiple(var);
    }
  }

  private void existentiallyUnbindSimple(CycVariable var) {
    final Set<ArgPosition> existentialPositions = new HashSet<>(1);
    for (final ArgPositionTrackingTreeWalker argWalker = new ArgPositionTrackingTreeWalker();
            argWalker.hasNext();) {
      if (CommonConstants.THERE_EXISTS.equals(argWalker.next()) && argWalker.getCurrentArgPosition().last() == 0) {
        final ArgPosition quantPosition = argWalker.getCurrentArgPosition();
        final ArgPosition parentPosition = quantPosition.deepCopy().toParent();
        final ArgPosition varPosition = parentPosition.deepCopy().extend(1);
        if (var.equals(getSpecifiedObject(varPosition))) {
          existentialPositions.add(parentPosition);
        }
      }
    }
    for (final ArgPosition existentialPosition : existentialPositions) {
      setSpecifiedObject(existentialPosition, getSpecifiedObject(
              existentialPosition.deepCopy().extend(2)));
    }
  }

  private void existentiallyUnbindMultiple(CycVariable var) {
    final Set<ArgPosition> varsPositions = new HashSet<>(1);
    for (final ArgPositionTrackingTreeWalker argWalker = new ArgPositionTrackingTreeWalker();
            argWalker.hasNext();) {
      if (CommonConstants.THERE_EXIST_VARS.equals(argWalker.next()) && argWalker.getCurrentArgPosition().last() == 0) {
        final ArgPosition quantPosition = argWalker.getCurrentArgPosition();
        final ArgPosition parentPosition = quantPosition.deepCopy().toParent();
        final ArgPosition varsPosition = parentPosition.deepCopy().extend(1);
        final Object vars = getSpecifiedObject(varsPosition);
        if (vars instanceof Collection || ((Collection) vars).contains(var)) {
          varsPositions.add(varsPosition);
        }
      }
    }
    for (final ArgPosition varsPosition : varsPositions) {
      final Collection vars = (Collection) getSpecifiedObject(varsPosition);
      if (vars.size() == 1) {
        final ArgPosition parentPosition = varsPosition.deepCopy().toParent();
        setSpecifiedObject(parentPosition, getSpecifiedObject(
                parentPosition.deepCopy().extend(2)));
      } else {
        vars.remove(var);
        setSpecifiedObject(varsPosition, vars);
      }
    }
  }
  
  @Override
  public boolean isUniversal() {
    return CommonConstants.FOR_ALL.equals(getOperator());
  }
  
  @Override
  public Map<CycVariable, String> getOptimizedVarNames(CycAccess access) throws CycConnectionException {
    Map<CycVariable, String> retMap = new HashMap<>();
    String command = makeSublStmt(PPH_OPTIMIZED_NAMES_FOR_VARIABLES, this);
    @SuppressWarnings("unchecked")
    CycList<CycObject> resultList = access.converse().converseList(command);
    for (CycObject singleValue : resultList) {
      if (singleValue instanceof CycArrayList) {
        final CycArrayList dottedPair = (CycArrayList) singleValue;
        if (dottedPair.first() instanceof CycVariableImpl) {
          if (dottedPair.getDottedElement() instanceof String) {
            retMap.put((CycVariableImpl) dottedPair.first(),
                    (String) (dottedPair.getDottedElement()));
          }
        } else {
          optimizedVarProblemResult(singleValue);
        }
      } else {
        optimizedVarProblemResult(singleValue);
      }
    }
    return retMap;
  }

  private void optimizedVarProblemResult(CycObject singleValue) throws BaseClientRuntimeException {
    throw new BaseClientRuntimeException(
            "Unable to interpret " + singleValue + " as an optimized variable name.");
  }
  
  @Override
  public FormulaSentenceImpl splice(FormulaSentence toInsert,
          ArgPosition argPosition,
          CycAccess access) throws CycConnectionException, OpenCycUnsupportedFeatureException {
    ADVANCED_SENTENCE_REQUIREMENTS.throwRuntimeExceptionIfIncompatible();
    final String command = SublApiHelper.makeSublStmt(
            "combine-formulas-at-position", this, toInsert, argPosition);
    final List result = access.converse().converseList(command);
    return new FormulaSentenceImpl(result);
  }
  
  @Override
  public List<Object> getCandidateReplacements(ArgPosition position,
          ElMt mt, CycAccess cyc) throws CycConnectionException, OpenCycUnsupportedFeatureException {
    ADVANCED_SENTENCE_REQUIREMENTS.throwRuntimeExceptionIfIncompatible();
    final String command = SublApiHelper.makeSublStmt(
            "candidate-replacements-for-arg", this, position, mt);
    return cyc.converse().converseList(command);
  }
  
  @Override
  public boolean isValidReplacement(ArgPosition position, Object newTerm,
          ElMt mt,
          CycAccess cyc) {
    final FormulaSentenceImpl subbed = this.deepCopy();
    subbed.setSpecifiedObject(position, newTerm);
    return subbed.getNonWffExplanation(cyc, mt) == null;
  }
  
  @Override
  public CycSentence getEqualsFoldedSentence(CycAccess access) throws CycConnectionException {
    return getEqualsFoldedSentence(access, CommonConstants.CURRENT_WORLD_DATA_MT);
  }
  
  @Override
  public CycSentence getEqualsFoldedSentence(CycAccess access, ElMt mt) throws CycConnectionException {
    String command = null;
    try {
      command = "(with-inference-mt-relevance " + mt.stringApiValue() + " (fold-equals " + this.stringApiValue() + "))";
    } catch (Exception ex) {
      throw (new BaseClientRuntimeException(ex));
    }
    Object rawResult = access.converse().converseObject(command);
    CycSentence result;
    if (rawResult instanceof CycArrayList) {
      result = new FormulaSentenceImpl((CycArrayList) rawResult);
    } else if (rawResult instanceof CycConstantImpl) {
      result = new CycConstantSentenceImpl((CycConstantImpl) rawResult);
    } else {
      throw new CycApiException(
              "getEqualsFoldedSentence returned " + rawResult
              + ", which is not a CycSentence.\nOriginal input: " + this.toString());
    }
    //System.out.println("FOLDED TO: "+result.toString());
    return result;
  }
  
  @Override
  public CycSentence getSimplifiedSentence(CycAccess access) throws CycConnectionException, OpenCycUnsupportedFeatureException {
    return getSimplifiedSentence(access, getDefaultSimplifierMt());
  }
  
  @Override
  public CycSentence getSimplifiedSentence(CycAccess access, ElMt mt) throws CycConnectionException, OpenCycUnsupportedFeatureException {
    ADVANCED_SENTENCE_REQUIREMENTS.throwRuntimeExceptionIfIncompatible();
    String command = null;
    try {
      command = "(with-inference-mt-relevance " + mt.stringApiValue() + " (simplify-cycl-sentence (fold-equals "
              + this.stringApiValue() + ")))";
      //System.out.println("TRYING TO SIMPLIFY WITH:"+ command);
    } catch (Exception ex) {
      throw (new BaseClientRuntimeException(ex));
    }
    Object rawResult = access.converse().converseObject(command);
    CycSentence result;
    if (rawResult instanceof CycArrayList) {
      result = new FormulaSentenceImpl((CycArrayList) rawResult);
    } else if (rawResult instanceof CycConstantImpl) {
      result = new CycConstantSentenceImpl((CycConstantImpl) rawResult);
    } else {
      throw new CycApiException(
              "getSimplifiedSentence returned " + rawResult
              + ", which is not a CycSentence.\nOriginal input: " + this.toString());
    }
    //System.out.println("SIMPLIFIED TO: "+result.toString());
    return result;
  }
  
  @Override
  public FormulaSentenceImpl getExpandedSentence(CycAccess access) throws CycConnectionException {
    return getExpandedSentence(access, getDefaultSimplifierMt());
  }
  
  @Override
  public FormulaSentenceImpl getExpandedSentence(CycAccess access, ElMt mt) throws CycConnectionException {
    String command = null;
    try {
      command = "(el-expand-all " + this.stringApiValue() + " " + mt.stringApiValue() + ")";
    } catch (Exception ex) {
      throw (new BaseClientRuntimeException(ex));
    }
    Object rawResult = access.converse().converseObject(command);
    FormulaSentenceImpl result;
    if (rawResult instanceof CycArrayList) {
      result = new FormulaSentenceImpl((CycArrayList) rawResult);
    } else {
      throw new CycApiException(
              "getExpandedSentence returned " + rawResult
              + ", which is not a CycFormulaSentence.\nOriginal input: " + this.toString());
    }
    //System.out.println("SIMPLIFIED TO: "+result.toString());
    return result;
  }
  
  @Override
  public FormulaSentenceImpl getCanonicalElSentence(CycAccess access) throws CycConnectionException {
    return getCanonicalElSentence(access, getDefaultSimplifierMt(), true);
  }
  
  @Override
  public FormulaSentenceImpl getCanonicalElSentence(CycAccess access,
          Boolean canonicalizeVars) throws CycConnectionException {
    return getCanonicalElSentence(access, getDefaultSimplifierMt(),
            canonicalizeVars);
  }
  
  @Override
  public FormulaSentenceImpl getCanonicalElSentence(CycAccess access, ElMt mt,
          Boolean canonicalizeVars) throws CycConnectionException {
    String command = null;
    //need to add the following to the command..." " + DefaultCycObjectImpl.stringApiValue(canonicalizeVars) +
    try {
      command = "(canonicalize-el-sentence " + this.stringApiValue() + " " + mt.stringApiValue() + " " + DefaultCycObjectImpl.stringApiValue(
              canonicalizeVars) + ")";
    } catch (Exception ex) {
      throw (new BaseClientRuntimeException(ex));
    }
    Object rawResult = access.converse().converseObject(command);
    FormulaSentenceImpl result;
    if (rawResult instanceof CycArrayList) {
      result = new FormulaSentenceImpl((CycArrayList) rawResult);
    } else {
      throw new CycApiException(
              "getCanonicalElSentence returned " + rawResult
              + ", which is not a CycFormulaSentence.\nOriginal input: " + this.toString());
    }
    return result;
  }
  
  @Override
  public boolean hasWffConstraintViolations(CycAccess access, ElMt mt) {
    try {
      String command = "(el-lenient-wff-assertible? "
              + this.stringApiValue() + " " + mt.stringApiValue() + ")";
      Object rawResult = access.converse().converseObject(command);
      boolean equalsT = CycObjectFactory.t.equals(rawResult);
      return (!equalsT);
    } catch (Exception ex) {
      throw (new CycApiException(
              "Unable to decide whether " + this + " is well-formed in " + mt,
              ex));
    }
  }
  
  @Override
  public String getNonWffAssertExplanation(CycAccess access) {
    try {
      return getNonWffAssertExplanation(access, CommonConstants.CURRENT_WORLD_DATA_MT);
    } catch (Exception ex) {
      throw (new CycApiException(
              "Unable to retrieve explanation for why " + this + " is not well-formed in " + CommonConstants.CURRENT_WORLD_DATA_MT,
              ex));
    }
  }
  
  @Override
  public String getNonWffAssertExplanation(CycAccess access, ElMt mt) {
    try {
      String command = "(with-inference-mt-relevance " + mt.stringApiValue()
              + " (opencyc-explanation-of-why-not-wff-assert "
              + this.stringApiValue() + " " + mt.stringApiValue() + "))";
      Object rawResult = access.converse().converseObject(command);
      if (rawResult instanceof String) {
        return (String) rawResult;
      } else {
        return null;
      }
    } catch (Exception ex) {
      throw (new CycApiException(
              "Unable to retrieve explanation for why " + this + " is not well-formed in " + mt,
              ex));
    }
  }
  
  @Override
  public String getNonWffExplanation(CycAccess access) {
    return getNonWffExplanation(access, getDefaultSimplifierMt());
  }
  
  @Override
  public String getNonWffExplanation(CycAccess access, ElMt mt) {
    try {
      String command = "(with-inference-mt-relevance " + mt.stringApiValue()
              + " (opencyc-explanation-of-why-not-wff "
              + this.stringApiValue() + " " + mt.stringApiValue() + "))";
      Object rawResult = access.converse().converseObject(command);
      if (rawResult instanceof String) {
        return (String) rawResult;
      } else {
        return null;
      }
    } catch (Exception ex) {
      throw (new CycApiException(
              "Unable to retrieve explanation for why " + this + " is not well-formed in " + mt,
              ex));
    }
  }
  
  @Override
  public FormulaSentenceImpl deepCopy() {
    return new FormulaSentenceImpl(super.deepCopy().getArgsUnmodifiable());
  }
  
  @Override
  public FormulaSentenceImpl substituteNonDestructive(Object original,
          Object replacement) {
    Map<Object, Object> map = new HashMap<>();
    map.put(original, replacement);
    return (FormulaSentenceImpl) this.applySubstitutionsNonDestructive(map);
  }
  
  @Override
  public void substituteDestructive(Object original, Object replacement) {
    Map<Object, Object> map = new HashMap<>();
    map.put(original, replacement);
    this.applySubstitutionsDestructive(map);
  }
  
  @Override
  public FormulaSentence treeSubstitute(CycAccess access,
          Map<CycObject, Object> substitutions) throws CycApiException {
    CycList terms = this.toDeepCycList();
    if (substitutions != null) {
      for (final Map.Entry<CycObject, Object> entry : substitutions.entrySet()) {
        final CycObject oldTerm = entry.getKey();
        final Object newTerm = entry.getValue();
        terms = terms.subst(maybeListify(newTerm), maybeListify(oldTerm));
      }
    }
    return new FormulaSentenceImpl(terms);
  }

  private static Object maybeListify(Object term) {
    if (term instanceof NonAtomicTerm) {
      return ((NonAtomicTerm) term).toDeepCycList();
    } else if (term instanceof Formula) {
      return ((Formula) term).toDeepCycList();
    } else {
      return term;
    }
  }
  
  @Override
  public CycList findIndexicals(CycAccess cyc) throws CycApiException, CycConnectionException  {
    final String command = makeSublStmt(
            "expression-gather", this, new SublApiHelper.AsIsTerm("'" + INDEXICAL_P));
    return cyc.converse().converseList(command);
  }
  
  @Override
  public Object clone() {
    return new FormulaSentenceImpl(args);
  }

  @Override
  public int compareTo(Object o) {
    if (o instanceof FormulaSentenceImpl) {
      return args.compareTo(((FormulaSentenceImpl) o).args);
    } else {
      return 0;
    }
  }
  
}
