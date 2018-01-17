package com.cyc.baseclient.cycobject;

/*
 * #%L
 * File: CycArrayList.java
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
import com.cyc.base.cycobject.CycAssertion;
import com.cyc.base.cycobject.CycConstant;
import com.cyc.base.cycobject.CycList;
import com.cyc.base.cycobject.CycListMap;
import com.cyc.base.cycobject.CycObject;
import com.cyc.base.cycobject.CycSymbol;
import com.cyc.base.cycobject.CycVariable;
import com.cyc.base.cycobject.Formula;
import com.cyc.base.cycobject.Guid;
import com.cyc.base.cycobject.Nart;
import com.cyc.base.cycobject.NonAtomicTerm;
import com.cyc.base.exception.BaseClientRuntimeException;
import com.cyc.baseclient.CycObjectFactory;
import com.cyc.baseclient.datatype.CycStringUtils;
import com.cyc.baseclient.datatype.Span;
import com.cyc.baseclient.xml.TextUtil;
import com.cyc.baseclient.xml.XmlStringWriter;
import com.cyc.baseclient.xml.XmlWriter;
import com.cyc.kb.ArgPosition;
import com.cyc.kb.KbObject;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Stack;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * Provides the behavior and attributes of a Base Client list, typically used
 * to represent assertions in their external (EL) form.
 *
 * @version $0.1$
 * @author Stephen L. Reed
 * @param <E>
 */
public class CycArrayList<E> extends ArrayList<E> implements CycList<E>, CycObject {

  //====|    Public static methods    |===========================================================//
  
  /** Returns a new proper CycArrayList having the given elements as its initial elements.
   *
   * @param <T>
   * @param elements the initial elements
   * @return a new proper CycArrayList having the given elements as its initial elements
   */
  public static <T> CycList<T> makeCycList(final T... elements) {
    final CycArrayList<T> result = new CycArrayList<>();
    for (final T element : elements) {
      result.add(element);
    }
    return result;
  }

  /**
   * Constructs a CycArrayList using the semantics of Lisp symbolic expressions.<br>
   * 1.  construct(a, NIL) --> (a)<br>
   * 2.  construct(a, b) --> (a . b)<br>
   * 
   * <p>Deprecated: Use addToBeginning(E) or makeDottedPair(T,T) instead.
   * 
   * @param object1 the first <tt>Object</tt> in the <tt>CycArrayList</tt>
   * @param object2 <tt>NIL</tt> or an <tt>Object</tt>
   * @return <tt>CycArrayList</tt> (object) if <tt>object2</tt> is <tt>NIL</tt>,
   * otherwise return the improper <tt>CycArrayList</tt> (object1 . object2)
   */
  @Deprecated
  public static <T> CycList<T> construct(final T object1, final Object object2) {
    final CycArrayList<T> cycList = new CycArrayList<>(object1);
    if (object2.equals(CycObjectFactory.nil)) {
      return cycList;
    }
    if (object2 instanceof CycArrayList) {
      final CycArrayList cycList2 = (CycArrayList) object2;
      cycList.addAll(cycList2);
      return cycList;
    }
    cycList.setDottedElement((T) object2);
    return cycList;
  }
  
  /**
   * Constructs a CycArrayList with a normal element and a dotted element.<br>
   *
   * @param <T>
   * @param normalElement the normal <tt>Object</tt> in the <tt>CycArrayList</tt>
   * @param dottedElement the <tt>Object</tt> to be the dotted element.
   * @return the dotted pair <tt>CycArrayList</tt> (normalElement . dottedElement)
   */
  public static <T> CycList<T> makeDottedPair(final T normalElement, final T dottedElement) {
    if (CycObjectFactory.nil.equals(dottedElement)) {
      return new CycArrayList<>(normalElement);
    }
    final CycArrayList<T> result = new CycArrayList<>(normalElement);
    result.setDottedElement((T) dottedElement);
    return result;
  }
  
  /**
   * Creates a new <tt>CycArrayList</tt> containing the given element.
   *
   * @param <E>
   * @param element the contents of the new <tt>CycArrayList</tt>
   * @return a new <tt>CycArrayList</tt> containing the given element
   */
  public static <E> CycList<E> list(final E element) {
    final CycArrayList<E> result = new CycArrayList<>();
    result.add(element);
    return result;
  }

  /**
   * Creates a new <tt>CycArrayList</tt> containing the given two elements.
   *
   * @param <E>
   * @param element1 the first item of the new <tt>CycArrayList</tt>
   * @param element2 the second item of the new <tt>CycArrayList</tt>
   * @return a new <tt>CycArrayList</tt> containing the given two elements
   */
  public static <E> CycList<E> list(final E element1, final E element2) {
    final CycArrayList<E> result = new CycArrayList<>();
    result.add(element1);
    result.add(element2);
    return result;
  }

  /**
   * Creates a new <tt>CycArrayList</tt> containing the given three elements.
   *
   * @param <E>
   * @param element1 the first item of the new <tt>CycArrayList</tt>
   * @param element2 the second item of the new <tt>CycArrayList</tt>
   * @param element3 the third item of the new <tt>CycArrayList</tt>
   * @return a new <tt>CycArrayList</tt> containing the given three elements
   */
  public static <E> CycList<E> list(final E element1, final E element2, final E element3) {
    final CycArrayList<E> result = new CycArrayList<>();
    result.add(element1);
    result.add(element2);
    result.add(element3);
    return result;
  }

  public static <E extends KbObject> CycList<CycObject> fromKbObjects(final Collection<E> elts) {
    final CycArrayList<CycObject> result = new CycArrayList<>();
    elts.forEach((elt) -> {
      result.add((CycObject)(elt.getCore()));
    });
    return result;
  }
  
  public static int getProperListSize(List list) {
    if (list instanceof CycList && !((CycList)list).isProperList()) {
      return list.size() - 1;
    } else {
      return list.size();
    }
  }
  
  /**
   * Create a new association list from a map.
   * @param map
   * @return the association list that corresponds to m.
   */
  public static CycList fromMap(Map<?,?> map) {
    final CycList result = new CycArrayList<>();
    map.entrySet()
            .forEach((e) -> {
              result.add(CycArrayList.makeDottedPair(e.getKey(), e.getValue()));
    });
    return result;
  }
  
  private static boolean isProperList(List list) {
    return !(list instanceof CycList) || ((CycList) list).isProperList();
  }
  
  static String stringApiValue(List list) {
    if (list.isEmpty()) {
      return "(list)";
    }
    final int fullSlices = (list.size() / MAX_STRING_API_VALUE_LIST_LITERAL_SIZE);
    if (fullSlices > MAX_STRING_API_VALUE_LIST_LITERAL_SIZE) {
      // TODO: this could be improved upon, since the 
      // (NCONC (LIST ... slice1 ... ) (LIST ... slice2 ...))
      // trick could be wrapped with another level of NCONC to handle even bigger
      // expressions, and so on and so forth, requiring only 
      // MAX_STRING_API_VALUE_LIST_LITERAL_SIZE+nesting depth stack space 
      // at any one point ...
      throw new IllegalArgumentException("Cannot currently handle LISTs longer than "
              + MAX_STRING_API_VALUE_LIST_LITERAL_SIZE * MAX_STRING_API_VALUE_LIST_LITERAL_SIZE);
    }
    final int tailSliceStart = fullSlices * MAX_STRING_API_VALUE_LIST_LITERAL_SIZE;
    final boolean fitsIntoOneSlice = fullSlices == 0;
    final StringBuilder result = new StringBuilder(list.size() * 20);
    //final boolean properList = ((!(list instanceof CycList)) || ((CycList)list).isProperList());
    final boolean properList = isProperList(list);
    if (!fitsIntoOneSlice) {
      // we have multiple slices 
      result.append("(nconc").append(" ");
      for (int i = 0; i < fullSlices; i++) {
        int start = i * MAX_STRING_API_VALUE_LIST_LITERAL_SIZE;
        int end = start + MAX_STRING_API_VALUE_LIST_LITERAL_SIZE;
        // and full slices are ALL proper
        CycArrayList.appendSubSlice(list, result, start, end, true);
      }
    }
    // NOTE: if fullSlices is 0, tailSliceStart will be 0 also
    appendSubSlice(list, result, tailSliceStart, CycArrayList.getProperListSize(list), properList);
    if (!fitsIntoOneSlice) {
      result.append(")");
    }
    return result.toString();
  }
  
  /**
   * @param map
   * @return a new CycArrayList representing the contents of map as a plist.
   */
  public static CycArrayList<?> convertMapToPlist(final Map<CycSymbol,?> map) {
    final CycArrayList result = new CycArrayList<>();
    if (map != null) {
      map.entrySet().stream()
              .map((entry) -> {
                result.add(entry.getKey());
                result.add(entry.getValue());
                return entry;
              });
    }
    return result;
  }
  /*
  public static <O> Map<CycSymbol, O> convertPlistToMap(CycList<?> plist) {
    final Map<CycSymbol, O> results = new LinkedHashMap<>();
    for (CycSymbol key : plist.getPlistKeys()) {
      final O value = plist.
      results.setf(key, );
    }
    return results;
  }
  */
  /**
   * Writes a CycArrayList element the the given XML output stream.
   *
   * @param object the object to be serialized as XML
   * @param xmlWriter the output XML serialization writer
   * @param indent specifies by how many spaces the XML output should be indented
   * @param relative specifies whether the indentation should be absolute --
   * indentation with respect to the beginning of a new line, relative = false
   * -- or relative to the indentation currently specified in the indent_string field
   * of the xml_writer object, relative = true.
   * @throws java.io.IOException
   */
  @Deprecated
  public static void toXML(
          final Object object, 
          final XmlWriter xmlWriter, 
          final int indent,
          final boolean relative) throws IOException {
    final int startingIndent = xmlWriter.getIndentLength();
    if (object instanceof Integer) {
      xmlWriter.printXMLStartTag(INTEGER_XML_TAG, indentLength, true, false);
      xmlWriter.print(object.toString());
      xmlWriter.printXMLEndTag(INTEGER_XML_TAG);
    } else if (object instanceof String) {
      xmlWriter.printXMLStartTag(STRING_XML_TAG, indentLength, true, false);
      xmlWriter.print(TextUtil.doEntityReference((String) object));
      xmlWriter.printXMLEndTag(STRING_XML_TAG);
    } else if (object instanceof Double) {
      xmlWriter.printXMLStartTag(DOUBLE_XML_TAG, indentLength, true, false);
      xmlWriter.print(object.toString());
      xmlWriter.printXMLEndTag(DOUBLE_XML_TAG);
    } else if (object instanceof FortImpl) {
      ((FortImpl) object).toXML(xmlWriter, indentLength, true);
    } else if (object instanceof ByteArray) {
      ((ByteArray) object).toXML(xmlWriter, indentLength, true);
    } else if (object instanceof CycVariableImpl) {
      ((CycVariableImpl) object).toXML(xmlWriter, indentLength, true);
    } else if (object instanceof CycSymbolImpl) {
      ((CycSymbolImpl) object).toXML(xmlWriter, indentLength, true);
    } else if (object instanceof Guid) {
      GuidImpl.fromGuid((Guid) object).toXML(xmlWriter, indentLength, true);
    } else if (object instanceof CycArrayList) {
      ((CycArrayList) object).toXML(xmlWriter, indentLength, true);
    } else if (object instanceof FormulaImpl) {
      ((FormulaImpl) object).toXML(xmlWriter, indentLength, true);
    } else if (object instanceof CycAssertionImpl) {
      ((CycAssertionImpl) object).toXML(xmlWriter, indentLength, true);
    } else {
      throw new BaseClientRuntimeException(
              "Invalid CycList object (" + object.getClass().getSimpleName() + ") " + object);
    }
    xmlWriter.setIndent(-indentLength, true);
    if (startingIndent != xmlWriter.getIndentLength()) {
      throw new BaseClientRuntimeException("Starting indent " + startingIndent
              + " is not equal to ending indent " + xmlWriter.getIndentLength() 
              + " for object " + object);
    }
  }
  
  //====|    Fields    |==========================================================================//
  
  static final long serialVersionUID = 2031704553206469327L;
  /**
   * XML serialization tags.
   */
  public static final String CYC_LIST_XML_TAG = "list";
  public static final String INTEGER_XML_TAG = "integer";
  public static final String DOUBLE_XML_TAG = "double";
  public static final String STRING_XML_TAG = "string";
  public static final String DOTTED_ELEMENT_XML_TAG = "dotted-element";
  /**
   * XML serialization indentation.
   */
  public static int indentLength = 2;
  private boolean isProperList = true;
  private E dottedElement;
  public static final CycArrayList EMPTY_CYC_LIST = new UnmodifiableCycList<Object>();
  
  final static private CycSymbolImpl LIST_NIL = new CycSymbolImpl("NIL");
  
  //====|    Construction    |====================================================================//
  
  /**
   * Constructs a new empty <tt>CycList</tt> object.
   */
  public CycArrayList() {
  }

  /**
   * Constructs a new empty <tt>CycList</tt> object of the given size.
   *
   * @param size the initial size of the list
   */
  public CycArrayList(final int size) {
    super(size);
  }

  public CycArrayList(CycArrayList<? extends E> list) {
    for (int i = 0; i < list.getProperListSize(); i++) {
      this.add(list.get(i));
    }
    if (!list.isProperList()) {
      setDottedElement(list.getDottedElement());
    }
  }

  /**
   * Constructs a new <tt>CycList</tt> object, containing the elements of the
   * specified collection, in the order they are returned by the collection's iterator.
   *
   * @param c the collection of assumed valid Base Client objects.
   */
  public CycArrayList(final Collection<? extends E> c) {
    super(c);
  }

  /**
   * Constructs a new <tt>CycList</tt> object, containing as its first element
   * <tt>firstElement</tt>, and containing as its remaining elements the
   * contents of the <tt>Collection</tt> remaining elements.
   *
   * @param firstElement the object which becomes the head of the <tt>CycList</tt>
   * @param remainingElements a <tt>Collection</tt>, whose elements become the
   * remainder of the <tt>CycList</tt>
   */
  public CycArrayList(final E firstElement, final Collection<? extends E> remainingElements) {
    add(firstElement);
    addAll(remainingElements);
  }

  /**
   * Constructs a new <tt>CycList</tt> object, containing as its sole element
   * <tt>element</tt>
   *
   * @param element the object which becomes the head of the <tt>CycList</tt>
   */
  public CycArrayList(final E element) {
    add(element);
  }

  /**
   * Constructs a new <tt>CycList</tt> object, containing as its first element
   * <tt>element1</tt>, and <tt>element2</tt> as its second element.
   *
   * @param element1 the object which becomes the head of the <tt>CycList</tt>
   * @param element2 the object which becomes the second element of the <tt>CycList</tt>
   */
  public CycArrayList(final E element1, final E element2) {
    add(element1);
    add(element2);
  }
  
  //====|    Methods    |=========================================================================//

  @Override
  public CycArrayList<E> addToBeginning(final E element) {
    if (isEmpty()) {
      add(element);
    } else {
      add(0, element);
    }
    return this;
  }
  
  @Override
  public CycArrayList clone() {
    return new CycArrayList<>(this);
  }
  
  @Override
  public CycArrayList<E> deepCopy() {
    final CycArrayList cycList = new CycArrayList<>();
    if (!this.isProperList()) {
      if (this.dottedElement instanceof CycList) {
        cycList.setDottedElement(((CycList) this.dottedElement).deepCopy());
      } else {
        cycList.setDottedElement(this.getDottedElement());
      }
    }
    for (int i = 0; i < super.size(); i++) {
      final Object element = this.get(i);
      if (element instanceof CycList) {
        cycList.add(((CycList) element).deepCopy());
      } else {
        cycList.add(element);
      }
    }
    return cycList;
  }
  
  @Override
  public E getDottedElement() {
    return dottedElement;
  }

  @Override
  public CycArrayList<E> setDottedElement(final E dottedElement) {
    this.dottedElement = dottedElement;
    this.isProperList = (dottedElement == null) || (CycObjectFactory.nil.equals(dottedElement));
    return this;
  }
  
  @Override
  public boolean isProperList() {
    return isProperList;
  }
  
  @Override
  public int size() {
    int result = super.size();
    if (!isProperList()) {
      result++;
    }
    return result;
  }

  @Override
  public int getProperListSize() {
    return super.size();
  }
  
  @Override
  public boolean isValid() {
    for (E elem : this) {
      if (elem instanceof String
          || elem instanceof Integer || elem instanceof Float
          || elem instanceof ByteArray || elem instanceof Guid
          || elem instanceof CycConstant || elem instanceof Nart) {
        // Continue iterating...
      } else if (elem instanceof CycList) {
        if (!((CycList) elem).isValid()) {
          return false;
        }
      } else {
        return false;
      }
    }
    return true;
  }

  /* *
   * Returns true if formula is well-formed in the relevant mt.
   *
   * @param mt the relevant mt
   * @return true if formula is well-formed in the relevant mt, otherwise false
   * @throws UnknownHostException if cyc server host not found on the network
   * @throws IOException if a data communication error occurs
   * @throws CycApiException if the api request results in a cyc server error
   * @ deprecated use CycAccess.isFormulaWellFormed(this, mt);
   */
  /*
  public boolean isFormulaWellFormed(final ElMt mt)
          throws IOException, UnknownHostException, CycApiException {
    return CycAccess.getCurrent().isFormulaWellFormed(this, mt);
  }
*/
  /* *
   * Returns true if formula is well-formed Non Atomic Reifable Term.
   *
   * @return true if formula is well-formed Non Atomic Reifable Term, otherwise false
   * @throws UnknownHostException if cyc server host not found on the network
   * @throws IOException if a data communication error occurs
   * @throws CycApiException if the api request results in a cyc server error
   * @ deprecated use CycAccess.isCycLNonAtomicReifableTerm();
   */
  /*
  public boolean isCycLNonAtomicReifableTerm()
          throws IOException, UnknownHostException, CycApiException {
    return CycAccess.getCurrent().isCycLNonAtomicReifableTerm(this);
  }
  */

  /* *
   * Returns true if formula is well-formed Non Atomic Un-reifable Term.
   *
   * @return true if formula is well-formed Non Atomic Un-reifable Term, otherwise false
   * @throws UnknownHostException if cyc server host not found on the network
   * @throws IOException if a data communication error occurs
   * @throws CycApiException if the api request results in a cyc server error
   * @ deprecated use CycAccess.isCycLNonAtomicUnreifableTerm();
   */
  /*
  public boolean isCycLNonAtomicUnreifableTerm()
          throws IOException, UnknownHostException, CycApiException {
    return CycAccess.getCurrent().isCycLNonAtomicUnreifableTerm(this);
  }
  */
  
  @Override
  public E first() {
    if (isEmpty()) {
      throw new BaseClientRuntimeException("First element not available for an empty CycList");
    }
    return this.get(0);
  }
  
  @Override
  public E second() {
    if (size() < 2) {
      throw new BaseClientRuntimeException("Second element not available");
    }
    return this.get(1);
  }
  
  @Override
  public E third() {
    if (size() < 3) {
      throw new BaseClientRuntimeException("Third element not available");
    }
    return this.get(2);
  }
  
  @Override
  public E fourth() {
    if (size() < 4) {
      throw new BaseClientRuntimeException("Fourth element not available");
    }
    return this.get(3);
  }
  
  @Override
  public E last() {
    if (isEmpty()) {
      throw new BaseClientRuntimeException("Last element not available");
    }
    return this.get(this.size() - 1);
  }
  
  @Override
  public Object rest() {
    if (isEmpty()) {
      throw new BaseClientRuntimeException("Cannot remove first element of an empty list.");
    } else if ((super.size() == 1) && (!this.isProperList)) {
      return this.getDottedElement();
    }
    final CycList<E> result = new CycArrayList<>(this);
    result.remove(0);
    return result;
  }
  
  @Override
  public CycArrayList<E> appendElements(final Collection<? extends E> cycList) {
    addAll(cycList);
    return this;
  }
  
  @Override
  public CycArrayList<E> appendElement(final E object) {
    add(object);
    return this;
  }
  
  /*
  @Override
  public CycArrayList appendElement(final int i) {
    add(i);
    return this;
  }
  
  @Override
  public CycArrayList appendElement(final long l) {
    add(l);
    return this;
  }
  
  @Override
  public CycArrayList appendElement(final boolean b) {
    add(b);
    return this;
  }
  */
  
  @Override
  public boolean add(E e) {
    return super.add(e);
  }

  /* *
   * Adds the given integer to this list by wrapping it with an Integer object.
   *
   * @param i the given integer to add
   * /
  public void add(final int i) {
    super.add((E) Integer.valueOf(i));
  }

  /* *
   * Adds the given long to this list by wrapping it with an Long object.
   *
   * @param l the given long to add
   * /
  public void add(final long l) {
    super.add((E) Long.valueOf(l));
  }

  /* *
   * Adds the given float to this list by wrapping it with a Float object.
   *
   * @param f the given float to add
   * /
  public void add(final float f) {
    super.add((E) Float.valueOf(f));
  }

  /* *
   * Adds the given double to this list by wrapping it with a Double object.
   *
   * @param d the given double to add
   * /
  public void add(final double d) {
    super.add((E) Double.valueOf(d));
  }

  /* *
   * Adds the given boolean to this list by wrapping it with a Boolean object.
   *
   * @param b the given boolean to add
   * /
  public void add(final boolean b) {
    super.add((E) Boolean.valueOf(b));
  }
  */
  
  @Override
  public CycArrayList<E> addNew(final E object) {
    if (!this.contains(object)) {
      this.add(object);
    }
    return this;
  }
  
  @Override
  public CycArrayList<E> addAllNew(final Collection<? extends E> objects) {
    for (Iterator<? extends E> iter = objects.iterator(); iter.hasNext();) {
      this.addNew((E) iter.next());
    }
    return this;
  }

  @Override
  public boolean addAll(Collection<? extends E> col) {
    boolean result = super.addAll(col);
    if (col instanceof CycList) {
      final CycList cycList = (CycList) col;
      if (!cycList.isProperList()) {
        final E elem = (E) cycList.getDottedElement();
        if (isProperList()) {
          setDottedElement(elem);
        } else {
          add(getDottedElement());
          setDottedElement(elem);
        }
      }
    }
    return result;
  }
  
  @Override
  public boolean contains(Object obj) {
    if (!isProperList()) {
      if (getDottedElement().equals(obj)) {
        return true;
      }
    }
    return super.contains(obj);
  }
  
  @Override
  public boolean containsDuplicates() {
    if (!isProperList) {
      if (this.contains(this.dottedElement)) {
        return true;
      }
    }
    for (int i = 0; i < this.size(); i++) {
      for (int j = i + 1; j < this.size(); j++) {
        if (this.get(i).equals(this.get(j))) {
          return true;
        }
      }
    }
    return false;
  }
  
  @Override
  public CycArrayList<E> deleteDuplicates() {
    if (this.isProperList) {
      if (this.contains(this.dottedElement)) {
        this.setDottedElement(null);
      }
    }
    for (int i = 0; i < this.size(); i++) {
      for (int j = i + 1; j < this.size(); j++) {
        if (this.get(i).equals(this.get(j))) {
          this.remove(j);
          j--;
        }
      }
    }
    return this;
  }
  
  @Override
  public CycArrayList<E> removeDuplicates() {
    final CycArrayList<E> result = this.deepCopy();
    return result.deleteDuplicates();
  }
  
  @Override
  public CycArrayList<E> flatten() {
    final CycArrayList<E> result = new CycArrayList<>();
    final Iterator<E> i = this.iterator();
    while (i.hasNext()) {
      E obj = i.next();
      if (obj instanceof CycList) {
        result.addAll(((CycList) obj).flatten());
      } else {
        result.add(obj);
      }
    } //end while
    if (!isProperList) {
      result.add(getDottedElement());
    }
    return result;
  }
  
  @Override
  public CycArrayList<E> reverse() {
    if (!isProperList) {
      throw new BaseClientRuntimeException(this + " is not a proper list and cannot be reversed");
    }
    final CycArrayList<E> result = new CycArrayList<>();
    for (int i = (this.size() - 1); i >= 0; i--) {
      result.add(this.get(i));
    }
    return result;
  }
  
  @Override
  public CycList<CycList<? extends E>> combinationsOf(int n) {
    if (!isProperList) {
      throw new BaseClientRuntimeException(this + " is not a proper list");
    }
    if (this.isEmpty() || n == 0) {
      return new CycArrayList<>();
    }
    return combinationsOf_internal(new CycArrayList<>(this.subList(0, n)),
            new CycArrayList<>(this.subList(n, this.size())));
  }

  /**
   * Provides the internal implementation <tt.combinationsOf</tt> using a recursive
   * algorithm.
   *
   * @param selectedItems a window of contiguous items to be combined
   * @param availableItems the complement of the selectedItems
   * @return a <tt>CycArrayList</tt> of the combinations of sublists from the
   * selectedItems.
   */
  private static <E> CycList<CycList<? extends E>> combinationsOf_internal(
          final CycList<E> selectedItems,
          final CycList<E> availableItems) {
    final CycList<CycList<? extends E>> result = CycArrayList.list(selectedItems);
    if (availableItems.isEmpty()) {
      return result;
    }
    CycList<E> combination;
    for (int i = 0; i < (selectedItems.size() - 1); i++) {
      for (int j = 0; j < availableItems.size(); j++) {
        final E availableItem = availableItems.get(j);
        // Remove it (making copy), shift left, append replacement.
        combination = (CycList) selectedItems.clone();
        combination.remove(i + 1);
        combination.add(availableItem);
        result.add(combination);
      }
    }
    final CycList newSelectedItems = (CycList) selectedItems.rest();
    newSelectedItems.add(availableItems.first());
    final CycList newAvailableItems = (CycList) availableItems.rest();
    result.addAll(combinationsOf_internal(newSelectedItems, newAvailableItems));
    return result;
  }
  
  @Override
  public CycArrayList<E> randomPermutation() {
    final CycArrayList remainingList = this.clone();
    if (this.isEmpty()) {
      return remainingList;
    }
    final Random random = new Random();
    int randomIndex;
    final CycArrayList permutedList = new CycArrayList<>();
    while (true) {
      if (remainingList.size() == 1) {
        permutedList.addAll(remainingList);
        return permutedList;
      }
      randomIndex = random.nextInt(remainingList.size() - 1);
      permutedList.add(remainingList.get(randomIndex));
      remainingList.remove(randomIndex);
    }
  }
  
  @Override
  public CycArrayList subst(final E newObject, final E oldObject) {
    final CycArrayList result = new CycArrayList<>();
    if (!isProperList) {
      result.setDottedElement((dottedElement.equals(oldObject)) ? oldObject : newObject);
    }
    for (int i = 0; i < getProperListSize(); i++) {
      final E element = this.get(i);
      if (element.equals(oldObject)) {
        result.add(newObject);
      } else if (element instanceof CycList) {
        result.add(((CycList) element).subst(newObject, oldObject));
      } else {
        result.add(element);
      }
    }
    return result;
  }
  
  @Override
  public String toString() {
    return toStringHelper(false);
  }

  /**
   * Returns a <tt>String</tt> representation of this <tt>CycArrayList</tt>.  When the parameter is
   * true, the representation is created without causing additional api calls to complete the name 
   * field of constants.
   *
   * @param safe when true, the representation is created without causing
   * additional api calls to complete the name field of constants
   * @return a <tt>String</tt> representation of this <tt>CycArrayList</tt>
   */
  protected String toStringHelper(final boolean safe) {
    final StringBuffer result = new StringBuffer("(");
    for (int i = 0; i < super.size(); i++) {
      if (i > 0) {
        result.append(" ");
      }
      final E element = this.get(i);
      if (element == null) {
        result.append("null");
      } else if (element instanceof String) {
        result.append("\"" + element + "\"");
      } else if (safe) {
        try {
          // If element understands the safeToString method, then use it.
          final Method safeToString = element.getClass().getMethod("safeToString");
          result.append(safeToString.invoke(element));
        } catch (Exception e) {
          result.append(element.toString());
        }
      } else {
        result.append(element.toString());
      }
    }
    if (!isProperList) {
      result.append(" . ");
      if (dottedElement instanceof String) {
        result.append("\"");
        result.append(dottedElement);
        result.append("\"");
      } else if (safe) {
        try {
          // If dottedElement understands the safeToString method, then use it.
          final Method safeToString = dottedElement.getClass().getMethod("safeToString");
          result.append(safeToString.invoke(dottedElement));
        } catch (Exception e) {
          result.append(dottedElement.toString());
        }
      } else {
        result.append(dottedElement.toString());
      }
    }
    result.append(")");
    return result.toString();
  }
  
  @Override
  public String toPrettyString(String indent) {
    return toPrettyStringInt(indent, "  ", "\n", false, false);
  }
  
  @Override
  public String toPrettyEscapedCyclifiedString(String indent) {
    return toPrettyStringInt(indent, "  ", "\n", true, true);
  }
  
  @Override
  public String toPrettyCyclifiedString(String indent) {
    return toPrettyStringInt(indent, "  ", "\n", true, false);
  }

  /**
   * Returns an HTML `pretty-printed' <tt>String</tt> representation of this
   * <tt>CycArrayList</tt>.
   * @param indent the indent string that is added before the
   * <tt>String</tt> representation this <tt>CycArrayList</tt>
   * @return an HTML `pretty-printed' <tt>String</tt> representation of this
   * <tt>CycArrayList</tt>.
   */
  @Deprecated
  public String toHTMLPrettyString(final String indent) {
    // dpb -- shouldn't this be "&nbsp;&nbsp;"?
    return "<html><body>"
            + toPrettyStringInt(indent, "&nbsp&nbsp", "<br>", false, false)
            + "</body></html>";
  }

  /**
   * Returns an HTML `pretty-printed' <tt>String</tt> representation of this
   * <tt>CycArrayList</tt> without having to say what the indent string is (empty indent).
   *
   * @return an HTML `pretty-printed' <tt>String</tt> representation of this
   * <tt>CycArrayList</tt>.
   */
  @Deprecated
  public String toHTMLPrettyString() {
    // dpb -- shouldn't this be "&nbsp;&nbsp;"?
    return "<html><body>"
            + toPrettyStringInt("", "&nbsp&nbsp", "<br>", false, false)
            + "</body></html>";
  }

  @Override
  public String toPrettyStringInt(final String indent,
          final String incrementIndent, final String newLineString,
          final boolean shouldCyclify, final boolean shouldEscape) {
    final StringBuffer result = new StringBuffer(indent + "(");
    for (int i = 0; i < super.size(); i++) {
      Object element = this.get(i);
      if (element instanceof NonAtomicTerm) {
        element = ((NonAtomicTerm) element).toCycList();
      }
      if (element instanceof FormulaImpl) {
        element = ((FormulaImpl) element);
      }
      if (element instanceof String) {
        if (i > 0) {
          result.append(" ");
        }
        result.append('"');
        if (shouldEscape) {
          result.append(CycStringUtils.escapeDoubleQuotes((String) element));
        } else {
          result.append(element);
        }
        result.append('"');
      } else if (element instanceof CycList) {
        result.append(newLineString)
                .append(((CycList) element)
                        .toPrettyStringInt(indent + incrementIndent,
                                           incrementIndent, newLineString, shouldCyclify,
                                           shouldEscape));
      } else if (element instanceof Formula) {
        result.append(newLineString)
                .append(((Formula) element)
                        .toCycList()
                        .toPrettyStringInt(indent + incrementIndent,
                                           incrementIndent, newLineString, shouldCyclify,
                                           shouldEscape));
      } else {
        if (i > 0) {
          result.append(" ");
        }
        if (shouldCyclify) {
          if (shouldEscape) {
            result.append(DefaultCycObjectImpl.cyclify(element));
          } else {
            result.append(DefaultCycObjectImpl.cyclifyWithEscapeChars(element, false));
          }
        } else {
          result.append(element.toString());
        }
      }
    }
    if (!isProperList) {
      result.append(" . ");
      if (dottedElement instanceof String) {
        result.append("\"");
        if (shouldEscape) {
          result.append(CycStringUtils.escapeDoubleQuotes((String) dottedElement));
        } else {
          result.append(dottedElement);
        }
        result.append("\"");
      } else {
        result.append(this.dottedElement.toString());
      }
    }
    result.append(")");
    return result.toString();
  }
  
  
    
    /*
    if (o instanceof CycArrayList) {
      final CycArrayList them = (CycArrayList) o;
      if (this.isProperList() != them.isProperList()) {
        return false;
      }
    }
    // else if (!this.isProperList()) { return false; }
    if (!(o instanceof CycArrayList)) {
      if (!this.isProperList()) {
        return false;
      }
    }
    if (!this.isProperList() 
        && (!(o instanceof CycArrayList) || ((CycArrayList) o).isProperList())) {
      return false;
    }
    if (this.isProperList()
            && ((o instanceof CycArrayList) && !((CycArrayList) o).isProperList())
            ) {
      return false;
    }
    
    if (!this.isProperList()) {
      if (!(o instanceof CycArrayList) || ((CycArrayList) o).isProperList()) {
        return false;
      }
    } else {
      if ((o instanceof CycArrayList) && !((CycArrayList) o).isProperList()) {
          return false;
        }
      }
    }
    
    */
  
  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof List)) {
      return false;
    }
    final List them = (List) o;
    if (this.isProperList() != isProperList(them)) {
      return false;
    }
    final ListIterator e1 = this.listIterator();
    final ListIterator e2 = them.listIterator();
    while (e1.hasNext() && e2.hasNext()) {
      Object o1 = e1.next();
      Object o2 = e2.next();
      if (o1 instanceof CycArrayList) {
        if (!((CycArrayList) o1).isProperList()) {
          if (!(o2 instanceof CycArrayList)) {
            return false;
          }
          if (((CycArrayList) o2).isProperList()) {
            return false;
          }
        } else {
          if (o2 instanceof CycArrayList) {
            if (!((CycArrayList) o2).isProperList()) {
              return false;
            }
          }
        }
      }
      if (!(o1 == null ? o2 == null : o1.equals(o2))) {
        return false;
      }
    }
    if (e1.hasNext() || e2.hasNext()) {
      return false;
    }
    if  (!isProperList()) {
      if (!(them instanceof CycArrayList)) {
        return false;
      }
      CycArrayList otherList = (CycArrayList) them;
      if (otherList.isProperList()) {
        return false;
      }
      Object dottedElement1 = getDottedElement();
      Object dottedElement2 = otherList.getDottedElement();
      if (dottedElement1 == dottedElement2) {
        return true;
      }
      if ((dottedElement1 == null) || (dottedElement2 == null)) {
        return (dottedElement1 == dottedElement2);
      }
      return dottedElement1.equals(dottedElement2);
    } else {
      return (!(them instanceof CycArrayList))
              || ((them instanceof CycArrayList) && ((CycArrayList) them).isProperList());
    }
  }
  
  /*
  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if ((o == null) || !(o instanceof List)) {
      return false;
    }
    if ((o == null) || !(o instanceof CycArrayList)) {
      return false;
    }
    final CycArrayList them = (CycArrayList) o;
    if (this.isProperList() != them.isProperList()) {
      return false;
    }
    if (!this.isProperList()) {
      if (!Objects.equals(this.getDottedElement(), 
                          them.getDottedElement())) {
        return false;
      }
    }
    // Lastly, recurse...
    final ListIterator e1 = this.listIterator();
    final ListIterator e2 = them.listIterator();
    while (e1.hasNext() && e2.hasNext()) {
      if (!Objects.equals(e1.next(), 
                          e2.next())) {
        return false;
      }
    }
    return e1.hasNext() == e2.hasNext();
  }
  */
  @Override
  public int hashCode() {
    int code = 0;
    for (final E item : this) {
      code = code * 31 + item.hashCode();
    }
    return code;
  }
  
  public int printHashCode() {
    int code = 0;
    System.out.println("==");
    for (final E item : this) {
      if (CycArrayList.class.isInstance(item)) {
        ((CycArrayList) item).printHashCode();
      } else {
        System.out.println("  - " + item.hashCode());
        System.out.println("    " + item);
      }
      code = code ^ item.hashCode();
    }
    System.out.println("==");
    return code;
  }
  
  @Override
  public CycListMap<? extends E, ? extends E> toMap() {
    final CycListMap<E, E> results = new CycListMapImpl<>(this.size());
    try {
      this.stream()
              .map((elt) -> (CycList<E>)elt)
              .forEachOrdered((eltAsList) -> {
                results.put(eltAsList.first(),
                            (E) eltAsList.rest());
      });
    } catch (Exception e) {
       if (this.isPlist()) {
        try {
          return CycListMapImpl.from(this); // TODO: this approach is not ideal - nwinant, 2017-08-07
        } catch (Exception e2) {
          // Fall through and let an exception be thrown...
        }
      }
      throw new UnsupportedOperationException(
              "Unable to convert CycList to Map because CycList is not an association-list.", e);
    }
    return results;
  }
  
  /*
  @Override
  public CycListMap<CycSymbol, ? extends E> toPlistMap() {
    return CycListMapImpl.from(this);
  }
  */
  
  @Override
  public boolean equalsAtEL(Object o) {
    Map<CycVariable, CycVariable> varMap = new HashMap<>();
    return equalsAtEL(o, varMap);
  }
  
  protected boolean equalsAtEL(Object o, Map<CycVariable, CycVariable> varMap) {
    if (o == this) {
      return true;
    }
    if (o == null) {
      return false;
    }
    if (o instanceof NonAtomicTerm) {
      o = ((NonAtomicTerm) o).toCycList();
    }
    if (!(o instanceof List)) {
      return false;
    }
    if (!isProperList()) {
      if (!(o instanceof CycArrayList)) {
        return false;
      }
      if (((CycArrayList) o).isProperList()) {
        return false;
      }
    } else {
      if (o instanceof CycArrayList) {
        if (!((CycArrayList) o).isProperList()) {
          return false;
        }
      }
    }
    final ListIterator e1 = listIterator();
    final ListIterator e2 = ((List) o).listIterator();
    while (e1.hasNext() && e2.hasNext()) {
      Object o1 = e1.next();
      if ((o1 != null) && (o1 instanceof NonAtomicTerm)) {
        o1 = ((NonAtomicTerm) o1).toCycList();
      }
      Object o2 = e2.next();
      if ((o2 != null) && (o2 instanceof NonAtomicTerm)) {
        o2 = ((NonAtomicTerm) o2).toCycList();
      }
      if (o1 instanceof CycArrayList) {
        if (!((CycList) o1).isProperList()) {
          if (!(o2 instanceof CycList)) {
            return false;
          }
          if (((CycList) o2).isProperList()) {
            return false;
          }
        } else {
          if (o2 instanceof CycList) {
            if (!((CycList) o2).isProperList()) {
              return false;
            }
          }
        }
        if (!(o1 == null ? o2 == null : ((CycArrayList) o1).equalsAtEL(o2, varMap))) {
          return false;
        }
      } else if ((o1 instanceof Integer && o2 instanceof Long)
                 || (o1 instanceof Long && o2 instanceof Integer)) {
        return ((Number) o1).longValue() == ((Number) o2).longValue();
      } else if ((o1 instanceof Float && o2 instanceof Double)
                 || (o1 instanceof Double && o2 instanceof Float)) {
        return ((Number) o1).doubleValue() == ((Number) o2).doubleValue();
      } else if (o1 instanceof CycVariableImpl && o2 instanceof CycVariableImpl) {
        if (varMap.containsKey(o1) && !varMap.get(o1).equals(o2)) {
          return false;
        } else {
          varMap.put((CycVariableImpl) o1, (CycVariableImpl) o2);
        }
      } else if (o1 instanceof FormulaSentenceImpl && o2 instanceof FormulaSentenceImpl) {
        if (!((FormulaSentenceImpl) o1).args.equalsAtEL(((FormulaSentenceImpl) o2).args, varMap)) {
          return false;
        }
      } else if (!(o1 == null ? o2 == null : o1.equals(o2))) {
        return false;
      }
    }
    return !(e1.hasNext() || e2.hasNext());
  }
  
  @Override
  public int compareTo(Object o) {
    if (o == this) {
      return 0;
    }
    if (o == null) {
      return 1;
    }
    if (!(o instanceof List)) {
      return 1;
    }
    if (!isProperList()) {
      if (!(o instanceof CycList)) {
        return 1;
      }
      if (((CycList) o).isProperList()) {
        return 1;
      }
    } else {
      if (o instanceof CycList) {
        if (!((CycList) o).isProperList()) {
          return -1;
        }
      }
    }
    final ListIterator<E> e1 = listIterator();
    final ListIterator e2 = ((List) o).listIterator();
    while (e1.hasNext() && e2.hasNext()) {
      E o1 = e1.next();
      Object o2 = e2.next();

      if (o1 == o2) {
        continue;
      }
      if (o1 == null) {
        return -1;
      }
      if (o2 == null) {
        return 1;
      }
      if (!(o1 instanceof Comparable) && !(o2 instanceof Comparable)) {
        continue;
      }
      if (!(o1 instanceof Comparable)) {
        return 1;
      }
      if (!(o2 instanceof Comparable)) {
        return -1;
      }
      final Comparable co1 = (Comparable) o1;
      final Comparable co2 = (Comparable) o2;
      if (co1 instanceof CycList) {
        if (!((CycList) co1).isProperList()) {
          if (!(co2 instanceof CycList)) {
            return 1;
          }
          if (((CycList) co2).isProperList()) {
            return 1;
          }
        } else {
          if (co2 instanceof CycList) {
            if (!((CycList) co2).isProperList()) {
              return -1;
            }
          }
        }
      }

      int ret = co1.compareTo(co2);
      if (ret != 0) {
        return ret;
      }
    }
    if (e1.hasNext()) {
      return 1;
    }
    if (e2.hasNext()) {
      return -1;
    }
    return 0;
  }
  
  @Override
  public String cyclifyWithEscapeChars() {
    return cyclifyWithEscapeChars(false);
  }
  
  @Override
  public String cyclifyWithEscapeChars(boolean isApi) {
    final StringBuffer result = new StringBuffer("(");
    String cyclifiedObject;
    for (int i = 0; i < super.size(); i++) {
      final E object = this.get(i);
      cyclifiedObject = DefaultCycObjectImpl.cyclifyWithEscapeChars(object, isApi);
      if (i > 0) {
        result.append(" ");
      }
      result.append(cyclifiedObject);
    }
    if (!isProperList) {
      result.append(" . ");
      result.append(DefaultCycObjectImpl.cyclifyWithEscapeChars(dottedElement, isApi));
    }
    result.append(")");
    return result.toString();
  }
  
  @Override
  public String cyclify() {
    final StringBuffer result = new StringBuffer("(");
    for (int i = 0; i < super.size(); i++) {
      E object = this.get(i);
      if (object == null) {
        throw new BaseClientRuntimeException(
                "Invalid null element after " + result + " in " + this);
      }
      if (i > 0) {
        result.append(" ");
      }
      result.append(DefaultCycObjectImpl.cyclify(object));
    }
    if (!isProperList) {
      result.append(" . ");
      result.append(DefaultCycObjectImpl.cyclify(dottedElement));
    }
    result.append(")");
    return result.toString();
  }

  public Map<ArgPosition, Span> getPrettyStringDetails() {
    Map<ArgPosition, Span> map = new HashMap<>();
    getPrettyStringDetails(this, "", 0, new ArgPositionImpl(), map);
    Span loc = new Span(0, toPrettyString("").length());
    map.put(ArgPositionImpl.TOP, loc);
    return map;
  }

  private static int getPrettyStringDetails(final CycList list, final String indent,
          int currentPos, final ArgPositionImpl argPos, final Map<ArgPosition, Span> map) {
    String str;
    ArgPosition newArgPos;
    String tab = "  ";
    str = indent + "(";
    currentPos += str.length();
    String cyclifiedObject = null;
    int tempPos;
    for (int i = 0, size = list.size(); i < size; i++) {
      if (i > 0) {
        str = " ";
        currentPos += str.length();
      }
      if ((!list.isProperList()) && ((i + 1) >= size)) {
        currentPos += 2;
      }
      Object element = list.get(i);
      if (element instanceof Nart) {
        element = ((Nart) element).toCycList();
      }
      if (element instanceof String) {
        str = "\"" + element + "\"";
        newArgPos = argPos.deepCopy();
        newArgPos.extend(i);
        Span loc = new Span(currentPos, currentPos + str.length());
        map.put(newArgPos, loc);
        currentPos += str.length();
      } else if (element instanceof CycList) {
        argPos.extend(i);
        tempPos = currentPos + indent.length() + tab.length();
        currentPos = getPrettyStringDetails((CycList) element,
                indent + tab, currentPos, argPos, map);
        Span loc = new Span(tempPos, currentPos);
        ArgPosition deepCopy = argPos.deepCopy();
        map.put(deepCopy, loc);
        argPos.toParent();
      } else {
        str = element.toString();
        newArgPos = argPos.deepCopy();
        newArgPos.extend(i);
        Span loc = new Span(currentPos, currentPos + str.length());
        map.put(newArgPos, loc);
        currentPos += str.length();
      }
    }
    str = ")";
    return currentPos + str.length();
  }
  
  @Override
  public String stringApiValue() {
    return CycArrayList.stringApiValue(this);
  }

  protected static StringBuilder appendSubSlice(
          List list,
          StringBuilder builder, 
          int start, 
          int end, 
          boolean properList) {
    // note the asterisk, which results in a dotted list
    if (list instanceof UnmodifiableCycList) {
      throw new UnsupportedOperationException();
    }
    builder.append(properList ? "(list" : "(list*");
    for (int i = start; i < end; i++) {
      CycArrayList.appendElement(builder, list.get(i));
    }
    if (!properList) {
      // Non-CycLists are proper lists, so this cast should be safe.
      ((CycArrayList)list).appendDottedElement(builder);
    }
    builder.append(")");
    return builder;
  }

  protected static void appendElement(StringBuilder builder, Object object) {
    if (object == null) {
      throw new BaseClientRuntimeException("Got unexpected null object.");
    }
    builder.append(" ");
    builder.append(DefaultCycObjectImpl.stringApiValue(object));
  }
  
  private void appendDottedElement(StringBuilder builder) {
    final E dottedObject = (E) ((dottedElement == null) ? LIST_NIL : dottedElement);
    appendElement(builder, dottedObject);    
  }
  
  @Override
  public Object cycListApiValue() {
    return cycListApiValue(false);
  }
  
  @Override
  public Object cycListApiValue(final boolean shouldQuote) {
    if (shouldQuote) {
      return makeCycList(CycObjectFactory.quote, this);
    } else {
      return this;
    }
  }
  
  @Override
  public CycArrayList sort() {
    final CycArrayList sortedList = new CycArrayList(this);
    Collections.sort(sortedList, new CycListComparator());
    return sortedList;
  }
  
  @Override
  public CycListVisitor cycListVisitor() {
    return new CycListVisitor(this);
  }
  
  @Override
  public CycArrayList<CycConstant> treeConstants() {
    final CycArrayList<CycConstant> constants = new CycArrayList<>();
    final Stack stack = new Stack();
    stack.push(this);
    while (!stack.empty()) {
      final Object obj = stack.pop();
      if (obj instanceof CycConstant) {
        constants.add((CycConstant) obj);
      } else if (obj instanceof CycAssertionImpl) {
        stack.push(((CycAssertion) obj).getMt());
        pushTreeConstantElements(((CycAssertion) obj).getFormula(), stack);
      } else if (obj instanceof Nart) {
        stack.push(((Nart) obj).getFunctor());
        pushTreeConstantElements(((Nart) obj).getArguments(), stack);
      } else if (obj instanceof CycList) {
        pushTreeConstantElements(((CycList) obj), stack);
      }
    }
    return constants;
  }

  private void pushTreeConstantElements(List list, Stack stack) {
    final Iterator iter = list.iterator();
    while (iter.hasNext()) {
      stack.push(iter.next());
    }
  }

  @Override
  public E get(int index) {
    if ((index == (size() - 1)) && (!isProperList())) {
      return getDottedElement();
    } else {
      return super.get(index);
    }
  }
  
  @Override
  public E set(int index, E element) {
    if ((index == (size() - 1)) && (!isProperList())) {
      final E oldValue = getDottedElement();
      setDottedElement(element);
      return oldValue;
    } else {
      return super.set(index, element);
    }
  }
  
  private int firstEvenIndexOf(Object elem) {
    if (elem == null) {
      for (int i = 0; i < size(); i = i + 2) {
        if (get(i) == null) {
          return i;
        }
      }
    } else {
      for (int i = 0; i < size(); i = i + 2) {
        if (elem.equals(get(i))) {
          return i;
        }
      }
    }
    return -1;
  }
  
  @Override
  public E getf(CycSymbol indicator, E defaultResult, boolean treatNilAsAbsent) {
    int indicatorIndex = firstEvenIndexOf(indicator);
    if (indicatorIndex == -1) { // the indicator is not present
      return defaultResult;
    } else {
      final E value = get(indicatorIndex + 1);
      if (treatNilAsAbsent && CycObjectFactory.nil.equals(value)) {
        return defaultResult;
      } else {
        return value;
      }
    }
  }
  
  @Override
  public E getf(CycSymbol indicator, E defaultResult) {
    return getf(indicator, defaultResult, false);
  }
  
  @Override
  public E getf(CycSymbol indicator) {
    return getf(indicator, null);
  }
  
  @Override
  public E getValueForKeyword(final CycSymbol keyword) {
    // FIXME: CycArrayListTest#testGetValueForKeyword() seems to expect this older, seemingly-broken behavior - nwinant, 2017-08-04
    for (int i = 0; i < this.size() - 1; i++) {
      if (this.get(i).equals(keyword)) {
        return this.get(i + 1);
      }
    }
    return null;
  }
  /*
  @Override
  public E getValueForKeyword(final CycSymbol keyword) {
    return getf(keyword);
  }
  */
  @Override
  public E setf(CycSymbol indicator, E value) {
    // TODO: add test coverage - nwinant, 2017-08-04
    int indicatorIndex = firstEvenIndexOf(indicator);
    if (indicatorIndex >= 0) {
      this.set(indicatorIndex + 1, value);
    } else {
      this.add((E) indicator);
      this.add(value);
    }
    return value;
  }
  
  @Override
  public boolean removeProperty(CycSymbol indicator) {
    // TODO: add test coverage - nwinant, 2017-08-04
    int indicatorIndex = firstEvenIndexOf(indicator);
    if ((indicatorIndex >= 0) && (indicatorIndex + 1 < size()))  {
      removeInt(indicatorIndex);
      removeInt(modCount);
    }
    return false;
  }
  
  @Override
  public boolean isPlist() {
    boolean expectingKeyword = true;
    List<CycSymbolImpl> keywords = new ArrayList<>();
    for (Object elt : this) {
        if (expectingKeyword) {
            if (elt instanceof CycSymbolImpl 
                    && ((CycSymbolImpl)elt).isKeyword()
                    && !keywords.contains(elt)) {
                expectingKeyword = false;
                keywords.add((CycSymbolImpl) elt);
            } else return false;
        } else { 
            expectingKeyword = true;
        }
    }
    return expectingKeyword;
  }
  
  @Override
  public List<CycSymbol> getPlistKeys() {
    //TODO: think about reimplementing isPlist in terms of this.
    boolean expectingKeyword = true;
    List<CycSymbol> keywords = new ArrayList<>();
    for (Object elt : this) {
        if (expectingKeyword) {
            if (elt instanceof CycSymbolImpl 
                    && ((CycSymbolImpl)elt).isKeyword() 
                    && !keywords.contains(elt)) {
                expectingKeyword = false;
                keywords.add((CycSymbolImpl) elt);
            } else throw new RuntimeException ("Unable to get plist keys from a non-plist");
        } else {
          expectingKeyword = true;
        }
    }
    if (expectingKeyword) {
        return keywords;
    } else { 
        throw new RuntimeException ("Unable to get plist keys from a non-plist");
    }      
  }
  
  @Override
  public boolean addPair(Object key, Object value) {
    final E pair = (E) makeDottedPair(key, value); 
    return add(pair);
  }
  
  @Override
  public void removePairs(Object key) {
    // TODO: add test coverage - nwinant, 2017-08-04
    removeIf(elem -> (elem instanceof CycList)
                             && (((CycList) elem).size() == 2)
                             && key.equals(((CycList) elem).first()));
  }
  
  private static class ElementNotFoundException extends BaseClientRuntimeException {
    public ElementNotFoundException(String msg) {
      super(msg);
    }
  }
  
  @Override
  public E findElementAfter(E searchObject) {
    int i = 0;
    for (Object curElement : this) {
      if ((searchObject == curElement) 
              || ((searchObject != null) && (searchObject.equals(curElement)))) {
        int index = i + 1;
        if (index >= size()) {
          throw new BaseClientRuntimeException(
                  "Search object: " + searchObject + " appears at end of list: " + this + "");
        }
        return get(index);
      }
      i++;
    }
    throw new ElementNotFoundException(
            "Search object: " + searchObject + " is not found in: " + this + "");
  }
  
  @Override
  public E findElementAfter(E searchObject, E notFound) {
    try {
      return findElementAfter(searchObject);
    } catch (ElementNotFoundException ex) {
      return notFound;
    }
  }
  
  @Override
  public CycList<Integer> allIndicesOf(E elem) {
    final CycArrayList<Integer> result = new CycArrayList<>();
    if (elem == null) {
      for (int i = 0; i < size(); i++) {
        if (get(i) == null) {
          result.add(i);
        }
      }
    } else {
      for (int i = 0; i < size(); i++) {
        if (elem.equals(get(i))) {
          result.add(i);
        }
      }
    }
    return result;
  }
  
  @Override
  public CycArrayList treeGather(Class<E> cls) {
    final CycArrayList result = new CycArrayList<>();
    final Stack stack = new Stack();
    stack.push(this);
    while (!stack.empty()) {
      final Object obj = stack.pop();
      if (cls.isInstance(obj)) {
        result.add(obj);
      } else if (obj instanceof CycList) {
        CycList l = (CycList) obj;
        final Iterator iter = l.iterator();
        while (iter.hasNext()) {
          stack.push(iter.next());
        }
        if (!l.isProperList()) {
          stack.push(l.getDottedElement());
        }
      }
    }
    return result;
  }
  
  @Override
  public boolean treeContains(E object) {
    if (object instanceof Nart) {
      object = (E) ((Nart) object).toCycList();
    }
    if (this.contains(object)) {
      return true;
    }
    for (int i = 0; i < this.size(); i++) {
      Object element = this.get(i);
      if (element instanceof Nart) {
        element = ((Nart) element).toCycList();
      }
      if (element.equals(object)) {
        return true;
      }
      if ((element instanceof CycList) && (((CycList) element).treeContains(object))) {
        return true;
      }
    }
    return false;
  }
  
  @Override
  public boolean doesElementPrecedeOthers(final E element, final Collection<? extends E> otherElements) {
    for (int i = 0; i < this.size(); i++) {
      if (element.equals(this.get(i))) {
        return true;
      }
      if (otherElements.contains(this.get(i))) {
        return false;
      }
    }
    return false;
  }

  /**
   * Returns the XML representation of this object.
   *
   * @return the XML representation of this object
   * @throws java.io.IOException
   */
  @Deprecated
  public String toXMLString() throws IOException {
    final XmlStringWriter xmlStringWriter = new XmlStringWriter();
    toXML(xmlStringWriter, 0, false);
    return xmlStringWriter.toString();
  }

  /**
   * Prints the XML representation of the <tt>CycArrayList</tt> to an <tt>XMLWriter</tt>
   *
   * @param xmlWriter the output XML serialization writer
   * @param indent specifies by how many spaces the XML output should be indented
   * @param relative specifies whether the indentation should be absolute --
   * indentation with respect to the beginning of a new line, relative = false
   * -- or relative to the indentation currently specified in the indent_string field
   * of the xml_writer object, relative = true.
   */
  @Deprecated
  public void toXML(final XmlWriter xmlWriter, final int indent,
          final boolean relative) throws IOException {
    final int startingIndent = xmlWriter.getIndentLength();
    xmlWriter.printXMLStartTag(CYC_LIST_XML_TAG, indent, relative, true);
    try {
      final Iterator iterator = this.iterator();
      Object arg;
      for (int i = 0, size = getProperListSize(); i < size; i++) {
        arg = iterator.next();
        toXML(arg, xmlWriter, indentLength, true);
      }
      if (!isProperList) {
        xmlWriter.printXMLStartTag(DOTTED_ELEMENT_XML_TAG, indentLength, relative,
                true);
        toXML(dottedElement, xmlWriter, indentLength, true);
        xmlWriter.printXMLEndTag(DOTTED_ELEMENT_XML_TAG, 0, true);
        xmlWriter.setIndent(-indentLength, true);
      }
    } catch (Exception e) {
      e.printStackTrace(System.err);
    } finally {
      xmlWriter.printXMLEndTag(CYC_LIST_XML_TAG, 0, true);
    }
  }
  
  @Override
  public CycArrayList<E> addQuoted(final Object object) {
    this.add((E) makeCycList(CycObjectFactory.quote, object));
    return this;
  }
  
  private <E> List<E> getRest(Collection<? extends E> collection) {
    if (collection instanceof CycList) {
      return (List<E>) ((CycList) collection).rest();
    } else {
      final List<E> result = new ArrayList(collection);
      result.remove(0);
      return result;
    }
  }
  
  @Override
  public Object getSpecifiedObject(final List<Integer> pathSpecification) {
    if (pathSpecification.isEmpty()) {
      return this;
    }
    Object answer = this.clone();
    List<Integer> tempPathSpecification = (pathSpecification instanceof CycList)
                                          ? new CycArrayList(pathSpecification)
                                          : new ArrayList(pathSpecification);
    int index = 0;
    try {
      while (!tempPathSpecification.isEmpty()) {
        index = tempPathSpecification.get(0);
        if (answer instanceof Nart) {
          if (index == 0) {
            answer = ((Nart) answer).getFunctor();
          } else {
            answer = ((Nart) answer).getArgument(index);
          }
        } else {
          answer = ((CycList) answer).get(index);
        }
        tempPathSpecification = getRest(tempPathSpecification);
      }
      return answer;
    } catch (Exception e) {
      throw new BaseClientRuntimeException(
              "Can't get object specified by path expression: '" + pathSpecification
              + "' in forumla: '" + this + "'.  answer: " + answer + " index: " + index
              + "\n" + CycStringUtils.getStringForException(e));
    }
  }
  
  @Override
  public Object getSpecifiedObject(final ArgPosition argPosition) {
    return getSpecifiedObject(argPosition.getPath());
  }
  
  @Override
  public CycArrayList<E> setSpecifiedObject(List<Integer> pathSpecification, final E value) {
    CycList parentContainer = null;
    Object container = this;
    int parentIndex = -1;
    int index = pathSpecification.get(0);
    pathSpecification = getRest(pathSpecification);
    while (true) {
      if (container instanceof Nart) {
        // after the first iteration the imbedded container can be a Nart
        container = ((Nart) container).toCycList();
        parentContainer.set(parentIndex, container);
      }
      if (pathSpecification.isEmpty()) {
        break;
      }
      parentContainer = (CycList) container;
      if (container instanceof CycList) {
        container = ((CycList) container).get(index);
      } else {
        throw new BaseClientRuntimeException("Don't know a path into: " + container);
      }
      parentIndex = index;
      index = pathSpecification.get(0);
      pathSpecification = getRest(pathSpecification);
    }
    if (container instanceof CycList) {
      container = ((CycList) container).set(index, value);
    } else if (container instanceof Nart) {
      if (index == 0) {
        ((Nart) container).setFunctor((FortImpl) value);
      } else {
        ((Nart) container).getArguments().set(index - 1, value);
      }
    } else {
      throw new BaseClientRuntimeException("Don't know about: " + container);
    }
    return this;
  }

  @Override
  public CycArrayList<E> setSpecifiedObject(ArgPosition pathSpecification, final E value) {
    setSpecifiedObject(pathSpecification.getPath(), value);
    return this;
  }
  
  @Override
  public CycArrayList<E> treeSubstitute(E oldObject, E newObject) {
    final List<ArgPosition> locs = getArgPositionsForTerm(oldObject);
    locs.forEach((loc) -> {
      setSpecifiedObject(loc, newObject);
    });
    return this;
  }
  
  @Override
  public List<ArgPosition> getArgPositionsForTerm(final E term) {
    if (this.equals(term)) {
      return Collections.emptyList();
    }
    final List<ArgPosition> result = new ArrayList<>();
    ArgPositionImpl curArgPosition = ArgPositionImpl.TOP;
    internalGetArgPositionsForTerm(term, this, curArgPosition, result);
    return result;
  }

  /** Private method used to implement getCycArgPositionForTerm() functionality.
   * @param term The term to search for
   * @param subTree The current sub part of the tree being explored
   * @param curPosPath The current arg position being explored
   * @param result Current store of arg positions found so far
   */
  private static void internalGetArgPositionsForTerm(Object term, Object subTree,
          final ArgPositionImpl curPosPath, final List<ArgPosition> result) {
    if (term instanceof Nart) {
      term = ((Nart) term).toCycList();
    }
    if (term == subTree) {
      final ArgPositionImpl newArgPos = new ArgPositionImpl(curPosPath.getPath());
      result.add(newArgPos);
      return;
    }
    if (subTree == null) {
      return;
    }
    if (subTree instanceof Nart) {
      subTree = ((Nart) subTree).toCycList();
    }
    if (subTree.equals(term)) {
      final ArgPositionImpl newArgPos = new ArgPositionImpl(curPosPath.getPath());
      result.add(newArgPos);
      return;
    }
    if ((subTree instanceof CycList) && ((CycList) subTree).treeContains(term)) {
      int newPos = 0;
      for (Iterator iter = ((List) subTree).iterator(); iter.hasNext(); newPos++) {
        final ArgPositionImpl newPosPath = new ArgPositionImpl(curPosPath.getPath());
        newPosPath.extend(newPos);
        internalGetArgPositionsForTerm(term, iter.next(), newPosPath, result);
      }
    }
  }

  @Override
  public List getReferencedConstants() {
    return treeConstants();
  }
  
  /*
  //// serializable
  
  private void writeObject(ObjectOutputStream stream) throws java.io.IOException {
    stream.defaultWriteObject();
    if (!isProperList) {
      stream.writeBoolean(false);
      stream.writeObject(this.dottedElement);
    } else {
      stream.writeBoolean(true);
    }
  }

  private void readObject(ObjectInputStream stream) throws java.io.IOException,
          java.lang.ClassNotFoundException {
    stream.defaultReadObject();
    isProperList = stream.readBoolean();
    if (!isProperList) {
      dottedElement = (E) stream.readObject();
    }
  }
  */
  
  @Override
  public ListIterator<E> listIterator(int index) {
    if ((index < 0) || (index > size())) {
      throw new IndexOutOfBoundsException("Index: " + index);
    }
    return new CycListItr(index);
  }

  @Override
  public ListIterator<E> listIterator() {
    return new CycListItr(0);
  }

  @Override
  public Iterator<E> iterator() {
    return new CycItr();
  }

  private void removeInt(int i) {
    remove(i);
  }

  private void setInt(int i, E val) {
    set(i, val);
  }

  private void addInt(int i, E val) {
    add(i, val);
  }

  //====|    CycItr    |==========================================================================//

  /**
   * An optimized version of AbstractList.Itr
   */
  private class CycItr implements Iterator<E> {

    int cursor;       // index of next element to return
    int lastRet = -1; // index of last element returned; -1 if no such
    int expectedModCount = modCount;
    int mySize = getProperListSize();

    @Override
    public boolean hasNext() {
      return (cursor != mySize);
    }

    @SuppressWarnings("unchecked")
    @Override
    public E next() {
      checkForComodification();
      int i = cursor;
      if (i >= size()) {
        throw new NoSuchElementException();
      }
      cursor = i + 1;
      return (E) get(lastRet = i);
    }

    @Override
    public void remove() {
      if (lastRet < 0) {
        throw new IllegalStateException();
      }
      checkForComodification();

      try {
        removeInt(lastRet);
        cursor = lastRet;
        lastRet = -1;
        expectedModCount = modCount;
      } catch (IndexOutOfBoundsException ex) {
        throw new ConcurrentModificationException();
      }
    }

    final void checkForComodification() {
      if (modCount != expectedModCount) {
        throw new ConcurrentModificationException();
      }
    }
  }
  
  //====|    CycListItr    |======================================================================//

  private class CycListItr extends CycItr implements ListIterator<E> {

    CycListItr(int index) {
      super();
      cursor = index;
    }

    @Override
    public boolean hasPrevious() {
      return (cursor != 0);
    }

    @Override
    public int nextIndex() {
      return cursor;
    }

    @Override
    public int previousIndex() {
      return (cursor - 1);
    }

    @SuppressWarnings("unchecked")
    @Override
    public E previous() {
      checkForComodification();
      int i = cursor - 1;
      if (i < 0) {
        throw new NoSuchElementException();
      }
      cursor = i;
      return (E) get(lastRet = i);
    }

    @Override
    public void set(E e) {
      if (lastRet < 0) {
        throw new IllegalStateException();
      }
      checkForComodification();
      try {
        setInt(lastRet, e);
      } catch (IndexOutOfBoundsException ex) {
        throw new ConcurrentModificationException();
      }
    }

    @Override
    public void add(E e) {
      checkForComodification();
      try {
        int i = cursor;
        addInt(i, e);
        cursor = i + 1;
        lastRet = -1;
        expectedModCount = modCount;
      } catch (IndexOutOfBoundsException ex) {
        throw new ConcurrentModificationException();
      }
    }
  }
  
  //====|    UnmodifiableCycList    |=============================================================//
  
  /**
   * Unmodifiable CycList. Attempts to invoke methods which would alter the underlying structure 
   * will cause a {@link java.lang.UnsupportedOperationException} to be thrown.
   * 
   * @param <E> 
   */
  static public class UnmodifiableCycList<E> extends CycArrayList<E> {

    public UnmodifiableCycList(CycList<? extends E> list) {
      for (int i = 0; i < list.getProperListSize(); i++) {
        super.add((E) list.get(i));
      }
      if (!list.isProperList()) {
        super.setDottedElement(list.getDottedElement());
      }
    }

    private UnmodifiableCycList() {
      super();
    }

    @Override
    public boolean add(E e) {
      throw new UnsupportedOperationException();
    }
    /*
    @Override
    public void add(boolean b) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void add(double d) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void add(float f) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void add(int i) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void add(long l) {
      throw new UnsupportedOperationException();
    }
    */
    @Override
    public void add(int index, E element) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends E> col) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
      throw new UnsupportedOperationException();
    }

    @Override
    public CycArrayList<E> addAllNew(Collection<? extends E> objects) {
      throw new UnsupportedOperationException();
    }

    @Override
    public CycArrayList<E> addNew(E object) {
      throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean addPair(Object key, Object value) {
      throw new UnsupportedOperationException();
    }

    @Override
    public CycArrayList<E> addQuoted(Object object) {
      throw new UnsupportedOperationException();
    }

    @Override
    public CycArrayList<E> addToBeginning(E element) {
      throw new UnsupportedOperationException();
    }

    @Override
    public CycArrayList<E> appendElement(E object) {
      throw new UnsupportedOperationException();
    }
    /*
    @Override
    public CycArrayList<E> appendElement(boolean b) {
      throw new UnsupportedOperationException();
    }

    @Override
    public CycArrayList<E> appendElement(int i) {
      throw new UnsupportedOperationException();
    }

    @Override
    public CycArrayList<E> appendElement(long l) {
      throw new UnsupportedOperationException();
    }
    */
    @Override
    public CycArrayList<E> appendElements(Collection<? extends E> cycList) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
      throw new UnsupportedOperationException();
    }

    @Override
    public E remove(int index) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection c) {
      throw new UnsupportedOperationException();
    }

    @Override
    public CycArrayList removeDuplicates() {
      throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean removeIf(Predicate filter) {
      throw new UnsupportedOperationException();
    }
    
    @Override
    public void removePairs(Object key) {
      throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean removeProperty(CycSymbol indicator) {
      throw new UnsupportedOperationException();
    }
    
    @Override
    protected void removeRange(int fromIndex, int toIndex) {
      throw new UnsupportedOperationException();
    }
    
    @Override
    public void replaceAll(UnaryOperator operator) {
      throw new UnsupportedOperationException();
    }
    
    @Override
    public E set(int index, E element) {
      throw new UnsupportedOperationException();
    }

    @Override
    public CycArrayList<E> setDottedElement(E dottedElement) {
      throw new UnsupportedOperationException();
    }

    @Override
    public CycArrayList<E> setSpecifiedObject(List<Integer> pathSpecification, E value) {
      throw new UnsupportedOperationException();
    }
    
    @Override
    public E setf(CycSymbol indicator, E value) {
      throw new UnsupportedOperationException();
    }
    
    @Override
    public CycArrayList sort() {
      throw new UnsupportedOperationException();
    }

    @Override
    public CycArrayList subst(E newObject, E oldObject) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void trimToSize() {
      throw new UnsupportedOperationException();
    }
    
    @Override
    public void sort(Comparator c) {
      throw new UnsupportedOperationException();
    }
    
  }
  
}
