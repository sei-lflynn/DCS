package gov.nasa.jpl.ammos.kmc.crypto.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The KmcHttpsConnection is used for creating a secure HTTP connection to access web applications.
 *
 *
 */
class KmcHttpsConnection {
    private static final Logger logger = LoggerFactory.getLogger(KmcHttpsConnection.class);

    private static final String AMMOS_TRUSTSTORE_FILENAME = "/etc/pki/tls/certs/ammos-truststore.jks"; // $NON-NLS-1$
    private static final String USERHOME_TRUSTSTORE_FILENAME = ".truststore"; // $NON-NLS-1$
    private static final String JAVAX_NET_SSL_TRUSTSTORE = "javax.net.ssl.trustStore"; // $NON-NLS-1$

    private final SSLSocketFactory sslSocketFactory;
    private KeyStore trustStore;

    /**
     * Constructor.
     *
     * @exception Exception if error occurs in creating SSL context.
     *
     */
    KmcHttpsConnection() throws Exception {
        SSLContext ctx = getSSLContext();
        sslSocketFactory = ctx.getSocketFactory();
    }

    /**
     * Gets a secure HTTP connection using the TLS protocol.
     *
     * @param targetURL the target URL.
     * @return Secure HTTP TLS connection.
     * @throws IOException if error occurs during connection.
     */
    final HttpsURLConnection getSecureHttpConnection(final String targetURL) throws IOException {
        logger.trace("Create https connection to " + targetURL);
        if (!targetURL.startsWith("https")) {
            throw new IOException("Not https URL: " + targetURL);
        }
        URL url = new URL(targetURL);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setSSLSocketFactory(sslSocketFactory);
        return conn;
    }

    /**
     * Gets the SSLContext for making SSL/TLS connection.
     * @return A SSLContext for making SSL/TLS connection.
     * @exception Exception if error occurs in creating SSL context.
     */
    private SSLContext getSSLContext() throws Exception {
        loadTrustStore();
        try {
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX");
            tmf.init(this.trustStore);
            TrustManager[] tm = tmf.getTrustManagers();
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, tm, null);
            return ctx;
        } catch (Exception e) {
            String msg = "Exception in getSSLContext(): " + e;
            logger.error(msg);
            throw new Exception(msg, e);
        }
    }

    /**
     * Loads the truststore file.
     *
     * <p>
     * The truststore is located in the following order:
     * <ol>
     * <li> The value of the property javax.net.ssl.trustStore,
     * <li> The .truststore at the user's home directory,
     * <li> The AMMOS truststore at /etc/pki/tls/certs/ammos-truststore.jks,
     * <li> The Java system truststore (i.e. cacerts).
     * </ol>
     *
     * If a relative truststore path is set, the file is found from classpath.
     *
     * @exception Exception if error occurs in loading the truststore.
     */
    private void loadTrustStore() throws Exception {
        String truststorePath = null;
        InputStream truststoreInputStream = null;
        String userTruststore = System.getProperty("user.home") // $NON-NLS-1$
            + File.separator + USERHOME_TRUSTSTORE_FILENAME;

        if (System.getProperty(JAVAX_NET_SSL_TRUSTSTORE) != null) {
            truststorePath = System.getProperty(JAVAX_NET_SSL_TRUSTSTORE);
        }
        if (truststorePath == null) {
            if (new File(userTruststore).exists()) {
                truststorePath = userTruststore;
            }
        }
        if (truststorePath == null) {
            if (new File(AMMOS_TRUSTSTORE_FILENAME).exists()) {
                truststorePath = AMMOS_TRUSTSTORE_FILENAME;
            }
        }
        if (truststorePath == null) {
            logger.warn("Truststore not found at " + userTruststore
                    + " nor " + AMMOS_TRUSTSTORE_FILENAME
                    + ", using Java system truststore.");
        } else {
            logger.info("load truststore from file: " + truststorePath);
            try {
                File truststoreFile = new File(truststorePath);
                truststoreInputStream = new FileInputStream(truststoreFile);
                System.setProperty(JAVAX_NET_SSL_TRUSTSTORE, truststorePath);
            } catch (FileNotFoundException e) {
                String msg = "Failed to open truststore file: " + e;
                logger.error(msg);
                throw new Exception(msg, e);
            }
            try {
                this.trustStore = KeyStore.getInstance("JKS");
                this.trustStore.load(truststoreInputStream, null);
                logger.debug("Truststore loaded OK from " + System.getProperty(JAVAX_NET_SSL_TRUSTSTORE));
            } catch (Exception e) {
                String msg = "Failed to load truststore: " + e;
                logger.error(msg);
                throw new Exception(msg, e);
            }
        }
    }

}
