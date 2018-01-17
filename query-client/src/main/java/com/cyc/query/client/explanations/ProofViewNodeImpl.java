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
package com.cyc.query.client.explanations;

/*
 * #%L
 * File: ProofViewNodeImpl.java
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
import static com.cyc.baseclient.connection.SublApiHelper.makeSublStmt;
import com.cyc.query.ProofViewNode;
import com.cyc.query.client.graph.AbstractGraphNodePathImpl;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of Node backed by a proof-view entry.
 *
 * @see org.opencyc.xml.proofview.ProofViewEntry.
 */
public class ProofViewNodeImpl implements ProofViewNode {

  // Inner classes
  
  public static class ProofViewNodePathImpl 
          extends AbstractGraphNodePathImpl<Integer, ProofViewNodePath>
          implements ProofViewNodePath {
    public ProofViewNodePathImpl(ProofViewNode node) {
      super((node.getParent() != null)
                      ? node.getParent().getEntryPath()
                      : null,
              node.getEntryId());
    }
  }
  
  
  // Fields
  
  private final ProofViewNode parent;
  private final ProofViewGeneratorImpl proofViewJustification;
  private final int entryId;
  private final ProofViewNodePath path;
  private final List<ProofViewNodeImpl> children = new ArrayList<>();
  private com.cyc.xml.query.ProofViewEntry entryJaxb;

  
  // Construction
  
  ProofViewNodeImpl(ProofViewNode parent, com.cyc.xml.query.ProofViewEntry entryJaxb, ProofViewGeneratorImpl proofView) {
    this.parent = parent;
    this.entryId = entryJaxb.getId().intValue();
    this.path = new ProofViewNodePathImpl(this);
    this.entryJaxb = entryJaxb;
    this.proofViewJustification = proofView;
    populateChildren();
  }

  
  // Public
  
  @Override
  public int getEntryId() {
    return this.entryId;
  }

  @Override
  public ProofViewNodePath getEntryPath() {
    return this.path;
  }
  
  @Override
  public List<? extends ProofViewNode> getChildren() {
    return Collections.unmodifiableList(children);
  }

  @Override
  public String getCyclString() {
    maybeFetchDetails();
    return entryJaxb.getCycl();
  }

  /**
   * Get the label for this node.
   *
   * @return the label, or null if none.
   */
  @Override
  public String getLabel() {
    return entryJaxb.getLabel();
  }

  @Override
  public String getHTML() {
    maybeFetchDetails();
    final com.cyc.baseclient.xml.cycml.Paraphrase paraphraseJaxb = entryJaxb.getParaphrase();
    final StringBuilder sb = new StringBuilder();
    if (paraphraseJaxb != null) {
      for (Object obj : paraphraseJaxb.getContent()) {
        if (obj instanceof String) {
          sb.append(obj);
        }
      }
    }
    return sb.toString();
  }

  @Override
  public boolean isExpandInitially() {
    return entryJaxb.isExpandInitially();
  }

  public void marshal(org.w3c.dom.Node destination) {
    try {
      marshal(destination, new com.cyc.xml.query.ProofViewMarshaller());
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Marshal this justification to the specified DOM node using the specified marshaller.
   *
   * @param destination
   * @param marshaller
   */
  public void marshal(org.w3c.dom.Node destination,
          final com.cyc.xml.query.ProofViewMarshaller marshaller) {
    try {
      marshaller.marshal(entryJaxb, destination);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  private synchronized void maybeFetchDetails() {
    if (needToFetchDetails()) {
      try {
        final String xml = proofViewJustification.getCyc().converse().converseString(proofViewJustification.requireNamespace(makeSublStmt("proof-view-entry-xml", proofViewJustification.getProofViewJaxb().getId(), entryId)));
        this.entryJaxb = proofViewJustification.getProofViewJaxbUnmarshaller().unmarshalEntry(new ByteArrayInputStream(
                xml.getBytes()));
        populateChildren();
      } catch (Exception e) {
        throw new RuntimeException(
                "Failed to fetch proof-view details for " + proofViewJustification.getProofViewJaxb().getId() + " " + entryId,
                e);
      }
    }
  }

  private boolean needToFetchDetails() {
    return entryJaxb.getParaphrase() == null;
  }

  private void populateChildren() {
    children.clear();
    final com.cyc.xml.query.SubEntries subEntriesJaxb = entryJaxb.getSubEntries();
    if (subEntriesJaxb != null) {
      for (final com.cyc.xml.query.ProofViewEntry subEntryJaxb : subEntriesJaxb.getProofViewEntry()) {
        final ProofViewNodeImpl child = new ProofViewNodeImpl(this, subEntryJaxb, proofViewJustification);
        children.add(child);
        //child.parent = this;
      }
    }
  }

  @Override
  public int getDepth() {
    if (getParent() == null) {
      return 0;
    } else {
      return getParent().getDepth() + 1;
    }
  }

  /**
   * Get the parent node of this node.
   *
   * @return the parent node, or null if it has no parent.
   */
  @Override
  public ProofViewNode getParent() {
    return parent;
  }
  
}
