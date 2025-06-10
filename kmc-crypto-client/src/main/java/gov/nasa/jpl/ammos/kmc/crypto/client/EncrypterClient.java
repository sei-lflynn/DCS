package gov.nasa.jpl.ammos.kmc.crypto.client;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import gov.nasa.jpl.ammos.kmc.crypto.Encrypter;
import gov.nasa.jpl.ammos.kmc.crypto.KmcCryptoException;
import gov.nasa.jpl.ammos.kmc.crypto.KmcCryptoException.KmcCryptoErrorCode;
import gov.nasa.jpl.ammos.kmc.crypto.KmcCryptoManager;
import gov.nasa.jpl.ammos.kmc.crypto.model.EncryptServiceResponse;
import gov.nasa.jpl.ammos.kmc.crypto.model.KeyInfo;
import gov.nasa.jpl.ammos.kmc.crypto.model.Status;

/**
 * The EncrypterClient encrypts data by use of the KMC Crypto Service.
 * <p>
 * The results of encryption include the cipher text and the metadata that has all the information needed
 * for decrypting the cipher text.
 * <p>
 * The key decides the algorithm to be used for encryption.  The transformation to be used is specified
 * in the kmc-crypto.cfg at the service side.
 *
 */
public class EncrypterClient implements Encrypter {
    private final KmcCryptoManager cryptoManager;
    private final CryptoServiceClient client;
    private final String keyRef;
    private final KeyInfo keyInfo;

    private static final Logger logger = LoggerFactory.getLogger(EncrypterClient.class);
    private static final Logger audit = LoggerFactory.getLogger("AUDIT");

    /**
     * Constructor of the {@link Encrypter} Client class.
     *
     * @param cryptoManager The KmcCryptoManager for accessing the configuration parameters.
     * @param keyRef A string for identifying the key, i.e. the name of the key.
     * @throws KmcCryptoException if error in retrieving the key.
     */
    public EncrypterClient(final KmcCryptoManager cryptoManager, final String keyRef)
                        throws KmcCryptoException {
        this.cryptoManager = cryptoManager;
        client = new CryptoServiceClient(cryptoManager);
        this.keyRef = keyRef;

        if (keyRef == null) {
            String msg = "Null keyRef.";
            logger.error(msg);
            throw new KmcCryptoException(KmcCryptoErrorCode.INVALID_INPUT_VALUE, msg, null);
        }
        keyInfo = client.getKeyInfo(cryptoManager, keyRef);
        audit.info("EncrypterClient: User created Encrypter that uses Crypto Service at "
                + cryptoManager.getKmcCryptoServiceURI());
    }

    @Override
    public final String encrypt(final InputStream inputStream, final OutputStream outputStream)
            throws KmcCryptoException {
        return encrypt(inputStream, 0, null, outputStream);
    }

    @Override
    public final String encrypt(final InputStream inputStream, final int encryptOffset,
            final String iv, final OutputStream outputStream) throws KmcCryptoException {
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
        String uri = "encrypt?keyRef=" + keyRef;
        String transformation = cryptoManager.getCipherTransformation(keyInfo.getAlgorithm());
        if (transformation != null) {
            uri = uri + "&transformation=" + transformation;
        }
        if (encryptOffset != 0) {
            uri = uri + "&encryptOffset=" + String.valueOf(encryptOffset);
        }
        if (iv != null) {
            uri = uri + "&iv=" + iv;
        }

        String response;
        try {
            response = client.executePost(uri, inputStream);
            logger.trace("response = " + response);
        } catch (KmcCryptoException e) {
            logger.error("KmcCryptoException: ", e);
            throw e;
        } catch (IOException e) {
            String msg = "Exception in sending request: " + e;
            logger.error(msg);
            throw new KmcCryptoException(KmcCryptoErrorCode.KMS_CONNECTION_ERROR, msg, e);
        }

        EncryptServiceResponse res;
        Gson gson = new Gson();
        try {
            res = gson.fromJson(response, EncryptServiceResponse.class);
        } catch (JsonSyntaxException e) {
            String msg = "Exception parsing the JSON response: " + e + ".\nresponse = " + response;
            logger.error(msg);
            throw new KmcCryptoException(KmcCryptoErrorCode.CRYPTO_MISC_ERROR, msg, e);
        }
        Status status = res.getStatus();
        if (!status.isSuccess()) {
            audit.info("EncrypterClient: User failed to encrypt data with Crypto Service");
            String reason = status.getReason();
            logger.error("HTTP code: " + status.getHttpCode() + ", " + reason);
            if (status.getHttpCode() == HttpServletResponse.SC_BAD_REQUEST) {
                if (reason.contains("does not exist")
                        || reason.contains("does not match")) {
                    throw new KmcCryptoException(KmcCryptoErrorCode.CRYPTO_KEY_ERROR, reason, null);
                } else if (reason.contains("algorithm is not allowed")
                        || reason.contains("algorithm provider")) {
                    throw new KmcCryptoException(KmcCryptoErrorCode.CRYPTO_ALGORITHM_ERROR, reason, null);
                } else if (reason.contains("key state")) {
                    throw new KmcCryptoException(KmcCryptoErrorCode.CRYPTO_KEY_ERROR, reason, null);
                } else {
                    throw new KmcCryptoException(KmcCryptoErrorCode.INVALID_INPUT_VALUE, reason, null);
                }
            } else if (status.getHttpCode() == HttpServletResponse.SC_INTERNAL_SERVER_ERROR) {
                throw new KmcCryptoException(KmcCryptoErrorCode.CRYPTO_MISC_ERROR, reason, null);
            } else {
                throw new KmcCryptoException(KmcCryptoErrorCode.KMS_CONNECTION_ERROR, reason, null);
            }
        }
        byte[] ciphertext = res.getCiphertext();
        //logger.info("result = " + result);
        //logger.info("encrypt() metadata: " + result.getMetadata());
        //logger.info("encrypt() ciphertext bytes : " + ciphertext.length);
        try {
            outputStream.write(ciphertext);
            closeStream(outputStream);
        } catch (IOException e) {
            String msg = "Exception in writing ciphertext: " + e;
            logger.error(msg);
            throw new KmcCryptoException(KmcCryptoErrorCode.CRYPTO_MISC_ERROR, msg, e);
        }
        audit.info("EncrypterClient: User encrypted data with Crypto Service");
        return res.getMetadata();
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
