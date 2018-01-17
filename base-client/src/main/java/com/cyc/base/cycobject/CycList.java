package com.cyc.base.cycobject;

/*
 * #%L
 * File: CycList.java
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

import com.cyc.base.exception.BaseClientRuntimeException;
import com.cyc.kb.ArgPosition;
import java.io.Serializable;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Provides the behavior and attributes of a Base Client list, typically used
 * to represent assertions in their external (EL) form.
 * 
 * @author nwinant
 * @param <E> the type of elements in this list
 */
public interface CycList<E> extends CycObject, List<E>, Serializable {
  
  /**
   * the limit on lists that are returned as one LIST expression;
   * lists longer than this are broken down into NCONC of LISTs expressions
   */
  int MAX_STRING_API_VALUE_LIST_LITERAL_SIZE = 2048;

  @Override
  boolean add(E e);

  /**
   * Adds the given elements to this list if they are not already contained.
   * @param objects
   * @return the list after adding the new elements
   */
  CycList<E> addAllNew(Collection<? extends E> objects);

  /**
   * Adds the given element to this list if it is not already contained.
   * @param object
   * @return the list after adding the new element
   */
  CycList<E> addNew(E object);

  /**
   * Forms a quote expression for the given object and adds it to the list.
   *
   * @param object the object to be quoted and added to this list
   * @return the list after adding the quoted element
   */
  CycList<E> addQuoted(Object object);

  /**
   * Inserts the specified element at the beginning of this list.
   * Shifts all current elements to the right (adds one to their indices).
   *
   * @param element element to be inserted
   * @return the list after inserting the given element at the beginning
   */
  CycList<E> addToBeginning(E element);

  /**
   * Returns a <tt>CycList</tt> of all the indices of the given element within this CycList.
   *
   * @param elem The element to search for in the list
   * @return a <tt>CycList</tt> of all the indices of the given element within this CycList.
   */
  CycList<Integer> allIndicesOf(E elem);

  /**
   * Appends the given element to the end of the list and returns the list (useful when nesting method calls).
   *
   * @param object the object element to add
   * @return the list after adding the given element to the end
   */
  CycList<E> appendElement(E object);
  
  /* *
   * Appends the given element to the end of the list and returns the list (useful when nesting method calls).
   *
   * @param i the integer element to add
   * @return the list after adding the given element to the end
   * /
  CycList<? extends E> appendElement(int i);

  /* *
   * Appends the given element to the end of the list and returns the list (useful when nesting method calls).
   *
   * @param l the long element to add
   * @return the list after adding the given element to the end
   * /
  CycList<? extends E> appendElement(long l);

  /* *
   * Appends the given element to the end of the list and returns the list (useful when nesting method calls).
   *
   * @param b the boolean element to add
   * @return the list after adding the given element to the end
   * /
  CycList<? extends E> appendElement(boolean b);
  */
  
  /**
   * Appends the given elements to the end of the list and returns the list (useful when nesting method calls).
   *
   * @param cycList the elements to add
   * @return the list after adding the given elements to the end
   */
  CycList<E> appendElements(Collection<? extends E> cycList);

  /**
   * Creates and returns a copy of this <tt>CycList</tt>.
   *
   * @return a clone of this instance
   */
  Object clone();

  /**
   * Returns a <tt>CycList</tt> of the length N combinations of sublists from this
   * object.  This algorithm preserves the list order with the sublists.
   *
   * @param n the length of the sublist
   * @return a <tt>CycList</tt> of the length N combinations of sublists from this
   * object
   */
  CycList<CycList<? extends E>> combinationsOf(int n);

  /**
   * Returns true iff this list contains duplicate elements.
   *
   * @return true iff this list contains duplicate elements
   */
  boolean containsDuplicates();

  /**
   * Returns this object in a form suitable for use as an <tt>CycList</tt> api expression value.
   *
   * @param shouldQuote Should the list be SubL-quoted?
   * @return this object in a form suitable for use as an <tt>CycList</tt> api expression value
   */
  Object cycListApiValue(boolean shouldQuote);

  /**
   * Returns a <tt>CycListVisitor</tt> enumeration of the non-CycList and non-nil elements.
   *
   * @return a <tt>CycListVisitor</tt> enumeration of the non-CycList and non-nil elements.
   */
  Enumeration<E> cycListVisitor();

  /**
   * Returns a cyclified string representation of the <tt>CycList</tt>.
   * Embedded constants are prefixed with "#$".
   *
   * @return a <tt>String</tt> representation in cyclified form.
   *
   */
  @Override
  String cyclify();

  /**
   * Returns a cyclified string representation of the <tt>CycList</tt>.
   * Embedded constants are prefixed with "#$".  Embedded quote and backslash
   * chars in strings are escaped.
   *
   * @return a <tt>String</tt> representation in cyclified form.
   *
   */
  @Override
  String cyclifyWithEscapeChars();

  /**
   * Returns a cyclified string representation of the <tt>CycList</tt>.
   * Embedded constants are prefixed with "#$".  Embedded quote and backslash
   * chars in strings are escaped.
   *
   * @param isApi Should the list be cyclified for as an API call?
   * @return a <tt>String</tt> representation in cyclified form.
   *
   */
  String cyclifyWithEscapeChars(boolean isApi);

  /**
   * Creates and returns a deep copy of this <tt>CycList</tt>.  In a deep copy,
   * directly embedded <tt>CycList</tt> objects are also deep copied.  Objects
   * which are not CycLists are cloned.
   *
   * @return a deep copy of this <tt>CycList</tt>
   */
  CycList<E> deepCopy();

  /**
   * Destructively delete duplicates from the list.
   * @return <code>this</code> list with the duplicates deleted.
   */
  CycList<E> deleteDuplicates();

  /**
   * Returns <tt>true</tt> if the element is a member of this <tt>CycList</tt> and
   * no element in the Collection <tt>otherElements</tt> precede it.
   *
   * @param element the element under consideration
   * @param otherElements the <tt>CycList</tt> of other elements under consideration
   * @return <tt>true</tt> if the element is a member of this <tt>CycList</tt> and
   * no elements in <tt>CycList</tt> otherElements contained in this <tt>CycList</tt>
   * precede it
   */
  boolean doesElementPrecedeOthers(E element, Collection<? extends E> otherElements);

  /** Returns true if the given object is equal to this object as EL CycL expressions
   *
   * @param o the given object
   * @return true if the given object is equal to this object as EL CycL expressions, otherwise
   * return false
   */
  boolean equalsAtEL(Object o);

  E findElementAfter(E searchObject, E notFound);

  E findElementAfter(E searchObject);

  /**
   * Returns the first element of the <tt>CycList</tt>.
   *
   * @return the <tt>Object</tt> which is the first element of the list.
   * @throws BaseClientRuntimeException if list is not long enough.
   */
  E first();

  /**
   * Flatten the list. Recursively iterate through tree, and return a list of
   * the atoms found.
   * @return List of atoms in <code>this</code> CycList.
   */
  CycList<E> flatten();

  /**
   * Returns the fourth element of the <tt>CycList</tt>.
   *
   * @return the <tt>Object</tt> which is the fourth element of the list.
   * @throws BaseClientRuntimeException if list is not long enough.
   */
  E fourth();

  /** Returns a list of arg positions that describe all the locations where
   * the given term can be found in this CycList. An arg position is a flat
   * list of Integers that give the nths (0 based) to get to a particular
   * sub term in a tree.
   * @param term The term to search for
   * @return The list of all arg postions where term can be found
   * class where possible.
   */
  List<ArgPosition> getArgPositionsForTerm(E term);

  /**
   * Gets the dotted element.
   *
   * @return the <tt>Object</tt> which forms the dotted element of this <tt>CycList</tt>
   */
  E getDottedElement();

  int getProperListSize();

  /**
   * Returns the object from the this CycList according to the
   * path specified by the given arg position.
   *
   * @param argPosition the given arg position
   * @return the object from this CycList according to the
   * path specified by the given (n1 n2 ...) zero-indexed path expression
   */
  Object getSpecifiedObject(ArgPosition argPosition);

  /**
   * Returns the object from the this CycList according to the
   * path specified by the given (n1 n2 ...) zero-indexed path expression.
   *
   * @param pathSpecification the given (n1 n2 ...) zero-indexed path expression
   * @return the object from this CycList according to the
   * path specified by the given (n1 n2 ...) zero-indexed path expression
   */
  Object getSpecifiedObject(List<Integer> pathSpecification);

  /**
   * Returns whether the list is a lisp PLIST.
   * 
   * @return true iff this CycList is a property-list, which is a list with an even number of elements,
   * consisting of alternating keywords and values, with no repeating keywords.
   * 
   * @see #getPlistKeys() 
   * @see #getValueForKeyword(com.cyc.base.cycobject.CycSymbol) 
   * @see #getf(com.cyc.base.cycobject.CycSymbol) 
   * @see #getf(com.cyc.base.cycobject.CycSymbol, java.lang.Object) 
   * @see #getf(com.cyc.base.cycobject.CycSymbol, java.lang.Object, boolean) 
   * @see #setf(com.cyc.base.cycobject.CycSymbol, java.lang.Object) 
   * @see #removeProperty(com.cyc.base.cycobject.CycSymbol) 
   * @see #toPlistMap() 
   */
  boolean isPlist();
  
  /**
   * Retrieves all PLIST keys.
   * 
   * @return 
   * 
   * @see #isPlist() 
   */
  public List<CycSymbol> getPlistKeys();
  
  /**
   * Gets the value following the given keyword symbol.
   *
   * @param keyword the keyword symbol
   * @return the value following the given keyword symbol, or null if not found
   * 
   * @see #isPlist() 
   * @see #getf(com.cyc.base.cycobject.CycSymbol) 
   */
  E getValueForKeyword(CycSymbol keyword);

  /**
   * This behaves like the SubL function GETF, but returns null if the indicator is not present.
   * @param indicator
   * @return the value for indicator, or <tt>null</tt> if the indicator is not present
   * 
   * @see #isPlist() 
   * @see #getValueForKeyword(com.cyc.base.cycobject.CycSymbol) 
   * @see #getf(com.cyc.base.cycobject.CycSymbol, java.lang.Object) 
   * @see #getf(com.cyc.base.cycobject.CycSymbol, java.lang.Object, boolean) 
   * @see #setf(com.cyc.base.cycobject.CycSymbol, java.lang.Object) 
   */
  E getf(CycSymbol indicator);

  /**
   * This behaves like the SubL function GETF
   * @param indicator
   * @param defaultResult
   * @return the value for indicator, or <tt>defaultResult</tt> if the indicator is not present
   * 
   * @see #isPlist() 
   * @see #getValueForKeyword(com.cyc.base.cycobject.CycSymbol) 
   * @see #getf(com.cyc.base.cycobject.CycSymbol) 
   * @see #getf(com.cyc.base.cycobject.CycSymbol, java.lang.Object, boolean) 
   * @see #setf(com.cyc.base.cycobject.CycSymbol, java.lang.Object) 
   * @see #removeProperty(com.cyc.base.cycobject.CycSymbol) 
   */
  E getf(CycSymbol indicator, E defaultResult);
  
  /**
   * This behaves like the SubL function GETF.
   * 
   * @param indicator
   * @param defaultResult
   * @param treatNilAsAbsent -- If true, return defaultResult when list contains NIL for indicator
   * @return the value for indicator, or <tt>defaultResult</tt> if the indicator is not present
   * 
   * @see #isPlist() 
   * @see #getValueForKeyword(com.cyc.base.cycobject.CycSymbol) 
   * @see #getf(com.cyc.base.cycobject.CycSymbol, java.lang.Object) 
   * @see #getf(com.cyc.base.cycobject.CycSymbol) 
   * @see #setf(com.cyc.base.cycobject.CycSymbol, java.lang.Object) 
   * @see #removeProperty(com.cyc.base.cycobject.CycSymbol) 
   */
  E getf(CycSymbol indicator, E defaultResult, boolean treatNilAsAbsent);
  
  /**
   * Sets the value for a property, much like SETF.
   * 
   * @param indicator
   * @param value
   * @return 
   * 
   * @see #isPlist() 
   * @see #getValueForKeyword(com.cyc.base.cycobject.CycSymbol) 
   * @see #getf(com.cyc.base.cycobject.CycSymbol) 
   * @see #getf(com.cyc.base.cycobject.CycSymbol, java.lang.Object) 
   * @see #getf(com.cyc.base.cycobject.CycSymbol, java.lang.Object, boolean) 
   * @see #removeProperty(com.cyc.base.cycobject.CycSymbol) 
   */
  E setf(CycSymbol indicator, E value);  // TODO: add test coverage - nwinant, 2017-08-04
  
  /**
   * Removes a property and its value from the list, much like REMPROP.
   * @param indicator
   * @return 
   * 
   * @see #isPlist() 
   * @see #getValueForKeyword(com.cyc.base.cycobject.CycSymbol) 
   * @see #getf(com.cyc.base.cycobject.CycSymbol) 
   * @see #getf(com.cyc.base.cycobject.CycSymbol, java.lang.Object) 
   * @see #getf(com.cyc.base.cycobject.CycSymbol, java.lang.Object, boolean) 
   * @see #setf(com.cyc.base.cycobject.CycSymbol, java.lang.Object) 
   */
  boolean removeProperty(CycSymbol indicator);  // TODO: add test coverage - nwinant, 2017-08-04
  
  // TODO: review PLIST methods - nwinant, 2017-08-04
  
  /**
   * Adds a CONS pair. Used to build lisp ALISTs.
   * 
   * @param key
   * @param value
   * @return 
   * 
   * @see #removePairs(java.lang.Object) 
   * @see #isPlist() 
   * @see #toMap() 
   */
  boolean addPair(Object key, Object value);  // TODO: add test coverage - nwinant, 2017-08-04
  
  /**
   * Removes CONS pairs with <tt>key</tt>.
   * 
   * @param key 
   * 
   * @see #addPair(java.lang.Object, java.lang.Object) 
   * @see #toMap() 
   */
  void removePairs(Object key);  // TODO: add test coverage - nwinant, 2017-08-04
  
  // TODO: add other ALIST methods - nwinant, 2017-08-04
  
  /**
   * Returns <tt>true</tt> if this is a proper list.
   *
   * @return <tt>true</tt> if this is a proper list, otherwise return <tt>false</tt>
   */
  boolean isProperList();

  /**
   * Answers true iff the CycList contains valid elements.  This is a necessary, but
   * not sufficient condition for CycL well-formedness.
   * 
   * @return 
   */
  boolean isValid();

  /**
   * Returns an iterator over the elements in this list in proper sequence.
   *
   * <p>The returned iterator is <a href="#fail-fast"><i>fail-fast</i></a>.
   *
   * @return an iterator over the elements in this list in proper sequence
   */
  @Override
  Iterator<E> iterator();

  /**
   * Returns the last element of the <tt>CycList</tt>.
   *
   * @return the <tt>Object</tt> which is the last element of the list.
   * @throws BaseClientRuntimeException if list is not long enough.
   */
  E last();

  /**
   * Returns a list iterator over the elements in this list (in proper
   * sequence), starting at the specified position in the list.
   * The specified index indicates the first element that would be
   * returned by an initial call to {@link ListIterator#next next}.
   * An initial call to {@link ListIterator#previous previous} would
   * return the element with the specified index minus one.
   *
   * <p>The returned list iterator is <a href="#fail-fast"><i>fail-fast</i></a>.
   *
   * @throws IndexOutOfBoundsException {@inheritDoc}
   */
  @Override
  ListIterator<E> listIterator(int index);

  /**
   * Returns a list iterator over the elements in this list (in proper
   * sequence).
   *
   * <p>The returned list iterator is <a href="#fail-fast"><i>fail-fast</i></a>.
   *
   * @see #listIterator(int)
   */
  @Override
  ListIterator<E> listIterator();

  /**
   * Returns a random ordering of the <tt>CycList</tt> without recursion.
   *
   * @return a random ordering of the <tt>CycList</tt> without recursion
   */
  CycList<E> randomPermutation();

  /**
   * Remove duplicates from the list.  Just like #deleteDuplicates but
   * non-destructive.
   * @return A new list with the duplicates removed.
   */
  CycList<E> removeDuplicates();

  /**
   * Returns a new CycList formed by removing the first element, and in the case of a
   * dotted pair, returns the dotted element.
   *
   * @return the CycList after removing the first element, and in the case of a
   * dotted pair, returns the dotted element.
   * @throws BaseClientRuntimeException if list is not long enough.
   */
  Object rest();

  /**
   * Returns a new <tt>CycList</tt> whose elements are the reverse of
   * this <tt>CycList</tt>, which is unaffected.
   *
   * @return new <tt>CycList</tt> with elements reversed.
   */
  CycList<E> reverse();

  /**
   * Returns the second element of the <tt>CycList</tt>.
   *
   * @return the <tt>Object</tt> which is the second element of the list.
   * @throws BaseClientRuntimeException if list is not long enough.
   */
  E second();

  /**
   * Replaces the element at the specified position in this list with
   * the specified element.
   *
   * @param index index of element to replace.
   * @param element element to be stored at the specified position.
   * @return the element previously at the specified position.
   * @throws    IndexOutOfBoundsException if index out of range
   *		  <tt>(index &lt; 0 || index &gt;= size())</tt>.
   */
  @Override
  E set(int index, E element);

  /**
   * Sets the dotted element and set the improper list attribute to <tt>true</tt>.
   * @param dottedElement
   * @return the list after setting the dotted element
   */
  CycList<E> setDottedElement(E dottedElement);

  /**
   * Sets the object in this CycList to the given value according to the
   * path specified by the given ((n1 n2 ...) zero-indexed path expression.
   *
   * @param pathSpecification the (n1 n2 ...) zero-indexed path expression
   * @param value the given value
   * @return the list after setting the specified object
   */
  CycList<E> setSpecifiedObject(ArgPosition pathSpecification, E value);

  /**
   * Sets the object in this CycList to the given value according to the
   * path specified by the given ((n1 n2 ...) zero-indexed path expression.
   *
   * @param pathSpecification the (n1 n2 ...) zero-indexed path expression
   * @param value the given value
   * @return the list after setting the specified object
   */
  CycList<E> setSpecifiedObject(List<Integer> pathSpecification, E value);
  
  /**
   * Returns the CycList size including the optional dotted element.  
   * Note that this fools list iterators.
   *
   * @return the CycList size including the optional dotted element
   */
  @Override
  int size();

  /**
   * Returns a new CycList, which is sorted in the default collating sequence.
   *
   * @return a new <tt>CycList</tt>, sorted in the default collating sequence.
   */
  CycList<E> sort();

  /**
   * Returns this object in a form suitable for use as a <tt>String</tt> api expression value.
   *
   * @return this object in a form suitable for use as a <tt>String</tt> api expression value
   * @throws IllegalArgumentException if the total size of the list exceeds
   * MAX_STRING_API_VALUE_LIST_LITERAL_SIZE times MAX_STRING_API_VALUE_LIST_LITERAL_SIZE in size,
   * because of the danger of causing a stack overflow in the communication
   * with the SubL interpreter
   */
  @Override
  String stringApiValue();

  /**
   * Returns a new <tt>CycList</tt> with every occurrence of Object <tt>oldObject</tt> 
   * replaced by Object <tt>newObject</tt> newObject.  Substitute recursively into embedded
   * <tt>CycList</tt> objects.
   *
   * @param newObject
   * @param oldObject
   * @return a new <tt>CycList</tt> with every occurrence of <tt>Object</tt> oldObject
   * replaced by <tt>Object</tt> newObject
   */
  CycList<E> subst(E newObject, E oldObject);

  /**
   * Returns the third element of the <tt>CycList</tt>.
   *
   * @return the <tt>Object</tt> which is the third element of the list.
   * @throws BaseClientRuntimeException if list is not long enough.
   */
  E third() throws BaseClientRuntimeException;
  
  /** 
   * Convert this to a Map.  This method is only valid if the list is an association list.
   *
   * @return the Map
   * 
   * @see CycListMap#toList() 
   */
  CycListMap<? extends E, ? extends E> toMap();
  
  //CycListMap<CycSymbol, ? extends E> toPlistMap();
  
  /**
   * Returns a `pretty-printed' <tt>String</tt> representation of this
   * <tt>CycList</tt>.
   * @param indent the indent string that is added before the
   * <tt>String</tt> representation this <tt>CycList</tt>
   * @return a `pretty-printed' <tt>String</tt> representation of this
   * <tt>CycList</tt>.
   */
  String toPrettyCyclifiedString(String indent);

  /**
   * Returns a `pretty-printed' <tt>String</tt> representation of this
   * <tt>CycList</tt> with embedded strings escaped.
   * @param indent the indent string that is added before the
   * <tt>String</tt> representation this <tt>CycList</tt>
   * @return a `pretty-printed' <tt>String</tt> representation of this
   * <tt>CycList</tt>.
   */
  String toPrettyEscapedCyclifiedString(String indent);

  /**
   * Returns a `pretty-printed' <tt>String</tt> representation of this
   * <tt>CycList</tt>.
   * @param indent the indent string that is added before the
   * <tt>String</tt> representation this <tt>CycList</tt>
   * @return a `pretty-printed' <tt>String</tt> representation of this
   * <tt>CycList</tt>.
   */
  String toPrettyString(String indent);

  /**
   * Returns a `pretty-printed' <tt>String</tt> representation of this
   * <tt>CycList</tt>.
   * @param indent the indent string that is added before the
   * <tt>String</tt> representation this <tt>CycList</tt>
   * @param incrementIndent the indent string that to the <tt>String</tt>
   * representation this <tt>CycList</tt>is added at each level
   * of indenting
   * @param newLineString the string added to indicate a new line
   * @param shouldCyclify indicates that the output constants should have #$ prefix
   * @param shouldEscape  indicates that embedded strings should have appropriate escapes for the
   *                      SubL reader
   * @return a `pretty-printed' <tt>String</tt> representation of this
   * <tt>CycList</tt>.
   */
  String toPrettyStringInt(String indent, 
                           String incrementIndent, 
                           String newLineString,
                           boolean shouldCyclify,
                           boolean shouldEscape);
  
  /**
   * Returns the list of constants found in the tree
   *
   * @return the list of constants found in the tree
   */
  CycList<CycConstant> treeConstants();

  /**
   * Returns true if the proper list tree contains the given object anywhere in the tree.
   *
   * @param object the object to be found in the tree.
   * @return true if the proper list tree contains the given object anywhere in the tree
   */
  boolean treeContains(E object);

  /**
   * Returns the list of objects of the specified type found in the tree.
   *
   * @param cls What class to select from the tree
   * @return the list of objects of type <code>cls</code> found in the tree
   */
  CycList<E> treeGather(Class<E> cls);

  CycList<E> treeSubstitute(E oldObject, E newObject);
  
}
