package com.cyc.baseclient;

/*
 * #%L
 * File: CycClient.java
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

//// External Imports
import com.cyc.Cyc;
import com.cyc.base.CycAccess;
import com.cyc.base.connection.CycConnection;
import com.cyc.base.connection.LeaseManager;
import com.cyc.base.connection.Worker;
import com.cyc.base.cycobject.CycList;
import com.cyc.base.cycobject.DenotationalTerm;
import com.cyc.base.cycobject.Fort;
import com.cyc.base.exception.BaseClientRuntimeException;
import com.cyc.base.exception.CycApiException;
import com.cyc.base.exception.CycConnectionException;
import com.cyc.base.kbtool.InspectorTool;
import com.cyc.base.kbtool.KeTextTool;
import com.cyc.baseclient.comm.Comm;
import com.cyc.baseclient.connection.CycConnectionImpl;
import com.cyc.baseclient.cycobject.CycArrayList;
import com.cyc.baseclient.cycobject.CycConstantImpl;
import com.cyc.baseclient.cycobject.GuidImpl;
import com.cyc.baseclient.exception.CycApiClosedConnectionException;
import com.cyc.baseclient.inference.params.DefaultInferenceParameterDescriptions;
import com.cyc.baseclient.kbtool.AssertToolImpl;
import com.cyc.baseclient.kbtool.ComparisonToolImpl;
import com.cyc.baseclient.kbtool.InferenceToolImpl;
import com.cyc.baseclient.kbtool.InspectorToolImpl;
import com.cyc.baseclient.kbtool.KbObjectToolImpl;
import com.cyc.baseclient.kbtool.KeTextToolImpl;
import com.cyc.baseclient.kbtool.LookupToolImpl;
import com.cyc.baseclient.kbtool.ObjectToolImpl;
import com.cyc.baseclient.kbtool.UnassertToolImpl;
import com.cyc.baseclient.subl.SublResourceLoader;
import com.cyc.baseclient.subl.SublSourceFile;
import com.cyc.baseclient.subl.functions.SublFunctions;
import com.cyc.baseclient.util.PasswordManager;
import com.cyc.session.CycAddress;
import com.cyc.session.CycSession;
import com.cyc.session.CycSessionConfiguration;
import com.cyc.session.exception.SessionCommunicationException;
import com.cyc.session.exception.SessionConfigurationException;
import com.cyc.session.exception.SessionException;
import com.cyc.session.exception.SessionInitializationException;
import com.cyc.session.exception.SessionRuntimeException;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.cyc.baseclient.connection.SublApiHelper.makeSublStmt;
import static java.util.stream.Collectors.toSet;

/**
 * Provides wrappers for the Base Client.
 *
 * <p>
 Collaborates with the <tt>CycConnection</tt> class which manages the API connections.
 </p>
 *
 * @version $Id: CycClient.java 176267 2017-12-13 04:02:46Z nwinant $
 * @author Stephen L. reed <p><p><p><p><p>
 */
public class CycClient implements CycAccess {
  
  //====|    CycAccessCache    |==================================================================//
  
  // CycAccessSupplierException
  
  public static class CycAccessSupplierException extends BaseClientRuntimeException {
    
    public static CycAccessSupplierException from(Throwable cause) {
      return (cause instanceof CycAccessSupplierException)
                     ? (CycAccessSupplierException) cause
                     : new CycAccessSupplierException(cause);
    }
    
    private CycAccessSupplierException(Throwable cause) {
      super(cause);
    }
    
    public void rethrow() throws CycConnectionException {
      final Throwable cause = getCause();
      if (cause instanceof CycConnectionException) {
        throw (CycConnectionException) cause;
      }
      throw CycApiException.from(cause);
    }
    
  }
  
  // CycAccessCache
  
  public static class CycAccessCache<K, V extends CycAccess> implements Closeable {
    
    // Fields
    
    private final Map<K, V> clients;
    private boolean closed;
    
    // Construction
    
    public CycAccessCache() {
      this.clients = new ConcurrentHashMap<>();
      this.closed = false;
    }
    
    // Internal helper methods
    
    private Optional<V> getClient(K key) {
      return Optional.ofNullable(clients.get(key));
    }
    
    private V reapIfClosed(K key) {
      return getClient(key)
              .filter(V::isClosed)
              .map(client -> clients.remove(key)).orElse(null);
    }
    
    // Public methods
    
    public synchronized V findOrCreate(K key, Supplier<V> factory)
            throws CycConnectionException, CycApiException {
      if (closed) {
        throw new BaseClientRuntimeException(this + " has been closed, and may no longer be used.");
      }
      reapIfClosed(key);
      if (clients.get(key) == null) {
        try {
          final long startMillis = System.currentTimeMillis();
          final V client = factory.get();
          LOGGER.info("Created new client for {} in {}ms: {}",
                  key, (System.currentTimeMillis() - startMillis), client);
          clients.put(key, client);
        } catch (RuntimeException ex) {
          CycAccessSupplierException.from(ex).rethrow(); // Delegates exception unpacking
        }
      }
      return clients.get(key);
    }
    
    public synchronized V close(K key) {
      return getClient(key).map(client -> {
        client.close();
        //if (!client.isClosed()) {
        //  LOGGER.info("Closing {}", client);
        //  client.close();
        //}
        return clients.remove(key);
      }).orElse(null);
    }
    
    public synchronized Set<V> closeAll() {
      return clients.keySet().stream()
              .map(key -> close(key))
              .filter(Objects::nonNull)
              .collect(toSet());
    }
    
    @Override
    public synchronized void close() throws IOException {
      closed = true;
      closeAll();
      clients.clear();
    }
    
  }
  
  //====|    CycClient Factory    |===============================================================//
  
  private static final CycAccessCache<Object, CycClient> FACTORY_CACHE = new CycAccessCache<>();
  
  private static CycAddress simplifyAddress(CycAddress addr) {
    return (addr != null) ? addr.toBaseAddress() : null;
  }
  
  /**
   * Returns a CycClient object for a CycConnection, creating a new CycClient if necessary.
   *
   * @param conn the Cyc connection object (in persistent, binary mode)
   * @return 
   * @throws CycConnectionException if cyc server host not found on the network or a data communication error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  public static CycClient get(CycConnection conn) throws CycConnectionException, CycApiException {
    return FACTORY_CACHE.findOrCreate(conn, () -> {
      try {
        return new CycClient(conn);
      } catch (CycConnectionException ex) {
        throw CycAccessSupplierException.from(ex);
      }
    });
  }
  
  /**
   * Returns a CycClient object for a Comm implementation, creating a new CycClient if necessary.
   * @param comm
   * @return 
   * @throws CycConnectionException
   * @throws CycApiException 
   */
  public static CycClient get(Comm comm) throws CycConnectionException, CycApiException {
    return FACTORY_CACHE.findOrCreate(comm, () -> {
      try {
        return new CycClient(comm);
      } catch (CycConnectionException ex) {
        throw CycAccessSupplierException.from(ex);
      }
    });
  }
  
  /**
   * Returns a CycClient object for a CycAddress, creating a new CycClient if necessary.
   * 
   * @param address The address of the server to connect to.
   * @return 
   * @throws CycConnectionException if cyc server host not found on the network or a data communication error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  public static CycClient get(CycAddress address)
            throws CycConnectionException, CycApiException {
    final CycAddress simplifiedAddr = simplifyAddress(address);
    return FACTORY_CACHE.findOrCreate(simplifiedAddr, () -> {
      try {
        return new CycClient(simplifiedAddr);
      } catch (CycConnectionException ex) {
        throw CycAccessSupplierException.from(ex);
      }
    });
  }
  
  /**
   * Returns a CycClient object for a CycSessionConfiguration, creating a new CycClient if 
   * necessary.
   *
   * @param config The CycSessionConfiguration to which the CycAccess should be tied.
   * @return 
   *
   * @throws CycConnectionException if cyc server host not found on the network or a data communication error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  public static CycClient get(CycSessionConfiguration config) 
          throws CycConnectionException, CycApiException {
    return get(config.getCycAddress());
  }
  
  /**
   * Returns a CycClient object for a CycSession, creating a new CycClient if necessary.
   *
   * @param session The CycSession to which the CycAccess should be tied.
   * @return 
   *
   * @throws CycConnectionException if cyc server host not found on the network or a data communication error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  public static CycClient get(CycSession session) throws CycConnectionException, CycApiException {
    return (session instanceof CycClientSession)
                   ? ((CycClientSession) session).getAccess()
                   : get(session.getConfiguration());
  }
  
  /**
   * Converts a CycAccess instance to a CycClient.
   *
   * @param access the CycAccess to convert
   * @return 
   */
  public static CycClient get(CycAccess access) {
    // CycClient is currently the only implementation of CycAccess, so it should be fine for us to
    // just cast it. If some other implementation of CycAccess *does* someday arise, then at least 
    // we'll get an error, rather than "helpful" magical behavior. - nwinant, 2017-10-11
    return (CycClient) access;
  }
  
  public static CycClient getCurrent() throws CycConnectionException, CycApiException {
    try {
      return get(CycSession.getCurrent());
    } catch (SessionConfigurationException
            | SessionCommunicationException 
            | SessionInitializationException ex) {
      if (CycConnectionException.isUnderlyingCause(ex)) {
        throw new CycConnectionException(ex);
      }
      throw CycApiException.from(ex);
    }
  }
  
  public static CycClient close(CycConnection conn) {
    return FACTORY_CACHE.close(conn);
  }
  
  public static CycClient close(Comm comm) {
    return FACTORY_CACHE.close(comm);
  }
  
  public static CycClient close(CycAddress server) {
    return FACTORY_CACHE.close(simplifyAddress(server));
  }
  
  public static Set<CycClient> closeAll() {
    return FACTORY_CACHE.closeAll();
  }
  
  //====|    Transactions    |====================================================================//
  
  @Deprecated
  private static ThreadLocal<KbTransaction> currentTransaction = new ThreadLocal<KbTransaction>() {
    @Override
    protected KbTransaction initialValue() {
      return null;
    }
  };
  
  @Deprecated
  protected static KbTransaction getCurrentTransaction() {
    return currentTransaction.get();
  }

  @Deprecated
  protected static void setCurrentTransaction(KbTransaction transaction) {
    currentTransaction.set(transaction);
  }
  
  //====|    Static fields    |===================================================================//
    
  /** 
   * Value indicating that the Base Client should use one TCP socket for the entire session.
   */
  public static final int PERSISTENT_CONNECTION = 2;
  
  /* *
   * Dictionary of CycClient instances, indexed by thread so that the application does not have to
   * keep passing around a CycClient object reference.
   */
  //@Deprecated
  //private static final Map<Thread, CycClient> CYC_ACCESS_INSTANCES = new HashMap<>();
  
  private static final String UTF8 = "UTF-8";

  private static final String CYC_IMAGE_ID_EXPRESSION = makeSublStmt("cyc-image-id");
  
  private static final Logger LOGGER = LoggerFactory.getLogger(CycClient.class);
  
  private static final Logger LOGGER_SUBL = LoggerFactory
          .getLogger(CycClient.class.getPackage().getName() + ".SubL");

  
  //====|    Instance fields    |=================================================================//
    
  /* * 
   * The indicator that API request forms should be logged to a file api-requests.lisp in the 
   * working directory.
   * /
  final private boolean areAPIRequestsLoggedToFile = false;
  
  // TODO: should this be replaced with SLF4J?
  private FileWriter apiRequestLog = null;
  */
  
  /* *
   * Parameter indicating whether the Base Client should use one TCP socket for the entire session,
   * or if the socket is created and then closed for each api call, or if an XML SOAP service 
   * provides the message transport.
   * /
  private int persistentConnection = PERSISTENT_CONNECTION;
  */
  
  /** 
   * the Cyc server host name 
   */
  final private String hostName;
  
  /** 
   * The Cyc server host TCP port number.
   */
  final private Integer port;
  
  private Comm comm;
  
  /** 
   * The parameter that, when true, causes a trace of the messages to and from the server .
   */
  private int trace = CycConnectionImpl.API_TRACE_NONE;
  //protected int trace = CycConnection.API_TRACE_MESSAGES;
  //protected int trace = CycConnection.API_TRACE_DETAILED;

  /**
   * Reference to <tt>CycConnection</tt> object which manages the api connection to the OpenCyc
   * server.
   */
  private CycConnection cycConnection;

  /** 
   * The timestamp for the previous access to Cyc, used to re-establish too-long unused connections.
   */
  private long previousAccessedMilliseconds = System.currentTimeMillis();
  
  /**
   * The Cyc image ID used for detecting new Cyc images that cause the constants cache to be reset.
   */
  private String cycImageID;
  
  /** 
   * Indicates whether the connection is closed.
   */
  private volatile boolean isClosed = false;
  
  private boolean hasServerPatchingBeenChecked = false;
  
  private boolean reestablishClosedConnections = true;
  private Boolean isOpenCyc = null;
  private CycCommandTool converseTool;
  private AssertToolImpl assertTool;
  private ComparisonToolImpl comparisonTool;
  private KbObjectToolImpl compatibilityTool;
  private InferenceToolImpl inferenceTool;
  private InspectorToolImpl inspectorTool;
  private KeTextToolImpl keTextTool;
  private LookupToolImpl lookupTool;
  private ObjectToolImpl objectTool;
  private UnassertToolImpl unassertTool;
  //private OwlToolImpl owlTool;
  //private RkfToolImpl rkfTool;
  private CycServerInfoImpl serverInfo;
  
  //====|    Construction    |====================================================================//
  
  /**
   * Constructs a new CycAccess object given a CycAddress address and no CycSession.
   * 
   * @param server The address of the server to connect to.
   * @throws CycConnectionException if cyc server host not found on the network or a data communication error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  private CycClient(CycAddress server) throws CycConnectionException, CycApiException {
    this(server.getHostName(), server.getBasePort());
    commonInitialization(new CycConnectionImpl(server, this));
  }
  
  /**
   * Constructs a new CycAccess object from a CycConnection.
   *
   * @param conn the Cyc connection object (in persistent, binary mode)
   * @throws CycConnectionException if cyc server host not found on the network or a data communication error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  private CycClient(CycConnection conn) throws CycConnectionException, CycApiException {
    this(conn.getHostName(), conn.getBasePort());
    commonInitialization(conn);
  }
  
  private CycClient(String hostName, Integer port) throws CycConnectionException, CycApiException {
    this.hostName = hostName;
    this.port = port;
  }
  
  /**
   * Constructs a new CycAccess object given a CycSessionConfiguration.
   *
   * @param config The CycSessionConfiguration to which the CycAccess should be tied.
   *
   * @throws CycConnectionException if cyc server host not found on the network or a data communication error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  private CycClient(CycSessionConfiguration config) throws CycConnectionException, CycApiException {
    this(config.getCycAddress());
  }
  
  /**
   * Constructs a new CycAccess object given a CycSession.
   *
   * @param session The CycSession to which the CycAccess should be tied.
   *
   * @throws CycConnectionException if cyc server host not found on the network or a data communication error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  private CycClient(CycClientSession session) throws CycConnectionException, CycApiException {
    this(session.getConfiguration());
  }
  
  /**
   * Constructs a new CycAccess object given a Comm implementation and a CycSession.
   * @param comm
   * @throws CycConnectionException
   * @throws CycApiException 
   */
  private CycClient(Comm comm) throws CycConnectionException, CycApiException {
    this(null, null);
    System.out.println("Cyc Access with Comm object: " + comm.toString());
    System.out.flush();
    this.comm = comm;
    CycConnection conn = new CycConnectionImpl(comm, this);
    this.comm.setCycConnection(conn);
    //if (comm instanceof SocketComm) {
      commonInitialization(conn);
    //}
  }
  
  /**
   * Provides common local and remote CycClient object initialization.
   *
   * @throws IOException if a data communication error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  private void commonInitialization(CycConnection cycConnection) 
          throws CycConnectionException, CycApiException {
    this.cycConnection = cycConnection; 
    LOGGER.debug("* * * Initializing * * *");
    //this.persistentConnection = PERSISTENT_CONNECTION;
    /*
    if (Log.current == null) {
      Log.makeLog("cyc-api.log");
    }
    final String apiRequestLogFile = "api-requests.lisp";
    if (areAPIRequestsLoggedToFile) {
      try {
        apiRequestLog = new FileWriter(apiRequestLogFile);
      } catch (IOException ioe) {
        throw new CycConnectionException("Could not write file " + apiRequestLogFile, ioe);
      }
    }
    */
    //CYC_ACCESS_INSTANCES.put(Thread.currentThread(), this);
    /* if (sharedCycAccessInstance == null) {
     * sharedCycAccessInstance = this;
     * } 
     */
    cycImageID = getCycImageID();
    try {
      DefaultInferenceParameterDescriptions.loadInferenceParameterDescriptions(this, 0);
    } catch (Exception e) {
      LOGGER.warn("Could not load inference parameter descriptions.", e);
      Throwable curr = e;
      while (curr != null) {
        LOGGER.warn(curr.getLocalizedMessage());
        curr = curr.getCause();
      }
    }
    LOGGER.info("Instantiated {}", this);
  }
  
  //====|    Public methods    |==================================================================//
  
  @Override
  public CycClientSession getCycSession() {
    // TODO: Can this method be made more robust? - nwinant, 2015-10-14
    
    //return this.session;
    final CycAddress server = this.getCycAddress();
    try {
      final CycClientSession session = CycClientManager.get().getSession(server);
      if (session.getAccess() != this) {
        throw new SessionRuntimeException(
                "Expected to receive a " + CycClientSession.class.getSimpleName()
                        + " tied to this " + CycClient.class.getSimpleName() + " (" + this + ")"
                        + " but received one tied to " + session.getAccess());
      }
      return session;
    } catch (SessionException ex) {
      throw SessionRuntimeException.fromThrowable(ex);
    }
  }
  
  @Override
  public CycClientOptions getOptions() {
    return this.getCycSession().getOptions();
  }

  /**
   * Returns a string representation of this object.
   *
   * @return a string representation of this object
   */
  @Override
  public String toString() {
    final String name = this.getClass().getSimpleName() + "@" + this.hashCode();
    if (cycConnection == null) {
      return name + " no valid connection";
    }
    return name + " " + cycConnection.connectionInfo();
  }

  /**
   * Returns the Cyc api services lease manager.
   *
   * @return the Cyc api services lease manager
   */
  @Deprecated
  @Override
  public LeaseManager getCycLeaseManager() {
    if (this.comm == null) {
      return this.getCycConnection().getCycLeaseManagerMap().get(this.hostName + Integer.toString(this.port + CycConnectionImpl.CFASL_PORT_OFFSET));
    } else {
      throw new CycApiException("Please access CycLeaseManager via CycConnection when Comm object is specified");
    }
  }
  
  /**
   * Turns on the diagnostic trace of socket messages.
   */
  @Override
  public void traceOn() {
    cycConnection.traceOn();
    trace = CycConnectionImpl.API_TRACE_MESSAGES;
  }

  /**
   * Turns on the detailed diagnostic trace of socket messages.
   */
  @Override
  public void traceOnDetailed() {
    if (cycConnection != null) {
      cycConnection.traceOnDetailed();
    }

    trace = CycConnectionImpl.API_TRACE_DETAILED;
  }

  /**
   * Turns off the diagnostic trace of socket messages.
   */
  @Override
  public void traceOff() {
    cycConnection.traceOff();
    trace = CycConnectionImpl.API_TRACE_NONE;
  }

  /**
   * Gets the CycAddress description of the server to which this CycAccess is connected.
   * @return a CycAddress object
   */
  @Override
  public CycAddress getCycAddress() {
    // TODO: this could be improved - nwinant, 2015-10-14
    return CycAddress.get(getHostName(), getBasePort());
  }
  
  /**
   * Does this CycClient access a single Cyc?  
   * 
   * @return true if this CycClient will always access the same Cyc server, and returns false if the
   * CycClient was constructed with  a {@link com.cyc.baseclient.comm.Comm} object that may result 
   * in different calls being sent to different Cyc servers.
   */
  @Override
  public boolean hasStaticCycServer() {
      return (cycConnection.connectedToStaticCyc());
  }
  
  /**
   * gets the hostname of the connection
   * 
   * <p>Deprecated: use getCycAddress().getHostName()
   * 
   * @return the hostname of the connection
   */
  @Deprecated
  @Override
  public String getHostName() {
    return cycConnection.getHostName();
  }

  /**
   * gets the baseport of the connection
   * 
   * <p>Deprecated: use getCycAddress().getBasePort()
   * 
   * @return the baseport of the connection
   */
  @Deprecated
  @Override
  public int getBasePort() {
    return cycConnection.getBasePort();
  }

  /**
   * <p>Deprecated: use getCycAddress().getHttpPort()
   * 
   * @return the http of server the connection is connected to.
   */
  @Deprecated
  @Override
  public int getHttpPort() {
    return cycConnection.getHttpPort();
  }
  
  /**
   * Returns the CycConnection object.
   *
   * @return the CycConnection object
   */
  @Override
  public synchronized CycConnection getCycConnection() {
    try {
      maybeReEstablishCycConnection();
    } catch (CycConnectionException ex) {
      LOGGER.warn("Couldn't re-establish Cyc connection.\n{}", ex.getLocalizedMessage());
    }
    return cycConnection;
  }

  /** Returns whether the connection is closed
   *
   * @return whether the connection is closed
   */
  @Override
  public boolean isClosed() {
    return isClosed;
  }

  /**
   * Closes the CycConnection object. Modified by APB to be able to handle multiple calls to
 close() safely.
   */
  @Override
  public synchronized void close() {
    if (isClosed) {
      LOGGER.debug("Attempting to close {}, but is already closed.", this);
      return;
    }
    LOGGER.debug("Attempting to close {}", this);

    isClosed = true;
    //TODO: Fix CycLeaseManager
    /*
    if (cycLeaseManager != null) {
      cycLeaseManager.interrupt();
    }
    */

    if (cycConnection != null) {
      cycConnection.close();
    }
    /*
    if (areAPIRequestsLoggedToFile) {
      try {
        apiRequestLog.close();
      } catch (IOException e) {
        LOGGER.error("Error when closing apiRequestLog: {}", e);
      }
    }
    */
    //CYC_ACCESS_INSTANCES.remove(Thread.currentThread());
    if (isClosed()) {
      LOGGER.info("Closed {}", this);
    } else {
      LOGGER.error("Could not close {}", this);
    }
  }
  
  /** 
   * Try to enhance <code>urlString</code> to log <code>cyclist</code> in and redirect 
   * to the page  it would otherwise go to directly.
   * 
   * @param urlString
   * @param cyclist
   * @param applicationTerm
   * @return URL string, possibly modified to include a login redirect
   */
  @Override
  public String maybeAddLoginRedirect(final String urlString,
          final Fort cyclist,
          final DenotationalTerm applicationTerm) {
    // sample urlString: cg?CB-CF&236134
    final int questionMarkPos = urlString.indexOf('?');
    if ((cyclist instanceof CycConstantImpl) && (questionMarkPos >= 0)) {
      final String cgiProgram = urlString.substring(0, questionMarkPos);
      final String originalQueryString = urlString.substring(questionMarkPos + 1);
      final StringBuilder stringBuilder = new StringBuilder(cgiProgram);
      stringBuilder.append("?cb-login-handler");
      stringBuilder.append("&new_login_name=").append(
              ((CycConstantImpl) cyclist).getName());
      maybeAddPassword(cyclist, applicationTerm, stringBuilder);
      stringBuilder.append("&redirect-handler=").append(originalQueryString);
      return stringBuilder.toString();
    } else {
      return urlString;
    }
  }

  /**
   * Sets the print-readable-narts feature on.
   *
   * @throws CycConnectionException if cyc server host not found on the network or a data communication error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  @Override
  public void setReadableNarts()
          throws CycConnectionException, CycApiException {
    converse().converseVoid("(csetq *print-readable-narts t)");
  }

  /**
   * Adds #$ to string for all CycConstants mentioned in the string that don't already have them.
   *
   * @param str the String that will have #$'s added to it.
   *
   * @return a copy of str with #$'s added where appropriate.
   *
   * @throws CycConnectionException if cyc server host not found on the network or a data communication error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  @Override
  public String cyclifyString(String str)
          throws CycConnectionException, CycApiException {
    final String command =
            makeSublStmt("cyclify-string", str);
    final String answer = converse().converseString(command);
    return answer;
  }
  
  /* *
   * Imports a MUC (Message Understanding Conference) formatted symbolic expression into cyc via
   * the function which parses the expression and creates assertions for the contained concepts
   * and relations between them.
   *
   * @param mucExpression the MUC (Message Understanding Conference) formatted symbolic expression
   * @param mtName the name of the microtheory in which the imported assertions will be made
   *
   * @return the number of assertions imported from the input MUC expression
   * @throws CycConnectionException if cyc server host not found on the network or a data communication error occurs
   * @throws CycApiException if the api request results in a cyc server error
   * /
  @Deprecated
  public int importMucExpression(CycList mucExpression,
          String mtName)
          throws CycConnectionException, CycApiException {
    String command = makeSublStmt("convert-netowl-sexpr-to-cycl-assertions",
            mucExpression, mtName);
    return converse().converseInt(command);
  }
  */

  /**
   * Returns true if this KB is an OpenCyc image.
   *
   * @return true if this KB is an OpenCyc image, otherwise false
   *
   * @throws CycConnectionException if cyc server host not found on the network or a data 
   * communication error occurs
   */
  @Override
  public synchronized boolean isOpenCyc() throws CycConnectionException {
    if (isOpenCyc == null) {
      try {
        isOpenCyc = converse().converseBoolean("(cyc-opencyc-feature)");
      } catch (CycApiException e) {
        isOpenCyc = false;
      }
    }
    return isOpenCyc;
  }
  
  /**
   * Returns true if this Cyc server has a Full KB.
   *
   * @return true if this Cyc has a Full KB, otherwise false
   *
   * @throws CycConnectionException if cyc server host not found on the network or a data communication error occurs
   */
  public synchronized boolean isFullKB() throws CycConnectionException {
    final InspectorTool inspector = getInspectorTool();
    // TODO: this test could be a lot more rigorous - nwinant, 2014-08-21
    return inspector.isConstantInKB(new CycConstantImpl("#$TKBConstant", new GuidImpl("ca09c15c-2ef2-41d7-87c1-bed958a19357")));
  }
  
  /**
   * Returns the Cyc image ID.
   *
   * @return the Cyc image ID string
   *
   * @throws CycConnectionException if cyc server host not found on the network or a data communication error occurs
   * @throws CycApiException if the Cyc server returns an error
   */
  @Override
  public String getCycImageID()
          throws CycConnectionException, CycApiException {
    return converse().converseString(CYC_IMAGE_ID_EXPRESSION);
  }

  /**
   * Returns whether or not we have a valid lease with the Cyc server.
   *
   * @return whether or not we have a valid lease with the Cyc server
   */
  @Override
  public boolean hasValidLease() {
    //TODO: Fix CycLeaseManager
    boolean valid = false;
    for (Entry<String, LeaseManager> kv : cycConnection.getCycLeaseManagerMap().entrySet()){
      if (kv.getValue().hasValidLease()){
        valid = true;
      }
    }
    if (comm != null) {
      for (Entry<InputStream, LeaseManager> kv : cycConnection.getCycLeaseManagerCommMap().entrySet()) {
        if (kv.getValue().hasValidLease()) {
          valid = true;
        }
      }  
    }
    return valid;
  }
  
  /**
   * Should the connection to the Cyc server be re-established if it for some reason becomes 
   * unreachable? If true, a reconnect will be attempted, and a connection will be established with
   * whatever Cyc server is reachable to the original address, even if it is a different server 
   * process.
   * 
   * @return Whether to reestablish closed connections
   */
  @Override
  public boolean getReestablishClosedConnections() {
    return reestablishClosedConnections;
  }

  /*
   * If true, the CycClient will try to re-establish a connection with a Cyc server at the
   * provided address. Note that this may end up connecting to a different Cyc server than
   * it was originally connected to. If it is important for your application that the CycClient only
   * ever connect to a single server, and not connect to a different server if the original one
   * becomes inaccessible, be sure to set this to false.
   * Default value is true.
   */
  @Override
  public void setReestablishClosedConnections(boolean value) {
    reestablishClosedConnections = value;
  }
  
  /**
   * Provides tools to send commands to a Cyc server in the SubL language, and receive the results 
   * expressed as Java objects.
   * 
   * @return CycCommandTool
   */
  @Override
  public CycCommandTool converse() {
    if (converseTool == null) {
      converseTool = new CycCommandTool(this);
    }
    return converseTool;
  }
  
  /**
   * Provides tools for asserting facts to the Cyc KB.
   * 
   * @return AssertToolImpl
   */
  @Override
  public AssertToolImpl getAssertTool() {
    if (assertTool == null) {
      assertTool = new AssertToolImpl(this);
    }
    return assertTool;
  }

  /**
   * Provides tools for comparing different CycObjects.
   * 
   * @return ComparisonToolImpl
   */
  @Override
  public ComparisonToolImpl getComparisonTool() {
    if (comparisonTool == null) {
      comparisonTool = new ComparisonToolImpl(this);
    }
    return comparisonTool;
  }
  
  @Override
  public KbObjectToolImpl getKbObjectTool() {
    if (compatibilityTool == null) {
      compatibilityTool = new KbObjectToolImpl(this);
    }
    return compatibilityTool;
  }
  
  /**
   * Provides tools for performing inferences over the Cyc KB.
   * 
   * @return InferenceToolImpl
   */
  @Override
  public InferenceToolImpl getInferenceTool() {
    if (inferenceTool == null) {
      inferenceTool = new InferenceToolImpl(this);
    }
    return inferenceTool;
  }

  /**
   * Provides tools for examining individual CycObjects.
   * 
   * @return InspectorToolImpl
   */
  @Override
  public InspectorToolImpl getInspectorTool() {
    if (inspectorTool == null) {
      inspectorTool = new InspectorToolImpl(this);
    }
    return inspectorTool;
  }
  
  @Override
  public KeTextTool getKeTextTool() {
    if (keTextTool == null) {
      keTextTool = new KeTextToolImpl(this);
    }
    return keTextTool;
  }

  /**
   * Provides tools for looking up CycObjects in the Cyc KB.
   * 
   * @return LookupToolImpl
   */
  @Override
  public LookupToolImpl getLookupTool() {
    if (lookupTool == null) {
     lookupTool  = new LookupToolImpl(this);
    }
    return lookupTool;
  }

  /**
   * Provides tools for creating simple CycObjects, such as constants and lists.
   * 
   * @return ObjectToolImpl
   */
  @Override
  public ObjectToolImpl getObjectTool() {
    if (objectTool == null) {
      objectTool = new ObjectToolImpl(this);
    }
    return objectTool;
  }

  /**
   * Provides tools for unasserting facts to the Cyc KB.
   * 
   * @return UnassertToolImpl
   */
  @Override
  public UnassertToolImpl getUnassertTool() {
    if (unassertTool == null) {
      unassertTool = new UnassertToolImpl(this);
    }
    return unassertTool;
  }
  
  /* *
   * Tools for importing OWL ontologies into the Cyc KB
   * 
   * <p>Deprecated: Will either by moved to the KnowledgeManagement API, or deleted.
   * 
   * @return set a new OwlToolImpl if null and return
   * /
  public OwlToolImpl getOwlTool() {
    if (owlTool == null) {
      owlTool = new OwlToolImpl(this);
    }
    return owlTool;
  }

  /* *
   * Tools from the RKF project.
   * 
   * <p>Deprecated: Will either by moved to the KnowledgeManagement API, or deleted.
   * 
   * @return set a new CycRKFTool if null and return
   * /
  public RkfToolImpl getRKFTool() {
    if (rkfTool == null) {
      rkfTool = new RkfToolImpl(this);
    }
    return rkfTool;
  }
  */
  
  /**
   * Provides basic information about the state and location of the current Cyc server.
   * 
   * @return CycServerInfo
   */
  @Override
  public CycServerInfoImpl getServerInfo() {
    if (serverInfo == null) {
      serverInfo = new CycServerInfoImpl(this);
    }
    return serverInfo;
  }
  
  //====|    Internal methods    |================================================================//
  
  AtomicLong converseCounter = new AtomicLong(0);
  
  /** 
   * Converses with Cyc to perform an API command. Creates a new connection for this command if the
   * connection is not persistent.
   *
   * @param command the command string or CycArrayList
   *
   * @return the result as an object array of two objects
   * @see CycConnection#converse(Object)
   *
   * @throws CycConnectionException if cyc server host not found on the network or a data
   * communication error occurs
   * @throws CycApiException if the api request results in a cyc server error
   */
  protected Object[] converse(Object command)
          throws CycConnectionException, CycApiException {
    //Object[] response = {null, null};
    long millis = 0;
    if (LOGGER_SUBL.isDebugEnabled()) {
      millis = converseCounter.incrementAndGet();
      //LOGGER_SUBL.debug("--> ({})\ncmd: {}", millis, toLoggableCommandString(command));
      LOGGER_SUBL.debug("--> ({})\n{}", millis, toLoggableCommandString(command));
    }
    final Object[] response = converseWithRetrying(command);
    previousAccessedMilliseconds = System.currentTimeMillis();
    if (LOGGER_SUBL.isDebugEnabled()) {
      LOGGER_SUBL.debug("<-- ({})\n{}", millis, toLoggableResponseString(response));

    }
    return response;
  }
  
  /**
   * Send a command to Cyc, and maybe try to recover from a closed connection.
   *
   * @param command - String, CycArrayList, or Worker
   * @return the results of evaluating the command
   * @throws CycApiException if the Cyc server returns an error
   * @throws CycConnectionException if cyc server host not found on the network or a data communication error occurs
   * @see #getReestablishClosedConnections()
   * @see CycConnection#converse(java.lang.Object)
   * @see CycConnection#converseBinary(com.cyc.base.connection.Worker)
   */
  protected Object[] converseWithRetrying(Object command) throws CycApiException, CycConnectionException {
    Object[] response = {null, null};
    try {
      response = doConverse(getCycConnection(), command);
    } catch (CycApiClosedConnectionException ex) {
      if (getReestablishClosedConnections()) {
        reEstablishCycConnection();
        response = doConverse(cycConnection, command);
      } else {
        throw (ex);
      }
    }
    return response;
  }
  
  protected synchronized void initializeSession(CycSessionConfiguration config) {
    loadSublPatches(config);
  }
  
  /** 
   * Re-establishes a stale binary CycConnection. 
   *
   * @throws CycConnectionException if cyc server host not found on the network or a data communication error occurs
   * @throws CycApiException if the Cyc server returns an error
   */
  private synchronized void reEstablishCycConnection() 
          throws CycConnectionException, CycApiException {
    LOGGER.warn("Trying to re-establish a closed Cyc connection: {}", this);
    
    //new Throwable().printStackTrace(System.err); // Uncomment this to get a stack trace.
    
    previousAccessedMilliseconds = System.currentTimeMillis();
    cycConnection.close();
    
    if (this.comm == null) {
      LOGGER.warn("Connect using host: {} and port: {}", hostName, port);
      cycConnection = new CycConnectionImpl(hostName, port, this);
    } else {
      LOGGER.warn("Connect using comm object: {}", comm.toString());
      cycConnection = new CycConnectionImpl(comm, this);
      comm.setCycConnection(cycConnection);
      
      // TODO: Vijay: There is a timing issue here.
      // If we do not sleep here, the next call to Cyc, which is getCycImageID()
      // enters a recursive loop of reEstablishCycConnection() and throws a 
      // stack-overflow exception.
      /*
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        // Even having this makes the code throw stack-overflow
        //Thread.currentThread().interrupt();
      }
      */ 
    }

    if (!(cycImageID.equals(getCycImageID()))) {
      LOGGER.warn("New Cyc image detected, resetting caches.");
      CycObjectFactory.resetCaches();
    }
  }
  
  /**
   * Apply any missing SubL patches to the Cyc server.
   * 
   * @throws CycConnectionException if cyc server host not found on the network or a data communication error occurs
   * @throws CycApiException if the Cyc server returns an error
   */
  private void loadSublPatches(CycSessionConfiguration config) throws BaseClientRuntimeException {
    if (!hasServerPatchingBeenChecked) {
      final String forMoreInfo = "For more information, see http://dev.cyc.com/api/server-patching/";
      final SublResourceLoader loader = new SublResourceLoader(this);
      try {
        if (!getServerInfo().isApiCompatible()) {
          String msg = "This server is not compatible with this release of the Core API Suite and cannot be patched; skipping. " + forMoreInfo;
          LOGGER.error(msg);
          throw new BaseClientRuntimeException(msg);
        } else if (Cyc.getSessionManager().getManagerConfiguration().isServerPatchingAllowed()) {
          // TODO: we may also want Cyc to be able to disallow server patching.
          LOGGER.info("Auto-loading SubL patches is enabled (" + CycSessionConfiguration.class.getSimpleName() + "#isServerPatchingAllowed() == true). " + forMoreInfo);
          loader.loadMissingResources(SublFunctions.SOURCES);
          hasServerPatchingBeenChecked = true;
        } else {
          final List<SublSourceFile> missing = loader.findMissingRequiredResources(SublFunctions.SOURCES);
          if (!missing.isEmpty()) {
            LOGGER.warn("Auto-loading SubL patches is not allowed (" + CycSessionConfiguration.class.getSimpleName() + "#isServerPatchingAllowed() == false); skipping. " + forMoreInfo);
            String numResources = (missing.size() >= 2) ? missing.size() + " required resources" : "required resource";
            String msg = "Cyc server " + getServerInfo().getCycAddress() + " is missing " + numResources + ": " + missing.toString() + ". " + forMoreInfo;
            LOGGER.error(msg);
            throw new BaseClientRuntimeException(msg);
          }
        }
      } catch (BaseClientRuntimeException ex) {
        throw ex;
      } catch (Exception ex) {
        String msg = "Could not load SubL patches. " + forMoreInfo;
        LOGGER.warn(msg, ex);
        throw new BaseClientRuntimeException(msg, ex);
      }
    }
  }
  
  private void maybeReEstablishCycConnection() throws CycConnectionException, CycApiException {
    //if (!isSOAPConnection) {
//      if ((previousAccessedMilliseconds + MAX_UNACCESSED_MILLIS) < System.currentTimeMillis()) {
//        Log.current.println("Re-establishing a stale Cyc connection.");
//        reEstablishCycConnection();
//      }
//      else
      if (!((CycConnectionImpl) cycConnection).isValidBinaryConnection()) {
        LOGGER.warn("Re-establishing an invalid Cyc connection  to {}", this);
        reEstablishCycConnection();
      }
    //}
  }
  
  private String toLoggableCommandString(Object command) {
    final CycList commandCycList = (command instanceof CycList)
                                           ? (CycList) command
                                           : getObjectTool().makeCycList((String) command);
    return commandCycList.toPrettyCyclifiedString("");
  }

  private String toLoggableResponseString(Object[] response) {
    if (response[1] instanceof CycArrayList) {
      return ((CycArrayList) response[1]).toPrettyString("");
    } else if (response[1] instanceof Fort) {
      return ((Fort) response[1]).cyclify();
    }
    return response[1].toString();
  }
  
  private Object[] doConverse(final CycConnection cycConnection,
          final Object command) throws CycConnectionException {
    if (command instanceof Worker) {
      //SubL workers talk directly to Cyc:
      cycConnection.converseBinary((Worker) command);
      return null;
    } else {
      //String or CycArrayList commands go through a few intermediaries:
      return cycConnection.converse(command);
    }
  }
  
  private String doubleURLEncode(final String password) throws UnsupportedEncodingException {
    return urlEncode(urlEncode(password));
  }

  private String urlEncode(final String password) throws UnsupportedEncodingException {
    return URLEncoder.encode(password, UTF8);
  }

  /** Add a piece to the URL being string-built to specify cyclist's (encrypted) password */
  private void maybeAddPassword(final Fort cyclist,
          final DenotationalTerm applicationTerm,
          final StringBuilder stringBuilder) {
    if (cyclist instanceof CycConstantImpl) {
      final PasswordManager passwordManager = new PasswordManager(this);
      try {
        if (passwordManager.isPasswordRequired()) {
          final String password = passwordManager.lookupPassword(
                  (CycConstantImpl) cyclist, applicationTerm);
          if (password != null) {
            // @hack -- Cyc decodes '+' characters twice, so we encode twice:
            final String urlEncodedPassword = doubleURLEncode(password);
            stringBuilder.append("&new_login_hashed_password=").append(
                    urlEncodedPassword);
          }
        }
      } catch (IOException ex) {
        // Ignore: User may have to supply password to browser.
      } catch (CycConnectionException ex) {
        // Ignore: User may have to supply password to browser.
      }
    }
  }
    
  /* *
   * Returns a with-bookkeeping-info macro expression.
   *
   * @return a with-bookkeeping-info macro expression
   * /
  private String withBookkeepingInfo() {
    String projectName = "nil";
    final Fort project = getOptions().getKePurpose();
    final Fort cyclist = getOptions().getCyclist();
    
    if (project != null) {
      projectName = project.stringApiValue();
    }

    String cyclistName = "nil";

    if (cyclist != null) {
      cyclistName = cyclist.stringApiValue();
    }

    return "(with-bookkeeping-info (new-bookkeeping-info " + cyclistName + " (the-date) "
            + projectName + "(the-second)) ";
  }
  */
  
}
