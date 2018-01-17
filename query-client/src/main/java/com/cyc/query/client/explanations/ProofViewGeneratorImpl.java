package com.cyc.query.client.explanations;

/*
 * #%L
 * File: ProofViewGeneratorImpl.java
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
import com.cyc.base.CycAccess;
import com.cyc.base.CycAccessSession;
import com.cyc.base.cycobject.CycObject;
import com.cyc.base.cycobject.CycSymbol;
import com.cyc.base.cycobject.DenotationalTerm;
import com.cyc.base.exception.CycApiException;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.baseclient.CycObjectFactory;
import com.cyc.baseclient.connection.SublApiHelper;
import com.cyc.kb.Context;
import com.cyc.kb.exception.CreateException;
import com.cyc.kb.exception.KbTypeException;
import com.cyc.query.InferenceAnswerIdentifier;
import com.cyc.query.InferenceIdentifier;
import com.cyc.query.ProofView;
import com.cyc.query.ProofViewGenerator;
import com.cyc.query.ProofViewMarshaller;
import com.cyc.query.ProofViewSpecification;
import com.cyc.query.QueryAnswer;
import com.cyc.query.QueryAnswerExplanationGenerator;
import com.cyc.query.exception.ProofViewException;
import com.cyc.query.exception.QueryRuntimeException;
import com.cyc.session.compatibility.CycSessionRequirementList;
import com.cyc.session.compatibility.NotOpenCycRequirement;
import com.cyc.session.exception.OpenCycUnsupportedFeatureException;
import com.cyc.session.exception.SessionCommunicationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import javax.xml.bind.JAXBException;

import static com.cyc.baseclient.CycObjectFactory.makeCycSymbol;
import static com.cyc.baseclient.connection.SublApiHelper.makeNestedSublStmt;
import static com.cyc.baseclient.connection.SublApiHelper.makeSublStmt;

/**
 * A {@link QueryAnswerExplanationGenerator} backed by a {@link ProofView}.
 *
 * Generally, a new ProofViewGenerator object is constructed, parameters are set as desired, then
 * {@link #generate()} is called to flesh it out. Then you can get its root note (via
 * {@link #getExplanation()}), and display it as an interactive tree structure.
 *
 * @author baxter
 */
public class ProofViewGeneratorImpl implements ProofViewGenerator {

  private final ProofViewSpecification spec;
  private final CycAccess cyc;
  private QueryAnswer answer;
  private final int proofViewId;
  private boolean isPopulated = false;

  final private Object lock = new Object();
  private SummaryAlgorithm summaryAlgorithm = SummaryAlgorithm.DEFAULT;
  private DenotationalTerm addressee = null;
  private com.cyc.xml.query.ProofView proofViewJaxb = null;
  private final com.cyc.xml.query.ProofViewJaxbUnmarshaller proofViewJaxbUnmarshaller;
  private com.cyc.query.ProofView root = null;
  //"get-new-empty-proof-view-id"

  public static final CycSessionRequirementList<OpenCycUnsupportedFeatureException> PROOF_VIEW_JUSTIFICATION_REQUIREMENTS = CycSessionRequirementList.fromList(
          NotOpenCycRequirement.NOT_OPENCYC
  );

  /**
   * Create a new, unpopulated justification for the specified answer.
   *
   * @param answer
   * @param spec
   * @throws CycConnectionException if there is a problem talking to Cyc.
   * @throws OpenCycUnsupportedFeatureException when run against an OpenCyc server.
   * @see ProofViewJustification#populate()
   */
  public ProofViewGeneratorImpl(QueryAnswer answer, ProofViewSpecification spec) throws CycConnectionException, OpenCycUnsupportedFeatureException {
    PROOF_VIEW_JUSTIFICATION_REQUIREMENTS.throwRuntimeExceptionIfIncompatible();
    this.answer = answer;
    this.spec = spec;
    final InferenceAnswerIdentifier answerID = answer.getId();
    final InferenceIdentifier inferenceID = answerID.getInferenceIdentifier();
    try {
      this.cyc = ((CycAccessSession) (inferenceID.getSession())).getAccess();
      this.proofViewId = cyc.converse().converseInt(makeSublStmt(
              "get-new-empty-proof-view-id",
              inferenceID.getProblemStoreId(), inferenceID.getInferenceId(),
              answerID.getAnswerId()));
      this.proofViewJaxbUnmarshaller = new com.cyc.xml.query.ProofViewJaxbUnmarshaller();
    } catch (JAXBException | CycApiException | CycConnectionException ex) {
      throw QueryRuntimeException.fromThrowable(ex);
    }
  }


  public ProofViewGeneratorImpl(QueryAnswer answer) throws CycConnectionException, OpenCycUnsupportedFeatureException {
    this(answer, new ProofViewSpecificationImpl());
  }
  
  @Deprecated
  @Override
  public QueryAnswer getAnswer() {
    throw new UnsupportedOperationException("Use getQueryAnswer() instead.");
  }

  public QueryAnswer getQueryAnswer() {
    return answer;
  }

  /**
   * Flesh out this justification, setting its root node and tree structure underneath the root.
   *
   * @throws com.cyc.session.exception.OpenCycUnsupportedFeatureException when run against an
   * OpenCyc server.
   */
  @Override
  public void generate() throws OpenCycUnsupportedFeatureException {
    synchronized (lock) {
      PROOF_VIEW_JUSTIFICATION_REQUIREMENTS.throwRuntimeExceptionIfIncompatible();
      requireNotPopulated();
      try {
        initializeProofViewProperties();
        converseVoid(makeSublStmt("proof-view-id-populate", proofViewId));
      } catch (CycApiException | CycConnectionException e) {
        throw QueryRuntimeException.fromThrowable("Failed to populate proof view.", e);
      }
      isPopulated = true;
    }
  }

  public void initializeProofViewProperties() {
    if (spec == null) {
      return;
    }
    try {
      ArrayList<SublApiHelper.AsIsTerm> propertySetters = new ArrayList<>();
      if (spec.isIncludeDetails() instanceof Boolean) {
        propertySetters.add(makeNestedSublStmt("set-proof-view-include-details",
                proofViewId,
                spec.isIncludeDetails()));
      }
      if (spec.isIncludeLinear() instanceof Boolean) {
        propertySetters.add(makeNestedSublStmt("set-proof-view-include-linear", proofViewId,
                spec.isIncludeLinear()));
      }
      if (spec.isIncludeSummary() instanceof Boolean) {
        propertySetters.add(makeNestedSublStmt("set-proof-view-include-summary", proofViewId, spec.isIncludeSummary()));
      }
      if (spec.getDomainContext() instanceof Context) {
        propertySetters.add(makeNestedSublStmt("set-proof-view-domain-mt", proofViewId, spec.getDomainContext().getCore()));
      }
      if (spec.getLanguageContext() instanceof Context) {
        propertySetters.add(makeNestedSublStmt("set-proof-view-language-mt", proofViewId, spec.getLanguageContext().getCore()));
      }
      if (spec.isIncludeAssertionBookkeeping() instanceof Boolean) {
        propertySetters.add(makeNestedSublStmt("set-proof-view-suppress-assertion-bookkeeping", proofViewId,!spec.isIncludeAssertionBookkeeping()));
      }
      if (spec.isIncludeAssertionCyclists() instanceof Boolean) {
        propertySetters.add(makeNestedSublStmt("set-proof-view-suppress-assertion-cyclists", proofViewId,!spec.isIncludeAssertionCyclists()));
      }
      String command = makeSublStmt("progn", propertySetters.toArray());
      getCyc().converse().converseVoid(command);
      
    } catch (CycConnectionException ex) {
      throw QueryRuntimeException.fromThrowable(ex);
    }
  }
  
  @Override
  public String toString() {
    return "Proof View for " + answer;
  }

  /**
   * Get the microtheory from which semantic checks are performed to construct this justification.
   *
   * @return the domain microtheory
   * @throws com.cyc.session.exception.SessionCommunicationException
   * @see #setDomainMt(ELMt)
   */
  public Context getDomainContext() throws SessionCommunicationException {
    if (spec.getDomainContext() == null) {
      try {
        final CycObject mtObject = cyc.converse().converseCycObject(makeSublStmt(
                "get-proof-view-domain-mt", proofViewId));
        return Context.get(mtObject.toString());
      } catch (CycConnectionException ex) {
        throw SessionCommunicationException.fromThrowable(ex);
      } catch (CreateException | KbTypeException ex) {
        throw QueryRuntimeException.fromThrowable(ex);
      }
    }
    return spec.getDomainContext();
  }

  /**
   * Should we include a detailed, drill-down section in this justification?
   *
   * @return true iff such a tree is or should be included
   * @throws com.cyc.session.exception.SessionCommunicationException
   * @see #setIncludeDetails(boolean)
   */
  public boolean isIncludeDetails() throws SessionCommunicationException {
    if (spec.isIncludeDetails() == null) {
      try {
        return cyc.converse().converseBoolean(makeSublStmt(
                "get-proof-view-include-details", proofViewId));
      } catch (CycConnectionException ex) {
        throw SessionCommunicationException.fromThrowable(ex);
      }
    } else {
      return spec.isIncludeDetails();
    }
  }

  /**
   * Should we include a linear, syllogistic section in this justification?
   *
   * @return true iff such a section is or should be included
   * @throws com.cyc.session.exception.SessionCommunicationException
   * @see #setIncludeLinear(boolean)
   */
  public boolean isIncludeLinear() throws SessionCommunicationException {
    if (spec.isIncludeLinear() == null) {
      try {
        return cyc.converse().converseBoolean(makeSublStmt(
                "get-proof-view-include-linear", proofViewId));
      } catch (CycConnectionException ex) {
        throw SessionCommunicationException.fromThrowable(ex);
      }
    } else {
      return spec.isIncludeLinear();
    }
  }

  /**
   * Should we include a short, executive-summary section in this justification?
   *
   * @return true iff such a section is or should be included
   * @throws com.cyc.session.exception.SessionCommunicationException
   * @see #setIncludeSummary(boolean)
   */

  public boolean isIncludeSummary() throws SessionCommunicationException {
    if (spec.isIncludeSummary() == null) {
      try {
        final String command = makeSublStmt(
                "get-proof-view-include-summary", proofViewId);
        return cyc.converse().converseBoolean(command);
      } catch (CycConnectionException ex) {
        throw SessionCommunicationException.fromThrowable(ex);
      }
    } else {
      return spec.isIncludeSummary();
    }
  }

  /**
   * Returns the microtheory used for natural-language generation in this justification
   *
   * @return the language microtheory
   * @throws com.cyc.session.exception.SessionCommunicationException
   * @throws com.cyc.kb.exception.KbTypeException
   * @throws com.cyc.kb.exception.CreateException
   * @see #setLanguageMt(com.cyc.base.cycobject.ELMt)
   */
  public Context getLanguageContext() throws SessionCommunicationException, KbTypeException, CreateException {
    if (spec.getLanguageContext() == null) {
      try {
        final CycObject mtObject = cyc.converse().converseCycObject(makeSublStmt(
                "get-proof-view-language-mt", proofViewId));
        return Context.get(mtObject.toString());
      } catch (CycConnectionException ex) {
        throw SessionCommunicationException.fromThrowable(ex);
      }
    }
    return spec.getLanguageContext();
  }

  CycAccess getCyc() {
    return cyc;
  }

  com.cyc.xml.query.ProofViewJaxbUnmarshaller getProofViewJaxbUnmarshaller() {
    return proofViewJaxbUnmarshaller;
  }

  /**
   * Get the JAXB-generated proof view object backing this justification.
   *
   * @return the proof view.
   * @throws com.cyc.session.exception.OpenCycUnsupportedFeatureException when run against an
   * OpenCyc server.
   */
  public com.cyc.xml.query.ProofView getProofViewJaxb() throws OpenCycUnsupportedFeatureException {
    ensureProofViewInitialized();
    return proofViewJaxb;
  }

  /**
   * Get the root of the tree structure of this justification. A suggested rendering algorithm would
   * display this node, and recurse on its child nodes iff it is to be expanded initially.
   *
   * @see com.cyc.base.justification.Justification.Node#isExpandInitially()
   * @return the root node
   */
  @Override
  public com.cyc.query.ProofView getExplanation() throws OpenCycUnsupportedFeatureException {
    ensureProofViewInitialized();
    return root;
  }

  String requireNamespace(String command) {
    return SublApiHelper.wrapVariableBinding(
            command, makeCycSymbol("*proof-view-include-namespace?*"),
            makeCycSymbol("T"));
  }

  private void setRoot(com.cyc.query.ProofView root) {
    this.root = root;
  }

  /**
   * Should bookkeeping data for assertions be included? . Bookkeeping data includes who made the
 getAssertionService and when, etc.
   *
   * @return true iff it should
   * @throws com.cyc.session.exception.SessionCommunicationException
   * @see #setSuppressAssertionBookkeeping(boolean)
   */
  public boolean isIncludeAssertionBookkeeping() throws SessionCommunicationException {
    if (spec.isIncludeAssertionBookkeeping() != null) {
      return spec.isIncludeAssertionBookkeeping();
    }
    try {
      return ! cyc.converse().converseBoolean(makeSublStmt(
              "get-proof-view-suppress-assertion-bookkeeping", proofViewId));
    } catch (CycConnectionException ex) {
      throw SessionCommunicationException.fromThrowable(ex);
    }
  }

  /**
   * Should the cyclist who made a given getAssertionService be cited?
   *
   * @return true iff the cyclist should be cited
   * @throws com.cyc.session.exception.SessionCommunicationException
   * @see #setSuppressAssertionCyclists(boolean)
   */
  public boolean isIncludeAssertionCyclists() throws SessionCommunicationException {
    if (spec.isIncludeAssertionCyclists() != null) {
      return spec.isIncludeAssertionCyclists();
    }
    try {
      return ! cyc.converse().converseBoolean(makeSublStmt(
              "get-proof-view-suppress-assertion-cyclists", proofViewId));
    } catch (CycConnectionException ex) {
      throw SessionCommunicationException.fromThrowable(ex);
    }
  }

  public SummaryAlgorithm getSummaryAlgorithm() {
    return summaryAlgorithm;
  }

  public void setSummaryAlgorithm(final SummaryAlgorithm algorithm) throws SessionCommunicationException {
    synchronized (lock) {
      try {
        requireNotPopulated();
        converseVoid(makeSublStmt("set-proof-view-summary-algorithm",
                proofViewId, algorithm.getCycName()));
        this.summaryAlgorithm = algorithm;
      } catch (CycConnectionException ex) {
        throw SessionCommunicationException.fromThrowable(ex);
      }
    }
  }

  public DenotationalTerm getAddressee() {
    return addressee;
  }

  public void setAddressee(final DenotationalTerm addressee) throws SessionCommunicationException {
    synchronized (lock) {
      try {
        requireNotPopulated();
        converseVoid(makeSublStmt("set-proof-view-addressee",
                proofViewId, addressee));
        this.addressee = addressee;
      } catch (CycConnectionException ex) {
        throw SessionCommunicationException.fromThrowable(ex);

      }
    }
  }

  public enum SummaryAlgorithm {

    DEFAULT(":default"), WHITELIST(":whitelist");
    private final CycSymbol cycName;

    private SummaryAlgorithm(final String cycName) {
      this.cycName = CycObjectFactory.makeCycSymbol(cycName);
    }

    private CycSymbol getCycName() {
      return cycName;
    }
  }

  @Override
  public void marshal(org.w3c.dom.Node destination) {
    try {
      marshal(destination, new com.cyc.xml.query.ProofViewJaxbMarshaller());
    } catch (JAXBException ex) {
      throw QueryRuntimeException.fromThrowable(ex);
    }
  }
  
  @Override
  public ProofViewMarshaller getMarshaller() throws ProofViewException {
    try {
      return new ProofViewMarshallerImpl(this);
    } catch (JAXBException ex) {
      throw ProofViewException.fromThrowable("Error attempting to return ProofViewMarshaller", ex);
    }
  }
  
  /**
   * Marshal this justification to the specified DOM node using the specified marshaller.
   *
   * @param destination
   * @param marshaller
   */
  public void marshal(org.w3c.dom.Node destination,
          final com.cyc.xml.query.ProofViewJaxbMarshaller marshaller) {
    try {
      marshaller.marshal(proofViewJaxb, destination);
    } catch (IOException | JAXBException ex) {
      throw QueryRuntimeException.fromThrowable(ex);
    }
  }

  private void requireNotPopulated() throws UnsupportedOperationException {
    if (isPopulated) {
      throw new UnsupportedOperationException(
              "Justification already populated.");
    }
  }

  private void converseVoid(final String command) throws CycConnectionException {
    try {
      cyc.converse().converseVoid(command);
    } catch (CycApiException e) {
      throw QueryRuntimeException.fromThrowable(e);
    }
  }
  
  private void ensureProofViewInitialized() throws RuntimeException, OpenCycUnsupportedFeatureException {
    PROOF_VIEW_JUSTIFICATION_REQUIREMENTS.throwRuntimeExceptionIfIncompatible();
    synchronized (lock) {
      if (!isPopulated) {
        generate();
      }
      try {
        final String xml = cyc.converse().converseString(requireNamespace(makeSublStmt(
                "proof-view-xml", proofViewId)));
        proofViewJaxb = (com.cyc.xml.query.ProofView) proofViewJaxbUnmarshaller.unmarshalProofview(new ByteArrayInputStream(
                xml.getBytes()));
        final com.cyc.xml.query.ProofViewEntry rootEntryJaxb = proofViewJaxb.getProofViewEntry();
        setRoot(new com.cyc.query.client.explanations.ProofViewImpl(rootEntryJaxb, this));
      } catch (CycApiException | CycConnectionException | JAXBException e) {
        e.printStackTrace(System.err);
        throw QueryRuntimeException.fromThrowable("Failed to get root of proof view.", e);
      }
    }
  }

}
