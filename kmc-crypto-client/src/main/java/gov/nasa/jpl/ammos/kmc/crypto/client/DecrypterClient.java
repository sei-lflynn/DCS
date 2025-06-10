package gov.nasa.jpl.ammos.kmc.crypto.client;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import gov.nasa.jpl.ammos.kmc.crypto.Decrypter;
import gov.nasa.jpl.ammos.kmc.crypto.KmcCryptoException;
import gov.nasa.jpl.ammos.kmc.crypto.KmcCryptoException.KmcCryptoErrorCode;
import gov.nasa.jpl.ammos.kmc.crypto.KmcCryptoManager;
import gov.nasa.jpl.ammos.kmc.crypto.model.DecryptServiceResponse;
import gov.nasa.jpl.ammos.kmc.crypto.model.Status;

/**
 * The DecrypterClient decrypts data with the KMC Crypto Service.
 * <p>
 * The results of encryption include the cipher text and the metadata that has all the information needed
 * for decrypting the cipher text.
 * <p>
 * The metadata contains all the information, such as the keyRef, needed for performing the decryption.
 *
 */
public class DecrypterClient implements Decrypter {

    private final CryptoServiceClient client;

    private static final Logger logger = LoggerFactory.getLogger(DecrypterClient.class);
    private static final Logger audit = LoggerFactory.getLogger("AUDIT");

    /**
     * Constructor of the {@link Decrypter} client that uses the KMC Crypto Service for decryption.
     *
     * @param cryptoManager The KmcCryptoManager for accessing the configuration parameters.
     * @throws KmcCryptoException if error occurred in connecting to KMS.
     */
    public DecrypterClient(final KmcCryptoManager cryptoManager) throws KmcCryptoException {
        client = new CryptoServiceClient(cryptoManager);
        audit.info("DecrypterClient: User created Decrypter that uses Crypto Service at "
                + cryptoManager.getKmcCryptoServiceURI());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void decrypt(final InputStream inputStream, final OutputStream outputStream,
            final String metadata) throws KmcCryptoException {
        if (inputStream == null) {
            String msg = "Null input stream.";
            logger.error(msg);
            throw new KmcCryptoException(KmcCryptoErrorCode.INVALID_INPUT_VALUE, msg, null);
        }
        if (outputStream == null) {
            String msg = "Null output stream.";
            logger.error(msg);
            throw new KmcCryptoException(KmcCryptoErrorCode.INVALID_INPUT_VALUE, msg, null);
        }
        if (metadata == null) {
            String msg = "Null metadata.";
            logger.error(msg);
            throw new KmcCryptoException(KmcCryptoErrorCode.INVALID_INPUT_VALUE, msg, null);
        }

        String response;
        try {
            logger.debug("DecrypterClient: metadata = " + metadata);
            String encodedMD = URLEncoder.encode(metadata, "UTF-8");
            logger.debug("DecrypterClient: urlencoded metadata = " + encodedMD);
            String uri = "decrypt?metadata=" + encodedMD;
            response = client.executePost(uri, inputStream);
            //logger.info("response = " + response);
        } catch (IOException e) {
            String msg = "Exception in sending request: " + e;
            logger.error(msg);
            throw new KmcCryptoException(KmcCryptoErrorCode.KMS_CONNECTION_ERROR, msg, e);
        }

        DecryptServiceResponse res;
        Gson gson = new Gson();
        try {
            res = gson.fromJson(response, DecryptServiceResponse.class);
        } catch (JsonSyntaxException e) {
            String msg = "Exception parsing the JSON response: " + e + ".\nresponse = " + response;
            logger.error(msg);
            throw new KmcCryptoException(KmcCryptoErrorCode.CRYPTO_MISC_ERROR, msg, e);
        }
        Status status = res.getStatus();
        /*
        if (status.getHttpCode() != HttpServletResponse.SC_OK) {
            String msg = "Failed to decrypt: " + status.getReason();
            logger.error(msg);
            throw new KmcCryptoException(KmcCryptoErrorCode.CRYPTO_MISC_ERROR, msg, null);
        }
        */
        if (!status.isSuccess()) {
            audit.info("DecrypterClient: User failed to decrypt data with Crypto Service");
            String reason = status.getReason();
            logger.error("HTTP code: " + status.getHttpCode() + ", " + reason);
            if (status.getHttpCode() == HttpServletResponse.SC_BAD_REQUEST) {
                if (reason.contains("does not exist")
                        || reason.contains("not found in keystore")
                        || reason.contains("BadPaddingException")
                        || reason.contains("Key algorithm")
                        || reason.contains("Key length")
                        || reason.contains("algorithm is not allowed")) {
                    throw new KmcCryptoException(KmcCryptoErrorCode.CRYPTO_KEY_ERROR, reason, null);
                } else if (reason.contains("Invalid cipher transformation")
                        || reason.contains("AEADBadTagException")
                        || reason.contains("Invalid padding scheme")
                        || reason.contains("algorithm provider")) {
                    throw new KmcCryptoException(KmcCryptoErrorCode.CRYPTO_ALGORITHM_ERROR, reason, null);
                } else if (reason.contains("key state")) {
                    throw new KmcCryptoException(KmcCryptoErrorCode.CRYPTO_KEY_ERROR, reason, null);
                } else if (reason.contains("metadata")) {
                    throw new KmcCryptoException(KmcCryptoErrorCode.CRYPTO_METADATA_ERROR, reason, null);
                } else if (reason.contains("Unexpected")) {
                    throw new KmcCryptoException(KmcCryptoErrorCode.CRYPTO_MISC_ERROR, reason, null);
                } else {
                    throw new KmcCryptoException(KmcCryptoErrorCode.INVALID_INPUT_VALUE, reason, null);
                }
            } else if (status.getHttpCode() == HttpServletResponse.SC_INTERNAL_SERVER_ERROR) {
                if (reason.contains("BadPaddingException")) {
                    throw new KmcCryptoException(KmcCryptoErrorCode.CRYPTO_KEY_ERROR, reason, null);
                } else {
                    throw new KmcCryptoException(KmcCryptoErrorCode.CRYPTO_MISC_ERROR, reason, null);
                }
            } else {
                throw new KmcCryptoException(KmcCryptoErrorCode.KMS_CONNECTION_ERROR, reason, null);
            }
        }
        byte[] cleartext = res.getCleartext();
        try {
            outputStream.write(cleartext);
            closeStream(outputStream);
        } catch (IOException e) {
            String msg = "Exception in writing ciphertext: " + e;
            logger.error(msg);
            throw new KmcCryptoException(KmcCryptoErrorCode.CRYPTO_MISC_ERROR, msg, e);
        }
        audit.info("DecrypterClient: User decrypted data with Crypto Service");
    }

    private void closeStream(final Closeable stream) {
        try {
            if (stream != null) {
                stream.close();
            }
        } catch (IOException e) {
            logger.error("Failed to close a stream in the finally block: " + e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadCryptoKey(final String keyRef) throws KmcCryptoException {
        // Crypto service client does not use keys directly.
        String msg = "Crypto service client does not retrieve keys.";
        throw new KmcCryptoException(KmcCryptoErrorCode.CRYPTO_MISC_ERROR, msg, null);
    }

}
