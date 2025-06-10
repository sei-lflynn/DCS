package gov.nasa.jpl.ammos.kmc.crypto.client;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import gov.nasa.jpl.ammos.kmc.crypto.IcvCreator;
import gov.nasa.jpl.ammos.kmc.crypto.KmcCryptoException;
import gov.nasa.jpl.ammos.kmc.crypto.KmcCryptoException.KmcCryptoErrorCode;
import gov.nasa.jpl.ammos.kmc.crypto.KmcCryptoManager;
import gov.nasa.jpl.ammos.kmc.crypto.model.IcvCreateServiceResponse;
import gov.nasa.jpl.ammos.kmc.crypto.model.KeyInfo;
import gov.nasa.jpl.ammos.kmc.crypto.model.Status;

/**
 * This class implements IcvCreator for creating integrity check value from input data.
 *
 */
public class IcvCreatorClient implements IcvCreator {
    private final KmcCryptoManager cryptoManager;
    private final CryptoServiceClient client;
    private KeyInfo keyInfo;
    private String keyRef;

    private static final Logger logger = LoggerFactory.getLogger(IcvCreatorClient.class);
    private static final Logger audit = LoggerFactory.getLogger("AUDIT");

    /**
     * Constructor of the {@link IcvCreator} implementation that uses the Message Digest algorithm for creating ICV.
     * To specify a particular algorithm other than the default, set the default Message Digest algorithm in
     * the KmcCryptoManager prior to calling this constructor.
     *
     * @param cryptoManager The KmcCryptoManager for accessing the configuration parameters.
     * @throws KmcCryptoException if the default Message Digest algorithm is invalid.
     */
    public IcvCreatorClient(final KmcCryptoManager cryptoManager) throws KmcCryptoException {
        this.cryptoManager = cryptoManager;
        client = new CryptoServiceClient(cryptoManager);
        audit.info("IcvCreatorClient: User created ICV Creator that uses Crypto Service at "
                + cryptoManager.getKmcCryptoServiceURI());
    }

    /**
     * Constructor of the {@link IcvCreator} implementation that uses a crypto key for creating ICV.
     * The cryptographic key is retrieved from the KMC Key Management Service (KMS).
     * If the retrieved key is a symmetric key a Message Authentication Code (MAC)
     * will be created.  If the retrieved key is an asymmetric key a Digital Signature
     * will be created.
     *
     * The HMAC algorithm to be used for MAC generation is determined by the retrieved symmetric key.
     * However, the digital signature algorithm is not determined by the key.
     * To specify a particular algorithm other than the default, set the default digital signature
     * algorithm in the KmcCryptoManager prior to calling this constructor.
     *
     * @param cryptoManager The KmcCryptoManager for accessing the configuration parameters.
     * @param keyRef A string for identifying the key, i.e. the name of the key.
     * @throws KmcCryptoException if error in retrieving the key.
     */
    public IcvCreatorClient(final KmcCryptoManager cryptoManager, final String keyRef) throws KmcCryptoException {
        this.cryptoManager = cryptoManager;
        client = new CryptoServiceClient(cryptoManager);

        if (keyRef == null) {
            String msg = "Null keyRef.";
            logger.error(msg);
            throw new KmcCryptoException(KmcCryptoErrorCode.INVALID_INPUT_VALUE, msg, null);
        } else if ("null".equals(keyRef)) {
            this.keyRef = null;
        } else {
            this.keyRef = keyRef;
            keyInfo = client.getKeyInfo(cryptoManager, keyRef);
        }
        audit.info("IcvCreatorClient: User created ICV Creator that uses Crypto Service at "
                + cryptoManager.getKmcCryptoServiceURI());
    }

    @Override
    public final String createIntegrityCheckValue(final InputStream inputStream) throws KmcCryptoException {
        if (inputStream == null) {
            String msg = "Null input stream.";
            logger.error(msg);
            throw new KmcCryptoException(KmcCryptoErrorCode.INVALID_INPUT_VALUE, msg, null);
        }
        String cryptoServiceURI = cryptoManager.getKmcCryptoServiceURI();
        if (cryptoServiceURI == null) {
            String msg = KmcCryptoManager.CFG_CRYPTO_SERVICE_URI + " not found in "
                    + KmcCryptoManager.DEFAULT_CRYPTO_CONFIG_FILE;
            logger.error(msg);
            throw new KmcCryptoException(KmcCryptoErrorCode.INVALID_INPUT_VALUE, msg, null);
        }
        String algorithm;
        String uri;
        String response;
        try {
            if (keyRef == null) {
                // message digest
                algorithm = cryptoManager.getMessageDigestAlgorithm();
                uri = "icv-create?keyRef=null&algorithm=" + algorithm;
            } else if ("SymmetricKey".equals(keyInfo.getType())) {
                // CMAC or HMAC
                algorithm = keyInfo.getAlgorithm();
                if (algorithm.equals("AES") || algorithm.equals("DESede")) {
                    algorithm = algorithm + "CMAC";
                }
                uri = "icv-create?keyRef=" + keyRef;
            } else {
                // digital signature
                algorithm = cryptoManager.getDefaultDigitalSignatureAlgorithm();
                uri = "icv-create?keyRef=" + keyRef + "&algorithm=" + algorithm;
            }
            logger.info("URI = " + uri);
            response = client.executePost(uri, inputStream);
            logger.info("response = " + response);
        } catch (KmcCryptoException e) {
            logger.info("KmcCryptoException = " + e);
            throw e;
        } catch (IOException e) {
            String msg = "Exception in sending request: " + e;
            logger.error(msg);
            throw new KmcCryptoException(KmcCryptoErrorCode.KMS_CONNECTION_ERROR, msg, e);
        }

        IcvCreateServiceResponse res;
        Gson gson = new Gson();
        try {
            res = gson.fromJson(response, IcvCreateServiceResponse.class);
        } catch (JsonSyntaxException e) {
            String msg = "Exception parsing the JSON response: " + e + ".\nresponse = " + response;
            logger.error(msg);
            throw new KmcCryptoException(KmcCryptoErrorCode.CRYPTO_MISC_ERROR, msg, e);
        }
        Status status = res.getStatus();
        if (!status.isSuccess()) {
            audit.info("IcvCreatorClient: User failed to create ICV for data with Crypto Service");
            String reason = status.getReason();
            logger.error("HTTP code: " + status.getHttpCode() + ", " + reason);
            if (status.getHttpCode() == HttpServletResponse.SC_BAD_REQUEST) {
                if (reason.contains("does not exist")
                        || reason.contains("does not match")) {
                    throw new KmcCryptoException(KmcCryptoErrorCode.CRYPTO_KEY_ERROR, reason, null);
                } else if (reason.contains("algorithm is not allowed")
                        || reason.contains("Invalid Message Digest")
                        || reason.contains("Invalid Mac")
                        || reason.contains("Invalid Digital Signature")
                        || reason.contains("Invalid provider")) {
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
        audit.info("IcvCreatorClient: User created ICV for data with Crypto Service");
        return res.getMetadata();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadCryptoKey(String keyRef) throws KmcCryptoException {
        // Crypto service client does not use keys directly.
        String msg = "Crypto service client does not retrieve keys.";
        throw new KmcCryptoException(KmcCryptoErrorCode.CRYPTO_MISC_ERROR, msg, null);
    }

}
