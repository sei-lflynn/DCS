package gov.nasa.jpl.ammos.kmc.crypto.client;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import gov.nasa.jpl.ammos.kmc.crypto.KmcCryptoException;
import gov.nasa.jpl.ammos.kmc.crypto.KmcCryptoException.KmcCryptoErrorCode;
import gov.nasa.jpl.ammos.kmc.crypto.KmcCryptoManager;
import gov.nasa.jpl.ammos.kmc.crypto.model.CryptoKeyServiceResponse;
import gov.nasa.jpl.ammos.kmc.crypto.model.KeyInfo;
import gov.nasa.jpl.ammos.kmc.crypto.model.Status;

/**
 * HTTPS Client to access the KMC Crypto Service.
 *
 *
 */
public class CryptoServiceClient {
    public static final int MAX_CRYPTO_SERVICE_BYTES = 100000000; //1024 * 1024 ;//* 100; // 100MB
    private static final int BUFSIZE = 4096;

    private final KmcHttpsConnection connManager;
    private final String cryptoServiceURI;
    private final String ssoCookie;

    private static final Logger logger = LoggerFactory.getLogger(CryptoServiceClient.class);

    /**
     * Constructor of the CryptoServiceClient for sending RESTful web service
     * requests and receiving responses of the KMC Crypto Service.
     * @param cryptoManager The KMC Crypto Manager.
     * @throws KmcCryptoException if crypto-service URI is not found.
     */
    public CryptoServiceClient(final KmcCryptoManager cryptoManager) throws KmcCryptoException {
        String uri = cryptoManager.getKmcCryptoServiceURI();
        if (uri == null) {
            String msg = KmcCryptoManager.CFG_CRYPTO_SERVICE_URI + " not found in "
                    + KmcCryptoManager.DEFAULT_CRYPTO_CONFIG_FILE;
            logger.error(msg);
            throw new KmcCryptoException(KmcCryptoErrorCode.INVALID_INPUT_VALUE, msg, null);
        }
        if (!uri.endsWith("/")) {
            uri = uri + "/";
        }
        try {
            connManager = new KmcHttpsConnection();
        } catch (Exception e) {
            String msg = "Exception in creating secure connection: " + e;
            logger.error(msg);
            throw new KmcCryptoException(KmcCryptoErrorCode.CRYPTO_MISC_ERROR, msg, e);
        }
        cryptoServiceURI = uri;
        ssoCookie = cryptoManager.getSsoCookie();
        logger.trace("CryptoServiceClient ssoCookie = " + ssoCookie);
    }

    /**
     * Returns the information of the key.
     *
     * @param cryptoManager The KMC Crypto Manager.
     * @param keyRef The keyRef of the key.
     * @return Information of the key.
     * @throws KmcCryptoException if error is encountered.
     */
    public final KeyInfo getKeyInfo(final KmcCryptoManager cryptoManager, final String keyRef)
            throws KmcCryptoException {
        String response;
        try {
            String uri = cryptoServiceURI + "key-info?keyRef=" + keyRef;
            response = executeGet(uri);
            logger.info("response = " + response);
        } catch (IOException e) {
            String msg = "Exception in sending request: " + e;
            logger.error(msg);
            throw new KmcCryptoException(KmcCryptoErrorCode.KMS_CONNECTION_ERROR, msg, e);
        }

        CryptoKeyServiceResponse res;
        Gson gson = new Gson();
        try {
            res = gson.fromJson(response, CryptoKeyServiceResponse.class);
        } catch (JsonSyntaxException e) {
            String msg = "Exception parsing the JSON response: " + e + ".\nResponse = " + response;
            logger.error(msg);
            throw new KmcCryptoException(KmcCryptoErrorCode.CRYPTO_MISC_ERROR, msg, e);
        }
        Status status = res.getStatus();
        if (!status.isSuccess()) {
            String reason = status.getReason();
            logger.error("HTTP code: " + status.getHttpCode() + ", " + reason);
            if (status.getHttpCode() == HttpServletResponse.SC_BAD_REQUEST) {
                if (reason.contains("does not exist")
                        || reason.contains("not found in keystore")
                        || reason.contains("does not match")) {
                    throw new KmcCryptoException(KmcCryptoErrorCode.CRYPTO_KEY_ERROR, reason, null);
                } else if (reason.contains("algorithm is not allowed")) {
                    throw new KmcCryptoException(KmcCryptoErrorCode.CRYPTO_ALGORITHM_ERROR, reason, null);
                } else {
                    throw new KmcCryptoException(KmcCryptoErrorCode.INVALID_INPUT_VALUE, reason, null);
                }
            } else if (status.getHttpCode() == HttpServletResponse.SC_INTERNAL_SERVER_ERROR) {
                throw new KmcCryptoException(KmcCryptoErrorCode.CRYPTO_MISC_ERROR, reason, null);
            } else {
                throw new KmcCryptoException(KmcCryptoErrorCode.KMS_CONNECTION_ERROR, reason, null);
            }
        }

        return res.getKeyInfo();
    }

    private String executeGet(final String targetURI) throws IOException, KmcCryptoException {
        HttpsURLConnection conn = null;

        String uri = targetURI;
        if (!uri.startsWith("https:")) {
            uri = cryptoServiceURI + uri;
        }
        logger.debug("executeGet() url = " + uri);

        try {
            // Create connection
            conn = connManager.getSecureHttpConnection(uri);
            conn.setRequestMethod("GET");
            //conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Cookie", ssoCookie);
            conn.setUseCaches(false);

            // Get Response
            String response = getResponseBody(conn.getInputStream());
            logger.debug("executeGet() response = " + response);
            return response;
        } catch (UnknownHostException e) {
            String msg = "Failed to connect to Crypto Service at " + targetURI + ": " + e.toString();
            throw new KmcCryptoException(KmcCryptoErrorCode.KMS_CONNECTION_ERROR, msg, e);
        } catch (ConnectException e) {
            String msg = "Failed to connect to Crypto Service at " + targetURI + ": " + e.toString();
            throw new KmcCryptoException(KmcCryptoErrorCode.KMS_CONNECTION_ERROR, msg, e);
        } catch (MalformedURLException e) {
            String msg = "Failed to connect to Crypto Service at " + targetURI + ": " + e.toString();
            throw new KmcCryptoException(KmcCryptoErrorCode.KMS_CONNECTION_ERROR, msg, e);
        } catch (FileNotFoundException e) {
            String msg = "Failed to connect to Crypto Service at " + targetURI + ": " + e.toString();
            throw new KmcCryptoException(KmcCryptoErrorCode.KMS_CONNECTION_ERROR, msg, e);
        } catch (IOException e) {
            logger.error("executeGet() Crypto Service returns error: " + e);
            if (conn == null) {
                logger.info("executeGet() Connection is null");
                throw new KmcCryptoException(KmcCryptoErrorCode.KMS_CONNECTION_ERROR, "Failed to connect to Crypto Service", e);
            }
            if (conn.getErrorStream() == null) {
                logger.info("executeGet() Connection ErrorStream is null");
                throw new KmcCryptoException(KmcCryptoErrorCode.KMS_CONNECTION_ERROR, "Failed to connect to Crypto Service", e);
            }
            String response = getResponseBody(conn.getErrorStream());
            logger.error("executeGet() Error response body: " + response);
            return response;
        } finally {
            logger.debug("close IO streams");
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * Send a POST request.
     * @param targetURI The URI request to be sent.
     * @param inputStream The data to be sent.
     * @return Response in a string.
     * @throws KmcCryptoException Exception during sending request or receiving response.
     * @throws IOException Exception during reading the response.
     */
    public final String executePost(final String targetURI, final InputStream inputStream)
            throws KmcCryptoException, IOException {
        HttpsURLConnection conn = null;

        String uri = targetURI;
        if (!uri.startsWith("https:")) {
            uri = cryptoServiceURI + uri;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("executePost() url = " + uri);
        }

        try {
            conn = connManager.getSecureHttpConnection(uri);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/octet-stream");
            conn.setRequestProperty("Cookie", ssoCookie);
            conn.setUseCaches(false);
            conn.setDoOutput(true);

            OutputStream output = conn.getOutputStream();
            byte[] buffer = new byte[BUFSIZE];
            int total = 0;
            int n;
            while ((n = inputStream.read(buffer)) != -1) {
                logger.trace("executePost() write bytes = " + n);
                output.write(buffer, 0, n);
                total = total + n;
            }
            output.close();
            logger.debug("executePost() Total number of bytes of data sent = " + total);

            String response = getResponseBody(conn.getInputStream());
            if (logger.isTraceEnabled()) {
                logger.trace("executePost() response = " + response);
            } else {
                logger.debug("executePost() Total number of bytes of response received = " + response.length());
            }
            return response;
        } catch (UnknownHostException e) {
            String msg = "Failed to connect to Crypto Service at " + targetURI + ": " + e.toString();
            throw new KmcCryptoException(KmcCryptoErrorCode.KMS_CONNECTION_ERROR, msg, e);
        } catch (MalformedURLException e) {
            String msg = "Failed to connect to Crypto Service at " + targetURI + ": " + e.toString();
            throw new KmcCryptoException(KmcCryptoErrorCode.KMS_CONNECTION_ERROR, msg, e);
        } catch (ConnectException e) {
            String msg = "Failed to connect to Crypto Service at " + targetURI + ": " + e.toString();
            throw new KmcCryptoException(KmcCryptoErrorCode.KMS_CONNECTION_ERROR, msg, e);
        } catch (FileNotFoundException e) {
            String msg = "Failed to connect to Crypto Service at " + targetURI + ": " + e.toString();
            throw new KmcCryptoException(KmcCryptoErrorCode.KMS_CONNECTION_ERROR, msg, e);
        } catch (IOException e) {
            String response;
            if (e.toString().contains("HTTP response code: 500")) {
                response = "Crypto service internal server error. Exception: " + e;
                logger.error(response);
                throw new KmcCryptoException(KmcCryptoErrorCode.CRYPTO_MISC_ERROR, response, e);
            } else if (conn == null || conn.getErrorStream() == null) {
                response = "No response is received from the crypto service. Exception: " + e;
                logger.error(response);
                throw new KmcCryptoException(KmcCryptoErrorCode.KMS_CONNECTION_ERROR, response, e);
            } else {
                response = getResponseBody(conn.getErrorStream());
                logger.error("executePost() " + e);
                logger.error("Error response: " + response);
                return response;
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private String getResponseBody(final InputStream is) throws IOException {
        String line;
        StringBuilder response = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        try {
            while ((line = reader.readLine()) != null) {
                response.append(line);
                response.append('\n');
            }
        } catch (IOException e) {
            logger.error("IOException in HTTP request: " + e);
            String msg = e.getMessage();
            if (msg.contains("HTTP response code: 400")) {
                logger.debug("Resource not found: " + e);
            } else {
                logger.error("Error in HTTP request: " + e + ", Response body:\n"
                        + response.toString());
            }
            throw e;
        } finally {
            logger.debug("close IO streams");
            close(reader);
        }
        return response.toString().trim();
    }

    private void close(final Closeable c) throws IOException {
        if (c == null) {
            return;
        }
        try {
            c.close();
        } catch (IOException e) {
            String msg = "Exception closing IO stream: " + e;
            logger.error(msg);
            throw e;
        }
    }

}
