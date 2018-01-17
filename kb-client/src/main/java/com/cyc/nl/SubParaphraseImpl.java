package com.cyc.nl;

/*
 * #%L
 * File: SubParaphraseImpl.java
 * Project: KB Client
 * %%
 * Copyright (C) 2013 - 2017 Cycorp, Inc
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

/**
 * A class representing a paraphrase of one argument of a formula, as part of a larger formula
 * paraphrase.
 *
 * @see FormulaParaphrase
 * @author baxter
 */
public class SubParaphraseImpl extends ParaphraseImpl implements SubParaphrase {

  /**
   * Creates a new sub-paraphrase object.
   *
   * @param parentParaphrase The parent paraphrase.
   * @param argPosition The arg position of the term paraphrased.
   * @param startIndex The start index of this paraphrase in the parent paraphrase.
   * @param nl The NL string of this paraphrase.
   * @param term The term of which this is a paraphrase.
   */
  protected SubParaphraseImpl(Paraphrase parentParaphrase, ArgPosition argPosition,
          int startIndex, String nl, Object term) {
    super(nl, term);
    this.parentParaphrase = parentParaphrase;
    this.argPosition = argPosition;
    this.startIndex = startIndex;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 53 * hash + (this.argPosition != null ? this.argPosition.hashCode() : 0);
    hash = 53 * hash + this.startIndex;
    hash = 53 * hash + (this.parentParaphrase != null ? this.parentParaphrase.hashCode() : 0);
    hash = 53 * hash + (this.nl != null ? this.nl.hashCode() : 0);
    hash = 53 * hash + (this.term != null ? this.term.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final SubParaphraseImpl other = (SubParaphraseImpl) obj;
    if (this.startIndex != other.startIndex) {
      return false;
    }
    if (this.argPosition != other.argPosition 
            && (this.argPosition == null || !this.argPosition.equals(other.argPosition))
            && (this.nl == null || !this.nl.equals(other.nl))
            && (this.term == null || !this.term.equals(other.term))) {
      return false;
    }
    return true;
  }

  /**
   * Returns the arg position of the term paraphrased in its parent formula.
   *
   * @return the arg position of the term paraphrased in its parent formula.
   */
  @Override
  public ArgPosition getArgPosition() {
    return argPosition;
  }

  /**
   * Returns the parent paraphrase of this sub-paraphrase.
   *
   * @return the parent paraphrase of this sub-paraphrase.
   */
  @Override
  public Paraphrase getParentParaphrase() {
    return parentParaphrase;
  }

  /**
   * Returns the start index of this paraphrase within its parent.
   *
   * @return the start index of this paraphrase within its parent.
   */
  @Override
  public int getStartIndex() {
    return startIndex;
  }
  private final ArgPosition argPosition;
  private final int startIndex;
  private final Paraphrase parentParaphrase;

}
