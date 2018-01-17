package com.cyc.kb.client;

/*
 * #%L
 * File: KbObjectImplFactory.java
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
import com.cyc.base.CycAccess;
import com.cyc.base.cycobject.CycAssertion;
import com.cyc.base.cycobject.CycList;
import com.cyc.base.cycobject.CycObject;
import com.cyc.base.cycobject.CycSymbol;
import com.cyc.base.cycobject.CycVariable;
import com.cyc.base.cycobject.DenotationalTerm;
import com.cyc.base.cycobject.FormulaSentence;
import com.cyc.base.cycobject.Nart;
import com.cyc.base.exception.CycApiException;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.baseclient.CycObjectFactory;
import com.cyc.baseclient.cycobject.CycVariableImpl;
import com.cyc.baseclient.cycobject.DefaultCycObjectImpl;
import com.cyc.baseclient.cycobject.FormulaSentenceImpl;
import com.cyc.kb.Context;
import com.cyc.kb.KbCollection;
import com.cyc.kb.KbObject;
import com.cyc.kb.KbStatus;
import com.cyc.kb.exception.CreateException;
import com.cyc.kb.exception.KbObjectNotFoundException;
import com.cyc.kb.exception.KbRuntimeException;
import com.cyc.kb.exception.KbTypeConflictException;
import com.cyc.kb.exception.KbTypeException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.cyc.kb.client.KbObjectImpl.getStaticAccess;

/**
 * This class provides factory methods to build instance of KBObjects and its subclasses. The class
 * also provides caching for the retrieved objects. Note that the cache may be stale and the API
 * does not yet attempt to synchronize based on KB Events.
 *
 * The class and the methods of this class are not part of the KB API.
 *
 * @author David Baxter
 * @version $Id: KbObjectImplFactory.java 176591 2018-01-09 17:27:27Z nwinant $
 */
public class KbObjectImplFactory {

  private static final Logger LOG = LoggerFactory.getLogger(KbObjectImplFactory.class);
  
  //a cache from the cyclified names/ids/non-cyclified names to classes to KBObjects
  private static final Map<String, Map<Class<?>, KbObjectImpl>> STRING_CACHE
          = new ConcurrentHashMap<>();
  
  /*
  private static final List<Class<? extends KbObjectImpl>> KB_OBJECT_TYPES = Arrays.asList(
          AssertionImpl.class,
          BinaryPredicateImpl.class,
          ContextImpl.class,
          FactImpl.class,
          FirstOrderCollectionImpl.class,
          KbFunctionImpl.class,
          KbCollectionImpl.class,
          KbIndividualImpl.class,
          KbObjectImpl.class,
          KbTermImpl.class,
          KbPredicateImpl.class,
          QuantifierImpl.class,
          LogicalConnectiveImpl.class,
          ScopingRelationImpl.class,
          RelationImpl.class,
          RuleImpl.class,
          SecondOrderCollectionImpl.class,
          SentenceImpl.class,
          VariableImpl.class);
  */
  
  protected static final Map<CycObject, Class<? extends KbObjectImpl>> CYC_OBJECT_TO_KB_API_CLASS 
          = new HashMap<>();
  
  static {
    CYC_OBJECT_TO_KB_API_CLASS.put(
            LogicalConnectiveImpl.getClassTypeCore(), 
            LogicalConnectiveImpl.class);
    CYC_OBJECT_TO_KB_API_CLASS.put(
            KbTermImpl.getClassTypeCore(),
            KbTermImpl.class);
    CYC_OBJECT_TO_KB_API_CLASS.put(
            KbCollectionImpl.getClassTypeCore(), 
            KbCollectionImpl.class);
    CYC_OBJECT_TO_KB_API_CLASS.put(
            FirstOrderCollectionImpl.getClassTypeCore(), 
            FirstOrderCollectionImpl.class);
    CYC_OBJECT_TO_KB_API_CLASS.put(
            SecondOrderCollectionImpl.getClassTypeCore(), 
            SecondOrderCollectionImpl.class);
    CYC_OBJECT_TO_KB_API_CLASS.put(
            KbIndividualImpl.getClassTypeCore(), 
            KbIndividualImpl.class);
    CYC_OBJECT_TO_KB_API_CLASS.put(
            ContextImpl.getClassTypeCore(),
            ContextImpl.class);
    CYC_OBJECT_TO_KB_API_CLASS.put(
            RelationImpl.getClassTypeCore(),
            RelationImpl.class);
    CYC_OBJECT_TO_KB_API_CLASS.put(
            KbFunctionImpl.getClassTypeCore(), 
            KbFunctionImpl.class);
    CYC_OBJECT_TO_KB_API_CLASS.put(
            KbPredicateImpl.getClassTypeCore(),
            KbPredicateImpl.class);
    CYC_OBJECT_TO_KB_API_CLASS.put(
            BinaryPredicateImpl.getClassTypeCore(),
            BinaryPredicateImpl.class);
    CYC_OBJECT_TO_KB_API_CLASS.put(
            QuantifierImpl.getClassTypeCore(),
            QuantifierImpl.class);
  }
  
  protected static final Map<Class<? extends KbObjectImpl>, Class<? extends CycObject>>
          KB_API_CYC_OBJECT_CONSTRUCTOR_ARG = new LinkedHashMap();
  
  static {
    KB_API_CYC_OBJECT_CONSTRUCTOR_ARG.put(KbTermImpl.class, DenotationalTerm.class);
    KB_API_CYC_OBJECT_CONSTRUCTOR_ARG.put(SentenceImpl.class, FormulaSentence.class);
    KB_API_CYC_OBJECT_CONSTRUCTOR_ARG.put(VariableImpl.class, CycVariable.class);
    KB_API_CYC_OBJECT_CONSTRUCTOR_ARG.put(SymbolImpl.class, CycSymbol.class);
    KB_API_CYC_OBJECT_CONSTRUCTOR_ARG.put(AssertionImpl.class, CycAssertion.class);
    KB_API_CYC_OBJECT_CONSTRUCTOR_ARG.put(KbObjectImpl.class, CycObject.class);
  }
  
  /**
   * Creates a term and reifies it based on cyclifiedStr
   *
   * @param cyclifiedStr  the string to be converted to a CycObject 
   *
   * @throws KbRuntimeException if IOException is thrown by CycAPI
   */
  private static CycObject getTempCoreFromCycL(final String possibleCyclString) 
          throws KbRuntimeException {
    try {
      final String cyclifiedStr = getStaticAccess().cyclifyString(possibleCyclString);
      CycObject tempCore;
      try {
        tempCore = getStaticAccess().getLookupTool().getTermByName(cyclifiedStr);
      } catch (CycApiException e) {
        try {
          tempCore = FormulaSentenceImpl.makeFormulaSentence(getStaticAccess(), cyclifiedStr);
        } catch (ClassCastException ex) {
          // TODO: Is there a better way to dothis? We rely on a ClassCastException to create new 
          //       terms, which is probably not the best way to handle the lowest level critical 
          //       code.
          return null;    // Could not parse as a Sentence, and we can't do anything more.
        }
      }
      if (tempCore instanceof Nart) {
        ((Nart) tempCore).ensureReified(getStaticAccess());
      }
      return tempCore;
    } catch (CycConnectionException cce) {
      throw KbRuntimeException.fromThrowable(cce);
    }
  }
  
  protected static CycObject getTempCoreFromNameOrId(final String nameOrIdOrVar) {
    try {
      if (nameOrIdOrVar.startsWith("?")) {
        // TODO: Should we check in the cache to avoid variable name clash?
        return new CycVariableImpl(nameOrIdOrVar);
      }
      try {
        final Object o = DefaultCycObjectImpl
                .fromPossibleCompactExternalId(nameOrIdOrVar, getStaticAccess());
        if (o instanceof CycObject) {
          return (CycObject) o;
        }
      } catch (CycApiException ex) {
        // Wasn't an HLID, so quietly move on to next attempt...
      }
      try {
        return getTempCoreFromCycL(nameOrIdOrVar);
      } catch (CycApiException ex) {
        return null;  //  ... but it also can't be retrieved as CycL, so we're done.
      }
    } catch (CycConnectionException cce) {
      throw KbRuntimeException.fromThrowable(cce);
    }
  }
  
  /**
   * Find an instance of {@link KbObjectImpl} subclass <code>O</code>, named <code>nameOrId</code>.
   * If no object exists in the KB with the name <code>nameOrId</code>, it will throw a
   * {@link KbObjectNotFoundException}.
   *
   * If there is already an object in the KB called <code>nameOrId</code>, and it is already a
   * {@link StandardKBObject#getType()}, it will be returned. If it is not already a
   * {@link StandardKBObject#getType()}, a {@link KbTypeException} is thrown
   *
   * @param nameOrId the string representation or the HLID of the candidate object to be returned
   * @param c represents the class <code>O</code>, a subclass of {@link KbObjectImpl}
   *
   * @return an instance of {@link KbObjectImpl} subclass <code>O</code>
   *
   * @throws CreateException
   * @throws KbTypeException
   */
  private static <O extends KbObjectImpl> O find(
          final String nameOrId, final Class<O> c) throws CreateException, KbTypeException {
    final O kbObj = KbObjectImplFactory.<O>getCached(nameOrId, c);
    if (kbObj != null) {
      LOG.trace("The object " + kbObj + " was retrieved from cache.");
      return kbObj;
    }
    final CycObject cycObject = getTempCoreFromNameOrId(nameOrId);
    if (cycObject != null) {
      return (O) get(cycObject, c);
    } else {
      String msg = "No KB object \"" + nameOrId + "\" as " + c.getSimpleName() + ".";
      //since this is called from findOrCreate, it's not necessarily an error. Don't log it as such.
      LOG.trace(msg);
      throw new KbObjectNotFoundException(msg);
    }
//    // Try subclasses of c, using most specific that works.
//    Class<? extends KBObjectImpl> bestClass = c;
//    kbObj = (O) getAsInstanceOfSpecifiedClass(nameOrId, bestClass, LookupType.FIND);
//    if (kbObj != null) {
//      bestClass = kbObj.getClass();
//    }
//    
//    if (kbObj == null || !kbObj.isVariable()){
//      LOGGER.trace("Attempting to find a more specific class than " + bestClass 
//                   + " for " + kbObj);
//      for (final Class<? extends KBObjectImpl> subclass : KB_OBJECT_TYPES) {
//        if (bestClass.isAssignableFrom(subclass) && !bestClass.equals(subclass)) {
//          try {
//            final O asSubclass = (O) getAsInstanceOfSpecifiedClass(
//                                              nameOrId, subclass, LookupType.FIND);
//            if (asSubclass != null) {
//              kbObj = (O) cacheKBObject(asSubclass, nameOrId, subclass);
//              LOGGER.trace("Found a more specific class " + subclass + " than " + bestClass
//                           + " for " + kbObj);
//              bestClass = subclass;
//            }
//          } catch (Exception ex) {
//            KBApiExceptionHandler.rethrowIfCycConnectionException(ex);
//            
//            // Guess it's not one of those.
//            
//            // FIXME: do something here.
//          } catch (Throwable t) {
//            // FIXME: do something here.
//          }
//        }
//      }
//    }
//    if (kbObj == null) {
//      String msg = "No KB object \"" + nameOrId + "\" as " + c.getSimpleName() + ".";
//      LOGGER.error(msg);
//      throw new KBObjectNotFoundException(msg);
//    } else {
//      LOGGER.trace("Found " + kbObj + " and cached it");
//      return (O) cacheKBObject(kbObj, nameOrId, c);
//    }
  }

  /**
   * This method tries to construct a object of Class <code>c</code> with <code>nameOrId</code> and
   * <code>lookup</code> as parameters to the constructor.
   *
   * Refer to {@link StandardKBObject#StandardKBObject(String, LookupType)} for the type of
   * constructor the method is looking for.
   *
   * @param nameOrId the string representation or the HLID of the candidate object to be returned
   * @param c the class of the object to be constructed
   * @param lookup find or create the candidate object
   *
   * @return a object of type <code>c</code> which is a subclass of KBObject
   *
   * @throws CreateException Refer to {@link StandardKBObject#StandardKBObject(String, LookupType)}
   * @throws KbTypeException Refer to {@link StandardKBObject#StandardKBObject(String, LookupType)}
   *
   * @throws IllegalArgumentException Java run-time exception
   * @throws SecurityException Java run-time exception
   */
  private static <O extends KbObjectImpl> O getAsInstanceOfSpecifiedClass(
          final String nameOrId, final Class<O> c, final LookupType lookup)
          throws IllegalArgumentException, SecurityException, CreateException, KbTypeException {
    O kbObj = null;
    try {
      kbObj = (O) c.getDeclaredConstructor(String.class, LookupType.class)
              .newInstance(nameOrId, lookup);
      kbObj = cacheKBObject(kbObj, nameOrId, c);
      LOG.trace("Found " + kbObj + " and cached it");
    } catch (InvocationTargetException ex) {
      KbExceptionHandler.rethrowIfCycConnectionException(ex.getCause());
      if (ex.getCause() instanceof KbTypeException) {
        throw (KbTypeException) ex.getCause();
      } else if (ex.getCause() instanceof CreateException) {
        throw (CreateException) ex.getCause();
      } else {
        LOG.error(ex.getMessage());
      }
    } catch (NoSuchMethodException | InstantiationException ex) {
      LOG.trace(ex.getMessage());
    } catch (IllegalAccessException ex) {
      LOG.error(ex.getMessage());
    }
    return kbObj;
  }

  /**
   * Clear all caches relating <code>KBObject</code>s to objects on the Cyc server. In most
   * applications, this will not be needed. However, it can be helpful in applications where the KB
   * is modified externally. For example, if a Cyc term is deleted and then recreated with the same
   * name by some external process, the KBAPI will still have the id information from the old
   * constant, and will retrieve the new constant. A call to <code>clearKBObjectCache</code> will
   * clear the cache and allow the KBAPI to successfully retrieve the newly created constant.
   */
  public static void clearKBObjectCache() {
    LOG.info("Cleaning the cache");
    STRING_CACHE.clear();
    CycObjectFactory.resetCycConstantCaches();
  }

  /**
   * Attempt to find a CycObject <code>cycObject</code> in the cache, as an instance of a subclass
   * <code>O</code> of KBObject
   *
   * @param cycObject
   * @param c represents the class <code>O</code>, a subclass of {@link KbObjectImpl}
   *
   * @return the cached {@link KbObjectImpl}, or null if there is no cached value
   */
  private static <O extends KbObject> O getCached(final CycObject cycObject, final Class<O> c) {
    return getCached(getCacheKey(cycObject), c);
  }

  /**
   * Attempt to find an object of class <code>c</code> represented by <code>nameOrId</code> in the
   * local API cache
   *
   * @param nameOrId the string representation or the HLID of the candidate object to be returned
   * @param c represents the class <code>O</code>, a subclass of {@link KbObjectImpl}
   *
   * @return the cached {@link KbObjectImpl}, or null if there is no cached value
   */
  private static <O extends KbObject> O getCached(final String nameOrId, final Class<O> c) {
    final List<String> invalidKeys = new ArrayList<>();
    O returnValue = null;
    if (STRING_CACHE.containsKey(nameOrId)) {
      //if we find something that could be a c, return it.  It might be a subclass, but that's OK.
      LOG.trace("Found cache-key \"" + nameOrId + "\" in the cache");
      for (Entry<Class<?>, KbObjectImpl> e : STRING_CACHE.get(nameOrId).entrySet()) {
        if (c.isAssignableFrom(e.getValue().getClass()) && e.getValue().isValid()) {
          returnValue = (O) e.getValue();
          LOG.debug("Found \"" + returnValue + "\" in the cache");
        } else if (!e.getValue().isValid()) {
          LOG.info("The cached entry " + e + " is not valid anymore! Adding to the cache.");
          invalidKeys.add(nameOrId);
          invalidKeys.add(e.getValue().getCore().cyclify());
          invalidKeys.add(e.getValue().getCore().toString());
        }
      }
    }
    if (!invalidKeys.isEmpty()) {
      for (String key : invalidKeys) {
        STRING_CACHE.remove(key);
      }
    }
    return returnValue;
  }

  /**
   * Cache any newly created object. The keys to the object are, <code>nameOrId</code>, the
   * cyclified string representation of the "core" object and the toString() representation of the
   * "core" object.
   *
   * @param kbObject the {@link KbObjectImpl} to be cached
   * @param nameOrId one of the keys to the {@link KbObjectImpl} in the cache
   * @param c represents the class <code>O</code>, a subclass of {@link KbObjectImpl}
   *
   * @return the kbObject casted to class <code>O</code>
   */
  private static <O extends KbObject> O cacheKBObject(
          final KbObjectImpl kbObject, final String nameOrId, final Class<O> c) {
    final CycObject core = kbObject.getCore();
    final String cyclifiedCore = core.cyclify();
    KbObjectImpl result = kbObject;
    LOG.trace("Storing " + result + " in cache");
    if (STRING_CACHE.containsKey(cyclifiedCore)
            && STRING_CACHE.get(cyclifiedCore).containsKey(c)) {
      //if this is already in the cache, but not by the ID, use the existing one.
      if (!STRING_CACHE.containsKey(nameOrId)) {
        result = STRING_CACHE.get(cyclifiedCore).get(c);
      }
    }
    final String coreString = getCacheKey(core);
    cacheAs(nameOrId, c, result);
    cacheAs(cyclifiedCore, c, result);
    cacheAs(coreString, c, result);
    return (O) result;
  }

  /**
   * Cache the <code>kbObject</code> with the key <code>key</code>.
   *
   * @param   key       the key to the {@link KbObjectImpl} cached
   * @param   c         represents the class <code>O</code>, a subclass of {@link KbObjectImpl}
   * @param   kbObject  the {@link KbObjectImpl} to be cached
   */
  private static <O extends KbObject> void cacheAs(
          final String key, Class<O> c, final KbObjectImpl kbObject) {
    if (!STRING_CACHE.containsKey(key)) {
      STRING_CACHE.put(key, new ConcurrentHashMap<Class<?>, KbObjectImpl>());
    }
    KbObjectImpl bestKBObject = kbObject;
    final Map<Class<?>, KbObjectImpl> cacheForKey = STRING_CACHE.get(key);
    // Ensure the most specific KBObject is used for all classes:
    for (final KbObjectImpl maybeBest : cacheForKey.values()) {
      if (bestKBObject != maybeBest
              && bestKBObject.getClass().isAssignableFrom(maybeBest.getClass())) {
        bestKBObject = maybeBest;
      }
    }
    cacheForKey.put(c, bestKBObject);
    for (final Class<?> oneClass : cacheForKey.keySet()) {
      cacheForKey.put(oneClass, bestKBObject);
    }
  }
  
  /**
   * Convert a CycObject into an instance of its most specific type. E.g., if a CycList instance 
   * were created for <code>(#$TheFruitFn #$AppleTree)</code>, this method would convert it into a 
   * Naut instance representing the same. This is important because otherwise two seemingly-
   * identical KbObjects could be created with different CycObject cores for the same term, and 
   * their #equals method would return false for each other.
   * 
   * @param   cycObject  the CycObject to tighten
   * @return  the cycObject, converted to the most specific appropriate CycObject type
   */
  public static CycObject tightenCycObject(final CycObject cycObject) {
    return (cycObject instanceof CycList)
            ? getStaticAccess().getObjectTool().toMostSpecificCycObject((CycList) cycObject)
            : cycObject;
  }
  
  /**
   * Find an instance of {@link KbObjectImpl} subclass <code>O</code>, based on
   * <code>cycObject</code>. If no object exists based on cycObject in the KB, it will throw a
   * {@link KbObjectNotFoundException}.
   *
   * <p>If there is already an object in the KB based on <code>cycObject</code>, and it is already a
   * {@link StandardKBObject#getType()}, it will be returned. If it is not already a
   * {@link StandardKBObject#getType()}, a {@link KbTypeException} is thrown.
   *
   * @param   <O>            the class of object to be returned
   * @param   rawCycObject   the candidate CycObject
   * @param   requestedClass the class <code>O</code>, a subclass of {@link KbObjectImpl} to be 
   *                         returned
   * @return  an instance of {@link KbObjectImpl} subclass <code>O</code>
   * @throws  CreateException  
   * @throws  KbTypeException  
   */
  //if there's already a CycObject, then just assume that it's a FIND, not a FIND_OR_CREATE
  //@todo document
  @Deprecated
  public static <O extends KbObjectImpl> O get(
          final CycObject rawCycObject, final Class<O> requestedClass) throws KbTypeException, CreateException {
    if (CycObjectFactory.nil.equals(rawCycObject)
            || (rawCycObject instanceof List && ((List) rawCycObject).isEmpty())) {
      throw new KbTypeException("NIL is not a " + requestedClass);
    }
    final CycObject cycObject = tightenCycObject(rawCycObject);
    final O cachedKbObj = KbObjectImplFactory.<O>getCached(cycObject, requestedClass);
    if (cachedKbObj != null) {
      CycObject cycObjNonNart = cycObject;
      CycObject kbObjNonNart = cachedKbObj.getCore();
      if (cycObject instanceof Nart) {
        cycObjNonNart = ((Nart) cycObject).getFormula();
      }
      if (kbObjNonNart instanceof Nart) {
        kbObjNonNart = ((Nart) kbObjNonNart).getFormula();
      }
      if (kbObjNonNart.equals(cycObjNonNart)) {
        LOG.trace("The object " + cachedKbObj + " was retrieved from cache.");
        return cachedKbObj;
      }
    }
    Class<? extends O> requiredKbClass = requestedClass;
    if (cycObject instanceof CycAssertion) {
      if (((CycAssertion) cycObject).isGaf()) {
        requiredKbClass = (Class<? extends O>) FactImpl.class;
      } else {
        requiredKbClass = (Class<? extends O>) RuleImpl.class;
      }
    } else if (cycObject instanceof FormulaSentence) {
      requiredKbClass = (Class<? extends O>) SentenceImpl.class;
    } else if (cycObject instanceof CycVariable) {
      requiredKbClass = (Class<? extends O>) VariableImpl.class;
    } else if (cycObject instanceof CycSymbol) {
      requiredKbClass = (Class<? extends O>) SymbolImpl.class;
    } else {
      CycObject tightestCycCol = null;
      try {
        tightestCycCol = getStaticAccess().getInspectorTool().categorizeTermWRTApi(cycObject);
      } catch (CycConnectionException cce) {
        throw KbRuntimeException.fromThrowable(cce);
      }
      final Class tightestKbClass = CYC_OBJECT_TO_KB_API_CLASS.get(tightestCycCol);
      if (tightestCycCol != null && tightestKbClass != null) {
        if (requiredKbClass.isAssignableFrom(tightestKbClass)) {
          requiredKbClass = tightestKbClass;
        } else // Currently the tightening code only makes sence for subclasses of KBTerm
         if (cycObject instanceof DenotationalTerm) {
            // If the user wants to tighten the object, we should allow them
            if (tightestKbClass.isAssignableFrom(requiredKbClass)) {
              // Say, currently TermX is an Individual and user wants to turn it into
              // Context (#$Microtheory) then this will be true
              // Best class will remain what the user has passed in.
              //
              // If the user wants to coerce, say in findOrCreate, it expects KBTypeException
              // In the get() code path, KBTypeException is appropriate to indicate, 
              // that something was found, but was a different but coercible type
              throw new KbTypeException(cycObject.toString() + " is of type "
                      + tightestKbClass.getSimpleName() + ", but is being requested as "
                      + requiredKbClass.getSimpleName() + "."
                      + " Use findOrCreate to coerce into the requested type.");
            } else {
              throw new KbTypeConflictException(cycObject.toString() + " is of type "
                      + tightestKbClass.getSimpleName() + ", but is being requested as "
                      + requiredKbClass.getSimpleName() + ", which are incompatible types.");
            }
          }
      }
    }
    Exception constructionException = null;
    try {
      final O kbObj = (O) constructKbObject(requiredKbClass, cycObject);
      return cacheKBObject(kbObj, getCacheKey(cycObject), requestedClass);
    } catch (NoSuchMethodException
            | SecurityException 
            | InstantiationException 
            | IllegalAccessException 
            | IllegalArgumentException
            | InvocationTargetException e) {
      LOG.error("Could not create {} from {}: {}", requiredKbClass, cycObject, e.getMessage());
      //e.printStackTrace(System.err);
      constructionException = e;
    }
//    for (final Class<? extends KBObjectImpl> subClass : KB_OBJECT_TYPES) {
//      if (bestClass.isAssignableFrom(subClass)) {
//        try {
//          kbObj = (O) subClass.getDeclaredConstructor(CycObject.class).newInstance(cycObject);
//          kbObj = cacheKBObject(kbObj, getCacheKey(cycObject), c);
//          LOGGER.trace("Found a more specific class " + subClass + " than " + bestClass + " for "
//                       + kbObj);
//          bestClass = (Class<? extends O>) subClass;
//        } catch (Exception e) {
//        }
//      }
//    }
    //if (kbObj == null) {
      // Why are we doing this? 
      // If we can't get something based on proper Cyc object, why do we expect to find
      // using the Cyclified string??
      // This may create an infinite loop/stack overflow, 
      // due to get (string) ->find-> (new link being introduced) get (CycObject)
      // DaveS says, we did this because, if a CycObject is deleted outside of the scope of
      // the API, then we may possibly find it based on just the cyclified string. We decided
      // not to support this behavior anymore.
      //return get(cycObject.cyclify(), c);
    final String msg
            = "No KB object \"" + cycObject.toString()
            + "\" as " + requestedClass.getSimpleName() + ".";
    LOG.error(msg);
    if (constructionException != null) {
      throw KbObjectNotFoundException.fromThrowable(msg, constructionException);
    }
    throw new KbObjectNotFoundException(msg);
  }
  
  static Class<? extends CycObject> getKbObjectCycCoreClassConstructorArg(Class requestedKbClass) {
    for (Class<? extends KbObjectImpl> kbClassKey : KB_API_CYC_OBJECT_CONSTRUCTOR_ARG.keySet()) {
      if (kbClassKey.isAssignableFrom(requestedKbClass)) {
        return KB_API_CYC_OBJECT_CONSTRUCTOR_ARG.get(kbClassKey);
      }
    }
    return CycObject.class;
  }
  
  static <O extends KbObjectImpl> O constructKbObject(
          final Class<O> requestedClass, final CycObject core)
          throws NoSuchMethodException, InstantiationException, IllegalAccessException,
          IllegalArgumentException, InvocationTargetException {
    final Class<? extends CycObject> constructorArgClass
            = getKbObjectCycCoreClassConstructorArg(requestedClass);
    return (O) requestedClass
            .getDeclaredConstructor(constructorArgClass)
            .newInstance(constructorArgClass.cast(core));
  }

  /**
   * Find an instance of {@link KbObjectImpl} subclass <code>O</code>, named <code>nameOrId</code>.
   * If no object exists in the KB with the name <code>nameOrId</code>, it will throw a
   * {@link KbObjectNotFoundException}.
   *
   * If there is already an object in the KB called <code>nameOrId</code>, and it is already a
   * {@link StandardKBObject#getType()}, it will be returned. If it is not already a
   * {@link StandardKBObject#getType()}, a {@link KbTypeException} is thrown
   *
   * @param <O> the class of object to be returned
   * @param nameOrId the string representation or the HLID of the candidate object to be returned
   * @param c represents the class <code>O</code>, a subclass of {@link KbObjectImpl}
   *
   * @return an instance of {@link KbObjectImpl} subclass <code>O</code>
   *
   * @throws CreateException
   * @throws KbTypeException
   */
  // NOTE: 2015-04-30, Vijay: Only KBTerm and its subclasses call this general get method
  // Assertion and its subclasses don't use this method. 
  static <O extends KbObjectImpl> O get(
          final String nameOrId, final Class<O> c) throws KbTypeException, CreateException {
    return KbObjectImplFactory.<O>find(nameOrId, c);
  }

  /**
   * Find or create an instance of {@link KbObjectImpl} subclass <code>O</code>, named
   * <code>nameOrId</code>. If no object exists in the KB with the name <code>nameOrId</code>, one
   * will be created, and it will be asserted to be an instance of
   * {@link StandardKBObject#getType()} in the KB.
   *
   * If there is already an object in the KB called <code>nameOrId</code>, and it is already a
   * {@link StandardKBObject#getType()}, it will be returned. If it is not already a
   * {@link StandardKBObject#getType()}, but can be made into one by addition of assertions to the
   * KB, such assertions will be made, and the object will be returned. If the object in the KB
   * cannot be turned into a {@link StandardKBObject#getType()} by adding assertions (i.e. some
   * existing assertion prevents it from being a {@link StandardKBObject#getType()}), a
   * <code>KBTypeConflictException</code>will be thrown.
   *
   * @param <O> the class of object to be returned
   * @param nameOrId the string representation or the HLID of the candidate object to be returned
   * @param c represents the class <code>O</code>, a subclass of {@link KbObjectImpl}
   *
   * @return an instance of {@link KbObjectImpl} subclass <code>O</code>
   *
   * @throws CreateException
   * @throws KbTypeException
   */
  static <O extends KbObjectImpl> O findOrCreate(final String nameOrId, final Class<O> c)
          throws CreateException, KbTypeException {
    try {
      return (O) find(nameOrId, c);
    } catch (KbTypeConflictException ex) {
      throw ex;
    } catch (KbObjectNotFoundException | KbTypeException ex) {
      // Coerce to desired type:
      return getAsInstanceOfSpecifiedClass(nameOrId, c, LookupType.FIND_OR_CREATE);
    }
  }

  /**
   * Find or create an instance of {@link KbObjectImpl} subclass <code>O</code>, based on
   * <code>cycObject</code>. In most cases, existence of <code>cycObject</code> implies that the
   * underlying concept is already in the KB.
   *
   * Check if <code>cycObject</code> is already a {@link StandardKBObject#getType()}, it will be
   * returned. If it is not already a {@link StandardKBObject#getType()}, but can be made into one
   * by addition of assertions to the KB, such assertions will be made, and the object will be
   * returned. If the object in the KB cannot be turned into a {@link StandardKBObject#getType()} by
   * adding assertions (i.e. some existing assertion prevents it from being a
   * {@link StandardKBObject#getType()}), a <code>KBTypeConflictException</code>will be thrown.
   *
   * @param <O> the class of object to be returned
   * @param cycObject the candidate CycObject
   * @param c represents the class <code>O</code>, a subclass of {@link KbObjectImpl} to be returned
   *
   * @return an instance of {@link KbObjectImpl} subclass <code>O</code>
   *
   * @throws CreateException
   * @throws KbTypeException
   */
  @Deprecated
  static <O extends KbObjectImpl> O findOrCreate(final CycObject cycObject, final Class<O> c)
          throws CreateException, KbTypeException {
    return findOrCreate(cycObject.cyclify(), c);
  }

  /**
   * Find or create an instance of {@link KbObjectImpl} subclass <code>O</code>, named
   * <code>nameOrId</code>, and also make it in instance of <code>constrainingCollection</code>. If
   * no object exists in the KB with the name <code>nameOrId</code>, one will be created, and it
   * will be asserted to be an instance of both {@link StandardKBObject#getType()} and
   * <code>constrainingCollection</code> in the KB.
   *
   * If there is already an object in the KB called <code>nameOrId</code>, and it is already a
   * {@link StandardKBObject#getType()} and a <code>constrainingCollection</code>, it will be
   * returned. If it is not already a {@link StandardKBObject#getType()} and a
   * <code>constrainingCollection</code>, but can be made into one by addition of assertions to the
   * KB, such assertions will be made, and the object will be returned. If the object in the KB
   * cannot be turned into both a {@link StandardKBObject#getType()} and a
   * <code>constrainingCollection</code> by adding assertions, a
   * <code>KBTypeConflictException</code>will be thrown.
   *
   * @param <O> the class of object to be returned
   * @param nameOrId the string representation or the HLID of the candidate object to be returned
   * @param constrainingCollection the additional constraining collection
   * @param c represents the class <code>O</code>, a subclass of {@link KbObjectImpl}
   *
   * @return an instance of {@link KbObjectImpl} subclass <code>O</code>
   *
   * @throws CreateException
   * @throws KbTypeException
   */
  static <O extends KbObjectImpl> O findOrCreate(
          final String nameOrId, final KbCollection constrainingCollection, final Class<O> c)
          throws CreateException, KbTypeException {
    O kbObject = findOrCreate(nameOrId, c);
    SentenceImpl s = new SentenceImpl(Constants.isa(), kbObject, constrainingCollection);
    FactImpl.findOrCreate(s, Constants.uvMt());
    /*try {
      new Fact(Constants.uvMt(), Constants.isa(), kbObject, constrainingCollection);
    } catch (Exception e) {
      throw new KBTypeException("The object \"" + kbObject + "\" can not be made an instance of \""
                                + constrainingCollection + "\"");
    }*/
    return kbObject;
  }

  /**
   * @see #findOrCreate(java.lang.String, com.cyc.kb.KBCollection, java.lang.Class)
   *
   * Instead of a KBCollection, a string representation of the KBCollection is the input.
   *
   */
  static <O extends KbObjectImpl> O findOrCreate(
          final String nameOrId, final String constrainingCollectionStr, final Class<O> c)
          throws CreateException, KbTypeException {
    O kbObject = findOrCreate(nameOrId, c);
    SentenceImpl s = new SentenceImpl(
            Constants.isa(), kbObject, KbCollectionImpl.get(constrainingCollectionStr));
    FactImpl.findOrCreate(s, Constants.uvMt());

    /*try {
      new Fact(Constants.uvMt(), Constants.isa(),
            kbObject, KBCollection.get(constrainingCollectionStr));
    } catch (Exception e){
      throw new KBTypeException("The object \"" + kbObject + "\" can not be made an instance of \"" 
                                + constrainingCollectionStr + "\"");
    }*/
    return kbObject;
  }

  /**
   * Find or create an instance of {@link KbObjectImpl} subclass <code>O</code>, named
   * <code>nameOrId</code>, and also make it in instance of <code>constrainingCollection</code>. If
   * no object exists in the KB with the name <code>nameOrId</code>, one will be created, and it
   * will be asserted to be an instance of both {@link StandardKBObject#getType()} and
   * <code>constrainingCollection</code> in <code>ctx</code> in the KB.
   *
   * If there is already an object in the KB called <code>nameOrId</code>, and it is already a
   * {@link StandardKBObject#getType()} and a <code>constrainingCollection</code>, it will be
   * returned. If it is not already a {@link StandardKBObject#getType()} and a
   * <code>constrainingCollection</code>, but can be made into one by addition of assertions to the
   * KB, such assertions will be made, and the object will be returned. If the object in the KB
   * cannot be turned into both a {@link StandardKBObject#getType()} and a
   * <code>constrainingCollection</code> by adding assertions, a
   * <code>KBTypeConflictException</code>will be thrown.
   *
   * @param <O> the class of object to be returned
   * @param nameOrId the string representation or the HLID of the candidate object to be returned
   * @param constrainingCollection the additional constraining collection
   * @param ctx ctx the context in which the resulting object must be an instance of
   * constrainingCollection
   * @param c represents the class <code>O</code>, a subclass of {@link KbObjectImpl}
   *
   * @return an instance of {@link KbObjectImpl} subclass <code>O</code>
   *
   * @throws CreateException
   * @throws KbTypeException
   */
  static <O extends KbObjectImpl> O findOrCreate(
          final String nameOrId,
          final KbCollection constrainingCollection,
          final Context ctx,
          final Class<O> c) throws CreateException, KbTypeException {
    final O kbObject = findOrCreate(nameOrId, c);
    final SentenceImpl s = new SentenceImpl(Constants.isa(), kbObject, constrainingCollection);
    FactImpl.findOrCreate(s, ctx);
    /*try {
      new Fact(ctx, Constants.isa(), kbObject, constrainingCollection);
    } catch (Exception e) {
      throw new KBTypeException("The object \"" + kbObject  + "\" can not be made an instance of \""
                                + constrainingCollection + "\"");
    }*/
    return kbObject;
  }

  /**
   * @see #findOrCreate(java.lang.String, com.cyc.kb.KBCollection, com.cyc.kb.Context,
   * java.lang.Class)
   *
   * Instead of a KBCollection and a Context, the string representations of them is the input.
   */
  static <O extends KbObjectImpl> O findOrCreate(
          final String nameOrId, 
          final String constrainingCollectionStr,
          final String ctxStr, 
          final Class<O> c) throws CreateException, KbTypeException {
    final O kbObject = findOrCreate(nameOrId, c);
    final SentenceImpl s = new SentenceImpl(
            Constants.isa(), kbObject, KbCollectionImpl.get(constrainingCollectionStr));
    FactImpl.findOrCreate(s, ContextImpl.get(ctxStr));
    /*try {
      new Fact(Context.get(ctxStr), Constants.isa(), kbObject,
          KBCollection.get(constrainingCollectionStr));
    } catch (Exception e) {
      throw new KBTypeException("The object \"" + kbObject
          + "\" can not be made an instance of \"" + constrainingCollectionStr
          + "\"");
    }*/
    return kbObject;
  }

  /**
   * Creates a new list of KB objects from a list of CycObjects. Also handles the case of
   * CycObjectFactory.nil as an empty list. If any of the objects are not CycObjects (e.g. numbers,
   * strings, etc.), they will be returned unchanged.
   *
   * @param objects
   * @return
   * @throws CreateException
   * @throws KbTypeException
   */
  public static List<Object> convertKbObjects(final Object objects)
          throws CreateException, KbTypeException {
    if (objects == CycObjectFactory.nil) {
      return Collections.EMPTY_LIST;
    }
    final List<Object> result = new ArrayList();
    for (Object obj : (List<Object>) objects) {
      if (obj instanceof CycObject) {
        result.add(KbObjectImpl.get((CycObject) obj));
      } else {
        result.add(obj);
      }
    }
    return result;
  }

  /**
   * Creates a new List of KB objects from a Collection of CycObjects. Also handles the case of
   * CycObjectFactory.nil as an empty list. If any of the objects are not CycObjects (e.g. numbers,
   * strings, etc.), a ClassCastException will be thrown.
   *
   * @param objects
   * @return
   * @throws CreateException
   * @throws KbTypeException
   */
  public static List<KbObject> asKbObjectList(final Object objects)
          throws CreateException, KbTypeException {
    if (objects == CycObjectFactory.nil) {
      return Collections.EMPTY_LIST;
    }
    final List<KbObject> result = new ArrayList<>();
    for (CycObject obj : (Collection<CycObject>) objects) {
      result.add(KbObjectImpl.get((CycObject) obj));
    }
    return result;
  }

  /**
   * Creates a new ordered Set of KB objects from a Collection of CycObjects. If the original
   * Collection is ordered, the elements of the Set will follow the same order based, with each
   * element positioned according to its <em>first</em> occurrence in the original Collection. Also
   * handles the case of CycObjectFactory.nil as an empty list. If any of the objects are not
   * CycObjects (e.g. numbers, strings, etc.), a ClassCastException will be thrown.
   *
   * @param objects
   * @return
   * @throws CreateException
   * @throws KbTypeException
   */
  public static Set<KbObject> asKbObjectSet(final Object objects)
          throws CreateException, KbTypeException {
    if (objects == CycObjectFactory.nil) {
      return Collections.EMPTY_SET;
    }
    final Set<KbObject> result = new LinkedHashSet<>();
    for (CycObject obj : (Collection<CycObject>) objects) {
      result.add(KbObjectImpl.get((CycObject) obj));
    }
    return result;
  }

  /**
   * Creates a new list of CycObjects from a list of KbObjects. If any of the objects are not
   * KbObjects (e.g. numbers, strings, etc.), a ClassCastException will be thrown.
   *
   * @param objects
   * @return
   * @throws CreateException
   * @throws KbTypeException
   */
  public static List<CycObject> asCycObjectList(final Collection<KbObject> objects)
          throws CreateException, KbTypeException {
    final List<CycObject> result = new ArrayList<>();
    for (KbObject obj : (List<KbObject>) objects) {
      result.add((CycObject) obj.getCore());
    }
    return result;
  }

  /**
   * Creates a new ordered Set of CycObjects from a Collection of KbObjects. If the original
   * Collection is ordered, the elements of the Set will follow the same order based, with each
   * element positioned according to its <em>first</em> occurrence in the original Collection. If
   * any of the objects are not KbObjects (e.g. numbers, strings, etc.), a ClassCastException will
   * be thrown.
   *
   * @param objects
   * @return
   * @throws CreateException
   * @throws KbTypeException
   */
  public static Set<CycObject> asCycObjectSet(final Collection<KbObject> objects) 
          throws CreateException, KbTypeException {
    final Set<CycObject> result = new LinkedHashSet<>();
    for (KbObject obj : (List<KbObject>) objects) {
      result.add((CycObject) obj.getCore());
    }
    return result;
  }

  /**
   * Returns a KBStatus enum which describes whether <code>nameOrId</code> exists in the KB and is
   * an instance of {@link StandardKBObject#getType()}.
   *
   * @param nameOrId either the name or HL ID of an entity in the KB
   * @param c represents the class <code>O</code>, a subclass of {@link KbObjectImpl}
   * @return an enum describing the existential status of the entity in the KB
   */
  public static KbStatus getStatus(final String nameOrId, final Class<? extends KbObjectImpl> c) {
    final CycAccess cyc = getStaticAccess();
    try {
      CycObject cycObject = (CycObject) DefaultCycObjectImpl
              .fromPossibleCompactExternalId(nameOrId, cyc); //also check from names
      if (cycObject == null) {
        final String cyclifiedIndStr = cyc.cyclifyString(nameOrId);
        try {
          cycObject = cyc.getLookupTool().getKnownFortByName(cyclifiedIndStr);
        } catch (CycApiException ex) {
          //do nothing, since this exception indicates that it couldn't find a fort by that name
        }
      }
      if (cycObject == null) {
        return KbStatus.DOES_NOT_EXIST;
      }
      return getStatus(cycObject, c);
    } catch (CycConnectionException ex) {
      throw KbRuntimeException.fromThrowable(ex);
    }
  }

  /**
   * Returns a KBStatus enum which describes whether <code>cycObject</code> exists in the KB and is
   * an instance of {@link StandardKBObject#getType()}.
   *
   * @param cycObject the candidate CycObject in the KB
   * @param c represents the class <code>O</code>, a subclass of {@link KbObjectImpl}
   * @return an enum describing the existential status of the entity in the KB
   */
  public static KbStatus getStatus(
          final CycObject cycObject, final Class<? extends KbObjectImpl> c) {
    final CycAccess cyc = getStaticAccess();
    try {
      final CycObject baseCycTypeCore = KbObjectImpl.getBaseCycTypeCore(c);
      if (cyc.getInspectorTool()
              .isa(cycObject, baseCycTypeCore, cyc.getObjectTool().makeElMt("InferencePSC"))) {
        return KbStatus.EXISTS_AS_TYPE;
      }
      if (cyc.getComparisonTool().provablyNotIsa(
              cycObject, baseCycTypeCore, cyc.getObjectTool().makeElMt("InferencePSC"))) {
        //this won't work for NAUT collections, but we shouldn't ever need those...
        return KbStatus.EXISTS_WITH_TYPE_CONFLICT;
      } else {
        return KbStatus.EXISTS_WITH_COMPATIBLE_TYPE;
      }
    } catch (CycConnectionException ex) {
      throw KbRuntimeException.fromThrowable(ex);
    }
  }

  /**
   * convert a CycObject to its string representation, which is one of the keys to the
   * <code>cycObject</code> if it is present in the cache.
   *
   * @param cycObject
   * @return a string representation of the CycObject
   */
  private static String getCacheKey(final CycObject cycObject) {
    return (cycObject instanceof CycAssertion) 
            ? String.valueOf(cycObject.hashCode())
            : cycObject.toString();
  }
}
