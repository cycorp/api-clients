package com.cyc.xml.query;

/*
 * #%L
 * File: ProofViewJaxbMarshaller.java
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
import com.cyc.baseclient.xml.CycJaxbMarshaller;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.bind.JAXBException;
import org.w3c.dom.Node;

import static com.cyc.baseclient.xml.cycml.Constants.CYCML_NAMESPACE;
import static com.cyc.xml.query.Constants.PROOFVIEW_NAMESPACE;

/**
 * A class to output ProofViews in XML.
 *
 * @author baxter
 */
public class ProofViewJaxbMarshaller extends CycJaxbMarshaller<ProofView> {

  /**
   * Construct a new ProofViewMarshaller.
   *
   * @throws JAXBException
   */
  public ProofViewJaxbMarshaller() throws JAXBException {
    super(ProofView.class);
    try {
      setXslUri(new URI("http://localhost/tmp/ProofView.xsl"));
    } catch (URISyntaxException ex) {
    }
    setDefaultNamespace(PROOFVIEW_NAMESPACE);
    setPreferredPrefix(CYCML_NAMESPACE, "cycml");
  }

  /**
   * Output the specified entry to the specified DOM Node.
   *
   * @param entry
   * @param destination
   * @throws JAXBException
   * @throws java.io.IOException
   * @see javax.xml.bind.Marshaller#marshal(java.lang.Object, org.w3c.dom.Node)
   */
  public void marshal(final ProofViewEntry entry, final Node destination) throws JAXBException, IOException {
    marshaller.marshal(entry, destination);
  }

}
