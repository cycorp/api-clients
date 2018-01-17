package com.cyc.xml.query;

/*
 * #%L
 * File: ProofViewMarshallerTest.java
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

import com.cyc.base.exception.CycApiException;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.base.exception.CycTimeOutException;
import com.cyc.kb.exception.CreateException;
import com.cyc.kb.exception.KbException;
import com.cyc.query.client.explanations.ProofViewGeneratorImpl;
import com.cyc.query.exception.QueryConstructionException;
import com.cyc.session.exception.OpenCycUnsupportedFeatureException;
import com.cyc.session.exception.SessionException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import javax.xml.bind.JAXBException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.cyc.query.client.TestUtils.assumeNotOpenCyc;
import static com.cyc.xml.query.ProofViewTestConstants.currentAnswer;
import static com.cyc.xml.query.ProofViewTestConstants.setup;
import static com.cyc.xml.query.ProofViewTestConstants.teardown;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 *
 * @author baxter
 */
public class ProofViewMarshallerTest {

  public ProofViewMarshallerTest() {
  }

  @BeforeClass
  public static void setUpClass() throws IOException, UnknownHostException, JAXBException, 
          CycApiException, CycConnectionException, CycTimeOutException, QueryConstructionException, 
          SessionException, CreateException, KbException, OpenCycUnsupportedFeatureException {
    setup();
    assumeNotOpenCyc();
    proofViewJustification = new ProofViewGeneratorImpl(currentAnswer);
  }

  @AfterClass
  public static void tearDownClass() {
    teardown();
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }
  private static ProofViewGeneratorImpl proofViewJustification;

  /**
   * Test of marshal method, of class ProofViewMarshaller.
   */
  @Test
  public void testMarshal_ProofView_Writer() throws IOException, OpenCycUnsupportedFeatureException, JAXBException {
    System.out.println("\nmarshal to writer");
    assumeNotOpenCyc();
    final File destination = File.createTempFile("proofViewTest", ".xml");
    System.out.println("Marshaling proof view to " + destination);
    final ProofView proofView = proofViewJustification.getProofViewJaxb();
    assertNotNull("Failed to get proof view.", proofView);
    new ProofViewJaxbMarshaller().marshal(proofView, new FileWriter(destination));
  }
  
  /**
   * Test of marshal method, of class ProofViewMarshaller.
   */
  @Test
  public void testMarshal_ProofView_OutputStream() throws OpenCycUnsupportedFeatureException, JAXBException, IOException {
    System.out.println("\nmarshal to stream");
    assumeNotOpenCyc();
    final ProofView proofView = proofViewJustification.getProofViewJaxb();
    assertNotNull("Failed to get proof view.", proofView);
    new ProofViewJaxbMarshaller().marshal(proofView, System.out);
  }

  /**
   * Test round-trip marshalling and unmarshalling.
   */
  @Test
  public void testMarshalUnmarshalRoundTrip() throws IOException, OpenCycUnsupportedFeatureException, JAXBException {
    System.out.println("\nTest marshal-unmarshal round trip.");
    assumeNotOpenCyc();
    final File file1 = File.createTempFile("proofViewTest", ".xml");
    final ProofView proofView = proofViewJustification.getProofViewJaxb();
    final ProofViewJaxbMarshaller marshaller = new ProofViewJaxbMarshaller();
    marshaller.marshal(proofView, new FileWriter(file1));
    final ProofView proofView2 = new ProofViewJaxbUnmarshaller().unmarshalProofview(new FileInputStream(
            file1));
    final File file2 = File.createTempFile("proofViewTest", ".xml");
    marshaller.marshal(proofView2, new FileWriter(file2));
    System.out.println("Marshalled proof views to " + file1 + "\n and " + file2);
    assertEquals("Proof view did not survive marshalling/unmarshalling.",
            readFile(file1), readFile(file2));
  }

  private static String readFile(File file) throws IOException {
    FileInputStream stream = new FileInputStream(file);
    try {
      FileChannel fc = stream.getChannel();
      MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
      /* Instead of using default, pass in a decoder. */
      return Charset.defaultCharset().decode(bb).toString();
    } finally {
      stream.close();
    }
  }
}
