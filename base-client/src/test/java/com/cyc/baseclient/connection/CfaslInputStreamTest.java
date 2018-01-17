package com.cyc.baseclient.connection;

/*
 * #%L
 * File: CfaslInputStreamTest.java
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * Tests CfaslInputStream and (to some extent) CfaslOutputStream.
 * 
 * @author daves
 */
public class CfaslInputStreamTest  {
  
  @Test
  public void testUnicodeCFASL() {
    System.out.println("\n**** testUnicodeCFASL ****");
    CFASLStringTest("abc", 15);
    CFASLStringTest("", 15);
    StringBuilder sb = new StringBuilder();
    sb.append("a");
    sb.append((char) 0x401);
    CFASLStringTest(sb.toString(), 53);
    System.out.println("**** testUnicodeCFASL OK ****");
  }

  private boolean CFASLStringTest(String str, int opcode) {
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream(4096);
      CfaslOutputStream cos = new CfaslOutputStream(baos);
      cos.writeObject(str);
      cos.flush();
      byte[] ba = baos.toByteArray();
      if (ba == null || ba.length == 0) {
        fail("Null Byte Array Return");
      }
      //System.out.println("BA test: "+ba.length);
      //for(int i=0;i<ba.length;i++)
      //  System.out.println("ba check "+i+" "+Integer.toHexString(0xff & (int)ba[i]));
      assertEquals((int) ba[0], opcode);  // make sure opcode is correct
      ByteArrayInputStream bais = new ByteArrayInputStream(ba);

      CfaslInputStream cis = new CfaslInputStream(bais);
      Object obj = cis.readObject();
      assertTrue(obj instanceof String);
      String result = (String) obj;
      assertTrue(result.equals(str));
    } catch (IOException e) {
      fail("IOException CFASLStringTest for: " + str);
    }
    return true;
  }
  
  /*
  // FIXME: delete! - nwinant, 2017-10-20
  @Test
  public void testSequenceVarParsing() throws Exception {
    final CycAccess cyc = CycAccessManager.getCurrentAccess();
    final String sent = "(queryAllowedRules SNCVA-FollowupQuery-OtherMetrics-ViperDB ?RULE)";
    final InferenceResultSet rs = cyc.getInferenceTool()
        .executeQuery(
                FormulaSentenceImpl.makeCycSentence(cyc, sent),
                cyc.getObjectTool().makeElMt("TestVocabularyMt"),
                new DefaultInferenceParameters(cyc, true));
    while (rs.next()) {
      for (String colName : rs.getColumnNames()) {
        //System.out.println("--------------------------------------------------------------");
        final CycAssertion asrt = (CycAssertion) rs.getObject(colName);
        //System.out.println(asrt.getFormula().toPrettyString("  ") + "\n");
        //System.out.println("wff err? " + asrt.getELFormula(cyc).getNonWffAssertExplanation(cyc));
      }
    }
  }
  */
  
}
