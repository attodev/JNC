/*    -*- Java -*-
 *
 *  Copyright 2007 Tail-F Systems AB. All rights reserved.
 *
 *  This software is the confidential and proprietary
 *  information of Tail-F Systems AB.
 *
 *  $Id$
 *
 */

package com.tailf.inm;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.ChannelCondition;
import java.util.ArrayList;
import java.io.File;

/**
 * A SSH NETCONF connection class. Can be used whenever {@link NetconfSession}
 * intends to use SSH for its transport.
 * <p>
 * Example:
 * 
 * <pre>
 * SSHConnection ssh = new SSHConnection(&quot;127.0.0.1&quot;, 2023);
 * ssh.authenticateWithPassword(&quot;ola&quot;, &quot;secret&quot;);
 * SSHSession tr = new SSHSession(ssh);
 * NetconfSession dev1 = new NetconfSession(tr);
 * </pre>
 */
public class SSHConnection {

    Connection connection = null;

    /**
     * By default we connect to the IANA registered port for NETCONF which is
     * 830
     * 
     * @param host
     *            Host or IP address to connect to
     */

    public SSHConnection(String host) throws IOException, INMException {
        this(host, 830, 0);
    }

    /**
     * This method establishes an SSH connection to a host, once the connection
     * is established it must be authenticated.
     * 
     * @param host
     *            Host name.
     * @param port
     *            Port number to connect to.
     */
    public SSHConnection(String host, int port) throws IOException,
            INMException {
        this(host, port, 0);
    }

    /**
     * This method establishes an SSH connection to a host, once the connection
     * is established it must be authenticated.
     * 
     * @param host
     *            Host name.
     * @param port
     *            Port number to connect to.
     * @param connectTimeout
     */
    public SSHConnection(String host, int port, int connectTimeout)
            throws IOException, INMException {

        connection = new Connection(host, port);
        connection.connect(null, connectTimeout, 0);
    }

    /**
     * This method establishes an SSH connection to a host, once the connection
     * is established it must be authenticated.
     * 
     * @param host
     *            Host name.
     * @param port
     *            Port number to connect to.
     * @param connectTimeout
     *            Connection timeout timer. Connect the underlying TCP socket to
     *            the server with the given timeout value (non-negative, in
     *            milliseconds). Zero means no timeout.
     * @param kexTimeout
     *            Key exchange timeout timer. Timeout for complete connection
     *            establishment (non-negative, in milliseconds). Zero means no
     *            timeout. The timeout counts until the first key-exchange round
     *            has finished.
     * @throws IOException
     *             In case of a timeout (either connectTimeout or kexTimeout) a
     *             SocketTimeoutException is thrown.
     *             <p>
     *             An exception may also be thrown if the connection was already
     *             successfully connected (no matter if the connection broke in
     *             the mean time) and you invoke <code>connect()</code> again
     *             without having called {@link #close()} first.
     */
    public SSHConnection(String host, int port, int connectTimeout,
            int kexTimeout) throws IOException, INMException {
        connection = new Connection(host, port);
        connection.connect(null, connectTimeout, kexTimeout);
    }

    /**
     * @return the underlying Ganymed connection object This is required if wish
     *         to use the addConnectionMonitor() method in the ganymed
     *         Connection class.
     * 
     */

    Connection getConnection() {
        return connection;
    }

    /**
     * This is required if wish to have access to the ganymed connection object
     * outside of this package.
     * 
     * @return the underlying Ganymed connection object
     */

    public Connection getGanymedConnection() {
        return connection;
    }

    /**
     * Authenticate with regular username pass.
     * 
     * @param user
     *            User name.
     * @param password
     *            Password.
     * 
     **/
    public void authenticateWithPassword(String user, String password)
            throws IOException, INMException {
        if (!connection.authenticateWithPassword(user, password))
            throw new INMException(INMException.AUTH_FAILED);
    }

    /**
     * Authenticate with the name of a file containing the private key See
     * ganymed docs for full explanation, use null for password if the key
     * doesn't have a passphrase.
     * 
     * @param user
     *            User name.
     * @param pemFile
     *            Fila name.
     * @param password
     *            Password.
     **/
    public void authenticateWithPublicKeyFile(String user, File pemFile,
            String password) throws IOException, INMException {
        try {
            if (!connection.authenticateWithPublicKey(user, pemFile, password)) {
                throw new INMException(INMException.AUTH_FAILED);
            }
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new INMException(INMException.AUTH_FAILED);
        }
    }

    /**
     * Authenticate with a private key. See ganymed docs for full explanation,
     * use null for password if the key doesn't have a passphrase.
     * 
     * @param user
     *            User name.
     * @param pemPrivateKey
     *            Private key.
     * @param pass
     *            Passphrase.
     **/
    public void authenticateWithPublicKey(String user, char[] pemPrivateKey,
            String pass) throws IOException, INMException {
        if (!connection.authenticateWithPublicKey(user, pemPrivateKey, pass)) {
            throw new INMException(INMException.AUTH_FAILED);
        }
    }

    /**
     * Closes the SSH session/connection.
     */
    public void close() {
        connection.close();
    }

}