package gov.nasa.jpl.ammos.kmc.crypto.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import gov.nasa.jpl.ammos.kmc.crypto.IcvVerifier;
import gov.nasa.jpl.ammos.kmc.crypto.KmcCryptoException;
import gov.nasa.jpl.ammos.kmc.crypto.KmcCryptoException.KmcCryptoErrorCode;
import gov.nasa.jpl.ammos.kmc.crypto.KmcCryptoManager;
import gov.nasa.jpl.ammos.kmc.crypto.model.IcvVerifyServiceResponse;
import gov.nasa.jpl.ammos.kmc.crypto.model.Status;

/**
 * This class implements {@link IcvVerifier} for verifying input data against the integrity check value.
 *
 */
public class IcvVerifierClient implements IcvVerifier {
    private final CryptoServiceClient client;

    private static final Logger logger = LoggerFactory.getLogger(IcvVerifierClient.class);
    private static final Logger audit = LoggerFactory.getLogger("AUDIT");

    /**
     * Constructor of the {@link IcvVerifier} implementation that does not need a crypto key
     * (for example, Message Digest algorithms are used)
     * or obtains the cryptographic key from the KMC Key Management Service (KMS).
     * The metadata, associated with the data to be verified, provides all the information needed
     * to perform the verification of the data.
     *
     * @param cryptoManager The KmcCryptoManager for accessing the configuration parameters.
     * @throws KmcCryptoException if error creating the CryptoServiceClient.
     */
    public IcvVerifierClient(final KmcCryptoManager cryptoManager) throws KmcCryptoException {
        this.client = new CryptoServiceClient(cryptoManager);
        audit.info("IcvVerifierClient: User created ICV Verifier that uses Crypto Service at "
                + cryptoManager.getKmcCryptoServiceURI());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean verifyIntegrityCheckValue(final InputStream inputStream, final String icvMetadata)
            throws KmcCryptoException {
        if (inputStream == null) {
            String msg = "Null input stream.";
            logger.error(msg);
            throw new KmcCryptoException(KmcCryptoErrorCode.INVALID_INPUT_VALUE, msg, null);
        }
        if (icvMetadata == null) {
            String msg = "Null metadata.";
            logger.error(msg);
            throw new KmcCryptoException(KmcCryptoErrorCode.INVALID_INPUT_VALUE, msg, null);
        }

        String response;
        try {
            String encodedMetadata = URLEncoder.encode(icvMetadata, "UTF-8");
            response = client.executePost("icv-verify?metadata=" + encodedMetadata, inputStream);
            //logger.info("response = " + response);
        } catch (KmcCryptoException e) {
            logger.info("KmcCryptoException = " + e);
            throw e;
        } catch (IOException e) {
            String msg = "Exception in sending request: " + e;
            logger.error(msg);
            throw new KmcCryptoException(KmcCryptoErrorCode.KMS_CONNECTION_ERROR, msg, e);
        }

        IcvVerifyServiceResponse res;
        Gson gson = new Gson();
        try {
            res = gson.fromJson(response, IcvVerifyServiceResponse.class);
        } catch (JsonSyntaxException e) {
            String msg = "Exception parsing the JSON response: " + e + ".\nresponse = " + response;
            logger.error(msg);
            throw new KmcCryptoException(KmcCryptoErrorCode.CRYPTO_MISC_ERROR, msg, e);
        }
        Status status = res.getStatus();
        if (!status.isSuccess()) {
            audit.info("IcvVerifierClient: User failed to verify data with ICV using Crypto Service.");
            String reason = status.getReason();
            logger.error("HTTP code: " + status.getHttpCode() + ", " + reason);
            if (status.getHttpCode() == HttpServletResponse.SC_BAD_REQUEST) {
                if (reason.contains("does not exist")
                        || reason.contains("not found in keystore")
                        || reason.contains("does not match")) {
                    throw new KmcCryptoException(KmcCryptoErrorCode.CRYPTO_KEY_ERROR, reason, null);
                } else if (reason.contains("algorithm is not allowed")
                        || reason.contains("Invalid algorithm")
                        || reason.contains("Invalid provider")
                        || reason.contains("Invalid Mac provider")) {
                    throw new KmcCryptoException(KmcCryptoErrorCode.CRYPTO_ALGORITHM_ERROR, reason, null);
                } else if (reason.contains("key state")
                        || reason.contains("key size")) {
                    throw new KmcCryptoException(KmcCryptoErrorCode.CRYPTO_KEY_ERROR, reason, null);
                } else if (reason.contains("initializing Mac")) {
                    throw new KmcCryptoException(KmcCryptoErrorCode.CRYPTO_KEY_ERROR, reason, null);
                } else if (reason.contains("metadata")) {
                    throw new KmcCryptoException(KmcCryptoErrorCode.CRYPTO_METADATA_ERROR, reason, null);
                } else {
                    throw new KmcCryptoException(KmcCryptoErrorCode.INVALID_INPUT_VALUE, reason, null);
                }
            } else if (status.getHttpCode() == HttpServletResponse.SC_INTERNAL_SERVER_ERROR) {
                /*
                if (reason.contains("Invalid metadata")) {
                    throw new KmcCryptoException(KmcCryptoErrorCode.CRYPTO_METADATA_ERROR, reason, null);
                } else {
                    throw new KmcCryptoException(KmcCryptoErrorCode.CRYPTO_MISC_ERROR, reason, null);
                }
                */
                throw new KmcCryptoException(KmcCryptoErrorCode.CRYPTO_MISC_ERROR, reason, null);
            } else {
                throw new KmcCryptoException(KmcCryptoErrorCode.KMS_CONNECTION_ERROR, reason, null);
            }
        }

        audit.info("IcvVerifierClient: User verified data with ICV using Crypto Service.  Verify result: " + res.getResult());
        logger.info("ICV verification result = " + res.getResult());
        return res.getResult();
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
