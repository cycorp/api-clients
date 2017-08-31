package com.cyc.query;

/*
 * #%L
 * File: QueryReader.java
 * Project: Query Client
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
import com.cyc.base.CycAccessManager;
import com.cyc.base.cycobject.CycConstant;
import com.cyc.base.cycobject.CycList;
import com.cyc.base.cycobject.CycObject;
import com.cyc.base.cycobject.CycSymbol;
import com.cyc.base.cycobject.DenotationalTerm;
import com.cyc.base.cycobject.ElMt;
import com.cyc.base.cycobject.FormulaSentence;
import com.cyc.base.cycobject.Nart;
import com.cyc.base.cycobject.Naut;
import com.cyc.base.cycobject.NonAtomicTerm;
import com.cyc.base.exception.CycApiException;
import com.cyc.base.exception.CycConnectionException;
import static com.cyc.baseclient.CycObjectFactory.makeCycSymbol;
import static com.cyc.baseclient.connection.SublApiHelper.wrapVariableBinding;
import com.cyc.baseclient.cycobject.CycConstantImpl;
import com.cyc.baseclient.cycobject.ElMtCycNaut;
import com.cyc.baseclient.cycobject.ElMtNart;
import com.cyc.baseclient.cycobject.GuidImpl;
import com.cyc.baseclient.inference.params.DefaultInferenceParameters;
import static com.cyc.baseclient.xml.cycml.CycmlDecoder.translateObject;
import com.cyc.kb.Context;
import com.cyc.kb.KbIndividual;
import com.cyc.kb.Sentence;
import com.cyc.kb.client.ContextImpl;
import com.cyc.kb.client.KbIndividualImpl;
import com.cyc.kb.client.SentenceImpl;
import com.cyc.kb.exception.KbException;
import com.cyc.query.exception.QueryConstructionException;
import com.cyc.query.exception.QueryRuntimeException;
import com.cyc.query.parameters.InferenceParameters;
import com.cyc.session.exception.SessionException;
import com.cyc.xml.query.CyclQuery;
import com.cyc.xml.query.CyclQueryUnmarshaller;
import com.cyc.xml.query.PropertyValue;
import com.cyc.xml.query.QueryFormula;
import com.cyc.xml.query.QueryID;
import com.cyc.xml.query.QueryInferenceProperties;
import com.cyc.xml.query.QueryInferenceProperty;
import com.cyc.xml.query.QueryMt;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Support for reading in a QueryImpl from XML.
 *
 * @see com.cyc.query.QueryImpl
 * @author baxter
 */
public class QueryReader {
  
  static private final Logger LOGGER = LoggerFactory.getLogger(QueryReader.class);
  final CyclQueryUnmarshaller unmarshaller;

  /**
   * Create a new QueryReader.
   *
   * @throws JAXBException
   */
  public QueryReader() throws JAXBException {
    unmarshaller = new CyclQueryUnmarshaller();
  }

  QueryReader(CyclQueryUnmarshaller unmarshaller) throws JAXBException {
    this.unmarshaller = unmarshaller;
  }

  /**
   * Read a query from an XML stream.
   *
   * @param stream
   * @return the query
   * @throws KbException if any needed KBObjects cannot be found or created.
   * @throws QueryConstructionException if any other problem is encountered
   */
  protected Query queryFromXML(final InputStream stream) throws KbException,
          QueryConstructionException {
    try {
      final Object contentTree = unmarshaller.unmarshal(stream);
      if (contentTree instanceof CyclQuery) {
        return convertQuery((CyclQuery) contentTree);
      } else {
        throw new IllegalArgumentException("Can't convert " + contentTree);
      }
    } catch (JAXBException ex) {
      throw new RuntimeException(ex);
    }
  }
  
  /**
   * Returns a Query object defined by a query term.
   *
   * @param queryTerm
   * @return a Query object defined by queryTerm
   * @throws KbException if any needed KBObjects cannot be found or created.
   * @throws com.cyc.query.exception.QueryConstructionException
   * @throws QueryRuntimeException if there is some other kind of problem
   */
  protected Query queryFromTerm(KbIndividual queryTerm) throws KbException,
          QueryConstructionException, QueryRuntimeException {
    final String coreCommand = "(get-cycl-query-in-xml " + queryTerm.stringApiValue() + ")";
    final String command
            = wrapVariableBinding(
                    coreCommand,
                    makeCycSymbol("*cycl-query-include-namespace?*"),
                    makeCycSymbol("T"));
    try {
      //final String xmlString = processXml(CycAccessManager.getCurrentAccess().converse().converseString(command));
      //final String xmlString = CycAccessManager.getCurrentAccess().converse().converseString(command);
      final String rawXmlString = CycAccessManager.getCurrentAccess().converse().converseString(command);
      LOGGER.debug(
              "Query XML:\n"
              + "---->>>>>> [START Query XML] ------------------------------------>>>>>>\n"
              + "{}\n"
              + "<<<<<<---- [END   Query XML] <<<<<<------------------------------------",
              rawXmlString);
      final String xmlString = processXml(rawXmlString);
      return queryFromXML(new ByteArrayInputStream(xmlString.getBytes()));
    } catch (KbException e) {
      throw e;
    } catch (CycApiException | CycConnectionException | SessionException e) {
      throw new QueryRuntimeException( e);
    }
  }

  /**
   * Convert a CyclQuery to a QueryImpl.
   *
   * @param cyclQuery
   * @return the new QueryImpl object.
   * @throws KbException if any needed KBObjects cannot be found or created.
   * @throws QueryConstructionException if any other problem is encountered
   * constructing the QueryImpl object.
   * @see CyclQuery
   */
  public Query convertQuery(CyclQuery cyclQuery) throws KbException, QueryConstructionException {
    final DenotationalTerm queryID = convertID(cyclQuery.getQueryID());
    final Sentence querySentence = new SentenceImpl(convertFormula(
            cyclQuery.getQueryFormula()));
    final Context queryContext = convertMt(cyclQuery.getQueryMt());
    InferenceParameters queryParams = null;
    for (final Object obj : cyclQuery.getQueryCommentOrQueryInferencePropertiesOrQueryIndexicals()) {
      if (obj instanceof QueryInferenceProperties) {
        try {
          queryParams = convertParams((QueryInferenceProperties) obj);
        } catch (SessionException ex) {
          throw new QueryConstructionException("Couldn't convert parameters.", ex);
        }
      }
    }
    final Query query = QueryFactory.getQuery(querySentence, queryContext, queryParams);
    ((QueryImpl)query).setId(KbIndividualImpl.findOrCreate(queryID));
    return query;
  }

  private DenotationalTerm convertID(QueryID queryID) {
    if (queryID.getConstant() != null) {
      return (CycConstant) translateObject(queryID.getConstant());
    } else {
      return (NonAtomicTerm) translateObject(queryID.getFunction());
    }
  }

  private FormulaSentence convertFormula(QueryFormula queryFormula) {
    return (FormulaSentence) translateObject(
            queryFormula.getSentence());
  }

  private Context convertMt(QueryMt queryMt) throws KbException {
    if (queryMt.getConstant() != null) {
      return ContextImpl.get(((CycConstant) translateObject(
              queryMt.getConstant())).stringApiValue());
    } else {
      final NonAtomicTerm mtNAT = (NonAtomicTerm) translateObject(
              queryMt.getFunction());
      final ElMt elmt = (mtNAT instanceof Nart) //@todo mtNat can probably be sent directly to Context.get(), without trying to convert to either an ELMt or a CycNaut
              ? ElMtNart.makeElMtNart((Nart) mtNAT)
              : ElMtCycNaut.makeElMtCycNaut(
                      ((Naut) mtNAT).getArgs());
      return ContextImpl.get(elmt);

    }
  }
  
// The code below was commented out as part of an egregious hack to allow list-based inference parameters like :result-sort-order.  Once
// that hack is backed out, this can become the main code here again.
//
//  private InferenceParameters convertParams (
//          QueryInferenceProperties queryInferenceProperties)  throws SessionException {
//    final StringBuilder sb = new StringBuilder();
//    for (final QueryInferenceProperty qip : queryInferenceProperties.getQueryInferenceProperty()) {
//      if (sb.length() > 0) {
//        sb.append(" ");
//      }
//      final String symbolName = qip.getPropertySymbol().getContent().trim();
//      sb.append(":").append(symbolName).append(" ");
//      appendPropertyValue(qip.getPropertyValue(), sb);
//    }
//    final String paramString = sb.toString();
//    return new DefaultInferenceParameters(CycAccessManager.getCurrentAccess(), paramString);
//  }
//
//  private void appendPropertyValue(final PropertyValue val,
//          final StringBuilder sb) {
//    if (val.getConstant() != null) {
//      sb.append(((CycConstant) translateObject(
//              val.getConstant())).stringApiValue());
//    } else if (val.getFunction() != null) {
//      sb.append(((NonAtomicTerm) translateObject(
//              val.getFunction())).stringApiValue());
//    } else if (val.getNumber() != null) {
//      sb.append((val.getNumber()));
//    } else if (val.getSentence() != null) {
//      sb.append(((FormulaSentence) translateObject(
//              val.getSentence())).stringApiValue());
//    } else if (val.getSymbol() != null) {
//      sb.append(((CycSymbol) translateObject(
//              val.getSymbol())).stringApiValue());
//    }
//  }
  
  
  
  
    
  
  // FIXME: remove this - nwinant, 2017-03-14
  // The code below contains an egregious hack to allow list-based inference parameters like :result-sort-order.
  // The basic problem is that the parameter values are passed around in CycML, and the CycML DTD/XSD has no support for SubL lists (though 
  // there's a hack in the Cyc-side CycML generator to allow them to be produced).  Thus, 
  // this hack was added to temporarily turns lists into cyc sentences, and then once they're parsed into appropriate java structures,
  // the extra sentences are cleaned up.
  private static final CycObject MUNGING_TEMP_PRED = new CycConstantImpl("JustATemporaryPredicate", new GuidImpl("704e2e28-0aba-4a8d-8434-9b07d71191e6"));
  
  private static final boolean MUNGE_LISTS_IF_NECESSARY = true;
  private static final boolean SKIP_MALFORMED_INFERENCE_PARAMETERS_AND_THEIR_VALUES = false;
  protected boolean hasMungedLists = false;
  
  private void warnOrThrowException(boolean shouldWarn, String errorMsg, String warnConsequenceMsg) {
    if (shouldWarn) {
      LOGGER.error(errorMsg + warnConsequenceMsg);
    } else {
      throw new QueryRuntimeException(errorMsg);
    }
  }
  
  private InferenceParameters convertParams (
          QueryInferenceProperties queryInferenceProperties)  throws SessionException {
    final StringBuilder sb = new StringBuilder();
    for (final QueryInferenceProperty qip : queryInferenceProperties.getQueryInferenceProperty()) {
      final String symbolName = ":" + qip.getPropertySymbol().getContent().trim();
      final String valueString = parsePropertyValue(qip.getPropertyValue());
      
      if (symbolName.equals(":")) {
        warnOrThrowException(SKIP_MALFORMED_INFERENCE_PARAMETERS_AND_THEIR_VALUES,
                "Inference property name is empty",
                "; skipping parameter because SKIP_MALFORMED_INFERENCE_PARAMETERS=" 
                        + SKIP_MALFORMED_INFERENCE_PARAMETERS_AND_THEIR_VALUES + ".");
      } else if ((valueString == null) || valueString.trim().isEmpty()) {
        warnOrThrowException(SKIP_MALFORMED_INFERENCE_PARAMETERS_AND_THEIR_VALUES,
                "Could not parse value for inference property \"" + symbolName + "\"",
                "; skipping parameter because SKIP_MALFORMED_INFERENCE_PARAMETERS="
                        + SKIP_MALFORMED_INFERENCE_PARAMETERS_AND_THEIR_VALUES + ".");
      } else {
        if (sb.length() > 0) {
          sb.append(" ");
        }
        sb.append(symbolName).append(" ").append(valueString);
      }
    }
    final String paramString = sb.toString();
    LOGGER.debug("InferenceParameters string: \"{}\"", paramString);
    return new DefaultInferenceParameters(CycAccessManager.getCurrentAccess(), paramString);
  }
  
  private String parsePropertyValue(final PropertyValue val) {    
    if (val.getConstant() != null) {
      return ((CycConstant) translateObject(
              val.getConstant())).stringApiValue();
    } else if (val.getFunction() != null) {
      return ((NonAtomicTerm) translateObject(
              val.getFunction())).stringApiValue();
    } else if (val.getNumber() != null) {
      return val.getNumber();
    } else if (val.getSentence() != null) {
      if (hasMungedLists) {
        final FormulaSentence sentence = ((FormulaSentence) translateObject(val.getSentence()));
        if (MUNGING_TEMP_PRED.equals(sentence.getArg0())) {
          final CycList args = sentence.getArgs();
          args.remove(0);
          return args.cyclify();
        }
        return sentence.stringApiValue();
      } else {
        return ((FormulaSentence) translateObject(
                val.getSentence())).stringApiValue();
      }
    } else if (val.getSymbol() != null) {
      return ((CycSymbol) translateObject(
              val.getSymbol())).stringApiValue();
    } else {
      return null;
    }
  }
  
  private int count(String str, String findStr) {
    int lastIndex = 0;
    int count = 0;
    while (lastIndex != -1) {
      lastIndex = str.indexOf(findStr, lastIndex);
      if (lastIndex != -1) {
        count++;
        lastIndex += findStr.length();
      }
    }
    return count;
  }
  
  private String processXml(String rawXml) {
    if (!MUNGE_LISTS_IF_NECESSARY) {
      return rawXml;
    }
    final String LIST_BEGIN_TAG = "<list xmlns=\"http://www.opencyc.org/xml/cycML/\">";
    final String LIST_CLOSE_TAG = "</list>";
    hasMungedLists = rawXml.contains(LIST_BEGIN_TAG);
    final String result;
    if (hasMungedLists) {
      final String SENT_BEGIN_TAG = "<sentence xmlns=\"http://www.opencyc.org/xml/cycML/\">";
      final String SENT_CLOSE_TAG = "</sentence>";
      final String TEMP_PRED_NAME = "JustATemporaryPredicate";
      LOGGER.info("MUNGED XML! Working around...");
      final String intermediateXml = rawXml;
      result = intermediateXml
              .replaceFirst(
                      LIST_BEGIN_TAG,
                      SENT_BEGIN_TAG + "<and>"
                      + "<constant>\n"
                      + "           <guid>704e2e28-0aba-4a8d-8434-9b07d71191e6</guid>\n"
                      + "           <name>" + TEMP_PRED_NAME + "</name>\n"
                      + "          </constant>")
              .replaceFirst(
                      LIST_CLOSE_TAG,
                      "</and>" + SENT_CLOSE_TAG)
              .replaceAll(
                      "<item>", "")
              .replaceAll(
                      "</item>", "");
      if (LOGGER.isDebugEnabled()) {
        System.out.println("[[--XML was processed to convert <list> occurrences  to <sentence>--]]");
        LOGGER.debug(
                "Processed Query XML:\n"
                + "---->>>>>> [START *PROCESSED* Query XML] ------------------------>>>>>>\n"
                + "{}\n"
                + "<<<<<<---- [END   *PROCESSED* Query XML] <<<<<<------------------------",
                result);
        LOGGER.debug("{} instances of {}", count(result, LIST_BEGIN_TAG), LIST_BEGIN_TAG);
        LOGGER.debug("{} instances of {}", count(result, LIST_CLOSE_TAG), LIST_CLOSE_TAG);
        LOGGER.debug("{} instances of {}", count(result, TEMP_PRED_NAME), TEMP_PRED_NAME);
        LOGGER.debug("{} instances of {}", count(result, SENT_BEGIN_TAG), SENT_BEGIN_TAG);
        LOGGER.debug("{} instances of {}", count(result, SENT_CLOSE_TAG), SENT_CLOSE_TAG);
      }
    } else {
      result = rawXml;
    }
    return result;
  }
  
}
