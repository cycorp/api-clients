package com.cyc.session.ui;

/*
 * #%L
 * File: CycServerPanel.java
 * Project: Session Client
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

import com.cyc.session.CycAddress;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * A GUI panel which prompts the user for a Cyc hostname & base port.
 * 
 * @author nwinant
 */
public class CycServerPanel extends JPanel {

  public CycServerPanel(CycAddress server) {
    hostField = new JTextField(server.getHostName(), 20);
    portField = new JComboBox(getBasePorts());
    if (server.getBasePort() != null) {
      portField.setSelectedItem(server.getBasePort());
    }
    add(new JLabel("Cyc host and base port:"));
    add(hostField);
    add(portField);
    
    addComponentListener(new ComponentAdapter() {
      @Override
      public void componentShown(ComponentEvent ce) {
        hostField.requestFocusInWindow();
      }
    });
  }
  
  public CycServerPanel(String defaultHost, Integer defaultPort) {
    this(CycAddress.get(defaultHost, defaultPort));
  }
  
  public CycServerPanel() {
    this(DEFAULT_SERVER);
  }
  
  
  // Public
  
  public CycAddress getCycAddress() {
    return CycAddress.get(getHostName(), getBasePort());
  }
  
  
  // Protected
  
  final protected Integer[] getBasePorts() {
    return new Integer[]{3600, 3620, 3640, 3660, 3680};
  }
  
  protected String getHostName() {
    return hostField.getText();
  }
  
  protected Integer getBasePort() {
    if (portField.getSelectedItem() != null) {
      return Integer.parseInt(portField.getSelectedItem().toString());
    }
    return null;
  }
  
  
  // Internal  
  
  public static final String TITLE = "Set Cyc Connection";
  
  private final JTextField hostField;
  private final JComboBox portField;
  
  /**
   * This is the default location of a Cyc server, but you should be very wary of assuming it.
   */
  private static final CycAddress DEFAULT_SERVER = CycAddress.get("localhost", 3600);
  
  
  // Static
  
  /**
   * Presents a CycDialogPanel to the user via a CycDialogPane, and
 returns the user's input wrapped in a CycAddress object.
   * 
   * @param server
   * @return Returns a CycAddress object wrapping the user's input, or null if they cancel.
   */
  private static CycAddress presentCycServerPanel(CycServerPanel panel) {
    final Object[] options = {"OK", "Cancel"};
    final CycDialogPane pane = CycDialogPane.create(panel, TITLE, options);
    pane.getDialog().setAlwaysOnTop(true);
    pane.prompt();
    final int result = pane.getSelectedValue();
    if (result == JOptionPane.OK_OPTION) {
      return panel.getCycAddress();
    }
    return null;
  }

  /**
   * Creates a CycConnectionPanel, presents it to the user via a JOptionPane, and
   * returns the user's input wrapped in a CycAddress object.
   * 
   * @param server
   * @return Returns a CycAddress object wrapping the user's input, or null if they cancel.
   */
  public static CycAddress promptUser(CycAddress server) {
    return presentCycServerPanel(new CycServerPanel(server));
  }
  
  /**
   * Creates a CycConnectionPanel, presents it to the user via a JOptionPane, and
   * returns the user's input wrapped in a CycAddress object.
   * 
   * @param defaultHost 
   * @param defaultPort 
   * @return Returns a CycAddress object wrapping the user's input, or null if they cancel.
   */
  public static CycAddress promptUser(String defaultHost, Integer defaultPort) {
    return presentCycServerPanel(new CycServerPanel(defaultHost, defaultPort));
  }
  
  /**
   * Creates a CycConnectionPanel, presents it to the user via a JOptionPane, and
   * returns the user's input wrapped in a CycAddress object.
   * 
   * @return Returns a CycAddress object wrapping the user's input, or null if they cancel.
   */
  public static CycAddress promptUser() {
    return presentCycServerPanel(new CycServerPanel());
  }

  
  // Main
  
  public static void main(String[] args) {
    try {
      System.out.println("Here we go...");
      CycAddress server = CycServerPanel.promptUser();
      System.out.println("Server: " + server);
    } catch (Exception ex) {
      ex.printStackTrace(System.err);
      System.exit(1);
    } finally {
      System.exit(0);
    }
  }
}
