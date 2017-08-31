package com.cyc.base.justification;

/*
 * #%L
 * File: Justification.java
 * Project: Base Client
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


import com.cyc.base.inference.InferenceAnswer;
import com.cyc.session.exception.OpenCycUnsupportedFeatureException;
import org.w3c.dom.Node;
import com.cyc.query.ProofViewNode;

/**
 * An interface for representing and generating justifications of Cyc inference
 * answers.
 * <p/>
 * The structure of a <code>Justification</code> is a tree of {@link Node}s,
 * with the content varying widely for different types of
 * <code>Justification</code>. In general they are intended to be rendered in an
 * interactive display, with CycL and/or NL (encoded in HTML) for nodes and
 * their children displayed or hidden according to user actions or preferences.
 *
 *
 * @author baxter
 */
public interface Justification {

  /**
   * Get the root of the tree structure of this justification. A suggested
   * rendering algorithm would display this node, and recurse on its child nodes
   * iff it is to be expanded initially.
   *
   * @throws com.cyc.session.exception.OpenCycUnsupportedFeatureException when run against an OpenCyc server.
   * @see com.cyc.base.justification.Justification.Node#isExpandInitially()
   * @return the root node
   */
  ProofViewNode getRoot() throws OpenCycUnsupportedFeatureException;

  /**
   * Flesh out this justification, setting its root node and tree structure
   * underneath the root.
   * 
   * @throws com.cyc.session.exception.OpenCycUnsupportedFeatureException when run against an OpenCyc server.
   */
  void populate() throws OpenCycUnsupportedFeatureException;

  /**
   * Returns the inference answer justified by this object
   *
   * @return the inference answer
   */
  InferenceAnswer getAnswer();

  /**
   * Marshal this justification into a DOM tree.
   *
   * @param destination
   */
  void marshal(org.w3c.dom.Node destination);

}
