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
 * File: ProofViewMarshallerImpl.java
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

import com.cyc.query.ProofView;
import com.cyc.query.ProofViewMarshaller;
import com.cyc.query.exception.ProofViewException;
import com.cyc.query.exception.QueryRuntimeException;
import com.cyc.session.exception.OpenCycUnsupportedFeatureException;
import com.cyc.xml.query.ProofViewJaxbMarshaller;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import javax.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

/**
 * 
 * 
 * @author nwinant
 */
public class ProofViewMarshallerImpl implements ProofViewMarshaller {
  
  //====|    Fields    |==========================================================================//
  
  private static final Logger LOG = LoggerFactory.getLogger(ProofViewMarshallerImpl.class);
  
  private final ProofViewGeneratorImpl generator;
  private final ProofViewJaxbMarshaller jaxbMarshaller;
  
  //====|    Construction    |====================================================================//
  
  public ProofViewMarshallerImpl(ProofViewGeneratorImpl generator) throws JAXBException {
    this.generator = generator;
    this.jaxbMarshaller = new ProofViewJaxbMarshaller();
  }
  
  //====|    Public methods    |==================================================================//
  
  @Override
  public void marshal(Node destination) throws ProofViewException {
    try {
      jaxbMarshaller.marshal(generator.getProofViewJaxb(), destination);
    } catch (OpenCycUnsupportedFeatureException | IOException | JAXBException ex) {
      throw ProofViewException.fromThrowable("Error attempting to marshall proofview to Node", ex);
    }
  }

  @Override
  public void marshal(Writer destination) throws ProofViewException {
    try {
      jaxbMarshaller.marshal(generator.getProofViewJaxb(), destination);
    } catch (OpenCycUnsupportedFeatureException | IOException | JAXBException ex) {
      throw ProofViewException.fromThrowable("Error attempting to marshall proofview to Writer", ex);
    }
  }

  @Override
  public void marshal(OutputStream destination) throws ProofViewException {
    try {
      jaxbMarshaller.marshal(generator.getProofViewJaxb(), destination);
    } catch (OpenCycUnsupportedFeatureException | IOException | JAXBException ex) {
      throw ProofViewException.fromThrowable("Error attempting to marshall proofview to OutputStream", ex);
    }
  }

}
